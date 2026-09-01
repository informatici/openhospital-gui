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
package org.isf.menu.gui;

import java.awt.Component;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import org.isf.generaldata.GeneralData;
import org.isf.generaldata.MessageBundle;
import org.isf.menu.manager.UserBrowsingManager;
import org.isf.menu.model.User;
import org.isf.utils.db.BCrypt;
import org.isf.utils.jobjects.MessageDialog;

/**
 * Reusable two-step (insert + repeat) new-password prompt shared by the administrator "reset password" action and the
 * mandatory password change at login (OP-896). It validates the password (not blank, minimum length and strength) and
 * returns the BCrypt-hashed password, leaving the persistence (and the {@code passwdMustChange} flag) to the caller.
 */
public final class ChangePasswordDialog {

	// BCrypt has a maximum length of 72 characters
	// see for example, https://security.stackexchange.com/questions/152430/what-maximum-password-length-to-choose-when-using-bcrypt
	private static final int BCRYPT_MAX_LENGTH = 72;

	private ChangePasswordDialog() {
	}

	/**
	 * Prompts the user for a new password (insert and repeat) and validates it.
	 *
	 * @param parent the parent component for the dialogs
	 * @param userBrowsingManager used to check the password strength policy
	 * @param user the user whose password is being changed, used to reject reusing the current password (may be {@code null} to skip that check)
	 * @param title the title shown on the dialogs
	 * @return the BCrypt-hashed new password, or {@code null} if the user cancelled or the two entries did not match
	 */
	public static String promptForNewPassword(Component parent, UserBrowsingManager userBrowsingManager, User user, String title) {
		JPasswordField pwd = new JPasswordField(10);
		pwd.addAncestorListener(new AncestorListener() {

			@Override
			public void ancestorRemoved(AncestorEvent event) {
			}

			@Override
			public void ancestorMoved(AncestorEvent event) {
			}

			@Override
			public void ancestorAdded(AncestorEvent event) {
				event.getComponent().requestFocusInWindow();
			}
		});

		// Loop so that a wrong repeat (or a too-long password) re-prompts instead of aborting the whole change
		while (true) {
			// 1. Insert new password
			pwd.setText("");
			String newPassword = "";
			JPanel insertPanel = new JPanel(new GridLayout(2, 1, 5, 5));
			if (GeneralData.STRONGLENGTH != 0) {
				insertPanel
					.add(new JLabel(MessageBundle.formatMessage("angal.userbrowser.step1.pleaseinsertanew.password.fmt.msg", GeneralData.STRONGLENGTH)));
			} else {
				insertPanel.add(new JLabel(MessageBundle.formatMessage("angal.userbrowser.step1.pleaseinsertanew.password.msg")));
			}
			insertPanel.add(pwd);

			while (newPassword.isEmpty()) {
				int action = JOptionPane.showConfirmDialog(parent, insertPanel, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
				if (JOptionPane.CANCEL_OPTION == action) {
					return null;
				}
				newPassword = new String(pwd.getPassword());
				if (newPassword.isEmpty()) {
					MessageDialog.error(parent, "angal.userbrowser.passwordmustnotbeblank.msg");
					pwd.setText("");
				} else if (GeneralData.STRONGLENGTH != 0 && newPassword.length() < GeneralData.STRONGLENGTH) {
					MessageDialog.error(parent, "angal.userbrowser.passwordmustbeatleastncharacters.fmt.msg", GeneralData.STRONGLENGTH);
					newPassword = "";
					pwd.setText("");
				} else if (!userBrowsingManager.isPasswordStrong(newPassword)) {
					MessageDialog.error(parent, "angal.userbrowser.passwordsmustcontainatleastonealphabeticnumericandspecialcharacter.msg");
					newPassword = "";
					pwd.setText("");
				} else if (userBrowsingManager.isSameAsCurrentPassword(user, newPassword)) {
					// OP-1431: a forced/self password change must not reuse the current password
					MessageDialog.error(parent, "angal.userbrowser.newpasswordmustbedifferentfromthecurrentone.msg");
					newPassword = "";
					pwd.setText("");
				}
			}

			// 2. Retype new password
			pwd.setText("");
			JPanel repeatPanel = new JPanel(new GridLayout(2, 1, 5, 5));
			repeatPanel.add(new JLabel(MessageBundle.getMessage("angal.userbrowser.step2.pleaserepeatthenewpassword.label")));
			repeatPanel.add(pwd);
			int action = JOptionPane.showConfirmDialog(parent, repeatPanel, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
			if (JOptionPane.CANCEL_OPTION == action) {
				return null;
			}
			String newPassword2 = new String(pwd.getPassword());

			// 3. Check: on mismatch or too-long, show the error and re-prompt
			if (!newPassword.equals(newPassword2)) {
				MessageDialog.error(parent, "angal.userbrowser.passwordsdonotmatchpleaseretry.msg");
				continue;
			}
			if (newPassword.length() > BCRYPT_MAX_LENGTH) {
				MessageDialog.error(parent, "angal.userbrowser.passwordistoolongmaximumof72characters.msg");
				continue;
			}
			return BCrypt.hashpw(newPassword, BCrypt.gensalt());
		}
	}
}
