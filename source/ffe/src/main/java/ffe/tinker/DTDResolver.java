/*
 * <p>Title: Force Field Explorer</p>
 * <p>Description: Force Field Explorer Molecular Modeling Program</p>
 * <p>Copyright: Copyright (c) 2004-2019 Jay William Ponder</p>
 * <p>Institution: Jay Ponder Lab, Washington University in Saint Louis</p>
 * @author Michael J. Schnieders
 * @version 8.7
 */

package ffe.tinker;

import java.net.URL;
import java.util.logging.Logger;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

/*
 * The DTDResolver class just points the DOM DocumentBuilder
 * to the XML Document Type Definition files.
 */

public class DTDResolver implements EntityResolver {

	private Logger logger = Logger.getLogger("ffe");
	
	public InputSource resolveEntity(String publicId, String systemId) {

		if (systemId.lastIndexOf("keywords.dtd") >= 0) {
			URL keyURL = getClass().getClassLoader().getResource(
					"ffe/xml/keywords.dtd");
			try {
				return new InputSource(keyURL.openStream());
			} catch (Exception e) {
				logger.warning("" + e);
				return null;
			}
		} else if (systemId.lastIndexOf("commands.dtd") >= 0) {
			URL commandURL = getClass().getClassLoader().getResource(
					"ffe/xml/commands.dtd");
			try {
				return new InputSource(commandURL.openStream());
			} catch (Exception e) {
				return null;
			}
		}
		return null;
	}
}
