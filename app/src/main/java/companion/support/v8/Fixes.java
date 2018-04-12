package companion.support.v8;

import android.content.Context;

import companion.support.v8.net.NetworkUtils;
import companion.support.v8.security.PRNGUtils;
import companion.support.v8.util.LogHelper;

/**
 * Class for important android fixes.
 * 
 * @author Vitor Ribeiro
 *
 */
public class Fixes {
	
	/** Log tag. */
	private static final String TAG = Fixes.class.getSimpleName();

	/** Hidden constructor to prevent instantiation. */
	private Fixes() {
	}
	
	public static void applyAll(Context context) {
		applyPRNGFixes();
		applyNetworkFixes(context);
	}

	/** Fix for the output of the default PRNG having low entropy. */
	public static void applyPRNGFixes() {
		try {
			PRNGUtils.applyFix();
		} catch (Exception e) {
			LogHelper.e(TAG, "Could not apply security fix", e);
		}
	}
	
	/** Fixes for network. */
	public static void applyNetworkFixes(Context context) {
		try {
			NetworkUtils.disableConnectionReuseIfNecessary();
			NetworkUtils.enableHttpResponseCache(context);
		} catch (Exception e) {
			LogHelper.e(TAG, "Could not apply network fix", e);
		}
	}
}
