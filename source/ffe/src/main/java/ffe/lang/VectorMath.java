/*
 * <p>Title: Force Field Explorer</p>
 * <p>Description: Force Field Explorer Molecular Modeling Program</p>
 * <p>Copyright: Copyright (c) 2004-2019 Jay William Ponder</p>
 * <p>Institution: Jay Ponder Lab, Washington University in Saint Louis</p>
 * @author Michael J. Schnieders
 * @version 8.7
 */

package ffe.lang;

import java.util.logging.Logger;

/*
 * The VectorMath class is a simple math library that operates on
 * 3-coordinate double and float arrays. The design objectives are
 * speed and no memory consumption.
 */
public final class VectorMath {
	
	private static Logger logger = Logger.getLogger("ffe");
	
	private static final float fa[] = new float[3];

	private static final float fb[] = new float[3];

	private static final float fc[] = new float[3];

	private static final float fd[] = new float[3];

	private static final float[] fba = new float[3];

	private static final float[] fcb = new float[3];

	private static final float[] fdc = new float[3];

	private static final float[] ft = new float[3];

	private static final float[] fu = new float[3];

	private static final float[] ftu = new float[3];

	private static final double da[] = new double[3];

	private static final double db[] = new double[3];

	private static final double dc[] = new double[3];

	private static final double dd[] = new double[3];

	private static final double[] dba = new double[3];

	private static final double[] dcb = new double[3];

	private static final double[] ddc = new double[3];

	private static final double[] dt = new double[3];

	private static final double[] du = new double[3];

	private static final double[] dtu = new double[3];

	public static double angle(double[] i, double[] j) {
		double x;
		norm(i, da);
		norm(j, db);
		x = dot(da, db);
		if (Math.abs(x) > 1) {
			if (x > 0) {
				x = 1;
			} else {
				x = -1;
			}
		}
		return Math.acos(x);
	}

	public static float angle(float[] i, float[] j) {
		float x;
		norm(i, fa);
		norm(j, fb);
		x = dot(fa, fb);
		if (Math.abs(x) > 1) {
			Logger.getLogger("ffe").warning("angle: abs(dot) > 1 " + x);
			if (x > 0) {
				x = 1;
			} else {
				x = -1;
			}
		}
		return (float) Math.acos(x);
	}

	public static long binomial(long n, long k) {
		return factorial(n) / (factorial(n - k) * factorial(k));
	}

	/*
	 * Finds the angle formed by three atoms
	 * 
	 * @param i
	 *            Atom position vector
	 * @param j
	 *            Atom position vector (central atom)
	 * @param k
	 *            Atom position vector
	 * @return The angle in the range [ -pi, pi ]
	 */
	public static double bondAngle(double[] i, double[] j, double[] k) {
		double x;
		diff(i, j, da);
		diff(k, j, db);
		norm(da, dc);
		norm(db, dd);
		x = dot(dc, dd);
		if (Math.abs(x) > 1) {
			if (x > 0) {
				x = 1;
			} else {
				x = -1;
			}
		}
		return Math.acos(x);
	}

	/*
	 * Finds the angle formed by three atoms
	 * 
	 * @param i
	 *            Atom position vector
	 * @param j
	 *            Atom position vector (central atom)
	 * @param k
	 *            Atom position vector
	 * @return The angle in the range [ -pi, pi ]
	 */
	public static float bondAngle(float[] i, float[] j, float[] k) {
		float x;
		diff(i, j, fa);
		diff(k, j, fb);
		norm(fa, fc);
		norm(fb, fd);
		x = dot(fc, fd);
		if (Math.abs(x) > 1) {
			Logger.getLogger("ffe").warning("bondAngle: abs(dot) > 1 " + x);
			if (x > 0) {
				x = 1;
			} else {
				x = -1;
			}
		}
		return (float) Math.acos((double) x);
	}

	/*
	 * Finds the cross-product between two vectors
	 * 
	 * @param a
	 *            First vector
	 * @param b
	 *            Second vector
	 * @param ret
	 *            The cross-product a x b
	 */
	public static void cross(double[] a, double[] b, double ret[]) {
		ret[0] = a[1] * b[2] - a[2] * b[1];
		ret[1] = a[2] * b[0] - a[0] * b[2];
		ret[2] = a[0] * b[1] - a[1] * b[0];
	}

	/*
	 * Finds the cross-product between two vectors
	 * 
	 * @param a
	 *            First vector
	 * @param b
	 *            Second vector
	 * @param ret
	 *            The cross-product a x b
	 */
	public static void cross(float[] a, float[] b, float ret[]) {
		ret[0] = a[1] * b[2] - a[2] * b[1];
		ret[1] = a[2] * b[0] - a[0] * b[2];
		ret[2] = a[0] * b[1] - a[1] * b[0];
	}

	/*
	 * Finds the difference between two vectors
	 * 
	 * @param a
	 *            First vector
	 * @param b
	 *            Second vector
	 * @param ret
	 *            Return Values
	 */
	public static void diff(double[] a, double[] b, double[] ret) {
		ret[0] = a[0] - b[0];
		ret[1] = a[1] - b[1];
		ret[2] = a[2] - b[2];
	}

	/*
	 * Finds the difference between two vectors
	 * 
	 * @param a
	 *            First vector
	 * @param b
	 *            Second vector
	 * @param ret
	 *            Return Values
	 */
	public static void diff(float[] a, float[] b, float[] ret) {
		ret[0] = a[0] - b[0];
		ret[1] = a[1] - b[1];
		ret[2] = a[2] - b[2];
	}

	/*
	 * Finds the dihedral angle formed between 4 atoms
	 * 
	 * @param a
	 *            Atom position vector
	 * @param b
	 *            Atom position vector
	 * @param c
	 *            Atom position vector
	 * @param d
	 *            Atom position vector
	 * @return The dihedral angle in the range [ -pi, pi ]
	 */
	public static double dihedralAngle(double[] a, double[] b, double[] c,
			double[] d) {
		diff(b, a, dba);
		diff(c, b, dcb);
		diff(d, c, ddc);
		cross(dba, dcb, dt);
		cross(dcb, ddc, du);
		cross(dt, du, dtu);
		double rt = dot(dt, dt);
		double ru = dot(du, du);
		double rtu = Math.sqrt(rt * ru);
		if (rtu != 0.0) {
			double rcb = r(dcb);
			double cosine = dot(dt, du) / rtu;
			double sine = dot(dcb, dtu) / (rcb * rtu);
			cosine = Math.min(1.0, Math.max(-1.0, cosine));
			double angle = Math.acos(cosine);
			if (sine < 0.0) {
				angle = -angle;
			}
			return angle;
		}
		return 0;
	}

	/*
	 * Finds the dihedral angle formed between 4 atoms
	 * 
	 * @param a
	 *            Atom position vector
	 * @param b
	 *            Atom position vector
	 * @param c
	 *            Atom position vector
	 * @param d
	 *            Atom position vector
	 * @return The dihedral angle in the range [ -pi, pi ]
	 */
	public static float dihedralAngle(float[] a, float[] b, float[] c, float[] d) {
		diff(b, a, fba);
		diff(c, b, fcb);
		diff(d, c, fdc);
		cross(fba, fcb, ft);
		cross(fcb, fdc, fu);
		cross(ft, fu, ftu);
		float rt = dot(ft, ft);
		float ru = dot(fu, fu);
		float rtu = (float) Math.sqrt(rt * ru);
		if (rtu != 0.0) {
			float rcb = r(fcb);
			float cosine = dot(ft, fu) / rtu;
			float sine = dot(fcb, ftu) / (rcb * rtu);
			cosine = Math.min(1.0f, Math.max(-1.0f, cosine));
			float angle = (float) Math.acos((double) cosine);
			if (sine < 0.0) {
				angle = -angle;
			}
			return angle;
		}
		return 0;
	}

	/*
	 * Finds the distance between two vectors
	 * 
	 * @param a
	 *            First vector
	 * @param b
	 *            Second vector
	 * @return The distance between vectors a and b
	 */
	public static double dist(double[] a, double[] b) {
		double x;
		x = (a[0] - b[0]) * (a[0] - b[0]);
		x += (a[1] - b[1]) * (a[1] - b[1]);
		x += (a[2] - b[2]) * (a[2] - b[2]);
		return Math.sqrt(x);
	}

	/*
	 * Finds the distance between two vectors
	 * 
	 * @param a
	 *            First vector
	 * @param b
	 *            Second vector
	 * @return The distance between vectors a and b
	 */
	public static float dist(float[] a, float[] b) {
		float x;
		x = (a[0] - b[0]) * (a[0] - b[0]);
		x += (a[1] - b[1]) * (a[1] - b[1]);
		x += (a[2] - b[2]) * (a[2] - b[2]);
		return (float) Math.sqrt((double) x);
	}

	/*
	 * Finds the dot product between two vectors
	 * 
	 * @param a
	 *            First vector
	 * @param b
	 *            Second vector
	 * @return The dot product of a and b
	 */
	public static double dot(double[] a, double[] b) {
		return ((a[0] * b[0]) + (a[1] * b[1]) + (a[2] * b[2]));
	}

	/*
	 * Finds the dot product between two vectors
	 * 
	 * @param a
	 *            First vector
	 * @param b
	 *            Second vector
	 * @return The dot product of a and b
	 */
	public static float dot(float[] a, float[] b) {
		return ((a[0] * b[0]) + (a[1] * b[1]) + (a[2] * b[2]));
	}

	/*
	 * Returns n!! Precondition: n >= -1 Returning 1 for -1 input
	 * is analogous to Maple behavior.
	 * 
	 * @param n
	 *            long
	 * @return long
	 */
	public static long doublefactorial(long n) {
		if (n < -1) {
			throw new RuntimeException("Underflow error in doublefactorial");
		} else if (n == 0 || n == 1 || n == -1) {
			return 1;
		} else {
			return n * doublefactorial(n - 2);
		}
	}

	/*
	 * Returns n! Precondition: n >= 0 and n <= 20 Max long =
	 * 9223372036854775807 20! = 2432902008176640000 is ok. 21! returns
	 * an overflow: -4249290049419214848
	 * 
	 * @param n
	 *            long
	 * @return long
	 */
	public static long factorial(long n) {
		if (n < 0) {
			throw new RuntimeException("Underflow error in factorial");
		} else if (n > 20) {
			throw new RuntimeException("Overflow error in factorial");
		} else if (n == 0) {
			return 1;
		} else {
			return n * factorial(n - 1);
		}
	}

	/*
	 * Normalizes a vector
	 * 
	 * @param n
	 *            A vector to be normalized.
	 * @param ret
	 *            The normalized vector.
	 */
	public static void norm(double[] n, double[] ret) {
		double length;
		length = r(n);
		ret[0] = n[0] / length;
		ret[1] = n[1] / length;
		ret[2] = n[2] / length;
	}

	/*
	 * Normalizes a vector
	 * 
	 * @param n
	 *            A vector to be normalized.
	 * @param ret
	 *            The normalized vector.
	 */
	public static void norm(float[] n, float[] ret) {
		float length;
		length = r(n);
		ret[0] = n[0] / length;
		ret[1] = n[1] / length;
		ret[2] = n[2] / length;
	}

	public static void printVector(double v[]) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < v.length; i++) {
			sb.append(String.format("%g ", v[i]));
		}
		logger.info("Vector ( " + sb.toString() + ")");
	}

	/*
	 * Finds the length of a vector
	 * 
	 * @param d
	 *            A vector to find the length of.
	 * @return Length of vector d.
	 */
	public static double r(double[] d) {
		return Math.sqrt((d[0] * d[0] + d[1] * d[1] + d[2] * d[2]));
	}

	/*
	 * Finds the length of a vector
	 * 
	 * @param d
	 *            A vector to find the length of.
	 * @return Length of vector d.
	 */
	public static float r(float[] d) {
		return (float) Math.sqrt((double) (d[0] * d[0] + d[1] * d[1] + d[2]
				* d[2]));
	}

	/*
	 * Scales a vector
	 * 
	 * @param n
	 *            A vector to be scaled
	 * @param a
	 *            A scaler value
	 * @param ret
	 *            The scaled vector
	 */
	public static void scalar(double[] n, double a, double[] ret) {
		ret[0] = n[0] * a;
		ret[1] = n[1] * a;
		ret[2] = n[2] * a;
	}

	/*
	 * Scales a vector
	 * 
	 * @param n
	 *            A vector to be scaled
	 * @param a
	 *            A scaler value
	 * @param ret
	 *            The scaled Vector
	 */
	public static void scalar(float[] n, float a, float[] ret) {
		ret[0] = n[0] * a;
		ret[1] = n[1] * a;
		ret[2] = n[2] * a;
	}

	public static void sum(double[] a, double[] b, double[] ret) {
		ret[0] = a[0] + b[0];
		ret[1] = a[1] + b[1];
		ret[2] = a[2] + b[2];
	}

	public static void sum(float[] a, float[] b, float[] ret) {
		ret[0] = a[0] + b[0];
		ret[1] = a[1] + b[1];
		ret[2] = a[2] + b[2];
	}
}
