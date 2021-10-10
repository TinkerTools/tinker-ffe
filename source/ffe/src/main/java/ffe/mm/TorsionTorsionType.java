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
 * The TorsionTorsionType class defines a Torsion-Torsion spline.
 */
public final class TorsionTorsionType extends BaseType {
	/*
	 * Atom classes that form this Torsion-Torsion type.
	 */
	public final int atomClasses[];

	public final int gridPoints[];

	public final double torsion1[];

	public final double torsion2[];

	public final double energy[];

	public TorsionTorsionType(int atomClasses[], int gridPoints[],
			double torsion1[], double torsion2[], double energy[]) {
		super(ForceField.ForceFieldType.TORTORS, new String(atomClasses[0]
				+ " " + atomClasses[1] + " " + atomClasses[2] + " "
				+ atomClasses[3] + " " + atomClasses[4]));
		this.atomClasses = atomClasses;
		this.gridPoints = gridPoints;
		this.torsion1 = torsion1;
		this.torsion2 = torsion2;
		this.energy = energy;
	}

	/*
	 * Nicely formatted torsion-torsion type.
	 * 
	 * @return String
	 */
	public String toString() {
		StringBuffer tortorBuffer = new StringBuffer("tortors");
		for (int i : atomClasses) {
			tortorBuffer.append(String.format("  %5d", i));
		}
		tortorBuffer.append(String.format("  %2d  %2d", gridPoints[0],
				gridPoints[1]));
		for (int i = 0; i < energy.length; i++) {
			tortorBuffer.append(String.format("\n  % 6.1f  % 6.1f  % 8.5f",
					torsion1[i], torsion2[i], energy[i]));
		}
		return tortorBuffer.toString();
	}
}
