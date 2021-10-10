/*
 * <p>Title: Force Field Explorer</p>
 * <p>Description: Force Field Explorer Molecular Modeling Program</p>
 * <p>Copyright: Copyright (c) 2004-2021 Jay William Ponder</p>
 * <p>Institution: Jay Ponder Lab, Washington University in Saint Louis</p>
 * @author Michael J. Schnieders
 * @version 8.10
 */

package ffe.lang;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.ListIterator;
import java.util.Vector;
import java.util.logging.Logger;

import org.jogamp.java3d.Appearance;
import org.jogamp.java3d.BoundingSphere;
import org.jogamp.java3d.BranchGroup;
import org.jogamp.java3d.ColoringAttributes;
import org.jogamp.java3d.GeometryArray;
import org.jogamp.java3d.Group;
import org.jogamp.java3d.LineArray;
import org.jogamp.java3d.LineAttributes;
import org.jogamp.java3d.Link;
import org.jogamp.java3d.Material;
import org.jogamp.java3d.Node;
import org.jogamp.java3d.RenderingAttributes;
import org.jogamp.java3d.Shape3D;
import org.jogamp.java3d.SharedGroup;
import org.jogamp.java3d.Switch;
import org.jogamp.java3d.Transform3D;
import org.jogamp.java3d.TransformGroup;
import org.jogamp.java3d.utils.picking.PickTool;
import org.jogamp.vecmath.Color3f;
import org.jogamp.vecmath.Matrix3d;
import org.jogamp.vecmath.Point3d;
import org.jogamp.vecmath.Vector3d;

import ffe.lang.Utilities.FileType;

/*
 * The MolecularAssembly class is a collection of Polymers,
 * Hetero Molecules, Ions and Water
 */
public class MolecularAssembly extends MSGroup {
	/*
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Logger logger = Logger.getLogger("ffe");
	
	public static final int MultiScaleLevel = 4;

	public static final double KCAL_TO_KJ = 4.184;

	private static double[] a = new double[3];

	// MolecularSystem member variables
	private File file;

	private Vector3d offset;

	private FileType fileType;

	private int cycles = 1;

	private int currentCycle = 1;

	private Vector<String> altLoc = null;

	// Data Nodes
	private MSNode ions = new MSNode("Ions");

	private MSNode water = new MSNode("Water");

	private MSNode molecules = new MSNode("Hetero Molecules");

	// Tinker Simulation variables
	private Vector3d box = new Vector3d();

	private Vector3d angle = new Vector3d();

	// 3D Graphics Nodes - There is a diagram explaining the MolecularSystem
	// Scenegraph below
	private BranchGroup branchGroup;

	private TransformGroup originToRot;

	private Transform3D originToRotT3D;

	private Vector3d originToRotV3D;

	private TransformGroup rotToCOM;

	private Transform3D rotToCOMT3D;

	private Vector3d rotToCOMV3D;

	private BranchGroup base;

	private Switch switchGroup;

	private Shape3D wire;

	private BranchGroup childNodes;

	private Atom[] atomLookUp;

	private LineAttributes lineAttributes;

	private boolean visible = false;

	private ArrayList<ROLS> bondList = null;

	private ArrayList<Atom> atomList = null;

	private final ArrayList<BranchGroup> myNewShapes = new ArrayList<BranchGroup>();

	// Constructors
	public MolecularAssembly(String name, boolean bk) {
		super(name, bk);
		getAtomNode().setName("MacroMolecules");
		add(molecules);
		add(ions);
		add(water);
	}

	public MolecularAssembly(String name, MSNode Polymers, boolean bk) {
		super(name, Polymers, bk);
	}

	public void addAltLocation(String s) {
		if (altLoc == null) {
			altLoc = new Vector<String>();
		}
		if (!altLoc.contains(s)) {
			altLoc.add(s);
		}
	}

	public void addMSNode(MSNode o) {
		ArrayList Polymers = getAtomNodeList();
		if (o instanceof Atom) {
			Atom atom = (Atom) o;
			Residue res = getResidue(atom.getChain(), atom.getResidueNumber(), atom
					.getResidueName(), true);
			if (res == null) {
				logger.warning("Atom with no home");
				return;
			}
			res.addMSNode(atom);
		} else if (o instanceof Residue) {
			Residue g = (Residue) o;
			String key = g.getPolymer();
			int index = Polymers.indexOf(new Polymer(key, getBondsKnown()));
			if (index != -1) {
				Polymer c = (Polymer) Polymers.get(index); // Find the
				// requested Polymer
				c.addMSNode(g); // Add the residue to the Polymer
				setFinalized(false);
			} else {
				Polymer newc = new Polymer(key, getBondsKnown()); // Create a
				// new
				// Polymer
				newc.addMSNode(g); // Add residue
				getAtomNode().add(newc);
				setFinalized(false);
			}
		} else if (o instanceof Polymer) {
			Polymer c = (Polymer) o;
			int index = Polymers.indexOf(c);
			if (index == -1) {
				// Look into dynamically creating the tree during load time
				// getAtomNode().insert(c, getAtomNode().getChildCount());
				getAtomNode().add(c);
				setFinalized(false);
			}
		} else if (o instanceof Molecule) {
			Molecule m = (Molecule) o;
			if (m.getAtomNode().getChildCount() == 1) {
				ions.add(m);
			} else if (Utilities.isWaterOxygen((Atom) m.getAtomNode()
					.getChildAt(0))) {
				water.add(m);
			} else {
				molecules.add(m);
			}
		}
	}

	public void center() {
		double center[] = getMultiScaleCenter(false);
		offset = new Vector3d(center);
		offset.negate();
		originToRotV3D.set(offset);
		originToRotT3D.setTranslation(originToRotV3D);
		originToRot.setTransform(originToRotT3D);
		rotToCOMT3D.setIdentity();
		rotToCOM.setTransform(rotToCOMT3D);
		offset.negate();
		rotateAbout(offset);
		originToRotT3D.get(offset);
	}

	public void centerAt(double[] d) {
		double[] Rc = { 0, 0, 0 };
		double[] c = new double[3];
		ListIterator li;
		int i, num = getAtomList().size();
		for (li = getAtomList().listIterator(); li.hasNext();) {
			((Atom) li.next()).getXYZ(a);
			Rc[0] += a[0];
			Rc[1] += a[1];
			Rc[2] += a[2];
		}
		for (i = 0; i < 3; i++) {
			Rc[i] /= num;
		}
		VectorMath.diff(d, Rc, c);
		for (li = getAtomList().listIterator(); li.hasNext();) {
			((Atom) li.next()).move(c);
		}
	}

	public void centerView(boolean rot, boolean trans) {
		originToRot.getTransform(originToRotT3D);
		if (rot) {
			Matrix3d m3d = new Matrix3d();
			m3d.setIdentity();
			originToRotT3D.setRotation(m3d);
			// rotToCOMT3D.setRotation(m3d);
		}
		if (trans) {
			originToRotV3D.set(offset);
			originToRotT3D.set(originToRotV3D);
		}
		originToRot.setTransform(originToRotT3D);
		// rotToCOM.setTransform(rotToCOMT3D);
	}

	public void createBox() {
		int vertices = 8;
		LineArray la = new LineArray(4 * vertices, GeometryArray.COORDINATES
				| GeometryArray.COLOR_4 | GeometryArray.NORMALS);
		la.setCapability(LineArray.ALLOW_COORDINATE_WRITE);
		la.setCapability(LineArray.ALLOW_COORDINATE_READ);
		la.setCapability(LineArray.ALLOW_COLOR_WRITE);
		la.setCapability(LineArray.ALLOW_COUNT_READ);
		la.setCapability(LineArray.ALLOW_INTERSECT);
		la.setCapability(LineArray.ALLOW_FORMAT_READ);
		// Create a normal
		// for (ListIterator li = bondlist.listIterator(); li.hasNext(); ){
		// la.setCoordinate(i, a1);
		// la.setColor(i, col);
		// la.setNormal(i++, a1);
		// }
		ColoringAttributes cola = new ColoringAttributes(new Color3f(),
				ColoringAttributes.SHADE_GOURAUD);
		Appearance app = new Appearance();
		lineAttributes = new LineAttributes();
		lineAttributes.setLineWidth(RendererCache.bondwidth);
		lineAttributes.setCapability(LineAttributes.ALLOW_WIDTH_WRITE);
		lineAttributes.setLineAntialiasingEnable(true);
		app.setLineAttributes(lineAttributes);
		app.setCapability(Appearance.ALLOW_LINE_ATTRIBUTES_READ);
		app.setCapability(Appearance.ALLOW_LINE_ATTRIBUTES_WRITE);
		RenderingAttributes ra = new RenderingAttributes();
		ra.setAlphaTestValue(0.1f);
		ra.setAlphaTestFunction(RenderingAttributes.GREATER);
		ra.setDepthBufferEnable(true);
		ra.setDepthBufferWriteEnable(true);
		app.setRenderingAttributes(ra);
		app.setColoringAttributes(cola);
		Shape3D wireframe = new Shape3D(la, app);
		// PickTool.setCapabilities(wire, PickTool.INTERSECT_COORD);
		wireframe.setUserData(this);
		wireframe.setBounds(new BoundingSphere(new Point3d(0, 0, 0), 10.0));
		try {
			wireframe.setBoundsAutoCompute(false);
		} catch (Exception e) {
			e.printStackTrace();
		}
		wireframe.setCapability(Shape3D.ALLOW_GEOMETRY_READ);
		wireframe.setCapability(Shape3D.ALLOW_APPEARANCE_READ);
		// return wire;
	}

	/*
	 * The MolecularAssembly BranchGroup has two TransformGroups between it and
	 * the "base" node where geometry is attached. If the point between the two
	 * transformations is where user rotation occurs. For example, if rotating
	 * about the center of mass of the system, the RotToCOM transformation will
	 * be an identity transformation (ie. none). If rotation is about some atom
	 * or group of atoms within the system, then the RotToCOM transformation
	 * will be a translation from that point to the COM.
	 * 
	 * @param zero
	 *            boolean
	 * @return BranchGroup
	 */

	public BranchGroup createScene(boolean zero) {
		originToRotT3D = new Transform3D();
		originToRotV3D = new Vector3d();
		originToRot = new TransformGroup(originToRotT3D);
		branchGroup = new BranchGroup();
		rotToCOM = new TransformGroup();
		rotToCOMT3D = new Transform3D();
		rotToCOMV3D = new Vector3d();
		// Set capabilities needed for picking and moving the MolecularAssembly
		branchGroup.setCapability(BranchGroup.ALLOW_DETACH);
		originToRot.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		originToRot.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
		originToRot.setCapability(TransformGroup.ENABLE_PICK_REPORTING);
		rotToCOM.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
		rotToCOM.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		// Put the MolecularAssembly in the middle of the scene
		if (zero) {
			originToRotV3D.set(0.0, 0.0, 0.0);
			originToRotT3D.set(originToRotV3D);
			originToRot.setTransform(originToRotT3D);
		}
		wire = renderWire();
		switchGroup = new Switch(Switch.CHILD_NONE);
		switchGroup.setCapability(Switch.ALLOW_SWITCH_WRITE);
		base = new BranchGroup();
		base.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
		base.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
		childNodes = new BranchGroup();
		childNodes.setCapability(BranchGroup.ALLOW_DETACH);
		childNodes.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
		childNodes.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
		switchGroup.addChild(base);
		if (wire != null) {
			base.addChild(wire);
		}
		switchGroup.setWhichChild(Switch.CHILD_ALL);
		rotToCOM.addChild(switchGroup);
		originToRot.addChild(rotToCOM);
		branchGroup.addChild(originToRot);
		branchGroup.compile();
		return branchGroup;
	}

	public boolean destroy() {
		detach();
		return super.destroy();
	}

	public void detach() {
		synchronized (this) {
			if (branchGroup != null && branchGroup.isLive()) {
				branchGroup.detach();
			}
		}
	}

	/*
	 * @todo Check the ROLS getList method.
	 * @param finalizeGroups
	 *            boolean
	 */
	public void finalize(boolean finalizeGroups) {
		setFinalized(false);
		if (finalizeGroups) {
			ArrayList Polymers = getAtomNodeList();
			for (ListIterator li = Polymers.listIterator(); li.hasNext();) {
				MSGroup group = (MSGroup) li.next();
				group.finalize(true);
			}
			for (MSNode m : molecules.getChildList()) {
				Molecule molecule = (Molecule) m;
				molecule.finalize(true);
			}
			for (MSNode m : water.getChildList()) {
				Molecule molecule = (Molecule) m;
				molecule.finalize(true);
			}
			for (MSNode m : ions.getChildList()) {
				Molecule molecule = (Molecule) m;
				molecule.finalize(true);
			}
		}
		createScene(!finalizeGroups);
		center();
		removeLeaves();
		setFinalized(true);
	}

	public String[] getAltLocations() {
		if (altLoc == null || altLoc.size() == 0) {
			return null;
		}
		String[] names = new String[altLoc.size()];
		int i = 0;
		for (String s : altLoc) {
			names[i++] = s;
		}
		return names;
	}

	public Atom getAtomFromWireVertex(int i) {
		if (atomLookUp != null && atomLookUp.length > i) {
			return atomLookUp[i];
		}
		return null;
	}

	public ArrayList<Atom> getAtomList() {
		if (atomList != null) {
			return atomList;
		}
		atomList = super.getAtomList();
		return atomList;
	}

	public ArrayList<Atom> getBackBoneAtoms() {
		ArrayList<Atom> backbone = new ArrayList<Atom>();
		Atom ca = new Atom("CA");
		ArrayList<ROLS> atoms = this.getList(Atom.class,
				new ArrayList<ROLS>());
		for (ROLS m : atoms) {
			Atom atom = (Atom) m;
			if (atom.equals(ca)) {
				backbone.add(atom);
				// else if (a.equals(new Atom("C"))) backbone.add(a);
				// else if (a.equals(new Atom("N"))) backbone.add(a);
			}
		}
		return backbone;
	}

	public ArrayList<ROLS> getBondList() {
		if (bondList != null) {
			return bondList;
		}
		bondList = super.getBondList();
		return bondList;
	}

	public BranchGroup getBranchGroup() {
		return branchGroup;
	}

	public String[] getChainNames() {
		ArrayList<String> temp = new ArrayList<String>();
		for (ListIterator li = getAtomNodeList().listIterator(); li.hasNext();) {
			MSNode node = (MSNode) li.next();
			if (node instanceof Polymer) {
				temp.add(((Polymer) node).getName());
			}
		}
		if (temp.size() == 0) {
			return null;
		}
		String[] names = new String[temp.size()];
		for (int i = 0; i < temp.size(); i++) {
			names[i] = temp.get(i);
		}
		return names;
	}

	public int getCurrentCycle() {
		return currentCycle;
	}

	public int getCycles() {
		return cycles;
	}

	public double getExtent() {
		double[] Rc = { 0, 0, 0 };
		int num = getAtomList().size();
		for (ListIterator li = getAtomList().listIterator(); li.hasNext();) {
			((Atom) li.next()).getXYZ(a);
			Rc[0] += a[0];
			Rc[1] += a[1];
			Rc[2] += a[2];
		}
		for (int i = 0; i < 3; i++) {
			Rc[i] /= num;
		}
		double r, d = 0;
		double[] xyz = new double[3];
		for (ListIterator li = getAtomList().listIterator(); li.hasNext();) {
			((Atom) li.next()).getXYZ(xyz);
			VectorMath.diff(xyz, Rc, xyz);
			r = VectorMath.r(xyz);
			if (d < r) {
				d = r;
			}
		}
		return d;
	}

	public File getFile() {
		return file;
	}

	public FileType getFileType() {
		return fileType;
	}

	public Vector3d getOffset() {
		return offset;
	}

	public TransformGroup getOriginToRot() {
		return originToRot;
	}

	public Polymer getPolymer(String chainName, boolean create) {
		for (ListIterator li = getAtomNodeList().listIterator(); li.hasNext();) {
			MSNode node = (MSNode) li.next();
			if (node instanceof Polymer && node.getName().equals(chainName)) {
				return (Polymer) node;
			}
		}
		if (create) {
			Polymer polymer = new Polymer(chainName, false, true);
			addMSNode(polymer);
			return polymer;
		}
		return null;
	}

	private Residue getResidue(String chainName, int resNum, String resName,
			boolean create) {
		// Find/Create the chain
		Polymer polymer = getPolymer(chainName, create);
		if (polymer == null) {
			return null;
		}
		// If the chain was found/created, find/create the residue
		return polymer.getResidue(resName, resNum, create);
	}

	public ArrayList<Residue> getResidueList() {
		ArrayList<Residue> residues = new ArrayList<Residue>();
		ListIterator li, lj;
		MSNode o;
		Polymer c;
		for (li = getAtomNodeList().listIterator(); li.hasNext();) {
			o = (MSNode) li.next();
			if (o instanceof Polymer) {
				c = (Polymer) o;
				for (lj = c.getAtomNodeList().listIterator(); lj.hasNext();) {
					o = (MSNode) lj.next();
					if (o instanceof Residue) {
						residues.add((Residue) o);
					}
				}
			}
		}
		return residues;
	}

	public TransformGroup getTransformGroup() {
		return originToRot;
	}

	public Node getWireFrame() {
		return wire;
	}

	public boolean isVisible() {
		return visible;
	}

	public void moveCenter(double[] d) {
		for (ListIterator li = getAtomList().listIterator(); li.hasNext();) {
			((Atom) li.next()).move(d);
		}
	}

	protected void removeLeaves() {
		super.removeLeaves();
		MSNode macroNode = getAtomNode();
		if (macroNode != null && macroNode.getChildCount() > 0) {
			getAtomNode().setName(
					"Macromolecules " + "(" + macroNode.getChildCount() + ")");
		} else {
			remove(macroNode);
		}
		if (molecules.getChildCount() == 0) {
			remove(molecules);
		} else {
			molecules.setName("Hetero Molecules " + "("
					+ molecules.getChildCount() + ")");
		}
		if (ions.getChildCount() == 0) {
			remove(ions);
		} else {
			ions.setName("Ions " + "(" + ions.getChildCount() + ")");
		}
		if (water.getChildCount() == 0) {
			remove(water);
		} else {
			water.setName("Water " + "(" + water.getChildCount() + ")");
		}
	}

	private Shape3D renderWire() {
		ArrayList<ROLS> bonds = getBondList();
		int numbonds = bonds.size();
		if (numbonds < 1) {
			return null;
		}
		Vector3d bondmidpoint = new Vector3d();
		double[] mid = { 0, 0, 0 };
		Vector3d v1 = new Vector3d();
		Vector3d v2 = new Vector3d();
		float[] a1 = { 0, 0, 0 };
		float[] a2 = { 0, 0, 0 };
		float[] col = new float[4];
		Bond bond;
		Atom atom1, atom2;
		LineArray la = new LineArray(4 * numbonds, GeometryArray.COORDINATES
				| GeometryArray.COLOR_4 | GeometryArray.NORMALS);
		la.setCapability(LineArray.ALLOW_COORDINATE_WRITE);
		la.setCapability(LineArray.ALLOW_COORDINATE_READ);
		la.setCapability(LineArray.ALLOW_COLOR_WRITE);
		la.setCapability(LineArray.ALLOW_COUNT_READ);
		la.setCapability(LineArray.ALLOW_INTERSECT);
		la.setCapability(LineArray.ALLOW_FORMAT_READ);
		atomLookUp = new Atom[4 * numbonds];
		int i = 0;
		col[3] = 0.9f;
		for (ListIterator li = bondList.listIterator(); li.hasNext();) {
			bond = (Bond) li.next();
			bond.setWire(la, i);
			atom1 = bond.getAtom(0);
			atom2 = bond.getAtom(1);
			atom1.getV3D(v1);
			atom2.getV3D(v2);
			a1[0] = (float) v1.x;
			a1[1] = (float) v1.y;
			a1[2] = (float) v1.z;
			a2[0] = (float) v2.x;
			a2[1] = (float) v2.y;
			a2[2] = (float) v2.z;
			// Find the bond center
			bondmidpoint.add(v1, v2);
			bondmidpoint.scale(0.5d);
			bondmidpoint.get(mid);
			// Atom #1
			Atom.AtomColor.get(atom1.getAtomicNumber()).get(col);
			atomLookUp[i] = atom1;
			la.setCoordinate(i, a1);
			la.setColor(i, col);
			la.setNormal(i, a2);
			i++;
			atomLookUp[i] = atom1;
			la.setCoordinate(i, mid);
			la.setColor(i, col);
			la.setNormal(i, a2);
			i++;
			// Atom #2
			Atom.AtomColor.get(atom2.getAtomicNumber()).get(col);
			atomLookUp[i] = atom2;
			la.setCoordinate(i, a2);
			la.setColor(i, col);
			la.setNormal(i, a1);
			i++;
			atomLookUp[i] = atom2;
			la.setCoordinate(i, mid);
			la.setColor(i, col);
			la.setNormal(i, a1);
			i++;
		}
		ColoringAttributes cola = new ColoringAttributes(new Color3f(),
				ColoringAttributes.SHADE_GOURAUD);
		Appearance app = new Appearance();
		lineAttributes = new LineAttributes();
		lineAttributes.setLineWidth(RendererCache.bondwidth);
		lineAttributes.setCapability(LineAttributes.ALLOW_WIDTH_WRITE);
		lineAttributes.setLineAntialiasingEnable(true);
		app.setLineAttributes(lineAttributes);
		app.setCapability(Appearance.ALLOW_LINE_ATTRIBUTES_READ);
		app.setCapability(Appearance.ALLOW_LINE_ATTRIBUTES_WRITE);
		RenderingAttributes ra = new RenderingAttributes();
		ra.setAlphaTestValue(0.1f);
		ra.setAlphaTestFunction(RenderingAttributes.GREATER);
		ra.setDepthBufferEnable(true);
		ra.setDepthBufferWriteEnable(true);
		app.setRenderingAttributes(ra);
		app.setColoringAttributes(cola);
		Shape3D wireframe = new Shape3D(la, app);
		// PickTool.setCapabilities(wire, PickTool.INTERSECT_COORD);
		wireframe.setUserData(this);
		wireframe.setBounds(new BoundingSphere(new Point3d(0, 0, 0), 1000.0));
		try {
			wireframe.setBoundsAutoCompute(false);
		} catch (Exception e) {
			e.printStackTrace();
		}
		wireframe.setCapability(Shape3D.ALLOW_GEOMETRY_READ);
		wireframe.setCapability(Shape3D.ALLOW_APPEARANCE_READ);
		wireframe.setCapability(Shape3D.ALLOW_LOCAL_TO_VWORLD_READ);
		return wireframe;
	}

	/**
	 * Rotate about a point in given in the System's Local Coordinates
	 * 
	 * @param v
	 *            Vector3d
	 */
	public void rotateAbout(Vector3d v) {
		Vector3d newRotPoint = new Vector3d(v);
		originToRot.getTransform(originToRotT3D);
		originToRotT3D.get(originToRotV3D);
		originToRotT3D.setTranslation(new Vector3d(0, 0, 0));
		rotToCOM.getTransform(rotToCOMT3D);
		rotToCOMT3D.get(rotToCOMV3D);
		newRotPoint.add(rotToCOMV3D);
		originToRotT3D.transform(newRotPoint);
		newRotPoint.add(originToRotV3D);
		originToRotT3D.setTranslation(newRotPoint);
		rotToCOMV3D.set(v);
		rotToCOMV3D.negate();
		rotToCOMT3D.setTranslation(rotToCOMV3D);
		originToRot.setTransform(originToRotT3D);
		rotToCOM.setTransform(rotToCOMT3D);
	}

	public void sceneGraphChange(List<BranchGroup> newShapes) {
		if (newShapes == null) {
			newShapes = myNewShapes;
		}
		if (newShapes.isEmpty()) {
			return;
		}
		boolean reCompile = false;
		// Check for nodes (new and/or recycled) being added to this
		// MolecularAssembly
		for (ListIterator<BranchGroup> li = newShapes.listIterator(); li
				.hasNext();) {
			BranchGroup group = li.next();
			li.remove();
			// This is code for cycling between two MolecularAssemblies
			if (group.getUserData() != null) {
				logger.info("" + group + " " + group.getUserData());
				/*
				 * Object userData = group.getUserData(); if (userData!=this) { //
				 * The appearance has already been set during a recursive call
				 * to setView, // although we need to turn back on Pickablility.
				 * TransformGroup tg = (TransformGroup) group.getChild(0);
				 * Shape3D shape = (Shape3D) tg.getChild(0);
				 * shape.setPickable(true); group.setUserData(this); if
				 * (!reCompile) { if (childNodes.isLive()) {
				 * childNodes.detach(); } reCompile = true; }
				 * childNodes.moveTo(group);
				 */
			} else {
				// This is a new group since it has no userData.
				// We can not query for the identity of its parent later, so
				// we will store it as a userData reference.
				group.setUserData(this);
				if (!reCompile) {
					if (childNodes.isLive()) {
						childNodes.detach();
					}
					reCompile = true;
				}
				childNodes.addChild(group);
			}
		}
		if (reCompile) {
			childNodes.compile();
			base.addChild(childNodes);
		}
	}

	public void setAngle(double a[]) {
		if (a == null) {
			return;
		}
		if (angle == null) {
			angle = new Vector3d();
		}
		angle.set(a);
	}

	public void setBox(double b[]) {
		if (b == null) {
			return;
		}
		if (box == null) {
			box = new Vector3d();
		}
		box.set(b);
	}

	public void setColor(RendererCache.ColorModel newColorModel, Color3f color,
			Material mat) {
		for (ListIterator li = getAtomNodeList().listIterator(); li.hasNext();) {
			MSGroup group = (MSGroup) li.next();
			group.setColor(newColorModel, color, mat);
		}
		for (MSNode m : molecules.getChildList()) {
			m.setColor(newColorModel, color, mat);
		}
		for (MSNode m : water.getChildList()) {
			m.setColor(newColorModel, color, mat);
		}
		for (MSNode m : ions.getChildList()) {
			m.setColor(newColorModel, color, mat);
		}
	}

	public void setCurrentCycle(int c) {
		if (c <= cycles && c > 0) {
			currentCycle = c;
			for (ListIterator li = getAtomList().listIterator(); li.hasNext();) {
				((Atom) li.next()).setCurrentCycle(currentCycle);
			}
		}
	}

	public void setCycles(int c) {
		cycles = c;
	}

	public void setFile(File f) {
		if (f == null) {
			return;
		}
		file = f;
	}

	public void setFileType(FileType fileType) {
		this.fileType = fileType;
	}

	public void setOffset(Vector3d o) {
		offset = o;
	}

	public void setView(RendererCache.ViewModel newViewModel,
			List<BranchGroup> newShapes) {
		// Just Detach the whole system branch group
		if (newViewModel == RendererCache.ViewModel.DESTROY) {
			switchGroup.setWhichChild(Switch.CHILD_NONE);
			visible = false;
		} else {
			setWireWidth(RendererCache.bondwidth);
			if (newViewModel == RendererCache.ViewModel.DETAIL
					&& childNodes.isLive()) {
				childNodes.detach();
			}
			// We'll collect new Scenegraph Shapes in our newShapeNode
			// This is to avoid the case where setView is called from the root
			// node and all new shapes for every MolecularAssembly would then be
			// put into the same ArrayList.
			super.setView(newViewModel, myNewShapes);
			ArrayList<ROLS> moleculeList = getList(Molecule.class,
					new ArrayList<ROLS>());
			for (ROLS m : moleculeList) {
				m.setView(newViewModel, myNewShapes);
			}
			for (MSNode m : molecules.getChildList()) {
				m.setView(newViewModel, myNewShapes);
			}
			for (MSNode m : water.getChildList()) {
				m.setView(newViewModel, myNewShapes);
			}
			for (MSNode m : ions.getChildList()) {
				m.setView(newViewModel, myNewShapes);
			}
			if (newViewModel == RendererCache.ViewModel.INVISIBLE) {
				switchGroup.setWhichChild(0);
			}
			if (newViewModel == RendererCache.ViewModel.DETAIL) {
				childNodes.compile();
				base.addChild(childNodes);
			}
		}
	}

	public void setWireWidth(float f) {
		if (wire == null) {
			return;
		}
		lineAttributes.setLineWidth(f);
	}

	public void sidePolymerCOM() {
		ArrayList residues = getResidueList();
		Residue r;
		ListIterator li;
		for (li = residues.listIterator(); li.hasNext();) {
			r = (Residue) li.next();
			r.printSideChainCOM();
		}
	}
}
