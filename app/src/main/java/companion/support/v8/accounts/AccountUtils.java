package companion.support.v8.accounts;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;

import companion.support.v8.util.LogHelper;

/**
 * This class provides account management utility methods.
 * 
 * @author Vitor Ribeiro
 * 
 */
public class AccountUtils {

	/** Log tag. */
	private static final String TAG = AccountUtils.class.getSimpleName();

	/**
	 * Get the default Google account
	 * 
	 * @param context the caller's context object.
	 * @return a string representing the default account (like user@gmail.com), or null if none is found.
	 * 
	 */
	public static String getDefaultAccount(Context context) {
		Account[] accounts = getAllGoogleAccounts(context);
		if (accounts==null) {
			return null;
		}

		if (accounts.length > 0) {
			return accounts[0].name;
		}

		return null;
	}
	
	/**
	 * Get all Google accounts.
	 * 
	 * @param context the caller's context object.
	 * @return An array of all the Google accounts in the smartphone, or null if none are found.
	 * 
	 */
	public static Account[] getAllGoogleAccounts(Context context) {
		AccountManager mAccount = null;
		Account[] accounts = null;

		try {
			mAccount = AccountManager.get(context);
			accounts = mAccount.getAccountsByType("com.google");
		} catch (Exception e) {
			LogHelper.e(TAG, "Account Manager error", e);
			return null;
		}

		return accounts;
	}

	/**
	 * Get all Google account names.
	 * 
	 * @param context the caller's context object.
	 * @return An array of the string representation (like user@gmail.com)
	 *         of all the Google accounts in the smartphone, or null if none are found.
	 * 
	 */
	public static String[] getAllGoogleAccountsNames(Context context) {
		Account[] accounts = getAllGoogleAccounts(context);
		if (accounts==null) {
			return null;
		}

		int size = accounts.length;
		String[] names = new String[size];
		for (int i = 0; i < size; i++) {
			names[i] = accounts[i].name;
		}

		return names;
	}

	/**
	 * Verifies if a user is valid iterating all the Google accounts in the smartphone.
	 * 
	 * @param context the caller's context object.
	 * @param gUser Google account to verify.
	 * @return true if the user exists, false otherwise.
	 * 
	 */
	public static boolean verifyGoogleAccount(Context context, String gUser) {
		if (context == null || gUser == null) {
			return false;
		}

		String[] accounts = getAllGoogleAccountsNames(context);
		if (accounts==null) {
			return false;
		}

		for (String account : accounts) {
			if (gUser.equalsIgnoreCase(account)) {
				return true;
			}
		}

		return false;
	}
}
