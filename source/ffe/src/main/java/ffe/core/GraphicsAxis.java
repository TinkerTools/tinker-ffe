/*
 * <p>Title: Force Field Explorer</p>
 * <p>Description: Force Field Explorer Molecular Modeling Program</p>
 * <p>Copyright: Copyright (c) 2004-2019 Jay William Ponder</p>
 * <p>Institution: Jay Ponder Lab, Washington University in Saint Louis</p>
 * @author Michael J. Schnieders
 * @version 8.5
 */

package ffe.core;

import java.awt.Color;
import java.awt.Font;
import java.util.Enumeration;

import org.jogamp.java3d.Appearance;
import org.jogamp.java3d.Bounds;
import org.jogamp.java3d.BranchGroup;
import org.jogamp.java3d.Canvas3D;
import org.jogamp.java3d.Font3D;
import org.jogamp.java3d.FontExtrusion;
import org.jogamp.java3d.Geometry;
import org.jogamp.java3d.Group;
import org.jogamp.java3d.Material;
import org.jogamp.java3d.Shape3D;
import org.jogamp.java3d.Text3D;
import org.jogamp.java3d.Transform3D;
import org.jogamp.java3d.TransformGroup;
import org.jogamp.java3d.utils.geometry.Cone;
import org.jogamp.java3d.utils.geometry.Cylinder;
import org.jogamp.java3d.utils.geometry.Sphere;
import org.jogamp.java3d.utils.picking.PickTool;
import org.jogamp.java3d.utils.universe.ViewingPlatform;
import org.jogamp.vecmath.AxisAngle4f;
import org.jogamp.vecmath.Color3f;
import org.jogamp.vecmath.Matrix3d;
import org.jogamp.vecmath.Vector3d;

import ffe.behaviors.MouseBehaviorCallback;

/*
 * The GraphicsAxis class encapsulates the 3D Axis that is used to display and
 * control rotation/translation in the global frame.
 */
public final class GraphicsAxis extends Group implements MouseBehaviorCallback {
	Canvas3D canvas;

	ViewingPlatform viewingPlatform;

	Bounds axisBounds;

	BranchGroup axisBranchGroup = new BranchGroup();

	TransformGroup axisTransformGroup = new TransformGroup();

	Transform3D axisTransform3D = new Transform3D();

	Vector3d axisVector3d = new Vector3d(-0.7, -0.6, -1.25);

	public Matrix3d matrix = new Matrix3d();

	public GraphicsAxis(ViewingPlatform v, Canvas3D c, Bounds b) {
		viewingPlatform = v;
		canvas = c;
		axisBounds = b;
		setCapability(Group.ENABLE_PICK_REPORTING);
		createAxis();
		setBounds(axisBounds);
		axisTransform3D.setTranslation(axisVector3d);
		axisTransform3D.setScale(0.015);
		axisTransformGroup.setTransform(axisTransform3D);
		axisTransformGroup.addChild(this);
		axisTransformGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		axisTransformGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
		axisTransformGroup.setCapability(TransformGroup.ENABLE_PICK_REPORTING);
		axisBranchGroup.setCapability(BranchGroup.ALLOW_DETACH);
		axisBranchGroup.setCapability(BranchGroup.ENABLE_PICK_REPORTING);
		axisBranchGroup.addChild(axisTransformGroup);
		axisBranchGroup.compile();
		viewingPlatform.getViewPlatformTransform().addChild(axisBranchGroup);
	}

	public void center() {
		axisTransform3D.setIdentity();
		axisTransform3D.setScale(0.015);
		axisTransform3D.setTranslation(axisVector3d);
		axisTransformGroup.setTransform(axisTransform3D);
	}

	public void createAxis() {
		Appearance ap = new Appearance();
		Color3f col = new Color3f(Color.lightGray);
		Color3f black = new Color3f(Color.black);
		Color3f white = new Color3f(Color.white);
		Material mat = new Material(col, black, col, white, 50.0f);
		mat.setLightingEnable(true);
		ap.setMaterial(mat);
		// X-Axis
		Cone xcone = new Cone(2.0f, 3.0f, ap);
		xcone.setUserData(this);
		Transform3D xconeT3d = new Transform3D();
		xconeT3d.setTranslation(new Vector3d(10.0f, 0.0f, 0.0f));
		xconeT3d.setRotation(new AxisAngle4f(0.0f, 0.0f, 1.0f, (float) Math.PI
				/ -2.0f));
		TransformGroup xconeTG = new TransformGroup(xconeT3d);
		xconeTG.addChild(xcone);
		Cylinder xcylinder = new Cylinder(1.0f, 9.0f, ap);
		xcylinder.setUserData(this);
		Transform3D xcyT3d = new Transform3D();
		xcyT3d.setTranslation(new Vector3d(4.5, 0.0, 0.0));
		xcyT3d.setRotation(new AxisAngle4f(0.0f, 0.0f, 1.0f,
				(float) Math.PI / 2.0f));
		TransformGroup xcyTG = new TransformGroup(xcyT3d);
		xcyTG.addChild(xcylinder);
		setCapabilities(xcone, xcylinder);
		addChild(xconeTG);
		addChild(xcyTG);
		// Y-Axis
		Cone ycone = new Cone(2.0f, 3.0f, ap);
		ycone.setUserData(this);
		Transform3D yconeT3d = new Transform3D();
		yconeT3d.setTranslation(new Vector3d(0.0f, 10.0f, 0.0f));
		TransformGroup yconeTG = new TransformGroup(yconeT3d);
		yconeTG.addChild(ycone);
		Cylinder ycylinder = new Cylinder(1.0f, 9.0f, ap);
		ycylinder.setUserData(this);
		Transform3D ycyT3d = new Transform3D();
		ycyT3d.setTranslation(new Vector3d(0.0, 4.5, 0.0));
		TransformGroup ycyTG = new TransformGroup(ycyT3d);
		ycyTG.addChild(ycylinder);
		setCapabilities(ycone, ycylinder);
		addChild(yconeTG);
		addChild(ycyTG);
		// Z-Axis
		Cone zcone = new Cone(2.0f, 3.0f, ap);
		zcone.setUserData(this);
		Transform3D zconeT3d = new Transform3D();
		zconeT3d.setTranslation(new Vector3d(0.0f, 0.0f, 10.0f));
		zconeT3d.setRotation(new AxisAngle4f(1.0f, 0.0f, 0.0f,
				(float) Math.PI / 2.0f));
		TransformGroup zconeTG = new TransformGroup(zconeT3d);
		zconeTG.addChild(zcone);
		Cylinder zcylinder = new Cylinder(1.0f, 9.0f, ap);
		zcylinder.setUserData(this);
		Transform3D zcyT3d = new Transform3D();
		zcyT3d.setTranslation(new Vector3d(0.0, 0.0, 4.5));
		zcyT3d.setRotation(new AxisAngle4f(1.0f, 0.0f, 0.0f,
				(float) Math.PI / 2.0f));
		TransformGroup zcyTG = new TransformGroup(zcyT3d);
		zcyTG.addChild(zcylinder);
		setCapabilities(zcone, zcylinder);
		addChild(zconeTG);
		addChild(zcyTG);
		Sphere sphere = new Sphere(1.0f, ap);
		if (!sphere.getShape().getGeometry(0).isCompiled()
				&& !sphere.getShape().getGeometry(0).isLive()) {
			PickTool.setCapabilities(sphere.getShape(),
					PickTool.INTERSECT_COORD);
		}
		addChild(sphere);
		// Labels
		ap = new Appearance();
		col = new Color3f(Color.yellow);
		mat = new Material(col, black, col, white, 50.0f);
		mat.setLightingEnable(true);
		ap.setMaterial(mat);
		Font font = new Font("Arial", Font.PLAIN, 4);
		Font3D font3d = new Font3D(font, new FontExtrusion());
		addChild(createAxisLabel("X", font3d, ap, 11.0, 0.0, 0.0));
		addChild(createAxisLabel("Y", font3d, ap, 0.0, 11.0, 0.0));
		addChild(createAxisLabel("Z", font3d, ap, 0.0, 0.0, 11.0));
	}

	@SuppressWarnings("unchecked")
	private TransformGroup createAxisLabel(String letter, Font3D font3d,
			Appearance ap, double x, double y, double z) {
		Text3D text = new Text3D(font3d, letter);
		text.setUserData(this);
		Transform3D t3D = new Transform3D();
		t3D.setTranslation(new Vector3d(x, y, z));
		TransformGroup tg = new TransformGroup(t3D);
		Shape3D text3d = new Shape3D(text, ap);
		text3d.setUserData(this);
		for (Enumeration<Geometry> e = text3d.getAllGeometries(); e.hasMoreElements();) {
			Geometry g = e.nextElement();
			g.setCapability(Geometry.ALLOW_INTERSECT);
		}
		text3d.setCapability(Shape3D.ENABLE_PICK_REPORTING);
		text3d.setCapability(Shape3D.ALLOW_GEOMETRY_READ);
		tg.addChild(text3d);
		return tg;
	}

	public TransformGroup getAxisTransformGroup() {
		return axisTransformGroup;
	}

	private void setCapabilities(Cone cone, Cylinder cylinder) {
		if (!cone.getShape(0).getGeometry(0).isLive()
				&& !cone.getShape(0).getGeometry(0).isCompiled()) {
			PickTool
					.setCapabilities(cone.getShape(0), PickTool.INTERSECT_COORD);
			cone.getShape(0).setUserData(this);
			PickTool
					.setCapabilities(cone.getShape(1), PickTool.INTERSECT_COORD);
			cone.getShape(1).setUserData(this);
			PickTool.setCapabilities(cylinder.getShape(0),
					PickTool.INTERSECT_COORD);
			cylinder.getShape(0).setUserData(this);
			PickTool.setCapabilities(cylinder.getShape(1),
					PickTool.INTERSECT_COORD);
			cylinder.getShape(1).setUserData(this);
			PickTool.setCapabilities(cylinder.getShape(2),
					PickTool.INTERSECT_COORD);
			cylinder.getShape(2).setUserData(this);
		}
	}

	public void showAxis(boolean b) {
		if (b == true && !axisBranchGroup.isLive()) {
			viewingPlatform.getViewPlatformTransform()
					.addChild(axisBranchGroup);
		} else if (b == false && axisBranchGroup.isLive()) {
			axisBranchGroup.detach();
		}
	}

	public void transformChanged(int type, Transform3D viewTransform) {
		viewTransform.get(matrix);
		matrix.invert();
		viewTransform.set(matrix);
		axisTransform3D.set(viewTransform);
		axisTransform3D.setTranslation(axisVector3d);
		axisTransform3D.setScale(0.015);
		axisTransformGroup.setTransform(axisTransform3D);
	}

	public void transformClicked(int type, Transform3D transform) {
		transformChanged(type, transform);
	}

	public void transformDoubleClicked(int type, Transform3D transform) {
		transformChanged(type, transform);
	}
}
