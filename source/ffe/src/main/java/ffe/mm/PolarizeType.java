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
 * The PolarizeType class defines an isotropic atomic polarizability.
 */
public final class PolarizeType extends BaseType {
	/*
	 * Atom type number.
	 */
	public final int atomType;

	/*
	 * Thole damping factor.
	 */
    public final double thole;
	
	/*
	 * Isotropic polarizability in units of Angstroms^3.
	 */
	public final double polarizability;

	/*
	 * Connected types in the polarization group of each atom. (may be null)
	 */
	public final int[] polarizationGroup;

	/*
	 * PolarizeType Constructor.
	 * 
	 * @param atomType
	 *            int
	 * @param polarizability
	 *            double
	 * @param polarizationGroup
	 *            int[]
	 */
	public PolarizeType(int atomType, double polarizability, double thole,
			int polarizationGroup[]) {
		super(ForceField.ForceFieldType.POLARIZE, new String("" + atomType));
		this.atomType = atomType;
		this.thole = thole;
		this.polarizability = polarizability;
		this.polarizationGroup = polarizationGroup;
	}

	/*
	 * Nicely formatted polarization type.
	 * 
	 * @return String
	 */
	public String toString() {
		StringBuffer polarizeString = new StringBuffer(String.format(
				"polarize  %5d  %5.3f %5.3f", atomType, polarizability, thole));
		if (polarizationGroup != null) {
			for (int a : polarizationGroup) {
				polarizeString.append(String.format("  %5d", a));
			}
		}
		return polarizeString.toString();
	}
}
