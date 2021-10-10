/*
 * <p>Title: Force Field Explorer</p>
 * <p>Description: Force Field Explorer Molecular Modeling Program</p>
 * <p>Copyright: Copyright (c) 2004-2021 Jay William Ponder</p>
 * <p>Institution: Jay Ponder Lab, Washington University in Saint Louis</p>
 * @author Michael J. Schnieders
 * @version 8.10
 */

package ffe.tinker;

import java.io.Serializable;

/*
 * The FFEMessage class is used to pass simple messages
 * between a TinkerServer and its FFEClient(s).
 */
public class FFEMessage implements Serializable {
	
	private static final long serialVersionUID = 1L;

	public static int SYSTEM = 0;

	public static int UPDATE = 1;

	public static int CLOSING = 2;

	public static int OK = 3;

	private int message = 0;

	private int step = -1;

	private double time = -1.0;

	private int type = 0;

	public FFEMessage(int m) {
		message = m;
	}

	public int getMessage() {
		return message;
	}

	public int getStep() {
		return step;
	}

	public double getTime() {
		return time;
	}

	public int getType() {
		return type;
	}

	public void print() {
		System.out.println(toString());
	}

	public void setMessage(int m) {
		message = m;
	}

	public void setStep(int s) {
		step = s;
	}

	public void setTime(double t) {
		time = t;
	}

	public void setType(int t) {
		type = t;
	}

	public String toString() {
		if (message == 0) {
			return new String("SYSTEM").intern();
		} else if (message == 1) {
			return new String("UPDATE").intern();
		} else if (message == 2) {
			return new String("CLOSING").intern();
		} else {
			return new String("OK").intern();
		}
	}
}
