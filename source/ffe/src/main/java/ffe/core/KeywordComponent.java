/*
 * <p>Title: Force Field Explorer</p>
 * <p>Description: Force Field Explorer Molecular Modeling Program</p>
 * <p>Copyright: Copyright (c) 2004-2021 Jay William Ponder</p>
 * <p>Institution: Jay Ponder Lab, Washington University in Saint Louis</p>
 * @author Michael J. Schnieders
 * @version 8.10
 */

package ffe.core;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import ffe.lang.Keyword;

/*
 * The KeywordComponent class is used to represent one Tinker keyword
 */
public final class KeywordComponent implements MouseListener, ActionListener,
		ChangeListener, DocumentListener {
	public enum SwingRepresentation {
		TEXTFIELD, CHECKBOX, CHECKBOXES, EDITCOMBOBOX, COMBOBOX, MULTIPOLE
	}

	/*
	 * This is used to test if any keyword has been modified,
	 * so it is static across all keyword objects
	 */
	private static boolean isModified = false;

	/*
	 * Some static layout variables
	 */
	private static String spaces = new String("                             ");
	
	private Logger logger = Logger.getLogger("ffe");
	
	private static Dimension labelDimension = null;

	private static Dimension entryDimension = null;

	static {
		JTextField textField = new JTextField(20);
		labelDimension = textField.getPreferredSize();
		textField = new JTextField(25);
		entryDimension = textField.getPreferredSize();
	}

	public static void fillPanel(JPanel p, GridBagLayout g, GridBagConstraints c) {
		JLabel jfill = new JLabel(" ");
		c.weightx = 1;
		c.weighty = 1;
		c.fill = GridBagConstraints.BOTH;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.gridheight = GridBagConstraints.REMAINDER;
		g.setConstraints(jfill, c);
		p.add(jfill);
	}

	public static boolean isKeywordModified() {
		return isModified;
	}

	public static void setKeywordModified(boolean b) {
		isModified = b;
	}

	/*
	 * Tinker Keyword String
	 */
	private String keyword;

	/*
	 * Tinker Keyword Group
	 */
	private String keywordGroup;

	/*
	 * An ArrayList of Components used to represent this Keyword
	 */
	private ArrayList<Component> keywordValues;

	private JPanel keywordGUI = null;

	private FlowLayout flowLayout = new FlowLayout(FlowLayout.LEFT, 5, 5);

	/*
	 * The type of Swing Conponent used in representing this Keyword
	 */
	private SwingRepresentation swingRepresentation;
	private String options[];

	private String keywordDescription;

	private JTextArea output;

	private boolean active;

	private boolean init = false;

	/*
	 * The Default Constructor k - Keyword String kg - Keyword Group
	 * t - Type of GUI Components used to represent Keyword modifiers
	 * d - Keyword description
	 */
	public KeywordComponent(String k, String kg, SwingRepresentation s,
			String d, JTextArea jta) {
		keyword = k;
		keywordGroup = kg;
		keywordValues = new ArrayList<Component>();
		swingRepresentation = s;
		keywordDescription = d;
		output = jta;
		active = false;
		flowLayout.setHgap(2);
		flowLayout.setVgap(1);
	}

	public KeywordComponent(String k, String kg, SwingRepresentation s,
			String d, JTextArea jta, String o[]) {
		this(k, kg, s, d, jta);
		options = o;
	}

	public void actionPerformed(ActionEvent evt) {
		synchronized (this) {
			isModified = true;
			if (evt.getSource() instanceof JButton) {
				JButton button = (JButton) evt.getSource();
				if (button.getText().equalsIgnoreCase("Add")) {
					JTextField text = (JTextField) keywordValues.get(3);
					String s = text.getText();
					if (s != null && !s.trim().equals("")) {
						JComboBox jcb = (JComboBox) keywordValues.get(1);
						jcb.addItem(s);
						text.setText("");
						active = true;
					}
				} else if (button.getText().equalsIgnoreCase("Remove")) {
					JComboBox jcb = (JComboBox) keywordValues.get(1);
					if (jcb.getItemCount() > 0) {
						jcb.removeItemAt(jcb.getSelectedIndex());
					}
					if (jcb.getItemCount() == 0) {
						active = false;
					}
				}
			} else if (evt.getSource() instanceof JComboBox) {
				JComboBox jcb = (JComboBox) evt.getSource();
				String selected = (String) jcb.getSelectedItem();
				if (selected == null) {
					active = false;
				} else if (selected.equalsIgnoreCase("DEFAULT")) {
					active = false;
				} else {
					active = true;
				}
			}
		}
	}

	public void changedUpdate(DocumentEvent evt) {
		isModified = true;
	}

	private void checkBoxesInit() {
		checkBoxInit();
		for (String s : options) {
			checkBoxInit(s);
		}
	}

	private void checkBoxInit() {
		JCheckBox cb = new JCheckBox(keyword, false);
		cb.setPreferredSize(labelDimension);
		cb.setMaximumSize(labelDimension);
		cb.addMouseListener(this);
		cb.addChangeListener(this);
		keywordValues.add(cb);
	}

	private void checkBoxInit(String label) {
		JCheckBox cb = new JCheckBox(label, false);
		cb.addMouseListener(this);
		cb.addChangeListener(this);
		keywordValues.add(cb);
	}

	public void clearKeywordComponent() {
		synchronized (this) {
			active = false;
			if (init == false) {
				return;
			}
			if (swingRepresentation == SwingRepresentation.CHECKBOX) {
				((JCheckBox) keywordValues.get(0)).setSelected(false);
			} else if (swingRepresentation == SwingRepresentation.CHECKBOXES) {
				for (ListIterator li = keywordValues.listIterator(); li
						.hasNext();) {
					((JCheckBox) li.next()).setSelected(false);
				}
			} else if (swingRepresentation == SwingRepresentation.COMBOBOX) {
				JComboBox jcb = (JComboBox) keywordValues.get(1);
				jcb.setSelectedItem("DEFAULT");
			} else if (swingRepresentation == SwingRepresentation.EDITCOMBOBOX) {
				((JComboBox) keywordValues.get(1)).removeAllItems();
			} else if (swingRepresentation == SwingRepresentation.TEXTFIELD) {
				((JTextField) keywordValues.get(1)).setText("");
			} else {
				logger.severe("Keyword Component: Unknown Keyword Type");
				logger.severe("Force Field Explorer can not continue...");
				System.exit(-1);
			}
		}
	}

	private void comboBoxInit() {
		JLabel jl = new JLabel(keyword);
		jl.addMouseListener(this);
		jl.setPreferredSize(labelDimension);
		jl.setMaximumSize(labelDimension);
		keywordValues.add(jl);
		JComboBox cb = new JComboBox();
		cb.setEditable(false);
		cb.addMouseListener(this);
		cb.addActionListener(this);
		cb.setPreferredSize(entryDimension);
		cb.setMaximumSize(entryDimension);
		for (String s : options) {
			cb.addItem(s);
		}
		cb.setSelectedItem("DEFAULT");
		keywordValues.add(cb);
	}

	private void editComboBoxInit() {
		JLabel jl = new JLabel(keyword);
		keywordValues.add(jl);
		jl.addMouseListener(this);
		jl.setPreferredSize(labelDimension);
		jl.setMaximumSize(labelDimension);
		JComboBox cb = new JComboBox();
		cb.setEditable(false);
		cb.addActionListener(this);
		cb.setPreferredSize(entryDimension);
		cb.setMaximumSize(entryDimension);
		keywordValues.add(cb);
		JButton remove = new JButton("Remove");
		remove.addActionListener(this);
		keywordValues.add(remove);
		JTextField textField = new JTextField();
		textField.setPreferredSize(entryDimension);
		textField.setMaximumSize(entryDimension);
		keywordValues.add(textField);
		JButton add = new JButton("Add");
		add.addActionListener(this);
		keywordValues.add(add);
	}

	/*
	 * Overidden equals method return true if object equals this,
	 * or if it is of the same class and has the same Tinker Keyword
	 */
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		} else if (object == null || getClass() != object.getClass()) {
			return false;
		}
		KeywordComponent other = (KeywordComponent) object;
		return keyword.equalsIgnoreCase(other.keyword);
	}

	public String getKeyword() {
		return keyword;
	}

	public void getKeywordData(Keyword keywordData) {
		synchronized (this) {
			if (keywordData == null || !active) {
				return;
			}
			for (Component c : keywordValues) {
				if (c instanceof JTextField) {
					JTextField tf = (JTextField) c;
					if (!tf.getText().equals("")) {
						keywordData.append(tf.getText());
					}
					break;
				} else if (c instanceof JComboBox) {
					JComboBox cb = (JComboBox) c;
					if (swingRepresentation == SwingRepresentation.COMBOBOX) {
						String s = (String) cb.getSelectedItem();
						if (s == "DEFAULT") {
							logger.warning("Keyword should not be active: "
									+ toString());
							return;
						}
						keywordData.append(s);
					} else {
						int num = cb.getItemCount();
						for (int i = 0; i < num; i++) {
							String s = (String) cb.getItemAt(i);
							keywordData.append(s);
						}
					}
					break;
				} else if (c instanceof JCheckBox) {
					JCheckBox cb = (JCheckBox) c;
					String text = cb.getText();
					if (cb.isSelected()) {
						keywordData.append(text);
					}
				}
			}
		}
	}

	public String getKeywordDescription() {
		return keywordDescription;
	}

	public String getKeywordGroup() {
		return keywordGroup;
	}

	/*
	 * Returns a JPanel with a GridLayout LayoutManager that contains a Swing
	 * representation of the Keyword and Modifiers in a single row.
	 */
	public JPanel getKeywordGUI() {
		synchronized (this) {
			if (keywordGUI == null) {
				if (!init) {
					initSwingComponents();
				}
				if (swingRepresentation == SwingRepresentation.MULTIPOLE) {
					keywordGUI.add(keywordValues.get(0));
				} else {
					keywordGUI = new JPanel(flowLayout);
					for (Component c : keywordValues) {
						keywordGUI.add(c);
					}
				}
			}
			return keywordGUI;
		}
	}

	public int hashCode() {
		return ffe.lang.HashCodeUtil.hash(106, keyword.hashCode());
	}

	private void initSwingComponents() {
		if (swingRepresentation == SwingRepresentation.CHECKBOX) {
			checkBoxInit();
		} else if (swingRepresentation == SwingRepresentation.CHECKBOXES) {
			checkBoxesInit();
		} else if (swingRepresentation == SwingRepresentation.COMBOBOX) {
			comboBoxInit();
		} else if (swingRepresentation == SwingRepresentation.EDITCOMBOBOX) {
			editComboBoxInit();
		} else if (swingRepresentation == SwingRepresentation.TEXTFIELD) {
			textFieldInit();
		} else {
			return;
		}
		init = true;
	}

	public void insertUpdate(DocumentEvent evt) {
		isModified = true;
	}

	public boolean isActive() {
		return active;
	}

	/*
	 * Load a single line Keyword entry into this KeywordComponent. Keywords
	 * that can be repeated multipule times are ComboBoxes are stored in
	 * ComboBoxes.
	 * 
	 * @param s
	 *            A Keyword line, not including the Keyword itself.
	 */
	public void loadKeywordEntry(String s) {
		synchronized (this) {
			if (!init) {
				initSwingComponents();
			}
			for (Component c : keywordValues) {
				if (c instanceof JCheckBox) {
					JCheckBox cb = (JCheckBox) c;
					if (s != null && s.equalsIgnoreCase(cb.getText())) {
						cb.setSelected(true);
					} else if (s == null) {
						cb.setSelected(false);
					}
				} else if (c instanceof JTextField) {
					JTextField tf = (JTextField) c;
					tf.setText(s);
					break;
				} else if (c instanceof JComboBox && s != null) {
					JComboBox cb = (JComboBox) c;
					if (swingRepresentation == SwingRepresentation.EDITCOMBOBOX) {
						cb.addItem(s);
					} else {
						cb.setSelectedItem(s);
					}
					break;
				}
			}
			active = true;
			if (toString() == null) {
				active = false;
			}
		}
	}

	public void mouseClicked(MouseEvent evt) {
		synchronized (this) {
			active = true;
			if (toString() == null) {
				active = false;
			}
			output.setText(keyword + ": " + keywordDescription);
			JViewport jsp = (JViewport) output.getParent();
			jsp.setViewPosition(new Point(20, 20));
		}
	}

	public void mouseEntered(MouseEvent evt) {
		mouseClicked(evt);
	}

	public void mouseExited(MouseEvent evt) {
		mouseClicked(evt);
	}

	public void mousePressed(MouseEvent evt) {
	}

	public void mouseReleased(MouseEvent evt) {
	}

	public void removeUpdate(DocumentEvent evt) {
		isModified = true;
	}

	public void stateChanged(ChangeEvent evt) {
		isModified = true;
	}

	private void textFieldInit() {
		JLabel jl = new JLabel(keyword);
		jl.addMouseListener(this);
		jl.setPreferredSize(labelDimension);
		jl.setMaximumSize(labelDimension);
		keywordValues.add(jl);
		JTextField tf;
		tf = new JTextField("");
		tf.setColumns(25);
		tf.setActionCommand(keyword);
		tf.addMouseListener(this);
		tf.getDocument().addDocumentListener(this);
		if (keyword.equalsIgnoreCase("PARAMETERS")
				|| keyword.equalsIgnoreCase("FORCEFIELD")) {
			tf.setEditable(false);
			EmptyBorder b = new EmptyBorder(1, 1, 1, 1);
			tf.setBorder(b);
			tf.setForeground(Color.BLUE);
		}
		tf.setPreferredSize(entryDimension);
		tf.setMaximumSize(entryDimension);
		keywordValues.add(tf);
	}

	/*
	 * Overridden toString methods facilitates Keyword output to a file.
	 */
	public String toString() {
		synchronized (this) {
			if (!active || !init) {
				return null;
			}
			StringBuffer s;
			// Torsion entries are long...
			if (!keyword.equalsIgnoreCase("TORSION")) {
				s = new StringBuffer(keyword
						+ spaces.substring(0, 18 - keyword.length()));
			} else {
				s = new StringBuffer(keyword);
			}
			for (Component c : keywordValues) {
				if (c instanceof JCheckBox) {
					JCheckBox cb = (JCheckBox) c;
					if (keywordValues.size() == 1) {
						if (!cb.isSelected()) {
							return null;
						}
					} else if (cb.getText().equalsIgnoreCase(keyword)) {
						if (!cb.isSelected()) {
							s = new StringBuffer();
						}
					} else {
						if (cb.isSelected()) {
							if (s.length() > 0) {
								s.append("\n" + keyword + " " + cb.getText());
							} else {
								s.append(keyword + " " + cb.getText());
							}
						} else {
							continue;
						}
					}
				} else if (c instanceof JTextField) {
					JTextField tf = (JTextField) c;
					if (tf.getText().equals("")) {
						return null;
					}
					String v = tf.getText();
					if (v.length() < 8) {
						s.append(v + spaces.substring(0, 8 - v.length()));
					} else {
						s.append(v);
					}
					break;
				} else if (c instanceof JComboBox) {
					JComboBox cb = (JComboBox) c;
					if (swingRepresentation == SwingRepresentation.EDITCOMBOBOX) {
						int count = cb.getItemCount();
						if (count == 0) {
							return null;
						}
						String entries[] = new String[count];
						for (int i = 0; i < count; i++) {
							entries[i] = (String) cb.getItemAt(i);
						}
						java.util.Arrays.sort(entries);
						StringBuffer sb = new StringBuffer();
						for (int i = 0; i < count; i++) {
							sb
									.append(keyword
											+ spaces.substring(0, 18 - keyword
													.length()));
							sb.append(entries[i].toUpperCase());
							if (i < count - 1) {
								sb.append("\n");
							}
						}
						s = sb;
					} else {
						String selection = (String) cb.getSelectedItem();
						if (selection.startsWith("DEFAULT")) {
							return null;
						} else if (!selection.equalsIgnoreCase("PRESENT")) {
							s.append(" " + selection);
						}
					}
					break;
				}
			}
			if (s.length() == 0) {
				return null;
			}
			return s.toString();
		}
	}
}
