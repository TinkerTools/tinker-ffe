/*
 * <p>Title: Force Field Explorer</p>
 * <p>Description: Force Field Explorer Molecular Modeling Program</p>
 * <p>Copyright: Copyright (c) 2004-2020 Jay William Ponder</p>
 * <p>Institution: Jay Ponder Lab, Washington University in Saint Louis</p>
 * @author Michael J. Schnieders
 * @version 8.8
 */

package ffe.lang;

import java.util.ArrayList;
import java.util.List;

import org.jogamp.java3d.BranchGroup;
import org.jogamp.java3d.Geometry;
import org.jogamp.java3d.LineArray;
import org.jogamp.java3d.Shape3D;
import org.jogamp.java3d.Transform3D;
import org.jogamp.java3d.TransformGroup;
import org.jogamp.vecmath.AxisAngle4d;
import org.jogamp.vecmath.Vector3d;

import ffe.lang.RendererCache.ViewModel;

/*
 * The Bond class represents a covalent bond formed between two atoms.
 */

public class Bond extends ValenceTerm {
	/*
	 * Bonding Character
	 */
	public enum BondCharacter {
		SINGLEBOND, DOUBLEBOND, TRIPLEBOND;
	}

	/*
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/*
	 * Length in Angstroms that is added to Atomic Radii when
	 * determining if two Atoms are within bonding distance
	 */
	public static final float BUFF = 0.7f;

	private static final float a0col[] = { 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
			0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f };

	private static final float f4a[] = { 0.0f, 0.0f, 0.0f, 0.9f };

	private static final float f4b[] = { 0.0f, 0.0f, 0.0f, 0.9f };

	private static float f16[] = { 0.0f, 0.0f, 0.0f, 0.9f, 0.0f, 0.0f, 0.0f,
			0.9f, 0.0f, 0.0f, 0.0f, 0.9f, 0.0f, 0.0f, 0.0f, 0.9f };

	// Some static variables used for computing cylinder orientations
	private static double d;

	private static double a13d[] = new double[3];

	private static double a23d[] = new double[3];

	private static double mid[] = new double[3];

	private static double diff3d[] = new double[3];

	private static double sum3d[] = new double[3];

	private static double coord[] = new double[12];

	private static double y[] = { 0.0d, 1.0d, 0.0d };

	private static AxisAngle4d axisAngle = new AxisAngle4d();

	private static double[] bcross = new double[4];

	private static double[] cstart = new double[3];

	private static Vector3d pos3d = new Vector3d();

	private static double angle;

	// List of Bonds that this Bond forms angles with
	private ArrayList<Bond> formsAngleWith = new ArrayList<Bond>();

	private int order; // Order of the bond, 1 for single, 2 for double, 3 for
						// triple

	/* ***************************************************************** */
	// Java3D methods and variables for visualization of this Bond.

	private RendererCache.ViewModel viewModel = RendererCache.ViewModel.INVISIBLE;

	private BranchGroup branchGroup;

	private TransformGroup cy1tg, cy2tg;

	private Transform3D cy1t3d, cy2t3d;

	private Shape3D cy1, cy2;

	private Vector3d scale;

	private int detail = 3;

	private LineArray la;

	private int lineIndex;

	private boolean wireVisible = true;

	public Bond(Atom a1, Atom a2, int ord) {
		atoms = new Atom[2];
		atoms[0] = a1;
		atoms[1] = a2;
		order = ord;
		a1.setBond(this);
		a2.setBond(this);
		setID_Key(false);
		viewModel = RendererCache.ViewModel.WIREFRAME;
	}

	/*
	 * Simple Bond constructor that is intended to be used with the equals
	 * method
	 * 
	 * @param n
	 *            Bond id
	 */
	public Bond(String n) {
		super(n);
	}

	/*
	 * Check to see if <b>this</b> Bond and another combine to form an angle
	 * 
	 * @return True if Bond b helps form an angle with <b>this</b> Bond
	 */
	public boolean formsAngleWith(Bond b) {
		for (Bond bond : formsAngleWith) {
			if (b == bond) {
				return true;
			}
		}
		return false;
	}

	/*
	 * Find the other Atom in this Bond. These two atoms
	 * are said to be 1-2.
	 * 
	 * @param a
	 *            The known Atom
	 * @return The other Atom that makes up this bond, or Null if Atom a
	 *         is not part of this bond
	 */
	public Atom get1_2(Atom a) {
		if (a == atoms[0]) {
			return atoms[1];
		}
		if (a == atoms[1]) {
			return atoms[0];
		}
		return null; // Atom not found in bond
	}

	/*
	 * Finds the common Atom between <b>this</b> Bond and Bond b
	 * 
	 * @param b
	 *            Bond to compare with
	 * @return The Atom the Bonds have in common or Null if they are
	 *         the same Bond or have no atom in common
	 */
	public Atom getCommonAtom(Bond b) {
		if (b == this || b == null) {
			return null; // undefined when comparing a bond to itself
		}
		if (b.atoms[0] == atoms[0]) {
			return atoms[0];
		}
		if (b.atoms[0] == atoms[1]) {
			return atoms[1];
		}
		if (b.atoms[1] == atoms[0]) {
			return atoms[0];
		}
		if (b.atoms[1] == atoms[1]) {
			return atoms[1];
		}
		return null; // Common atom not found
	}

	/*
	 * Find the Atom that <b>this</b> Bond and Bond b do not have in common.
	 * 
	 * @param b
	 *            Bond to compare with
	 * @return The Atom that Bond b and <b>this</b> Bond do not have
	 *         in common, or Null if they have no Atom in common
	 */
	public Atom getOtherAtom(Bond b) {
		if (b == this || b == null) {
			return null;
		}
		if (b.atoms[0] == atoms[0]) {
			return atoms[1];
		}
		if (b.atoms[0] == atoms[1]) {
			return atoms[0];
		}
		if (b.atoms[1] == atoms[0]) {
			return atoms[1];
		}
		if (b.atoms[1] == atoms[1]) {
			return atoms[0];
		}
		return null;
	}

	/*
	 * Create the Bond Scenegraph Objects.
	 * 
	 * @param newShapes
	 *            List
	 */
	private void initJ3D(List<BranchGroup> newShapes) {
		detail = RendererCache.detail;
		branchGroup = RendererCache.doubleCylinderFactory(atoms[0], atoms[1],
				detail);
		cy1tg = (TransformGroup) branchGroup.getChild(0);
		cy2tg = (TransformGroup) branchGroup.getChild(1);
		cy1 = (Shape3D) cy1tg.getChild(0);
		cy2 = (Shape3D) cy2tg.getChild(0);
		newShapes.add(branchGroup);
		cy1t3d = RendererCache.transform3DFactory();
		cy2t3d = RendererCache.transform3DFactory();
		update();
	}

	public void removeFromParent() {
		super.removeFromParent();
		cy1 = null;
		cy2 = null;
		cy1tg = null;
		cy2tg = null;
		if (cy1t3d != null) {
			RendererCache.poolTransform3D(cy1t3d);
			RendererCache.poolTransform3D(cy2t3d);
			cy1t3d = null;
			cy2t3d = null;
		}
		if (branchGroup != null) {
			branchGroup.detach();
			branchGroup.setUserData(null);
			RendererCache.poolDoubleCylinder(branchGroup);
			branchGroup = null;
		}
	}

	public boolean sameGroup() {
		if (atoms[0].getParent() == atoms[1].getParent()) {
			return true;
		}
		return false;
	}

	/*
	 * Specifies <b>this</b> Bond helps form an angle with the given Bond
	 * 
	 * @param b
	 *            Bond that forms an angle with <b>this</b> Bond
	 */
	public void setAngleWith(Bond b) {
		formsAngleWith.add(b);
	}

	public void setBondTransform3d(Transform3D t3d, double[] pos,
			double[] orient, double len, boolean newRot) {
		// Bond Orientation
		if (newRot) {
			angle = VectorMath.angle(orient, y);
			VectorMath.cross(y, orient, bcross);
			bcross[3] = angle - Math.PI;
			axisAngle.set(bcross);
		}
		// Scale the orientation vector to be a fourth the bond length
		// and add it to the position vector of the of the first atom
		VectorMath.scalar(orient, len / 4.0d, cstart);
		VectorMath.sum(cstart, pos, cstart);
		pos3d.set(cstart);
		t3d.setTranslation(pos3d);
		t3d.setRotation(axisAngle);
		t3d.setScale(scale);
	}

	/*
	 * Set the color of this Bond's Java3D shapes based on the passed Atom.
	 * 
	 * @param a
	 *            Atom
	 */
	public void setColor(Atom a) {
		if (viewModel != ViewModel.INVISIBLE
				&& viewModel != ViewModel.WIREFRAME && branchGroup != null) {
			if (a == atoms[0]) {
				cy1.setAppearance(a.getAtomAppearance());
			} else if (a == atoms[1]) {
				cy2.setAppearance(a.getAtomAppearance());
			}
		}
		setWireVisible(wireVisible);
	}

	/*
	 * Manage cylinder visibility.
	 * 
	 * @param visible
	 *            boolean
	 * @param newShapes
	 *            List
	 */
	public void setCylinderVisible(boolean visible, List<BranchGroup> newShapes) {
		if (!visible) {
			// Make this Bond invisible.
			if (branchGroup != null) {
				cy1.setPickable(false);
				cy1.setAppearance(RendererCache.nullAp);
				cy2.setPickable(false);
				cy2.setAppearance(RendererCache.nullAp);
				// branchGroup = null;
			}
		} else if (branchGroup == null) {
			// Get Java3D primitives from the RendererCache
			initJ3D(newShapes);
		} else {
			// Scale the cylinders to match the current ViewModel
			cy1t3d.setScale(scale);
			cy1tg.setTransform(cy1t3d);
			cy2t3d.setScale(scale);
			cy2tg.setTransform(cy2t3d);
			cy1.setAppearance(atoms[0].getAtomAppearance());
			cy2.setAppearance(atoms[1].getAtomAppearance());
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
	public void setView(RendererCache.ViewModel newViewModel,
			List<BranchGroup> newShapes) {
		switch (newViewModel) {
		case WIREFRAME:
			viewModel = ViewModel.WIREFRAME;
			setWireVisible(true);
			setCylinderVisible(false, newShapes);
			break;
		case SPACEFILL:
		case INVISIBLE:
		case RMIN:
			viewModel = ViewModel.INVISIBLE;
			setWireVisible(false);
			setCylinderVisible(false, newShapes);
			break;
		case RESTRICT:
			if (!atoms[0].isSelected() || !atoms[1].isSelected()) {
				viewModel = ViewModel.INVISIBLE;
				setWireVisible(false);
				setCylinderVisible(false, newShapes);
			}
			break;
		case BALLANDSTICK:
		case TUBE:
			viewModel = newViewModel;
			// Get the radius to use
			double rad;
			double len = getValue() / 2.0d;
			if (viewModel == RendererCache.ViewModel.BALLANDSTICK) {
				rad = 0.1d * RendererCache.radius;
			} else {
				rad = 0.2d * RendererCache.radius;
			}
			if (scale == null) {
				scale = new Vector3d();
			}
			scale.set(rad, len, rad);
			setWireVisible(false);
			setCylinderVisible(true, newShapes);
			break;
		case DETAIL:
			int res = RendererCache.detail;
			if (res != detail) {
				detail = res;
				if (branchGroup != null) {
					Geometry g1 = RendererCache.getCylinderGeom(0, detail);
					Geometry g2 = RendererCache.getCylinderGeom(1, detail);
					Geometry g3 = RendererCache.getCylinderGeom(2, detail);
					cy1.removeAllGeometries();
					cy2.removeAllGeometries();
					cy1.addGeometry(g1);
					cy1.addGeometry(g2);
					cy1.addGeometry(g3);
					cy2.addGeometry(g1);
					cy2.addGeometry(g2);
					cy2.addGeometry(g3);
				}
			}
			if (scale == null) {
				scale = new Vector3d();
			}
			double newRadius;
			if (viewModel == RendererCache.ViewModel.BALLANDSTICK) {
				newRadius = 0.1d * RendererCache.radius;
			} else if (viewModel == RendererCache.ViewModel.TUBE) {
				newRadius = 0.2d * RendererCache.radius;
			} else {
				break;
			}
			if (newRadius != scale.x) {
				scale.x = newRadius;
				scale.y = newRadius;
				if (branchGroup != null) {
					setView(viewModel, newShapes);
				}
			}
			break;
		case SHOWHYDROGENS:
			if (atoms[0].getAtomicNumber() == 1
					|| atoms[1].getAtomicNumber() == 1) {
				setView(viewModel, newShapes);
			}
			break;
		case HIDEHYDROGENS:
			if (atoms[0].getAtomicNumber() == 1
					|| atoms[1].getAtomicNumber() == 1) {
				viewModel = ViewModel.INVISIBLE;
				setWireVisible(false);
				setCylinderVisible(false, newShapes);
			}
			break;
		case FILL:
		case POINTS:
		case LINES:
			if (branchGroup != null && viewModel != ViewModel.INVISIBLE) {
				cy1.setAppearance(atoms[0].getAtomAppearance());
				cy2.setAppearance(atoms[1].getAtomAppearance());
			}
			break;
		}
	}

	public void setWire(LineArray l, int i) {
		la = l;
		lineIndex = i;
	}

	/*
	 * Manage Wireframe Visibility.
	 * 
	 * @param visible
	 *            boolean
	 */
	public void setWireVisible(boolean visible) {
		if (!visible) {
			wireVisible = false;
			la.setColors(lineIndex, a0col);
		} else {
			wireVisible = true;
			float cols[] = f16;
			float col1[] = f4a;
			float col2[] = f4b;
			atoms[0].getAtomColor().get(col1);
			atoms[1].getAtomColor().get(col2);
			for (int i = 0; i < 3; i++) {
				cols[i] = col1[i];
				cols[4 + i] = col1[i];
				cols[8 + i] = col2[i];
				cols[12 + i] = col2[i];
			}
			la.setColors(lineIndex, cols);
		}
	}

	/*
	 * Update recomputes the bonds length, Wireframe vertices,
	 * and Cylinder Transforms
	 */
	public void update() {
		// Update the Bond Length
		atoms[0].getXYZ(a13d);
		atoms[1].getXYZ(a23d);
		VectorMath.diff(a13d, a23d, diff3d);
		d = VectorMath.r(diff3d);
		setValue(d);
		VectorMath.sum(a13d, a23d, sum3d);
		VectorMath.scalar(sum3d, 0.5d, mid);
		// Update the Wireframe Model.
		if (la != null) {
			for (int i = 0; i < 3; i++) {
				coord[i] = a13d[i];
				coord[3 + i] = mid[i];
				coord[6 + i] = mid[i];
				coord[9 + i] = a23d[i];
			}
			la.setCoordinates(lineIndex, coord);
		}
		// Update the Bond cylinder transforms.
		if (branchGroup != null) {
			VectorMath.norm(diff3d, diff3d);
			scale.y = d / 2.0d;
			setBondTransform3d(cy1t3d, mid, diff3d, d, true);
			VectorMath.scalar(diff3d, -1.0d, diff3d);
			setBondTransform3d(cy2t3d, mid, diff3d, d, false);
			cy1tg.setTransform(cy1t3d);
			cy2tg.setTransform(cy2t3d);
		}
	}
}
