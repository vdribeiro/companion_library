package companion.support.v8.graphics;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.AsyncTask;

/**
 * Task that converts an image to string in the background.
 *
 * @author Vitor Ribeiro
 */
@SuppressLint("NewApi")
public abstract class ImageTask extends AsyncTask<Bitmap, Void, String> {

    /** The image task interface. */
    public interface ImageCallback {
        void onPrepare();
        void onSuccess(String result);
        void onFailure();
    }

    private final ImageCallback callback;

    protected ImageTask(ImageCallback callback) {
        this.callback = callback;
    }

    @Override
    protected void onPreExecute() {
        callback.onPrepare();
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(Bitmap... params) {
        Bitmap bitmap = params[0];
        String encoded;
        try {
            encoded = ImageUtils.compressAndEncodeBitmap(bitmap);
        } catch (Throwable t) {
            encoded = null;
        }
        return encoded;
    }

    @Override
    protected void onPostExecute(final String result) {
        callback.onSuccess(result);
        super.onPostExecute(result);
    }

    @Override
    protected void onCancelled(final String result) {
        callback.onFailure();
        super.onCancelled(result);
    }
}