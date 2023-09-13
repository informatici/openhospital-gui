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
package org.isf.dicom.gui;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import org.isf.dicom.manager.AbstractDicomLoader;
import org.isf.generaldata.MessageBundle;

/**
 * Progress loading
 *
 * @author Pietro Castellucci
 * @version 1.0.0
 */
public class DicomLoader extends AbstractDicomLoader {

	private static final long serialVersionUID = 1L;

	private int numfiles;
	private JLabel jLabelTitle;
	private String labelTitle = MessageBundle.getMessage("angal.dicom.loading");
	private	JProgressBar bar;
	private Color bkgColor = Color.BLUE;
	private Color fgColor = Color.WHITE;

	public DicomLoader(int numfiles, JFrame owner) {
		super(owner);
		jLabelTitle = new JLabel(labelTitle);
		jLabelTitle.setForeground(fgColor);
		this.numfiles = numfiles;
		JPanel jp = new JPanel(new BorderLayout());
		jp.setBackground(bkgColor);
		bar = new JProgressBar(0, numfiles);
		jp.add(jLabelTitle, BorderLayout.NORTH);
		jp.add(bar, BorderLayout.CENTER);
		add(jp);
		setVisible(true);
		pack();

		setLocationRelativeTo(owner);
		setVisible(true);
	}

	@Override
	public void setLoaded(int loaded) {
		bar.setValue(loaded);
		jLabelTitle.setText(labelTitle + " [" + loaded + '/' + numfiles + ']');
	}
}
