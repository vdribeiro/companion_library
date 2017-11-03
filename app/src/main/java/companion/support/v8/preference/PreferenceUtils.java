package companion.support.v8.preference;

import java.util.Iterator;
import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import companion.support.v8.os.Utils;
import companion.support.v8.util.LogHelper;

public class PreferenceUtils {

	/** Log tag. */
	private static final String TAG = PreferenceUtils.class.getSimpleName();

	/** This prevents the class from being instantiated. 
	 */
	private PreferenceUtils() {
	}

	/**
	 * Determines whether the simplified settings UI should be shown. This is
	 * true if the device doesn't have newer APIs like {@link PreferenceFragment}, 
	 * or the device doesn't have an extra-large screen. In these cases, a single-pane
	 * "simplified" settings UI should be shown.
	 */
	public static boolean isSimplePreferences(Context context) {
		return !Utils.hasHoneycomb() || !Utils.isXLargeTablet(context);
	}

	/**
	 * Print shared preferences.
	 * @param context caller's context.
	 */
	@SuppressWarnings({"rawtypes"})
	public static void print(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		Map<String, ?> map = prefs.getAll();
		Iterator<?> it = map.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pairs = (Map.Entry) it.next();
			LogHelper.i(TAG, pairs.getKey() + " = " + pairs.getValue());
		}
	}
}
