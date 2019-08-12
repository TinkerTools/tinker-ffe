/*
 * <p>Title: Force Field Explorer</p>
 * <p>Description: Force Field Explorer Molecular Modeling Program</p>
 * <p>Copyright: Copyright (c) 2004-2019 Jay William Ponder</p>
 * <p>Institution: Jay Ponder Lab, Washington University in Saint Louis</p>
 * @author Michael J. Schnieders
 * @version 8.7
 */

package ffe.core;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;

import javax.swing.JPanel;

/*
 * The GraphicsPanel class contains the 3D Canvas and its status box
 */
public class GraphicsPanel extends JPanel {

	private GraphicsCanvas graphics;

	private JPanel canvasPanel;

	private JPanel statusPanel;

	public GraphicsPanel(GraphicsCanvas g, JPanel s) {
		super();
		setLayout(new BorderLayout());
		statusPanel = s;
		if (g != null) {
			canvasPanel = new JPanel(new GridLayout(1, 1));
			canvasPanel.add(g);
			add(canvasPanel, BorderLayout.CENTER);
		} else {
			setBackground(Color.BLACK);
		}
		add(statusPanel, BorderLayout.SOUTH);
	}

	public void setVisible(boolean v) {
		super.setVisible(v);
		if (graphics != null) {
			graphics.setVisible(v);
		}
	}
}
