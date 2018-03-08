package companion.support.v8.telephony;

import android.telephony.TelephonyManager;

/**
 * Utility class for telephony.
 * 
 * @author Vitor Ribeiro
 * 
 */
public class TelephonyUtils {

	/** This prevents the class from being instantiated. 
	 */
	private TelephonyUtils() {
	}
	
	/**
	 * Converts latitude or longitude from 0.25 seconds 
	 * (as defined in the 3GPP2 C.S0005-A v6.0 standard) to decimal degrees.
	 * 
	 * @param quartSec latitude or longitude in 0.25 seconds units
	 * @return latitude or longitude in decimal degrees units
	 */
	public static double quartSecToDecDegrees(int quartSec) {
		if (Double.isNaN(quartSec) || quartSec < -2592000 || quartSec > 2592000){
			// Invalid value
			return Double.NaN;
		}
		return ((double) quartSec) / (3600 * 4);
	}
	
	public static int asuToDBm(int asu) {
		return (2 * asu - 113);
	}

	/** Get the type of radio used to transmit voice calls. 
	 * Usually GSM or CDMA.
	 * @param type constant of the {@code TelephonyManager} object.
	 * @return String representation of the phone type.
	 */
	public static String getPhoneType(int type) {
		String phoneType = null;
		switch (type) {
		case TelephonyManager.PHONE_TYPE_CDMA:
			phoneType = "CDMA";
			break;
		case TelephonyManager.PHONE_TYPE_GSM:
			phoneType = "GSM";
			break;
		case TelephonyManager.PHONE_TYPE_SIP:
			phoneType = "SIP";
			break;
		default:
			phoneType = "NONE";
			break;
		}
		return phoneType;
	}

	/** Get the network type.
	 * @param type constant of the {@code TelephonyManager} object.
	 * @return String representation of the network type.
	 */
	public static String getNetworkType(int type) {
		String networkType = null;
		switch (type) {
		case TelephonyManager.NETWORK_TYPE_1xRTT:
			networkType = "1xRTT";
			break;
		case TelephonyManager.NETWORK_TYPE_CDMA:
			networkType = "CDMA";
			break;
		case TelephonyManager.NETWORK_TYPE_EDGE:
			networkType = "EDGE";
			break;
		case TelephonyManager.NETWORK_TYPE_EHRPD:
			networkType = "EHRPD";
			break;
		case TelephonyManager.NETWORK_TYPE_EVDO_0:
			networkType = "EVDO 0";
			break;
		case TelephonyManager.NETWORK_TYPE_EVDO_A:
			networkType = "EVDO A";
			break;
		case TelephonyManager.NETWORK_TYPE_EVDO_B:
			networkType = "EVDO B";
			break;
		case TelephonyManager.NETWORK_TYPE_GPRS:
			networkType = "GPRS";
			break;
		case TelephonyManager.NETWORK_TYPE_HSDPA:
			networkType = "HSDPA";
			break;
		case TelephonyManager.NETWORK_TYPE_HSPA:
			networkType = "HSPA";
			break;
		case TelephonyManager.NETWORK_TYPE_HSPAP:
			networkType = "HSPAP";
			break;
		case TelephonyManager.NETWORK_TYPE_HSUPA:
			networkType = "HSUPA";
			break;
		case TelephonyManager.NETWORK_TYPE_IDEN:
			networkType = "IDEN";
			break;
		case TelephonyManager.NETWORK_TYPE_LTE:
			networkType = "LTE";
			break;
		case TelephonyManager.NETWORK_TYPE_UMTS:
			networkType = "UMTS";
			break;
		default:
			networkType = "UNKNOWN";
			break;
		}
		return networkType;
	}
}
