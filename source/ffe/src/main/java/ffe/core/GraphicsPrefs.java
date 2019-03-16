/*
 * <p>Title: Force Field Explorer</p>
 * <p>Description: Force Field Explorer Molecular Modeling Program</p>
 * <p>Copyright: Copyright (c) 2004-2019 Jay William Ponder</p>
 * <p>Institution: Jay Ponder Lab, Washington University in Saint Louis</p>
 * @author Michael J. Schnieders
 * @version 8.6
 */

package ffe.core;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import ffe.lang.MSRoot;
import ffe.lang.RendererCache;

/*
 * The GraphicsPrefs class allows users to select graphics preferences.
 */
public class GraphicsPrefs extends JDialog implements ActionListener {
	/*
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private MSRoot root;

	private GridBagConstraints constraints;

	private boolean change = false;

	private Logger logger = Logger.getLogger("ffe");
	
	/*
	 * Contructor
	 * 
	 * @param frame
	 *            Parent frame
	 * @param r
	 *            Data structure root
	 */
	public GraphicsPrefs(Frame frame, MSRoot r) {
		super(frame, "", true);
		root = r;
		setTitle("Graphics Preferences");
		setSize(400, 200);
		setResizable(false);
		getContentPane().setLayout(new GridBagLayout());
		constraints = new GridBagConstraints();
		constraints.weighty = 100;
		constraints.gridheight = 1;
		constraints.gridwidth = 2;
		constraints.ipadx = 5;
		constraints.ipady = 5;
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.insets = new Insets(5, 5, 5, 5);
		// Slider for radius
		JSlider radius = new JSlider(0, 200, 1);
		radius.setMajorTickSpacing(50);
		radius.setMinorTickSpacing(10);
		radius.setPaintLabels(true);
		radius.setPaintTicks(true);
		radius.setValue((int) (RendererCache.radius * 100));
		Hashtable<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>();
		labelTable.put(new Integer(1), new JLabel("0%"));
		labelTable.put(new Integer(50), new JLabel("50%"));
		labelTable.put(new Integer(100), new JLabel("100%"));
		labelTable.put(new Integer(150), new JLabel("150%"));
		labelTable.put(new Integer(200), new JLabel("200%"));
		radius.setLabelTable(labelTable);
		addSlider(radius, " Radius", 1);
		// Slider for bondwidth
		JSlider bondwidth = new JSlider(1, 5, 5);
		bondwidth.setMajorTickSpacing(1);
		bondwidth.setMinorTickSpacing(1);
		bondwidth.setPaintLabels(true);
		bondwidth.setPaintTicks(true);
		labelTable = new Hashtable<Integer, JLabel>();
		labelTable.put(new Integer(1), new JLabel("1"));
		labelTable.put(new Integer(3), new JLabel("3"));
		labelTable.put(new Integer(5), new JLabel("5"));
		bondwidth.setLabelTable(labelTable);
		bondwidth.setValue(RendererCache.bondwidth);
		addSlider(bondwidth, " Wireframe Thickness", 3);
		// Slider for detail
		JSlider detail = new JSlider(0, 10, 3);
		detail.setMajorTickSpacing(1);
		detail.setMinorTickSpacing(1);
		detail.setPaintLabels(true);
		detail.setPaintTicks(true);
		detail.setValue((int) RendererCache.detail);
		labelTable = new Hashtable<Integer, JLabel>();
		labelTable.put(new Integer(0), new JLabel("Performance"));
		labelTable.put(new Integer(10), new JLabel("Quality"));
		detail.setLabelTable(labelTable);
		addSlider(detail, " Detail", 2);
		constraints.gridwidth = 1;
		JButton jb = new JButton("Apply");
		jb.addActionListener(this);
		getContentPane().add(jb, constraints);
		JButton jbclose = new JButton("Close");
		jbclose.addActionListener(this);
		constraints.gridx++;
		getContentPane().add(jbclose, constraints);
		pack();
		Dimension dim = getToolkit().getScreenSize();
		Dimension ddim = getSize();
		setLocation((dim.width - ddim.width) / 2,
				(dim.height - ddim.height) / 2);
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equalsIgnoreCase("Apply")) {
			if (change != false) {
				root.setView(RendererCache.ViewModel.DETAIL, null);
			}
			change = false;
		} else if (e.getActionCommand().equalsIgnoreCase("Close")) {
			if (change != false) {
				root.setView(RendererCache.ViewModel.DETAIL, null);
			}
			change = false;
			dispose();
		}
	}

	public void addSlider(JSlider s, String description, final int sliderID) {
		Border eb = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
		s.setSnapToTicks(true);
		s.setBorder(new TitledBorder(eb, description));
		s.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider) e.getSource();
				if (source.getValueIsAdjusting()) {
					return;
				}
				int value = source.getValue();
				switch (sliderID) {
				case 1:
					if (value < 1) {
						return;
					}
					double temp = value / 100.0d;
					if (temp != RendererCache.radius) {
						change = true;
						RendererCache.radius = temp;
					}
					break;
				case 2:
					if (RendererCache.detail != value) {
						change = true;
						RendererCache.detail = value;
					}
					break;
				case 3:
					if (RendererCache.bondwidth != value) {
						change = true;
						RendererCache.bondwidth = value;
					}
					break;
				default:
					logger.info("Unknown Slider");
				}
			}
		});
		// add three components into the next row
		constraints.gridx = 0;
		constraints.anchor = GridBagConstraints.WEST;
		constraints.fill = GridBagConstraints.NONE;
		getContentPane().add(s, constraints);
		constraints.gridy++;
	}
}
