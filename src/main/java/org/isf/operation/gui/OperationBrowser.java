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
package org.isf.operation.gui;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import org.isf.generaldata.MessageBundle;
import org.isf.menu.manager.Context;
import org.isf.operation.gui.OperationEdit.OperationListener;
import org.isf.operation.manager.OperationBrowserManager;
import org.isf.operation.model.Operation;
import org.isf.opetype.manager.OperationTypeBrowserManager;
import org.isf.opetype.model.OperationType;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.ModalJFrame;

/**
 * This class shows a list of operations. It is possible to filter data with a
 * selection combo box and edit-insert-delete records
 *
 * @author Rick, Vero, Pupo
 */
public class OperationBrowser extends ModalJFrame implements OperationListener {

	private static final long serialVersionUID = 1L;
	private static final String STR_ALL = MessageBundle.getMessage("angal.common.all.txt").toUpperCase();

	@Override
	public void operationInserted(AWTEvent e) {
		pOperation.add(0, operation);
		((OperationBrowserModel) table.getModel()).fireTableDataChanged();
		if (table.getRowCount() > 0) {
			table.setRowSelectionInterval(0, 0);
		}
	}

	@Override
	public void operationUpdated(AWTEvent e) {
		pOperation.set(selectedrow, operation);
		((OperationBrowserModel) table.getModel()).fireTableDataChanged();
		table.updateUI();
		if ((table.getRowCount() > 0) && selectedrow > -1) {
			table.setRowSelectionInterval(selectedrow, selectedrow);
		}
	}
	
	//TODO: replace with mapping mnemonic / translation in OperationBrowserManager
	public static final String OPD = MessageBundle.getMessage("angal.admission.opd.txt").toUpperCase();
	public static final String ADMISSION = MessageBundle.getMessage("angal.admission.admission.txt").toUpperCase();
	public static final String OPD_ADMISSION = OPD + " / " + ADMISSION;

	private static final int pfrmBase = 8;
	private static final int pfrmWidth = 5;
	private static final int pfrmHeight = 5;
	private int selectedrow;
	private JComboBox<OperationType> diseaseTypeFilter;
	private List<Operation> pOperation;
	private String[] pColumns = {
			MessageBundle.getMessage("angal.common.id.txt").toUpperCase(),
			MessageBundle.getMessage("angal.common.type.txt").toUpperCase(),
			MessageBundle.getMessage("angal.common.name.txt").toUpperCase(),
			MessageBundle.getMessage("angal.operation.operationcontext.col").toUpperCase()
	};
	private int[] pColumnWidth = { 50, 180, 200, 100 };
	private Operation operation;
	private DefaultTableModel model;
	private JTable table;
	private JFrame myFrame;
	private String pSelection;
	
	private OperationBrowserManager operationBrowserManager = Context.getApplicationContext().getBean(OperationBrowserManager.class);
	private OperationTypeBrowserManager operationTypeBrowserManager = Context.getApplicationContext().getBean(OperationTypeBrowserManager.class);
	
	public OperationBrowser() {

		setTitle(MessageBundle.getMessage("angal.operation.operationsbrowser.title"));
		Toolkit kit = Toolkit.getDefaultToolkit();
		Dimension screensize = kit.getScreenSize();
		int pfrmBordX = (screensize.width - (screensize.width / pfrmBase * pfrmWidth)) / 2;
		int pfrmBordY = (screensize.height - (screensize.height / pfrmBase * pfrmHeight)) / 2;
		this.setBounds(pfrmBordX, pfrmBordY, screensize.width / pfrmBase * pfrmWidth,
				screensize.height / pfrmBase * pfrmHeight);
		myFrame = this;
		model = new OperationBrowserModel();
		table = new JTable(model);
		table.getColumnModel().getColumn(0).setMaxWidth(pColumnWidth[0]);
		table.getColumnModel().getColumn(1).setPreferredWidth(pColumnWidth[1]);
		table.getColumnModel().getColumn(2).setPreferredWidth(pColumnWidth[2]);
		table.getColumnModel().getColumn(3).setPreferredWidth(pColumnWidth[3]);
		table.getColumnModel().getColumn(3).setCellRenderer(new CenterAlignmentCellRenderer());

		setLayout(new BorderLayout());
		add(new JScrollPane(table), BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel();

		JLabel selectlabel = new JLabel(MessageBundle.getMessage("angal.operation.selecttype")); //$NON-NLS-1$
		buttonPanel.add(selectlabel);

		diseaseTypeFilter = new JComboBox<>();
		diseaseTypeFilter.addItem(new OperationType("", MessageBundle.getMessage("angal.common.all.txt").toUpperCase()));
		List<OperationType> type;
		try {
			type = operationTypeBrowserManager.getOperationType();
			for (OperationType elem : type) {
				diseaseTypeFilter.addItem(elem);
			}
		} catch (OHServiceException e1) {
			OHServiceExceptionUtil.showMessages(e1);
		}

		diseaseTypeFilter.addActionListener(actionEvent -> {
			pSelection = diseaseTypeFilter.getSelectedItem().toString();
			if (pSelection.compareTo(STR_ALL) == 0) {
				model = new OperationBrowserModel();
			} else {
				model = new OperationBrowserModel(pSelection);
			}
			model.fireTableDataChanged();
			table.updateUI();
		});
		buttonPanel.add(diseaseTypeFilter);

		JButton buttonNew = new JButton(MessageBundle.getMessage("angal.common.new.btn"));
		buttonNew.setMnemonic(MessageBundle.getMnemonic("angal.common.new.btn.key"));
		buttonNew.addActionListener(actionEvent -> {
			operation = new Operation(null, "", new OperationType("", ""), 0); // operation will reference the new record
			OperationEdit newrecord = new OperationEdit(myFrame, operation, true);
			newrecord.addOperationListener(this);
			newrecord.setVisible(true);
		});
		buttonPanel.add(buttonNew);

		JButton buttonEdit = new JButton(MessageBundle.getMessage("angal.common.edit.btn"));
		buttonEdit.setMnemonic(MessageBundle.getMnemonic("angal.common.edit.btn.key"));
		buttonEdit.addActionListener(actionEvent -> {
			if (table.getSelectedRow() < 0) {
				MessageDialog.error(null, "angal.common.pleaseselectarow.msg");
			} else {
				selectedrow = table.getSelectedRow();
				operation = (Operation) (model.getValueAt(table.getSelectedRow(), -1));
				OperationEdit editrecord = new OperationEdit(myFrame, operation, false);
				editrecord.addOperationListener(this);
				editrecord.setVisible(true);
			}
		});
		buttonPanel.add(buttonEdit);

		JButton buttonDelete = new JButton(MessageBundle.getMessage("angal.common.delete.btn"));
		buttonDelete.setMnemonic(MessageBundle.getMnemonic("angal.common.delete.btn.key"));
		buttonDelete.addActionListener(actionEvent -> {
			if (table.getSelectedRow() < 0) {
				MessageDialog.error(null, "angal.common.pleaseselectarow.msg");
			} else {
				Operation operation = (Operation) model.getValueAt(table.getSelectedRow(), -1);
				int answer = MessageDialog.yesNo(null, "angal.operation.deleteoperation.fmt.msg", operation.getDescription());
				if (answer == JOptionPane.YES_OPTION) {
					try {
						operationBrowserManager.deleteOperation(operation);
						pOperation.remove(table.getSelectedRow());
						model.fireTableDataChanged();
						table.updateUI();
					} catch (OHServiceException e) {
						OHServiceExceptionUtil.showMessages(e);
					}
				}
			}
		});
		buttonPanel.add(buttonDelete);

		JButton buttonClose = new JButton(MessageBundle.getMessage("angal.common.close.btn"));
		buttonClose.setMnemonic(MessageBundle.getMnemonic("angal.common.close.btn.key"));
		buttonClose.addActionListener(actionEvent -> dispose());
		buttonPanel.add(buttonClose);
		add(buttonPanel, BorderLayout.SOUTH);

		setVisible(true);
	}

	class OperationBrowserModel extends DefaultTableModel {

		private static final long serialVersionUID = 1L;

		public OperationBrowserModel(String s) {
			try {
				pOperation = operationBrowserManager.getOperationByTypeDescription(s);
			} catch (OHServiceException e) {
				OHServiceExceptionUtil.showMessages(e);
			}
		}

		public OperationBrowserModel() {
			try {
				pOperation = operationBrowserManager.getOperation();
			} catch (OHServiceException e) {
				OHServiceExceptionUtil.showMessages(e);
			}

		}

		@Override
		public int getRowCount() {
			if (pOperation == null) {
				return 0;
			}
			return pOperation.size();
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
			Operation operation = pOperation.get(r);
			String p = operation.getOpeFor();
			if (c == 0) {
				return operation.getCode();
			} else if (c == -1) {
				return operation;
			} else if (c == 1) {
				return operation.getType().getDescription();
			} else if (c == 2) {
				return operation.getDescription();
			} else if (c == 3) { // TODO: use bundles
				if (p != null) {
					if (p.equals("1")) {
						return OPD_ADMISSION;
					} else if (p.equals("2")) {
						return ADMISSION;
					} else {
						return OPD;
					}
				} else {
					return MessageBundle.getMessage("angal.common.notdefined.txt");
				}
			}
			return null;
		}

		@Override
		public boolean isCellEditable(int arg0, int arg1) {
			// return super.isCellEditable(arg0, arg1);
			return false;
		}
	}

	class CenterAlignmentCellRenderer extends DefaultTableCellRenderer {

		private static final long serialVersionUID = 1L;

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

			Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			setHorizontalAlignment(SwingConstants.CENTER);
			return cell;
		}
	}

}
