package companion.support.v8.graphics;

import android.graphics.Color;
import android.util.SparseArray;

/**
 * Utility class with a bundle of color methods.
 * 
 * @author Vitor Ribeiro
 * 
 */
public class ColorUtils {

	/** This prevents the class from being instantiated. 
	 */
	private ColorUtils() {
	}

	public static class ColorInterval {
		/** Initial color */
		public final int color1;
		/** Final color */
		public final int color2;
		/** The period over which the color changes from color1 to color2. */
		public final float duration;

		public ColorInterval(int color1, int color2, float duration) {
			this.color1 = color1;
			this.color2 = color2;
			this.duration = duration;
		}
	}

	/**
	 * Converts the YUV used in the Android NV21 format into an
	 * RGB value using code from 
	 * http://msdn.microsoft.com/en-us/library/ms893078
	 * @param nY - y value (from WxH byte array)
	 * @param nU - u value (second byte in WxH/2 byte array)
	 * @param nV - v value (first byte in WxH/2 byte array)
	 * @return Android Color for the converted color
	 */
	public static int YUVToColor(int nY, int nU, int nV) {
		int nC = nY - 16;
		int nD = nU - 128;
		int nE = nV - 128;

		int nR = Math.max(0, Math.min(255, ((298 * nC            + 409 * nE + 128) >> 8)));
		int nG = Math.max(0, Math.min(255, ((298 * nC - 100 * nD - 208 * nE + 128) >> 8)));
		int nB = Math.max(0, Math.min(255, ((298 * nC + 516 * nD            + 128) >> 8)));
		return Color.argb(255, nR, nG, nB);
	}

	/**
	 * Converts from RGB to YUV using code from 
	 * http://msdn.microsoft.com/en-us/library/ms893078.
	 * Returns the value as a Android Color value in with the red byte
	 * is the Y value, green the U value, and blue the V value.
	 * @param nR - red value (0-255)
	 * @param nG - green value (0-255)
	 * @param nB - blue value (0-255)
	 * @return Android Color value.
	 */
	public static int RGBToYUV(int nR, int nG, int nB) {
		int nY = (( 66 * nR + 129 * nG +  25 * nB + 128) >> 8) +  16;
		int nU = ((-38 * nR -  74 * nG + 112 * nB + 128) >> 8) + 128;
		int nV = ((112 * nR -  94 * nG -  18 * nB + 128) >> 8) + 128;
		return Color.argb(255, nY, nU, nV);
	}

	/**
	 * Generates a color map from a given array of colors and the fractions
	 * that the colors represent by interpolating between their HSV values.
	 * The colors and starting points must be parallel arrays.
	 * 
	 * @param colors to be used in the gradient.
	 * @param startPoints the starting point for each color in increasing order, 
	 * given as a percentage of the maximum intensity, from 0 to 1.
	 * @param size of a color map.
	 * @param opacity applied to all colors.
	 * @return the generated color map based on the gradient.
	 */
	public static int[] generateColorMap(int[] colors, float[] startPoints, int size, double opacity) {
		if (colors.length != startPoints.length || colors.length == 0) {
			return null;
		}
		for (int i = 1; i < startPoints.length; i++) {
			if (startPoints[i] <= startPoints[i - 1]) {
				return null;
			}
		}
		
		// Color intervals
		SparseArray<ColorInterval> colorIntervals = new SparseArray<ColorInterval>();
		// Create first color if not already created
		// The initial color is transparent by default
		if (startPoints[0] != 0) {
			int initialColor = Color.argb(0, Color.red(colors[0]), Color.green(colors[0]), Color.blue(colors[0]));
			colorIntervals.append(0, new ColorInterval(initialColor, colors[0], size * startPoints[0]));
		}
		
		// Generate color intervals
		for (int i = 1; i < colors.length; i++) {
			colorIntervals.append(((int) (size * startPoints[i - 1])),
				new ColorInterval(colors[i - 1], colors[i], (size * (startPoints[i] - startPoints[i - 1]))));
		}
		
		// Extend to a final color
		// If color for 100% intensity is not given, the color of highest intensity is used.
		if (startPoints[startPoints.length - 1] != 1) {
			int i = startPoints.length - 1;
			colorIntervals.append(((int) (size * startPoints[i])),
				new ColorInterval(colors[i], colors[i], size * (1 - startPoints[i])));
		}
		
		int[] colorMap = new int[size];
		ColorInterval interval = colorIntervals.get(0);
		int start = 0;
		for (int i = 0; i < size; i++) {
			if (colorIntervals.indexOfKey(i)>=0) {
				interval = colorIntervals.get(i);
				start = i;
			}
			
			// Calculate fraction of the distance between color1 and color2
			float ratio = (i - start) / interval.duration;
			
			// Interpolate between two colors using their HSV values.
			int alpha = (int) ((Color.alpha(interval.color2) - Color.alpha(interval.color1)) * ratio + Color.alpha(interval.color1));
			float[] hsv1 = new float[3];
			Color.RGBToHSV(Color.red(interval.color1), Color.green(interval.color1), Color.blue(interval.color1), hsv1);
			float[] hsv2 = new float[3];
			Color.RGBToHSV(Color.red(interval.color2), Color.green(interval.color2), Color.blue(interval.color2), hsv2);

			// adjust so that the shortest path on the color wheel will be taken
			if (hsv1[0] - hsv2[0] > 180) {
				hsv2[0] += 360;
			} else if (hsv2[0] - hsv1[0] > 180) {
				hsv1[0] += 360;
			}

			// Interpolate using calculated ratio
			float[] result = new float[3];
			for (int j = 0; j < 3; j++) {
				result[j] = (hsv2[j] - hsv1[j]) * (ratio) + hsv1[j];
			}

			colorMap[i] = Color.HSVToColor(alpha, result);
		}
		
		if (opacity != 1) {
			for (int i = 0; i < size; i++) {
				int c = colorMap[i];
				colorMap[i] = Color.argb((int) (Color.alpha(c) * opacity),
					Color.red(c), Color.green(c), Color.blue(c));
			}
		}

		return colorMap;
	}
}
