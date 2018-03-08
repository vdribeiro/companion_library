package companion.support.v8.content;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class BroadcastUtils {
	
	/** This prevents the class from being instantiated. 
	 */
	private BroadcastUtils() {
	}
	
	/** Checks if a broadcast is already queued.
	 * @param context of the application package implementing this class.
	 * @param cls component class that is to be used for the intent.
	 * @param action broadcasts's unique identifier.
	 * @return true if it is, false otherwise.
	 */
	public static boolean checkBroadcast(Context context, Class<?> cls, String action) {
		Intent intent = new Intent(context, cls);
		intent.setAction(action);

        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_NO_CREATE) != null;
    }
	
}
