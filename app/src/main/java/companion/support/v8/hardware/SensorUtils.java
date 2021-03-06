package companion.support.v8.hardware;

import android.bluetooth.BluetoothClass;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;

import java.util.List;
import java.util.Locale;

import companion.support.v8.lang.ParsingUtils;
import companion.support.v8.os.Utils;
import companion.support.v8.util.ArraysUtils;
import companion.support.v8.util.LogHelper;

/**
 * Class with a bundle of Sensor methods.
 * 
 * @author Vitor Ribeiro
 *
 */
public class SensorUtils {

	/** Log tag. */
	private static final String TAG = SensorUtils.class.getSimpleName();

	/**
	 * <p>The values from the sensor {@link android.hardware.Sensor#TYPE_ACCELEROMETER
	 * Sensor.TYPE_ACCELEROMETER}.
	 * All values are in SI units (m/s^2) and measure the acceleration minus G 
	 * in the X, Y and Z axis.</p>
	 */
	public static float[] accelerationValues = new float[3];

	/**
	 * <p>The values from the sensor {@link android.hardware.Sensor#TYPE_MAGNETIC_FIELD
	 * Sensor.TYPE_MAGNETIC_FIELD}.
	 * All values are in micro-Tesla (uT) and measure the ambient magnetic field
	 * in the X, Y and Z axis.</p>
	 */
	public static float[] magneticValues = new float[3];

	/**
	 * <p>The values from the sensor {@link android.hardware.Sensor#TYPE_ROTATION_VECTOR 
	 * Sensor.TYPE_ROTATION_VECTOR}.
	 * The rotation vector represents the orientation of the device as a combination of an <i>angle</i>
	 * and an <i>axis</i>, in which the device has rotated through an angle &#952 around an axis &lt;x, y, z>.</p>
	 * 
	 * <p>The three elements of the rotation vector are
	 * &lt;x*sin(&#952/2), y*sin(&#952/2), z*sin(&#952/2)>, such that the magnitude of the rotation
	 * vector is equal to sin(&#952/2), and the direction of the rotation vector is equal to the
	 * direction of the axis of rotation.</p>
	 * 
	 * </p>The three elements of the rotation vector are equal to
	 * the last three components of a <b>unit</b> quaternion
	 * &lt;cos(&#952/2), x*sin(&#952/2), y*sin(&#952/2), z*sin(&#952/2)>.</p>
	 * 
	 * <p>Elements of the rotation vector are unitless.
	 * The x,y, and z axis are defined in the same way as the acceleration
	 * sensor. </p>
	 *
	 * <ul>
	 * <li> values[0]: x*sin(&#952/2) </li>
	 * <li> values[1]: y*sin(&#952/2) </li>
	 * <li> values[2]: z*sin(&#952/2) </li>
	 * <li> values[3]: cos(&#952/2) </li>
	 * <li> values[4]: estimated heading Accuracy (in radians) (-1 if unavailable)</li>
	 * </ul>
	 */
	public static float[] rotationValues = new float[5];

	/**
	 * <p>Can either be the values from the sensor {@link android.hardware.Sensor#TYPE_ORIENTATION
	 * Sensor.TYPE_ORIENTATION}, or the calculated from other sensors.
	 * <p>All values are in radians and represent:</p>
	 * 
	 * <ul>
	 * <li>values[0]: <i>azimuth</i>, rotation around the Z axis.</li>
	 * <li>values[1]: <i>pitch</i>, rotation around the X axis.</li>
	 * <li>values[2]: <i>roll</i>, rotation around the Y axis.</li>
	 * </ul>
	 */
	public static float[] orientationValues = new float[3];

	/** <p> The rotation matrix is an array of 9 floats holding 
	 * the identity matrix when the device is aligned with the
	 * world's coordinate system, that is, when the device's X axis points
	 * toward East, the Y axis points to the North Pole and the device is facing the sky. </p>
	 * 
	 * <p> By definition: [0 0 g] = <b>R</b> * <b>gravity</b> (g = magnitude of gravity) </p>
	 */
	public static float[] rotationMatrix = new float[16];

	/** <p> The inclination matrix is an array of 9 floats holding 
	 * the rotation matrix transforming the geomagnetic vector into
	 * the same coordinate space as gravity (the world's coordinate space). </p>
	 * 
	 * <p> By definition: [0 m 0] = <b>I</b> * <b>R</b> * <b>geomagnetic</b> (m = magnitude of geomagnetic field) </p>
	 */
	public static float[] inclinationMatrix = new float[16];

	/** This prevents the class from being instantiated. 
	 */
	private SensorUtils() {
	}

	/**
	 * Get sensor information regarding name, power, maximum range and resolution.
	 * @param sensor object.
	 * @param unit of the sensor (like mA for milliamps).
	 * @return string with sensor information.
	 */
	public static String getSensorInfo(Sensor sensor, String unit) {
		if (sensor == null) {
			return null;
		}
		unit = " " + unit;
		return "Name: " + sensor.getName()
				+ "; Power: " + sensor.getPower() + unit
				+ "; Max Range: " + sensor.getMaximumRange() + unit
				+ "; Resolution: " + sensor.getResolution() + unit;
	}

	/**
	 * Get information from all the existing sensors regarding name, power, maximum range and
	 * resolution.
	 * @param context the caller context.
	 * @return a string with all the sensors information.
	 */
	public static String getAllSensorInfo(Context context) {
		SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
		if (sensorManager == null) {
			return null;
		}

		List<Sensor> sensorList = sensorManager.getSensorList(Sensor.TYPE_ALL);
		if (sensorList == null) {
			return null;
		}

		StringBuilder sensorInfo = new StringBuilder("");
		LogHelper.d(TAG, sensorList.toString());
		for (Sensor s : sensorList) {
			sensorInfo.append("Sensor Name: ");
			sensorInfo.append(s.getName());
			sensorInfo.append(" Vendor: ");
			sensorInfo.append(s.getVendor());
			sensorInfo.append(" Version: ");
			sensorInfo.append(s.getVersion());
			sensorInfo.append(" Resolution: ");
			sensorInfo.append(s.getResolution());
			sensorInfo.append(" MaxRange: ");
			sensorInfo.append(s.getMaximumRange());
			sensorInfo.append("\n");
		}
		return sensorInfo.toString();
	}

	@SuppressWarnings("deprecation")
	public static void updateValues(SensorEvent event) {
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			accelerationValues = event.values.clone();
		} else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
			magneticValues = event.values.clone();
		} else if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
			orientationValues = event.values.clone();
		} else if (Utils.hasGingerbread() && event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
			rotationValues = event.values.clone();
		}
	}

	/**
	 * <p>Computes the device's orientation based on the rotation matrix. </p>
	 *
	 * @return <code>true</code> on success, <code>false</code> on failure.
	 */
	public static boolean getOrientation() {
		getRotationMatrix();

		orientationValues[0] = (float) Math.atan2(rotationMatrix[1], rotationMatrix[5]);
		orientationValues[1] = (float) Math.asin(-rotationMatrix[9]);
		orientationValues[2] = (float) Math.atan2(-rotationMatrix[8], rotationMatrix[10]);

		return true;
	}

	/**
	 * <p>Computes the device's orientation according to the True North. </p>
	 *
	 * @return <code>true</code> on success, <code>false</code> on failure.
	 */
	public static boolean getTrueOrientation() {
		getRotationMatrix();

		remapCoordinateSystem(SensorManager.AXIS_X, SensorManager.AXIS_Z, rotationMatrix);

		orientationValues[0] = (float) Math.atan2(rotationMatrix[1], rotationMatrix[5]);
		orientationValues[1] = (float) Math.asin(-rotationMatrix[9]);
		orientationValues[2] = (float) Math.atan2(-rotationMatrix[8], rotationMatrix[10]);

		return true;
	}
	
	/**
	 * <p>Computes the device's orientation based on the rotation matrix. </p>
	 *
	 * @return <code>true</code> on success, <code>false</code> on failure.
	 */
	public static boolean getOrientationFromRotation() {
		getRotationMatrixFromVector();

		orientationValues[0] = (float) Math.atan2(rotationMatrix[1], rotationMatrix[5]);
		orientationValues[1] = (float) Math.asin(-rotationMatrix[9]);
		orientationValues[2] = (float) Math.atan2(-rotationMatrix[8], rotationMatrix[10]);

		return true;
	}

	/**
	 * <p>Computes the device's orientation according to the True North. </p>
	 *
	 * @return <code>true</code> on success, <code>false</code> on failure.
	 */
	public static boolean getTrueOrientationFromRotation() {
		getRotationMatrixFromVector();

		remapCoordinateSystem(SensorManager.AXIS_X, SensorManager.AXIS_Z, rotationMatrix);

		orientationValues[0] = (float) Math.atan2(rotationMatrix[1], rotationMatrix[5]);
		orientationValues[1] = (float) Math.asin(-rotationMatrix[9]);
		orientationValues[2] = (float) Math.atan2(-rotationMatrix[8], rotationMatrix[10]);

		return true;
	}

	/**
	 * <p> Computes the inclination matrix <b>I</b> as well as the rotation matrix
	 * <b>R</b> transforming a vector from the device coordinate system to the
	 * world's coordinate system which is defined as a direct orthonormal basis</p>
	 *        
	 * @return <code>true</code> on success, <code>false</code> on failure.
	 * The matrices generated by this function are meaningful only when the
	 * device is not free-falling and it is not close to the magnetic north. If
	 * the device is accelerating, or placed into a strong magnetic field, the
	 * matrices may be inaccurate.
	 *
	 */
	public static boolean getRotationMatrix() {
		float Ax = accelerationValues[0];
		float Ay = accelerationValues[1];
		float Az = accelerationValues[2];
		final float Ex = magneticValues[0];
		final float Ey = magneticValues[1];
		final float Ez = magneticValues[2];

		float Hx = Ey*Az - Ez*Ay;
		float Hy = Ez*Ax - Ex*Az;
		float Hz = Ex*Ay - Ey*Ax;
		final float normH = (float)Math.sqrt(Hx*Hx + Hy*Hy + Hz*Hz);
		if (normH < 0.1f) {
			// device is close to free fall (or in space?), or close to
			// magnetic north pole. Typical values are  > 100.
			return false;
		}

		final float invH = 1.0f / normH;
		Hx *= invH;
		Hy *= invH;
		Hz *= invH;
		final float invA = 1.0f / (float)Math.sqrt(Ax*Ax + Ay*Ay + Az*Az);
		Ax *= invA;
		Ay *= invA;
		Az *= invA;
		final float Mx = Ay*Hz - Az*Hy;
		final float My = Az*Hx - Ax*Hz;
		final float Mz = Ax*Hy - Ay*Hx;

		if (rotationMatrix != null) {
			rotationMatrix[0]  = Hx;    rotationMatrix[1]  = Hy;    rotationMatrix[2]  = Hz;   rotationMatrix[3]  = 0;
			rotationMatrix[4]  = Mx;    rotationMatrix[5]  = My;    rotationMatrix[6]  = Mz;   rotationMatrix[7]  = 0;
			rotationMatrix[8]  = Ax;    rotationMatrix[9]  = Ay;    rotationMatrix[10] = Az;   rotationMatrix[11] = 0;
			rotationMatrix[12] = 0;     rotationMatrix[13] = 0;     rotationMatrix[14] = 0;    rotationMatrix[15] = 1;
		}

		if (inclinationMatrix != null) {
			// compute the inclination matrix by projecting the geomagnetic
			// vector onto the Z (gravity) and X (horizontal component
			// of geomagnetic vector) axes.
			final float invE = 1.0f / (float)Math.sqrt(Ex*Ex + Ey*Ey + Ez*Ez);
			final float c = (Ex*Mx + Ey*My + Ez*Mz) * invE;
			final float s = (Ex*Ax + Ey*Ay + Ez*Az) * invE;

			inclinationMatrix[0] = 1;     inclinationMatrix[1] = 0;     inclinationMatrix[2] = 0;
			inclinationMatrix[4] = 0;     inclinationMatrix[5] = c;     inclinationMatrix[6] = s;
			inclinationMatrix[8] = 0;     inclinationMatrix[9] =-s;     inclinationMatrix[10]= c;
			inclinationMatrix[3] = inclinationMatrix[7] = inclinationMatrix[11] = inclinationMatrix[12] = inclinationMatrix[13] = inclinationMatrix[14] = 0;
			inclinationMatrix[15] = 1;
		}

		return true;
	}

	/** Converts a rotation vector to a rotation matrix.
	 * 
	 * @return <code>true</code> on success, <code>false</code> on failure.
	 */
	public static boolean getRotationMatrixFromVector() {
		float q0;
		float q1 = rotationValues[0];
		float q2 = rotationValues[1];
		float q3 = rotationValues[2];

		if (rotationValues.length >= 4) {
			q0 = rotationValues[3];
		} else {
			q0 = 1 - q1*q1 - q2*q2 - q3*q3;
			q0 = (q0 > 0) ? (float)Math.sqrt(q0) : 0;
		}

		float sq_q1 = 2 * q1 * q1;
		float sq_q2 = 2 * q2 * q2;
		float sq_q3 = 2 * q3 * q3;
		float q1_q2 = 2 * q1 * q2;
		float q3_q0 = 2 * q3 * q0;
		float q1_q3 = 2 * q1 * q3;
		float q2_q0 = 2 * q2 * q0;
		float q2_q3 = 2 * q2 * q3;
		float q1_q0 = 2 * q1 * q0;

		rotationMatrix[0] = 1 - sq_q2 - sq_q3;
		rotationMatrix[1] = q1_q2 - q3_q0;
		rotationMatrix[2] = q1_q3 + q2_q0;
		rotationMatrix[3] = 0.0f;

		rotationMatrix[4] = q1_q2 + q3_q0;
		rotationMatrix[5] = 1 - sq_q1 - sq_q3;
		rotationMatrix[6] = q2_q3 - q1_q0;
		rotationMatrix[7] = 0.0f;

		rotationMatrix[8] = q1_q3 - q2_q0;
		rotationMatrix[9] = q2_q3 + q1_q0;
		rotationMatrix[10] = 1 - sq_q1 - sq_q2;
		rotationMatrix[11] = 0.0f;

		rotationMatrix[12] = rotationMatrix[13] = rotationMatrix[14] = 0.0f;
		rotationMatrix[15] = 1.0f;

		return true;
	}

	/**
	 * <p> Rotates the supplied rotation matrix so it is expressed in a different
	 * coordinate system. This is typically used when an application needs to
	 * compute the three orientation angles of the device in a different coordinate system. </p>
	 *
	 * <p> Since the resulting coordinate system is orthonormal, only two axes need
	 * to be specified. </p>
	 *
	 * @param X defines on which world axis and direction the X axis of the device is mapped.
	 *
	 * @param Y defines on which world axis and direction the Y axis of the device is mapped.
	 *
	 * @param outR the transformed rotation matrix.
	 *
	 * @return <code>true</code> on success. <code>false</code> if the input
	 *         parameters are incorrect, for instance if X and Y define the same
	 *         axis. Or if inR and outR don't have the same length.
	 */
	public static boolean remapCoordinateSystem(int X, int Y, float[] outR) {
		if (rotationMatrix == outR) {
			final float[] temp = new float[16];
			// we don't expect to have a lot of contention
			if (remapCoordinateSystemImpl(X, Y, temp)) {
				for (int i=0 ; i < outR.length ; i++) {
					outR[i] = temp[i];
				}
				return true;
			}
		}

		return remapCoordinateSystemImpl(X, Y, outR);
	}

	private static boolean remapCoordinateSystemImpl(int X, int Y, float[] outR) {
		/*
		 * X and Y define a rotation matrix 'r':
		 *
		 *  (X==1)?((X&0x80)?-1:1):0    (X==2)?((X&0x80)?-1:1):0    (X==3)?((X&0x80)?-1:1):0
		 *  (Y==1)?((Y&0x80)?-1:1):0    (Y==2)?((Y&0x80)?-1:1):0    (Y==3)?((X&0x80)?-1:1):0
		 *                              r[0] ^ r[1]
		 *
		 * where the 3rd line is the vector product of the first 2 lines
		 *
		 */

		final int length = outR.length;
		if (rotationMatrix.length != length) {
			return false; // invalid parameter
		}
		if ((X & 0x7C)!=0 || (Y & 0x7C)!=0) {
			return false; // invalid parameter
		}
		if (((X & 0x3)==0) || ((Y & 0x3)==0)) {
			return false; // no axis specified
		}
		if ((X & 0x3) == (Y & 0x3)) {
			return false; // same axis specified
		}

		// Z is "the other" axis, its sign is either +/- sign(X)*sign(Y)
		// this can be calculated by exclusive-or'ing X and Y; except for
		// the sign inversion (+/-) which is calculated below.
		int Z = X ^ Y;

		// extract the axis (remove the sign), offset in the range 0 to 2.
		final int x = (X & 0x3)-1;
		final int y = (Y & 0x3)-1;
		final int z = (Z & 0x3)-1;

		// compute the sign of Z (whether it needs to be inverted)
		final int axis_y = (z+1)%3;
		final int axis_z = (z+2)%3;
		if (((x^axis_y)|(y^axis_z)) != 0) {
			Z ^= 0x80;
		}

		final boolean sx = (X>=0x80);
		final boolean sy = (Y>=0x80);
		final boolean sz = (Z>=0x80);

		// Perform R * r, in avoiding actual muls and adds.
		final int rowLength = ((length==16)?4:3);
		for (int j=0 ; j<3 ; j++) {
			final int offset = j*rowLength;
			for (int i=0 ; i<3 ; i++) {
				if (x==i) {
					outR[offset+i] = sx ? -rotationMatrix[offset+0] : rotationMatrix[offset+0];
				}
				if (y==i) {
					outR[offset+i] = sy ? -rotationMatrix[offset+1] : rotationMatrix[offset+1];
				}
				if (z==i) {
					outR[offset+i] = sz ? -rotationMatrix[offset+2] : rotationMatrix[offset+2];
				}
			}
		}
		if (length == 16) {
			outR[3] = outR[7] = outR[11] = outR[12] = outR[13] = outR[14] = 0;
			outR[15] = 1;
		}
		return true;
	}

	/**
	 * Compress a given value (possibly losing precision) to fit in an interval.
	 *
	 * @param value to compress.
	 * @param min new minimum value.
	 * @param max new maximum value.
	 * @param scale to fit.
	 * @param size number of resulting bytes.
	 * @return compressed value.
	 */
	public static byte[] compress(float value, float min, float max, float scale, int size) {
		// Warn if overflow is possible
		if ((max - min) * scale > Math.pow(256, size)) {
			LogHelper.w(TAG, "Overflow is possible. MAX: " + max + " SCALE: " + scale + " SIZE: " + size);
		}

		if (min > max) {
			return null;
		}
		if (value > max) {
			value = max;
		}
		if (value < min) {
			value = min;
		}
		value *= scale;

		byte[] array = ParsingUtils.intToBytes((int) value);
		return ArraysUtils.truncateLeftBytes(array, size);
	}

	public static float decompress(byte[] array, float scale, boolean positive) {
		int val = ParsingUtils.bytesToInt(array);
		if (positive && val < 0) {
			val = 256 * 256 + val;
		}
		return (val / scale);
	}

	public static byte[] compressAccelerometer(float value) {
		return compress(value, -126, 126, 256, 2);
	}

	public static float decompressAccelerometer(byte[] array) {
		return decompress(array, 256, false);
	}

	public static byte[] compressMagnetometer(float value) {
		return compress(value, -4080, 4080, 8, 2);
	}

	public static float decompressMagnetometer(byte[] array) {
		return decompress(array, 8, false);
	}

	public static byte[] compressGyroscope(float value) {
		return compress(value, -62, 62, 512, 2);
	}

	public static float decompressGyroscope(byte[] array) {
		return decompress(array, 512, false);
	}

	public static byte[] compressLight(float value) {
		return compress(value, 0, 8160, 8, 2);
	}

	public static float decompressLight(byte[] array) {
		return decompress(array, 8, true);
	}

	public static byte[] compressPressure(float value) {
		return compress(value, 0, 2040, 32, 2);
	}

	public static float decompressPressure(byte[] array) {
		return decompress(array, 32, true);
	}

	public static byte[] compressProximity(float value) {
		return compress(value, 0, 25, 10, 1);
	}

	public static float decompressProximity(byte[] array) {
		return decompress(array, 10, true);
	}

	public static byte[] compressHumidity(float value) {
		return compress(value, 0, 120, 2, 1);
	}

	public static float decompressHumidity(byte[] array) {
		return decompress(array, 2, true);
	}

	public static byte[] compressTemperature(float value) {
		return compress(value, -254, 254, 128, 2);
	}

	public static float decompressTemperature(byte[] array) {
		return decompress(array, 128, false);
	}

	/**
	 * Returns the code of the authentication method of the AP.
	 * @param capabilities a string describing the authentication, key management, and encryption schemes
	 *                        supported by the access point.
	 * @return code value as:
	 * <ul>
	 * <li>
	 * 0 - Unknown.</li>
	 * <li>
	 * 1 - Open.</li>
	 * <li>
	 * 2 - WEP.</li>
	 * <li>
	 * 3 - WPA.</li>
	 * <li>
	 * 4 - WPA2.</li>
	 * <li>
	 * 5 - Enterprise (RADIUS/.11x/Other).</li>
	 * </ul>
	 */
	public static byte getAPAuthentication(String capabilities) {
		if (capabilities==null) {
			return 0;
		}
		capabilities = capabilities.toUpperCase(Locale.getDefault());

		if (capabilities.contains("EAP")) {
			return 5;
		}
		if (capabilities.contains("WPA2")) {
			return 4;
		}
		if (capabilities.length()<=0
				|| capabilities.equalsIgnoreCase("[ESS]")
				|| capabilities.equalsIgnoreCase("[IBSS]")
				|| capabilities.equalsIgnoreCase("[P2P]")
				|| capabilities.contains("OPEN")) {
			return 1;
		}
		if (capabilities.contains("WPA")) {
			return 3;
		}
		if (capabilities.contains("WEP")) {
			return 2;
		}

		return 0;
	}

	/**
	 * Returns the code of the mode of the AP.
	 * @param capabilities a string describing the authentication, key management, and encryption schemes
	 *                        supported by the access point.
	 * @return code value as:
	 * <ul>
	 * <li>
	 * 0 - Unknown.</li>
	 * <li>
	 * 1 - AdHoc.</li>
	 * <li>
	 * 2 - Mesh.</li>
	 * <li>
	 * 3 - AP.</li>
	 * <li>
	 * 4 - Repeater.</li>
	 * <li>
	 * 5 - Secondary AP.</li>
	 * </ul>
	 */
	public static byte getAPMode(String capabilities) {
		if (capabilities==null) {
			return 0;
		}
		capabilities = capabilities.toUpperCase(Locale.getDefault());

		if (capabilities.contains("IBSS") || capabilities.contains("P2P"))
			return 1;
		if (capabilities.contains("ESS"))
			return 3;

		return 0;
	}

	/**
	 * Returns the channel number given a frequency.
	 *
	 * @param frequency in MHz of the channel over which the client is communicating with the access point.
	 * @return channel.
	 */
	public static short getAPChannelNumber(int frequency) {
		return (short) ((frequency - 2407) / 5);
	}

	/**
	 * Get Bluetooth Device Class Code.
	 * @param bluetoothClass BluetoothClass object.
	 * @return an unique code.
	 */
	public static short getBluetoothClassCode(BluetoothClass bluetoothClass) {
		return (short) ((bluetoothClass.hashCode() & 0xFFFF00 >> 8));
	}
}
