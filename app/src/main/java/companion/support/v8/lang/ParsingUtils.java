package companion.support.v8.lang;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;

import org.json.JSONObject;

import android.support.annotation.NonNull;
import android.util.Base64;

import companion.support.v8.util.ArraysUtils;

/** 
 * Utility class with a bundle of parsing methods.
 * 
 * @author Vitor Ribeiro
 * 
 */
public class ParsingUtils {

	/** This prevents the class from being instantiated. 
	 */
	private ParsingUtils() {
	}

	/** Convert a short to a byte array.
	 * @param s short to convert.
	 * @return converted byte array.
	 */
	public static byte[] shortToBytes(short s) {
		return new byte[] { (byte) ((s & 0xFF00) >> 8), (byte) (s & 0x00FF) };
	}

	/** Convert a integer to a byte array.
	 * @param value integer to convert.
	 * @return converted byte array.
	 */
	public static byte[] intToBytes(int value) {
		return new byte[] { (byte) (value >>> 24), (byte) (value >>> 16), (byte) (value >>> 8), (byte) value };
	}

	/** Convert a byte array to an integer.
	 * @param b bytes to convert.
	 * @return corresponding integer.
	 */
	public static int bytesToInt(byte[] b) {
		if (b==null) {
			return Integer.MAX_VALUE;
		}

		int count = 0;
		int ret = 0;
		byte[] newbytes = b;
		if (b.length>4) {
			newbytes = ArraysUtils.truncateLeftBytes(b, 4);
		}
		if (b.length<4) {
			byte sign = (byte) (b[0]<0? 0xFF: 0x00);
			try {
				newbytes = new byte[]{sign, sign, sign, sign};
				System.arraycopy(b, 0, newbytes, 4-b.length, b.length);
			} catch (Exception e) {
				return 0;
			}
		}
		for (int i = newbytes.length; i > 0; i--) {
			ret += (newbytes[i - 1] & 0xFF) << count;
			count += 8;
		}
		return ret;
	}

	/**
	 * Convert a byte array to an unsigned integer.
	 * 
	 * @param b bytes to convert.
	 * @return corresponding integer.
	 */
	public static int bytesToUnsignedInt(byte[] b) {
		if (b==null) {
			return Integer.MAX_VALUE;
		}

		int count = 0;
		int ret = 0;
		byte[] newbytes = b;
		if (b.length > 4) {
			newbytes = ArraysUtils.truncateLeftBytes(b, 4);
		}
		for (int i = newbytes.length; i > 0; i--) {
			ret += (newbytes[i - 1] & 0xFF) << count;
			count += 8;
		}
		if (ret < 0) {
			ret = Integer.MAX_VALUE;
		}
		return ret;
	}

	/** Convert a long to a byte array.
	 * @param value long to convert.
	 * @return converted byte array.
	 */
	public static byte[] longToBytes(long value) {
		return new byte[] { 
			(byte) (value >>> 56), (byte) (value >>> 48), 
			(byte) (value >>> 40), (byte) (value >>> 32), 
			(byte) (value >>> 24), (byte) (value >>> 16), 
			(byte) (value >>> 8), (byte) value 
		};
	}

	/** Convert a boolean array to a byte array.
	 * @param bools boolean array to convert.
	 * @return converted byte array.
	 */
	public static byte[] booleanArrayToBytes(boolean[] bools) {
		if (bools == null) {
			return null;
		}

		int size = bools.length;
		byte[] bytes = new byte[size];
		for (int i = 0; i < size; i++) {
			bytes[i] = (byte) ((bools[i]) ? 1 : 0);
		}
		return bytes;
	}

	/** Convert a byte array to a boolean array.
	 * @param bytes to convert.
	 * @return corresponding boolean array.
	 */
	public static boolean[] bytesToBoleanArray(byte[] bytes) {
		if (bytes == null) {
			return null;
		}

		int size = bytes.length;
		boolean[] bools = new boolean[size];
		for (int i = 0; i < size; i++) {
			bools[i] = bytes[i] == 1;
		}
		return bools;
	}

	/** Converts a char array of 0 and 1 characters into a boolean array.
	 * Incompatible characters are converted into false.
	 * 
	 * @param array char array.
	 * @return boolean array.
	 */
	public static boolean[] charToBooleanArray(char[] array){
		if (array == null) {
			return null;
		}

		int size = array.length;
		boolean[] bools = new boolean[size];
		for (int i = 0;i<size;i++) {
			bools[i] = (array[i]=='1');
		}
		return bools;	
	}

	/** Returns the binary representation of the given bytes.
	 * @param buffer bytes to convert.
	 * @return binary string representation.
	 */
	public static String bytesToBitString(byte[] buffer) {
		if (buffer == null) {
			return null;
		}

		StringBuilder str = new StringBuilder();

		// parse response
		for (byte b : buffer) {
			StringBuilder response = new StringBuilder(Integer.toBinaryString((short) ((short) b & 0xff)));

			// we need 8 bits, so if the response is not of size 8, zeros are added on the left
			int size = 8-response.length();
			if (size < 0) {
				size=0;
			}
			for (int i = 0; i<size; i++) {
				response.insert(0, "0");
			}
			str.append(response);
		}

		return str.toString();
	}

	/** Convert a byte array to a long.
	 * @param b bytes to convert.
	 * @return corresponding long.
	 */
	public static long bytesToLong(byte[] b) {
		if (b==null) {
			return Long.MAX_VALUE;
		}

		int count = 0;
		int ret = 0;
		byte[] newBytes = b;
		if (b.length>8) {
			newBytes = ArraysUtils.truncateLeftBytes(b, 8);
		}
		if (b.length<8) {
			byte sign = (byte) (b[0]<0? 0xFF: 0x00);
			try {
				newBytes = new byte[]{sign, sign, sign, sign};
				System.arraycopy(b, 0, newBytes, 8-b.length, b.length);
			} catch (Exception e) {
				return 0;
			}
		}
		for (int i = newBytes.length; i > 0; i--) {
			ret += (newBytes[i - 1] & 0xFF) << count;
			count += 8;
		}
		return ret;
	}

	/** Returns the binary representation of the given bytes.
	 * @param bytes bytes to convert.
	 * @return binary string representation.
	 */
	public static String bytesToBinaryString(byte[] bytes) {
		if (bytes==null) {
			return null;
		}

		return Integer.toBinaryString(ParsingUtils.bytesToInt(bytes));
	}

	/** Converts a binary string in bytes.
	 * @param str binary string to convert.
	 * @return corresponding bytes.
	 */
	public static byte[] binaryStringToBytes(String str) {
		if (str==null) {
			return null;
		}

		int size = str.length() / 8;
		byte[] bytes = new byte[size];

		for(int i = 0; i < size; ++i) {
			try {
				bytes[i] = Byte.parseByte(str.substring(8 * i, 8), 2);	
			} catch (Exception e) {
				// Ignore
			}
		}

		return bytes;
	}

	/** Convert a string to its hexadecimal representation.
	 * @param str string to convert.
	 * @return corresponding hexadecimal string.
	 */
	public static String stringToHexadecimal(String str){
		if (str==null) {
			return null;
		}

		char[] chars = str.toCharArray();
		StringBuilder hex = new StringBuilder();
		for (char aChar : chars) {
			hex.append(Integer.toHexString((int) aChar));
		}

		return hex.toString();
	}

	/** Convert an hexadecimal to plain text.
	 * @param hex hexadecimal to convert.
	 * @return corresponding string.
	 */
	public static String hexadecimalToString(String hex){
		if (hex==null) {
			return null;
		}

		StringBuilder sb = new StringBuilder();
		StringBuilder temp = new StringBuilder();

		if (!(hex.length() % 2 == 0)) {
			// odd
		} else {
			// even
			hex = "0" + hex;
		}

		// split into two characters
		for(int i=0; i<hex.length()-1; i+=2){

			// grab the hex in pairs
			String output = hex.substring(i, (i + 2));
			if (output.equalsIgnoreCase("00")) continue;
			// convert hex to decimal
			int decimal = Integer.parseInt(output, 16);
			// convert the decimal to character
			sb.append((char)decimal);

			temp.append(decimal); 
		}

		return sb.toString();
	}

	/**
	 * Parse string to integer.
	 * @param string to parse.
	 * @return integer.
	 */
	public static int stringToInt(String string) {
		int intValue;
		try {
			intValue = Integer.parseInt(string);
		} catch (Throwable t) {
			intValue = -1;
		}

		return intValue;
	}

	/**
	 * Flatten a string array.
	 * @param array to flatten.
	 * @return string.
	 */
	public static String stringArrayToString(@NonNull String[] array) {
		StringBuilder string = new StringBuilder();

		for (String s : array) {
			string.append(s).append(";");
		}

		int len = string.length();
		if (len > 0 && string.charAt(len - 1) == ';') {
			string = new StringBuilder(string.substring(0, string.length() - 1));
		}

		return string.toString();
	}

	/** Convert bytes to hexadecimal form.
	 * @param bytes bytes to convert.
	 * @param algorithm to use.
	 * @return corresponding hexadecimal string.
	 */
	public static String bytesToHexadecimal(byte[] bytes, int algorithm) {
		if (bytes==null) {
			return null;
		}

		String result = null;
		if (algorithm==1) {
			String hex = "0123456789ABCDEF";
			StringBuffer buffer = new StringBuffer(2 * bytes.length);
			for (byte aByte : bytes) {
				buffer.append(hex.charAt((aByte >> 4) & 0x0f)).append(hex.charAt(aByte & 0x0f));
			}
			result = buffer.toString();
		} else if (algorithm==2) {
			StringBuilder sb = new StringBuilder();
			for (byte aByte : bytes) {
				String hex = Integer.toHexString(0xFF & aByte);
				if (hex.length() == 1) {
					sb.append('0');
				}
				sb.append(hex);
			}
			result = sb.toString();
		} else {
			result = String.format("%0" + (bytes.length << 1) + "X", new BigInteger(1, bytes));
		}

		return result;
	}

	/** Convert an hexadecimal string to a byte array.
	 * @param hex hexadecimal string.
	 * @param algorithm to use.
	 * @return converted bytes.
	 */
	public static byte[] hexadecimalToBytes(String hex, int algorithm) {
		if (hex==null) {
			return null;
		}

		byte[] result = null;

		if (algorithm==1) {
			try {
				if (hex.length()>1 && hex.substring(0, 1).equalsIgnoreCase("0x")) {
					hex = hex.substring(2);
				}
				if (hex.length()<2) {
					throw new Exception();
				}
			} catch (Exception e) {
				return null;
			}

			try {
				ByteArrayOutputStream buff = new ByteArrayOutputStream();
				for (int i = 0; i < hex.length()/2; i++) {
					String sub = hex.substring(2 * i, 2 * i + 2);
					if (sub.equalsIgnoreCase("\r") || sub.equalsIgnoreCase("\n")) {
						try {
							buff.write(sub.getBytes());
						} catch (Exception e) {
							// Ignore
						}
					} else {
						buff.write((byte) Integer.parseInt(sub,16));
					}
				}
				result = buff.toByteArray();
				buff.close();
			} catch (Exception e) {
				// Ignore
			}
		} else {
			int len = hex.length() / 2;
			result = new byte[len];
			for (int i = 0; i < len; i++) {
				result[i] = Integer.valueOf(hex.substring(2 * i, 2 * i + 2), 16).byteValue();
			}
		}

		return result;
	}

	/** Returns a new byte array containing the characters of this string encoded in 'UTF-8'.
	 * 
	 * @param str string to convert.
	 * @return corresponding byte array.
	 */
	public static byte[] stringToUTF(String str) {
		try {
			return str.getBytes("UTF-8");
		} catch (Exception e) {
			// Ignore
		}
		return null;
	}

	/** Converts a byte array to a string using the 'UTF-8' charset.
	 * 
	 * @param byteArray bytes to convert.
	 * @return corresponding string.
	 */
	public static String UTFToString(byte[] byteArray) {
		try {
			return new String(byteArray, "UTF-8");
		} catch (Exception e) {
			// Ignore
		}
		return null;
	}

	/** Convert Base64 to bytes.
	 * @param str Base64 String to convert. 
	 * @return corresponding bytes.
	 */
	public static byte[] base64ToBytes(String str) {
		if (str == null) {
			return null;
		}

		return Base64.decode(str, Base64.DEFAULT);
	}

	/** Convert bytes to Base64.
	 * @param byteArray bytes to convert. 
	 * @return corresponding Base64 string.
	 */
	public static String bytesToBase64(byte[] byteArray) {
		if (byteArray == null) {
			return null;
		}

		return Base64.encodeToString(byteArray, Base64.DEFAULT);
	}

	/**
	 * Converts a JSONObject to a byte array.
	 * 
	 * @param json JSONObject to convert.
	 * @return corresponding bytes.
	 */
	public static byte[] JSONObjectToBytes(JSONObject json) {
		if (json == null) {
			return null;
		}

		return stringToUTF(json.toString());
	}

	/**
	 * Converts bytes to a JSONObject.
	 * 
	 * @param byteArray bytes to convert.
	 * @return corresponding JSONObject.
	 */
	public static JSONObject bytesToJSONObject(byte[] byteArray) {
		if (byteArray == null) {
			return null;
		}

		JSONObject json = null;
		try {
			json = new JSONObject(UTFToString(byteArray));
		} catch (Exception e) {
			// Ignore
		}

		return json;
	}
}
