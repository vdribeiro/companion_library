package companion.support.v8.location;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;

import java.util.Arrays;
import java.util.List;

import companion.support.v8.lang.Mathematical;

/**
 * This class contains utility methods for locations.
 * 
 * @author Vitor Ribeiro
 *
 */
public class LocationUtils {

	/** This prevents the class from being instantiated. 
	 */
	private LocationUtils() {
	}

	/**
	 * The universal gravitational constant in Newton-meter squared per kilogram squared.
	 */
	public static final double G = (6.6740831 * Math.pow(10,-11));

	/**
	 * The Earth's mass in kilograms.
	 */
	public static final double EARTH_MASS = 5.9722 * Math.pow(10, 24);

	/**
	 * The Earth's mean radius in meters as defined by IUGG.
	 */
	public static final double EARTH_MEAN_RADIUS = 6371009d;

	/**
	 * The Earth's gravitational acceleration in meters per second squared.
	 */
	public static final double G_FORCE = 9.80665;

	/**
	 * Get the best last known location.
	 * @param context of the caller.
	 * @return location.
	 */
	@SuppressWarnings({"MissingPermission", "ConstantConditions"})
	public static Location getLastKnownLocation(Context context) {
		Location bestLocation = null;

		try {
			LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
			List<String> providers = locationManager.getProviders(true);

			for (String provider : providers) {
				Location l = locationManager.getLastKnownLocation(provider);
				if (l == null) {
					continue;
				}

				if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
					bestLocation = l;
				}
			}
		} catch (Throwable t) {
			bestLocation = null;
		}

		return bestLocation;
	}

	/** Calculate haversine.
	 * @param x radians.
	 * @return angle in radians.
	 */
	public static double hav(double x) {
		double sinHalf = Math.sin(x / 2);
		return sinHalf * sinHalf;
	}

	/**
	 * Calculate inverse haversine.
	 * @param x between 0 and 1.
	 * @return positive value.
	 */
	public static double ahav(double x) {
		return 2 * Math.asin(Math.sqrt(x));
	}

	/**
	 * Get the number of satellites.
	 * 
	 * The Location object does not always have the information about the number of satellites.
	 * This information is in the Location extras and it is ROM dependent.
	 * 
	 * @param loc Location object.
	 * @return number of satellites.
	 */
	public static byte getNumberOfSatellites(Location loc) {
		byte nSats = -1;

		Object satExtra = null;
		try {
			satExtra = loc.getExtras().get("satellites");

			if (satExtra instanceof Integer) {
				nSats = ((Integer) satExtra).byteValue();
			} else {
				nSats = ((Byte) satExtra).byteValue();
			}
		} catch (Exception e) {
			// Ignore
		}

		return nSats;
	}

	/**
	 * Correct time offsets due to the leap year bug.
	 * 
	 * @param locTime the location timestamp.
	 * @param systemTime the system timestamp.
	 * @return the adjusted time.
	 */
	public static long correctTime(long locTime, long systemTime) {
		// One day seconds offset
		int oneDay = 86400000;

		// If error is between 23.75 and 24.25 hours, it very likely has the leap-year bug
		if (locTime >= systemTime + oneDay-900000 && locTime <= systemTime + oneDay+900000) {
			return locTime - oneDay;
		}

		return locTime;
	}

	/**
	 * Computes the approximate distance in meters between two
	 * locations and the initial and final bearings of the
	 * shortest path between them.
	 * Distance and bearing are defined using the
	 * WGS84 ellipsoid.
	 * 
	 * <p> Based on http://www.ngs.noaa.gov/PUBS_LIB/inverse.pdf 
	 * using the "Inverse Formula" (section 4)
	 *
	 * @param lat1 the starting latitude.
	 * @param lon1 the starting longitude.
	 * @param lat2 the ending latitude.
	 * @param lon2 the ending longitude.
	 * 
	 * @return an array of doubles that holds the results. 
	 * The computed distance is stored in result[0]. 
	 * The initial bearing is stored in result[1]. 
	 * The final bearing is stored in result[2].
	 */
	public static double[] calculateDistanceAndBearing(double lat1, double lon1, double lat2, double lon2) {

		double[] results = new double[3];
		results[0] = Double.NaN;
		results[1] = Double.NaN;
		results[2] = Double.NaN;

		if (Double.isNaN(lat1) || Double.isNaN(lat2) || Double.isNaN(lon1) || Double.isNaN(lon2)) {
			return results;
		}

		// number of iterations
		int maxiters = 20;
		
		// Convert lat/long to radians
		lat1 *= Math.PI / 180.0d;
		lat2 *= Math.PI / 180.0d;
		lon1 *= Math.PI / 180.0d;
		lon2 *= Math.PI / 180.0d;

		double a = 6378137.0d; // WGS84 major axis
		double b = 6356752.3142d; // WGS84 semi-major axis
		double f = (a - b) / a;
		double aSqMinusBSqOverBSq = (a * a - b * b) / (b * b);

		double l = lon2 - lon1;
		double A = 0.0d;
		double U1 = Math.atan((1.0 - f) * Math.tan(lat1));
		double U2 = Math.atan((1.0 - f) * Math.tan(lat2));

		double cosU1 = Math.cos(U1);
		double cosU2 = Math.cos(U2);
		double sinU1 = Math.sin(U1);
		double sinU2 = Math.sin(U2);
		double cosU1cosU2 = cosU1 * cosU2;
		double sinU1sinU2 = sinU1 * sinU2;

		double sigma = 0.0d;
		double deltaSigma = 0.0d;
		double cosSqAlpha = 0.0d;
		double cos2SM = 0.0d;
		double cosSigma = 0.0d;
		double sinSigma = 0.0d;
		double cosLambda = 0.0d;
		double sinLambda = 0.0d;

		double lambda = l; // initial guess
		for (int iter = 0; iter < maxiters; iter++) {
			double lambdaOrig = lambda;
			cosLambda = Math.cos(lambda);
			sinLambda = Math.sin(lambda);
			double t1 = cosU2 * sinLambda;
			double t2 = cosU1 * sinU2 - sinU1 * cosU2 * cosLambda;
			double sinSqSigma = t1 * t1 + t2 * t2; // (14)
			sinSigma = Math.sqrt(sinSqSigma);
			cosSigma = sinU1sinU2 + cosU1cosU2 * cosLambda; // (15)
			sigma = Math.atan2(sinSigma, cosSigma); // (16)
			double sinAlpha = (sinSigma == 0) ? 0.0 : cosU1cosU2 * sinLambda / sinSigma; // (17)
			cosSqAlpha = 1.0 - sinAlpha * sinAlpha;
			cos2SM = (cosSqAlpha == 0) ? 0.0 : cosSigma - 2.0 * sinU1sinU2 / cosSqAlpha; // (18)

			double uSquared = cosSqAlpha * aSqMinusBSqOverBSq; // defn
			A = 1 + (uSquared / 16384.0d) * // (3)
				(4096.0d + uSquared * (-768d + uSquared * (320.0d - 175.0d * uSquared)));
			double B = (uSquared / 1024.0d) * // (4)
					(256.0d + uSquared * (-128.0d + uSquared * (74.0d - 47.0d * uSquared)));
			double C = (f / 16.0d) * cosSqAlpha * (4.0d + f * (4.0d - 3.0d * cosSqAlpha)); // (10)
			double cos2SMSq = cos2SM * cos2SM;
			deltaSigma = B * sinSigma * // (6)
				(cos2SM + (B / 4.0d) *
				(cosSigma * (-1.0d + 2.0d * cos2SMSq) -
				(B / 6.0d) * cos2SM *
				(-3.0d + 4.0d * sinSigma * sinSigma) *
				(-3.0d + 4.0d * cos2SMSq)));

			lambda = l +
				(1.0d - C) * f * sinAlpha *
				(sigma + C * sinSigma *
				(cos2SM + C * cosSigma *
				(-1.0d + 2.0d * cos2SM * cos2SM))); // (11)

			double delta = (lambda - lambdaOrig) / lambda;
			if (Math.abs(delta) < 1.0e-12) {
				break;
			}
		}

		double distance = b * A * (sigma - deltaSigma);
		results[0] = distance;

		double initialBearing = Math.atan2(
			cosU2 * sinLambda,
			cosU1 * sinU2 - sinU1 * cosU2 * cosLambda
		);
		initialBearing *= 180.0d / Math.PI;
		results[1] = initialBearing;

		double finalBearing = Math.atan2(
			cosU1 * sinLambda,
			-sinU1 * cosU2 + cosU1 * sinU2 * cosLambda
		);
		finalBearing *= 180.0d / Math.PI;
		results[2] = finalBearing;

		return results;
	}

	/**
	 * Returns the heading from one point to another.
	 * 
	 * @param lat1 the starting latitude.
	 * @param lon1 the starting longitude.
	 * @param lat2 the ending latitude.
	 * @param lon2 the ending longitude.
	 * @return the heading in degrees clockwise from north.
	 */
	public static double computeHeading(double lat1, double lon1, double lat2, double lon2) {
		if (Double.isNaN(lat1) || Double.isNaN(lat2) || Double.isNaN(lon1) || Double.isNaN(lon2)) {
			return Double.NaN;
		}

		lat1 = Math.toRadians(lat1);
		lat2 = Math.toRadians(lat2);
		double dLng = Math.toRadians(lon2) - Math.toRadians(lon1);
		double heading = Math.atan2(
			Math.sin(dLng) * Math.cos(lat2), 
			Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(dLng)
		);

		heading = Math.toDegrees(heading);
		if (heading < -180 || heading >= 180) {
			heading = Mathematical.mod(heading + 180, 360) - 180;
		}

		return heading;
	}

	/**
	 * Returns the resulting coordinates from moving a distance from an origin
	 * in the specified heading (expressed in degrees clockwise from north).
	 * @param latitude coordinate.
	 * @param longitude coordinate.
	 * @param distance to travel.
	 * @param heading in degrees clockwise from north.
	 * @param planetRadius the planet's mean radius.
	 * @return an array of doubles that holds the latitude and longitude respectively.
	 */
	public static double[] computeOffset(double latitude, double longitude, double distance, double heading, double planetRadius) {
		distance /= planetRadius;
		heading = Math.toRadians(heading);
		latitude = Math.toRadians(latitude);
		double cosDistance = Math.cos(distance);
		double sinDistance = Math.sin(distance);
		double sinFromLat = Math.sin(latitude);
		double cosFromLat = Math.cos(latitude);
		double sinLat = cosDistance * sinFromLat + sinDistance * cosFromLat * Math.cos(heading);
		double dLng = Math.atan2(
			sinDistance * cosFromLat * Math.sin(heading),
			cosDistance - sinFromLat * sinLat
		);
		return new double[] {Math.toDegrees(Math.asin(sinLat)), Math.toDegrees(Math.toRadians(longitude) + dLng)};
	}

	/**
	 * Returns the resulting coordinates from moving a distance from an origin
	 * in the specified heading (expressed in degrees clockwise from north)
	 * considering the default earth's mean radius defined by IUGG.
	 * @param latitude coordinate.
	 * @param longitude coordinate.
	 * @param distance to travel.
	 * @param heading in degrees clockwise from north.
	 * @return an array of doubles that holds the latitude and longitude respectively.
	 */
	public static double[] computeOffset(double latitude, double longitude, double distance, double heading) {
		return computeOffset(latitude, longitude, distance, heading, EARTH_MEAN_RADIUS);
	}

	/**
	 * Given a list of ordered coordinates, 
	 * it returns the length of the given path.
	 * 
	 * @param latitudes list of ordered latitude values.
	 * @param longitudes list of ordered longitude values.
	 * @param planetRadius the planet's mean radius.
	 * @return length of the path, in meters.
	 */
	public static double computeLength(List<Double> latitudes, List<Double> longitudes, double planetRadius) {
		int len = latitudes.size();
		if (len != longitudes.size() || len < 2) {
			return 0;
		}

		double length = 0;
		double prevLat = Math.toRadians(latitudes.get(0));
		double prevLng = Math.toRadians(longitudes.get(0));
		for (int i = 0; i < len; i++) {
			double lat = Math.toRadians(latitudes.get(i));
			double lng = Math.toRadians(longitudes.get(i));
			double havDistance = hav(prevLat - lat) + 
				hav(prevLng - lng) * Math.cos(prevLat) * Math.cos(lat);
			length += ahav(havDistance);

			prevLat = lat;
			prevLng = lng;
		}

		return length * planetRadius;
	}

	/**
	 * Given a list of ordered coordinates, 
	 * it returns the length of the given path on Earth.
	 * 
	 * @param latitudes list of ordered latitude values.
	 * @param longitudes list of ordered longitude values.
	 * @return length of the path, in meters.
	 */
	public static double computeLength(List<Double> latitudes, List<Double> longitudes) {
		return computeLength(latitudes, longitudes, EARTH_MEAN_RADIUS);
	}

	/**
	 * Calculate the surface gravity using the MKS system,
	 * where the units for distance are meters,
	 * the units for mass are kilograms, and the units for time are seconds.
	 * @param mass in kilograms.
	 * @param radius in meters.
	 * @return surface gravity in meters per second squared.
	 */
	public static double calculateSurfaceGravity(double mass, double radius) {
		return (G * mass) / Math.pow(radius, 2);
	}

	/**
	 * Calculate the g force relative to Earth,
	 * where the units are Earth mass and Earth radius.
	 * @param mass in Earth mass.
	 * @param radius in Earth radius.
	 * @return g force of the planet.
	 */
	public static double calculateGForceRelativeToEarth(double mass, double radius) {
		return ((G * mass * EARTH_MASS) / Math.pow(radius * EARTH_MEAN_RADIUS, 2) / G_FORCE);
	}

	/**
	 * Convert right ascension given in hours, minutes and seconds,
	 * and declination given in degrees, minutes and seconds to degrees.
	 * @param rAscension right ascension string.
	 * @param declination string.
	 * @return an array of two doubles in degrees, or filled with NaN if the format is incorrect.
	 */
	public static double[] convertRightAscensionAndDeclinationToDegrees(String rAscension, String declination) {
		String[] ra = rAscension.split(" ");
		String[] dec = declination.split(" ");

		double res[] = new double[2];

		try {
			res[0] = ((Float.parseFloat(ra[0]) * 15) +
					(Float.parseFloat(ra[1]) / 4) +
					(Float.parseFloat(ra[2]) / 240));

			res[1] = ((Float.parseFloat(dec[0])) +
					(Float.parseFloat(dec[1]) / 60) +
					(Float.parseFloat(dec[2]) / 360));
		} catch (Exception e) {
			Arrays.fill(res, Double.NaN);
		}

		return res;
	}

}