/*
 * <p>Title: Force Field Explorer</p>
 * <p>Description: Force Field Explorer Molecular Modeling Program</p>
 * <p>Copyright: Copyright (c) 2004-2021 Jay William Ponder</p>
 * <p>Institution: Jay Ponder Lab, Washington University in Saint Louis</p>
 * @author Michael J. Schnieders
 * @version 8.10
 */

package ffe.behaviors;

import java.awt.AWTEvent;
import java.awt.Event;
import java.awt.event.MouseEvent;
import java.util.Iterator;

import org.jogamp.java3d.Behavior;
import org.jogamp.java3d.Bounds;
import org.jogamp.java3d.BranchGroup;
import org.jogamp.java3d.Canvas3D;
import org.jogamp.java3d.TransformGroup;
import org.jogamp.java3d.WakeupCriterion;
import org.jogamp.java3d.WakeupOnAWTEvent;
import org.jogamp.java3d.WakeupOr;
import org.jogamp.java3d.utils.picking.PickCanvas;

/*
 * The PickMouseBehavior class is the base class for mouse picking behaviors.
 */
public abstract class PickMouseBehavior extends Behavior {
	static int count = 0;

	protected PickCanvas pickCanvas;

	protected WakeupCriterion[] conditions;

	protected WakeupOr wakeupCondition;

	protected boolean buttonPress = false;

	protected boolean shiftButton = false;

	protected boolean controlButton = false;

	protected TransformGroup currGrp;

	protected MouseEvent mevent;

	/*
	 * Creates a PickMouseBehavior given current canvas, root of the tree to
	 * operate on, and the bounds.
	 */
	public PickMouseBehavior(Canvas3D canvas, BranchGroup root, Bounds bounds) {
		super();
		setSchedulingBounds(bounds);
		currGrp = new TransformGroup();
		currGrp.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		currGrp.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
		root.addChild(currGrp);
		pickCanvas = new PickCanvas(canvas, root);
		pickCanvas.setMode(PickCanvas.GEOMETRY);
		pickCanvas.setTolerance(10.0f);
	}

	public void initialize() {
		conditions = new WakeupCriterion[1];
		conditions[0] = new WakeupOnAWTEvent(Event.MOUSE_DOWN);
		wakeupCondition = new WakeupOr(conditions);
		wakeupOn(wakeupCondition);
	}

	private void processMouseEvent(MouseEvent evt) {
		buttonPress = false;
		if (evt.getID() == MouseEvent.MOUSE_PRESSED
				| evt.getID() == MouseEvent.MOUSE_CLICKED
				| evt.getID() == MouseEvent.MOUSE_RELEASED) {
			buttonPress = true;
		}
		if (evt.isControlDown()) {
			controlButton = true;
		} else {
			controlButton = false;
		}
		if (evt.isShiftDown()) {
			shiftButton = true;
		} else {
			shiftButton = false;
		}
	}

	public void processStimulus(Iterator criteria) {
		WakeupCriterion wakeup;
		AWTEvent[] evt = null;
		int xpos = 0, ypos = 0;
		while (criteria.hasNext()) {
			wakeup = (WakeupCriterion) criteria.next();
			if (wakeup instanceof WakeupOnAWTEvent) {
				evt = ((WakeupOnAWTEvent) wakeup).getAWTEvent();
			}
		}
		if (evt[0] instanceof MouseEvent) {
			mevent = (MouseEvent) evt[0];
			processMouseEvent((MouseEvent) evt[0]);
			xpos = mevent.getPoint().x;
			ypos = mevent.getPoint().y;
		}
		if (buttonPress) {
			updateScene(xpos, ypos);
		}
		wakeupOn(wakeupCondition);
	}

	public void setTolerance(float tol) {
		if (pickCanvas != null) {
			pickCanvas.setTolerance(tol);
		}
	}

	/*
	 * Subclasses shall implement this update function
	 */
	public abstract void updateScene(int xpos, int ypos);
}
