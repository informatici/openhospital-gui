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

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import org.isf.generaldata.MessageBundle;
import org.isf.menu.gui.UserEdit.UserListener;
import org.isf.menu.manager.Context;
import org.isf.menu.manager.UserBrowsingManager;
import org.isf.menu.model.User;
import org.isf.menu.model.UserGroup;
import org.isf.utils.exception.OHDataIntegrityViolationException;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.ModalJFrame;

public class UserBrowsing extends ModalJFrame implements UserListener {

	private static final long serialVersionUID = 1L;
	private static final String ALL_STR = MessageBundle.getMessage("angal.common.all.txt").toUpperCase();
	private final JComboBox<UserGroup> userGroupFilter;
	private final String[] pColumns = {
		MessageBundle.getMessage("angal.userbrowser.user.col").toUpperCase(),
		MessageBundle.getMessage("angal.common.group.txt").toUpperCase(),
		MessageBundle.getMessage("angal.common.description.txt").toUpperCase(),
		MessageBundle.getMessage("angal.userbrowser.locked.col").toUpperCase(),
		MessageBundle.getMessage("angal.common.deleted.col").toUpperCase() };
	private final int[] pColumnWidth = { 70, 70, 150, 20, 20 };
	private final JTable table;
	private final UserBrowsing myFrame;
	private final UserBrowsingManager userBrowsingManager = Context.getApplicationContext().getBean(UserBrowsingManager.class);
	private int selectedrow;
	private List<User> userList;
	private User user;
	private DefaultTableModel model;
	private String pSelection;

	public UserBrowsing() {

		setTitle(MessageBundle.getMessage("angal.userbrowser.title"));
		myFrame = this;

		model = new UserBrowserModel();
		table = new JTable(model);
		table.getColumnModel().getColumn(0).setPreferredWidth(pColumnWidth[0]);
		table.getColumnModel().getColumn(1).setPreferredWidth(pColumnWidth[1]);
		table.getColumnModel().getColumn(2).setPreferredWidth(pColumnWidth[2]);
		table.getColumnModel().getColumn(3).setPreferredWidth(pColumnWidth[3]);
		table.getColumnModel().getColumn(4).setPreferredWidth(pColumnWidth[4]);

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

				// null user: an administrator reset does not check the target user's current password (the admin does not know it)
				String hashed = ChangePasswordDialog.promptForNewPassword(this, userBrowsingManager, null,
					MessageBundle.getMessage("angal.userbrowser.resetpassword.title"));
				if (hashed == null) {
					return;
				}
				user.setPasswd(hashed);
				// OP-896: a password set by an administrator must be changed by the user at next login
				user.setPasswdMustChange(true);
				try {
					if (userBrowsingManager.updatePassword(user) != null) {
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
				} catch (OHDataIntegrityViolationException ex) {
					try {
						User oldUser = userBrowsingManager.getUserByName(selectedUser.getUserName(), true);
						if (oldUser.isDeleted()) {
							MessageDialog.error(null, "angal.userbrowser.alreadysoftdeleted.msg");
						} else {
							answer = MessageDialog.yesNo(null, "angal.userbrowser.softdeleteuser.fmt.msg", selectedUser.getUserName());
							if (answer == JOptionPane.YES_OPTION) {
								selectedUser.setDeleted(true);
								userBrowsingManager.updateUser(selectedUser);
								model.fireTableDataChanged();
								table.updateUI();
							}
						}
					} catch (OHServiceException e) {
						selectedUser.setDeleted(false);
						OHServiceExceptionUtil.showMessages(e);
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
			return (column == 3 || column == 4) ? Boolean.class : String.class;
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
			} else if (c == 4) {
				return userList.get(r).isDeleted();
			}
			return null;
		}

		@Override
		public boolean isCellEditable(int arg0, int arg1) {
			return false;
		}
	}
}
