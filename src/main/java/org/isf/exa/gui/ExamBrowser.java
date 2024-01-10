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
package org.isf.exa.gui;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableModel;

import org.isf.exa.gui.ExamEdit.ExamListener;
import org.isf.exa.manager.ExamBrowsingManager;
import org.isf.exa.model.Exam;
import org.isf.exatype.model.ExamType;
import org.isf.generaldata.MessageBundle;
import org.isf.menu.manager.Context;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.ModalJFrame;

/**
 * ExamBrowser - list all exams. Let the user select an exam to edit
 */
public class ExamBrowser extends ModalJFrame implements ExamListener {

	private static final long serialVersionUID = 1L;
	private static final String STR_ALL = MessageBundle.getMessage("angal.common.all.txt").toUpperCase();

	private int selectedrow;
	private JComboBox<ExamType> examTypeFilter;
	private List<Exam> examList;
	private String[] pColumns = {
			MessageBundle.getMessage("angal.common.code.txt").toUpperCase(),
			MessageBundle.getMessage("angal.common.type.txt").toUpperCase(),
			MessageBundle.getMessage("angal.common.description.txt").toUpperCase(),
			MessageBundle.getMessage("angal.exa.proc.col").toUpperCase(),
			MessageBundle.getMessage("angal.exa.default.col").toUpperCase()
	};
	private int[] pColumnWidth = { 60, 330, 160, 60, 200 };
	private Exam exam;

	private DefaultTableModel model ;
	private JTable table;
	private final JFrame myFrame;
	private JButton jButtonNew;
	private JButton jButtonEdit;
	private JButton jButtonClose;
	private JButton jButtonShow;
	private JPanel jContentPanel;
	private JPanel buttonPanel;
	private ExamBrowsingManager examBrowsingManager = Context.getApplicationContext().getBean(ExamBrowsingManager.class);

	public ExamBrowser() {
		myFrame = this;
		setTitle(MessageBundle.getMessage("angal.exa.exambrowser.title"));
		this.setContentPane(getJContentPanel());
		setMinimumSize(new Dimension(800, 400));
		pack();
		setLocationRelativeTo(null);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setVisible(true);
	}

	private JPanel getJContentPanel() {
		if (jContentPanel == null) {
			jContentPanel = new JPanel();
			jContentPanel.setLayout(new BorderLayout());
			jContentPanel.add(getJButtonPanel(), BorderLayout.SOUTH);
			jContentPanel.add(new JScrollPane(getJTable()), BorderLayout.CENTER);
		}
		return jContentPanel;
	}

	
	private JPanel getJButtonPanel() {
		if (buttonPanel == null) {
			buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
			buttonPanel.add(new JLabel(MessageBundle.getMessage("angal.exa.selecttype")));
			buttonPanel.add(getJComboBoxExamType());
			buttonPanel.add(getJButtonNew());
			buttonPanel.add(getJButtonEdit());
			buttonPanel.add(getJButtonDelete());
			buttonPanel.add(getJButtonShow());
			buttonPanel.add(getJButtonClose());
		}
		return buttonPanel;
	}

	private JComboBox<ExamType> getJComboBoxExamType() {
		if (examTypeFilter == null) {
			examTypeFilter = new JComboBox<>();
			examTypeFilter.addItem(new ExamType("", MessageBundle.getMessage("angal.common.all.txt").toUpperCase()));
			List<ExamType> type;
			try {
				type = examBrowsingManager.getExamType();
			} catch (OHServiceException e1) {
				type = null;
				OHServiceExceptionUtil.showMessages(e1);
			}
			if (null != type) {
				for (ExamType elem : type) {
					examTypeFilter.addItem(elem);
				}
			}
			examTypeFilter.addActionListener(actionEvent -> reloadTable());
		}
		return examTypeFilter;
	}

	private JTable getJTable() {
		if (table == null) {
			model = new ExamBrowsingModel();
			table = new JTable(model);
			table.setAutoCreateColumnsFromModel(false);
			table.getColumnModel().getColumn(0).setMinWidth(pColumnWidth[0]);
			table.getColumnModel().getColumn(1).setMinWidth(pColumnWidth[1]);
			table.getColumnModel().getColumn(2).setMinWidth(pColumnWidth[2]);
			table.getColumnModel().getColumn(3).setMinWidth(pColumnWidth[3]);
			table.getColumnModel().getColumn(4).setMinWidth(pColumnWidth[4]);
			table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			table.getSelectionModel().addListSelectionListener(selectionEvent -> {
				if (!selectionEvent.getValueIsAdjusting()) {
					selectedrow = table.convertRowIndexToModel(table.getSelectedRow());
					exam = (Exam) model.getValueAt(selectedrow, -1);
					jButtonShow.setEnabled(exam.getProcedure() != 3);
				}
			});
		}
		return table;
	}

	private JButton getJButtonDelete() {
		JButton jButtonDelete = new JButton(MessageBundle.getMessage("angal.common.delete.btn"));
		jButtonDelete.setMnemonic(MessageBundle.getMnemonic("angal.common.delete.btn.key"));
		jButtonDelete.addActionListener(actionEvent -> {
			if (table.getSelectedRow() < 0) {
				MessageDialog.error(this, "angal.common.pleaseselectarow.msg");
				return;
			}
			selectedrow = table.convertRowIndexToModel(table.getSelectedRow());
			Exam examToDelete = (Exam) model.getValueAt(selectedrow, -1);
			int answer = MessageDialog.yesNo(null, "angal.exa.deleteexam.fmt.msg", examToDelete.getCode(), examToDelete.getDescription());
			if (answer == JOptionPane.YES_OPTION) {
				boolean deleted = false;
				try {
					examBrowsingManager.deleteExam(examToDelete);
					deleted = true;
				} catch (OHServiceException e1) {
					OHServiceExceptionUtil.showMessages(e1);
				}
				if (deleted) {
					reloadTable();
				}
			}
		});
		return jButtonDelete;
	}

	private JButton getJButtonNew() {
            
		if (jButtonNew == null) {
			jButtonNew = new JButton(MessageBundle.getMessage("angal.common.new.btn"));
			jButtonNew.setMnemonic(MessageBundle.getMnemonic("angal.common.new.btn.key"));
			jButtonNew.addActionListener(actionEvent -> {
				exam = new Exam("", "", new ExamType("", ""), 0, "");
				ExamEdit newrecord = new ExamEdit(myFrame, exam, true);
				newrecord.addExamListener(this);
				newrecord.setVisible(true);
			});
		}
		return jButtonNew;
	}

	private JButton getJButtonEdit() {
		if (jButtonEdit == null) {
			jButtonEdit = new JButton(MessageBundle.getMessage("angal.common.edit.btn"));
			jButtonEdit.setMnemonic(MessageBundle.getMnemonic("angal.common.edit.btn.key"));
			jButtonEdit.addActionListener(actionEvent -> {
				if (table.getSelectedRow() < 0) {
					MessageDialog.error(this, "angal.common.pleaseselectarow.msg");
				} else {
					selectedrow = table.convertRowIndexToModel(table.getSelectedRow());
					exam = (Exam) model.getValueAt(selectedrow, -1);
					ExamEdit editrecord = new ExamEdit(myFrame, exam, false);
					editrecord.addExamListener(this);
					editrecord.setVisible(true);
				}
			});
		}
		return jButtonEdit;
	}
	
	private JButton getJButtonShow() {
		if (jButtonShow == null) {
			jButtonShow = new JButton(MessageBundle.getMessage("angal.exa.results.btn"));
			jButtonShow.setMnemonic(MessageBundle.getMnemonic("angal.exa.results.btn.key"));
			jButtonShow.addActionListener(actionEvent -> {
				if (table.getSelectedRow() < 0) {
					MessageDialog.error(this, "angal.common.pleaseselectarow.msg");
				} else {
					selectedrow = table.convertRowIndexToModel(table.getSelectedRow());
					exam = (Exam) model.getValueAt(selectedrow, -1);
					new ExamShow(myFrame, exam);
				}
			});
		}
		return jButtonShow;
	}
	
	private JButton getJButtonClose() {
		if (jButtonClose == null) {
			jButtonClose = new JButton(MessageBundle.getMessage("angal.common.close.btn"));
			jButtonClose.setMnemonic(MessageBundle.getMnemonic("angal.common.close.btn.key"));
			jButtonClose.addActionListener(actionEvent -> dispose());
		}
		return jButtonClose;
	}

	class ExamBrowsingModel extends DefaultTableModel {
		
		private static final long serialVersionUID = 1L;
		
		public ExamBrowsingModel(String s) {
			try {
				examList = examBrowsingManager.getExamsByTypeDescription(s);
                                
			} catch (OHServiceException e) {
				examList = null;
				OHServiceExceptionUtil.showMessages(e);
			}
		}
		public ExamBrowsingModel() {
			try {
				examList = examBrowsingManager.getExams();
			} catch (OHServiceException e) {
				examList = null;
				OHServiceExceptionUtil.showMessages(e);
			}
		}
		@Override
		public int getRowCount() {
			if (examList == null) {
				return 0;
			}
			return examList.size();
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
			Exam exam = examList.get(r);
			if (c == -1) {
				return exam;
			} else if (c == 0) {
				return exam.getCode();
			} else if (c == 1) {
				return exam.getExamtype().getDescription();
			} else if (c == 2) {
				return exam.getDescription();
			} else if (c == 3) {
				return exam.getProcedure();
			} else if (c == 4) {
				return exam.getDefaultResult();
			}
			return null;
		}
		
		@Override
		public boolean isCellEditable(int arg0, int arg1) {
			return false;
		}
	}
	
	@Override
	public void examUpdated(AWTEvent e) {
		reloadTable();
		if (table.getRowCount() > 0 && selectedrow > -1) {
			table.setRowSelectionInterval(selectedrow, selectedrow);
		}
	}

	@Override
	public void examInserted(AWTEvent e) {
		reloadTable();
		if (table.getRowCount() > 0) {
			table.setRowSelectionInterval(0, 0);
		}
	}

	private void reloadTable() {
		String pSelection = examTypeFilter.getSelectedItem().toString();
		if (pSelection.compareTo(STR_ALL) == 0) {
			model = new ExamBrowsingModel();
		} else {
			model = new ExamBrowsingModel(pSelection);
		}
		model.fireTableDataChanged();
		table.updateUI();
	}

}
