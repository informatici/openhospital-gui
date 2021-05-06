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
package org.isf.menu.gui;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.Locale;

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
import org.isf.menu.manager.Context;
import org.isf.menu.manager.UserBrowsingManager;
import org.isf.menu.model.User;
import org.isf.menu.model.UserGroup;
import org.isf.utils.db.BCrypt;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.ModalJFrame;

public class UserBrowsing extends ModalJFrame implements UserEdit.UserListener {

	private static final long serialVersionUID = 1L;
	private static final String ALL_STR = MessageBundle.getMessage("angal.userbrowser.all.txt");

	@Override
	public void userInserted(AWTEvent e) {
		User u = (User) e.getSource();
		pUser.add(0, u);
		((UserBrowserModel) table.getModel()).fireTableDataChanged();
		table.updateUI();
		if (table.getRowCount() > 0) {
			table.setRowSelectionInterval(0, 0);
		}
	}

	@Override
	public void userUpdated(AWTEvent e) {
		pUser.set(selectedrow, user);
		((UserBrowserModel) table.getModel()).fireTableDataChanged();
		table.updateUI();
		if ((table.getRowCount() > 0) && (selectedrow > -1)) {
			table.setRowSelectionInterval(selectedrow, selectedrow);
		}
	}

	private static final int DEFAULT_WIDTH = 400;
	private static final int DEFAULT_HEIGHT = 200;
	private int pfrmWidth;
	private int pfrmHeight;
	private int selectedrow;
	private JLabel selectlabel;
	private JComboBox pbox;
	private ArrayList<User> pUser;
	private String[] pColumns = {
			MessageBundle.getMessage("angal.userbrowser.user.txt").toUpperCase(),
			MessageBundle.getMessage("angal.userbrowser.group.txt").toUpperCase(),
			MessageBundle.getMessage("angal.userbrowser.description.txt").toUpperCase() };
	private int[] pColumnWidth = { 70, 70, 150 };
	private User user;
	private DefaultTableModel model;
	private JTable table;
	private JScrollPane scrollPane;

	private String pSelection;

	private UserBrowsing myFrame;
	private UserBrowsingManager manager = Context.getApplicationContext().getBean(UserBrowsingManager.class);

	public UserBrowsing() {

		setTitle(MessageBundle.getMessage("angal.userbrowser.title"));
		myFrame = this;

		setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
		Toolkit kit = Toolkit.getDefaultToolkit();
		Dimension screensize = kit.getScreenSize();
		pfrmWidth = (screensize.width / 2) + 100;
		pfrmHeight = screensize.height / 2;
		setBounds(screensize.width / 4, screensize.height / 4, pfrmWidth, pfrmHeight);

		model = new UserBrowserModel();
		table = new JTable(model);
		table.getColumnModel().getColumn(0).setPreferredWidth(pColumnWidth[0]);
		table.getColumnModel().getColumn(1).setPreferredWidth(pColumnWidth[1]);
		table.getColumnModel().getColumn(2).setPreferredWidth(pColumnWidth[2]);

		scrollPane = new JScrollPane(table);
		add(scrollPane, BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel();

		selectlabel = new JLabel(MessageBundle.getMessage("angal.userbrowser.selectgroup.label"));
		buttonPanel.add(selectlabel);

		pbox = new JComboBox();
		pbox.addItem(ALL_STR);
		ArrayList<UserGroup> group = null;
		try {
			group = manager.getUserGroup();
		} catch (OHServiceException e) {
			OHServiceExceptionUtil.showMessages(e);
		}
		if (group != null) {
			for (UserGroup elem : group) {
				pbox.addItem(elem);
			}
		}
		pbox.addActionListener(event -> {
			pSelection = pbox.getSelectedItem().toString();
			if (pSelection.compareTo(ALL_STR) == 0) {
				model = new UserBrowserModel();
			} else {
				model = new UserBrowserModel(pSelection);
			}
			model.fireTableDataChanged();
			table.updateUI();
		});
		buttonPanel.add(pbox);

		JButton buttonNew = new JButton(MessageBundle.getMessage("angal.common.new.btn"));
		buttonNew.setMnemonic(MessageBundle.getMnemonic("angal.common.new.btn.key"));
		buttonNew.addActionListener(event -> {
			user = new User("", new UserGroup(), "", "");
			new UserEdit(myFrame, user, true);
		});
		buttonPanel.add(buttonNew);

		JButton buttonEdit = new JButton(MessageBundle.getMessage("angal.common.edit.btn"));
		buttonEdit.setMnemonic(MessageBundle.getMnemonic("angal.common.edit.btn.key"));
		buttonEdit.addActionListener(event -> {
			if (table.getSelectedRow() < 0) {
				MessageDialog.error(null, "angal.common.select.row.msg");
			} else {
				selectedrow = table.getSelectedRow();
				user = (User) model.getValueAt(table.getSelectedRow(), -1);
				new UserEdit(myFrame, user, false);
			}
		});
		buttonPanel.add(buttonEdit);

		JButton buttonResetPassword = new JButton(MessageBundle.getMessage("angal.userbrowser.resetpassword.btn"));
		buttonResetPassword.setMnemonic(MessageBundle.getMnemonic("angal.userbrowser.resetpassword.btn.key"));
		buttonResetPassword.addActionListener(event -> {
			if (table.getSelectedRow() < 0) {
				MessageDialog.error(null, "angal.common.select.row.msg");
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
				stepPanel.add(new JLabel(MessageBundle.getMessage("angal.userbrowser.step1.insert.new.password.label")));
				stepPanel.add(pwd);

				while (newPassword.isEmpty()) {
					int action = JOptionPane
							.showConfirmDialog(UserBrowsing.this, stepPanel, MessageBundle.getMessage("angal.userbrowser.reset.password.title"),
									JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
					if (JOptionPane.CANCEL_OPTION == action) {
						return;
					}
					newPassword = new String(pwd.getPassword());
					if (newPassword.isEmpty() || newPassword.length() < 6) {
						MessageDialog.error(UserBrowsing.this, "angal.userbrowser.password.too.short.msg");
						newPassword = "";
						pwd.setText("");
					}
				}

				// 2. Retype new password
				pwd.setText("");
				stepPanel = new JPanel(new GridLayout(2, 1, 5, 5));
				stepPanel.add(new JLabel(MessageBundle.getMessage("angal.userbrowser.step2.repeat.password.label")));
				stepPanel.add(pwd);
				int action = JOptionPane
						.showConfirmDialog(UserBrowsing.this, stepPanel, MessageBundle.getMessage("angal.userbrowser.reset.password.title"),
								JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
				if (JOptionPane.CANCEL_OPTION == action) {
					return;
				}
				String newPassword2 = new String(pwd.getPassword());

				// 3. Check & Save
				if (!newPassword.equals(newPassword2)) {
					MessageDialog.error(UserBrowsing.this, "angal.userbrowser.passwords.do.not.match.msg");
					return;
				}
				String hashed = BCrypt.hashpw(newPassword, BCrypt.gensalt());
				user.setPasswd(hashed);
				try {
					if (manager.updatePassword(user)) {
						MessageDialog.info(UserBrowsing.this, "angal.userbrowser.password.changed.msg");
					}
				} catch (OHServiceException e) {
					OHServiceExceptionUtil.showMessages(e);
				}
			}
		});
		buttonPanel.add(buttonResetPassword);

		JButton buttonDelete = new JButton(MessageBundle.getMessage("angal.common.delete.btn"));
		buttonDelete.setMnemonic(MessageBundle.getMnemonic("angal.common.delete.btn.key"));
		buttonDelete.addActionListener(event -> {
			if (table.getSelectedRow() < 0) {
				MessageDialog.error(null, "angal.common.select.row.msg");
			} else {
				User selectedUser = (User) model.getValueAt(table.getSelectedRow(), -1);
				int n = JOptionPane.showConfirmDialog(null, MessageBundle.formatMessage("angal.userbrowser.delete.user.fmt.label", selectedUser.getUserName()),
						MessageBundle.getMessage("angal.userbrowser.delete.user.title"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
				try {
					if ((JOptionPane.YES_OPTION == n) && manager.deleteUser(selectedUser)) {
						pUser.remove(table.getSelectedRow());
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
		buttonClose.addActionListener(event -> dispose());
		buttonPanel.add(buttonClose);

		add(buttonPanel, BorderLayout.SOUTH);
		setVisible(true);
	}

	class UserBrowserModel extends DefaultTableModel {

		private static final long serialVersionUID = 1L;

		public UserBrowserModel(String s) {
			try {
				pUser = manager.getUser(s);
			} catch (OHServiceException e) {
				OHServiceExceptionUtil.showMessages(e);
			}
		}

		public UserBrowserModel() {
			try {
				pUser = manager.getUser();
			} catch (OHServiceException e) {
				OHServiceExceptionUtil.showMessages(e);
			}
		}

		@Override
		public int getRowCount() {
			if (pUser == null) {
				return 0;
			}
			return pUser.size();
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
				return pUser.get(r).getUserName();
			} else if (c == -1) {
				return pUser.get(r);
			} else if (c == 1) {
				return pUser.get(r).getUserGroupName();
			} else if (c == 2) {
				return pUser.get(r).getDesc();
			}
			return null;
		}

		@Override
		public boolean isCellEditable(int arg0, int arg1) {
			return false;
		}
	}
}
