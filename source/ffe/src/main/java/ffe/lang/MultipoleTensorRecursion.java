/*
 * <p>Title: Force Field Explorer</p>
 * <p>Description: Force Field Explorer Molecular Modeling Program</p>
 * <p>Copyright: Copyright (c) 2004-2019 Jay William Ponder</p>
 * <p>Institution: Jay Ponder Lab, Washington University in Saint Louis</p>
 * @author Michael J. Schnieders
 * @version 8.5
 */

package ffe.lang;

import java.util.logging.Level;
import java.util.logging.Logger;

public class MultipoleTensorRecursion {
	private static final Logger logger = Logger.getLogger("ffe");

	/*
	 * Store the auxillary tensor memory to avoid memory consumption.
	 */
	private static double T000[] = null;

	public static void main(String[] args) throws Exception {
		// Sum of the tensors for order = 17 is 4.024861808459854E9
		int order = 5;
		if (args.length > 0) {
			order = Integer.parseInt(args[0]);
		}
		int maxOrder = 5;
		if (args.length > 1) {
			maxOrder = Integer.parseInt(args[1]);
		}
		int loops = 2;
		if (args.length > 2) {
			loops = Integer.parseInt(args[2]);
		}
		int n = 10000;
		if (args.length > 3) {
			n = Integer.parseInt(args[3]);
		}
		final double[] r = { 1.1, 1.2, 1.3 };
		for (; order <= maxOrder; order++) {
			MultipoleTensorRecursion multipoleTensor = new MultipoleTensorRecursion(
					order);
			MultipoleTensorRecursion.logger.setLevel(Level.FINE);
			for (int l = 1; l <= loops; l++) {
				int size = (int) VectorMath.binomial(order + 3, 3);
				final double[] tensor = new double[size];
				// Number of sites.
				int niter = n * (int) Math.log(n);
				// niter = 1;
				long start = System.currentTimeMillis();
				for (int i = 0; i < niter; i++) {
					multipoleTensor.tensorRecursion(r, tensor);
				}
				long done = System.currentTimeMillis() - start;
				// RECORD - Order 5, n = 100,000, n * ln(n) interactions
				// (1,100,000): 1.08 (sec)
				// RECORD - Order 5, n = 1,000,000, n * ln(n) interactions
				// (13,000,000): 12.91 (sec)
				logger.info("Tensor Recursion - Order           : "
						+ order + " n " + niter + " time " + done + " size "
						+ size);
				/*
				 * start = System.currentTimeMillis(); for (int i = 0; i <
				 * niter; i++) { multipoleTensor.noStorageTensorRecursion(r,
				 * tensor); } done = System.currentTimeMillis() - start;
				 * System.out.println("No Storage Tensor Recursion - Order: " +
				 * order + " n " + niter + " time " + done + " size " + size);
				 */
			}
		}
	}

	/*
	 * The index is based on the idea of filling tetrahedron: 1/r has an index
	 * of 0, derivatives of x are first -> indeces from 1..o for d/dx..do/dox)
	 * derivatives of x & y are second -> base triangle of size (o+1)(o+2)/2
	 * derivatives of x & y & z are last -> total size (o+1)*(o+2)*(o+3)/6
	 * 
	 * This function is useful to set up masking constants. static int Tlmn =
	 * tensorIndex(l,m,n,order) For example the (d/dy)^2 (1/R) storage location:
	 * static int T020 = tensorIndex(0,2,0,order)
	 * 
	 * @param dx
	 *            int The number of d/dx operations.
	 * @param dy
	 *            int The number of d/dy operations.
	 * @param dz
	 *            int The number of d/dz operations.
	 * @param order
	 *            int The maximum tensor order (0 <= dx + dy + dz <= order).
	 * @return int in the range (0..binomial(order + 3, 3) - 1)
	 */
	public static int tensorIndex(int dx, int dy, int dz, int order) {
		int size = (order + 1) * (order + 2) * (order + 3) / 6;
		// We only get to the top of the tetrahedron if dz = order,
		// otherwise subtract off the top, including the level of the requested
		// tensor index.
		int top = order + 1 - dz;
		top = top * (top + 1) * (top + 2) / 6;
		int zindex = size - top;
		// Given the "dz level", dy can range from 0..order - dz)
		// To get to the row for a specific value of dy,
		// dy*(order + 1) - y*(y-1)/2 indeces are skipped.
		// This is an operation that looks like the area of rectangle, minus
		// the area of an empty triangle.
		int yindex = dy * (order + 1) - dy * (dy - 1) / 2;
		// Given the dz level and dy row, dx can range from (0..order - dz - dy)
		// The dx index is just walking down the dy row for "dx" steps.
		return dx + yindex + zindex;
	}

	/*
	 * This routine implements the recurrence relations for computation of of
	 * any Cartesion multipole tensor in ~O(L^8) time, where L is the total
	 * order l + m + n, given the auxillary elements T0000.
	 * 
	 * It implements the recursion relationships from the reference below in
	 * brute force fashion, without saving intermediate values. This is useful
	 * for finding a single tensor, rather than all binomial(L + 3, 3).
	 * 
	 * The specific recursion equations (41-43) and set of auxillary tensor
	 * elements from equation (40) can be found in:
	 * 
	 * Matt Challacombe, Eric Schwegler and Jan Almlof Modern developments in
	 * Hartree-Fock theory: Fast methods for computing the Coulomb matrix.
	 * Computational Chemistry: Review of Current Trends pp. 53-107, Ed. J.
	 * Leczszynski, World Scientifc, 1996.
	 * 
	 * @param l
	 *            int The number of (d/dx) operations.
	 * @param m
	 *            int The number of (d/dy) operations.
	 * @param n
	 *            int The number of (d/dz) operations.
	 * @param j
	 *            int j = 0 is the Tlmn tensor, j > 0 is an intermediate.
	 * @param r
	 *            double[] The {x,y,z} coordinates.
	 * @param T000
	 *            double[] Initial auxillary tensor elements from Eq. (40).
	 * @return double The requested Tensor element (intermediate if j > 0).
	 */
	private static double Tlmnj(final int l, final int m, final int n,
			final int j, final double[] r, final double[] T000) {
		if (m == 0 && n == 0) {
			if (l > 1) {
				return r[0] * Tlmnj(l - 1, 0, 0, j + 1, r, T000) + (l - 1)
						* Tlmnj(l - 2, 0, 0, j + 1, r, T000);
			} else if (l == 1) { // l == 1, d/dx is done.
				return r[0] * Tlmnj(0, 0, 0, j + 1, r, T000);
			} else { // l = m = n = 0. Recursion is done.
				return T000[j];
			}
		} else if (n == 0) { // m >= 1
			if (m > 1) {
				return r[1] * Tlmnj(l, m - 1, 0, j + 1, r, T000) + (m - 1)
						* Tlmnj(l, m - 2, 0, j + 1, r, T000);
			}
			return r[1] * Tlmnj(l, 0, 0, j + 1, r, T000);
		} else { // n >= 1
			if (n > 1) {
				return r[2] * Tlmnj(l, m, n - 1, j + 1, r, T000) + (n - 1)
						* Tlmnj(l, m, n - 2, j + 1, r, T000);
			}
			return r[2] * Tlmnj(l, m, 0, j + 1, r, T000);
		}
	}

	/*
	 * Store the work array to avoid memory consumption. Note that rather
	 * than use an array for intermediate values, a 4D matrix was tried.
	 * It was approximately 50% slower than the linear work array.
	 */
	private final double work[];

	private final int order;

	private final double t000j_Constants[];

	private final int o1;

	private final int il;

	private final int im;

	private final int in;

	public MultipoleTensorRecursion(int order) {
		if (order == 0) {
			logger.warning("The tensor recursion requires order > 0");
			work = null;
			t000j_Constants = null;
			o1 = 0;
			il = 0;
			im = 0;
			in = 0;
			this.order = order;
			return;
		}
		o1 = order + 1;
		il = o1;
		im = il * o1;
		in = im * o1;
		work = new double[in * o1];
		t000j_Constants = new double[o1];
		for (int j = 0; j <= order; j++) {
			// Math.pow(-1.0, j) returns positive for all j, with -1.0 as the
			// arguement rather than -1. This is a bug.
			t000j_Constants[j] = Math.pow(-1, j)
					* VectorMath.doublefactorial(2 * j - 1);
		}
		this.order = order;
	}

	/*
	 * This method is a driver to collect elements of the cartesion multipole
	 * tensor given the recursion relationships implmented by the method
	 * "Tlmnj", which can be called directly to get a single tensor element.
	 * 
	 * It does not store intermediate values of the recursion, causing it to
	 * scale O(order^8). For order = 5, this approach is a factor of 10X
	 * slower than tensorRecursion.
	 * 
	 * @param r
	 *            double[] vector between two sites.
	 * @param tensor
	 *            double[] length must be at least binomial(order + 3, 3).
	 */
	public void noStorageTensorRecursion(double r[], double tensor[]) {
		// Check for bad input.
		if (tensor == null) {
			return;
		}
		// Check for not enough tensor memory.
		// long size = VectorMath.binomial(3 + order, 3);
		// if (tensor.length < size) {
		// return;
		// }

		if (T000 == null) {
			T000 = new double[order + 1];
		}
		// R.
		double rr = 1.0 / VectorMath.r(r);
		double rr2 = rr * rr;
		// Create the auxillary tensors elements (equation 40).
		for (int j = 0; j <= order; j++) {
			T000[j] = t000j_Constants[j] * rr;
			rr = rr * rr2;
		}
		// 1/R.
		tensor[0] = T000[0];
		if (order == 0) {
			return;
		}
		// Start the tensor index rolling.
		int index = 1;
		// Find (d/dx)^l for l = 1..order (m = 0, n = 0)
		for (int l = 1; l <= order; l++) {
			tensor[index++] = Tlmnj(l, 0, 0, 0, r, T000);
		}
		// Find (d/dx)^l * (d/dy)^m for l + m = 1..order (m >= 1, n = 0)
		for (int m = 1; m <= order; m++) {
			int ldone = order - m;
			for (int l = 0; l <= ldone; l++) {
				tensor[index++] = Tlmnj(l, m, 0, 0, r, T000);
			}
		}
		// Find (d/dx)^l * (d/dy)^m * (d/dz)^n for l + m + n = 1..o (n >= 1)
		for (int n = 1; n <= order; n++) {
			int mdone = order - n;
			for (int m = 0; m <= mdone; m++) {
				int ldone = order - m - n;
				for (int l = 0; l <= ldone; l++) {
					tensor[index++] = Tlmnj(l, m, n, 0, r, T000);
				}
			}
		}
		// Log the tensors.
		if (logger.isLoggable(Level.FINEST)) {
			logger.log(Level.FINEST, "No Storage Tensor Recursion (order: "
					+ order + ")");
			logger.log(Level.FINEST, String.format(
					"|r|: %9.6f, {%9.6f,%9.6f,%9.6f}", VectorMath.r(r), r[0],
					r[1], r[2]));
			double sum = 0.0;
			for (int i = 0; i < tensor.length; i++) {
				sum += tensor[i];
			}
			logger.log(Level.FINEST, String.format(
					"No Storage Tensor Sum: %18.16E", sum));
			System.out.println(String.format("No Storage Tensor Sum: %18.16E",
					sum));
		}
	}

	/*
	 * This function is a driver to collect elements of the cartesion
	 * multipole tensor.
	 * 
	 * Collecting all tensors scales slightly better than O(order^4).
	 * 
	 * For a quadrupole force field, for example, up to order 5 is needed for
	 * energy gradients. The number of terms this requires is binomial(5 + 3, 3)
	 * or 8! / (5! * 3!), which is 56.
	 * 
	 * The packing of the tensor elements is:
	 * 
	 * for order of 1 tensor[0] = 1/|r| tensor[1] = -x/|r|^3 tensor[2] =
	 * -y/|r|^3 tensor[3] = -z/|r|^3
	 * 
	 * for general order (o) Shown is (order = 2) tensor[0] = 1/|r| (d/dx)^l (l =
	 * 1..o) tensor[1] = -x/|r|^3 (index = 1..o) tensor[2] = d/dx (-x/|r|^3)
	 * 
	 * (d/dx)^l * (d/dy)^m tensor[3] = -y/|r|^3) for (m=1,m<=o,m++) tensor[4] =
	 * d/dy (-x/|r|^3) for (l=0,l+m<=o,l++) tensor[5] = d/dy (-y/|r|^3)
	 * 
	 * (d/dx)^l * (d/dy)^m * (d/dz)^n tensor[6] = -z/|r|^3 for (n=1,n<=o,n++)
	 * tensor[7] = d/dx (-z/|r|^3) for (m=0,n+m<=o,m++) tensor[8] = d/dy
	 * (-z/|r|^3) for (l=0,l+m+n<=o,l++) tensor[9] = d/dz (-z/|r|^3)
	 * 
	 * @param r
	 *            double[] vector between two sites.
	 * @param tensor
	 *            double[] length must be at least binomial(order + 3, 3).
	 */
	public void tensorRecursion(final double r[], final double tensor[]) {
		// 1/R.
		int a;
		final double x = r[0];
		final double y = r[1];
		final double z = r[2];
		int n;
		int m;
		int l;
		final double rr2 = 1.0 / (x * x + y * y + z * z);
		double rr = Math.sqrt(rr2);
		// Create the auxillary tensors elements (equation 40).
		for (int j = 0; j < o1; j++) {
			work[j] = t000j_Constants[j] * rr;
			rr *= rr2;
		}
		tensor[0] = work[0];
		// Start the tensor index rolling.
		int index = 1;
		// Find (d/dx)^l for l = 1..order (m = 0, n = 0)
		// Any (d/dx) term can be formed as
		// Tl00j = x * T(l-1)00(j+1) + (l-1) * T(l-2)00(j+1)
		// All intermediate terms are indexed as l*il + m*im + n*in + j;
		// Store the l=1 tensor T100 (d/dx)
		// Starting the loop at l=2 avoids an if statement.
		double current;
		double previous = work[1];
		tensor[index++] = x * previous;
		for (l = 2; l < o1; l++) {
			// Initial condition for the inner loop is formation of T100(l-1).
			// Starting the inner loop at a=2 avoid an if statement.
			// T100(l-1) = x * T000(l)
			current = x * work[l];
			int iw = il + l - 1;
			work[iw] = current;
			for (a = 1; a < l - 1; a++) {
				// T200(l-2) = x * T100(l-1) + (2 - 1) * T000(l-1)
				// T300(l-3) = x * T200(l-2) + (3 - 1) * T100(l-2)
				// ...
				// T(l-1)001 = x * T(l-2)002 + (l - 2) * T(l-3)002
				current = x * current + a * work[iw - il];
				iw += il - 1;
				work[iw] = current;
			}
			// Store the Tl00 tensor (d/dx)^l
			// Tl00 = x * [[ T(l-1)001 ]] + (l - 1) * T(l-2)001
			tensor[index++] = x * current + (l - 1) * previous;
			previous = current;
		}
		// Find (d/dx)^l * (d/dy)^m for l+m = 1..order (m > 0, n = 0)
		// Any (d/dy) term can be formed as:
		// Tlm0j = y * Tl(m-1)00(j+1) + (m-1) * Tl(m-2)00(j+1)
		for (l = 0; l < order; l++) {
			// Store the m=1 tensor (d/dx)^l *(d/dy)
			// Tl10 = y * Tl001
			previous = work[l * il + 1];
			tensor[index++] = y * previous;
			for (m = 2; m + l < o1; m++) {
				// Tl10(m-1) = y * Tl00m;
				int iw = l * il + m;
				current = y * work[iw];
				iw += im - 1;
				work[iw] = current;
				for (a = 1; a < m - 1; a++) {
					// Tl20(m-2) = Y * Tl10(m-1) + (2 - 1) * T100(m-1)
					// Tl30(m-3) = Y * Tl20(m-2) + (3 - 1) * Tl10(m-2)
					// ...
					// Tl(m-1)01 = Y * Tl(m-2)02 + (m - 2) * T(m-3)02
					current = y * current + a * work[iw - im];
					iw += im - 1;
					work[iw] = current;
				}
				// Store the tensor (d/dx)^l * (d/dy)^m
				// Tlm0 = y * Tl(m-1)01 + (m - 1) * Tl(m-2)01
				tensor[index++] = y * current + (m - 1) * previous;
				previous = current;
			}
		}
		// Find (d/dx)^l * (d/dy)^m * (d/dz)^n for l+m+n = 1..order (n > 0)
		// Any (d/dz) term can be formed as:
		// Tlmnj = z * Tlm(n-1)(j+1) + (n-1) * Tlm(n-2)(j+1)
		for (l = 0; l < order; l++) {
			for (m = 0; m + l < order; m++) {
				// Store the n=1 tensor (d/dx)^l *(d/dy)^m * (d/dz)
				// Tlmn = z * Tlm01
				final int lm = m + l;
				final int lilmim = l * il + m * im;
				previous = work[lilmim + 1];
				tensor[index++] = z * previous;
				for (n = 2; lm + n < o1; n++) {
					// Tlm1(n-1) = z * Tlm0n;
					int iw = lilmim + n;
					current = z * work[iw];
					iw += in - 1;
					work[iw] = current;
					final int n1 = n - 1;
					for (a = 1; a < n1; a++) {
						// Tlm2(n-2) = z * Tlm1(n-1) + (2 - 1) * T1m0(n-1)
						// Tlm3(n-3) = z * Tlm2(n-2) + (3 - 1) * Tlm1(n-2)
						// ...
						// Tlm(n-1)1 = z * Tlm(n-2)2 + (n - 2) * Tlm(n-3)2
						current = z * current + a * work[iw - in];
						iw += in - 1;
						work[iw] = current;
					}
					// Store the tensor (d/dx)^l * (d/dy)^m * (d/dz)^n
					// Tlmn = z * Tlm(n-1)1 n - 1) * Tlm(n-2)1
					tensor[index++] = z * current + n1 * previous;
					previous = current;
				}
			}
		}
		/*
		 * // Log the tensors. if (logger.isLoggable(Level.FINEST)) {
		 * logger.log(Level.FINEST, "Tensor Recursion (order: " + order + ")");
		 * logger.log(Level.FINEST, String.format("|r|: %9.6f,
		 * {%9.6f,%9.6f,%9.6f}", VectorMath.r(r), r[0], r[1], r[2])); double sum =
		 * 0.0; for (int i = 0; i < tensor.length; i++) { sum += tensor[i]; }
		 * logger.log(Level.FINEST, String.format("Tensor Sum: %17.15E", sum));
		 * System.out.println(String.format("Tensor Sum : %18.16E", sum)); }
		 */
	}
}
