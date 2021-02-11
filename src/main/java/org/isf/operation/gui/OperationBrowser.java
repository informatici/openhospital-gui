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
package org.isf.operation.gui;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

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
import org.isf.operation.manager.OperationBrowserManager;
import org.isf.operation.model.Operation;
import org.isf.opetype.manager.OperationTypeBrowserManager;
import org.isf.opetype.model.OperationType;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.ModalJFrame;

/**
 * This class shows a list of operations. It is possible to filter data with a
 * selection combo box and edit-insert-delete records
 *
 * @author Rick, Vero, Pupo
 */
public class OperationBrowser extends ModalJFrame implements OperationEdit.OperationListener {

	private static final long serialVersionUID = 1L;

	public void operationInserted(AWTEvent e) {
		pOperation.add(0, operation);
		((OperationBrowserModel) table.getModel()).fireTableDataChanged();
		// table.updateUI();
		if (table.getRowCount() > 0)
			table.setRowSelectionInterval(0, 0);
	}

	public void operationUpdated(AWTEvent e) {
		pOperation.set(selectedrow, operation);
		((OperationBrowserModel) table.getModel()).fireTableDataChanged();
		table.updateUI();
		if ((table.getRowCount() > 0) && selectedrow > -1)
			table.setRowSelectionInterval(selectedrow, selectedrow);

	}
	
	//TODO: replace with mapping mnemonic / translation in OperationBrowserManager
	public static String OPD = MessageBundle.getMessage("angal.admission.opd").toUpperCase();
	public static String ADMISSION = MessageBundle.getMessage("angal.admission.admission").toUpperCase();
	public static String OPD_ADMISSION = OPD + " / " + ADMISSION;

	private int pfrmBase = 8;
	private int pfrmWidth = 5;
	private int pfrmHeight = 5;
	private int pfrmBordX;
	private int pfrmBordY;
	private int selectedrow;
	private JLabel selectlabel;
	private JComboBox pbox;
	private ArrayList<Operation> pOperation;
	private String[] pColumns = {
			MessageBundle.getMessage("angal.operation.idm"), //$NON-NLS-1$
			MessageBundle.getMessage("angal.operation.typem"),  //$NON-NLS-1$
			MessageBundle.getMessage("angal.operation.namem"),  //$NON-NLS-1$
			MessageBundle.getMessage("angal.operation.operationcontext").toUpperCase() //$NON-NLS-1$
	};
	private int[] pColumnWidth = { 50, 180, 200, 100 };
	private Operation operation;
	private DefaultTableModel model;
	private JTable table;
	private JFrame myFrame;
	private String pSelection;
	
	private OperationBrowserManager operationManager = Context.getApplicationContext().getBean(OperationBrowserManager.class);
	private OperationTypeBrowserManager operationTypeManager = Context.getApplicationContext().getBean(OperationTypeBrowserManager.class);
	
	public OperationBrowser() {

		setTitle(MessageBundle.getMessage("angal.operation.operationsbrowser")); //$NON-NLS-1$
		Toolkit kit = Toolkit.getDefaultToolkit();
		Dimension screensize = kit.getScreenSize();
		pfrmBordX = (screensize.width - (screensize.width / pfrmBase * pfrmWidth)) / 2;
		pfrmBordY = (screensize.height - (screensize.height / pfrmBase * pfrmHeight)) / 2;
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

		selectlabel = new JLabel(MessageBundle.getMessage("angal.operation.selecttype")); //$NON-NLS-1$
		buttonPanel.add(selectlabel);

		pbox = new JComboBox();
		pbox.addItem(MessageBundle.getMessage("angal.operation.allm")); //$NON-NLS-1$
		ArrayList<OperationType> type;
		try {
			type = operationTypeManager.getOperationType();
			for (OperationType elem : type) {
				pbox.addItem(elem);
			}
		} catch (OHServiceException e1) {
			type = null;
			OHServiceExceptionUtil.showMessages(e1);
		}

		pbox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				pSelection = pbox.getSelectedItem().toString();
				if (pSelection.compareTo(MessageBundle.getMessage("angal.operation.allm")) == 0) //$NON-NLS-1$
					model = new OperationBrowserModel();
				else
					model = new OperationBrowserModel(pSelection);
				model.fireTableDataChanged();
				table.updateUI();
			}
		});
		buttonPanel.add(pbox);

		JButton buttonNew = new JButton(MessageBundle.getMessage("angal.common.new")); //$NON-NLS-1$
		buttonNew.setMnemonic(KeyEvent.VK_N);
		buttonNew.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent event) {
				operation = new Operation(null, "", new OperationType("", ""), 0); // operation will reference the new //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
																					// record
				OperationEdit newrecord = new OperationEdit(myFrame, operation, true);
				newrecord.addOperationListener(OperationBrowser.this);
				newrecord.setVisible(true);
			}
		});
		buttonPanel.add(buttonNew);

		JButton buttonEdit = new JButton(MessageBundle.getMessage("angal.common.edit")); //$NON-NLS-1$
		buttonEdit.setMnemonic(KeyEvent.VK_E);
		buttonEdit.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent event) {
				if (table.getSelectedRow() < 0) {
					JOptionPane.showMessageDialog(null, MessageBundle.getMessage("angal.common.pleaseselectarow"), //$NON-NLS-1$
							MessageBundle.getMessage("angal.hospital"), JOptionPane.PLAIN_MESSAGE); //$NON-NLS-1$
					return;
				} else {
					selectedrow = table.getSelectedRow();
					operation = (Operation) (((OperationBrowserModel) model).getValueAt(table.getSelectedRow(), -1));
					OperationEdit editrecord = new OperationEdit(myFrame, operation, false);
					editrecord.addOperationListener(OperationBrowser.this);
					editrecord.setVisible(true);
				}
			}
		});
		buttonPanel.add(buttonEdit);

		JButton buttonDelete = new JButton(MessageBundle.getMessage("angal.common.delete")); //$NON-NLS-1$
		buttonDelete.setMnemonic(KeyEvent.VK_D);
		buttonDelete.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (table.getSelectedRow() < 0) {
					JOptionPane.showMessageDialog(null, MessageBundle.getMessage("angal.common.pleaseselectarow"), //$NON-NLS-1$
							MessageBundle.getMessage("angal.hospital"), JOptionPane.PLAIN_MESSAGE); //$NON-NLS-1$
					return;
				} else {
					Operation m = (Operation) (((OperationBrowserModel) model).getValueAt(table.getSelectedRow(), -1));
					int n = JOptionPane.showConfirmDialog(
							null, MessageBundle.getMessage("angal.operation.deleteoperation") + " \"" //$NON-NLS-1$ //$NON-NLS-2$
									+ m.getDescription() + "\" ?", //$NON-NLS-1$
							MessageBundle.getMessage("angal.hospital"), JOptionPane.YES_NO_OPTION); //$NON-NLS-1$
					try {
						if ((n == JOptionPane.YES_OPTION) && (operationManager.deleteOperation(m))) {
							pOperation.remove(table.getSelectedRow());
							model.fireTableDataChanged();
							table.updateUI();
						}
					} catch (OHServiceException e) {
						OHServiceExceptionUtil.showMessages(e);
					}
				}
			}
		});
		buttonPanel.add(buttonDelete);

		JButton buttonClose = new JButton(MessageBundle.getMessage("angal.common.close")); //$NON-NLS-1$
		buttonClose.setMnemonic(KeyEvent.VK_C);
		buttonClose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				dispose();
			}
		});
		buttonPanel.add(buttonClose);
		add(buttonPanel, BorderLayout.SOUTH);

		setVisible(true);
	}

	class OperationBrowserModel extends DefaultTableModel {

		private static final long serialVersionUID = 1L;

		public OperationBrowserModel(String s) {
			try {
				pOperation = operationManager.getOperationByTypeDescription(s);
			} catch (OHServiceException e) {
				OHServiceExceptionUtil.showMessages(e);
			}
		}

		public OperationBrowserModel() {
			try {
				pOperation = operationManager.getOperation();
			} catch (OHServiceException e) {
				OHServiceExceptionUtil.showMessages(e);
			}

		}

		public int getRowCount() {
			if (pOperation == null)
				return 0;
			return pOperation.size();
		}

		public String getColumnName(int c) {
			return pColumns[c];
		}

		public int getColumnCount() {
			return pColumns.length;
		}

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
					return MessageBundle.getMessage("angal.common.notdefined");
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
			
			Component cell=super.getTableCellRendererComponent(table,value,isSelected,hasFocus,row,column);
			setHorizontalAlignment(SwingConstants.CENTER);
			return cell;
		}
	}

}
