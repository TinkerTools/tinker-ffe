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
 * The ChargeType class defines a partial atomic charge type.
 */
public final class ChargeType extends BaseType {

	/*
	 * The atom type that uses this charge parameter.
	 */
	public final int atomType;

	/*
	 * Partial atomic charge in units of electrons.
	 */
	public final double charge;

	/*
	 * ChargeType constructor.
	 * 
	 * @param atomType
	 *            int
	 * @param charge
	 *            double
	 */
	public ChargeType(int atomType, double charge) {
		super(ForceField.ForceFieldType.CHARGE, new String("" + atomType));
		this.atomType = atomType;
		this.charge = charge;
	}

	/*
	 * Nicely formatted Charge type.
	 * 
	 * @return String
	 */
	public String toString() {
		return String.format("charge  %5d  % 7.5f", atomType, charge);
	}

}
