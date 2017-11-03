package companion.support.v8.graphics;

import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.util.Base64;

import companion.support.v8.lang.ParsingUtils;
import companion.support.v8.os.Utils;

public class GraphicsUtils {

	/** This prevents the class from being instantiated. 
	 */
	private GraphicsUtils() {
	};

	/**
	 * Return the byte usage per pixel of a bitmap based on its configuration.
	 * @param config The bitmap configuration.
	 * @return The byte usage per pixel.
	 */
	public static int getBytesPerPixel(Config config) {
		if (config == Config.ARGB_8888) {
			return 4;
		} else if (config == Config.RGB_565) {
			return 2;
		} else if (config == Config.ARGB_4444) {
			return 2;
		} else if (config == Config.ALPHA_8) {
			return 1;
		}
		return 1;
	}

	/**
	 * Get the size in bytes of a bitmap in a BitmapDrawable. Note that from Android 4.4 (KitKat)
	 * onward this returns the allocated memory size of the bitmap which can be larger than the
	 * actual bitmap data byte count (in the case it was re-used).
	 *
	 * @param value
	 * @return size in bytes
	 */
	@SuppressLint("NewApi")
	public static int getBitmapSize(BitmapDrawable value) {
		Bitmap bitmap = value.getBitmap();

		// From KitKat onward use getAllocationByteCount() as allocated bytes can potentially be
		// larger than bitmap byte count.
		if (Utils.hasKitKat()) {
			return bitmap.getAllocationByteCount();
		}

		if (Utils.hasHoneycombMR1()) {
			return bitmap.getByteCount();
		}

		// Pre HC-MR1
		return bitmap.getRowBytes() * bitmap.getHeight();
	}

	/** Check if bitmap is valid for the inBitmap option.
	 * @param candidate - Bitmap to check
	 * @param targetOptions - Options that have the out* value populated
	 * @return true if <code>candidate</code> can be used for inBitmap re-use with
	 *      <code>targetOptions</code>
	 */
	@SuppressLint("NewApi")
	public static boolean canUseForInBitmap(Bitmap candidate, BitmapFactory.Options targetOptions) {

		if (!Utils.hasKitKat()) {
			// On earlier versions, the dimensions must match exactly and the inSampleSize must be 1
			return candidate.getWidth() == targetOptions.outWidth && 
				candidate.getHeight() == targetOptions.outHeight && 
				targetOptions.inSampleSize == 1;
		}

		// From Android 4.4 (KitKat) onward we can re-use if the byte size of the new bitmap
		// is smaller than the reusable bitmap candidate allocation byte count.
		int width = targetOptions.outWidth / targetOptions.inSampleSize;
		int height = targetOptions.outHeight / targetOptions.inSampleSize;
		int byteCount = width * height * GraphicsUtils.getBytesPerPixel(candidate.getConfig());
		return byteCount <= candidate.getAllocationByteCount();
	}

	/**
	 * Calculate an inSampleSize for use in a {@link BitmapFactory.Options} object when decoding
	 * bitmaps using the decode* methods from {@link BitmapFactory}. This implementation calculates
	 * the closest inSampleSize that is a power of 2 and will result in the final decoded bitmap
	 * having a width and height equal to or larger than the requested width and height.
	 *
	 * @param options object with out* params already populated 
	 * (run through a decode* method with inJustDecodeBounds==true).
	 * @param reqWidth the requested width of the resulting bitmap.
	 * @param reqHeight the requested height of the resulting bitmap.
	 * @return value to be used for inSampleSize.
	 */
	public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {

			final int halfHeight = height / 2;
			final int halfWidth = width / 2;

			// Calculate the largest inSampleSize value that is a power of 2 and keeps both
			// height and width larger than the requested height and width.
			while ((halfHeight / inSampleSize) > reqHeight && (halfWidth / inSampleSize) > reqWidth) {
				inSampleSize *= 2;
			}

			// This offers some additional logic in case the image has a strange
			// aspect ratio. For example, a panorama may have a much larger
			// width than height. In these cases the total pixels might still
			// end up being too large to fit comfortably in memory, so we should
			// be more aggressive with sample down the image (=larger inSampleSize).
			long totalPixels = width * height / inSampleSize;

			// Anything more than 2x the requested pixels we'll sample down further
			final long totalReqPixelsCap = reqWidth * reqHeight * 2;

			while (totalPixels > totalReqPixelsCap) {
				inSampleSize *= 2;
				totalPixels /= 2;
			}
		}
		return inSampleSize;
	}

	/** 
	 * Get Bitmap options where the bitmap is sampled down from the original 
	 * with the same aspect ratio and dimensions that are equal to or greater than the requested width and height.
	 * @param options object to fill.
	 * @param reqWidth the requested width of the resulting bitmap.
	 * @param reqHeight the requested height of the resulting bitmap.
	 * @param inBitmap if it is on Honeycomb or newer it can use a inBitmap, or null to do not use this option.
	 * @return options object.
	 */
	@SuppressLint("NewApi")
	public static BitmapFactory.Options getBitmapOptions(BitmapFactory.Options options, int reqWidth, int reqHeight, Bitmap inBitmap) {

		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

		// If we're running on Honeycomb or newer, try to use inBitmap
		if (Utils.hasHoneycomb()) {
			// inBitmap only works with mutable bitmaps so force the decoder to
			// return mutable bitmaps.
			options.inMutable = true;
			if (inBitmap != null) {
				options.inBitmap = inBitmap;
			}
		}
		
		// Can now decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;

		return options;
	}
	
	/**
	 * Get Bitmap options where the bitmap is sampled down from the original 
	 * with the same aspect ratio and dimensions that are equal to or greater than the requested width and height.
	 * @param image the image data.
	 * @param reqWidth the requested width of the resulting bitmap.
	 * @param reqHeight the requested height of the resulting bitmap.
	 * @param inBitmap if it is on Honeycomb or newer it can use a inBitmap, or null to do not use this option.
	 * @return bitmap options.
	 */
	public static BitmapFactory.Options getBitmapOptions(byte[] image, int reqWidth, int reqHeight, Bitmap inBitmap) {

		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;		
		BitmapFactory.decodeByteArray(image, 0, image.length, options);

		return getBitmapOptions(options, reqWidth, reqHeight, inBitmap);
	}
	
	/**
	 * Get Bitmap options where the bitmap is sampled down from the original 
	 * with the same aspect ratio and dimensions that are equal to or greater than the requested width and height.
	 *
	 * @param res the resources object containing the image data.
	 * @param resId the resource id of the image data.
	 * @param reqWidth the requested width of the resulting bitmap.
	 * @param reqHeight the requested height of the resulting bitmap.
	 * @param inBitmap if it is on Honeycomb or newer it can use a inBitmap, or null to do not use this option.
	 * @return bitmap options.
	 */
	@SuppressLint("NewApi")
	public static BitmapFactory.Options getBitmapOptions(Resources res, int resId, int reqWidth, int reqHeight, Bitmap inBitmap) {

		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeResource(res, resId, options);

		return getBitmapOptions(options, reqWidth, reqHeight, inBitmap);
	}
	
	/**
	 * Get Bitmap options where the bitmap is sampled down from the original 
	 * with the same aspect ratio and dimensions that are equal to or greater than the requested width and height.
	 *
	 * @param filename the full path of the file to decode.
	 * @param reqWidth the requested width of the resulting bitmap.
	 * @param reqHeight the requested height of the resulting bitmap.
	 * @param inBitmap if it is on Honeycomb or newer it can use a inBitmap, or null to do not use this option.
	 * @return bitmap options.
	 */
	@SuppressLint("NewApi")
	public static BitmapFactory.Options getBitmapOptions(String filename, int reqWidth, int reqHeight, Bitmap inBitmap) {

		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(filename, options);

		return getBitmapOptions(options, reqWidth, reqHeight, inBitmap);
	}

	/**
	 * Get Bitmap options where the bitmap is sampled down from the original 
	 * with the same aspect ratio and dimensions that are equal to or greater than the requested width and height.
	 *
	 * @param fileDescriptor the file descriptor to read from.
	 * @param reqWidth the requested width of the resulting bitmap.
	 * @param reqHeight the requested height of the resulting bitmap.
	 * @param inBitmap if it is on Honeycomb or newer it can use a inBitmap, or null to do not use this option.
	 * @return bitmap options.
	 */
	@SuppressLint("NewApi")
	public static BitmapFactory.Options getBitmapOptions(FileDescriptor fileDescriptor, int reqWidth, int reqHeight, Bitmap inBitmap) {

		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);

		return getBitmapOptions(options, reqWidth, reqHeight, inBitmap);
	}

	/** Convert Bitmap to Base64.
	 * @param bitmap Bitmap to convert. 
	 * @return corresponding Base64 string.
	 */
	public static String bitmapToBase64(Bitmap bitmap) {
		if (bitmap == null) {
			return null;
		}

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
		return Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
	}

	/** Convert base64 to Bitmap.
	 * @param str Base64 String to convert. 
	 * @return corresponding Bitmap.
	 */
	public static Bitmap base64ToBitmap(String str) {
		if (str == null) {
			return null;
		}

		return BitmapFactory.decodeByteArray(ParsingUtils.base64ToBytes(str), 0, ParsingUtils.base64ToBytes(str).length);
	}

	/** Convert bitmap to a byte array.
	 * @param bitmap to convert.
	 * @return converted bitmap bytes.
	 */
	public static byte[] bitmapToBytes(Bitmap bitmap) {
		if (bitmap == null) {
			return null;
		}

		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
		return stream.toByteArray();
	}

	/** Convert bytes to Bitmap.
	 * @param str Base64 String to convert. 
	 * @return corresponding Bitmap.
	 */
	public static Bitmap bytesToBitmap(byte[] bytes) {
		if (bytes == null) {
			return null;
		}

		return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
	}

	/** Returns in pixels the value of density dependent pixels.
	 * @param dp density.
	 * @return pixel value.
	 */
	public static int densityToPixels(Context context, int dp){
		if (context == null) {
			return -1;
		}

		float scale = context.getResources().getDisplayMetrics().density;
		int paddingInPx = (int) (dp * scale + 0.5f);
		return paddingInPx;
	}
}
