/*
 * <p>Title: Force Field Explorer</p>
 * <p>Description: Force Field Explorer Molecular Modeling Program</p>
 * <p>Copyright: Copyright (c) 2004-2019 Jay William Ponder</p>
 * <p>Institution: Jay Ponder Lab, Washington University in Saint Louis</p>
 * @author Michael J. Schnieders
 * @version 8.6
 */

package ffe.core;

import java.awt.Container;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

/*
 * The KeyFileEditor class is a wrapper for the KeywordPanel
 * to create a standalone Tinker Key File Editor
 */
public final class KeyFileEditor extends JFrame {

	private static final long serialVersionUID = 1L;
	private Logger logger = Logger.getLogger("ffe");
	
	public static void main(String[] args) {
		KeyFileEditor editor = new KeyFileEditor();
		editor.setVisible(true);
	}

	KeywordPanel tkp;

	public KeyFileEditor() {
		try {
			UIManager.setLookAndFeel(UIManager
					.getCrossPlatformLookAndFeelClassName());
		} catch (Exception e) {
			logger.warning("Can't set look and feel: " + e);
		}
		tkp = new KeywordPanel(null);
		Container contentPane = getContentPane();
		contentPane.add(tkp);
		setTitle("Key File Editor");
		setSize(800, 800);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				close();
				System.exit(0);
			}
		});
	}

	private void close() {
		if (tkp.isFileOpen()) {
			int option = JOptionPane.showConfirmDialog(this,
					"Save Changes First", "Closing Key Editor",
					JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
			if (option == JOptionPane.YES_OPTION) {
				tkp.keySave(null);
			}
		}
	}
}
