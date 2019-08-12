/*
 * <p>Title: Force Field Explorer</p>
 * <p>Description: Force Field Explorer Molecular Modeling Program</p>
 * <p>Copyright: Copyright (c) 2004-2019 Jay William Ponder</p>
 * <p>Institution: Jay Ponder Lab, Washington University in Saint Louis</p>
 * @author Michael J. Schnieders
 * @version 8.7
 */

package ffe.parsers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;
import java.util.logging.Logger;

import ffe.lang.Atom;
import ffe.lang.Bond;
import ffe.lang.MolecularAssembly;
import ffe.lang.VectorMath;
import ffe.lang.Utilities.FileType;
import ffe.mm.AtomType;
import ffe.mm.ForceField;

/*
 * The INTFilter class parses Tinker Internal Coordinate (*.INT) files.
 */
public class INTFilter extends SystemFilter {
	
	private Logger logger = Logger.getLogger("ffe");
	
	private static double x[] = new double[3];

	private static double zcos[] = new double[2];

	private static double zsin[] = new double[2];

	private static double xa[] = new double[3];

	private static double xb[] = new double[3];

	private static double xc[] = new double[3];

	private static double xab[] = new double[3];

	private static double xba[] = new double[3];

	private static double xbc[] = new double[3];

	private static double xac[] = new double[3];

	private static double xt[] = new double[3];

	private static double xu[] = new double[3];

	private static double rab, cosb, sinb, cosg, sing;

	private static double sine, sine2, cosine;

	private static double a, b, c;

	private static double xtmp, ztmp;

	private static double eps = 0.0000001d;

	/*
	 * Default Constructor
	 */
	public INTFilter() {
		setType(FileType.INT);
	}

	public INTFilter(MolecularAssembly sys) {
		super(sys);
		setType(FileType.INT);
	}

	public INTFilter(MolecularAssembly sys, ForceField forceField) {
		super(sys, forceField);
		setType(FileType.INT);
	}

	/*
	 * This routine was derived from a similar routine in Tinker.
	 * 
	 * @param atoms
	 *            ArrayList
	 * @param atom
	 *            Atom
	 * @param zi
	 *            int[]
	 * @param zv
	 *            double[]
	 */
	public void intxyz(ArrayList<Atom> atoms, Atom atom, int zi[], double zv[]) {
		zv[1] = Math.toRadians(zv[1]);
		zv[2] = Math.toRadians(zv[2]);
		zcos[0] = Math.cos(zv[1]);
		zcos[1] = Math.cos(zv[2]);
		zsin[0] = Math.sin(zv[1]);
		zsin[1] = Math.sin(zv[2]);
		// No partners
		if (zi[0] == 0) {
			atom.moveTo(0.0d, 0.0d, 0.0d);
			return;
		}
		// One partner - place on the z-axis
		if (zi[1] == 0) {
			(atoms.get(zi[0] - 1)).getXYZ(xa);
			xa[2] += zv[0];
			atom.moveTo(xa);
			return;
		}
		// Two partners - place in the xz-plane
		if (zi[2] == 0) {
			atoms.get(zi[0] - 1).getXYZ(xa);
			atoms.get(zi[1] - 1).getXYZ(xb);
			VectorMath.diff(xa, xb, xab);
			rab = VectorMath.r(xab);
			VectorMath.norm(xab, xab);
			cosb = xab[2];
			sinb = Math.sqrt(xab[0] * xab[0] + xab[1] * xab[1]);
			if (sinb == 0.0d) {
				cosg = 1.0d;
				sing = 0.0d;
			} else {
				cosg = xab[1] / sinb;
				sing = xab[0] / sinb;
			}
			xtmp = zv[0] * zsin[0];
			ztmp = rab - zv[0] * zcos[0];
			x[0] = xb[0] + xtmp * cosg + ztmp * sing * sinb;
			x[1] = xb[1] - xtmp * sing + ztmp * cosg * sinb;
			x[2] = xb[2] + ztmp * cosb;
			atom.moveTo(x);
			return;
		}
		// General case - with a dihedral
		if (zi[3] == 0) {
			atoms.get(zi[0] - 1).getXYZ(xa);
			atoms.get(zi[1] - 1).getXYZ(xb);
			atoms.get(zi[2] - 1).getXYZ(xc);
			VectorMath.diff(xa, xb, xab);
			VectorMath.norm(xab, xab);
			VectorMath.diff(xb, xc, xbc);
			VectorMath.norm(xbc, xbc);
			xt[0] = xab[2] * xbc[1] - xab[1] * xbc[2];
			xt[1] = xab[0] * xbc[2] - xab[2] * xbc[0];
			xt[2] = xab[1] * xbc[0] - xab[0] * xbc[1];
			cosine = xab[0] * xbc[0] + xab[1] * xbc[1] + xab[2] * xbc[2];
			sine = Math.sqrt(Math.max(1.0d - cosine * cosine, eps));
			if (Math.abs(cosine) >= 1.0d) {
				logger.warning("Undefined Dihedral");
			}
			VectorMath.scalar(xt, 1.0d / sine, xt);
			xu[0] = xt[1] * xab[2] - xt[2] * xab[1];
			xu[1] = xt[2] * xab[0] - xt[0] * xab[2];
			xu[2] = xt[0] * xab[1] - xt[1] * xab[0];
			x[0] = xa[0]
					+ zv[0]
					* (xu[0] * zsin[0] * zcos[1] + xt[0] * zsin[0] * zsin[1] - xab[0]
							* zcos[0]);
			x[1] = xa[1]
					+ zv[0]
					* (xu[1] * zsin[0] * zcos[1] + xt[1] * zsin[0] * zsin[1] - xab[1]
							* zcos[0]);
			x[2] = xa[2]
					+ zv[0]
					* (xu[2] * zsin[0] * zcos[1] + xt[2] * zsin[0] * zsin[1] - xab[2]
							* zcos[0]);
			atom.moveTo(x);
			return;
		}
		// General case - with two angles
		if (Math.abs(zi[3]) == 1) {
			atoms.get(zi[0] - 1).getXYZ(xa);
			atoms.get(zi[1] - 1).getXYZ(xb);
			atoms.get(zi[2] - 1).getXYZ(xc);
			VectorMath.diff(xb, xa, xba);
			VectorMath.norm(xba, xba);
			VectorMath.diff(xa, xc, xac);
			VectorMath.norm(xac, xac);
			xt[0] = xba[2] * xac[1] - xba[1] * xac[2];
			xt[1] = xba[0] * xac[2] - xba[2] * xac[0];
			xt[2] = xba[1] * xac[0] - xba[0] * xac[1];
			cosine = xba[0] * xac[0] + xba[1] * xac[1] + xba[2] * xac[2];
			sine2 = Math.max(1.0d - cosine * cosine, eps);
			if (Math.abs(cosine) >= 1.0d) {
				logger.warning("Defining Atom Colinear");
			}
			a = (-zcos[1] - cosine * zcos[0]) / sine2;
			b = (zcos[0] + cosine * zcos[1]) / sine2;
			c = (1.0d + a * zcos[1] - b * zcos[0]) / sine2;
			if (c > eps) {
				c = zi[3] * Math.sqrt(c);
			} else if (c < -eps) {
				c = Math.sqrt((a * xac[0] + b * xba[0])
						* (a * xac[0] + b * xba[0]) + (a * xac[1] + b * xba[1])
						* (a * xac[1] + b * xba[1]) + (a * xac[2] + b * xba[2])
						* (a * xac[2] + b * xba[2]));
				a /= c;
				b /= c;
				c = 0.0d;
			} else {
				c = 0.0d;
			}
			x[0] = xa[0] + zv[0] * (a * xac[0] + b * xba[0] + c * xt[0]);
			x[1] = xa[1] + zv[0] * (a * xac[1] + b * xba[1] + c * xt[1]);
			x[2] = xa[2] + zv[0] * (a * xac[2] + b * xba[2] + c * xt[2]);
			atom.moveTo(x);
		}
	}

	/*
	 * Parse the INT File
	 * 
	 * @return Returns true on successful read, false otherwise
	 */
	public boolean readFile() {
		Logger logger = Logger.getLogger("ffe");
		File intFile = molecularAssembly.getFile();
		if (forceField == null) {
			logger.warning("No force field is associated with "
					+ intFile.toString());
			return false;
		}
		// Open a data stream to the Internal Coordinate file
		try {
			FileReader fr = new FileReader(intFile);
			BufferedReader br = new BufferedReader(fr);
			String data = br.readLine().trim();
			// Read blank lines at the top of the file
			while (data != null && data.length() == 0) {
				data = br.readLine().trim();
			}
			if (data == null) {
				logger.warning("Empty file: " + intFile.toString());
				return false;
			}
			int numberOfAtoms;
			String tokens[] = data.trim().split("\\s+");
			try {
				numberOfAtoms = Integer.parseInt(tokens[0]);
				if (numberOfAtoms < 1) {
					logger.warning("Invalid number of atoms: " + numberOfAtoms);
					return false;
				}
			} catch (Exception e) {
				logger.severe("Error parsing the number of atoms.\n" + e);
				return false;
			}
			if (tokens.length >= 2) {
				tokens = data.trim().split(" +", 2);
				molecularAssembly.setName(tokens[1]);
			}
			logger.info("  Opening " + intFile.getName() + " with "
					+ numberOfAtoms + " atoms");
			double d[] = { 0.0d, 0.0d, 0.0d };
			int zi[][] = new int[numberOfAtoms][4];
			double zv[][] = new double[numberOfAtoms][3];
			Vector<int[]> zadd = new Vector<int[]>();
			Vector<int[]> zdel = new Vector<int[]>();
			atomList = new ArrayList<Atom>();
			for (int i = 0; i < numberOfAtoms; i++) {
				// Atom Data
				if (!br.ready()) {
					return false;
				}
				data = br.readLine();
				if (data == null) {
					logger.severe("  Check atom " + (i + 1) + " in "
							+ molecularAssembly.getFile().getName());
					return false;
				}
				tokens = data.trim().split("\\s+");
				if (tokens == null || tokens.length < 3) {
					logger.severe("  Check atom " + (i + 1) + " in "
							+ molecularAssembly.getFile().getName());
					return false;
				}
				// Atom number, name, type
				String name = tokens[1];
				int type = Integer.parseInt(tokens[2]);
				AtomType atomType = (AtomType) forceField.getForceFieldType(
						ForceField.ForceFieldType.ATOM, String.valueOf(type));
				if (atomType == null) {
					logger.severe("  Check atom " + (i + 1) + " in "
							+ molecularAssembly.getFile().getName());
					return false;
				}
				Atom a = new Atom(i + 1, name, atomType, d);
				atomList.add(a);
				// Bond partner and bond value
				if (tokens.length >= 5) {
					zi[i][0] = Integer.parseInt(tokens[3]);
					zv[i][0] = Double.parseDouble(tokens[4]);
				} else {
					zi[i][0] = 0;
					zv[i][0] = 0.0d;
				}
				// Angle partner and angle value
				if (tokens.length >= 7) {
					zi[i][1] = Integer.parseInt(tokens[5]);
					zv[i][1] = Double.parseDouble(tokens[6]);
				} else {
					zi[i][1] = 0;
					zv[i][1] = 0.0d;
				}
				// Torsion partner and dihedral value
				if (tokens.length >= 10) {
					zi[i][2] = Integer.parseInt(tokens[7]);
					zv[i][2] = Double.parseDouble(tokens[8]);
					zi[i][3] = Integer.parseInt(tokens[9]);
				} else {
					zi[i][2] = 0;
					zv[i][2] = 0.0d;
					zi[i][3] = 0;
				}
			}
			if (br.ready()) {
				data = br.readLine();
				// Check for a first blank line
				if (data.trim().equalsIgnoreCase("")) {
					// Parse bond pairs to add until EOF or a blank line is
					// reached
					boolean blank = false;
					while (br.ready() && !blank) {
						data = br.readLine();
						if (data.trim().equalsIgnoreCase("")) {
							blank = true;
						} else {
							tokens = data.trim().split("\\s+");
							if (tokens.length != 2) {
								logger
										.severe("  Check Additional Bond Pair: "
												+ (zadd.size() + 1)
												+ " in "
												+ molecularAssembly.getFile()
														.getName());
								return false;
							}
							int pair[] = new int[2];
							pair[0] = Integer.parseInt(tokens[0]);
							pair[1] = Integer.parseInt(tokens[1]);
							zadd.add(pair);
						}
					}
					// Parse bond pairs to be removed until EOF
					while (br.ready()) {
						data = br.readLine();
						tokens = data.trim().split("\\s+");
						if (tokens.length != 2) {
							logger.severe("  Check Bond Pair to Remove: "
									+ (zadd.size() + 1) + " in "
									+ molecularAssembly.getFile().getName());
							return false;
						}
						int pair[] = new int[2];
						pair[0] = Integer.parseInt(tokens[0]);
						pair[1] = Integer.parseInt(tokens[1]);
						zdel.add(pair);
					}
				}
			}
			br.close();
			fr.close();
			if (atomList.size() == numberOfAtoms) {
				// Add bonds specified in the Z-matrix
				bondList = new ArrayList<Bond>();
				for (int i = 1; i < numberOfAtoms; i++) {
					int partner = zi[i][0];
					boolean del = false;
					for (int j = 0; j < zdel.size(); j++) {
						int pair[] = zdel.get(j);
						if (pair[0] == i + 1 && pair[1] == partner) {
							del = true;
						}
						if (pair[1] == i + 1 && pair[0] == partner) {
							del = true;
						}
					}
					if (!del) {
						Atom atom1 = atomList.get(i);
						Atom atom2 = atomList.get(partner - 1);
						bondList.add(new Bond(atom1, atom2, 1));
					}
				}
				// Add additional bonds
				for (int i = 0; i < zadd.size(); i++) {
					int pair[] = zadd.get(i);
					Atom atom1 = atomList.get(pair[0] - 1);
					Atom atom2 = atomList.get(pair[1] - 1);
					bondList.add(new Bond(atom1, atom2, 1));
				}
				// Determine coordinates from Z-matrix values
				for (int i = 0; i < numberOfAtoms; i++) {
					intxyz(atomList, atomList.get(i), zi[i], zv[i]);
				}
				return true;
			}
			logger.warning("Reported number of Atoms: " + numberOfAtoms
					+ "\nNumber of Atoms Found: " + atomList.size());
		} catch (IOException e) {
			logger.severe(e.toString());
		}
		return false;
	}

	public boolean writeFile() {
		/*
		 * File xyzfile = getFile(); if (xyzfile == null) { return false; } try {
		 * FileWriter fw = new FileWriter(xyzfile); BufferedWriter bw = new
		 * BufferedWriter(fw);
		 *  // XYZ File First Line FSystem M = getFSystem(); int numatoms =
		 * M.getAtomList().size(); String blanks = new String(" ");
		 * 
		 * int len = (new String("" + numatoms)).length();
		 * bw.write(blanks.substring(0, 6 - len) + numatoms + " " + M.toString() +
		 * "\n");
		 * 
		 * Atom a, a2; Bond b; ArrayList bonds; StringBuffer line; StringBuffer
		 * lines[] = new StringBuffer[numatoms]; String indexS, id, type, xS,
		 * yS, zS; int xi, yi, zi;
		 *  // XYZ File Atom Lines List atoms = M.getAtomList(); Vector3d offset =
		 * M.getOffset(); for (ListIterator li = atoms.listIterator();
		 * li.hasNext(); ) { a = (Atom) li.next(); indexS = new String("" +
		 * a.getXYZIndex()); line = new StringBuffer(blanks.substring(0, 6 -
		 * indexS.length()) + indexS + " "); id = a.getID(); line.append(id +
		 * blanks.substring(0, 3 - id.length())); xS =
		 * formatCoord.format(a.getX() - offset.x); yS =
		 * formatCoord.format(a.getY() - offset.y); zS =
		 * formatCoord.format(a.getZ() - offset.z);
		 * line.append(blanks.substring(0, 12 - xS.length()) + xS);
		 * line.append(blanks.substring(0, 12 - yS.length()) + yS);
		 * line.append(blanks.substring(0, 12 - zS.length()) + zS); type = new
		 * String("" + a.getAtomType()); line.append(blanks.substring(0, 6 -
		 * type.length()) + type); bonds = a.getBonds(); if (bonds != null) {
		 * for (ListIterator lj = bonds.listIterator(); lj.hasNext(); ) { b =
		 * (Bond) lj.next(); a2 = b.getOtherAtom(a); xS =
		 * formatBond.format(a2.xyzindex); line.append(blanks.substring(0, 6 -
		 * xS.length()) + xS); } lines[a.getXYZIndex() - 1] = line.append("\n"); } }
		 * for (int i = 0; i < numatoms; i++) { try {
		 * bw.write(lines[i].toString()); } catch (Exception e) {
		 * System.out.println("" + i); } } bw.close(); fw.close(); } catch
		 * (IOException e) { System.out.println("" + e); return false; } return
		 * true;
		 */
		return false;
	}
}
