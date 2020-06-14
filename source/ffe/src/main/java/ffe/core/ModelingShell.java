/*
 * <p>Title: Force Field Explorer</p>
 * <p>Description: Force Field Explorer Molecular Modeling Program</p>
 * <p>Copyright: Copyright (c) 2004-2020 Jay William Ponder</p>
 * <p>Institution: Jay Ponder Lab, Washington University in Saint Louis</p>
 * @author Michael J. Schnieders
 * @version 8.8
 */

package ffe.core;

import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;
import java.util.prefs.Preferences;

import javax.swing.ImageIcon;

/*
 * The ModelingShell is used to script Multiscale Modeling Routines
 * via the Groovy scripting language
 *
 * Functionality available through the modeling shell includes the Force
 * Field Explorer API, control of Tinker and the extensive Java APIs
 */
public class ModelingShell extends ffe.shell.Console {

	private static final long serialVersionUID = 1L;

	private MainPanel mainPanel;

	public ModelingShell(MainPanel m) {
		super();
		mainPanel = m;
		setVariable("dat", mainPanel.getHierarchy());
		setVariable("cmd", mainPanel);
		setVariable("vis", mainPanel.getGraphics3D());
		setVariable("job", mainPanel.getModelingPanel());
		setVariable("sh", this);
		setVariable("ffe", MainPanel.ffeDir.getAbsolutePath());
		setVariable("tinker", MainPanel.tinkerDir.getAbsolutePath());
		run();
		Frame frame = getFrame();
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				mainPanel.exit();
				System.exit(0);
			}
		});
		URL iconURL = getClass().getClassLoader().getResource(
				"ffe/icons/icon64.png");
		ImageIcon icon = new ImageIcon(iconURL);
		frame.setIconImage(icon.getImage());
		loadPrefs();
	}

	public void loadPrefs() {
		Preferences prefs = Preferences.userNodeForPackage(ModelingShell.class);
		int fontSize = prefs.getInt("ModelingShell_fontSize", 12);
		String b[] = prefs.get("ModelingShell_background", "0 0 0").split(" +"); // Black
		String f[] = prefs.get("ModelingShell_foreground", "255 255 255")
				.split(" +"); // White
	}

	public void savePrefs() {
		Preferences prefs = Preferences.userNodeForPackage(ModelingShell.class);

		/*
		prefs.putInt("ModelingShell_fontSize", c.getFont().getSize());
		Color b = c.getBackground();
		prefs.put("ModelingShell_background", b.getRed() + " " + b.getGreen()
				+ " " + b.getBlue());
		Color f = c.getForeground();
		prefs.put("ModelingShell_foreground", f.getRed() + " " + f.getGreen()
				+ " " + f.getBlue());
		*/
	}

	public void setMeasurement(String measurement, double d) {
		try {
			appendOutput(measurement, getOutputStyle());
		} catch (Exception e) {
			System.err.println(" " + e);
		}
	}

	public void sync() {
		try {
		} catch (Exception e) {
			System.out.println("Exception Syncing Force Field Explorer Directories");
		}
	}

	public String toString() {
		return "Modeling Shell";
	}
}
