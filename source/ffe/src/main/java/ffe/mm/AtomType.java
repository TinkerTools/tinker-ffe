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
 * The AtomType class represents one molecular mechanics atom type.
 */
public final class AtomType extends BaseType {
	/*
	 * Type.
	 */
	public final int type;

	/*
	 * Class.
	 */
	public final int atomClass;

	/*
	 * Short name (ie CH3/CH2 etc).
	 */
	public final String name;

	/*
	 * Description of the atom's bonding environment.
	 */
	public final String environment;

	/*
	 * Atomic Number.
	 */
	public final int atomicNumber;

	/*
	 * Atomic Mass.
	 */
	public final double mass;

	/*
	 * Atomic Hybridization.
	 */
	public final int hybridization;

	/*
	 * AtomType Constructor.
	 * 
	 * @param type
	 *            int
	 * @param atomClass
	 *            int
	 * @param name
	 *            String
	 * @param environment
	 *            String
	 * @param atomicNumber
	 *            int
	 * @param mass
	 *            double
	 * @param hybridization
	 *            int
	 */
	public AtomType(int type, int atomClass, String name, String environment,
			int atomicNumber, double mass, int hybridization) {
		super(ForceField.ForceFieldType.ATOM, new String("" + type));
		this.type = type;
		this.atomClass = atomClass;
		this.name = name;
		this.environment = environment;
		this.atomicNumber = atomicNumber;
		this.mass = mass;
		this.hybridization = hybridization;
	}

	/*
	 * Nicely formatted atom type string.
	 * 
	 * @return String
	 */
	public String toString() {
		String s;
		if (atomClass >= 0) {
			s = String.format("atom  %5d  %5d  %-4s  %-25s  %3d  %8.4f  %d",
					type, atomClass, name, environment, atomicNumber, mass,
					hybridization);
		} else {
			s = String.format("atom  %5d  %-4s  %-25s  %3d  %8.4f  %d", type,
					name, environment, atomicNumber, mass, hybridization);
		}
		return s;
	}

}
