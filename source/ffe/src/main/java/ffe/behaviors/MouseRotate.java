/*
 * <p>Title: Force Field Explorer</p>
 * <p>Description: Force Field Explorer Molecular Modeling Program</p>
 * <p>Copyright: Copyright (c) 2004-2019 Jay William Ponder</p>
 * <p>Institution: Jay Ponder Lab, Washington University in Saint Louis</p>
 * @author Michael J. Schnieders
 * @version 8.7
 */

package ffe.behaviors;

import java.awt.AWTEvent;
import java.awt.event.MouseEvent;
import java.util.Enumeration;

import org.jogamp.java3d.Behavior;
import org.jogamp.java3d.Transform3D;
import org.jogamp.java3d.TransformGroup;
import org.jogamp.java3d.WakeupCriterion;
import org.jogamp.java3d.WakeupOnAWTEvent;
import org.jogamp.vecmath.Matrix4d;
import org.jogamp.vecmath.Vector3d;

// import ffe.panels.*;

/*
 * The MouseRotate class implements a mouse rotation behavior.
 */
public class MouseRotate extends MouseBehavior {
	private static Transform3D VPTG_T3D = new Transform3D();

	private static final Vector3d zero3d = new Vector3d(0.0, 0.0, 0.0);

	private static Vector3d translation = new Vector3d();

	private static Matrix4d mat = new Matrix4d();

	double x_angle, y_angle;

	double x_factor = 0.001;

	double y_factor = 0.001;

	int doneID = 0;

	private MouseBehaviorCallback callback = null;

	public MouseRotate(int flags, TransformGroup VPTG) {
		super(flags, VPTG);
	}

	public MouseRotate(int flags, TransformGroup VPTG, Behavior behavior,
			int postID, int dID) {
		super(flags, VPTG, behavior, postID);
		doneID = dID;
	}

	/*
	 * Return the x-axis movement multipler.
	 */
	public double getXFactor() {
		return x_factor;
	}

	/*
	 * Return the y-axis movement multipler.
	 */
	public double getYFactor() {
		return y_factor;
	}

	public void initialize() {
		super.initialize();
		x_angle = 0;
		y_angle = 0;
		if ((flags & INVERT_INPUT) == INVERT_INPUT) {
			invert = true;
			x_factor *= -1;
			y_factor *= -1;
		}
	}

	public void processStimulus(Enumeration criteria) {
		while (criteria.hasMoreElements()) {
			WakeupCriterion wakeup = (WakeupCriterion) criteria.nextElement();
			if (wakeup instanceof WakeupOnAWTEvent) {
				AWTEvent event[] = ((WakeupOnAWTEvent) wakeup).getAWTEvent();
				for (int i = 0; i < event.length; i++) {
					MouseEvent mevent = (MouseEvent) event[i];
					processMouseEvent(mevent);
					int id = event[i].getID();
					// Drag and Button 1 down
					if ((id == MouseEvent.MOUSE_DRAGGED)
							&& ((mevent.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK) == MouseEvent.BUTTON1_DOWN_MASK)
							&& transformGroup != null) {
						x = ((MouseEvent) event[i]).getX();
						y = ((MouseEvent) event[i]).getY();
						int dx = x - x_last;
						int dy = y - y_last;
						if (!reset) {
							x_angle = dy * y_factor;
							y_angle = dx * x_factor;
							transformX.rotX(x_angle);
							transformY.rotY(y_angle);
							transformGroup.getTransform(currXform);
							currXform.get(mat);
							currXform.setTranslation(zero3d);
							if (ViewerTG != null) {
								ViewerTG.getTransform(VPTG_T3D);
								VPTG_T3D.setTranslation(zero3d);
								VPTG_T3D.invert();
								currXform.mul(VPTG_T3D, currXform);
							}
							if (invert) {
								currXform.mul(currXform, transformX);
								currXform.mul(currXform, transformY);
							} else {
								currXform.mul(transformX, currXform);
								currXform.mul(transformY, currXform);
							}
							if (ViewerTG != null) {
								VPTG_T3D.invert();
								currXform.mul(VPTG_T3D, currXform);
							}
							translation.set(mat.m03, mat.m13, mat.m23);
							currXform.setTranslation(translation);
							transformGroup.setTransform(currXform);
							transformChanged(currXform);
							if (callback != null) {
								callback.transformChanged(
										MouseBehaviorCallback.TRANSLATE,
										currXform);
							}
						} else {
							reset = false;
						}
						x_last = x;
						y_last = y;
					} else if (id == MouseEvent.MOUSE_PRESSED) {
						x_last = ((MouseEvent) event[i]).getX();
						y_last = ((MouseEvent) event[i]).getY();
					} else if (id == MouseEvent.MOUSE_RELEASED) {
						setTransformGroup(null);
					}
				}
			}
		}
		if (transformGroup != null || postCriterion == null) {
			wakeupOn(mouseCriterion);
		} else {
			postId(doneID);
			wakeupOn(postCriterion);
		}
	}

	/*
	 * Set the x-axis amd y-axis movement multipler with factor.
	 */
	public void setFactor(double factor) {
		x_factor = y_factor = factor;
	}

	/*
	 * Set the x-axis amd y-axis movement multipler with xFactor and yFactor
	 * respectively.
	 */
	public void setFactor(double xFactor, double yFactor) {
		x_factor = xFactor;
		y_factor = yFactor;
	}

	public void setupCallback(MouseBehaviorCallback c) {
		callback = c;
	}

	public void transformChanged(Transform3D transform) {
	}
}
