/*
 * <p>Title: Force Field Explorer</p>
 * <p>Description: Force Field Explorer Molecular Modeling Program</p>
 * <p>Copyright: Copyright (c) 2004-2021 Jay William Ponder</p>
 * <p>Institution: Jay Ponder Lab, Washington University in Saint Louis</p>
 * @author Michael J. Schnieders
 * @version 8.10
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
 * The PickTranslateBehavior class implements a translation behavior on a picked
 * scenegraph object.
 */
public class PickTranslateBehavior extends PickMouseBehavior implements
		MouseBehaviorCallback {
	public MouseTranslate translate;

	private PickingCallback callback = null;

	private TransformGroup currentTG;

	public PickTranslateBehavior(BranchGroup root, Canvas3D canvas,
			Bounds bounds, TransformGroup VPTG, int pickMode) {
		super(canvas, root, bounds);
		translate = new MouseTranslate(MouseBehavior.MANUAL_WAKEUP, VPTG);
		translate.setTransformGroup(currGrp);
		translate.setFactor(0.1);
		currGrp.addChild(translate);
		translate.setSchedulingBounds(bounds);
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

	/*
	 * Register the class @param callback to be called each time the picked
	 * object moves
	 */
	public void setupCallback(PickingCallback callback) {
		this.callback = callback;
		if (callback == null) {
			translate.setupCallback(null);
		} else {
			translate.setupCallback(this);
		}
	}

	public void transformChanged(int type, Transform3D transform) {
		callback.transformChanged(PickingCallback.TRANSLATE, currentTG);
	}

	public void transformClicked(int type, Transform3D transform) {
		callback.transformClicked(PickingCallback.TRANSLATE, currentTG);
	}

	public void transformDoubleClicked(int type, Transform3D transform) {
		callback.transformDoubleClicked(PickingCallback.TRANSLATE, currentTG);
	}

	/*
	 * Update the scene to manipulate any nodes. This is not meant to be called
	 * by users. Behavior automatically calls this. You can call this only if
	 * you know what you are doing.
	 * 
	 * @param xpos Current mouse X pos. @param ypos Current mouse Y pos.
	 */
	public void updateScene(int xpos, int ypos) {
		if ((mevent.getModifiersEx() & MouseEvent.BUTTON3_DOWN_MASK) == MouseEvent.BUTTON3_DOWN_MASK) {
			pickCanvas.setShapeLocation(xpos, ypos);
			PickResult r = pickCanvas.pickClosest();
			if (r != null) {
				if (callback != null) {
					callback.transformChanged(PickingCallback.NO_PICK, null);
				}
			}
		}
	}
}
