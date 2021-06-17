/*
 * Open Hospital (www.open-hospital.org)
 * Copyright © 2006-2020 Informatici Senza Frontiere (info@informaticisenzafrontiere.org)
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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.isf.help;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

import javax.swing.JDialog;

import org.isf.generaldata.GeneralData;
import org.isf.utils.jobjects.MessageDialog;

/**
 * HelpViewer.java - 27/nov/2012
 *
 * @author Mwithi
 */
public class HelpViewer extends JDialog {

	private static final long serialVersionUID = 1L;

	private static final String MANUAL_PDF_FILE = "doc/UserManual.pdf";

	public HelpViewer() {
		File file = new File(MANUAL_PDF_FILE);
		if (file != null) {
			if (Desktop.isDesktopSupported()) { //Try to find system PDF viewer
				try {
					Desktop.getDesktop().open(file);
				} catch (IOException e) {
					MessageDialog.error(HelpViewer.this, "angal.help.userguidenotfound");
				}
			} else if (!GeneralData.INTERNALVIEWER) { //Try specified PDF viewer, if any
				try {
					Runtime rt = Runtime.getRuntime();
					rt.exec(GeneralData.VIEWER + " " + file);
				} catch (IOException e) {
					MessageDialog.error(HelpViewer.this, "angal.help.pdfviewernotfoundoruserguidenotfound");
				}
			} else { //abort operation
				MessageDialog.error(HelpViewer.this, "angal.help.pdfviewernotfound");
			}
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new HelpViewer();
	}
}
