/*
 * <p>Title: Force Field Explorer</p>
 * <p>Description: Force Field Explorer Molecular Modeling Program</p>
 * <p>Copyright: Copyright (c) 2004-2021 Jay William Ponder</p>
 * <p>Institution: Jay Ponder Lab, Washington University in Saint Louis</p>
 * @author Michael J. Schnieders
 * @version 8.10
 */

package ffe.lang;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import ffe.mm.AtomType;
import ffe.mm.ForceField;
import ffe.mm.MultipoleType;
import ffe.mm.ForceField.ForceFieldType;

/*
 * The Utilities class provides methods to locate functional units
 * of an organic system.
 */
public final class Utilities {
	/*
	 * An enumeration of recognized file types.
	 */
	public enum FileType {
		XYZ, INT, ARC, PDB, ANY, SIM, UNK
	}

	/*
	 * An enumeration of recognized organic polymers.
	 */
	public enum PolymerType {
		AMINOACID, NUCLEICACID, UNKNOWN
	}

	/*
	 * The algorithms used to split arrays of atoms into a structural hierarchy
	 * using a bunch of List instances. Pooling them seems like it might be
	 * a performance win, although better algorithms probably exist. This is
	 * currently backed by ArrayLists.
	 */
	public static List<List<Atom>> atomListPool = new ArrayList<List<Atom>>();

	static int count = 0;

	/*
	 * Repeating atomic numbers of an amino acid chain.
	 */
	public static final int AAPATTERN[] = { 7, 6, 6 };

	/*
	 * Repeating atomic numbers of a nucleic acid chain.
	 */
	public static final int NAPATTERN[] = { 8, 6, 6, 6, 8 };

	private static Logger logger = Logger.getLogger("ffe");

	/*
	 * Stoichiometry of side chains can be used for identification, accept for a
	 * couple cases: 1) Proline & Valine 2) Leucine & Isoleucine 3) DNA Gaunine &
	 * RNA Adenine. This Hashtable returns the 3-letter name for amino acids, a
	 * single letter for nucleic acids, or an integer indicating a special case.
	 */
	static private Hashtable<String, String> sidechainStoichiometry = new Hashtable<String, String>();

	/*
	 * Save about 30 * N * 8 bytes of memory per rotateMultipole call by
	 * creating a few static double arrays.
	 */
	private static final double localOrigin[] = new double[3];

	private static final double zaxis[] = new double[3];

	private static final double xaxis[] = new double[3];

	private static final double rotmat[][] = new double[3][3];

	private static final double dipole[] = new double[3];

	private static final double quadrupole[][] = new double[3][3];

	private static final double p4 = 15.236;

	private static final double p5 = 1.254;

	private static final double p5inv = 1.0 / 1.254;

	private static final double pip5 = Math.PI * p5;

	private static final double convert = -332.05382 / 2.0;

	private static final double[] x1 = new double[3];

	private static final double[] x2 = new double[3];

	static {
		// Amino Acid Side Chains
		sidechainStoichiometry.put("S1C3", "MET");
		sidechainStoichiometry.put("S1C1", "CYS");
		sidechainStoichiometry.put("O1C1", "SER");
		sidechainStoichiometry.put("O1C2", "THR");
		sidechainStoichiometry.put("O1C7", "TYR");
		sidechainStoichiometry.put("O2C2", "ASP");
		sidechainStoichiometry.put("O2C3", "GLU");
		sidechainStoichiometry.put("O1N1C2", "ASN");
		sidechainStoichiometry.put("O1N1C3", "GLN");
		sidechainStoichiometry.put("N3C4", "ARG");
		sidechainStoichiometry.put("N2C4", "HIS");
		sidechainStoichiometry.put("N1C9", "TRP");
		sidechainStoichiometry.put("N1C4", "LYS");
		sidechainStoichiometry.put("C7", "PHE");
		sidechainStoichiometry.put("H", "GLY");
		sidechainStoichiometry.put("C1", "ALA");
		// DNA
		sidechainStoichiometry.put("O2N3C6", "DC");
		sidechainStoichiometry.put("O1N5C7", "DA");
		sidechainStoichiometry.put("O3N2C7", "DT");
		// RNA
		sidechainStoichiometry.put("O3N5C7", "G");
		sidechainStoichiometry.put("O3N3C6", "C");
		sidechainStoichiometry.put("O4N2C6", "U");
		// SPECIAL CASES
		sidechainStoichiometry.put("C3", "1"); // Proline / Valine
		sidechainStoichiometry.put("C4", "2"); // (ISO)Leucine
		sidechainStoichiometry.put("O2N5C7", "3"); // DNA Gaunine / RNA Adenine
	}

	public static void addAtomListToPool(List<Atom> a) {
		a.clear();
		atomListPool.add(a);
	}

	/*
	 * Collect all the atoms in a polymer opposite the end atom, and put them
	 * into the residue. This assumes that the "cap" is only linked to the rest
	 * of the polymer through the end atom.
	 * 
	 * @param end
	 *            Atom
	 * @param seed
	 *            Atom
	 * @param residue
	 *            Residue
	 */
	public static void addCap(Atom end, Atom seed, Residue residue) {
		List<Atom> cap = getAtomListFromPool();
		cap.add(end);
		collectAtoms(seed, cap);
		// Assume the end & seed atoms are already part of the residue
		cap.remove(0);
		cap.remove(0);
		for (Atom a : cap) {
			residue.addMSNode(a);
		}
	}

	/*
	 * Add a phosphate and its bonded oxygens that are not bonded
	 * to a carbon to the specified residue.
	 * 
	 * @param phosphate
	 *            Atom
	 * @param residue
	 *            Residue
	 */
	public static void addPhosphate(Atom phosphate, Residue residue) {
		if (phosphate == null) {
			return;
		}
		residue.addMSNode(phosphate);
		for (Bond b : phosphate.getBonds()) {
			Atom oxygen = b.get1_2(phosphate);
			// Add oxygens not bonded to a Carbon
			if (numberOfBondsWith(oxygen, 6) == 0) {
				residue.addMSNode(oxygen);
				// Add hydrogens atoms for protonated oxygen groups
				Atom hydrogen = findBondWith(oxygen, 1);
				if (hydrogen != null) {
					residue.addMSNode(hydrogen);
				}
			}
		}
	}

	private static boolean assignMultipole(Atom atom, ForceField forceField) {
		AtomType atomType = atom.getAtomType();
		if (atomType == null) {
			logger.warning("Multipoles can only be assigned to atoms that have been typed");
			return false;
		}
		MultipoleType multipoleType = null;
		String key = null;
		// No reference atoms.
		key = atomType.key + " 0 0";
		multipoleType = (MultipoleType) forceField.getForceFieldType(
				ForceFieldType.MULTIPOLE, key);
		if (multipoleType != null) {
			atom.setMultipoleType(multipoleType, null);
			return true;
		}
		// No bonds - no soup for you.
		List<Bond> bonds = atom.getBonds();
		if (bonds == null || bonds.size() < 1) {
			logger.warning("Multipoles can only be assigned after bonded relationships are defined");
			return false;
		}
		// 1 reference atom.
		for (Bond b : bonds) {
			Atom atom2 = b.get1_2(atom);
			key = atomType.key + " " + atom2.getAtomType().key + " 0";
			multipoleType = (MultipoleType) forceField.getForceFieldType(
					ForceFieldType.MULTIPOLE, key);
			if (multipoleType != null) {
				Atom multipoleReferenceAtoms[] = new Atom[1];
				multipoleReferenceAtoms[0] = atom2;
				atom.setMultipoleType(multipoleType, multipoleReferenceAtoms);
				return true;
			}
		}
		// 2 reference atoms.
		for (Bond b : bonds) {
			Atom atom2 = b.get1_2(atom);
			String key2 = atom2.getAtomType().key;
			for (Bond b2 : bonds) {
				if (b == b2) {
					continue;
				}
				Atom atom3 = b2.get1_2(atom);
				String key3 = atom3.getAtomType().key;
				key = atomType.key + " " + key2 + " " + key3;
				multipoleType = (MultipoleType) forceField.getForceFieldType(
						ForceFieldType.MULTIPOLE, key);
				if (multipoleType != null) {
					Atom multipoleReferenceAtoms[] = new Atom[2];
					multipoleReferenceAtoms[0] = atom2;
					multipoleReferenceAtoms[1] = atom3;
					atom.setMultipoleType(multipoleType,
							multipoleReferenceAtoms);
					return true;
				}
			}
		}
		// 3 reference atoms (chiral).
		for (Bond b : bonds) {
			Atom atom2 = b.get1_2(atom);
			String key2 = atom2.getAtomType().key;
			for (Bond b2 : bonds) {
				if (b == b2) {
					continue;
				}
				Atom atom3 = b2.get1_2(atom);
				String key3 = atom3.getAtomType().key;
				for (Bond b3 : bonds) {
					if (b == b3 || b2 == b3) {
						continue;
					}
					Atom atom4 = b3.get1_2(atom);
					String key4 = atom4.getAtomType().key;
					key = atomType.key + " " + key2 + " " + key3 + " " + key4;
					multipoleType = (MultipoleType) forceField
							.getForceFieldType(ForceFieldType.MULTIPOLE, key);
					if (multipoleType != null) {
						Atom multipoleReferenceAtoms[] = new Atom[3];
						multipoleReferenceAtoms[0] = atom2;
						multipoleReferenceAtoms[1] = atom3;
						multipoleReferenceAtoms[2] = atom4;
						atom.setMultipoleType(multipoleType,
								multipoleReferenceAtoms);
						return true;
					}
				}
				List<Angle> angles = atom.getAngles();
				for (Angle angle : angles) {
					Atom atom4 = angle.get1_3(atom);
					if (atom4 != null) {
						String key4 = atom4.getAtomType().key;
						key = atomType.key + " " + key2 + " " + key3 + " "
								+ key4;
						multipoleType = (MultipoleType) forceField
								.getForceFieldType(ForceFieldType.MULTIPOLE,
										key);
						if (multipoleType != null) {
							Atom multipoleReferenceAtoms[] = new Atom[3];
							multipoleReferenceAtoms[0] = atom2;
							multipoleReferenceAtoms[1] = atom3;
							multipoleReferenceAtoms[2] = atom4;
							atom.setMultipoleType(multipoleType,
									multipoleReferenceAtoms);
							return true;
						}
					}
				}
			}
		}
		// Revert to a 2 reference atom definition that may include a 1-3 site.
		// For example a hydrogen on water
		for (Bond b : bonds) {
			Atom atom2 = b.get1_2(atom);
			String key2 = atom2.getAtomType().key;
			List<Angle> angles = atom.getAngles();
			for (Angle angle : angles) {
				Atom atom3 = angle.get1_3(atom);
				if (atom3 != null) {
					String key3 = atom3.getAtomType().key;
					key = atomType.key + " " + key2 + " " + key3;
					multipoleType = (MultipoleType) forceField
							.getForceFieldType(ForceFieldType.MULTIPOLE, key);
					if (multipoleType != null) {
						Atom multipoleReferenceAtoms[] = new Atom[2];
						multipoleReferenceAtoms[0] = atom2;
						multipoleReferenceAtoms[1] = atom3;
						atom.setMultipoleType(multipoleType,
								multipoleReferenceAtoms);
						return true;
					}
				}
			}
		}
		return false;
	}

	/*
	 * Given an array of atoms (with atom types), assign
	 * multipole types and reference sites
	 * 
	 * @param atoms
	 *            List
	 * @param forceField
	 *            ForceField
	 */
	public static void assignMultipoles(List<Atom> atoms, ForceField forceField) {
		if (forceField == null) {
			logger.warning("Could not assign multipoles due to null ForceField");
			return;
		}
		if (forceField.getForceFieldTypeCount(ForceFieldType.MULTIPOLE) < 1) {
			logger.warning("Force Field has no multipole types");
			return;
		}
		if (atoms == null || atoms.size() < 1) {
			logger.warning("Could not assign multipoles due to null atom list");
			return;
		}
		for (Atom atom : atoms) {
			if (!assignMultipole(atom, forceField)) {
				logger.warning("No multipole could be assigned to atom:\n"
						+ atom + "\nOf type:\n" + atom.getAtomType());
			} else {
				if (logger.isLoggable(Level.FINEST)) {
					Atom ref[] = atom.getMultipoleReferenceSites();
					StringBuffer refBuffer = new StringBuffer();
					if (ref != null) {
						refBuffer.append("\nReference Sites:\n");
						for (Atom a : ref) {
							refBuffer.append(a.toString() + " "
									+ a.getAtomType().toString() + "\n");
						}
					}
					logger.finest("Atom " + atom.toString() + "\nof type: "
							+ atom.getAtomType().toString()
							+ "\nassigned multipole:\n"
							+ atom.getMultipoleType().toString()
							+ refBuffer.toString());
				}
			}
		}
	}

	private static Residue assignResidue(List<Atom> backbone, int start,
			List<Atom> atoms, List<Atom> sidePolymer) {
		Atom a;
		int atomicnum;
		int bins[] = new int[5]; // 0 = S, 1 = P, 2 = O, 3 = N, 4 = C
		char chars[] = { 'S', 'P', 'O', 'N', 'C' };
		for (ListIterator li = sidePolymer.listIterator(); li.hasNext();) {
			a = (Atom) li.next();
			atomicnum = a.getAtomicNumber();
			switch (atomicnum) {
			case 1:
				// ignore hydrogens
				break;
			case 6:
				// Carbon
				bins[4]++;
				break;
			case 7:
				// Nitrogen
				bins[3]++;
				break;
			case 8:
				// Oxygen
				bins[2]++;
				break;
			case 15:
				// Phosphorus
				bins[1]++;
				break;
			case 16:
				// Sulfur
				bins[0]++;
				break;
			default:
				return null;
			}
		}
		StringBuffer key = new StringBuffer();
		int atomCount = 0;
		for (int i = 0; i < 5; i++) {
			if (bins[i] != 0) {
				atomCount += bins[i];
				key.append(chars[i]);
				key.append(Integer.toString(bins[i]));
			}
		}
		if (atomCount == 0) {
			key.append("H"); // Glycine
		}
		String resname = sidechainStoichiometry.get(key.toString());
		if (resname == null) {
			resname = "Unknown";
		} else {
			resname = resname.intern();
		}
		if (resname == "1" || resname == "2") {
			// Special case where atom string keys aren't unique
			Atom alpha = backbone.get(start + 1);
			Atom carbonyl = backbone.get(start + 2);
			Atom beta = null;
			List alphabonds = alpha.getBonds();
			Bond abond;
			for (ListIterator li = alphabonds.listIterator(); li.hasNext();) {
				abond = (Bond) li.next();
				beta = abond.get1_2(alpha);
				// Don't want the peptide nitrogen or alpha hydrogen or carbonyl
				// carbon
				if (beta.getAtomicNumber() != 7 && beta.getAtomicNumber() != 1
						&& beta != carbonyl) {
					break;
				}
				beta = null;
			}
			if (beta == null) {
				return null;
			}
			List betabonds = beta.getBonds();
			Atom gamma;
			int carboncount = 0;
			for (ListIterator li = betabonds.listIterator(); li.hasNext();) {
				abond = (Bond) li.next();
				gamma = abond.get1_2(beta);
				if (gamma.getAtomicNumber() == 6) {
					carboncount++;
				}
			}
			if (resname == "1") {
				if (carboncount == 2) {
					resname = "PRO";
				} else {
					resname = "VAL";
				}
			} else {
				if (carboncount == 2) {
					resname = "LEU";
				} else {
					resname = "ILE";
				}
			}
		} else if (resname == "3") {
			Atom c3 = backbone.get(start + 3);
			int num = countCO(c3);
			if (num == 2) {
				resname = "A";
			} else {
				resname = "DG";
			}
		}
		Residue residue = null;
		try {
			Residue.NA3.valueOf(resname.toUpperCase());
			residue = new Residue(resname, true, Residue.ResidueType.NA);
		} catch (Exception e) {
		}
		if (residue == null) {
			try {
				Residue.AA3.valueOf(resname.toUpperCase());
				residue = new Residue(resname, true, Residue.ResidueType.AA);
			} catch (Exception e) {
			}
		}
		if (residue == null) {
			residue = new Residue(resname, true, Residue.ResidueType.UNK);
		}
		// Create the Residue group
		for (ListIterator li = atoms.listIterator(); li.hasNext();) {
			a = (Atom) li.next();
			residue.addMSNode(a);
		}
		return residue;
	}

	/*
	 * This routine sub-divides a system into groups of ions, water,
	 * heteromolecules, and polynucleotides/polypeptides.
	 * 
	 * @param m
	 * @param atoms
	 */
	public static void biochemistry(MolecularAssembly m, List<Atom> atoms) {
		Atom atom, seed = null;
		int num = 0;
		int waterNum = 0;
		int ionNum = 0;
		int heteroNum = 0;

		while (atoms.size() > 0) {
			// Nitrogens are used to "seed" a backbone search rather than carbon
			// because a carbon can be separated from the backbone by a sulfur
			// (ie, in MET residues)
			for (Atom a : atoms) {
				seed = a;
				if (seed.getAtomicNumber() == 7) {
					break;
				}
			}
			// No more nitrogens, so no DNA/RNA/Protein molecules are left
			if (seed.getAtomicNumber() != 7) {
				List<Atom> resatoms;
				while (atoms.size() > 0) {
					atom = atoms.get(0);
					// Check for a metal ion or noble gas
					if (atom.getNumBonds() == 0) {
						ionNum++;
						Molecule ion = new Molecule(atom.getName() + ": "
								+ ionNum, true);
						ion.addMSNode(atom);
						atoms.remove(0);
						m.addMSNode(ion);
						continue;
					}
					// Check for water
					else if (atom.getAtomicNumber() == 8 && isWaterOxygen(atom)) {
						waterNum++;
						Molecule water = new Molecule("H2O: " + waterNum, true);
						water.addMSNode(atom);
						atoms.remove(0);
						List<Bond> bonds = atom.getBonds();
						for (Bond b : bonds) {
							Atom o = b.get1_2(atom);
							water.addMSNode(o);
							atoms.remove(o);
						}
						m.addMSNode(water);
						continue;
					}
					// Otherwise classify the molecule as a hetero
					heteroNum++;
					Molecule hetero = new Molecule("Hetero: " + heteroNum, true);
					resatoms = getAtomListFromPool();
					collectAtoms(atoms.get(0), resatoms);
					while (resatoms.size() > 0) {
						atom = resatoms.get(0);
						resatoms.remove(0);
						hetero.addMSNode(atom);
						atoms.remove(atom);
					}
					hetero.setName(String.format("Hetero %7.1f: %6d", hetero
							.getMW(), heteroNum));
					m.addMSNode(hetero);
				}
				seed = null;
				break;
			}
			List<Atom> backbone = findPolymer(atoms, seed, null);
			if (backbone.size() > 0) {
				for (ListIterator li = backbone.listIterator(backbone.size()); li
						.hasPrevious();) {
					seed = (Atom) li.previous();
					if (seed.getAtomicNumber() == 7) {
						break;
					}
				}
				backbone = findPolymer(atoms, seed, null);
			}
			Polymer c = new Polymer(polymerLookup(num), true, true);
			if (backbone.size() > 2 && divideBackbone(backbone, c)) {
				for (Atom a : c.getAtomList()) {
					atoms.remove(a);
				}
				logger.info("Sequenced Chain: " + c.getName());
				m.addMSNode(c);
				num++;
			} else {
				heteroNum++;
				Molecule hetero = new Molecule("Hetero: " + heteroNum, true);
				atom = backbone.get(0);
				List<Atom> heteroAtomList = getAtomListFromPool();
				collectAtoms(atom, heteroAtomList);
				for (Atom a : heteroAtomList) {
					hetero.addMSNode(a);
				}
				for (Atom a : hetero.getAtomList()) {
					atoms.remove(a);
				}
				hetero.setName(String.format("Hetero %7.1f: %6d", hetero
						.getMW(), heteroNum));
				m.addMSNode(hetero);
			}
		}
	}

	/*
	 * Given an array of bonded atoms, this function recursively
	 * collects all other connected atoms, without backtracking
	 * over atoms already in the list.
	 * 
	 * Disulfide bonds are not crossed. (the intent is to search
	 * along a peptide backbone)
	 * 
	 * Atoms preloaded into the List provide search termination.
	 * 
	 * @param seed
	 *            Atom
	 * @param atoms
	 *            List
	 */
	private static void collectAtoms(Atom seed, List<Atom> atoms) {
		if (seed == null) {
			return;
		}
		atoms.add(seed);
		for (Bond b : seed.getBonds()) {
			Atom nextAtom = b.get1_2(seed);
			if (nextAtom.getParent() != null) {
				continue;
			}
			// avoid crossing disulfides
			if ((nextAtom.getAtomicNumber() != 16 || seed.getAtomicNumber() != 16)
					&& !atoms.contains(nextAtom)) {
				collectAtoms(nextAtom, atoms);
			}
		}
	}

	public static int countCO(Atom adjacent) {
		int total = 0;
		for (Bond b : adjacent.getBonds()) {
			Atom carbonyl = b.get1_2(adjacent);
			if (carbonyl.getAtomicNumber() == 6) {
				for (Bond b2 : carbonyl.getBonds()) {
					Atom oxygen = b2.get1_2(carbonyl);
					if (oxygen.getAtomicNumber() == 8) {
						total++;
					}
				}
			}
		}
		return total;
	}

	public static boolean divideBackbone(List<Atom> backbone, Polymer c) {
		int length = backbone.size();
		// Try to find a Phosphorus or Nitrogen in the backbone
		int n, p;
		n = p = 0;
		for (Atom match : backbone) {
			int an = match.getAtomicNumber();
			if (an == 15) {
				p++;
			} else if (an == 7) {
				n++;
			}
		}
		PolymerType type;
		if (p >= n && p > 1) {
			type = PolymerType.NUCLEICACID;
		} else if (n > p && n > 2) {
			type = PolymerType.AMINOACID;
		} else {
			return false;
		}
		int start = -1;
		for (int i = 0; i < length; i++) {
			Residue res = patternMatch(i, backbone, type);
			if (res != null) {
				for (Atom a : res.getAtomList()) {
					a.setParent(null);
				}
				if (!(res.getName() == "Unknown")) {
					start = i;
					// Want 5' to 3'
					if (type == PolymerType.NUCLEICACID) {
						Atom carbon5 = backbone.get(start + 1);
						if (numberOfBondsWith(carbon5, 6) != 1) {
							start = -1;
						}
					}
					break;
				}
			}
		}
		if (start == -1) {
			backbone = reverseAtomList(backbone);
			for (int i = 0; i < length; i++) {
				Residue res = patternMatch(i, backbone, type);
				if (res != null) {
					for (Atom a : res.getAtomList()) {
						a.setParent(null);
					}
					if (!(res.getName() == "Unknown")) {
						start = i;
						break;
					}
				}
			}
		}
		if (start == -1) {
			return false;
		}
		// Potential Polypeptide
		if (type == PolymerType.AMINOACID) {
			Atom nitrogen, alpha, carbonyl = null;
			Atom nitrogen2, carbonyl2;
			Residue aa = null;
			int lastRes = 0;
			int firstRes = -1;
			List<Residue> aaArray = new ArrayList<Residue>();
			while (start < length) {
				aa = patternMatch(start, backbone, PolymerType.AMINOACID);
				if (aa != null) {
					if (firstRes == -1) {
						firstRes = start;
						carbonyl = findCarbonyl(backbone.get(start));
					}
					aaArray.add(aa);
					lastRes = start;
				}
				start += 3;
			}
			// Make sure the first residue is found
			aa = null;
			if (carbonyl != null) {
				alpha = findAlphaCarbon(carbonyl);
				if (alpha != null) {
					nitrogen = findBondWith(alpha, 7);
					if (nitrogen != null) {
						nitrogen2 = findBondWith(carbonyl, 7);
						List<Atom> firstAtoms = getAtomListFromPool();
						firstAtoms.add(nitrogen);
						firstAtoms.add(alpha);
						firstAtoms.add(carbonyl);
						firstAtoms.add(nitrogen2);
						aa = patternMatch(0, firstAtoms, PolymerType.AMINOACID);
						addAtomListToPool(firstAtoms);
						if (aa != null) {
							addCap(alpha, nitrogen, aa);
							aaArray.add(0, aa);
						}
					}
				}
			}
			// Add the remaining atoms to the end of the Polymer
			if (aa == null) {
				nitrogen = backbone.get(firstRes);
				alpha = backbone.get(firstRes + 1);
				addCap(alpha, nitrogen, aaArray.get(0));
			}
			// Make sure the last residue is found
			aa = null;
			carbonyl = findCarbonyl(backbone.get(lastRes + 1));
			if (carbonyl != null) {
				nitrogen = findBondWith(carbonyl, 7);
				if (nitrogen != null) {
					alpha = findAlphaCarbon(nitrogen);
					if (alpha != null) {
						carbonyl2 = findCarbonyl(alpha);
						if (carbonyl2 != null) {
							List<Atom> lastAtoms = getAtomListFromPool();
							lastAtoms.add(carbonyl);
							lastAtoms.add(nitrogen);
							lastAtoms.add(alpha);
							lastAtoms.add(carbonyl2);
							aa = patternMatch(1, lastAtoms,
									PolymerType.AMINOACID);
							addAtomListToPool(lastAtoms);
							if (aa != null) {
								addCap(alpha, carbonyl2, aa);
								aaArray.add(aa);
							}
						}
					}
				}
			}
			if (aa == null) {
				carbonyl = backbone.get(lastRes + 2);
				alpha = backbone.get(lastRes + 1);
				addCap(alpha, carbonyl, aaArray.get(aaArray.size() - 1));
			}
			int index = 1;
			for (Residue r : aaArray) {
				r.setNumber(index++);
				c.addMSNode(r);
			}
			// Potential DNA/RNA
		} else if (type == PolymerType.NUCLEICACID) {
			Residue base;
			int lastRes = 0;
			boolean firstBase = true;
			Atom phos = null;
			Atom oxygen1 = null;
			Atom phosphate1 = null;
			List<Residue> na = new ArrayList<Residue>();
			while (start < length) {
				base = patternMatch(start, backbone, PolymerType.NUCLEICACID);
				if (base != null) {
					phos = backbone.get(start - 1);
					if (phos != null && phos.getAtomicNumber() == 15) {
						addPhosphate(phos, base);
					}
					na.add(base);
					if (firstBase) {
						firstBase = false;
						phosphate1 = backbone.get(start - 1);
						oxygen1 = backbone.get(start);
					}
					lastRes = start;
				}
				start += 6;
			}
			// Make sure the first base is found
			Atom o2, o3;
			Atom c1, c2, c3;
			if (phosphate1 != null && oxygen1 != null) {
				o2 = findOtherOxygen(phosphate1, oxygen1);
				if (o2 != null) {
					c1 = findBondWith(o2, 6);
					if (c1 != null) {
						c2 = findCO(c1);
						if (c2 != null) {
							c3 = findC5(c2);
							if (c3 != null) {
								o3 = findBondWith(c3, 8);
								if (o3 != null) {
									List<Atom> firstAtoms = getAtomListFromPool();
									firstAtoms.add(o3);
									firstAtoms.add(c3);
									firstAtoms.add(c2);
									firstAtoms.add(c1);
									firstAtoms.add(o2);
									firstAtoms.add(phosphate1);
									base = patternMatch(0, firstAtoms, type);
									if (base != null) {
										addCap(c3, o3, base);
										na.add(0, base);
									}
								}
							}
						}
					}
				}
			}
			// Make sure the last base is found
			oxygen1 = backbone.get(lastRes + 4);
			phosphate1 = backbone.get(lastRes + 5);
			if (phosphate1 != null && oxygen1 != null) {
				o2 = findOtherOxygen(phosphate1, oxygen1);
				if (o2 != null) {
					c1 = findBondWith(o2, 6);
					if (c1 != null) {
						c2 = findBondWith(c1, 6);
						if (c2 != null) {
							c3 = findCCO(c2);
							if (c3 != null) {
								o3 = findBondWith(c3, 8);
								if (o3 != null) {
									List<Atom> lastAtoms = getAtomListFromPool();
									lastAtoms.add(phosphate1);
									lastAtoms.add(o2);
									lastAtoms.add(c1);
									lastAtoms.add(c2);
									lastAtoms.add(c3);
									lastAtoms.add(o3);
									base = patternMatch(1, lastAtoms, type);
									if (base != null) {
										addPhosphate(phosphate1, base);
										addCap(c3, o3, base);
										na.add(base);
									}
								}
							}
						}
					}
				}
			}
			int index = 1;
			for (Residue r : na) {
				r.setNumber(index++);
				c.addMSNode(r);
			}
		} else {
			return false;
		}
		return true;
	}

	/*
	 * Returns a carbon that is bonded to the atom a, a carbonyl group,
	 * and a nitrogen.
	 * 
	 * O=C-alpha-N
	 * 
	 * @param a
	 *            Atom
	 * @return Atom
	 */
	public static Atom findAlphaCarbon(Atom a) {
		for (Bond b : a.getBonds()) {
			Atom alpha = b.get1_2(a);
			if (alpha.getAtomicNumber() == 6 && findCO(alpha) != null
					&& formsBondsWith(alpha, 7)) {
				return alpha;
			}
		}
		return null;
	}

	/*
	 * Returns the first atom with the specified atomic number
	 * that bonds with atom a, or null otherwise.
	 * 
	 * @param a
	 *            Atom
	 * @param atomicNumber
	 *            int
	 * @return Atom
	 */
	public static Atom findBondWith(Atom a, int atomicNumber) {
		for (Bond b : a.getBonds()) {
			Atom other = b.get1_2(a);
			if (other.getAtomicNumber() == atomicNumber) {
				return other;
			}
		}
		return null;
	}

	/*
	 * Returns a carbon that is bonded to the adjacent atom,
	 * which bonds 1 carbon and 1 oxygen.
	 * 
	 * @param adjacent
	 *            Atom
	 * @return Atom
	 */
	public static Atom findC5(Atom adjacent) {
		for (Bond b : adjacent.getBonds()) {
			Atom carbon = b.get1_2(adjacent);
			if (carbon.getAtomicNumber() == 6
					&& numberOfBondsWith(carbon, 6) == 1
					&& numberOfBondsWith(carbon, 8) == 1) {
				return carbon;
			}
		}
		return null;
	}

	/*
	 * Returns a carbon that is bonded to the adjacent atom and an oxygen.
	 * 
	 * @param adjacent
	 *            Atom
	 * @return Atom
	 */
	public static Atom findCarbonyl(Atom adjacent) {
		for (Bond b : adjacent.getBonds()) {
			Atom carbonyl = b.get1_2(adjacent);
			if (carbonyl.getAtomicNumber() == 6) {
				for (Bond b2 : carbonyl.getBonds()) {
					Atom oxygen = b2.get1_2(carbonyl);
					if (oxygen.getAtomicNumber() == 8
							&& oxygen.getBonds().size() == 1) {
						return carbonyl;
					}
				}
			}
		}
		return null;
	}

	/*
	 * Returns a carbon that is bonded to the adjacent atom,
	 * which bonds 2 carbons and 1 oxygen.
	 * 
	 * @param adjacent
	 *            Atom
	 * @return Atom
	 */
	public static Atom findCCO(Atom adjacent) {
		for (Bond b : adjacent.getBonds()) {
			Atom carbon = b.get1_2(adjacent);
			if (carbon.getAtomicNumber() == 6
					&& numberOfBondsWith(carbon, 6) == 2
					&& numberOfBondsWith(carbon, 8) >= 1) {
				return carbon;
			}
		}
		return null;
	}

	/*
	 * Returns a carbon that is bonded to the adjacent atom,
	 * which bonds at least 1 oxygen.
	 * 
	 * @param adjacent
	 *            Atom
	 * @return Atom
	 */
	public static Atom findCO(Atom adjacent) {
		for (Bond b : adjacent.getBonds()) {
			Atom carbon = b.get1_2(adjacent);
			if (carbon.getAtomicNumber() == 6 && formsBondsWith(carbon, 8)) {
				return carbon;
			}
		}
		return null;
	}

	/*
	 * Returns an oxygen atom that is bonded to atom p and a carbon,
	 * but is not atom o.
	 * 
	 * O-P-X-C
	 * 
	 * where X is the returned atom. This is useful for traversing
	 * a nucleic acid backbone.
	 * 
	 * @param p
	 *            Atom
	 * @param o
	 *            Atom
	 * @return Atom
	 */
	public static Atom findOtherOxygen(Atom p, Atom o) {
		for (Bond b : p.getBonds()) {
			Atom oxygen = b.get1_2(p);
			if (oxygen.getAtomicNumber() == 8 && oxygen != o
					&& formsBondsWith(oxygen, 6)) {
				return oxygen;
			}
		}
		return null;
	}

	/*
	 * @param atoms
	 *            List
	 * @param currentAtom
	 *            Atom
	 * @param path
	 *            List
	 * @return List
	 */
	public static List<Atom> findPolymer(List<Atom> atoms, Atom currentAtom,
			List<Atom> path) {
		// Atom has no bonds to follow
		if (currentAtom.getBonds() == null) {
			path = getAtomListFromPool();
			path.add(currentAtom);
			return path;
		}
		// End of Recursion conditions
		if (currentAtom.getParent() != null) {
			return null;
		}
		int anum = currentAtom.getAtomicNumber();
		// Only C,N,O,P in a DNA/RNA/protein backbone
		if (anum != 6 && anum != 7 && anum != 8 && anum != 15) {
			return null;
		}
		// Allow search to get out of side chains, but not enter them
		if (path != null && path.size() > 7) {
			// Oxygen is only in the backbone for nucleic acids
			// in a phosphate group
			if (anum == 8) {
				if (!formsBondsWith(currentAtom, 15)) {
					return null;
				}
			// Nitrogen is only in the backbone in peptide bonds
			} else if (anum == 7) {
				Atom carbonyl = findCarbonyl(currentAtom);
				if (carbonyl == null) {
					return null;
				}
			// Make sure nitrogen is bonded to an alpha carbon
				Atom alphaCarbon = findAlphaCarbon(currentAtom);
				if (alphaCarbon == null) {
					return null;
				}
			// Avoid more than three carbons in a row (phenyls, etc.)
			} else if (anum == 6) {
				Atom a;
				int anum2, anum3, anum4;
				int size = path.size();
				a = path.get(size - 1);
				anum2 = a.getAtomicNumber();
				if (anum2 == 6) {
					a = path.get(size - 2);
					anum3 = a.getAtomicNumber();
					if (anum3 == 6) {
						a = path.get(size - 3);
						anum4 = a.getAtomicNumber();
						if (anum4 == 6) {
							return null;
						}
					}
				}
			}
		}
		// Atoms with only one bond are at the end of a polymer
		Atom previousAtom = null;
		if (path != null) {
			previousAtom = path.get(path.size() - 1);
		}
		List<Bond> bonds = currentAtom.getBonds();
		if (bonds.size() == 1 && previousAtom != null) {
			return null;
		}
		// Initialization
		if (path == null) {
			path = getAtomListFromPool();
			previousAtom = null;
		// Or Continuation
		} else {
			List<Atom> pathclone = getAtomListFromPool();
			pathclone.addAll(path);
			path = pathclone;
		}
		// Add the currentAtom to the growing path
		path.add(currentAtom);
		// Continue search in each bond direction, but no backtracking over
		// previousAtom
		Atom nextAtom;
		List<Atom> newPolymer, maxPolymer = getAtomListFromPool();
		for (Bond b : bonds) {
			nextAtom = b.get1_2(currentAtom);
			// Check to avoid returning in the same direction and loops
			if (nextAtom != previousAtom && !path.contains(nextAtom)) {

				//
				// recursive call below is VERY slow for cucurbituril !!
				//
				newPolymer = findPolymer(atoms, nextAtom, path);
                                //newPolymer = null;

				if (newPolymer != null) {
					// Check to see if the Polymers contain any of the same
					// atoms; if so, use the shorter Polymer (avoids loops)
					if (haveCommonAtom(newPolymer, maxPolymer)) {
						if (newPolymer.size() < maxPolymer.size()) {
							addAtomListToPool(maxPolymer);
							maxPolymer = newPolymer;
						}
					} else if (newPolymer.size() > maxPolymer.size()) {
						addAtomListToPool(maxPolymer);
						maxPolymer = newPolymer;
					}
				}
			}
		}
		// Add the currentAtom to the longest discovered chain and return
		maxPolymer.add(0, currentAtom);
		return maxPolymer;
	}

	/*
	 * Returns an atom bonded to the "end" atom, which is not equal to "other".
	 * 
	 * @param end
	 *            Atom
	 * @param other
	 *            Atom
	 * @return Atom
	 */
	public static Atom findSeed(Atom end, Atom other) {
		for (Bond b : end.getBonds()) {
			Atom seed = b.get1_2(end);
			if (seed != other) {
				return seed;
			}
		}
		return null;
	}

	/*
	 * True if Atom a forms a bond with another atom
	 * of the specified atomic Number.
	 * 
	 * @param a
	 *            Atom
	 * @param atomicNumber
	 *            int
	 * @return boolean
	 */
	public static boolean formsBondsWith(Atom a, int atomicNumber) {
		for (Bond b : a.getBonds()) {
			Atom other = b.get1_2(a);
			if (other.getAtomicNumber() == atomicNumber) {
				return true;
			}
		}
		return false;
	}

	public static List<Atom> getAtomListFromPool() {
		if (atomListPool.isEmpty()) {
			return new ArrayList<Atom>();
		}
		return atomListPool.remove(0);
	}

	/*
	 * public static void numericalBornRadii (List<Atom> atoms) { double
	 * initialThickness = 0.1; double enlarge = 1.5; int numberOfAtoms =
	 * atoms.size(); double radii[] = new double[numberOfAtoms]; double born[] =
	 * new double[numberOfAtoms]; for (int i = 0; i < radii.length; i++) {
	 * double initialR = radii[i]; double thickness = initialThickness; double
	 * total = 0.0; double fraction = 0.0; while (fraction < 0.99) { radii[i] +=
	 * 0.5 * thickness; double area = surface(i, radii); fraction = area / (4.0 *
	 * Math.PI * radii[i] * radii[i]); if (fraction < 0.99) { double inner =
	 * radii[i] - 0.5 * thickness; double outer = inner + thickness; double
	 * shell = 1.0 / inner - 1.0 / outer; total += fraction * shell; radii[i] +=
	 * 0.5 * thickness; thickness *= enlarge; } else { double inner = radii[i] -
	 * 0.5 * thickness; total += 1.0 / inner; } } born[i] = 1.0 / total;
	 * radii[i] = initialR; } }
	 * 
	 * private static double surface(int index, double radii[]){ return 1.0; }
	 */

	/*
	 * Returns true if the lists contain any atom in common.
	 * 
	 * @param list1
	 *            List
	 * @param list2
	 *            List
	 * @return boolean
	 */
	private static boolean haveCommonAtom(List<Atom> list1, List<Atom> list2) {
		if (list1 == null || list2 == null) {
			return false;
		}
		for (Atom a : list1) {
			if (list2.contains(a)) {
				return true;
			}
		}
		return false;
	}

	/*
	 * Returns true if Atom a is water oxygen.
	 * 
	 * @param a
	 *            Atom
	 * @return boolean
	 */
	public static boolean isWaterOxygen(Atom a) {
		if (a.getAtomicNumber() != 8) {
			return false;
		}
		for (Bond b : a.getBonds()) {
			Atom h = b.get1_2(a);
			if (h.getAtomicNumber() != 1) {
				return false;
			}
		}
		return true;
	}

	/*
	 * This function returns the number of times atom "a" is bonded
	 * to an atom of the specified atomic number.
	 * 
	 * @param a
	 *            Atom
	 * @param atomicNumber
	 *            int
	 * @return int
	 */
	public static int numberOfBondsWith(Atom a, int atomicNumber) {
		int total = 0;
		for (Bond b : a.getBonds()) {
			Atom other = b.get1_2(a);
			if (other.getAtomicNumber() == atomicNumber) {
				total++;
			}
		}
		return total;
	}

	// Check to see if a portion of the backbone matches that of a
	// biological polymer, and if so determine the respective residue
	private static Residue patternMatch(int start, List<Atom> backbone,
			PolymerType type) {
		int pattern[];
		// Initialization
		if (type == PolymerType.AMINOACID) {
			pattern = AAPATTERN;
			if (backbone.size() < start + pattern.length) {
				return null;
			}
			// Check for correct Carbonyl placement
			Atom a = backbone.get(start + 1);
			if (formsBondsWith(a, 8)) {
				return null;
			}
			a = backbone.get(start + 2);
			if (!formsBondsWith(a, 8)) {
				return null;
			}
		} else if (type == PolymerType.NUCLEICACID) {
			pattern = NAPATTERN;
			if (backbone.size() < start + pattern.length) {
				return null;
			}
		} else {
			return null;
		}
		int length = pattern.length;
		List<Atom> atoms = getAtomListFromPool();
		List<Atom> sidePolymer = getAtomListFromPool();
		for (int i = 0; i < length; i++) {
			Atom a = backbone.get(start + i);
			// add backbone atoms to terminate sidePolymer search
			sidePolymer.add(a);
			if (a.getAtomicNumber() != pattern[i]) {
				return null;
			}
		}
		// Collect all the atoms in the Residue
		// Add the atom before and after the backbone pattern to
		// terminate the search, then remove them
		if (start > 0) {
			atoms.add(backbone.get(start - 1));
		}
		if (start + length < backbone.size()) {
			atoms.add(backbone.get(start + length));
		}
		collectAtoms(backbone.get(start), atoms);
		if (start > 0) {
			atoms.remove(0);
		}
		if (start + length < backbone.size()) {
			atoms.remove(0);
			// Collect Just Side chain atoms, then remove backbone termination
		}
		if (type == PolymerType.AMINOACID) {
			collectAtoms(sidePolymer.get(1), sidePolymer);
		} else if (type == PolymerType.NUCLEICACID) {
			Atom seed = null;
			for (Atom a : atoms) {
				if (a.getAtomicNumber() == 7) {
					seed = a;
					break;
				}
			}
			if (seed != null && seed.getAtomicNumber() == 7) {
				sidePolymer.add(seed);
				collectAtoms(seed, sidePolymer);
			} else {
				return null;
			}
		}
		for (int i = 0; i <= length; i++) {
			sidePolymer.remove(0);
		}
		Residue res = assignResidue(backbone, start, atoms, sidePolymer);
		return res;
	}

	/*
	 * Determine a unique string for given the polymer number.
	 * 
	 * @param i
	 *            int
	 * @return String
	 */
	public static String polymerLookup(int i) {
		if (i > 25) {
			int repeat = i / 25;
			i = i % 25;
			return String.valueOf((char) (i + 65)) + repeat;
		}
		return String.valueOf((char) (i + 65));
	}

	/*
	 * Returns an List with reversed ordering.
	 * 
	 * @param atomList
	 *            List
	 * @return List
	 */
	static private List<Atom> reverseAtomList(List<Atom> atomList) {
		List<Atom> reversedList = getAtomListFromPool();
		for (Atom a : atomList) {
			reversedList.add(0, a);
		}
		return reversedList;
	}

	/*
	 * Finds the RMS deviation between the atoms of MolecularAssembly
	 * m1 and m2 provided they have the same number of atoms.
	 */
	public static double RMSCoordDev(MolecularAssembly m1, MolecularAssembly m2) {
		if (m1 == null || m2 == null) {
			return 0;
		}
		int n1 = m1.getAtomList().size();
		int n2 = m2.getAtomList().size();
		if (n1 != n2) {
			return 0;
		}
		Atom a1, a2;
		double[] d = new double[3];
		double[] da = new double[3];
		double[] db = new double[3];
		double rms = 0;
		ListIterator li, lj;
		for (li = m1.getAtomList().listIterator(), lj = m2.getAtomList()
				.listIterator(); li.hasNext();) {
			a1 = (Atom) li.next();
			a2 = (Atom) lj.next();
			a1.getXYZ(da);
			a2.getXYZ(db);
			VectorMath.diff(da, db, d);
			rms += d[0] * d[0] + d[1] * d[1] + d[2] * d[2];
		}
		return Math.sqrt(rms / n1);
	}

	/*
	 * Rotate atomic multipoles into the global frame.
	 * 
	 * @param atoms
	 *            List
	 */
	public static void rotateMulitpoles(List<Atom> atoms) {
		for (Atom atom : atoms) {
			MultipoleType multipoleType = atom.getMultipoleType();
			if (multipoleType == null) {
				logger.warning("No multipole assigned to: " + atom
						+ "\nOf type:\n" + atom.getAtomType());
				continue;
			}
			atom.getXYZ(localOrigin);
			Atom referenceSites[] = atom.getMultipoleReferenceSites();
			for (int i = 0; i < 3; i++) {
				zaxis[i] = 0.0;
				xaxis[i] = 0.0;
				dipole[i] = 0.0;
				for (int j = 0; j < 3; j++) {
					quadrupole[i][j] = 0.0;
				}
			}
			if (referenceSites != null) {
				if (referenceSites.length == 1) {
					referenceSites[0].getXYZ(zaxis);
				} else if (referenceSites.length == 2) {
					referenceSites[0].getXYZ(zaxis);
					referenceSites[1].getXYZ(xaxis);
				}
			}
			VectorMath.diff(zaxis, localOrigin, zaxis);
			VectorMath.norm(zaxis, zaxis);
			VectorMath.diff(xaxis, localOrigin, xaxis);
			// Separate differences between the Z-THEN-X definition
			// and BISECTOR methods for finding the Z elements of the
			// rotation matrix.
			if (multipoleType.frameDefinition == MultipoleType.MultipoleFrameDefinition.ZTHENX) {
				rotmat[0][2] = zaxis[0];
				rotmat[1][2] = zaxis[1];
				rotmat[2][2] = zaxis[2];
			} else {
				VectorMath.norm(xaxis, xaxis);
				// Take the norm of the sum of unit vectors to find their
				// bisector.
				VectorMath.sum(zaxis, xaxis, zaxis);
				VectorMath.norm(zaxis, zaxis);
				rotmat[0][2] = zaxis[0];
				rotmat[1][2] = zaxis[1];
				rotmat[2][2] = zaxis[2];
			}
			// Find the X elements.
			double dot = VectorMath.dot(zaxis, xaxis);
			VectorMath.scalar(zaxis, dot, zaxis);
			VectorMath.diff(xaxis, zaxis, xaxis);
			VectorMath.norm(xaxis, xaxis);
			rotmat[0][0] = xaxis[0];
			rotmat[1][0] = xaxis[1];
			rotmat[2][0] = xaxis[2];
			// Finally the Y elements.
			rotmat[0][1] = rotmat[2][0] * rotmat[1][2] - rotmat[1][0]
					* rotmat[2][2];
			rotmat[1][1] = rotmat[0][0] * rotmat[2][2] - rotmat[2][0]
					* rotmat[0][2];
			rotmat[2][1] = rotmat[1][0] * rotmat[0][2] - rotmat[0][0]
					* rotmat[1][2];
			// Do the rotation.
			double localDipole[] = multipoleType.dipole;
			double localQuadrupole[][] = multipoleType.quadrupole;
			for (int i = 0; i < 3; i++) {
				double[] rotmati = rotmat[i];
				double[] quadrupolei = quadrupole[i];
				for (int j = 0; j < 3; j++) {
					double[] rotmatj = rotmat[j];
					dipole[i] += rotmati[j] * localDipole[j];
					if (j < i) {
						quadrupolei[j] = quadrupole[j][i];
					} else {
						for (int k = 0; k < 3; k++) {
							double[] localQuadrupolek = localQuadrupole[k];
							quadrupolei[j] += rotmati[k]
									* (rotmatj[0] * localQuadrupolek[0]
										+ rotmatj[1] * localQuadrupolek[1]
										+ rotmatj[2] * localQuadrupolek[2]);
						}
					}
				}
			}
			atom.setGlobalMultipole(dipole, quadrupole);
			if (logger.isLoggable(Level.FINEST)) {
				logger.finest(atom.toMultipoleString());
			}
		}
	}

	public static void stillBornRadii(List<Atom> atoms) {
		if (atoms == null || atoms.size() < 1) {
			logger.warning("No Born radii computed due to empty an atom list");
			return;
		}
		int numberOfAtoms = atoms.size();
		int skip[] = new int[numberOfAtoms + 1];
		double ccf = 0.0;
		for (int i = 0; i < numberOfAtoms; i++) {
			Atom a1 = atoms.get(i);
			a1.getXYZ(x1);
			double vdw1 = a1.getRDielectric();
			double gpi = a1.getGPol();
			List<Bond> bonds = a1.getBonds();
			for (Bond b : bonds) {
				int other = b.get1_2(a1).getXYZIndex();
				skip[other] = i + 1;
			}
			List<Angle> angles = a1.getAngles();
			for (Angle a : angles) {
				Atom other = a.get1_3(a1);
				if (other != null) {
					int i13 = other.getXYZIndex();
					skip[i13] = i + 1;
				}
			}
			for (int k = 0; k < numberOfAtoms; k++) {
				if (skip[k] != i + 1) {
					Atom a2 = atoms.get(k);
					a2.getXYZ(x2);
					VectorMath.diff(x2, x1, x2);
					double r2 = x2[0] * x2[0] + x2[1] * x2[1] + x2[2] * x2[2];
					double r4 = r2 * r2;
					double rvdw = vdw1 + a2.getRDielectric();
					double ratio = r2 / (rvdw * rvdw);
					if (ratio > p5inv) {
						ccf = 1.0;
					} else {
						double term = 0.5 * (1.0 - Math.cos(ratio * pip5));
						ccf = term * term;
					}
					gpi += p4 * ccf * a2.getBornVolume() / r4;
				}
			}
			a1.setBornRadius(convert / gpi);
		}
	}
}
