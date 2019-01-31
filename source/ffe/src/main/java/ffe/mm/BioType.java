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
 * The BioType class maps PDB identifiers to atom types.
 */
public final class BioType extends BaseType {

	public final int index;

	public final String PDB;

	public final String residue;

	public final int atomType;

	/*
	 * BioType Constructor.
	 * 
	 * @param index
	 *            int
	 * @param PDB
	 *            String
	 * @param residue
	 *            String
	 * @param atomType
	 *            int
	 */
	public BioType(int index, String PDB, String residue, int atomType) {
		super(ForceField.ForceFieldType.BIOTYPE,
				new String(residue + " " + PDB));
		this.index = index;
		this.PDB = PDB;
		this.residue = residue;
		this.atomType = atomType;
	}

	/*
	 * Nicely formatted biotype.
	 * 
	 * @return String
	 */
	public String toString() {
		return String.format("biotype  %5d  %-4s  %-25s  %5d", index, PDB,
				residue, atomType);
	}

}
