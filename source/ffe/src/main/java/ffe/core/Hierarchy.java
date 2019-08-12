/*
 * <p>Title: Force Field Explorer</p>
 * <p>Description: Force Field Explorer Molecular Modeling Program</p>
 * <p>Copyright: Copyright (c) 2004-2019 Jay William Ponder</p>
 * <p>Institution: Jay Ponder Lab, Washington University in Saint Louis</p>
 * @author Michael J. Schnieders
 * @version 8.7
 */

package ffe.core;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.ListIterator;

import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.RowMapper;
import javax.swing.tree.TreePath;

import ffe.lang.MSNode;
import ffe.lang.MSRoot;
import ffe.lang.ROLSP;
import ffe.lang.RendererCache;

/*
 * The Hierarchy Class creates and manages a JTree view of the data structure.
 * It is used for synchronization, handles the selection mechanism, and sets
 * the active system and nodes.
 */
public final class Hierarchy extends JTree implements TreeSelectionListener {
	/*
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private MSRoot root;

	private MainPanel mainPanel;

	private DefaultTreeModel treeModel;

	private DefaultTreeSelectionModel treeSelectionModel;

	private FFESystem activeSystem = null; // Reference to the active FSystem

	private MSNode activeNode = null; // Reference to the last Selected Node

	private ArrayList<MSNode> activeNodes = new ArrayList<MSNode>();

	private JLabel status = null;

	private JLabel step = null;

	private JLabel energy = null;

	ArrayList<TreePath> newPaths = new ArrayList<TreePath>();

	ArrayList<TreePath> removedPaths = new ArrayList<TreePath>();

	ArrayList<TreePath> prePaths = new ArrayList<TreePath>();

	ArrayList<MSNode> picks = new ArrayList<MSNode>();

	TreePath nullPath = null;

	/*
	 * The Default Constructor initializes JTree properties, then updates
	 * the representation based on the Structure of the Tree that extends
	 * from the Root argument.
	 */
	public Hierarchy(MainPanel f) {
		mainPanel = f;
		root = mainPanel.getDataRoot();
		initTree();
	}

	public void addSelection(MSNode f) {
		if (f == null) {
			return;
		}
		synchronized (this) {
			TreePath path = new TreePath(f.getPath());
			try {
				addSelectionPath(path);
			} catch(Exception e) {}
			f.setSelected(true);
		}
	}

	public void addSelections(ArrayList<MSNode> a) {
		synchronized (this) {
			for (MSNode f : a) {
				addSelection(f);
			}
		}
	}

	public void addSystemNode(FFESystem newSystem) {
		addTreeNode(newSystem, root, root.getChildCount());
	}

	public void addTreeNode(MSNode nodeToAdd, MSNode parent, int index) {
		if (nodeToAdd == null || nodeToAdd.getParent() != null) {
			return;
		}
		synchronized (this) {
			int childCount = parent.getChildCount();
			if (index < 0 || index > childCount) {
				index = parent.getChildCount();
			}
			// Add a parallel node if the ffe.lang.parallel flag was set
			if (ROLSP.GO_PARALLEL) {
				ROLSP parallelNode = new ROLSP();
				parallelNode.add(nodeToAdd);
				treeModel.insertNodeInto(parallelNode, parent, index);
			} else {
				treeModel.insertNodeInto(nodeToAdd, parent, index);
			}
			if (nodeToAdd instanceof FFESystem) {
				attach((FFESystem) nodeToAdd);
				treeModel.nodeStructureChanged(nodeToAdd);
			}
			onlySelection(nodeToAdd);
			if (!isRootVisible()) {
				setRootVisible(true);
			}
		}
	}

	private void attach(FFESystem newModel) {
		if (newModel == null) {
			return;
		}
		newModel.finalize(true);
		mainPanel.getGraphics3D().attachModel(newModel);
		if (newModel.getBondList().size() == 0) {
			mainPanel.getGraphics3D().updateScene(newModel, false, true,
					RendererCache.ViewModel.SPACEFILL, false, null);
		}
	}

	public void collapseAll() {
		int row = getRowCount() - 1;
		while (row >= 0) {
			collapseRow(row);
			row--;
		}
	}

	/*
	 * Removes a MolecularSystem from the Root and sets the first
	 * child FSystem to be active.
	 */
	private void detach(FFESystem closedModel) {
		if (closedModel == null) {
			return;
		}
		closedModel.setView(RendererCache.ViewModel.DESTROY, null);
		FileCloser cf = new FileCloser(closedModel);
		Thread t = new Thread(cf);
		t.setPriority(Thread.MIN_PRIORITY);
		t.start();
	}

	/*
	 * Returns the active FSystem.
	 */
	public FFESystem getActive() {
		return activeSystem;
	}

	public MSNode getActiveNode() {
		return activeNode;
	}

	public ArrayList<MSNode> getActiveNodes() {
		return activeNodes;
	}

	public FFESystem[] getNonActiveSystems() {
		synchronized (this) {
			int childCount = root.getChildCount();
			if (childCount == 0) {
				return null;
			}
			FFESystem[] systems = new FFESystem[childCount - 1];
			int index = 0;
			for (Enumeration e = root.children(); e.hasMoreElements();) {
				FFESystem system = (FFESystem) e.nextElement();
				if (system != getActive()) {
					systems[index++] = system;
				}
			}
			return systems;
		}
	}

	public FFESystem[] getSystems() {
		synchronized (this) {
			int childCount = root.getChildCount();
			if (childCount == 0) {
				return null;
			}
			FFESystem[] systems = new FFESystem[childCount];
			int index = 0;
			for (Enumeration e = root.children(); e.hasMoreElements();) {
				systems[index++] = (FFESystem) e.nextElement();
			}
			return systems;
		}
	}

	public void groupSelection(MSNode f1, MSNode f2) {
		if (f1 == null || f2 == null) {
			return;
		}
		synchronized (this) {
			TreePath paths[] = new TreePath[2];
			paths[0] = new TreePath(f1.getPath());
			paths[1] = new TreePath(f2.getPath());
			RowMapper rm = treeSelectionModel.getRowMapper();
			int[] rows = rm.getRowsForPaths(paths);
			setSelectionInterval(rows[0], rows[1]);
		}
	}

	/*
	 * Initialize the Tree representation based on the Root data node
	 */
	public void initTree() {
		addTreeSelectionListener(this);
		setExpandsSelectedPaths(true);
		setScrollsOnExpand(true);
		setLargeModel(true);
		setEditable(false);
		putClientProperty("JTree.lineStyle", "Angled");
		setShowsRootHandles(true);
		DefaultTreeCellRenderer tcr = new DefaultTreeCellRenderer();
		tcr.setBackgroundSelectionColor(Color.yellow);
		tcr.setBorderSelectionColor(Color.black);
		tcr.setTextSelectionColor(Color.black);
		setCellRenderer(tcr);
		treeModel = new DefaultTreeModel(root);
		treeSelectionModel = new DefaultTreeSelectionModel();
		setModel(treeModel);
		setSelectionModel(treeSelectionModel);
		setRootVisible(false);
	}

	public void onlySelection(MSNode f) {
		synchronized (this) {
			if (activeNodes != null) {
				int num = activeNodes.size();
				TreePath paths[] = new TreePath[num];
				for (int i = 0; i < num; i++) {
					paths[i] = new TreePath(((MSNode) activeNodes.get(i))
							.getPath());
				}
				removeSelectionPaths(paths);
			}
			collapseAll();
			addSelection(f);
		}
	}

	public void removeSelection(MSNode f) {
		if (f == null) {
			return;
		}
		synchronized (this) {
			TreePath path = new TreePath(f.getPath());
			for (Enumeration e = getExpandedDescendants(path); e
					.hasMoreElements();) {
				TreePath treePath = new TreePath((DefaultMutableTreeNode) e
						.nextElement());
				collapsePath(treePath);
			}
			removeSelectionPath(path);
			f.setSelected(false);
		}
	}

	public void removeSelections(ArrayList<MSNode> a) {
		synchronized (this) {
			for (MSNode f : a) {
				removeSelection(f);
			}
		}
	}

	public void removeTreeNode(MSNode nodeToRemove) {
		if (nodeToRemove == null) {
			return;
		}
		synchronized (this) {
			if (root.getChildCount() <= 1) {
				setRootVisible(false);
			}
			treeModel.removeNodeFromParent(nodeToRemove);
			if (getActive() == nodeToRemove && root.getChildCount() != 0) {
				FFESystem m = (FFESystem) root.getChildAt(0);
				setActive(m);
				onlySelection(activeSystem);
			} else {
				setActive(null);
			}
			if (nodeToRemove instanceof FFESystem) {
				detach((FFESystem) nodeToRemove);
			}
		}
	}

	public void selectAll() {
		if (activeSystem == null) {
			return;
		}
		synchronized (this) {
			onlySelection(root);
		}
	}

	/*
	 * Sets the FSystem s to be active.
	 */
	public void setActive(FFESystem s) {
		if (s == activeSystem) {
			return;
		}
		synchronized (this) {
			activeSystem = s;
			updateStatus();
			if (mainPanel.getKeywordPanel() != null) {
				mainPanel.getKeywordPanel().loadActive(activeSystem);
			}
			if (mainPanel.getModelingPanel() != null) {
				mainPanel.getModelingPanel().loadActive(activeSystem);
			}
			if (mainPanel.getModelingShell() != null) {
				mainPanel.getModelingShell().sync();
			}
		}
	}

	public void setActive(int i) {
		synchronized (this) {
			if (i < root.getChildCount()) {
				setActive((FFESystem) root.getChildAt(i));
			} else if (root.getChildCount() == 0) {
				setActive(null);
			}
		}
	}

	public void setHighlighting(boolean h) {
		synchronized (this) {
			if (RendererCache.highlightSelections != h) {
				RendererCache.highlightSelections = h;
				for (MSNode node : activeNodes) {
					node.setSelected(h);
				}
				mainPanel.getGraphics3D().updateScene(activeNodes, false,
						false, null, true, RendererCache.ColorModel.SELECT);
			}
		}
	}

	public void setStatus(JLabel s, JLabel t, JLabel e) {
		status = s;
		step = t;
		energy = e;
	}

	public void toggleSelection(MSNode f) {
		if (f == null) {
			return;
		}
		synchronized (this) {
			TreePath path = new TreePath(f.getPath());
			if (isPathSelected(path)) {
				removeSelectionPath(path);
			} else {
				addSelectionPath(path);
			}
		}
	}

	public void toggleSelections(ArrayList<MSNode> a) {
		synchronized (this) {
			for (MSNode f : a){
				toggleSelection(f);
			}
		}
	}
	/*
	 * public void actionPerformed (ActionEvent e) { }
	 * 
	 * public void mouseClicked (MouseEvent e) { }
	 * 
	 * public void mousePressed (MouseEvent e) { System.out.println(e); if
	 * (e.getButton() == MouseEvent.BUTTON3){ JPopupMenu popUp = createPopUp();
	 * if (popUp!=null) { popUp.show(e.getComponent(), e.getX(), e.getY()); } } }
	 * 
	 * public void mouseReleased (MouseEvent e) { }
	 * 
	 * public void mouseEntered (MouseEvent e) { }
	 * 
	 * public void mouseExited (MouseEvent e) { }
	 * 
	 * private JPopupMenu createPopUp () { if (getActive() == null){ return
	 * null; } JPopupMenu popup = new JPopupMenu(); FFESystem system =
	 * getActive(); popup.setName(system.getName()); File file =
	 * system.getFile(); if (file != null){ JMenuItem menuItem = new
	 * JMenuItem("Rename " + file.getName()); menuItem.addActionListener(this);
	 * popup.add(menuItem); } return popup; }
	 */

	public String toString() {
		return "Structural Hierarchy";
	}

	public void updateStatus() {
		if (activeSystem == null) {
			status.setText("  ");
			step.setText("  ");
			energy.setText("  ");
			return;
		}
		if (activeSystem.getFile() != null) {
			status.setText("  " + activeSystem.toFFString());
		} else {
			status.setText("  " + activeSystem.toString());
		}
		if (activeSystem.isSimulation()) {
			step.setText(activeSystem.getTimeString() + " ");
		} else if (activeSystem.isOptimization()) {
			step.setText(activeSystem.getStepString() + " ");
		} else if (activeSystem.getCycles() > 1) {
			step.setText("" + activeSystem.getCurrentCycle() + "/"
					+ activeSystem.getCycles());
		} else {
			step.setText("");
		}
		if (activeSystem.isSimulation()) {
			energy.setText(activeSystem.getEnergyString() + " ");
		} else if (activeSystem.isOptimization()) {
			energy.setText(activeSystem.getEnergyString() + " ");
		} else if (activeSystem.getCycles() > 1) {
			energy.setText("");
		} else {
			energy.setText("");
		}
	}

	public void valueChanged(TreeSelectionEvent e) {
		synchronized (this) {
			// Determine the Active System
			MSNode lastNode = (MSNode) getLastSelectedPathComponent();
			if (lastNode != null) {
				activeNode = lastNode;
				FFESystem s = (FFESystem) activeNode.getMSNode(FFESystem.class);
				if (s != null) {
					setActive(s);
				}
			}
			TreePath[] paths = e.getPaths();
			if (paths == null) {
				return;
			}
			// Reuse the same ArrayLists
			ArrayList<TreePath> temp = prePaths;
			prePaths = newPaths;
			newPaths = temp;
			// Determine new and removed paths
			newPaths.clear();
			removedPaths.clear();
			for (int i = 0; i < paths.length; i++) {
				if (e.isAddedPath(i)) {
					newPaths.add(paths[i]);
				} else {
					removedPaths.add(paths[i]);
				}
			}
			// Create a non-redundant set of new/removed paths
			TreePath pathi, pathj;
			for (int i = 0; i < newPaths.size(); i++) {
				pathi = (TreePath) newPaths.get(i);
				if (pathi == nullPath) {
					continue;
				}
				for (int j = i + 1; j < newPaths.size(); j++) {
					pathj = (TreePath) newPaths.get(j);
					if (pathi == nullPath || pathj == nullPath) {
						continue;
					}
					if (pathi.isDescendant(pathj)) {
						newPaths.set(j, nullPath);
					} else if (pathj.isDescendant(pathi)) {
						newPaths.set(i, nullPath);
					}
				}
			}
			boolean check = true;
			while (check) {
				check = newPaths.remove(nullPath);
			}
			for (int i = 0; i < removedPaths.size(); i++) {
				pathi = (TreePath) removedPaths.get(i);
				if (pathi == nullPath) {
					continue;
				}
				for (int j = i + 1; j < removedPaths.size(); j++) {
					pathj = (TreePath) removedPaths.get(j);
					if (pathi == nullPath || pathj == nullPath) {
						continue;
					}
					if (pathi.isDescendant(pathj)) {
						removedPaths.set(j, nullPath);
					} else if (pathj.isDescendant(pathi)) {
						removedPaths.set(i, nullPath);
					}
				}
			}
			check = true;
			while (check) {
				check = removedPaths.remove(nullPath);
			}
			// Remove the RemovedPaths from the Existing List
			for (int i = 0; i < prePaths.size(); i++) {
				pathi = (TreePath) prePaths.get(i);
				for (int j = 0; j < removedPaths.size(); j++) {
					pathj = (TreePath) removedPaths.get(j);
					if (pathj.isDescendant(pathi)) {
						prePaths.set(i, nullPath);
						break;
					}
				}
			}
			check = true;
			while (check) {
				check = prePaths.remove(nullPath);
			}
			// Combine new Paths and Existing Paths non-redundantly
			for (int i = 0; i < newPaths.size(); i++) {
				pathi = (TreePath) newPaths.get(i);
				if (pathi == nullPath) {
					continue;
				}
				for (int j = 0; j < prePaths.size(); j++) {
					pathj = (TreePath) prePaths.get(j);
					if (pathj == nullPath) {
						continue;
					}
					if (pathi == nullPath || pathj == nullPath) {
						continue;
					}
					if (pathi.isDescendant(pathj)) {
						prePaths.set(j, nullPath);
					} else if (pathj.isDescendant(pathi)) {
						newPaths.set(i, nullPath);
					}
				}
			}
			check = true;
			while (check) {
				check = newPaths.remove(nullPath);
			}
			check = true;
			while (check) {
				check = prePaths.remove(nullPath);
			}
			newPaths.addAll(prePaths);
			activeNodes.clear();
			for (int i = 0; i < newPaths.size(); i++) {
				pathi = (TreePath) newPaths.get(i);
				activeNodes.add((MSNode) pathi.getLastPathComponent());
			}
			if (activeNode != null) {
				TreePath activePath = new TreePath(activeNode);
				expandPath(activePath.getParentPath());
				makeVisible(activePath);
				scrollPathToVisible(activePath);
			}
			// We now have a non-redundant set of Active Paths; and a
			// non-redundant set of removed paths
			picks = new ArrayList<MSNode>();
			// Clear highlight of de-selected nodes
			for (TreePath r : removedPaths) {
				boolean change = true;
				for (TreePath n : newPaths) {
					if (n.isDescendant(r)) {
						change = false;
					}
				}
				if (change) {
					MSNode f = (MSNode) r.getLastPathComponent();
					f.setSelected(false);
					picks.add(f);
				}
			}
			for (TreePath n : newPaths) {
				boolean change = true;
				for (TreePath p : prePaths) {
					if (p.isDescendant(n)) {
						change = false;
					}
				}
				if (change) {
					MSNode f = (MSNode) n.getLastPathComponent();
					f.setSelected(true);
					picks.add(f);
				}
			}
			if (RendererCache.highlightSelections) {
				mainPanel.getGraphics3D().updateScene(picks, false, false,
						null, true, RendererCache.ColorModel.SELECT);
			} else if (RendererCache.labelAtoms || RendererCache.labelResidues) {
				mainPanel.getGraphics3D().setLabelsUpdated();
			}
		}
	}
}
