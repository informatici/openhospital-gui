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
package org.isf.pregtreattype.gui;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import org.isf.generaldata.MessageBundle;
import org.isf.menu.manager.Context;
import org.isf.pregtreattype.gui.PregnantTreatmentTypeEdit.PregnantTreatmentTypeListener;
import org.isf.pregtreattype.manager.PregnantTreatmentTypeBrowserManager;
import org.isf.pregtreattype.model.PregnantTreatmentType;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.ModalJFrame;

/**
 * Browsing of table PregnantTreatmentType
 *
 * @author Furlanetto, Zoia, Finotto
 */
public class PregnantTreatmentTypeBrowser extends ModalJFrame implements PregnantTreatmentTypeListener{

	private static final long serialVersionUID = 1L;
	private ArrayList<PregnantTreatmentType> pPregnantTreatmentType;
	private String[] pColumns = {
			MessageBundle.getMessage("angal.common.code.txt").toUpperCase(),
			MessageBundle.getMessage("angal.common.description.txt").toUpperCase()
	};
	private int[] pColumnWidth = {80, 200};
	private JPanel jContainPanel = null;
	private JPanel jButtonPanel = null;
	private JButton jNewButton = null;
	private JButton jEditButton = null;
	private JButton jCloseButton = null;
	private JButton jDeleteButton = null;
	private JTable jTable = null;
	private PregnantTreatmentTypeBrowserModel model;
	private int selectedrow;
	private PregnantTreatmentTypeBrowserManager manager = Context.getApplicationContext().getBean(PregnantTreatmentTypeBrowserManager.class);
	private PregnantTreatmentType pregnantTreatmentType = null;
	private final JFrame myFrame;
	

	/**
	 * This method initializes 
	 */
	public PregnantTreatmentTypeBrowser() {
		super();
		myFrame=this;
		initialize();
		setVisible(true);
	}
	
	
	private void initialize() {
		Toolkit kit = Toolkit.getDefaultToolkit();
		Dimension screensize = kit.getScreenSize();
		final int pfrmBase = 10;
        final int pfrmWidth = 5;
        final int pfrmHeight =4;
        this.setBounds((screensize.width - screensize.width * pfrmWidth / pfrmBase ) / 2, (screensize.height - screensize.height * pfrmHeight / pfrmBase)/2, 
                screensize.width * pfrmWidth / pfrmBase, screensize.height * pfrmHeight / pfrmBase);
		this.setTitle(MessageBundle.getMessage("angal.preagtreattype.pregnanttreatmenttypebrowser.title"));
		this.setContentPane(getJContainPanel());
		//pack();	
	}
	
	
	private JPanel getJContainPanel() {
		if (jContainPanel == null) {
			jContainPanel = new JPanel();
			jContainPanel.setLayout(new BorderLayout());
			jContainPanel.add(getJButtonPanel(), java.awt.BorderLayout.SOUTH);
			jContainPanel.add(new JScrollPane(getJTable()),
					java.awt.BorderLayout.CENTER);
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
			jNewButton.addActionListener(new ActionListener() {
				
				public void actionPerformed(ActionEvent event) {
					pregnantTreatmentType = new PregnantTreatmentType("","");
					PregnantTreatmentTypeEdit newrecord = new PregnantTreatmentTypeEdit(myFrame,pregnantTreatmentType, true);
					newrecord.addPregnantTreatmentTypeListener(PregnantTreatmentTypeBrowser.this);
					newrecord.setVisible(true);
				}
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
			jEditButton.addActionListener(new ActionListener() {
				
				public void actionPerformed(ActionEvent event) {
					if (jTable.getSelectedRow() < 0) {
						MessageDialog.error(null, "angal.common.pleaseselectarow.msg");
					} else {
						selectedrow = jTable.getSelectedRow();
						pregnantTreatmentType = (PregnantTreatmentType) (model.getValueAt(selectedrow, -1));
						PregnantTreatmentTypeEdit newrecord = new PregnantTreatmentTypeEdit(myFrame,pregnantTreatmentType, false);
						newrecord.addPregnantTreatmentTypeListener(PregnantTreatmentTypeBrowser.this);
						newrecord.setVisible(true);
					}
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
			jCloseButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					dispose();
				}
			});
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
			jDeleteButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					if (jTable.getSelectedRow() < 0) {
						MessageDialog.error(null, "angal.common.pleaseselectarow.msg");
					} else {
						PregnantTreatmentType preTreatmentType = (PregnantTreatmentType) (model.getValueAt(jTable.getSelectedRow(), -1));
						int answer = MessageDialog.yesNo(null, "angal.preagtreattype.deletetreatmenttype.fmt.msg", preTreatmentType.getDescription());
						try {
							if ((answer == JOptionPane.YES_OPTION) && (manager.deletePregnantTreatmentType(preTreatmentType))) {
								pPregnantTreatmentType.remove(jTable.getSelectedRow());
								model.fireTableDataChanged();
								jTable.updateUI();
							}
						} catch(OHServiceException e) {
							OHServiceExceptionUtil.showMessages(e);
						}
					}
				}
			});
		}
		return jDeleteButton;
	}
	
	public JTable getJTable() {
		if (jTable == null) {
			model = new PregnantTreatmentTypeBrowserModel();
			jTable = new JTable(model);
			jTable.getColumnModel().getColumn(0).setMinWidth(pColumnWidth[0]);
			jTable.getColumnModel().getColumn(1).setMinWidth(pColumnWidth[1]);
		}return jTable;
	}
	
	
	
class PregnantTreatmentTypeBrowserModel extends DefaultTableModel {

	private static final long serialVersionUID = 1L;

		public PregnantTreatmentTypeBrowserModel() {
			PregnantTreatmentTypeBrowserManager manager = Context.getApplicationContext().getBean(PregnantTreatmentTypeBrowserManager.class);
			try {
				pPregnantTreatmentType = manager.getPregnantTreatmentType();
			}catch(OHServiceException e){
				OHServiceExceptionUtil.showMessages(e);
			}
		}
		
		public int getRowCount() {
			if (pPregnantTreatmentType == null)
				return 0;
			return pPregnantTreatmentType.size();
		}
		
		public String getColumnName(int c) {
			return pColumns[c];
		}

		public int getColumnCount() {
			return pColumns.length;
		}

		public Object getValueAt(int r, int c) {
			if (c == 0) {
				return pPregnantTreatmentType.get(r).getCode();
			} else if (c == -1) {
				return pPregnantTreatmentType.get(r);
			} else if (c == 1) {
				return pPregnantTreatmentType.get(r).getDescription();
			}
			return null;
		}
		
		@Override
		public boolean isCellEditable(int arg0, int arg1) {
			//return super.isCellEditable(arg0, arg1);
			return false;
		}
	}




public void pregnantTreatmentTypeUpdated(AWTEvent e) {
	pPregnantTreatmentType.set(selectedrow, pregnantTreatmentType);
	((PregnantTreatmentTypeBrowserModel) jTable.getModel()).fireTableDataChanged();
	jTable.updateUI();
	if ((jTable.getRowCount() > 0) && selectedrow > -1)
		jTable.setRowSelectionInterval(selectedrow, selectedrow);
}


public void pregnantTreatmentTypeInserted(AWTEvent e) {
	pPregnantTreatmentType.add(0, pregnantTreatmentType);
	((PregnantTreatmentTypeBrowserModel) jTable.getModel()).fireTableDataChanged();
	if (jTable.getRowCount() > 0)
		jTable.setRowSelectionInterval(0, 0);
}
	
	
}
