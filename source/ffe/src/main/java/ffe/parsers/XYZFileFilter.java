/*
 * <p>Title: Force Field Explorer</p>
 * <p>Description: Force Field Explorer Molecular Modeling Program</p>
 * <p>Copyright: Copyright (c) 2004-2019 Jay William Ponder</p>
 * <p>Institution: Jay Ponder Lab, Washington University in Saint Louis</p>
 * @author Michael J. Schnieders
 * @version 8.6
 */

package ffe.parsers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import javax.swing.filechooser.FileFilter;

/*
 * The XYZFileFilter class is used to choose Tinker Cartesian
 * Coordinate (*.XYZ) files
 */
public final class XYZFileFilter extends FileFilter {
	/*
	 * Default Constructor
	 */
	public XYZFileFilter() {
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
		int dot = filename.lastIndexOf(".");
		return filename.regionMatches(false, dot + 1, "xyz", 0, 3);
	}

	public boolean acceptDeep(File file) {
		try {
			if (file == null || file.isDirectory() || !file.canRead()) {
				return false;
			}
			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);
			if (!br.ready()) {
				// Empty File?
				return false;
			}
			// If the first token is not an integer this file is not
			// a Tinker Cartesian coordinate File.
			String rawdata = br.readLine();
			String header[] = rawdata.trim().split("\\s+");
			if (header == null || header.length == 0) {
				return false;
			}
			try {
				Integer.parseInt(header[0]);
			} catch (Exception e) {
				return false;
			}
			// If the the first atom line does not begin with an integer and
			// contain at least six tokens, this is not a Tinker Cartesian
			// coordinate file; modified to read a second atom line, in order
			// to handle a first line with periodic box values; JWP, Feb 2015
			String firstAtom = br.readLine();
			if (firstAtom == null) {
				return false;
			}
			String secondAtom = br.readLine();
			if (secondAtom == null) {
				return false;
			}
			br.close();
			fr.close();
			String data[] = secondAtom.trim().split("\\s+");
			if (data == null || data.length < 6) {
				return false;
			}
			try {
				Integer.parseInt(data[0]);
			} catch (Exception e) {
				return false;
			}
			return true;
		} catch (Exception e) {
			return true;
		}
	}

	/*
	 * Provides a description of this FileFilter
	 */
	public String getDescription() {
		return new String("Tinker Cartesian Coordinate Files: *.xyz");
	}
}
