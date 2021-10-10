/*
 * <p>Title: Force Field Explorer</p>
 * <p>Description: Force Field Explorer Molecular Modeling Program</p>
 * <p>Copyright: Copyright (c) 2004-2021 Jay William Ponder</p>
 * <p>Institution: Jay Ponder Lab, Washington University in Saint Louis</p>
 * @author Michael J. Schnieders
 * @version 8.10
 */

package ffe.behaviors;

import org.jogamp.java3d.Bounds;
import org.jogamp.java3d.BranchGroup;
import org.jogamp.java3d.Canvas3D;
import org.jogamp.java3d.Transform3D;
import org.jogamp.java3d.TransformGroup;
import org.jogamp.java3d.utils.picking.PickResult;

/*
 * The PickOrbitBehavior class implements a mouse orbit behavior.
 */
public class PickOrbitBehavior extends PickMouseBehavior implements
		MouseBehaviorCallback {
	public MouseOrbit orbit;

	private PickingCallback callback = null;

	private TransformGroup currentTG;

	public PickOrbitBehavior(BranchGroup root, Canvas3D canvas, Bounds bounds,
			TransformGroup VPTG, int pickMode) {
		super(canvas, root, bounds);
		orbit = new MouseOrbit(MouseOrbit.MANUAL_WAKEUP, VPTG);
		orbit.setTransformGroup(currGrp);
		currGrp.addChild(orbit);
		orbit.setSchedulingBounds(bounds);
		setSchedulingBounds(bounds);
		pickCanvas.setMode(pickMode);
	}

	/*
	 * Return the pickMode component of this PickTranslateBehavior.
	 */
	public int getPickMode() {
		return pickCanvas.getMode();
	}

	/*
	 * Sets the pickMode component of this PickTranslateBehavior to the value of
	 * the passed pickMode. @param pickMode the pickMode to be copied.
	 */
	public void setPickMode(int pickMode) {
		pickCanvas.setMode(pickMode);
	}

	public void setTransformGroups(TransformGroup StarTG, TransformGroup VPTG) {
		orbit.setTransformGroups(StarTG, VPTG);
	}

	/*
	 * Register the class @param callback to be called each time the picked
	 * object moves
	 */
	public void setupCallback(PickingCallback c) {
		callback = c;
		if (callback == null) {
			orbit.setupCallback(null);
		} else {
			orbit.setupCallback(this);
		}
	}

	/*
	 * Callback method from MouseOrbit This is used when the Picking callback is
	 * enabled
	 */
	public void transformChanged(int type, Transform3D transform) {
		callback.transformChanged(PickingCallback.ORBIT, currentTG);
	}

	public void transformClicked(int type, Transform3D transform) {
		callback.transformClicked(PickingCallback.ORBIT, currentTG);
	}

	public void transformDoubleClicked(int type, Transform3D transform) {
		callback.transformDoubleClicked(PickingCallback.ORBIT, currentTG);
	}

	/*
	 * Update the scene to manipulate any nodes. This is not meant to be called
	 * by users. Behavior automatically calls this. You can call this only if
	 * you know what you are doing.
	 * 
	 * @param xpos Current mouse X pos. @param ypos Current mouse Y pos.
	 */
	public void updateScene(int xpos, int ypos) {
		TransformGroup tg = null;
		if (mevent.isMetaDown() && !mevent.isAltDown()) {
			pickCanvas.setShapeLocation(xpos, ypos);
			PickResult r = pickCanvas.pickClosest();
			if (r != null) {
				tg = (TransformGroup) r.getNode(PickResult.TRANSFORM_GROUP);
				if ((tg != null)
						&& (tg
								.getCapability(TransformGroup.ALLOW_TRANSFORM_READ))
						&& (tg
								.getCapability(TransformGroup.ALLOW_TRANSFORM_WRITE))) {
					orbit.setTransformGroup(tg);
					orbit.wakeup();
					currentTG = tg;
					if (callback != null) {
						callback.transformClicked(PickingCallback.ORBIT,
								currentTG);
					}
				}
			} else if (callback != null) {
				callback.transformChanged(PickingCallback.NO_PICK, null);
			}
		}
	}
}
