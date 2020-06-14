/*
 * <p>Title: Force Field Explorer</p>
 * <p>Description: Force Field Explorer Molecular Modeling Program</p>
 * <p>Copyright: Copyright (c) 2004-2020 Jay William Ponder</p>
 * <p>Institution: Jay Ponder Lab, Washington University in Saint Louis</p>
 * @author Michael J. Schnieders
 * @version 8.8
 */

package ffe.lang;

import java.util.Hashtable;
import java.util.List;
import java.util.logging.Logger;

import org.jogamp.java3d.BranchGroup;
import org.jogamp.java3d.Material;
import org.jogamp.vecmath.Color3f;

/*
 * The ValenceTerm class is extended by all Valence Geometry classes
 * (bond, angle, dihedral, improper).
 */
public abstract class ValenceTerm extends MSNode {
	
	private Logger logger = Logger.getLogger("ffe");
	
	/*
	 * This method sets the Term's id and key by concatenating the
	 * respective id and keys of the Atoms that are used in forming
	 * the term. Order can be reversed for help in finding corresponding
	 * Molecular Mechanics file entries for the Term.
	 */
	private static StringBuffer idtemp = new StringBuffer();

	private static StringBuffer keytemp = new StringBuffer();

	private String id;

	private String key; // Concatenated atom types

	protected Atom atoms[]; // Atoms that are used to form this term

	protected Bond bonds[]; // Bonds that are used to form this term

	private double value; // Value of the term

	private ValenceType valenceType; // Molecular mechanics parameters

	/*
	 * Default Constructor
	 */
	public ValenceTerm() {
		super("", 1);
		setAllowsChildren(false);
	}

	/*
	 * Constructor which sets the Term's id.
	 */
	public ValenceTerm(String i) {
		this();
		id = i;
	}

	public boolean destroy() {
		super.destroy();
		id = null;
		key = null;
		value = 0;
		valenceType = null;
		return true;
	}

	/*
	 * Overidden method that returns true if object is not equals to this,
	 * is of the same Class and has the same id.
	 */
	public final boolean equals(Object object) {
		if (this == object) {
			return true;
		} else if (object == null || getClass() != object.getClass()) {
			return false;
		}
		ValenceTerm other = (ValenceTerm) object;
		return getID() == other.getID();
	}

	/*
	 * Get the constituent Atom specified by index.
	 */
	public Atom getAtom(int index) {
		if (index >= 0 && index < atoms.length) {
			return atoms[index];
		}
		return null;
	}

	/*
	 * Get the constituent Bond specified by index.
	 */
	public Bond getBond(int index) {
		if (index >= 0 && index < atoms.length) {
			return bonds[index];
		}
		return null;
	}

	/*
	 * Get the Term's id.
	 */
	public String getID() {
		return new String(id);
	}

	/*
	 * Get the Term's key.
	 */
	public String getKey() {
		return new String(key);
	}

	/*
	 * Get the Term's Molecular Mechanics reference.
	 */
	public ValenceType getMM() {
		return valenceType;
	}

	/*
	 * Get the Term's value.
	 */
	public double getValue() {
		return value;
	}

	public final int hashCode() {
		return HashCodeUtil.hash(HashCodeUtil.BONDTERMSEED, getID().hashCode());
	}

	/*
	 * Prints the toString method to stdout
	 */
	public void print() {
		logger.info(toString());
	}

	/*
	 * Add a constituent Atom to the Term.
	 */
	public void setAtoms(Atom a[]) {
		atoms = a;
	}

	/*
	 * Add a constituent Bond to the Term.
	 */
	public void setBonds(Bond b[]) {
		bonds = b;
	}

	public void setColor(RendererCache.ColorModel newColorModel, Color3f color,
			Material mat) {
		if (atoms == null) {
			return;
		}
		for (Atom atom : atoms) {
			atom.setColor(newColorModel, color, mat);
		}
	}

	/*
	 * Sets the Term's id.
	 */
	public void setID(String i) {
		id = new String(i);
	}

	public final void setID_Key(boolean reverse) {
		Atom a;
		// Reuse the string buffers
		if (idtemp.length() > 0) {
			idtemp.delete(0, idtemp.length() - 1);
		}
		if (keytemp.length() > 0) {
			keytemp.delete(0, keytemp.length() - 1);
		}
		if (atoms != null) {
			if (!reverse) {
				for (int i = 0; i < atoms.length; i++) {
					a = atoms[i];
					if (i != 0) {
						idtemp.append("-").append(a.getID());
					} else {
						idtemp.append(a.getID());
					}
					keytemp.append(a.getKey());
				}
			} else { // Reverse Order
				for (int i = 0; i < atoms.length; i++) {
					a = atoms[i];
					if (i != 0) {
						idtemp.append("-").append(a.getID());
					} else {
						idtemp.append(a.getID());
					}
					keytemp.append(a.getKey());
				}
			}
			id = idtemp.substring(1).toString().intern();
			key = keytemp.toString().intern();
		}
	}

	/*
	 * Sets the Term's energy.
	 */
	// public void setEnergy(double e) { energy = e; }
	/*
	 * Sets the Term's key.
	 */
	public void setKey(String k) {
		key = new String(k);
	}

	/*
	 * Sets a Reference to Molecular Mechanics data (MMTerm) for the Term
	 */
	public void setMM(Hashtable data) {
		valenceType = (ValenceType) data.get(key);
		if (valenceType == null) {
			setID_Key(true);
			valenceType = (ValenceType) data.get(key);
		}
	}

	/*
	 * Sets a reference to Molecular Mechcanics Data for this term.
	 */
	public void setMM(ValenceType mmterm) {
		valenceType = mmterm;
	}

	public void setSelected(boolean b) {
		super.setSelected(b);
		if (atoms == null) {
			return;
		}
		for (Atom a : atoms) {
			a.setSelected(b);
		}
		if (!(this instanceof Bond)) {
			if (bonds == null) {
				return;
			}
			for (Bond bond : bonds) {
				bond.setSelected(b);
			}
		}
	}

	/*
	 * Sets the Term's value.
	 */
	public void setValue(double v) {
		value = v;
	}

	public void setView(RendererCache.ViewModel newViewModel,
			List<BranchGroup> newShapes) {
		if (atoms == null) {
			return;
		}
		for (Atom atom : atoms) {
			atom.setView(newViewModel, newShapes);
		}
		if (bonds == null) {
			return;
		}
		for (Bond bond : bonds) {
			bond.setView(newViewModel, newShapes);
		}
	}

	/*
	 * Overidden toString Method returns the Term's id.
	 */
	public String toString() {
		return String.format("%s: %7.1f", id, value);
	}

	public abstract void update();
}
