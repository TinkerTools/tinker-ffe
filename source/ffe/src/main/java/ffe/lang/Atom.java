/*
 * <p>Title: Force Field Explorer</p>
 * <p>Description: Force Field Explorer Molecular Modeling Program</p>
 * <p>Copyright: Copyright (c) 2004-2019 Jay William Ponder</p>
 * <p>Institution: Jay Ponder Lab, Washington University in Saint Louis</p>
 * @author Michael J. Schnieders
 * @version 8.6
 */

package ffe.lang;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.logging.Logger;

import org.jogamp.java3d.Appearance;
import org.jogamp.java3d.BranchGroup;
import org.jogamp.java3d.Canvas3D;
import org.jogamp.java3d.Geometry;
import org.jogamp.java3d.J3DGraphics2D;
import org.jogamp.java3d.Material;
import org.jogamp.java3d.Node;
import org.jogamp.java3d.Shape3D;
import org.jogamp.java3d.Transform3D;
import org.jogamp.java3d.TransformGroup;
import org.jogamp.vecmath.AxisAngle4d;
import org.jogamp.vecmath.Color3f;
import org.jogamp.vecmath.Point2d;
import org.jogamp.vecmath.Point3d;
import org.jogamp.vecmath.Vector3d;

import ffe.lang.RendererCache.ColorModel;
import ffe.lang.RendererCache.ViewModel;
import ffe.mm.AtomType;
import ffe.mm.ChargeType;
import ffe.mm.MultipoleType;
import ffe.mm.VDWType;

/*
 * The Atom class represents a single atom.
 * 
 * @todo Group similar Atom data types into their own Class.
 */
public class Atom extends MSNode {

	public enum AtomName {
		H, He, Li, Be, B, C, N, O, F, Ne, Na, Mg, Al, Si, P, S, Cl, Ar, K, Ca, Sc, Ti, V, Cr, Mn, Fe, Co, Ni, Cu, Zn, Ga, Ge, As, Se, Br, Kr, Rb, Sr, Y, Zr, Nb, Mo, Tc, Ru, Rh, Pd, Ag, Cd, In, Sn, Sb, Te, I, Xe, Cs, Ba, La, Ce, Pr, Nd, Pm, Sm, Eu, Gd, Tb, Dy, Ho, Er, Tm, Yb, Lu, Hf, Ta, W, Re, Os, Ir, Pt, Au, Hg, Tl, Pb, Po, At, Rn, Fr, Ra, Ac, Th, Pa, U, Np, Pu, Am, Cm, Bk, Cf, Es, Fm, Md, No, Lr;
	}

	/*
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Logger logger = Logger.getLogger("ffe");
	
	// Theses "max" values are used for relative vector display
	private static double maxInduced, maxVel, maxAccel, maxForce;

	private static Appearance vectorAppearance = null;

	private static Point3d point3d = new Point3d();

	private static Point2d point2d = new Point2d();

	private static double[] y = { 0.0d, 1.0d, 0.0d };

	private static double[] ycrossv = new double[4];

	private static double[] shapeVector = new double[3];

	private static Vector3d scaleV3D = new Vector3d();

	private static Vector3d transV3D = new Vector3d();

	private static AxisAngle4d axisAngle = new AxisAngle4d();

	private static double angle;

	public static Hashtable<Integer, Color3f> AtomColor = new Hashtable<Integer, Color3f>();

	public static Hashtable<Integer, Float> AtomVDW = new Hashtable<Integer, Float>();

	/*
	 * Hybridizations
	 */
	public static final int SP = 2, SP2 = 3, SP3 = 4;

	public final static Hashtable<String, Integer> hybridTable = new Hashtable<String, Integer>();

	static {
		AtomColor.put(0, RendererCache.RED);
		AtomColor.put(1, RendererCache.WHITE);
		AtomColor.put(2, RendererCache.GREEN);
		AtomColor.put(3, RendererCache.MAGENTA);
		AtomColor.put(4, RendererCache.MAGENTA);
		AtomColor.put(5, RendererCache.MAGENTA);
		AtomColor.put(6, RendererCache.GRAY);
		AtomColor.put(7, RendererCache.BLUE);
		AtomColor.put(8, RendererCache.RED);
		AtomColor.put(9, RendererCache.GREEN);
		AtomColor.put(10, RendererCache.GREEN);
		AtomColor.put(11, RendererCache.MAGENTA);
		AtomColor.put(12, RendererCache.MAGENTA);
		AtomColor.put(13, RendererCache.MAGENTA);
		AtomColor.put(14, RendererCache.GRAY);
		AtomColor.put(15, RendererCache.ORANGE);
		AtomColor.put(16, RendererCache.YELLOW);
		AtomColor.put(17, RendererCache.GREEN);
		AtomColor.put(18, RendererCache.GREEN);
		for (int i = 19; i < 109; i++) {
			if (i != 36 && i != 54 && i != 86) {
				AtomColor.put(i, RendererCache.MAGENTA);
			} else {
				AtomColor.put(i, RendererCache.GREEN);
			}
		}
	}

	static {
		AtomVDW.put(0, 1.0f);
		AtomVDW.put(1, 1.20f);
		AtomVDW.put(2, 1.22f);
		AtomVDW.put(3, 0.78f);
		AtomVDW.put(4, 0.34f);
		AtomVDW.put(5, 2.08f);
		AtomVDW.put(6, 1.85f);
		AtomVDW.put(7, 1.54f);
		AtomVDW.put(8, 1.40f);
		AtomVDW.put(9, 1.35f);
		AtomVDW.put(10, 1.60f);
		AtomVDW.put(11, 0.98f);
		AtomVDW.put(12, 0.78f);
		AtomVDW.put(13, 0.57f);
		AtomVDW.put(14, 2.00f);
		AtomVDW.put(15, 1.90f);
		AtomVDW.put(16, 1.85f);
		AtomVDW.put(17, 1.81f);
		AtomVDW.put(18, 1.91f);
		AtomVDW.put(19, 1.33f);
		AtomVDW.put(20, 1.06f);
		AtomVDW.put(21, 0.91f);
		AtomVDW.put(22, 0.83f);
		AtomVDW.put(23, 0.82f);
		AtomVDW.put(24, 2.0f);
		AtomVDW.put(25, 2.0f);
		AtomVDW.put(26, 2.0f);
		AtomVDW.put(27, 2.0f);
		AtomVDW.put(28, 2.0f);
		AtomVDW.put(29, 2.0f);
		AtomVDW.put(30, 2.0f);
		AtomVDW.put(31, 2.0f);
		AtomVDW.put(32, 2.0f);
		AtomVDW.put(33, 2.0f);
		AtomVDW.put(34, 2.0f);
		AtomVDW.put(35, 1.95f);
		AtomVDW.put(36, 1.89f);
		for (int i = 37; i < 109; i++) {
			AtomVDW.put(i, 2.0f);
		}
	}

	static {
		hybridTable.put("1", 1);
		hybridTable.put("6", 4);
		hybridTable.put("7", 3);
		hybridTable.put("8", 2);
		hybridTable.put("15", 4);
		hybridTable.put("16", 2);
		hybridTable.put("19", 0);
		hybridTable.put("26", 8);
	}

	public static double getMaxAcceleration() {
		return maxAccel;
	}

	public static double getMaxForce() {
		return maxForce;
	}

	public static double getMaxInduced() {
		return maxInduced;
	}

	public static double getMaxVelocity() {
		return maxVel;
	}

	public static void setMaxAcceleration(double f) {
		maxAccel = f;
	}

	public static void setMaxForce(double f) {
		maxForce = f;
	}

	public static void setMaxInduced(double f) {
		maxInduced = f;
	}

	public static void setMaxVelocity(double f) {
		maxVel = f;
	}

	// Identification Info
	public int xyzindex; // Index in Tinker Files

	private String id = null;

	// Identifiers used for PDB Files
	private String residue = null;

	private int residueNumber = -1;

	private String chain = null;
	
	private double occupancy = 1.0d;
	
	private double bfactor = 2.0d;

	// Coordinates
	private Vector3d positionVector3d;

	private ArrayList<Vector3d> trajectory;

	// Molecular Mechanics Info
	private AtomType atomType = null;

	private VDWType vdwType = null;

	private ChargeType chargeType = null;

	// multipole
	private MultipoleType multipoleType = null;

	private Atom[] multipoleReferenceSites = null;

	private double globalDipole[] = null;

	private double globalQuadrupole[][] = null;

	// solvation
	private double bornRadius;

	private double[] induced;

	private double inducedMag;

	private double[] velocity;

	private double velMag;

	private double[] acceleration;

	private double accelMag;

	private double[] force;

	private double forceMag;

	private double[] currentVector = null;

	private double currentMag = 0.0;

	private double currentMax = 1.0;

	private double currentLength = 0.0;

	// Connectivity Informaton, for example an atom "knows" what bonded
	// molecular
	// mechanics terms it contributes to.
	private ArrayList<Bond> bonds;

	private ArrayList<Angle> angles;

	private ArrayList<Dihedral> dihedrals;

	/* ***************************************************************** */
	// Java3D methods and variables for visualization of this Atom.
	/* Scenegraph Diagram. */
	/*
	 * childShapes (The childShapes group is from this Atom's MolecularAssembly) | |
	 * branchGroup vectorBranchGroup | _______|_______
	 * transformGroup(transform3D) | | | cylTG(cylT3D) coneTG(coneT3D) sphere | |
	 * cylinder cone
	 */

	// The current ViewModel
	private ViewModel viewModel = ViewModel.INVISIBLE;

	private ViewModel vectorType = ViewModel.HIDEVECTORS;

	private ViewModel vectorLength = ViewModel.RELATIVE;

	private ViewModel polygonType = ViewModel.FILL;

	private ColorModel colorModel = ColorModel.CPK;

	// Java3D Scenegraph Objects
	private Shape3D sphere, cylinder, cone;

	private BranchGroup branchGroup, vectorBranchGroup;

	private TransformGroup transformGroup, cylTG, coneTG;

	private Transform3D transform3D, cylT3D, coneT3D;

	// Appearance and Coloring
	private Appearance appearance;

	private Color3f currentCol, previousCol;

	private Color3f userColor = RendererCache.userColor;

	private int detail = RendererCache.detail;

	private double radius = RendererCache.radius;

	private double scale = 1.0;

	// "stale" is True if this Atom's J3D transforms need to updated before
	// making it visible
	private boolean stale = false;

	/*
	 * Constructor used when parsing XYZ files
	 */
	public Atom(int xyznum, String id, AtomType atomType, double[] d) {
		this(id, atomType, d);
		xyzindex = xyznum;
	}

	/*
	 * Constructor used when parsing PDB files
	 */
	public Atom(int index, String id, AtomType atomType, double[] d, String r,
			int n, String c) {
		this(index, id, atomType, d);
		residue = r;
		residueNumber = n;
		chain = c;
	}

	/*
	 * Constructor
	 * 
	 * @param s
	 *            The Atom's PDB/Type identifier
	 */
	public Atom(String s) {
		super(s, 1);
		id = s;
		currentCol = previousCol = RendererCache.toAtomColor(id);
		colorModel = ColorModel.CPK;
	}

	/*
	 * Constructor
	 */
	public Atom(String id, AtomType atomType, double[] d) {
		this(id);
		this.atomType = atomType;
		positionVector3d = new Vector3d(d);
		setAllowsChildren(false);
		if (atomType != null) {
			currentCol = previousCol = AtomColor.get(atomType.atomicNumber);
		}
	}

	public void addTrajectoryCoords(Vector3d coords, int position) {
		if (trajectory == null) {
			trajectory = new ArrayList<Vector3d>();
			trajectory.add(0, positionVector3d);
		}
		trajectory.add(position, coords);
	}

	@Override
	public void drawLabel(Canvas3D canvas, J3DGraphics2D g2d, Node node) {
		if (RendererCache.labelAtoms) {
			point3d.x = getX();
			point3d.y = getY();
			point3d.z = getZ();
			RendererCache.getScreenCoordinate(canvas, node, point3d, point2d);
			g2d.drawString(toShortString(), (float) point2d.x,
					(float) point2d.y);
		}
	}

	/*
	 * Overidden equals method.
	 * 
	 * @param object
	 *            The Object to compare with <b>this</b>
	 * @return True if <b>this</b> atom and object do not reference the same
	 *         object, are of the same class, and have the same id
	 */
	@Override
	public final boolean equals(Object object) {
		if (this == object) {
			return true;
		}
		return false;
	}

	public void getAcceleration(double[] t) {
		if (acceleration == null || t == null) {
			return;
		}
		t[0] = acceleration[0];
		t[1] = acceleration[1];
		t[2] = acceleration[2];
	}

	public ArrayList<Angle> getAngles() {
		return angles;
	}

	public Appearance getAtomAppearance() {
		if (appearance == null) {
			appearance = RendererCache.appearanceFactory(currentCol,
					polygonType);
		}
		return appearance;
	}

	public Color3f getAtomColor() {
		return currentCol;
	}

	/*
	 * Gets the Atomic Number
	 * 
	 * @return Atomic Number
	 */
	public int getAtomicNumber() {
		return atomType.atomicNumber;
	}

	public ArrayList<Atom> getAtomList() {
		ArrayList<Atom> atoms = new ArrayList<Atom>();
		atoms.add(this);
		return atoms;
	}

	public AtomType getAtomType() {
		return atomType;
	}

	public Bond getBond(Atom a) {
		if (bonds == null) {
			return null;
		}
		for (Bond bond : bonds) {
			if (bond.get1_2(a) == this) {
				return bond;
			}
		}
		return null;
	}

	/*
	 * Gets the list of the Bonds <b>this</b> Atom helps to form
	 * 
	 * @return A list of the bonds this atom helps to form
	 */
	public ArrayList<Bond> getBonds() {
		return bonds;
	}

	public double getBornRadius() {
		return bornRadius;
	}

	public double getBornVolume() {
		return 2.0;
	}

	/*
	 * Get the chain name
	 * 
	 * @return String
	 */
	public String getChain() {
		if (chain != null) {
			return chain;
		}
		Polymer p = (Polymer) this.getMSNode(Polymer.class);
		if (p == null) {
			return null;
		}
		chain = p.getName();
		return chain;
	}

	/*
	 * Gets the partial atomic charge
	 * 
	 * @return partial atomic charge
	 */
	public double getCharge() {
		return 1.0;
	}

	public ArrayList<Dihedral> getDihedrals() {
		return dihedrals;
	}

	/*
	 * Gets the Epsilon value
	 * 
	 * @return Epsilon value
	 */
	public double getEpsilon() {
		return 1.0;
	}

	public void getForce(double[] t) {
		if (force == null || t == null) {
			return;
		}
		t[0] = force[0];
		t[1] = force[1];
		t[2] = force[2];
	}

	public double getGPol() {
		return 2.0;
	}

	/*
	 * Gets the energy gradient
	 * 
	 * @return energy gradient
	 */
	// public Vector3d getGradient(){ return new Vector3d(gradient); }
	/*
	 * Gets the Atomic Hybridization
	 * 
	 * @return Atomic Hybridization
	 */
	public int getHybridization() {
		return atomType.hybridization;
	}

	/*
	 * Gets the ID
	 * 
	 * @return ID
	 */
	public String getID() {
		return id;
	}

	/*
	 * Gets the atom ID
	 * 
	 * @return atom ID
	 */
	public String getIdent() {
		String s = new String(atomType.environment);
		return s;
	}

	public void getInducedDipole(double[] t) {
		if (induced == null || t == null) {
			return;
		}
		t[0] = induced[0];
		t[1] = induced[1];
		t[2] = induced[2];
	}

	/*
	 * Gets the atom Key
	 * 
	 * @return atom Key
	 */
	public String getKey() {
		if (atomType != null) {
			return atomType.key;
		}
		return null;
	}

	/*
	 * Gets the Atomic Mass
	 * 
	 * @return Atomic Mass
	 */
	public double getMass() {
		return atomType.mass;
	}

	public Atom[] getMultipoleReferenceSites() {
		return this.multipoleReferenceSites;
	}

	public MultipoleType getMultipoleType() {
		return multipoleType;
	}

	public final int getNumAngles() {
		if (angles == null) {
			return 0;
		}
		return angles.size();
	}

	/*
	 * Gets the number of atoms bonded to <b>this</b> Atom
	 * 
	 * @return Number of atoms bonded to this atom
	 */
	public final int getNumBonds() {
		if (bonds == null) {
			return 0;
		}
		return bonds.size();
	}

	public final int getNumDihedrals() {
		if (dihedrals == null) {
			return 0;
		}
		return dihedrals.size();
	}

	public double getRDielectric() {
		return 2.0;
	}

	/*
	 * public final String toPDBString(int num){ String blanks = new String("
	 * "); String xS, yS, zS; int xi,yi,zi; StringBuffer temp = new
	 * StringBuffer((new Integer(num)).toString()); temp.insert(0,
	 * blanks.substring(0,5 - temp.length())); StringBuffer record = new
	 * StringBuffer("ATOM " + temp.toString()); temp = new StringBuffer(id);
	 * temp.insert(0, blanks.substring(0,5 - temp.length()));
	 * record.append(temp.toString() + " " + myResidue.toString() + " " +
	 * myPolymer.toString()); temp = new StringBuffer((new
	 * Integer(myResidueNumber)).toString()); temp.insert(0,
	 * blanks.substring(0,5 - temp.length())); record.append(temp.toString() + "
	 * "); xS = new String("" + v3d.x); xi = xS.indexOf("."); xS =
	 * xS.substring(0,xi + 4); temp = new StringBuffer(xS); temp.insert(0,
	 * blanks.substring(0, 8 - xS.length())); xS = temp.toString(); yS = new
	 * String("" + v3d.y); yi = yS.indexOf("."); yS = yS.substring(0,yi + 4);
	 * temp = new StringBuffer(yS); temp.insert(0, blanks.substring(0, 8 -
	 * yS.length())); yS = temp.toString(); zS = new String("" + v3d.z); zi =
	 * zS.indexOf("."); zS = zS.substring(0,zi + 4); temp = new
	 * StringBuffer(zS); temp.insert(0, blanks.substring(0, 8 - zS.length()));
	 * zS = temp.toString(); record.append(" " + xS + yS + zS); return
	 * record.toString(); }
	 */

	public double getRelativeLength() {
		return currentMag / currentMax;
	}

	/*
	 * Get the residue name
	 * 
	 * @return String
	 */
	public String getResidueName() {
		if (residue != null) {
			return residue;
		}
		Residue r = (Residue) getMSNode(Residue.class);
		return r.getName();
	}

	public int getResidueNumber() {
		return residueNumber;
	}

	/*
	 * Gets the Sigma value
	 * 
	 * @return Sigma value
	 */
	public double getSigma() {
		return 1.0;
	}

	public Vector3d getTrajectoryCoords(int position) {
		return trajectory.get(position);
	}

	public int getTrajectoryLength() {
		return trajectory.size();
	}

	public int getType() {
		return atomType.type;
	}

	/*
	 * Gets the Atom's Cartesian Coordinates return The Cartesian Coordinates
	 */
	public void getV3D(Vector3d temp) {
		temp.set(positionVector3d);
	}

	// public final Vector3f getnewV3D(){ return new Vector3f(v3f); }
	/*
	 * Gets the van der Waals radius return van der Waals radius
	 */
	public double getVDWR() {
		return 1.0;
	}

	public double getVectorLength() {
		return currentLength;
	}

	public void getVelocity(double[] t) {
		if (velocity == null || t == null) {
			return;
		}
		t[0] = velocity[0];
		t[1] = velocity[1];
		t[2] = velocity[2];
	}

	/*
	 * Gets the x coordinate
	 * 
	 * @return x coordinate
	 */
	public double getX() {
		return positionVector3d.x;
	}

	public void getXYZ(double[] xyz) {
		positionVector3d.get(xyz);
	}

	/*
	 * Gets the XYZ Index
	 * 
	 * @return XYZ Index
	 */
	public final int getXYZIndex() {
		return xyzindex;
	}

	/*
	 * Gets the y coordinate
	 * 
	 * @return y coordinate
	 */
	public final double getY() {
		return positionVector3d.y;
	}

	/*
	 * Gets the z coordinate
	 * 
	 * @return z coordinate
	 */
	public final double getZ() {
		return positionVector3d.z;
	}

	@Override
	public final int hashCode() {
		int ret = HashCodeUtil.hash(HashCodeUtil.ATOMSEED, xyzindex);
		return ret;
	}

	/*
	 * Create the Sphere Java3D objects.
	 * 
	 * @param newShapes
	 *            List
	 */
	private void initSphere(List<BranchGroup> newShapes) {
		if (appearance == null) {
			appearance = RendererCache.appearanceFactory(currentCol,
					ViewModel.FILL);
		}
		if (transform3D == null) {
			transform3D = RendererCache.transform3DFactory(positionVector3d,
					scale);
		} else {
			transform3D.setTranslation(positionVector3d);
			transform3D.setScale(scale);
		}
		detail = RendererCache.detail;
		branchGroup = RendererCache.sphereFactory(appearance, detail,
				transform3D);
		transformGroup = (TransformGroup) branchGroup.getChild(0);
		sphere = (Shape3D) transformGroup.getChild(0);
		sphere.setUserData(this);
		newShapes.add(branchGroup);
	}

	/*
	 * Create the Vector Java3D objects.
	 * 
	 * @param newShapes
	 *            List
	 */
	public void initVector(List<BranchGroup> newShapes) {
		appearance = RendererCache
				.appearanceFactory(currentCol, ViewModel.FILL);
		detail = RendererCache.detail;
		cylinder = RendererCache.createCylinder(appearance, detail);
		cylinder.setUserData(this);
		cylT3D = new Transform3D();
		cylTG = new TransformGroup(cylT3D);
		cylTG.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
		cylTG.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		cylTG.addChild(cylinder);
		cone = RendererCache.coneFactory(appearance, detail);
		cone.setUserData(this);
		coneT3D = new Transform3D();
		coneT3D.setScale(0.1d);
		coneTG = new TransformGroup(coneT3D);
		coneTG.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
		coneTG.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		coneTG.addChild(cone);
		vectorBranchGroup = new BranchGroup();
		vectorBranchGroup.addChild(cylTG);
		vectorBranchGroup.addChild(coneTG);
		newShapes.add(vectorBranchGroup);
		updateVector();
	}

	/*
	 * Checks to see if an Atom is bonded to <b>this</b> Atom
	 * 
	 * @param a
	 *            Atom to check
	 * @return True is Atom a is bonded to <b>this</b>this atom
	 */
	public final boolean isBonded(Atom a) {
		for (Bond bond : bonds) {
			if (bond.get1_2(a) == this) {
				return true;
			}
		}
		return false;
	}

	/*
	 * Gets whether or not the Atom is under-constrained
	 * 
	 * @return True if the atom is under-constrained (ie has can accept bonds)
	 */
	public boolean isDangeling() {
		Integer hybrid = hybridTable.get("" + atomType.atomicNumber);
		if (hybrid == null) {
			return false;
		}
		int result = hybrid.compareTo(bonds.size());
		return (result > 0);
	}

	public boolean isStale() {
		return stale;
	}

	// True if this Atom's Sphere or Vector is visible
	public boolean isVisible() {
		return (viewModel != ViewModel.INVISIBLE || vectorType != ViewModel.INVISIBLE);
	}

	/*
	 * Add a vector to the Atom's current position vector
	 * 
	 * @param d
	 *            Vector to add to the current position
	 */
	public void move(double[] d) {
		positionVector3d.x += d[0];
		positionVector3d.y += d[1];
		positionVector3d.z += d[2];
		stale = true;
	}

	public void moveTo(double x, double y, double z) {
		positionVector3d.set(x, y, z);
		stale = true;
	}

	/*
	 * Moves the atom to the specified location
	 * 
	 * @param d
	 *            Location to move <b>this</b> Atom to
	 */
	public void moveTo(double[] d) {
		positionVector3d.set(d);
		stale = true;
	}

	public void moveTo(Vector3d newPosition) {
		positionVector3d.set(newPosition);
		stale = true;
	}

	/*
	 * Prints the atom identity and Cartesian coordinates to stout
	 */
	@Override
	public final void print() {
		logger.info(toString());
	}

	public void removeFromParent() {
		super.removeFromParent();
		if (angles != null) {
			angles.clear();
		}
		if (dihedrals != null) {
			dihedrals.clear();
		}
		if (trajectory != null) {
			trajectory.clear();
		}
		trajectory = null;
		stale = false;
		viewModel = ViewModel.INVISIBLE;
		vectorType = ViewModel.HIDEVECTORS;
		vectorLength = ViewModel.UNIT;
		RendererCache.poolTransform3D(transform3D);
		if (branchGroup != null) {
			branchGroup.detach();
			branchGroup.setUserData(null);
			RendererCache.poolSphere(branchGroup);
			branchGroup = null;
		}
	}

	public void setAcceleration(double x, double y, double z) {
		if (acceleration == null) {
			acceleration = new double[3];
		}
		acceleration[0] = x;
		acceleration[1] = y;
		acceleration[2] = z;
		accelMag = VectorMath.r(acceleration);
		if (accelMag > maxAccel) {
			maxAccel = accelMag;
		}
		stale = true;
	}

	public void setAngle(Angle a) {
		if (angles == null) {
			angles = new ArrayList<Angle>();
		}
		angles.add(a);
	}

	/*
	 * Specify that <b>this</b> Atom is part of a Bond
	 * 
	 * @param b
	 *            Bond that <b>this</b> Atom is part of
	 */
	public void setBond(Bond b) {
		if (bonds == null) {
			bonds = new ArrayList<Bond>();
		}
		bonds.add(b);
	}
	
	/*
	 * Set the occupancy.
	 * @param o
	 */
	public void setOccupancy(double o){
		occupancy = o;
	}
	
	/*
	 * Set the Debye-Waller Factor.
	 * Also known as Atomic Displacement Parameter (ADF).
	 * @param b
	 */
	public void setBFactor(double b) {
		bfactor = b;
	}
	
	/*
	 * Set the effective Born Radius.
	 * @param bornRadius
	 */
	public void setBornRadius(double bornRadius) {
		this.bornRadius = bornRadius;
	}

	/*
	 * Polymorphic setColor method.
	 * 
	 * @param newColorModel
	 *            ColorModel
	 * @param newCol
	 *            Color3f
	 * @param newMat
	 *            Material
	 */
	public void setColor(ColorModel newColorModel, Color3f newCol,
			Material newMat) {
		switch (newColorModel) {
		case CPK:
			newCol = RendererCache.getColor(this, ColorModel.CPK);
			if (newCol == currentCol) {
				return;
			}
			colorModel = newColorModel;
			currentCol = previousCol = newCol;
			break;
		case USERCOLOR:
			colorModel = newColorModel;
			currentCol = previousCol = userColor;
			break;
		case APPLYUSERCOLOR:
			userColor = RendererCache.userColor;
			currentCol = previousCol = userColor;
			break;
		case MONOCHROME:
			colorModel = newColorModel;
			currentCol = previousCol = RendererCache.WHITE;
			break;
		case SELECT:
			if (isSelected()) {
				newCol = RendererCache.selectionColor;
				if (newCol != currentCol) {
					currentCol = newCol;
				} else {
					return;
				}
			} else {
				currentCol = previousCol;
			}
			break;
		case PICK:
			newCol = RendererCache.pickingColor;
			if (newCol != currentCol) {
				currentCol = newCol;
			} else {
				return;
			}
			break;
		case REVERT:
			if (RendererCache.highlightSelections && isSelected()) {
				currentCol = RendererCache.selectionColor;
			} else {
				currentCol = previousCol;
			}
			break;
		case PARTIALCHARGE:
			newCol = RendererCache.getColor(this, ColorModel.PARTIALCHARGE);
			if (newCol == currentCol) {
				return;
			}
			colorModel = newColorModel;
			currentCol = previousCol = newCol;
			break;
		case VECTORMAGNITUDE:
			newCol = RendererCache.getColor(this, newColorModel);
			if (newCol == currentCol) {
				return;
			}
			colorModel = newColorModel;
			currentCol = previousCol = newCol;
			break;
		default:
			// Check for a Color Choice sent from a higher level structure
			// (residue,polymer,etc)
			if (newCol == currentCol || newCol == null) {
				return;
			}
			colorModel = newColorModel;
			currentCol = previousCol = newCol;
		}
		// Apply the Color Change
		appearance = RendererCache.appearanceFactory(currentCol, polygonType);
		if (branchGroup != null && viewModel != ViewModel.INVISIBLE) {
			sphere.setAppearance(appearance);
		}
		if (vectorBranchGroup != null && vectorType != ViewModel.HIDEVECTORS) {
			cylinder.setAppearance(appearance);
			cone.setAppearance(appearance);
		}
		if (bonds != null) {
			for (Bond bond : bonds) {
				bond.setColor(this);
			}
		}
	}

	public void setCurrentCycle(int cycle) {
		if (trajectory == null) {
			return;
		}
		if (cycle <= 0 || cycle > trajectory.size()) {
			return;
		}
		positionVector3d = trajectory.get(cycle - 1);
		stale = true;
	}

	public void setCurrentVector() {
		switch (vectorType) {
		case FORCE:
			if (force == null) {
				break;
			}
			currentVector = force;
			currentMag = forceMag;
			currentMax = maxForce;
			return;
		case INDUCEDDIPOLE:
			if (induced == null) {
				break;
			}
			currentVector = induced;
			currentMag = inducedMag;
			currentMax = maxInduced;
			return;
		case VELOCITY:
			if (velocity == null) {
				break;
			}
			currentVector = velocity;
			currentMag = velMag;
			currentMax = maxVel;
			return;
		case ACCELERATION:
			if (acceleration == null) {
				break;
			}
			currentVector = acceleration;
			currentMag = accelMag;
			currentMax = maxAccel;
			return;
		default:
			break;
		}
		vectorType = ViewModel.HIDEVECTORS;
		currentVector = null;
		currentMag = 0.0;
		currentMax = 1.0;
	}

	public void setDihedral(Dihedral d) {
		if (dihedrals == null) {
			dihedrals = new ArrayList<Dihedral>();
		}
		dihedrals.add(d);
	}

	public void setForce(double x, double y, double z) {
		if (force == null) {
			force = new double[3];
		}
		force[0] = x;
		force[1] = y;
		force[2] = z;
		forceMag = VectorMath.r(force);
		if (forceMag > maxVel) {
			maxForce = forceMag;
		}
		stale = true;
	}

	public void setGlobalMultipole(double dipole[], double quadrupole[][]) {
		if (globalDipole == null) {
			globalDipole = new double[3];
		}
		if (globalQuadrupole == null) {
			globalQuadrupole = new double[3][3];
		}
		for (int i = 0; i < 3; i++) {
			globalDipole[i] = dipole[i];
			for (int j = 0; j < 3; j++) {
				globalQuadrupole[i][j] = quadrupole[i][j];
			}
		}
	}

	public void setInducedDipole(double x, double y, double z) {
		if (induced == null) {
			induced = new double[3];
		}
		induced[0] = x;
		induced[1] = y;
		induced[2] = z;
		inducedMag = VectorMath.r(induced);
		if (inducedMag > maxInduced) {
			maxInduced = inducedMag;
		}
		stale = true;
	}

	public void setMultipoleType(MultipoleType multipoleType,
			Atom[] multipoleReferenceSites) {
		this.multipoleType = multipoleType;
		this.multipoleReferenceSites = multipoleReferenceSites;
	}

	@Override
	public void setSelected(boolean b) {
		selected = b;
	}

	// Vector Methods

	public void setSphereVisible(boolean sphereVisible,
			List<BranchGroup> newShapes) {
		if (!sphereVisible) {
			// Make this atom invisible.
			if (branchGroup != null) {
				sphere.setPickable(false);
				sphere.setAppearance(RendererCache.nullAp);
			}
		} else {
			// Make this atom visible.
			if (branchGroup == null) {
				initSphere(newShapes);
			}
			sphere.setAppearance(appearance);
			sphere.setPickable(true);
			updateSphere();
		}
	}

	public void setVeclocity(double x, double y, double z) {
		if (velocity == null) {
			velocity = new double[3];
		}
		velocity[0] = x;
		velocity[1] = y;
		velocity[2] = z;
		velMag = VectorMath.r(velocity);
		if (velMag > maxVel) {
			maxVel = velMag;
		}
		stale = true;
	}

	public void setVectorLength() {
		switch (vectorLength) {
		case UNIT:
			currentLength = 1.0;
			return;
		case RELATIVE:
			currentLength = currentMag / currentMax;
			return;
		case ABSOLUTE:
			currentLength = currentMag;
			return;
		default:
			break;
		}
		currentLength = 0.0;
	}

	public void setVectorVisible(boolean vectorVisible,
			List<BranchGroup> newShapes) {
		if (!vectorVisible) {
			if (vectorBranchGroup != null) {
				cylinder.setAppearance(RendererCache.nullAp);
				cylinder.setPickable(false);
				cone.setAppearance(RendererCache.nullAp);
				cone.setPickable(false);
			}
		} else {
			if (vectorType != ViewModel.HIDEVECTORS) {
				if (vectorBranchGroup == null) {
					initVector(newShapes);
				}
				cylinder.setAppearance(vectorAppearance);
				cylinder.setPickable(true);
				cone.setAppearance(vectorAppearance);
				cone.setPickable(true);
				updateVector();
			}
		}
	}

	/*
	 * Polymorphic setView method.
	 * 
	 * @param newViewModel
	 *            ViewModel
	 * @param newShapes
	 *            List
	 */
	@Override
	public void setView(ViewModel newViewModel, List<BranchGroup> newShapes) {
		switch (newViewModel) {
		// case INVISIBLE through case TUBE change the "ViewModel"
		case INVISIBLE:
			viewModel = ViewModel.INVISIBLE;
			vectorType = ViewModel.HIDEVECTORS;
			setSphereVisible(false, newShapes);
			setVectorVisible(false, newShapes);
			break;
		case WIREFRAME:
			viewModel = ViewModel.INVISIBLE;
			setSphereVisible(false, newShapes);
			break;
		case SPACEFILL:
			viewModel = ViewModel.SPACEFILL;
			scale = AtomVDW.get(atomType.atomicNumber) * radius;
			setSphereVisible(true, newShapes);
			break;
		case RMIN:
			viewModel = ViewModel.RMIN;
			scale = 1.0 * radius;
			setSphereVisible(true, newShapes);
			break;
		case BALLANDSTICK:
			viewModel = ViewModel.BALLANDSTICK;
			scale = AtomVDW.get(atomType.atomicNumber) / 5.0d * radius;
			setSphereVisible(true, newShapes);
			break;
		case TUBE:
			viewModel = ViewModel.TUBE;
			scale = RendererCache.radius * 0.2d;
			setSphereVisible(true, newShapes);
			break;
		case ACCELERATION:
		case VELOCITY:
		case INDUCEDDIPOLE:
		case FORCE:
			// Modify vectorType
			vectorType = newViewModel;
			setVectorVisible(true, newShapes);
			break;
		case HIDEVECTORS:
			vectorType = ViewModel.HIDEVECTORS;
			setVectorVisible(false, newShapes);
			break;
		case ABSOLUTE:
		case RELATIVE:
		case UNIT:
			// Modify vectorLength
			vectorLength = newViewModel;
			setVectorVisible(true, newShapes);
			break;
		case SHOWHYDROGENS:
			if (atomType.atomicNumber == 1) {
				return;
			}
			break;
		case HIDEHYDROGENS:
			if (atomType.atomicNumber == 1) {
				viewModel = ViewModel.INVISIBLE;
				vectorType = ViewModel.HIDEVECTORS;
				setSphereVisible(false, newShapes);
				setVectorVisible(false, newShapes);
				return;
			}
			break;
		case RESTRICT:
			if (!isSelected()) {
				viewModel = ViewModel.INVISIBLE;
				vectorType = ViewModel.HIDEVECTORS;
				setSphereVisible(false, newShapes);
				setVectorVisible(false, newShapes);
				return;
			}
			break;
		case DETAIL:
			int newdetail = RendererCache.detail;
			if (newdetail != detail) {
				detail = newdetail;
				if (sphere != null) {
					Geometry geom = RendererCache.getSphereGeom(detail);
					sphere.removeAllGeometries();
					sphere.addGeometry(geom);
				}
			}
			double newradius = RendererCache.radius;
			if (newradius != radius) {
				radius = newradius;
				setView(viewModel, newShapes);
			}
			break;
		// Polygon Appearance Selection
		case FILL:
		case POINTS:
		case LINES:
			polygonType = newViewModel;
			appearance = RendererCache.appearanceFactory(currentCol,
					polygonType);
			if (viewModel != ViewModel.INVISIBLE) {
				setSphereVisible(true, newShapes);
			}
			if (vectorType != ViewModel.HIDEVECTORS) {
				setVectorVisible(true, newShapes);
			}
			break;
		}
	}

	public void setXYZIndex(int index) {
		xyzindex = index;
	}
	public String toMultipoleString() {
		{
			if (multipoleType == null || globalDipole == null
					|| globalQuadrupole == null) {
				return null;
			}
			StringBuffer multipoleBuffer = new StringBuffer(toString());
			multipoleBuffer.append(String.format("\n%11$s % 7.5f\n"
					+ "%11$s % 7.5f % 7.5f % 7.5f\n" + "%11$s % 7.5f\n"
					+ "%11$s % 7.5f % 7.5f\n" + "%11$s % 7.5f % 7.5f % 7.5f",
					multipoleType.charge, globalDipole[0], globalDipole[1],
					globalDipole[2], globalQuadrupole[0][0],
					globalQuadrupole[1][0], globalQuadrupole[1][1],
					globalQuadrupole[2][0], globalQuadrupole[2][1],
					globalQuadrupole[2][2], "                 "));
			return multipoleBuffer.toString();
		}
	}

	public String toShortString() {
		StringBuffer s = new StringBuffer("" + xyzindex);
		s.append("-");
		s.append(id);
		return s.toString();
	}
	/*
	 * @return The string: "INDEX - ID (X, Y, Z)"
	 */
	@Override
	public String toString() {
		return String.format("%7d - %s (%7.3f, %7.3f, %7.3f)", xyzindex, id,
				positionVector3d.x, positionVector3d.y, positionVector3d.z);
	}

	/*
	 * Java3D transforms are not updated unless they are visible.
	 * This allows better performance for rendering partial structures
	 * during an interactive dynamics run or during trajectory playback.
	 */
	@Override
	public void update() {
		if (stale) {
			updateSphere();
			updateVector();
			stale = false;
		}
	}

	public void updateSphere() {
		if (branchGroup != null && viewModel != ViewModel.INVISIBLE) {
			transform3D.setTranslation(positionVector3d);
			transform3D.setScale(scale);
			transformGroup.setTransform(transform3D);
			if (colorModel == ColorModel.VECTORMAGNITUDE) {
				setColor(colorModel, null, null);
			}
		}
	}
	public void updateVector() {
		if (vectorBranchGroup != null && vectorType != ViewModel.HIDEVECTORS) {
			setCurrentVector();
			if (vectorType == ViewModel.HIDEVECTORS) {
				return;
			}
			double v[] = currentVector;
			setVectorLength();
			double mag = currentLength * RendererCache.vectorScale;
			// Cylinder and Cone Rotation from the Positive Y-axis
			angle = VectorMath.angle(v, y);
			VectorMath.cross(y, v, ycrossv);
			ycrossv[3] = angle; // Math.PI;
			axisAngle.set(ycrossv);
			// Cylinder Orientation
			cylT3D.setRotation(axisAngle);
			VectorMath.norm(v, shapeVector);
			VectorMath.scalar(shapeVector, mag * 0.5d, shapeVector);
			transV3D.set(shapeVector);
			transV3D.add(positionVector3d);
			cylT3D.setTranslation(transV3D);
			scaleV3D.set(0.05d, mag, 0.05d);
			cylT3D.setScale(scaleV3D);
			cylTG.setTransform(cylT3D);
			// Cone Orientation
			coneT3D.setRotation(axisAngle);
			VectorMath.norm(shapeVector, shapeVector);
			VectorMath.scalar(shapeVector, mag + 0.05d, shapeVector);
			transV3D.set(shapeVector);
			transV3D.add(positionVector3d);
			coneT3D.setTranslation(transV3D);
			coneTG.setTransform(coneT3D);
		}
	}
}
