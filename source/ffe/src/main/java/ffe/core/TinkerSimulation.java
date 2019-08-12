/*
 * <p>Title: Force Field Explorer</p>
 * <p>Description: Force Field Explorer Molecular Modeling Program</p>
 * <p>Copyright: Copyright (c) 2004-2019 Jay William Ponder</p>
 * <p>Institution: Jay Ponder Lab, Washington University in Saint Louis</p>
 * @author Michael J. Schnieders
 * @version 8.7
 */

package ffe.core;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetSocketAddress;
import java.util.List;

import javax.swing.Timer;

import ffe.lang.Atom;
import ffe.tinker.FFEClient;
import ffe.tinker.SimulationFilter;
import ffe.tinker.TinkerSystem;
import ffe.tinker.TinkerUpdate;

/*
 * This TinkerSimulation class oversees loading information
 * from an executing Tinker program into Force Field Explorer
 */
public class TinkerSimulation implements ActionListener {
	// The client monitors a socket based connection to an
	// executing Tinker program
	private FFEClient client;

	private SimulationFilter simulationFilter;

	private InetSocketAddress address;

	// If the Tinker program was launched from the GUI,
	// the job Thread will be alive until the program exits
	private Thread job = null;

	// Once the simulation is finished, this flag will be true
	private boolean finished = false;

	// The reader thread contains a SimulationFilter instance
	// that will read a simulation into the FFE
	private Thread reader;

	private MainPanel mainPanel;

	private FFESystem system;

	private TinkerUpdate tinkerUpdate = null;

	private boolean firstUpdate = true;

	private Timer timer;

	private int delay = 10;

	private double time = 0.0;

	private int step = 0;

	// Constructor
	public TinkerSimulation(FFESystem s, Thread j, MainPanel f,
			InetSocketAddress a) {
		system = s;
		job = j;
		mainPanel = f;
		address = a;
		if (address == null) {
			finished = true;
			return;
		}
	}

	public void actionPerformed(ActionEvent e) {
		// Check for connection to a Tinker Server
		if (!connect()) {
			return;
		}
		// Check if need to initialize the Simulation System
		if (system == null) {
			TinkerSystem sys = client.getSystem();
			if (sys != null) {
				if (simulationFilter == null) {
					if (system == null) {
						system = new FFESystem("Simulation", null, null, true);
					}
					simulationFilter = new SimulationFilter(sys, system);
					FileOpener openFile = new FileOpener(simulationFilter,
							mainPanel);
					reader = new Thread(openFile);
					reader.start();
				} else if (simulationFilter.fileRead()) {
					system = (FFESystem) simulationFilter.getMolecularSystem();
					simulationFilter = null;
				}
			}
		}
		// A Simulation System exists, attempt to Update
		else {
			if (tinkerUpdate == null || tinkerUpdate.read) {
				tinkerUpdate = client.getUpdate();
			}
			if (tinkerUpdate != null && !tinkerUpdate.read
					&& !mainPanel.getGraphics3D().isSceneRendering()) {
				update();
			}
		}
	}

	public boolean connect() {
		if (isFinished()) {
			return false;
		}
		if (isConnected()) {
			return true;
		}
		// Create a timer to regularly wake up this TinkerSimulation
		if (timer == null) {
			timer = new Timer(delay, this);
			timer.setCoalesce(true);
			timer.setDelay(5);
			timer.start();
		}
		// Create the FFEClient to monitor messages to/from Tinker
		if (client == null) {
			client = new FFEClient(address);
		}
		// Try to connect.
		client.connect();
		// If connected, change to our "steady-state" timer delay
		if (client.isConnected()) {
			timer.setDelay(delay);
			return true;
		}
		// The FFEClient and the Timer are set up, but a Tinker simulation
		// has not responded yet. This connect method will be called again
		// through "actionPerformed" when the timer wakes up
		return false;
	}

	public FFESystem getFSystem() {
		return system;
	}

	public boolean isConnected() {
		if (client != null && client.isConnected()) {
			return true;
		}
		return false;
	}

	public boolean isFinished() {
		if (client != null && client.isClosed()) {
			finished = true;
		}
		if (job != null && !job.isAlive()) {
			finished = true;
		}
		if (finished) {
			if (timer != null) {
				timer.stop();
			}
			update();
			release();
		}
		return finished;
	}

	// Release the simulation
	public void release() {
		finished = true;
		if (timer != null) {
			timer.stop();
		}
		if (client != null) {
			client.release();
		}
		if (system != null) {
			system.setSimulation(TinkerUpdate.NONE);
		}
		mainPanel.getMainMenu().setConnect(true);
	}

	private void update() {
		if (system.isStale()) {
			return;
		}
		if (tinkerUpdate == null || tinkerUpdate.read == true) {
			return;
		}
		system.setSimulation(tinkerUpdate.type);
		// Sanity check; FFE and Tinker should agree on the number of atoms
		List<Atom> atoms = system.getAtomList();
		int n = atoms.size();
		if (tinkerUpdate.numatoms != n) {
			return;
		}
		// This is either an MD Simulation or an Optimization
		system.setEnergy(tinkerUpdate.energy);
		if (tinkerUpdate.type == TinkerUpdate.SIMULATION) {
			if (tinkerUpdate.time == time) {
				tinkerUpdate.read = true;
				return;
			}
			time = tinkerUpdate.time;
			system.setTime(time);
			system.setTemperature(tinkerUpdate.temperature);
		} else if (tinkerUpdate.type == TinkerUpdate.OPTIMIZATION) {
			if (tinkerUpdate.step == step) {
				tinkerUpdate.read = true;
				return;
			}
			step = tinkerUpdate.step;
			system.setStep(step);
		}
		// Reset the Maximum Magnitude Values, such they will be consistent
		// with this frame of the simulation after the update
		Atom.setMaxForce(0.0);
		Atom.setMaxAcceleration(0.0);
		Atom.setMaxVelocity(0.0);
		Atom.setMaxInduced(0.0);
		double d[] = new double[3];
		for (Atom a : atoms) {
			int index = a.getXYZIndex() - 1;
			d[0] = tinkerUpdate.coordinates[0][index];
			d[1] = tinkerUpdate.coordinates[1][index];
			d[2] = tinkerUpdate.coordinates[2][index];
			a.moveTo(d);
			if (tinkerUpdate.amoeba) {
				// Flip the sign so Induced Dipoles point to more
				// positive potentials
				a.setInducedDipole(-1.0 * tinkerUpdate.induced[0][index], -1.0
						* tinkerUpdate.induced[1][index], -1.0
						* tinkerUpdate.induced[2][index]);
			}
			if (tinkerUpdate.type == TinkerUpdate.SIMULATION) {
				a.setVeclocity(tinkerUpdate.velocity[0][index],
						tinkerUpdate.velocity[1][index],
						tinkerUpdate.velocity[2][index]);
				a.setAcceleration(tinkerUpdate.acceleration[0][index],
						tinkerUpdate.acceleration[1][index],
						tinkerUpdate.acceleration[2][index]);
			} else if (tinkerUpdate.type == TinkerUpdate.OPTIMIZATION) {
				// Flip the sign to get forces
				a.setForce(-1.0 * tinkerUpdate.gradients[0][index], -1.0
						* tinkerUpdate.gradients[1][index], -1.0
						* tinkerUpdate.gradients[2][index]);
			}
		}
		if (firstUpdate) {
			system.center();
			firstUpdate = false;
		}
		mainPanel.getGraphics3D().updateScene(system, true, false, null, false, null);
		mainPanel.getHierarchy().updateStatus();
		mainPanel.getHierarchy().repaint();
		tinkerUpdate.read = true;
		tinkerUpdate = client.getUpdate();
	}
}
