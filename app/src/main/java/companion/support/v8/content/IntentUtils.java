package companion.support.v8.content;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.CalendarContract;
import android.text.TextUtils;

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

}
