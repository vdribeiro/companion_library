package companion.support.v8.graphics.drawable;

import java.lang.ref.WeakReference;

import companion.support.v8.os.BitmapWorkerTask;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;

/**
 * A custom Drawable that will be attached to the ImageView while the work is in progress.
 * Contains a reference to the actual worker task, so that it can be stopped if a new binding is
 * required, and makes sure that only the last started worker process can bind its result,
 * independently of the finish order.
 */
public class AsyncDrawable extends BitmapDrawable {
	private final WeakReference<BitmapWorkerTask> bitmapWorkerTaskReference;

	public AsyncDrawable(Resources res, Bitmap bitmap, BitmapWorkerTask bitmapWorkerTask) {
		super(res, bitmap);
		bitmapWorkerTaskReference =	new WeakReference<BitmapWorkerTask>(bitmapWorkerTask);
	}

	public BitmapWorkerTask getBitmapWorkerTask() {
		return bitmapWorkerTaskReference.get();
	}
}
