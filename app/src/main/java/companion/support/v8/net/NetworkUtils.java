package companion.support.v8.net;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;

import companion.support.v8.os.Utils;
import companion.support.v8.util.LogHelper;

/**
 * Utility class with a bundle of network methods.
 * 
 * @author Vitor Ribeiro
 * 
 */
public class NetworkUtils {

	/** Log tag. */
	private final static String TAG = NetworkUtils.class.getSimpleName();

	/** This prevents the class from being instantiated. 
	 */
	private NetworkUtils() {
	}

	/**
	 * Download a file from an URL and write the content to an output stream.
	 *
	 * @param urlString the URL to fetch.
	 * @return true if successful, false otherwise.
	 */
	public static boolean downloadUrlToStream(String urlString, OutputStream outputStream) {
		disableConnectionReuseIfNecessary();
		HttpURLConnection urlConnection = null;
		BufferedOutputStream out = null;
		BufferedInputStream in = null;

		try {
			final URL url = new URL(urlString);
			urlConnection = (HttpURLConnection) url.openConnection();
			in = new BufferedInputStream(urlConnection.getInputStream());
			out = new BufferedOutputStream(outputStream);

			int b;
			while ((b = in.read()) != -1) {
				out.write(b);
			}
			return true;
		} catch (final IOException e) {
			LogHelper.e(TAG, "Error in downloadBitmap - " + e);
		} finally {
			if (urlConnection != null) {
				urlConnection.disconnect();
			}
			try {
				if (out != null) {
					out.close();
				}
				if (in != null) {
					in.close();
				}
			} catch (final IOException e) {
				// Ignore
			}
		}
		return false;
	}

	/**
	 * Prior to Froyo, calling close() on a readable InputStream could poison the connection pool.
	 * @see <a href="http://android-developers.blogspot.com/2011/09/androids-http-clients.html">http://android-developers.blogspot.com/2011/09/androids-http-clients.html</a> 
	 */
	public static void disableConnectionReuseIfNecessary() {
		// HTTP connection reuse which was buggy pre-froyo
		if (!Utils.hasFroyo()) {
			System.setProperty("http.keepAlive", "false");
		}
	}

	/** Use reflection to enable HTTP response caching on devices that support it.
	 * @param context of the caller.
	 * @throws Exception on error.
	 */
	public static void enableHttpResponseCache(Context context) throws Exception {
		long httpCacheSize = 10 * 1024 * 1024; // 10 MiB
		File httpCacheDir = new File(context.getCacheDir(), "http");
		Class.forName("android.net.http.HttpResponseCache")
		.getMethod("install", File.class, long.class)
		.invoke(null, httpCacheDir, httpCacheSize);
	}

	/** Reset the preferred network to the system default.
	 * @param context caller's context.
	 */
	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	@SuppressWarnings("deprecation")
	public static void resetToDefaultNetwork(Context context) {
		if (Utils.hasLollipop()) {
			ConnectivityManager.setProcessDefaultNetwork(null);
		} else {
			ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			if (cm != null) {
				cm.setNetworkPreference(ConnectivityManager.DEFAULT_NETWORK_PREFERENCE);
			}
		}
	}

	/** Set the preferred network to Wifi.
	 * <p>
	 * WARNING: This method might not work starting API 21.
	 * @param context caller's context.
	 */
	@SuppressWarnings("deprecation")
	public static void setToWifiNetwork(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (cm != null) {
			cm.setNetworkPreference(ConnectivityManager.TYPE_WIFI);
		}
	}

	/** Set the preferred network to Mobile.
	 * <p>
	 * WARNING: This method might not work starting API 21.
	 * @param context caller's context.
	 */
	@SuppressWarnings("deprecation")
	public static void setToMobileNetwork(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (cm != null) {
			cm.setNetworkPreference(ConnectivityManager.TYPE_MOBILE);
		}
	}

	/** Get whether the network connectivity exists and it is possible to establish connections 
	 * and transmit data for all types of connections.
	 * <p>
	 * WARNING: This method might not work starting API 21.
	 * 
	 * @param context caller's context.
	 * @return boolean array of connectivity states: 0 - Ethernet; 1 - Wifi; 2 - Mobile; 3 - Bluetooth; 4 - WiMax.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	public static boolean[] getConnectionStatus(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

		boolean[] state = new boolean[] {false, false, false, false, false, false};

		if (cm == null) {
			return state;
		}

		if (Utils.hasHoneycombMR2()) {
			NetworkInfo ethernet = cm.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET);
			if (ethernet!=null) state[0] = ethernet.isConnected();
		}

		NetworkInfo wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		if (wifi!=null) state[1] = wifi.isConnected();

		NetworkInfo mobile = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		if (mobile!=null) state[2] = mobile.isConnected();

		if (Utils.hasHoneycombMR2()) {
			NetworkInfo bluetooth = cm.getNetworkInfo(ConnectivityManager.TYPE_BLUETOOTH);
			if (bluetooth!=null) state[3] = bluetooth.isConnected();
		}

		NetworkInfo wimax = cm.getNetworkInfo(ConnectivityManager.TYPE_WIMAX);
		if (wimax!=null) state[4] = wimax.isConnected();

		return state;
	}

	/** Get whether the Wifi connectivity exists and it is possible to establish connections and transmit data.
	 * <p>
	 * WARNING: This method might not work starting API 21.
	 * 
	 * @param context caller's context.
	 * @return true if connectivity exists, false otherwise.
	 */
	public static boolean getWifiStatus(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (cm == null) {
			return false;
		}

		boolean state = false;

		NetworkInfo wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		if (wifi!=null) state = wifi.isConnected();

		return state;
	}

	/** Get whether the Mobile connectivity exists and it is possible to establish connections and transmit data.
	 * <p>
	 * WARNING: This method might not work starting API 21.
	 * 
	 * @param context caller's context.
	 * @return true if connectivity exists, false otherwise.
	 */
	public static boolean getMobileStatus(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (cm == null) {
			return false;
		}

		boolean state = false;

		NetworkInfo mobile = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		if (mobile!=null) state = mobile.isConnected();

		return state;
	}

	/** Checks Internet availability using the active network.
	 * 
	 * @param context caller's context.
	 * @return true if Internet is available, false otherwise.
	 */
	public static boolean isInternetAvailable(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (cm == null) {
			return false;
		}
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isAvailable() && netInfo.isConnected();
    }

	/** Checks Internet availability by sending a ping to Google.
	 *  
	 * @return true if there is Internet, false otherwise.
	 */
	public static boolean isInternetAvailable() {
		try {
			Process ipProcess = Runtime.getRuntime().exec("/system/bin/ping -c 1 8.8.8.8");
			int exitValue = ipProcess.waitFor();
			return (exitValue == 0);
		} catch (Exception e) {
			LogHelper.w(TAG, "Could not ping Google: " + e.getMessage());
		}

		return false;
	}

	/** Checks if there is an active Internet connection.
	 *  
	 * @return true if there is Internet, false otherwise.
	 */
	public static boolean isInternetConnected(int timeout) {
		//POST
		HttpURLConnection conn = null;
		try {
			URL url = new URL("http://www.gstatic.com/generate_204");
			conn = (HttpURLConnection) url.openConnection();
			HttpURLConnection.setFollowRedirects(true);
			conn.setConnectTimeout(timeout);
			conn.setReadTimeout(timeout);
			conn.setRequestMethod("GET");
			conn.setUseCaches(false);
			conn.connect();

			Thread.yield();

			int responseCode = conn.getResponseCode();
			LogHelper.i(TAG, "gstatic response code: " + responseCode);
			if (responseCode == HttpsURLConnection.HTTP_NO_CONTENT) {
				return true;
			}

		} catch (Exception e) {
			LogHelper.e(TAG, "Cannot communicate with server", e);
		} finally {
			try {
				conn.disconnect();
			} catch (Exception unimportantException) {
				// Ignore
			}
		}

		return false;
	}

	/** Checks server availability. 
	 * This method first tries to use ICMP (ICMP ECHO REQUEST), 
	 * falling back to a TCP connection on port 7 (Echo).
	 * 
	 * @param serverAddress server address to reach.
	 * @param timeout in milliseconds before the test fails if no connection could be established.
	 * @return true if server is reachable, false otherwise.
	 */
	public static boolean isServerAvailable(String serverAddress, int timeout) {
		boolean res = false;

		// check if the server address is reachable
		try {
			InetAddress addr = InetAddress.getByName(serverAddress);
			res = addr.isReachable(timeout);
		} catch (Exception e) {
			LogHelper.w(TAG, "Could not reach server with ICMP (ICMP ECHO REQUEST) " +
					"or TCP connection on port 7 (Echo) : " + e.getMessage());
			res = false;
		}
		if (res) {
			LogHelper.w(TAG, serverAddress + " reachable");
		} else {
			LogHelper.w(TAG, serverAddress + " not reachable");
		}

		return res;
	}
}
