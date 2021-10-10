/*
 * <p>Title: Force Field Explorer</p>
 * <p>Description: Force Field Explorer Molecular Modeling Program</p>
 * <p>Copyright: Copyright (c) 2004-2021 Jay William Ponder</p>
 * <p>Institution: Jay Ponder Lab, Washington University in Saint Louis</p>
 * @author Michael J. Schnieders
 * @version 8.10
 */

package ffe.lang;

import java.util.logging.Logger;

/*
 * The Molecule class is a general container used for simple compounds or in
 * cases where more specialized classes have not been implemented.
 */
public class Molecule extends MSGroup {
	/*
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final int MultiScaleLevel = 2;
	private Logger logger = Logger.getLogger("ffe");
	
	public Molecule() {
	}

	public Molecule(String name, boolean bk) {
		super(name, bk);
	}

	/*
	 * Allows adding Atom FNodes to the Molecule.
	 */
	public void addMSNode(MSNode o) {
		if (o instanceof Atom) {
			getAtomNode().add(o);
			setFinalized(false);
		} else {
			logger.warning("Can not add " + o.getClass()
					+ " to a Molecule, not of type Atom");
		}
	}

	public void finalize(boolean finalizeGeometry) {
		setFinalized(false);
		getAtomNode().setName("Atoms: " + getAtomList().size());
		if (finalizeGeometry) {
			if (!getBondsKnown()) {
				constructValenceTerms();
			} else {
				collectValenceTerms();
			}
			removeLeaves();
		}
		// findDangelingAtoms();
		setCenter(getMultiScaleCenter(false));
		setFinalized(true);
	}
}
