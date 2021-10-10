/*
 * <p>Title: Force Field Explorer</p>
 * <p>Description: Force Field Explorer Molecular Modeling Program</p>
 * <p>Copyright: Copyright (c) 2004-2021 Jay William Ponder</p>
 * <p>Institution: Jay Ponder Lab, Washington University in Saint Louis</p>
 * @author Michael J. Schnieders
 * @version 8.10
 */

package ffe.lang;

/*
 * The Angle class represents an angle formed between three linearly bonded
 * atoms.
 */
public class Angle extends ValenceTerm {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static double vi[] = new double[3];

	private static double vj[] = new double[3];

	private static double vk[] = new double[3];

	/*
	 * Angle contructor
	 * 
	 * @param b1
	 *            Bond that forms one leg of the angle
	 * @param b2
	 *            Bond that forms the other leg of the angle
	 */
	public Angle(Bond b1, Bond b2) {
		super();
		bonds = new Bond[2];
		bonds[0] = b1;
		bonds[1] = b2;
		b1.setAngleWith(b2);
		b2.setAngleWith(b1);
		Atom a2 = b1.getCommonAtom(b2);
		Atom a1 = b1.get1_2(a2);
		Atom a3 = b2.get1_2(a2);
		atoms = new Atom[3];
		atoms[0] = a1;
		atoms[1] = a2;
		atoms[2] = a3;
		a1.setAngle(this);
		a2.setAngle(this);
		a3.setAngle(this);
		setID_Key(false);
	}

	/*
	 * If the specified atom is not the central atom of this angle,
	 * the atom of the opposite leg is returned. These atoms are
	 * said to be 1-3 to each other.
	 * 
	 * @param a
	 *            Atom
	 * @return Atom
	 */
	public Atom get1_3(Atom a) {
		if (a == atoms[0]) {
			return atoms[2];
		}
		if (a == atoms[2]) {
			return atoms[0];
		}
		return null;
	}

	/*
	 * Finds the common bond between <b>this</b> angle and another
	 * 
	 * @param a
	 *            An Angle that may have a common bond with <b>this</b> angle
	 * @return The common Bond between this Angle and Angle a, or null if this ==
	 *         a or no common bond exists
	 */
	public Bond getCommonBond(Angle a) {
		// Comparing an angle to itself returns null
		// Comparing to a null angle return null
		if (a == this || a == null) {
			return null;
		}
		if (a.bonds[0] == bonds[0]) {
			return bonds[0];
		}
		if (a.bonds[0] == bonds[1]) {
			return bonds[1];
		}
		if (a.bonds[1] == bonds[0]) {
			return bonds[0];
		}
		if (a.bonds[1] == bonds[1]) {
			return bonds[1];
		}
		return null; // No common bond found
	}

	/*
	 * Finds the other bond that makes up <b>this</b> angle
	 * 
	 * @param b
	 *            The bond to find the opposite of
	 * @return The other Bond that makes up this Angle, or null if Bond b is not
	 *         part of this Angle
	 */
	public Bond getOtherBond(Bond b) {
		if (b == bonds[0]) {
			return bonds[1];
		}
		if (b == bonds[1]) {
			return bonds[0];
		}
		return null; // b not found in angle
	}

	/*
	 * Update recomputes the angle's value
	 */
	@Override
	public void update() {
		atoms[0].getXYZ(vi);
		atoms[1].getXYZ(vj);
		atoms[2].getXYZ(vk);
		setValue(Math.toDegrees(VectorMath.bondAngle(vi, vj, vk)));
	}
}
