/*
 * <p>Title: Force Field Explorer</p>
 * <p>Description: Force Field Explorer Molecular Modeling Program</p>
 * <p>Copyright: Copyright (c) 2004-2020 Jay William Ponder</p>
 * <p>Institution: Jay Ponder Lab, Washington University in Saint Louis</p>
 * @author Michael J. Schnieders
 * @version 8.8
 */

package ffe.parsers;

import java.io.File;

import javax.swing.filechooser.FileFilter;

/*
 * The DYNFileFilter class is used to choose Tinker Restart (*.DYN) files
 */
public final class DYNFileFilter extends FileFilter {
	/*
	 * Default Constructor
	 */
	public DYNFileFilter() {
	}

	/*
	 * This method determines whether or not the parm File parameter
	 * is a Tinker *.xyz or not, returning true if it is; Also returns
	 * true for any directory
	 */
	public boolean accept(File parm) {
		if (parm.isDirectory()) {
			return true;
		}
		String filename = parm.getName().toLowerCase();
		int dot = filename.lastIndexOf(".");
		return filename.regionMatches(false, dot + 1, "dyn", 0, 3);
	}

	/*
	 * Provides a description of this FileFilter
	 */
	public String getDescription() {
		return new String("Tinker Restart Files: *.dyn");
	}
}
