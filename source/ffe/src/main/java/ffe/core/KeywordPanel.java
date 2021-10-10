/*
 * <p>Title: Force Field Explorer</p>
 * <p>Description: Force Field Explorer Molecular Modeling Program</p>
 * <p>Copyright: Copyright (c) 2004-2021 Jay William Ponder</p>
 * <p>Institution: Jay Ponder Lab, Washington University in Saint Louis</p>
 * @author Michael J. Schnieders
 * @version 8.10
 */

package ffe.core;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Vector;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import ffe.lang.Keyword;
import ffe.parsers.KeyFileFilter;
import ffe.parsers.KeyFilter;
import ffe.tinker.DTDResolver;

/*
 * The KeywordPanel class provides a View and Control of Tinker Keyword files
 */
public final class KeywordPanel extends JPanel implements ActionListener {
	/*
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Logger logger = Logger.getLogger("ffe");
	
	// True if a Key File is open.
	private boolean fileOpen = false;

	// Currently open Keywords.
	private Hashtable<String, Keyword> currentKeys;

	// Currently open Key File.
	private File currentKeyFile;

	// FFESystem associated with currently open Key File (if any).
	private FFESystem currentSystem;

	// Swing Components.
	// The MainPanel has references to many things the KeywordPanel uses.
	private MainPanel mainPanel;

	// HashMap for Keyword GUI Components.
	private LinkedHashMap<String, KeywordComponent> keywordHashMap;

	// HashMap for Keyword Groups.
	private LinkedHashMap<String, String> groupHashMap;

	// JComboBox with Keyword Groups, plus a few special caes (Active, Flat
	// File).
	private JComboBox groupComboBox;

	// The editPanel holds the toolBar (north), splitPane (center) and
	// statusLabel (south).
	private JPanel editPanel;

	private JToolBar toolBar;

	// The splitPane holds the editScrollPane (top) and descriptScrollPane
	// (bottom).
	private JSplitPane splitPane;

	private JLabel statusLabel = new JLabel("  ");

	// The editScrollPane holds the gridPanel, where KeywordComponents actually
	// live.
	private JScrollPane editScrollPane;

	// The descriptScrollPane holds the descriptTextArea for Keyword
	// Descriptions.
	private JScrollPane descriptScrollPane;

	// The descriptTextArea actually holds Keyword Descriptions.
	private JTextArea descriptTextArea;

	// Allow the user to show/hide Keyword Descriptions.
	private JCheckBoxMenuItem descriptCheckBox;

	private GridBagLayout gridBagLayout = new GridBagLayout();

	private GridBagConstraints gridBagConstraints = new GridBagConstraints();

	// The gridPanel holds an array of KeywordComponents.
	private JPanel gridPanel = new JPanel(gridBagLayout);

	// Lines in Keyword files that are comments, unrecognized keywords, or
	// keywords where editing is not supported are stored in a big StringBuffer.
	private StringBuffer commentStringBuffer = new StringBuffer();

	// This component shows what the saved Key file will look like (WYSIWYG).
	private JTextArea flatfileTextArea;

	// A simple label if no Keyword File is open.
	private JLabel noSystemLabel = new JLabel(
			"Keywords for the active system are edited here. ");

	// A simple label if no Keyword Description is available.
	private JLabel noKeywordLabel = new JLabel(
			"Keyword desciptions are displayed here.");

	private JPanel noKeywordPanel = new JPanel(new FlowLayout(FlowLayout.LEFT,
			5, 5));

	private FlowLayout flowLayout = new FlowLayout(FlowLayout.LEFT, 5, 5);

	private BorderLayout borderLayout = new BorderLayout();

	// Location of Tinker parameter files
	private File paramDir = null;

	private File paramFiles[] = null;

	private String paramNames[] = null;

	private Hashtable<String, String> paramHashtable = null;

	/*
	 * Default Construtor where parent is the Tinker Window Frame object
	 */
	public KeywordPanel(MainPanel f) {
		super();
		mainPanel = f;
		initialize();
	}

	/*
	 * Handles input from KeywordPanel ToolBar buttons.
	 */
	public void actionPerformed(ActionEvent evt) {
		String arg = evt.getActionCommand();
		if (arg.equals("Open...")) {
			keyOpen();
		} else if (arg.equals("Close")) {
			keyClose();
		} else if (arg.equals("Save")) {
			keySave(currentKeyFile);
		} else if (arg.equals("Save As...")) {
			keySaveAs();
		} else if (arg.equals("Description")) {
			JCheckBoxMenuItem box = (JCheckBoxMenuItem) evt.getSource();
			setDivider(box.isSelected());
		}
		if (evt.getSource() instanceof JComboBox) {
			loadKeywordGroup();
		}
	}

	public String getKeyword(String key) {
		if (key == null) {
			return null;
		}
		synchronized (this) {
			KeywordComponent keyword = keywordHashMap.get(key.toUpperCase());
			if (keyword == null) {
				return null;
			}
			return keyword.toString();
		}
	}

	public String getKeywordDescription(String key) {
		if (key == null) {
			return null;
		}
		synchronized (this) {
			KeywordComponent keyword = keywordHashMap.get(key.toUpperCase());
			if (keyword == null) {
				return null;
			}
			return keyword.getKeywordDescription();
		}
	}

	public String getKeywordValue(String key) {
		if (key == null) {
			return null;
		}
		synchronized (this) {
			KeywordComponent keyword = keywordHashMap.get(key.toUpperCase());
			if (keyword == null) {
				return null;
			}
			String value = keyword.toString().trim();
			if (value == null) {
				return null;
			}
			int firstSpace = value.indexOf(" ");
			if (firstSpace < 1) {
				return value;
			}
			return value.substring(firstSpace, value.length());
		}
	}

	public String[] getParamFiles() {
		return paramNames;
	}

	public String getParamPath(String key) {
		return (String) paramHashtable.get(key);
	}

	private void initialize() {
		// Load the Keyword Definitions
		loadXML();
		// TextAreas
		flatfileTextArea = new JTextArea();
		flatfileTextArea.setEditable(false);
		flatfileTextArea.setFont(Font.decode("monospaced plain 12"));
		Insets insets = flatfileTextArea.getInsets();
		insets.set(5, 5, 5, 5);
		flatfileTextArea.setMargin(insets);
		// Keyword Edit Panel
		editPanel = new JPanel(flowLayout);
		ClassLoader loader = getClass().getClassLoader();
		ImageIcon icKeyPanel = new ImageIcon(loader
				.getResource("ffe/icons/keywords.png"));
		noSystemLabel.setIcon(icKeyPanel);
		ImageIcon icon = new ImageIcon(loader.getResource("ffe/icons/info.png"));
		noKeywordLabel.setIcon(icon);
		noKeywordPanel.add(noKeywordLabel);
		editScrollPane = new JScrollPane(editPanel,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		descriptScrollPane = new JScrollPane(descriptTextArea,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		Border eb = BorderFactory.createEtchedBorder(EtchedBorder.RAISED);
		descriptScrollPane.setBorder(eb);
		// Add the Keyword Group Panel and Decription Panel to a JSplitPane
		splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, editScrollPane,
				descriptScrollPane);
		splitPane.setResizeWeight(1.0);
		splitPane.setOneTouchExpandable(true);
		statusLabel.setBorder(eb);
		// Add the main pieces to the overall KeywordPanel (except the ToolBar)
		setLayout(new BorderLayout());
		add(splitPane, BorderLayout.CENTER);
		add(statusLabel, BorderLayout.SOUTH);
		// Init the GridBagContraints
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		gridBagConstraints.gridheight = 1;
		gridBagConstraints.gridwidth = 1;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		initToolBar();
		add(toolBar, BorderLayout.NORTH);
		setParamPath();
		loadPrefs();
		loadKeywordGroup();
	}

	public void initToolBar() {
		toolBar = new JToolBar("Keyword Editor");
		toolBar.setLayout(flowLayout);
		ClassLoader loader = getClass().getClassLoader();
		JButton jbopen = new JButton(new ImageIcon(loader
				.getResource("ffe/icons/open.png")));
		jbopen.setActionCommand("Open...");
		jbopen.setToolTipText("Open a *.KEY File for Editing");
		jbopen.addActionListener(this);
		jbopen.setBorderPainted(false);
		Insets insets = jbopen.getInsets();
		insets.top = 2;
		insets.bottom = 2;
		insets.left = 2;
		insets.right = 2;
		jbopen.setMargin(insets);
		toolBar.add(jbopen);
		JButton jbsave = new JButton(new ImageIcon(loader
				.getResource("ffe/icons/save.png")));
		jbsave.setActionCommand("Save");
		jbsave.setToolTipText("Save the Active *.KEY File");
		jbsave.addActionListener(this);
		jbsave.setBorderPainted(false);
		jbsave.setMargin(insets);
		toolBar.add(jbsave);
		JButton jbsaveas = new JButton(new ImageIcon(loader
				.getResource("ffe/icons/saveAs.png")));
		jbsaveas.setActionCommand("Save As...");
		jbsaveas.setToolTipText("Save the Active *.KEY File Under a New Name");
		jbsaveas.addActionListener(this);
		jbsaveas.setBorderPainted(false);
		jbsaveas.setMargin(insets);
		toolBar.add(jbsaveas);
		JButton jbclose = new JButton(new ImageIcon(loader
				.getResource("ffe/icons/close.png")));
		jbclose.setActionCommand("Close");
		jbclose.setToolTipText("Close the Active *.KEY File");
		jbclose.addActionListener(this);
		jbclose.setBorderPainted(false);
		jbclose.setMargin(insets);
		toolBar.add(jbclose);
		toolBar.addSeparator();
		groupComboBox.setMaximumSize(groupComboBox.getPreferredSize());
		groupComboBox.addActionListener(this);
		groupComboBox.setEditable(false);
		toolBar.add(groupComboBox);
		toolBar.addSeparator();
		ImageIcon icinfo = new ImageIcon(loader
				.getResource("ffe/icons/info.png"));
		descriptCheckBox = new JCheckBoxMenuItem(icinfo);
		descriptCheckBox.setActionCommand("Description");
		descriptCheckBox.setToolTipText("Show/Hide Keyword Descriptions");
		descriptCheckBox.addActionListener(this);
		descriptCheckBox.setMargin(insets);
		toolBar.add(descriptCheckBox);
		toolBar.add(new JLabel(""));
		toolBar.setBorderPainted(false);
		toolBar.setFloatable(false);
		toolBar.setRollover(true);
		toolBar.setOrientation(JToolBar.HORIZONTAL);
	}

	public boolean isFileOpen() {
		return fileOpen;
	}

	public boolean isKeyword(String key) {
		synchronized (this) {
			KeywordComponent keyword = keywordHashMap.get(key.toUpperCase());
			if (keyword == null) {
				return false;
			}
			return true;
		}
	}

	public void keyClear() {
		synchronized (this) {
			// Clear each KeywordComponent
			for (KeywordComponent kw : keywordHashMap.values()) {
				kw.clearKeywordComponent();
			}
			KeywordComponent.setKeywordModified(false);
			// Reset internal variables
			fileOpen = false;
			currentKeys = null;
			currentSystem = null;
			currentKeyFile = null;
			// Reset the View
			commentStringBuffer = new StringBuffer();
			statusLabel.setText("  ");
			loadKeywordGroup();
		}
	}

	public boolean keyClose() {
		if (KeywordComponent.isKeywordModified()) {
			return false;
		}
		keyClear();
		return true;
	}

	/*
	 * Give the user a File Dialog Box so they can select a key file.
	 */
	public void keyOpen() {
		if (fileOpen && KeywordComponent.isKeywordModified()) {
			int option = JOptionPane.showConfirmDialog(this,
					"Save Changes First", "Opening New File",
					JOptionPane.YES_NO_CANCEL_OPTION,
					JOptionPane.INFORMATION_MESSAGE);
			if (option == JOptionPane.CANCEL_OPTION) {
				return;
			} else if (option == JOptionPane.YES_OPTION) {
				keySave(currentKeyFile);
			}
			keyClear();
		}
		JFileChooser d = MainPanel.getFileChooser();
		if (currentSystem != null) {
			File cwd = currentSystem.getFile();
			if (cwd != null && cwd.getParentFile() != null) {
				d.setCurrentDirectory(cwd.getParentFile());
			}
		}
		d.setAcceptAllFileFilterUsed(false);
		d.setFileFilter(MainPanel.keyfilefilter);
		d.setDialogTitle("Open KEY File");
		int result = d.showOpenDialog(this);
		if (result == JFileChooser.APPROVE_OPTION) {
			File newKeyFile = d.getSelectedFile();
			if (newKeyFile != null && newKeyFile.exists()
					&& newKeyFile.canRead()) {
				keyOpen(newKeyFile);
			}
		}
	}

	public boolean keyOpen(File newKeyFile) {
		if (newKeyFile != null && newKeyFile.exists() && newKeyFile.canRead()) {
			Hashtable<String, Keyword> newKeys = KeyFilter.open(newKeyFile);
			if (newKeys != null && newKeys.size() > 0) {
				if (currentSystem != null) {
					currentSystem.setKeyFile(currentKeyFile);
					currentSystem.setKeywords(currentKeys);
				}
				loadActive(currentSystem, newKeys, newKeyFile);
				return true;
			}
		}
		return false;
	}

	public void keySave(File f) {
		if (f != null) {
			currentKeyFile = f;
		}
		if (!fileOpen || currentKeyFile == null) {
			return;
		}
		storeActive();
		saveKeywords(currentKeyFile, keywordHashMap, commentStringBuffer);
	}

	public void keySaveAs() {
		if (!fileOpen) {
			return;
		}
		JFileChooser d = MainPanel.getFileChooser();
		d.setDialogTitle("Save KEY File");
		d.setAcceptAllFileFilterUsed(false);
		if (currentKeyFile != null) {
			d.setCurrentDirectory(currentKeyFile.getParentFile());
			d.setSelectedFile(currentKeyFile);
		}
		d.setFileFilter(new KeyFileFilter());
		int result = d.showSaveDialog(this);
		if (result == JFileChooser.APPROVE_OPTION) {
			currentKeyFile = d.getSelectedFile();
			keySave(currentKeyFile);
		}
	}

	public boolean loadActive(FFESystem newSystem) {
		synchronized (this) {
			if (newSystem == null) {
				keyClear();
				return false;
			}
			File newKeyFile = newSystem.getKeyFile();
			Hashtable<String, Keyword> newKeys = newSystem.getKeywords();
			if (newKeyFile == null || newKeys == null) {
				return false;
			}
			return loadActive(newSystem, newKeys, newKeyFile);
		}
	}

	public boolean loadActive(FFESystem newSystem,
			Hashtable<String, Keyword> newKeys, File newKeyFile) {
		synchronized (this) {
			// Store changes made to the current system (if any) first.
			if (currentKeys != null && KeywordComponent.isKeywordModified()) {
				if (currentSystem != null && currentSystem != newSystem) {
					storeActive();
				} else {
					saveChanges();
				}
			}
			// Clear previous values
			keyClear();
			// No keywords to load
			if (newKeys == null || newKeys.size() == 0) {
				return false;
			}
			currentSystem = newSystem;
			currentKeyFile = newKeyFile;
			currentKeys = newKeys;
			fileOpen = true;
			/*
			 * Keys to remove are those entries not recognized by the FFE
			 * Keyword Editor These keys are removed from the active System's
			 * keyword Hashtable and appended to the generic "Comments"
			 * KeywordData instance.
			 */
			Vector<String> keysToRemove = new Vector<String>();
			Keyword comments = currentKeys.get("COMMENTS");
			for (Keyword keyword : currentKeys.values()) {
				String label = keyword.getKeyword();
				Vector<String> data = keyword.getEntries();
				if (label.equals("COMMENTS")) {
					continue;
				}
				KeywordComponent tk = keywordHashMap.get(label
						.toUpperCase());
				// If the keyword label isn't recognized, preserve the line
				// as a Comment
				if (tk == null) {
					keysToRemove.add(keyword.getKeyword());
					if (data.size() == 0) {
						comments.append(label);
					} else if (label.equalsIgnoreCase("MULTIPOLE")) {
						int count = 5;
						for (String entry : data) {
							count++;
							if (count > 5) {
								comments.append(label + " " + entry);
								count = 1;
							} else {
								comments.append(entry);
							}
						}
					} else if (label.equalsIgnoreCase("TORTORS")) {
						int points = 0;
						int count = 0;
						for (String entry : data) {
							count++;
							if (count > points) {
								String res[] = entry.split(" +");
								int xres = Integer.parseInt(res[5]);
								int yres = Integer.parseInt(res[5]);
								points = xres * yres;
								comments.append(label + " " + entry);
								count = 0;
							} else {
								comments.append(entry);
							}
						}
					} else {
						for (String entry : data) {
							comments.append(label + " " + entry);
						}
					}
					continue;
				}
				// No data for this keyword (something like "verbose" that
				// doesn't have modifiers)
				if (data.size() == 0) {
					tk.loadKeywordEntry(label);
				}
				// One to many data entries (something "atom" or "bond" that
				// can be repeated)
				for (String s : data) {
					tk.loadKeywordEntry(s);
				}
			}
			// Load up the Comments
			Vector<String> entries = comments.getEntries();
			if (entries != null) {
				for (String s : entries) {
					if (s.length() > 1) {
						if (!groupHashMap.containsKey(s.substring(1)
								.toUpperCase().trim())) {
							commentStringBuffer.append(s + "\n");
						}
					} else {
						commentStringBuffer.append(s + "\n");
					}
				}
			}
			// Keywords that aren't recognized have been turned into Comments
			for (String k : keysToRemove) {
				currentKeys.remove(k);
			}
			if (currentSystem != null) {
				currentSystem.setKeywords(currentKeys);
				currentSystem.setKeyFile(currentKeyFile);
			}
			loadKeywordGroup();
			return true;
		}
	}

	private void loadKeywordGroup() {
		synchronized (this) {
			editPanel.removeAll();
			String selectedGroup = (String) groupComboBox.getSelectedItem();
			if (currentKeys == null) {
				editPanel.setLayout(flowLayout);
				editPanel.add(noSystemLabel);
				int temp = splitPane.getDividerLocation();
				splitPane.setBottomComponent(noKeywordPanel);
				splitPane.setDividerLocation(temp);
			} else if (selectedGroup.equalsIgnoreCase("Flat File View")) {
				editPanel.setLayout(borderLayout);
				publishKeywords();
				editPanel.add(flatfileTextArea, BorderLayout.CENTER);
				int temp = splitPane.getDividerLocation();
				splitPane.setBottomComponent(noKeywordPanel);
				splitPane.setDividerLocation(temp);
			} else if (selectedGroup.equalsIgnoreCase("Active Keywords")) {
				gridPanel.removeAll();
				gridBagConstraints.gridy = 0;
				gridBagConstraints.anchor = GridBagConstraints.WEST;
				gridBagConstraints.gridheight = 1;
				gridBagConstraints.gridwidth = 1;
				gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
				for (KeywordComponent keywordComponent : keywordHashMap
						.values()) {
					if (keywordComponent.isActive() == true) {
						JPanel jptemp = keywordComponent.getKeywordGUI();
						gridBagLayout
								.setConstraints(jptemp, gridBagConstraints);
						gridPanel.add(jptemp);
						gridBagConstraints.gridy++;
					}
				}
				KeywordComponent.fillPanel(gridPanel, gridBagLayout,
						gridBagConstraints);
				editPanel.setLayout(flowLayout);
				editPanel.add(gridPanel);
				int temp = splitPane.getDividerLocation();
				splitPane.setBottomComponent(descriptScrollPane);
				splitPane.setDividerLocation(temp);
			} else {
				gridPanel.removeAll();
				gridBagConstraints.gridy = 0;
				gridBagConstraints.gridx = 0;
				gridBagConstraints.anchor = GridBagConstraints.WEST;
				gridBagConstraints.gridheight = 1;
				gridBagConstraints.gridwidth = 1;
				gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
				for (KeywordComponent keywordComponent : keywordHashMap
						.values()) {
					if (keywordComponent.getKeywordGroup().equalsIgnoreCase(
							selectedGroup)) {
						JPanel jptemp = keywordComponent.getKeywordGUI();
						gridBagLayout
								.setConstraints(jptemp, gridBagConstraints);
						gridPanel.add(jptemp);
						gridBagConstraints.gridy++;
					}
				}
				KeywordComponent.fillPanel(gridPanel, gridBagLayout,
						gridBagConstraints);
				editPanel.setLayout(flowLayout);
				editPanel.add(gridPanel);
				int temp = splitPane.getDividerLocation();
				splitPane.setBottomComponent(descriptScrollPane);
				splitPane.setDividerLocation(temp);
			}
			if (currentKeyFile != null) {
				statusLabel.setText("  " + currentKeyFile.toString());
			} else {
				statusLabel.setText("  ");
			}
			editScrollPane.validate();
			editScrollPane.repaint();
		}
	}

	public void loadPrefs() {
		Preferences prefs = Preferences.userNodeForPackage(KeywordPanel.class);
		descriptCheckBox.setSelected(!prefs.getBoolean(
				"KeywordPanel_description", true));
		descriptCheckBox.doClick();
	}

	/*
	 * Load up the Tinker Keyword Definitions
	 */
	private void loadXML() {
		NodeList groups, keywords, values;
		Element group, keyword, value;
		String groupName;
		String keywordName, keywordDescription, keywordGUI;
		groups = null;
		try {
			// Build the Document from the keywords.xml file
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			db.setEntityResolver(new DTDResolver());
			URL keyURL = getClass().getClassLoader().getResource(
					"ffe/tinker/keywords.xml");
			Document doc = db.parse(keyURL.openStream());
			Element document = doc.getDocumentElement();
			Element body = (Element) document.getElementsByTagName("body")
					.item(0);
			groups = body.getElementsByTagName("section");
		} catch (ParserConfigurationException e) {
			logger.warning("" + e);
		} catch (SAXException e) {
			logger.warning("" + e);
		} catch (IOException e) {
			logger.warning("" + e);
		}
		keywordHashMap = new LinkedHashMap<String, KeywordComponent>();
		groupHashMap = new LinkedHashMap<String, String>();
		groupHashMap.put("ACTIVE KEYWORDS", "Active Keywords");
		groupHashMap.put("FLAT FILE VIEW", "Flat File View");
		groupComboBox = new JComboBox();
		groupComboBox.addItem("Active Keywords");
		groupComboBox.addItem("Flat File View");
		descriptTextArea = new JTextArea();
		descriptTextArea.setLineWrap(true);
		descriptTextArea.setWrapStyleWord(true);
		Insets insets = descriptTextArea.getInsets();
		insets.set(5, 5, 5, 5);
		descriptTextArea.setMargin(insets);
		int length = groups.getLength();
		// Iterate through the Keyword Groups
		for (int i = 0; i < length; i++) {
			group = (Element) groups.item(i);
			groupName = group.getAttribute("name");
			groupComboBox.addItem(groupName);
			keywords = group.getElementsByTagName("subsection");
			int klength = keywords.getLength();
			for (int j = 0; j < klength; j++) {
				keyword = (Element) keywords.item(j);
				keywordName = keyword.getAttribute("name");
				Node text = (Node) keyword.getFirstChild();
				keywordDescription = text.getNodeValue().replace('\n', ' ');
				keywordDescription = keywordDescription.replace("  ", " ");
				keywordGUI = keyword.getAttribute("rep");
				KeywordComponent.SwingRepresentation type;
				try {
					type = KeywordComponent.SwingRepresentation
							.valueOf(keywordGUI.toUpperCase());
				} catch (Exception e) {
					type = null;
					logger.warning(keywordName
							+ ": Unknown GUI Component - " + type);
					System.exit(-1);
				}
				KeywordComponent key;
				if (type == KeywordComponent.SwingRepresentation.CHECKBOXES
						|| type == KeywordComponent.SwingRepresentation.COMBOBOX) {
					values = keyword.getElementsByTagName("Value");
					String labels[] = new String[values.getLength()];
					for (int k = 0; k < values.getLength(); k++) {
						value = (Element) values.item(k);
						labels[k] = value.getAttribute("name");
					}
					key = new KeywordComponent(keywordName, groupName, type,
							keywordDescription, descriptTextArea, labels);
				} else {
					key = new KeywordComponent(keywordName, groupName, type,
							keywordDescription, descriptTextArea);
				}
				keywordHashMap.put(keywordName.toUpperCase(), key);
				groupHashMap.put(groupName.toUpperCase(), groupName);
			}
		}
		groupComboBox.setSelectedIndex(0);
	}

	public void publishKeywords() {
		synchronized (this) {
			flatfileTextArea.setText("");
			boolean writegroup = false;
			String pgroup = null;
			// Write out keywords in groups
			for (KeywordComponent keyword : keywordHashMap.values()) {
				String group = keyword.getKeywordGroup();
				if (pgroup == null || !group.equalsIgnoreCase(pgroup)) {
					writegroup = true;
					pgroup = group;
				}
				String line = keyword.toString();
				if (line != null) {
					if (writegroup == true) {
						flatfileTextArea.append("\n");
						flatfileTextArea.append("# " + group);
						flatfileTextArea.append("\n");
						writegroup = false;
					}
					flatfileTextArea.append(line);
					flatfileTextArea.append("\n");
				}
			}
			flatfileTextArea.append("\n");
			String s = commentStringBuffer.toString();
			if (s != null && !s.trim().equals("")) {
				flatfileTextArea.append(s.trim());
			}
			flatfileTextArea.append("\n");
		}
	}

	public boolean saveChanges() {
		if (KeywordComponent.isKeywordModified() && currentKeyFile != null) {
			keySave(currentKeyFile);
			return true;
		}
		return false;
	}

	public boolean saveKeywords(File keyFile,
			LinkedHashMap<String, KeywordComponent> keywordHashMap,
			StringBuffer comments) {
		synchronized (this) {
			FileWriter fw = null;
			BufferedWriter bw = null;
			try {
				fw = new FileWriter(keyFile);
				bw = new BufferedWriter(fw);
				boolean writegroup = false;
				String pgroup = null;
				// Write out keywords in groups
				for (KeywordComponent keyword : keywordHashMap.values()) {
					String group = keyword.getKeywordGroup();
					if (pgroup == null || !group.equalsIgnoreCase(pgroup)) {
						writegroup = true;
						pgroup = group;
					}
					String line = keyword.toString();
					if (line != null) {
						if (writegroup == true) {
							bw.newLine();
							bw.write("# " + group);
							bw.newLine();
							writegroup = false;
						}
						bw.write(line);
						bw.newLine();
					}
				}
				bw.newLine();
				String s = comments.toString();
				if (s != null && !s.trim().equals("")) {
					bw.write(s.trim());
				}
				bw.newLine();
				bw.flush();
				KeywordComponent.setKeywordModified(false);
			} catch (FileNotFoundException e) {
				logger.warning(e.toString());
				return false;
			} catch (IOException e) {
				logger.warning(e.toString());
				return false;
			} finally {
				try {
					if (bw != null) {
						bw.close();
					}
					if (fw != null) {
						fw.close();
					}
				} catch (Exception e) {
					logger.warning(e.toString());
				}
			}
			return true;
		}
	}

	public void savePrefs() {
		Preferences prefs = Preferences
				.userNodeForPackage(ffe.core.KeywordPanel.class);
		prefs.putInt("KeywordPanel_divider", splitPane.getDividerLocation());
		prefs.putBoolean("KeywordPanel_description", descriptCheckBox
				.isSelected());
	}

	public void selected() {
		setDivider(descriptCheckBox.isSelected());
		validate();
		repaint();
	}

	public void setDivider(boolean b) {
		if (b) {
			descriptCheckBox.setSelected(b);
			int spDivider = (int) (this.getHeight() * (3.0f / 5.0f));
			splitPane.setDividerLocation(spDivider);
		} else {
			splitPane.setDividerLocation(1.0);
		}
	}

	/*
	 * Make the passed Keyword Group active in the editor.
	 * 
	 * @param keygroup
	 *            String
	 */
	public void setKeywordGroup(String keygroup) {
		synchronized (this) {
			keygroup = groupHashMap.get(keygroup.toUpperCase());
			if (keygroup == null) {
				return;
			}
			if (!groupComboBox.getSelectedItem().equals(keygroup)) {
				groupComboBox.setSelectedItem(keygroup);
				loadKeywordGroup();
			}
		}
	}

	/*
	 * Load a value into a KeywordComponent. Value is equivalent to one line in
	 * a Tinker KEY file, except without the keyword at the beginning. Value
	 * should be null to just indicate the Keyword is present (active). If this
	 * Keyword can apprear many times, value will be appended to the list.
	 * 
	 * @param key
	 *            String
	 * @param value
	 *            String
	 */
	public void setKeywordValue(String key, String value) {
		synchronized (this) {
			KeywordComponent keyword = keywordHashMap.get(key.toUpperCase());
			if (keyword == null) {
				return;
			}
			keyword.loadKeywordEntry(value);
			String keygroup = keyword.getKeywordGroup();
			if (!groupComboBox.getSelectedItem().equals(keygroup)) {
				groupComboBox.setSelectedItem(keygroup);
				loadKeywordGroup();
			}
			mainPanel.setPanel(MainPanel.KEYWORDS);
		}
	}

	public void setParamPath() {
		paramDir = new File(MainPanel.tinkerDir.getAbsolutePath() + File.separator
				+ "params");
		if (paramDir != null && paramDir.exists()) {
			paramFiles = paramDir.listFiles();
			paramHashtable = new Hashtable<String, String>();
			for (int i = 0; i < paramFiles.length; i++) {
				File f = paramFiles[i];
				if (f.exists() && f.canRead()
						&& MainPanel.forceFieldFileFilter.accept(f)) {
					paramHashtable.put(f.getName(), f.getAbsolutePath());
				}
			}
			int num = paramHashtable.size();
			paramNames = new String[num + 1];
			int i = 1;
			for (Enumeration e = paramHashtable.keys(); e.hasMoreElements();) {
				paramNames[i] = (String) e.nextElement();
				i++;
			}
			paramNames[0] = "AAA";
			java.util.Arrays.sort(paramNames);
			paramNames[0] = "Use an existing Tinker KEY file".intern();
		}
	}

	/*
	 * Store the KeywordPanel's current keyword content into sys.
	 * 
	 * @param sys
	 *            FFESystem
	 */
	public void store(FFESystem sys) {
		synchronized (this) {
			FFESystem back = currentSystem;
			currentSystem = sys;
			storeActive();
			currentSystem = back;
		}
	}

	/*
	 * Store the KeywordPanel's current keyword content in the activeSystem.
	 */
	public void storeActive() {
		synchronized (this) {
			if (currentSystem == null) {
				return;
			}
			// No changes
			if (!KeywordComponent.isKeywordModified()) {
				return;
			}
			Hashtable<String, Keyword> currentKeys = currentSystem
					.getKeywords();
			Hashtable<String, Keyword> newKeys = new Hashtable<String, Keyword>();
			for (KeywordComponent kc : keywordHashMap.values()) {
				if (kc.isActive() == true) {
					Keyword keywordData = null;
					if (currentKeys != null) {
						keywordData = currentKeys.get(kc.getKeyword());
					}
					if (keywordData == null) {
						keywordData = new Keyword(kc.getKeyword());
					} else {
						keywordData.clear();
					}
					kc.getKeywordData(keywordData);
					newKeys.put(kc.getKeyword(), keywordData);
				}
			}
			Keyword comments = new Keyword("COMMENTS", commentStringBuffer
					.toString());
			newKeys.put("COMMENTS", comments);
			currentSystem.setKeywords(newKeys);
		}
	}

	public String toString() {
		return "Keyword Editor";
	}
}
