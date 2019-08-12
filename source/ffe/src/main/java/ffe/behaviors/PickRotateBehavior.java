/*
 * <p>Title: Force Field Explorer</p>
 * <p>Description: Force Field Explorer Molecular Modeling Program</p>
 * <p>Copyright: Copyright (c) 2004-2019 Jay William Ponder</p>
 * <p>Institution: Jay Ponder Lab, Washington University in Saint Louis</p>
 * @author Michael J. Schnieders
 * @version 8.7
 */

package ffe.behaviors;

import java.awt.event.MouseEvent;

import org.jogamp.java3d.Bounds;
import org.jogamp.java3d.BranchGroup;
import org.jogamp.java3d.Canvas3D;
import org.jogamp.java3d.Transform3D;
import org.jogamp.java3d.TransformGroup;

/*
 * The PickRotateBehavior class implements a mouse rotate behavior on a picked
 * object.
 */
public class PickRotateBehavior extends PickMouseBehavior implements
		MouseBehaviorCallback {
	public MouseRotate drag;

	private PickingCallback callback = null;

	private TransformGroup currentTG;

	public PickRotateBehavior(BranchGroup bg, Canvas3D canvas, Bounds bounds,
			TransformGroup VPTG, int pickMode) {
		super(canvas, bg, bounds);
		drag = new MouseRotate(MouseRotate.MANUAL_WAKEUP, VPTG);
		drag.setTransformGroup(currGrp);
		currGrp.addChild(drag);
		drag.setFactor(0.025);
		setSchedulingBounds(bounds);
		drag.setSchedulingBounds(bounds);
		pickCanvas.setMode(pickMode);
	}

	/*
	 * Return the pickMode component of this PickRotateBehavior.
	 */
	public int getPickMode() {
		return pickCanvas.getMode();
	}

	/*
	 * Sets the pickMode component of this PickRotateBehavior to the value of
	 * the passed pickMode. @param pickMode the pickMode to be copied.
	 */
	public void setPickMode(int pickMode) {
		pickCanvas.setMode(pickMode);
	}

	/*
	 * Register the class @param callback to be called each time the picked
	 * object moves
	 */
	public void setupCallback(PickingCallback c) {
		callback = c;
		if (callback == null) {
			drag.setupCallback(null);
		} else {
			drag.setupCallback(this);
		}
	}

	/*
	 * Callback method from MouseRotate This is used when the Picking callback
	 * is enabled
	 */
	public void transformChanged(int type, Transform3D transform) {
		callback.transformChanged(PickingCallback.ROTATE, currentTG);
	}

	public void transformClicked(int type, Transform3D transform) {
		callback.transformClicked(PickingCallback.ROTATE, currentTG);
	}

	public void transformDoubleClicked(int type, Transform3D transform) {
		callback.transformDoubleClicked(PickingCallback.ROTATE, currentTG);
	}

	/*
	 * Update the scene to manipulate any nodes. This is not meant to be called
	 * by users. Behavior automatically calls this. You can call this only if
	 * you know what you are doing.
	 * 
	 * @param xpos Current mouse X pos. @param ypos Current mouse Y pos.
	 */
	public void updateScene(int xpos, int ypos) {
		if ((mevent.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK) == MouseEvent.BUTTON1_DOWN_MASK) {
			pickCanvas.setShapeLocation(xpos, ypos);
			// PickResult r = pickCanvas.pickClosest();
			if (callback != null) {
				callback.transformChanged(PickingCallback.NO_PICK, null);
			}
		}
	}
}
