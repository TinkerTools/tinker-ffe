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
 * The InducedFileFilter class is used to choose Tinker Induced (*.IND) files.
 */
public final class InducedFileFilter extends FileFilter {
	/*
	 * Default Constructor
	 */
	public InducedFileFilter() {
	}

	/*
	 * This method determines whether or not the parm File parameter
	 * is a Tinker *.xyz or not, returning true if it is. (Also returns
	 * true for any directory)
	 */
	public boolean accept(File parm) {
		if (parm.isDirectory()) {
			return true;
		}
		String filename = parm.getName().toLowerCase();
		return filename.endsWith("u");
	}

	/*
	 * Provides a description of this FileFilter
	 */
	public String getDescription() {
		return new String("Tinker Induced Dipole Files: *.*u");
	}
}
