/*
 * <p>Title: Force Field Explorer</p>
 * <p>Description: Force Field Explorer Molecular Modeling Program</p>
 * <p>Copyright: Copyright (c) 2004-2021 Jay William Ponder</p>
 * <p>Institution: Jay Ponder Lab, Washington University in Saint Louis</p>
 * @author Michael J. Schnieders
 * @version 8.10
 */

package ffe.core;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;

import ffe.lang.MolecularAssembly;

/*
 * The Trajectory class controls playback of a Tinker trajectory
 */
public class Trajectory implements ActionListener {
	private MolecularAssembly molecularSystem;

	private MainPanel mainPanel;

	private Timer timer;

	private int delay = 50;

	private int desiredspeed = 20;

	private int cycle = 1;

	private int sign = 1;

	private int skip = 1;

	private boolean oscillate = false;

	public Trajectory(MolecularAssembly mol, MainPanel f) {
		molecularSystem = mol;
		mainPanel = f;
		timer = new Timer(delay, this);
		timer.setCoalesce(true);
	}

	public void actionPerformed(ActionEvent e) {
		if (mainPanel.getGraphics3D().isCacheFull()) {
			return;
		}
		cycle = advance(skip * sign);
		setFrame(cycle);
	}

	private int advance(int adv) {
		if (molecularSystem != null) {
			cycle = molecularSystem.getCurrentCycle();
			int frame = cycle + adv;
			if ((frame) <= 0) {
				sign = 1;
				if (oscillate) {
					frame = -adv - cycle;
				} else {
					frame = molecularSystem.getCycles() + (adv + cycle);
				}
			} else if ((frame) > molecularSystem.getCycles()) {
				if (oscillate) {
					frame = molecularSystem.getCycles() + (-adv + cycle);
					sign = -1;
				} else {
					sign = 1;
					frame = cycle - molecularSystem.getCycles() + adv;
				}
			}
			return frame;
		}
		return 0;
	}

	public void back() {
		setFrame(getFrame() - 1);
	}

	public void forward() {
		setFrame(getFrame() + 1);
	}

	public int getFrame() {
		return molecularSystem.getCurrentCycle();
	}

	public MolecularAssembly getFSystem() {
		return molecularSystem;
	}

	public boolean getOscillate() {
		return oscillate;
	}

	public int getRate() {
		return desiredspeed;
	}

	public int getSkip() {
		return skip;
	}

	public void rewind() {
		setFrame(1);
	}

	public void setFrame(int f) {
		if (molecularSystem != null) {
			molecularSystem.setCurrentCycle(f);
			mainPanel.getGraphics3D().updateScene(molecularSystem, true, false,
					null, false, null);
			mainPanel.getHierarchy().updateStatus();
		}
	}

	public void setOscillate(boolean o) {
		oscillate = o;
	}

	public void setRate(int s) {
		if (s > 0 && s <= 100) {
			desiredspeed = s;
			delay = 1000 / s;
			timer.setDelay(delay);
		}
	}

	public void setSkip(int s) {
		if (s < 1) {
			return;
		}
		skip = s % molecularSystem.getAtomList().size();
	}

	public void start() {
		timer.start();
	}

	public void stop() {
		timer.stop();
	}
}
