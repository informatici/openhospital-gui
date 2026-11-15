/*
 * Open Hospital (www.open-hospital.org)
 * Copyright © 2006-2026 Informatici Senza Frontiere (info@informaticisenzafrontiere.org)
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
package org.isf.patient.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;

import org.isf.generaldata.MessageBundle;
import org.isf.menu.manager.Context;
import org.isf.patadminissue.manager.PatientAdminIssueBrowserManager;
import org.isf.patadminissue.model.PatientAdminIssue;
import org.isf.patient.model.Patient;
import org.isf.utils.jobjects.MessageDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Signals that a patient has open administrative issues, so that the staff can check with the administration before
 * providing further services. The warning never prevents the operation: the user chooses whether to go on with it.
 */
public final class PatientAdministrativeFlagWarning {

	private static final Logger LOGGER = LoggerFactory.getLogger(PatientAdministrativeFlagWarning.class);

	private static final int TEXT_WIDTH = 450;
	private static final int TEXT_MAX_HEIGHT = 300;

	private PatientAdministrativeFlagWarning() {
	}

	/**
	 * Shows a warning listing the open administrative issues of the given patient, if any, and asks whether to continue.
	 *
	 * @param parentComponent the component the dialog is shown over
	 * @param patient the patient being selected, may be {@code null}
	 * @return {@code false} if the user gave up the operation, {@code true} otherwise
	 */
	public static boolean confirmIfFlagged(Component parentComponent, Patient patient) {
		if (patient == null || patient.getCode() == null) {
			return true;
		}
		List<PatientAdminIssue> openIssues;
		try {
			openIssues = Context.getApplicationContext().getBean(PatientAdminIssueBrowserManager.class).getOpenIssues(patient.getCode());
		} catch (Exception e) {
			// a warning must never get in the way of the operation the user is starting
			LOGGER.error("Unable to read the administrative issues of patient {}", patient.getCode(), e);
			return true;
		}
		if (openIssues.isEmpty()) {
			return true;
		}
		String reasons = openIssues.stream().map(issue -> "- " + issue.getReason()).collect(Collectors.joining("\n"));
		String cancel = MessageBundle.getMessage("angal.common.cancel.btn");
		String[] options = { cancel, MessageBundle.getMessage("angal.patadminissue.continueanyway.btn") };
		int choice = JOptionPane.showOptionDialog(parentComponent,
						wrappedText(MessageBundle.formatMessage("angal.patadminissue.warning.fmt.msg", reasons)), MessageDialog.WARNING_MESSAGE,
						JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, options, cancel);
		return choice == 1;
	}

	/**
	 * Lays out a text of any length on several lines, to show it in a dialog that stays within the screen.
	 *
	 * @param text the text to show
	 * @return a read-only component that looks like a label, with a scroll bar when the text is very long
	 */
	static Component wrappedText(String text) {
		JTextArea textArea = new JTextArea(text);
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		textArea.setEditable(false);
		textArea.setFocusable(false);
		textArea.setOpaque(false);
		textArea.setFont(UIManager.getFont("Label.font"));
		// the height of a wrapped text is only known once the width is set
		textArea.setSize(new Dimension(TEXT_WIDTH, Short.MAX_VALUE));
		if (textArea.getPreferredSize().height <= TEXT_MAX_HEIGHT) {
			return textArea;
		}
		JScrollPane scrollPane = new JScrollPane(textArea, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setBorder(null);
		scrollPane.setOpaque(false);
		scrollPane.getViewport().setOpaque(false);
		scrollPane.setPreferredSize(new Dimension(TEXT_WIDTH + scrollPane.getVerticalScrollBar().getPreferredSize().width, TEXT_MAX_HEIGHT));
		return scrollPane;
	}
}
