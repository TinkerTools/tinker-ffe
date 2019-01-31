/*
 * <p>Title: Force Field Explorer</p>
 * <p>Description: Force Field Explorer Molecular Modeling Program</p>
 * <p>Copyright: Copyright (c) 2004-2019 Jay William Ponder</p>
 * <p>Institution: Jay Ponder Lab, Washington University in Saint Louis</p>
 * @author Michael J. Schnieders
 * @version 8.5
 */

package ffe.core;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JSplitPane;

/*
 * The GraphicsSplitPane is an attempt at working around issues caused
 * by the heavyweight Canvas3D inside a lightweight Swing SplitPane;
 * Specifically, you can't drag the slider toward the heavyweight Canvas3D
 */
public class GraphicsSplitPane extends JSplitPane implements MouseListener,
		MouseMotionListener {

	private static final long serialVersionUID = 1L;

	boolean mouseClicked = false;

	int currentPos = 0;

	public GraphicsSplitPane() {
		super();
		addMouseListener(this);
		addMouseMotionListener(this);
	}

	public GraphicsSplitPane(int orient, boolean b, Component left,
			Component right) {
		super(orient, b, left, right);
		addMouseListener(this);
		addMouseMotionListener(this);
	}

	public void mouseClicked(MouseEvent e) {
		mouseClicked = true;
		currentPos = e.getX();
		// System.out.println("Split Clicked");
	}

	public void mouseDragged(MouseEvent e) {
		// System.out.println("Split Dragged");
		if (mouseClicked) {
			int change = e.getX() - currentPos;
			setDividerLocation(getDividerLocation() + change);
		}
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mouseMoved(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
	}

	public void mouseReleased(MouseEvent e) {
		mouseClicked = false;
	}
}
