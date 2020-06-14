/*
 * <p>Title: Force Field Explorer</p>
 * <p>Description: Force Field Explorer Molecular Modeling Program</p>
 * <p>Copyright: Copyright (c) 2004-2020 Jay William Ponder</p>
 * <p>Institution: Jay Ponder Lab, Washington University in Saint Louis</p>
 * @author Michael J. Schnieders
 * @version 8.8
 */

package ffe.parsers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;

import ffe.lang.Atom;
import ffe.lang.MolecularAssembly;
import ffe.lang.VectorMath;

/*
 * The DYNFilter class parses Tinker Restart (*.dyn) files
 */

public class DYNFilter {
	MolecularAssembly fsystem;

	File file;

	public DYNFilter(MolecularAssembly s, File f) {
		fsystem = s;
		file = f;
	}

	public boolean read() {
		if (!file.exists() || !file.canRead()) {
			return false;
		}
		try {
			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);
			br.readLine();
			String data = br.readLine().trim();
			String tokens[] = data.split("\\s+");
			if (tokens.length == 0) {
				return false;
			}
			int numatoms = Integer.parseInt(tokens[0]);
			if (numatoms != fsystem.getAtomList().size()) {
				return false;
			}
			br.readLine();
			data = br.readLine().trim();
			tokens = data.split("\\s+");
			if (tokens.length != 3) {
				return false;
			}
			double d[] = new double[3];
			d[0] = Double.parseDouble(tokens[0]);
			d[1] = Double.parseDouble(tokens[1]);
			d[2] = Double.parseDouble(tokens[2]);
			fsystem.setBox(d);
			data = br.readLine().trim();
			tokens = data.split("\\s+");
			if (tokens.length != 3) {
				return false;
			}
			d[0] = Double.parseDouble(tokens[0]);
			d[1] = Double.parseDouble(tokens[1]);
			d[2] = Double.parseDouble(tokens[2]);
			fsystem.setAngle(d);
			// Positions
			br.readLine();
			double x[][] = new double[numatoms][3];
			for (int i = 0; i < numatoms; i++) {
				data = br.readLine().trim();
				tokens = data.split("\\s+");
				if (tokens.length != 3) {
					return false;
				}
				x[i][0] = Double.parseDouble(tokens[0]);
				x[i][1] = Double.parseDouble(tokens[1]);
				x[i][2] = Double.parseDouble(tokens[2]);
			}
			List<Atom> atoms = fsystem.getAtomList();
			for (Atom a : atoms) {
				int j = a.getXYZIndex() - 1;
				a.moveTo(x[j][0], x[j][1], x[j][2]);
			}
			// Velocities
			br.readLine();
			for (int i = 0; i < numatoms; i++) {
				data = br.readLine().trim();
				tokens = data.split("\\s+");
				if (tokens.length != 3) {
					return false;
				}
				x[i][0] = Double.parseDouble(tokens[0]);
				x[i][1] = Double.parseDouble(tokens[1]);
				x[i][2] = Double.parseDouble(tokens[2]);
			}
			double max = 0.0d;
			for (Atom a : atoms) {
				int j = a.getXYZIndex() - 1;
				a.setVeclocity(x[j][0], x[j][1], x[j][2]);
				double r = VectorMath.r(x[j]);
				if (r > max) {
					max = r;
				}
			}
			Atom.setMaxVelocity(max);
			// Accelerations
			br.readLine();
			for (int i = 0; i < numatoms; i++) {
				data = br.readLine().trim();
				tokens = data.split("\\s+");
				if (tokens.length != 3) {
					return false;
				}
				x[i][0] = Double.parseDouble(tokens[0]);
				x[i][1] = Double.parseDouble(tokens[1]);
				x[i][2] = Double.parseDouble(tokens[2]);
			}
			max = 0.0d;
			for (Atom a : atoms) {
				int j = a.getXYZIndex() - 1;
				a.setAcceleration(x[j][0], x[j][1], x[j][2]);
				double r = VectorMath.r(x[j]);
				if (r > max) {
					max = r;
				}
			}
			Atom.setMaxAcceleration(max);
			br.close();
			fr.close();
		} catch (Exception e) {
			return false;
		}
		return true;
	}
}
