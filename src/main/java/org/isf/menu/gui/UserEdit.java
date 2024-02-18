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
package org.isf.menu.gui;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Arrays;
import java.util.EventListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.WindowConstants;
import javax.swing.event.EventListenerList;

import org.isf.generaldata.GeneralData;
import org.isf.generaldata.MessageBundle;
import org.isf.menu.manager.Context;
import org.isf.menu.manager.UserBrowsingManager;
import org.isf.menu.model.User;
import org.isf.menu.model.UserGroup;
import org.isf.utils.db.BCrypt;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.layout.SpringUtilities;

public class UserEdit extends JDialog {

	private static final long serialVersionUID = 1L;
	private EventListenerList userListeners = new EventListenerList();

    public interface UserListener extends EventListener {
        void userUpdated(AWTEvent e);
        void userInserted(AWTEvent e);
    }

    public void addUserListener(UserListener l) {
        userListeners.add(UserListener.class, l);
    }

    public void removeUserListener(UserListener listener) {
        userListeners.remove(UserListener.class, listener);
    }

	private void fireUserInserted(User aUser) {
		AWTEvent event = new AWTEvent(aUser, AWTEvent.RESERVED_ID_MAX + 1) {

			private static final long serialVersionUID = 1L;
		};

		EventListener[] listeners = userListeners.getListeners(UserListener.class);
		for (EventListener listener : listeners) {
			((UserListener) listener).userInserted(event);
		}
	}

	private void fireUserUpdated() {
		AWTEvent event = new AWTEvent(new Object(), AWTEvent.RESERVED_ID_MAX + 1) {

			private static final long serialVersionUID = 1L;
		};

		EventListener[] listeners = userListeners.getListeners(UserListener.class);
		for (EventListener listener : listeners) {
			((UserListener) listener).userUpdated(event);
		}
	}

	private JPanel jContentPane;
	private JPanel dataPanel;
	private JPanel buttonPanel;
	private JButton cancelButton;
	private JButton okButton;
	private JTextField descriptionTextField;
	private JTextField nameTextField;
	private JPasswordField pwdTextField;
	private JPasswordField pwd2TextField;
	private JComboBox<UserGroup> userGroupComboBox;
	private JCheckBox accountLocked;

	private User user;
	private boolean insert;

	private UserBrowsingManager userBrowsingManager = Context.getApplicationContext().getBean(UserBrowsingManager.class);

	/**
	 * This is the default constructor; we pass the arraylist and the selectedrow
     * because we need to update them
	 */
	public UserEdit(UserBrowsing parent, User old, boolean inserting) {
		super(parent, (inserting ? MessageBundle.getMessage("angal.userbrowser.addnewuser.title")
				: MessageBundle.getMessage("angal.userbrowser.edituser.title")), true);
		addUserListener(parent);
		insert = inserting;
		user = old;
		initialize();
	}

	/**
	 * This method initializes this
	 */
	private void initialize() {
		this.setContentPane(getJContentPane());
		this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}

	/**
	 * This method initializes jContentPane
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jContentPane = new JPanel();
			jContentPane.setLayout(new BorderLayout());
			jContentPane.add(getDataPanel(), BorderLayout.NORTH);
			jContentPane.add(getButtonPanel(), BorderLayout.SOUTH);
		}
		return jContentPane;
	}

	/**
	 * This method initializes dataPanel
	 *
	 * @return javax.swing.JPanel
	 * tipo combo
	 * nome text
	 * desc text
	 * pwd  text
	 * pwd2	text
	 */
	private JPanel getDataPanel() {
		if (dataPanel == null) {
			dataPanel = new JPanel(new SpringLayout());
			dataPanel.add(new JLabel(MessageBundle.getMessage("angal.userbrowser.group.label")));
			dataPanel.add(getUserGroupComboBox());
			dataPanel.add(new JLabel(MessageBundle.getMessage("angal.userbrowser.name.label")));
			dataPanel.add(getNameTextField());
			if (insert) {
				dataPanel.add(new JLabel(MessageBundle.getMessage("angal.userbrowser.password.label")));
				dataPanel.add(getPwdTextField());
				dataPanel.add(new JLabel(MessageBundle.getMessage("angal.userbrowser.retype.password.label")));
				dataPanel.add(getPwd2TextField());
			}
			dataPanel.add(new JLabel(MessageBundle.getMessage("angal.userbrowser.description.label")));
			dataPanel.add(getDescriptionTextField());
			if (!insert) {
				dataPanel.add(new JLabel(MessageBundle.getMessage("angal.userbrowser.locked.label")));
				accountLocked = new JCheckBox();
				accountLocked.setSelected(user.isAccountLocked());
				dataPanel.add(accountLocked);
			}
			SpringUtilities.makeCompactGrid(dataPanel,
					insert ? 5 : 4, 2,
					5, 5,
					5, 5);
		}
		return dataPanel;
	}

	/**
	 * This method initializes buttonPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getButtonPanel() {
		if (buttonPanel == null) {
			buttonPanel = new JPanel();
			buttonPanel.add(getOkButton(), null);
			buttonPanel.add(getCancelButton(), null);
		}
		return buttonPanel;
	}

	/**
	 * This method initializes cancelButton
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getCancelButton() {
		if (cancelButton == null) {
			cancelButton = new JButton(MessageBundle.getMessage("angal.common.cancel.btn"));
			cancelButton.setMnemonic(MessageBundle.getMnemonic("angal.common.cancel.btn.key"));
			cancelButton.addActionListener(actionEvent -> dispose());
		}
		return cancelButton;
	}

	/**
	 * This method initializes okButton
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getOkButton() {
		if (okButton == null) {
			okButton = new JButton(MessageBundle.getMessage("angal.common.ok.btn"));
			okButton.setMnemonic(MessageBundle.getMnemonic("angal.common.ok.btn.key"));
			okButton.addActionListener(actionEvent -> {
				String userName = nameTextField.getText().trim();
				if (userName.isEmpty()) {
					MessageDialog.error(null, "angal.userbrowser.pleaseprovideavalidusername.msg");
					return;
				}
				user.setUserName(userName);
				user.setDesc(descriptionTextField.getText());
				if (insert) {
					char[] password = pwdTextField.getPassword();
					char[] repeatPassword = pwd2TextField.getPassword();

					if (Arrays.equals(password, new char[0])) {
						MessageDialog.error(null, "angal.userbrowser.pleaseprovideapassword.msg");
						Arrays.fill(password, '0');
						Arrays.fill(repeatPassword, '0');
						return;
					}
					if (Arrays.equals(repeatPassword, new char[0])) {
						MessageDialog.error(null, "angal.userbrowser.pleaseprovidetheretypepassword.msg");
						Arrays.fill(password, '0');
						Arrays.fill(repeatPassword, '0');
						return;
					}
					if (GeneralData.STRONGLENGTH != 0 && password.length < GeneralData.STRONGLENGTH) {
						MessageDialog.error(null, "angal.userbrowser.passwordmustbeatleastncharacters.fmt.msg", GeneralData.STRONGLENGTH);
						Arrays.fill(password, '0');
						Arrays.fill(repeatPassword, '0');
						return;
					}
					if (!Arrays.equals(password, repeatPassword)) {
						MessageDialog.error(null, "angal.userbrowser.passwordsdonotmatchpleasecorrect.msg");
						Arrays.fill(password, '0');
						Arrays.fill(repeatPassword, '0');
						return;
					}
					String passwordStr = new String(password);
					if (!userBrowsingManager.isPasswordStrong(passwordStr)) {
						MessageDialog.error(null, "angal.userbrowser.passwordsmustcontainatleastonealphabeticnumericandspecialcharacter.msg");
						Arrays.fill(password, '0');
						Arrays.fill(repeatPassword, '0');
						passwordStr = null;
						return;
					}
					// BCrypt has a maximum length of 72 characters
					// see for example, https://security.stackexchange.com/questions/152430/what-maximum-password-length-to-choose-when-using-bcrypt
					if (password.length > 72) {
						MessageDialog.error(null, "angal.userbrowser.passwordistoolongmaximumof72characters.msg");
						Arrays.fill(password, '0');
						Arrays.fill(repeatPassword, '0');
						return;
					}
					String hashed = BCrypt.hashpw(new String(password), BCrypt.gensalt());
					user.setPasswd(hashed);
					user.setUserGroupName((UserGroup) userGroupComboBox.getSelectedItem());
					try {
						userBrowsingManager.newUser(user);
						fireUserInserted(user);
						dispose();
					} catch (OHServiceException e1) {
						MessageDialog.info(null, "angal.common.datacouldnotbesaved.msg");
						OHServiceExceptionUtil.showMessages(e1);
					}
					Arrays.fill(password, '0');
					Arrays.fill(repeatPassword, '0');
				} else {
					user.setUserGroupName((UserGroup) userGroupComboBox.getSelectedItem());
					try {
						if (user.isAccountLocked() && !accountLocked.isSelected()) {
							userBrowsingManager.unlockUser(user);
						} else if (!user.isAccountLocked() && accountLocked.isSelected()) {
							userBrowsingManager.lockUser(user);
						}
						userBrowsingManager.updateUser(user);
						fireUserUpdated();
						dispose();
					} catch (OHServiceException e1) {
						MessageDialog.info(null, "angal.common.datacouldnotbesaved.msg");
						OHServiceExceptionUtil.showMessages(e1);
					}
				}
			});
		}
		return okButton;
	}

	/**
	 * This method initializes descriptionTextField
	 *
	 * @return javax.swing.JTextField
	 */
	private JTextField getDescriptionTextField() {
		if (descriptionTextField == null) {
			if (insert) {
				descriptionTextField = new JTextField();
			} else {
				descriptionTextField = new JTextField(user.getDesc());
			}
			descriptionTextField.setColumns(25);
		}
		return descriptionTextField;
	}


	private JTextField getNameTextField() {
		if (nameTextField == null) {
			if (insert) {
				nameTextField = new JTextField();
			} else {
				nameTextField = new JTextField(user.getUserName());
				nameTextField.setEnabled(false);
			}
			nameTextField.setColumns(15);
		}
		return nameTextField;
	}

	private JPasswordField getPwdTextField() {
		if (pwdTextField == null) {
			pwdTextField = new JPasswordField(15);
		}
		return pwdTextField;
	}

	private JTextField getPwd2TextField() {
		if (pwd2TextField == null) {
			pwd2TextField = new JPasswordField(15);
		}
		return pwd2TextField;
	}

	/**
	 * This method initializes userGroupComboBox
	 *
	 * @return javax.swing.JComboBox
	 */
	private JComboBox<UserGroup> getUserGroupComboBox() {
		if (userGroupComboBox == null) {
			userGroupComboBox = new JComboBox<>();
			try {
				List<UserGroup> group = userBrowsingManager.getUserGroup();
				if (insert) {
					if (group != null) {
						for (UserGroup elem : group) {
							userGroupComboBox.addItem(elem);
						}
					}
				} else {
					UserGroup selectedUserGroup = null;
					if (group != null) {
						for (UserGroup elem : group) {
							userGroupComboBox.addItem(elem);
							if (user.getUserGroupName().equals(elem)) {
								selectedUserGroup = elem;
							}
						}
					}
					if (selectedUserGroup != null) {
						userGroupComboBox.setSelectedItem(selectedUserGroup);
					}
					// user is not allowed to change their own group
					if (user.getUserName().equals(UserBrowsingManager.getCurrentUser())) {
						userGroupComboBox.setEnabled(false);
					}
				}
				Dimension d = userGroupComboBox.getPreferredSize();
				userGroupComboBox.setPreferredSize(new Dimension(150, d.height));
			} catch (OHServiceException e) {
				OHServiceExceptionUtil.showMessages(e);
			}
		}
		return userGroupComboBox;
	}

}
