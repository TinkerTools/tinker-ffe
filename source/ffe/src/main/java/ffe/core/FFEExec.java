/*
 * <p>Title: Force Field Explorer</p>
 * <p>Description: Force Field Explorer Molecular Modeling Program</p>
 * <p>Copyright: Copyright (c) 2004-2019 Jay William Ponder</p>
 * <p>Institution: Jay Ponder Lab, Washington University in Saint Louis</p>
 * @author Michael J. Schnieders
 * @version 8.7
 */

package ffe.core;

import java.io.File;
import java.util.logging.Logger;

import org.apache.commons.lang.SystemUtils;
import org.apache.commons.lang.builder.ToStringBuilder;

/*
 * FFEExec encapsulates a native replacement for the JDK System.exec() method.
 * Tinker programs are executed in their own thread through a call to a Native
 * method called "FFEExec" which in turn calls the function "system()". The
 * reason we are not using the System.exec() methods is that some Tinker
 * routines execute indefinitely. Users may want to exit Force Field Explorer
 * and shut down the JVM after launching a dynamics run, for example. In this
 * case the thread should not be dependent on a JVM instance.
 */
public class FFEExec implements Runnable {

	private static String path;

	private static String ld_library_path;

	private static String classpath;

	private static Logger logger = Logger.getLogger("ffe");

	// Set PATH (as TINKER/bin), CLASSPATH and LD_LIBRARY_PATH variables
	private void setEnv() {
		path = (new File(MainPanel.tinkerDir + File.separator + "bin"))
				.getAbsolutePath();
		classpath = MainPanel.classpath;
		// java.home should be the jre directory.
		ld_library_path = System.getProperty("java.home", ".");
		if (SystemUtils.IS_OS_LINUX) {
			ld_library_path = ld_library_path + "/lib/amd64/server:"
					+ ld_library_path + "/lib/amd64";
		} else if (SystemUtils.IS_OS_MAC) {
			ld_library_path = ld_library_path + "/lib/server:"
					+ ld_library_path + "/lib";
		} else if (SystemUtils.IS_OS_WINDOWS) {
			ld_library_path = ld_library_path + "\\bin\\client";
			path = path + File.pathSeparator + ld_library_path;
		}
	}

	private FFESystem system;

	private String name;

	private String args;

	private String dir;

	private MainPanel mainPanel;

	private File newFile;

	private boolean alive = true;

	private boolean openOnto;

	private int returnValue = 0;

	/*
	 * Constructor
	 * 
	 * @param s      FFESystem the Native command will execute on
	 * @param n      Name of the log file
	 * @param a      Command to execute
	 * @param d      Directory to launch the command in
	 * @param m      MainPanel
	 * @param file   File to open
	 * @param o      Load resulting version file onto passed FFESystem
	 */
	public FFEExec(FFESystem s, String n, String a, String d, MainPanel m,
			File file, boolean o) {
		system = s;
		name = n;
		args = a;
		dir = d;
		mainPanel = m;
		newFile = file;
		openOnto = o;
		logger.info(toString());
	}

	public int getReturnValue() {
		return returnValue;
	}

	public boolean isAlive() {
		return alive;
	}

	/*
	 * nativeExec method for launching native executables
	 * 
	 * @param argv        String
	 * @param dir         String
	 * @param path        String
	 * @param classpath   String
	 * @param jre         String
	 * @return int
	 */
	private native int nativeExec(String argv, String dir, String path,
			String classpath, String jre);

	/*
	 * Executes the native call to "System()" and notifies the
	 * ResultPanel upon completion. This should only be called
	 * indirectly by Thread.Start()
	 */
	public void run() {
		setEnv();
		if (args == null || dir == null || path == null || classpath == null
				|| ld_library_path == null) {
			Logger.getLogger("ffe").severe(
					"Native Command Cannot be Executed"
						+ "\nCOMMAND: " + args
						+ "\nDIR: " + dir
						+ "\nPATH: " + path
						+ "\nCLASSPATH: " + classpath
						+ "\nLD_LIBRARY_PATH: " + ld_library_path);
			return;
		}
		Logger.getLogger("ffe").info(
				"Native Command Executed"
					+ "\nCOMMAND: " + args
					+ "\nDIR: " + dir
					+ "\nPATH: " + path
					+ "\nCLASSPATH: " + classpath
					+ "\nLD_LIBRARY_PATH: " + ld_library_path);
		System.out.println("args: " + args);
		System.out.println("dir: " + dir);
		System.out.println("path: " + path);
		System.out.println("classpath: " + classpath);
		returnValue = nativeExec(args, dir, path, classpath, ld_library_path);
		// Check for a bad return value
		if (returnValue < 0) {
			Logger.getLogger("ffe").warning(
					"The following job exited with a failure status: "
							+ returnValue + "\n" + args);
		}
		// Open any created file and display the log
		if (mainPanel != null) {
			if (newFile != null) {
				String[] labels = args.split(" +");
				String command = labels[0].toUpperCase() + " on "
						+ system.getFile().getName();
				if (openOnto) {
					mainPanel.openOn(newFile, system, command);
				} else {
					mainPanel.open(newFile, command);
				}
			}
			mainPanel.getLogPanel().setDone(name);
		}
		alive = false;
	}

	/*
	 * Commons.Lang Style toString
	 */
	public String toString() {
		ToStringBuilder toStringBuilder = new ToStringBuilder(this)
				.append(path).append(classpath).append(ld_library_path);
		return toStringBuilder.toString();
	}
}
