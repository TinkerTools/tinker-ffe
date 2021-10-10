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
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Hashtable;
import java.util.Vector;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;

/*
 * The LogPanel is a very simple editor that displays log files
 * created by Tinker jobs. Any text file can be edited.
 */
public class LogPanel extends JPanel implements ActionListener {
	/*
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Logger logger = Logger.getLogger("ffe");
	
	private MainPanel mainPanel;

	private Vector<Thread> tinkerThreads;

	// A Hashtable of JTextAreas labeled by absolute path to the Log File each
	// displays
	private Hashtable<String, JTextArea> logFiles = new Hashtable<String, JTextArea>();

	// LogPanel GUI Components
	private JToolBar toolBar;

	private JTabbedPane resultsTabbedPane = new JTabbedPane(JTabbedPane.BOTTOM);

	private JProgressBar statusProgressBar;

	private JLabel status = new JLabel("  ");

	private Font font;

	private JLabel noLogsLabel = new JLabel("Tinker logs are displayed here");

	private JPanel noLogsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5,
			5));

	EtchedBorder eb = new EtchedBorder(EtchedBorder.RAISED);

	/*
	 * Constructor
	 * 
	 * @param f
	 */
	public LogPanel(MainPanel f) {
		mainPanel = f;
		tinkerThreads = mainPanel.getModelingPanel().getModelingJobs();
		initToolBar();
		setLayout(new BorderLayout());
		Border eb = BorderFactory.createEtchedBorder(EtchedBorder.RAISED);
		statusProgressBar = new JProgressBar();
		statusProgressBar.setBorder(eb);
		statusProgressBar.setStringPainted(true);
		ClassLoader loader = getClass().getClassLoader();
		ImageIcon icon = new ImageIcon(loader.getResource("ffe/icons/data.png"));
		noLogsLabel.setIcon(icon);
		noLogsPanel.add(noLogsLabel);
		noLogsPanel.setBorder(eb);
		status.setBorder(eb);
		add(toolBar, BorderLayout.NORTH);
		add(noLogsPanel, BorderLayout.CENTER);
		add(status, BorderLayout.SOUTH);
		font = Font.decode("Monospaced");
		refreshStatus();
	}

	public void actionPerformed(ActionEvent evt) {
		if (evt.getSource() instanceof javax.swing.Timer) {
			if (resultsTabbedPane.getTabCount() > 0) {
				refresh();
			}
			return;
		}
		String arg = evt.getActionCommand();
		if (arg == null) {
			return;
		}
		if (arg.equalsIgnoreCase("Refresh")) {
			refresh();
		} else if (arg.equalsIgnoreCase("Close")) {
			close();
		} else if (arg.equalsIgnoreCase("Close All")) {
			closeAll();
		} else if (arg.equalsIgnoreCase("Open...")) {
			JFileChooser d = MainPanel.getFileChooser();
			d.setAcceptAllFileFilterUsed(true);
			d.setDialogTitle("Open Log File");
			int result = d.showOpenDialog(this);
			if (result == JFileChooser.APPROVE_OPTION) {
				File f = d.getSelectedFile();
				addPane(f);
			}
		} else if (arg.equalsIgnoreCase("Save")) {
			saveSelected();
		} else if (arg.equalsIgnoreCase("Save As...")) {
			saveSelectedAs();
		}
	}

	private void addPane(File logFile) {
		if (!logFile.exists()) {
			return;
		}
		JTextArea logTextArea = new JTextArea();
		logTextArea.setEditable(logFile.canWrite());
		logTextArea.setFont(font);
		logFiles.put(logFile.getAbsolutePath(), logTextArea);
		JScrollPane scrollPane = new JScrollPane(logTextArea,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		scrollPane.setBorder(eb);
		resultsTabbedPane.add(scrollPane, logFile.getAbsolutePath());
		resultsTabbedPane.setSelectedIndex(resultsTabbedPane
				.getComponentCount() - 1);
		if (resultsTabbedPane.getComponentCount() == 1) {
			remove(noLogsPanel);
			add(resultsTabbedPane, BorderLayout.CENTER);
			validate();
			repaint();
		}
		loadText(logTextArea, logFile);
	}

	public void close() {
		int index = resultsTabbedPane.getSelectedIndex();
		if (index >= 0) {
			String title = resultsTabbedPane.getTitleAt(index);
			resultsTabbedPane.remove(index);
			logFiles.remove(title);
			for (Thread t : tinkerThreads) {
				String name = t.getName();
				if (name.equals(title)) {
					tinkerThreads.remove(t);
					break;
				}
			}
			if (resultsTabbedPane.getComponentCount() == 0) {
				remove(resultsTabbedPane);
				add(noLogsPanel, BorderLayout.CENTER);
			}
			validate();
			repaint();
		}
	}

	public void close(String file) {
		synchronized (this) {
			int index = -1;
			for (int i = 0; i < resultsTabbedPane.getTabCount(); i++) {
				String title = resultsTabbedPane.getTitleAt(i);
				if (file.equals(title)) {
					index = i;
					break;
				}
			}
			if (index < 0)
				return;

			String title = resultsTabbedPane.getTitleAt(index);
			resultsTabbedPane.remove(index);
			logFiles.remove(title);
			for (Thread t : tinkerThreads) {
				String name = t.getName();
				if (name.equals(title)) {
					tinkerThreads.remove(t);
					break;
				}
			}
			if (resultsTabbedPane.getComponentCount() == 0) {
				remove(resultsTabbedPane);
				add(noLogsPanel, BorderLayout.CENTER);
			}
			validate();
			repaint();
		}
	}

	public void closeAll() {
		synchronized (this) {
			resultsTabbedPane.removeAll();
			logFiles.clear();
			tinkerThreads.clear();
		}
	}

	public JProgressBar getProgressBar() {
		return statusProgressBar;
	}

	private void initToolBar() {
		toolBar = new JToolBar("Results");
		toolBar.setLayout(new FlowLayout(FlowLayout.LEFT));
		JButton jbrefresh = new JButton(new ImageIcon(getClass()
				.getClassLoader().getResource("ffe/icons/refresh.png")));
		jbrefresh.setActionCommand("Refresh");
		jbrefresh.setToolTipText("Refresh the Logs Panel");
		jbrefresh.addActionListener(this);
		jbrefresh.setBorderPainted(false);
		Insets insets = jbrefresh.getInsets();
		insets.top = 2;
		insets.bottom = 2;
		insets.left = 2;
		insets.right = 2;
		jbrefresh.setMargin(insets);
		toolBar.add(jbrefresh);
		toolBar.addSeparator();
		JButton jbopen = new JButton(new ImageIcon(getClass().getClassLoader()
				.getResource("ffe/icons/open.png")));
		jbopen.setActionCommand("Open...");
		jbopen.setToolTipText("Open any Text File");
		jbopen.addActionListener(this);
		jbopen.setBorderPainted(false);
		jbopen.setMargin(insets);
		toolBar.add(jbopen);
		JButton jbsave = new JButton(new ImageIcon(getClass().getClassLoader()
				.getResource("ffe/icons/save.png")));
		jbsave.setActionCommand("Save");
		jbsave.setToolTipText("Save the Active File");
		jbsave.addActionListener(this);
		jbsave.setBorderPainted(false);
		jbsave.setMargin(insets);
		toolBar.add(jbsave);
		JButton jbsaveas = new JButton(new ImageIcon(getClass()
				.getClassLoader().getResource("ffe/icons/saveAs.png")));
		jbsaveas.setActionCommand("Save As...");
		jbsaveas.setToolTipText("Save the Active Text File Under a New Name");
		jbsaveas.addActionListener(this);
		jbsaveas.setBorderPainted(false);
		jbsaveas.setMargin(insets);
		toolBar.add(jbsaveas);
		JButton jbclose = new JButton(new ImageIcon(getClass().getClassLoader()
				.getResource("ffe/icons/close.png")));
		jbclose.setActionCommand("Close");
		jbclose.setToolTipText("Close the Active Text File");
		jbclose.addActionListener(this);
		jbclose.setBorderPainted(false);
		jbclose.setMargin(insets);
		toolBar.add(jbclose);
		JButton jbcloseall = new JButton(new ImageIcon(getClass()
				.getClassLoader().getResource("ffe/icons/closeAll.png")));
		jbcloseall.setActionCommand("Close All");
		jbcloseall.setToolTipText("Close All Open Text Files");
		jbcloseall.addActionListener(this);
		jbcloseall.setBorderPainted(false);
		jbcloseall.setMargin(insets);
		toolBar.add(jbcloseall);
		toolBar.setFloatable(false);
		toolBar.setRollover(true);
		toolBar.setOrientation(JToolBar.HORIZONTAL);
	}

	private void loadText(JTextArea logTextArea, File logFile) {
		if (logTextArea == null || (!logFile.exists()) || (!logFile.canRead())) {
			return;
		}
		logTextArea.setText("");
		try {
			FileInputStream inputStream = new FileInputStream(logFile);
			BufferedReader bufferedReader = new BufferedReader(
					new InputStreamReader(inputStream));
			while (bufferedReader.ready()) {
				logTextArea.append(bufferedReader.readLine() + "\n");
			}
			bufferedReader.close();
			inputStream.close();
		} catch (IOException e) {
			logger.severe("" + e);
		}
		mainPanel.setPanel(MainPanel.LOGS);
	}

	public void refresh() {
		synchronized (tinkerThreads) {
			for (Thread t : tinkerThreads) {
				File file = new File(t.getName());
				if (!file.exists()) {
					continue;
				}
				// Refresh an already existing JTextArea
				if (logFiles.containsKey(file.getAbsolutePath())) {
					JTextArea ta = logFiles.get(file.getAbsolutePath());
					loadText(ta, file);
				}
				// Create a new JTextArea for the file
				else {
					addPane(file);
				}
			}
		}
		refreshStatus();
		validate();
		repaint();
	}

	public void refreshStatus() {
		int count = tinkerThreads.size();
		if (count == 0) {
			statusProgressBar.setString("");
			statusProgressBar.setIndeterminate(false);
		} else if (count == 1) {
			statusProgressBar.setString("1 Job Running");
			statusProgressBar.setIndeterminate(true);
		} else {
			statusProgressBar.setString("" + count + " Jobs Running");
			statusProgressBar.setIndeterminate(true);
		}
	}

	private void saveSelected() {
		int index = resultsTabbedPane.getSelectedIndex();
		if (index < 0) {
			return;
		}
		String title = resultsTabbedPane.getTitleAt(index);
		JTextArea logTextArea = logFiles.get(title);
		File logFile = new File(title);
		if (logTextArea != null && logFile.exists() && logFile.canWrite()) {
			try {
				FileOutputStream outputStream = new FileOutputStream(logFile);
				BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
						outputStream));
				bw.write(logTextArea.getText());
				bw.close();
				outputStream.close();
			} catch (IOException e) {
				logger.severe("" + e);
			}
		}
	}

	private void saveSelectedAs() {
		synchronized (resultsTabbedPane) {
			int index = resultsTabbedPane.getSelectedIndex();
			if (index < 0) {
				return;
			}
			String title = resultsTabbedPane.getTitleAt(index);
			JTextArea logTextArea = logFiles.get(title);
			File logFile = new File(title);
			if (logTextArea != null && logFile.exists() && logFile.canWrite()) {
				JFileChooser fileChooser = MainPanel.getFileChooser();
				fileChooser.setSelectedFile(logFile);
				fileChooser.setAcceptAllFileFilterUsed(true);
				int result = fileChooser.showSaveDialog(this);
				if (result == JFileChooser.APPROVE_OPTION) {
					logFile = fileChooser.getSelectedFile();
				} else {
					return;
				}
				try {
					logFiles.remove(title);
					FileOutputStream outputStream = new FileOutputStream(
							logFile);
					BufferedWriter bw = new BufferedWriter(
							new OutputStreamWriter(outputStream));
					bw.write(logTextArea.getText());
					bw.close();
					outputStream.close();
					resultsTabbedPane.setTitleAt(index, logFile
							.getAbsolutePath());
					logFiles.put(logFile.getAbsolutePath(), logTextArea);
				} catch (IOException e) {
					logger.severe("" + e);
				}
			}
		}
	}

	public void selected() {
		validate();
		repaint();
	}

	public void setDone(String logFileName) {
		synchronized (this) {
			if (logFileName == null) {
				return;
			}
			File logFile = new File(logFileName);
			for (Thread thread : tinkerThreads) {
				String jobName = thread.getName();
				if (jobName.equals(logFileName)) {
					tinkerThreads.remove(thread);
					break;
				}
			}
			if (!logFile.exists()) {
				return;
			}
			if (logFiles.containsKey(logFile.getAbsolutePath())) {
				JTextArea logTextArea = logFiles.get(logFile.getAbsolutePath());
				loadText(logTextArea, logFile);
			}
			// Create a new TextArea for the file
			else {
				addPane(logFile);
			}
			resultsTabbedPane.setSelectedIndex(resultsTabbedPane
					.indexOfTab(logFile.getAbsolutePath()));
			refreshStatus();
		}
	}

	public String toString() {
		return "Logging";
	}
}
