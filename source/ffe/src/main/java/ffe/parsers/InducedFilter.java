/*
 * <p>Title: Force Field Explorer</p>
 * <p>Description: Force Field Explorer Molecular Modeling Program</p>
 * <p>Copyright: Copyright (c) 2004-2019 Jay William Ponder</p>
 * <p>Institution: Jay Ponder Lab, Washington University in Saint Louis</p>
 * @author Michael J. Schnieders
 * @version 8.6
 */

package ffe.parsers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;
import java.util.logging.Logger;

import ffe.lang.Atom;
import ffe.lang.MolecularAssembly;
import ffe.lang.VectorMath;

/*
 * The InducedFilter class parses Tinker Induced Dipole (*.IND) files.
 */
public class InducedFilter {
	MolecularAssembly fsystem;

	File file;

	private Logger logger = Logger.getLogger("ffe");
	
	public InducedFilter(MolecularAssembly s, File f) {
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
			String data = br.readLine();
			String tokens[] = data.trim().split("\\s+");
			if (tokens.length == 0) {
				return false;
			}
			int numatoms = Integer.parseInt(tokens[0]);
			if (numatoms != fsystem.getAtomList().size()) {
				return false;
			}
			// Read the Induced Dipoles
			double x[][] = new double[numatoms][3];
			for (int i = 0; i < numatoms; i++) {
				data = br.readLine().trim();
				tokens = data.split("\\s+");
				if (tokens.length != 5) {
					return false;
				}
				x[i][0] = Double.parseDouble(tokens[2]);
				x[i][1] = Double.parseDouble(tokens[3]);
				x[i][2] = Double.parseDouble(tokens[4]);
			}
			List<Atom> atoms = fsystem.getAtomList();
			double max = 0.0d;
			for (Atom a : atoms){
				int j = a.getXYZIndex() - 1;
				a.setInducedDipole(-x[j][0], -x[j][1], -x[j][2]);
				double r = VectorMath.r(x[j]);
				if (r > max) {
					max = r;
				}
			}
			Atom.setMaxInduced(max);
			logger.warning("Max Induced: " + max);
			br.close();
			fr.close();
		} catch (Exception e) {
			return false;
		}
		return true;
	}
}
