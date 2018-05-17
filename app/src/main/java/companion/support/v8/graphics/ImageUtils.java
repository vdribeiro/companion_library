package companion.support.v8.graphics;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.view.View;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import companion.support.v8.lang.ParsingUtils;
import companion.support.v8.os.Storage;
import companion.support.v8.os.Utils;
import companion.support.v8.time.DateTimeUtils;

/**
 * Image related utility class.
 *
 * @author Vitor Ribeiro
 */
@SuppressLint("NewApi")
public class ImageUtils {

    /** Log tag. */
    private static final String TAG = ImageUtils.class.getSimpleName();

    /** Width 1080p. */
    public static int WIDTH_1080P = 1920;
    /** Height 1080p. */
    public static int HEIGHT_1080P = 1080;

    /** Width 720p. */
    public static int WIDTH_720P = 1280;
    /** Height 720p. */
    public static int HEIGHT_720P = 720;

    /** Maximum image size in bytes. */
    public static int MAX_BYTE_SIZE = 1000000;
    /** Default image quality for compression. */
    public static int DEFAULT_QUALITY = 80;
    /** Default image quality value to decrement if image size
     * is larger than MAX_BYTE_SIZE when compressed. */
    public static int DEFAULT_QUALITY_DECREMENT = 20;
    /** Minimum accepted image quality for compression. */
    public static int MIN_QUALITY = 50;
    /** Default ripple radius. */
    public static int DEFAULT_RIPPLE_RADIUS = 3;

    /** Hidden constructor to prevent instantiation. */
    private ImageUtils() {
    }

    /**
     * Return the byte usage per pixel of a bitmap based on its configuration.
     * @param config The bitmap configuration.
     * @return The byte usage per pixel.
     */
    public static int getBytesPerPixel(Bitmap.Config config) {
        if (config == Bitmap.Config.ARGB_8888) {
            return 4;
        } else if (config == Bitmap.Config.RGB_565) {
            return 2;
        } else if (config == Bitmap.Config.ARGB_4444) {
            return 2;
        } else if (config == Bitmap.Config.ALPHA_8) {
            return 1;
        }
        return 1;
    }

    /** Returns in pixels the value of density dependent pixels.
     * @param dp density.
     * @return pixel value.
     */
    public static int densityToPixels(Context context, int dp){
        if (context == null) {
            return -1;
        }

        Resources resources = context.getResources();
        if (resources == null) {
            return -1;
        }

        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        if (displayMetrics == null) {
            return -1;
        }

        float scale = displayMetrics.density;
        return (int) (dp * scale + 0.5f);
    }

    /**
     * Scale the provided bitmap.
     * @param bitmap to scale.
     * @param width to scale.
     * @param height to scale.
     * @return scaled bitmap.
     */
    private static Bitmap scaleBitmap(Bitmap bitmap, int width, int height) {
        if (bitmap == null) {
            return null;
        }

        Bitmap scaledBitmap;
        try {
            scaledBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
        } catch (Throwable t) {
            scaledBitmap = bitmap;
        }

        return scaledBitmap;
    }

    /**
     * Compress a bitmap.
     * @param bitmap to convert.
     * @return compressed bitmap bytes.
     */
    public static byte[] compressBitmap(Bitmap bitmap) {
        return compressBitmap(bitmap, DEFAULT_QUALITY);
    }

    /**
     * Compress a bitmap.
     * @param bitmap to convert.
     * @param quality 0-100: 0 meaning compress for small size,
     *                100 meaning compress for max quality.
     * @return compressed bitmap bytes.
     */
    public static byte[] compressBitmap(Bitmap bitmap, int quality) {
        if (bitmap == null) {
            return null;
        }

        ByteArrayOutputStream out;
        try {
            do {
                out = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out);
                quality -= DEFAULT_QUALITY_DECREMENT;
            } while (out.size() > MAX_BYTE_SIZE && quality >= MIN_QUALITY);
        } catch (Throwable t) {
            out = new ByteArrayOutputStream();
        }

        return out.toByteArray();
    }

    /**
     * Compress and encode a bitmap.
     * @param bitmap to compress.
     * @return encoded bitmap.
     */
    public static String compressAndEncodeBitmap(final Bitmap bitmap) {
        return compressAndEncodeBitmap(bitmap, DEFAULT_QUALITY);
    }

    /**
     * Compress and encode a bitmap.
     * @param bitmap to compress.
     * @param quality 0-100: 0 meaning compress for small size,
     *                100 meaning compress for max quality.
     * @return encoded bitmap.
     */
    public static String compressAndEncodeBitmap(final Bitmap bitmap, int quality) {
        String encoded;
        try {
            byte[] out = compressBitmap(bitmap, quality);
            encoded = Base64.encodeToString(out, Base64.DEFAULT);
        } catch (Throwable t) {
            encoded = null;
        }

        return encoded;
    }

    /** Convert base64 to Bitmap.
     * @param str Base64 String to convert.
     * @return corresponding Bitmap.
     */
    public static Bitmap base64ToBitmap(String str) {
        if (str == null) {
            return null;
        }

        return BitmapFactory.decodeByteArray(
                ParsingUtils.base64ToBytes(str),
                0, ParsingUtils.base64ToBytes(str).length);
    }

    /** Convert bytes to Bitmap.
     * @param bytes to convert.
     * @return corresponding Bitmap.
     */
    public static Bitmap bytesToBitmap(byte[] bytes) {
        if (bytes == null) {
            return null;
        }

        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    /**
     * Get image from file.
     * @param filePath path of the photo.
     * @return bitmap.
     */
    private static Bitmap parseImageFromFile(final String filePath) {
        Bitmap bitmap;
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(filePath, options);
            ImageConfig imageConfig = new ImageConfig(options.outWidth, options.outHeight, WIDTH_720P);

            options.inJustDecodeBounds = false;
            options.inScaled = true;
            options.inSampleSize = imageConfig.inSampleSize;
            options.inDensity = imageConfig.getInDensity();
            options.inTargetDensity = imageConfig.getInTargetDensity();

            bitmap = BitmapFactory.decodeFile(filePath, options);
        } catch (Throwable t) {
            bitmap = null;
        }

        return bitmap;
    }

    /**
     * Get image using MediaStore.
     * @param context of the caller.
     * @param data result intent.
     * @return bitmap.
     */
    private static Bitmap parseImageFromMediaStore(Context context, final Intent data) {
        Bitmap bitmap;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), data.getData());
            ImageConfig imageConfig = new ImageConfig(bitmap.getWidth(), bitmap.getHeight(), WIDTH_720P);
            bitmap = scaleBitmap(bitmap, imageConfig.dstWidth, imageConfig.dstHeigth);
        } catch (Throwable t) {
            bitmap = null;
        }

        return bitmap;
    }

    /**
     * Get image from intent.
     * @param data result intent.
     * @return bitmap.
     */
    @SuppressWarnings("ConstantConditions")
    private static Bitmap parseImageFromIntent(final Intent data) {
        Bitmap bitmap;
        try {
            bitmap = (Bitmap) data.getExtras().get("data");
        } catch (Throwable t) {
            bitmap = null;
        }

        return bitmap;
    }

    /**
     * Get image from gallery.
     * @param context of the caller.
     * @param data result intent.
     * @return bitmap.
     */
    public static Bitmap parseGalleryImage(final Context context, final Intent data) {
        if (data == null) {
            return null;
        }

        String path = null;
        try {
            path = Storage.getRealPathFromURI(context, data.getData());
        } catch (Throwable throwable) {
            // Ignore
        }
        Bitmap bitmap = parseImageFromFile(path);

        if (bitmap != null) {
            return bitmap;
        }

        return parseImageFromMediaStore(context, data);
    }

    /**
     * Get image from gallery.
     * @param file of the photo.
     * @param data result intent.
     * @return bitmap.
     */
    public static Bitmap parseCameraImage(final File file, final Intent data) {
        if (file == null) {
            return null;
        }

        String path = file.getAbsolutePath();
        Bitmap bitmap = parseImageFromFile(path);

        if (bitmap != null) {
            return bitmap;
        }

        return parseImageFromIntent(data);
    }

    /**
     * Create a JPEG file on the local storage.
     * @param context of the caller.
     * @return file.
     */
    public static File createPhotoFile(Context context) throws Throwable {
        String state = Environment.getExternalStorageState();
        if (!state.equals(Environment.MEDIA_MOUNTED)) {
            return null;
        }

        // Create the storage directory if it does not exist
        File mediaStorageDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), TAG);
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()) {
            return null;
        }

        // Create a JPEG file
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", DateTimeUtils.LOCALE).format(new Date());
        return new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");
    }

    /**
     * Get the size in bytes of a bitmap in a BitmapDrawable. Note that from Android 4.4 (KitKat)
     * onward this returns the allocated memory size of the bitmap which can be larger than the
     * actual bitmap data byte count (in the case it was re-used).
     *
     * @param value bitmap.
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
        int byteCount = width * height * getBytesPerPixel(candidate.getConfig());
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

    /**
     * Helper method to get the drawable by its resource.
     * @param context of the caller.
     * @param drawableRes drawable resource id.
     */
    public static Drawable getDrawableCompat(Context context, @DrawableRes int drawableRes) {
        try {
            if (Utils.hasLollipop()) {
                return context.getResources().getDrawable(drawableRes, context.getTheme());
            } else {
                return context.getResources().getDrawable(drawableRes);
            }
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Add a ripple effect to the view when touched.
     * @param context of the caller.
     * @param view to add the effect to.
     * @param primaryColor value.
     * @param rippleColor value.
     */
    public static void addRippleEffect(Context context, View view, int primaryColor, int rippleColor) {
        Drawable rippleDrawable = getRippleDrawable(
                new ColorDrawable(ContextCompat.getColor(context, primaryColor)),
                ContextCompat.getColor(context, rippleColor));
        setBackgroundCompat(view, rippleDrawable);
    }

    /**
     * Get ripple mask.
     * @param color value.
     * @param radius radius of final ripple
     * @return ripple mask.
     */
    public static Drawable getRippleMask(@ColorInt int color, int radius) {
        float[] outerRadii = new float[8];
        Arrays.fill(outerRadii, radius);
        RoundRectShape r = new RoundRectShape(outerRadii, null, null);
        ShapeDrawable shapeDrawable = new ShapeDrawable(r);
        shapeDrawable.getPaint().setColor(color);
        return shapeDrawable;
    }

    /**
     * Get drawable with ripple effect.
     * @param drawable object.
     * @param rippleColor the color of the ripple.
     * @return the RippleDrawable with the chosen background drawable.
     */
    public static Drawable getRippleDrawable(Drawable drawable, @ColorInt int rippleColor) {
        if (Utils.hasLollipop()) {
            return new RippleDrawable(ColorStateList.valueOf(rippleColor), drawable, getRippleMask(Color.BLACK, DEFAULT_RIPPLE_RADIUS));
        }
        return drawable;
    }

    /**
     * Helper method to set the background.
     * @param view to apply the drawable.
     * @param drawable drawable object.
     */
    public static void setBackgroundCompat(View view, Drawable drawable) {
        ViewCompat.setBackground(view, drawable);
    }

    /**
     * Helper method to set the background.
     * @param view to apply the drawable.
     * @param drawableRes drawable resource id.
     */
    public static void setBackgroundCompat(View view, @DrawableRes int drawableRes) {
        setBackgroundCompat(view, getDrawableCompat(view.getContext(), drawableRes));
    }

    /**
     * Set background of a view safely in an oval shape.
     * @param view to update.
     * @param color of the background.
     */
    public static void setOvalBackground(View view, int color) {
        GradientDrawable gd = new GradientDrawable();
        try {
            gd.setShape(GradientDrawable.OVAL);
            gd.setColor(Color.parseColor("#" + Integer.toString(color, 16)));
        } catch (Exception e) {
            return;
        }

        setBackgroundCompat(view, gd);
    }

    // TODO: Picasso utils
//    /**
//     * Wrapper for the Picasso library.
//     * @param context of the caller.
//     * @param tag reference.
//     * @param url image source.
//     * @param imageView destination view.
//     * @param crop true to center crop, false to center inside.
//     */
//    public static void usePicasso(Context context, Object tag, String url, ImageView imageView, boolean crop) {
//        RequestCreator builder = Picasso.with(context)
//                .load(url)
//                .error(R.drawable.ic_logo)
//                .placeholder(R.color.imageBackground)
//                .tag(tag)
//                .fit();
//
//        if (crop) {
//            builder.centerCrop().into(imageView);
//        } else {
//            builder.centerInside().into(imageView);
//        }
//    }
}
