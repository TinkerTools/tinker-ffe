/*
 * <p>Title: Force Field Explorer</p>
 * <p>Description: Force Field Explorer Molecular Modeling Program</p>
 * <p>Copyright: Copyright (c) 2004-2020 Jay William Ponder</p>
 * <p>Institution: Jay Ponder Lab, Washington University in Saint Louis</p>
 * @author Michael J. Schnieders
 * @version 8.8
 */

package ffe.mm;

import java.io.File;
import java.util.TreeMap;
import java.util.logging.Logger;

/*
 * The ForceField class organizes parameters for
 * a molecular mechanics force field.
 */
public class ForceField {
	public enum ForceFieldDouble {
		ANGLE_CUBIC, ANGLE_QUARTIC, ANGLE_PENTIC, ANGLE_SEXTIC, BOND_CUBIC, BOND_QUARTIC, OPBENDUNIT, TORSIONUNIT, DIELECTRIC, POLAR_DAMP, VDW_13_SCALE, VDW_14_SCALE, VDW_15_SCALE, MPOLE_11_SCALE, MPOLE_12_SCALE, MPOLE_13_SCALE, MPOLE_14_SCALE, MPOLE_15_SCALE, POLAR_11_SCALE, POLAR_12_SCALE, POLAR_13_SCALE, POLAR_14_SCALE, POLAR_15_SCALE, DIRECT_11_SCALE, DIRECT_12_SCALE, DIRECT_13_SCALE, DIRECT_14_SCALE, MUTUAL_11_SCALE, MUTUAL_12_SCALE, MUTUAL_13_SCALE, MUTUAL_14_SCALE
	}

	public enum ForceFieldString {
		EPSILONRULE, FORCEFIELD, RADIUSRULE, RADIUSSIZE, RADIUSTYPE, POLARIZATION, VDWTYPE
	}

	public enum ForceFieldType {
		ATOM, ANGLE, BIOTYPE, BOND, CHARGE, MULTIPOLE, OPBEND, PITORS, POLARIZE, STRBND, TORSION, TORTORS, UREYBRAD, VDW
	}

	public File forceFieldFile;
	public File keyFile;

	private final Logger logger;
	private final TreeMap<String, AngleType> angle;
	private final TreeMap<String, AtomType> atom;
	private final TreeMap<String, BondType> bond;
	private final TreeMap<String, BioType> biotype;
	private final TreeMap<String, ChargeType> charge;
	private final TreeMap<String, MultipoleType> multipole;
	private final TreeMap<String, OutOfPlaneBendType> opbend;
	private final TreeMap<String, PolarizeType> polarize;
	private final TreeMap<String, StretchBendType> strbnd;
	private final TreeMap<String, PiTorsionType> pitors;
	private final TreeMap<String, TorsionType> torsion;
	private final TreeMap<String, TorsionTorsionType> tortor;
	private final TreeMap<String, UreyBradleyType> ureybrad;
	private final TreeMap<String, VDWType> vdw;
	private final TreeMap<ForceFieldType, TreeMap> types;
	private final TreeMap<ForceFieldString, String> keyString;
	private final TreeMap<ForceFieldDouble, Double> keyDouble;

	/*
	 * ForceField Constructor.
	 */
	public ForceField(File forceFieldFile, File keyFile) {
		this.forceFieldFile = forceFieldFile;
		this.keyFile = keyFile;
		logger = Logger.getLogger("ffe");
		angle = new TreeMap<String, AngleType>();
		atom = new TreeMap<String, AtomType>();
		bond = new TreeMap<String, BondType>();
		biotype = new TreeMap<String, BioType>();
		charge = new TreeMap<String, ChargeType>();
		opbend = new TreeMap<String, OutOfPlaneBendType>();
		multipole = new TreeMap<String, MultipoleType>();
		pitors = new TreeMap<String, PiTorsionType>();
		polarize = new TreeMap<String, PolarizeType>();
		strbnd = new TreeMap<String, StretchBendType>();
		torsion = new TreeMap<String, TorsionType>();
		tortor = new TreeMap<String, TorsionTorsionType>();
		ureybrad = new TreeMap<String, UreyBradleyType>();
		vdw = new TreeMap<String, VDWType>();
		types = new TreeMap<ForceFieldType, TreeMap>();
		types.put(ForceFieldType.ANGLE, angle);
		types.put(ForceFieldType.ATOM, atom);
		types.put(ForceFieldType.BOND, bond);
		types.put(ForceFieldType.BIOTYPE, biotype);
		types.put(ForceFieldType.CHARGE, charge);
		types.put(ForceFieldType.OPBEND, opbend);
		types.put(ForceFieldType.MULTIPOLE, multipole);
		types.put(ForceFieldType.PITORS, pitors);
		types.put(ForceFieldType.POLARIZE, polarize);
		types.put(ForceFieldType.STRBND, strbnd);
		types.put(ForceFieldType.TORSION, torsion);
		types.put(ForceFieldType.TORTORS, tortor);
		types.put(ForceFieldType.UREYBRAD, ureybrad);
		types.put(ForceFieldType.VDW, vdw);
		keyString = new TreeMap<ForceFieldString, String>();
		keyDouble = new TreeMap<ForceFieldDouble, Double>();
	}

	/*
	 * Add a force field keyword that is represented by a double.
	 * 
	 * @param keyword
	 *            ForceFieldDouble
	 * @param value
	 *            double
	 */
	public void addForceFieldDouble(ForceFieldDouble keyword, double value) {
		if (keyDouble.containsKey(keyword)) {
			String oldValue = keyString.get(keyword);
			logger.warning("Old " + keyword + " value: " + oldValue
					+ "\nReplaced with: " + value);
		}
		keyDouble.put(keyword, value);
	}

	/*
	 * Store a force field keyword that is represented by a String.
	 * 
	 * @param keyword
	 *            ForceFieldString
	 * @param newEntry
	 *            String
	 */
	public void addForceFieldString(ForceFieldString keyword, String newEntry) {
		if (keyString.containsKey(keyword)) {
			String oldValue = keyString.get(keyword);
			logger.warning("Old " + keyword + " value: " + oldValue
					+ "\nReplaced with: " + newEntry);
		}
		keyString.put(keyword, newEntry);
	}

	/*
	 * Add an instance of a force field type. Force Field types are
	 * more complicated than simple Strings or doubles, in that they
	 * have multiple fields and may occur multiple times.
	 * 
	 * @param type
	 *            BaseType
	 */
	public void addForceFieldType(BaseType type) {
		if (type == null) {
			logger.info("Force field type is NULL.");
			return;
		}
		TreeMap treeMap = types.get(type.forceFieldType);
		if (treeMap == null) {
			logger
					.info("Unrecognized Force Field Type: "
							+ type.forceFieldType);
			type.print();
		}
		if (treeMap.containsKey(type.key)) {
			logger.fine("A force field entry of type " + type.forceFieldType
					+ " already exists with the key: " + type.key
					+ "\nThe (discarded) old entry:\n"
					+ treeMap.get(type.key).toString() + "\nThe new entry:\n"
					+ type.toString());
		}
		Class baseTypeClass = type.getClass();
		treeMap.put(type.key, baseTypeClass.cast(type));
	}

	public BaseType getForceFieldType(ForceFieldType type, String key) {
		TreeMap<String, BaseType> treeMap = (TreeMap<String, BaseType>) types
				.get(type);
		if (treeMap == null) {
			logger.warning("Unrecognized Force Field Type: " + type);
			return null;
		}
		return treeMap.get(key);
	}

	public int getForceFieldTypeCount(ForceFieldType type) {
		TreeMap<String, BaseType> treeMap = (TreeMap<String, BaseType>) types
				.get(type);
		if (treeMap == null) {
			logger.warning("Unrecognized Force Field Type: " + type);
			return 0;
		}
		return treeMap.size();
	}

	public void print() {
		for (ForceFieldString s : keyString.keySet()) {
			print(s.toString());
		}
		for (ForceFieldDouble s : keyDouble.keySet()) {
			print(s.toString());
		}
		for (ForceFieldType s : types.keySet()) {
			print(s.toString());
		}
	}

	/*
	 * Prints any force field keyword to Standard.out.
	 * 
	 * @param keyword
	 *            String
	 */
	public void print(String keyword) {
		String string = toString(keyword);
		if (string == null) {
			logger.warning(keyword + " was not recognized");
			return;
		}
		logger.info(string);
	}

	/*
	 * Return a String for any Force Field keyword.
	 * 
	 * @param keyword
	 *            String
	 * @return String
	 */
	public String toString(String keyword) {
		keyword = keyword.toUpperCase().replaceAll("-", "_");
		try {
			ForceFieldString forceFieldString = ForceFieldString
					.valueOf(keyword);
			String value = keyString.get(forceFieldString);
			if (value == null) {
				return null;
			}
			String string = String.format("%-25s  %s", keyword.toLowerCase()
					.replaceAll("_", "-"), value.toUpperCase());
			return string;
		} catch (Exception e) {
			try {
				ForceFieldDouble forceFieldDouble = ForceFieldDouble
						.valueOf(keyword);
				Double value = keyDouble.get(forceFieldDouble);
				if (value == null) {
					return null;
				}
				String string = String.format("%-25s  %g", keyword
						.toLowerCase().replaceAll("_", "-"), value
						.doubleValue());
				return string;
			} catch (Exception e2) {
				try {
					ForceFieldType type = ForceFieldType.valueOf(keyword);
					StringBuffer stringBuffer = new StringBuffer();
					TreeMap t = types.get(type);
					for (Object o : t.values()) {
						stringBuffer.append(o.toString() + "\n");
					}
					return stringBuffer.toString();
				} catch (Exception e3) {
					return null;
				}
			}
		}
	}
}
