/*
 * <p>Title: Force Field Explorer</p>
 * <p>Description: Force Field Explorer Molecular Modeling Program</p>
 * <p>Copyright: Copyright (c) 2004-2019 Jay William Ponder</p>
 * <p>Institution: Jay Ponder Lab, Washington University in Saint Louis</p>
 * @author Michael J. Schnieders
 * @version 8.7
 */

package ffe.lang;

import java.util.Hashtable;
import java.util.ListIterator;

import org.jogamp.vecmath.Vector3d;

/*
 * The Improper class represents an improper bonded energy term.
 */
public class Improper extends ValenceTerm {
	/*
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static Vector3d vi = new Vector3d();

	private static Vector3d vj = new Vector3d();

	private static Vector3d vk = new Vector3d();

	private static Vector3d vl = new Vector3d();

	private static Vector3d vij = new Vector3d();

	private static Vector3d vkj = new Vector3d();

	private static Vector3d vlk = new Vector3d();

	private static Vector3d dr = new Vector3d();

	private static Vector3d ds = new Vector3d();

	private static double dotij, dotlk, cosphi;

	private Angle an1, an2;

	// Constructor
	public Improper(Angle a1, Angle a2, Hashtable mmdi) {
		super();
		an1 = a1;
		an2 = a2;
		bonds = new Bond[3];
		bonds[1] = an1.getCommonBond(an2);
		bonds[0] = an1.getOtherBond(bonds[1]);
		bonds[2] = an2.getOtherBond(bonds[1]);
		atoms = new Atom[4];
		atoms[2] = bonds[0].getCommonAtom(bonds[1]);
		atoms[1] = bonds[0].get1_2(atoms[2]);
		atoms[0] = bonds[1].get1_2(atoms[2]);
		atoms[3] = bonds[2].get1_2(atoms[1]);
		setMM(mmdi);
	}

	// Public Methods
	public final void setID_Key(int combo) {
		ListIterator li;
		Atom a;
		String[] id = new String[4];
		String[] key = new String[4];
		int i = 0;
		for (li = getAtomList().listIterator(); li.hasNext();) {
			a = (Atom) li.next();
			id[i] = new String(a.getID());
			key[i++] = new String(a.getKey());
		}
		switch (combo) {
		case 0:
			setID(new String(id[0] + id[1] + id[2] + id[3]));
			setKey(new String(key[0] + key[1] + key[2] + key[3]));
			break;
		case 1:
			setID(new String(id[1] + id[0] + id[2] + id[3]));
			setKey(new String(key[1] + key[0] + key[2] + key[3]));
			break;
		case 2:
			setID(new String(id[3] + id[1] + id[2] + id[0]));
			setKey(new String(key[3] + key[1] + key[2] + key[0]));
			break;
		case 3:
			setID(new String(id[1] + id[3] + id[2] + id[0]));
			setKey(new String(key[1] + key[3] + key[2] + key[0]));
			break;
		case 4:
			setID(new String(id[3] + id[0] + id[2] + id[1]));
			setKey(new String(key[3] + key[0] + key[2] + key[1]));
			break;
		case 5:
			setID(new String(id[0] + id[3] + id[2] + id[1]));
			setKey(new String(key[0] + key[3] + key[2] + key[1]));
			break;
		}
	}

	public void setMM(Hashtable data) {
		ValenceType mmterm;
		for (int i = 0; i < 6; i++) {
			setID_Key(i);
			mmterm = (ValenceType) data.get(getKey());
			if (mmterm != null) {
				setMM(mmterm);
				return;
			}
		}
	}

	/*
	 * This code needs to be checked.
	 */
	public void update() {
		atoms[0].getV3D(vi);
		atoms[1].getV3D(vj);
		atoms[2].getV3D(vk);
		atoms[3].getV3D(vl);
		vij.sub(vi, vj);
		vkj.sub(vk, vj);
		vlk.sub(vl, vk);
		vkj.normalize();
		dotij = vij.dot(vkj);
		dotlk = vlk.dot(vkj);
		dr.scaleAdd(-dotij, vkj, vij);
		ds.scaleAdd(-dotlk, vkj, vlk);
		dr.normalize();
		ds.normalize();
		cosphi = dr.dot(ds);
		setValue(Math.toDegrees(Math.acos(cosphi)));
	}
}
