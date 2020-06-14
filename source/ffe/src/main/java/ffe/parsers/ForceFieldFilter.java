/*
 * <p>Title: Force Field Explorer</p>
 * <p>Description: Force Field Explorer Molecular Modeling Program</p>
 * <p>Copyright: Copyright (c) 2004-2020 Jay William Ponder</p>
 * <p>Institution: Jay Ponder Lab, Washington University in Saint Louis</p>
 * @author Michael J. Schnieders
 * @version 8.8
 */

package ffe.parsers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Hashtable;
import java.util.logging.Logger;

import ffe.mm.AngleType;
import ffe.mm.AtomType;
import ffe.mm.BioType;
import ffe.mm.BondType;
import ffe.mm.ChargeType;
import ffe.mm.ForceField;
import ffe.mm.MultipoleType;
import ffe.mm.OutOfPlaneBendType;
import ffe.mm.PiTorsionType;
import ffe.mm.PolarizeType;
import ffe.mm.StretchBendType;
import ffe.mm.TorsionTorsionType;
import ffe.mm.TorsionType;
import ffe.mm.UreyBradleyType;
import ffe.mm.VDWType;
import ffe.mm.ForceField.ForceFieldDouble;
import ffe.mm.ForceField.ForceFieldString;
import ffe.mm.ForceField.ForceFieldType;

/*
 * The ForceFieldFilter Class is used to parse and store molecular mechanics
 * data from Tinker Keyword (*.KEY) and Parameter (*.PRM) files.
 */
public class ForceFieldFilter {
	/*
	 * A Hashtable of Amino Acid Name Strings, stored by three-letter
	 * abreviations
	 */
	public static final Hashtable<String, String> aminoAcidLookup = new Hashtable<String, String>();

	static {
		aminoAcidLookup.put("GLY", "Glycine");
		aminoAcidLookup.put("ALA", "Alanine");
		aminoAcidLookup.put("VAL", "Valine");
		aminoAcidLookup.put("LEU", "Leucine");
		aminoAcidLookup.put("ILE", "Isoleucine");
		aminoAcidLookup.put("SER", "Serine");
		aminoAcidLookup.put("THR", "Threonine");
		aminoAcidLookup.put("CYS", "Cystine (-SS-)");
		aminoAcidLookup.put("PRO", "Proline");
		aminoAcidLookup.put("PHE", "Phenylalanine");
		aminoAcidLookup.put("TYR", "Tyrosine");
		aminoAcidLookup.put("TRP", "Tryptophan");
		aminoAcidLookup.put("HIS", "Histidine (HD)");
		aminoAcidLookup.put("ASP", "Aspartic Acid");
		aminoAcidLookup.put("ASN", "Asparagine");
		aminoAcidLookup.put("GLU", "Glutamic Acid");
		aminoAcidLookup.put("GLN", "Glutamine");
		aminoAcidLookup.put("MET", "Methionine");
		aminoAcidLookup.put("LYS", "Lysine");
		aminoAcidLookup.put("ARG", "Arginine");
		aminoAcidLookup.put("A", "Adenine");
		aminoAcidLookup.put("T", "Thymine");
		aminoAcidLookup.put("G", "Guanine");
		aminoAcidLookup.put("C", "Cytosine");
		aminoAcidLookup.put("HOH", "HOH");
		aminoAcidLookup.put("HEM", "HEM");
	}

	private ForceField forceField = null;
	private Logger logger = Logger.getLogger("ffe");
	
	/*
	 * ForceFieldFilter Constructor.
	 */
	public ForceFieldFilter(File forceFieldFile, File keyFile) {
		forceField = new ForceField(forceFieldFile, keyFile);
	}

	/*
	 * Parse a Force Field parameter file.
	 * 
	 * @return ForceField
	 */
	public ForceField parse() {
		if (forceField.forceFieldFile != null) {
			parse(forceField.forceFieldFile);
		}
		if (forceField.keyFile != null){
			parse(forceField.keyFile);
		}
		return forceField;
	}
	
	private void parse(File file){
		try {
			FileInputStream fis = new FileInputStream(file);
			BufferedReader br = new BufferedReader(new InputStreamReader(fis));
			while (br.ready()) {
				String input = br.readLine();
				String tokens[] = input.trim().split("\\s+");
				if (tokens != null) {
					String keyword = tokens[0].toUpperCase().replaceAll("-",
							"_");
					// Parse Keywords with String values.
					try {
						ForceFieldString forceFieldString = ForceFieldString
								.valueOf(keyword);
						forceField.addForceFieldString(forceFieldString,
								tokens[1]);
						continue;
					} catch (Exception e) {
						// Parse Keywords with double values.
						try {
							ForceFieldDouble forceFieldDouble = ForceFieldDouble
									.valueOf(keyword);
							double value = Double.parseDouble(tokens[1]);
							forceField.addForceFieldDouble(forceFieldDouble,
									value);
							continue;
						} catch (Exception e2) {
							// Parse Force Field Types.
							try {
								ForceFieldType type = ForceFieldType
										.valueOf(tokens[0].toUpperCase());
								switch (type) {
								case ATOM:
									parseAtom(input, tokens);
									break;
								case ANGLE:
									parseAngle(input, tokens);
									break;
								case BIOTYPE:
									parseBioType(input, tokens);
									break;
								case BOND:
									parseBond(input, tokens);
									break;
								case CHARGE:
									parseCharge(input, tokens);
									break;
								case MULTIPOLE:
									parseMultipole(input, tokens, br);
									break;
								case OPBEND:
									parseOPBend(input, tokens);
									break;
								case STRBND:
									parseStrBnd(input, tokens);
									break;
								case PITORS:
									parsePiTorsion(input, tokens);
									break;
								case TORSION:
									parseTorsion(input, tokens);
									break;
								case TORTORS:
									parseTorsionTorsion(input, tokens, br);
									break;
								case UREYBRAD:
									parseUreyBradley(input, tokens);
									break;
								case VDW:
									parseVDW(input, tokens);
									break;
								case POLARIZE:
									parsePolarize(input, tokens);
									break;
								default:
									logger
											.warning("ForceField type recognized, but not stored:"
													+ type);
								}
								continue;
							} catch (Exception e3) {
								//logger.finest("Parsed as a comment: " + input);
							}
						}
					}
				}
			}
			br.close();
			fis.close();
		} catch (Exception e) {
			logger.warning("Error parsing force field parameters from: "
					+ file + "\n" + e);
		}
	}

	private void parseAngle(String input, String tokens[]) {
		if (tokens.length < 6) {
			logger.warning("Invalid ANGLE type:\n" + input);
			return;
		}
		int atomClasses[] = new int[3];
		double forceConstant = 0.0;
		int i = 0;
		int angles = 0;
		double bondAngle[] = null;
		try {
			atomClasses[0] = Integer.parseInt(tokens[1]);
			atomClasses[1] = Integer.parseInt(tokens[2]);
			atomClasses[2] = Integer.parseInt(tokens[3]);
			forceConstant = Double.parseDouble(tokens[4]);
			angles = tokens.length - 5;
			bondAngle = new double[angles];
			for (i = 0; i < angles; i++) {
				bondAngle[i] = Double.parseDouble(tokens[5 + i]);
			}
		} catch (Exception e) {
			if (!tokens[5+i].startsWith("!!")) {
			logger
					.warning("Exception parsing ANGLE type:\n" + input + "\n"
							+ e);
			}
		}
		
		double newBondAngle[] = new double[i];
		for (int j = 0; j < i; j++) {
			newBondAngle[j] = bondAngle[i];
		}		
		AngleType angleType = new AngleType(atomClasses, forceConstant,
				newBondAngle);
		forceField.addForceFieldType(angleType);
	}

	private void parseAtom(String input, String[] tokens) {
		if (tokens.length < 7) {
			logger.warning("Invalid ATOM type:\n" + input);
			return;
		}
		try {
			int index = 1;
			// Atom Type
			int type = Integer.parseInt(tokens[index++]);
			// Atom Class
			int atomClass = -1;
			// The following try/catch is a nasty hack to check for one of the
			// the following two cases:
			//
			// NUMBER TYPE CLASS IDENTIFIER ... (example is OPLSAA)
			// vs.
			// NUMBER TYPE IDENTIFIER ... (example is OPLSUA)
			//
			// If there is no atom class, a harmless exception will be caught
			// and the atomClass field will remain equal to null.
			try {
				atomClass = Integer.parseInt(tokens[index]);
				// If the parseInt succeeds, this force field has atom classes.
				index++;
			} catch (Exception e) {
				// Some force fields do not use atom classes.
				atomClass = -1;
			}
			// Name
			String name = tokens[index++].intern();
			// The "environment" string may contain spaces,
			// and is therefore surrounded in quotes located at "first" and
			// "last".
			int first = input.indexOf("\"");
			int last = input.lastIndexOf("\"");
			if (first >= last) {
				logger.warning("Invalid ATOM type:\n" + input);
				return;
			}
			// Environment
			String environment = input.substring(first, last + 1).intern();
			// Shrink the tokens array to only include entries
			// after the environment field.
			tokens = input.substring(last + 1).trim().split("\\s+");
			index = 0;
			// Atomic Number
			int atomicNumber = Integer.parseInt(tokens[index++]);
			// Atomic Mass
			double mass = Double.parseDouble(tokens[index++]);
			// Hybridization
			int hybridization = Integer.parseInt(tokens[index++]);
			AtomType atomType = new AtomType(type, atomClass, name,
					environment, atomicNumber, mass, hybridization);
			forceField.addForceFieldType(atomType);
		} catch (Exception e) {
			logger.warning("Exception parsing CHARGE type:\n" + input + "\n"
					+ e);
		}
	}

	private void parseBioType(String input, String tokens[]) {
		if (tokens.length < 5) {
			logger.warning("Invalid BIOTYPE type:\n" + input);
			return;
		}
		try {
			int index = Integer.parseInt(tokens[1]);
			String PDB = tokens[2];
			// The "residue" string may contain spaces,
			// and is therefore surrounded in quotes located at "first" and
			// "last".
			int first = input.indexOf("\"");
			int last = input.lastIndexOf("\"");
			if (first >= last) {
				logger.warning("Invalid BIOTYPE type:\n" + input);
				return;
			}
			// Environment
			String residue = input.substring(first, last + 1).intern();
			// Shrink the tokens array to only include entries
			// after the environment field.
			tokens = input.substring(last + 1).trim().split("\\s+");
			int atomType = Integer.parseInt(tokens[0]);
			BioType bioType = new BioType(index, PDB, residue, atomType);
			forceField.addForceFieldType(bioType);
		} catch (Exception e) {
			logger.warning("Exception parsing BIOTYPE type:\n" + input + "\n"
					+ e);
		}
	}

	private void parseBond(String input, String[] tokens) {
		if (tokens.length < 5) {
			logger.warning("Invalid BOND type:\n" + input);
			return;
		}
		try {
			int atomClasses[] = new int[2];
			atomClasses[0] = Integer.parseInt(tokens[1]);
			atomClasses[1] = Integer.parseInt(tokens[2]);
			double forceConstant = Double.parseDouble(tokens[3]);
			double distance = Double.parseDouble(tokens[4]);
			BondType bondType = new BondType(atomClasses, forceConstant,
					distance);
			forceField.addForceFieldType(bondType);
		} catch (Exception e) {
			logger.warning("Exception parsing BOND type:\n" + input + "\n" + e);
		}
	}

	private void parseCharge(String input, String tokens[]) {
		if (tokens.length < 3) {
			logger.warning("Invalid CHARGE type:\n" + input);
			return;
		}
		try {
			int atomType = Integer.parseInt(tokens[1]);
			double partialCharge = Double.parseDouble(tokens[2]);
			ChargeType chargeType = new ChargeType(atomType, partialCharge);
			forceField.addForceFieldType(chargeType);
		} catch (Exception e) {
			logger.warning("Exception parsing CHARGE type:\n" + input + "\n"
					+ e);
		}
	}

	private void parseMultipole(String input, String[] tokens, BufferedReader br) {
		if (tokens.length < 5) {
			logger.warning("Invalid MULTIPOLE type:\n" + input);
			return;
		}
		try {
			MultipoleType.MultipoleFrameDefinition frameDefinition = MultipoleType.MultipoleFrameDefinition.ZTHENX;
			int numTypes = tokens.length - 2;
			int atomTypes[] = new int[numTypes];
			for (int i = 0; i < numTypes; i++) {
				atomTypes[i] = Integer.parseInt(tokens[i + 1]);
				if (atomTypes[i] < 0) {
					frameDefinition = MultipoleType.MultipoleFrameDefinition.BISECTOR;
					atomTypes[i] = Math.abs(atomTypes[i]);
				}
			}
			double c = Double.parseDouble(tokens[1 + numTypes]);
			input = br.readLine();
			tokens = input.trim().split("\\s+");
			if (tokens.length != 3) {
				logger.warning("Invalid MULTIPOLE type:\n" + input);
				return;
			}
			double dipole[] = new double[3];
			dipole[0] = Double.parseDouble(tokens[0]);
			dipole[1] = Double.parseDouble(tokens[1]);
			dipole[2] = Double.parseDouble(tokens[2]);
			input = br.readLine();
			tokens = input.trim().split("\\s+");
			if (tokens.length != 1) {
				logger.warning("Invalid MULTIPOLE type:\n" + input);
				return;
			}
			double quadrupole[][] = new double[3][3];
			quadrupole[0][0] = Double.parseDouble(tokens[0]);
			input = br.readLine();
			tokens = input.trim().split("\\s+");
			if (tokens.length != 2) {
				logger.warning("Invalid MULTIPOLE type:\n" + input);
				return;
			}
			quadrupole[1][0] = Double.parseDouble(tokens[0]);
			quadrupole[1][1] = Double.parseDouble(tokens[1]);
			input = br.readLine();
			tokens = input.trim().split("\\s+");
			if (tokens.length != 3) {
				logger.warning("Invalid MULTIPOLE type:\n" + input);
				return;
			}
			quadrupole[2][0] = Double.parseDouble(tokens[0]);
			quadrupole[2][1] = Double.parseDouble(tokens[1]);
			quadrupole[2][2] = Double.parseDouble(tokens[2]);
			// Fill in symmetric components.
			quadrupole[0][1] = quadrupole[1][0];
			quadrupole[0][2] = quadrupole[2][0];
			quadrupole[1][2] = quadrupole[2][1];
			MultipoleType multipoleType = new MultipoleType(c, dipole,
					quadrupole, atomTypes, frameDefinition);
			forceField.addForceFieldType(multipoleType);
		} catch (Exception e) {
			logger.warning("Exception parsing MULTIPOLE type:\n" + input + "\n"
					+ e.toString());
		}
	}

	private void parseOPBend(String input, String[] tokens) {
		if (tokens.length < 4) {
			logger.warning("Invalid OPBEND type:\n" + input);
			return;
		}
		try {
			int atomClasses[] = new int[2];
			atomClasses[0] = Integer.parseInt(tokens[1]);
			atomClasses[1] = Integer.parseInt(tokens[2]);
			double forceConstant = Double.parseDouble(tokens[3]);
			OutOfPlaneBendType opbendType = new OutOfPlaneBendType(atomClasses,
					forceConstant);
			forceField.addForceFieldType(opbendType);
		} catch (Exception e) {
			logger.warning("Exception parsing OPBEND type:\n" + input + "\n"
					+ e);
		}
	}

	private void parsePiTorsion(String input, String[] tokens) {
		if (tokens.length < 4) {
			logger.warning("Invalid PITORS type:\n" + input);
			return;
		}
		try {
			int atomClasses[] = new int[2];
			atomClasses[0] = Integer.parseInt(tokens[1]);
			atomClasses[1] = Integer.parseInt(tokens[2]);
			double forceConstant = Double.parseDouble(tokens[3]);
			PiTorsionType piTorsionType = new PiTorsionType(atomClasses,
					forceConstant);
			forceField.addForceFieldType(piTorsionType);
		} catch (Exception e) {
			logger.warning("Exception parsing PITORS type:\n" + input + "\n"
					+ e);
		}
	}

	private void parsePolarize(String input, String tokens[]) {
		if (tokens.length < 4) {
			logger.warning("Invalid POLARIZE type:\n" + input);
		}
		try {
			int atomType = Integer.parseInt(tokens[1]);
			double polarizability = Double.parseDouble(tokens[2]);
			double thole = Double.parseDouble(tokens[3]);
			int entries = tokens.length - 4;
			int polarizationGroup[] = null;
			if (entries > 0) {
				polarizationGroup = new int[entries];
				for (int i = 4; i < tokens.length; i++) {
					polarizationGroup[i - 4] = Integer.parseInt(tokens[i]);
				}
			}
			PolarizeType polarizeType = new PolarizeType(atomType,
					polarizability, thole, polarizationGroup);
			forceField.addForceFieldType(polarizeType);
		} catch (Exception e) {
			logger.warning("Exception parsing POLARIZE type:\n" + input + "\n"
					+ e);
		}
	}

	private void parseStrBnd(String input, String[] tokens) {
		if (tokens.length < 5) {
			logger.warning("Invalid STRBND type:\n" + input);
			return;
		}
		try {
			int atomClasses[] = new int[3];
			atomClasses[0] = Integer.parseInt(tokens[1]);
			atomClasses[1] = Integer.parseInt(tokens[2]);
			atomClasses[2] = Integer.parseInt(tokens[3]);
			double forceConstants[] = new double[2];
			forceConstants[0] = Double.parseDouble(tokens[4]);
			forceConstants[1] = Double.parseDouble(tokens[5]);
			StretchBendType strbndType = new StretchBendType(atomClasses,
					forceConstants);
			forceField.addForceFieldType(strbndType);
		} catch (Exception e) {
			logger.warning("Exception parsing STRBND type:\n" + input + "\n"
					+ e);
		}
	}

	private void parseTorsion(String input, String tokens[]) {
		if (tokens.length < 5) {
			logger.warning("Invalid TORSION type:\n" + input);
			return;
		}
		try {
			int atomClasses[] = new int[4];
			atomClasses[0] = Integer.parseInt(tokens[1]);
			atomClasses[1] = Integer.parseInt(tokens[2]);
			atomClasses[2] = Integer.parseInt(tokens[3]);
			atomClasses[3] = Integer.parseInt(tokens[4]);
			int terms = (tokens.length - 5) / 3;
			double amplitude[] = new double[terms];
			double phase[] = new double[terms];
			int periodicity[] = new int[terms];
			int index = 5;
			for (int i = 0; i < terms; i++) {
				amplitude[i] = Double.parseDouble(tokens[index++]);
				phase[i] = Double.parseDouble(tokens[index++]);
				periodicity[i] = Integer.parseInt(tokens[index++]);
			}
			TorsionType torsionType = new TorsionType(atomClasses, amplitude,
					phase, periodicity);
			forceField.addForceFieldType(torsionType);
		} catch (Exception e) {
			logger.warning("Exception parsing TORSION type:\n" + input + "\n"
					+ e);
		}
	}

	private void parseTorsionTorsion(String input, String[] tokens,
			BufferedReader br) {
		if (tokens.length < 8) {
			logger.warning("Invalid TORTORS type:\n" + input);
			return;
		}
		try {
			int atomClasses[] = new int[5];
			for (int i = 0; i < 5; i++) {
				atomClasses[i] = Integer.parseInt(tokens[i + 1]);
			}
			int gridPoints[] = new int[2];
			gridPoints[0] = Integer.parseInt(tokens[6]);
			gridPoints[1] = Integer.parseInt(tokens[7]);
			int points = gridPoints[0] * gridPoints[1];
			double torsion1[] = new double[points];
			double torsion2[] = new double[points];
			double energy[] = new double[points];
			for (int i = 0; i < points; i++) {
				input = br.readLine();
				tokens = input.trim().split("\\s+");
				if (tokens.length != 3) {
					logger.warning("Invalid TORTORS type:\n" + input);
					return;
				}
				torsion1[i] = Double.parseDouble(tokens[0]);
				torsion2[i] = Double.parseDouble(tokens[1]);
				energy[i] = Double.parseDouble(tokens[2]);
			}
			TorsionTorsionType torsionTorsionType = new TorsionTorsionType(
					atomClasses, gridPoints, torsion1, torsion2, energy);
			forceField.addForceFieldType(torsionTorsionType);
		} catch (Exception e) {
			logger.warning("Exception parsing TORTORS type:\n" + input + "\n"
					+ e);
		}
	}

	private void parseUreyBradley(String input, String[] tokens) {
		if (tokens.length < 5) {
			logger.warning("Invalid UREYBRAD type:\n" + input);
			return;
		}
		try {
			int atomClasses[] = new int[3];
			atomClasses[0] = Integer.parseInt(tokens[1]);
			atomClasses[1] = Integer.parseInt(tokens[2]);
			atomClasses[2] = Integer.parseInt(tokens[3]);
			double forceConstant = Double.parseDouble(tokens[4]);
			double distance = Double.parseDouble(tokens[5]);
			UreyBradleyType ureyType = new UreyBradleyType(atomClasses,
					forceConstant, distance);
			forceField.addForceFieldType(ureyType);
		} catch (Exception e) {
			logger.warning("Exception parsing UREYBRAD type:\n" + input + "\n"
					+ e);
		}
	}
	private void parseVDW(String input, String[] tokens) {
		if (tokens.length < 4) {
			logger.warning("Invalid VDW type:\n" + input);
			return;
		}
		try {
			int atomType = Integer.parseInt(tokens[1]);
			double radius = Double.parseDouble(tokens[2]);
			double wellDepth = Double.parseDouble(tokens[3]);
			double reductionFactor = -1.0;
			if (tokens.length == 5) {
				reductionFactor = Double.parseDouble(tokens[4]);
			}
			VDWType vdwType = new VDWType(atomType, radius, wellDepth,
					reductionFactor);
			forceField.addForceFieldType(vdwType);
		} catch (Exception e) {
			logger.warning("Exception parsing VDW type:\n" + input + "\n" + e);
		}
	}
}
