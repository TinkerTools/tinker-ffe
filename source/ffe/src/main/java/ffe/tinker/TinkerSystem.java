/*
 * <p>Title: Force Field Explorer</p>
 * <p>Description: Force Field Explorer Molecular Modeling Program</p>
 * <p>Copyright: Copyright (c) 2004-2019 Jay William Ponder</p>
 * <p>Institution: Jay Ponder Lab, Washington University in Saint Louis</p>
 * @author Michael J. Schnieders
 * @version 8.6
 */

package ffe.tinker;

import java.io.Serializable;
import java.util.logging.Logger;

/*
 * The TinkerSystem class is a serializable wrapper
 * that specifies a Tinker system.
 */
public class TinkerSystem implements Serializable {
	
	private static final long serialVersionUID = 1L;

	public boolean read = true;

	// System definition
	public int numatoms;

	public int numkeys;

	public String file;

	public String forcefield;

	public String[] keywords;

	public double[][] coordinates;

	public int[][] connectivity;

	public int[] types;

	public String[] name;

	public String[] story;

	public double[] charge;

	public double[] mass;

	public int[] atomic;

	/*
	 * Constructor that allocates space for a Tinker system
	 * 
	 * @param a
	 *            The number of atoms
	 * @param k
	 *            The number of keywords
	 */
	public TinkerSystem(int a, int k) {
		numatoms = a;
		numkeys = k;
		keywords = new String[k];
		coordinates = new double[3][a];
		connectivity = new int[4][a];
		types = new int[a];
		name = new String[a];
		story = new String[a];
		charge = new double[a];
		mass = new double[a];
		atomic = new int[a];
	}

	public void print() {
		System.out.println(this.toString());
	}

	public String toString() {
		return new String("Atoms: " + numatoms + " Keywords: " + numkeys);
	}
}
