/*
 * <p>Title: Force Field Explorer</p>
 * <p>Description: Force Field Explorer Molecular Modeling Program</p>
 * <p>Copyright: Copyright (c) 2004-2019 Jay William Ponder</p>
 * <p>Institution: Jay Ponder Lab, Washington University in Saint Louis</p>
 * @author Michael J. Schnieders
 * @version 8.6
 */

package ffe.parsers;

import java.util.ArrayList;

import ffe.lang.Atom;
import ffe.lang.Bond;
import ffe.lang.MolecularAssembly;

/*
 * The MergeFilter class allows Force Field Explorer to treat merging
 * of Systems just like opening a file from a hard disk or socket.
 */
public class MergeFilter extends SystemFilter {
	public MergeFilter(MolecularAssembly f, ArrayList<Atom> a, ArrayList<Bond> b) {
		super(f);
		atomList = a;
		bondList = b;
	}

	/*
	 * 
	 */
	public boolean readFile() {
		return true;
	}

	/*
	 * 
	 */
	public boolean writeFile() {
		return false;
	}
}
