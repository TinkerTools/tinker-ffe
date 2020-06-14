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
import java.io.IOException;
import java.util.Hashtable;
import java.util.logging.Logger;

import ffe.lang.Keyword;

/*
 * The KeyFilter class parses Tinker Keyword (*.KEY) files.
 */
public class KeyFilter {

	private static Logger logger = Logger.getLogger("ffe");
	
	public static Hashtable<String, Keyword> open(File keyFile) {
		if (keyFile == null || !keyFile.exists() || !keyFile.canRead()) {
			return null;
		}
		Hashtable<String, Keyword> keywordHash = new Hashtable<String, Keyword>();
		FileReader fr = null;
		BufferedReader br = null;
		try {
			fr = new FileReader(keyFile);
			br = new BufferedReader(fr);
			Keyword comments = new Keyword("COMMENTS");
			keywordHash.put("COMMENTS", comments);
			while (br.ready()) {
				String s = br.readLine();
				if (s == null) {
					continue;
				}
				s = s.trim();
				s = s.replace("\t", " "); // Replace tabs with spaces, JWP, Feb 2015
                                s = s.replace("~", System.getProperty("user.home"));
						// Replace tilde with Home Directory, JWP, Feb 2015

				if (s.equals("")) {
					continue; // Skip blank lines
				}
				// Store comments together
				if (s.startsWith("#") || s.toUpperCase().startsWith("ECHO")) {
					comments.append(s);
				} else {
					int firstspace = s.indexOf(" ");
					String keyword, data;
					if (firstspace == -1) { // no parameters
						keyword = s.trim().toUpperCase();
						// Rattle is special case, because it can be active
						// without being checked;
						// Valid Key files can have: RATTLE, or RATTLE BONDS,
						// or RATTLE & RATTLE BONDS as separate lines;
						// Each of these valid cases mean different things...
						if (keyword.equalsIgnoreCase("RATTLE")) {
							data = "RATTLE";
						} else {
							data = null;
						}
					} else {
						keyword = s.substring(0, firstspace).toUpperCase();
						data = s.substring(firstspace).trim();
					}
					Keyword kd = (Keyword) keywordHash.get(keyword);
					if (kd == null) {
						kd = new Keyword(keyword);
						keywordHash.put(keyword, kd);
					}
					if (data != null) {
						kd.append(data);
					}
					/*
					 * MULITPOLE and TORTORS are the only keywords that span
					 * multiple lines. Editing these from within Force Field
					 * Explorer seems unlikely, so they are treated as comments.
					 */
					if (keyword.equalsIgnoreCase("MULTIPOLE")) {
						int mnum[] = { 3, 1, 2, 3 };
						for (int i = 0; i < 4; i++) {
							if (!br.ready()) {
								System.out
										.println("Check for an invalid MULTIPOLE keyword.");
								return null;
							}
							s = br.readLine();
							if (s == null) {
								logger.warning("MULTIPOLE format error.");
								return null;
							}
							s = s.trim();
							if (s.split("\\s+").length != mnum[i]) {
								logger.warning("MULTIPOLE format error.");
								return null;
							}
							kd.append(s);
						}
					} else if (keyword.equalsIgnoreCase("TORTORS")) {
						String res[] = data.split("\\s+");
						if (res == null || res.length < 7) {
							logger.warning("TORTORS format error.");
							return null;
						}
						int xres = Integer.parseInt(res[5]);
						int yres = Integer.parseInt(res[6]);
						for (int i = 0; i < xres * yres; i++) {
							if (!br.ready()) {
								System.out
										.println("Check for an invalid TORTORS keyword.");
								return null;
							}
							s = br.readLine();
							if (s == null) {
								logger.warning("TORTORS format error.");
								return null;
							}
							s = s.trim();
							if (s.split("\\s+").length != 3) {
								logger.warning("TORTORS format error.");
								return null;
							}
							kd.append(s);
						}
					}
				}
			}
			return keywordHash;
		} catch (IOException e) {
			System.err.println("Error reading Key File: " + e);
			return null;
		} finally {
			if (fr != null) {
				try {
					fr.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public KeyFilter() {
	}
}
