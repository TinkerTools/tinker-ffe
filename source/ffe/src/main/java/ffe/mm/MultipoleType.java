/*
 * <p>Title: Force Field Explorer</p>
 * <p>Description: Force Field Explorer Molecular Modeling Program</p>
 * <p>Copyright: Copyright (c) 2004-2020 Jay William Ponder</p>
 * <p>Institution: Jay Ponder Lab, Washington University in Saint Louis</p>
 * @author Michael J. Schnieders
 * @version 8.8
 */

package ffe.mm;

/*
 * The MultipoleType class defines a multipole in its local frame.
 */
public final class MultipoleType extends BaseType {
	/*
	 * The local multipole frame is defined by the Z-then-X or Bisector
	 * convention.
	 */
	public enum MultipoleFrameDefinition {
		ZTHENX, BISECTOR
	}

	/*
	 * Conversion from electron-Angstroms to Debyes
	 */
	public static final double DEBYE = 4.8033324e0;

	/*
	 * Conversion from electron-Angstroms^2 to Buckinghams
	 */
	public static final double BUCKINGHAM = DEBYE * DEBYE;

	/*
	 * Conversion from Bohr to Angstroms
	 */
	public static final double BOHR = 0.5291772083e0;

	/*
	 * Conversion from Bohr^2 to Angstroms^2
	 */
	public static final double BOHR2 = BOHR * BOHR;

	/*
	 * Partial atomic charge (e).
	 */
	public final double charge;

	/*
	 * Atomic dipole. 1 x 3 (e Angstroms).
	 */
	public final double dipole[];

	/*
	 * Atomic quadrupole. 3 x 3 (e Angstroms^2).
	 */
	public final double quadrupole[][];

	/*
	 * Local frame definition method.
	 */
	public final MultipoleFrameDefinition frameDefinition;

	/*
	 * Atom types that define the local frame of this multipole.
	 */
	public final int[] frameAtomTypes;

	/*
	 * Multipole Constructor. This assumes the dipole and quadrupole are
	 * in units of Bohr, and are converted to electron-Angstroms and
	 * electron-Angstroms^2, respectively, before the constructor returns.
	 * 
	 * @param charge
	 *            double
	 * @param dipole
	 *            double[]
	 * @param quadrupole
	 *            double[]
	 * @param multipoleFrameTypes
	 *            int[]
	 */
	public MultipoleType(double charge, double dipole[], double quadrupole[][],
			int[] multipoleFrameTypes, MultipoleFrameDefinition frameDefinition) {
		super(ForceField.ForceFieldType.MULTIPOLE, multipoleFrameTypes);
		this.charge = charge;
		this.dipole = dipole;
		this.quadrupole = quadrupole;
		this.frameAtomTypes = multipoleFrameTypes;
		this.frameDefinition = frameDefinition;
		initMultipole();
	}

	private void initMultipole() {
		// Check symmetry.
		double check = Math.abs(quadrupole[0][1] - quadrupole[1][0]);
		if (check > 1.0e-6) {
			logger.warning("Multipole component Qxy != Qyx");
			print();
		}
		check = Math.abs(quadrupole[0][2] - quadrupole[2][0]);
		if (check > 1.0e-6) {
			logger.warning("Multipole component Qxz != Qzx");
			print();
		}
		check = Math.abs(quadrupole[1][2] - quadrupole[2][1]);
		if (check > 1.0e-6) {
			logger.warning("Multipole component Qyz != Qzy");
			print();
		}
		// Convert to electron-Angstroms
		for (int i = 0; i < 3; i++) {
			dipole[i] *= BOHR;
		}
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				quadrupole[i][j] *= BOHR * BOHR;
			}
		}
		// Warn if the multipole is not traceless.
		double sum = quadrupole[0][0] + quadrupole[1][1] + quadrupole[2][2];
		if (Math.abs(sum) > 1.0e-5) {
			String warn = String
					.format("Multipole is not traceless: %7.5f", sum);
			logger.warning(warn + "\n" + toBohrString());
		}

	}

	/*
	 * Print this multipole to System.out.
	 */
	public void print() {
		logger.info(toBohrString());
	}

	/*
	 * Nicely formatted multipole string. Dipole and qaudrupole are
	 * in electron-Bohrs and electron-Bohrs^2, respectively.
	 * 
	 * @return String
	 */
	public String toBohrString() {
		StringBuffer multipoleBuffer = new StringBuffer("multipole");
		for (int i : frameAtomTypes) {
			multipoleBuffer.append(String.format("  %5d", i));
		}
		if (frameAtomTypes.length == 3) {
			multipoleBuffer.append("       ");
		}
		multipoleBuffer.append(String.format("  % 7.5f\n"
				+ "%11$s % 7.5f % 7.5f % 7.5f\n" + "%11$s % 7.5f\n"
				+ "%11$s % 7.5f % 7.5f\n" + "%11$s % 7.5f % 7.5f % 7.5f",
				charge, dipole[0] / BOHR, dipole[1] / BOHR, dipole[2] / BOHR,
				quadrupole[0][0] / BOHR2, quadrupole[1][0] / BOHR2,
				quadrupole[1][1] / BOHR2, quadrupole[2][0] / BOHR2,
				quadrupole[2][1] / BOHR2, quadrupole[2][2] / BOHR2,
				"                                      "));
		return multipoleBuffer.toString();
	}

	/*
	 * Nicely formatted multipole string. Dipole and qaudrupole are
	 * in units of Debye and Buckinghams, respectively.
	 * 
	 * @return String
	 */
	public String toDebyeString() {
		StringBuffer multipoleBuffer = new StringBuffer("multipole");
		for (int i : frameAtomTypes) {
			multipoleBuffer.append(String.format("  %5d", i));
		}
		if (frameAtomTypes.length == 3) {
			multipoleBuffer.append("       ");
		}
		multipoleBuffer.append(String.format("  % 7.5f\n"
				+ "%11$s % 7.5f % 7.5f % 7.5f\n" + "%11$s % 7.5f\n"
				+ "%11$s % 7.5f % 7.5f\n" + "%11$s % 7.5f % 7.5f % 7.5f",
				charge, dipole[0] * DEBYE, dipole[1] * DEBYE,
				dipole[2] * DEBYE, quadrupole[0][0] * BUCKINGHAM,
				quadrupole[1][0] * BUCKINGHAM, quadrupole[1][1] * BUCKINGHAM,
				quadrupole[2][0] * BUCKINGHAM, quadrupole[2][1] * BUCKINGHAM,
				quadrupole[2][2] * BUCKINGHAM,
				"                                      "));
		return multipoleBuffer.toString();
	}

	public String toString() {
		return toBohrString();
	}
}
