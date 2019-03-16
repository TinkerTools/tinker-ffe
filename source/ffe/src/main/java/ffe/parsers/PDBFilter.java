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
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Hashtable;
import java.util.Vector;
import java.util.ArrayList;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import ffe.lang.Atom;
import ffe.lang.MolecularAssembly;
import ffe.lang.Polymer;
import ffe.lang.Residue;
import ffe.lang.Utilities.FileType;
import ffe.mm.AtomType;

/*
 * The PDBFilter class parses data from a Protein DataBank (*.PDB) file.
 * The following records are recognized: HEADER, ATOM, HETATM, LINK,
 * SSBOND, HELIX, SHEET, TURN. The rest are ignored.
 */
public final class PDBFilter extends SystemFilter {
	private static Pattern p = Pattern.compile("\\d");

	/*
	 * Create some default atom types for PDB files.
	 */
	static Hashtable<String, AtomType> atomTypes = new Hashtable<String, AtomType>();

	static {
		atomTypes.put("H", new AtomType(1, -1, "H", "Hydrogen", 1, 12.0, 4));
		atomTypes.put("C", new AtomType(6, -1, "C", "Carbon", 6, 12.0, 4));
		atomTypes.put("N", new AtomType(7, -1, "N", "Nitrogen", 7, 14.0, 3));
		atomTypes.put("O", new AtomType(8, -1, "O", "Oxygen", 8, 16.0, 2));
		atomTypes
				.put("P", new AtomType(15, -1, "P", "Phosphorus", 15, 31.0, 4));
		atomTypes.put("S", new AtomType(16, -1, "S", "Sulfur", 16, 32.0, 2));
		atomTypes.put("X", new AtomType(0, -1, "X", "X", 3, 1.0, 4));
	}

	public static AtomType getAtomType(String s) {
		String c = s.toLowerCase();
		if (c.startsWith("h")) {
			return atomTypes.get("H");
		}
		if (c.startsWith("c")) {
			return atomTypes.get("C");
		}
		if (c.startsWith("n")) {
			return atomTypes.get("N");
		}
		if (c.startsWith("o")) {
			return atomTypes.get("O");
		}
		if (c.startsWith("p")) {
			return atomTypes.get("P");
		}
		if (c.startsWith("s")) {
			return atomTypes.get("S");
		}
		if (p.matcher(s.substring(0, 1)).matches()) {
			return getAtomType(s.substring(1, s.length()));
		}
		return atomTypes.get("X");
	}

	private String pdbURL = null;

	/*
	 * Default Constructor
	 */
	public PDBFilter() {
		super();
	}

	/*
	 * Parse the PDB File from a local disk
	 */
	public PDBFilter(MolecularAssembly f) {
		super(f);
	}

	/*
	 * Parse the PDB File from a URL
	 */
	public PDBFilter(MolecularAssembly f, String pdb) {
		super(f);
		pdbURL = pdb;
	}

	/*
	 * Parse the PDB File
	 */
	public boolean readFile() {
		FileWriter fw = null;
		BufferedReader br = null;
		BufferedWriter bw = null;
		Logger logger = Logger.getLogger("ffe");
		molecularAssembly.setFileType(FileType.PDB);
		try {
			setFileRead(false);
			if (pdbURL == null) {
				// Open a data stream to the PDB file
				File pdbFile = molecularAssembly.getFile();
				if (pdbFile == null || !pdbFile.exists() || !pdbFile.canRead()) {
					return false;
				}
				FileReader fr = new FileReader(pdbFile);
				br = new BufferedReader(fr);
				logger.info("  Opening " + pdbFile.getName());
			} else {
				try {
					URL url = new URL(pdbURL);
					br = new BufferedReader(new InputStreamReader(url
							.openStream()));
					int retry = 0;
					while (!br.ready() && retry < 10) {
						synchronized (this) {
							logger.info("Waiting on Network");
							wait(50);
							retry++;
						}
					}
				} catch (Exception e) {
					logger.severe(e.toString() + "\n");
					return false;
				}
				// The downloaded PBD file will be echoed to the local file
				// system (if the file does not already exist)
				File pdbFile = molecularAssembly.getFile();
				if (pdbFile != null && !pdbFile.exists()) {
					fw = new FileWriter(pdbFile);
					bw = new BufferedWriter(fw);
					logger.info("  Opening " + pdbFile.getName());
				}
			}
			String rawdata = br.readLine();
			// First atom is #1, to match xyz file format
			int atomnum = 1;
			Atom a = null;
			double[] d = new double[3];
			String altloc, type, polymerType, residueType;
			int residueNum;
			AtomType atomType;
			boolean link = false;
			String[] connect;
			ArrayList<String[]> links = new ArrayList<String[]>();
			Vector<String[]> structs = new Vector<String[]>();
			String[] struct;
			Residue residue = null;
			Polymer polymer = null;
			String identity;
			// While the END parameter is not read in, load atoms
			while ((rawdata != null) && (!rawdata.startsWith("END"))) {
				int len = rawdata.length();
				if (len > 6) {
					len = 6;
				}
				identity = rawdata.substring(0, len).trim().toUpperCase()
						.intern();
				if (identity == "ATOM" || identity == "HETATM") {
					altloc = rawdata.substring(16, 17).toUpperCase();
					if (!altloc.equals(" ")) {
						molecularAssembly.addAltLocation(altloc);
					}
					if (altloc.equals(" ") || altloc.equals("A")) {
						type = rawdata.substring(12, 16).trim().intern();
						residueType = rawdata.substring(17, 20).trim().intern();
						residueNum = Integer.decode(
								rawdata.substring(22, 26).trim()).intValue();
						polymerType = rawdata.substring(21, 22).intern();
						if (polymerType.equalsIgnoreCase(" ")) {
							polymerType = "Blank".intern();
						}
						d[0] = Double.valueOf(rawdata.substring(30, 38).trim())
								.doubleValue();
						d[1] = Double.valueOf(rawdata.substring(38, 46).trim())
								.doubleValue();
						d[2] = Double.valueOf(rawdata.substring(46, 54).trim())
								.doubleValue();
						atomType = getAtomType(type);
						a = new Atom(atomnum++, type, atomType, d, residueType,
								residueNum, polymerType);
						if (residue != null && polymer != null
								&& residue.getResidueNumber() == residueNum
								&& residue.getName() == residueType
								&& polymer.getName() == polymerType) {
							residue.addMSNode(a);
						} else {
							molecularAssembly.addMSNode(a);
							residue = (Residue) a.getMSNode(Residue.class);
							polymer = (Polymer) a.getMSNode(Polymer.class);
						}
					}
				} else if (identity == "CONECT") {
					link = true;
					connect = new String[2];
					connect[0] = new String(rawdata.substring(7, 11).trim());
					connect[1] = new String(rawdata.substring(12, 16).trim());
					links.add(connect);
				} else if (identity == "SSBOND") {
					link = true;
					connect = new String[6];
					connect[0] = new String(rawdata.substring(15, 16));
					// Polymers
					connect[1] = new String(rawdata.substring(29, 30));
					connect[2] = new String(rawdata.substring(17, 21).trim());
					// Residues
					connect[3] = new String(rawdata.substring(31, 35).trim());
					connect[4] = new String("SG");
					// Atoms
					connect[5] = new String("SG");
					links.add(connect);
				} else if (identity == "LINK") {
					link = true;
					connect = new String[6];
					connect[0] = new String(rawdata.substring(21, 22));
					// Polymers
					connect[1] = new String(rawdata.substring(51, 52));
					connect[2] = new String(rawdata.substring(22, 26).trim());
					// Residues
					connect[3] = new String(rawdata.substring(52, 56).trim());
					connect[4] = new String(rawdata.substring(12, 16).trim());
					// Atoms
					connect[5] = new String(rawdata.substring(42, 46).trim());
					links.add(connect);
				} else if (identity == "HELIX") {
					struct = new String[6];
					struct[0] = new String(rawdata.substring(0, 6).trim()); // HELIX
					struct[1] = new String(rawdata.substring(19, 20)); // Polymers
					struct[2] = new String(rawdata.substring(31, 32));
					struct[3] = new String(rawdata.substring(21, 25).trim()); // Residue
					struct[4] = new String(rawdata.substring(33, 37).trim());
					struct[5] = new String(rawdata.substring(38, 40).trim()); // Helix
					// Type
					structs.add(struct);
				} else if (identity == "SHEET") {
					struct = new String[6];
					struct[0] = new String(rawdata.substring(0, 6).trim()); // SHEET
					struct[1] = new String(rawdata.substring(21, 22)); // Polymers
					struct[2] = new String(rawdata.substring(32, 33));
					struct[3] = new String(rawdata.substring(22, 26).trim()); // Residue
					struct[4] = new String(rawdata.substring(33, 37).trim());
					struct[5] = new String(rawdata.substring(38, 40).trim()); // Strand
					// Sense
					structs.add(struct);
				} else if (identity == "TURN") {
					struct = new String[6];
					struct[0] = new String(rawdata.substring(0, 6).trim()); // TURN
					struct[1] = new String(rawdata.substring(19, 20)); // Polymers
					struct[2] = new String(rawdata.substring(30, 31));
					struct[3] = new String(rawdata.substring(20, 24).trim()); // Residue
					struct[4] = new String(rawdata.substring(31, 35).trim());
					structs.add(struct);
				}
				if (bw != null) {
					bw.write(rawdata);
					bw.newLine();
				}
				rawdata = br.readLine();
			}
			if (bw != null) {
				bw.flush();
				bw.close();
			}
			br.close();
			atomnum--;
			logger.info("  Read " + atomnum + " atoms");
			// Assign Secondary Structure Based on PDB Info
			for (String[] s : structs) {
				if (s[1].equalsIgnoreCase(" ")) {
					s[1] = "Blank".intern();
				}
				Polymer p = molecularAssembly.getPolymer(s[1], false);
				if (p != null) {
					for (int i = Integer.parseInt(s[3]); i <= Integer
							.parseInt(s[4]); i++) {
						Residue r = p.getResidue(i);
						if (r != null) {
							r.setSSType(Residue.SSType.valueOf(s[0]));
						}
					}
				}
			}
			setFileRead(true);
		} catch (IOException e) {
			logger.severe("Exception opening: "
					+ molecularAssembly.getFile().getName());
			return false;
		}
		return true;
	}

	public boolean writeFile() {
		return false;
	}
}
