/*
 * <p>Title: Force Field Explorer</p>
 * <p>Description: Force Field Explorer Molecular Modeling Program</p>
 * <p>Copyright: Copyright (c) 2004-2019 Jay William Ponder</p>
 * <p>Institution: Jay Ponder Lab, Washington University in Saint Louis</p>
 * @author Michael J. Schnieders
 * @version 8.7
 */

package ffe.lang;

import java.util.ArrayList;
import java.util.Enumeration;

/*
 * The Selection class will be used to make recursive multiscale selections,
 * however its implementation is not yet complete.
 */
public class Selection {
	public static Selection select(MSNode m, Class scale, String criteria) {
		Selection s = new Selection(m, scale, criteria);
		s.evaluate();
		return s;
	}

	public static Selection select(MSNode m, String scale, String criteria) {
		Selection s = new Selection(m, scale, criteria);
		s.evaluate();
		return s;
	}

	Class scale = null;

	String criteria = null;

	MSNode m = null;

	private ArrayList<MSNode> selected = null;

	private Selection(MSNode m, Class scale, String criteria) {
		this.scale = scale;
		this.criteria = criteria;
		this.m = m;
		selected = new ArrayList<MSNode>();
	}

	private Selection(MSNode m, String scale, String criteria) {
		try {
			this.scale = Class.forName(scale);
		} catch (Exception e) {
			this.scale = null;
		}
		this.criteria = criteria;
		this.m = m;
		selected = new ArrayList<MSNode>();
	}

	public Selection and(Class scale, String criteria) {
		this.scale = scale;
		this.criteria = criteria;
		evaluate();
		return this;
	}

	private void evaluate() {
		Enumeration e = m.depthFirstEnumeration();
		while (e.hasMoreElements()) {
			MSNode n = (MSNode) e.nextElement();
			if (scale.isInstance(n)) {
				selected.add(n);
			}
		}
	}

	public Selection or(Class scale, String criteria) {
		this.scale = scale;
		this.criteria = criteria;
		evaluate();
		return this;
	}
}
