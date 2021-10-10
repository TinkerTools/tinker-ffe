/*
 * <p>Title: Force Field Explorer</p>
 * <p>Description: Force Field Explorer Molecular Modeling Program</p>
 * <p>Copyright: Copyright (c) 2004-2021 Jay William Ponder</p>
 * <p>Institution: Jay Ponder Lab, Washington University in Saint Louis</p>
 * @author Michael J. Schnieders
 * @version 8.10
 */

package ffe.lang;

import java.lang.reflect.Array;

/*
 * The HashCodeUtil class implements utilities to produce hashcode values
 * for Force Field Explorer classes that overide the Equals method.
 */
public final class HashCodeUtil {
	/*
	 * An initial value for a <code>hashCode</code>, to which is added
	 * contributions from fields. Using a non-zero value decreases
	 * collisions of <code>hashCode</code> values.
	 */
	public static final int ATOMSEED = 1023;

	public static final int DATANODESEED = 2024;

	public static final int RESIDUESEED = 3025;

	public static final int POLYMERSEED = 4026;

	public static final int KEYWORDSEED = 5027;

	public static final int BONDTERMSEED = 6028;

	public static final int PARALLELMSMSEED = 7029;

	// / PRIVATE ///
	private static final int fODD_PRIME_NUMBER = 37;

	private static int firstTerm(int aSeed) {
		return fODD_PRIME_NUMBER * aSeed;
	}

	/*
	 * booleans.
	 */
	public static int hash(int aSeed, boolean aBoolean) {
		return firstTerm(aSeed) + (aBoolean ? 1 : 0);
	}

	/*
	 * chars.
	 */
	public static int hash(int aSeed, char aChar) {
		return firstTerm(aSeed) + (int) aChar;
	}

	/*
	 * doubles.
	 */
	public static int hash(int aSeed, double aDouble) {
		return hash(aSeed, Double.doubleToLongBits(aDouble));
	}

	/*
	 * floats.
	 */
	public static int hash(int aSeed, float aFloat) {
		return hash(aSeed, Float.floatToIntBits(aFloat));
	}

	/*
	 * ints.
	 */
	public static int hash(int aSeed, int aInt) {
		/*
		 * Implementation Note Note that byte and short are handled
		 * by this method, through implicit conversion.
		 */
		return firstTerm(aSeed) + aInt;
	}

	/*
	 * longs.
	 */
	public static int hash(int aSeed, long aLong) {
		return firstTerm(aSeed) + (int) (aLong ^ (aLong >>> 32));
	}

	/*
	 * <code>aObject</code> is a possibly-null object field,
	 * and possibly an array.
	 * 
	 * If <code>aObject</code> is an array, then each element
	 * may be a primitive or a possibly-null object.
	 */
	public static int hash(int aSeed, Object aObject) {
		int result = aSeed;
		if (aObject == null) {
			result = hash(result, 0);
		} else if (!isArray(aObject)) {
			result = hash(result, aObject.hashCode());
		} else {
			int length = Array.getLength(aObject);
			for (int idx = 0; idx < length; ++idx) {
				Object item = Array.get(aObject, idx);
				result = hash(result, item);
			}
		}
		return result;
	}

	private static boolean isArray(Object aObject) {
		return aObject.getClass().isArray();
	}
}
