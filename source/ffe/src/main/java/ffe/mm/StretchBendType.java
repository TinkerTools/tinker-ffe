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
 * The StretchBendType class defines one stretch-bending energy type
 */
public final class StretchBendType extends BaseType {
	/*
	 * Atom class for this Stretch-Bend type
	 */
	public final int atomClasses[];

	/*
	 * Force constants (Kcal/mole/Angstrom-Degrees; for 1st and 2nd bond)
	 */
	public final double forceConstants[];

	/*
	 * @param atomClasses
	 * @param forceConstants
	 */
	public StretchBendType(int atomClasses[], double forceConstants[]) {
		super(ForceField.ForceFieldType.STRBND, new String(atomClasses[0]
				+ " " + atomClasses[1] + " " + atomClasses[2]));
		this.atomClasses = atomClasses;
		this.forceConstants = forceConstants;
	}

	/*
	 * Nicely formatted stretch-bending string
	 * 
	 * @return String
	 */
	public String toString() {
		return String.format("strbnd  %5d  %5d  %5d  %6.2f  %6.2f",
				atomClasses[0], atomClasses[1], atomClasses[2],
				forceConstants[0], forceConstants[1]);
	}
}
