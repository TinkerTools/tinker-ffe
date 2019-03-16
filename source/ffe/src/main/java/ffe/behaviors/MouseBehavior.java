/*
 * <p>Title: Force Field Explorer</p>
 * <p>Description: Force Field Explorer Molecular Modeling Program</p>
 * <p>Copyright: Copyright (c) 2004-2019 Jay William Ponder</p>
 * <p>Institution: Jay Ponder Lab, Washington University in Saint Louis</p>
 * @author Michael J. Schnieders
 * @version 8.6
 */

package ffe.behaviors;

import java.awt.event.MouseEvent;
import java.util.Enumeration;

import org.jogamp.java3d.Behavior;
import org.jogamp.java3d.Transform3D;
import org.jogamp.java3d.TransformGroup;
import org.jogamp.java3d.WakeupCriterion;
import org.jogamp.java3d.WakeupOnAWTEvent;
import org.jogamp.java3d.WakeupOnBehaviorPost;
import org.jogamp.java3d.WakeupOr;

/*
 * The MouseBehavior class is the Base class for all mouse manipulators.
 */
public abstract class MouseBehavior extends Behavior {
	/*
	 * Set this flag if you want to manually wakeup the behavior.
	 */
	public static final int MANUAL_WAKEUP = 0x1;

	/*
	 * Set this flag if you want to invert the inputs. This is useful
	 * when the transform for the view platform is being changed
	 * instead of the transform for the object.
	 */
	public static final int INVERT_INPUT = 0x2;

	protected WakeupCriterion[] mouseEvents;

	protected WakeupOr mouseCriterion;

	protected Behavior poster;

	protected int id;

	protected WakeupOnBehaviorPost postCriterion;

	protected int x, y;

	protected int x_last, y_last;

	protected TransformGroup transformGroup;

	protected Transform3D transformX;

	protected Transform3D transformY;

	protected Transform3D currXform;

	protected boolean buttonPress = false;

	protected boolean reset = false;

	protected boolean invert = false;

	protected boolean wakeUp = false;

	protected int flags = 0;

	protected TransformGroup ViewerTG;

	/*
	 * Swap a new transformGroup replacing the old one. This allows
	 * manipulators to operate on different nodes.
	 * 
	 * @param transformGroup The *new* transform group to be manipulated.
	 */
	Transform3D t3d = new Transform3D();

	public MouseBehavior(int format, TransformGroup VPTG) {
		super();
		flags = format;
		ViewerTG = VPTG;
		currXform = new Transform3D();
		transformX = new Transform3D();
		transformY = new Transform3D();
		reset = true;
	}

	public MouseBehavior(int format, TransformGroup VPTG, Behavior b, int i) {
		this(format, VPTG);
		poster = b;
		id = i;
	}

	/*
	 * Initializes the behavior.
	 */
	public void initialize() {
		mouseEvents = new WakeupCriterion[3];
		mouseEvents[0] = new WakeupOnAWTEvent(MouseEvent.MOUSE_DRAGGED);
		mouseEvents[1] = new WakeupOnAWTEvent(MouseEvent.MOUSE_PRESSED);
		mouseEvents[2] = new WakeupOnAWTEvent(MouseEvent.MOUSE_RELEASED);
		mouseCriterion = new WakeupOr(mouseEvents);
		if (poster == null) {
			wakeupOn(mouseCriterion);
		} else {
			postCriterion = new WakeupOnBehaviorPost(poster, id);
			wakeupOn(postCriterion);
		}
		x = 0;
		y = 0;
		x_last = 0;
		y_last = 0;
	}

	/*
	 * Handles mouse events
	 */
	public void processMouseEvent(MouseEvent evt) {
		if (evt.getID() == MouseEvent.MOUSE_PRESSED) {
			buttonPress = true;
		} else if (evt.getID() == MouseEvent.MOUSE_RELEASED) {
			buttonPress = false;
			wakeUp = false;
		}
	}

	public abstract void processStimulus(Enumeration criteria);

	public void setTransformGroup(TransformGroup t) {
		transformGroup = t;
		currXform = new Transform3D();
		transformX = new Transform3D();
		transformY = new Transform3D();
		reset = true;
	}

	/*
	 * Manually wake up the behavior. If MANUAL_WAKEUP flag was set upon
	 * creation, you must wake up this behavior each time it is handled.
	 */
	public void wakeup() {
		wakeUp = true;
	}
}
