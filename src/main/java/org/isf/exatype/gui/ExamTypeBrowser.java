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
package org.isf.exatype.gui;

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

import org.isf.exatype.gui.ExamTypeEdit.ExamTypeListener;
import org.isf.exatype.manager.ExamTypeBrowserManager;
import org.isf.exatype.model.ExamType;
import org.isf.generaldata.MessageBundle;
import org.isf.menu.manager.Context;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.ModalJFrame;

/**
 * ------------------------------------------
 * ExamTypeBrowser - list all exam types. Let the user select an exam type to edit
 * -----------------------------------------
 * modification history
 * ??/??/2005 - first beta version
 * 03/11/2006 - ross - version is now 1.0
 * ------------------------------------------
 */
public class ExamTypeBrowser extends ModalJFrame implements ExamTypeListener{

	private static final long serialVersionUID = 1L;

	private ArrayList<ExamType> pExamType;
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
	private ExamTypeBrowserModel model;
	private int selectedrow;
	private ExamTypeBrowserManager manager = Context.getApplicationContext().getBean(ExamTypeBrowserManager.class);
	private ExamType examType = null;
	private final JFrame myFrame;

	
	/**
	 * This method initializes 
	 */
	public ExamTypeBrowser() {
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
        final int pfrmHeight = 4;
        this.setBounds((screensize.width - screensize.width * pfrmWidth / pfrmBase ) / 2, (screensize.height - screensize.height * pfrmHeight / pfrmBase)/2, 
                screensize.width * pfrmWidth / pfrmBase, screensize.height * pfrmHeight / pfrmBase);
		this.setTitle( MessageBundle.getMessage("angal.exatype.examtypebrowser.title"));
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
					examType = new ExamType("","");
					ExamTypeEdit newrecord = new ExamTypeEdit(myFrame,examType, true);
					newrecord.addExamTypeListener(ExamTypeBrowser.this);
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
						examType = (ExamType) (model.getValueAt(selectedrow, -1));
						ExamTypeEdit newrecord = new ExamTypeEdit(myFrame,examType, false);
						newrecord.addExamTypeListener(ExamTypeBrowser.this);
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
						ExamType examType = (ExamType) (model.getValueAt(jTable.getSelectedRow(), -1));
						int answer = MessageDialog.yesNo(null,"angal.exatype.deleteexamtype.fmt.msg", examType.getDescription());
						if (answer == JOptionPane.YES_OPTION) {
							
							boolean deleted;
							try {
								deleted = manager.deleteExamType(examType);
							} catch (OHServiceException e) {
								deleted = false;
								OHServiceExceptionUtil.showMessages(e);
							}
							
							if (deleted) {
								pExamType.remove(jTable.getSelectedRow());
								model.fireTableDataChanged();
								jTable.updateUI();
							}
						}
					}
				}
				
			});
		}
		return jDeleteButton;
	}
	
	public JTable getJTable() {
		if (jTable == null) {
			model = new ExamTypeBrowserModel();
			jTable = new JTable(model);
			jTable.getColumnModel().getColumn(0).setMinWidth(pColumnWidth[0]);
			jTable.getColumnModel().getColumn(1).setMinWidth(pColumnWidth[1]);
		}return jTable;
	}
	
class ExamTypeBrowserModel extends DefaultTableModel {
		
	private static final long serialVersionUID = 1L;

		public ExamTypeBrowserModel() {
			ExamTypeBrowserManager manager = Context.getApplicationContext().getBean(ExamTypeBrowserManager.class);
			try {
				pExamType = manager.getExamType();
			} catch (OHServiceException e) {
				pExamType = null;
				OHServiceExceptionUtil.showMessages(e);
			}
		}
		
		public int getRowCount() {
			if (pExamType == null)
				return 0;
			return pExamType.size();
		}
		
		public String getColumnName(int c) {
			return pColumns[c];
		}

		public int getColumnCount() {
			return pColumns.length;
		}

		public Object getValueAt(int r, int c) {
			ExamType examType = pExamType.get(r);
			if (c == 0) {
				return examType.getCode();
			} else if (c == -1) {
				return examType;
			} else if (c == 1) {
				return examType.getDescription();
			}
			return null;
		}
		
		@Override
		public boolean isCellEditable(int arg0, int arg1) {
			//return super.isCellEditable(arg0, arg1);
			return false;
		}
	}


	public void examTypeUpdated(AWTEvent e) {
		pExamType.set(selectedrow, examType);
		((ExamTypeBrowserModel) jTable.getModel()).fireTableDataChanged();
		jTable.updateUI();
		if ((jTable.getRowCount() > 0) && selectedrow > -1)
			jTable.setRowSelectionInterval(selectedrow, selectedrow);
	}
	
	
	public void examTypeInserted(AWTEvent e) {
		pExamType.add(0, examType);
		((ExamTypeBrowserModel) jTable.getModel()).fireTableDataChanged();
		if (jTable.getRowCount() > 0)
			jTable.setRowSelectionInterval(0, 0);
	}

}
