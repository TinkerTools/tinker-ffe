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
import java.util.Enumeration;
import java.util.List;
import java.util.ListIterator;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import org.jogamp.java3d.BranchGroup;
import org.jogamp.java3d.Canvas3D;
import org.jogamp.java3d.J3DGraphics2D;
import org.jogamp.java3d.Material;
import org.jogamp.java3d.Node;
import org.jogamp.vecmath.Color3f;

/*
 * The MSNode class forms the basic unit that all data classes extend.
 */
public class MSNode extends DefaultMutableTreeNode implements ROLS {
	/*
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// UNIT TESTING FLAG
	public static boolean UNIT_TESTING = false;
	static {
		try {
			boolean b = Boolean.parseBoolean(System.getProperty("ffe.junit",
					"false"));
			UNIT_TESTING = b;
		} catch (Exception e) {
			UNIT_TESTING = false;
		}
	}

	public final int MultiScaleLevel;

	private String name;

	protected boolean selected = false;

	/*
	 * Default MSNode Constructor
	 */
	public MSNode() {
		name = null;
		MultiScaleLevel = ROLS.MaxLengthScale;
	}

	/*
	 * Constructs a MSNode with the name n.
	 */
	public MSNode(String n) {
		name = n;
		MultiScaleLevel = ROLS.MaxLengthScale;
	}

	public MSNode(String n, int multiScaleLevel) {
		this.name = n;
		this.MultiScaleLevel = multiScaleLevel;
	}

	/*
	 * Returns true if Class c can be below this Class in the Hierarchy
	 * 
	 * @param c
	 *            Class
	 * @return boolean
	 */
	public boolean canBeChild(Class c) {
		try {
			int multiScaleLevel = c.getDeclaredField("MultiScaleLevel").getInt(
					null);
			if (multiScaleLevel >= this.MultiScaleLevel) {
				return false;
			}
		} catch (Exception e) {
			return true;
		}
		return true;
	}

	public boolean destroy() {
		if (getParent() != null) {
			removeFromParent();
		}
		name = null;
		selected = false;
		return true;
	}

	public void drawLabel(Canvas3D graphics, J3DGraphics2D g2d, Node node) {
		if (!isSelected()) {
			return;
		}
		MSNode dataNode;
		for (Enumeration e = children(); e.hasMoreElements();) {
			dataNode = (MSNode) e.nextElement();
			dataNode.drawLabel(graphics, g2d, node);
		}
	}

	/*
	 * Overidden equals method that returns true if object is not equal
	 * to this object, is of the same class as this, and has the same name.
	 */
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		} else if (object == null || getClass() != object.getClass()) {
			return false;
		}
		MSNode other = (MSNode) object;
		return (name == other.getName());
	}

	/*
	 * Returns an ArrayList of all Atoms below the present MSNode.
	 */
	public ArrayList<Atom> getAtomList() {
		ArrayList<Atom> arrayList = new ArrayList<Atom>();
		Enumeration e = depthFirstEnumeration();
		while (e.hasMoreElements()) {
			MSNode dataNode = (MSNode) e.nextElement();
			if (dataNode instanceof Atom) {
				arrayList.add((Atom) dataNode);
			}
		}
		return arrayList;
	}

	/*
	 * Returns an ArrayList of all Bonds below the present MSNode.
	 */
	public ArrayList<ROLS> getBondList() {
		ArrayList<ROLS> arrayList = new ArrayList<ROLS>();
		return getList(Bond.class, arrayList);
	}

	public double[] getCenter(boolean w) {
		double[] Rc = { 0, 0, 0 };
		double sum = 0, mass = 1;
		ArrayList<Atom> atomList = getAtomList();
		for (Atom a : atomList) {
			if (w) {
				mass = a.getMass();
				sum += mass;
			}
			Rc[0] += mass * a.getX();
			Rc[1] += mass * a.getY();
			Rc[2] += mass * a.getZ();
		}
		if (!w) {
			sum = atomList.size();
		}
		for (int i = 0; i < 3; i++) {
			Rc[i] /= sum;
		}
		return Rc;
	}

	/*
	 * Returns an ArrayList of the MSNode's Children (instead of
	 * using an Enumeration).
	 */
	public ArrayList<MSNode> getChildList() {
		ArrayList<MSNode> l = new ArrayList<MSNode>();
		Enumeration e = children();
		while (e.hasMoreElements()) {
			l.add((MSNode) e.nextElement());
		}
		return l;
	}

	/*
	 * Returns a ListIterator containing this FNode's children.
	 */
	public ListIterator<MSNode> getChildListIterator() {
		return getChildList().listIterator();
	}

	public double getExtent() {
		double extent = 0.0;
		for (Enumeration e = children(); e.hasMoreElements();) {
			MSNode node = (MSNode) e.nextElement();
			double temp = node.getExtent();
			if (temp > extent) {
				extent = temp;
			}
		}
		return extent;
	}

	public ArrayList<ROLS> getList(Class c, ArrayList<ROLS> nodes) {
		if (c.isInstance(this)) {
			nodes.add(this);
		}
		if (isLeaf() || !canBeChild(c)) {
			return nodes;
		}
		for (Enumeration e = children(); e.hasMoreElements();) {
			ROLS node = (ROLS) e.nextElement();
			node.getList(c, nodes);
		}
		return nodes;
	}

	public long getMSCount(Class c, long count) {
		if (c.isInstance(this)) {
			count++;
		}
		if (!canBeChild(c)) {
			return count;
		}
		for (Enumeration e = children(); e.hasMoreElements();) {
			MSNode node = (MSNode) e.nextElement();
			count += node.getMSCount(c, count);
		}
		return count;
	}

	public ROLS getMSNode(Class c) {
		TreeNode[] nodes = getPath();
		for (TreeNode n : nodes) {
			if (c.isInstance(n)) {
				ROLS msm = (ROLS) n;
				return msm;
			}
		}
		return null;
	}

	public int getMultiScaleLevel() {
		return MultiScaleLevel;
	}

	public double getMW() {
		double weight = 0.0;
		for (ListIterator<Atom> li = getAtomList().listIterator(); li.hasNext();) {
			weight += li.next().getMass();
		}
		return weight;
	}

	/*
	 * Returns the name of this FNode.
	 */
	public String getName() {
		return name;
	}

	public int hashCode() {
		return HashCodeUtil.hash(HashCodeUtil.DATANODESEED, name.hashCode());
	}

	public boolean isSelected() {
		return selected;
	}

	/*
	 * Prints the MSNode's name
	 */
	public void print() {
		System.out.println(name);
	}

	public void setColor(RendererCache.ColorModel colorModel, Color3f color,
			Material mat) {
		for (Enumeration e = children(); e.hasMoreElements();) {
			MSNode node = (MSNode) e.nextElement();
			node.setColor(colorModel, color, mat);
		}
	}

	/*
	 * Sets the name of this NodeObect to n.
	 */
	public void setName(String n) {
		name = n;
	}

	public void setSelected(boolean b) {
		selected = b;
		for (Enumeration e = children(); e.hasMoreElements();) {
			MSNode node = (MSNode) e.nextElement();
			node.setSelected(b);
		}
	}

	public void setView(RendererCache.ViewModel viewModel,
			List<BranchGroup> newShapes) {
		for (Enumeration e = children(); e.hasMoreElements();) {
			MSNode node = (MSNode) e.nextElement();
			node.setView(viewModel, newShapes);
		}
	}

	/*
	 * Overidden toString method returns the MSNode's name
	 */
	public String toString() {
		return name;
	}

	public void update() {
		for (Enumeration e = children(); e.hasMoreElements();) {
			MSNode node = (MSNode) e.nextElement();
			node.update();
		}
	}
}
