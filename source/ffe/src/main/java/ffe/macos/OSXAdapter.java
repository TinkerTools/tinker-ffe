/*
 * <p>Title: Force Field Explorer</p>
 * <p>Description: Force Field Explorer Molecular Modeling Program</p>
 * <p>Copyright: Copyright (c) 2004-2021 Jay William Ponder</p>
 * <p>Institution: Jay Ponder Lab, Washington University in Saint Louis</p>
 * @author Michael J. Schnieders
 * @version 8.10
 */

package ffe.macos;

import java.lang.reflect.Method;
import java.util.logging.Logger;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.apple.eawt.Application;
import com.apple.eawt.ApplicationAdapter;
import com.apple.eawt.ApplicationEvent;

import ffe.core.MainPanel;

/*
 * The OSXAdapter class was developed by following an example supplied on
 * the Apple site. It handles events generated by the following standard
 * macOS toolbar items: About, Preferences, Quit and File Associations
 */
public class OSXAdapter extends ApplicationAdapter {
	private static OSXAdapter osxAdapter;

	private static Application osxApplication;

	private static Logger logger = Logger.getLogger("ffe");
	
	public static void registerMacOSXApplication(MainPanel m) {
		if (osxApplication == null) {
			osxApplication = new Application();
		}
		if (osxAdapter == null) {
			osxAdapter = new OSXAdapter(m);
		}
		osxApplication.addApplicationListener(osxAdapter);
		osxApplication.setEnabledPreferencesMenu(true);
		osxApplication.setEnabledAboutMenu(true);
	}
	
	@SuppressWarnings("unchecked")
	public static void macOSXRegistration(MainPanel m) {
		try {
			Class osxAdapter = Class.forName("ffe.macos.OSXAdapter");
			Class[] defArgs = { MainPanel.class };
			Method registerMethod = osxAdapter.getDeclaredMethod(
					"registerMacOSXApplication", defArgs);
			if (registerMethod != null) {
				Object[] args = { m };
				registerMethod.invoke(osxAdapter, args);
			}
		} catch (NoClassDefFoundError e) {
			logger.info("\nThis version of MacOS does not support "
					+ "the Apple EAWT. Application Menu "
					+ "handling has been disabled\n" + e);
		} catch (ClassNotFoundException e) {
			logger.info("\nThis version of MacOS does not support "
					+ "the Apple EAWT. Application Menu "
					+ "handling has been disabled" + e);
		} catch (Exception e) {
			logger.info("\nException while loading the OSXAdapter" + e);
		}
	}
	
	/**
	 * Set MacOS Systems Properties to promote native integration.
	 */
	public static void setOSXProperties(){
	   	System.setProperty(
				"apple.mrj.application.apple.menu.about.name",
				"Force Field Explorer");
		System.setProperty("apple.laf.useScreenMenuBar", "true");
		System.setProperty("apple.awt.showGrowBox", "true");
		System.setProperty("apple.mrj.application.growbox.intrudes",
				"false");
		System.setProperty("apple.awt.brushMetalLook", "true");
		System.setProperty("apple.mrj.application.live-resize", "true");
		System.setProperty("apple.macos.smallTabs", "true");
//		 -Xdock:name="Force Field Explorer"
	}

	private MainPanel mainPanel;

	private OSXAdapter(MainPanel m) {
		mainPanel = m;
	}

	public void handleAbout(ApplicationEvent ae) {
		if (mainPanel != null) {
			ae.setHandled(true);
			mainPanel.about();
		} else {
			ae.setHandled(false);
		}
	}

	public void handleOpenFile(ApplicationEvent ae) {
		if (mainPanel != null) {
			mainPanel.open(ae.getFilename());
			ae.setHandled(true);
		} else {
			ae.setHandled(false);
		}
	}

	public void handlePreferences(ApplicationEvent ae) {
		if (mainPanel != null) {
			mainPanel.getGraphics3D().preferences();
			ae.setHandled(true);
		} else {
			ae.setHandled(false);
		}
	}

	public void handleQuit(ApplicationEvent ae) {
		if (mainPanel != null) {
			ae.setHandled(false);
			mainPanel.exit();
		} else {
			System.exit(-1);
		}
	}

	public String toString() {
		return new ToStringBuilder(this).append(osxApplication).toString();
	}
}
