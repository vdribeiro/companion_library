package companion.support.v8.location;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.text.TextUtils.SimpleStringSplitter;


/**
 * NMEA is a specification for communication between marine electronics 
 * such as GPS receivers and many other types of instruments. 
 * It has been defined by, and is controlled by, the National Marine Electronics Association. 
 * <p>
 * This class contains the NMEA object representation and
 * methods to parse NMEA sentences, and can compute its checksum.
 * 
 * @author Vitor Ribeiro
 * @author Herbert von Broeuschmeul
 */
public class NMEA {

	/** Type of sentence */
	public String name;
	/** UTC time of fix. */
	public String time;
	/** UTC date of fix. */
	public String date;
	/** Latitude. */
	public String latitude;
	/** Direction (N/S). */
	public String latitudeDirection;
	/** Longitude. */
	public String longitude;
	/** Direction (E/W). */
	public String longitudeDirection;
	/** Magnetic Variation. */
	public String magneticVariation;
	/** Magnetic variation direction (E/W). */
	public String magneticDirection;
	/** Speed over the ground in knots. */		 
	public String speedKnots;
	/** Speed over the ground in Kilometers per hour. */		 
	public String speedKm;
	/** Track angle in degrees True. */
	public String bearing;
	/** Fix quality: 
	 * 0 = invalid
	 * 1 = GPS fix (SPS)
	 * 2 = DGPS fix
	 * 3 = PPS fix
	 * 4 = Real Time Kinematic
	 * 5 = Float RTK
	 * 6 = estimated (dead reckoning) (2.3 feature)
	 * 7 = Manual input mode
	 * 8 = Simulation mode */
	public String fixQuality;
	/** Fix type: 1 - no fix; 2 - 2D; 3 - 3D. */
	public String fixType;
	/** Fix status: A=active, V=void, D=differential, E=estimated, N=not valid, S=simulator. */ 
	public String fixStatus;
	/** Mode: A = Auto selection of 2D or 3D fix; M = manual. */
	public String mode;
	/** Number of satellites being tracked. */
	public String numeberOfSatellites;
	/** Position dilution of precision. */
	public String pdop;
	/** Horizontal dilution of precision. */
	public String hdop;
	/** Vertical dilution of precision. */
	public String vdop;
	/** Altitude in Meters, above mean sea level. */
	public String altitude;
	/** Height of mean sea level above WGS84 ellipsoid. */
	public String geoHeight;
	/** Time in seconds since last DGPS update. */
	public String timeDGPS;
	/** DGPS station ID number. */
	public String idDGPS;
	/** Checksum. */
	public String checksum;
	
	public NMEA() {
		super();
		
		name = null;
		time = null;
		date = null;
		latitude = null;
		latitudeDirection = null;
		longitude = null;
		longitudeDirection = null;
		magneticVariation = null;
		magneticDirection = null;	 
		speedKnots = null;	 
		speedKm = null;
		bearing = null;
		fixQuality = null;
		fixType = null; 
		fixStatus = null;
		mode = null;
		numeberOfSatellites = null;
		pdop = null;
		hdop = null;
		vdop = null;
		altitude = null;
		geoHeight = null;
		timeDGPS = null;
		idDGPS = null;
		checksum = null;
	}
	
	/** 
	 * Parse a NMEA Sentence. 
	 * There are several types of sentences. 
	 * The supported types are:
	 * <ul>
	 * <li>GPGGA</li>
	 * <li>GPRMC</li>
	 * <li>GPGSA</li>
	 * <li>GPVTG</li>
	 * <li>GPGLL</li>
	 * </ul>
	 * <p>
	 * NOTE: This method still needs refining and should only be used for debug purposes for now. 
	 * 
	 * @param NMEA sentence.
	 * @return NMEA object.
	 * @throws Exception
	 */
	public static NMEA parseNmeaSentence(String nmeaSentence) throws Exception {
		String sentence = null;
		
		Pattern patern = Pattern.compile("\\$([^*$]*)(?:\\*([0-9A-F][0-9A-F]))?\r\n");
		Matcher matcher = patern.matcher(nmeaSentence);

		if (matcher.matches()){
			NMEA nmea = new NMEA();
			
			nmea.name = matcher.group(0);
			sentence = matcher.group(1);
			nmea.checksum = matcher.group(2);

			SimpleStringSplitter splitter = new SimpleStringSplitter(',');
			splitter.setString(sentence);
			String command = splitter.next();
			

			if (command.equalsIgnoreCase("GPGGA")) {
				// $GPGGA,123456,1234.123,N,1234.123,E,1,99,0.1,123.4,M,12.3,M,1,1*1A
				// Where:
				//  GGA          Global Positioning System Fix Data
				//  123456       Fix taken at 12:34:56 UTC
				//  1234.123,N   Latitude 12 deg 34.123' N
				//  1234.123,E   Longitude 12 deg 34.123' E
				//  1            Fix quality
				//  99           Number of satellites being tracked
				//  0.1          Horizontal dilution of position
				//  123.4,M      Altitude in Meters, above mean sea level
				//  12.3,M       Height of mean sea level above WGS84 ellipsoid
				//  1            Time in seconds since last DGPS update
				//  1            DGPS station ID number
				//  *1A          The checksum data, always begins with *
				nmea.name = "GPGGA";

				// UTC time of fix.
				nmea.time = splitter.next();
				// Latitude.
				nmea.latitude = splitter.next();
				// Direction (N/S).
				nmea.latitudeDirection = splitter.next();
				// Longitude.
				nmea.longitude = splitter.next();
				// Direction (E/W).
				nmea.longitudeDirection = splitter.next();
				// Fix quality: 
				// 0 = invalid
				// 1 = GPS fix (SPS)
				// 2 = DGPS fix
				// 3 = PPS fix
				// 4 = Real Time Kinematic
				// 5 = Float RTK
				// 6 = estimated (dead reckoning) (2.3 feature)
				// 7 = Manual input mode
				// 8 = Simulation mode
				nmea.fixQuality = splitter.next();
				// Number of satellites being tracked.
				nmea.numeberOfSatellites = splitter.next();
				// Horizontal dilution of position.
				nmea.hdop = splitter.next();
				// Altitude in Meters, above mean sea level.
				nmea.altitude = splitter.next();
				// Height of mean sea level above WGS84 ellipsoid.
				nmea.geoHeight = splitter.next();
				// Time in seconds since last DGPS update.
				nmea.timeDGPS = splitter.next();
				// DGPS station ID number.
				nmea.idDGPS = splitter.next();
				// Checksum.
				//nmea.checksum = splitter.next();

			} else if (command.equalsIgnoreCase("GPRMC")){
				// $GPRMC,123456,A,1234.123,N,1234.123,E,123.4,123.4,121212,123.4,E*1A
				// Where:
				//	RMC          Recommended Minimum sentence C
				//	123456       Fix taken at 12:34:56 UTC
				//	A            Status.
				//	1234.123,N   Latitude 12 deg 34.123' N
				//	1234.123,E   Longitude 12 deg 34.123' E
				//	123.4        Speed over the ground in knots
				//	123.4        Track angle in degrees True
				//	121212       Date - 12/12/12
				//	123.4,E      Magnetic Variation
				//	*1A          The checksum data, always begins with *
				nmea.name = "GPRMC";

				// UTC time of fix.
				nmea.time = splitter.next();
				// Fix status: A=active, V=void, D=differential, E=estimated, N=not valid, S=simulator. 
				nmea.fixStatus = splitter.next();
				// Latitude.
				nmea.latitude = splitter.next();
				// Direction (N/S).
				nmea.latitudeDirection = splitter.next();
				// Longitude.
				nmea.longitude = splitter.next();
				// Direction (E/W).
				nmea.longitudeDirection = splitter.next();
				// Speed over the ground in knots.		 
				nmea.speedKnots = splitter.next();
				// Track angle in degrees True.
				nmea.bearing = splitter.next();
				// UTC date of fix.
				nmea.date = splitter.next();
				// Magnetic Variation.
				nmea.magneticVariation = splitter.next();
				// Magnetic variation direction (E/W).
				nmea.magneticDirection = splitter.next();
				// Checksum.
				//nmea.checksum = splitter.next();

			} else if (command.equalsIgnoreCase("GPGSA")){
				// $GPGSA,A,3,01,02,03,04,05,06,07,08,09,10,11,12,2.5,1.5,2.0*1A
				// Where:
				//  GSA      Satellite status
				//  A        Auto selection of 2D or 3D fix (M = manual) 
				//  3        3D fix
				//  01 to 12 PRNs of satellites used for fix (space for 12) 
				//  2.5      PDOP (Position dilution of precision) 
				//  1.5      Horizontal dilution of precision (HDOP) 
				//  2.0      Vertical dilution of precision (VDOP)
				//  *1A      The checksum data, always begins with *
				nmea.name = "GPGSA";

				// Mode: A = Auto selection of 2D or 3D fix; M = manual.
				nmea.mode = splitter.next();
				// Fix type: 1 - no fix; 2 - 2D; 3 - 3D.
				nmea.fixType = splitter.next();
				// Discard PRNs of satellites used for fix (space for 12) 
				for (int i=0 ; ((i<12)&&(!"1".equals(nmea.fixType))); i++){
					splitter.next();
				}
				// Position dilution of precision (float).
				nmea.pdop = splitter.next();
				// Horizontal dilution of precision (float).
				nmea.hdop = splitter.next();
				// Vertical dilution of precision (float).
				nmea.vdop = splitter.next();		
				// Checksum.
				//nmea.checksum = splitter.next();

			} else if (command.equalsIgnoreCase("GPVTG")){
				// $GPVTG,123.4,T,123.4,M,123.4,N,123.4,K*1A
				// Where:
				//	VTG          Track made good and ground speed
				//	123.4,T      True track made good (degrees)
				//	123.4,M      Magnetic track made good
				//	123.4,N      Ground speed, knots
				//	123.4,K      Ground speed, Kilometers per hour
				//  *1A          The checksum data, always begins with *
				nmea.name = "GPVTG";

				// Track angle in degrees True.
				nmea.bearing = splitter.next();
				// T.
				splitter.next();
				// Magnetic track made good.
				nmea.magneticVariation = splitter.next();
				// M.
				splitter.next();
				// Speed over the ground in knots.		 
				nmea.speedKnots = splitter.next();
				// N.
				splitter.next();
				// Speed over the ground in Kilometers per hour.		 
				nmea.speedKm = splitter.next();
				// K.
				splitter.next();
				// Checksum.
				//nmea.checksum = splitter.next();

			} else if (command.equalsIgnoreCase("GPGLL")){
				// $GPGLL,1234.12,N,1234.12,E,123456,A,*1A
				// Where:
				//	GLL          Geographic position, Latitude and Longitude
				//	1234.12,N    Latitude 12 deg. 34.12 min. North
				//	1234.12,W    Longitude 12 deg. 34.12 min. West
				//	123412       Fix taken at 12:34:56 UTC
				//	A            Status
				//  *1A          The checksum data, always begins with *
				nmea.name = "GPGLL";

				// Latitude.
				nmea.latitude = splitter.next();
				// Direction (N/S).
				nmea.latitudeDirection = splitter.next();
				// Longitude.
				nmea.longitude = splitter.next();
				// Direction (E/W).
				nmea.longitudeDirection = splitter.next();
				// UTC time of fix.
				nmea.time = splitter.next();
				// Fix status: A=active, V=void, D=differential, E=estimated, N=not valid, S=simulator.
				nmea.fixStatus = splitter.next();
				// Checksum.
				//nmea.checksum = splitter.next();
			}
		}
		return null;
	}

	/** Convert latitude from degrees to double representation.
	 * 
	 * @param lat in degrees.
	 * @param orientation either N or S.
	 * @return latitude as a double.
	 */
	public static double parseNmeaLatitude(String lat, String orientation) {
		double latitude = 0.0;
		if (lat != null && orientation != null && lat.length()>0 && orientation.length()>0) {
			double temp1 = Double.parseDouble(lat);
			double temp2 = Math.floor(temp1/100d); 
			double temp3 = (temp1/100 - temp2)/0.6d;
			
			if (orientation.equals("S")) {
				latitude = -(temp2+temp3);
			} else if (orientation.equals("N")) {
				latitude = (temp2+temp3);
			}
		}
		
		return latitude;
	}

	/** Convert longitude from degrees to double representation.
	 * 
	 * @param lon in degrees.
	 * @param orientation either W or E.
	 * @return longitude as a double.
	 */
	public static double parseNmeaLongitude(String lon, String orientation) {
		double longitude = 0.0;
		if (lon != null && orientation != null && lon.length()>0 && orientation.length()>0) {
			double temp1 = Double.parseDouble(lon);
			double temp2 = Math.floor(temp1/100d); 
			double temp3 = (temp1/100d - temp2)/0.6d;
			
			if (orientation.equals("W")) {
				longitude = -(temp2+temp3);
			} else if (orientation.equals("E")) {
				longitude = (temp2+temp3);
			}
		}
		
		return longitude;
	}

	/** Convert speed to meters per second.
	 * @param speed unit.
	 * @param metric unit representation.
	 * @return speed in meters per second.
	 */
	public static float parseNmeaSpeed(String speed, String metric) {
		float meterSpeed = 0.0f;
		if (speed != null && metric != null && speed.length()>0 && metric.length()>0) {
			float temp1 = Float.parseFloat(speed)/3.6f;
			if (metric.equals("K")) {
				meterSpeed = temp1;
			} else if (metric.equals("N")) {
				meterSpeed = temp1*1.852f;
			}
		}
		
		return meterSpeed;
	}

	/** Convert time to the Unix timestamp.
	 * @param time
	 * @return timestamp.
	 * @throws Exception
	 */
	public static long parseNmeaTime(String time) throws Exception {
		long timestamp = 0;
		SimpleDateFormat fmt = new SimpleDateFormat("HHmmss.SSS", Locale.getDefault());
		fmt.setTimeZone(TimeZone.getTimeZone("GMT"));

		if (time != null && time != null) {
			long now = System.currentTimeMillis();
			long today = now - (now %86400000L);
			
			// Sometime we don't have millisecond in the time string, so we have to reformat it. 
			long temp1 = fmt.parse(String.format((Locale)null,"%010.3f", Double.parseDouble(time))).getTime();
			long temp2 = today+temp1;
			
			// If we're around midnight we could have a problem.
			if (temp2 - now > 43200000l) {
				timestamp  = temp2 - 86400000l;
			} else if (now - temp2 > 43200000l){
				timestamp  = temp2 + 86400000l;
			} else {
				timestamp  = temp2;
			}
		}

		return timestamp;
	}

	/** Compute checksum.
	 * @param string
	 * @return checksum.
	 */
	public static byte computeNmeaChecksum(String string) {
		byte checksum = 0;
		for (char c : string.toCharArray()){
			checksum ^= (byte)c;			
		}
		
		return checksum;
	}
}
