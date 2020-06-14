/*
 * <p>Title: Force Field Explorer</p>
 * <p>Description: Force Field Explorer Molecular Modeling Program</p>
 * <p>Copyright: Copyright (c) 2004-2020 Jay William Ponder</p>
 * <p>Institution: Jay Ponder Lab, Washington University in Saint Louis</p>
 * @author Michael J. Schnieders
 * @version 8.8
 */

package ffe.core;

import java.awt.Cursor;
import java.util.logging.Logger;

import org.apache.commons.lang.time.StopWatch;

import ffe.lang.Utilities;
import ffe.lang.Utilities.FileType;
import ffe.parsers.SystemFilter;
import ffe.tinker.SimulationFilter;

/*
 * The FileOpener class opens a file into Force Field Explorer using a
 * filter from the ffe.parsers package; The OpenFile class implements
 * the Runnable interface so that opening a file does not freeze FFE
 */
public class FileOpener implements Runnable {
	private static final long KB = 1024;

	private static final long MB = KB * KB;

	SystemFilter systemFilter = null;

	MainPanel mainPanel = null;

	private boolean timer = false;

	private boolean gc = false;

	private long occupiedMemory;

	private StopWatch stopWatch;
	private Logger logger = Logger.getLogger("ffe");

	public FileOpener(SystemFilter systemFilter, MainPanel mainPanel) {
		this.systemFilter = systemFilter;
		this.mainPanel = mainPanel;
		if (System.getProperty("ffe.timer", "false").equalsIgnoreCase("true")) {
			timer = true;
			if (System.getProperty("ffe.timer.gc", "false").equalsIgnoreCase(
					"true")) {
				gc = true;
			}
		}
	}

	private void open() {
		if (timer) {
			startTimer();
		}
		FFESystem ffeSystem = null;
		// Continue if the file was read in successfully
		if (systemFilter != null && systemFilter.readFile()) {
			ffeSystem = (FFESystem) systemFilter.getMolecularSystem();
			if (ffeSystem.getFileType() != FileType.PDB) {
				logger.info("Determining Structure");
				Utilities.biochemistry(ffeSystem, systemFilter.getAtomList());
			}
			logger.info("Finalizing Model");
			// Add the opened system to the Multiscale Hierarchy
			mainPanel.getHierarchy().addSystemNode(ffeSystem);
		}
		mainPanel.setCursor(Cursor.getDefaultCursor());
		if (ffeSystem != null && timer) {
			stopTimer(ffeSystem);
		}
	}

	public void run() {
		if (mainPanel != null && systemFilter != null) {
			open();
		}
	}

	/*
	 * Rather verbose output for timed File Operations makes
	 * it easy to grep log files for specific information.
	 */
	private void startTimer() {		
		stopWatch = new StopWatch();
		Runtime runtime = Runtime.getRuntime();
		if (gc) {
			runtime.runFinalization();
			runtime.gc();
		}
		occupiedMemory = runtime.totalMemory() - runtime.freeMemory();
		stopWatch.start();
	}

	private void stopTimer(FFESystem ffeSystem) {
		stopWatch.stop();
		logger.info("Opened " + ffeSystem.toString() + " with "
				+ ffeSystem.getAtomList().size() + " atoms.\n"
				+ "File Op Time  (msec): " + stopWatch.getTime());
		Runtime runtime = Runtime.getRuntime();
		if (gc) {
			runtime.runFinalization();
			runtime.gc();
			long moleculeMemory = (runtime.totalMemory() - runtime.freeMemory())
					- occupiedMemory;
			logger.info("File Op Memory  (Kb): " + moleculeMemory / KB);
		}
		occupiedMemory = runtime.totalMemory() - runtime.freeMemory();
		logger.info("\nAfter File Op FFE Up-Time       (sec): "
				+ (MainPanel.stopWatch.getTime()) / 1000 
				+ "\nAfter File Op FFE Memory         (Mb): "
				+ occupiedMemory / MB + " " + runtime.freeMemory() / MB + " "
				+ runtime.totalMemory() / MB);
	}
}
