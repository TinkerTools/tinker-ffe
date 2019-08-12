/*
 * <p>Title: Force Field Explorer</p>
 * <p>Description: Force Field Explorer Molecular Modeling Program</p>
 * <p>Copyright: Copyright (c) 2004-2019 Jay William Ponder</p>
 * <p>Institution: Jay Ponder Lab, Washington University in Saint Louis</p>
 * @author Michael J. Schnieders
 * @version 8.7
 */

package ffe.core;

import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JColorChooser;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import org.jogamp.java3d.AmbientLight;
import org.jogamp.java3d.Background;
import org.jogamp.java3d.BoundingSphere;
import org.jogamp.java3d.Bounds;
import org.jogamp.java3d.BranchGroup;
import org.jogamp.java3d.Canvas3D;
import org.jogamp.java3d.DirectionalLight;
import org.jogamp.java3d.GraphicsConfigTemplate3D;
import org.jogamp.java3d.GraphicsContext3D;
import org.jogamp.java3d.ImageComponent;
import org.jogamp.java3d.ImageComponent2D;
import org.jogamp.java3d.J3DGraphics2D;
import org.jogamp.java3d.Raster;
import org.jogamp.java3d.Transform3D;
import org.jogamp.java3d.TransformGroup;
import org.jogamp.java3d.View;
import org.jogamp.java3d.utils.universe.SimpleUniverse;
import org.jogamp.vecmath.Color3f;
import org.jogamp.vecmath.Point3d;
import org.jogamp.vecmath.Point3f;
import org.jogamp.vecmath.Vector3d;
import org.jogamp.vecmath.Vector3f;

import ffe.lang.MSNode;
import ffe.lang.MolecularAssembly;
import ffe.lang.RendererCache;

/*
 * The GraphicsCanvas class provides a Canvas on which to render 3D Graphics.
 * The following display types are currently supported: Wireframe, Ball & Stick,
 * Spacefill/CPK, RMIN and Tube.
 */
@SuppressWarnings("serial")
public class GraphicsCanvas extends Canvas3D implements ActionListener {
	/*
	 * The ImageFormat enum lists supported image formats.
	 */
	public enum ImageFormat {
		BMP, GIF, JPEG, PNG, WBMP;
	}

	/*
	 * The MouseMode enum describes what system is affected by mouse drags.
	 */
	public enum MouseMode {
		SYSTEMBELOWMOUSE, ACTIVESYSTEM;
	}
	
	/*
	 * The LeftButtonMode enum describes what the left mouse button does.
	 */
	public enum LeftButtonMode {
		ROTATE, TRANSLATE, ZOOM;
	}

	public static final Hashtable<String, ImageFormat> imageFormatHash = new Hashtable<String, ImageFormat>();

	static {
		ImageFormat values[] = ImageFormat.values();
		for (ImageFormat value : values) {
			imageFormatHash.put(value.toString(), value);
		}
	}

	// Controller Classes
	private ffe.lang.Renderer renderer;

	private GraphicsEvents graphicsEvents;

	private GraphicsPicking rendererPicking;

	private MainPanel mainPanel;

	private GraphicsAxis graphicsAxis;

	private GraphicsFullScreen fullScreenWindow;

	private static Logger logger = Logger.getLogger("ffe");

	// 3D Universe Variables
	private SimpleUniverse universe;

	private Background background;

	private BranchGroup baseBranchGroup;

	private TransformGroup baseTransformGroup;

	private Transform3D baseTransform3D = new Transform3D();

	private Bounds bounds;

	// State Variables
	private GraphicsPrefs graphics3DPrefs = null;

	private MouseMode mouseMode = MouseMode.ACTIVESYSTEM;

	private ImageFormat imageFormat = ImageFormat.PNG;

	private LeftButtonMode leftButtonMode = LeftButtonMode.ROTATE;
	
	private boolean imageCapture = false;

	private File imageName;

	/*
	 * The GraphicsCanvas constructor initializes the Java3D Universe and
	 * Behaviors.
	 */
	public GraphicsCanvas(GraphicsConfiguration config, MainPanel f) {
		super(config);
		mainPanel = f;
		// logger.info("CG: " +
		// isShadingLanguageSupported(Shader.SHADING_LANGUAGE_CG));
		// logger.info("GLSL: " +
		// isShadingLanguageSupported(Shader.SHADING_LANGUAGE_GLSL));
		initialize();
	}

	public GraphicsCanvas(MainPanel f) {
		this(GraphicsEnvironment.getLocalGraphicsEnvironment()
				.getDefaultScreenDevice().getBestConfiguration(
						new GraphicsConfigTemplate3D()), f);
	}

	/*
	 * Handles ActionEvents from the Selection, Display, Color, Options, and
	 * Picking Menus.
	 */
	public void actionPerformed(ActionEvent evt) {
		String arg = evt.getActionCommand();
		// Selection Menu
		if (arg.equals("LabelSelectedAtoms")) {
			labelSelectedAtoms();
		} else if (arg.equals("LabelSelectedResidues")) {
			labelSelectedResidues();
		} else if (arg.equals("SetLabelFontColor")) {
			setLabelFontColor();
		} else if (arg.equals("SetLabelFontSize")) {
			setLabelFontSize();
			// Display Menu
		} else if (RendererCache.viewModelHash.containsKey(arg.toUpperCase())) {
			setViewModel(arg);
		} else if (arg.equals("Preferences")) {
			preferences();
			// Color Menu
		} else if (RendererCache.colorModelHash.containsKey(arg.toUpperCase())) {
			setColorModel(arg);
		} else if (arg.equals("SetSelectionColor")) {
			setSelectionColor();
		} else if (arg.equals("SetUserColor")) {
			setUserColor();
			// Options
		} else if (arg.equals("SystemBelowMouse")) {
			mouseMode = MouseMode.SYSTEMBELOWMOUSE;
		} else if (arg.equals("ActiveSystem")) {
			mouseMode = MouseMode.ACTIVESYSTEM;
		} else if (arg.equals("Rotate")) {
			leftButtonMode = LeftButtonMode.ROTATE;
		} else if (arg.equals("Translate")) {
			leftButtonMode = LeftButtonMode.TRANSLATE;
		} else if (arg.equals("Zoom")) {
			leftButtonMode = LeftButtonMode.ZOOM;
		} else if (arg.equals("ResetRotation")) {
			resetRotation();
		} else if (arg.equals("ResetTranslation")) {
			resetTranslation();
		} else if (arg.equals("ResetRotationAndTranslation")) {
			resetRotationAndTranslation();
		} else if (arg.equalsIgnoreCase("RotateAboutPick")) {
			rotateAboutPick();
		} else if (arg.equalsIgnoreCase("RotateAboutCenter")) {
			rotateAboutCenter();
		} else if (arg.equalsIgnoreCase("ResetGlobalTranslation")) {
			resetGlobalTranslation();
		} else if (arg.equalsIgnoreCase("ResetGlobalRotation")) {
			resetGlobalRotation();
		} else if (arg.equalsIgnoreCase("ResetGlobalZoom")) {
			resetGlobalZoom();
		} else if (arg.equalsIgnoreCase("ResetGlobalView")) {
			resetGlobalView();
		} else if (arg.equals("FullScreen")) {
			fullScreen();
		} else if (arg.equals("SetBackgroundColor")) {
			setBackgroundColor();
		} else if (arg.equalsIgnoreCase("ZoomIn")) {
			zoomIn();
		} else if (arg.equalsIgnoreCase("ZoomOut")) {
			zoomOut();
			// Picking Menu
		} else if (arg.equalsIgnoreCase("GraphicsPicking")) {
			graphicsPicking(evt);
		} else if (imageFormatHash.containsKey(arg.toUpperCase())) {
			setImageFormat(arg);
		} else if (arg.equals("CaptureGraphics")) {
			captureGraphics();
		} else if (GraphicsPicking.pickLevelHash.containsKey(arg.toUpperCase())) {
			setPickingLevel(arg);
		} else if (arg.equals("SetGraphicsPickingColor")) {
			setGraphicsPickingColor();
		} else {
			logger.warning("Graphics Menu command not found: "
					+ arg.toString());
		}
	}

	/*
	 * This attaches a MolecularAssembly to the Scene BranchGroup.
	 * 
	 * @param s
	 *            MolecularAssembly to attach.
	 */
	public void attachModel(MolecularAssembly s) {
		if (s == null) {
			return;
		}
		synchronized (this) {
			BranchGroup bg = s.getBranchGroup();
			resetGlobalView();
			baseBranchGroup.addChild(bg);
		}
	}

	private void captureGraphics() {
		MolecularAssembly active = mainPanel.getHierarchy().getActive();
		if (active == null) {
			return;
		}
		imageName = null;
		String name = active.getName();
		JFileChooser fileChooser = MainPanel.getFileChooser();
		fileChooser.setAcceptAllFileFilterUsed(true);
		if (mainPanel.getHierarchy().getActive() != null) {
			imageName = mainPanel.getHierarchy().getActive().getFile();
		} else {
			imageName = null;
		}
		if (imageName != null) {
			if (name.indexOf(".") > 0) {
				name = name.substring(0, name.indexOf("."));
			}
			imageName = new File(imageName.getParentFile() + File.separator
					+ name + "." + imageFormat);
			fileChooser.setSelectedFile(imageName);
		}
		fileChooser.setDialogTitle("Select Name for Screen Capture " + "("
				+ imageFormat + ")");
		fileChooser.setCurrentDirectory(MainPanel.getCWD());
		int result = fileChooser.showSaveDialog(this);
		if (result == JFileChooser.APPROVE_OPTION) {
			imageName = fileChooser.getSelectedFile();
			mainPanel.setCWD(fileChooser.getCurrentDirectory());
			imageCapture = true;
			repaint();
		}
	}

	public void fullScreen() {
		if (fullScreenWindow == null) {
			fullScreenWindow = new GraphicsFullScreen(
					mainPanel.getFrame(), this);
		}
		fullScreenWindow.enterFullScreen();
	}

	public MouseMode getMouseMode() {
		return mouseMode;
	}
	
	public LeftButtonMode getLeftButtonMode() {
		return leftButtonMode;
	}

	public GraphicsAxis getNavigation() {
		return graphicsAxis;
	}

	public JLabel getStatusBar() {
		return mainPanel.getStatusBar();
	}

	public void graphicsPicking(ActionEvent evt) {
		if (evt.getSource() instanceof JButton) {
			MainMenu m = mainPanel.getMainMenu();
			boolean picking = m.getPicking();
			if (picking) {
				rendererPicking.clear();
				rendererPicking.setPicking(false);
				m.setPickBehavior(false);
			} else {
				rendererPicking.setPicking(true);
				m.setPickBehavior(true);
			}
		} else if (evt.getSource() instanceof JCheckBoxMenuItem) {
			JCheckBoxMenuItem jcbmi = (JCheckBoxMenuItem) evt.getSource();
			if (jcbmi.isSelected()) {
				rendererPicking.setPicking(true);
			} else {
				rendererPicking.setPicking(false);
			}
		}
	}

	/*
	 * Initialization of the GraphisCanvas.
	 */
	private void initialize() {
		setBackground(Color.black);
		universe = new SimpleUniverse(this);
		SimpleUniverse.setJ3DThreadPriority(Thread.MAX_PRIORITY);
		universe.getViewingPlatform().setNominalViewingTransform();
		// Create the Scene Root BranchGroup
		BranchGroup objRoot = new BranchGroup();
		baseTransformGroup = new TransformGroup();
		Transform3D t3d = new Transform3D();
		t3d.setScale(0.1d);
		baseTransformGroup.setTransform(t3d);
		baseTransformGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		baseTransformGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
		// Set the Background
		Color3f bgColor = new Color3f(RendererCache.BLACK);
		background = new Background(bgColor);
		background.setCapability(Background.ALLOW_COLOR_READ);
		background.setCapability(Background.ALLOW_COLOR_WRITE);
		bounds = new BoundingSphere(new Point3d(0.0, 0.0, 0.0), 2000.0);
		background.setApplicationBounds(bounds);
		// Create lights
		AmbientLight aLgt = new AmbientLight(new Color3f(Color.darkGray));
		aLgt.setInfluencingBounds(bounds);
		Vector3f dir = new Vector3f(0.0f, -1.0f, -1.0f);
		Color3f dLgtColor = new Color3f(Color.lightGray);
		DirectionalLight dLgt = new DirectionalLight(dLgtColor, dir);
		dLgt.setInfluencingBounds(bounds);
		dir = new Vector3f(0.0f, 1.0f, -1.0f);
		dLgtColor = new Color3f(0.1f, 0.1f, 0.1f);
		DirectionalLight dLgt2 = new DirectionalLight(dLgtColor, dir);
		dLgt2.setInfluencingBounds(bounds);
		// Create the Base of the Molecular Scene
		baseBranchGroup = new BranchGroup();
		baseBranchGroup.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
		baseBranchGroup.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
		baseBranchGroup.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
		baseBranchGroup.setCapability(BranchGroup.ALLOW_BOUNDS_READ);
		// Add children created above to the base TransformGroup
		baseTransformGroup.addChild(background);
		baseTransformGroup.addChild(baseBranchGroup);
		objRoot.addChild(baseTransformGroup);
		// Position the view platmform and add lights
		View v = universe.getViewer().getView();
		v.setProjectionPolicy(View.PARALLEL_PROJECTION);
		v.setFrontClipPolicy(View.VIRTUAL_EYE);
		v.setFrontClipDistance(1.0);
		v.setBackClipPolicy(View.VIRTUAL_EYE);
		v.setBackClipDistance(10.0);
		v.setTransparencySortingPolicy(View.TRANSPARENCY_SORT_NONE);
		Transform3D trans = new Transform3D();
		trans.set(new Vector3d(0.0d, 0.0d, 2.0d));
		TransformGroup vptg = universe.getViewingPlatform()
				.getViewPlatformTransform();
		vptg.setTransform(trans);
		BranchGroup viewBranch = new BranchGroup();
		viewBranch.addChild(aLgt);
		viewBranch.addChild(dLgt);
		viewBranch.addChild(dLgt2);
		vptg.addChild(viewBranch);
		// Initialize Behaviors
		graphicsAxis = new GraphicsAxis(universe.getViewingPlatform(), this,
				bounds);
		graphicsEvents = new GraphicsEvents(mainPanel, this, graphicsAxis,
				universe, bounds, baseBranchGroup, baseTransformGroup);
		baseBranchGroup.addChild(graphicsEvents);
		rendererPicking = new GraphicsPicking(baseBranchGroup, bounds, this,
				mainPanel);
		baseBranchGroup.addChild(rendererPicking);
		renderer = new ffe.lang.Renderer(bounds, mainPanel.getStatusBar());
		baseBranchGroup.addChild(renderer);
		// Compile the Root BranchGroup and add it to the Universe
		objRoot.compile();
		universe.addBranchGraph(objRoot);
	}

	// protected void processMouseEvent(java.awt.event.MouseEvent event){
	// super.processMouseEvent(event);
	// logger.info("MouseEvent: " + event.toString());
	// }

	public boolean isCacheFull() {
		return renderer.isCacheFull();
	}

	public boolean isSceneRendering() {
		return renderer.isArmed();
	}

	public void labelSelectedAtoms() {
		if (RendererCache.labelAtoms == true) {
			RendererCache.labelAtoms = false;
			getStatusBar().setText("  Atom Labeling Turned Off");
		} else {
			RendererCache.labelAtoms = true;
			getStatusBar().setText("  Atom Labeling Turned On");
		}
		repaint();
	}

	/* ***************************************************************** */
	// Selection Commands
	public void labelSelectedResidues() {
		if (RendererCache.labelResidues == true) {
			RendererCache.labelResidues = false;
			getStatusBar().setText("  Residue Labeling Turned Off");
		} else {
			RendererCache.labelResidues = true;
			getStatusBar().setText("  Residue Labeling Turned On");
		}
		repaint();
	}

	/*
	 * Load preferences from the user node.
	 */
	public void loadPrefs() {
		Preferences prefs = Preferences.userNodeForPackage(MainPanel.class);
		RendererCache.bondwidth = prefs.getInt("Graphics_bondwidth", 3);
		RendererCache.detail = prefs.getInt("Graphics_detail", 3);
		RendererCache.radius = prefs.getDouble("Graphics_radius", 1.0d);
		String s = prefs.get("Graphics_mouse", MouseMode.ACTIVESYSTEM.name());
		if (s.equalsIgnoreCase("ACTIVESYSTEM")
				|| s.equalsIgnoreCase("SYSTEMBELOWMOUSE")) {
			mouseMode = MouseMode.valueOf(s);
		} else {
			mouseMode = MouseMode.ACTIVESYSTEM;
		}
		mainPanel.getMainMenu().setMouseMode(mouseMode);
		RendererCache.highlightSelections = prefs.getBoolean(
				"Graphics_highlight", false);
		mainPanel.getMainMenu().setHighlighting(
				RendererCache.highlightSelections);
		String[] hlColor = prefs.get("Graphics_highlightColor", "153 153 255")
				.trim().split(" +");
		Color newColor = new Color(Integer.parseInt(hlColor[0]), Integer
				.parseInt(hlColor[1]), Integer.parseInt(hlColor[2]));
		RendererCache.selectionColor = new Color3f(newColor);
		RendererCache.labelAtoms = prefs.getBoolean("Graphics_labelAtoms",
				false);
		mainPanel.getMainMenu().setAtomLabels(RendererCache.labelAtoms);
		RendererCache.labelResidues = prefs.getBoolean(
				"Graphics_labelResidues", false);
		mainPanel.getMainMenu().setResidueLabels(RendererCache.labelResidues);
		/*
		 * int fontSize = prefs.getInt("Graphics_labelSize", 12); J3DGraphics2D
		 * j2D = getOffscreenCanvas3D().getGraphics2D(); Font currentFont =
		 * j2D.getFont(); Font newFont = new Font(currentFont.getName(),
		 * currentFont.getStyle(), fontSize); j2D.setFont(newFont); String[]
		 * fontColor = prefs.get("Graphics_labelColor", "255 255 255")
		 * .trim().split(" +"); newColor = new
		 * Color(Integer.parseInt(fontColor[0]), Integer
		 * .parseInt(fontColor[1]), Integer.parseInt(fontColor[2]));
		 * j2D.setPaint(newColor);
		 */
		String[] pickColor = prefs.get("Graphics_pickColor", "102 255 102")
				.trim().split(" +");
		newColor = new Color(Integer.parseInt(pickColor[0]), Integer
				.parseInt(pickColor[1]), Integer.parseInt(pickColor[2]));
		RendererCache.pickingColor = new Color3f(newColor);
		String pickLevel = prefs.get("Graphics_pickLevel", "PickAtom");
		mainPanel.getMainMenu().setPickLevel(pickLevel);
		boolean pickMode = prefs.getBoolean("Graphics_picking", false);
		if (pickMode) {
			rendererPicking.setPicking(true);
		}
		mainPanel.getMainMenu().setPickBehavior(pickMode);
		String[] userColor = prefs.get("Graphics_userColor", "255 255 255")
				.trim().split(" +");
		newColor = new Color(Integer.parseInt(userColor[0]), Integer
				.parseInt(userColor[1]), Integer.parseInt(userColor[2]));
		RendererCache.userColor = new Color3f(newColor);
		/*
		 * String[] bgColor = prefs.get("Graphics_backgroundColor", "0 0 0")
		 * .trim().split(" +"); newColor = new
		 * Color(Integer.parseInt(bgColor[0]), Integer .parseInt(bgColor[1]),
		 * Integer.parseInt(bgColor[2])); background.setColor(new
		 * Color3f(newColor));
		 */
	}

	/* ***************************************************************** */
	// The following three methods modify default Canvas3D methods.
	public void paint(java.awt.Graphics g) {
		super.paint(g);
		Toolkit.getDefaultToolkit().sync();
	}

	/*
	 * Labels are drawn in postRender.
	 */
	public void postRender() {
		if (RendererCache.labelAtoms || RendererCache.labelResidues) {
			J3DGraphics2D g2D = getGraphics2D();
			synchronized (mainPanel.getHierarchy()) {
				ArrayList<MSNode> nodes = mainPanel.getHierarchy()
						.getActiveNodes();
				if (nodes != null && nodes.size() > 0) {
					for (MSNode node : nodes) {
						MolecularAssembly sys = (MolecularAssembly) node
								.getMSNode(MolecularAssembly.class);
						if (sys != null) {
							node.drawLabel(this, g2D, sys.getWireFrame());
						}
					}
				} else {
					return;
				}
			}
			g2D.flush(true);
		}
		// logger.info("Frame Time: " +
		// this.getView().getLastFrameDuration());
	}

	/*
	 * Image capture from the 3D Canvas is done in postSwap.
	 */
	public void postSwap() {
		if (!imageCapture || mainPanel.getHierarchy().getActive() == null) {
			return;
		}
		GraphicsContext3D ctx = getGraphicsContext3D();
		Rectangle rect = getBounds();
		BufferedImage img = new BufferedImage(rect.width, rect.height,
				BufferedImage.TYPE_INT_RGB);
		ImageComponent2D comp = new ImageComponent2D(ImageComponent.FORMAT_RGB,
				img);
		Raster ras = new Raster(new Point3f(-1.0f, -1.0f, -1.0f),
				Raster.RASTER_COLOR, 0, 0, rect.width, rect.height, comp, null);
		ctx.readRaster(ras);
		img = ras.getImage().getImage();
		try {

			if (!ImageIO.write(img, imageFormat.toString(), imageName)) {
				String os = System.getProperty("os.name");
				logger.severe("No image writer was found for "
						+ imageFormat.toString() + " on " + os 
						+ ".\nThis Java Runtime Environment (JRE) fails to meet the javax.imageio specification.\n"
						+ "Shame on you " + os + "!\n"
						+ "Please try a different image format.\n");
				imageName.delete();
			} else {
				logger.info("" + imageName + " was captured.");
			}
		} catch (IOException e) {
			logger.warning("Image capture failed.\n" + e);
		}
		imageCapture = false;
	}

	public void preferences() {
		if (graphics3DPrefs == null) {
			graphics3DPrefs = new GraphicsPrefs(mainPanel.getFrame(),
					mainPanel.getDataRoot());
			graphics3DPrefs.setModal(false);
		}
		graphics3DPrefs.setVisible(true);
		graphics3DPrefs.toFront();
	}

	public void resetGlobalRotation() {
		graphicsEvents.centerView(true, false, false);
	}

	public void resetGlobalTranslation() {
		graphicsEvents.centerView(false, true, false);
	}

	/*
	 * This functions centers the scene.
	 */
	public void resetGlobalView() {
		double radius = mainPanel.getDataRoot().getExtent();
		Transform3D t3d = new Transform3D();
		t3d.setScale(1.0d / (1.2d * radius));
		baseTransformGroup.setTransform(t3d);
		graphicsEvents.centerView(true, true, true);
	}

	public void resetGlobalZoom() {
		double radius = mainPanel.getDataRoot().getExtent();
		baseTransformGroup.getTransform(baseTransform3D);
		baseTransform3D.setScale(1.0d / (1.2d * radius));
		baseTransformGroup.setTransform(baseTransform3D);
	}

	/* ***************************************************************** */
	// Options Commands
	public void resetRotation() {
		MolecularAssembly sys = mainPanel.getHierarchy().getActive();
		if (sys != null) {
			sys.centerView(true, false);
		}
	}

	public void resetRotationAndTranslation() {
		MolecularAssembly sys = mainPanel.getHierarchy().getActive();
		if (sys != null) {
			sys.centerView(true, true);
		}
	}

	public void resetTranslation() {
		MolecularAssembly sys = mainPanel.getHierarchy().getActive();
		if (sys != null) {
			sys.centerView(false, true);
		}
	}

	public void rotateAboutCenter() {
		MolecularAssembly sys = mainPanel.getHierarchy().getActive();
		double center[] = sys.getMultiScaleCenter(false);
		sys.rotateAbout(new Vector3d(center));
	}

	public void rotateAboutPick() {
		MSNode node = rendererPicking.getPick();
		if (node != null) {
			double center[] = node.getCenter(false);
			MolecularAssembly m = (MolecularAssembly) node
					.getMSNode(MolecularAssembly.class);
			m.rotateAbout(new Vector3d(center));
		}
	}

	/* ***************************************************************** */
	// The following three methods modidfy default Canvas3D methods.
	/*
	 * Save preferences to the user node.
	 */
	public void savePrefs() {
		Preferences prefs = Preferences.userNodeForPackage(MainPanel.class);
		prefs.putInt("Graphics_bondwidth", RendererCache.bondwidth);
		prefs.putInt("Graphics_detail", RendererCache.detail);
		prefs.putDouble("Graphics_radius", RendererCache.radius);
		prefs.put("Graphics_mouse", mouseMode.name());
		prefs.putBoolean("Graphics_highlight",
				RendererCache.highlightSelections);
		Color col = RendererCache.selectionColor.get();
		prefs.put("Graphics_highlightColor", "" + col.getRed() + " "
				+ col.getGreen() + " " + col.getBlue());
		prefs.putBoolean("Graphics_labelAtoms", RendererCache.labelAtoms);
		prefs.putBoolean("Graphics_labelResidues", RendererCache.labelResidues);
		prefs.putInt("Graphics_labelSize", getGraphics2D().getFont().getSize());
		Color fontColor = getGraphics2D().getColor();
		prefs.put("Graphics_labelColor", "" + fontColor.getRed() + " "
				+ fontColor.getGreen() + " " + fontColor.getBlue());
		col = RendererCache.pickingColor.get();
		prefs.put("Graphics_pickColor", "" + col.getRed() + " "
				+ col.getGreen() + " " + col.getBlue());
		prefs.putBoolean("Graphics_picking", rendererPicking.getPicking());
		prefs.put("Graphics_pickLevel", rendererPicking.getPickLevel());
		col = RendererCache.userColor.get();
		prefs.put("Graphics_userColor", "" + col.getRed() + " "
				+ col.getGreen() + " " + col.getBlue());
		Color3f temp = new Color3f();
		background.getColor(temp);
		col = temp.get();
		prefs.put("Graphics_backgroundColor", "" + col.getRed() + " "
				+ col.getGreen() + " " + col.getBlue());
	}

	public void selected() {
		validate();
		repaint();
	}

	public void setAxisShowing(boolean b) {
		if (b && graphicsAxis == null) {
			graphicsAxis = new GraphicsAxis(universe.getViewingPlatform(),
					this, bounds);
		} else if (graphicsAxis != null) {
			graphicsAxis.showAxis(b);
		}
	}

	public void setBackgroundColor() {
		Color3f col = new Color3f();
		background.getColor(col);
		Color newcolor = JColorChooser.showDialog(this,
				"Choose Background Color", col.get());
		if (newcolor != null && newcolor != col.get()) {
			background.setColor(new Color3f(newcolor));
		}
	}

	public void setCaptures(boolean c) {
		imageCapture = c;
	}

	/* ***************************************************************** */
	// Color Commands
	/*
	 * Operates on the Active nodes.
	 * 
	 * @param model
	 *            String
	 */
	public void setColorModel(String model) {
		if (!RendererCache.colorModelHash.containsKey(model.toUpperCase())) {
			return;
		}
		RendererCache.ColorModel colorModel = RendererCache.colorModelHash
				.get(model.toUpperCase());
		ArrayList<MSNode> active = mainPanel.getHierarchy().getActiveNodes();
		if (active == null) {
			return;
		}
		renderer.arm(active, false, false, null, true, colorModel);
	}

	/*
	 * Operates on the Active nodes.
	 * 
	 * @param model
	 *            String
	 */
	public void setColorModel(String model, MSNode node) {
		if (node == null) {
			return;
		}
		if (!RendererCache.colorModelHash.containsKey(model.toUpperCase())) {
			return;
		}
		RendererCache.ColorModel colorModel = RendererCache.colorModelHash
				.get(model.toUpperCase());
		renderer.arm(node, false, false, null, true, colorModel);
	}

	public void setGraphicsPickingColor() {
		Color3f col = new Color3f();
		RendererCache.pickingColor.get(col);
		Color newcolor = JColorChooser.showDialog(this, "Choose Picking Color",
				col.get());
		if (newcolor != null) {
			RendererCache.pickingColor = new Color3f(newcolor);
		}
	}

	/* ***************************************************************** */
	// Export Commands
	public void setImageFormat(String format) {
		if (format == null) {
			return;
		}
		format = format.toUpperCase();
		if (!imageFormatHash.containsKey(format)) {
			return;
		}
		imageFormat = imageFormatHash.get(format);
	}

	public void setLabelFontColor() {
		Color color = getGraphics2D().getColor();
		Color newColor = JColorChooser.showDialog(this, "Choose Font Color",
				color);
		if (newColor != null && newColor != color) {
			getGraphics2D().setPaint(newColor);
			if (RendererCache.labelAtoms || RendererCache.labelResidues) {
				repaint();
			}
			getStatusBar().setText(
					"  Label Font Color Changed to (" + newColor.getRed() + ","
							+ newColor.getGreen() + "," + newColor.getBlue()
							+ ")");
		}
	}

	public void setLabelFontSize() {
		Font currentFont = getGraphics2D().getFont();
		int currentSize = currentFont.getSize();
		String size = new String("" + currentSize);
		size = JOptionPane.showInputDialog("Set the Font Size (8 to 64)", size);
		try {
			int f = Integer.parseInt(size);
			if (f < 8 || f > 64 || f == currentSize) {
				return;
			}
			Font newFont = new Font(currentFont.getName(), currentFont
					.getStyle(), f);
			getGraphics2D().setFont(newFont);
			if (RendererCache.labelAtoms || RendererCache.labelResidues) {
				repaint();
			}
			getStatusBar().setText(
					"  Label Font Size Changed to " + newFont.getSize());
		} catch (NumberFormatException e) {
			return;
		}
	}

	/* ***************************************************************** */
	// Misc. get and set methods.
	public void setLabelsUpdated() {
		repaint();
	}

	/* ***************************************************************** */
	// Picking Commands
	public void setPickingLevel(String level) {
		if (level == null) {
			return;
		}
		level = level.toUpperCase();
		if (GraphicsPicking.pickLevelHash.containsKey(level)) {
			GraphicsPicking.PickLevel pickLevel = GraphicsPicking.pickLevelHash
					.get(level);
			switch (pickLevel) {
			case PICKATOM:
			case PICKBOND:
			case PICKANGLE:
			case PICKDIHEDRAL:
			case PICKRESIDUE:
			case PICKPOLYMER:
			case PICKSYSTEM:
				rendererPicking.setPickLevel(level);
				break;
			case MEASUREDISTANCE:
			case MEASUREANGLE:
			case MEASUREDIHEDRAL:
				rendererPicking.setPickLevel(level);
				rendererPicking.setPicking(true);
				mainPanel.getMainMenu().setPickBehavior(true);
				rendererPicking.clear();
				rendererPicking.resetCount();
				break;
			default:
				logger.warning("Unexpected PickingLevel");
			}
		}
	}

	public void setPosition() {
		setPosition(mainPanel.getHierarchy().getActive());
	}

	public void setPosition(MSNode node) {
		updateScene(node, true, false, null, true, null);
	}

	public void setSelectionColor() {
		Color3f col = new Color3f();
		RendererCache.selectionColor.get(col);
		Color newcolor = JColorChooser.showDialog(this,
				"Choose Selection Color", col.get());
		if (newcolor != null) {
			RendererCache.selectionColor = new Color3f(newcolor);
		}
		if (RendererCache.highlightSelections) {
			this.updateScene(mainPanel.getDataRoot(), false, false, null, true,
					RendererCache.ColorModel.SELECT);
		}
	}

	public void setUserColor() {
		Color3f col = new Color3f();
		RendererCache.selectionColor.get(col);
		Color newcolor = JColorChooser.showDialog(this, "Choose User Color",
				col.get());
		if (newcolor != null) {
			RendererCache.userColor = new Color3f(newcolor);
		}
	}

	/* ***************************************************************** */
	// Display Commands
	/*
	 * Operates on the active nodes.
	 * 
	 * @param model
	 *            String
	 */
	public void setViewModel(String model) {
		if (!RendererCache.viewModelHash.containsKey(model.toUpperCase())) {
			return;
		}
		RendererCache.ViewModel viewModel = RendererCache.viewModelHash
				.get(model.toUpperCase());
		if (viewModel == RendererCache.ViewModel.RESTRICT) {
			renderer.arm(mainPanel.getDataRoot(), false, true, viewModel,
					false, null);
			return;
		}
		ArrayList<MSNode> active = mainPanel.getHierarchy().getActiveNodes();
		if (active == null) {
			return;
		}
		renderer.arm(active, false, true, viewModel, false, null);
	}

	/*
	 * Operates on the supplied node.
	 * 
	 * @param model
	 *            String
	 */
	public void setViewModel(String model, MSNode node) {
		if (node == null) {
			return;
		}
		if (!RendererCache.viewModelHash.containsKey(model.toUpperCase())) {
			return;
		}
		RendererCache.ViewModel viewModel = RendererCache.viewModelHash
				.get(model.toUpperCase());
		renderer.arm(node, false, true, viewModel, false, null);
	}

	public String toString() {
		return "3D Graphics";
	}

	public void updateScene(ArrayList<MSNode> n, boolean t, boolean v,
			RendererCache.ViewModel newViewModel, boolean c,
			RendererCache.ColorModel newColorModel) {
		if (n != null) {
			renderer.arm(n, t, v, newViewModel, c, newColorModel);
		}
	}

	public void updateScene(MSNode n, boolean t, boolean v,
			RendererCache.ViewModel newViewModel, boolean c,
			RendererCache.ColorModel newColorModel) {
		if (n != null) {
			renderer.arm(n, t, v, newViewModel, c, newColorModel);
		}
	}

	public void updateSceneWait(ArrayList<MSNode> n, boolean t, boolean v,
			RendererCache.ViewModel newViewModel, boolean c,
			RendererCache.ColorModel newColorModel) {
		if (n != null) {
			renderer.arm(n, t, v, newViewModel, c, newColorModel);
		}
		while (isSceneRendering() || isCacheFull()) {
			synchronized (this) {
				try {
					wait(1);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void updateSceneWait(MSNode n, boolean t, boolean v,
			RendererCache.ViewModel newViewModel, boolean c,
			RendererCache.ColorModel newColorModel) {
		if (n != null) {
			renderer.arm(n, t, v, newViewModel, c, newColorModel);
		}
		while (isSceneRendering() || isCacheFull()) {
			synchronized (this) {
				try {
					wait(1);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void zoomIn() {
		baseTransformGroup.getTransform(baseTransform3D);
		double scale = baseTransform3D.getScale() + 0.01;
		baseTransform3D.setScale(scale);
		baseTransformGroup.setTransform(baseTransform3D);
	}

	public void zoomOut() {
		baseTransformGroup.getTransform(baseTransform3D);
		double scale = baseTransform3D.getScale() - 0.01;
		if (scale > 0.0) {
			baseTransform3D.setScale(scale);
			baseTransformGroup.setTransform(baseTransform3D);
		}
	}
}
