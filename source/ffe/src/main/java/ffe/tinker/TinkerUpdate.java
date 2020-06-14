/*
 * <p>Title: Force Field Explorer</p>
 * <p>Description: Force Field Explorer Molecular Modeling Program</p>
 * <p>Copyright: Copyright (c) 2004-2020 Jay William Ponder</p>
 * <p>Institution: Jay Ponder Lab, Washington University in Saint Louis</p>
 * @author Michael J. Schnieders
 * @version 8.8
 */

package ffe.tinker;

import java.io.Serializable;

/*
 * The TinkerUpdate class is a serializable wrapper for Tinker
 * simulation data that changes during a simulation.
 */

public class TinkerUpdate implements Serializable {
		
	private static final long serialVersionUID = 1L;

	public static int NONE = 0;

	public static int SIMULATION = 1;

	public static int OPTIMIZATION = 2;

	public boolean read = true;

	// Type
	public int type;

	// Coordinates
	public int numatoms;

	public double[][] coordinates = null;

	// Simulation Data
	public double time = -0.1d;

	public double temperature = 0.0d;

	public double energy = 0.0d;

	public double potential = 0.0d;

	public double kinetic = 0.0d;

	public double intermolecular = 0.0d;

	public double pressure = 0.0d;

	public double density = 0.0d;

	public double[][] velocity = null;

	public double[][] acceleration = null;

	// Optimization Data
	public int step = 0;

	public double[][] gradients = null;

	// Amoeba Data
	public boolean amoeba;

	public double[][] induced = null;

	public TinkerUpdate(int n, int t, boolean a) {
		numatoms = n;
		amoeba = a;
		type = t;
		coordinates = new double[3][numatoms];
		if (type == SIMULATION) {
			velocity = new double[3][numatoms];
			acceleration = new double[3][numatoms];
		} else if (type == OPTIMIZATION) {
			gradients = new double[3][numatoms];
		}
		if (amoeba) {
			induced = new double[3][numatoms];
		}
	}

	public boolean isNewer(FFEMessage message) {
		if (type == SIMULATION && time > message.getTime()) {
			return true;
		}
		if (type == OPTIMIZATION && step > message.getStep()) {
			return true;
		}
		return false;
	}

	public void print() {
		if (type == TinkerUpdate.SIMULATION) {
			System.out.println("Time: " + time + " Energy: " + energy);
		} else {
			System.out.println("Step: " + step + " Energy: " + energy);
		}
	}
}
