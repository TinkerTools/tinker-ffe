/*
 * <p>Title: Force Field Explorer</p>
 * <p>Description: Force Field Explorer Molecular Modeling Program</p>
 * <p>Copyright: Copyright (c) 2004-2019 Jay William Ponder</p>
 * <p>Institution: Jay Ponder Lab, Washington University in Saint Louis</p>
 * @author Michael J. Schnieders
 * @version 8.7
 */

package ffe.behaviors;

import org.jogamp.java3d.TransformGroup;

/*
 * The PickingCallback interface is implemented by classes wishing to recieve
 * notification that a picked object has moved.
 */
public interface PickingCallback {
	public final static int ROTATE = 0;

	public final static int TRANSLATE = 1;

	public final static int ZOOM = 2;

	public final static int SELECTION = 4;

	public final static int PROPERTIES = 5;

	public final static int ORBIT = 6;

	/*
	 * The user made a selection but nothing was actually picked
	 */
	public final static int NO_PICK = 3;

	/*
	 * Called by the Pick Behavior with which this callback is registered each
	 * time the Picked object is moved
	 */
	public void transformChanged(int type, TransformGroup tg);

	public void transformClicked(int type, TransformGroup tg);

	public void transformDoubleClicked(int type, TransformGroup tg);
}
