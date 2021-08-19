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
package org.isf.exa.gui;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import org.isf.exa.gui.ExamRowEdit.ExamRowListener;
import org.isf.exa.manager.ExamRowBrowsingManager;
import org.isf.exa.model.Exam;
import org.isf.exa.model.ExamRow;
import org.isf.generaldata.MessageBundle;
import org.isf.menu.manager.Context;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.MessageDialog;

/**
 * ------------------------------------------
 * ExamShow - list all possible results for an exam
 * -----------------------------------------
 * modification history
 * 03/11/2006 - ross - changed title
 * 			         - version is now 1.0
 * ------------------------------------------
 */
public class ExamShow extends JDialog implements ExamRowListener {

	private static final long serialVersionUID = 1L;

	private JPanel jContentPane = null;
	private JPanel dataPanel = null;
	private JPanel buttonPanel = null;
	private JButton closeButton = null;
	private Exam exam = null;
	private JButton newButton = null;
	private JButton deleteButton = null;
	private String[] pColumns = {
			MessageBundle.getMessage("angal.common.code.txt").toUpperCase(),
			MessageBundle.getMessage("angal.common.description.txt").toUpperCase()
	};
	private int[] pColumnWidth = {50,250};
	private DefaultTableModel model ;
	private JTable table;
	private ExamRow examRow = null;
	private ArrayList<ExamRow> pExamRow;
	private JDialog myFrame;
	
	public ExamShow(JFrame owner, Exam aExam){
		super(owner,true);
		myFrame = this;
		exam = aExam;
		initialize();
	}
	
	private void initialize(){
		Toolkit kit = Toolkit.getDefaultToolkit();
		Dimension screensize = kit.getScreenSize();
        final int pfrmBase = 10;
        final int pfrmWidth = 3;
        final int pfrmHeight = 3;
        this.setBounds((screensize.width - screensize.width * pfrmWidth / pfrmBase ) / 2, (screensize.height - screensize.height * pfrmHeight / pfrmBase)/2, 
                screensize.width * pfrmWidth / pfrmBase, screensize.height * pfrmHeight / pfrmBase);
		this.setContentPane(getJContentPane());
		this.setTitle(MessageBundle.formatMessage("angal.exa.results.fmt.title", exam.getDescription()));
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.setVisible(true);
	}
	
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jContentPane = new JPanel();
			jContentPane.setLayout(new BorderLayout());
			jContentPane.add(getDataPanel(), java.awt.BorderLayout.NORTH);  
			jContentPane.add(getButtonPanel(), java.awt.BorderLayout.SOUTH);  
		}
		return jContentPane;
	}
	
	private JPanel getDataPanel() {
		if (dataPanel == null) {
			dataPanel= new JPanel();
                        
			model = new ExamRowBrowsingModel(exam.getCode());
			table = new JTable(model);
			table.getColumnModel().getColumn(0).setMinWidth(pColumnWidth[0]);
			table.getColumnModel().getColumn(1).setMinWidth(pColumnWidth[1]);
			jContentPane.add(new JScrollPane(table),BorderLayout.CENTER);
		}
		return dataPanel;
	}
	
	private JPanel getButtonPanel() {
		if (buttonPanel == null) {
			buttonPanel = new JPanel();
			buttonPanel.add(getNewButton(), null);  
			buttonPanel.add(getDeleteButton());  
			buttonPanel.add(getCloseButton());
		}
		return buttonPanel;
	}

	private JButton getNewButton() {
		if (newButton == null) {
			newButton = new JButton(MessageBundle.getMessage("angal.common.new.btn"));
			newButton.setMnemonic(MessageBundle.getMnemonic("angal.common.new.btn.key"));
			newButton.addActionListener(e -> {
				examRow = new ExamRow();
				ExamRowEdit newrecord = new ExamRowEdit(myFrame, examRow, exam);
				newrecord.addExamListener(ExamShow.this);
				newrecord.setVisible(true);
			});
		}
		return newButton;
	}

	private JButton getCloseButton() {
		if (closeButton == null) {
			closeButton = new JButton(MessageBundle.getMessage("angal.common.close.btn"));
			closeButton.setMnemonic(MessageBundle.getMnemonic("angal.common.close.btn.key"));
			closeButton.addActionListener(e -> dispose());
		}
		return closeButton;
	}

	private JButton getDeleteButton() {
		if (deleteButton == null) {
			deleteButton = new JButton(MessageBundle.getMessage("angal.common.delete.btn"));
			deleteButton.setMnemonic(MessageBundle.getMnemonic("angal.common.delete.btn.key"));
			deleteButton.addActionListener(e -> {
				if (table.getSelectedRow() < 0) {
					MessageDialog.error(null, "angal.common.pleaseselectarow.msg");
				} else {
					ExamRowBrowsingManager manager = Context.getApplicationContext().getBean(ExamRowBrowsingManager.class);
					ExamRow row = (ExamRow) (((ExamRowBrowsingModel) model).getValueAt(table.getSelectedRow(), -1));
					int answer = MessageDialog.yesNo(null, "angal.exa.deleteexamresult.fmt.msg", row.getDescription());
					if ((answer == JOptionPane.YES_OPTION)) {
						try {
							boolean deleted = manager.deleteExamRow(row);

							if (deleted) {
								examRowDeleted();
							}
						} catch (OHServiceException e1) {
							OHServiceExceptionUtil.showMessages(e1);
						}
					}
				}
			});
		}
		return deleteButton;
	}

	class ExamRowBrowsingModel extends DefaultTableModel {

		private static final long serialVersionUID = 1L;

		private ExamRowBrowsingManager manager = Context.getApplicationContext().getBean(ExamRowBrowsingManager.class);

		public ExamRowBrowsingModel(String aCode) {

			try {
				pExamRow = manager.getExamRowByExamCode(aCode);
			} catch (OHServiceException e) {
				pExamRow = null;
				OHServiceExceptionUtil.showMessages(e);
			}
		}

		@Override
		public int getRowCount() {
			if (pExamRow == null) {
				return 0;
			}
			return pExamRow.size();
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
			ExamRow examRow = pExamRow.get(r);
			if (c == -1) {
				return examRow;
			} else if (c == 0) {
				return examRow.getCode();
			} else if (c == 1) {
				return examRow.getDescription();
			}
			return null;
		}

		@Override
		public boolean isCellEditable(int arg0, int arg1) {
			//return super.isCellEditable(arg0, arg1);
			return false;
		}
	}

	@Override
	public void examRowInserted(AWTEvent e) {
		pExamRow.add(0, examRow);
		((ExamRowBrowsingModel) table.getModel()).fireTableDataChanged();
		if (table.getRowCount() > 0) {
			table.setRowSelectionInterval(0, 0);
		}
	}

	public void examRowDeleted() {
		pExamRow.remove(table.getSelectedRow());
		model.fireTableDataChanged();
		table.updateUI();
	}
}
