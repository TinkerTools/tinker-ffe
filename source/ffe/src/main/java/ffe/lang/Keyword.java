/*
 * <p>Title: Force Field Explorer</p>
 * <p>Description: Force Field Explorer Molecular Modeling Program</p>
 * <p>Copyright: Copyright (c) 2004-2019 Jay William Ponder</p>
 * <p>Institution: Jay Ponder Lab, Washington University in Saint Louis</p>
 * @author Michael J. Schnieders
 * @version 8.7
 */

package ffe.lang;

import java.util.Vector;
import java.util.logging.Logger;

/*
 * The Keyword class holds a single Tinker keyword entry.
 */
public class Keyword {
	private String keyword = null;

	private Vector<String> data = null;

	private Logger logger = Logger.getLogger("ffe");
	
	public Keyword(String k) {
		keyword = k;
		data = new Vector<String>();
	}

	public Keyword(String k, String entry) {
		this(k);
		data.add(entry);
	}

	public Keyword(String k, String entry[]) {
		this(k);
		for (String s : entry) {
			data.add(s);
		}
	}

	public void append(String entry) {
		data.add(entry);
	}

	public void append(String entry[]) {
		for (String s : entry) {
			data.add(s);
		}
	}

	public void clear() {
		data.clear();
	}

	public Vector<String> getEntries() {
		return data;
	}

	public String getEntry(int i) {
		return data.get(i);
	}

	public String getKeyword() {
		return keyword;
	}

	public void print() {
		logger.info(this.toString());
	}

	public String toString() {
		StringBuffer sb = new StringBuffer(keyword + " ");
		for (String s : data) {
			sb.append(s);
		}
		return sb.toString();
	}
}
