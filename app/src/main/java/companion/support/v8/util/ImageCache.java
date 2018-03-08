package companion.support.v8.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.app.FragmentManager;
import android.support.v4.util.LruCache;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.SoftReference;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import companion.support.v8.app.RetainFragment;
import companion.support.v8.graphics.ImageUtils;
import companion.support.v8.graphics.drawable.BitmapDrawableRecycled;
import companion.support.v8.os.ImageWorker;
import companion.support.v8.os.Storage;
import companion.support.v8.os.Utils;

/**
 * This class handles disk and memory caching of bitmaps in conjunction with the
 * {@link ImageWorker} class and its subclasses. Use
 * {@link ImageCache#getInstance(FragmentManager, ImageCacheParams)} to get an instance of this
 * class, although usually a cache should be added directly to an {@link ImageWorker} by calling
 * {@link ImageWorker#addImageCache(FragmentManager, ImageCacheParams)}.
 * 
 * @author Vitor Ribeiro
 *
 */
public class ImageCache {

	/** Log tag. */
	public static final String LOG = "ImageCache";

	// Constants to easily toggle various caches
	public static final boolean DEFAULT_MEM_CACHE_ENABLED = true;
	public static final boolean DEFAULT_DISK_CACHE_ENABLED = true;
	public static final boolean DEFAULT_INIT_DISK_CACHE_ON_CREATE = false;

	// Default memory cache size
	public static final int DEFAULT_MEM_CACHE_SIZE = 1024 * 1024 * 5;

	// Default disk cache size
	public static final int DEFAULT_DISK_CACHE_SIZE = 1024 * 1024 * 10;

	// Disk cache directory
	public static final String DEFAULT_CACHE_DIR = "thumbs";

	// Compression settings when writing images to disk cache
	public static final CompressFormat DEFAULT_COMPRESS_FORMAT = CompressFormat.JPEG;
	public static final int DEFAULT_COMPRESS_QUALITY = 70;
	public static final int DISK_CACHE_INDEX = 0;

	private LruCache<String, BitmapDrawable> mMemoryCache;
	private ImageCacheParams mCacheParams;
	private DiskLruCache mDiskLruCache;
	private final Object mDiskCacheLock = new Object();
	private boolean mDiskCacheStarting = true;

	private Set<SoftReference<Bitmap>> mReusableBitmaps;

	/**
	 * Create a new ImageCache object using the specified parameters. This should not be
	 * called directly by other classes, instead use
	 * {@link ImageCache#getInstance(FragmentManager, ImageCacheParams)} to fetch an ImageCache
	 * instance.
	 *
	 * @param cacheParams The cache parameters to use to initialize the cache
	 */
	private ImageCache(ImageCacheParams cacheParams) {
		init(cacheParams);
	}

	/**
	 * Return an {@link ImageCache} instance. A {@link RetainFragment} is used to retain the
	 * ImageCache object across configuration changes such as a change in device orientation.
	 *
	 * @param fragmentManager The fragment manager to use when dealing with the retained fragment.
	 * @param cacheParams The cache parameters to use if the ImageCache needs instantiation.
	 * @return An existing retained ImageCache object or a new one if one did not exist
	 */
	public static ImageCache getInstance(FragmentManager fragmentManager, ImageCacheParams cacheParams) {

		// Search for, or create an instance of the non-UI RetainFragment
		final RetainFragment mRetainFragment = RetainFragment.findOrCreateRetainFragment(fragmentManager, LOG);

		// See if we already have an ImageCache stored in RetainFragment
		ImageCache imageCache = (ImageCache) mRetainFragment.getObject();

		// No existing ImageCache, create one and store it in RetainFragment
		if (imageCache == null) {
			imageCache = new ImageCache(cacheParams);
			mRetainFragment.setObject(imageCache);
		}

		return imageCache;
	}

	/**
	 * Initialize the cache, providing all parameters.
	 *
	 * @param cacheParams The cache parameters to initialize the cache
	 */
	private void init(ImageCacheParams cacheParams) {
		mCacheParams = cacheParams;

		// Set up memory cache
		if (mCacheParams.memoryCacheEnabled) {

			LogHelper.d(LOG, "Memory cache created (size = " + mCacheParams.memCacheSize + ")");

			// If we're running on Honeycomb or newer, create a set of reusable bitmaps that can be
			// populated into the inBitmap field of BitmapFactory.Options. Note that the set is
			// of SoftReferences which will actually not be very effective due to the garbage
			// collector being aggressive clearing Soft/WeakReferences. A better approach
			// would be to use a strongly references bitmaps, however this would require some
			// balancing of memory usage between this set and the bitmap LruCache. It would also
			// require knowledge of the expected size of the bitmaps. From Honeycomb to JellyBean
			// the size would need to be precise, from KitKat onward the size would just need to
			// be the upper bound (due to changes in how inBitmap can re-use bitmaps).
			if (Utils.hasHoneycomb()) {
				mReusableBitmaps = Collections.synchronizedSet(new HashSet<SoftReference<Bitmap>>());
			}

			mMemoryCache = new LruCache<String, BitmapDrawable>(mCacheParams.memCacheSize) {

				/**
				 * Notify the removed entry that is no longer being cached
				 */
				@Override
				protected void entryRemoved(boolean evicted, String key, BitmapDrawable oldValue, BitmapDrawable newValue) {
					if (BitmapDrawableRecycled.class.isInstance(oldValue)) {
						// The removed entry is a recycling drawable, so notify it 
						// that it has been removed from the memory cache
						((BitmapDrawableRecycled) oldValue).setIsCached(false);
					} else {
						// The removed entry is a standard BitmapDrawable

						if (Utils.hasHoneycomb()) {
							// We're running on Honeycomb or later, so add the bitmap
							// to a SoftReference set for possible use with inBitmap later
							mReusableBitmaps.add(new SoftReference<Bitmap>(oldValue.getBitmap()));
						}
					}
				}

				/**
				 * Measure item size in kilobytes rather than units which is more practical
				 * for a bitmap cache
				 */
				@Override
				protected int sizeOf(String key, BitmapDrawable value) {
					final int bitmapSize = ImageUtils.getBitmapSize(value) / 1024;
					return bitmapSize == 0 ? 1 : bitmapSize;
				}
			};
		}

		// By default the disk cache is not initialized here as it should be initialized
		// on a separate thread due to disk access.
		if (cacheParams.initDiskCacheOnCreate) {
			// Set up disk cache
			initDiskCache();
		}
	}

	/**
	 * Initializes the disk cache. Note that this includes disk access so this should not be
	 * executed on the main/UI thread. By default an ImageCache does not initialize the disk
	 * cache when it is created, instead you should call initDiskCache() to initialize it on a
	 * background thread.
	 */
	public void initDiskCache() {
		// Set up disk cache
		synchronized (mDiskCacheLock) {
			if (mDiskLruCache == null || mDiskLruCache.isClosed()) {
				File diskCacheDir = mCacheParams.diskCacheDir;
				if (mCacheParams.diskCacheEnabled && diskCacheDir != null) {
					if (!diskCacheDir.exists()) {
						diskCacheDir.mkdirs();
					}
					if (Storage.getUsableSpace(diskCacheDir) > mCacheParams.diskCacheSize) {
						try {
							mDiskLruCache = DiskLruCache.open(diskCacheDir, 1, 1, mCacheParams.diskCacheSize);

							LogHelper.d(LOG, "Disk cache initialized");
						} catch (final IOException e) {
							mCacheParams.diskCacheDir = null;

							LogHelper.e(LOG, "initDiskCache - " + e);
						}
					}
				}
			}
			mDiskCacheStarting = false;
			mDiskCacheLock.notifyAll();
		}
	}

	/**
	 * Adds a bitmap to both memory and disk cache.
	 * @param key Unique identifier for the bitmap to store
	 * @param value The bitmap drawable to store
	 */
	public void addBitmapToCache(String key, BitmapDrawable value) {
		if (key == null || value == null) {
			return;
		}

		// Add to memory cache
		if (mMemoryCache != null) {
			if (BitmapDrawableRecycled.class.isInstance(value)) {
				// The removed entry is a recycling drawable, so notify it 
				// that it has been added into the memory cache
				((BitmapDrawableRecycled) value).setIsCached(true);
			}
			mMemoryCache.put(key, value);
		}

		synchronized (mDiskCacheLock) {
			// Add to disk cache
			if (mDiskLruCache != null) {
				final String hashKey = DiskLruCache.hashKeyForDisk(key);
				OutputStream out = null;
				try {
					DiskLruCache.Snapshot snapshot = mDiskLruCache.get(hashKey);
					if (snapshot == null) {
						final DiskLruCache.Editor editor = mDiskLruCache.edit(hashKey);
						if (editor != null) {
							out = editor.newOutputStream(DISK_CACHE_INDEX);
							value.getBitmap().compress(mCacheParams.compressFormat, mCacheParams.compressQuality, out);
							editor.commit();
							out.close();
						}
					} else {
						snapshot.getInputStream(DISK_CACHE_INDEX).close();
					}
				} catch (final IOException e) {
					LogHelper.e(LOG, "addBitmapToCache - " + e);
				} catch (Exception e) {
					LogHelper.e(LOG, "addBitmapToCache - " + e);
				} finally {
					try {
						if (out != null) {
							out.close();
						}
					} catch (IOException e) {
						// Ignore
					}
				}
			}
		}
	}

	/**
	 * Get from memory cache.
	 *
	 * @param key Unique identifier for which item to get
	 * @return The bitmap drawable if found in cache, null otherwise
	 */
	public BitmapDrawable getBitmapFromMemCache(String key) {
		BitmapDrawable memValue = null;

		if (mMemoryCache != null) {
			memValue = mMemoryCache.get(key);
		}

		if (memValue != null) {
			LogHelper.d(LOG, "Memory cache hit");
		}

		return memValue;
	}

	/**
	 * Get from disk cache.
	 *
	 * @param key Unique identifier for which item to get
	 * @return The bitmap if found in cache, null otherwise
	 */
	public Bitmap getBitmapFromDiskCache(String key) {
		final String hashKey = DiskLruCache.hashKeyForDisk(key);
		Bitmap bitmap = null;

		synchronized (mDiskCacheLock) {
			while (mDiskCacheStarting) {
				try {
					mDiskCacheLock.wait();
				} catch (InterruptedException e) {
					// Ignore
				}
			}
			if (mDiskLruCache != null) {
				InputStream inputStream = null;
				try {
					final DiskLruCache.Snapshot snapshot = mDiskLruCache.get(hashKey);
					if (snapshot != null) {
						LogHelper.d(LOG, "Disk cache hit");
						inputStream = snapshot.getInputStream(DISK_CACHE_INDEX);
						if (inputStream != null) {
							FileDescriptor fd = ((FileInputStream) inputStream).getFD();

							// Decode bitmap, but we don't want to sample so give
							// MAX_VALUE as the target dimensions
							bitmap = ImageWorker.decodeSampledBitmapFromDescriptor(fd, Integer.MAX_VALUE, Integer.MAX_VALUE, this);
						}
					}
				} catch (final IOException e) {
					LogHelper.e(LOG, "getBitmapFromDiskCache - " + e);
				} finally {
					try {
						if (inputStream != null) {
							inputStream.close();
						}
					} catch (IOException e) {
						// Ignore
					}
				}
			}
			return bitmap;
		}
	}

	/**
	 * @param options - BitmapFactory.Options with out* options populated
	 * @return Bitmap that case be used for inBitmap
	 */
	public Bitmap getBitmapFromReusableSet(BitmapFactory.Options options) {
		Bitmap bitmap = null;

		if (mReusableBitmaps != null && !mReusableBitmaps.isEmpty()) {
			synchronized (mReusableBitmaps) {
				final Iterator<SoftReference<Bitmap>> iterator = mReusableBitmaps.iterator();
				Bitmap item = null;

				while (iterator.hasNext()) {
					item = iterator.next().get();

					if (null != item && item.isMutable()) {
						// Check to see it the item can be used for inBitmap
						if (ImageUtils.canUseForInBitmap(item, options)) {
							bitmap = item;

							// Remove from reusable set so it can't be used again
							iterator.remove();
							break;
						}
					} else {
						// Remove from the set if the reference has been cleared.
						iterator.remove();
					}
				}
			}
		}

		return bitmap;
	}

	/**
	 * Clears both the memory and disk cache associated with this ImageCache object. Note that
	 * this includes disk access so this should not be executed on the main/UI thread.
	 */
	public void clearCache() {
		if (mMemoryCache != null) {
			mMemoryCache.evictAll();
			LogHelper.d(LOG, "Memory cache cleared");
		}

		synchronized (mDiskCacheLock) {
			mDiskCacheStarting = true;
			if (mDiskLruCache != null && !mDiskLruCache.isClosed()) {
				try {
					mDiskLruCache.delete();
					LogHelper.d(LOG, "Disk cache cleared");
				} catch (IOException e) {
					LogHelper.e(LOG, "clearCache - " + e);
				}
				mDiskLruCache = null;
				initDiskCache();
			}
		}
	}

	/**
	 * Flushes the disk cache associated with this ImageCache object. Note that this includes
	 * disk access so this should not be executed on the main/UI thread.
	 */
	public void flush() {
		synchronized (mDiskCacheLock) {
			if (mDiskLruCache != null) {
				try {
					mDiskLruCache.flush();
					LogHelper.d(LOG, "Disk cache flushed");
				} catch (IOException e) {
					LogHelper.e(LOG, "flush - " + e);
				}
			}
		}
	}

	/**
	 * Closes the disk cache associated with this ImageCache object. Note that this includes
	 * disk access so this should not be executed on the main/UI thread.
	 */
	public void close() {
		synchronized (mDiskCacheLock) {
			if (mDiskLruCache != null) {
				try {
					if (!mDiskLruCache.isClosed()) {
						mDiskLruCache.close();
						mDiskLruCache = null;
						LogHelper.d(LOG, "Disk cache closed");
					}
				} catch (IOException e) {
					LogHelper.e(LOG, "close - " + e);
				}
			}
		}
	}

	/**
	 * A holder class that contains cache parameters.
	 */
	public static class ImageCacheParams {
		public int memCacheSize = DEFAULT_MEM_CACHE_SIZE;
		public int diskCacheSize = DEFAULT_DISK_CACHE_SIZE;
		public File diskCacheDir;

		public CompressFormat compressFormat = DEFAULT_COMPRESS_FORMAT;
		public int compressQuality = DEFAULT_COMPRESS_QUALITY;
		public boolean memoryCacheEnabled = DEFAULT_MEM_CACHE_ENABLED;
		public boolean diskCacheEnabled = DEFAULT_DISK_CACHE_ENABLED;
		public boolean initDiskCacheOnCreate = DEFAULT_INIT_DISK_CACHE_ON_CREATE;

		/**
		 * Create a set of image cache parameters that can be provided to
		 * {@link ImageCache#getInstance(FragmentManager, ImageCacheParams)} or
		 * {@link ImageWorker#addImageCache(FragmentManager, ImageCacheParams)}.
		 * @param context A context to use.
		 * @param diskCacheDirectoryName A unique subdirectory name that will be appended to the
		 *                               application cache directory. Usually "cache" or "images"
		 *                               is sufficient.
		 */
		public ImageCacheParams(Context context, String diskCacheDirectoryName) {
			diskCacheDir = new File(Storage.getAvailableCacheDir(context) + diskCacheDirectoryName);
		}

		/**
		 * Sets the memory cache size based on a percentage of the max available VM memory.
		 * Eg. setting percent to 0.2 would set the memory cache to one fifth of the available
		 * memory. Throws {@link IllegalArgumentException} if percent is < 0.01 or > .8.
		 * memCacheSize is stored in kilobytes instead of bytes as this will eventually be passed
		 * to construct a LruCache which takes an int in its constructor.
		 *
		 * This value should be chosen carefully based on a number of factors
		 * Refer to the corresponding Android Training class for more discussion:
		 * http://developer.android.com/training/displaying-bitmaps/
		 *
		 * @param percent Percent of available app memory to use to size memory cache
		 */
		public void setMemCacheSizePercent(float percent) {
			if (percent < 0.01f || percent > 0.8f) {
				throw new IllegalArgumentException("setMemCacheSizePercent - percent must be between 0.01 and 0.8 (inclusive)");
			}
			memCacheSize = Math.round(percent * Runtime.getRuntime().maxMemory() / 1024);
		}
	}
}
