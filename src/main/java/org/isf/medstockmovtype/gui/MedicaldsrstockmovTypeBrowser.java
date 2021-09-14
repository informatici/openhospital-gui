/*
 * Open Hospital (www.open-hospital.org)
 * Copyright © 2006-2021 Informatici Senza Frontiere (info@informaticisenzafrontiere.org)
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
package org.isf.medstockmovtype.gui;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import org.isf.generaldata.MessageBundle;
import org.isf.medstockmovtype.gui.MedicaldsrstockmovTypeBrowserEdit.MedicaldsrstockmovTypeListener;
import org.isf.medstockmovtype.manager.MedicaldsrstockmovTypeBrowserManager;
import org.isf.medstockmovtype.model.MovementType;
import org.isf.menu.manager.Context;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.ModalJFrame;

/**
 * Browsing of table MedicalStockMovType
 *
 * @author Furlanetto, Zoia, Finotto
 */
public class MedicaldsrstockmovTypeBrowser extends ModalJFrame implements MedicaldsrstockmovTypeListener{

	private static final long serialVersionUID = 1L;
	private List<MovementType> pMedicaldsrstockmovType;
	private String[] pColumns = {
			MessageBundle.getMessage("angal.common.code.txt").toUpperCase(),
			MessageBundle.getMessage("angal.common.description.txt").toUpperCase(),
			MessageBundle.getMessage("angal.common.type.txt").toUpperCase()
	};
	private int[] pColumnWidth = {80, 200, 40};

	private JPanel jContainPanel = null;
	private JPanel jButtonPanel = null;
	private JButton jNewButton = null;
	private JButton jEditButton = null;
	private JButton jCloseButton = null;
	private JButton jDeleteButton = null;
	private JTable jTable = null;
	private MedicaldsrstockmovTypeBrowserModel model;
	private int selectedrow;
	private MedicaldsrstockmovTypeBrowserManager manager = Context.getApplicationContext().getBean(MedicaldsrstockmovTypeBrowserManager.class);
	private MovementType medicaldsrstockmovType = null;
	private final JFrame myFrame;
	
	/**
	 * This method initializes
	 */
	public MedicaldsrstockmovTypeBrowser() {
		super();
		myFrame = this;
		initialize();
		setVisible(true);
	}

	private void initialize() {
		this.setTitle(MessageBundle.getMessage("angal.medstockmovtype.medicalstockmovementtypebrowser.title"));
		this.setContentPane(getJContainPanel());
		pack();
		setLocationRelativeTo(null);
	}
	
	private JPanel getJContainPanel() {
		if (jContainPanel == null) {
			jContainPanel = new JPanel();
			jContainPanel.setLayout(new BorderLayout());
			jContainPanel.add(getJButtonPanel(), java.awt.BorderLayout.SOUTH);
			jContainPanel.add(new JScrollPane(getJTable()), java.awt.BorderLayout.CENTER);
			validate();
		}
		return jContainPanel;
	}
	
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

	private JButton getJNewButton() {
		if (jNewButton == null) {
			jNewButton = new JButton(MessageBundle.getMessage("angal.common.new.btn"));
			jNewButton.setMnemonic(MessageBundle.getMnemonic("angal.common.new.btn.key"));
			jNewButton.addActionListener(actionEvent -> {
				MovementType mdsr = new MovementType("","","");
				MedicaldsrstockmovTypeBrowserEdit newrecord = new MedicaldsrstockmovTypeBrowserEdit(myFrame,mdsr, true);
				newrecord.addMedicaldsrstockmovTypeListener(MedicaldsrstockmovTypeBrowser.this);
				newrecord.setVisible(true);
			});
		}
		return jNewButton;
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
				if (jTable.getSelectedRow() < 0) {
					MessageDialog.error(null, "angal.common.pleaseselectarow.msg");
				} else {
					selectedrow = jTable.getSelectedRow();
					medicaldsrstockmovType = (MovementType) (model.getValueAt(selectedrow, -1));
					MedicaldsrstockmovTypeBrowserEdit newrecord = new MedicaldsrstockmovTypeBrowserEdit(myFrame,medicaldsrstockmovType, false);
					newrecord.addMedicaldsrstockmovTypeListener(MedicaldsrstockmovTypeBrowser.this);
					newrecord.setVisible(true);
				}
			});
		}
		return jEditButton;
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
	 * This method initializes jDeleteButton
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJDeleteButton() {
		if (jDeleteButton == null) {
			jDeleteButton = new JButton(MessageBundle.getMessage("angal.common.delete.btn"));
			jDeleteButton.setMnemonic(MessageBundle.getMnemonic("angal.common.delete.btn.key"));
			jDeleteButton.addActionListener(actionEvent -> {
				if (jTable.getSelectedRow() < 0) {
					MessageDialog.error(null, "angal.common.pleaseselectarow.msg");
				} else {
					MovementType movType = (MovementType) (model.getValueAt(jTable.getSelectedRow(), -1));
					int answer = MessageDialog.yesNo(null, "angal.medstockmovtype.deletemovementtype.fmt.msg", movType.getDescription());
					if ((answer == JOptionPane.YES_OPTION)) {

						boolean deleted;

						try {
							deleted = manager.deleteMedicaldsrstockmovType(movType);
						} catch (OHServiceException e) {
							deleted = false;
							OHServiceExceptionUtil.showMessages(e);
						}

						if (deleted) {
							pMedicaldsrstockmovType.remove(jTable.getSelectedRow());
							model.fireTableDataChanged();
							jTable.updateUI();
						}
					}
				}
			});
		}
		return jDeleteButton;
	}
	
	public JTable getJTable() {
		if (jTable == null) {
			model = new MedicaldsrstockmovTypeBrowserModel();
			jTable = new JTable(model);
			jTable.getColumnModel().getColumn(0).setMinWidth(pColumnWidth[0]);
			jTable.getColumnModel().getColumn(1).setMinWidth(pColumnWidth[1]);
		}
		return jTable;
	}
	
	class MedicaldsrstockmovTypeBrowserModel extends DefaultTableModel {

		private static final long serialVersionUID = 1L;

		public MedicaldsrstockmovTypeBrowserModel() {
			try {
				pMedicaldsrstockmovType = manager.getMedicaldsrstockmovType();
			} catch (OHServiceException e) {
				pMedicaldsrstockmovType = null;
				OHServiceExceptionUtil.showMessages(e);
			}
		}

		@Override
		public int getRowCount() {
			if (pMedicaldsrstockmovType == null) {
				return 0;
			}
			return pMedicaldsrstockmovType.size();
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
			MovementType movType = pMedicaldsrstockmovType.get(r);
			if (c == 0) {
				return movType.getCode();
			} else if (c == -1) {
				return movType;
			} else if (c == 1) {
				return movType.getDescription();
			} else if (c == 2) {
				return movType.getType();
			}
			return null;
		}

		@Override
		public boolean isCellEditable(int arg0, int arg1) {
			return false;
		}
	}


	@Override
	public void medicaldsrstockmovTypeUpdated(AWTEvent e) {
		pMedicaldsrstockmovType.set(selectedrow, medicaldsrstockmovType);
		((MedicaldsrstockmovTypeBrowserModel) jTable.getModel()).fireTableDataChanged();
		jTable.updateUI();
		if ((jTable.getRowCount() > 0) && selectedrow > -1) {
			jTable.setRowSelectionInterval(selectedrow, selectedrow);
		}
	}
	
	
	@Override
	public void medicaldsrstockmovTypeInserted(AWTEvent e) {
		medicaldsrstockmovType = (MovementType)e.getSource();
		pMedicaldsrstockmovType.add(0, medicaldsrstockmovType);
		((MedicaldsrstockmovTypeBrowserModel) jTable.getModel()).fireTableDataChanged();
		if (jTable.getRowCount() > 0) {
			jTable.setRowSelectionInterval(0, 0);
		}
	}

}
