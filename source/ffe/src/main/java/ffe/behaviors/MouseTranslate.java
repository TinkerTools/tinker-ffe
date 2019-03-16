/*
 * <p>Title: Force Field Explorer</p>
 * <p>Description: Force Field Explorer Molecular Modeling Program</p>
 * <p>Copyright: Copyright (c) 2004-2019 Jay William Ponder</p>
 * <p>Institution: Jay Ponder Lab, Washington University in Saint Louis</p>
 * @author Michael J. Schnieders
 * @version 8.6
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
import org.jogamp.vecmath.Vector3d;

/*
 * The MouseTranslate class implements a mouse translate behavior.
 */
public class MouseTranslate extends MouseBehavior {
	private static Vector3d zero3d = new Vector3d(0.0, 0.0, 0.0);

	double x_factor = 0.05; // .01;

	double y_factor = 0.05; // .01;

	Vector3d translation = new Vector3d();

	private MouseBehaviorCallback callback = null;

	int mouseButton = MouseEvent.BUTTON3_DOWN_MASK;
	
	int doneID = 0;

	public MouseTranslate(int flags, TransformGroup VPTG) {
		super(flags, VPTG);
	}

	public MouseTranslate(int flags, TransformGroup VPTG, Behavior behavior,
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
		if ((flags & INVERT_INPUT) == INVERT_INPUT) {
			invert = true;
			x_factor *= -1;
			y_factor *= -1;
		}
	}
	
	public void setMouseButton(int button){
		mouseButton = button;
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
					int mod = mevent.getModifiersEx();
					boolean rightButton = ((mod & mouseButton) == mouseButton);
					if (!rightButton) {
						rightButton = ((mod & MouseEvent.SHIFT_DOWN_MASK) == MouseEvent.SHIFT_DOWN_MASK);
					}
					if ((id == MouseEvent.MOUSE_DRAGGED) && rightButton
							&& transformGroup != null) {
						x = ((MouseEvent) event[i]).getX();
						y = ((MouseEvent) event[i]).getY();
						int dx = x - x_last;
						int dy = y - y_last;
						if ((!reset)
								&& ((Math.abs(dy) < 50) && (Math.abs(dx) < 50))) {
							transformGroup.getTransform(currXform);
							Transform3D VPTG_T3D = new Transform3D();
							ViewerTG.getTransform(VPTG_T3D);
							VPTG_T3D.setTranslation(zero3d);
							VPTG_T3D.invert();
							currXform.mul(VPTG_T3D, currXform);
							translation.x = dx * x_factor;
							translation.y = -dy * y_factor;
							transformX.set(translation);
							if (invert) {
								currXform.mul(currXform, transformX);
							} else {
								currXform.mul(transformX, currXform);
							}
							VPTG_T3D.invert();
							currXform.mul(VPTG_T3D, currXform);
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
					}
					if (id == MouseEvent.MOUSE_PRESSED) {
						x_last = ((MouseEvent) event[i]).getX();
						y_last = ((MouseEvent) event[i]).getY();
					} else if (id == MouseEvent.MOUSE_RELEASED) {
						setTransformGroup(null);
					}
				}
			}
		}
		if (transformGroup != null) {
			wakeupOn(mouseCriterion);
		} else {
			mouseButton = MouseEvent.BUTTON3_DOWN_MASK;
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

	/*
	 * The transformChanged method in the callback class will be called every
	 * time the transform is updated
	 */
	public void setupCallback(MouseBehaviorCallback c) {
		callback = c;
	}

	public void transformChanged(Transform3D transform) {
	}
}
