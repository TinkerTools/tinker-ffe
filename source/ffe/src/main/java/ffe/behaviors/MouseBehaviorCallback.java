/*
 * <p>Title: Force Field Explorer</p>
 * <p>Description: Force Field Explorer Molecular Modeling Program</p>
 * <p>Copyright: Copyright (c) 2004-2021 Jay William Ponder</p>
 * <p>Institution: Jay Ponder Lab, Washington University in Saint Louis</p>
 * @author Michael J. Schnieders
 * @version 8.10
 */

package ffe.behaviors;

import org.jogamp.java3d.Transform3D;

/*
 * The MouseBehaviorCallback interface is implemented by classes that want to
 * receive callbacks when transforms are updated.
 */

public interface MouseBehaviorCallback {
	public final static int ROTATE = 0;

	public final static int TRANSLATE = 1;

	public final static int ZOOM = 2;

	public final static int SELECTION = 4;

	public final static int PROPERTIES = 5;

	public final static int ORBIT = 6;

	/*
	 * Classes implementing this interface that are registered with one of
	 * the MouseBehaviors will be called every time the behavior updates
	 * the Transform @param type will be one of ROTATE, TRANSLATE or ZOOM
	 */
	public void transformChanged(int type, Transform3D transform);

	public void transformClicked(int type, Transform3D transform);

	public void transformDoubleClicked(int type, Transform3D transform);
}
