/*
 * <p>Title: Force Field Explorer</p>
 * <p>Description: Force Field Explorer Molecular Modeling Program</p>
 * <p>Copyright: Copyright (c) 2004-2019 Jay William Ponder</p>
 * <p>Institution: Jay Ponder Lab, Washington University in Saint Louis</p>
 * @author Michael J. Schnieders
 * @version 8.7
 */

package ffe.lang;

import java.util.Enumeration;
import java.util.List;

import javax.swing.tree.TreeNode;

import org.jogamp.java3d.BranchGroup;

/*
 * The Joint class contains the geometry produced by the FGroup Joiner method.
 */
public class Joint extends MSNode {
	/*
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/*
	 * One of two Moieties forming this Joint
	 */
	public MSGroup m1, m2;

	/*
	 * Default Constructor
	 */
	public Joint() {
		super("Joint");
		m1 = m2 = null;
	}

	/*
	 * Constructs a Joint between Moieties m1 and m2, where the Bond, Angle and
	 * Dihedral geometry objects have been attached to FNode b,a and d,
	 * respectively
	 */
	public Joint(MSGroup m1, MSGroup m2, MSNode b, MSNode a, MSNode d) {
		super(m1.toString() + "-" + m2.toString());
		this.m1 = m1;
		this.m2 = m2;
		if (b.getChildCount() != 0) {
			add(b);
		}
		if (a.getChildCount() != 0) {
			add(a);
		}
		if (d.getChildCount() != 0) {
			add(d);
		}
		refresh(null, null, null);
	}

	public MSNode getAngles() {
		for (Enumeration e = children(); e.hasMoreElements();) {
			MSNode m = (MSNode) e.nextElement();
			TreeNode node = m.getChildAt(0);
			if (node instanceof Angle) {
				return m;
			}
		}
		return null;
	}

	public MSNode getBonds() {
		for (Enumeration e = children(); e.hasMoreElements();) {
			MSNode m = (MSNode) e.nextElement();
			TreeNode node = m.getChildAt(0);
			if (node instanceof Bond) {
				return m;
			}
		}
		return null;
	}

	public MSNode getDihedrals() {
		for (Enumeration e = children(); e.hasMoreElements();) {
			MSNode m = (MSNode) e.nextElement();
			TreeNode node = m.getChildAt(0);
			if (node instanceof Dihedral) {
				return m;
			}
		}
		return null;
	}

	public void merge(Joint j) {
		if (!((m1 == j.m1 && m2 == j.m2) || (m2 == j.m1 && m1 == j.m2))) {
			return;
		}
		refresh(j.getBonds(), j.getAngles(), j.getDihedrals());
	}

	private void refresh(MSNode bonds, MSNode angles, MSNode dihedrals) {
		for (Enumeration e = children(); e.hasMoreElements();) {
			MSNode jointChild = (MSNode) e.nextElement();
			if (jointChild.getChildCount() == 0) {
				jointChild.removeFromParent();
				continue;
			}
			MSNode node = (MSNode) jointChild.getChildAt(0);
			if (node instanceof Bond) {
				jointChild
						.setName("Bonds (" + jointChild.getChildCount() + ")");
				if (bonds != null) {
					for (Enumeration e2 = bonds.children(); e2
							.hasMoreElements();) {
						jointChild.add((MSNode) e2.nextElement());
					}
				}
				bonds = null;
			} else if (node instanceof Angle) {
				jointChild.setName("Angles (" + jointChild.getChildCount()
						+ ")");
				if (angles != null) {
					for (Enumeration e2 = angles.children(); e2
							.hasMoreElements();) {
						jointChild.add((MSNode) e2.nextElement());
					}
				}
				angles = null;
			} else if (node instanceof Dihedral) {
				jointChild.setName("Dihedrals (" + jointChild.getChildCount()
						+ ")");
				if (dihedrals != null) {
					for (Enumeration e2 = dihedrals.children(); e2
							.hasMoreElements();) {
						jointChild.add((MSNode) e2.nextElement());
					}
				}
				dihedrals = null;
			}
		}
		if (bonds != null) {
			add(bonds);
		}
		if (angles != null) {
			add(angles);
		}
		if (dihedrals != null) {
			add(dihedrals);
		}
	}

	public void setColor(RendererCache.ColorModel newColorModel) {
		MSNode bonds = getBonds();
		if (bonds == null) {
			return;
		}
		for (Enumeration e = bonds.children(); e.hasMoreElements();) {
			Bond b = (Bond) e.nextElement();
			b.setColor(b.getAtom(0));
			b.setColor(b.getAtom(1));
		}
	}

	public void setView(RendererCache.ViewModel newViewModel,
			List<BranchGroup> newShapes) {
		MSNode bonds = getBonds();
		if (bonds == null) {
			return;
		}
		for (Enumeration e = bonds.children(); e.hasMoreElements();) {
			Bond b = (Bond) e.nextElement();
			b.setView(newViewModel, newShapes);
		}
	}

	/*
	 * Overidden toString method returns: "Joint: m1 Name - m2 Name"
	 */
	public String toString() {
		return getName();
	}
}
