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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.logging.Logger;

import org.jogamp.vecmath.Vector3d;

import ffe.lang.Atom;
import ffe.lang.Bond;
import ffe.lang.MolecularAssembly;
import ffe.lang.Utilities.FileType;
import ffe.mm.AtomType;
import ffe.mm.ForceField;
import ffe.mm.ForceField.ForceFieldType;

/*
 * The XYZFilter class parses Tinker Cartesian Coordinate (*.XYZ) files
 */
public class XYZFilter extends SystemFilter {
	public static boolean readOnto(File newFile, MolecularAssembly oldSystem) {
		if (newFile == null || !newFile.exists() || oldSystem == null) {
			return false;
		}
		try {
			FileReader fr = new FileReader(newFile);
			BufferedReader br = new BufferedReader(fr);
			String data = br.readLine();
			if (data == null) {
				return false;
			}
			String tokens[] = data.trim().split("\\s+");
			int num_atoms = Integer.parseInt(tokens[0]);
			if (num_atoms != oldSystem.getAtomList().size()) {
				return false;
			}
			double d[][] = new double[3][num_atoms];
			double box[] = new double[6];
			for (int i = 0; i < num_atoms; i++) {
				if (!br.ready()) {
					return false;
				}
				data = br.readLine();
				if (data == null) {
					Logger.getLogger("ffe").warning("Check atom " + (i + 1));
					return false;
				}
				tokens = data.trim().split("\\s+");
				if (tokens == null || tokens.length < 6) {
					Logger.getLogger("ffe").warning("Check atom " + (i + 1));
					return false;
				}

				// Try to parse the first line for cell dimensions
				if (i == 0) {
					try {
						box[0] = Double.parseDouble(tokens[0]);
						box[1] = Double.parseDouble(tokens[1]);
						box[2] = Double.parseDouble(tokens[2]);
						box[3] = Double.parseDouble(tokens[3]);
						box[4] = Double.parseDouble(tokens[4]);
						box[5] = Double.parseDouble(tokens[5]);
						data = br.readLine();
						if (data == null) {
							Logger.getLogger("ffe").warning("Check atom " + (i + 1));
							return false;
						}
						tokens = data.trim().split("\\s+");
						if (tokens == null || tokens.length < 6) {
							Logger.getLogger("ffe").warning("Check atom " + (i + 1));
							return false;
						}
					} catch(NumberFormatException e) {
					}
				}

				d[0][i] = Double.parseDouble(tokens[2]);
				d[1][i] = Double.parseDouble(tokens[3]);
				d[2][i] = Double.parseDouble(tokens[4]);
			}
			double[] x = new double[3];
			ArrayList<Atom> atoms = oldSystem.getAtomList();
			for (Atom a : atoms) {
				int index = a.getXYZIndex() - 1;
				x[0] = d[0][index];
				x[1] = d[1][index];
				x[2] = d[2][index];
				a.moveTo(x);
			}
			oldSystem.center();
			oldSystem.setFile(newFile);
			br.close();
			fr.close();
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public XYZFilter() {
		super();
		setType(FileType.XYZ);
	}

	public XYZFilter(MolecularAssembly system) {
		super(system);
		setType(FileType.XYZ);
	}

	public XYZFilter(MolecularAssembly system, ForceField forceField) {
		super(system, forceField);
		setType(FileType.XYZ);
	}

	/*
	 * Parse the XYZ File
	 */
	public boolean readFile() {
		Logger logger = Logger.getLogger("ffe");
		File xyzFile = molecularAssembly.getFile();
		if (forceField == null) {
			logger.warning("No force field is associated with "
					+ xyzFile.toString());
			return false;
		}
		try {
			FileReader fr = new FileReader(xyzFile);
			BufferedReader br = new BufferedReader(fr);
			String data = br.readLine();
			// Read blank lines at the top of the file
			while (data != null && data.trim().equals("")) {
				data = br.readLine();
			}
			if (data == null) {
				return false;
			}
			String tokens[] = data.trim().split("\\s+", 2);
			int numberOfAtoms = Integer.parseInt(tokens[0]);
			if (numberOfAtoms < 1) {
				return false;
			}
			if (tokens.length == 2) {
				getMolecularSystem().setName(tokens[1]);
			}
			logger.info("  Opening " + xyzFile.getName() + " with "
					+ numberOfAtoms + " atoms");
			// The header line is reasonable; prepare to parse atom lines	
			Hashtable<Integer, Integer> labelHash = new Hashtable<Integer, Integer>();
			int label[] = new int[numberOfAtoms];
			int bonds[][] = new int[numberOfAtoms][8];
			double d[] = new double[3];
			double box[] = new double[6];
			boolean renumber = false;
			atomList = new ArrayList<Atom>();
			// Loop over the expected number of atoms
			for (int i = 0; i < numberOfAtoms; i++) {
				if (!br.ready()) {
					return false;
				}
				data = br.readLine();
				if (data == null) {
					logger.warning("Check atom " + (i + 1) + " in "
							+ molecularAssembly.getFile().getName());
					return false;
				}
				tokens = data.trim().split("\\s+");
				if (tokens == null || tokens.length < 6) {
					logger.warning("Check atom " + (i + 1) + " in "
							+ molecularAssembly.getFile().getName());
					return false;
				}

				// Try to parse the first line for cell dimensions
				if (i == 0) {
					try {
						box[0] = Double.parseDouble(tokens[0]);
						box[1] = Double.parseDouble(tokens[1]);
						box[2] = Double.parseDouble(tokens[2]);
						box[3] = Double.parseDouble(tokens[3]);
						box[4] = Double.parseDouble(tokens[4]);
						box[5] = Double.parseDouble(tokens[5]);
						data = br.readLine();
						if (data == null) {
							logger.warning("Check atom " + (i + 1) + " in "
									+ molecularAssembly.getFile().getName());
							return false;
						}
						tokens = data.trim().split("\\s+");
						if (tokens == null || tokens.length < 6) {
							logger.warning("Check atom " + (i + 1) + " in "
									+ molecularAssembly.getFile().getName());
							return false;
						}
					} catch(NumberFormatException e) {
					}
				}

                                // Parse the line to get the current atom values
				label[i] = Integer.parseInt(tokens[0]);
				// Check for valid atom numbering, or flag for re-numbering.
				if (label[i] != i + 1) {
					renumber = true;
				}
				String atomName = tokens[1];
				d[0] = Double.parseDouble(tokens[2]);
				d[1] = Double.parseDouble(tokens[3]);
				d[2] = Double.parseDouble(tokens[4]);
				int type = Integer.parseInt(tokens[5]);
				AtomType atomType = (AtomType) forceField.getForceFieldType(
						ForceFieldType.ATOM, Integer.toString(type));
				if (atomType == null) {
					logger.warning("Check Atom Type for Atom " + (i + 1)
							+ " in " + molecularAssembly.getFile().getName());
					return false;
				}
				Atom a = new Atom(i + 1, atomName, atomType, d);
				atomList.add(a);
				// Bond Data
				int numberOfBonds = tokens.length - 6;
				for (int b = 0; b < 8; b++) {
					if (b < numberOfBonds) {
						int bond = Integer.parseInt(tokens[6 + b]);
						bonds[i][b] = bond;
					} else {
						bonds[i][b] = 0;
					}
				}
			}
			// Check if this is an archive
			if (br.ready()) {
				// Read past blank lines between archive files
				data = br.readLine().trim();
				while (data != null && data.equals("")) {
					data = br.readLine().trim();
				}
				if (data != null) {
					tokens = data.split("\\s+", 2);
					if (tokens != null && tokens.length > 0) {
						try {
							int archiveNumberOfAtoms = Integer
									.parseInt(tokens[0]);
							if (archiveNumberOfAtoms == numberOfAtoms) {
								setType(FileType.ARC);
							}
						} catch (Exception e) {
							tokens = null;
						}
					}
				}
			}
			br.close();
			fr.close();
			// Try to renumber
			if (renumber) {
				for (int i = 0; i < numberOfAtoms; i++) {
					if (labelHash.containsKey(label[i])) {
						logger.warning("Two atoms have the same index: "
								+ label[i]);
						return false;
					}
					labelHash.put(label[i], i + 1);
				}
				for (int i = 0; i < numberOfAtoms; i++) {
					int j = -1;
					while (j < 3 && bonds[i][++j] > 0) {
						bonds[i][j] = labelHash.get(bonds[i][j]);
					}
				}
			}
			bondList = new ArrayList<Bond>();
			for (int i = 1; i <= numberOfAtoms; i++) {
				int a1 = i;
				int j = -1;
				while (j < 7 && bonds[i - 1][++j] > 0) {
					int a2 = bonds[i - 1][j];
					if (a1 < a2) {
						if (a1 > numberOfAtoms || a1 < 1 || a2 > numberOfAtoms
								|| a2 < 1) {
							logger.warning("Check the Bond Bewteen " + a1
									+ " and " + a2 + " in "
									+ molecularAssembly.getFile().getName());
							return false;
						}
						// Check for bidirectional connection
						boolean bidirectional = false;
						int k = -1;
						while (k < 7 && bonds[a2 - 1][++k] > 0) {
							int a3 = bonds[a2 - 1][k];
							if (a3 == a1) {
								bidirectional = true;
								break;
							}
						}
						if (!bidirectional) {
							logger.warning("Check the Bond Bewteen " + a1
									+ " and " + a2 + " in "
									+ molecularAssembly.getFile().getName());
							return false;
						}
						Atom atom1 = atomList.get(a1 - 1);
						Atom atom2 = atomList.get(a2 - 1);
						if (atom1 == null || atom2 == null) {
							logger.warning("Check the Bond Bewteen " + a1
									+ " and " + a2 + " in "
									+ molecularAssembly.getFile().getName());
							return false;
						}
						Bond bond = new Bond(atom1, atom2, 1);
						bondList.add(bond);
					}
				}
			}
			if (getType() == FileType.ARC) {
				return readtrajectory();
			}
			return true;
		} catch (IOException e) {
			logger.severe(e.toString());
		}
		return false;
	}

	public boolean readtrajectory() {
		// If the first entry was read successfully, reopen the
		// archive file and read the rest of the coordinates
		try {
			Logger logger = Logger.getLogger("ffe");
			logger.info("Trying to parse " + molecularAssembly.getFile() + " as an archive.");
			BufferedReader bin = new BufferedReader(new FileReader(
					molecularAssembly.getFile()));
			String data = null;
			int numatoms = atomList.size();
			int cycle = 1;
			double box[] = new double[6];
			double coords[][] = new double[numatoms][3];
			
			// Read past the first coordinate set
			for (int i = 0; i < numatoms + 1; i++) {
				data = bin.readLine();
				while (data != null && data.trim().equals("")) {
					data = bin.readLine();
				}
				String[] tokens = data.trim().split("\\s+");
				if (i == 1) {
					try {
						box[0] = Double.parseDouble(tokens[0]);
						box[1] = Double.parseDouble(tokens[1]);
						box[2] = Double.parseDouble(tokens[2]);
						box[3] = Double.parseDouble(tokens[3]);
						box[4] = Double.parseDouble(tokens[4]);
						box[5] = Double.parseDouble(tokens[5]);
						data = bin.readLine();
					} catch(NumberFormatException e) {
					}
				}
			}
			
			while (bin.ready()) {
				data = bin.readLine();
				// Read past blank lines
				while (data != null && data.trim().equals("")) {
					data = bin.readLine();
				}
				String[] tokens = data.trim().split("\\s+");
				try {
					int num = Integer.parseInt(data.trim().split("\\s+")[0]);
					if (num != numatoms) {
						logger.warning(num + " atoms for archive entry " + cycle + " is not equal to " + numatoms + "."
								+ "Only the first " + (cycle - 1) + " entries were read.");
						return true;
					}
				} catch (Exception e) {
					logger.severe(e.toString());
					return false;
				}
				for (int i = 0; i < numatoms; i++) {
					data = bin.readLine();
					// Read past blank lines
					while (data != null && data.trim().equals("")) {
						data = bin.readLine();
					}	
					tokens = data.trim().split("\\s+");
					if (tokens == null || tokens.length < 6) {
						logger.warning("Check atom " + (i + 1) + ", archive entry "
								+ (cycle + 1) + " in "
								+ molecularAssembly.getFile().getName());
						return false;
					}
					if (i == 0) {
						try {
							box[0] = Double.parseDouble(tokens[0]);
							box[1] = Double.parseDouble(tokens[1]);
							box[2] = Double.parseDouble(tokens[2]);
							box[3] = Double.parseDouble(tokens[3]);
							box[4] = Double.parseDouble(tokens[4]);
							box[5] = Double.parseDouble(tokens[5]);
							data = bin.readLine();
							while (data != null && data.trim().equals("")) {
								data = bin.readLine();
							}	
							tokens = data.trim().split("\\s+");
							if (tokens == null || tokens.length < 6) {
								logger.warning("Check atom " + (i + 1) + ", archive entry "
										+ (cycle + 1) + " in "
										+ molecularAssembly.getFile().getName());
								return false;
							}
						} catch(NumberFormatException e) {
						}
					}
					coords[i][0] = Double.parseDouble(tokens[2]);
					coords[i][1] = Double.parseDouble(tokens[3]);
					coords[i][2] = Double.parseDouble(tokens[4]);
				}
				for (Atom a : atomList) {
					int i = a.xyzindex - 1;
					Vector3d v3d = new Vector3d(coords[i][0], coords[i][1],
							coords[i][2]);
					a.addTrajectoryCoords(v3d, cycle);
				}
				cycle++;
			}
			molecularAssembly.setCycles(cycle);
			molecularAssembly.setFileType(FileType.ARC);
			setFileRead(true);
			return true;
		} catch (FileNotFoundException e) {
			Logger.getLogger("ffe").warning(e.toString());
		} catch (IOException e) {
			Logger.getLogger("ffe").warning(e.toString());
		}
		return false;
	}

	public boolean writeFile() {
		File xyzfile = molecularAssembly.getFile();
		if (xyzfile == null) {
			return false;
		}
		try {
			FileWriter fw = new FileWriter(xyzfile);
			BufferedWriter bw = new BufferedWriter(fw);
			// XYZ File First Line
			int numberOfAtoms = molecularAssembly.getAtomList().size();
			String output = String.format("%6d  %s\n", numberOfAtoms,
					molecularAssembly.toString());
			System.out.println(output);
			bw.write(output);
			Atom a2;
			StringBuffer line;
			StringBuffer lines[] = new StringBuffer[numberOfAtoms];
			// XYZ File Atom Lines
			ArrayList<Atom> atoms = molecularAssembly.getAtomList();
			Vector3d offset = molecularAssembly.getOffset();
			for (Atom a : atoms) {
				line = new StringBuffer(String.format(
						"%6d%4s% 10.6f % 10.6f % 10.6f%6d", a.getXYZIndex(), a
								.getID(), a.getX() - offset.x, a.getY()
								- offset.y, a.getZ() - offset.z, a.getType()));
				for (Bond b : a.getBonds()) {
					a2 = b.get1_2(a);
					line.append(String.format("%6d", a2.xyzindex));
				}
				lines[a.getXYZIndex() - 1] = line.append("\n");
			}
			for (int i = 0; i < numberOfAtoms; i++) {
				try {
					bw.write(lines[i].toString());
				} catch (Exception e) {
					Logger
							.getLogger("ffe")
							.severe(
									"Their was an unexpected error "
											+ "writing to "
											+ getMolecularSystem().toString()
											+ "\n"
											+ e
											+ "\nForce Field Explorer will continue...");
					return false;
				}
			}
			bw.close();
			fw.close();
		} catch (IOException e) {
			Logger.getLogger("ffe").severe(
					"Their was an unexpected error " + "writing to "
							+ getMolecularSystem().toString() + "\n" + e
							+ "\nForce Field Explorer will continue...");
			return false;
		}
		return true;
	}
}
