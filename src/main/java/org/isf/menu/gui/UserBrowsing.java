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
import java.awt.GridLayout;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.table.DefaultTableModel;

import org.isf.generaldata.GeneralData;
import org.isf.generaldata.MessageBundle;
import org.isf.menu.gui.UserEdit.UserListener;
import org.isf.menu.manager.Context;
import org.isf.menu.manager.UserBrowsingManager;
import org.isf.menu.model.User;
import org.isf.menu.model.UserGroup;
import org.isf.utils.db.BCrypt;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.ModalJFrame;

public class UserBrowsing extends ModalJFrame implements UserListener {

	private static final long serialVersionUID = 1L;
	private static final String ALL_STR = MessageBundle.getMessage("angal.common.all.txt").toUpperCase();

	@Override
	public void userInserted(AWTEvent e) {
		User u = (User) e.getSource();
		userList.add(0, u);
		((UserBrowserModel) table.getModel()).fireTableDataChanged();
		table.updateUI();
		if (table.getRowCount() > 0) {
			table.setRowSelectionInterval(0, 0);
		}
	}

	@Override
	public void userUpdated(AWTEvent e) {
		userList.set(selectedrow, user);
		((UserBrowserModel) table.getModel()).fireTableDataChanged();
		table.updateUI();
		if ((table.getRowCount() > 0) && (selectedrow > -1)) {
			table.setRowSelectionInterval(selectedrow, selectedrow);
		}
	}

	private int selectedrow;
	private JComboBox<UserGroup> userGroupFilter;
	private List<User> userList;
	private String[] pColumns = {
			MessageBundle.getMessage("angal.userbrowser.user.col").toUpperCase(),
			MessageBundle.getMessage("angal.common.group.txt").toUpperCase(),
			MessageBundle.getMessage("angal.common.description.txt").toUpperCase(),
			MessageBundle.getMessage("angal.userbrowser.locked.col").toUpperCase()};
	private int[] pColumnWidth = {70, 70, 150, 20};
	private User user;
	private DefaultTableModel model;
	private JTable table;

	private String pSelection;

	private UserBrowsing myFrame;
	private UserBrowsingManager userBrowsingManager = Context.getApplicationContext().getBean(UserBrowsingManager.class);

	public UserBrowsing() {

		setTitle(MessageBundle.getMessage("angal.userbrowser.title"));
		myFrame = this;

		model = new UserBrowserModel();
		table = new JTable(model);
		table.getColumnModel().getColumn(0).setPreferredWidth(pColumnWidth[0]);
		table.getColumnModel().getColumn(1).setPreferredWidth(pColumnWidth[1]);
		table.getColumnModel().getColumn(2).setPreferredWidth(pColumnWidth[2]);
		table.getColumnModel().getColumn(3).setPreferredWidth(pColumnWidth[3]);

		JScrollPane scrollPane = new JScrollPane(table);
		add(scrollPane, BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel();

		JLabel selectlabel = new JLabel(MessageBundle.getMessage("angal.userbrowser.selectgroup.label"));
		buttonPanel.add(selectlabel);

		userGroupFilter = new JComboBox<>();
		userGroupFilter.addItem(new UserGroup(ALL_STR, ALL_STR));
		List<UserGroup> group = null;
		try {
			group = userBrowsingManager.getUserGroup();
		} catch (OHServiceException e) {
			OHServiceExceptionUtil.showMessages(e);
		}
		if (group != null) {
			for (UserGroup elem : group) {
				userGroupFilter.addItem(elem);
			}
		}
		userGroupFilter.addActionListener(actionEvent -> {
			pSelection = userGroupFilter.getSelectedItem().toString();
			if (pSelection.compareTo(ALL_STR) == 0) {
				model = new UserBrowserModel();
			} else {
				model = new UserBrowserModel(pSelection);
			}
			model.fireTableDataChanged();
			table.updateUI();
		});
		buttonPanel.add(userGroupFilter);

		JButton buttonNew = new JButton(MessageBundle.getMessage("angal.common.new.btn"));
		buttonNew.setMnemonic(MessageBundle.getMnemonic("angal.common.new.btn.key"));
		buttonNew.addActionListener(actionEvent -> {
			user = new User("", new UserGroup(), "", "");
			new UserEdit(myFrame, user, true);
		});
		buttonPanel.add(buttonNew);

		JButton buttonEdit = new JButton(MessageBundle.getMessage("angal.common.edit.btn"));
		buttonEdit.setMnemonic(MessageBundle.getMnemonic("angal.common.edit.btn.key"));
		buttonEdit.addActionListener(actionEvent -> {
			if (table.getSelectedRow() < 0) {
				MessageDialog.error(null, "angal.common.pleaseselectarow.msg");
			} else {
				selectedrow = table.getSelectedRow();
				user = (User) model.getValueAt(table.getSelectedRow(), -1);
				new UserEdit(myFrame, user, false);
			}
		});
		buttonPanel.add(buttonEdit);

		JButton buttonResetPassword = new JButton(MessageBundle.getMessage("angal.userbrowser.resetpassword.btn"));
		buttonResetPassword.setMnemonic(MessageBundle.getMnemonic("angal.userbrowser.resetpassword.btn.key"));
		buttonResetPassword.addActionListener(actionEvent -> {
			if (table.getSelectedRow() < 0) {
				MessageDialog.error(null, "angal.common.pleaseselectarow.msg");
			} else {
				selectedrow = table.getSelectedRow();
				user = (User) model.getValueAt(table.getSelectedRow(), -1);

				// 1. Insert new password
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
				String newPassword = "";
				JPanel stepPanel = new JPanel(new GridLayout(2, 1, 5, 5));
				if (GeneralData.STRONGLENGTH != 0) {
					stepPanel.add(new JLabel(MessageBundle.formatMessage("angal.userbrowser.step1.pleaseinsertanew.password.fmt.msg", GeneralData.STRONGLENGTH)));
				} else {
					stepPanel.add(new JLabel(MessageBundle.formatMessage("angal.userbrowser.step1.pleaseinsertanew.password.msg")));
				}

				stepPanel.add(pwd);

				while (newPassword.isEmpty()) {
					int action = JOptionPane
							.showConfirmDialog(this, stepPanel, MessageBundle.getMessage("angal.userbrowser.resetpassword.title"),
									JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
					if (JOptionPane.CANCEL_OPTION == action) {
						return;
					}
					newPassword = new String(pwd.getPassword());
					if (newPassword.isEmpty()) {
						MessageDialog.error(this, "angal.userbrowser.passwordmustnotbeblank.msg");
						newPassword = "";
						pwd.setText("");
					} else {
						if (GeneralData.STRONGLENGTH != 0 && newPassword.length() < GeneralData.STRONGLENGTH) {
							MessageDialog.error(this, "angal.userbrowser.passwordmustbeatleastncharacters.fmt.msg", GeneralData.STRONGLENGTH);
							newPassword = "";
							pwd.setText("");
						} else {
							if (!userBrowsingManager.isPasswordStrong(newPassword)) {
								MessageDialog.error(this, "angal.userbrowser.passwordsmustcontainatleastonealphabeticnumericandspecialcharacter.msg");
								newPassword = "";
								pwd.setText("");
							}
						}
					}
				}

				// 2. Retype new password
				pwd.setText("");
				stepPanel = new JPanel(new GridLayout(2, 1, 5, 5));
				stepPanel.add(new JLabel(MessageBundle.getMessage("angal.userbrowser.step2.pleaserepeatthenewpassword.label")));
				stepPanel.add(pwd);
				int action = JOptionPane
						.showConfirmDialog(this, stepPanel, MessageBundle.getMessage("angal.userbrowser.resetpassword.title"),
								JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
				if (JOptionPane.CANCEL_OPTION == action) {
					return;
				}
				String newPassword2 = new String(pwd.getPassword());

				// 3. Check & Save
				if (!newPassword.equals(newPassword2)) {
					MessageDialog.error(this, "angal.userbrowser.passwordsdonotmatchpleaseretry.msg");
					newPassword = null;
					newPassword2 = null;
					return;
				}

				// BCrypt has a maximum length of 72 characters
				// see for example, https://security.stackexchange.com/questions/152430/what-maximum-password-length-to-choose-when-using-bcrypt
				if (newPassword.length() > 72) {
					MessageDialog.error(this, "angal.userbrowser.passwordistoolongmaximumof72characters.msg");
					newPassword = null;
					newPassword2 = null;
					return;
				}
				String hashed = BCrypt.hashpw(newPassword, BCrypt.gensalt());
				newPassword = null;
				newPassword2 = null;
				user.setPasswd(hashed);
				try {
					if (userBrowsingManager.updatePassword(user)) {
						MessageDialog.info(this, "angal.userbrowser.thepasswordhasbeenchanged.msg");
					}
				} catch (OHServiceException e) {
					OHServiceExceptionUtil.showMessages(e);
				}
			}
		});
		buttonPanel.add(buttonResetPassword);

		JButton buttonDelete = new JButton(MessageBundle.getMessage("angal.common.delete.btn"));
		buttonDelete.setMnemonic(MessageBundle.getMnemonic("angal.common.delete.btn.key"));
		buttonDelete.addActionListener(actionEvent -> {
			if (table.getSelectedRow() < 0) {
				MessageDialog.error(null, "angal.common.pleaseselectarow.msg");
			} else {
				User selectedUser = (User) model.getValueAt(table.getSelectedRow(), -1);
				int answer = MessageDialog.yesNo(null, "angal.userbrowser.deleteuser.fmt.msg", selectedUser.getUserName());
				try {
					if (answer == JOptionPane.YES_OPTION) {
						userBrowsingManager.deleteUser(selectedUser);
						userList.remove(table.getSelectedRow());
						model.fireTableDataChanged();
						table.updateUI();
					}
				} catch (OHServiceException e) {
					OHServiceExceptionUtil.showMessages(e);
				}
			}
		});
		buttonPanel.add(buttonDelete);

		JButton buttonClose = new JButton(MessageBundle.getMessage("angal.common.close.btn"));
		buttonClose.setMnemonic(MessageBundle.getMnemonic("angal.common.close.btn.key"));
		buttonClose.addActionListener(actionEvent -> dispose());
		buttonPanel.add(buttonClose);

		add(buttonPanel, BorderLayout.SOUTH);

		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}

	class UserBrowserModel extends DefaultTableModel {

		private static final long serialVersionUID = 1L;

		public UserBrowserModel(String s) {
			try {
				userList = userBrowsingManager.getUser(s);
			} catch (OHServiceException e) {
				OHServiceExceptionUtil.showMessages(e);
			}
		}

		public UserBrowserModel() {
			try {
				userList = userBrowsingManager.getUser();
			} catch (OHServiceException e) {
				OHServiceExceptionUtil.showMessages(e);
			}
		}

		@Override
		public Class getColumnClass(int column) {
			return (column == 3) ? Boolean.class : String.class;
		}

		@Override
		public int getRowCount() {
			if (userList == null) {
				return 0;
			}
			return userList.size();
		}

		@Override
		public String getColumnName(int c) {
			return pColumns[c];
		}

		@Override
		public int getColumnCount() {
			return pColumns.length;
		}

		@Override
		public Object getValueAt(int r, int c) {
			if (c == 0) {
				return userList.get(r).getUserName();
			} else if (c == -1) {
				return userList.get(r);
			} else if (c == 1) {
				return userList.get(r).getUserGroupName();
			} else if (c == 2) {
				return userList.get(r).getDesc();
			} else if (c == 3) {
				return userList.get(r).isAccountLocked();
			}
			return null;
		}

		@Override
		public boolean isCellEditable(int arg0, int arg1) {
			return false;
		}
	}
}
