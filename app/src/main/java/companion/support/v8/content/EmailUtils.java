package companion.support.v8.content;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Patterns;

import java.util.Set;

@SuppressWarnings("WeakerAccess")
public final class EmailUtils {

    /** Hidden constructor to prevent instantiation. */
    private EmailUtils() {
    }

    /**
     * Open email.
     * @param context of the caller.
     */
    public static boolean showEmail(Context context, Set<String>to, String subject, String body) {
        if (context == null || TextUtils.isEmpty(subject)) {
            return false;
        }

        for (String email : to) {
            if (!checkEmail(email)) {
                return false;
            }
        }

        if (!hasNoLineBreaks(subject)) {
            return false;
        }

        body = fixLineBreaks(body);

        StringBuilder mailto = new StringBuilder(1024);
        mailto.append("mailto:");
        for (String recipient : to) {
            mailto.append(encodeRecipient(recipient));
            mailto.append(',');
        }
        mailto.setLength(mailto.length() - 1);

        mailto.append('?').append("subject").append('=').append(Uri.encode(subject));
        mailto.append('&').append("body").append('=').append(Uri.encode(body));

        Uri mailtoUri = Uri.parse(mailto.toString());

        Intent intent = new Intent(Intent.ACTION_SENDTO, mailtoUri);
        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }

        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(intent);
        }

        return true;
    }

    public static boolean checkEmail(String email) {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();

    }

    public static boolean hasNoLineBreaks(String text) {
        return !TextUtils.isEmpty(text) && !(text.indexOf('\r') != -1 || text.indexOf('\n') != -1);

    }

    public static String fixLineBreaks(String text) {
        return text.replaceAll("\r\n", "\n").replace('\r', '\n').replaceAll("\n", "\r\n");
    }

    public static String encodeRecipient(String recipient) {
        int index = recipient.lastIndexOf('@');
        String name = recipient.substring(0, index);
        String host = recipient.substring(index + 1);
        return Uri.encode(name) + "@" + Uri.encode(host);
    }

}