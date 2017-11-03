package companion.support.v8.util.zip;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import org.json.JSONException;
import org.json.JSONObject;

import companion.support.v8.lang.ParsingUtils;

/** 
 * Utility class for zipping methods.
 * 
 * @author Vitor Ribeiro
 * 
 */
public class ZipUtils {

	/** This prevents the class from being instantiated. 
	 */
	private ZipUtils() {
	}

	/** Compress bytes.
	 * @param input bytes to compress.
	 * @return compressed bytes.
	 */
	public static byte[] bytesToZlib(byte[] input) {
		if (input == null) {
			return null;
		}

		Deflater compressor = new Deflater();
		//compressor.setLevel(Deflater.BEST_COMPRESSION);

		compressor.setInput(input);
		compressor.finish();

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		byte[] buf = new byte[1024];
		while (!compressor.finished()) {
			int count = compressor.deflate(buf);
			bos.write(buf, 0, count);
		}
		byte[] array = bos.toByteArray();
		try {
			bos.close();
		} catch (Exception e) {
			// Ignore
		}
		return array;
	}

	/** Decompress bytes.
	 * @param byteArray bytes to decompress.
	 * @return decompressed bytes.
	 */
	public static byte[] zlibToBytes(byte[] byteArray) {
		if (byteArray == null) {
			return null;
		}

		Inflater decompressor = new Inflater();
		decompressor.setInput(byteArray);

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		byte[] buf = new byte[1024];
		while (!decompressor.finished()) {
			try {
				int count = decompressor.inflate(buf);
				bos.write(buf, 0, count);
			} catch (Exception e) {
				// Ignore
			}
		}
		byte[] array = bos.toByteArray();
		try {
			bos.close();
		} catch (Exception e) {
			// Ignore
		}
		return array;
	}

	/** Compress a String.
	 * @param input string to compress.
	 * @return compressed bytes.
	 */
	public static byte[] stringToZlib(String input) {
		if (input == null) {
			return null;
		}

		return bytesToZlib(ParsingUtils.stringToUTF(input));
	}

	/** Decompress a String.
	 * @param byteArray bytes to decompress.
	 * @return decompressed string.
	 */
	public static String zlibToString(byte[] byteArray) {
		if (byteArray == null) {
			return null;
		}

		return ParsingUtils.UTFToString(zlibToBytes(byteArray));
	}

	/**
	 * Converts a JSONObject to a compressed byte array.
	 * 
	 * @param json
	 *            JSONObject to convert.
	 * @return corresponding bytes.
	 */
	public static byte[] JSONObjectToZlib(JSONObject json) {
		if (json == null) {
			return null;
		}

		return bytesToZlib(ParsingUtils.JSONObjectToBytes(json));
	}

	/**
	 * Converts compressed bytes to a JSONObject.
	 * 
	 * @param byteArray
	 *            bytes to convert.
	 * @return corresponding JSONObject.
	 * @throws JSONException
	 * @throws UnsupportedEncodingException
	 */
	public static JSONObject zlibToJSONObject(byte[] byteArray) {
		if (byteArray == null) {
			return null;
		}

		JSONObject json = null;
		try {
			json = new JSONObject(ParsingUtils.UTFToString(zlibToBytes(byteArray)));
		} catch (Exception e) {
			// Ignore
		}

		return json;
	}
}
