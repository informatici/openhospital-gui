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
package org.isf.medtype.gui;

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
import org.isf.medtype.gui.MedicalTypeBrowserEdit.MedicalTypeListener;
import org.isf.medtype.manager.MedicalTypeBrowserManager;
import org.isf.medtype.model.MedicalType;
import org.isf.menu.manager.Context;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.ModalJFrame;

/**
 * Browsing of table MedicalType
 *
 * @author Furlanetto, Zoia, Finotto
 */
public class MedicalTypeBrowser extends ModalJFrame implements MedicalTypeListener {

	private static final long serialVersionUID = 1L;
	private List<MedicalType> pMedicalType;
	private String[] pColumns = {
			MessageBundle.getMessage("angal.common.code.txt").toUpperCase(),
			MessageBundle.getMessage("angal.common.description.txt").toUpperCase()
	};
	private int[] pColumnWidth = {80, 200};
	private JPanel jContainPanel;
	private JPanel jButtonPanel;
	private JButton jNewButton;
	private JButton jEditButton;
	private JButton jCloseButton;
	private JButton jDeleteButton;
	private JTable jTable;
	private MedicalTypeBrowserModel model;
	private int selectedrow;
	private MedicalTypeBrowserManager medicalTypeBrowserManager = Context.getApplicationContext().getBean(MedicalTypeBrowserManager.class);
	private MedicalType medicalType;
	private final JFrame myFrame;
	
	/**
	 * This method initializes
	 */
	public MedicalTypeBrowser() {
		super();
		myFrame = this;
		initialize();
		setVisible(true);
	}
	
	private void initialize() {
		this.setTitle(MessageBundle.getMessage("angal.medtype.medicaltypebrowser.title"));
		this.setContentPane(getJContainPanel());
		pack();
		setLocationRelativeTo(null);
	}
	
	private JPanel getJContainPanel() {
		if (jContainPanel == null) {
			jContainPanel = new JPanel();
			jContainPanel.setLayout(new BorderLayout());
			jContainPanel.add(getJButtonPanel(), BorderLayout.SOUTH);
			jContainPanel.add(new JScrollPane(getJTable()), BorderLayout.CENTER);
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
				medicalType = new MedicalType("", "");
				MedicalTypeBrowserEdit newrecord = new MedicalTypeBrowserEdit(myFrame, medicalType, true);
				newrecord.addMedicalTypeListener(this);
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
					MessageDialog.error(this, "angal.common.pleaseselectarow.msg");
				} else {
					selectedrow = jTable.getSelectedRow();
					medicalType = (MedicalType) model.getValueAt(selectedrow, -1);
					MedicalTypeBrowserEdit newrecord = new MedicalTypeBrowserEdit(myFrame, medicalType, false);
					newrecord.addMedicalTypeListener(this);
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
					MessageDialog.error(this, "angal.common.pleaseselectarow.msg");
				} else {
					MedicalType medType = (MedicalType) model.getValueAt(jTable.getSelectedRow(), -1);
					int answer = MessageDialog.yesNo(null, "angal.medtype.deletemedicaltype.fmt.msg", medType.getDescription());
					if (answer == JOptionPane.YES_OPTION) {
						try {
							medicalTypeBrowserManager.deleteMedicalType(medType);
							pMedicalType.remove(jTable.getSelectedRow());
							model.fireTableDataChanged();
							jTable.updateUI();
						} catch (OHServiceException serviceException) {
							OHServiceExceptionUtil.showMessages(serviceException);
						}
					}
				}
			});
		}
		return jDeleteButton;
	}

	private JTable getJTable() {
		if (jTable == null) {
			model = new MedicalTypeBrowserModel();
			jTable = new JTable(model);
			jTable.getColumnModel().getColumn(0).setMinWidth(pColumnWidth[0]);
			jTable.getColumnModel().getColumn(1).setMinWidth(pColumnWidth[1]);
		}
		return jTable;
	}
	
	
	class MedicalTypeBrowserModel extends DefaultTableModel {
		
		private static final long serialVersionUID = 1L;

		public MedicalTypeBrowserModel() {
			try {
				pMedicalType = medicalTypeBrowserManager.getMedicalType();
			} catch (OHServiceException e) {
				pMedicalType = null;
				OHServiceExceptionUtil.showMessages(e);
			}
		}
		
		@Override
		public int getRowCount() {
			if (pMedicalType == null) {
				return 0;
			}
			return pMedicalType.size();
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
			MedicalType medType = pMedicalType.get(r);
			if (c == -1) {
				return medType;
			} else if (c == 0) {
				return medType.getCode();
			} else if (c == 1) {
				return medType.getDescription();
			}
			return null;
		}
		
		@Override
		public boolean isCellEditable(int arg0, int arg1) {
			return false;
		}
	}

	@Override
	public void medicalTypeUpdated(AWTEvent e) {
		pMedicalType.set(selectedrow, medicalType);
		((MedicalTypeBrowserModel) jTable.getModel()).fireTableDataChanged();
		jTable.updateUI();
		if (jTable.getRowCount() > 0 && selectedrow > -1) {
			jTable.setRowSelectionInterval(selectedrow, selectedrow);
		}
	}
	
	
	@Override
	public void medicalTypeInserted(AWTEvent e) {
		pMedicalType.add(0, medicalType);
		((MedicalTypeBrowserModel) jTable.getModel()).fireTableDataChanged();
		if (jTable.getRowCount() > 0) {
			jTable.setRowSelectionInterval(0, 0);
		}
	}
}
