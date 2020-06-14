/*
 * <p>Title: Force Field Explorer</p>
 * <p>Description: Force Field Explorer Molecular Modeling Program</p>
 * <p>Copyright: Copyright (c) 2004-2020 Jay William Ponder</p>
 * <p>Institution: Jay Ponder Lab, Washington University in Saint Louis</p>
 * @author Michael J. Schnieders
 * @version 8.8
 */

package ffe.tinker;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Vector;

import ffe.lang.Atom;
import ffe.lang.Bond;
import ffe.lang.MolecularAssembly;
import ffe.lang.Utilities.FileType;
import ffe.mm.AtomType;
import ffe.parsers.SystemFilter;

/*
 * The SimulationFilter class parses system data sent by Tinker
 * to Force Field Explorer.
 */
public final class SimulationFilter extends SystemFilter {
	TinkerSystem system;

	Hashtable<Integer, AtomType> atomTypes = new Hashtable<Integer, AtomType>();

	public SimulationFilter(TinkerSystem sys, MolecularAssembly m) {
		super(m);
		system = sys;
		setType(FileType.SIM);
		setFileRead(false);
	}

	public boolean readFile() {
		molecularAssembly.setFileType(FileType.SIM);
		// Create Molecular Mechanics Data Objects from the TinkerSystem
		// information
		for (int i = 0; i < system.numatoms; i++) {
			AtomType atomType = atomTypes.get(system.types[i]);
			if (atomType == null) {
				atomType = new AtomType(system.types[i], -1, system.name[i],
						system.story[i], system.atomic[i], system.mass[i], 0);
				atomTypes.put(system.types[i], atomType);
			}
		}
		atomList = new ArrayList<Atom>();
		Vector<Integer> bonds1 = new Vector<Integer>();
		Vector<Integer> bonds2 = new Vector<Integer>();
		double d[] = new double[3];
		int b[] = new int[4];
		for (int i = 0; i < system.numatoms; i++) {
			d[0] = system.coordinates[0][i];
			d[1] = system.coordinates[1][i];
			d[2] = system.coordinates[2][i];
			int s = system.types[i];
			AtomType atomType = atomTypes.get(s);
			int aNum = i+1;
			String aStr = new String("" + atomType.type);
			Atom a = new Atom(i + 1, new String("" + atomType.type), atomType,
					d);
			atomList.add(a);
			int b1 = i + 1;
			b[0] = system.connectivity[0][i];
			b[1] = system.connectivity[1][i];
			b[2] = system.connectivity[2][i];
			b[3] = system.connectivity[3][i];
			int j = 0;
			while (j < 4 && b[j] != 0) {
				int b2 = b[j];
				bonds1.add(b1);
				bonds2.add(b2);
				j++;
			}
		}
		bondList = new ArrayList<Bond>();
		for (int i = 0; i < bonds1.size(); i++) {
			int a1 = bonds1.get(i);
			int a2 = bonds2.get(i);
			if (a1 < a2) {
				Atom atom1 = atomList.get(a1 - 1);
				Atom atom2 = atomList.get(a2 - 1);
				bondList.add(new Bond(atom1, atom2, 1));
			}
		}
		setFileRead(true);
		return true;
	}

	public boolean writeFile() {
		return false;
	}
}
