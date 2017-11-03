package companion.support.v8.os;

import java.lang.ref.WeakReference;

import android.annotation.TargetApi;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Build;
import android.widget.ImageView;

import companion.support.v8.graphics.drawable.AsyncDrawable;

public abstract class BitmapWorkerTask extends AsyncTaskCompat<Object, Object, Object> {
	
	protected static final String TAG = BitmapWorkerTask.class.getSimpleName();

	// The length of the image transition in milliseconds, if enabled
	public static int FADE_IN_TIME = 300;

	protected final WeakReference<ImageView> imageViewReference;
	protected boolean mFadeInBitmap = true;
	protected Resources mResources;
	protected Bitmap mLoadingBitmap;

	/**
	 * @param resources to access.
	 * @param loadingBitmap
	 * @param imageView
	 * @param fadeIn sets if the image will fade-in once it has been loaded by the background thread.
	 */
	public BitmapWorkerTask(Resources resources, Bitmap loadingBitmap, ImageView imageView, boolean fadeIn) {
		// Use a WeakReference to ensure the ImageView can be garbage collected
		imageViewReference = new WeakReference<ImageView>(imageView);
		mFadeInBitmap = fadeIn;
		mResources = resources;
		mLoadingBitmap = loadingBitmap;
	}

	// Once complete, see if ImageView is still around and set the image Object.
	@SuppressWarnings("deprecation")
	@Override
	protected void onPostExecute(Object image) {
		super.onPostExecute(image);

		ImageView imageView = null;

		if (imageViewReference != null && image != null) {
			// Once the image is processed, associates it to the imageView.
			imageView = getAttachedImageView();
			if (imageView != null) {
				Drawable drawable = null;
				if (image instanceof Drawable) {
					drawable = (Drawable) image;	
				} else if (image instanceof Bitmap) {
					drawable = new BitmapDrawable(mResources, (Bitmap) image);
				} else if (image instanceof Integer) {
					drawable = mResources.getDrawable((Integer) image);
				}
				setImageDrawable(imageView, drawable);
			}
		}
	}

	/**
	 * Returns the ImageView associated with this task as long as the ImageView's task still
	 * points to this task as well. Returns null otherwise.
	 */
	public ImageView getAttachedImageView() {
		final ImageView imageView = imageViewReference.get();
		final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

		if (this == bitmapWorkerTask) {
			return imageView;
		}

		return null;
	}

	/**
	 * Called when the processing is complete and the final drawable should be 
	 * set on the ImageView.
	 *
	 * @param imageView placeholder ImageView.
	 * @param drawable placeholder Drawable.
	 */
	@SuppressWarnings("deprecation")
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private void setImageDrawable(ImageView imageView, Drawable drawable) {
		if (mFadeInBitmap) {
			// Transition drawable with a transparent drawable and the final drawable
			final TransitionDrawable td = new TransitionDrawable(
				new Drawable[] {
					new ColorDrawable(android.R.color.transparent), drawable
				}
			);

			// Set background to loading bitmap
			BitmapDrawable bitmap = new BitmapDrawable(mResources, mLoadingBitmap);
			if (Utils.hasJellyBean()) {
				imageView.setBackground(bitmap);
			} else {
				imageView.setBackgroundDrawable(bitmap);
			}

			imageView.setImageDrawable(td);
			td.startTransition(FADE_IN_TIME);
		} else {
			imageView.setImageDrawable(drawable);
		}
	}

	/** Get the work task associated with the imageView.
	 * @param imageView any imageView.
	 * @return the currently active work task (if any) associated with this imageView, or null if there is no such task.
	 */
	public static BitmapWorkerTask getBitmapWorkerTask(ImageView imageView) {
		if (imageView != null) {
			final Drawable drawable = imageView.getDrawable();
			if (drawable instanceof AsyncDrawable) {
				final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
				return asyncDrawable.getBitmapWorkerTask();
			}
		}
		return null;
	}
}
