/*
 * <p>Title: Force Field Explorer</p>
 * <p>Description: Force Field Explorer Molecular Modeling Program</p>
 * <p>Copyright: Copyright (c) 2004-2019 Jay William Ponder</p>
 * <p>Institution: Jay Ponder Lab, Washington University in Saint Louis</p>
 * @author Michael J. Schnieders
 * @version 8.5
 */

package ffe.core;

import java.io.File;
import java.util.Hashtable;

import ffe.lang.Atom;
import ffe.lang.Keyword;
import ffe.lang.MolecularAssembly;
import ffe.mm.ForceField;
import ffe.tinker.TinkerUpdate;

/*
 * The FFESystem class contains extensions to the generic
 * ffe.lang.MolecularAssembly class specific to Force Field
 * Explorer interacting with Tinker
 */
public class FFESystem extends MolecularAssembly {

	private static final long serialVersionUID = 50L;

	public static final int MultiScaleLevel = 4;

	// Log file being used for modeling commands
	private File logFile;

	// Key file for this system
	private File keyFile;

	private Hashtable<String, Keyword> keywords = new Hashtable<String, Keyword>();

	// Force Field being used
	private ForceField forceField;

	// Command Description if this System is the result of a Tinker commad
	private String commandDescription = null;

	// Archive
	private Trajectory trajectory = null;

	// Simulation type
	private int simulation = 0;

	// Simulation data
	private double time, temperature, energy;

	private int step;

	// Flag to indicate this System is being closed
	private boolean closing = false;

	/*
	 * FFESystem Constructor
	 * 
	 * @param name         String
	 * @param bondsKnown   boolean
	 */
	public FFESystem(String name, String description, File file,
			boolean bondsKnown) {
		super(name, bondsKnown);
		setFile(file);
		commandDescription = description;
	}

	public void addKeyword(Keyword k) {
		if (keywords.containsKey(k.getKeyword())) {
			return;
		}
		keywords.put(k.getKeyword(), k);
	}

	public boolean destroy() {
		setClosing(true);
		return super.destroy();
	}

	public double getEnergy() {
		return energy;
	}

	public String getEnergyString() {
		return String.format("Energy: %9.3f kcal/mole", energy);
	}

	public ForceField getForceField() {
		return forceField;
	}

	public File getKeyFile() {
		return keyFile;
	}

	public Keyword getKeyword(String k) {
		return keywords.get(k);
	}

	public Hashtable<String, Keyword> getKeywords() {
		return keywords;
	}

	public File getLogFile() {
		if (logFile == null) {
			if (getFile() == null) {
				return null;
			}
			String fileName = getFile().getName();
			int dot = fileName.lastIndexOf(".");
			fileName = fileName.subSequence(0, dot) + ".log";
			logFile = new File(fileName);
		}
		return logFile;
	}

	public String getStepString() {
		return String.format("Step: %12d", step);
	}

	public double getTemperature() {
		return temperature;
	}

	public double getTime() {
		return time;
	}

	public String getTimeString() {
		return String.format("Time: %9.3f picoseconds", this.time);
	}

	public Trajectory getTrajectory() {
		return trajectory;
	}

	public boolean isClosing() {
		return closing;
	}

	public boolean isOptimization() {
		if (simulation == TinkerUpdate.OPTIMIZATION) {
			return true;
		}
		return false;
	}

	public boolean isSimulation() {
		if (simulation == TinkerUpdate.SIMULATION) {
			return true;
		}
		return false;
	}

	public boolean isStale() {
		for (Atom a : getAtomList()) {
			if (a.isStale()) {
				return true;
			}
		}
		return false;
	}

	public void removeKeyword(Keyword kd) {
		if (keywords.containsKey(kd.getKeyword())) {
			keywords.remove(kd.getKeyword());
		}
	}

	public void setClosing(boolean b) {
		closing = b;
	}

	public void setCommandDescription(String command) {
		commandDescription = command;
	}

	public void setEnergy(double e) {
		energy = e;
	}

	public void setForceField(ForceField forceField) {
		this.forceField = forceField;
	}

	public void setKeyFile(File f) {
		keyFile = f;
	}

	public void setKeywords(Hashtable<String, Keyword> k) {
		keywords = k;
	}

	public void setLogFile(File f) {
		logFile = f;
	}

	public void setSimulation(int type) {
		simulation = type;
	}

	public void setStep(int s) {
		step = s;
	}

	public void setTemperature(double t) {
		temperature = t;
	}

	public void setTime(double t) {
		time = t;
	}

	public void setTrajectory(Trajectory t) {
		trajectory = t;
	}

	public String toFFString() {
		StringBuffer sb = new StringBuffer(toString());
		if (forceField != null) {
			String ff = forceField.toString("forcefield");
			if (ff != null) {
				ff = ff.substring(10).trim();
				sb.append(" (");
				sb.append(ff);
				sb.append(")");
			}
		}
		return sb.toString();
	}

	public String toFileString() {
		if (getFile() == null) {
			return toFFString();
		}
		StringBuffer sb = new StringBuffer(getFile().getAbsolutePath());
		if (forceField != null) {
			String ff = forceField.toString("forcefield");
			if (ff != null) {
				ff = ff.substring(10).trim();
				sb.append(" (");
				sb.append(ff);
				sb.append(")");
			}
		}
		return sb.toString();
	}

	public String toString() {
		if (getFile() != null) {
			if (commandDescription != null) {
				return getFile().getName() + " (" + commandDescription + ")";
			}
			return getFile().getName();
		}
		if (getName() != null) {
			if (commandDescription != null) {
				return getName() + commandDescription;
			}
			return getName();
		}
		return "FFE System";
	}
}
