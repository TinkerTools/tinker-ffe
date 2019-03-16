/*
 * <p>Title: Force Field Explorer</p>
 * <p>Description: Force Field Explorer Molecular Modeling Program</p>
 * <p>Copyright: Copyright (c) 2004-2019 Jay William Ponder</p>
 * <p>Institution: Jay W. Ponder, Washington University in Saint Louis</p>
 * @author Michael J. Schnieders
 * @version 8.6
 */

package ffe.properties;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.logging.Logger;

/*
 * The FFELocale class encapsulates internationalization features
 */
public class FFELocale {

	private Logger logger = Logger.getLogger("ffe");

	private Locale currentLocale;

	private PropertyResourceBundle ffeLabels;

	private Hashtable<String, String> reverseLookUp = new Hashtable<String, String>();

	public FFELocale() {
		currentLocale = Locale.getDefault();
		ffeLabels = (PropertyResourceBundle) ResourceBundle.getBundle(
				"ffe.properties.StringBundle", currentLocale);
		loadHashtable();
	}

	public FFELocale(String language, String country) {
		setLocale(language, country);
	}

	public String getKey(String string) {
		return reverseLookUp.get(string);
	}

	public String getValue(String key) {
		return ffeLabels.getString(key).trim();
	}

	public void list() {
		for (String value : reverseLookUp.keySet()) {
			String key = reverseLookUp.get(value);
			logger.info("key = " + key + ", " + "value = " + value);
		}
	}

	private void loadHashtable() {
		reverseLookUp.clear();
		Enumeration<String> e = ffeLabels.getKeys();
		while (e.hasMoreElements()) {
			String key = e.nextElement();
			String value = getValue(key);
			reverseLookUp.put(value, key);
		}
	}

	public boolean setLocale(String language, String country) {
		Locale locale = new Locale(language, country);
		try {
			ffeLabels = (PropertyResourceBundle) ResourceBundle.getBundle(
					"ffe.properties.StringBundle", locale);
		} catch (Exception ex) {
			Logger.getLogger("ffe").severe("" + ex);
			return false;
		}
		loadHashtable();
		currentLocale = locale;
		return true;
	}
}
