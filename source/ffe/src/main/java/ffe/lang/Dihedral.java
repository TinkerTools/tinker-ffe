/*
 * <p>Title: Force Field Explorer</p>
 * <p>Description: Force Field Explorer Molecular Modeling Program</p>
 * <p>Copyright: Copyright (c) 2004-2019 Jay William Ponder</p>
 * <p>Institution: Jay Ponder Lab, Washington University in Saint Louis</p>
 * @author Michael J. Schnieders
 * @version 8.5
 */

package ffe.lang;

/*
 * The Dihedral class represents a dihedral angle formed between four bonded
 * atoms.
 */
public class Dihedral extends ValenceTerm {

	/*
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/*
	 * Update recomputes the dihedral angle value
	 */
	private static double vi[] = new double[3];

	private static double vj[] = new double[3];

	private static double vk[] = new double[3];

	private static double vl[] = new double[3];

	/*
	 * Dihedral contructor
	 * 
	 * @param an1
	 *            Angle that combines to form the Diherdral Angle
	 * @param an2
	 *            Angle that combines to form the Diherdral Angle
	 */
	public Dihedral(Angle an1, Angle an2) {
		super();
		bonds = new Bond[3];
		bonds[1] = an1.getCommonBond(an2);
		bonds[0] = an1.getOtherBond(bonds[1]);
		bonds[2] = an2.getOtherBond(bonds[1]);
		Initialize();
	}

	/*
	 * Dihedral contructor
	 * 
	 * @param a
	 *            Angle that has one Atom in common with Bond b
	 * @param b
	 *            Bond that has one Atom in common with Angle A
	 */
	public Dihedral(Angle a, Bond b) {
		super();
		bonds = new Bond[3];
		bonds[0] = b;
		bonds[1] = a.getBond(0);
		bonds[2] = a.getBond(1);
		// See if bond 2 or bond 3 is the middle bond
		Atom atom = bonds[1].getCommonAtom(b);
		if (atom == null) {
			Bond temp = bonds[1];
			bonds[1] = bonds[2];
			bonds[2] = temp;
		}
		Initialize();
	}

	/*
	 * Create a Dihedral from 3 connected bonds (no error checking)
	 * 
	 * @param b1
	 *            Bond
	 * @param b2
	 *            Bond
	 * @param b3
	 *            Bond
	 */
	public Dihedral(Bond b1, Bond b2, Bond b3) {
		super();
		bonds = new Bond[3];
		bonds[0] = b1;
		bonds[1] = b2;
		bonds[2] = b3;
		Initialize();
	}

	/*
	 * Dihedral Constructor
	 * 
	 * @param n
	 *            Dihedral id
	 */
	public Dihedral(String n) {
		super(n);
	}

	// Initialization
	private void Initialize() {
		atoms = new Atom[4];
		atoms[1] = bonds[0].getCommonAtom(bonds[1]);
		atoms[0] = bonds[0].get1_2(atoms[1]);
		atoms[2] = bonds[1].get1_2(atoms[1]);
		atoms[3] = bonds[2].get1_2(atoms[2]);
		atoms[0].setDihedral(this);
		atoms[1].setDihedral(this);
		atoms[2].setDihedral(this);
		atoms[3].setDihedral(this);
		setID_Key(false);
	}

	public void update() {
		atoms[0].getXYZ(vi);
		atoms[1].getXYZ(vj);
		atoms[2].getXYZ(vk);
		atoms[3].getXYZ(vl);
		setValue(Math.toDegrees(VectorMath.dihedralAngle(vi, vj, vk, vl)));
	}
}
