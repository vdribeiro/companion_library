package companion.support.v8.util;

import java.util.Collection;

import android.content.Context;
import android.support.v8.BuildConfig;
import android.util.Log;

import companion.support.v8.lang.ExceptionParser;

/**
 * Log class to print messages in the LogCat, controlling whether it only appears in debug mode. 
 * It has an error parser that deobfuscates class and package names.
 * 
 * @author Vitor Ribeiro
 *
 */
public class LogHelper {

	/** Log tag. */
	public static final String TAG = LogHelper.class.getSimpleName();

	/** Write to LogCat only on debug mode. */
	public static boolean debugOnly = true;

	/** Interface responsible for parsing a Throwable and providing a short description. */
	public static ExceptionParser exceptionParser = null;

	/** Initialize parser.
	 * @param context caller's context.
	 * @param packages packages to track.
	 * @param debug true to write on Log only on debug mode.
	 */
	public static synchronized void init(Context context, Collection<String> packages, boolean debug) {
		try {
			exceptionParser = new ExceptionParser(context, packages, debug);
			debugOnly = debug;
		} catch (Throwable e) {
			Log.e(TAG, "Cannot initialize exception parser");
		}
	}

	/**
	 * Set if the log messages appear on debug mode only.
	 * 
	 * @param debug true to appear on debug mode only, false otherwise.
	 */
	public static void setDebug(boolean debug) {
		debugOnly = debug;
	}

	/**
	 * Hit count an error level throwable.
	 * 
	 * @param tag name, if it is null this class tag will be used.
	 * @param description of the problem, can be null.
	 * @param throwable object, if it is null no stack trace will be added in the report.
	 */
	public static synchronized void e(String tag, String description, Throwable throwable) {
		if (!debugOnly || BuildConfig.DEBUG) {
			try {
				String[] result = exceptionParser.getThrowable(tag, description, throwable);
				Log.e(result[0], result[1]);
			} catch (Throwable e) {
				Log.e(tag, description, throwable);	
			}
		}
	}

	/**
	 * Hit count an error level message.
	 * 
	 * @param tag name, if it is null this class tag will be used.
	 * @param description of the message, can be null.
	 */
	public static synchronized void e(String tag, String description) {
		e(tag, description, null);
	}

	/**
	 * Hit count a warning level throwable.
	 * 
	 * @param tag name, if it is null this class tag will be used.
	 * @param description of the problem, can be null.
	 * @param throwable object, if it is null no stack trace will be added in the report.
	 */
	public static synchronized void w(String tag, String description, Throwable throwable) {
		if (!debugOnly || BuildConfig.DEBUG) {
			try {
				String[] result = exceptionParser.getThrowable(tag, description, throwable);
				Log.w(result[0], result[1]);	
			} catch (Throwable e) {
				Log.w(tag, description, throwable);
			}
		}
	}

	/**
	 * Hit count a warning level message.
	 * 
	 * @param tag name, if it is null this class tag will be used.
	 * @param description of the message, can be null.
	 */
	public static synchronized void w(String tag, String description) {
		w(tag, description, null);
	}

	/**
	 * Hit count an info level throwable.
	 * 
	 * @param tag name, if it is null this class tag will be used.
	 * @param description of the problem, can be null.
	 * @param throwable object, if it is null no stack trace will be added in the report.
	 */
	public static synchronized void i(String tag, String description, Throwable throwable) {
		if (!debugOnly || BuildConfig.DEBUG) {
			try {
				String[] result = exceptionParser.getThrowable(tag, description, throwable);
				Log.i(result[0], result[1]);	
			} catch (Throwable e) {
				Log.i(tag, description, throwable);
			}
		}
	}

	/**
	 * Hit count an info level message.
	 * 
	 * @param tag name, if it is null this class tag will be used.
	 * @param description of the message, can be null.
	 */
	public static synchronized void i(String tag, String description) {
		i(tag, description, null);
	}

	/**
	 * Hit count a debug level throwable.
	 * 
	 * @param tag name, if it is null this class tag will be used.
	 * @param description of the problem, can be null.
	 * @param throwable object, if it is null no stack trace will be added in the report.
	 */
	public static synchronized void d(String tag, String description, Throwable throwable) {
		if (!debugOnly || BuildConfig.DEBUG) {
			try {
				String[] result = exceptionParser.getThrowable(tag, description, throwable);
				Log.d(result[0], result[1]);	
			} catch (Throwable e) {
				Log.d(tag, description, throwable);
			}
		}
	}

	/**
	 * Hit count a debug level message.
	 * 
	 * @param tag name, if it is null this class tag will be used.
	 * @param description of the message, can be null.
	 */
	public static synchronized void d(String tag, String description) {
		d(tag, description, null);
	}

	/**
	 * Hit count a verbose level throwable.
	 * 
	 * @param tag name, if it is null this class tag will be used.
	 * @param description of the problem, can be null.
	 * @param throwable object, if it is null no stack trace will be added in the report.
	 */
	public static synchronized void v(String tag, String description, Throwable throwable) {
		if (!debugOnly || BuildConfig.DEBUG) {
			try {
				String[] result = exceptionParser.getThrowable(tag, description, throwable);
				Log.v(result[0], result[1]);
			} catch (Throwable e) {
				Log.v(tag, description, throwable);
			}
		}
	}

	/**
	 * Hit count a verbose level message.
	 * 
	 * @param tag name, if it is null this class tag will be used.
	 * @param description of the message, can be null.
	 */
	public static synchronized void v(String tag, String description) {
		v(tag, description, null);
	}
}
