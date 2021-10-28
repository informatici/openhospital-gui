/*
 * Open Hospital (www.open-hospital.org)
 * Copyright © 2006-2021 Informatici Senza Frontiere (info@informaticisenzafrontiere.org)
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
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.event.HyperlinkEvent;

import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.isf.generaldata.GeneralData;
import org.isf.generaldata.MessageBundle;
import org.isf.utils.jobjects.MessageDialog;

/**
 * HelpViewer.java - 27/nov/2012
 *
 * @author Mwithi
 */
public class HelpViewer extends JDialog {

	
	private static final long serialVersionUID = 1L;

	private static final String MANUAL_PDF_FILE = "UserManual.pdf";
	
	public HelpViewer() {
		GeneralData.getGeneralData();
		String doc_file = GeneralData.DOC_DIR + File.separator + MANUAL_PDF_FILE;
		File file = new File(doc_file);
		if (file != null) {
			if (Desktop.isDesktopSupported()) { //Try to find system PDF viewer
				try {
					Desktop.getDesktop().open(file);
				} catch (IOException | IllegalArgumentException e) {
					new HelpDialog(HelpViewer.this, MessageBundle.getMessage("angal.help.userguidenotfound"));
				}
			} else if (!GeneralData.INTERNALVIEWER) { //Try specified PDF viewer, if any
				try {
					Runtime rt = Runtime.getRuntime();
					rt.exec(GeneralData.VIEWER + " " + file);
				} catch (IOException e) {
					new HelpDialog(HelpViewer.this, MessageBundle.getMessage("angal.help.pdfviewernotfoundoruserguidenotfound"));
				}
			} else { //abort operation
				new HelpDialog(HelpViewer.this, MessageBundle.getMessage("angal.help.pdfviewernotfound"));
			}
		}
	}

	public class HelpDialog extends JDialog {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private static final String URL_USER_MANUAL = "https://github.com/informatici/openhospital-doc/blob/develop/doc_user/UserManual.adoc";
		private static final String URL_ADMIN_MANUAL = "https://github.com/informatici/openhospital-doc/blob/develop/doc_admin/AdminManual.adoc";

		public HelpDialog(HelpViewer helpViewer, String title) {
			super(helpViewer, true);
			setTitle(title);
			initComponents();
		}

		private void initComponents() {
			JPanel dialogPanel = new JPanel();
			dialogPanel.setLayout(new BoxLayout(dialogPanel, BoxLayout.Y_AXIS));
			dialogPanel.add(new JLabel("You can check our online documentation"));
			dialogPanel.add(createLink("Online User Manual", URL_USER_MANUAL));
			dialogPanel.add(createLink("Online Admin Manual", URL_ADMIN_MANUAL));
			getContentPane().add(dialogPanel);
			setLocationRelativeTo(null);
			setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			setVisible(true);
		}

		private JEditorPane createLink(String text, String url) {
			StringBuilder sb = new StringBuilder();
			sb.append("<html><a href='").append(url).append("'>").append(text).append("</a></html>");
			
			JEditorPane jep = new JEditorPane();
			jep.setContentType("text/html");
			jep.setText(sb.toString());
			jep.setEditable(false);
			jep.addHyperlinkListener(e -> {
				if (HyperlinkEvent.EventType.ACTIVATED.equals(e.getEventType())) {
					System.out.println(e.getURL());
					Desktop desktop = Desktop.getDesktop();
					try {
						desktop.browse(e.getURL().toURI());
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			});
			
			return jep;
		}

	}

}
