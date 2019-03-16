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

import org.jogamp.java3d.Transform3D;
import org.jogamp.java3d.TransformGroup;
import org.jogamp.java3d.WakeupCriterion;
import org.jogamp.java3d.WakeupOnAWTEvent;

/*
 * The MouseProperties class is simple extension of MouseBehavior.
 */
public class MouseProperties extends MouseBehavior {
	double x_angle, y_angle;

	double x_factor = .03;

	double y_factor = .03;

	// private MouseBehaviorCallback callback = null;

	public MouseProperties(int flags, TransformGroup VPTG) {
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

	public void processStimulus(Enumeration criteria) {
		WakeupCriterion wakeup;
		AWTEvent[] event;
		int id;
		while (criteria.hasMoreElements()) {
			wakeup = (WakeupCriterion) criteria.nextElement();
			if (wakeup instanceof WakeupOnAWTEvent) {
				event = ((WakeupOnAWTEvent) wakeup).getAWTEvent();
				for (int i = 0; i < event.length; i++) {
					processMouseEvent((MouseEvent) event[i]);
					if (((buttonPress) && ((flags & MANUAL_WAKEUP) == 0))
							|| ((wakeUp) && ((flags & MANUAL_WAKEUP) != 0))) {
						id = event[i].getID();
						if ((id == MouseEvent.MOUSE_DRAGGED)) {
							x_last = ((MouseEvent) event[i]).getX();
							y_last = ((MouseEvent) event[i]).getY();
						} else if (id == MouseEvent.MOUSE_PRESSED) {
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
		// callback = c;
	}

	public void transformChanged(Transform3D transform) {
	}
}
