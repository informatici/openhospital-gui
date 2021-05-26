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
package org.isf.vaccine.gui;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import org.isf.generaldata.MessageBundle;
import org.isf.menu.manager.Context;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.ModalJFrame;
import org.isf.vaccine.manager.VaccineBrowserManager;
import org.isf.vaccine.model.Vaccine;
import org.isf.vactype.manager.VaccineTypeBrowserManager;
import org.isf.vactype.model.VaccineType;

/**
 * This class shows a list of vaccines.
 * It is possible to edit-insert-delete records
 *
 * @author Eva
 * 
 * modification history
 * 20/10/2011 - Cla - insert vaccinetype managment
 */
public class VaccineBrowser extends ModalJFrame implements VaccineEdit.VaccineListener {

	private static final long serialVersionUID = 1L;

	public void vaccineInserted(AWTEvent e) {
		pVaccine.add(0,vaccine);
		((VaccineBrowserModel)table.getModel()).fireTableDataChanged();
		if (table.getRowCount() > 0)
			table.setRowSelectionInterval(0, 0);
	}

	public void vaccineUpdated(AWTEvent e) {
		pVaccine.set(selectedrow,vaccine);
		((VaccineBrowserModel)table.getModel()).fireTableDataChanged();
		table.updateUI();
		if ((table.getRowCount() > 0) && selectedrow >-1)
			table.setRowSelectionInterval(selectedrow,selectedrow);

	}

	private JPanel jContentPane = null;
	private JPanel jButtonPanel = null;
	private JButton jEditButton = null;
	private JButton jNewButton = null;
	private JButton jDeleteButton = null;
	private JButton jCloseButton = null;
	private JComboBox  jSelectionCombo = null;
	
	private JScrollPane jScrollPane = null;
	private JTable table = null;
	private DefaultTableModel model = null;
	private String[] pColumns = {
			MessageBundle.getMessage("angal.common.code"),
			MessageBundle.getMessage("angal.vaccine.vaccinetype"),
			MessageBundle.getMessage("angal.common.description")
	};
	private int[] pColumnWidth = {100, 50, 120};
	private int selectedrow;
	private ArrayList<Vaccine> pVaccine;
	private Vaccine vaccine;

	
	/**
	 * This is the default constructor
	 */
	public VaccineBrowser() {
		super();
//		myFrame = this;
		initialize();
        setVisible(true);
	}

	/**
	 * This method initializes this
	 */
	private void initialize() {
		this.setTitle(MessageBundle.getMessage("angal.vaccine.vaccinebrowser"));
		Toolkit kit = Toolkit.getDefaultToolkit();
		Dimension screensize = kit.getScreenSize();
        final int pfrmBase = 6;
        final int pfrmWidth = 4;
        final int pfrmHeight = 2;
        this.setBounds((screensize.width - screensize.width * pfrmWidth / pfrmBase ) / 2, (screensize.height - screensize.height * pfrmHeight / pfrmBase)/2,
                screensize.width * pfrmWidth / pfrmBase, screensize.height * pfrmHeight / pfrmBase);
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
			jContentPane.add(getJButtonPanel(), java.awt.BorderLayout.SOUTH);
			jContentPane.add(getJScrollPane(), java.awt.BorderLayout.CENTER);
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
			jButtonPanel.add(new JLabel(MessageBundle.getMessage("angal.vaccine.selectavaccinetype")), null);
			jButtonPanel.add(getJVaccineTypeComboBox(), null);
			jButtonPanel.add(getJNewButton(), null);
			jButtonPanel.add(getJEditButton(), null);
			jButtonPanel.add(getJDeleteButton(), null);
			jButtonPanel.add(getJCloseButton(), null);
		}
		return jButtonPanel;
	}

	/**
	 * This method initializes getJVaccineTypeComboBox
	 *
	 * @return JComboBox
	 */
	private JComboBox getJVaccineTypeComboBox() {
		if (jSelectionCombo == null) {
			jSelectionCombo = new JComboBox();
			jSelectionCombo.setPreferredSize(new Dimension(200, 30));
			VaccineTypeBrowserManager manager = Context.getApplicationContext().getBean(VaccineTypeBrowserManager.class);
			ArrayList<VaccineType> allVacType = null;
			try {
				allVacType = manager.getVaccineType();
			} catch (OHServiceException e1) {
				OHServiceExceptionUtil.showMessages(e1);
			}
			jSelectionCombo
					.addItem(new Vaccine("", MessageBundle.getMessage("angal.vaccine.all"), new VaccineType("", "")));
			if (allVacType != null) {
				for (VaccineType elem : allVacType) {
					jSelectionCombo.addItem(elem);
				}
			}
			jSelectionCombo.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					String pSelectionVaccineType = jSelectionCombo.getSelectedItem().toString();
					if (pSelectionVaccineType.equals(MessageBundle.getMessage("angal.vaccine.all")))
						model = new VaccineBrowserModel();
					else
						model = new VaccineBrowserModel(((VaccineType) jSelectionCombo.getSelectedItem()).getCode());
					model.fireTableDataChanged();
					table.updateUI();
				}
			});
		}

		return jSelectionCombo;
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
			jEditButton.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent event) {
					if (table.getSelectedRow() < 0) {
						MessageDialog.error(null, "angal.common.pleaseselectarow.msg");
					} else {
						selectedrow = table.getSelectedRow();
						vaccine = (Vaccine)(((VaccineBrowserModel) model).getValueAt(table.getSelectedRow(), -1));
						VaccineEdit editrecord = new VaccineEdit(VaccineBrowser.this, vaccine,false);
						editrecord.addVaccineListener(VaccineBrowser.this);
						editrecord.setVisible(true);
					}
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
			jNewButton.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent event) {
					vaccine=new Vaccine(null,"",new VaccineType("",""));	//operation will reference the new record
					VaccineEdit newrecord = new VaccineEdit(VaccineBrowser.this, vaccine, true);
					newrecord.addVaccineListener(VaccineBrowser.this);
					newrecord.setVisible(true);
				}
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
			jDeleteButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					if (table.getSelectedRow() < 0) {
						MessageDialog.error(null, "angal.common.pleaseselectarow.msg");
					} else {
						VaccineBrowserManager manager = Context.getApplicationContext().getBean(VaccineBrowserManager.class);
						Vaccine m = (Vaccine)(((VaccineBrowserModel) model).getValueAt(table.getSelectedRow(), -1));
						int n = JOptionPane.showConfirmDialog(
								null,
								MessageBundle.getMessage("angal.vaccine.deletevaccine") + " \""+m.getDescription()+"\" ?",
								MessageBundle.getMessage("angal.hospital"),
								JOptionPane.YES_NO_OPTION);
                        try{
                            if ((n == JOptionPane.YES_OPTION) && (manager.deleteVaccine(m))){
                                pVaccine.remove(table.getSelectedRow());
                                model.fireTableDataChanged();
                                table.updateUI();
                            }
                        } catch (OHServiceException e) {
                            OHServiceExceptionUtil.showMessages(e);
                        }
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
			jCloseButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					dispose();
				}
			});
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
			model = new VaccineBrowserModel();
			table = new JTable(model);
			table.getColumnModel().getColumn(0).setMaxWidth(pColumnWidth[0]);
			table.getColumnModel().getColumn(1).setPreferredWidth(pColumnWidth[1]);
			table.getColumnModel().getColumn(2).setPreferredWidth(pColumnWidth[2]);
		}
		return table;
	}

	class VaccineBrowserModel extends DefaultTableModel {

		private static final long serialVersionUID = 1L;

		public VaccineBrowserModel() {
			VaccineBrowserManager manager = Context.getApplicationContext().getBean(VaccineBrowserManager.class);
            try {
                pVaccine  = manager.getVaccine();
            } catch (OHServiceException e) {
                OHServiceExceptionUtil.showMessages(e);
            }
        }
		
		
		public VaccineBrowserModel(String vaccineType) {
			VaccineBrowserManager manager = Context.getApplicationContext().getBean(VaccineBrowserManager.class);
            try {
                pVaccine = manager.getVaccine(vaccineType);
            } catch (OHServiceException e) {
                OHServiceExceptionUtil.showMessages(e);
            }
        }
		
		public int getRowCount() {
			if (pVaccine == null)
				return 0;
			return pVaccine.size();
		}

		public String getColumnName(int c) {
			return pColumns[c];
		}

		public int getColumnCount() {
			return pColumns.length;
		}

		public Object getValueAt(int r, int c) {
			Vaccine vac = pVaccine.get(r);
			if (c == -1) {
				return vac;
			} else if (c == 0) {
				return vac.getCode();
			}else if (c == 1) {
				return vac.getVaccineType();
			} else if (c == 2) {
				return vac.getDescription();
			}
			return null;
		}

		public boolean isCellEditable(int arg0, int arg1) {
			//return super.isCellEditable(arg0, arg1);
			return false;
		}
	}
}
