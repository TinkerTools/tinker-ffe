/*
 * <p>Title: Force Field Explorer</p>
 * <p>Description: Force Field Explorer Molecular Modeling Program</p>
 * <p>Copyright: Copyright (c) 2004-2019 Jay William Ponder</p>
 * <p>Institution: Jay Ponder Lab, Washington University in Saint Louis</p>
 * @author Michael J. Schnieders
 * @version 8.7
 */

package ffe.mm;

/*
 * The PiTorsionType class defines a Pi-Torsion energy term.
 */
public final class PiTorsionType extends BaseType {
	/*
	 * Atom classes that form this Pi-Torsion.
	 */
	public final int atomClasses[];

	/*
	 * Force constant.
	 */
	public final double forceConstant;

	/*
	 * PiTorsionType Constructor.
	 * 
	 * @param atomClasses
	 *            int[]
	 * @param forceConstant
	 *            double
	 */
	public PiTorsionType(int atomClasses[], double forceConstant) {
		super(ForceField.ForceFieldType.PITORS, new String(atomClasses[0] + " "
				+ atomClasses[1]));
		this.atomClasses = atomClasses;
		this.forceConstant = forceConstant;
	}

	/*
	 * Nicely formatted Pi-Torsion type.
	 * 
	 * @return String
	 */
	public String toString() {
		return String.format("pitors  %5d  %5d  %4.2f", atomClasses[0],
				atomClasses[1], forceConstant);
	}

}
