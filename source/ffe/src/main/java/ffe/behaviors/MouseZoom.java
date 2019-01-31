/*
 * <p>Title: Force Field Explorer</p>
 * <p>Description: Force Field Explorer Molecular Modeling Program</p>
 * <p>Copyright: Copyright (c) 2004-2019 Jay William Ponder</p>
 * <p>Institution: Jay Ponder Lab, Washington University in Saint Louis</p>
 * @author Michael J. Schnieders
 * @version 8.5
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
 * The MouseZoom class implements a Mouse Zoom behavior.
 */
public class MouseZoom extends MouseBehavior {
	double z_factor = 0.0002;

	Vector3d translation = new Vector3d();

	private MouseBehaviorCallback callback = null;

	int mouseButton = MouseEvent.BUTTON2_DOWN_MASK;
	
	int doneID = 0;

	boolean first = true;

	public MouseZoom(int flags, TransformGroup VPTG) {
		super(flags, VPTG);
	}

	public MouseZoom(int flags, TransformGroup VPTG, Behavior behavior,
			int postID, int dID) {
		super(flags, VPTG, behavior, postID);
		doneID = dID;
	}

	/*
	 * Return the y-axis movement multipler.
	 */
	public double getFactor() {
		return z_factor;
	}

	public void initialize() {
		super.initialize();
		if ((flags & INVERT_INPUT) == INVERT_INPUT) {
			z_factor *= -1;
			invert = true;
		}
	}
	
	public void setMouseButton(int button){
		mouseButton = button;
	}

	public void processStimulus(Enumeration criteria) {
		AWTEvent[] event;
		boolean done = false;
		while (criteria.hasMoreElements()) {
			WakeupCriterion wakeup = (WakeupCriterion) criteria.nextElement();
			if (wakeup instanceof WakeupOnAWTEvent) {
				event = ((WakeupOnAWTEvent) wakeup).getAWTEvent();
				for (int i = 0; i < event.length; i++) {
					processMouseEvent((MouseEvent) event[i]);
					int id = event[i].getID();
					MouseEvent mevent = (MouseEvent) event[i];
					int mod = mevent.getModifiersEx();
					boolean middleButton = ((mod & mouseButton) == mouseButton);
					if (!middleButton) {
						middleButton = ((mod & MouseEvent.ALT_DOWN_MASK) == MouseEvent.ALT_DOWN_MASK);
					}
					if ((id == MouseEvent.MOUSE_DRAGGED) && middleButton) {
						y = ((MouseEvent) event[i]).getY();
						int dy = y - y_last;
						if (!reset) {
							transformGroup.getTransform(currXform);
							double z = (-1.0) * dy * z_factor;
							double scale = currXform.getScale() + z;
							if (scale > 0) {
								currXform.setScale(scale);
								transformGroup.setTransform(currXform);
								transformChanged(currXform);
							}
							if (callback != null) {
								callback.transformChanged(
										MouseBehaviorCallback.ZOOM, currXform);
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
						done = true;
					}
				}
			}
		}
		if (!done) {
			wakeupOn(mouseCriterion);
		} else {
			reset = true;
			mouseButton = MouseEvent.BUTTON2_DOWN_MASK;
			postId(doneID);
			wakeupOn(postCriterion);
		}
	}

	/*
	 * Set the y-axis movement multipler with factor.
	 */
	public void setFactor(double factor) {
		z_factor = factor;
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
