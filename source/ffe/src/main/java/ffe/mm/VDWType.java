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
 * The VDWType class defines a van der Waals type.
 */
public final class VDWType extends BaseType {
	public enum RadiusSize {
		RADIUS, DIAMETER
	}

	public enum RadiusType {
		RMIN, SIGMA
	}

	/*
	 * The atom class that uses this van der Waals parameter.
	 */
	public final int atomClass;

	/*
	 * The radius of the minimum well depth energy (angstroms).
	 */
	public final double radius;

	/*
	 * The minimum energy of the vdw function (kcal/mol).
	 */
	public final double wellDepth;

	/*
	 * Reduction factor for evaluating van der Waals pairs.
	 * Valid range: 0.0 > reduction <= 1.0
	 * Usually only hydrogen atom have a reduction factor.
	 * Setting the reduction to < 0.0 indicates it is not being used.
	 */
	public final double reductionFactor;

	/*
	 * van der Waals constructor. If the reduction factor is <= 0.0,
	 * no reduction is used for this atom type.
	 * 
	 * @param atomClass
	 *            int
	 * @param radius
	 *            double
	 * @param wellDepth
	 *            double
	 * @param reductionFactor
	 *            double
	 */
	public VDWType(int atomClass, double radius, double wellDepth,
			double reductionFactor) {
		super(ForceField.ForceFieldType.VDW, new String("" + atomClass));
		this.atomClass = atomClass;
		this.radius = radius;
		this.wellDepth = wellDepth;
		this.reductionFactor = reductionFactor;
	}

	/*
	 * Nicely formatted van der Waals type.
	 * 
	 * @return String
	 */
	public String toString() {
		String vdwString;
		// No reduction factor.
		if (reductionFactor <= 0e0) {
			vdwString = String.format("vdw  %5d  %11.9f  %11.9f", atomClass,
					radius, wellDepth);
		} else {
			vdwString = String.format("vdw  %5d  %11.9f  %11.9f  %5.3f",
					atomClass, radius, wellDepth, reductionFactor);
		}
		return vdwString;
	}
}
