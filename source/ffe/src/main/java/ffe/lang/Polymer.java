/*
 * <p>Title: Force Field Explorer</p>
 * <p>Description: Force Field Explorer Molecular Modeling Program</p>
 * <p>Copyright: Copyright (c) 2004-2019 Jay William Ponder</p>
 * <p>Institution: Jay Ponder Lab, Washington University in Saint Louis</p>
 * @author Michael J. Schnieders
 * @version 8.7
 */

package ffe.lang;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.ListIterator;
import java.util.Vector;
import java.util.logging.Logger;

import org.jogamp.java3d.BranchGroup;
import org.jogamp.java3d.Material;
import org.jogamp.vecmath.Color3f;

/*
 * The Polymer class encapsulates a polypeptide or polynucleotide chain.
 */
public class Polymer extends MSGroup {
	
	private Logger logger = Logger.getLogger("ffe");	

	private static final long serialVersionUID = 1L;

	public static final int MultiScaleLevel = 3;

	private static int count = 0;

	// private int maxResidue = 0;

	private static double[] da = new double[3];

	private static double[] db = new double[3];

	public static Hashtable<Integer, Color3f> polymerColor = new Hashtable<Integer, Color3f>();

	static {
		polymerColor.put(0, RendererCache.RED);
		polymerColor.put(1, RendererCache.ORANGE);
		polymerColor.put(2, RendererCache.YELLOW);
		polymerColor.put(3, RendererCache.GREEN);
		polymerColor.put(4, RendererCache.BLUE);
		polymerColor.put(5, RendererCache.MAGENTA);
		polymerColor.put(6, RendererCache.CYAN);
		polymerColor.put(7, RendererCache.WHITE);
		polymerColor.put(8, RendererCache.GRAY);
		polymerColor.put(9, RendererCache.PINK);
	}

	private boolean link = false;

	private int polymerNumber;

	/*
	 * Default Polymer Construtor
	 */
	public Polymer() {
		super();
		polymerNumber = ++count;
	}

	public Polymer(String n, boolean bk) {
		super(n, bk);
		polymerNumber = ++count;
	}

	/*
	 * Polymer Constructor
	 * 
	 * @param n
	 *            Polymer indentifier, generally a letter in PDB files (ie
	 *            A,B,C,etc)
	 */
	public Polymer(String n, boolean bk, boolean l) {
		this(n, bk);
		link = l;
	}

	/*
	 * Polymer Constructor
	 * 
	 * @param n
	 *            Polymer indentifier, generally a letter in PDB files (ie
	 *            A,B,C,etc)
	 * @param residues
	 *            Represents a FNode where the Polymer's residues have been
	 *            attached
	 */
	public Polymer(String n, MSNode residues, boolean bk) {
		super(n, residues, bk);
		polymerNumber = ++count;
	}

	/*
	 * A generic method for adding a MSNode to the Polymer.
	 * 
	 * @param o
	 *            If the MSNode is a Residue, it will be added to the Polymer, 
	 *            so long as its sequence number is not already in use.
	 */
	public void addMSNode(MSNode o) {
		if (!(o instanceof Residue)) {
			logger.warning("Polymer can not contain nodes of type: "
					+ o.getClass());
		} else {
			getAtomNode().add(o);
		}
	}

	/*
	 * Joiner joins Moieties m1 and m2 and returns the Geometry
	 * objects formed in a Joint.
	 */
	public Joint createJoint(Residue residue1, Residue residue2) {
		Joint joint = null;
		for (Enumeration e = residue1.getAtomNode().children(); e
				.hasMoreElements();) {
			Atom a1 = (Atom) e.nextElement();
			a1.getXYZ(da);
			for (Enumeration e2 = residue2.getAtomNode().children(); e2
					.hasMoreElements();) {
				Atom a2 = (Atom) e2.nextElement();
				a2.getXYZ(db);
				double d1 = VectorMath.dist(da, db);
				double d2 = Bond.BUFF + a1.getVDWR() / 2 + a2.getVDWR() / 2;
				if (d1 < d2) {
					Bond b = new Bond(a1, a2, 1);
					Joint newJoint = createJoint(b, residue1, residue2);
					if (joint != null) {
						joint.merge(newJoint);
					} else {
						joint = newJoint;
					}
				}
			}
		}
		return joint;
	}

	/*
	 * Overidden equals method.
	 * 
	 * @param object
	 *            Object to compare
	 * @return True if object is not <b>this</b> Polymer, is of Class Polymer,
	 *         and both object and this Polymer have identical names
	 */
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		} else if (object == null || getClass() != object.getClass()) {
			return false;
		}
		Polymer other = (Polymer) object;
		return (getName() == other.getName());
	}

	/*
	 * Finalize should be called after all the Residues have been added to the
	 * Polymer. This method in turn calls the Finalize method of each Residue,
	 * then forms Joints between adjacent Residues in the Polymer
	 */
	public void finalize(boolean finalizeGroups) {
		ListIterator li, lj;
		ArrayList res = getAtomNodeList();
		setFinalized(false);
		// Finalize the residues in the Polymer
		if (finalizeGroups) {
			for (li = res.listIterator(); li.hasNext();) {
				((Residue) li.next()).finalize(true);
			}
		}
		// Join the residues in the Polymer
		if (link) {
			getAtomNode().setName("Residues " + "(" + res.size() + ")");
			Joint j;
			MSNode joints = getTerms();
			joints.removeAllChildren();
			List atoms = getAtomList();
			if (getBondsKnown()) {
				Atom a;
				Bond b;
				for (li = atoms.listIterator(); li.hasNext();) {
					a = (Atom) li.next();
					if (a.getNumBonds() > 0) {
						for (lj = a.getBonds().listIterator(); lj.hasNext();) {
							b = (Bond) lj.next();
							if (!b.sameGroup() && b.getParent() == null) {
								Residue r1 = (Residue) a
										.getMSNode(Residue.class);
								Residue r2 = (Residue) b.get1_2(a).getMSNode(
										Residue.class);
								j = createJoint(b, r1, r2);
								joints.add(j);
							}
						}
					}
				}
			} else {
				Residue next = getResidue(0);
				Residue current = null;
				for (int i = 1; i <= getAtomNode().getChildCount(); i++) {
					current = next;
					next = getResidue(i);
					if (current != null && next != null) {
						j = createJoint(current, next);
						if (j != null) {
							joints.add(j);
						}
					}
				}
			}
			getTerms()
					.setName("Linkages " + "(" + joints.getChildCount() + ")");
		} else {
			getAtomNode().setName("Sub-Groups " + "(" + res.size() + ")");
			if (getTerms().getParent() != null) {
				remove(getTerms());
			}
		}
		removeLeaves();
		setFinalized(true);
	}

	public boolean getLink() {
		return link;
	}

	/*
	 * Get the Phi Psi List for the Polymer
	 * 
	 * @return An ArrayList of Dihedral objects representing the Phi/Psi angles
	 *         of the Polymer, useful for creating Ramachandran plots
	 */
	public Vector<ArrayList<Dihedral>> getPhiPsiList() {
		MSNode dihedrals;
		ListIterator li, lj;
		Vector<ArrayList<Dihedral>> phipsi = new Vector<ArrayList<Dihedral>>();
		ArrayList<Dihedral> phi = new ArrayList<Dihedral>();
		ArrayList<Dihedral> psi = new ArrayList<Dihedral>();
		phipsi.add(phi);
		phipsi.add(psi);
		MSNode joints = getTerms();
		for (li = joints.getChildListIterator(); li.hasNext();) {
			dihedrals = ((Joint) li.next()).getDihedrals();
			for (lj = dihedrals.getChildListIterator(); lj.hasNext();) {
				Dihedral d = (Dihedral) lj.next();
				String s = d.getKey();
				// Phi
				if (s == "C-N-CA-C".intern() || s == "C-CA-N-C".intern()) {
					phi.add(d);
				}
				// Psi
				else if (s == "N-C-CA-N".intern() || s == "N-CA-C-N".intern()) {
					psi.add(d);
				}
			}
		}
		return phipsi;
	}

	public Residue getResidue(int resNum) {
		if (resNum > 0 && getAtomNode().getChildCount() >= resNum) {
			Residue r = (Residue) getAtomNode().getChildAt(resNum - 1);
			if (r.getResidueNumber() == resNum) {
				return r;
			}
		}
		// Fall back for non-ordered children
		for (Enumeration e = getAtomNode().children(); e.hasMoreElements();) {
			Residue r = (Residue) e.nextElement();
			if (r.getResidueNumber() == resNum) {
				return r;
			}
		}
		return null;
	}

	public Residue getResidue(String resName, int resNum, boolean create) {
		if (resNum > 0 && getAtomNode().getChildCount() >= resNum) {
			Residue r = (Residue) getAtomNode().getChildAt(resNum - 1);
			if (r.getResidueNumber() == resNum
					&& r.getName().equalsIgnoreCase(resName)) {
				return r;
			}
		}
		for (Enumeration e = getAtomNode().children(); e.hasMoreElements();) {
			Residue r = (Residue) e.nextElement();
			if (r.getResidueNumber() == resNum
					&& r.getName().equalsIgnoreCase(resName)) {
				return r;
			}
		}
		if (!create) {
			return null;
		}
		Residue residue = null;
		resName = resName.toUpperCase();
		if (resName.length() == 1) {
			try {
				Residue.NA1.valueOf(resName);
				residue = new Residue(resName, resNum, false,
						Residue.ResidueType.NA);
			} catch (Exception e) {
				try {
					Residue.AA1.valueOf(resName);
					residue = new Residue(resName, resNum, false,
							Residue.ResidueType.AA);
				} catch (Exception ex) {
					;
				}
			}
		} else if (resName.length() == 2 || resName.length() == 3) {
			try {
				Residue.NA3.valueOf(resName);
				residue = new Residue(resName, resNum, false,
						Residue.ResidueType.NA);
			} catch (Exception e) {
				try {
					Residue.AA3.valueOf(resName);
					residue = new Residue(resName, resNum, false,
							Residue.ResidueType.AA);
				} catch (Exception ex) {
					;
				}
			}
		}
		if (residue == null) {
			residue = new Residue(resName, resNum, false,
					Residue.ResidueType.UNK);
		}
		addMSNode(residue);
		return residue;
	}

	public int hashCode() {
		return HashCodeUtil.hash(HashCodeUtil.POLYMERSEED, polymerNumber);
	}

	public void setColor(RendererCache.ColorModel newColorModel, Color3f color,
			Material mat) {
		// If coloring by Polymer, pass this Polymer's color
		if (newColorModel == RendererCache.ColorModel.POLYMER) {
			int index = polymerNumber % 10;
			color = polymerColor.get(index);
			mat = RendererCache.materialFactory(color);
		}
		for (ListIterator li = getAtomNodeList().listIterator(); li.hasNext();) {
			MSGroup atomGroup = (MSGroup) li.next();
			atomGroup.setColor(newColorModel, color, mat);
		}
		for (Enumeration e = getTerms().children(); e.hasMoreElements();) {
			Joint joint = (Joint) e.nextElement();
			joint.setColor(newColorModel);
		}
	}

	public void setLink(boolean t) {
		link = t;
	}
	public void setView(RendererCache.ViewModel newViewModel,
			List<BranchGroup> newShapes) {
		for (ListIterator li = getAtomNodeList().listIterator(); li.hasNext();) {
			MSGroup atomGroup = (MSGroup) li.next();
			atomGroup.setView(newViewModel, newShapes);
		}
		for (Enumeration e = getTerms().children(); e.hasMoreElements();) {
			Joint joint = (Joint) e.nextElement();
			joint.setView(newViewModel, newShapes);
		}
	}
}
