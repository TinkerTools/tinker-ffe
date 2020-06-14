/*
 * <p>Title: Force Field Explorer</p>
 * <p>Description: Force Field Explorer Molecular Modeling Program</p>
 * <p>Copyright: Copyright (c) 2004-2020 Jay William Ponder</p>
 * <p>Institution: Jay Ponder Lab, Washington University in Saint Louis</p>
 * @author Michael J. Schnieders
 * @version 8.8
 */

package ffe.behaviors;

import java.awt.AWTEvent;
import java.awt.event.MouseEvent;
import java.util.Iterator;

import org.jogamp.java3d.Transform3D;
import org.jogamp.java3d.TransformGroup;
import org.jogamp.java3d.WakeupCriterion;
import org.jogamp.java3d.WakeupOnAWTEvent;

/*
 * The MouseSelection class implements a mouse selection behavior.
 */
public class MouseSelection extends MouseBehavior {
	double x_angle, y_angle;

	double x_factor = .03;

	double y_factor = .03;

	private MouseBehaviorCallback callback = null;

	public MouseSelection(int flags, TransformGroup VPTG) {
		super(flags, VPTG);
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

	public void processStimulus(Iterator criteria) {
		WakeupCriterion wakeup;
		AWTEvent[] event;
		int id;
		while (criteria.hasNext()) {
			wakeup = (WakeupCriterion) criteria.next();
			if (wakeup instanceof WakeupOnAWTEvent) {
				event = ((WakeupOnAWTEvent) wakeup).getAWTEvent();
				for (int i = 0; i < event.length; i++) {
					processMouseEvent((MouseEvent) event[i]);
					if (((buttonPress) && ((flags & MANUAL_WAKEUP) == 0))
							|| ((wakeUp) && ((flags & MANUAL_WAKEUP) != 0))) {
						id = event[i].getID();
						if ((id == MouseEvent.MOUSE_DRAGGED)) {
							if (!reset) {
								transformChanged(currXform);
								if (callback != null) {
									callback.transformChanged(
											MouseBehaviorCallback.SELECTION,
											currXform);
								}
							} else {
								reset = false;
							}
							x_last = ((MouseEvent) event[i]).getX();
							y_last = ((MouseEvent) event[i]).getY();
						}
						if (id == MouseEvent.MOUSE_PRESSED) {
							x_last = ((MouseEvent) event[i]).getX();
							y_last = ((MouseEvent) event[i]).getY();
						}
					}
				}
			}
		}
		wakeupOn(mouseCriterion);
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
