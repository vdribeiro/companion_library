package companion.support.v8.os;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Environment;
import android.os.StatFs;

/**
 * Utility class for storage methods.
 * 
 * @author Vitor Ribeiro
 * 
 */
public class Storage {

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
		if (Environment.MEDIA_MOUNTED.equalsIgnoreCase(state)) {
			return true;
		}
		return false;
	}

	/** Check if the primary "external" storage device is readable.
	 * 
	 * @return true if readable, false otherwise.
	 */
	public static boolean isReadable() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equalsIgnoreCase(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equalsIgnoreCase(state)) {
			return true;
		}
		return false;
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
	 * Get parent path.
	 *
	 * @param dbfile absolute path.
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
			in.close();
		} catch (Exception e) {
			// Ignore
		}
		try {
			out.close();
		} catch (Exception e) {
			// Ignore
		}

		return success;
	}

	/**
	 * Copies a file to new location, overwriting existing files.
	 * 
	 * @param sourcePath
	 *            path of source file
	 * @param destPath
	 *            path of destination file
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

}
