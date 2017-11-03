package companion.support.v8;

import java.util.List;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.StrictMode;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import companion.support.v8.util.LogHelper;

/**
 * Class containing some static utility methods for the OS,
 * like versions, keyboard, screen, etc.
 * Static final constants declared in later versions
 * of the OS are inlined at compile time.
 * 
 * @author Vitor Ribeiro
 *
 */
public class Utils {

	/** Log Tag */
	private static final String TAG = Utils.class.getSimpleName();

	/**
	 * This prevents the class from being instantiated.
	 */
	private Utils() {
	}

	/**
	 * StrictMode is a developer tool which detects things
	 * you might be doing by accident and brings them
	 * to your attention so you can fix them.
	 */
	@TargetApi(VERSION_CODES.HONEYCOMB)
	public static void enableStrictMode(Class<?>[] classArray) {
		if (Utils.hasGingerbread()) {
			StrictMode.ThreadPolicy.Builder threadPolicyBuilder = 
				new StrictMode.ThreadPolicy.Builder()
				.detectAll()
				.penaltyLog();
			StrictMode.VmPolicy.Builder vmPolicyBuilder =
				new StrictMode.VmPolicy.Builder()
				.detectAll()
				.penaltyLog();

			if (Utils.hasHoneycomb()) {
				threadPolicyBuilder.penaltyFlashScreen();
				for (int i = 0; i < classArray.length; i++) {
					vmPolicyBuilder.setClassInstanceLimit(classArray[i], 1);
				}
			}

			StrictMode.setThreadPolicy(threadPolicyBuilder.build());
			StrictMode.setVmPolicy(vmPolicyBuilder.build());
		}
	}

	/**
	 * Check if the intent is available.
	 * 
	 * @param ctx caller's context.
	 * @param intent to check availability.
	 * @return true if the intent is available, false otherwise.
	 */
	public static boolean isIntentAvailable(Context ctx, Intent intent) {
		final PackageManager mgr = ctx.getPackageManager();
		List<ResolveInfo> list = mgr.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
		return list.size() > 0;
	}

	/**
	 * Get thread identifier.
	 * 
	 * @return the thread's identifier.
	 */
	public static long getThreadId() {
		return Thread.currentThread().getId();
	}

	/**
	 * Get thread signature.
	 * 
	 * @return the thread's signature.
	 */
	public static String getThreadSignature() {
		Thread t = Thread.currentThread();
		return (t.getName() + ":(id)" + t.getId() + ":(priority)" + t.getPriority() + ":(group)" + t.getThreadGroup().getName());
	}

	/**
	 * Causes the thread which sent this message to sleep for the given interval of time (given in milliseconds).
	 * The precision is not guaranteed, the Thread may sleep more or less than requested.
	 * 
	 * @param time to sleep in milliseconds, approximately.
	 */
	public static void sleep(long time) {
		if (time < 0) {
			time = 500
		}
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
		}
	}

	/**
	 * Check if the user-visible SDK version of the framework is equal or newer to API 7.
	 * 
	 * @return true if API 7+, false otherwise.
	 */
	public static boolean hasEclairMR1() {
		return Build.VERSION.SDK_INT >= VERSION_CODES.ECLAIR_MR1;
	}

	/**
	 * Check if the user-visible SDK version of the framework is equal or newer to API 8.
	 * 
	 * @return true if API 8+, false otherwise.
	 */
	public static boolean hasFroyo() {
		return Build.VERSION.SDK_INT >= VERSION_CODES.FROYO;
	}

	/**
	 * Check if the user-visible SDK version of the framework is equal or newer to API 9.
	 * 
	 * @return true if API 9+, false otherwise.
	 */
	public static boolean hasGingerbread() {
		return Build.VERSION.SDK_INT >= VERSION_CODES.GINGERBREAD;
	}

	/**
	 * Check if the user-visible SDK version of the framework is equal or newer to API 10.
	 * 
	 * @return true if API 10+, false otherwise.
	 */
	public static boolean hasGingerbreadMR1() {
		return Build.VERSION.SDK_INT >= VERSION_CODES.GINGERBREAD_MR1;
	}

	/**
	 * Check if the user-visible SDK version of the framework is equal or newer to API 11.
	 * 
	 * @return true if API 11+, false otherwise.
	 */
	public static boolean hasHoneycomb() {
		return Build.VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB;
	}

	/**
	 * Check if the user-visible SDK version of the framework is equal or newer to API 12.
	 * 
	 * @return true if API 12+, false otherwise.
	 */
	public static boolean hasHoneycombMR1() {
		return Build.VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB_MR1;
	}

	/**
	 * Check if the user-visible SDK version of the framework is equal or newer to API 13.
	 * 
	 * @return true if API 13+, false otherwise.
	 */
	public static boolean hasHoneycombMR2() {
		return Build.VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB_MR2;
	}

	/**
	 * Check if the user-visible SDK version of the framework is equal or newer to API 14.
	 * 
	 * @return true if API 14+, false otherwise.
	 */
	public static boolean hasIceCreamSandwich() {
		return Build.VERSION.SDK_INT >= VERSION_CODES.ICE_CREAM_SANDWICH;
	}

	/**
	 * Check if the user-visible SDK version of the framework is equal or newer to API 15.
	 * 
	 * @return true if API 15+, false otherwise.
	 */
	public static boolean hasIceCreamSandwichMR1() {
		return Build.VERSION.SDK_INT >= VERSION_CODES.ICE_CREAM_SANDWICH_MR1;
	}

	/**
	 * Check if the user-visible SDK version of the framework is equal or newer to API 16.
	 * 
	 * @return true if API 16+, false otherwise.
	 */
	public static boolean hasJellyBean() {
		return Build.VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN;
	}

	/**
	 * Check if the user-visible SDK version of the framework is equal or newer to API 17.
	 * 
	 * @return true if API 17+, false otherwise.
	 */
	public static boolean hasJellyBeanMR1() {
		return Build.VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN_MR1;
	}

	/**
	 * Check if the user-visible SDK version of the framework is equal or newer to API 18.
	 * 
	 * @return true if API 18+, false otherwise.
	 */
	public static boolean hasJellyBeanMR2() {
		return Build.VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN_MR2;
	}

	/**
	 * Check if the user-visible SDK version of the framework is equal or newer to API 19.
	 * 
	 * @return true if API 19+, false otherwise.
	 */
	public static boolean hasKitKat() {
		return Build.VERSION.SDK_INT >= VERSION_CODES.KITKAT;
	}

	/**
	 * Check if the user-visible SDK version of the framework is equal or newer to API 20.
	 * 
	 * @return true if API 20+, false otherwise.
	 */
	public static boolean hasKitKatWatch() {
		return Build.VERSION.SDK_INT >= VERSION_CODES.KITKAT_WATCH;
	}

	/**
	 * Check if the user-visible SDK version of the framework is equal or newer to API 21.
	 * 
	 * @return true if API 21+, false otherwise.
	 */
	public static boolean hasLollipop() {
		return Build.VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP;
	}

	/**
	 * Check if the user-visible SDK version of the framework is equal or newer to API 22.
	 *
	 * @return true if API 22+, false otherwise.
	 */
	public static boolean hasLollipopMR1() {
		return Build.VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP_MR1;
	}

	/**
	 * Check if the user-visible SDK version of the framework is equal or newer to API 23.
	 *
	 * @return true if API 23+, false otherwise.
	 */
	public static boolean hasMarshmallow() {
		return Build.VERSION.SDK_INT >= VERSION_CODES.M;
	}

	/**
	 * Check if the user-visible SDK version of the framework is equal or newer to API 24.
	 *
	 * @return true if API 24+, false otherwise.
	 */
	public static boolean hasNougat() {
		return Build.VERSION.SDK_INT >= VERSION_CODES.N;
	}

	/**
	 * Check if the user-visible SDK version of the framework is equal or newer to API 25.
	 *
	 * @return true if API 25+, false otherwise.
	 */
	public static boolean hasNougatMR1() {
		return Build.VERSION.SDK_INT >= VERSION_CODES.N_MR1;
	}

	/**
	 * Helper method to determine if the device has a small sized screen.
	 */
	public static boolean isSmallScreen(Context context) {
		return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_SMALL;
	}

	/**
	 * Helper method to determine if the device has a normal sized screen.
	 */
	public static boolean isNormalScreen(Context context) {
		return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_NORMAL;
	}

	/**
	 * Helper method to determine if the device has a large screen.
	 */
	public static boolean isLargeScreen(Context context) {
		return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
	}

	/**
	 * Helper method to determine if the device has an extra-large screen. For
	 * example, 10" tablets are extra-large.
	 */
	@TargetApi(VERSION_CODES.GINGERBREAD)
	public static boolean isXLargeTablet(Context context) {
		return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
	}

	/**
	 * Hide keyboard.
	 * 
	 * @param activity from which to hide the keyboard.
	 */
	public static void hideKeyboard(Activity activity) {
		if (activity == null) {
			return;
		}

		try {
			InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
			if (imm != null) {
				View focus = activity.getCurrentFocus();
				if (focus != null) {
					imm.hideSoftInputFromWindow(focus.getWindowToken(), 0);
					imm.hideSoftInputFromWindow(focus.getApplicationWindowToken(), 0);
				}
			}
		} catch (Exception e) {
			LogHelper.e(TAG, "Unidentified Error", e);
		}
	}

	/**
	 * Show keyboard.
	 * 
	 * @param activity from which to show the keyboard.
	 * @param view the currently focused view, which would like to receive soft keyboard input.
	 */
	public static void showKeyboard(Activity activity, View view) {
		if (activity == null || view == null) {
			return;
		}

		try {
			InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
			if (imm != null) {
				imm.showSoftInput(view, InputMethodManager.SHOW_FORCED);
				imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
			}
		} catch (Exception e) {
			LogHelper.e(TAG, "Unidentified Error", e);
		}
	}
}
