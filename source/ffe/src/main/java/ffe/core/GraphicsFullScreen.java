/*
 * <p>Title: Force Field Explorer</p>
 * <p>Description: Force Field Explorer Molecular Modeling Program</p>
 * <p>Copyright: Copyright (c) 2004-2019 Jay William Ponder</p>
 * <p>Institution: Jay Ponder Lab, Washington University in Saint Louis</p>
 * @author Michael J. Schnieders
 * @version 8.6
 */

package ffe.core;

import java.awt.Frame;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/*
 * The FullScreenWindow class controls full screen graphics.
 */
public class GraphicsFullScreen extends Window implements KeyListener {
	/*
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private boolean fullScreen = false;

	public GraphicsFullScreen(Frame f, GraphicsCanvas graphics) {
		super(f);
		/*
		setLayout(new BorderLayout());
		screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setSize(screenSize);
		fullScreenCanvas = new Canvas3D(graphics.getGraphicsConfiguration());
		fullScreenCanvas.stopRenderer();
		graphics.getView().addCanvas3D(fullScreenCanvas);
		addKeyListener(this);
		fullScreenCanvas.addKeyListener(this);
		setFocusable(true);
		fullScreenCanvas.setFocusable(true);
		add(fullScreenCanvas, BorderLayout.CENTER); */
	}

	public void enterFullScreen() {
		/*
		if (fullScreen) {
			return;
		}
		fullScreenCanvas.startRenderer();
		setVisible(true);
		fullScreenCanvas.requestFocus();
		fullScreen = true; */
	}

	public void exitFullScreen() {
		/*
		if (!fullScreen) {
			return;
		}
		setVisible(false);
		fullScreenCanvas.stopRenderer();
		fullScreen = false;
		*/
	}

	public void keyPressed(KeyEvent evt) {
		if (evt.getKeyCode() == KeyEvent.VK_ESCAPE) {
			exitFullScreen();
		} else if (evt.getKeyChar() == 'e') {
			exitFullScreen();
		} else if (evt.getKeyChar() == 'x') {
			exitFullScreen();
		}
	}

	public void keyReleased(KeyEvent evt) {
		keyPressed(evt);
	}

	public void keyTyped(KeyEvent evt) {
		keyPressed(evt);
	}

	public void toggleFullScreen() {
		if (fullScreen) {
			exitFullScreen();
		} else {
			enterFullScreen();
		}
	}
}
