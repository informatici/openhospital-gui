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
package org.isf.disease.gui;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Locale;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import org.isf.disease.manager.DiseaseBrowserManager;
import org.isf.disease.model.Disease;
import org.isf.distype.manager.DiseaseTypeBrowserManager;
import org.isf.distype.model.DiseaseType;
import org.isf.generaldata.MessageBundle;
import org.isf.menu.manager.Context;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.ModalJFrame;

/**
 * ------------------------------------------
 * DiseaseBrowser - This class shows a list of diseases.
 * 					It is possible to filter data with a selection combo box
 * 					and edit-insert-delete records
 * -----------------------------------------
 * modification history
 * 25-gen-2006 - Rick, Vero, Pupo - first beta version
 * 03/11/2006 - ross - version is now 1.0
 * ------------------------------------------
 */
public class DiseaseBrowser extends ModalJFrame implements DiseaseEdit.DiseaseListener {

	private static final long serialVersionUID = 1L;

	public void diseaseInserted(AWTEvent e) {
		pDisease.add(0,disease);
		((DiseaseBrowserModel)table.getModel()).fireTableDataChanged();
		//table.updateUI();
		if (table.getRowCount() > 0)
			table.setRowSelectionInterval(0, 0);
	}
	
	public void diseaseUpdated(AWTEvent e) {
		pDisease.set(selectedrow,disease);
		((DiseaseBrowserModel)table.getModel()).fireTableDataChanged();
		table.updateUI();
		if ((table.getRowCount() > 0) && selectedrow >-1)
			table.setRowSelectionInterval(selectedrow,selectedrow);
		
	}
	
	private int selectedrow;
	private JLabel selectlabel;
	private JComboBox pbox;
	private ArrayList<Disease> pDisease;
	private String[] pColumns = {
			MessageBundle.getMessage("angal.common.code.txt").toUpperCase(),
			MessageBundle.getMessage("angal.common.type.txt").toUpperCase(),
			MessageBundle.getMessage("angal.common.name.txt").toUpperCase()
	};
	private int[] pColumnWidth = {50, 180, 200};
	private Disease disease;
	private DefaultTableModel model ;
	private JTable table;
	private JFrame myFrame;
	private DiseaseType pSelection;
	private DiseaseBrowserManager manager = Context.getApplicationContext().getBean(DiseaseBrowserManager.class);
	private DiseaseTypeBrowserManager disTypeManager = Context.getApplicationContext().getBean(DiseaseTypeBrowserManager.class);
	
	
	public DiseaseBrowser() {
		
		setTitle(MessageBundle.getMessage("angal.disease.diseasesbrowser.title"));
		myFrame = this;
		model = new DiseaseBrowserModel();
		table = new JTable(model);
		table.setDefaultRenderer(Object.class, new ColorTableCellRenderer());
		table.getColumnModel().getColumn(0).setMaxWidth(pColumnWidth[0]);
		table.getColumnModel().getColumn(1).setPreferredWidth(pColumnWidth[1]);
		table.getColumnModel().getColumn(2).setPreferredWidth(pColumnWidth[2]);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		add(new JScrollPane(table), BorderLayout.CENTER);
		
		JPanel buttonPanel = new JPanel();
		
		selectlabel = new JLabel(MessageBundle.getMessage("angal.disease.selecttype"));
		buttonPanel.add(selectlabel);
		
		pbox = new JComboBox();
		pbox.addItem(new DiseaseType("0", MessageBundle.getMessage("angal.common.all.txt").toUpperCase()));
		ArrayList<DiseaseType> type = null;
		try {
			type = disTypeManager.getDiseaseType();
		} catch(OHServiceException ohServiceException) {
			MessageDialog.showExceptions(ohServiceException);
		}
		// for efficiency in the sequent for
		if (type != null){
			for (DiseaseType elem : type) {
				pbox.addItem(elem);
			}
		}
		pbox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				pSelection = (DiseaseType) pbox.getSelectedItem();
				if (pSelection.getDescription().compareTo(MessageBundle.getMessage("angal.common.all.txt").toLowerCase()) == 0)
					model = new DiseaseBrowserModel();
				else
					model = new DiseaseBrowserModel(pSelection.getCode());
				model.fireTableDataChanged();
				table.updateUI();
			}
		});
		buttonPanel.add(pbox);
		
		JButton buttonNew = new JButton(MessageBundle.getMessage("angal.common.new.btn"));
		buttonNew.setMnemonic(MessageBundle.getMnemonic("angal.common.new.btn.key"));
		buttonNew.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent event) {
				disease=new Disease(null,"",new DiseaseType("",""));	//disease will reference the new record
				DiseaseEdit newrecord = new DiseaseEdit(myFrame,disease,true);
				newrecord.addDiseaseListener(DiseaseBrowser.this);
				newrecord.setVisible(true);
			}
		});
		buttonPanel.add(buttonNew);
		
		JButton buttonEdit = new JButton(MessageBundle.getMessage("angal.common.edit.btn"));
		buttonEdit.setMnemonic(MessageBundle.getMnemonic("angal.common.edit.btn.key"));
		buttonEdit.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent event) {
				if (table.getSelectedRow() < 0) {
					MessageDialog.error(DiseaseBrowser.this, "angal.common.pleaseselectarow.msg");
				} else {
					selectedrow = table.getSelectedRow();
					disease = (Disease)(((DiseaseBrowserModel) model).getValueAt(selectedrow, -1));	
					DiseaseEdit editrecord = new DiseaseEdit(myFrame,disease,false);
					editrecord.addDiseaseListener(DiseaseBrowser.this);
					editrecord.setVisible(true);
				}
			}
		});
		buttonPanel.add(buttonEdit);
		
		JButton buttonDelete = new JButton(MessageBundle.getMessage("angal.common.delete.btn"));
		buttonDelete.setMnemonic(MessageBundle.getMnemonic("angal.common.delete.btn.key"));
		buttonDelete.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (table.getSelectedRow() < 0) {
					MessageDialog.error(DiseaseBrowser.this, "angal.common.pleaseselectarow.msg");
				} else {
					selectedrow = table.getSelectedRow();
					disease = (Disease)(((DiseaseBrowserModel) model).getValueAt(selectedrow, -1));
					int answer = MessageDialog.yesNo(DiseaseBrowser.this, "angal.disease.deletedisease.fmt.msg", disease.getDescription());
					try {
						if ((answer == JOptionPane.YES_OPTION) && (manager.deleteDisease(disease))){
							disease.setIpdInInclude(false);
							disease.setIpdOutInclude(false);
							disease.setOpdInclude(false);
							diseaseUpdated(null);
						}
					} catch(OHServiceException ohServiceException) {
						MessageDialog.showExceptions(ohServiceException);
					}
				}
			}
		});
		buttonPanel.add(buttonDelete);
		
		JButton buttonClose = new JButton(MessageBundle.getMessage("angal.common.close.btn"));
		buttonClose.setMnemonic(MessageBundle.getMnemonic("angal.common.close.btn.key"));
		buttonClose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				dispose();
			}
		});
		buttonPanel.add(buttonClose);
		
		add(buttonPanel, BorderLayout.SOUTH);
		pack();
		setVisible(true);
		setLocationRelativeTo(null);
	}
	
	
	class DiseaseBrowserModel extends DefaultTableModel {

		private static final long serialVersionUID = 1L;

		public DiseaseBrowserModel(String s) {
			try {
				pDisease = manager.getDisease(s);
			} catch(OHServiceException ohServiceException) {
				MessageDialog.showExceptions(ohServiceException);
			}
		}
		public DiseaseBrowserModel() {
			try {
				pDisease = manager.getDiseaseAll();
			} catch(OHServiceException ohServiceException) {
				MessageDialog.showExceptions(ohServiceException);
			}
		}
		public int getRowCount() {
			if (pDisease == null)
				return 0;
			return pDisease.size();
		}
		
		public String getColumnName(int c) {
			return pColumns[c];
		}
		
		public int getColumnCount() {
			return pColumns.length;
		}
		
		public Object getValueAt(int r, int c) {
			Disease disease = pDisease.get(r);
			if (c == 0) {
				return disease.getCode();
			} else if (c == -1) {
				return disease;
			} else if (c == 1) {
				return disease.getType().getDescription();
			} else if (c == 2) {
				return disease.getDescription();
			}
			return null;
		}
		
		@Override
		public boolean isCellEditable(int arg0, int arg1) {
			return false;
		}
	}
	
	//////////////////////////////////////////////////////////////////////////////////////
	class ColorTableCellRenderer extends DefaultTableCellRenderer
	{

		private static final long serialVersionUID = 1L;

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, 
	      boolean hasFocus, int row, int column)
	   {  
		   Component cell=super.getTableCellRendererComponent(table,value,isSelected,hasFocus,row,column);
		   cell.setForeground(Color.BLACK);
		   if (!((Disease)table.getValueAt(row,-1)).getIpdInInclude() &&
				   !((Disease)table.getValueAt(row,-1)).getIpdOutInclude() &&
				   !((Disease)table.getValueAt(row,-1)).getOpdInclude()) {
			   cell.setForeground(Color.GRAY);
		   }
	      return cell;
	   }
	}
}
