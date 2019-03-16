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
 * The TorsionType class defines a torsional angle.
 */
public final class TorsionType extends BaseType {
	/*
	 * Atom classes that for this Torsion angle.
	 */
	public final int atomClasses[];

	/*
	 * Amplitudes of the Fourier series.
	 */
	public final double amplitude[];

	/*
	 * Phases of the Fourier series.
	 */
	public final double phase[];

	/*
	 * Periodicity of the Fourier series.
	 */
	public final int periodicity[];

	/*
	 * TorsionType Constructor.
	 * 
	 * @param atomClasses
	 *            int[]
	 * @param amplitude
	 *            double[]
	 * @param phase
	 *            double[]
	 * @param periodicity
	 *            double[]
	 */
	public TorsionType(int atomClasses[], double amplitude[], double phase[],
			int periodicity[]) {
		super(ForceField.ForceFieldType.TORSION, new String(atomClasses[0]
				+ " " + atomClasses[1] + " " + atomClasses[2] + " "
				+ atomClasses[3]));
		this.atomClasses = atomClasses;
		this.amplitude = amplitude;
		this.phase = phase;
		this.periodicity = periodicity;
	}

	/*
	 * Nicely formatted Torsion angle.
	 * 
	 * @return String
	 */
	public String toString() {
		StringBuffer torsionBuffer = new StringBuffer("torsion");
		for (int i : atomClasses) {
			torsionBuffer.append(String.format("  %5d", i));
		}
		for (int i = 0; i < amplitude.length; i++) {
			torsionBuffer.append(String.format("  % 5.3f  %5.3f  %1d",
					amplitude[i], phase[i], periodicity[i]));
		}
		return torsionBuffer.toString();
	}
}
