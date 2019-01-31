/*
 * <p>Title: Force Field Explorer</p>
 * <p>Description: Force Field Explorer Molecular Modeling Program</p>
 * <p>Copyright: Copyright (c) 2004-2019 Jay William Ponder</p>
 * <p>Institution: Jay Ponder Lab, Washington University in Saint Louis</p>
 * @author Michael J. Schnieders
 * @version 8.5
 */

package ffe.behaviors;

import org.jogamp.java3d.Bounds;
import org.jogamp.java3d.BranchGroup;
import org.jogamp.java3d.Canvas3D;
import org.jogamp.java3d.Transform3D;
import org.jogamp.java3d.TransformGroup;
import org.jogamp.java3d.utils.picking.PickResult;

/*
 * The PickPropertiesBehavior class.
 */

public class PickPropertiesBehavior extends PickMouseBehavior implements
		MouseBehaviorCallback {
	MouseProperties drag;

	private PickingCallback callback = null;

	private TransformGroup currentTG;

	public PickPropertiesBehavior(BranchGroup root, Canvas3D canvas,
			Bounds bounds, TransformGroup VPTG, int pickMode) {
		super(canvas, root, bounds);
		drag = new MouseProperties(MouseProperties.MANUAL_WAKEUP, VPTG);
		drag.setTransformGroup(currGrp);
		currGrp.addChild(drag);
		drag.setSchedulingBounds(bounds);
		setSchedulingBounds(bounds);
		pickCanvas.setMode(pickMode);
	}

	public int getPickMode() {
		return pickCanvas.getMode();
	}

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
		callback.transformChanged(PickingCallback.PROPERTIES, currentTG);
	}

	public void transformClicked(int type, Transform3D transform) {
		callback.transformClicked(PickingCallback.PROPERTIES, currentTG);
	}

	public void transformDoubleClicked(int type, Transform3D transform) {
		callback.transformDoubleClicked(PickingCallback.PROPERTIES, currentTG);
	}

	/*
	 * Update the scene to manipulate any nodes.
	 * 
	 * @param xpos Current mouse X pos. @param ypos Current mouse Y pos.
	 */
	public void updateScene(int xpos, int ypos) {
		TransformGroup tg = null;
		if (!mevent.isMetaDown() && !mevent.isAltDown()) {
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
					if (callback != null && mevent.getClickCount() == 2) {
						callback.transformDoubleClicked(
								PickingCallback.PROPERTIES, currentTG);
					}
				}
			} else if (callback != null) {
				callback.transformDoubleClicked(PickingCallback.NO_PICK, null);
			}
		}
	}
}
