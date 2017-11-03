package companion.support.v8.time;

import java.util.Calendar;

/**
 * Utility class for time functions.
 * 
 * @author Vitor Ribeiro
 * 
 */
public class CalendarUtils {

	/** This prevents the class from being instantiated. 
	 */
	private CalendarUtils() {
	}
	
	public static String getCurrentTimeString(Calendar calendar) {
		return (
			getTwoDigitString(calendar.get(Calendar.HOUR)) + ":" + 
			getTwoDigitString(calendar.get(Calendar.MINUTE)) + ":" + 
			getTwoDigitString(calendar.get(Calendar.SECOND))
		);
	}

	public static String getCurrentTimeString() {
		return getCurrentTimeString(Calendar.getInstance());
	}
	
	public static String getCurrentTimeString(long systemTime) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(systemTime);
		return getCurrentTimeString(calendar);
	}

	public static String getTwoDigitString(int digit) {
		if (digit<10) {
			return "0" + digit;
		}

		return Integer.toString(digit);
	}

}
