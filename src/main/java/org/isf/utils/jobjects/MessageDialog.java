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

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JOptionPane;

import org.isf.generaldata.MessageBundle;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.model.OHExceptionMessage;
import org.isf.utils.exception.model.OHSeverityLevel;

public class MessageDialog {

	public static final String ERROR_MESSAGE = MessageBundle.getMessage("angal.messagedialog.error.title");
	public static final String WARNING_MESSAGE = MessageBundle.getMessage("angal.messagedialog.warning.title");
	public static final String INFO_MESSAGE = MessageBundle.getMessage("angal.messagedialog.info.title");
	public static final String PLAIN_MESSAGE = MessageBundle.getMessage("angal.messagedialog.plain.title");
	public static final String QUESTION = MessageBundle.getMessage("angal.messagedialog.question.title");

	public static void error(Component parentComponent, String messageKey, Object... additionalArgs) {
		JOptionPane.showMessageDialog(
				parentComponent,
				(additionalArgs.length == 0)
						? MessageBundle.getMessage(messageKey)
						: MessageBundle.formatMessage(messageKey, additionalArgs),
				ERROR_MESSAGE,
				JOptionPane.ERROR_MESSAGE);
	}

	public static void warning(Component parentComponent, String messageKey, Object... additionalArgs) {
		JOptionPane.showMessageDialog(
				parentComponent,
				(additionalArgs.length == 0)
						? MessageBundle.getMessage(messageKey)
						: MessageBundle.formatMessage(messageKey, additionalArgs),
				WARNING_MESSAGE,
				JOptionPane.WARNING_MESSAGE);
	}

	public static void info(Component parentComponent, String messageKey, Object... additionalArgs) {
		JOptionPane.showMessageDialog(
				parentComponent,
				(additionalArgs.length == 0)
						? MessageBundle.getMessage(messageKey)
						: MessageBundle.formatMessage(messageKey, additionalArgs),
				INFO_MESSAGE,
				JOptionPane.INFORMATION_MESSAGE);
	}

	public static void plain(Component parentComponent, String messageKey, Object... additionalArgs) {
		JOptionPane.showMessageDialog(
				parentComponent,
				(additionalArgs.length == 0)
						? MessageBundle.getMessage(messageKey)
						: MessageBundle.formatMessage(messageKey, additionalArgs),
				PLAIN_MESSAGE,
				JOptionPane.PLAIN_MESSAGE);
	}

	public static int yesNo(Component parentComponent, String messageKey, Object... additionalArgs) {
		return yesNo(parentComponent, null, messageKey, additionalArgs);
	}

	public static int yesNo(Component parentComponent, Icon icon, String messageKey, Object... additionalArgs) {
		return JOptionPane.showConfirmDialog(
				parentComponent,
				(additionalArgs.length == 0)
						? MessageBundle.getMessage(messageKey)
						: MessageBundle.formatMessage(messageKey, additionalArgs),
				QUESTION,
				JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE,
				icon
		);
	}

	public static int yesNoCancel(Component parentComponent, String messageKey, Object... additionalArgs) {
		return JOptionPane.showConfirmDialog(
				parentComponent,
				(additionalArgs.length == 0)
						? MessageBundle.getMessage(messageKey)
						: MessageBundle.formatMessage(messageKey, additionalArgs),
				QUESTION,
				JOptionPane.YES_NO_CANCEL_OPTION
		);
	}

	public static int okCancel(Component parentComponent, String messageKey, Object... additionalArgs) {
		return JOptionPane.showConfirmDialog(
				parentComponent,
				(additionalArgs.length == 0)
						? MessageBundle.getMessage(messageKey)
						: MessageBundle.formatMessage(messageKey, additionalArgs),
				QUESTION,
				JOptionPane.OK_CANCEL_OPTION
		);
	}

	public static Object inputDialog(Component parentComponent, Icon icon, Object[] selectionValues, Object initSelection, String messageKey,
			Object... additionalArgs) {
		return JOptionPane.showInputDialog(
				parentComponent,
				(additionalArgs.length == 0)
						? MessageBundle.getMessage(messageKey)
						: MessageBundle.formatMessage(messageKey, additionalArgs),
				INFO_MESSAGE,
				JOptionPane.INFORMATION_MESSAGE,
				icon,
				selectionValues,
				initSelection);
	}

	public static void showExceptions(OHServiceException ohServiceException) {
		if (ohServiceException.getMessages() == null) {
			return;
		}
		for (OHExceptionMessage ohExceptionMessage : ohServiceException.getMessages()) {
			OHSeverityLevel severity = ohExceptionMessage.getLevel();
			if (OHSeverityLevel.ERROR == severity) {
				JOptionPane.showMessageDialog(
						null,
						ohExceptionMessage.getMessage(),
						ERROR_MESSAGE,
						JOptionPane.ERROR_MESSAGE);
			} else if (OHSeverityLevel.WARNING == severity) {
				JOptionPane.showMessageDialog(
						null,
						ohExceptionMessage.getMessage(),
						WARNING_MESSAGE,
						JOptionPane.WARNING_MESSAGE);
			} else if (OHSeverityLevel.INFO == severity) {
				JOptionPane.showMessageDialog(
						null,
						ohExceptionMessage.getMessage(),
						INFO_MESSAGE,
						JOptionPane.INFORMATION_MESSAGE);
			} else {
				JOptionPane.showMessageDialog(
						null,
						ohExceptionMessage.getMessage(),
						PLAIN_MESSAGE,
						JOptionPane.PLAIN_MESSAGE);
			}
		}
	}

}
