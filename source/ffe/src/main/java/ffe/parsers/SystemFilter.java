/*
 * <p>Title: Force Field Explorer</p>
 * <p>Description: Force Field Explorer Molecular Modeling Program</p>
 * <p>Copyright: Copyright (c) 2004-2019 Jay William Ponder</p>
 * <p>Institution: Jay Ponder Lab, Washington University in Saint Louis</p>
 * @author Michael J. Schnieders
 * @version 8.6
 */

package ffe.parsers;

import java.io.File;
import java.util.ArrayList;

import ffe.lang.Atom;
import ffe.lang.Bond;
import ffe.lang.MolecularAssembly;
import ffe.lang.Utilities.FileType;
import ffe.mm.ForceField;

/*
 * The SystemFilter class is the base class for most file parsers.
 */
public abstract class SystemFilter {
	/*
	 * This follows the Tinker file versioning scheme.
	 * 
	 * @param file
	 *            File to find a version for.
	 * @return File Versioned File.
	 */
	public static File version(File file) {
		if (file == null) {
			return null;
		}
		if (!file.exists()) {
			return file;
		}
		String fileName = file.getAbsolutePath();
		int dot = file.getAbsolutePath().lastIndexOf(".");
		int under = file.getAbsolutePath().lastIndexOf("_");
		File newFile = file;
		if (under > dot) {
			String name = fileName.substring(0, under);
			newFile = new File(name);
		}
		File oldFile = newFile;
		int i = 1;
		while (newFile.exists()) {
			i = i + 1;
			newFile = oldFile;
			int thousand = i / 1000;
			int hundred = (i - 1000 * thousand) / 100;
			int tens = (i - 1000 * thousand - 100 * hundred) / 10;
			int ones = i - 1000 * thousand - 100 * hundred - 10 * tens;
			StringBuffer newFileString = new StringBuffer(oldFile
					.getAbsolutePath());
			if (thousand != 0) {
				newFileString.append('_').append(thousand).append(hundred)
						.append(tens).append(ones);
			} else if (hundred != 0) {
				newFileString.append('_').append(hundred).append(tens).append(
						ones);
			} else if (tens != 0) {
				newFileString.append('_').append(tens).append(ones);
			} else {
				newFileString.append('_').append(ones);
			}
			newFile = new File(newFileString.toString());
		}
		return newFile;
	}
	
	public static File previousVersion(File file){
		if (file == null) {
			return null;
		}
		
		String fileName = file.getAbsolutePath();
		int dot = file.getAbsolutePath().lastIndexOf(".");
		int under = file.getAbsolutePath().lastIndexOf("_");
		File newFile = file;
		if (under > dot) {
			String name = fileName.substring(0, under);
			newFile = new File(name);
		}
		File baseFile = newFile;
		File previousFile = null;
		int i = 1;
		while (newFile.exists()) {
			i = i + 1;
			previousFile = newFile;
			newFile = baseFile;
			int thousand = i / 1000;
			int hundred = (i - 1000 * thousand) / 100;
			int tens = (i - 1000 * thousand - 100 * hundred) / 10;
			int ones = i - 1000 * thousand - 100 * hundred - 10 * tens;
			StringBuffer newFileString = new StringBuffer(baseFile
					.getAbsolutePath());
			if (thousand != 0) {
				newFileString.append('_').append(thousand).append(hundred)
						.append(tens).append(ones);
			} else if (hundred != 0) {
				newFileString.append('_').append(hundred).append(tens).append(
						ones);
			} else if (tens != 0) {
				newFileString.append('_').append(tens).append(ones);
			} else {
				newFileString.append('_').append(ones);
			}
			newFile = new File(newFileString.toString());
		}
		return previousFile;
	}

	protected MolecularAssembly molecularAssembly = null;

	protected FileType fileType = FileType.UNK;

	/*
	 * The atomList and bondList are filled by the filters
	 * that extend this base class.
	 */
	protected ArrayList<Atom> atomList = null;

	protected ArrayList<Bond> bondList = null;

	protected ForceField forceField = null;

	protected boolean fileRead = false;

	/*
	 * Default constructor.
	 */
	public SystemFilter() {
	}

	/*
	 * SystemFilter constructor.
	 * 
	 * @param f
	 *            MolecularAssembly
	 */
	public SystemFilter(MolecularAssembly f) {
		molecularAssembly = f;
	}

	public SystemFilter(MolecularAssembly f, ForceField mm) {
		this(f);
		forceField = mm;
	}

	/*
	 * Returns true if the read was successful
	 */
	public boolean fileRead() {
		return fileRead;
	}

	public int getAtomCount() {
		if (atomList == null) {
			return 0;
		}
		return atomList.size();
	}

	public ArrayList<Atom> getAtomList() {
		return atomList;
	}

	public int getBondCount() {
		if (bondList == null) {
			return 0;
		}
		return bondList.size();
	}

	/*
	 * Return the MolecularSystem that has been read in
	 */
	public MolecularAssembly getMolecularSystem() {
		return molecularAssembly;
	}

	public FileType getType() {
		return fileType;
	}

	/*
	 * This method is different for each subclass and must be overidden
	 */
	public abstract boolean readFile();

	public void setFileRead(boolean b) {
		fileRead = b;
	}

	public void setForceField(ForceField f) {
		forceField = f;
	}

	public void setMolecularSystem(MolecularAssembly f) {
		molecularAssembly = f;
	}

	public void setType(FileType fileType) {
		this.fileType = fileType;
	}

	/*
	 * This method is different for each subclass and must be overidden
	 */
	public abstract boolean writeFile();
}
