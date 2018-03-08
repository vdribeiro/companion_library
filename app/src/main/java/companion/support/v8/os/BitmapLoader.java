package companion.support.v8.os;

import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.widget.ImageView;

import companion.support.v8.graphics.ImageUtils;
import companion.support.v8.util.LogHelper;

/**
 * Set of methods to optimize bitmap memory allocation.
 * 
 * @author Vitor Ribeiro
 * @author Jose Silva
 *
 */
public class BitmapLoader extends BitmapWorkerTask {

	private byte[] data;

	public BitmapLoader(Resources resources, ImageView imageView, byte[] data, boolean animate) {
		super(resources, null, imageView, animate);

		this.data = data;
	}

	public BitmapLoader(Resources resources, ImageView imageView, byte[] data) {
		this(resources, imageView, data, true);
	}

	@Override
	protected Object doInBackground(Object... params) {
		int reqWidth = 640;
		int reqHeight = 480;

		try {
			reqWidth = (Integer) params[0];
			reqHeight = (Integer) params[1];
		} catch (Exception e) {
			LogHelper.e(TAG, "doInBackground - invalid identifiers: " + e);	
		}

		Options options = ImageUtils.getBitmapOptions(data, reqWidth, reqHeight, null);
		return BitmapFactory.decodeByteArray(data, 0, data.length, options);
	}

	@Override
	public ImageView getAttachedImageView() {
		return imageViewReference.get();
	}

}
