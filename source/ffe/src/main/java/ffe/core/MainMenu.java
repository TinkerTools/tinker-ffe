/*
 * <p>Title: Force Field Explorer</p>
 * <p>Description: Force Field Explorer Molecular Modeling Program</p>
 * <p>Copyright: Copyright (c) 2004-2019 Jay William Ponder</p>
 * <p>Institution: Jay Ponder Lab, Washington University in Saint Louis</p>
 * @author Michael J. Schnieders
 * @version 8.7
 */

package ffe.core;

import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.Font;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;

import org.apache.commons.lang.SystemUtils;

import ffe.properties.FFELocale;

/*
 * The MainMenu class creates the Force Field Explorer Menu Bar
 */
public class MainMenu extends JMenuBar {
	/*
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static int keyMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
	
	// Controller References
	private MainPanel mainPanel;

	private GraphicsCanvas graphics;

	// Locale and ClassLoader
	private FFELocale locale;

	private ClassLoader loader;

	// Toolbar
	private JToolBar toolBar;

	private Insets insets;

	private ImageIcon blankIcon;

	// Selection Menu
	private JCheckBoxMenuItem highlightCBMI;

	private JCheckBoxMenuItem labelResiduesMI;

	private JCheckBoxMenuItem labelAtomsMI;

	// Options Menu
	private JRadioButtonMenuItem activeRBMI;

	private JRadioButtonMenuItem mouseRBMI;

	private ButtonGroup dragModeButtonGroup;

	private ButtonGroup leftMouseButtonGroup;
	private JRadioButtonMenuItem rotateRBMI;
	private JRadioButtonMenuItem translateRBMI;
	private JRadioButtonMenuItem zoomRBMI;

	// Picking Menu
	private JCheckBoxMenuItem pickingCBMI;

	private JRadioButtonMenuItem atomRBMI;

	private JRadioButtonMenuItem bondRBMI;

	private JRadioButtonMenuItem angleRBMI;

	private JRadioButtonMenuItem dihedralRBMI;

	private JRadioButtonMenuItem residueRBMI;

	private JRadioButtonMenuItem polymerRBMI;

	private JRadioButtonMenuItem moleculeRBMI;

	private JRadioButtonMenuItem systemRBMI;

	private JRadioButtonMenuItem measureDistanceRBMI;

	private JRadioButtonMenuItem measureAngleRBMI;

	private JRadioButtonMenuItem measureDihedralRBMI;

	private ButtonGroup levelBG;

	// Trajectory Menu
	private JCheckBoxMenuItem oscillateCBMI;

	// Simulation Menu
	private JMenuItem localMI;

	private JMenuItem remoteMI;

	private JMenuItem releaseMI;

	// Export Menu
	private ButtonGroup captureFormatButtonGroup;

	// Window Menu
	private JCheckBoxMenuItem systemsCBMI;

	private JCheckBoxMenuItem toolBarCBMI;

	private JCheckBoxMenuItem globalAxisCBMI;

	private JRadioButtonMenuItem javaRBMI;

	private JRadioButtonMenuItem nativeRBMI;

	private ButtonGroup skinButtonGroup;

	/*
	 * Constructor @param t Main application panel (for event listening)
	 * @param g Main graphics panel (for event Listening)
	 */
	public MainMenu(MainPanel f) {
		mainPanel = f;
		graphics = mainPanel.getGraphics3D();
		locale = mainPanel.getFFELocale();
		loader = getClass().getClassLoader();
		blankIcon = new ImageIcon(loader.getResource("ffe/icons/blank.gif"));
		// Main Menubar
		JMenu fileMenu = addMenu("File", 'F');
		JMenu selectionMenu = addMenu("Selection", 'E');
		JMenu displayMenu = addMenu("Display", 'D');
		JMenu colorMenu = addMenu("Color", 'C');
		JMenu optionsMenu = addMenu("Options", 'O');
		JMenu pickingMenu = addMenu("Picking", 'P');
		JMenu trajectoryMenu = addMenu("Trajectory", 'T');
		JMenu simulationMenu = addMenu("Simulation", 'S');
		JMenu exportMenu = addMenu("Export", 'X');
		JMenu windowMenu = addMenu("Window", 'W');

		// File Menu - All Events Handled by the MainPanel Class
		addMenuItem(fileMenu, "open", "Open", 'O', KeyEvent.VK_O, mainPanel);
		addMenuItem(fileMenu, "saveAs", "SaveAs", 'S', KeyEvent.VK_S, mainPanel);
		addMenuItem(fileMenu, "close", "Close", 'C', -1, mainPanel);
		addMenuItem(fileMenu, "BLANK", "CloseAll", 'A', -1, mainPanel);
		fileMenu.addSeparator();
		addMenuItem(fileMenu, "BLANK", "DownloadFromPubChem", 'M', -1, mainPanel);
		addMenuItem(fileMenu, "BLANK", "DownloadFromNCI", 'M', -1, mainPanel);
		addMenuItem(fileMenu, "BLANK", "DownloadFromPDB", 'M', -1, mainPanel);
		fileMenu.addSeparator();
		addMenuItem(fileMenu, "BLANK", "ChooseKeyFile", 'R', -1, mainPanel);
		addMenuItem(fileMenu, "BLANK", "ChooseLogFile", 'I', -1, mainPanel);
		addMenuItem(fileMenu, "BLANK", "LoadRestartData", 'R', -1, mainPanel);
		addMenuItem(fileMenu, "BLANK", "LoadInducedData", 'I', -1, mainPanel);
		fileMenu.addSeparator();
		addMenuItem(fileMenu, "BLANK", "ChooseTinkerLocation", 'I', -1,
				mainPanel);
		fileMenu.addSeparator();
		addMenuItem(fileMenu, "exit", "Exit", 'E', -1, mainPanel);
		// Selection Menu - Events Handled by the MainPanel
		// and GraphicsCanvas Classes
		addMenuItem(selectionMenu, "BLANK", "SelectAll", 'A', KeyEvent.VK_A,
				mainPanel);
		addMenuItem(selectionMenu, "BLANK", "RestrictToSelections", 'R', -1,
				graphics);
		addMenuItem(selectionMenu, "BLANK", "MergeSelections", 'M', -1,
				mainPanel);
		selectionMenu.addSeparator();
		highlightCBMI = addCBMenuItem(selectionMenu, "highlight",
				"HighlightSelections", 'H', KeyEvent.VK_H, mainPanel);
		addMenuItem(selectionMenu, "color", "SetSelectionColor", 'S', -1,
				graphics);
		selectionMenu.addSeparator();
		labelAtomsMI = addCBMenuItem(selectionMenu, "BLANK",
				"LabelSelectedAtoms", 'O', -1, graphics);
		labelResiduesMI = addCBMenuItem(selectionMenu, "BLANK",
				"LabelSelectedResidues", 'R', -1, graphics);
		addMenuItem(selectionMenu, "BLANK", "SetLabelFontSize", 'Z', -1,
				graphics);
		addMenuItem(selectionMenu, "color", "SetLabelFontColor", 'C', -1,
				graphics);
		highlightCBMI.setSelected(false);
		labelAtomsMI.setSelected(false);
		labelResiduesMI.setSelected(false);
		// Display Menu - All Events Handled by the Graphics Class
		addMenuItem(displayMenu, "BLANK", "Wireframe", 'W', -1, graphics);
		addMenuItem(displayMenu, "BLANK", "Tube", 'T', -1, graphics);
		addMenuItem(displayMenu, "BLANK", "Spacefill", 'S', -1, graphics);
		addMenuItem(displayMenu, "BLANK", "BallAndStick", 'B', -1, graphics);
		addMenuItem(displayMenu, "BLANK", "Invisible", 'I', -1, graphics);
		addMenuItem(displayMenu, "BLANK", "RMIN", 'R', -1, graphics);
		displayMenu.addSeparator();
		addMenuItem(displayMenu, "BLANK", "ShowHydrogens", 'H', -1, graphics);
		addMenuItem(displayMenu, "BLANK", "HideHydrogens", 'Y', -1, graphics);
		JMenu vectorTypeMenu = addSubMenu(displayMenu, "VectorType", 'T');
		addMenuItem(vectorTypeMenu, "BLANK", "InducedDipole", 'I', -1, graphics);
		addMenuItem(vectorTypeMenu, "BLANK", "Force", 'F', -1, graphics);
		addMenuItem(vectorTypeMenu, "BLANK", "Velocity", 'V', -1, graphics);
		addMenuItem(vectorTypeMenu, "BLANK", "Acceleration", 'A', -1, graphics);
		addMenuItem(vectorTypeMenu, "BLANK", "HideVectors", 'I', -1, graphics);
		JMenu vectorLengthMenu = addSubMenu(displayMenu, "VectorLength", 'L');
		addMenuItem(vectorLengthMenu, "BLANK", "Unit", 'U', -1, graphics);
		addMenuItem(vectorLengthMenu, "BLANK", "Relative", 'R', -1, graphics);
		addMenuItem(vectorLengthMenu, "BLANK", "Absolute", 'A', -1, graphics);
		displayMenu.addSeparator();
		addMenuItem(displayMenu, "BLANK", "Fill", 'F', -1, graphics);
		addMenuItem(displayMenu, "BLANK", "Points", 'P', -1, graphics);
		addMenuItem(displayMenu, "BLANK", "Lines", 'I', -1, graphics);
		displayMenu.addSeparator();
		addMenuItem(displayMenu, "settings", "Preferences", 'P', -1, graphics);
		// Color Menu - All Events Handled by Graphics Class
		addMenuItem(colorMenu, "BLANK", "Monochrome", 'M', -1, graphics);
		addMenuItem(colorMenu, "BLANK", "CPK", 'C', -1, graphics);
		addMenuItem(colorMenu, "BLANK", "Residue", 'R', -1, graphics);
		addMenuItem(colorMenu, "BLANK", "Structure", 'S', -1, graphics);
		addMenuItem(colorMenu, "BLANK", "Polymer", 'M', -1, graphics);
		addMenuItem(colorMenu, "BLANK", "PartialCharge", 'P', -1, graphics);
		addMenuItem(colorMenu, "BLANK", "VectorMagnitude", 'V', -1, graphics);
		addMenuItem(colorMenu, "BLANK", "UserColor", 'U', -1, graphics);
		colorMenu.addSeparator();
		addMenuItem(colorMenu, "BLANK", "ApplyUserColor", 'A', -1, graphics);
		addMenuItem(colorMenu, "BLANK", "SetUserColor", 'C', -1, graphics);
		// All Options Menu ActionEvents are handled by the GraphicsCanvas
		// Class.
		dragModeButtonGroup = new ButtonGroup();
		activeRBMI = addBGMI(dragModeButtonGroup, optionsMenu, "active",
				"ActiveSystem", 'A', KeyEvent.VK_A, graphics);
		mouseRBMI = addBGMI(dragModeButtonGroup, optionsMenu, "mouse",
				"SystemBelowMouse", 'S', KeyEvent.VK_M, graphics);
		activeRBMI.setSelected(true);
		optionsMenu.addSeparator();
		JMenu leftMouseMenu = addSubMenu(optionsMenu, "LeftMouseButton", 'M');
		leftMouseButtonGroup = new ButtonGroup();
		rotateRBMI = addBGMI(leftMouseButtonGroup, leftMouseMenu, "BLANK",
				"Rotate", 'R', KeyEvent.VK_R, graphics);
		translateRBMI = addBGMI(leftMouseButtonGroup, leftMouseMenu, "BLANK",
				"Translate", 'T', KeyEvent.VK_T, graphics);
		zoomRBMI = addBGMI(leftMouseButtonGroup, leftMouseMenu, "BLANK",
				"Zoom", 'Z', KeyEvent.VK_Z, graphics);
		rotateRBMI.setSelected(true);
		optionsMenu.addSeparator();
		addMenuItem(optionsMenu, "BLANK", "ResetRotation", 'R', -1, graphics);
		addMenuItem(optionsMenu, "BLANK", "ResetTranslation", 'T', -1, graphics);
		addMenuItem(optionsMenu, "BLANK", "ResetRotationAndTranslation", 'E',
				-1, graphics);
		addMenuItem(optionsMenu, "BLANK", "RotateAboutCenter", 'C',
				KeyEvent.VK_C, graphics);
		addMenuItem(optionsMenu, "BLANK", "RotateAboutPick", 'P',
				KeyEvent.VK_P, graphics);
		optionsMenu.addSeparator();
		addMenuItem(optionsMenu, "BLANK", "ResetGlobalRotation", 'N', -1,
				graphics);
		addMenuItem(optionsMenu, "BLANK", "ResetGlobalTranslation", 'O', -1,
				graphics);
		addMenuItem(optionsMenu, "BLANK", "ResetGlobalZoom", 'Z', -1, graphics);
		addMenuItem(optionsMenu, "globalReset", "ResetGlobalView", 'V', -1,
				graphics);
		optionsMenu.addSeparator();
		addMenuItem(optionsMenu, "fullScreen", "FullScreen", 'F',
				KeyEvent.VK_F, graphics);
		addMenuItem(optionsMenu, "background", "SetBackgroundColor", 'S', -1,
				graphics);
		// All Picking Menu ActionEvents are handled by the GraphicsCanvas
		// Class.
		levelBG = new ButtonGroup();
		pickingCBMI = addCBMenuItem(pickingMenu, "picking", "GraphicsPicking",
				'G', KeyEvent.VK_0, graphics);
		pickingCBMI.setSelected(false);
		pickingMenu.addSeparator();
		atomRBMI = addBGMI(levelBG, pickingMenu, "BLANK", "PickAtom", 'A',
				KeyEvent.VK_1, graphics);
		bondRBMI = addBGMI(levelBG, pickingMenu, "BLANK", "PickBond", 'B',
				KeyEvent.VK_2, graphics);
		angleRBMI = addBGMI(levelBG, pickingMenu, "BLANK", "PickAngle", 'N',
				KeyEvent.VK_3, graphics);
		dihedralRBMI = addBGMI(levelBG, pickingMenu, "BLANK", "PickDihedral",
				'D', KeyEvent.VK_4, graphics);
		residueRBMI = addBGMI(levelBG, pickingMenu, "BLANK", "PickResidue",
				'R', KeyEvent.VK_5, graphics);
		polymerRBMI = addBGMI(levelBG, pickingMenu, "BLANK", "PickPolymer",
				'P', KeyEvent.VK_6, graphics);
		moleculeRBMI = addBGMI(levelBG, pickingMenu, "BLANK", "PickMolecule",
				'M', KeyEvent.VK_7, graphics);
		systemRBMI = addBGMI(levelBG, pickingMenu, "BLANK", "PickSystem", 'S',
				KeyEvent.VK_8, graphics);
		pickingMenu.addSeparator();
		measureDistanceRBMI = addBGMI(levelBG, pickingMenu, "BLANK",
				"MeasureDistance", 'I', -1, graphics);
		measureAngleRBMI = addBGMI(levelBG, pickingMenu, "BLANK",
				"MeasureAngle", 'L', -1, graphics);
		measureDihedralRBMI = addBGMI(levelBG, pickingMenu, "BLANK",
				"MeasureDihedral", 'H', -1, graphics);
		atomRBMI.setSelected(true);
		pickingMenu.addSeparator();
		addMenuItem(pickingMenu, "color", "SetGraphicsPickingColor", 'S', -1,
				graphics);
		// All Trajectory Menu ActionEvents are handled by the MainPanel class.
		oscillateCBMI = addCBMenuItem(trajectoryMenu, "oscillate", "Oscillate",
				'O', -1, mainPanel);
		oscillateCBMI.setSelected(false);
		addMenuItem(trajectoryMenu, "BLANK", "Frame", 'A', -1, mainPanel);
		addMenuItem(trajectoryMenu, "BLANK", "Speed", 'E', -1, mainPanel);
		addMenuItem(trajectoryMenu, "BLANK", "Skip", 'K', -1, mainPanel);
		trajectoryMenu.addSeparator();
		addMenuItem(trajectoryMenu, "play", "Play", 'P', -1, mainPanel);
		addMenuItem(trajectoryMenu, "stop", "Stop", 'S', -1, mainPanel);
		addMenuItem(trajectoryMenu, "forward", "StepForward", 'F', -1,
				mainPanel);
		addMenuItem(trajectoryMenu, "back", "StepBack", 'B', -1, mainPanel);
		addMenuItem(trajectoryMenu, "start", "Reset", 'R', -1, mainPanel);
		// All Simulation Menu ActionEvents are handled by the MainPanel class.
		releaseMI = addMenuItem(simulationMenu, "release", "ReleaseJob", 'E',
				-1, mainPanel);
		localMI = addMenuItem(simulationMenu, "local", "ConnectToLocalJob",
				'L', KeyEvent.VK_L, mainPanel);
		simulationMenu.addSeparator();
		remoteMI = addMenuItem(simulationMenu, "network", "ConnectToRemoteJob",
				'R', -1, mainPanel);
		addMenuItem(simulationMenu, "BLANK", "SetRemoteJobAddress", 'A', -1,
				mainPanel);
		simulationMenu.addSeparator();
		addMenuItem(simulationMenu, "BLANK", "SetPort", 'P', -1, mainPanel);
		// All Export Menu ActionEvents are handled by the GraphicsCanvas class.
		addMenuItem(exportMenu, "capture", "CaptureGraphics", 'C',
				KeyEvent.VK_G, graphics);
		exportMenu.addSeparator();
		captureFormatButtonGroup = new ButtonGroup();
		addBGMI(captureFormatButtonGroup, exportMenu, "BLANK", "PNG", 'P', -1,
				graphics).setSelected(true);
		addBGMI(captureFormatButtonGroup, exportMenu, "BLANK", "JPEG", 'J', -1,
				graphics);
		addBGMI(captureFormatButtonGroup, exportMenu, "BLANK", "BMP", 'B', -1,
				graphics);
		addBGMI(captureFormatButtonGroup, exportMenu, "BLANK", "WBMP", 'W', -1,
				graphics);
		addBGMI(captureFormatButtonGroup, exportMenu, "BLANK", "GIF", 'G', -1,
				graphics);
		// All Window Options ActionEvents are handled by the GraphicsCanvas
		// class.
		addMenuItem(windowMenu, "resetPanes", "ResetPanes", 'R', -1, mainPanel);
		windowMenu.addSeparator();
		systemsCBMI = addCBMenuItem(windowMenu, "active", "ShowTree", 'T', -1,
				mainPanel);
		toolBarCBMI = addCBMenuItem(windowMenu, "showToolBar", "ShowToolBar",
				'B', -1, mainPanel);
		globalAxisCBMI = addCBMenuItem(windowMenu, "BLANK", "ShowGlobalAxes",
				'C', -1, mainPanel);
		addMenuItem(windowMenu, "refresh", "ResetConsole", 'L', -1, mainPanel);
		globalAxisCBMI.setSelected(true);
		windowMenu.addSeparator();
		skinButtonGroup = new ButtonGroup();
		javaRBMI = addBGMI(skinButtonGroup, windowMenu, "java",
				"OceanLookAndFeel", 'J', -1, mainPanel);

			if (SystemUtils.IS_OS_LINUX) {
				/*
				javaRBMI = addBGMI(skinButtonGroup, windowMenu, "java",
						"OceanLookAndFeel", 'J', -1, mainPanel);
				nativeRBMI = addBGMI(skinButtonGroup, windowMenu, "tux",
						"MotifLookAndFeel", 'M', -1, mainPanel);
				javaRBMI.setSelected(true); */
			} else if (SystemUtils.IS_OS_MAC) {
				/*
				nativeRBMI = addBGMI(skinButtonGroup, windowMenu, "mac",
						"MacOSXLookAndFeel", 'W', -1, mainPanel);
				nativeRBMI.setSelected(true); */
			} else if (SystemUtils.IS_OS_WINDOWS) {
				javaRBMI = addBGMI(skinButtonGroup, windowMenu, "java",
						"OceanLookAndFeel", 'J', -1, mainPanel);
				nativeRBMI = addBGMI(skinButtonGroup, windowMenu, "win",
						"WindowsLookAndFeel", 'W', -1, mainPanel);
				javaRBMI.setSelected(true);
			} 
		
		// Create the Tool Bar
		toolBar = new JToolBar("Toolbar", JToolBar.HORIZONTAL);
		toolBar.setBorderPainted(false);
		toolBar.setRollover(true);
		// File ToolBar
		JButton temp = new JButton();
		insets = temp.getInsets();
		insets.set(0, 0, 0, 0);
		addButton("open2", mainPanel, "Open", "Open");
		addButton("saveAs2", mainPanel, "SaveAs", "Save As");
		addButton("close2", mainPanel, "Close", "Close");
		toolBar.addSeparator();
		// Selection ToolBar
		addButton("highlight2", mainPanel, "HighlightSelections", "Highlight");
		toolBar.addSeparator();
		// Graphics ToolBar
		//addButton("settings2", mainPanel, "Preferences", "Prefs");
		//toolBar.addSeparator();
		// Options ToolBar
		addButton("globalReset2", graphics, "ResetGlobalView", "Reset");
		addButton("zoomOut2", graphics, "ZoomOut", "Out");
		addButton("zoomIn2", graphics, "ZoomIn", "In");
		toolBar.addSeparator();
		// Picking ToolBar
		addButton("picking2", graphics, "GraphicsPicking", "Pick");
		toolBar.addSeparator();
		// Trajectory ToolBar
		addButton("forward2", mainPanel, "StepForward", "Forward");
		addButton("play2", mainPanel, "Play", "Play");
		addButton("stop2", mainPanel, "Stop", "Stop");
		addButton("back2", mainPanel, "StepBack", "Back");
		addButton("start2", mainPanel, "Reset", "Reset");
		toolBar.addSeparator();
		// Simulation ToolBar
		addButton("local2", mainPanel, "ConnectToLocalJob", "Local");
		addButton("network2", mainPanel, "ConnectToRemoteJob", "Remote");
		addButton("release2", mainPanel, "ReleaseJob", "Release");
		toolBar.addSeparator();
		// Export ToolBar
		addButton("capture2", graphics, "CaptureGraphics", "Capture");
		//toolBar.addSeparator();
		// Window ToolBar
		addButton("left2", mainPanel, "ExpandGraphicsWindow", "Left");
		addButton("right2", mainPanel, "ShrinkGraphicsWindow", "Right");
		//addButton("resetPanes2", mainPanel, "ResetPanes", "Restore");
	}

	private JRadioButtonMenuItem addBGMI(ButtonGroup buttonGroup, JMenu menu,
			String icon, String actionCommand, char mnemonic, int accelerator,
			ActionListener actionListener) {
		ImageIcon imageIcon = null;
		if (icon != "BLANK") {
			imageIcon = new ImageIcon(loader.getResource("ffe/icons/"
					+ icon + ".png"));
		} else {
			imageIcon = blankIcon;
		}
		JRadioButtonMenuItem menuItem = new JRadioButtonMenuItem(locale
				.getValue(actionCommand), imageIcon);
		menuItem.setActionCommand(actionCommand);
		buttonGroup.add(menuItem);
		menu.add(menuItem);
		setMenuOptions(menuItem, mnemonic, accelerator, actionListener);
		return menuItem;
	}

	private void addButton(String icon, ActionListener al, String actionCommand) {
		ImageIcon imageIcon = null;
		if (icon != "BLANK") {
			imageIcon = new ImageIcon(loader.getResource("ffe/icons/"
					+ icon + ".png"));
		} else {
			imageIcon = blankIcon;
		}
		JButton button = new JButton(imageIcon);
		toolBar.add(button);
		button.addActionListener(al);
		button.setFont(new Font("Arial", Font.PLAIN, 8));
		button.setVerticalTextPosition(SwingConstants.BOTTOM);
	    	button.setHorizontalTextPosition(SwingConstants.CENTER);
		button.setBorderPainted(false);
		button.setOpaque(false);
		//button.setMargin(new Insets(0,0,0,0));
		button.setActionCommand(actionCommand);
		button.setToolTipText(locale.getValue(actionCommand));
	}

	private void addButton(String icon, ActionListener al, String actionCommand, String helpText) {
		ImageIcon imageIcon = null;
		if (icon != "BLANK") {
			imageIcon = new ImageIcon(loader.getResource("ffe/icons/"
					+ icon + ".png"));
		} else {
			imageIcon = blankIcon;
		}
		JButton button = new JButton(imageIcon);
		toolBar.add(button);
		button.addActionListener(al);
		button.setText(helpText);
		button.setFont(new Font("Arial", Font.PLAIN, 8));
		button.setVerticalTextPosition(SwingConstants.BOTTOM);
	    	button.setHorizontalTextPosition(SwingConstants.CENTER);
		button.setBorderPainted(false);
		button.setOpaque(false);
		//button.setMargin(new Insets(0,0,0,0));
		button.setActionCommand(actionCommand);
		button.setToolTipText(locale.getValue(actionCommand));
	}

	private JCheckBoxMenuItem addCBMenuItem(JMenu menu, String icon,
			String actionCommand, char mnemonic, int accelerator,
			ActionListener actionListener) {
		ImageIcon imageIcon = null;
		if (icon != "BLANK") {
			imageIcon = new ImageIcon(loader.getResource("ffe/icons/"
					+ icon + ".png"));
		} else {
			imageIcon = blankIcon;
		}
		JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem(locale
				.getValue(actionCommand), imageIcon);
		menuItem.setActionCommand(actionCommand);
		menu.add(menuItem);
		setMenuOptions(menuItem, mnemonic, accelerator, actionListener);
		return menuItem;
	}

	private JMenu addMenu(String name, char mnemonic) {
		JMenu menu = new JMenu(locale.getValue(name));
		add(menu);
		if (mnemonic != '.') {
			menu.setMnemonic(mnemonic);
		}
		return menu;
	}

	private JMenuItem addMenuItem(JMenu menu, String icon,
			String actionCommand, char mnemonic, int accelerator,
			ActionListener actionListener) {
		ImageIcon imageIcon = null;
		if (icon != "BLANK") {
			imageIcon = new ImageIcon(loader.getResource("ffe/icons/"
					+ icon + ".png"));
		} else {
			imageIcon = blankIcon;
		}
		JMenuItem menuItem = new JMenuItem(locale.getValue(actionCommand),
				imageIcon);
		menuItem.setActionCommand(actionCommand);
		menu.add(menuItem);
		setMenuOptions(menuItem, mnemonic, accelerator, actionListener);
		return menuItem;
	}

	private JMenu addSubMenu(JMenu parent, String name, char mnemonic) {
		JMenu menu = new JMenu(locale.getValue(name));
		parent.add(menu);
		if (mnemonic != '.') {
			menu.setMnemonic(mnemonic);
		}
		menu.setIcon(blankIcon);
		return menu;
	}

	public boolean getHighlighting() {
		return highlightCBMI.isSelected();
	}

	public GraphicsCanvas.MouseMode getMouseMode() {
		if (activeRBMI.isSelected()) {
			return GraphicsCanvas.MouseMode.ACTIVESYSTEM;
		}
		return GraphicsCanvas.MouseMode.SYSTEMBELOWMOUSE;
	}

	public boolean getPicking() {
		return pickingCBMI.isSelected();
	}

	/*
	 * Get a reference the tool bar
	 * 
	 * @return Force Field Explorer ToolBar
	 */
	public JToolBar getToolBar() {
		return toolBar;
	}

	public boolean isAxisShowing() {
		return globalAxisCBMI.isSelected();
	}

	public boolean isMenuShowing() {
		return toolBarCBMI.isSelected();
	}

	public boolean isPickingActive() {
		return pickingCBMI.isSelected();
	}

	public boolean isSystemShowing() {
		return systemsCBMI.isSelected();
	}

	public void menuClick() {
		toolBarCBMI.doClick();
	}

	public void setAtomLabels(boolean b) {
		labelAtomsMI.setSelected(b);
	}

	public void setAxisShowing(boolean b) {
		globalAxisCBMI.setSelected(b);
	}

	/*
	 * Toggle connection status
	 */
	public void setConnect(boolean b) {
		localMI.setEnabled(b);
		remoteMI.setEnabled(b);
		releaseMI.setEnabled(!b);
	}

	public void setHighlighting(boolean h) {
		highlightCBMI.setSelected(h);
	}

	private void setMenuOptions(JMenuItem menuItem, char mnemonic,
			int accelerator, ActionListener actionListener) {
		if (mnemonic != '.' && !SystemUtils.IS_OS_MAC) {
			menuItem.setMnemonic(mnemonic);
		}
		if (accelerator != -1) {
			menuItem.setAccelerator(KeyStroke
					.getKeyStroke(accelerator, keyMask));
		}
		if (actionListener != null) {
			menuItem.addActionListener(actionListener);
		}
	}

	public void setMenuShowing(boolean b) {
		toolBarCBMI.setSelected(b);
	}

	public void setMouseMode(GraphicsCanvas.MouseMode m) {
		if (m == GraphicsCanvas.MouseMode.ACTIVESYSTEM) {
			activeRBMI.doClick();
		} else {
			mouseRBMI.doClick();
		}
	}

	public void setPickBehavior(boolean pick) {
		pickingCBMI.setSelected(pick);
	}

	public void setPickLevel(String arg) {
		if (arg.equals("PickAtom")) {
			atomRBMI.doClick();
		} else if (arg.equals("PickBond")) {
			bondRBMI.doClick();
		} else if (arg.equals("PickAngle")) {
			angleRBMI.doClick();
		} else if (arg.equals("PickDihedral")) {
			dihedralRBMI.doClick();
		} else if (arg.equals("PickResidue")) {
			residueRBMI.doClick();
		} else if (arg.equals("PickPolymer")) {
			polymerRBMI.doClick();
		} else if (arg.equals("PickMolecule")) {
			moleculeRBMI.doClick();
		} else if (arg.equals("PickSystem")) {
			systemRBMI.doClick();
		} else if (arg.equals("MeasureDistance")) {
			measureDistanceRBMI.doClick();
		} else if (arg.equals("MeasureAngle")) {
			measureAngleRBMI.doClick();
		} else if (arg.equals("MeasureDihedral")) {
			measureDihedralRBMI.doClick();
		}
	}

	public void setResidueLabels(boolean b) {
		labelResiduesMI.setSelected(b);
	}

	public void setSystemShowing(boolean b) {
		systemsCBMI.setSelected(b);
	}

	public void systemClick() {
		systemsCBMI.doClick();
	}
}
