/*
 * <p>Title: Force Field Explorer</p>
 * <p>Description: Force Field Explorer Molecular Modeling Program</p>
 * <p>Copyright: Copyright (c) 2004-2020 Jay William Ponder</p>
 * <p>Company: Jay Ponder Lab, Washington University in Saint Louis</p>
 * @author Michael J. Schnieders
 * @version 8.8
 */

package ffe.behaviors;

import org.jogamp.java3d.Canvas3D;
import org.jogamp.java3d.Transform3D;
import org.jogamp.java3d.utils.behaviors.vp.OrbitBehavior;
import org.jogamp.vecmath.Matrix3d;
import org.jogamp.vecmath.Vector3d;

/*
 * The GlobalBehavior class allows mouse control over camera position, adding a
 * few functions to the OrbitBehavior class.
 */
public class GlobalBehavior extends OrbitBehavior {
	private Transform3D trans3D = new Transform3D();

	private Matrix3d homeQuat = new Matrix3d();

	private Vector3d homeTrans = new Vector3d(0.0, 0.0, 2.0);

	private Vector3d trans = new Vector3d();

	private MouseBehaviorCallback navigation = null;

	private boolean first = false;

	public GlobalBehavior() {
		super();
	}

	public GlobalBehavior(Canvas3D canvas) {
		super(canvas, OrbitBehavior.MOUSE_MOTION_LISTENER);
		trans3D.setTranslation(homeTrans);
		setHomeTransform(trans3D);
		setReverseRotate(true);
		setReverseTranslate(true);
		setRotFactors(2.0, 2.0);
		setProportionalZoom(true);
		setEnable(false);
		homeQuat.setIdentity();
	}

	public void centerView(boolean resetRotation, boolean resetTranslation) {
		if (!resetRotation && !resetTranslation) {
			return;
		}
		vp.getViewPlatformTransform().getTransform(trans3D);
		trans3D.get(trans);
		if (resetRotation) {
			trans3D.set(homeQuat);
			trans3D.setTranslation(homeTrans);
		}
		if (resetTranslation) {
			trans3D.setTranslation(homeTrans);
		}
		setHomeTransform(trans3D);
		goHome();
		if (resetRotation) {
			navigation.transformChanged(MouseBehaviorCallback.ORBIT, trans3D);
		}
	}

	public void integrateTransforms() {
		// The "first" flag allows the mouse motion to be reset
		// (ie. dx = x - x_last where x_last is wrong initially)
		if (first) {
			vp.getViewPlatformTransform().getTransform(trans3D);
		}
		super.integrateTransforms();
		if (first) {
			first = false;
			vp.getViewPlatformTransform().setTransform(trans3D);
		}
		vp.getViewPlatformTransform().getTransform(trans3D);
		navigation.transformChanged(MouseBehaviorCallback.ORBIT, trans3D);
	}

	public void setEnable(boolean b) {
		super.setEnable(b);
		if (b) {
			first = true;
		}
	}

	public void setUpCallback(MouseBehaviorCallback m) {
		navigation = m;
	}
}
