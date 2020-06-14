/*
 * <p>Title: Force Field Explorer</p>
 * <p>Description: Force Field Explorer Molecular Modeling Program</p>
 * <p>Copyright: Copyright (c) 2004-2020 Jay William Ponder</p>
 * <p>Institution: Jay Ponder Lab, Washington University in Saint Louis</p>
 * @author Michael J. Schnieders
 * @version 8.8
 */

package ffe.mm;

import java.util.logging.Level;
import java.util.logging.Logger;

import ffe.mm.ForceField.ForceFieldType;

/*
 * The BaseType class.
 */
public abstract class BaseType {

	
	public final ForceFieldType forceFieldType;

	public final String key;

	protected final Logger logger = Logger.getLogger("ffe");

	public BaseType(ForceFieldType forceFieldType, int keys[]) {
		this.forceFieldType = forceFieldType;
		if (keys == null) {
			key = null;
		} else {
			StringBuffer keyBuffer = new StringBuffer(Integer.toString(keys[0]));
			for (int i = 1; i < keys.length; i++) {
				keyBuffer.append(" " + keys[i]);
			}
			key = keyBuffer.toString();
		}
	}

	public BaseType(ForceFieldType forceFieldType, String key) {
		this.forceFieldType = forceFieldType;
		this.key = key;
	}

	public void log() {
		if (logger.isLoggable(Level.FINE))
			logger.fine(this.toString());
	}

	/*
	 * Print the Type to System.out.
	 */
	public void print() {
		logger.info(toString());
	}

}
