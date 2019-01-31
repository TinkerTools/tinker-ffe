/*
 * <p>Title: Force Field Explorer</p>
 * <p>Description: Force Field Explorer Molecular Modeling Program</p>
 * <p>Copyright: Copyright (c) 2004-2019 Jay William Ponder</p>
 * <p>Institution: Jay Ponder Lab, Washington University in Saint Louis</p>
 * @author Michael J. Schnieders
 * @version 8.5
 */

package ffe.mm;

/*
 * The BondType class defines one harmonic bond stretch energy term.
 */
public final class BondType extends BaseType {
	/*
	 * Atom classes that form this bond stretch.
	 */
	public final int atomClasses[];

	/*
	 * Force constant (Kcal/mol).
	 */
	public final double forceConstant;

	/*
	 * Equilibrium separation (Angstroms).
	 */
	public final double distance;

	/*
	 * BondType constructor.
	 * 
	 * @param atomClasses
	 *            int[]
	 * @param forceConstant
	 *            double
	 * @param distance
	 *            double
	 */
	public BondType(int atomClasses[], double forceConstant, double distance) {
		super(ForceField.ForceFieldType.BOND, new String(atomClasses[0] + " "
				+ atomClasses[1]));
		this.atomClasses = atomClasses;
		this.forceConstant = forceConstant;
		this.distance = distance;
	}

	/*
	 * Nicely formatted bond stretch string.
	 * 
	 * @return String
	 */
	public String toString() {
		return String.format("bond  %5d  %5d  %6.1f  %7.4f", atomClasses[0],
				atomClasses[1], forceConstant, distance);
	}

}
