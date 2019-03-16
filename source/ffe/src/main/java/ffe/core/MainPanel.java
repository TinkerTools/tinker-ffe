/*
 * <p>Title: Force Field Explorer</p>
 * <p>Description: Force Field Explorer Molecular Modeling Program</p>
 * <p>Copyright: Copyright (c) 2004-2019 Jay William Ponder</p>
 * <p>Institution: Jay Ponder Lab, Washington University in Saint Louis</p>
 * @author Michael J. Schnieders
 * @version 8.6
 */

package ffe.core;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GraphicsConfigTemplate;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URL;

import java.net.URLDecoder;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import java.util.Scanner;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileSystemView;
import javax.swing.JTextField;

import org.jogamp.java3d.GraphicsConfigTemplate3D;
import org.jogamp.java3d.Transform3D;
import org.jogamp.java3d.TransformGroup;
import org.jogamp.vecmath.Vector3d;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.commons.lang.time.StopWatch;

import ffe.lang.Atom;
import ffe.lang.Bond;
import ffe.lang.Keyword;
import ffe.lang.MSNode;
import ffe.lang.MSRoot;
import ffe.lang.ROLS;
import ffe.lang.RendererCache;
import ffe.lang.Utilities.FileType;
import ffe.mm.ForceField;
import ffe.parsers.ARCFileFilter;
import ffe.parsers.DYNFileFilter;
import ffe.parsers.DYNFilter;
import ffe.parsers.ForceFieldFileFilter;
import ffe.parsers.ForceFieldFilter;
import ffe.parsers.INTFileFilter;
import ffe.parsers.INTFilter;
import ffe.parsers.InducedFileFilter;
import ffe.parsers.InducedFilter;
import ffe.parsers.KeyFileFilter;
import ffe.parsers.KeyFilter;
import ffe.parsers.MergeFilter;
import ffe.parsers.PDBFileFilter;
import ffe.parsers.PDBFilter;
import ffe.parsers.SystemFilter;
import ffe.parsers.XYZFileFilter;
import ffe.parsers.XYZFilter;
import ffe.properties.FFELocale;

/*
 * The MainPanel class is the main container for Force Field Explorer,
 * handles file input/output and is used to pass references among the
 * various sub-Panels.
 */
public final class MainPanel extends JPanel implements ActionListener,
		ChangeListener {

	// Static Variables
	private static JFileChooser fileChooser = null;

	private static File cwd;

	private static Logger logger = Logger.getLogger("ffe");

	// Panel Order in the TabbedPane
	public static final int GRAPHICS = 0;

	public static final int KEYWORDS = 1;

	public static final int MODELING = 2;

	public static final int LOGS = 3;

	public static String classpath;

	public static File ffeDir;

	public static File tinkerDir;

	public static String ld_library_path;

	// FileFilters for filtering file selection in the JFileChooser
	public static final XYZFileFilter xyzFileFilter = new XYZFileFilter();

	public static final ARCFileFilter arcFileFilter = new ARCFileFilter();

	public static final INTFileFilter intFileFilter = new INTFileFilter();

	public static final DYNFileFilter dynFileFilter = new DYNFileFilter();

	public static final InducedFileFilter indFileFilter = new InducedFileFilter();

	public static final ForceFieldFileFilter forceFieldFileFilter = new ForceFieldFileFilter();

	public static final PDBFileFilter pdbFileFilter = new PDBFileFilter();

	public static final KeyFileFilter keyfilefilter = new KeyFileFilter();

	public static StopWatch stopWatch = new StopWatch();

	static {
		try {
			// FFE HOME DIRECTORY
			String ffeString = System.getenv("FFE_HOME");
			if (ffeString == null)
				ffeString = ".";
			ffeString = System.getProperty("ffe.dir", ffeString);
			ffeDir = new File(ffeString);
			if (SystemUtils.IS_OS_LINUX) {
				System.load(ffeDir.getAbsolutePath()
						+ "/native/linux/libffe.so");
			} else if (SystemUtils.IS_OS_MAC) {
				System.load(ffeDir.getAbsolutePath()
						+ "/native/macos/libffe.jnilib");
			} else if (SystemUtils.IS_OS_WINDOWS) {
				System.load(ffeDir.getAbsolutePath()
						+ "/native/windows/ffe.dll");
			}

			// Tinker Home Directory
			String tinkerString = System.getenv("TINKER_HOME");
			if (tinkerString == null)
				tinkerString = ".";
			tinkerString = System.getProperty("ffe.tinker.dir", tinkerString);
			tinkerDir = new File(tinkerString);

			// CLASSPATH & LD_LIBRARY_PATH
			classpath = System.getProperty("java.class.path");
			ld_library_path = System.getProperty("java.library.path");

			// CURRENT WORKING DIRECTORY
			cwd = MainPanel.getCWD();

			logger.info("\nCLASSPATH: " + classpath + "\nld_library_path: "
					+ ld_library_path + "\nFFE_HOME: " + ffeDir.getAbsolutePath()
					+ "\nTINKER_HOME: " + tinkerDir.getAbsolutePath() + "\nCURRENT_DIRECTORY: "
					+ cwd + "\n");

		} catch (Exception e) {
			logger.severe("FFE/Tinker Directories Could Not be Found\n" + e);
		}
	}

	public static File getCWD() {
		if (cwd == null) {
			cwd = new File(System.getProperty("user.dir", FileSystemView
					.getFileSystemView().getDefaultDirectory()
					.getAbsolutePath()));
		}
		return cwd;
	}

	/**
	 * JFileChooser
	 */
	public static JFileChooser getFileChooser() {
		if (fileChooser == null) {
			fileChooser = new JFileChooser();
		}
		fileChooser.resetChoosableFileFilters();
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileChooser.setFileHidingEnabled(true);
		fileChooser.setAcceptAllFileFilterUsed(true);
		fileChooser.setCurrentDirectory(getCWD());
		fileChooser.setMultiSelectionEnabled(false);
		fileChooser.setSelectedFile(null);
		return fileChooser;
	}

	// Force Field Explorer Panels and Components
	private JFrame frame;

	private MSRoot dataRoot;

	private Hierarchy hierarchy;

	private MainMenu menuMenu;

	private GraphicsPanel graphicsPanel;

	private ModelingPanel modelingPanel;

	private KeywordPanel keywordPanel;

	private LogPanel logPanel;

	private GraphicsCanvas graphicsCanvas;

	// Misc. Components
	// The SplitPane holds the Hierarchy and JTabbedPane
	private JSplitPane splitPane;

	private int splitPaneDivider;

	// Holds 3D Graphics, Keyword Editor, Modeling Commands and Log Panels
	private JTabbedPane tabbedPane;

	private JLabel statusLabel;

	private ForceFieldFilter forceFieldFilter;

	private FFELocale locale = null;

	private JDialog aboutDialog = null;

	private JTextArea aboutTextArea = null;

	private Thread openThread = null;

	private boolean oscillate = false;

	// Tinker Simulation Variables
	private TinkerSimulation simulation = null;

	private String ip = new String("");

	private int port = 2000;

	private InetAddress address = null;

	private InetSocketAddress socketAddress = new InetSocketAddress(port);

	private ModelingShell modelingShell = null;

	/**
	 * Initialize all the sub-Panels and put them together
	 */
	private boolean init = false;

	/**
	 * MainPanel Constructor
	 * 
	 * @param f
	 *            Application Frame
	 */
	public MainPanel(JFrame f) {
		frame = f;
	}

	public void about() {
		if (aboutDialog == null) {
			aboutDialog = new JDialog(frame, "About FFE", true);
			URL tinkerURL = getClass().getClassLoader().getResource(
					"ffe/icons/splash.png");
			ImageIcon logoIcon = new ImageIcon(tinkerURL);
			JLabel logoLabel = new JLabel(logoIcon);
			logoLabel.setBorder(BorderFactory
					.createEtchedBorder(EtchedBorder.RAISED));
			Container contentpane = aboutDialog.getContentPane();
			contentpane.setLayout(new BorderLayout());
			if (aboutTextArea == null) {
				initAbout();
			}
			contentpane.add(aboutTextArea, BorderLayout.SOUTH);
			contentpane.add(logoLabel, BorderLayout.CENTER);
			aboutDialog.pack();
			Dimension dim = getToolkit().getScreenSize();
			Dimension ddim = aboutDialog.getSize();
			aboutDialog.setLocation((dim.width - ddim.width) / 2,
					(dim.height - ddim.height) / 2);
			aboutDialog.setResizable(false);
		}
		aboutDialog.setVisible(true);
	}

	/*
	 * Handle most File, Selection, Trajectory, Simulation,
	 * and Window Menu Commands
	 */
	public void actionPerformed(ActionEvent evt) {
		String arg = evt.getActionCommand();
		if (arg.equals("Open")) {
			open();
		} else if (arg.equals("DownloadFromPubChem")) {
			openFromPubChem();
		} else if (arg.equals("DownloadFromNCI")) {
			openFromNCI();
		} else if (arg.equals("DownloadFromPDB")) {
			openFromPDB();
		} else if (arg.equals("SaveAs")) {
			save(null);
		} else if (arg.equals("Close")) {
			close();
		} else if (arg.equals("CloseAll")) {
			closeAll();
		} else if (arg.equals("ChooseKeyFile")) {
			chooseKey();
		} else if (arg.equals("ChooseLogFile")) {
			chooseLog();
		} else if (arg.equals("LoadRestartData")) {
			openRestart();
		} else if (arg.equals("LoadInducedData")) {
			openInduced();
		} else if (arg.endsWith("ChooseTinkerLocation")) {
			chooseTinker();
		} else if (arg.equals("SelectAll")) {
			selectAll();
		} else if (arg.equals("MergeSelections")) {
			merge();
		} else if (arg.equals("HighlightSelections")) {
			highlightSelections(evt);
		} else if (arg.equals("Play")) {
			play();
		} else if (arg.equals("Stop")) {
			stop();
		} else if (arg.equals("StepForward")) {
			stepForward();
		} else if (arg.equals("StepBack")) {
			stepBack();
		} else if (arg.equals("Reset")) {
			reset();
		} else if (arg.equals("Oscillate")) {
			oscillate(evt);
		} else if (arg.equals("Frame")) {
			frame();
		} else if (arg.equals("Speed")) {
			speed();
		} else if (arg.equals("Skip")) {
			skip();
		} else if (arg.equals("ConnectToLocalJob")) {
			connectToTinker(null, null);
		} else if (arg.equals("ConnectToRemoteJob")) {
			connect();
		} else if (arg.equals("ReleaseJob")) {
			release();
		} else if (arg.equals("SetPort")) {
			setPort();
		} else if (arg.equals("SetRemoteJobAddress")) {
			setRemoteJobAddress();
		} else if (arg.equals("ShowToolBar")) {
			showToolBar(evt);
		} else if (arg.equals("ShowTree")) {
			showTree(evt);
		} else if (arg.equals("ShowGlobalAxes")) {
			showGlobalAxes(evt);
		} else if (arg.equals("ResetPanes")) {
			resetPanes();
		} else if (arg.equals("ResetConsole")) {
			resetShell();
		} else if (arg.equals("OceanLookAndFeel")) {
			oceanLookAndFeel();
		} else if (arg.equals("WindowsLookAndFeel")
				|| arg.equals("MacOSXLookAndFeel")
				|| arg.equals("MotifLookAndFeel")) {
			platformLookAndFeel();
		} else if (arg.equals("ShrinkGraphicsWindow")) {
			resizePanes(20);
		} else if (arg.equals("ExpandGraphicsWindow")) {
			resizePanes(-20);
		} else if (arg.equals("About")) {
			about();
		} else if (arg.equals("GarbageCollect")) {
			Runtime.getRuntime().runFinalization();
			Runtime.getRuntime().gc();
		} else if (arg.equals("Exit")) {
			exit();
		} else {
			System.err.println("MainPanel - Menu command not found: "
					+ arg.toString());
		}
	}

	/*
	 * Prompt the user to select an alternate key file.
	 */
	private void chooseKey() {
		JFileChooser d = MainPanel.getFileChooser();
		d.setFileFilter(new KeyFileFilter());
		d.setAcceptAllFileFilterUsed(false);
		FFESystem sys = getHierarchy().getActive();
		if (sys != null) {
			File newCWD = sys.getFile();
			if (newCWD != null && newCWD.getParentFile() != null) {
				d.setCurrentDirectory(newCWD.getParentFile());
			}
		} else {
			return;
		}
		int result = d.showOpenDialog(this);
		if (result == JFileChooser.APPROVE_OPTION) {
			File f = d.getSelectedFile();
			sys.setKeyFile(f);
			sys.setKeywords(KeyFilter.open(f));
			getKeywordPanel().loadActive(sys);
		}
	}

	/*
	 * Prompt the user to select an alternate log file
	 */
	private void chooseLog() {
		JFileChooser d = getFileChooser();
		FFESystem sys = getHierarchy().getActive();
		if (sys != null) {
			File cwd = sys.getFile();
			if (cwd != null && cwd.getParentFile() != null) {
				d.setCurrentDirectory(cwd.getParentFile());
			}
		} else {
			return;
		}
		d.setDialogTitle("Select a log file");
		d.setAcceptAllFileFilterUsed(true);
		int result = d.showOpenDialog(this);
		if (result == JFileChooser.APPROVE_OPTION) {
			File f = d.getSelectedFile();
			if (f != null) {
				sys.setLogFile(f);
				setCWD(d.getCurrentDirectory());
				getModelingPanel().selected();
			}
		}
	}

	/*
	 * Prompt the user to select the path to the Tinker directory
	 */
	private void chooseTinker() {
		JFileChooser d = MainPanel.getFileChooser();
		d.setDialogTitle("Select the Tinker Directory");
		d.setSelectedFile(MainPanel.tinkerDir);
		d.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int result = d.showOpenDialog(this);
		if (result == JFileChooser.APPROVE_OPTION) {
			tinkerDir = d.getSelectedFile();
		}
		d.setFileSelectionMode(JFileChooser.FILES_ONLY);
	}

	/*
	 * Detach the active FSystem's BranchGroup from the Scene and clear that
	 * FSystem's data
	 */
	public void close() {
		FFESystem m = hierarchy.getActive();
		close(m);
	}

	public void close(FFESystem closedModel) {
		if (closedModel != null && closedModel.getParent() != null) {
			Trajectory traj = closedModel.getTrajectory();
			if (traj != null) {
				traj.stop();
			}
			if (simulation != null && simulation.getFSystem() == closedModel) {
				release();
			}
			hierarchy.removeTreeNode(closedModel);
		}
	}

	/*
	 * Close all open systems
	 */
	public void closeAll() {
		while (hierarchy.getActive() != null) {
			close();
		}
	}

	/*
	 * Attempt to connect to a Tinker Simulation
	 */
	private void connect() {
		if (simulation == null || simulation.isFinished()) {
			if (simulation != null) {
				simulation.release();
			}
			simulation = new TinkerSimulation(null, null, this, socketAddress);
			simulation.connect();
			menuMenu.setConnect(false);
			setPanel(GRAPHICS);
		}
	}

	public void connectToTinker(FFESystem system, Thread modelingThread) {
		if (simulation == null || simulation.isFinished()) {
			if (simulation != null) {
				simulation.release();
			}
			InetSocketAddress tempAddress = null;
			// Use ipconfig for IP address on Mac, as Java method fails, JWP, 08/2017
			try {
				if (SystemUtils.IS_OS_MAC) {
					final Process p = Runtime.getRuntime().exec("ipconfig getifaddr en0");
					BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
					String line = null;
					String myIP = null;
					while ((line = input.readLine()) != null) {
						myIP = line;
					}
					p.waitFor();
					tempAddress = new InetSocketAddress(myIP, port);
				} else {
					tempAddress = new InetSocketAddress(InetAddress.getLocalHost(),
							port);
				}
			} catch (Exception e) {
				try {
					tempAddress = new InetSocketAddress(InetAddress
							.getByName(null), port);
				} catch (Exception ex) {
					System.err.println("Could not determine Local Host: " + ex);
					return;
				}
			}
			simulation = new TinkerSimulation(system, modelingThread, this,
					tempAddress);
			if (modelingThread != null) {
				modelingThread.start();
			}
			simulation.connect();
			menuMenu.setConnect(false);
			setPanel(GRAPHICS);
		}
	}

	public boolean createKeyFile(FFESystem system) {
		String message = new String("Please select a parameter file "
				+ "and a Tinker Key file will be created.");
		String params = (String) JOptionPane.showInputDialog(this, message,
				"Parameter File", JOptionPane.QUESTION_MESSAGE, null,
				keywordPanel.getParamFiles(), null);
		if (params != null) {
			if (params.equalsIgnoreCase("Use an Existing Tinker KEY file")) {
				JFileChooser fc = getFileChooser();
				fc.setDialogTitle("Choose a KEY File");
				fc.setCurrentDirectory(cwd);
				fc.setSelectedFile(null);
				fc.setFileFilter(keyfilefilter);
				int result = fc.showOpenDialog(this);
				if (result == JFileChooser.APPROVE_OPTION) {
					File keyfile = fc.getSelectedFile();
					if (keyfile.exists()) {
						Hashtable<String, Keyword> keywordHash = KeyFilter
								.open(keyfile);
						if (keywordHash != null) {
							system.setKeywords(keywordHash);
						} else {
							return false;
						}
						system.setKeyFile(keyfile);
						system.setForceField(null);
						return true;
					}
				}
			} else {
				File tempFile = system.getFile();
				if (tempFile.getParentFile().canWrite()) {
					String path = system.getFile().getParent()
							+ File.separatorChar;
					String keyFileName = system.getName() + ".key";
					File keyfile = new File(path + keyFileName);
					try {
						FileWriter fw = new FileWriter(keyfile);
						BufferedWriter bw = new BufferedWriter(fw);
						bw.write("\n");
						bw.write("# Force Field Selection\n");
						String tempParm = keywordPanel.getParamPath(params);
						if (tempParm.indexOf(" ") > 0) {
							tempParm = "\"" + keywordPanel.getParamPath(params)
									+ "\"";
						}
						bw.write("PARAMETERS        " + tempParm + "\n");
						bw.close();
						fw.close();
						Hashtable<String, Keyword> keywordHash = KeyFilter
								.open(keyfile);
						if (keywordHash != null) {
							system.setKeywords(keywordHash);
						} else {
							return false;
						}
						system.setKeyFile(keyfile);
						system.setForceField(null);
						return true;
					} catch (Exception e) {
						logger.warning("" + e);
						message = new String("There was an error creating "
								+ keyfile.getAbsolutePath());
						JOptionPane.showMessageDialog(this, message);
					}
				} else {
					message = new String("Could not create a Key file because "
							+ cwd.getAbsolutePath() + " is not writable");
					JOptionPane.showMessageDialog(this, message);
				}
			}
		}
		return false;
	}

	public void exit() {
		savePrefs();
		System.exit(0);
	}

	public void frame() {
		Trajectory trajectory = getTrajectory();
		if (trajectory == null) {
			return;
		}
		String frameNumber = new String("" + trajectory.getFrame());
		frameNumber = JOptionPane.showInputDialog("Enter the Frame Number",
				frameNumber);
		try {
			int f = Integer.parseInt(frameNumber);
			trajectory.setFrame(f);
		} catch (NumberFormatException e) {
			return;
		}
	}

	public MSRoot getDataRoot() {
		return dataRoot;
	}

	public FFELocale getFFELocale() {
		return locale;
	}

	public GraphicsCanvas getGraphics3D() {
		return graphicsCanvas;
	}

	public Hierarchy getHierarchy() {
		return hierarchy;
	}

	public KeywordPanel getKeywordPanel() {
		return keywordPanel;
	}

	public LogPanel getLogPanel() {
		return logPanel;
	}

	public MainMenu getMainMenu() {
		return menuMenu;
	}

	public Frame getFrame() {
		return frame;
	}

	public ModelingPanel getModelingPanel() {
		return modelingPanel;
	}

	public ModelingShell getModelingShell() {
		if (modelingShell == null) {
			modelingShell = new ModelingShell(this);
		}
		return modelingShell;
	}

	public JLabel getStatusBar() {
		return statusLabel;
	}

	/*
	 * Get the Trajectory wrapper for the active system
	 * 
	 * @return trajectory
	 */
	public Trajectory getTrajectory() {
		FFESystem system = hierarchy.getActive();
		if (system == null) {
			return null;
		}
		if (system.getFileType() != FileType.ARC) {
			return null;
		}
		Trajectory trajectory = system.getTrajectory();
		if (trajectory != null) {
			return trajectory;
		}
		trajectory = new Trajectory(system, this);
		trajectory.setOscillate(oscillate);
		system.setTrajectory(trajectory);
		return trajectory;
	}

	public void highlightSelections(ActionEvent evt) {
		if (evt.getSource() instanceof JCheckBoxMenuItem) {
			JCheckBoxMenuItem jcb = (JCheckBoxMenuItem) evt.getSource();
			if (jcb.isSelected()) {
				hierarchy.setHighlighting(true);
			} else {
				hierarchy.setHighlighting(false);
			}
		} else {
			boolean highlighting = RendererCache.highlightSelections;
			if (highlighting) {
				hierarchy.setHighlighting(false);
				menuMenu.setHighlighting(false);
			} else {
				hierarchy.setHighlighting(true);
				menuMenu.setHighlighting(true);
			}
		}
	}

	private void initAbout() {
		aboutTextArea = new JTextArea(
				"  Copyright (c) M. Schnieders & J. Ponder, 2004-2019\n"
					+ "  All Rights Reserved\n"
					+ "  Email to: ponder@dasher.wustl.edu\n");
		aboutTextArea.setBorder(BorderFactory
				.createEtchedBorder(EtchedBorder.RAISED));
		aboutTextArea.setEditable(false);
	}

	public void initialize() {
		if (init) {
			return;
		}
		init = true;
		String dir = System.getProperty("user.dir", FileSystemView
				.getFileSystemView().getDefaultDirectory().getAbsolutePath());
		if (!SystemUtils.IS_OS_WINDOWS) {
			String tdir = "";
 			for (int i=0; i<dir.length(); i++) {
				char tchar = dir.charAt(i);
				if (tchar == ' ')
					tdir += "\\ ";
				else
					tdir += tchar;
			}
			dir = tdir;
		}
		String topdir = dir.replaceAll("ffe", "");
		setCWD(new File(topdir));
		locale = new FFELocale("en", "US");
		// Splash Screen
		JFrame.setDefaultLookAndFeelDecorated(true);
		JDialog splashScreen = new JDialog(frame, false);
		ClassLoader loader = getClass().getClassLoader();
		ImageIcon logo = new ImageIcon(loader
				.getResource("ffe/icons/splash.png"));
		JLabel tinkerLabel = new JLabel(logo);
		tinkerLabel.setBorder(BorderFactory
				.createEtchedBorder(EtchedBorder.RAISED));
		Container contentpane = splashScreen.getContentPane();
		contentpane.setLayout(new BorderLayout());
		contentpane.add(tinkerLabel, BorderLayout.CENTER);
		splashScreen.setUndecorated(true);
		splashScreen.pack();
		Dimension screenDimension = getToolkit().getScreenSize();
		Dimension splashDimension = splashScreen.getSize();
		splashScreen.setLocation(
				(screenDimension.width - splashDimension.width) / 2,
				(screenDimension.height - splashDimension.height) / 2);
		splashScreen.setResizable(false);
		splashScreen.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		splashScreen.setVisible(true);
		// Make all pop-up Menus Heavyweight so they play nicely with Java3D
		JPopupMenu.setDefaultLightWeightPopupEnabled(false);
		// Create the Root Node
		dataRoot = new MSRoot();
		Border bb = BorderFactory.createEtchedBorder(EtchedBorder.RAISED);
		statusLabel = new JLabel("  ");
		JLabel stepLabel = new JLabel("  ");
		stepLabel.setHorizontalAlignment(JLabel.RIGHT);
		JLabel energyLabel = new JLabel("  ");
		energyLabel.setHorizontalAlignment(JLabel.RIGHT);
		JPanel statusPanel = new JPanel(new GridLayout(1, 3));
		statusPanel.setBorder(bb);
		statusPanel.add(statusLabel);
		statusPanel.add(stepLabel);
		statusPanel.add(energyLabel);
		GraphicsConfigTemplate3D template3D = new GraphicsConfigTemplate3D();
		template3D.setDoubleBuffer(GraphicsConfigTemplate.PREFERRED);
		GraphicsConfiguration gc = GraphicsEnvironment
				.getLocalGraphicsEnvironment().getDefaultScreenDevice()
				.getBestConfiguration(template3D);
		graphicsCanvas = new GraphicsCanvas(gc, this);
		graphicsPanel = new GraphicsPanel(graphicsCanvas, statusPanel);
		// Initialize various Panels
		hierarchy = new Hierarchy(this);
		hierarchy.setStatus(statusLabel, stepLabel, energyLabel);
		keywordPanel = new KeywordPanel(this);
		modelingPanel = new ModelingPanel(this);
		logPanel = new LogPanel(this);
		JPanel treePane = new JPanel(new BorderLayout());
		JScrollPane scrollPane = new JScrollPane(hierarchy,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		treePane.add(scrollPane, BorderLayout.CENTER);
		treePane.add(logPanel.getProgressBar(), BorderLayout.SOUTH);
		ImageIcon graphicsIcon = new ImageIcon(loader
				.getResource("ffe/icons/display.png"));
		ImageIcon keywordIcon = new ImageIcon(loader
				.getResource("ffe/icons/keywords.png"));
		ImageIcon modelingIcon = new ImageIcon(loader
				.getResource("ffe/icons/commands.png"));
		ImageIcon logIcon = new ImageIcon(loader
				.getResource("ffe/icons/data.png"));
		// Put everything together
		tabbedPane = new JTabbedPane();
		tabbedPane.addTab(locale.getValue("Graphics"), graphicsIcon,
				graphicsPanel);
		tabbedPane.addTab(locale.getValue("KeywordEditor"), keywordIcon,
				keywordPanel);
		tabbedPane.addTab(locale.getValue("ModelingCommands"), modelingIcon,
				modelingPanel);
		tabbedPane.addTab(locale.getValue("Logs"), logIcon, logPanel);
		tabbedPane.addChangeListener(this);
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, false,
				treePane, tabbedPane);
		splitPane.setResizeWeight(0.25);
		splitPane.setOneTouchExpandable(true);
		setLayout(new BorderLayout());
		add(splitPane, BorderLayout.CENTER);
		menuMenu = new MainMenu(this);
		add(menuMenu.getToolBar(), BorderLayout.NORTH);
		getModelingShell();
		loadPrefs();
		SwingUtilities.updateComponentTreeUI(SwingUtilities.getRoot(this));
		splashScreen.dispose();
		stopWatch.start();
	}

	public boolean isOpening() {
		return (openThread != null && openThread.isAlive());
	}

	/*
	 * Load preferences from the user node
	 */
	public void loadPrefs() {
		// UIManager.LookAndFeelInfo[] i = UIManager.getInstalledLookAndFeels();
		// for (UIManager.LookAndFeelInfo l : i){
		// System.out.println("" + l);
		// }
		JFrame frame1 = (JFrame) SwingUtilities.getRoot(this);
		Toolkit toolkit = getToolkit();
		Dimension screenSize = toolkit.getScreenSize();
		Preferences prefs = Preferences.userNodeForPackage(MainPanel.class);
		int x = prefs.getInt("MainPanel_x", screenSize.width / 8);
		int y = prefs.getInt("MainPanel_y", screenSize.height / 8);
		int width = prefs.getInt("MainPanel_width", screenSize.width * 3 / 4);
		int height = prefs
				.getInt("MainPanel_height", screenSize.height * 3 / 4);
		if (width > screenSize.width * 0.4 && width < screenSize.width * 0.8
				&& height > screenSize.height * 0.4
				&& height < screenSize.height * 0.8) {
			frame1.setSize(width, height);
		} else {
			frame1.setSize(screenSize.width * 4 / 5, screenSize.height * 4 / 5);
		}
		if (x > 0 && x < screenSize.width / 2 && y > 0
				&& y < screenSize.height / 2) {
			frame1.setLocation(x, y);
		} else {
			frame1.setLocation(screenSize.width / 8, screenSize.height / 8);
		}
		splitPaneDivider = prefs.getInt("MainPanel_divider", 200);
		if (splitPaneDivider < frame1.getWidth() * (1.0f / 4.0f)) {
			splitPaneDivider = (int) (frame1.getWidth() * (1.0f / 4.0f));
		}
		splitPane.setDividerLocation(splitPaneDivider);
		if (!prefs.getBoolean("MainPanel_system", true)) {
			menuMenu.setSystemShowing(false);
			splitPane.setDividerLocation(0);
		} else {
			menuMenu.setSystemShowing(true);
		}
		if (!prefs.getBoolean("MainPanel_menu", true)) {
			remove(menuMenu.getToolBar());
			menuMenu.setMenuShowing(false);
			validate();
		} else {
			menuMenu.setMenuShowing(true);
		}
		try {
			port = prefs.getInt("MainPanel_port", 2000);
			ip = prefs.get("MainPanel_ip", InetAddress.getLocalHost()
					.getHostAddress());
			if (ip != null) {
				address = InetAddress.getByName(ip);
				socketAddress = new InetSocketAddress(address, port);
			} else {
				socketAddress = new InetSocketAddress(port);
			}
		} catch (Exception e) {
			logger.log(Level.WARNING, e.toString());
		}
		graphicsCanvas.loadPrefs();
	}

	public void merge() {
		ArrayList<MSNode> activeNodes = hierarchy.getActiveNodes();
		if (activeNodes.size() >= 2) {
			merge(activeNodes);
		}
	}

	/*
	 * Merge two or more selected FSystem Nodes into one FSystem node.
	 * There are a few gotchas that need to be fixed.
	 */
	public void merge(ArrayList<MSNode> nodesToMerge) {
		ArrayList<MSNode> activeNodes = new ArrayList<MSNode>();
		for (MSNode node : nodesToMerge) {
			if (node != null && !(node instanceof MSRoot)) {
				activeNodes.add(node);
			}
		}
		if (activeNodes.size() <= 1) {
			return;
		}
		// Set up a structure to hold the new system
		FFESystem active = hierarchy.getActive();
		File file = SystemFilter.version(hierarchy.getActive().getFile());
		FFESystem system = new FFESystem(active.getName(), "Merge Result",
				file, true);
		system.setKeyFile(active.getKeyFile());
		system.setKeywords(KeyFilter.open(active.getKeyFile()));
		system.setFileType(active.getFileType());
		// Fill arrays with the atoms and bonds from the systems to be combined
		ArrayList<Atom> mergedAtoms = new ArrayList<Atom>();
		ArrayList<Bond> mergedBonds = new ArrayList<Bond>();
		ArrayList<FFESystem> systems = new ArrayList<FFESystem>();
		TransformGroup parentTransformGroup = null;
		FFESystem parentSystem;
		Transform3D parentTransform3D = new Transform3D();
		Vector3d parentPosition = new Vector3d();
		Vector3d atomPosition = new Vector3d();
		// Tinker Atom Numbers start at 1
		int atomNum = 1;
		Vector3d zero = new Vector3d(0.0, 0.0, 0.0);
		for (MSNode m : activeNodes) {
			parentSystem = (FFESystem) m.getMSNode(FFESystem.class);
			if (parentSystem == null) {
				return;
			}
			if (!systems.contains(parentSystem)) {
				graphicsCanvas.updateSceneWait(parentSystem, false, true,
						RendererCache.ViewModel.WIREFRAME, false, null);
				systems.add(parentSystem);
			}
			// Move each atom into the global frame by applying the System
			// Transform to relative atomic position
			parentTransformGroup = parentSystem.getOriginToRot();
			parentTransformGroup.getTransform(parentTransform3D);
			parentTransform3D.get(parentPosition);
			parentTransform3D.setTranslation(zero);
			// parentTransform3D.setScale(1.0d);
			ArrayList<Atom> atoms = m.getAtomList();
			ArrayList<ROLS> bonds = m.getBondList();
			for (Atom atom : atoms) {
				atom.removeFromParent();
				atom.setXYZIndex(atomNum++);
				mergedAtoms.add(atom);
				atom.getV3D(atomPosition);
				parentTransform3D.transform(atomPosition);
				atomPosition.add(parentPosition);
				atom.moveTo(atomPosition);
			}
			for (ROLS msm : bonds) {
				Bond bond = (Bond) msm;
				bond.removeFromParent();
				mergedBonds.add((Bond) msm);
			}
		}
		for (FFESystem sys : systems) {
			close(sys);
		}
		MergeFilter mergeFilter = new MergeFilter(system, mergedAtoms,
				mergedBonds);
		FileOpener fileOpener = new FileOpener(mergeFilter, this);
		Thread thread = new Thread(fileOpener);
		thread.start();
	}

	public void merge(MSNode[] nodesToMerge) {
		ArrayList<MSNode> activeNodes = new ArrayList<MSNode>();
		for (MSNode node : nodesToMerge) {
			if (node != null) {
				activeNodes.add(node);
			}
		}
		if (activeNodes.size() > 1) {
			merge(activeNodes);
		}
	}

	public void oceanLookAndFeel() {
		try {
			UIManager.setLookAndFeel(UIManager
					.getCrossPlatformLookAndFeelClassName());
			SwingUtilities.updateComponentTreeUI(SwingUtilities.getRoot(this));
		} catch (Exception e) {
			return;
		}
	}

	/*
	 * Trys to open a file picked from a JFileChooser
	 */
	private void open() {
		if (openThread != null && openThread.isAlive()) {
			return;
		}
		JFileChooser fc = getFileChooser();
		fc.setDialogTitle("Choose XYZ/Archive File");
		fc.setAcceptAllFileFilterUsed(true);
		fc.addChoosableFileFilter(xyzFileFilter);
		fc.addChoosableFileFilter(pdbFileFilter);
		fc.addChoosableFileFilter(intFileFilter);
		fc.addChoosableFileFilter(arcFileFilter);
		int result = fc.showOpenDialog(this);
		if (result == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			open(file, null);
		}
	}

	/*
	 * Attempts to load the supplied file
	 * 
	 * @param file
	 *            File to open
	 * @param commandDescription
	 *            Description of the command that created this file.
	 */
	public void open(File file, String commandDescription) {
		if (file == null || !file.isFile() || !file.canRead()) {
			return;
		}
		file = new File(FilenameUtils.normalize(file.getAbsolutePath()));
		// Set the Current Working Directory based on this file.
		setCWD(file.getParentFile());
		// Get "filename" from "filename.extension".
		String fileName = FilenameUtils.getBaseName(file.getName());
		// Create a FFESystem for this file.
		FFESystem newSystem = new FFESystem(fileName, commandDescription, file,
				false);
		SystemFilter systemFilter = null;
		// Decide which parser to use.
		if (xyzFileFilter.acceptDeep(file)) {
			// Use the Tinker Cartesian Coordinate File Parser.
			newSystem.setFileType(FileType.XYZ);
			systemFilter = new XYZFilter(newSystem);
		} else if (intFileFilter.acceptDeep(file)) {
			// Use the Tinker Internal Coordinate File Parser.
			newSystem.setFileType(FileType.INT);
			systemFilter = new INTFilter(newSystem);
		} else {
			// Use the PDB File Parser.
			newSystem.setFileType(FileType.PDB);
			systemFilter = new PDBFilter(newSystem);
		}
		// Open the keyword file for this coordinate file, if one exists
		if (openKey(newSystem, true)) {
			// Try to parse the force field specified in the key file.
			Keyword parameters = newSystem.getKeyword("PARAMETERS");
			if (parameters != null) {
				String parmname = parameters.getEntry(0);
				File parameterFile = null;
				if (parmname != null && !parmname.equalsIgnoreCase("NONE")) {
					// Remove quotes
					parmname = parmname.replaceAll("\"", "");
					if (!parmname.endsWith(".prm")) {
						parmname = parmname + ".prm";
					}
					parameterFile = new File(parmname);
					if (!parameterFile.exists()) {
						File keyFile = newSystem.getKeyFile();
						if (keyFile != null) {
							// See if the path is relative to the key file
							// location.
							parameterFile = new File(keyFile.getParent()
									+ File.separator + parmname);
						}
					}
				}
				forceFieldFilter = new ForceFieldFilter(parameterFile,
						newSystem.getKeyFile());
				ForceField forceField = forceFieldFilter.parse();
				newSystem.setForceField(forceField);
				systemFilter.setForceField(forceField);
			}
			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			FileOpener openFile = new FileOpener(systemFilter, this);
			openThread = new Thread(openFile);
			openThread.start();
			setPanel(GRAPHICS);
		}
	}

	public void open(String name) {
		if (name == null) {
			return;
		}
		// Check for an absolute pathname
		File f = new File(name);
		if (!f.exists()) {
			// Check for a file in the CWD
			f = new File(cwd + File.separator + name);
			if (!f.exists()) {
				logger.warning(name + ": could not be found.");
				return;
			}
		}
		open(f, null);
	}

	private void openInduced() {
		FFESystem active = hierarchy.getActive();
		JFileChooser fileChooser = getFileChooser();
		fileChooser.setCurrentDirectory(cwd);
		fileChooser.setSelectedFile(active.getFile());
		fileChooser.setDialogTitle("Choose Induced Dipole File");
		fileChooser.addChoosableFileFilter(indFileFilter);
		fileChooser.setAcceptAllFileFilterUsed(true);
		fileChooser.setFileFilter(indFileFilter);
		int result = fileChooser.showOpenDialog(this);
		if (result == JFileChooser.APPROVE_OPTION) {
			File f = fileChooser.getSelectedFile();
			InducedFilter indFilter = new InducedFilter(active, f);
			indFilter.read();
		}
	}

	/*
	 * Attempt to open a Tinker KEY file
	 * 
	 * @param newSystem
	 *            FFESystem that needs an associated Key File
	 * @param createKey
	 *            flag to create a key file be created
	 * @return Key file that was found, or null if nothing could be found
	 */
	public boolean openKey(FFESystem newSystem, boolean createKey) {
		String keyFileName = null;
		String temp = newSystem.getFile().getName();
		int dot = temp.lastIndexOf(".");
		if (dot > 0) {
			keyFileName = temp.substring(0, dot) + ".key";
		} else {
			keyFileName = temp + ".key";
		}
		String path = newSystem.getFile().getParent() + File.separator;
		File keyfile = new File(path + keyFileName);
		// System.out.println("" + keyfile);
		if (keyfile.exists()) {
			Hashtable<String, Keyword> keywordHash = KeyFilter.open(keyfile);
			if (keywordHash != null) {
				newSystem.setKeywords(keywordHash);
			} else {
				return false;
			}
			newSystem.setKeyFile(keyfile);
			newSystem.setForceField(null);
			return true;
		}
		keyfile = new File(path + "tinker.key");
		if (keyfile.exists()) {
			logger.info("Using tinker.key: " + keyfile);
			Hashtable<String, Keyword> keywordHash = KeyFilter.open(keyfile);
			if (keywordHash != null) {
				newSystem.setKeywords(keywordHash);
			} else {
				return false;
			}
			newSystem.setKeyFile(keyfile);
			newSystem.setForceField(null);
			return true;
		}
		if (createKey) {
			return createKeyFile(newSystem);
		}
		return false;
	}

	public void openOn(File f, FFESystem oldSystem, String command) {
		if (oldSystem.getFileType() == FileType.XYZ) {
			XYZFilter.readOnto(f, oldSystem);
		} else {
			open(f, command);
			return;
		}
		oldSystem.setCommandDescription(command);
		graphicsCanvas.updateScene(oldSystem, true, false, null, false, null);
		getHierarchy().updateStatus();
		getHierarchy().repaint();
	}

	private void openRestart() {
		FFESystem active = hierarchy.getActive();
		JFileChooser fileChooser = getFileChooser();
		fileChooser.setCurrentDirectory(cwd);
		fileChooser.setSelectedFile(hierarchy.getActive().getFile());
		fileChooser.setDialogTitle("Choose Restart File");
		fileChooser.addChoosableFileFilter(dynFileFilter);
		fileChooser.setAcceptAllFileFilterUsed(true);
		int result = fileChooser.showOpenDialog(this);
		if (result == JFileChooser.APPROVE_OPTION) {
			File f = fileChooser.getSelectedFile();
			DYNFilter dynFilter = new DYNFilter(active, f);
			dynFilter.read();
		}
	}

	public void oscillate(ActionEvent evt) {
		oscillate = ((JCheckBoxMenuItem) evt.getSource()).isSelected();
		FFESystem[] systems = getHierarchy().getSystems();
		for (int i = 0; i < systems.length; i++) {
			Trajectory trajectory = systems[i].getTrajectory();
			if (trajectory != null) {
				trajectory.setOscillate(oscillate);
			}
		}
	}

	public void platformLookAndFeel() {
		try {
			if (SystemUtils.IS_OS_LINUX) {
				UIManager.setLookAndFeel("com.sun.java.swing.plaf.motif.MotifLookAndFeel");
			} else {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			}
			SwingUtilities.updateComponentTreeUI(SwingUtilities.getRoot(this));
		} catch (Exception e) {
			logger.warning("Can't Set Look and Feel: " + e);
		}
	}

	public void play() {
		Trajectory trajectory = getTrajectory();
		if (trajectory == null) {
			return;
		}
		trajectory.start();
	}

	/*
	 * Close the connection to a running simulation
	 */
	private void release() {
		if (simulation != null) {
			simulation.release();
			simulation = null;
			menuMenu.setConnect(true);
		}
	}

	public void reset() {
		Trajectory trajectory = getTrajectory();
		if (trajectory == null) {
			return;
		}
		trajectory.stop();
		trajectory.rewind();
	}

	public void resetPanes() {
		resizePanes(0);
	}

	public void resetShell() {
		modelingShell = getModelingShell();
		modelingShell.savePrefs();
		modelingShell = new ModelingShell(this);
	}

	/*
	 * Set the split panes to their default proportions
	 */
	public void resizePanes(int move) {
		if (move == 0) {
			splitPaneDivider = 0;
			menuMenu.setMenuShowing(false);
			menuMenu.menuClick();
			menuMenu.setSystemShowing(false);
			menuMenu.systemClick();
		} else {
			splitPane.setDividerLocation(splitPane.getDividerLocation() + move);
		}
	}

	/*
	 * Save the currently selected System to disk
	 * 
	 * @param file
	 *            File to save the system to
	 */
	public void save(File file) {
		FFESystem system = hierarchy.getActive();
		if (system != null && !system.isClosing()) {
			SystemFilter filter;
			if (system.getFileType() == FileType.XYZ) {
				filter = new XYZFilter();
			} else {
				return;
			}
			File savefile = null;
			if (file != null) {
				savefile = file;
			} else {
				JFileChooser fileChooser = getFileChooser();
				fileChooser.setCurrentDirectory(cwd);
				fileChooser.setAcceptAllFileFilterUsed(true);
				int result = fileChooser.showSaveDialog(this);
				if (result == JFileChooser.APPROVE_OPTION) {
					savefile = fileChooser.getSelectedFile();
					cwd = savefile.getParentFile();
				}
			}
			if (savefile != null) {
				filter.setMolecularSystem(system);
				if (filter.writeFile()) {
					system.setFile(savefile);
					system.setName(savefile.getName());
					// Refresh Panels with the new System name
					hierarchy.setActive(system);
				}
			}
		}
	}

	/*
	 * Save preferences to the user node
	 */
	private void savePrefs() {
		Preferences prefs = Preferences.userNodeForPackage(MainPanel.class);
		JFrame frame = (JFrame) SwingUtilities.getRoot(this);
		prefs.putInt("MainPanel_x", frame.getLocation().x);
		prefs.putInt("MainPanel_y", frame.getLocation().y);
		prefs.putInt("MainPanel_width", frame.getWidth());
		prefs.putInt("MainPanel_height", frame.getHeight());
		prefs.putBoolean("MainPanel_system", menuMenu.isSystemShowing());
		prefs.putInt("MainPanel_divider", splitPane.getDividerLocation());
		prefs.putBoolean("MainPanel_menu", menuMenu.isMenuShowing());
		prefs.putBoolean("MainPanel_axis", menuMenu.isAxisShowing());
		if (ip == null) {
			ip = new String("");
		}
		if (address != null) {
			String s = address.getHostAddress();
			if (s != null) {
				prefs.put("MainPanel_ip", s);
			}
			prefs.putInt("MainPanel_port", socketAddress.getPort());
		}
		prefs.put("MainPanel_cwd", cwd.toString());
		modelingPanel.savePrefs();
		keywordPanel.savePrefs();
		modelingShell.savePrefs();
		if (graphicsCanvas != null) {
			graphicsCanvas.savePrefs();
		}
	}

	public void selectAll() {
		if (dataRoot.getChildCount() == 0) {
			return;
		}
		hierarchy.selectAll();
	}

	public void setCWD(File file) {
		if ((file == null) || (!file.exists())) {
			return;
		}
		cwd = file;
	}

	public void setPanel(int panel) {
		tabbedPane.setSelectedIndex(panel);
	}

	public void setPort() {
		String s = new String("" + port);
		s = JOptionPane.showInputDialog("Enter a port number", s);
		if (s == null) {
			return;
		}
		int temp;
		try {
			temp = Integer.parseInt(s);
		} catch (NumberFormatException e) {
			return;
		}
		port = temp;
		socketAddress = new InetSocketAddress(address, port);
	}

	public void setRemoteJobAddress() {
		if (address == null) {
			try {
				address = InetAddress.getLocalHost();
			} catch (Exception e) {
				try {
					address = InetAddress.getByName(null);
				} catch (Exception ex) {
					return;
				}
			}
		}
		String s = new String("" + address.getHostAddress());
		s = JOptionPane.showInputDialog(
				"Enter an IP Address (XXX.XXX.XXX.XXX)", s);
		if (s == null) {
			return;
		}
		InetAddress newAddress;
		InetSocketAddress newSocketAddress;
		try {
			newAddress = InetAddress.getByName(s);
			newSocketAddress = new InetSocketAddress(newAddress, port);
		} catch (NumberFormatException e) {
			return;
		} catch (Exception e) {
			return;
		}
		address = newAddress;
		socketAddress = newSocketAddress;
	}

	public void showGlobalAxes(ActionEvent evt) {
		JCheckBoxMenuItem showAxesCheckBox = (JCheckBoxMenuItem) evt
				.getSource();
		graphicsCanvas.setAxisShowing(showAxesCheckBox.isSelected());
	}

	public void showToolBar(ActionEvent evt) {
		JCheckBoxMenuItem toolBarCheckBox = (JCheckBoxMenuItem) evt.getSource();
		if (toolBarCheckBox.isSelected()) {
			add(menuMenu.getToolBar(), BorderLayout.NORTH);
			frame.validate();
		} else {
			remove(menuMenu.getToolBar());
			frame.validate();
		}
	}

	public void showTree(ActionEvent evt) {
		JCheckBoxMenuItem showTreeCheckBox = (JCheckBoxMenuItem) evt
				.getSource();
		if (showTreeCheckBox.isSelected()) {
			if (splitPaneDivider < frame.getWidth() * (1.0f / 4.0f)) {
				splitPaneDivider = (int) (frame.getWidth() * (1.0f / 4.0f));
			}
			splitPane.setDividerLocation(splitPaneDivider);
		} else {
			splitPaneDivider = splitPane.getDividerLocation();
			splitPane.setDividerLocation(0.0);
		}
	}

	public void skip() {
		Trajectory trajectory = getTrajectory();
		if (trajectory == null) {
			return;
		}
		String skip = new String("" + (int) trajectory.getSkip());
		skip = JOptionPane.showInputDialog(
				"Enter the Number of Frames to Skip", skip);
		try {
			int f = Integer.parseInt(skip);
			trajectory.setSkip(f);
		} catch (NumberFormatException e) {
			return;
		}
	}

	public void speed() {
		Trajectory trajectory = getTrajectory();
		if (trajectory == null) {
			return;
		}
		String frame = new String("" + (int) trajectory.getRate());
		frame = JOptionPane.showInputDialog("Enter the Frame Rate (1-100)",
				frame);
		try {
			int f = Integer.parseInt(frame);
			trajectory.setRate(f);
		} catch (NumberFormatException e) {
			return;
		}
	}

	public void stateChanged(ChangeEvent evt) {
		JTabbedPane jtp = (JTabbedPane) evt.getSource();
		int index = jtp.getSelectedIndex();
		if (index == 0) {
			graphicsCanvas.selected();
		} else if (index == 1) {
			keywordPanel.selected();
		} else if (index == 2) {
			modelingPanel.selected();
		} else if (index == 3) {
			logPanel.selected();
		}
	}

	public void stepBack() {
		Trajectory trajectory = getTrajectory();
		if (trajectory == null) {
			return;
		}
		trajectory.stop();
		trajectory.back();
	}

	public void stepForward() {
		Trajectory trajectory = getTrajectory();
		if (trajectory == null) {
			return;
		}
		trajectory.stop();
		trajectory.forward();
	}

	public void stop() {
		Trajectory trajectory = getTrajectory();
		if (trajectory == null) {
			return;
		}
		trajectory.stop();
	}

	public String toString() {
		return "Program Control";
	}

	public void openFromPDB() {
		if (openThread != null && openThread.isAlive()) {
			return;
		}
		String code = JOptionPane.showInputDialog(
				"Enter the 4-Character PDB ID:", "");
		code = code.trim();
		if (code == null || code.length() != 4) {
			return;
		}

		System.out.println("Request: "+code);
		System.out.println("Database: PDB");
		String pdbAddress = "http://www.rcsb.org/pdb/download/downloadFile.do?fileFormat=pdb&compression=NO&structureId="
				+ code;
		logger.log(Level.INFO, pdbAddress);

		try {
			// Get PDB File from the RCSB Protein Data Bank

			String path = MainPanel.class.getProtectionDomain().getCodeSource().getLocation().getPath();
			String decodedPath = URLDecoder.decode(path, "UTF-8");
			decodedPath = decodedPath.replaceAll("ffe/lib/.*jar", "");
			String PDBDir = decodedPath + "PDBdownloads/";
			String fileName = code + ".pdb";
			File pdbFile = new File(PDBDir + fileName);

			File directory=new File(PDBDir);
			if (!directory.exists()) {
				directory.mkdir();
			}

			FFESystem newSystem = new FFESystem(code, null, pdbFile, false);
			PDBFilter pdbFilter = new PDBFilter(newSystem, pdbAddress);
			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			FileOpener openFile = new FileOpener(pdbFilter, this);
			openThread = new Thread(openFile);
			openThread.start();
			setPanel(GRAPHICS);
		} catch (Exception e) {
			return;
		}
	}

	public void openFromPubChem() {
		String databaseName = "PubChem";
		JTextField moleculeName = new JTextField();
		String result = JOptionPane.showInputDialog(
				null, "Enter the Molecule Name:", "", 1);
		System.out.println("Request: "+result);
		if (result!=null) {
			xyzFromURL(result, databaseName);
		}
	}

	public void openFromNCI() {
		String databaseName = "NCI";
		JTextField moleculeName = new JTextField();
		String result = JOptionPane.showInputDialog(
				null, "Enter the Molecule Name:", "", 1);
		System.out.println("Request: "+result);
		if (result!=null) {
			xyzFromURL(result, databaseName);
		}
	}

	public void xyzFromURL(String moleculeName, String databaseName) {
		int atoms;
		int bonds;
		float x[];
		float y[];
		float z[];
		String atom[];
		int bondatom[];
		int numberofbonds[];
		int atomtype[];

		try {
			// Get SDF file from NCBI PubChem or NCI Cactus Database

			String path = MainPanel.class.getProtectionDomain().getCodeSource().getLocation().getPath();
			String decodedPath = URLDecoder.decode(path, "UTF-8");
			decodedPath = decodedPath.replaceAll("ffe/lib/.*jar", "");
			String NIHDir = decodedPath + "NIHdownloads/";
			File directory=new File(NIHDir);
			if (!directory.exists()) {
				directory.mkdir();
			}

			System.out.println("Database: "+databaseName);
			// url must have a default value, use PubChem database
			URL url=new URL("https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/name/"
					+ moleculeName + "/record/SDF/?record_type=3d&response_type=display");
			// switch to NCI database if it was requested
			if (databaseName == "NCI") {
				url=new URL("https://cactus.nci.nih.gov/chemical/structure/"
					+ moleculeName + "/file?format=sdf");
			}

			BufferedReader in=new BufferedReader(new InputStreamReader(url.openStream()));
			Scanner scan=new Scanner(in);
			scan.nextLine();
			scan.nextLine();
			scan.nextLine();
			atoms=scan.nextInt();
			if (atoms>99) {
				bonds=atoms%1000;
				atoms=atoms/1000;
			} else {
				bonds=scan.nextInt();
			}
			scan.nextLine();
			x=new float[atoms];
			y=new float[atoms];
			z=new float[atoms];
			atom=new String[atoms];
			bondatom=new int[bonds*2];
			int linecount=0;
			while (linecount<atoms) {
				x[linecount]=scan.nextFloat();
				y[linecount]=scan.nextFloat();
				z[linecount]=scan.nextFloat();
				atom[linecount]=scan.next();
				scan.nextLine();
				linecount++;
			}
            
			linecount=0;
			while (linecount<bonds*2) {
				bondatom[linecount]=Integer.parseInt(scan.next());
				if (bondatom[linecount]>99) {
					bondatom[linecount+1]=bondatom[linecount]%1000;
					bondatom[linecount]=bondatom[linecount]/1000;
					linecount=linecount+2;
					scan.nextLine();
				} else {
					linecount++;
					bondatom[linecount]=Integer.parseInt(scan.next());
					scan.nextLine();
					linecount++;
				}
			}
			in.close();
			scan.close();
          
			File keyfile=new File(NIHDir+moleculeName+".key");
			FileWriter keyfilewrite=new FileWriter(keyfile);
			BufferedWriter keyoutput=new BufferedWriter(keyfilewrite);
			String PRMDir = NIHDir.replaceAll("NIHdownloads/", "") + "tinker/params/";
			// Modify Keyfile Directory for Windows, JWP July 2017
			PRMDir = PRMDir.replaceAll("/C:", "C:");
			keyoutput.append("\n# Force Field Selection\nPARAMETERS        "
				+ PRMDir + "tiny.prm\n");
			keyoutput.close();
            
			File file=new File(NIHDir+moleculeName+".xyz");
			FileWriter filewrite=new FileWriter(file);
			BufferedWriter output=new BufferedWriter(filewrite);
			output.append(atoms+"\t"+moleculeName+"\n");
			linecount=0;
			numberofbonds=new int[atoms];
			atomtype=new int[atoms];
			while (linecount<atoms) {
				output.append((linecount+1)+"\t");
				output.append(atom[linecount]+"\t");
				output.append(x[linecount]+"\t \t");
				output.append(y[linecount]+"\t \t");
				output.append(z[linecount]+"\t \t");
				int bondcount=0;
				while (bondcount<bonds*2) {
					if (bondatom[bondcount]==linecount+1) {
						numberofbonds[linecount]++;
					}
					bondcount++;
				}
				if (atom[linecount].contains("Cl")) {
					atomtype[linecount]=170+numberofbonds[linecount];
				} else if (atom[linecount].contains("Si")){
					atomtype[linecount]=140+numberofbonds[linecount];
				} else if (atom[linecount].contains("Br")){
					atomtype[linecount]=350+numberofbonds[linecount];
				} else if (atom[linecount].contains("C")){
					atomtype[linecount]=60+numberofbonds[linecount];
				} else if (atom[linecount].contains("H")){
					atomtype[linecount]=10+numberofbonds[linecount];
				} else if (atom[linecount].contains("O")){
					atomtype[linecount]=80+numberofbonds[linecount];
				} else if (atom[linecount].contains("N")){
					atomtype[linecount]=70+numberofbonds[linecount];
				} else if (atom[linecount].contains("S")){
					atomtype[linecount]=160+numberofbonds[linecount];
				} else if (atom[linecount].contains("P")){
					atomtype[linecount]=150+numberofbonds[linecount];
				} else if (atom[linecount].contains("B")){
					atomtype[linecount]=50+numberofbonds[linecount];
				} else if (atom[linecount].contains("I")){
					atomtype[linecount]=530+numberofbonds[linecount];
				} else if (atom[linecount].contains("F")){
					atomtype[linecount]=90+numberofbonds[linecount];
				} else {
					atomtype[linecount]=70+numberofbonds[linecount];
				}
				output.append(atomtype[linecount]+"\t");

				bondcount=0;
				while (bondcount<bonds*2) {
					if (bondatom[bondcount]==linecount+1) {
						if (bondcount%2==0) {
							output.append(bondatom[bondcount+1]+"\t");
						} else {
							output.append(bondatom[bondcount-1]+"\t");
						}
					}
					bondcount++;
				}
				output.append("\n");
				linecount++;
			}
			output.close();
			open(file, null);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
}
