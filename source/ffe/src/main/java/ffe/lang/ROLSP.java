/*
 * <p>Title: Force Field Explorer</p>
 * <p>Description: Force Field Explorer Molecular Modeling Program</p>
 * <p>Copyright: Copyright (c) 2004-2019 Jay William Ponder</p>
 * <p>Institution: Jay Ponder Lab, Washington University in Saint Louis</p>
 * @author Michael J. Schnieders
 * @version 8.7
 */

package ffe.lang;

import java.util.Enumeration;
import java.util.List;
import java.util.logging.Logger;

import org.jogamp.java3d.BranchGroup;

/*
 * The ROLSP class is used for Proof-Of-Concept Parallel Recusive Over Length
 * Scales (ROLS) Methods (currently only on shared memory systems). Simply
 * Simply inserting a ParallelMSM node into the Hierarchy causes a seperate
 * thread of execution to be created for all operations on nodes below the ROLSP
 * node. This is very preliminary code, but a useful concept for parallelizing
 * ROLS in ffe.lang.
 */
public class ROLSP extends MSNode implements ROLS, Runnable {

	private Logger logger = Logger.getLogger("ffe");
	
	public enum PARALLELMETHOD {
		SETVIEW, NONE;
	}

	/*
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static boolean GO_PARALLEL = false;

	public static int parallelNotDone = 0;

	static {
		try {
			boolean b = Boolean.parseBoolean(System.getProperty(
					"ffe.lang.parallel", "false"));
			GO_PARALLEL = b;
		} catch (Exception e) {
			GO_PARALLEL = false;
		}
	}

	private PARALLELMETHOD parallelMethod = PARALLELMETHOD.NONE;

	private Thread thread = null;

	private long startTime = 0;

	private long threadTime = 0;

	private RendererCache.ViewModel viewModel = null;

	private List<BranchGroup> newShapes = null;

	public ROLSP() {
		super("Parallel Node");
	}

	/*
	 * Overidden equals method.
	 */
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}
		return false;
	}

	public int hashCode() {
		MSNode child = (MSNode) getChildAt(0);
		if (child == null) {
			return HashCodeUtil.hash(HashCodeUtil.DATANODESEED, "none"
					.hashCode());
		}
		return HashCodeUtil
				.hash(HashCodeUtil.PARALLELMSMSEED, child.hashCode());
	}

	public void run() {
		switch (parallelMethod) {
		case SETVIEW:
			setView(viewModel, newShapes);
			break;
		default:
			return;
		}
		threadTime = System.currentTimeMillis() - startTime;
		logger.info("Start Time: " + startTime + " Total Time: "
				+ threadTime);
		parallelNotDone--;
	}

	public void setView(RendererCache.ViewModel viewModel,
			List<BranchGroup> newShapes) {
		// Set Up the Parallel setView Method
		if (parallelMethod == PARALLELMETHOD.NONE) {
			startTime = System.currentTimeMillis();
			this.viewModel = viewModel;
			this.newShapes = newShapes;
			parallelMethod = PARALLELMETHOD.SETVIEW;
			thread = new Thread(this);
			thread.setName(getParent().toString() + ": Parallel setView MSM");
			thread.setPriority(Thread.MAX_PRIORITY);
			parallelNotDone++;
			thread.start();
		} else if (parallelMethod == PARALLELMETHOD.SETVIEW) {
			// setView has been called from within the 'run' method of the
			// "setView" thread
			for (Enumeration e = children(); e.hasMoreElements();) {
				MSNode node = (MSNode) e.nextElement();
				node.setView(viewModel, newShapes);
			}
			parallelMethod = PARALLELMETHOD.NONE;
			thread = null;
			viewModel = null;
			newShapes = null;
		} else {
			logger.info("Parallel setView method called by: "
					+ parallelMethod);
			return;
		}
	}

	public String toString() {
		if (threadTime != 0) {
			return "Parallel Node " + threadTime + " (msec)";
		}
		return "Parallel Node";
	}
}
