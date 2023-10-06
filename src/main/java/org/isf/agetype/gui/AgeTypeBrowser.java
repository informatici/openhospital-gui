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
package org.isf.agetype.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import org.isf.agetype.manager.AgeTypeBrowserManager;
import org.isf.agetype.model.AgeType;
import org.isf.generaldata.MessageBundle;
import org.isf.menu.manager.Context;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.ModalJFrame;

/**
 * Browsing of table AgeType
 *
 * @author Alessandro
 */
public class AgeTypeBrowser extends ModalJFrame {

	private static final long serialVersionUID = 1L;
	private List<AgeType> pAgeType;
	private String[] pColumns = {
			MessageBundle.getMessage("angal.common.code.txt").toUpperCase(),
			MessageBundle.getMessage("angal.common.from.txt").toUpperCase(),
			MessageBundle.getMessage("angal.common.to.txt").toUpperCase(),
			MessageBundle.getMessage("angal.common.description.txt").toUpperCase()
	};
	private int[] pColumnWidth = {80, 80, 80, 200};
	private JPanel jContainPanel;
	private JPanel jButtonPanel;
	private JButton jEditSaveButton;
	private JButton jCloseButton;
	private JTable jTable;
	private boolean edit;

	private AgeTypeBrowserManager ageTypeBrowserManager = Context.getApplicationContext().getBean(AgeTypeBrowserManager.class);

	/**
	 * This method initializes
	 */
	public AgeTypeBrowser() {
		super();
		initialize();
		setVisible(true);
	}

	private void initialize() {
		setTitle(MessageBundle.getMessage("angal.agetype.agetypebrowser.title"));
		setContentPane(getJContainPanel());
		pack();
		setLocationRelativeTo(null);
	}

	private JPanel getJContainPanel() {
		if (jContainPanel == null) {
			jContainPanel = new JPanel();
			jContainPanel.setLayout(new BorderLayout());
			jContainPanel.add(getJButtonPanel(), BorderLayout.SOUTH);
			jContainPanel.add(getJTable(), BorderLayout.CENTER);
			validate();
		}
		return jContainPanel;
	}

	private JPanel getJButtonPanel() {
		if (jButtonPanel == null) {
			jButtonPanel = new JPanel();
			jButtonPanel.add(getJEditSaveButton(), null);
			jButtonPanel.add(getJCloseButton(), null);
		}
		return jButtonPanel;
	}

	/**
	 * This method initializes jEditSaveButton
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getJEditSaveButton() {
		if (jEditSaveButton == null) {
			jEditSaveButton = new JButton(MessageBundle.getMessage("angal.common.edit.btn"));
			jEditSaveButton.setMnemonic(MessageBundle.getMnemonic("angal.common.edit.btn.key"));
			jEditSaveButton.addActionListener(actionEvent -> {
				if (!edit) {
					edit = true;
					jEditSaveButton.setText(MessageBundle.getMessage("angal.common.save.btn"));
					jEditSaveButton.setMnemonic(MessageBundle.getMnemonic("angal.common.save.btn.key"));
					jTable.updateUI();
				} else {
					if (jTable.isEditing()) {
						jTable.getCellEditor().stopCellEditing();
					}
					try {
						ageTypeBrowserManager.updateAgeType(pAgeType);
					} catch (OHServiceException e) {
						OHServiceExceptionUtil.showMessages(e);
					}
					edit = false;
					jEditSaveButton.setText(MessageBundle.getMessage("angal.common.edit.btn"));
					jEditSaveButton.setMnemonic(MessageBundle.getMnemonic("angal.common.edit.btn.key"));
					jTable.updateUI();
				}
			});
		}
		return jEditSaveButton;
	}
	
	/**
	 * This method initializes jCloseButton
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getJCloseButton() {
		if (jCloseButton == null) {
			jCloseButton = new JButton(MessageBundle.getMessage("angal.common.close.btn"));
			jCloseButton.setMnemonic(MessageBundle.getMnemonic("angal.common.close.btn.key"));
			jCloseButton.addActionListener(actionEvent -> dispose());
		}
		return jCloseButton;
	}

	private JTable getJTable() {
		if (jTable == null) {
			jTable = new JTable(new AgeTypeBrowserModel());
			for (int i = 0; i < pColumns.length; i++) {
				jTable.getColumnModel().getColumn(i).setMinWidth(pColumnWidth[i]);
			}
			jTable.setDefaultRenderer(Object.class, new ColorTableCellRenderer());
		}
		return jTable;
	}

	class AgeTypeBrowserModel extends DefaultTableModel {

		private static final long serialVersionUID = 1L;

		public AgeTypeBrowserModel() {
			try {
				pAgeType = ageTypeBrowserManager.getAgeType();
			} catch (OHServiceException e) {
				pAgeType = new ArrayList<>();
				OHServiceExceptionUtil.showMessages(e);
			}
		}

		@Override
		public int getRowCount() {
			if (pAgeType == null) {
				return 0;
			}
			return pAgeType.size();
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
				return pAgeType.get(r).getCode();
			} else if (c == -1) {
				return pAgeType.get(r);
			} else if (c == 1) {
				return pAgeType.get(r).getFrom();
			} else if (c == 2) {
				return pAgeType.get(r).getTo();
			} else if (c == 3) {
				return MessageBundle.getMessage(pAgeType.get(r).getDescription());
			}
			return null;
		}
		
		@Override
		public void setValueAt(Object value, int row, int col) {
			int number;
			try {
				number = Integer.parseInt((String) value);
			} catch (NumberFormatException e) {
				MessageDialog.error(AgeTypeBrowser.this, "angal.agetype.insertvalidage");
				return;
			}
			
			if (col == 1) {
				pAgeType.get(row).setFrom(number);
			} else if (col == 2) {
				pAgeType.get(row).setTo(number);
			}
	        fireTableCellUpdated(row, col);
	    }

		@Override
		public boolean isCellEditable(int r, int c) {
			if (edit) {
				return c == 1 || c == 2;
			}
			return false;
		}
	}
	
	class ColorTableCellRenderer extends DefaultTableCellRenderer {

		private static final long serialVersionUID = 1L;

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			if (edit) {
				if (column == 0 || column == 3) {
					cell.setBackground(Color.LIGHT_GRAY);
				} else {
					cell.setBackground(Color.WHITE);
				}
			} else {
				cell.setBackground(Color.WHITE);
			}
			return cell;
		}
	}
}
