package companion.support.v8.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.util.AttributeSet;
import android.widget.ImageView;

import companion.support.v8.graphics.drawable.BitmapDrawableRecycled;

/**
 * Sub-class of ImageView which automatically notifies 
 * the drawable when it is being displayed.
 * 
 * @author Vitor Ribeiro
 *
 */
public class ImageViewRecycled extends ImageView {
	
	public ImageViewRecycled(Context context) {
		super(context);
	}

	public ImageViewRecycled(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ImageViewRecycled(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onDetachedFromWindow() {
		// This has been detached from Window, so clear the drawable
		setImageDrawable(null);

		super.onDetachedFromWindow();
	}

	@Override
	public void setImageDrawable(Drawable drawable) {
		// Keep hold of previous Drawable
		final Drawable previousDrawable = getDrawable();

		// Call super to set new Drawable
		super.setImageDrawable(drawable);

		// Notify new Drawable that it is being displayed
		notifyDrawable(drawable, true);

		// Notify old Drawable so it is no longer being displayed
		notifyDrawable(previousDrawable, false);
	}

	/**
	 * Notifies the drawable that it's displayed state has changed.
	 *
	 * @param drawable Drawable to notify.
	 * @param isDisplayed Target display state.
	 */
	private static void notifyDrawable(Drawable drawable, final boolean isDisplayed) {
		if (drawable instanceof BitmapDrawableRecycled) {
			// The drawable is a CountingBitmapDrawable, so notify it
			((BitmapDrawableRecycled) drawable).setIsDisplayed(isDisplayed);
		} else if (drawable instanceof LayerDrawable) {
			// The drawable is a LayerDrawable, so recurse on each layer
			LayerDrawable layerDrawable = (LayerDrawable) drawable;
			for (int i = 0, z = layerDrawable.getNumberOfLayers(); i < z; i++) {
				notifyDrawable(layerDrawable.getDrawable(i), isDisplayed);
			}
		}
	}

}
