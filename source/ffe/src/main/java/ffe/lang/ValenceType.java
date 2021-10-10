/*
 * <p>Title: Force Field Explorer</p>
 * <p>Description: Force Field Explorer Molecular Modeling Program</p>
 * <p>Copyright: Copyright (c) 2004-2021 Jay William Ponder</p>
 * <p>Institution: Jay Ponder Lab, Washington University in Saint Louis</p>
 * @author Michael J. Schnieders
 * @version 8.10
 */

package ffe.lang;

/*
 * The ValenceType class stores constants for one bonded
 * molecular mechanics term.
 */
public class ValenceType {
	/*
	 * Concatenation of atom types for the atoms referred to in the term
	 */
	public final String key;

	/*
	 * Equilibrium value for the term. (ie ~2 angstroms for a bond length)
	 */
	public final double eq;

	/*
	 * Force Constant for the term
	 */
	public final double fc, fc2;

	/*
	 * Phase value for Dihedral and Improper Terms
	 */
	public final double phase, phase2;

	/*
	 * Periodicity value for Dihedral and Improper Terms
	 */
	public final int periodicity, periodicity2;

	/*
	 * Constructor for Bond and Angle Terms
	 */
	public ValenceType(String k, double f, double e) {
		key = new String(k);
		fc = f;
		eq = e;
		fc2 = phase = phase2 = 0;
		periodicity = periodicity2 = 0;
	}

	/*
	 * Construtor for Dihedral and Improper Terms with one set of values
	 */
	public ValenceType(String k, double f, double ph, int pe) {
		key = new String(k);
		fc = f;
		phase = ph;
		periodicity = pe;
		fc2 = phase2 = eq = 0;
		periodicity2 = 0;
	}

	/*
	 * Constructor for Dihedral and Improper Terms with two sets of values
	 */
	public ValenceType(String k, double f, double ph, int pe, double f2,
			double ph2, int pe2) {
		key = new String(k);
		fc = f;
		phase = ph;
		periodicity = pe;
		fc2 = f2;
		phase2 = ph2;
		periodicity2 = pe2;
		eq = 0;
	}
}
