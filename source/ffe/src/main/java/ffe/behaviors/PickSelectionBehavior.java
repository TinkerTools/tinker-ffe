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

import org.jogamp.java3d.Bounds;
import org.jogamp.java3d.BranchGroup;
import org.jogamp.java3d.Canvas3D;
import org.jogamp.java3d.Transform3D;
import org.jogamp.java3d.TransformGroup;
import org.jogamp.java3d.utils.picking.PickResult;

/*
 * The PickSelectionBehavior class implements a mouse based selections behavior.
 */
public class PickSelectionBehavior extends PickMouseBehavior implements
		MouseBehaviorCallback {
	MouseSelection drag;

	private PickingCallback callback = null;

	private TransformGroup currentTG;

	public PickSelectionBehavior(BranchGroup root, Canvas3D canvas,
			Bounds bounds, TransformGroup VPTG, int pickMode) {
		super(canvas, root, bounds);
		drag = new MouseSelection(MouseSelection.MANUAL_WAKEUP, VPTG);
		drag.setTransformGroup(currGrp);
		currGrp.addChild(drag);
		drag.setSchedulingBounds(bounds);
		setSchedulingBounds(bounds);
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

	public void transformChanged(int type, Transform3D transform) {
		callback.transformChanged(PickingCallback.SELECTION, currentTG);
	}

	public void transformClicked(int type, Transform3D transform) {
		callback.transformClicked(PickingCallback.SELECTION, currentTG);
	}

	public void transformDoubleClicked(int type, Transform3D transform) {
		callback.transformDoubleClicked(PickingCallback.SELECTION, currentTG);
	}

	/*
	 * Update the scene to manipulate any nodes.
	 * 
	 * @param xpos Current mouse X pos. @param ypos Current mouse Y pos.
	 */
	public void updateScene(int xpos, int ypos) {
		TransformGroup tg = null;
		if ((mevent.getModifiersEx() & MouseEvent.BUTTON1) == MouseEvent.BUTTON1) {
			pickCanvas.setShapeLocation(xpos, ypos);
			PickResult r = pickCanvas.pickClosest();
			if (r != null) {
				tg = (TransformGroup) r.getNode(PickResult.TRANSFORM_GROUP);
				if ((tg != null)
						&& (tg
								.getCapability(TransformGroup.ALLOW_TRANSFORM_READ))
						&& (tg
								.getCapability(TransformGroup.ALLOW_TRANSFORM_WRITE))) {
					drag.wakeup();
					currentTG = tg;
					if (callback != null) {
						callback.transformClicked(PickingCallback.SELECTION,
								currentTG);
					}
				}
			} else if (callback != null) {
				callback.transformClicked(PickingCallback.NO_PICK, null);
			}
		}
	}
}
