/*
 * Open Hospital (www.open-hospital.org)
 * Copyright © 2006-2025 Informatici Senza Frontiere (info@informaticisenzafrontiere.org)
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
package org.isf.profession.gui;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Font;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import org.isf.generaldata.MessageBundle;
import org.isf.menu.manager.Context;
import org.isf.profession.gui.ProfessionEdit.ProfessionListener;
import org.isf.profession.manager.ProfessionBrowserManager;
import org.isf.profession.model.Profession;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.ModalJFrame;

/**
 * Browsing of table Profession
 */
public class ProfessionBrowser extends ModalJFrame implements ProfessionListener {

	private static final long serialVersionUID = 1L;
	private List<Profession> pProfession;
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
	private ProfessionBrowserModel model;
	private int selectedrow;
	private ProfessionBrowserManager professionBrowserManager = Context.getApplicationContext().getBean(ProfessionBrowserManager.class);
	private Profession profession;
	private final JFrame myFrame;

	/**
	 * This method initializes
	 */
	public ProfessionBrowser() {
		super();
		myFrame = this;
		initialize();
		setVisible(true);
	}

	private void initialize() {
		this.setTitle(MessageBundle.getMessage("angal.profession.professionbrowser.title"));
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
				profession = new Profession("", "");
				ProfessionEdit newrecord = new ProfessionEdit(myFrame, profession, true);
				newrecord.addProfessionListener(this);
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
					profession = (Profession) model.getValueAt(selectedrow, -1);
					ProfessionEdit newrecord = new ProfessionEdit(myFrame, profession, false);
					newrecord.addProfessionListener(this);
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
					Profession profession = (Profession) model.getValueAt(jTable.getSelectedRow(), -1);
					int answer = MessageDialog.yesNo(null, "angal.profession.deleteprofession.fmt.msg", profession.getDescription());
					try {
						if (answer == JOptionPane.YES_OPTION) {
							professionBrowserManager.deleteProfession(profession);
							pProfession.remove(jTable.getSelectedRow());
							model.fireTableDataChanged();
							jTable.updateUI();
						}
					} catch (OHServiceException ohServiceException) {
						MessageDialog.showExceptions(ohServiceException);
					}
				}
			});
		}
		return jDeleteButton;
	}

	private JTable getJTable() {
		if (jTable == null) {
			model = new ProfessionBrowserModel();
			jTable = new JTable(model);
			jTable.getColumnModel().getColumn(0).setMinWidth(pColumnWidth[0]);
			jTable.getColumnModel().getColumn(1).setMinWidth(pColumnWidth[1]);
			JTableHeader header = jTable.getTableHeader();
			header.setFont(new Font(null, Font.BOLD, 12));
		}
		return jTable;
	}

	class ProfessionBrowserModel extends DefaultTableModel {

		private static final long serialVersionUID = 1L;

		public ProfessionBrowserModel() {
			try {
				pProfession = professionBrowserManager.getProfessions();
			} catch (OHServiceException ohServiceException) {
				MessageDialog.showExceptions(ohServiceException);
			}
		}

		@Override
		public int getRowCount() {
			if (pProfession == null) {
				return 0;
			}
			return pProfession.size();
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
				return pProfession.get(r).getCode();
			} else if (c == -1) {
				return pProfession.get(r);
			} else if (c == 1) {
				return pProfession.get(r).getDescription();
			}
			return null;
		}

		@Override
		public boolean isCellEditable(int arg0, int arg1) {
			return false;
		}
	}

	@Override
	public void professionUpdated(AWTEvent e) {
		pProfession.set(selectedrow, profession);
		((ProfessionBrowserModel) jTable.getModel()).fireTableDataChanged();
		jTable.updateUI();
		if (jTable.getRowCount() > 0 && selectedrow > -1) {
			jTable.setRowSelectionInterval(selectedrow, selectedrow);
		}
	}

	@Override
	public void professionInserted(AWTEvent e) {
		pProfession.add(0, profession);
		((ProfessionBrowserModel) jTable.getModel()).fireTableDataChanged();
		if (jTable.getRowCount() > 0) {
			jTable.setRowSelectionInterval(0, 0);
		}
	}

}
