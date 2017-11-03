package companion.support.v8.util;

import java.util.Random;

/**
 * Utility class for randomization.
 * 
 * @author Vitor Ribeiro
 *
 */
public class RandomUtils {
	
	/** This prevents the class from being instantiated. 
	 */
	private RandomUtils() {
	}
	
	/**
	 * Returns a pseudo-random number between min and max, inclusive.
	 * The difference between min and max can be at most
	 * <code>Integer.MAX_VALUE - 1</code>.
	 *
	 * @param min minimum value
	 * @param max maximum value, must be greater than min.
	 * @return integer between min and max, inclusive.
	 * @see java.util.Random#nextInt(int)
	 */
	public static int nextInt(int min, int max) {
		
		if (min>max) {
			return Integer.MAX_VALUE;
		}

	    // nextInt is normally exclusive of the top value,
	    // so add 1 to make it inclusive
	    return new Random().nextInt((max - min) + 1) + min;
	}
}
