/*
 * <p>Title: Force Field Explorer</p>
 * <p>Description: Force Field Explorer Molecular Modeling Program</p>
 * <p>Copyright: Copyright (c) 2004-2021 Jay William Ponder</p>
 * <p>Institution: Jay Ponder Lab, Washington University in Saint Louis</p>
 * @author Michael J. Schnieders
 * @version 8.10
 */

package ffe.core;

/*
 * The FileCloser class wraps the closing of an FFESystem within a thread.
 */
public final class FileCloser implements Runnable {
	FFESystem ffeSystem;

	public FileCloser(FFESystem sys) {
		ffeSystem = sys;
	}

	public void run() {
		ffeSystem.destroy();
	}
}
