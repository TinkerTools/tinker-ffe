/*
 * <p>Title: Force Field Explorer</p>
 * <p>Description: Force Field Explorer Molecular Modeling Program</p>
 * <p>Copyright: Copyright (c) 2004-2019 Jay William Ponder</p>
 * <p>Institution: Jay Ponder Lab, Washington University in Saint Louis</p>
 * @author Michael J. Schnieders
 * @version 8.7
 */

package ffe.lang;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.logging.Logger;

import org.jogamp.java3d.Appearance;
import org.jogamp.java3d.BranchGroup;
import org.jogamp.java3d.Canvas3D;
import org.jogamp.java3d.ColoringAttributes;
import org.jogamp.java3d.Geometry;
import org.jogamp.java3d.GeometryArray;
import org.jogamp.java3d.LineAttributes;
import org.jogamp.java3d.Material;
import org.jogamp.java3d.Node;
import org.jogamp.java3d.PointAttributes;
import org.jogamp.java3d.PolygonAttributes;
import org.jogamp.java3d.RenderingAttributes;
import org.jogamp.java3d.ShaderAppearance;
import org.jogamp.java3d.ShaderProgram;
import org.jogamp.java3d.Shape3D;
import org.jogamp.java3d.Transform3D;
import org.jogamp.java3d.TransformGroup;
import org.jogamp.java3d.TransparencyAttributes;
import org.jogamp.java3d.utils.geometry.Cone;
import org.jogamp.java3d.utils.geometry.Cylinder;
import org.jogamp.java3d.utils.geometry.Sphere;
import org.jogamp.vecmath.Color3f;
import org.jogamp.vecmath.Point2d;
import org.jogamp.vecmath.Point3d;
import org.jogamp.vecmath.Vector3d;

/*
 * The RendererCache class defines constants related to rendering
 * modes and caches primitives for the Renderer.
 */
public class RendererCache {
	
	private static Logger logger = Logger.getLogger("ffe");
	
	public enum ColorModel {
		CPK, GROUP, POLYMER, RESIDUE, MOLECULE, PICK, SELECT, REVERT, PARTIALCHARGE, STRUCTURE, VECTORMAGNITUDE, USERCOLOR, APPLYUSERCOLOR, MONOCHROME
	}

	public enum ViewModel {
		WIREFRAME, BALLANDSTICK, SPACEFILL, RMIN, TUBE, RIBBON, INVISIBLE, RESTRICT, DETAIL, SHOWHYDROGENS, HIDEHYDROGENS, INDUCEDDIPOLE, FORCE, VELOCITY, ACCELERATION, HIDEVECTORS, UNIT, RELATIVE, ABSOLUTE, POINTS, LINES, FILL, DESTROY
	}

	// Scene Attributes
	public static final LineAttributes lineAttributes = new LineAttributes();

	public static final PointAttributes pointAttributes = new PointAttributes();

	public static final ColoringAttributes coloringAttributes = new ColoringAttributes();

	public static final RenderingAttributes renderingAttributes = new RenderingAttributes();

	public static final TransparencyAttributes transparencyAttributes = new TransparencyAttributes();

	public static final PolygonAttributes fillPolygonAttributes = new PolygonAttributes();

	public static final PolygonAttributes pointPolygonAttributes = new PolygonAttributes();

	public static final PolygonAttributes linePolygonAttributes = new PolygonAttributes();

	// Create colors that will be used frequently
	public static final Color3f ORANGE = new Color3f(Color.orange);

	public static final Color3f RED = new Color3f(Color.red);

	public static final Color3f BLUE = new Color3f(Color.blue);

	public static final Color3f GRAY = new Color3f(Color.lightGray);

	public static final Color3f YELLOW = new Color3f(Color.yellow);

	public static final Color3f CYAN = new Color3f(Color.cyan);

	public static final Color3f GREEN = new Color3f(Color.green);

	public static final Color3f WHITE = new Color3f(Color.white);

	public static final Color3f PINK = new Color3f(Color.pink);

	public static final Color3f MAGENTA = new Color3f(Color.magenta);

	public static final Color3f BLACK = new Color3f(Color.black);

	public static final Color3f NULLColor = new Color3f(Color.darkGray);

	public static final Color bgColor = Color.black;

	// Some default values for the 3D Universe
	public static ColorModel colorModel = ColorModel.CPK;

	public static ViewModel viewModel = ViewModel.WIREFRAME;

	public static int detail = 3;

	public static double radius = 1.0d;

	public static double bondRadius = 1.0d;

	public static double vectorScale = 1.8d;

	public static int bondwidth = 3;

	public static boolean highlightSelections = false;

	public static boolean labelAtoms = false;

	public static boolean labelResidues = false;

	public static Color3f pickingColor = MAGENTA;
	public static Color3f selectionColor = YELLOW;

	public static Color3f userColor = WHITE;

	public static final Hashtable<String, ViewModel> viewModelHash = new Hashtable<String, ViewModel>();

	public static final Hashtable<String, ColorModel> colorModelHash = new Hashtable<String, ColorModel>();

	/*
	 * Viewing Models - These strings need to be coordinated with the Locale
	 * keys such that the following would return true.
	 * spacefillLocaleKey.equalsIgnoreCase(ViewModel.SPACEFILL)
	 */
	private static final Transform3D localToVworld = new Transform3D();

	private static final Transform3D worldToImagePlate = new Transform3D();
	private static final Hashtable<Color3f, Material> materials = new Hashtable<Color3f, Material>();

	/*
	 * Color Models - These strings need to be coordinated with the Locale keys
	 * so that the following would return true.
	 * 
	 * cpkLocaleKey.equalsIgnoreCase(ColorModel.CPK)
	 */

	private static List<Transform3D> transform3DPool = Collections
			.synchronizedList(new ArrayList<Transform3D>());

	protected static List<BranchGroup> spherePool = Collections
			.synchronizedList(new ArrayList<BranchGroup>());
	private static List<BranchGroup> doubleCylinderPool = Collections
			.synchronizedList(new ArrayList<BranchGroup>());

	private static final Geometry sphereGeom[] = new Geometry[11];

	private static final Geometry cylgeom[][] = new Geometry[3][11];

	private static final Geometry conegeom[][] = new Geometry[2][4];

	static private final Hashtable<Color3f, Appearance> pointAppearances = new Hashtable<Color3f, Appearance>();

	static private final Hashtable<Color3f, Appearance> lineAppearances = new Hashtable<Color3f, Appearance>();

	static private final Hashtable<Color3f, Appearance> fillAppearances = new Hashtable<Color3f, Appearance>();

	private static ShaderProgram shaderProgram = null;

	private static final Color3f negCharge[] = new Color3f[1000];

	private static final Color3f posCharge[] = new Color3f[1000];

	// For hiding live, but recycled Java3D Nodes
	static public final Appearance nullAp;

	static {
		coloringAttributes.setShadeModel(ColoringAttributes.NICEST);
		coloringAttributes.setColor(new Color3f(0, 0, 0));
		lineAttributes.setLineAntialiasingEnable(true);
		lineAttributes.setLinePattern(LineAttributes.PATTERN_SOLID);
		lineAttributes.setLineWidth(1.0f);
		pointAttributes.setPointAntialiasingEnable(true);
		pointAttributes.setPointSize(1.0f);
		fillPolygonAttributes.setPolygonMode(PolygonAttributes.POLYGON_FILL);
		fillPolygonAttributes.setCullFace(PolygonAttributes.CULL_BACK);
		linePolygonAttributes.setPolygonMode(PolygonAttributes.POLYGON_LINE);
		pointPolygonAttributes.setPolygonMode(PolygonAttributes.POLYGON_POINT);
		renderingAttributes.setVisible(true);
		renderingAttributes.setDepthBufferEnable(true);
		renderingAttributes.setDepthBufferWriteEnable(true);
		renderingAttributes.setIgnoreVertexColors(true);
		transparencyAttributes.setTransparencyMode(TransparencyAttributes.NONE);
	}

	static {
		ViewModel values[] = ViewModel.values();
		for (ViewModel value : values) {
			viewModelHash.put(value.toString(), value);
		}
	}

	static {
		ColorModel values[] = ColorModel.values();
		for (ColorModel value : values) {
			colorModelHash.put(value.toString(), value);
		}
	}

	static {
		nullAp = new Appearance();
		RenderingAttributes ra = new RenderingAttributes();
		ra.setVisible(false);
		nullAp.setRenderingAttributes(ra);
	}

	protected static Appearance appearanceFactory(Color3f col,
			ViewModel polygonType) {
		if (col == null) {
			return null;
		}
		Appearance ap;
		if (polygonType == RendererCache.ViewModel.FILL) {
			ap = (Appearance) fillAppearances.get(col);
		} else if (polygonType == RendererCache.ViewModel.POINTS) {
			ap = (Appearance) pointAppearances.get(col);
		} else {
			ap = (Appearance) lineAppearances.get(col);
		}
		if (ap == null) {
			ap = createAppearance(col, polygonType);
		}
		return ap;
	}

	protected static Shape3D coneFactory(Appearance ap, int res) {
		if (res > 3) {
			res = 3;
		}
		Shape3D cone = new Shape3D();
		cone.setAppearance(ap);
		cone.setCapability(Shape3D.ALLOW_GEOMETRY_READ);
		cone.setCapability(Shape3D.ALLOW_GEOMETRY_WRITE);
		cone.setCapability(Shape3D.ALLOW_APPEARANCE_WRITE);
		cone.setCapability(Shape3D.ALLOW_PICKABLE_WRITE);
		cone.addGeometry(getConeGeom(0, res));
		cone.addGeometry(getConeGeom(1, res));
		return cone;
	}

	static private Appearance createAppearance(Color3f col,
			ViewModel polygonType) {
		Appearance ap = null;
		if (shaderProgram != null) {
			ShaderAppearance sap = new ShaderAppearance();
			sap.setShaderProgram(shaderProgram);
			ap = sap;
		}
		if (ap == null) {
			ap = new Appearance();
		}
		Material mat = materialFactory(col);
		ap.setMaterial(mat);
		ap.setRenderingAttributes(renderingAttributes);
		ap.setColoringAttributes(coloringAttributes);
		ap.setLineAttributes(lineAttributes);
		ap.setPointAttributes(pointAttributes);
		if (polygonType == RendererCache.ViewModel.FILL) {
			ap.setPolygonAttributes(fillPolygonAttributes);
			fillAppearances.put(col, ap);
		} else if (polygonType == RendererCache.ViewModel.POINTS) {
			ap.setPolygonAttributes(pointPolygonAttributes);
			pointAppearances.put(col, ap);
		} else {
			ap.setPolygonAttributes(linePolygonAttributes);
			lineAppearances.put(col, ap);
		}
		return ap;
	}

	/*
	 * This method creates a Cylinder
	 */
	protected static final Shape3D createCylinder(Appearance ap, int res) {
		if (res < 0) {
			res = 0;
		}
		if (res > 10) {
			res = 10;
		}
		final Shape3D cyl = new Shape3D();
		cyl.setAppearance(ap);
		cyl.addGeometry(getCylinderGeom(0, res));
		cyl.addGeometry(getCylinderGeom(1, res));
		cyl.addGeometry(getCylinderGeom(2, res));
		cyl.setCapability(Shape3D.ALLOW_GEOMETRY_READ);
		cyl.setCapability(Shape3D.ALLOW_GEOMETRY_WRITE);
		cyl.setCapability(Shape3D.ALLOW_APPEARANCE_WRITE);
		cyl.setCapability(Shape3D.ALLOW_APPEARANCE_READ);
		cyl.setCapability(Shape3D.ENABLE_PICK_REPORTING);
		cyl.setCapability(Shape3D.ALLOW_PICKABLE_WRITE);
		return cyl;
	}

	/*
	 * This method creates a single Sphere from the given appearance
	 */
	private static Shape3D createSphere(Appearance ap, int div) {
		Shape3D shape3d = new Shape3D();
		shape3d.setAppearance(ap);
		shape3d.addGeometry(getSphereGeom(div));
		shape3d.setCapability(Shape3D.ALLOW_GEOMETRY_READ);
		shape3d.setCapability(Shape3D.ALLOW_GEOMETRY_WRITE);
		shape3d.setCapability(Shape3D.ALLOW_APPEARANCE_WRITE);
		shape3d.setCapability(Shape3D.ALLOW_APPEARANCE_READ);
		shape3d.setCapability(Shape3D.ENABLE_PICK_REPORTING);
		shape3d.setCapability(Shape3D.ALLOW_PICKABLE_WRITE);
		return shape3d;
	}

	private static final TransformGroup createTransformGroup(
			Transform3D transform3D) {
		TransformGroup transformGroup;
		if (transform3D == null) {
			transformGroup = new TransformGroup();
		} else {
			transformGroup = new TransformGroup(transform3D);
		}
		transformGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
		transformGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		transformGroup.setCapability(TransformGroup.ALLOW_CHILDREN_READ);
		return transformGroup;
	}

	protected static final BranchGroup doubleCylinderFactory(Atom a1, Atom a2,
			int div) {
		BranchGroup branchGroup;
		if (doubleCylinderPool.size() > 0) {
			branchGroup = doubleCylinderPool.remove(0);
			if (branchGroup != null) {
				TransformGroup cy1tg = (TransformGroup) branchGroup.getChild(0);
				Shape3D cy1 = (Shape3D) cy1tg.getChild(0);
				cy1.setAppearance(a1.getAtomAppearance());
				cy1.setUserData(a1);
				TransformGroup cy2tg = (TransformGroup) branchGroup.getChild(1);
				Shape3D cy2 = (Shape3D) cy2tg.getChild(0);
				cy2.setUserData(a2);
				cy2.setAppearance(a2.getAtomAppearance());
				return branchGroup;
			}
		}
		branchGroup = new BranchGroup();
		branchGroup.setCapability(BranchGroup.ALLOW_DETACH);
		branchGroup.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
		branchGroup.setCapability(BranchGroup.ENABLE_PICK_REPORTING);
		TransformGroup cy1tg = createTransformGroup(null);
		Shape3D cy1 = createCylinder(a1.getAtomAppearance(), div);
		cy1.setUserData(a1);
		cy1tg.addChild(cy1);
		branchGroup.addChild(cy1tg);
		TransformGroup cy2tg = createTransformGroup(null);
		Shape3D cy2 = createCylinder(a2.getAtomAppearance(), div);
		cy2.setUserData(a2);
		cy2tg.addChild(cy2);
		branchGroup.addChild(cy2tg);
		branchGroup.compile();
		return branchGroup;
	}

	protected static final Color3f getColor(Atom a, ColorModel mode) {
		switch (mode) {
		case CPK:
			return Atom.AtomColor.get(a.getAtomicNumber());
		case PICK:
			return pickingColor;
		case SELECT:
			return selectionColor;
		case PARTIALCHARGE:
			int index = 0;
			double charge = a.getCharge();
			if (charge < 0.0d) {
				float c = (float) (charge * 1000.0);
				index = -1 * Math.round(c);
				if (index > 999) {
					index = 999;
				}
				if (negCharge[index] == null) {
					float value = index * 0.001f;
					negCharge[index] = new Color3f(1.0f, 1.0f - value,
							1.0f - value);
				}
				return negCharge[index];
			} else if (charge == 0) {
				return WHITE;
			} else {
				float c = (float) (charge * 1000.0);
				index = Math.round(c);
				if (index > 999) {
					index = 999;
				}
				if (posCharge[index] == null) {
					float value = index * 0.001f;
					posCharge[index] = new Color3f(1.0f - value, 1.0f - value,
							1.0f);
				}
				return posCharge[index];
			}
		case VECTORMAGNITUDE:
			index = 0;
			double mag = a.getRelativeLength();
			if (mag > 0) {
				index = (int) Math.round(mag * 1000.0);
				if (index < 0) {
					logger.info(a + " " + mag);
					return WHITE;
				}
				if (index > 999) {
					index = 999;
				}
				if (negCharge[index] == null) {
					float value = index * 0.001f;
					negCharge[index] = new Color3f(1.0f, 1.0f - value,
							1.0f - value);
				}
				return negCharge[index];
			}
			return NULLColor;
		default:
			return NULLColor;
		}
	}

	protected static Geometry getConeGeom(int num, int res) {
		if (res > 3) {
			res = 3;
		}
		if (conegeom[num][res] == null) {
			initConeGeom(res);
		}
		return conegeom[num][res];
	}

	protected static final Geometry getCylinderGeom(int num, int res) {
		if (res < 0) {
			res = 0;
		}
		if (res > 10) {
			res = 10;
		}
		if (cylgeom[num][res] == null) {
			initCylinderGeom(res);
		}
		return cylgeom[num][res];
	}

	protected static final Geometry getPolarGeom(int res) {
		return getSphereGeom(res);
	}

	protected static void getScreenCoordinate(Canvas3D canvas, Node node,
			Point3d point3d, final Point2d point) {
		if (point == null) {
			return;
		}
		// Get the transform to put the node in the world coordinate system
		node.getLocalToVworld(localToVworld);
		// Get the image plate transform
		canvas.getVworldToImagePlate(worldToImagePlate);
		// Transform into world coordinates
		localToVworld.transform(point3d);
		// Transform into imageplate coordinates
		worldToImagePlate.transform(point3d);
		// Final step to the 2D Screen.
		canvas.getPixelLocationFromImagePlate(point3d, point);
		/*
		 * Now we have the location where the point will be rendered on the
		 * screen depending on resize, placement, size, and eye point policies.
		 * This should only be called on points that reside within the clipping
		 * planes.
		 */
	}

	protected static final Geometry getSphereGeom(int res) {
		if (res < 0) {
			res = 0;
		}
		if (res > 10) {
			res = 10;
		}
		if (sphereGeom[res] == null) {
			initSphereGeom(res);
		}
		return sphereGeom[res];
	}

	private static void initConeGeom(int res) {
		Cone cone = new Cone(1.0f, 1.0f, Cone.GENERATE_NORMALS
				| Cone.ENABLE_GEOMETRY_PICKING | Cone.ENABLE_APPEARANCE_MODIFY,
				(res + 1) * 4, 1, nullAp);
		for (int i = 0; i < 2; i++) {
			conegeom[i][res] = cone.getShape(i).getGeometry();
			/*
			 * conegeom[i][res].setCapability(Geometry.ALLOW_INTERSECT);
			 * conegeom[i][res].setCapability(GeometryArray.ALLOW_FORMAT_READ);
			 * conegeom[i][res].setCapability(GeometryArray.ALLOW_COUNT_READ);
			 * conegeom[i][res].setCapability(GeometryArray.ALLOW_COORDINATE_READ);
			 */
		}
	}

	private static void initCylinderGeom(int res) {
		Appearance ap = new Appearance();
		Cylinder cyl = new Cylinder(1.0f, 1.0f, Cylinder.GENERATE_NORMALS
				| Cylinder.ENABLE_APPEARANCE_MODIFY
				| Cylinder.ENABLE_GEOMETRY_PICKING, 2 + res, 1, ap);
		for (int i = 0; i < 3; i++) {
			cylgeom[i][res] = cyl.getShape(i).getGeometry();
			try {
				cylgeom[i][res].setCapability(Geometry.ALLOW_INTERSECT);
				cylgeom[i][res].setCapability(GeometryArray.ALLOW_FORMAT_READ);
				cylgeom[i][res].setCapability(GeometryArray.ALLOW_COUNT_READ);
				cylgeom[i][res]
						.setCapability(GeometryArray.ALLOW_COORDINATE_READ);
			} catch (Exception e) {
				return;
			}
		}
	}

	private static void initSphereGeom(int res) {
		Appearance ap = new Appearance();
		Sphere sphere;
		sphere = new Sphere(1.0f, Sphere.GENERATE_NORMALS
				| Sphere.ENABLE_APPEARANCE_MODIFY
				| Sphere.ENABLE_GEOMETRY_PICKING, 4 + 3 * res, ap);
		sphereGeom[res] = sphere.getShape().getGeometry();
		// GeometryArray g = (GeometryArray) sphereGeom[res];
		/*
		 * if (!g.isLive()) { g.setCapability(g.ALLOW_FORMAT_READ);
		 * g.setCapability(g.ALLOW_COUNT_READ);
		 * g.setCapability(g.ALLOW_COORDINATE_READ); }
		 */
	}

	static protected Material materialFactory(Color3f col) {
		if (col == null) {
			return null;
		}
		Material mat = (Material) materials.get(col);
		if (mat == null) {
			mat = new Material(col, BLACK, col, WHITE, 75.0f);
			mat.setLightingEnable(true);
			materials.put(col, mat);
		}
		return mat;
	}

	// A pool of TransformGroups with one child, a sphere.
	protected static void poolDoubleCylinder(BranchGroup branchGroup) {
		if (branchGroup != null) {
			doubleCylinderPool.add(branchGroup);
		}
	}

	// A pool of TransformGroups with one child, a sphere.
	protected static void poolSphere(BranchGroup tg) {
		if (tg != null) {
			spherePool.add(tg);
		}
	}

	protected static void poolTransform3D(Transform3D transform3D) {
		if (transform3D != null) {
			transform3DPool.add(transform3D);
		}
	}

	protected static final BranchGroup sphereFactory(Appearance ap, int div,
			Transform3D transform3D) {
		BranchGroup branchGroup;
		if (spherePool.size() > 0) {
			branchGroup = spherePool.remove(0);
			if (branchGroup != null) {
				TransformGroup transformGroup = (TransformGroup) branchGroup
						.getChild(0);
				transformGroup.setTransform(transform3D);
				Shape3D sphere = (Shape3D) transformGroup.getChild(0);
				sphere.setAppearance(ap);
				return branchGroup;
			}
		}
		branchGroup = new BranchGroup();
		branchGroup.setCapability(BranchGroup.ALLOW_DETACH);
		branchGroup.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
		branchGroup.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
		TransformGroup transformGroup = createTransformGroup(transform3D);
		Shape3D sphere = createSphere(ap, div);
		transformGroup.addChild(sphere);
		branchGroup.addChild(transformGroup);
		branchGroup.compile();
		return branchGroup;
	}

	protected static final Color3f toAtomColor(String s) {
		String c = s.toLowerCase();
		if (c.startsWith("h")) {
			return WHITE;
		}
		if (c.startsWith("c")) {
			return GRAY;
		}
		if (c.startsWith("n")) {
			return BLUE;
		}
		if (c.startsWith("o")) {
			return RED;
		}
		if (c.startsWith("p")) {
			return GREEN;
		}
		if (c.startsWith("s")) {
			return YELLOW;
		}
		return NULLColor;
	}

	/*
	 * static { try { String vertexProgram =
	 * StringIO.readFully(ClassLoader.getSystemResource("ffe/lang/shader.vert"));
	 * String fragmentProgram =
	 * StringIO.readFully(ClassLoader.getSystemResource("ffe/lang/shader.frag"));
	 * Shader[] shaders = new Shader[2]; shaders[0] = new
	 * SourceCodeShader(Shader.SHADING_LANGUAGE_GLSL, Shader.SHADER_TYPE_VERTEX,
	 * vertexProgram); shaders[1] = new
	 * SourceCodeShader(Shader.SHADING_LANGUAGE_GLSL,
	 * Shader.SHADER_TYPE_FRAGMENT, fragmentProgram); shaderProgram = new
	 * GLSLShaderProgram(); shaderProgram.setShaders(shaders); } catch
	 * (Exception e){ e.printStackTrace(); shaderProgram = null; } }
	 */

	protected static final Transform3D transform3DFactory() {
		Transform3D transform3D;
		if (transform3DPool.size() > 0) {
			transform3D = transform3DPool.get(0);
			if (transform3D != null) {
				return transform3D;
			}
		}
		transform3D = new Transform3D();
		return transform3D;
	}

	protected static final Transform3D transform3DFactory(Vector3d position,
			double scale) {
		Transform3D transform3D;
		if (transform3DPool.size() > 0) {
			transform3D = transform3DPool.get(0);
			if (transform3D != null) {
				transform3D.setTranslation(position);
				transform3D.setScale(scale);
				return transform3D;
			}
		}
		transform3D = new Transform3D();
		transform3D.setTranslation(position);
		transform3D.setScale(scale);
		return transform3D;
	}

	public RendererCache() {
	}
	public RendererCache(ViewModel v, ColorModel c) {
		viewModel = v;
		colorModel = c;
	}
}
