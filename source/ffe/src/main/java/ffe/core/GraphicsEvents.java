/*
 * <p>Title: Force Field Explorer</p>
 * <p>Description: Force Field Explorer Molecular Modeling Program</p>
 * <p>Copyright: Copyright (c) 2004-2021 Jay William Ponder</p>
 * <p>Institution: Jay Ponder Lab, Washington University in Saint Louis</p>
 * @author Michael J. Schnieders
 * @version 8.10
 */

package ffe.core;

import java.awt.AWTEvent;
import java.awt.Event;
import java.awt.event.MouseEvent;
import java.util.Iterator;

import org.jogamp.java3d.Behavior;
import org.jogamp.java3d.Bounds;
import org.jogamp.java3d.BranchGroup;
import org.jogamp.java3d.Node;
import org.jogamp.java3d.SceneGraphPath;
import org.jogamp.java3d.Shape3D;
import org.jogamp.java3d.TransformGroup;
import org.jogamp.java3d.WakeupCriterion;
import org.jogamp.java3d.WakeupOnAWTEvent;
import org.jogamp.java3d.WakeupOnBehaviorPost;
import org.jogamp.java3d.WakeupOr;
import org.jogamp.java3d.utils.picking.PickCanvas;
import org.jogamp.java3d.utils.picking.PickIntersection;
import org.jogamp.java3d.utils.picking.PickResult;
import org.jogamp.java3d.utils.universe.SimpleUniverse;
import org.jogamp.vecmath.Point3d;

import ffe.behaviors.GlobalBehavior;
import ffe.behaviors.MouseRotate;
import ffe.behaviors.MouseTranslate;
import ffe.behaviors.MouseZoom;
import ffe.core.GraphicsCanvas.LeftButtonMode;
import ffe.lang.Atom;
import ffe.lang.MolecularAssembly;

/*
 * The GraphicsEvents class listens for mouse events over the Java3D
 * GraphicsCanvas, dispatching work to more specialized System Rotation &
 * Translation Behaviors or to the GlobalOrbitBehavior.
 */
public class GraphicsEvents extends Behavior {
	// Behavior Post IDs
	public static int ROTATEPOST = 1;

	public static int TRANSLATEPOST = 2;

	public static int ZOOMPOST = 3;

	public static int BEHAVIORDONEPOST = 4;

	// GUI Panels
	private MainPanel mainPanel;

	private GraphicsCanvas graphics3D;

	private GraphicsAxis globalAxis;

	// Scenegraph Nodes
	private SimpleUniverse simpleUniverse;

	private TransformGroup viewTransformGroup;

	private Bounds bounds;

	private BranchGroup baseBranchGroup;

	private TransformGroup baseTransform;

	// Wake up conditions
	private WakeupOr mouseCriterion;

	private WakeupOr postCriterion;

	// Mouse/Pick state upon wake up event
	private boolean buttonPress;

	private int x, y;

	private boolean leftButton;

	private boolean rightButton;

	private boolean middleButton;

	private PickCanvas pickCanvas;

	private PickResult pickResult;

	private Atom atom;

	private boolean axisSelected;

	// Behaviors
	private MouseRotate systemRotate;

	private MouseTranslate systemTranslate;

	private MouseZoom globalZoom;

	private GlobalBehavior viewOrbitBehavior;

	public GraphicsEvents(MainPanel f, GraphicsCanvas g, GraphicsAxis n,
			SimpleUniverse u, Bounds b, BranchGroup root, TransformGroup tg) {
		mainPanel = f;
		graphics3D = g;
		globalAxis = n;
		simpleUniverse = u;
		bounds = b;
		baseBranchGroup = root;
		baseTransform = tg;
		viewTransformGroup = u.getViewingPlatform().getViewPlatformTransform();
		setSchedulingBounds(b);
		// Initialize the System Rotate Behavior
		systemRotate = new MouseRotate(MouseRotate.MANUAL_WAKEUP,
				viewTransformGroup, this, ROTATEPOST, BEHAVIORDONEPOST);
		systemRotate.setFactor(0.025);
		systemRotate.setSchedulingBounds(bounds);
		baseBranchGroup.addChild(systemRotate);
		// Initialize the System Translate Behavior
		systemTranslate = new MouseTranslate(MouseTranslate.MANUAL_WAKEUP,
				viewTransformGroup, this, TRANSLATEPOST, BEHAVIORDONEPOST);
		systemTranslate.setFactor(0.5);
		systemTranslate.setSchedulingBounds(bounds);
		baseBranchGroup.addChild(systemTranslate);
		// Initialize the globalZoom Behavior
		globalZoom = new MouseZoom(MouseZoom.MANUAL_WAKEUP, viewTransformGroup,
				this, ZOOMPOST, BEHAVIORDONEPOST);
		globalZoom.setFactor(0.0005);
		globalZoom.setSchedulingBounds(bounds);
		globalZoom.setTransformGroup(baseTransform);
		baseBranchGroup.addChild(globalZoom);
		// Initialize the viewOrbitBehavior
		viewOrbitBehavior = new GlobalBehavior(graphics3D);
		viewOrbitBehavior.setUpCallback(globalAxis);
		viewOrbitBehavior.setSchedulingBounds(bounds);
		u.getViewingPlatform().setViewPlatformBehavior(viewOrbitBehavior);
		// Initialize the PickCanvas
		pickCanvas = new PickCanvas(graphics3D, simpleUniverse.getLocale());
		pickCanvas.setMode(PickCanvas.GEOMETRY);
		pickCanvas.setTolerance(20.0f);
	}

	public void centerView(boolean resetRotation, boolean resetTranslation,
			boolean resetZoom) {
		viewOrbitBehavior.centerView(resetRotation, resetTranslation);
	}

	private boolean globalZoom() {
		postId(ZOOMPOST);
		return true;
	}

	public void initialize() {
		WakeupCriterion[] behaviorPost = new WakeupCriterion[3];
		behaviorPost[0] = new WakeupOnBehaviorPost(systemRotate,
				BEHAVIORDONEPOST);
		behaviorPost[1] = new WakeupOnBehaviorPost(systemTranslate,
				BEHAVIORDONEPOST);
		behaviorPost[2] = new WakeupOnBehaviorPost(globalZoom, BEHAVIORDONEPOST);
		postCriterion = new WakeupOr(behaviorPost);
		WakeupCriterion awtCriterion[] = new WakeupCriterion[2];
		awtCriterion[0] = new WakeupOnAWTEvent(Event.MOUSE_DOWN);
		awtCriterion[1] = new WakeupOnAWTEvent(Event.MOUSE_UP);
		mouseCriterion = new WakeupOr(awtCriterion);		
		wakeupOn(mouseCriterion);
	}

	public void processMouseEvent(MouseEvent evt) {
		buttonPress = false;
		leftButton = false;
		middleButton = false;
		rightButton = false;
		int mod = evt.getModifiersEx();
		
		if (evt.getID() == MouseEvent.MOUSE_PRESSED) {
			buttonPress = true;
		}
		// Left Button
		if ((mod & MouseEvent.BUTTON1_DOWN_MASK) == MouseEvent.BUTTON1_DOWN_MASK) {
			leftButton = true;
		}
		// Middle Button
		if ((mod & MouseEvent.BUTTON2_DOWN_MASK) == MouseEvent.BUTTON2_DOWN_MASK) {
			middleButton = true;
		}
		// Alternatively, map "alt + button1" to the middle button
		if ((mod & MouseEvent.ALT_DOWN_MASK) == MouseEvent.ALT_DOWN_MASK) {
			if (leftButton) {
				middleButton = true;
				leftButton = false;
			}
		}
		// Right Button
		if ((mod & MouseEvent.BUTTON3_DOWN_MASK) == MouseEvent.BUTTON3_DOWN_MASK) {
			rightButton = true;
		}
		// Alternatively, map "shift + button1" to the right button
		if ((mod & MouseEvent.SHIFT_DOWN_MASK) == MouseEvent.SHIFT_DOWN_MASK) {
			if (leftButton) {
				rightButton = true;
				leftButton = false;
			}
		}
		
		x = evt.getX();
		y = evt.getY();
		atom = null;
		axisSelected = false;
		if (buttonPress) {
			// Picking Results
			pickCanvas.setShapeLocation(x, y);
			// Once in a while "pickClosest" throws an exception due to
			// not being able to invert a matrix??
			// Catch and ignore this until a fix is determined...
			try {
				pickResult = pickCanvas.pickClosest();
			} catch (Exception e) {
				pickResult = null;
			}
			if (pickResult != null) {
				SceneGraphPath sgp = pickResult.getSceneGraphPath();
				Node node = sgp.getObject();
				if (node instanceof Shape3D) {
					Shape3D s = (Shape3D) node;
					Object o = s.getUserData();
					if (o instanceof MolecularAssembly) {
						MolecularAssembly sys = (MolecularAssembly) o;
						if (pickResult.numIntersections() > 0) {
							PickIntersection pi = pickResult.getIntersection(0);
							int coords[] = pi.getPrimitiveCoordinateIndices();
							atom = sys.getAtomFromWireVertex(coords[0]);
						}
					} else if (o instanceof Atom) {
						atom = (Atom) o;
					} else if (o instanceof GraphicsAxis) {
						axisSelected = true;
					}
				}
			}
		}
	}

	
	// Most of the logic for mouse interaction with the Scenegraph is here.
	public void processStimulus(Iterator criteria) {
		viewOrbitBehavior.setEnable(false);
		AWTEvent awtEvents[] = null;
		while (criteria.hasNext()) {
			WakeupCriterion wakeup = (WakeupCriterion) criteria.next();
			if (wakeup instanceof WakeupOnAWTEvent) {
				awtEvents = ((WakeupOnAWTEvent) wakeup).getAWTEvent();
				if (awtEvents == null) {
					continue;
				}
				for (int i = 0; i < awtEvents.length; i++) {
					MouseEvent mouseEvent = null;
					if (awtEvents[i] instanceof MouseEvent) {
						mouseEvent = (MouseEvent) awtEvents[i];
						processMouseEvent(mouseEvent);
					} else {
						continue;
					}
					if (!axisSelected) {
						// Wake Up System Translate Behavior
						if (rightButton && buttonPress){
							systemTranslate.setMouseButton(MouseEvent.BUTTON3_DOWN_MASK);
							if (systemTranslate()) {
								wakeupOn(postCriterion);
								return;
							}
						}
						// Wake Up Left Button Mode
						if (leftButton && buttonPress) {
							LeftButtonMode leftButtonMode = graphics3D.getLeftButtonMode();
							switch (leftButtonMode) {
							case ROTATE:
								if (systemRotate()) {
									wakeupOn(postCriterion);
									return;
								}
								break;
							case TRANSLATE:
								systemTranslate.setMouseButton(MouseEvent.BUTTON1_DOWN_MASK);
								if (systemTranslate()) {
									wakeupOn(postCriterion);
									return;
								}
								break;
							case ZOOM:
								globalZoom.setMouseButton(MouseEvent.BUTTON1_DOWN_MASK);
								if (globalZoom()) {
									wakeupOn(postCriterion);
									return;
								}
							}
						}							
						// Wake up Global Zoom Behavior
						if (middleButton && buttonPress){
							globalZoom.setMouseButton(MouseEvent.BUTTON2_DOWN_MASK);
							if (globalZoom()) {
								wakeupOn(postCriterion);
								return;
							}
						}
					} else {
						viewOrbitBehavior.setEnable(true);
						wakeupOn(mouseCriterion);
						return;
					}
				}
			}
		}
		wakeupOn(mouseCriterion);
	}

	public void setGlobalCenter(double d[]) {
		Point3d point = new Point3d(d);
		viewOrbitBehavior.setRotationCenter(point);
	}

	private boolean systemRotate() {
		TransformGroup tg = null;
		GraphicsCanvas.MouseMode mouseMode = graphics3D.getMouseMode();
		if ((mouseMode == GraphicsCanvas.MouseMode.SYSTEMBELOWMOUSE)
				&& atom != null) {
			tg = (TransformGroup) pickResult
					.getNode(PickResult.TRANSFORM_GROUP);
		} else if (mouseMode == GraphicsCanvas.MouseMode.ACTIVESYSTEM) {
			if (mainPanel.getHierarchy().getActive() != null) {
				tg = mainPanel.getHierarchy().getActive().getTransformGroup();
			}
		}
		if ((tg != null)
				&& (tg.getCapability(TransformGroup.ALLOW_TRANSFORM_READ))
				&& (tg.getCapability(TransformGroup.ALLOW_TRANSFORM_WRITE))) {
			systemRotate.setTransformGroup(tg);
			postId(ROTATEPOST);
			return true;
		}
		return false;
	}

	private boolean systemTranslate() {
		TransformGroup tg = null;
		GraphicsCanvas.MouseMode mouseMode = graphics3D.getMouseMode();
		if ((mouseMode == GraphicsCanvas.MouseMode.SYSTEMBELOWMOUSE)
				&& atom != null) {
			tg = (TransformGroup) pickResult
					.getNode(PickResult.TRANSFORM_GROUP);
		} else if (mouseMode == GraphicsCanvas.MouseMode.ACTIVESYSTEM) {
			if (mainPanel.getHierarchy().getActive() != null) {
				tg = mainPanel.getHierarchy().getActive().getTransformGroup();
			}
		}
		if ((tg != null)
				&& (tg.getCapability(TransformGroup.ALLOW_TRANSFORM_READ))
				&& (tg.getCapability(TransformGroup.ALLOW_TRANSFORM_WRITE))) {
			systemTranslate.setTransformGroup(tg);
			postId(TRANSLATEPOST);
			return true;
		}
		return false;
	}
}
