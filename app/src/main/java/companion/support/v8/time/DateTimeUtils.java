package companion.support.v8.time;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Date and Time utility class.
 *
 * @author Vitor Ribeiro
 */
public class DateTimeUtils {

    /** Default Locale */
    public static Locale LOCALE = Locale.ENGLISH;

    // Default date and time formats
    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private static final String TIME_FORMAT = "HH:mm:ss";

    /** Hidden constructor to prevent instantiation. */
    private DateTimeUtils() {
    }

    /**
     * Check calendar reference and generate a new if invalid.
     * @param calendar reference.
     * @return calendar reference.
     */
    private static Calendar checkCalendar(Calendar calendar) {
        if (calendar == null) {
            calendar = Calendar.getInstance();
        }
        return calendar;
    }

    /**
     * Get calendar object for a given date and format.
     * @param date string type date.
     * @param dateFormat date format.
     * @return calendar object set to the given date and format.
     */
    private static Calendar getCalendar(String date, String dateFormat) {
        Calendar calendar = Calendar.getInstance();
        try {
            SimpleDateFormat format = new SimpleDateFormat(dateFormat, LOCALE);
            calendar.setTime(format.parse(date));
        } catch (Exception e) {
            calendar = null;
        }

        return calendar;
    }

    /**
     * Get calendar object for a given date.
     * @param date string type date.
     * @return calendar object set to the given date, or null if date is not parsable.
     */
    public static Calendar getCalendar(String date) {
        Calendar calendar = getCalendar(date, DATE_TIME_FORMAT);
        if (calendar == null) {
            calendar = getCalendar(date, DATE_FORMAT);
        }
        if (calendar == null) {
            calendar = getCalendar(date, TIME_FORMAT);
        }
        return calendar;
    }

    /**
     * Check if two calendars are set to the same date.
     * @param calendar1 first calendar to compare.
     * @param calendar2 second calendar to compare.
     * @return true if the date is equal, false otherwise.
     */
    public static boolean calendarDateIsEqual(Calendar calendar1, Calendar calendar2) {
        return (!(calendar1 == null || calendar2 == null) &&
                calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR) &&
                calendar1.get(Calendar.DAY_OF_YEAR) == calendar2.get(Calendar.DAY_OF_YEAR)
        );
    }

    /**
     * Check if two calendars are set to the same month of the year.
     * @param calendar1 first calendar to compare.
     * @param calendar2 second calendar to compare.
     * @return true if the date is equal, false otherwise.
     */
    public static boolean calendarMonthIsEqual(Calendar calendar1, Calendar calendar2) {
        return (!(calendar1 == null || calendar2 == null) &&
                calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR) &&
                calendar1.get(Calendar.MONTH) == calendar2.get(Calendar.MONTH)
        );
    }

    /**
     * Parse integer to a 24 hour timed string.
     * @param digit to parse.
     * @return two digit time string.
     */
    private static String getTwoDigitTimeString(int digit) {
        if (digit > 24) {
            digit = digit % 24;
        }

        if (digit < 10) {
            return "0" + digit;
        }

        return Integer.toString(digit);
    }

    /**
     * Get a calendar object from an Unix timestamp.
     * @param systemTime Unix timestamp.
     * @return Calendar object.
     */
    public static Calendar systemTimeToCalendar(long systemTime) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(systemTime);
        return calendar;
    }

    /**
     * Get current hour, minutes and seconds string.
     * @return hour, minutes and seconds.
     */
    public static String getCurrentTimeString() {
        return getHourMinuteSecondString(Calendar.getInstance());
    }

    /**
     * Get hour, minutes and seconds string.
     * @param calendar reference.
     * @return hour, minutes and seconds.
     */
    public static String getHourMinuteSecondString(Calendar calendar) {
        calendar = checkCalendar(calendar);
        return (
                getTwoDigitTimeString(calendar.get(Calendar.HOUR)) + ":" +
                        getTwoDigitTimeString(calendar.get(Calendar.MINUTE)) + ":" +
                        getTwoDigitTimeString(calendar.get(Calendar.SECOND))
        );
    }

    /**
     * Get hour and minutes string.
     * @param calendar reference.
     * @return hour and minutes.
     */
    public static String getHourMinuteString(Calendar calendar) {
        calendar = checkCalendar(calendar);
        return (getTwoDigitTimeString(calendar.get(Calendar.HOUR_OF_DAY)) + ":" +
                getTwoDigitTimeString(calendar.get(Calendar.MINUTE))
        );
    }

    /**
     * Get month and day string.
     * @param calendar reference.
     * @return month and day.
     */
    public static String getMonthDayString(Calendar calendar) {
        calendar = checkCalendar(calendar);
        return (calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, LOCALE) + " " +
                calendar.get(Calendar.DAY_OF_MONTH)
        );
    }

    /**
     * Get month and year string.
     * @param calendar reference.
     * @return month and day.
     */
    public static String getMonthYearString(Calendar calendar) {
        calendar = checkCalendar(calendar);
        return (calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, LOCALE) + " " +
                calendar.get(Calendar.YEAR)
        );
    }

    /**
     * Get month, day and year string.
     * @param calendar reference.
     * @return month, day and year.
     */
    public static String getMonthDayYearString(Calendar calendar) {
        calendar = checkCalendar(calendar);
        return (getMonthDayString(calendar) + ", " +
                calendar.get(Calendar.YEAR)
        );
    }

    /**
     * Get hour and minute string.
     * @param calendar reference.
     * @return hour and minute.
     */
    public static String getMonthDayYearHourMinuteString(Calendar calendar) {
        calendar = checkCalendar(calendar);
        return (getMonthDayYearString(calendar) + " at " +
                getHourMinuteString(calendar)
        );
    }

    /**
     * Get age relative to present day.
     * @param calendar reference.
     * @return years.
     */
    public static int getAge(Calendar calendar) {
        calendar = checkCalendar(calendar);
        return Calendar.getInstance().get(Calendar.YEAR) - calendar.get(Calendar.YEAR);
    }

    /**
     * Get a range string.
     * @param start first calendar.
     * @param end second calendar.
     * @return range.
     */
    public static String getMonthRange(Calendar start, Calendar end) {
        return getMonthDayString(start) + " - " + getMonthDayString(end);
    }

}
