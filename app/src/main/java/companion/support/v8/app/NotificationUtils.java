package companion.support.v8.app;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;

/** 
 * Utility class for Notifications.
 * 
 * @author Vitor Ribeiro
 * 
 */
public class NotificationUtils {

	/** This prevents the class from being instantiated. 
	 */
	private NotificationUtils() {
	}

	/** Get a notification builder with the set parameters.
	 * @param context caller's context.
	 * @param ongoing true is persistent
	 * @param autoCancel cleared when pressed.
	 * @param showDate shows date.
	 * @param showChronometer shows chronometer.
	 * @param ticker text of the status bar.
	 * @param title 1st line of text.
	 * @param text 2nd line of text.
	 * @param smallIcon icon on status bar.
	 * @param largeIcon icon on notification.
	 * @param callerClass activity to launch.
	 * @param pattern vibration pattern.
	 * @param audioResource sound resource.
	 * @return notification builder.
	 */
	public static Builder getNotificationBuilder(Context context, boolean ongoing, boolean autoCancel, 
			boolean showDate, boolean showChronometer, String ticker, String title, String text,
			int smallIcon, Bitmap largeIcon, Class<?> callerClass, long[] pattern, Integer audioResource) {
		Builder builder = new NotificationCompat.Builder(context);

		builder.setOngoing(ongoing);
		builder.setAutoCancel(autoCancel);
		builder.setShowWhen(showDate);
		builder.setUsesChronometer(showChronometer);

		if (ticker!=null) {
			builder.setTicker(ticker); 
		}
		if (title!=null) {
			builder.setContentTitle(title);
		}
		if (text!=null) {
			builder.setContentText(text); 
		}

		builder.setSmallIcon(smallIcon); 
		if (largeIcon != null) {
			builder.setLargeIcon(largeIcon);
		}

		if (callerClass!=null) {
			Intent intent = new Intent(context, callerClass);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);			
			builder.setContentIntent(PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT));
		}
 
		if (pattern!=null) {
			builder.setVibrate(pattern);
		}

		if (audioResource!=null) {
			Uri sound = Uri.parse("android.resource://" + context.getPackageName() + "/" + audioResource);
			builder.setSound(sound, AudioManager.STREAM_NOTIFICATION);
		}

		return builder;
	}

	public static Notification getNotification(Context context, boolean ongoing, boolean autoCancel, 
		boolean showDate, boolean showChronometer, String ticker, String title, String text,
		int smallIcon, Bitmap largeIcon, Class<?> callerClass, long[] pattern, Integer audioResource) {

		return getNotificationBuilder(context, ongoing, autoCancel, showDate, showChronometer, 
			ticker, title, text, smallIcon, largeIcon, callerClass, pattern, audioResource).build();
	}
}
