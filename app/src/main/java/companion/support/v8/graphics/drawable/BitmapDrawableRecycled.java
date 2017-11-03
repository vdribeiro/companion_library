package companion.support.v8.graphics.drawable;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;

import companion.support.v8.util.LogHelper;

/**
 * A BitmapDrawable that keeps track of whether it is being displayed or cached.
 * When the drawable is no longer being displayed or cached,
 * {@link Bitmap#recycle() recycle()} will be called on this drawable's bitmap.
 * 
 * @author Vitor Ribeiro
 * 
 */
public class BitmapDrawableRecycled extends BitmapDrawable {

	/** Log tag. */
	private static final String TAG = BitmapDrawableRecycled.class.getSimpleName();

	private int mCacheRefCount = 0;
	private int mDisplayRefCount = 0;

	private boolean mHasBeenDisplayed;

	public BitmapDrawableRecycled(Resources res, Bitmap bitmap) {
		super(res, bitmap);
	}

	/**
	 * Notify the drawable that the displayed state has changed. Internally a
	 * count is kept so that the drawable knows when it is no longer being
	 * displayed.
	 *
	 * @param isDisplayed Whether the drawable is being displayed or not
	 */
	public void setIsDisplayed(boolean isDisplayed) {
		synchronized (this) {
			if (isDisplayed) {
				mDisplayRefCount++;
				mHasBeenDisplayed = true;
			} else {
				mDisplayRefCount--;
			}
		}

		// Check to see if recycle() can be called
		checkState();
	}

	/**
	 * Notify the drawable that the cache state has changed. Internally a count
	 * is kept so that the drawable knows when it is no longer being cached.
	 *
	 * @param isCached Whether the drawable is being cached or not
	 */
	public void setIsCached(boolean isCached) {
		synchronized (this) {
			if (isCached) {
				mCacheRefCount++;
			} else {
				mCacheRefCount--;
			}
		}

		// Check to see if recycle() can be called
		checkState();
	}

	private synchronized void checkState() {
		// If the drawable cache and display ref counts = 0, and this drawable
		// has been displayed, then recycle
		if (mCacheRefCount <= 0 && mDisplayRefCount <= 0 && mHasBeenDisplayed && hasValidBitmap()) {
			LogHelper.d(TAG, "No longer being used or cached so recycling. " + toString());
			getBitmap().recycle();
		}
	}

	private synchronized boolean hasValidBitmap() {
		Bitmap bitmap = getBitmap();
		return bitmap != null && !bitmap.isRecycled();
	}

}
