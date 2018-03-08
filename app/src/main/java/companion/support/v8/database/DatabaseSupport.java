package companion.support.v8.database;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import companion.support.v8.os.Storage;
import companion.support.v8.util.LogHelper;
import companion.support.v8.util.zip.ZipUtils;

/** 
 * This class has static utility methods for dealing with databases.
 * 
 * @author Vitor Ribeiro
 *
 */
public class DatabaseSupport {

	/** Log Tag */
	private static final String TAG = DatabaseSupport.class.getSimpleName();

	/** This prevents the class from being instantiated. 
	 */
	private DatabaseSupport() {
	}

	/** Creates an INSERT string.
	 * @param tableName table name.
	 * @param columnNames array of column names.
	 * @return insert string.
	 */
	public static String createInsert(final String tableName, final String[] columnNames) {
		if (tableName == null || columnNames == null || columnNames.length == 0) {
			return null;
		}

		return "INSERT INTO " + tableName +
				createValuesExpr(columnNames);
	}

	/** Creates an INSERT OR IGNORE string.
	 * @param tableName table name.
	 * @param columnNames array of column names.
	 * @return insert string.
	 */
	public static String createInsertIgnore(final String tableName, final String[] columnNames) {
		if (tableName == null || columnNames == null || columnNames.length == 0) {
			return null;
		}

		return "INSERT OR IGNORE INTO " + tableName +
				createValuesExpr(columnNames);
	}

	/** Creates an INSERT OR REPLACE string.
	 * @param tableName table name.
	 * @param columnNames array of column names.
	 * @return insert string.
	 */
	public static String createInsertReplace(final String tableName, final String[] columnNames) {
		if (tableName == null || columnNames == null || columnNames.length == 0) {
			return null;
		}

		return "INSERT OR REPLACE INTO " + tableName +
				createValuesExpr(columnNames);
	}

	/** Creates a VALUES expression.
	 * @param columnNames array of column names.
	 * @return values expression.
	 */
	public static String createValuesExpr(final String[] columnNames) {
		if (columnNames == null || columnNames.length == 0) {
			return null;
		}

		final StringBuilder s = new StringBuilder();
		s.append(" (");

		for (String column : columnNames) {
			s.append(column).append(" ,");
		}

		int length = s.length();
		s.delete(length - 2, length);
		s.append(") VALUES( ");

		for (String columnName : columnNames) {
			s.append(" ? ,");
		}

		length = s.length();
		s.delete(length - 2, length);
		s.append(")");

		return s.toString();
	}

	/**
	 * SQL-escape a string.
	 */
	public static String sqlEscapeString(String sqlString) {
		StringBuilder sb = new StringBuilder();

		sb.append('\'');
		if (sqlString.indexOf('\'') != -1) {
			int length = sqlString.length();
			for (int i = 0; i < length; i++) {
				char c = sqlString.charAt(i);
				if (c == '\'') {
					sb.append('\'');
				}
				sb.append(c);
			}
		} else {
			sb.append(sqlString);
		}
		sb.append('\'');

		return sb.toString();
	}

	/**
	 * Concatenates two SQL WHERE clauses, handling empty or null values.
	 */
	public static String concatenateWhere(String a, String b) {
		if (TextUtils.isEmpty(a)) {
			return b;
		}
		if (TextUtils.isEmpty(b)) {
			return a;
		}

		return "(" + a + ") AND (" + b + ")";
	}

	/**
	 * Appends one set of selection args to another. This is useful when adding a selection
	 * argument to a user provided set.
	 */
	public static String[] appendSelectionArgs(String[] originalValues, String[] newValues) {
		if (originalValues == null || originalValues.length == 0) {
			return newValues;
		}
		String[] result = new String[originalValues.length + newValues.length];
		System.arraycopy(originalValues, 0, result, 0, originalValues.length);
		System.arraycopy(newValues, 0, result, originalValues.length, newValues.length);
		return result;
	}

	/**
	 * Check if the database already exists or if its version is superior.
	 * 
	 * @param database the full database path.
	 * @param version the database version.
	 * 
	 * @return true if it complies with the checks, false otherwise.
	 */
	public static boolean checkDatabase(String database, int version) {
		SQLiteDatabase db = null;
		try {
			// Check if the file exists
			if (Storage.checkFile(database)) {
				// Try to open database
				db = SQLiteDatabase.openDatabase(database, null, SQLiteDatabase.OPEN_READONLY);				
			} else {
				return false;
			}

			// Compare versions
			if (db.getVersion() < version) {
				return false;
			}

		} catch (Exception e) {
			return false;
		} finally {
			// Close database
			try {
				db.close();
			} catch (Exception e) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Copies a database from the local assets folder to another destination.
	 * 
	 * @param context caller's context.
	 * @param assetsPath the path of the asset.
	 * @param destination the destination path.
	 * @param database name in assets.
	 * @return true if successful, false otherwise.
	 */
	public static boolean copyDatabaseFromAssets(
		Context context, String assetsPath, 
		String destination, String database
	) {
		InputStream inputStream = null;
		FileOutputStream outputStream = null;

		// Verify paths
		if (assetsPath!=null && assetsPath.length()>0) {
			assetsPath += File.separator;
		} else {
			assetsPath = "";
		}
		if (destination!=null && destination.length()>0) {
			destination += File.separator;
		} else {
			destination = "";
		}

		try {
			// Open source and destination files
			inputStream = context.getAssets().open(assetsPath + database);
			outputStream = new FileOutputStream(destination + database);

			// Copy
			byte[] buf = new byte[1024];
			int bytesRead = 0;
			while ((bytesRead = inputStream.read(buf)) > 0) {
				outputStream.write(buf, 0, bytesRead);
			}
		} catch (Exception e) {
			return false;
		} finally {
			// Close the streams
			try {
				if (inputStream != null) {
					inputStream.close();
				}
			} catch (Exception e) {
				// Ignore
			}
			try {
				if (outputStream != null) {
					outputStream.flush();
				}
			} catch (Exception e) {
				// Ignore
			}
			try {
				if (outputStream != null) {
					outputStream.close();
				}
			} catch (Exception e) {
				// Ignore
			}
		}

		return true;
	}

	/**
	 * Copies a compressed database from the local assets folder to another destination.
	 * 
	 * @param context caller's context.
	 * @param assetsPath the path of the asset.
	 * @param destination the destination path.
	 * @param database name in assets.
	 * @param extension extension of compressed file.
	 * @return true if successful, false otherwise.
	 */
	public static boolean copyCompressedDatabaseFromAssets(Context context, String assetsPath, String destination, 
			String database, String extension) {
		InputStream inputStream = null;
		FileOutputStream outputStream = null;
		ByteArrayOutputStream bos = null;

		// Verify strings
		if (assetsPath!=null && assetsPath.length()>0) {
			assetsPath += File.separator;
		} else {
			assetsPath = "";
		}
		if (destination!=null && destination.length()>0) {
			destination += File.separator;
		} else {
			destination = "";
		}
		if (extension!=null && extension.length()>0) {
			extension = "." + extension;
		} else {
			extension = "";
		}

		try {
			// Open source and destination files
			inputStream = context.getAssets().open(assetsPath + database + extension);
			outputStream = new FileOutputStream(destination + database);
			bos = new ByteArrayOutputStream();

			// Copy
			byte[] buf = new byte[1024];
			int bytesRead = 0;
			while ((bytesRead = inputStream.read(buf)) > 0) {
				bos.write(buf, 0, bytesRead);
			}
			outputStream.write(ZipUtils.zlibToBytes(bos.toByteArray()));
		} catch (OutOfMemoryError e) {
			LogHelper.e(TAG, "Unidentified Error", e);
			return false;
		} catch (Exception e) {
			LogHelper.e(TAG, "Unidentified Error", e);
			return false;
		} finally {
			// Close the streams
			try {
				if (inputStream != null) {
					inputStream.close();
				}
			} catch (Exception e) {
				// Ignore
			}
			try {
				if (outputStream != null) {
					outputStream.flush();
				}
			} catch (Exception e) {
				// Ignore
			}
			try {
				if (outputStream != null) {
					outputStream.close();
				}
			} catch (Exception e) {
				// Ignore
			}
			try {
				if (bos != null) {
					bos.flush();
				}
			} catch (Exception e) {
				// Ignore
			}
			try {
				if (bos != null) {
					bos.close();
				}
			} catch (Exception e) {
				// Ignore
			}
		}

		return true;
	}
}
