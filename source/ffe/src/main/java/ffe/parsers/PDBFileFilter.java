/*
 * <p>Title: Force Field Explorer</p>
 * <p>Description: Force Field Explorer Molecular Modeling Program</p>
 * <p>Copyright: Copyright (c) 2004-2019 Jay William Ponder</p>
 * <p>Institution: Jay Ponder Lab, Washington University in Saint Louis</p>
 * @author Michael J. Schnieders
 * @version 8.7
 */

package ffe.parsers;

import java.io.File;

import javax.swing.filechooser.FileFilter;

/*
 * The PDBFileFilter class is used to choose Protein Databank (*.PDB) files
 */
public final class PDBFileFilter extends FileFilter {
	/*
	 * Default Constructor
	 */
	public PDBFileFilter() {
	}

	/*
	 * This method determines whether or not the parm File parameter is a
	 * Protein Database *.pdb file or not, returning true if it is. (Also
	 * returns true for any directory)
	 */
	public boolean accept(File parm) {
		if (parm.isDirectory()) {
			return true;
		}
		String filename = parm.getName();
		int dot = filename.lastIndexOf(".");
		return filename.regionMatches(false, dot + 1, "pdb", 0, 3);
	}

	/*
	 * Provides a description of this FileFilter
	 */
	public String getDescription() {
		return new String("Protein Data Bank Files: *.pdb");
	}
}
