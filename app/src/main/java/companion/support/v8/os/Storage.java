package companion.support.v8.os;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.StatFs;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.v4.content.CursorLoader;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Utility class for storage methods.
 * 
 * @author Vitor Ribeiro
 * 
 */
public class Storage {

	/** Log Tag. */
	private static final String TAG = Storage.class.getSimpleName();

	/** MIME types.
	 * */
	public static final String MIME_TYPE_ALL = "*/*";
	public static final String MIME_TYPE_TEXT = "text/*";
	public static final String MIME_TYPE_AUDIO = "audio/*";
	public static final String MIME_TYPE_IMAGE = "image/*";
	public static final String MIME_TYPE_VIDEO = "video/*";
	public static final String MIME_TYPE_APP = "application/*";
	public static final String MIME_TYPE_BINARY = "application/octet-stream";

	/** Providers from uri.
	 */
	private static final String EXTERNAL_STORAGE_PROVIDER = "com.android.externalstorage.documents";
	private static final String DOWNLOADS_PROVIDER = "com.android.providers.downloads.documents";
	private static final String MEDIA_PROVIDER = "com.android.providers.media.documents";
	private static final String GOOGLE_PHOTOS_PROVIDER = "com.google.android.apps.photos.content";

	/** This prevents the class from being instantiated. 
	 */
	private Storage() {
	}

	/** Check if the primary "external" storage device is writable.
	 * 
	 * @return true if writable, false otherwise.
	 */
	public static boolean isWritable() {
		String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equalsIgnoreCase(state);
    }

	/** Check if the primary "external" storage device is readable.
	 * 
	 * @return true if readable, false otherwise.
	 */
	public static boolean isReadable() {
		String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equalsIgnoreCase(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equalsIgnoreCase(state);
    }

	/** Get internal storage absolute path.
	 * @param context of the caller.
	 * @return absolute path or null if not available.
	 */
	public static String getStorageDir(Context context) {
		File filesDir = context.getFilesDir();
		if (filesDir != null) {
			return filesDir.getAbsolutePath() + File.separator;
		}

		return null;
	}

	/** Get external storage absolute path.
	 * @param context of the caller.
	 * @return absolute path or null if not available.
	 */
	public static String getExternalStorageDir(Context context) {
		if (Storage.isWritable()) {
			File filesDir = context.getExternalFilesDir(null);
			if (filesDir != null) {
				return filesDir.getAbsolutePath() + File.separator;
			}
		}

		return null;
	}

	/** Get the absolute path of the primary available storage. 
	 * @param context caller's context.
	 * @return absolute path.
	 */
	public static String getAvailableStorageDir(Context context) {
		String storage = getExternalStorageDir(context);

		if (assertDirectory(storage)) {
			return storage;
		}
		// If it fails getting external dir, get internal one
		storage = getStorageDir(context);

		if (assertDirectory(storage)) {
			return storage;
		}

		return "";
	}

	/**
	 * Get the cache directory.
	 * @param context to use.
	 * @return The cache path.
	 */
	public static String getCacheDir(Context context) {
		File filesDir = context.getCacheDir();
		if (filesDir != null) {
			return filesDir.getAbsolutePath() + File.separator;
		}

		return null;
	}

	/**
	 * Get the external cache directory.
	 * @param context to use.
	 * @return The external cache path.
	 */
	public static String getExternalCacheDir(Context context) {
		if (Storage.isWritable()) {
			if (Utils.hasFroyo()) {
				File filesDir = context.getExternalCacheDir();
				if (filesDir != null) {
					return filesDir.getAbsolutePath() + File.separator;
				}
			}

			// Before Froyo we need to construct the external cache dir ourselves
			return "/Android/data/" + context.getPackageName() + "/cache/";
		}

		return null;
	}

	/**
	 * Get a usable cache directory (external if available, internal otherwise).
	 *
	 * @param context to use.
	 * @return cache path.
	 */
	public static String getAvailableCacheDir(Context context) {
		String storage = getExternalCacheDir(context);

		if (storage==null) {
			storage = getCacheDir(context);
		}

		if (assertDirectory(storage)) {
			return storage;
		}

		return File.separator;
	}

	/**
	 * Create a JPEG file on the local storage.
	 * @param context of the caller.
	 * @return file.
	 * @throws Throwable it storage is unavailable.
	 */
	public static File getStorageDir(Context context, String dir) throws Throwable {
		String state = Environment.getExternalStorageState();
		if (!state.equals(Environment.MEDIA_MOUNTED)) {
			throw new Throwable("Storage unavailable");
		}

		// Create the storage directory if it does not exist
		File mediaStorageDir = new File(context.getExternalFilesDir(dir), TAG);
		if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()) {
			throw new Throwable("Storage unavailable");
		}

		return mediaStorageDir;
	}

	/**
	 * Create a JPEG file on the local storage.
	 * @param context of the caller.
	 * @return file.
	 * @throws Throwable it storage is unavailable.
	 */
	public static File createPhotoFile(Context context) throws Throwable {
		File mediaStorageDir = getStorageDir(context, Environment.DIRECTORY_PICTURES);
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
		return new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");
	}

	/**
	 * Create a MP4 file on the local storage.
	 * @param context of the caller.
	 * @return file.
	 * @throws Throwable it storage is unavailable.
	 */
	public static File createVideoFile(Context context) throws Throwable {
		File mediaStorageDir = getStorageDir(context, Environment.DIRECTORY_MOVIES);
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
		return new File(mediaStorageDir.getPath() + File.separator + "MOV_" + timeStamp + ".mp4");
	}

	/**
	 * Get parent path.
	 *
	 * @param dbFile absolute path.
	 * @return parent path.
	 */
	public static String getParent(String dbFile) {
		String dbPath = null;
		try {
			dbPath = new File(dbFile).getParent();
			if (dbPath==null) {
				dbPath = File.separator;
			} else {
				dbPath += File.separator;
			}
		} catch (Exception e) {
			// Ignore
		}

		return dbPath;
	}

	/**
	 * Check if external storage is built-in or removable.
	 *
	 * @return True if external storage is removable (like an SD card), false
	 *         otherwise.
	 */
	@SuppressLint("NewApi")
	public static boolean isExternalStorageRemovable() {
		if (Utils.hasGingerbread()) {
			return Environment.isExternalStorageRemovable();
		}
		return true;
	}

	/** Given a file path, converts said file to a byte array.
	 * @param path file path.
	 * @return file contents in an array of bytes.
	 */
	public static byte[] fileToBytes(String path) {
		byte[] fileBuffer = null;
		try {
			File file = new File(path);
			FileInputStream fileInputStream = new FileInputStream(file);
			fileBuffer = new byte[(int) file.length()];
			fileInputStream.read(fileBuffer);
			fileInputStream.close();
		} catch (Exception e) {
			fileBuffer = null;
		}
		return fileBuffer;
	}

	/** Asserts a directory.
	 * 
	 * @param path directory to assert.
	 * @return true if the directory is available, false otherwise.
	 */
	public static boolean assertDirectory(String path) {
		if (path == null) {
			return false;
		}

		try {
			File directory = new File(path);
			return (directory.mkdirs() || directory.isDirectory());
		} catch (Exception e) {
			// Ignore
		}

		return false; 
	}

	/** Indicating whether this file can be found on the underlying file system.
	 * 
	 * @param path file path.
	 * @return true if the file is available, false otherwise.
	 */
	public static boolean checkFile(String path) {
		if (!isReadable()) {
			return false;
		}

		try {
			File file = new File(path);
			return file.exists();
		} catch (Exception e) {
			// Ignore
		}

		return false;
	}

	/**
	 * Delete a file.
	 * 
	 * @return true if the file has been successfully deleted.
	 */
	public static boolean deleteFile(String path) {
		try {
			File f = new File(path);
			return f.delete();
		} catch (Exception e) {
			// Ignore
		}

		return false;
	}

	/**
	 * Delete files in a directory that match a given String.
	 * 
	 * @return true if all the files has been successfully deleted.
	 */
	public static boolean deleteFiles(String path, String filter) {
		boolean flag = true;

		try {
			File f = new File(path);
			File[] list = f.listFiles();
			for (File file : list) {
				if (file.getName().contains(filter)) {
					if (!file.delete()) {
						flag = false;
					}
				}
			}
		} catch (Exception e) {
			// Ignore
		}

		return flag;
	}

	/**
	 * Recursively delete file or directory.
	 * @param file to delete
	 * @return true if successful, false otherwise.
	 */
	public static boolean deleteFileRecursive(File file) {
		boolean deletedAll = true;
		if (file != null) {
			if (file.isDirectory()) {
				String[] children = file.list();
				for (String aChildren : children) {
					deletedAll = deleteFileRecursive(new File(file, aChildren)) && deletedAll;
				}
			} else {
				deletedAll = file.delete();
			}
		}
		return deletedAll;
	}

	/**
	 * Copy Uri to file.
	 * @param context of the caller.
	 * @param uri source.
	 * @param dest file destination.
	 */
	public static void copyFileStream(Context context, Uri uri, File dest) {
		InputStream is = null;
		OutputStream os = null;
		try {
			is = context.getContentResolver().openInputStream(uri);
			os = new FileOutputStream(dest);
			byte[] buffer = new byte[1024];
			int length;

			if (is != null) {
				while ((length = is.read(buffer)) > 0) {
					os.write(buffer, 0, length);
				}
			}
		} catch (Throwable t) {
			// Ignore
		} finally {
			try {
				if (is != null) {
					is.close();
				}
			} catch (Throwable t) {
				// Ignore
			}
			try {
				if (os != null) {
					os.close();
				}
			} catch (Throwable t) {
				// Ignore
			}
		}
	}

	/**
	 * Copy a file.
	 * 
	 * @param fileOrig original file path and name.
	 * @param fileDest destination file path and name.
	 * @return true if operation is successful, false otherwise.
	 */
	public static boolean copyFile_old(String fileOrig, String fileDest) {
		boolean success = true;

		InputStream in = null;
		OutputStream out = null;
		try {
			in = new FileInputStream(fileOrig);
			out = new FileOutputStream(fileDest);

			byte[] buf = new byte[1024];
			int len = 0;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
		} catch (Exception e) {
			success = false;
		}

		try {
			if (in != null) {
				in.close();
			}
		} catch (Exception e) {
			// Ignore
		}
		try {
			if (out != null) {
				out.close();
			}
		} catch (Exception e) {
			// Ignore
		}

		return success;
	}

	/**
	 * Copies a file to new location, overwriting existing files.
	 * 
	 * @param sourcePath path of source file
	 * @param destPath path of destination file
	 * @return true if operation is successful, false otherwise.
	 */
	public static boolean copyFile(String sourcePath, String destPath) {
		return copyFile(sourcePath, destPath, true);
	}
	
	/**
	 * Copies a file to new location.
	 * 
	 * @param sourcePath
	 *            path of source file
	 * @param destPath
	 *            path of destination file
	 * @param overwrite
	 *            overwrite existing destination file
	 * @return true if operation is successful, false otherwise.
	 */
	public static boolean copyFile(String sourcePath, String destPath, boolean overwrite) {
		boolean flag = true;

		File sourceFile = null;
		File destFile = null;
		FileInputStream sourceInput = null;
		FileOutputStream destinationOutput = null;
		FileChannel source = null;
		FileChannel destination = null;
		try {
			// create files
			sourceFile = new File(sourcePath);
			destFile = new File(destPath);

			// verify files
			if (!sourceFile.exists()) {
				return false;
			}
			if (destFile.exists()) {
				if(!overwrite || !destFile.delete()) {
					return false;
				}
			} else {
				if (!(destFile.getParentFile().mkdirs() || destFile.getParentFile().isDirectory())) {
					return false;
				}
				//destFile.createNewFile();
			}

			// open channels
			sourceInput = new FileInputStream(sourceFile);
			destinationOutput = new FileOutputStream(destFile);
			source = sourceInput.getChannel();
			destination = destinationOutput.getChannel();

			// copy files
			long count = 0;
			long size = source.size();
			while ((count += destination.transferFrom(source, count, size - count)) < size);
		} catch (Exception e) {
			flag = false;
		} finally {
			// close everything
			try {
				if (source!=null) {
					source.close();
				}
				if (destination != null) {
					destination.close();
				}
				if (sourceInput != null) {
					sourceInput.close();
				}
				if (destinationOutput != null) {
					destinationOutput.close();
				}
			} catch (Exception e) {
				// Ignore
			}
		}
		return flag;
	}

	/**
	 * Moves a file to new location, overwriting existing files.
	 * 
	 * @param sourcePath path of source file
	 * @param destPath path of destination file
	 * @return true if operation is successful, false otherwise.
	 */
	public static boolean moveFile(String sourcePath, String destPath) {
		boolean flag = true;

		File sourceFile = null;
		try {
			// Copy files
			if( !copyFile(sourcePath, destPath, true)) {
				return false;
			}
			
			// Delete source
			sourceFile = new File(sourcePath);
			sourceFile.delete();
		} catch (Exception e) {
			flag = false;
		}
		return flag;
	}

	/**
	 * Check how much usable space is available at a given path.
	 *
	 * @param path The path to check
	 * @return The space available in bytes
	 */
	@SuppressLint("NewApi")
	@SuppressWarnings("deprecation")
	public static long getUsableSpace(File path) {
		if (Utils.hasGingerbread()) {
			return path.getUsableSpace();
		}
		final StatFs stats = new StatFs(path.getPath());
		return (long) stats.getBlockSize() * (long) stats.getAvailableBlocks();
	}

	/**
	 * Gets the extension of a file.
	 * @param uri path.
	 * @return extension including the dot, empty if there is no extension.
	 */
	public static String getExtension(String uri) {
		if (uri == null) {
			return null;
		}

		int dot = uri.lastIndexOf(".");
		if (dot >= 0) {
			return uri.substring(dot);
		} else {
			return "";
		}
	}

	/**
	 * Gets the name minus the path from a full filename.
	 *
	 * @param filename to query.
	 * @return the name of the file without the path, or an empty string if none exists.
	 */
	public static String getName(final String filename) {
		if (filename == null) {
			return null;
		}

		if (failIfNullBytePresent(filename)) {
			final int index = indexOfLastSeparator(filename);
			return filename.substring(index + 1);
		}

		return null;
	}

	/**
	 * Check the input for null bytes, a sign of non-sanitized data being passed to to file level functions.
	 * This may be used for injection attacks."
	 *
	 * @param path to check.
	 */
	private static boolean failIfNullBytePresent(final String path) {
		final int len = path.length();
		for (int i = 0; i < len; i++) {
			if (path.charAt(i) == 0) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Returns the index of the last directory separator character.
	 *
	 * @param filename to find the last path separator in, null returns -1.
	 * @return the index of the last separator character,
	 * or -1 if there is no such character.
	 */
	public static int indexOfLastSeparator(final String filename) {
		if (filename == null) {
			return -1;
		}
		final int lastUnixPos = filename.lastIndexOf('/');
		final int lastWindowsPos = filename.lastIndexOf('\\');
		return Math.max(lastUnixPos, lastWindowsPos);
	}

	/**
	 * Get file name.
	 * @param context of the caller.
	 * @param uri to parse.
	 * @param path of the file or null if unknown.
	 * @return file name.
	 * @throws Throwable in case of unforeseen errors.
	 */
	public static String getRealNameFromURI(Context context, Uri uri, String path) throws Throwable  {
		String filename = null;

		ContentResolver contentResolver = context.getContentResolver();
		String mimeType = contentResolver.getType(uri);

		if (mimeType == null) {
			if (path == null) {
				filename = getName(uri.toString());
			} else {
				File file = new File(path);
				filename = file.getName();
			}
		} else {
			Cursor returnCursor = contentResolver.query(uri, null, null, null, null);
			if (returnCursor != null) {
				int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
				returnCursor.moveToFirst();
				filename = returnCursor.getString(nameIndex);
				returnCursor.close();
			}
		}

		return filename;
	}

	/**
	 * Get URI path.
	 * @param context of the caller.
	 * @param uri to parse.
	 * @return uri path.
	 * @throws Throwable in case of unforeseen errors.
	 */
	public static String getRealPathFromURI(Context context, Uri uri) throws Throwable {
		// DocumentProvider
		if (Utils.hasKitKat() && DocumentsContract.isDocumentUri(context, uri)) {

			// ExternalStorageProvider
			if (isExternalStorageDocument(uri)) {
				final String docId = DocumentsContract.getDocumentId(uri);
				final String[] split = docId.split(":");
				final String type = split[0];

				if ("primary".equalsIgnoreCase(type)) {
					return Environment.getExternalStorageDirectory() + "/" + split[1];
				}

				// TODO handle non-primary volumes
				return null;
			}

			// DownloadsProvider
			if (isDownloadsDocument(uri)) {
				final String id = DocumentsContract.getDocumentId(uri);
				final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

				return getDataColumn(context, contentUri, null, null);
			}

			// MediaProvider
			if (isMediaDocument(uri)) {
				final String docId = DocumentsContract.getDocumentId(uri);
				final String[] split = docId.split(":");
				final String type = split[0];
				final String selection = "_id=?";
				final String[] selectionArgs = new String[] {split[1]};

				Uri contentUri = null;
				if ("image".equals(type)) {
					contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
				} else if ("video".equals(type)) {
					contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
				} else if ("audio".equals(type)) {
					contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
				}
				return getDataColumn(context, contentUri, selection, selectionArgs);
			}

			return null;
		}

		// MediaStore (and general)
		if ("content".equalsIgnoreCase(uri.getScheme())) {
			// Return the remote address
			if (isGooglePhotosUri(uri)) {
				return uri.getLastPathSegment();
			}

			return getDataColumn(context, uri, null, null);
		}

		// File
		if ("file".equalsIgnoreCase(uri.getScheme())) {
			return uri.getPath();
		}

		// Manual
		String[] proj = { MediaStore.Images.Media.DATA };
		Cursor cursor;

		if (Utils.hasKitKat()) {
			String wholeID = DocumentsContract.getDocumentId(uri);
			String id = wholeID.split(":")[1];
			String sel = MediaStore.Images.Media._ID + "=?";
			cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
					proj, sel, new String[]{ id }, null);
		} else {
			CursorLoader cursorLoader = new CursorLoader(context, uri, proj, null, null, null);
			cursor = cursorLoader.loadInBackground();
		}

		if (cursor == null) {
			return null;
		}

		int columnIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
		if (columnIndex < 0) {
			return null;
		}

		cursor.moveToFirst();
		String result = cursor.getString(columnIndex);
		cursor.close();

		return result;
	}

	public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
		Cursor cursor = null;
		final String column = "_data";
		final String[] projection = { column };
		try {
			cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
			if (cursor != null && cursor.moveToFirst()) {
				final int index = cursor.getColumnIndexOrThrow(column);
				return cursor.getString(index);
			}
		} finally {
			if (cursor != null)
				cursor.close();
		}
		return null;
	}

	/**
	 * Checks whether the Uri authority is External Storage Provider.
	 * @param uri to check.
	 * @return true if it is, false otherwise.
	 */
	public static boolean isExternalStorageDocument(Uri uri) {
		return EXTERNAL_STORAGE_PROVIDER.equals(uri.getAuthority());
	}

	/**
	 * Checks whether the Uri authority is Downloads Provider.
	 * @param uri to check.
	 * @return true if it is, false otherwise.
	 */
	public static boolean isDownloadsDocument(Uri uri) {
		return DOWNLOADS_PROVIDER.equals(uri.getAuthority());
	}

	/**
	 * Checks whether the Uri authority is Media Provider.
	 * @param uri to check.
	 * @return true if it is, false otherwise.
	 */
	public static boolean isMediaDocument(Uri uri) {
		return MEDIA_PROVIDER.equals(uri.getAuthority());
	}

	/**
	 * Checks whether the Uri authority is Google Photos.
	 * @param uri to check.
	 * @return true if it is, false otherwise.
	 */
	public static boolean isGooglePhotosUri(Uri uri) {
		return GOOGLE_PHOTOS_PROVIDER.equals(uri.getAuthority());
	}

	/**
	 * Get MIME type for the given file.
	 * @return The MIME type for the given file.
	 */
	public static String getMimeType(File file) {
		String extension = getExtension(file.getName());

		if (extension.length() > 0) {
			return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.substring(1));
		}

		return MIME_TYPE_BINARY;
	}

}
