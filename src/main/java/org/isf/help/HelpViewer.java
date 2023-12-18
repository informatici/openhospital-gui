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
package org.isf.help;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import org.isf.generaldata.GeneralData;
import org.isf.generaldata.Version;
import org.isf.utils.jobjects.MessageDialog;

/**
 * HelpViewer.java - 27/nov/2012
 *
 * @author Mwithi
 */
public class HelpViewer extends JDialog {

	private static final long serialVersionUID = 1L;

	private static final String MANUAL_PDF_FILE = "UserManual.pdf";
	private static final String USER_MANUAL_PREFIX = "https://github.com/informatici/openhospital-doc/blob/";
	private static final String USER_MANUAL_SUFFIX = "/doc_user/UserManual.adoc";

	public HelpViewer() {
		GeneralData.getGeneralData();
		String docFile = GeneralData.DOC_DIR + File.separator + MANUAL_PDF_FILE;
		File file = new File(docFile);
		if (Desktop.isDesktopSupported()) { //Try to find system PDF viewer
			try {
				Desktop.getDesktop().open(file);
			} catch (IOException | IllegalArgumentException e) {
				// Either system viewer or file is not found
				int answer = MessageDialog.yesNo(this, "angal.help.userguidenotfound.msg");
				if (answer == JOptionPane.NO_OPTION) {
					return;
				}
				try {
					Version.getVersion();
					Desktop.getDesktop().browse(new URI(USER_MANUAL_PREFIX + Version.VER_MAJOR + '.' + Version.VER_MINOR + '.' + Version.VER_RELEASE + USER_MANUAL_SUFFIX));
				} catch (IOException | URISyntaxException ex) {
					MessageDialog.error(this, "angal.help.userguidenotfound.msg");
				}
			}
		} else if (!GeneralData.INTERNALVIEWER) { //Try specified PDF viewer, if any
			try {
				Runtime rt = Runtime.getRuntime();
				rt.exec(GeneralData.VIEWER + ' ' + file);
			} catch (IOException e) {
				MessageDialog.error(this, "angal.help.pdfviewernotfoundoruserguidenotfound.msg");
			}
		} else { //abort operation
			MessageDialog.error(this, "angal.help.pdfviewernotfound.msg");
		}
	}

}
