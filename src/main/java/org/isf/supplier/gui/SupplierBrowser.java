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
package org.isf.supplier.gui;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import org.isf.generaldata.MessageBundle;
import org.isf.menu.manager.Context;
import org.isf.supplier.gui.SupplierEdit.SupplierListener;
import org.isf.supplier.manager.SupplierBrowserManager;
import org.isf.supplier.model.Supplier;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.ModalJFrame;

/**
 * This class shows a list of suppliers.
 * It is possible to edit-insert-delete records
 * 
 * @author Mwithi
 */
public class SupplierBrowser extends ModalJFrame implements SupplierListener {

	private static final long serialVersionUID = 1L;

	@Override
	public void supplierInserted(AWTEvent e) {
		pSupplier.add(0, supplier);
		((SupplierBrowserModel) table.getModel()).fireTableDataChanged();
		if (table.getRowCount() > 0) {
			table.setRowSelectionInterval(0, 0);
		}
	}

	@Override
	public void supplierUpdated(AWTEvent e) {
		pSupplier.set(selectedrow, supplier);
		((SupplierBrowserModel) table.getModel()).fireTableDataChanged();
		table.updateUI();
		if (table.getRowCount() > 0 && selectedrow > -1) {
			table.setRowSelectionInterval(selectedrow, selectedrow);
		}
	}
	
	private int pfrmBase = 10;
	private int pfrmWidth = 8;
	private int pfrmHeight = 6;
	private JPanel jContentPane;
	private JPanel jButtonPanel;
	private JButton jEditButton;
	private JButton jNewButton;
	private JButton jDeleteButton;
	private JButton jCloseButton;
	private JScrollPane jScrollPane;
	private JTable table;
	private DefaultTableModel model;
	private String[] pColumns = {
			MessageBundle.getMessage("angal.common.id.txt").toUpperCase(),
			MessageBundle.getMessage("angal.common.name.txt").toUpperCase(),
			MessageBundle.getMessage("angal.common.address.txt").toUpperCase(),
			MessageBundle.getMessage("angal.supplier.taxcode.col").toUpperCase(),
			MessageBundle.getMessage("angal.common.telephone.txt").toUpperCase(),
			MessageBundle.getMessage("angal.common.fax.txt").toUpperCase(),
			MessageBundle.getMessage("angal.common.email.txt").toUpperCase(),
			MessageBundle.getMessage("angal.common.note.txt").toUpperCase(),
			MessageBundle.getMessage("angal.supplier.deleted.col").toUpperCase()
	};
	private int[] pColumnWidth = {45, 80, 60, 60, 80, 30, 30, 30, 30};
	private int selectedrow;
	private List<Supplier> pSupplier;
	private Supplier supplier;
	private final JFrame myFrame;

	private SupplierBrowserManager supplierBrowserManager = Context.getApplicationContext().getBean(SupplierBrowserManager.class);
	
	/**
	 * This is the default constructor
	 */
	public SupplierBrowser() {
		super();
		myFrame = this;
		initialize();
		setVisible(true);
	}
	
	/**
	 * This method initializes this
	 */
	private void initialize() {
		this.setTitle(MessageBundle.getMessage("angal.supplier.suppliersbrowser.title"));
		Toolkit kit = Toolkit.getDefaultToolkit();
		Dimension screensize = kit.getScreenSize();
		int pfrmBordX = (screensize.width - screensize.width / pfrmBase * pfrmWidth) / 2;
		int pfrmBordY = (screensize.height - screensize.height / pfrmBase * pfrmHeight) / 2;
		this.setBounds(pfrmBordX, pfrmBordY, screensize.width / pfrmBase * pfrmWidth, screensize.height / pfrmBase * pfrmHeight);
		this.setContentPane(getJContentPane());
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
			jContentPane.add(getJButtonPanel(), BorderLayout.SOUTH);
			jContentPane.add(getJScrollPane(), BorderLayout.CENTER);
		}
		return jContentPane;
	}
	
	/**
	 * This method initializes jButtonPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJButtonPanel() {
		if (jButtonPanel == null) {
			jButtonPanel = new JPanel();
			jButtonPanel.add(getJNewButton(), null);
			jButtonPanel.add(getJEditButton(), null);
			jButtonPanel.add(getJDeleteButton(), null);
			jButtonPanel.add(getJCloseButton(), null);
		}
		return jButtonPanel;
	}
	
	/**
	 * This method initializes jEditButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJEditButton() {
		if (jEditButton == null) {
			jEditButton = new JButton(MessageBundle.getMessage("angal.common.edit.btn"));
			jEditButton.setMnemonic(MessageBundle.getMnemonic("angal.common.edit.btn.key"));
			jEditButton.addActionListener(actionEvent -> {
				if (table.getSelectedRow() < 0) {
					MessageDialog.error(null, "angal.common.pleaseselectarow.msg");
				} else {
					selectedrow = table.getSelectedRow();
					supplier = (Supplier) model.getValueAt(table.getSelectedRow(), -1);
					SupplierEdit editrecord = new SupplierEdit(myFrame, supplier, false);
					editrecord.addSupplierListener(this);
					editrecord.setVisible(true);
				}
			});
		}
		return jEditButton;
	}
	
	/**
	 * This method initializes jNewButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJNewButton() {
		if (jNewButton == null) {
			jNewButton = new JButton(MessageBundle.getMessage("angal.common.new.btn"));
			jNewButton.setMnemonic(MessageBundle.getMnemonic("angal.common.new.btn.key"));
			jNewButton.addActionListener(actionEvent -> {
				supplier = new Supplier();    //operation will reference the new record
				SupplierEdit newrecord = new SupplierEdit(myFrame, supplier, true);
				newrecord.addSupplierListener(this);
				newrecord.setVisible(true);
			});
		}
		return jNewButton;
	}
	
	/**
	 * This method initializes jDeleteButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJDeleteButton() {
		if (jDeleteButton == null) {
			jDeleteButton = new JButton(MessageBundle.getMessage("angal.common.delete.btn"));
			jDeleteButton.setMnemonic(MessageBundle.getMnemonic("angal.common.delete.btn.key"));
			jDeleteButton.addActionListener(actionEvent -> {
				if (table.getSelectedRow() < 0) {
					MessageDialog.error(null, "angal.common.pleaseselectarow.msg");
				} else {
					Supplier m = (Supplier) model.getValueAt(table.getSelectedRow(), -1);
					if (m.getSupDeleted().equals('Y')) {
						return;
					}
					int answer = MessageDialog.yesNo(null, "angal.supplier.deletesupplier.fmt.msg", m.getSupName());
					if (answer == JOptionPane.YES_OPTION) {
						m.setSupDeleted('Y');
						try {
							supplierBrowserManager.saveOrUpdate(m);
						} catch (OHServiceException e) {
							OHServiceExceptionUtil.showMessages(e);
						}
						model.fireTableDataChanged();
						table.updateUI();
					}
				}
			});
		}
		return jDeleteButton;
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
	
	/**
	 * This method initializes jScrollPane	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getJScrollPane() {
		if (jScrollPane == null) {
			jScrollPane = new JScrollPane();
			jScrollPane.setViewportView(getJTable());
		}
		return jScrollPane;
	}
	
	/**
	 * This method initializes table	
	 * 	
	 * @return javax.swing.JTable	
	 */
	private JTable getJTable() {
		if (table == null) {
			model = new SupplierBrowserModel();
			table = new JTable(model);
			table.getColumnModel().getColumn(0).setMaxWidth(pColumnWidth[0]);
			table.getColumnModel().getColumn(1).setPreferredWidth(pColumnWidth[1]);
			table.getColumnModel().getColumn(2).setPreferredWidth(pColumnWidth[2]);
			table.getColumnModel().getColumn(3).setPreferredWidth(pColumnWidth[3]);
			table.getColumnModel().getColumn(4).setPreferredWidth(pColumnWidth[4]);
			table.getColumnModel().getColumn(5).setPreferredWidth(pColumnWidth[5]);
			table.getColumnModel().getColumn(6).setPreferredWidth(pColumnWidth[6]);
			table.getColumnModel().getColumn(7).setPreferredWidth(pColumnWidth[7]);
			table.getColumnModel().getColumn(8).setPreferredWidth(pColumnWidth[8]);
		}
		return table;
	}
	
	class SupplierBrowserModel extends DefaultTableModel {

		private static final long serialVersionUID = 1L;

		public SupplierBrowserModel() {
			try {
				pSupplier = supplierBrowserManager.getAll();
            } catch (OHServiceException e) {
                OHServiceExceptionUtil.showMessages(e);
            }
		}
		@Override
		public int getRowCount() {
			if (pSupplier == null) {
				return 0;
			}
			return pSupplier.size();
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
			Supplier sup = pSupplier.get(r);
			if (c == -1) {
				return sup;
			} else if (c == 0) {
				return sup.getSupId();
			} else if (c == 1) {
				return sup.getSupName();
			} else if (c == 2) {
				return sup.getSupAddress();
			} else if (c == 3) {
				return sup.getSupTaxcode();
			} else if (c == 4) {
				return sup.getSupPhone();
			} else if (c == 5) {
				return sup.getSupFax();
			} else if (c == 6) {
				return sup.getSupEmail();
			} else if (c == 7) {
				return sup.getSupNote();
			} else if (c == 8) {
				return sup.getSupDeleted().equals('Y');
			}
			return null;
		}
		
		/* (non-Javadoc)
		 * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
		 */
		@Override
		public Class<?> getColumnClass(int columnIndex) {
			if (columnIndex == pColumns.length - 1) {
				return Boolean.class;
			}
			return super.getColumnClass(columnIndex);
		}
		@Override
		public boolean isCellEditable(int arg0, int arg1) {
			//return super.isCellEditable(arg0, arg1);
			return false;
		}
	}
}
