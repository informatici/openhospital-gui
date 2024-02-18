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
import java.awt.Toolkit;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import org.isf.generaldata.MessageBundle;
import org.isf.menu.gui.GroupEdit.GroupListener;
import org.isf.menu.manager.Context;
import org.isf.menu.manager.UserBrowsingManager;
import org.isf.menu.model.UserGroup;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.ModalJFrame;

public class UserGroupBrowsing extends ModalJFrame implements GroupListener {

	private static final long serialVersionUID = 1L;

	@Override
	public void groupInserted(AWTEvent e) {
		pGroup.add(0, group);
		((UserGroupBrowserModel) table.getModel()).fireTableDataChanged();
		table.updateUI();
		if (table.getRowCount() > 0) {
			table.setRowSelectionInterval(0, 0);
		}
	}

	@Override
	public void groupUpdated(AWTEvent e) {
		pGroup.set(selectedrow, group);
		((UserGroupBrowserModel) table.getModel()).fireTableDataChanged();
		table.updateUI();
		if (table.getRowCount() > 0 && selectedrow > -1) {
			table.setRowSelectionInterval(selectedrow, selectedrow);
		}
	}
	
	private static final int DEFAULT_WIDTH = 200;
	private static final int DEFAULT_HEIGHT = 150;
	private int selectedrow;
	private List<UserGroup> pGroup;
	private String[] pColumns = {
			MessageBundle.getMessage("angal.common.group.txt").toUpperCase(),
			MessageBundle.getMessage("angal.common.description.txt").toUpperCase()
	};
	private int[] pColumnWidth = {70,  100};
	private UserGroup group;
	private DefaultTableModel model;
	private JTable table;
	
	private UserGroupBrowsing myFrame;

	private UserBrowsingManager userBrowsingManager = Context.getApplicationContext().getBean(UserBrowsingManager.class);

	public UserGroupBrowsing() {
		myFrame = this;
		setTitle(MessageBundle.getMessage("angal.groupsbrowser.title"));
		
		setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
		Toolkit kit = Toolkit.getDefaultToolkit();
		Dimension screensize = kit.getScreenSize();
		int pfrmWidth = screensize.width / 2;
		int pfrmHeight = screensize.height / 4;
		setBounds(screensize.width / 4, screensize.height / 4, pfrmWidth, pfrmHeight);
		
		model = new UserGroupBrowserModel();
		table = new JTable(model);
		table.getColumnModel().getColumn(0).setPreferredWidth(pColumnWidth[0]);
		table.getColumnModel().getColumn(1).setPreferredWidth(pColumnWidth[1]);
				
		add(new JScrollPane(table), BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel();

		JButton buttonNew = new JButton(MessageBundle.getMessage("angal.common.new.btn"));
		buttonNew.setMnemonic(MessageBundle.getMnemonic("angal.common.new.btn.key"));
		buttonNew.addActionListener(actionEvent -> {
			group = new UserGroup();
			new GroupEdit(myFrame, group, true);
		});
		buttonPanel.add(buttonNew);

		JButton buttonEdit = new JButton(MessageBundle.getMessage("angal.common.edit.btn"));
		buttonEdit.setMnemonic(MessageBundle.getMnemonic("angal.common.edit.btn.key"));
		buttonEdit.addActionListener(actionEvent -> {
			if (table.getSelectedRow() < 0) {
				MessageDialog.error(null, "angal.common.pleaseselectarow.msg");
			} else {
				selectedrow = table.getSelectedRow();
				group = (UserGroup) model.getValueAt(table.getSelectedRow(), -1);
				new GroupEdit(myFrame, group, false);
			}
		});
		buttonPanel.add(buttonEdit);

		JButton buttonPrivilege = new JButton(MessageBundle.getMessage("angal.groupsbrowser.groupmenu.btn"));
		buttonPrivilege.setMnemonic(MessageBundle.getMnemonic("angal.groupsbrowser.groupmenu.btn.key"));
		buttonPrivilege.addActionListener(actionEvent -> {
			if (table.getSelectedRow() < 0) {
				MessageDialog.error(null, "angal.common.pleaseselectarow.msg");
			} else {
				UserGroup userGroup = (UserGroup) model.getValueAt(table.getSelectedRow(), -1);
				new PrivilegeTree(myFrame, userGroup);
			}
		});
		buttonPanel.add(buttonPrivilege);

		JButton buttonDelete = new JButton(MessageBundle.getMessage("angal.common.delete.btn"));
		buttonDelete.setMnemonic(MessageBundle.getMnemonic("angal.common.delete.btn.key"));
		buttonDelete.addActionListener(actionEvent -> {
			if (table.getSelectedRow() < 0) {
				MessageDialog.error(null, "angal.common.pleaseselectarow.msg");
			} else {
				UserGroup userGroup = (UserGroup) model.getValueAt(table.getSelectedRow(), -1);
				int answer = MessageDialog.yesNo(null, "angal.groupsbrowser.deletegroup.fmt.msg", userGroup.getCode());
				try {
					if (answer == JOptionPane.YES_OPTION) {
						userBrowsingManager.deleteGroup(userGroup);
						pGroup.remove(table.getSelectedRow());
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
		setLocationRelativeTo(null);
		setVisible(true);
	}

	class UserGroupBrowserModel extends DefaultTableModel {

		private static final long serialVersionUID = 1L;

		public UserGroupBrowserModel() {
            try {
                pGroup = userBrowsingManager.getUserGroup();
            } catch (OHServiceException e) {
                OHServiceExceptionUtil.showMessages(e);
            }
        }

		@Override
		public int getRowCount() {
			if (pGroup == null) {
				return 0;
			}
			return pGroup.size();
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
				return pGroup.get(r).getCode();
			} else if (c == -1) {
				return pGroup.get(r);
			} else if (c == 1) {
				return pGroup.get(r).getDesc();
			} 
			return null;
		}
		
		@Override
		public boolean isCellEditable(int arg0, int arg1) {
			return false;
		}
	}

}
