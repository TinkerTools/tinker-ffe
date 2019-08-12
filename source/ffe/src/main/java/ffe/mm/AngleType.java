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
 * The AngleType class defines one harmonic angle bend energy term.
 */
public final class AngleType extends BaseType {
	/*
	 * Atom classes that for this Angle bend.
	 */
	public final int atomClasses[];

	/*
	 * Force constant (Kcal/mole/radian^2).
	 */
	public final double forceConstant;

	/*
	 * Equilibrium angle (degrees). There can be up to three equilibrium
	 * angles, depending on the number of attached hydrogens (0, 1, or 2).
	 */
	public final double angle[];

	/*
	 * @param atomClasses
	 * @param forceConstant
	 * @param angle
	 */
	public AngleType(int atomClasses[], double forceConstant, double angle[]) {
		super(ForceField.ForceFieldType.ANGLE, new String(atomClasses[0] + " "
				+ atomClasses[1] + " " + atomClasses[2]));
		this.atomClasses = atomClasses;
		this.forceConstant = forceConstant;
		this.angle = angle;
	}

	/*
	 * Nicely formatted Angle bending string.
	 * 
	 * @return String
	 */
	public String toString() {
		StringBuffer angleString = new StringBuffer(String.format(
				"angle  %5d  %5d  %5d  %6.2f", atomClasses[0], atomClasses[1],
				atomClasses[2], forceConstant));
		for (double eq : angle) {
			angleString.append(String.format("  %6.2f", eq));
		}
		return angleString.toString();
	}
}
