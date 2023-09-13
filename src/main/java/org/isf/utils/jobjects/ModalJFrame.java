/*
 * Open Hospital (www.open-hospital.org)
 * Copyright © 2006-2023 Informatici Senza Frontiere (info@informaticisenzafrontiere.org)
 *
 * Open Hospital is a free and open source software for healthcare data management.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * https://www.gnu.org/licenses/gpl-3.0-standalone.html
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.isf.utils.jobjects;

import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.ImageIcon;
import javax.swing.JFrame;

/**
 * @author Santhosh Kumar T - santhosh@in.fiorano.com
 */
public class ModalJFrame extends JFrame {

	private static final long serialVersionUID = 1L;

	protected final JFrame frame = this;

	private final ImageIcon img = new ImageIcon("./rsc/icons/oh.png");

	/**
	 * Method to enable/disable a owner JFrame launching this ModalJFrame
	 *
	 * @param owner - the JFrame owner
	 */
	public void showAsModal(final JFrame owner) {

		setIconImage(img.getImage());

		this.addWindowListener(new WindowAdapter() {

			@Override
			public void windowOpened(WindowEvent e) {
				owner.setEnabled(false);
			}

			@Override
			public void windowClosing(WindowEvent e) {
				owner.setEnabled(true);
				owner.toFront();
				frame.removeWindowListener(this);
			}

			@Override
			public void windowClosed(WindowEvent e) {
				owner.setEnabled(true);
				owner.toFront();
				frame.removeWindowListener(this);
			}
		});

		owner.addWindowListener(new WindowAdapter() {

			@Override
			public void windowActivated(WindowEvent e) {
				if (frame.isShowing()) {
					frame.setExtendedState(Frame.NORMAL);
					frame.toFront();
				} else {
					owner.removeWindowListener(this);
				}
			}
		});

		frame.setVisible(true);
	}

}
