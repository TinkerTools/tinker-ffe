/*
 * <p>Title: Force Field Explorer</p>
 * <p>Description: Force Field Explorer Molecular Modeling Program</p>
 * <p>Copyright: Copyright (c) 2004-2019 Jay William Ponder</p>
 * <p>Institution: Jay Ponder Lab, Washington University in Saint Louis</p>
 * @author Michael J. Schnieders
 * @version 8.5
 */

package ffe.core;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Vector;
import java.util.logging.Logger;

import javax.swing.tree.TreePath;

import org.jogamp.java3d.Bounds;
import org.jogamp.java3d.BranchGroup;
import org.jogamp.java3d.Node;
import org.jogamp.java3d.SceneGraphPath;
import org.jogamp.java3d.Shape3D;
import org.jogamp.java3d.Transform3D;
import org.jogamp.java3d.utils.picking.PickCanvas;
import org.jogamp.java3d.utils.picking.PickIntersection;
import org.jogamp.java3d.utils.picking.PickResult;
import org.jogamp.vecmath.Vector3d;

import ffe.behaviors.PickMouseBehavior;
import ffe.lang.Atom;
import ffe.lang.MSNode;
import ffe.lang.MolecularAssembly;
import ffe.lang.Molecule;
import ffe.lang.Polymer;
import ffe.lang.RendererCache;
import ffe.lang.Residue;
import ffe.lang.ValenceTerm;

/*
 * The GraphicsPicking class is used to make selections and measurements.
 * 
 * @todo Switch to Java3D PickFast Behaviors.
 */
public class GraphicsPicking extends PickMouseBehavior {
	public enum PickLevel {
		PICKATOM, PICKBOND, PICKANGLE, PICKDIHEDRAL, PICKRESIDUE, PICKMOLECULE, PICKPOLYMER, PICKSYSTEM, MEASUREDISTANCE, MEASUREANGLE, MEASUREDIHEDRAL;
	}

	public static final Hashtable<String, PickLevel> pickLevelHash = new Hashtable<String, PickLevel>();

	static {
		PickLevel values[] = PickLevel.values();
		for (PickLevel value : values) {
			pickLevelHash.put(value.toString(), value);
		}
	}

	private MainPanel mainPanel;

	// Turn On/Off picking
	private boolean picking = false;

	private static Logger logger = Logger.getLogger("ffe");

	// Picking Level
	private PickLevel pickLevel = PickLevel.PICKATOM;

	private PickLevel newPickLevel = PickLevel.PICKATOM;

	// Previously picked Atom
	private Atom previousAtom = null;

	// Number of times the previousAtom has been picked consecutively
	private int pickNumber = 0;

	// Previously picked MSNode
	private MSNode previousPick = null;

	// Selected Atoms for Measuring
	private Vector<Atom> atomCache = new Vector<Atom>(4);
	private int count = 0;

	/* A few static variables for reuse */
	private double a[] = new double[3];

	private double b[] = new double[3];

	private double c[] = new double[3];

	private double d[] = new double[3];

	private Transform3D systemTransform3D = new Transform3D();

	private Vector3d syspos = new Vector3d();

	private Vector3d atpos = new Vector3d();

	/*
	 * Constructor
	 * 
	 * @param base
	 *            Base of the Scenegraph
	 * @param bounds
	 *            Behavior bounds
	 * @param g
	 *            Scene Canvas3D
	 * @param f
	 *            MainPanel
	 */
	public GraphicsPicking(BranchGroup base, Bounds bounds, GraphicsCanvas g,
			MainPanel f) {
		super(g, base, bounds);
		mainPanel = f;
		pickCanvas.setMode(PickCanvas.GEOMETRY);
		pickCanvas.setTolerance(3.0f);
	}

	/*
	 * Clear currently selected nodes
	 */
	public void clear() {
		if (previousPick != null) {
			mainPanel.getHierarchy().collapsePath(
					new TreePath(previousPick.getPath()));
			previousPick.setSelected(false);
			previousPick.setColor(RendererCache.ColorModel.SELECT, null, null);
			previousPick = null;
			pickNumber = 0;
		}
		for (Atom a : atomCache) {
			a.setSelected(false);
			a.setColor(RendererCache.ColorModel.SELECT, null, null);
		}
		atomCache.clear();
	}

	private void distance(Atom atom, double pos[]) {
		MolecularAssembly m = (MolecularAssembly) atom
				.getMSNode(MolecularAssembly.class);
		m.getTransformGroup().getTransform(systemTransform3D);
		systemTransform3D.get(syspos);
		systemTransform3D.setScale(1.0d);
		systemTransform3D.setTranslation(new Vector3d(0, 0, 0));
		atom.getV3D(atpos);
		systemTransform3D.transform(atpos);
		atpos.add(syspos);
		atpos.get(pos);
	}

	public MSNode getPick() {
		return previousPick;
	}

	public boolean getPicking() {
		return picking;
	}

	public String getPickLevel() {
		return pickLevel.toString();
	}

	/* 
	 * @param measureLevel
	 * @param atoms
	 */
	private void measure() {
		String measurement = null;
		double value = 0.0;
		Atom a1, a2, a3, a4;
		switch (pickLevel) {
		case MEASUREDISTANCE:
			if (atomCache.size() < 2) {
				return;
			}
			a1 = atomCache.get(0);
			a2 = atomCache.get(1);
			distance(a1, a);
			distance(a2, b);
			value = ffe.lang.VectorMath.dist(a, b);
			measurement = "\nDistance\t" + a1.xyzindex + ", " + a2.xyzindex
					+ ":   \t" + String.format("%10.5f", value);
			break;
		case MEASUREANGLE:
			if (atomCache.size() < 3) {
				return;
			}
			a1 = atomCache.get(0);
			a2 = atomCache.get(1);
			a3 = atomCache.get(2);
			distance(a1, a);
			distance(a2, b);
			distance(a3, c);
			value = ffe.lang.VectorMath.bondAngle(a, b, c);
			value = Math.toDegrees(value);
			measurement = "\nAngle\t" + a1.xyzindex + ", " + a2.xyzindex + ", "
					+ a3.xyzindex + ":   \t" + String.format("%10.5f", value);
			break;
		case MEASUREDIHEDRAL:
			if (atomCache.size() < 4) {
				return;
			}
			a1 = atomCache.get(0);
			a2 = atomCache.get(1);
			a3 = atomCache.get(2);
			a4 = atomCache.get(3);
			distance(a1, a);
			distance(a2, b);
			distance(a3, c);
			distance(a4, d);
			value = ffe.lang.VectorMath.dihedralAngle(a, b, c, d);
			value = Math.toDegrees(value);
			measurement = "\nDihedral\t" + a1.xyzindex + ", " + a2.xyzindex
					+ ", " + a3.xyzindex + ", " + a4.xyzindex + ":\t" + String.format("%10.5f", value);
			break;
		default:
			return;
		}
		logger.info(measurement);
		ModelingShell modelingShell = mainPanel.getModelingShell();
		modelingShell.setMeasurement(measurement, value);
		count = 0;
	}

	public void resetCount() {
		count = 0;
	}

	public void setPicking(boolean m) {
		picking = m;
		if (picking == false) {
			clear();
		}
	}

	public void setPickLevel(String newPick) {
		if (pickLevelHash.containsKey(newPick.toUpperCase())) {
			newPickLevel = pickLevelHash.get(newPick.toUpperCase());
		}
	}

	/*
	 * Called by Java3D when an atom is picked
	 * 
	 * @param xpos
	 *            Horizontal mouse position
	 * @param ypos
	 *            Vertical mouse position
	 */
	public void updateScene(int xpos, int ypos) {
		if (picking == false) {
			return;
		}
		// Determine what FNode was picked
		pickCanvas.setShapeLocation(xpos, ypos);
		PickResult result = pickCanvas.pickClosest();
		if (result != null) {
			SceneGraphPath sceneGraphPath = result.getSceneGraphPath();
			Node node = sceneGraphPath.getObject();
			if (!(node instanceof Shape3D)) {
				return;
			}
			Shape3D pickedShape3D = (Shape3D) node;
			Object userData = pickedShape3D.getUserData();
			if (userData instanceof MolecularAssembly) {
				FFESystem sys = (FFESystem) userData;
				if (result.numIntersections() > 0) {
					PickIntersection pickIntersection = result
							.getIntersection(0);
					int coords[] = pickIntersection
							.getPrimitiveCoordinateIndices();
					userData = sys.getAtomFromWireVertex(coords[0]);
				} else {
					return;
				}
			}
			if (userData instanceof Atom) {
				Atom a = (Atom) userData;
				// Check to see if the pickLevel has changed
				if (!(pickLevel == newPickLevel)) {
					pickLevel = newPickLevel;
					pickNumber = 0;
				}
				// Clear selections between measurements
				String pickLevelString = pickLevel.toString();
				boolean measure = pickLevelString.startsWith("MEASURE");
				if (!measure || count == 0) {
					for (Atom matom : atomCache) {
						matom.setSelected(false);
						matom.setColor(RendererCache.ColorModel.SELECT, null,
								null);
					}
					atomCache.clear();
					count = 0;
				}
				// If measuring, select the current atom and add it to the cache
				if (measure && !atomCache.contains(a)) {
					atomCache.add(0, a);
					a.setSelected(true);
					a.setColor(RendererCache.ColorModel.PICK, null, null);
					count++;
					measure();
				}
				if (!measure) {
					// Check to see if the same Atom has been selected twice in
					// a row
					// This allows iteration through the atom's terms
					if (a == previousAtom) {
						pickNumber++;
					} else {
						previousAtom = a;
						pickNumber = 0;
					}
					MSNode currentPick = null;
					switch (pickLevel) {
					case PICKATOM:
						currentPick = a;
						break;
					case PICKBOND:
					case PICKANGLE:
					case PICKDIHEDRAL:
						ArrayList terms = null;
						if (pickLevel == PickLevel.PICKBOND) {
							terms = a.getBonds();
						} else if (pickLevel == PickLevel.PICKANGLE) {
							terms = a.getAngles();
						} else if (pickLevel == PickLevel.PICKDIHEDRAL) {
							terms = a.getDihedrals();
						}
						if (terms == null) {
							return;
						}
						int num = terms.size();
						if (pickNumber >= num) {
							pickNumber = 0;
						}
						currentPick = (ValenceTerm) terms.get(pickNumber);
						break;
					case PICKRESIDUE:
					case PICKPOLYMER:
					case PICKMOLECULE:
					case PICKSYSTEM:
						MSNode dataNode = null;
						if (pickLevel == PickLevel.PICKRESIDUE) {
							dataNode = (MSNode) a.getMSNode(Residue.class);
						} else if (pickLevel == PickLevel.PICKPOLYMER) {
							dataNode = (MSNode) a.getMSNode(Polymer.class);
						} else if (pickLevel == PickLevel.PICKSYSTEM) {
							dataNode = (MSNode) a
									.getMSNode(MolecularAssembly.class);
						} else if (pickLevel == PickLevel.PICKMOLECULE) {
							dataNode = (MSNode) a.getMSNode(Molecule.class);
							if (dataNode == null) {
								dataNode = (MSNode) a.getMSNode(Polymer.class);
							}
						}
						currentPick = dataNode;
						break;
					case MEASUREANGLE:
					case MEASUREDIHEDRAL:
					case MEASUREDISTANCE:
						break;
					}
					// Add the selected node to the Tree View
					if (currentPick != null) {
						if (controlButton) {
							mainPanel.getHierarchy().toggleSelection(
									currentPick);
						} else if (currentPick != previousPick) {
							mainPanel.getHierarchy().onlySelection(currentPick);
						}
						// Color the Current Pick by Picking Color
						mainPanel.getGraphics3D().updateScene(currentPick,
								false, false, null, true,
								RendererCache.ColorModel.PICK);
					}
					// Remove picking color from the previousPick
					if (previousPick != null && previousPick != currentPick) {
						previousPick.setColor(RendererCache.ColorModel.REVERT,
								null, null);
					}
					previousPick = currentPick;
				}
			}
		}
	}
}
