/*
 * <p>Title: Force Field Explorer</p>
 * <p>Description: Force Field Explorer Molecular Modeling Program</p>
 * <p>Copyright: Copyright (c) 2004-2019 Jay William Ponder</p>
 * <p>Institution: Jay Ponder Lab, Washington University in Saint Louis</p>
 * @author Michael J. Schnieders
 * @version 8.6
 */

package ffe.lang;

/*
 * The MSRoot class is the root of the Force Field Explorer data structure.
 */
public class MSRoot extends MSNode {
	/*
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final int MultiScaleLevel = ROLS.MaxLengthScale;

	/*
	 * Default MSRoot Constructor
	 */
	public MSRoot() {
		super("Structural Heirarchy");
	}

	public String toString() {
		return "Structural Heirarchy";
	}
}
