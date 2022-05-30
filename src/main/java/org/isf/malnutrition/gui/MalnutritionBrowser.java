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
package org.isf.malnutrition.gui;

import static org.isf.utils.Constants.DATE_FORMAT_DD_MM_YYYY;

import java.time.LocalDateTime;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import org.isf.admission.model.Admission;
import org.isf.generaldata.MessageBundle;
import org.isf.malnutrition.gui.InsertMalnutrition.MalnutritionListener;
import org.isf.malnutrition.manager.MalnutritionManager;
import org.isf.malnutrition.model.Malnutrition;
import org.isf.menu.manager.Context;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.time.TimeTools;

public class MalnutritionBrowser extends JDialog implements MalnutritionListener {
	
	private static final long serialVersionUID = 1L;
	
	@Override
	public void malnutritionInserted() {
		pMaln.add(pMaln.size(), malnutrition);
		((MalnBrowsingModel) table.getModel()).fireTableDataChanged();
		if (table.getRowCount() > 0) {
			table.setRowSelectionInterval(0, 0);
		}
	}

	@Override
	public void malnutritionUpdated(Malnutrition maln) {
		pMaln.set(selectedrow, maln);
		((MalnBrowsingModel) table.getModel()).fireTableDataChanged();
		table.updateUI();
		if ((table.getRowCount() > 0) && selectedrow > -1) {
			table.setRowSelectionInterval(selectedrow, selectedrow);
		}
	}

	private Malnutrition malnutrition;

	private JPanel jContentPane;

	private JPanel buttonPanel;

	private JButton newButton;

	private JButton closeButton;

	private JButton deleteButton;

	private JButton editButton;


	private String admId;

	private JTable table;

	private String[] pColumns = {
			MessageBundle.getMessage("angal.malnutrition.datesupp.col").toUpperCase(),
			MessageBundle.getMessage("angal.malnutrition.dateconf.col").toUpperCase(),
			MessageBundle.getMessage("angal.common.height.txt").toUpperCase(),
			MessageBundle.getMessage("angal.common.weight.txt").toUpperCase()
	};

	private int[] pColumnWidth = { 200, 200, 150, 150 };

	private DefaultTableModel model;

	private List<Malnutrition> pMaln;

	private int selectedrow;

	private Admission adm;

	private MalnutritionManager manager = Context.getApplicationContext().getBean(MalnutritionManager.class);

	public MalnutritionBrowser(JFrame owner, Admission aAdm) {
		super(owner, true);
		adm = aAdm;
		admId = String.valueOf(adm.getId());
		setTitle(MessageBundle.getMessage("angal.malnutrition.malnutritionbrowser.title"));
		add(getJContentPane());
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}

	private JPanel getJContentPane() {
		jContentPane = new JPanel();
		jContentPane.setLayout(new BoxLayout(jContentPane, BoxLayout.Y_AXIS));
		jContentPane.add(new JScrollPane(getTable()));
		jContentPane.add(getButtonPanel());
		validate();
		return jContentPane;
	}

	private JPanel getButtonPanel() {
		buttonPanel = new JPanel();
		buttonPanel.add(getNewButton());
		buttonPanel.add(getEditButton());
		buttonPanel.add(getDeleteButton());
		buttonPanel.add(getCloseButton());
		return buttonPanel;
	}

	private JButton getNewButton() {
		newButton = new JButton(MessageBundle.getMessage("angal.common.new.btn"));
		newButton.setMnemonic(MessageBundle.getMnemonic("angal.common.new.btn.key"));
		newButton.addActionListener(actionEvent -> {
			malnutrition = new Malnutrition(0, null, null, adm, 0, 0);
			InsertMalnutrition newRecord = new InsertMalnutrition(MalnutritionBrowser.this, malnutrition, true);
			newRecord.addMalnutritionListener(MalnutritionBrowser.this);
			newRecord.setVisible(true);
		});
		return newButton;

	}

	private JButton getEditButton() {
		editButton = new JButton(MessageBundle.getMessage("angal.common.edit.btn"));
		editButton.setMnemonic(MessageBundle.getMnemonic("angal.common.edit.btn.key"));
		editButton.addActionListener(actionEvent -> {
			if (table.getSelectedRow() < 0) {
				MessageDialog.error(MalnutritionBrowser.this, "angal.common.pleaseselectarow.msg");
			} else {
				selectedrow = table.getSelectedRow();
				malnutrition = (Malnutrition) (model.getValueAt(selectedrow, -1));
				InsertMalnutrition editRecord = new InsertMalnutrition(MalnutritionBrowser.this, malnutrition, false);
				editRecord.addMalnutritionListener(MalnutritionBrowser.this);
				editRecord.setVisible(true);
			}
		});
		return editButton;
	}

	private JButton getDeleteButton() {
		deleteButton = new JButton(MessageBundle.getMessage("angal.common.delete.btn"));
		deleteButton.setMnemonic(MessageBundle.getMnemonic("angal.common.delete.btn.key"));
		deleteButton.addActionListener(actionEvent -> {
			if (table.getSelectedRow() < 0) {
				MessageDialog.error(MalnutritionBrowser.this, "angal.common.pleaseselectarow.msg");
			} else {
				Malnutrition malnutrition = (Malnutrition) (model.getValueAt(table.getSelectedRow(), -1));
				int answer = MessageDialog.yesNo(null, "angal.malnutrition.delete.msg");
				if (answer == JOptionPane.YES_OPTION) {
					if (malnutrition == null) {
						MessageDialog.error(MalnutritionBrowser.this, "angal.common.pleaseselectarow.msg");
					} else {
						boolean deleted;
						try {
							deleted = manager.deleteMalnutrition(malnutrition);
						} catch (OHServiceException e) {
							deleted = false;
							OHServiceExceptionUtil.showMessages(e);
						}

						if (deleted) {
							pMaln.remove(table.getSelectedRow());
							model.fireTableDataChanged();
							table.updateUI();
						}
					}
				}
			}
		});

		return deleteButton;
	}

	private JButton getCloseButton() {
		closeButton = new JButton(MessageBundle.getMessage("angal.common.close.btn"));
		closeButton.setMnemonic(MessageBundle.getMnemonic("angal.common.close.btn.key"));
		closeButton.addActionListener(actionEvent -> dispose());
		return closeButton;
	}

	private JTable getTable() {
		model = new MalnBrowsingModel(admId);
		table = new JTable(model);
		table.getColumnModel().getColumn(0).setMaxWidth(pColumnWidth[0]);
		table.getColumnModel().getColumn(1).setMaxWidth(pColumnWidth[1]);
		table.getColumnModel().getColumn(2).setMaxWidth(pColumnWidth[2]);
		table.getColumnModel().getColumn(3).setMaxWidth(pColumnWidth[3]);
		return table;
	}

	class MalnBrowsingModel extends DefaultTableModel {
		
		private static final long serialVersionUID = 1L;

		public MalnBrowsingModel(String s) {

			pMaln = null;
			
			if (null != s && !s.isEmpty()) {
				try {
					pMaln = manager.getMalnutrition(s);
				} catch (OHServiceException e) {
					OHServiceExceptionUtil.showMessages(e);
				}				
			} else {
				MessageDialog.error(MalnutritionBrowser.this, "angal.malnutrition.nonameselected");
			}
		}

		@Override
		public int getRowCount() {
			if (pMaln == null) {
				return 0;
			}
			return pMaln.size();
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
			Malnutrition malnutrition = pMaln.get(r);
			if (c == -1) {
				return malnutrition;
			} else if (c == 0) {
				return getConvertedString(malnutrition.getDateSupp());
			} else if (c == 1) {
				return getConvertedString(malnutrition.getDateConf());
			} else if (c == 2) {
				return malnutrition.getHeight();
			} else if (c == 3) {
				return malnutrition.getWeight();
			}
			return null;
		}

		@Override
		public boolean isCellEditable(int arg0, int arg1) {
			return false;
		}
	}

	private String getConvertedString(LocalDateTime time) {
		if (time == null) {
			return MessageBundle.getMessage("angal.malnutrition.nodate.msg");
		}
		return TimeTools.formatDateTime(time, DATE_FORMAT_DD_MM_YYYY);
	}
}
