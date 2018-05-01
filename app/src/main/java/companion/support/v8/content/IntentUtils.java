package companion.support.v8.content;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.provider.CalendarContract;
import android.provider.MediaStore;
import android.text.TextUtils;

import java.util.List;

/**
 * Utility class with intent methods.
 *
 * @author Vitor Ribeiro
 */
public class IntentUtils {

    /** Hidden constructor to prevent instantiation. */
    private IntentUtils() {
    }

    /**
     * Go to URL.
     * @param context of the caller.
     * @param uri value.
     * @param chooser text for the chooser.
     */
    public static void showURL(Context context, String uri, String chooser) {
        if (context == null || TextUtils.isEmpty(uri)) {
            return;
        }

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));

        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(Intent.createChooser(intent, chooser));
        }
    }

    /**
     * Open calendar.
     * @param context of the caller.
     * @param title of the event (can be null).
     * @param subtitle of the event (can be null).
     * @param start event start time in Unix timestamp (can be negative).
     * @param end event end time in Unix timestamp (can be negative).
     */
    @SuppressLint("NewApi")
    public static void showCalendar(Context context, String title, String subtitle, long start, long end) {
        Intent intent = new Intent(Intent.ACTION_INSERT).setData(CalendarContract.Events.CONTENT_URI);

        if (!TextUtils.isEmpty(title)) {
            intent.putExtra(CalendarContract.Events.TITLE, title);
        }

        if (!TextUtils.isEmpty(subtitle)) {
            intent.putExtra(CalendarContract.Events.DESCRIPTION, subtitle);
        }

        if (start >= 0) {
            intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, start);
        }

        if (end >= 0) {
            intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, end);
        }

        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(intent);
        }
    }

    /**
     * Get image capture intent.
     * @param context of the caller.
     * @param photoURI for the photo file.
     * @return intent or null if an error occurs.
     */
    public static Intent getImageCaptureIntent(Context context, Uri photoURI) throws Throwable {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        List<ResolveInfo> resolvedIntentActivities = context.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo resolvedIntentInfo : resolvedIntentActivities) {
            String packageName = resolvedIntentInfo.activityInfo.packageName;
            context.grantUriPermission(packageName, photoURI, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }

        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
        return intent;
    }
}
