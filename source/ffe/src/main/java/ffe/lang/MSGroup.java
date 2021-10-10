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
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Logger;

import org.jogamp.java3d.BranchGroup;
import org.jogamp.java3d.Material;
import org.jogamp.vecmath.Color3f;

/*
 * The MSGroup class has one subnode containing atoms, and one
 * that contains molecular mechanics/geometry terms.
 */
public abstract class MSGroup extends MSNode {
	/*
	 * Constructs the Geometry of this MultiScaleGroup and stores
	 * the terms as children in the Term node. Bonds are made based
	 * purely on distance.
	 */
	private static double[] da = new double[3];

	private static double[] db = new double[3];

	private Logger logger = Logger.getLogger("ffe");
	
	// Atoms Node
	private MSNode atomNode = new MSNode("Atoms");

	// Geometry Nodes
	private MSNode termNode = new MSNode("Connectivity");

	private MSNode bondNode = new MSNode("Bonds");

	private MSNode angleNode = new MSNode("Angles");

	private MSNode dihedralNode = new MSNode("Dihedrals");

	private boolean bondsKnown = true;

	// Whether the terms are current
	private boolean finalized;

	// Center of the MultiScaleGroup
	private double[] center;

	// List of underconstrained Atoms
	private ArrayList<Atom> dangelingatoms;

	/*
	 * Default Constructor initializes a MultiScaleGroup
	 * and a few of its subnodes.
	 */
	public MSGroup() {
		super("", 2);
		finalized = false;
		termNode.add(bondNode);
		termNode.add(angleNode);
		termNode.add(dihedralNode);
		add(atomNode);
		add(termNode);
	}

	/*
	 * Constructs a MultiScaleGroup object with name n.
	 */
	public MSGroup(String n, boolean bk) {
		this();
		setName(n);
		bondsKnown = bk;
	}

	/*
	 * Constructs a MultiScaleGroup object with name n and sets its AtomGroup
	 * node equals to node.
	 */
	public MSGroup(String n, MSNode node, boolean bk) {
		this(n, bk);
		atomNode = node;
		add(atomNode);
	}

	/*
	 * Abstract method that should specify how to add various MSNodes
	 * subclasses (such as Atoms, Residues and Polymers) to the MSGroup
	 */
	public abstract void addMSNode(MSNode m);

	public void collectValenceTerms() {
		MSNode b = new MSNode("Bonds");
		MSNode a = new MSNode("Angles");
		MSNode d = new MSNode("Dihedrals");

		// Collect all bonds for which both atoms are in this MultiScaleGroup
		ArrayList<Bond> newBonds = new ArrayList<Bond>();
		for (Atom a1 : getAtomList()) {
			if (!(a1.getNumBonds() == 0)) {
				for (Bond bond : a1.getBonds()) {
					if (bond.sameGroup() && bond.getParent() == null) {
						b.add(bond);
						newBonds.add(bond);
					}
				}
			}
		}
		b.setName("Bonds (" + b.getChildCount() + ")");
		setBonds(b);
		// Collect Angles
		ArrayList<Angle> angleAL = new ArrayList<Angle>();
		for (Atom a1 : getAtomList()) {
			int index = 0;
			if (a1.getBonds() != null) {
				for (Bond b1 : a1.getBonds()) {
					index++;
					for (ListIterator<Bond> li = a1.getBonds().listIterator(
							index); li.hasNext();) {
						Bond b2 = li.next();
						Angle an1 = new Angle(b1, b2);
						a.insert(an1, 0);
						angleAL.add(an1);
					}
				}
			}
		}
		a.setName("Angles (" + a.getChildCount() + ")");
		setAngles(a);
		// Collect Dihedrals
		for (Bond b1 : newBonds) {
			Atom a1 = b1.getAtom(0);
			Atom a2 = b1.getAtom(1);
			ArrayList<Bond> a1Bonds = a1.getBonds();
			ArrayList<Bond> a2Bonds = a2.getBonds();
			if (a1Bonds != null && a2Bonds != null) {
				for (Bond b2 : a1.getBonds()) {
					if (b2 != b1) {
						for (Bond b3 : a2.getBonds()) {
							if (b3 != b1) {
								d.add(new Dihedral(b2, b1, b3));
							}
						}
					}
				}
			}
		}
		d.setName("Dihedrals (" + d.getChildCount() + ")");
		setDihedrals(d);
	}

	public void constructValenceTerms() {
		MSNode b = new MSNode("Bonds");
		MSNode a = new MSNode("Angles");
		MSNode d = new MSNode("Dihedrals");
		int index = 0;
		ArrayList<Atom> atomList = getAtomList();
		for (Atom a1 : atomList) {
			index++;
			for (ListIterator li = atomList.listIterator(index); li.hasNext();) {
				Atom a2 = (Atom) li.next();
				a1.getXYZ(da);
				a2.getXYZ(db);
				double d1 = VectorMath.dist(da, db);
				double d2 = Bond.BUFF + a1.getVDWR() / 2 + a2.getVDWR() / 2;
				if (d1 < d2) {
					ArrayList<Bond> adjunctBonds = new ArrayList<Bond>();
					if (a1.getNumBonds() > 0) {
						adjunctBonds.addAll(a1.getBonds());
					}
					if (a2.getNumBonds() > 0) {
						adjunctBonds.addAll(a2.getBonds());
					}
					Bond newbond = new Bond(a1, a2, 1);
					b.add(newbond);
					for (Bond adjunctBond : adjunctBonds) {
						if (newbond == adjunctBond) {
							logger.info("New Bond = Adjunct Bond");
						} else {
							Angle newangle = new Angle(newbond, adjunctBond);
							a.add(newangle);
							Atom atom13 = adjunctBond.getOtherAtom(newbond);
							for (Bond bond14 : atom13.getBonds()) {
								if (bond14 != adjunctBond) {
									d.add(new Dihedral(newangle, bond14));
								}
							}
						}
					}
				}
			}
		}
		setBonds(b);
		setAngles(a);
		setDihedrals(d);
	}

	/*
	 * Joiner joins
	 * 
	 * @param bond
	 *            Bond
	 * @return Joint
	 */
	public Joint createJoint(Bond bond, MSGroup g1, MSGroup g2) {
		MSNode b = new MSNode("Bonds");
		MSNode a = new MSNode("Angles");
		MSNode d = new MSNode("Dihedrals");
		// Collect all bonds for which one atom is in each MultiScaleGroup
		b.add(bond);
		b.setName("Bonds (" + b.getChildCount() + ")");
		// Collect Angles that include the joining bond(s)
		ArrayList<Angle> angleAL = new ArrayList<Angle>();
		// MultiScaleGroup #1
		Atom a1 = bond.getAtom(0);
		for (ROLS m2 : a1.getBonds()) {
			Bond b2 = (Bond) m2;
			if (bond == b2) {
				continue;
			}
			if (bond.getOtherAtom(b2) != null) {
				Angle an1 = new Angle(bond, b2);
				a.add(an1);
				angleAL.add(an1);
			}
		}
		// MultiScaleGroup #2
		Atom a2 = bond.getAtom(1);
		for (ROLS m2 : a2.getBonds()) {
			Bond b2 = (Bond) m2;
			if (bond == b2) {
				continue;
			}
			if (bond.getOtherAtom(b2) != null) {
				Angle an1 = new Angle(bond, b2);
				a.add(an1);
				angleAL.add(an1);
			}
		}
		a.setName("Angles (" + a.getChildCount() + ")");
		Joint j = new Joint(g1, g2, b, a, d);
		return j;
	}

	/*
	 * Joiner joins Moieties m1 and m2 and returns
	 * the Geometry objects in a Joint.
	 */
	public Joint createJoint(MSGroup m1, MSGroup m2) {
		Joint joint = null;
		for (Atom a1 : m1.getAtomList()) {
			a1.getXYZ(da);
			for (Atom a2 : m2.getAtomList()) {
				a2.getXYZ(db);
				double d1 = VectorMath.dist(da, db);
				double d2 = Bond.BUFF + a1.getVDWR() / 2 + a2.getVDWR() / 2;
				if (d1 < d2) {
					Bond b = new Bond(a1, a2, 1);
					Joint newJoint = createJoint(b, m1, m2);
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
	 * Abstact method that should specify how to finalize a MultiScaleGroup
	 */
	public abstract void finalize(boolean finalizeGroups);

	/*
	 * This method constructs an ArrayList of atoms which are
	 * are under-constrained. (ie They can except more bonds)
	 */
	public void findDangelingAtoms() {
		ArrayList<Atom> d = new ArrayList<Atom>();
		for (Atom a : getAtomList()) {
			if (a.isDangeling()) {
				d.add(a);
			}
		}
		setDangelingAtoms(d);
	}

	/*
	 * Returns the MultiScaleGroup's angles FNode.
	 */
	public MSNode getAngles() {
		return angleNode;
	}

	/*
	 * Returns the AtomNode.
	 */
	public MSNode getAtomNode() {
		return atomNode;
	}

	/*
	 * Returns the MSNode at the given index.
	 */
	public MSNode getAtomNode(int index) {
		return (MSNode) getAtomNodeList().get(index);
	}

	/*
	 * Returns the AtomNode specified by the String n.
	 */
	public MSNode getAtomNode(String n) {
		int i = getAtomNodeList().indexOf(new MSNode(n));
		if (i == -1) {
			return null;
		}
		return (MSNode) getAtomNodeList().get(i);
	}

	/*
	 * Returns an ArrayList of the AtomNode's children.
	 */
	public ArrayList getAtomNodeList() {
		return atomNode.getChildList();
	}

	/*
	 * Returns the Bond at the supplied index.
	 */
	public Bond getBond(int index) {
		return (Bond) bondNode.getChildAt(index);
	}

	/*
	 * Returns the Bond with the given id.
	 */
	public Bond getBond(String id) {
		int i = bondNode.getIndex(new Bond(id));
		if (i == -1) {
			return null;
		}
		return (Bond) bondNode.getChildAt(i);
	}

	/*
	 * Returns the MultiScaleGroup's bonds FNode.
	 */
	public MSNode getBonds() {
		return bondNode;
	}

	public boolean getBondsKnown() {
		return bondsKnown;
	}

	/*
	 * Returns the MultiScaleGroup's center as a double[3].
	 */
	public double[] getCenter() {
		return center;
	}

	/*
	 * Returns the MultiScaleGroup's impropers FNode.
	 */
	// public FNode getImpropers(){ return impropers; }
	/*
	 * Returns the MultiScaleGroup's dangelingatoms list.
	 */
	public ArrayList getDangelingAtoms() {
		return dangelingatoms;
	}

	/*
	 * Returns the MultiScaleGroup's dihedrals FNode.
	 */
	public MSNode getDihedrals() {
		return dihedralNode;
	}

	/*
	 * This method finds the Geometrical center of this MultiScaleGroup,
	 * or the mass-weighted center if w is set to true, and returns
	 * it as a double[3].
	 */
	public double[] getMultiScaleCenter(boolean w) {
		// Find the center of mass if w == true, the center of geometry if w ==
		// false

		double[] Rc = { 0.0d, 0.0d, 0.0d };
		ArrayList<Atom> atoms = getAtomList();
		if (atoms == null) {
			return Rc;
		}
		double sum = 0.0d;
		if (w) {
			for (Atom a : atoms) {
				double mass = a.getMass();
				Rc[0] += mass * a.getX();
				Rc[1] += mass * a.getY();
				Rc[2] += mass * a.getZ();
				sum += mass;
			}
		} else {
			for (Atom a : atoms) {
				Rc[0] += a.getX();
				Rc[1] += a.getY();
				Rc[2] += a.getZ();
			}
			sum = atoms.size();
		}
		Rc[0] /= sum;
		Rc[1] /= sum;
		Rc[2] /= sum;
		return Rc;
	}

	/*
	 * Returns the MultiScaleGroup's terms FNode.
	 */
	public MSNode getTerms() {
		return termNode;
	}

	/*
	 * Returns true if the MultiScaleGroup is finalized.
	 */
	public boolean isFinalized() {
		return finalized;
	}

	/*
	 * Prints the MultiScaleGroup's Atoms and Bonds.
	 */
	public void print() {
		super.print();
		for (Atom a : atomNode.getAtomList()) {
			a.print();
		}
		for (ROLS m : bondNode.getBondList()) {
			Bond b = (Bond) m;
			b.print();
		}
	}

	protected void removeLeaves() {
		if (termNode.getParent() == null) {
			return;
		}
		if (bondNode.getChildCount() == 0 && !(bondNode.getParent() == null)) {
			termNode.remove(bondNode);
		}
		if (angleNode.getChildCount() == 0 && !(angleNode.getParent() == null)) {
			termNode.remove(angleNode);
		}
		if (dihedralNode.getChildCount() == 0
				&& !(dihedralNode.getParent() == null)) {
			termNode.remove(dihedralNode);
		}
		if (termNode.getChildCount() == 0) {
			remove(termNode);
		}
	}

	/*
	 * Sets the MultiScaleGroup's angles node to t.
	 */
	public void setAngles(MSNode t) {
		termNode.remove(angleNode);
		angleNode = t;
		termNode.add(angleNode);
	}

	/*
	 * Sets the MultiScaleGroup's moieties node to t.
	 */
	public void setAtomNode(MSNode t) {
		remove(atomNode);
		atomNode = t;
		add(atomNode);
	}

	/*
	 * Sets the MultiScaleGroup's bonds node to t.
	 */
	public void setBonds(MSNode t) {
		termNode.remove(bondNode);
		bondNode.removeAllChildren();
		bondNode = t;
		termNode.add(bondNode);
	}

	/*
	 * Sets the BondsKnown Variable
	 */
	public void setBondsKnown(boolean b) {
		bondsKnown = b;
	}

	/*
	 * Set the value of Center to d.
	 */
	public void setCenter(double[] d) {
		center = d;
	}

	public void setColor(RendererCache.ColorModel newColorModel, Color3f color,
			Material mat) {
		if (newColorModel == RendererCache.ColorModel.MOLECULE
				&& (color == null || mat == null)) {
			return;
		}
		atomNode.setColor(newColorModel, color, mat);
	}

	/*
	 * Sets the MultiScaleGroup's impropers node to t.
	 */
	/*
	 * public void setImpropers(FNode t){ terms.remove(impropers); impropers =
	 * t; terms.add(impropers); }
	 */
	/*
	 * Sets the MultiScaleGroup's dangelingatoms member to a.
	 */
	public void setDangelingAtoms(ArrayList<Atom> a) {
		dangelingatoms = a;
	}

	/*
	 * Sets the MultiScaleGroup's dihedrals node to t.
	 */
	public void setDihedrals(MSNode t) {
		termNode.remove(dihedralNode);
		dihedralNode = t;
		termNode.add(dihedralNode);
	}

	// Public Mutators
	/*
	 * Specifies whether the MultiScaleGroup has been finalized.
	 */
	public void setFinalized(boolean t) {
		finalized = t;
	}

	/*
	 * Sets the MultiScaleGroup's terms node to t.
	 */
	public void setTerms(MSNode t) {
		remove(termNode);
		termNode = t;
		add(termNode);
	}

	public void setView(RendererCache.ViewModel newViewModel,
			List<BranchGroup> newShapes) {
		atomNode.setView(newViewModel, newShapes);
		bondNode.setView(newViewModel, newShapes);
	}

	/*
	 * Returns the MultiScaleGroup's name.
	 */
	public String toString() {
		return getName();
	}

	public void update() {
		updateAtoms();
		updateBonds();
	}

	public void updateAtoms() {
		for (Atom a : getAtomList()) {
			a.update();
		}
	}

	public void updateBonds() {
		for (ROLS b : getBondList()) {
			b.update();
		}
	}
}
