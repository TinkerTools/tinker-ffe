/*
 * <p>Title: Force Field Explorer</p>
 * <p>Description: Force Field Explorer Molecular Modeling Program</p>
 * <p>Copyright: Copyright (c) 2004-2020 Jay William Ponder</p>
 * <p>Institution: Jay Ponder Lab, Washington University in Saint Louis</p>
 * @author Michael J. Schnieders
 * @version 8.8
 */

package ffe.lang;

import java.util.ArrayList;
import java.util.List;

import org.jogamp.java3d.BranchGroup;
import org.jogamp.java3d.Canvas3D;
import org.jogamp.java3d.J3DGraphics2D;
import org.jogamp.java3d.Material;
import org.jogamp.java3d.Node;
import org.jogamp.vecmath.Color3f;

import ffe.lang.RendererCache.ColorModel;
import ffe.lang.RendererCache.ViewModel;

/*
 * The ROLS Interace defines "Recursive Over Length Scales" (ROLS) Methods.
 */
public interface ROLS {

	public static final int MaxLengthScale = 5;

	public static int LengthScale = MaxLengthScale;

	public void drawLabel(Canvas3D graphics, J3DGraphics2D g2d, Node node);

	public double[] getCenter(boolean w);

	public ArrayList<ROLS> getList(Class c, ArrayList<ROLS> nodes);

	public long getMSCount(Class c, long count);

	public ROLS getMSNode(Class c);

	public double getMW();

	public void setColor(ColorModel colorModel, Color3f color, Material mat);

	public void setView(ViewModel viewModel, List<BranchGroup> newShapes);

	public void update();
}
