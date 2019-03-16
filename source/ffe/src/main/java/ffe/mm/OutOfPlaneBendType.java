/*
 * <p>Title: Force Field Explorer</p>
 * <p>Description: Force Field Explorer Molecular Modeling Program</p>
 * <p>Copyright: Copyright (c) 2004-2019 Jay William Ponder</p>
 * <p>Institution: Jay Ponder Lab, Washington University in Saint Louis</p>
 * @author Michael J. Schnieders
 * @version 8.6
 */

package ffe.mm;

/*
 * The OutOfPlaneBendType class defines one out-of-plane angle bending
 * energy type.
 */
public final class OutOfPlaneBendType extends BaseType {
	/*
	 * Atom classes for this out-of-plane angle bending type.
	 */
	public final int atomClasses[];

	/*
	 * Force constant (Kcal/mol).
	 */
	public final double forceConstant;

	/*
	 * OutOfPlaneBendType Constructor.
	 * 
	 * @param atomClasses
	 *            int[]
	 * @param forceConstant
	 *            double
	 */
	public OutOfPlaneBendType(int atomClasses[], double forceConstant) {
		super(ForceField.ForceFieldType.OPBEND, new String(atomClasses[0] + " "
				+ atomClasses[1]));
		this.atomClasses = atomClasses;
		this.forceConstant = forceConstant;
	}

	/*
	 * Nicely formatted out-of-plane angle bending string.
	 * 
	 * @return String
	 */
	public String toString() {
		return String.format("opbend  %5d  %5d  %4.2f", atomClasses[0],
				atomClasses[1], forceConstant);
	}
}
