/*
 * <p>Title: Force Field Explorer</p>
 * <p>Description: Force Field Explorer Molecular Modeling Program</p>
 * <p>Copyright: Copyright (c) 2004-2021 Jay William Ponder</p>
 * <p>Institution: Jay Ponder Lab, Washington University in Saint Louis</p>
 * @author Michael J. Schnieders
 * @version 8.10
 */

package ffe;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.URL;

import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.UIManager;

import org.apache.commons.lang.SystemUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.time.StopWatch;

import sun.misc.Signal;
import sun.misc.SignalHandler;

import ffe.core.MainPanel;
import ffe.macos.OSXAdapter;

/*
 * The Main class is the entry point to Force Field Explorer
 * and does some initializations
 */
public class Main extends JFrame {

	/*
	 * Look for Java3D extensions. If they are not found, directions
	 * are printed on where to find them. The JARs should always be
	 * in the ffe/lib directory, while the native libraries should
	 * be in ffe/native/${platform}
	 */
	private void checkJava3D() {
		try {
			getClass().getClassLoader().loadClass("org.jogamp.java3d.Canvas3D");
		} catch (Exception e) {
			logger.severe("\n\nERROR: Java3D Extensions Are Not Installed\n\n" + e);
			logger.severe("\n\nERROR: Force Field Explorer Cannot Continue\n\n");
			System.exit(-1);
		}
	}

	/*
	 * Set up a console and file logger
	 */
	private static void configureLoggers() {
		try {
			// Remove the console handler from the root logger
			Logger defaultLogger = LogManager.getLogManager().getLogger("");
			Handler defaultHandlers[] = defaultLogger.getHandlers();
			for (Handler h : defaultHandlers) {
				defaultLogger.removeHandler(h);
			}
			// Set the logger to forward only certain messages
			logger.setLevel(Level.ALL);

			// Create a file handler for FFE logging
			Handler fileHandler = new FileHandler("ffe.log");
			fileHandler.setLevel(Level.ALL);
			fileHandler.setFormatter(new SimpleFormatter());
			logger.addHandler(fileHandler);

			// Create a console handler for FFE logging
			Handler consoleHandler = new ConsoleHandler();
			consoleHandler.setLevel(Level.ALL);
			logger.addHandler(consoleHandler);
		} catch (Exception e) {
			System.err.println("Exception During Initialization of Logging\n" + e);
		}
	}

	/*
	 * Create an instance of Force Field Explorer
	 */
	public static void main(String[] args) throws Exception {
		// MacOS specific features to help FFE look native;
		// These need to be set before the MainPanel is created
		if (SystemUtils.IS_OS_MAC) {
			OSXAdapter.setOSXProperties();
		}

		// Start the clock
		stopWatch.start();
		// Set up logging
		configureLoggers();
		logger.info("Force Field Explorer is Starting Up");

		// If a file was supplied on the command line, get its absolute path
		File commandLineFile = null;
		if (args != null && args.length > 0) {
			commandLineFile = new File(args[0]);
			// Resolve a relavtive path
			if (commandLineFile.exists()) {
				commandLineFile = new File(commandLineFile.getAbsolutePath());
			}
		}
		// Set some Swing Constants
		UIManager.put("swing.boldMetal", Boolean.FALSE);
		setDefaultLookAndFeelDecorated(false);
		// Initialize the main frame and Force Field Explorer MainPanel
		Main m = new Main(commandLineFile);
		if (System.getProperty("ffe.timer") != null) {
			logger.info("\nStart-up Time (msec): " + stopWatch.getTime());
			Runtime runtime = Runtime.getRuntime();
			runtime.runFinalization();
			runtime.gc();
			long occupiedMemory = runtime.totalMemory() - runtime.freeMemory();
			long KB = 1024;
			logger.info("Memory for: " + m + "\nIn-Use Memory   (Kb): " + occupiedMemory / KB
					+ "\nFree Memory     (Kb): " + runtime.freeMemory() / KB
					+ "\nTotal Memory    (Kb): " + runtime.totalMemory() / KB);
		}
	}

	/*
	 * Main does some window initializations
	 */
	public Main(File commandLineFile) {
		super("Force Field Explorer");
		this.setVisible(false);

		// Check for Java3D Extensions
		checkJava3D();
		// Catch 'Control-C' events so FFE can save preferences
		// and shut down cleanly
		SignalHandler handler = new SignalHandler() {
			public void handle(Signal sig) {
				logger.severe(" Signal: " + sig.getName() + " - "
						+ sig.getNumber() + "\n"
						+ " Force Field Explorer is Shutting Down");
				try {
					if (mainPanel != null) {
						mainPanel.exit();
					}
				} catch (Exception e) {
					logger.severe("FFE Could Not Cleanly Shut Down\n" + e);
				}
				System.exit(-1);
			}
		};
		Signal.handle(new Signal("INT"), handler);

		// Create the MainPanel and MainMenu, then add them to the JFrame
		java.awt.Toolkit.getDefaultToolkit().setDynamicLayout(true);
		mainPanel = new MainPanel(this);
		add(mainPanel);
		mainPanel.initialize();
		setJMenuBar(mainPanel.getMainMenu());

		// Set the Title and Icon
		setTitle("Force Field Explorer");
		URL iconURL = getClass().getClassLoader().getResource(
				"ffe/icons/icon64.png");
		ImageIcon icon = new ImageIcon(iconURL);
		setIconImage(icon.getImage());
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				if (mainPanel != null) {
					mainPanel.exit();
				}
				System.exit(0);
			}
		});

		// This is a hack to get GraphicsCanvas to initialize
		// on some combinations of platform and Java3D
		mainPanel.setPanel(MainPanel.LOGS);
		setVisible(true);
		mainPanel.setPanel(MainPanel.GRAPHICS);

		// MacOS specific features to help FFE look native on Macs;
		// This needs to be done after the MainPanel is created
		if (SystemUtils.IS_OS_MAC_OSX) {
			OSXAdapter.macOSXRegistration(mainPanel);
		}

		// Finally, open the supplied file if necessary
		if (commandLineFile != null) {
			if (commandLineFile.exists()) {
				mainPanel.open(commandLineFile, null);
			} else {
				logger.warning(commandLineFile.toString() + " was not found");
			}
		}
	}

	/*
	 * Commons.Lang Style toString
	 */
	public String toString() {
		ToStringBuilder toStringBuilder = new ToStringBuilder(this).append(
				"Up Time: " + stopWatch).append("Logger: " + logger.getName());
		return toStringBuilder.toString();
	}

	/*
	 * This is the main application wrapper
	 */
	public MainPanel mainPanel;
	public static StopWatch stopWatch = new StopWatch();
	private static Logger logger = Logger.getLogger("ffe");
}
