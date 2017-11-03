package companion.support.v8.lang;

import java.util.Arrays;

/** 
 * Utility class with a bundle of mathematical and physics-related methods.
 * 
 * @author Vitor Ribeiro
 * @author Joao Rodrigues
 */
public class Mathematical {
	
	/** This prevents the class from being instantiated. 
	 */
	private Mathematical() {
	}
	
	/** Convert from meters To kilometers.
	 * @param value to convert.
	 * @return converted value.
	 */
	public static double metersToKilometers(double value) {
		return (value / 1000d);
	}

	/** Convert from kilometers to meters.
	 * @param value to convert.
	 * @return converted value.
	 */
	public static double kilometerstoMeters(double value) {
		return (value * 1000d);
	}

	/** Convert from meters per second to kilometers per hour.
	 * @param value to convert.
	 * @return converted value.
	 */
	public static double metersPerSecondToKilometersPerHour(double value) {
		return (value * 3.6d);
	}

	/** Convert from kilometers per hour to meters per second.
	 * @param value to convert.
	 * @return converted value.
	 */
	public static double kilometersPerHourToMetersPerSecond(double value) {
		return (value / 3.6d);
	}

	/** Convert from meters per second squared to kilometers per hour second.
	 * @param value to convert.
	 * @return converted value.
	 */
	public static double metersPerSecondSquaredToKilometersPerHourSecond(double value) {
		return (value * 3.6d);
	}

	/** Convert from meters per second squared to kilometers per hour squared.
	 * @param value to convert.
	 * @return converted value.
	 */
	public static double metersPerSecondSquaredToKilometersPerHourSquared(double value) {
		return (value * 12960d);
	}

	/** Convert from kilometers per hour squared to meters per second squared.
	 * @param value to convert.
	 * @return converted value.
	 */
	public static double kilometersPerHourSquaredToMetersPerSecondSquared(double value) {
		return (value / 12960d);
	}
	
	/** Returns the result of rounding the argument to two decimals.
	 * 
	 * @param round number to round.
	 * @return rounded number.
	 */
	public static double roundTwoDecimals(double round) {
		if (round==Double.NaN) {
			return round;
		}
		return (double) Math.round(round * 100) / 100;
	}
	
	/**
     * Returns the non-negative remainder of x / m.
     * @param x operand.
     * @param m modulus.
     */
	public static double mod(double x, double m) {
        return ((x % m) + m) % m;
    }
	
	/** 
	 * Calculates Speed.
	 * 
	 * @param idistance in meters.
	 * @param fdistance in meters.
	 * @param itime in seconds.
	 * @param ftime in seconds.
	 * @return speed in meters/second. 
	 */
	public static double calculateSpeed(double idistance, double fdistance, double itime, double ftime) {
		if (Double.isNaN(idistance) || Double.isNaN(fdistance) || Double.isNaN(itime) || Double.isNaN(ftime)) {
			return Double.NaN;
		}

		double deltaDistance = (fdistance - idistance);
		double deltaTime = (ftime - itime);
		double speed = (deltaDistance / deltaTime);
		return speed;
	}

	/** 
	 * Calculates Acceleration.
	 * 
	 * @param ispeed in meters/second. 
	 * @param fspeed in meters/second.
	 * @param itime in seconds.
	 * @param ftime in seconds.
	 * @return acceleration in meters/second^2. 
	 */
	public static double calculateAcceleration(double ispeed, double fspeed, double itime, double ftime) {
		if (Double.isNaN(ispeed) || Double.isNaN(fspeed) || Double.isNaN(itime) || Double.isNaN(ftime)) {
			return Double.NaN;
		}

		double deltaSpeed = (fspeed - ispeed);
		double deltaTime = (ftime - itime);
		double acceleration = (deltaSpeed / deltaTime);
		return acceleration;
	}

	/** 
	 * Calculates Acceleration as the Least Square Error slope between the 3 surrounding speeds
	 * 
	 * @param speed in meters/second. 
	 * @param time in seconds.
	 * @param range is the maximum allowed distance between neighbors.
	 * @return acceleration in meters/second^2. 
	 */
	public static double[][] calculateAcceleration(double[] speed, double[] time, double range) {
		return slopeLeastSquares(time, speed, range, 3, false);
	}

	/** 
	 * Calculates Inclination.
	 * 
	 * @param ialtitude in meters.
	 * @param faltitude in meters.
	 * @param idistance in meters.
	 * @param fdistance in meters.
	 * @return inclination in degrees.
	 */
	public static double calculateInclination(double ialtitude, double faltitude, double idistance, double fdistance) {
		if (Double.isNaN(ialtitude) || Double.isNaN(faltitude) || Double.isNaN(idistance) || Double.isNaN(fdistance)) {
			return Double.NaN;
		}

		double deltaAltitude = (ialtitude - faltitude);
		double deltaDistance = (idistance - fdistance);
		double inclination = (double) (Math.atan(deltaAltitude / deltaDistance) * (180d / Math.PI));
		return inclination;
	}

	/** 
	 * Calculates Inclination as the Least Square Error slope between the surrounding altitudes.
	 * 
	 * @param altitude in meters. 
	 * @param distance in meters.
	 * @param range is the maximum allowed distance between neighbors.
	 * @param window is the starting distance between neighbors.
	 * @return inclination in degrees. 
	 */
	public static double[][] calculateInclination(double[] altitude, double[] distance, double range, double window) {
		// The distance should be the one traveled so far: monotonic and sorted

		// perform a moving average of size 3 on the GPS Altitudes (Low-Pass)
		//double[] filteredAltitude =  movingAverage3(distance, altitude, 60);

		// calculate the inclination as the LeasSquareError-slope between the surrounding altitudes
		double[][] filteredInclination = slopeLeastSquares(distance, altitude, range, window, true);

		// transform inclination from Altitude/Distance (m/m) to Degrees.
		for (int i = 0; i < filteredInclination.length; i++) {
			try {
				filteredInclination[i][0] = (double) (Math.atan(filteredInclination[i][0]) * (180.0d / Math.PI));
			} catch (Exception e) {
				filteredInclination[i][0] = Double.NaN;
			}
		}

		return filteredInclination;
	}
	
	/** Calculate the quadratic mean of an array of values.
	 * @param values
	 * @return root mean squared
	 */
	public static double rootMeanSquared(double[] values) {
		double ms = 0;
		for (int i = 0; i < values.length; i++) {
			ms += values[i] * values[i];
		}
		ms /= values.length;
		return Math.sqrt(ms);
	}
	
	/** Applies a low-pass filter. This reduces the amplitude of values with frequencies higher than the cutoff frequency.
	 * @param input values to filter.
	 * @param output values to filter.
	 * @param alpha the time smoothing constant to apply. A smaller value basically means more smoothing. If alpha is 1 or 0, no filter applies.
	 * @return filtered values.
	 */
	public static float[] lowPassFilter(float[] input, float[] output, float alpha) {
	    if (output == null) {
	    	return input;
	    }

	    for (int i = 0; i < input.length; i++) {
	        output[i] = output[i] + alpha * (input[i] - output[i]);
	    }
	    return output;
	}

	/** Apply Linear Interpolation.
	 * 
	 * @param x values of the horizontal axis. Must be monotonic and sorted.
	 * @param y corresponding Y values. Must have the same size of x.
	 * @param xi desired x positions to be interpolated.
	 * @param maxDistance maximum allowed distance between neighbors. Use 0 for infinity.
	 * @return yi array of the Y values at the desired x positions.
	 * Returned position = double.NaN if the neighbors are not found. 
	 */
	public static double[] linearInterpolation(double[] x, double[] y, double[] xi, double maxDistance) {

		if (x.length != y.length) {
			return null;
		}
		if (x.length <= 1) {
			return null;
		}
		double[] dx = new double[x.length - 1];
		double[] dy = new double[x.length - 1];
		double[] slope = new double[x.length - 1];
		double[] intercept = new double[x.length - 1];

		// Calculate the line equation (i.e. slope and intercept) between each point
		for (int i = 0; i < x.length - 1; i++) {
			dx[i] = x[i + 1] - x[i];
			if (dx[i] == 0) {
				return null;
			}
			if (dx[i] < 0) {
				return null;
			}
			dy[i] = y[i + 1] - y[i];
			slope[i] = dy[i] / dx[i];
			intercept[i] = y[i] - x[i] * slope[i];
		}

		// Perform the interpolation here
		double[] yi = new double[xi.length];
		for (int j = 0; j < xi.length; j++) {
			if ((xi[j] > x[x.length - 1]) || (xi[j] < x[0])) {
				yi[j] = Double.NaN;
			} else {
				int loc = Arrays.binarySearch(x, xi[j]);
				if (loc < -1) {
					loc = (-loc - 1) - 1;
					if( loc == x.length-1 || (maxDistance != 0 && (Math.abs(x[loc+1] - x[loc]) > maxDistance*2.0))) {
						yi[j] = Double.NaN;
					} else {
						yi[j] = slope[loc] * xi[j] + intercept[loc];
					}
				} else {
					yi[j] = y[loc];
				}
			}
		}

		return yi;
	}

	/** Get Nearest Neighbor.
	 * 
	 * @param x values of the horizontal axis
	 * @param y corresponding Y values. Must have the same size of x
	 * @param xi desired x positions to be found for nearest
	 * @param maxDistance maximum allowed distance between neighbors. Use 0 for infinity
	 * @return yi array of the Y values at the desired x positions. 
	 * Returned position = double.NaN if the neighbors are not found. 
	 */
	public static double[] nearestNeighbour(double[] x, double[] y, double[] xi, double maxDistance) {

		if (x.length != y.length) {
			return null;
		}
		if (x.length == 1) {
			return null;
		}

		// Perform the interpolation here
		double[] yi = new double[xi.length];
		for (int i = 0; i < xi.length; i++) {
			if ((xi[i] > x[x.length - 1]) || (xi[i] < x[0])) {
				yi[i] = Double.NaN;
			} else {
				int loc = Arrays.binarySearch(x, xi[i]);
				if (loc < -1) {
					loc = -loc - 1;
					double d0 = Math.abs(x[loc - 1] -  xi[i]);
					double d1 = Math.abs(x[loc] -  xi[i]);
					if ( maxDistance != 0 && d0 > maxDistance && d1 > maxDistance) {
						yi[i] = Double.NaN;
					} else {
						if( d0 <= d1 ) {
							yi[i] = y[loc-1];
						} else {
							yi[i] = y[loc];
						}
					}
				} else {
					yi[i] = y[loc];
				}
			}
		}

		return yi;
	}

	/** Get Least Square Error.
	 * 
	 * @param x values of the horizontal axis. Must be monotonic and sorted.
	 * @param y corresponding Y values. Must have the same size of x.
	 * @param maxDistance maximum allowed distance between neighbors. Use 0 for infinity.
	 * @param interval corresponding interval.
	 * @param spatial indicates if it is time or spatially limited.
	 * @return array with y-slope/inclination for every x. 
	 * Returned position = double.NaN if only the middle point is valid.
	 */
	public static double[][] slopeLeastSquares(double[] x, double[] y, double maxDistance, double interval, boolean spatial) {

		if (x.length != y.length) {
			return null;
		}
		if (x.length < interval) {
			return null;
		}

		int radius = (int) (interval/2);
		double[][] yi = new double[x.length][2];
		double ln, lx, ly, lxx, lxy;
		for (int i = 0; i < x.length; i++) {
			if (i < x.length - 1 && x[i + 1] < x[i]) {
				return null;
			}
			
			ln = 0.0d;
			lx = 0.0d;
			ly = 0.0d;
			lxx = 0.0d;
			lxy = 0.0d;
			
			if(!spatial) { // Time limited
				for (int k = -radius; k <= radius; k++) {
					if (i + k >= 0 && i + k < x.length && Math.abs(x[i + k] - x[i]) <= maxDistance) {
						ln++;
						lx += x[i + k];
						ly += y[i + k];
						lxx += x[i + k] * x[i + k];
						lxy += x[i + k] * y[i + k];
					}	
				}
			} else { // Spatially limited
				for (int k = 0; i + k >= 0 && Math.abs(x[i + k] - x[i]) <= radius; k--) {
					ln++;
					lx += x[i + k];
					ly += y[i + k];
					lxx += x[i + k] * x[i + k];
					lxy += x[i + k] * y[i + k];
				}
				for (int k = 1; i + k < x.length && Math.abs(x[i + k] - x[i]) <= radius; k++) {
					ln++;
					lx += x[i + k];
					ly += y[i + k];
					lxx += x[i + k] * x[i + k];
					lxy += x[i + k] * y[i + k];
				}
				if (ln <= 3) { // Moving fast, use first up to meters=maxDistance
					ln = 0.0d;
					lx = 0.0d;
					ly = 0.0d;
					lxx = 0.0d;
					lxy = 0.0d;
					for (int k = 0; i + k >= 0 && Math.abs(x[i + k] - x[i]) <= radius*2; k--) {
						ln++;
						lx += x[i + k];
						ly += y[i + k];
						lxx += x[i + k] * x[i + k];
						lxy += x[i + k] * y[i + k];
					}
					for (int k = 1; i + k < x.length && Math.abs(x[i + k] - x[i]) <= radius*2; k++) {
						ln++;
						lx += x[i + k];
						ly += y[i + k];
						lxx += x[i + k] * x[i + k];
						lxy += x[i + k] * y[i + k];
					}
				}
				if (ln <= 3) { // Moving fast, use first up to meters=maxDistance
					ln = 0.0d;
					lx = 0.0d;
					ly = 0.0d;
					lxx = 0.0d;
					lxy = 0.0d;
					for (int k = 0; i + k >= 0 && Math.abs(x[i + k] - x[i]) <= radius*4; k--) {
						ln++;
						lx += x[i + k];
						ly += y[i + k];
						lxx += x[i + k] * x[i + k];
						lxy += x[i + k] * y[i + k];
					}
					for (int k = 1; i + k < x.length && Math.abs(x[i + k] - x[i]) <= radius*4; k++) {
						ln++;
						lx += x[i + k];
						ly += y[i + k];
						lxx += x[i + k] * x[i + k];
						lxy += x[i + k] * y[i + k];
					}
				}
				if (ln <= 3) { // Moving fast, use first up to meters=maxDistance
					ln = 0.0d;
					lx = 0.0d;
					ly = 0.0d;
					lxx = 0.0d;
					lxy = 0.0d;
					for (int k = 0; i + k >= 0 && Math.abs(x[i + k] - x[i]) <= radius*8; k--) {
						ln++;
						lx += x[i + k];
						ly += y[i + k];
						lxx += x[i + k] * x[i + k];
						lxy += x[i + k] * y[i + k];
					}
					for (int k = 1; i + k < x.length && Math.abs(x[i + k] - x[i]) <= radius*8; k++) {
						ln++;
						lx += x[i + k];
						ly += y[i + k];
						lxx += x[i + k] * x[i + k];
						lxy += x[i + k] * y[i + k];
					}
				}
				if (ln <= 3) { // Moving fast, use first up to meters=maxDistance
					if (i - 1 >= 0 && Math.abs(x[i - 1] - x[i]) <= maxDistance) {
						ln++;
						lx += x[i - 1];
						ly += y[i - 1];
						lxx += x[i - 1] * x[i - 1];
						lxy += x[i - 1] * y[i - 1];
					}	
					if (i + 1 < x.length && Math.abs(x[i + 1] - x[i]) <= maxDistance) {
						ln++;
						lx += x[i + 1];
						ly += y[i + 1];
						lxx += x[i + 1] * x[i + 1];
						lxy += x[i + 1] * y[i + 1];
					}
				}
			}

			if (ln <= 1) {
				// If there's only the middle point in the window, no slope can be found
				yi[i][0] = Double.NaN;
			} else {
				yi[i][0] = (ln * lxy - lx * ly) / (ln * lxx - lx * lx);
			}
			yi[i][1] = ln;
		}
		return yi;
	}

	/** Get Moving Average.
	 * 
	 * @param x values of the horizontal axis. Must be monotonic and sorted.
	 * @param y corresponding Y values. Must have the same size of x.
	 * @param maxDistance maximum allowed distance between neighbors. Use 0 for infinity.
	 * @return array of the filtered Y values at the x positions.
	 */
	public static double[] movingAverage3(double[] x, double[] y, double maxDistance) {

		if (x.length != y.length) {
			return null;
		}
		if (x.length <= 2) {
			return null;
		}

		double[] yi = new double[x.length];
		double w, sum;
		for (int i = 0; i < x.length; i++) {
			if (i < x.length - 1 && x[i + 1] <= x[i]) {
				return null;
			}
			w = 0.0d;
			sum = 0.0d;

			if (i == 0 || i == x.length - 1 || (x[i] - x[i - 1]) > maxDistance|| (x[i + 1] - x[i]) > maxDistance) {
				yi[i] = y[i];
				continue;
			}
			w++;
			sum += y[i - 1];
			w++;
			sum += y[i];
			w++;
			sum += y[i + 1];

			yi[i] = sum / w;
		}
		return yi;
	}
}
