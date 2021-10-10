/*
 * <p>Title: Force Field Explorer</p>
 * <p>Description: Force Field Explorer Molecular Modeling Program</p>
 * <p>Copyright: Copyright (c) 2004-2021 Jay William Ponder</p>
 * <p>Institution: Jay Ponder Lab, Washington University in Saint Louis</p>
 * @author Michael J. Schnieders
 * @version 8.10
 */

package ffe.mm;

/*
 * The UreyBradleyType class defines one harmonic UreyBradley cross term.
 */
public final class UreyBradleyType extends BaseType {
	/*
	 * Atom classes that form this Urey-Bradley cross term.
	 */
	public final int atomClasses[];

	/*
	 * Force constant (Kcal/mole/angstroms^2).
	 */
	public final double forceConstant;

	/*
	 * Equilibrium 1-3 separation (Angstroms).
	 */
	public final double distance;

	/*
	 * UreyBradleyType constructor.
	 * 
	 * @param atomClasses
	 *            int[]
	 * @param forceConstant
	 *            double
	 * @param distance
	 *            double
	 */
	public UreyBradleyType(int atomClasses[], double forceConstant,
			double distance) {
		super(ForceField.ForceFieldType.UREYBRAD, new String(atomClasses[0]
				+ " " + atomClasses[1] + " " + atomClasses[2]));
		this.atomClasses = atomClasses;
		this.forceConstant = forceConstant;
		this.distance = distance;
	}

	/*
	 * Nicely formatted Urey-Bradley string.
	 * 
	 * @return String
	 */
	public String toString() {
		return String.format("ureybrad  %5d  %5d  %5d  %6.2f  %7.4f",
				atomClasses[0], atomClasses[1], atomClasses[2], forceConstant,
				distance);
	}
}
