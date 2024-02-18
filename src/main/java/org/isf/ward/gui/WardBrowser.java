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
package org.isf.ward.gui;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

import org.isf.generaldata.MessageBundle;
import org.isf.menu.manager.Context;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.ModalJFrame;
import org.isf.ward.gui.WardEdit.WardListener;
import org.isf.ward.manager.WardBrowserManager;
import org.isf.ward.model.Ward;

import com.github.lgooddatepicker.zinternaltools.WrapLayout;

/**
 * This class shows a list of wards.
 * It is possible to edit-insert-delete records
 * 
 * @author Rick
 */
public class WardBrowser extends ModalJFrame implements WardListener {

	private static final long serialVersionUID = 1L;

	@Override
	public void wardInserted(AWTEvent e) {
		pWard.add(0, ward);
		((WardBrowserModel) table.getModel()).fireTableDataChanged();
		if (table.getRowCount() > 0) {
			table.setRowSelectionInterval(0, 0);
		}
	}

	@Override
	public void wardUpdated(AWTEvent e) {
		pWard.set(selectedrow, ward);
		((WardBrowserModel) table.getModel()).fireTableDataChanged();
		table.updateUI();
		if (table.getRowCount() > 0 && selectedrow > -1) {
			table.setRowSelectionInterval(selectedrow, selectedrow);
		}
	}

	private WardBrowserManager wardBrowserManager = Context.getApplicationContext().getBean(WardBrowserManager.class);

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
			MessageBundle.getMessage("angal.common.code.txt").toUpperCase(),
			MessageBundle.getMessage("angal.common.name.txt").toUpperCase(),
			MessageBundle.getMessage("angal.common.telephone.txt").toUpperCase(),
			MessageBundle.getMessage("angal.common.fax.txt").toUpperCase(),
			MessageBundle.getMessage("angal.common.email.txt").toUpperCase(),
			MessageBundle.getMessage("angal.ward.beds.col").toUpperCase(),
			MessageBundle.getMessage("angal.ward.nurses.col").toUpperCase(),
			MessageBundle.getMessage("angal.ward.doctors.col").toUpperCase(),
			MessageBundle.getMessage("angal.ward.hasopd.col").toUpperCase(),
			MessageBundle.getMessage("angal.ward.haspharmacy.col").toUpperCase(),
			MessageBundle.getMessage("angal.common.male.txt").toUpperCase(),
			MessageBundle.getMessage("angal.common.female.txt").toUpperCase(),
			MessageBundle.getMessage("angal.ward.duration.col").toUpperCase()
	};
	private int[] pColumnWidth = {45, 80, 60, 60, 80, 30, 30, 30, 30, 30, 30, 30, 30};
	private Class[] pColumnClass = {String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, Boolean.class, Boolean.class, Boolean.class, Boolean.class, int.class};
	private int selectedrow;
	private List<Ward> pWard;
	private Ward ward;
	private final JFrame myFrame;
	
	/**
	 * This is the default constructor
	 */
	public WardBrowser() {
		super();
		myFrame = this;
		//check if in the db maternity and OPD wards exist
		WardBrowserManager wardBrowserManager = Context.getApplicationContext().getBean(WardBrowserManager.class);
		try {
			wardBrowserManager.maternityControl(true);
			wardBrowserManager.opdControl(true);
		} catch (OHServiceException e) {
			OHServiceExceptionUtil.showMessages(e);
		}
		this.setTitle(MessageBundle.getMessage("angal.ward.wardbrowser.title"));
		setContentPane(getJContentPane());
		setMinimumSize(new Dimension(800, 400));
		setPreferredSize(new Dimension(1100, 400));
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jContentPane = new JPanel(new BorderLayout());
			jContentPane.add(getJScrollPane(), BorderLayout.CENTER);
			jContentPane.add(getJButtonPanel(), BorderLayout.SOUTH);
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
			jButtonPanel = new JPanel(new WrapLayout());
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
					ward = (Ward) model.getValueAt(table.getSelectedRow(), -1);
					WardEdit editrecord = new WardEdit(myFrame, ward, false);
					editrecord.addWardListener(this);
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
				ward = new Ward(null, "", "", "", "", null, null, null, false, false);    //operation will reference the new record
				WardEdit newrecord = new WardEdit(myFrame, ward, true);
				newrecord.addWardListener(this);
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
					MessageDialog.error(this, "angal.common.pleaseselectarow.msg");
				} else {
					Ward ward = (Ward) model.getValueAt(table.getSelectedRow(), -1);
					int answer = MessageDialog.yesNo(this, "angal.ward.deleteward.fmt.msg", ward.getDescription());
					try {
						if (answer == JOptionPane.YES_OPTION) {
							wardBrowserManager.deleteWard(ward);
							pWard.remove(table.getSelectedRow());
							model.fireTableDataChanged();
							table.updateUI();
						}
					} catch (OHServiceException e) {
						OHServiceExceptionUtil.showMessages(e);
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
			model = new WardBrowserModel();
			table = new JTable(model);
			TableColumnModel columnModel = table.getColumnModel();
			columnModel.getColumn(0).setMaxWidth(pColumnWidth[0]);
			columnModel.getColumn(1).setPreferredWidth(pColumnWidth[1]);
			columnModel.getColumn(2).setPreferredWidth(pColumnWidth[2]);
			columnModel.getColumn(3).setPreferredWidth(pColumnWidth[3]);
			columnModel.getColumn(4).setPreferredWidth(pColumnWidth[4]);
			columnModel.getColumn(5).setPreferredWidth(pColumnWidth[5]);
			columnModel.getColumn(6).setPreferredWidth(pColumnWidth[6]);
			columnModel.getColumn(7).setPreferredWidth(pColumnWidth[7]);
			columnModel.getColumn(8).setPreferredWidth(pColumnWidth[8]);
			columnModel.getColumn(8).setPreferredWidth(pColumnWidth[8]);
			columnModel.getColumn(9).setPreferredWidth(pColumnWidth[9]);
			columnModel.getColumn(10).setPreferredWidth(pColumnWidth[10]);
			columnModel.getColumn(11).setPreferredWidth(pColumnWidth[11]);
		}
		return table;
	}
	
	class WardBrowserModel extends DefaultTableModel {

		private static final long serialVersionUID = 1L;

		public WardBrowserModel() {
			try {
				pWard = wardBrowserManager.getWards();
			} catch (OHServiceException e) {
				pWard = new ArrayList<>();
				OHServiceExceptionUtil.showMessages(e);
			}
		}

		@Override
		public int getRowCount() {
			if (pWard == null) {
				return 0;
			}
			return pWard.size();
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
			Ward ward = pWard.get(r);
			int i = 0;
			if (c == 0) {
				return ward.getCode();
			} else if (c == -1) {
				return ward;
			} else if (c == ++i) {
				return ward.getDescription();
			} else if (c == ++i) {
				return ward.getTelephone();
			} else if (c == ++i) {
				return ward.getFax();
			} else if (c == ++i) {
				return ward.getEmail();
			} else if (c == ++i) {
				return ward.getBeds();
			} else if (c == ++i) {
				return ward.getNurs();
			} else if (c == ++i) {
				return ward.getDocs();
			} else if (c == ++i) {
				return ward.isOpd();
			} else if (c == ++i) {
				return ward.isPharmacy();
			} else if (c == ++i) {
				return ward.isMale();
			} else if (c == ++i) {
				return ward.isFemale();
			} else if (c == ++i) {
				return ward.getVisitDuration();
			}
			return null;
		}
		
		/* (non-Javadoc)
		 * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
		 */
		@Override
		public Class<?> getColumnClass(int columnIndex) {
			return pColumnClass[columnIndex];
		}

		@Override
		public boolean isCellEditable(int arg0, int arg1) {
			return false;
		}
	}

}
