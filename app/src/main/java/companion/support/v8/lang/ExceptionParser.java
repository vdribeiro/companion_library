package companion.support.v8.lang;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import android.content.Context;
import android.content.pm.ActivityInfo;

import companion.support.v8.util.LogHelper;

/**
 * Because of obfuscation tools (like Proguard), Google Analytics reports the obfuscated class name of the code 
 * without its package name. Without knowing the package name, there is no way to deobfuscate the class name 
 * with the mapping file as multiple classes can have the same obfuscated class names as long as their package names are distinct.
 * This implementation of a StandardExceptionParser fixes that issue. 
 * 
 * @see com.google.android.gms.analytics.StandardExceptionParser
 * 
 * @author Vitor Ribeiro
 *
 */
@SuppressWarnings({"rawtypes","unchecked"})
public class ExceptionParser {

	/** Log Tag */
	private static final String TAG = ExceptionParser.class.getSimpleName();

	private final TreeSet<String> includedPackages;

	public ExceptionParser(Context context, Collection<String> additionalPackages, boolean debug) {
		this.includedPackages = new TreeSet();
		this.setIncludedPackages(context, additionalPackages);
		LogHelper.setDebug(debug);
	}

	public ExceptionParser(Context context, Collection<String> additionalPackages) {
		this(context, additionalPackages, true);
	}

	public void setIncludedPackages(Context context, Collection<String> additionalPackages) {
		this.includedPackages.clear();
		Set<String> packages = new HashSet();
		if (additionalPackages != null) {
			packages.addAll(additionalPackages);
		}

		if (context != null) {
			ActivityInfo[] ai;
			try {
				String appPackage = context.getApplicationContext().getPackageName();
				this.includedPackages.add(appPackage);
				ai = context.getApplicationContext().getPackageManager().getPackageInfo(appPackage, 15).activities;
				if (ai != null) {
					ActivityInfo[] aInfo = ai;
					int len = aInfo.length;
					int i = 0;
					while (i < len) {
						packages.add(aInfo[i].packageName);
						i++;
					}
				}
			} catch (Exception unimportantException) {
				// Ignore
			}
		}

		Iterator r5Iterator = packages.iterator();
		while (r5Iterator.hasNext()) {
			String packageName = (String) r5Iterator.next();
			boolean needToAdd = true;
			Iterator it = this.includedPackages.iterator();
			while (it.hasNext()) {
				String oldName = (String) it.next();
				if (!packageName.startsWith(oldName)) {
					if (oldName.startsWith(packageName)) {
						this.includedPackages.remove(oldName);
					}
				} else {
					needToAdd = false;
				}
			}

			if (needToAdd) {
				this.includedPackages.add(packageName);
			}
		}
	}

	public StackTraceElement getBestStackTraceElement(Throwable t) {
		StackTraceElement[] elements = t.getStackTrace();
		if (elements == null || elements.length == 0) {
			return null;
		} else {
			StackTraceElement[] array = elements;
			int len = array.length;
			int i = 0;
			while (i < len) {
				StackTraceElement e = array[i];
				String className = e.getClassName();
				Iterator i$_2 = this.includedPackages.iterator();
				while (i$_2.hasNext()) {
					if (className.startsWith((String) i$_2.next())) {
						return e;
					}
				}
				i = i + 1;
			}
			return elements[0];
		}
	}

	public Throwable getCause(Throwable t) {
		Throwable result = t;
		while (result.getCause() != null) {
			result = result.getCause();
		}
		return result;
	}

	public String getDescription(Throwable cause, StackTraceElement element, String threadName) {
		StringBuilder descriptionBuilder = new StringBuilder();

		if (cause!=null) {
			descriptionBuilder.append(cause.getClass().getSimpleName());
		}

		if (element != null) {
			descriptionBuilder.append(
				String.format(" (@%s:%s:%s)", element.getClassName(), 
				element.getMethodName(), element.getLineNumber())
			);
		}

		if (threadName != null) {
			descriptionBuilder.append(String.format(" {%s}", threadName));
		}

		Object[] r4ObjectA;
		descriptionBuilder.append("\n" + cause.getClass().getSimpleName());
		
		if (element != null) {
			String[] classNameParts = element.getClassName().split("\\.");
			String className = "unknown";
			
			if (classNameParts == null || classNameParts.length <= 0) {
				r4ObjectA = new Object[3];
				r4ObjectA[0] = className;
				r4ObjectA[1] = element.getMethodName();
				r4ObjectA[2] = Integer.valueOf(element.getLineNumber());
				descriptionBuilder.append(String.format(" (@%s:%s:%s)", r4ObjectA));
			} else {
				className = classNameParts[classNameParts.length - 1];
				r4ObjectA = new Object[3];
				r4ObjectA[0] = className;
				r4ObjectA[1] = element.getMethodName();
				r4ObjectA[2] = Integer.valueOf(element.getLineNumber());
				descriptionBuilder.append(String.format(" (@%s:%s:%s)", r4ObjectA));
			}
		}
		
		if (threadName != null) {
			r4ObjectA = new Object[1];
			r4ObjectA[0] = threadName;
			descriptionBuilder.append(String.format(" {%s}", r4ObjectA));
		}
		
		return descriptionBuilder.toString();
	}

	public String getDescription(String threadName, Throwable throwable) {
		String description = null;
		try {
			Throwable cause = getCause(throwable);
			description = getDescription(cause, getBestStackTraceElement(cause), threadName);
		} catch (Exception unimportantException) {}

		return description;
	}

	/**
	 * Parse a throwable.
	 * 
	 * @param threadName name, if it is null this class tag will be used.
	 * @param description of the problem, can be null.
	 * @param throwable object, if it is null no stack trace will be added in the report.
	 * 
	 * @return parsed string values.
	 */
	public synchronized String[] getThrowable(String threadName, String description, Throwable throwable) {
		if (threadName==null) {
			threadName = TAG;
		}

		if (throwable!=null) {
			String desc = getDescription(threadName, throwable);
			if (description!=null && description.length()>0) {
				description += " - " + desc;
			} else {
				description = desc;
			}
		}

		return new String[] {threadName, description};
	}
}