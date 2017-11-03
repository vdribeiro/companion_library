package companion.support.v8.os;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.widget.ImageView;

import companion.support.v8.graphics.GraphicsUtils;
import companion.support.v8.graphics.drawable.AsyncDrawable;
import companion.support.v8.graphics.drawable.BitmapDrawableRecycled;
import companion.support.v8.lang.ParsingUtils;
import companion.support.v8.net.NetworkUtils;
import companion.support.v8.util.DiskLruCache;
import companion.support.v8.util.ImageCache;
import companion.support.v8.util.LogHelper;

/**
 * This class wraps up completing long running work when loading a bitmap to an ImageView. 
 * It handles memory and disk cache usage, running the work in a background
 * thread, setting a placeholder image. It also resizes images from resources given a target width
 * and height. Useful for when the input images is too large to simply load directly into memory.
 * 
 * @author Vitor Ribeiro
 *
 */
public class ImageWorker {

	/** Log tag. */
	private static final String TAG = ImageWorker.class.getSimpleName();

	// Messages to deliver to the cache task 
	private static final int MESSAGE_CLEAR = 0;
	private static final int MESSAGE_INIT_DISK_CACHE = 1;
	private static final int MESSAGE_FLUSH = 2;
	private static final int MESSAGE_CLOSE = 3;

	// Image Cache
	private ImageCache mImageCache;
	private ImageCache.ImageCacheParams mImageCacheParams;

	protected Resources mResources;
	protected Bitmap mLoadingBitmap;
	protected int mImageWidth;
	protected int mImageHeight;
	private boolean mFadeInBitmap = true;

	// Disk Cache
	private DiskLruCache mDiskCache;
	private File mCacheDir;
	private boolean mDiskCacheStarting = true;
	private final Object mDiskCacheLock = new Object();

	public String cacheDir = ImageCache.DEFAULT_CACHE_DIR;
	public int diskCacheSize = ImageCache.DEFAULT_DISK_CACHE_SIZE;
	public int cacheIndex = ImageCache.DISK_CACHE_INDEX;

	// Tasks
	protected boolean mExitTasksEarly = false;
	protected boolean mPauseWork = false;
	private final Object mPauseWorkLock = new Object();

	/**
	 * Initialize providing a target image size;
	 *
	 * @param context the caller's context.
	 * @param imageWidth default image width.
	 * @param imageHeight default image height.
	 */
	public ImageWorker(Context context, int imageWidth, int imageHeight) {
		mResources = context.getResources();
		setImageSize(imageWidth, imageHeight);
		init(context);
	}

	/**
	 * Initialize providing all parameters;
	 *
	 * @param context the caller's context.
	 * @param imageWidth default image width.
	 * @param imageHeight default image height.
	 * @param cacheDir the directory where data is stored.
	 * @param diskCacheSize maximum cache size.
	 * @param cacheIndex starting index.
	 */
	public ImageWorker(Context context, int imageWidth, int imageHeight, String cacheDir, int diskCacheSize, int cacheIndex) {
		this(context, imageWidth, imageHeight);
		this.cacheDir = cacheDir;
		this.diskCacheSize = diskCacheSize;
		this.cacheIndex = cacheIndex;
	}

	/** 
	 * Initialize cache.
	 * 
	 * @param context the caller's context.
	 */
	private void init(Context context) {
		mCacheDir = new File(Storage.getAvailableCacheDir(context) + cacheDir);
	}

	/**
	 * Initialize cache.
	 */
	private void initDiskCache() {
		if (!mCacheDir.exists()) {
			mCacheDir.mkdirs();
		}
		synchronized (mDiskCacheLock) {
			if (Storage.getUsableSpace(mCacheDir) > diskCacheSize) {
				try {
					mDiskCache = DiskLruCache.open(mCacheDir, 1, 1, diskCacheSize);

					LogHelper.d(TAG, "cache initialized");

				} catch (IOException e) {
					mDiskCache = null;
				}
			}
			mDiskCacheStarting = false;
			mDiskCacheLock.notifyAll();
		}
	}

	/**
	 * Initialize cache.
	 */
	protected void initDiskCacheInternal() {
		if (mImageCache != null) {
			mImageCache.initDiskCache();
		}
		initDiskCache();
	}

	/**
	 * Adds an {@link ImageCache} to this {@link ImageWorker} to handle disk and memory bitmap
	 * caching.
	 * @param fragmentManager interface for interacting with Fragment objects inside of an Activity.
	 * @param cacheParams the cache parameters to use for the image cache.
	 */
	public void addImageCache(FragmentManager fragmentManager,	ImageCache.ImageCacheParams cacheParams) {
		mImageCacheParams = cacheParams;
		mImageCache = ImageCache.getInstance(fragmentManager, mImageCacheParams);
		new CacheAsyncTask().execute(MESSAGE_INIT_DISK_CACHE);
	}

	/**
	 * Adds an {@link ImageCache} to this {@link ImageWorker} to handle disk and memory bitmap
	 * caching.
	 * @param activity base class for activities that use the support-based Fragment and Loader APIs.
	 * @param diskCacheDirectoryName see {@link ImageCache.ImageCacheParams#ImageCacheParams(Context, String)}.
	 */
	public void addImageCache(FragmentActivity activity, String diskCacheDirectoryName) {
		mImageCacheParams = new ImageCache.ImageCacheParams(activity, diskCacheDirectoryName);
		mImageCache = ImageCache.getInstance(activity.getSupportFragmentManager(), mImageCacheParams);
		new CacheAsyncTask().execute(MESSAGE_INIT_DISK_CACHE);
	}

	/**
	 * @return the {@link ImageCache} object currently being used by this ImageWorker.
	 */
	protected ImageCache getImageCache() {
		return mImageCache;
	}

	/** 
	 * Load an image specified by the data parameter into an ImageView. A memory and
	 * disk cache will be used if an {@link ImageCache} has been added using
	 * {@link ImageWorker#addImageCache(FragmentManager, ImageCache.ImageCacheParams)}. If the
	 * image is found in the memory cache, it is set immediately, otherwise an {@link AsyncTaskCompat}
	 * will be created to asynchronously load the bitmap.
	 * 
	 * <p>
	 * 
	 * TODO - This class needs a better implementation about the alternative.
	 *
	 * @param key the resource to identify which image to process.
	 * @param imageView to bind the image to.
	 * @param obj the alternative to fetch the image if it does not exist in cache.
	 */
	public void loadImage(String key, ImageView imageView, Object obj) {
		if (key == null) {
			return;
		}

		BitmapDrawable value = null;

		if (mImageCache != null) {
			value = mImageCache.getBitmapFromMemCache(key);
		}

		if (value != null) {
			// Bitmap found in memory cache
			imageView.setImageDrawable(value);
		} else if (cancelPotentialWork(key, imageView)) {
			final CacheBitmapWorkerTask task = new CacheBitmapWorkerTask(imageView);
			final AsyncDrawable asyncDrawable = new AsyncDrawable(mResources, mLoadingBitmap, task);
			imageView.setImageDrawable(asyncDrawable);

			// NOTE: This uses a custom version of AsyncTask that has been pulled from the
			// framework and slightly modified. Refer to the docs at the top of the class
			// for more info on what was changed.
			task.executeOnExecutor(AsyncTaskCompat.DUAL_THREAD_EXECUTOR, key, obj);
		}
	}

	/**
	 * Set the target image width and height.
	 *
	 * @param width target image width.
	 * @param height target image height.
	 */
	public void setImageSize(int width, int height) {
		mImageWidth = width;
		mImageHeight = height;
	}

	/**
	 * Set placeholder bitmap that shows when the the background thread is running.
	 *
	 * @param bitmap target bitmap.
	 */
	public void setLoadingImage(Bitmap bitmap) {
		mLoadingBitmap = bitmap;
	}

	/**
	 * Set placeholder bitmap that shows when the the background thread is running.
	 *
	 * @param resId target bitmap resource Id.
	 */
	public void setLoadingImage(int resId) {
		mLoadingBitmap = BitmapFactory.decodeResource(mResources, resId);
	}

	/**
	 * If set to true, the image will fade-in once it has been loaded by the background thread.
	 */
	public void setImageFadeIn(boolean fadeIn) {
		mFadeInBitmap = fadeIn;
	}

	public void setExitTasksEarly(boolean exitTasksEarly) {
		mExitTasksEarly = exitTasksEarly;
		setPauseWork(false);
	}

	/**
	 * Pause any ongoing background work. This can be used as a temporary
	 * measure to improve performance. For example background work could
	 * be paused when a ListView or GridView is being scrolled using a
	 * {@link android.widget.AbsListView.OnScrollListener} to keep
	 * scrolling smooth.
	 * <p>
	 * If work is paused, be sure setPauseWork(false) is called again
	 * before your fragment or activity is destroyed (for example during
	 * {@link android.app.Activity#onPause()}), or there is a risk the
	 * background thread will never finish.
	 */
	public void setPauseWork(boolean pauseWork) {
		synchronized (mPauseWorkLock) {
			mPauseWork = pauseWork;
			if (!mPauseWork) {
				mPauseWorkLock.notifyAll();
			}
		}
	}

	/**
	 * The main processing method. This happens in a background task.
	 *
	 * @param object to identify which image to process.
	 * @return the processed bitmap.
	 */
	private Bitmap processBitmap(Object object) {
		if (object == null) {
			return null;
		}

		Bitmap bitmap = null;

		LogHelper.d(TAG, "processBitmap - " + object);

		if (object instanceof Integer) {
			bitmap = processBitmap((Integer) object);
		} else if (object instanceof String) {
			bitmap = processBitmap((String) object);
		} else if (object instanceof byte[]) {
			bitmap = processBitmap((byte[]) object);
		}

		return bitmap;
	}

	/**
	 * The main processing method. This happens in a background task.
	 *
	 * @param object the Resource ID Integer.
	 * @return the processed bitmap.
	 */
	private Bitmap processBitmap(Integer object) {
		Bitmap bitmap = null;

		try {
			bitmap = decodeSampledBitmapFromResource(mResources, object, mImageWidth, mImageHeight, getImageCache());
		} catch (Exception e) {
			LogHelper.e(TAG, "processBitmap - cannot process bitmap");
		}

		return bitmap;
	}

	/**
	 * The main processing method. This happens in a background task.
	 *
	 * @param object the String URL.
	 * @return the processed bitmap.
	 */
	private Bitmap processBitmap(String object) {
		Bitmap bitmap = null;

		try {
			final String key = DiskLruCache.hashKeyForDisk(object);
			FileDescriptor fileDescriptor = null;
			FileInputStream fileInputStream = null;
			DiskLruCache.Snapshot snapshot;
			synchronized (mDiskCacheLock) {
				// Wait for disk cache to initialize
				while (mDiskCacheStarting) {
					try {
						mDiskCacheLock.wait();
					} catch (InterruptedException e) {
						// Ignore
					}
				}

				if (mDiskCache != null) {
					try {
						snapshot = mDiskCache.get(key);
						if (snapshot == null) {
							LogHelper.d(TAG, "processBitmap - not found in cache, downloading...");

							DiskLruCache.Editor editor = mDiskCache.edit(key);
							if (editor != null) {
								if (NetworkUtils.downloadUrlToStream(object, editor.newOutputStream(cacheIndex))) {
									editor.commit();
								} else {
									editor.abort();
								}
							}
							snapshot = mDiskCache.get(key);
						}
						if (snapshot != null) {
							fileInputStream = (FileInputStream) snapshot.getInputStream(cacheIndex);
							fileDescriptor = fileInputStream.getFD();
						}
					} catch (IOException e) {

						LogHelper.e(TAG, "processBitmap - " + e);

					} catch (IllegalStateException e) {

						LogHelper.e(TAG, "processBitmap - " + e);

					} finally {
						if (fileDescriptor == null && fileInputStream != null) {
							try {
								fileInputStream.close();
							} catch (IOException e) {
								// Ignore
							}
						}
					}
				}
			}

			if (fileDescriptor != null) {
				bitmap = decodeSampledBitmapFromDescriptor(fileDescriptor, mImageWidth, mImageHeight, getImageCache());
			}
			if (fileInputStream != null) {
				try {
					fileInputStream.close();
				} catch (IOException e) {
					// Ignore
				}
			}
		} catch (Exception e) {
			LogHelper.e(TAG, "processBitmap - cannot process bitmap");
		}

		return bitmap;
	}

	/**
	 * The main processing method. This happens in a background task.
	 *
	 * @param object the byte array.
	 * @return the processed bitmap.
	 */
	private Bitmap processBitmap(byte[] object) {
		Bitmap bitmap = null;

		try {
			String byteArrayString = ParsingUtils.bytesToBase64(object);

			LogHelper.d(TAG, "processBitmap - " + byteArrayString);

			final String key = DiskLruCache.hashKeyForDisk(byteArrayString);
			FileDescriptor fileDescriptor = null;
			FileInputStream fileInputStream = null;
			DiskLruCache.Snapshot snapshot;
			synchronized (mDiskCacheLock) {
				// Wait for disk cache to initialize
				while (mDiskCacheStarting) {
					try {
						mDiskCacheLock.wait();
					} catch (InterruptedException e) {
						// Ignore
					}
				}

				if (mDiskCache != null) {
					try {
						snapshot = mDiskCache.get(key);
						if (snapshot == null) {

							LogHelper.d(TAG, "processBitmap - not found in cache, requesting...");

							DiskLruCache.Editor editor = mDiskCache.edit(key);
							if (editor != null) {
								OutputStream outputStream = editor.newOutputStream(cacheIndex);
								BufferedOutputStream out = new BufferedOutputStream(outputStream);
								try {
									out.write(object);
									editor.commit();
								} catch (Exception e) {
									editor.abort();
								} finally {
									out.close();
								}
							}
							snapshot = mDiskCache.get(key);
						}
						if (snapshot != null) {
							fileInputStream = (FileInputStream) snapshot.getInputStream(cacheIndex);
							fileDescriptor = fileInputStream.getFD();
						}
					} catch (IOException e) {
						LogHelper.e(TAG, "processBitmap - " + e);
					} catch (IllegalStateException e) {
						LogHelper.e(TAG, "processBitmap - " + e);
					} finally {
						if (fileDescriptor == null && fileInputStream != null) {
							try {
								fileInputStream.close();
							} catch (IOException e) {
								// Ignore
							}
						}
					}
				}
			}

			if (fileDescriptor != null) {
				bitmap = decodeSampledBitmapFromDescriptor(fileDescriptor, mImageWidth, mImageHeight, getImageCache());
			}
			if (fileInputStream != null) {
				try {
					fileInputStream.close();
				} catch (IOException e) {
					// Ignore
				}
			}

		} catch (Exception e) {
			LogHelper.e(TAG, "processBitmap - cannot process bitmap");
		}

		return bitmap;
	}

	protected void clearCacheInternal() {
		if (mImageCache != null) {
			mImageCache.clearCache();
		}
		synchronized (mDiskCacheLock) {
			if (mDiskCache != null && !mDiskCache.isClosed()) {
				try {
					mDiskCache.delete();

					LogHelper.d(TAG, "cache cleared");
				} catch (IOException e) {
					LogHelper.e(TAG, "clearCacheInternal - " + e);
				}
				mDiskCache = null;
				mDiskCacheStarting = true;
				initDiskCache();
			}
		}
	}

	public void clearCache() {
		new CacheAsyncTask().execute(MESSAGE_CLEAR);
	}

	protected void flushCacheInternal() {
		if (mImageCache != null) {
			mImageCache.flush();
		}
		synchronized (mDiskCacheLock) {
			if (mDiskCache != null) {
				try {
					mDiskCache.flush();

					LogHelper.d(TAG, "cache flushed");
				} catch (IOException e) {
					LogHelper.e(TAG, "flush - " + e);
				}
			}
		}
	}

	public void flushCache() {
		new CacheAsyncTask().execute(MESSAGE_FLUSH);
	}

	protected void closeCacheInternal() {
		if (mImageCache != null) {
			mImageCache.close();
			mImageCache = null;
		}
		synchronized (mDiskCacheLock) {
			if (mDiskCache != null) {
				try {
					if (!mDiskCache.isClosed()) {
						mDiskCache.close();
						mDiskCache = null;

						LogHelper.d(TAG, "cache closed");
					}
				} catch (IOException e) {
					LogHelper.e(TAG, "closeCacheInternal - " + e);
				}
			}
		}
	}

	public void closeCache() {
		new CacheAsyncTask().execute(MESSAGE_CLOSE);
	}
	
	/**
	 * Decode and sample down a bitmap from resources to the requested width and height.
	 *
	 * @param res the resources object containing the image data.
	 * @param resId the resource id of the image data.
	 * @param reqWidth the requested width of the resulting bitmap.
	 * @param reqHeight the requested height of the resulting bitmap.
	 * @param cache the ImageCache used to find candidate bitmaps for use with inBitmap.
	 * @return a bitmap sampled down from the original with the same aspect ratio and dimensions
	 *         that are equal to or greater than the requested width and height.
	 */
	@SuppressLint("NewApi")
	public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId, int reqWidth, int reqHeight, ImageCache cache) {
		
		final BitmapFactory.Options options = GraphicsUtils.getBitmapOptions(res, resId, reqWidth, reqHeight, null);
		if (cache != null) {
			// Try and find a bitmap to use for inBitmap
			Bitmap inBitmap = cache.getBitmapFromReusableSet(options);
			if (inBitmap != null) {
				options.inBitmap = inBitmap;
			}
		}
		
		return BitmapFactory.decodeResource(res, resId, options);
	}

	/**
	 * Decode and sample down a bitmap from a file input stream to the requested width and height.
	 *
	 * @param fileDescriptor the file descriptor to read from.
	 * @param reqWidth the requested width of the resulting bitmap.
	 * @param reqHeight the requested height of the resulting bitmap.
	 * @param cache the ImageCache used to find candidate bitmaps for use with inBitmap.
	 * @return a bitmap sampled down from the original with the same aspect ratio and dimensions
	 *         that are equal to or greater than the requested width and height.
	 */
	@SuppressLint("NewApi")
	public static Bitmap decodeSampledBitmapFromDescriptor(FileDescriptor fileDescriptor, int reqWidth, int reqHeight, ImageCache cache) {
		
		final BitmapFactory.Options options = GraphicsUtils.getBitmapOptions(fileDescriptor, reqWidth, reqHeight, null);
		if (cache != null) {
			// Try and find a bitmap to use for inBitmap
			Bitmap inBitmap = cache.getBitmapFromReusableSet(options);
			if (inBitmap != null) {
				options.inBitmap = inBitmap;
			}
		}

		return BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);
	}

	/**
	 * Cancels any pending work attached to the provided ImageView.
	 * @param imageView target ImageView.
	 */
	public static void cancelWork(ImageView imageView) {
		final CacheBitmapWorkerTask bitmapWorkerTask = (CacheBitmapWorkerTask) BitmapWorkerTask.getBitmapWorkerTask(imageView);
		if (bitmapWorkerTask != null) {
			bitmapWorkerTask.cancel(true);

			LogHelper.d(TAG, "cancelWork - cancelled work for " + bitmapWorkerTask.key);
		}
	}

	/**
	 * @param key the resource to identify which image to process.
	 * @param imageView to bind the image to.
	 * @return true if the current work has been canceled or if there was no work in
	 * progress on this image view, or false if the work in progress deals with the same data. The work is not
	 * stopped in that case.
	 */
	public static boolean cancelPotentialWork(String key, ImageView imageView) {
		final CacheBitmapWorkerTask bitmapWorkerTask = (CacheBitmapWorkerTask) BitmapWorkerTask.getBitmapWorkerTask(imageView);

		if (bitmapWorkerTask != null) {
			final String bitmapKey = bitmapWorkerTask.key;
			if (bitmapKey == null || !bitmapKey.equals(key)) {
				bitmapWorkerTask.cancel(true);

				LogHelper.d(TAG, "cancelPotentialWork - cancelled work for " + key);
			} else {
				// The same work is already in progress.
				return false;
			}
		}
		return true;
	}

	protected class CacheAsyncTask extends AsyncTaskCompat<Object, Void, Void> {

		@Override
		protected Void doInBackground(Object... params) {
			switch ((Integer)params[0]) {
			case MESSAGE_CLEAR:
				clearCacheInternal();
				break;
			case MESSAGE_INIT_DISK_CACHE:
				initDiskCacheInternal();
				break;
			case MESSAGE_FLUSH:
				flushCacheInternal();
				break;
			case MESSAGE_CLOSE:
				closeCacheInternal();
				break;
			}
			return null;
		}
	}

	/**
	 * The actual AsyncTask that will asynchronously process the image.
	 */
	private class CacheBitmapWorkerTask extends BitmapWorkerTask {
		private String key;

		public CacheBitmapWorkerTask(ImageView imageView) {
			super(ImageWorker.this.mResources, ImageWorker.this.mLoadingBitmap, imageView, ImageWorker.this.mFadeInBitmap);
		}

		/**
		 * Background processing.
		 */
		@Override
		protected BitmapDrawable doInBackground(Object... params) {

			LogHelper.d(TAG, "doInBackground - starting work");
			
			try {
				key = (String) params[0];	
			} catch (Exception e) {
				LogHelper.e(TAG, "doInBackground - invalid identifier: " + e);
			}

			Bitmap bitmap = null;
			BitmapDrawable drawable = null;

			// Wait here if work is paused and the task is not cancelled
			synchronized (mPauseWorkLock) {
				while (mPauseWork && !isCancelled()) {
					try {
						mPauseWorkLock.wait();
					} catch (InterruptedException e) {
						// Ignore
					}
				}
			}

			// If the image cache is available and this task has not been cancelled by another
			// thread and the ImageView that was originally bound to this task is still bound back
			// to this task and our "exit early" flag is not set then try and fetch the bitmap from
			// the cache
			if (mImageCache != null && !isCancelled() && getAttachedImageView() != null && !mExitTasksEarly) {
				bitmap = mImageCache.getBitmapFromDiskCache(key);
			}

			// If the bitmap was not found in the cache and this task has not been cancelled by
			// another thread and the ImageView that was originally bound to this task is still
			// bound back to this task and our "exit early" flag is not set, then call the main
			// process method (as implemented by a subclass)
			if (bitmap == null && !isCancelled() && getAttachedImageView() != null && !mExitTasksEarly) {
				bitmap = processBitmap(params[1]);
			}

			// If the bitmap was processed and the image cache is available, then add the processed
			// bitmap to the cache for future use. Note we don't check if the task was cancelled
			// here, if it was, and the thread is still running, we may as well add the processed
			// bitmap to our cache as it might be used again in the future
			if (bitmap != null) {
				if (Utils.hasHoneycomb()) {
					// Running on Honeycomb or newer, so wrap in a standard BitmapDrawable
					drawable = new BitmapDrawable(mResources, bitmap);
				} else {
					// Running on Gingerbread or older, so wrap in a RecyclingBitmapDrawable
					drawable = new BitmapDrawableRecycled(mResources, bitmap);
				}

				if (mImageCache != null) {
					mImageCache.addBitmapToCache(key, drawable);
				}
			}

			LogHelper.d(TAG, "doInBackground - finished work");
			return drawable;
		}

		@Override
		protected void onProgressUpdate(Object... progress) {
			super.onProgressUpdate(progress);
			//setProgressPercent(progress[0]);
		}

		@Override
		protected void onPostExecute(Object value) {
			// if cancel was called on this task or the "exit early" flag is set then we're done
			if (isCancelled() || mExitTasksEarly) {
				value = null;
			}

			// Once the image is processed, associates it to the imageView.
			super.onPostExecute(value);
		}

		@Override
		protected void onCancelled(Object value) {
			super.onCancelled(value);
			synchronized (mPauseWorkLock) {
				mPauseWorkLock.notifyAll();
			}
		}
	}
}
