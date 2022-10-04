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
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ------------------------------------------
 * ExamBrowser - list all exams. Let the user select an exam to edit
 * -----------------------------------------
 * modification history
 * 11/12/2005 - bob  - first beta version
 * 03/11/2006 - ross - changed button Show into Results
 * 			         - version is now 1.0
 * 10/11/2006 - ross - corrected exam deletion, before it was never deleted
 * ------------------------------------------
 */
public class ExamBrowser extends ModalJFrame implements ExamListener {

	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = LoggerFactory.getLogger(ExamBrowser.class);
	private static final String STR_ALL = MessageBundle.getMessage("angal.common.all.txt").toUpperCase();

	private int selectedrow;
	private JComboBox pbox;
	private List<Exam> pExam;
	private String[] pColumns = {
			MessageBundle.getMessage("angal.common.code.txt").toUpperCase(),
			MessageBundle.getMessage("angal.common.type.txt").toUpperCase(),
			MessageBundle.getMessage("angal.common.description.txt").toUpperCase(),
			MessageBundle.getMessage("angal.exa.proc.col").toUpperCase(),
			MessageBundle.getMessage("angal.exa.default.col").toUpperCase()
	};
	private int[] pColumnWidth = { 60, 330, 160, 60, 130 };
	private Exam exam;

	private DefaultTableModel model ;
	private JTable table;
	private final JFrame myFrame;
	private String pSelection;
	private JButton jButtonNew;
	private JButton jButtonEdit;
	private JButton jButtonClose;
	private JButton jButtonShow;
	private JButton jButtonDelete;
	private JPanel jContentPanel;
	private JPanel buttonPanel;
	private JTextField searchTextField;
	private ExamBrowsingManager examBrowsingManager = Context.getApplicationContext().getBean(ExamBrowsingManager.class);

	public ExamBrowser() {
		myFrame = this;
		setTitle(MessageBundle.getMessage("angal.exa.exambrowser.title"));
		Toolkit kit = Toolkit.getDefaultToolkit();
		Dimension screensize = kit.getScreenSize();
		final int pfrmBase = 20;
		final int pfrmWidth = 15;
		final int pfrmHeight = 8;
		this.setBounds((screensize.width - screensize.width * pfrmWidth / pfrmBase) / 2, (screensize.height - screensize.height * pfrmHeight / pfrmBase) / 2,
				screensize.width * pfrmWidth / pfrmBase, screensize.height * pfrmHeight / pfrmBase);
		this.setContentPane(getJContentPanel());
		setVisible(true);
	}

	private JPanel getJContentPanel() {
		if (jContentPanel == null) {
			jContentPanel = new JPanel();
			jContentPanel.setLayout(new BorderLayout());
			jContentPanel.add(getJButtonPanel(), BorderLayout.SOUTH);
			jContentPanel.add(new JScrollPane(getJTable()), BorderLayout.CENTER);
			
			JPanel panelSearch = new JPanel();
			jContentPanel.add(panelSearch, BorderLayout.NORTH);
			
			JLabel searchLabel = new JLabel(MessageBundle.getMessage("angal.exams.find"));
			panelSearch.add(searchLabel);
			
			searchTextField = new JTextField();
			searchTextField.setColumns(20);
			searchTextField.getDocument().addDocumentListener(new DocumentListener() {
				@Override
				public void insertUpdate(DocumentEvent e) {
					filterExam();
				}

				@Override
				public void removeUpdate(DocumentEvent e) {
					filterExam();
				}

				@Override
				public void changedUpdate(DocumentEvent e) {
					filterExam();
				}
			});
			panelSearch.add(searchTextField);
			validate();
		}
		return jContentPanel;
	}

	
	private JPanel getJButtonPanel() {
		if (buttonPanel == null) {
			buttonPanel = new JPanel();
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

	private JComboBox getJComboBoxExamType() {
		if (pbox == null) {
			pbox = new JComboBox();
			pbox.addItem(MessageBundle.getMessage("angal.common.all.txt").toUpperCase());
			List<ExamType> type;
			try {
				type = examBrowsingManager.getExamType();	//for efficiency in the sequent for
			} catch (OHServiceException e1) {
				type = null;
				OHServiceExceptionUtil.showMessages(e1);
			}
			if (null != type) {
				for (ExamType elem : type) {
					pbox.addItem(elem);
				}
			}
			pbox.addActionListener(actionEvent -> reloadTable());
		}
		return pbox;
	}

	private TableRowSorter<TableModel> sorter;

	private JTable getJTable() {
		if (table == null) {
			model = new ExamBrowsingModel();
			table = new JTable(model);
			table.setAutoCreateColumnsFromModel(false);
			sorter = new TableRowSorter<>(model);
			table.setRowSorter(sorter);
			table.getColumnModel().getColumn(0).setMinWidth(pColumnWidth[0]);
			table.getColumnModel().getColumn(1).setMinWidth(pColumnWidth[1]);
			table.getColumnModel().getColumn(2).setMinWidth(pColumnWidth[2]);
			table.getColumnModel().getColumn(3).setMinWidth(pColumnWidth[3]);
			table.getColumnModel().getColumn(4).setMinWidth(pColumnWidth[4]);
			table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			table.getSelectionModel().addListSelectionListener(selectionEvent -> {
				if (!selectionEvent.getValueIsAdjusting()) {
					selectedrow = table.convertRowIndexToModel(table.getSelectedRow());
					exam = (Exam) (((ExamBrowsingModel) model).getValueAt(selectedrow, -1));
					jButtonShow.setEnabled(exam.getProcedure() != 3);
				}
			});
		}
		return table;
	}

	private JButton getJButtonDelete() {
		jButtonDelete = new JButton(MessageBundle.getMessage("angal.common.delete.btn"));
		jButtonDelete.setMnemonic(MessageBundle.getMnemonic("angal.common.delete.btn.key"));
		jButtonDelete.addActionListener(actionEvent -> {
			if (table.getSelectedRow() < 0) {
				MessageDialog.error(ExamBrowser.this, "angal.common.pleaseselectarow.msg");
				return;
			}
			selectedrow = table.convertRowIndexToModel(table.getSelectedRow());
			Exam examToDelete = (Exam) (((ExamBrowsingModel) model).getValueAt(selectedrow, -1));
			int answer = MessageDialog.yesNo(null, "angal.exa.deleteexam.fmt.msg", examToDelete.getCode(), examToDelete.getDescription());
			if ((answer == JOptionPane.YES_OPTION)) {
				boolean deleted;

				try {
					deleted = examBrowsingManager.deleteExam(examToDelete);
				} catch (OHServiceException e1) {
					deleted = false;
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
				newrecord.addExamListener(ExamBrowser.this);
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
					MessageDialog.error(ExamBrowser.this, "angal.common.pleaseselectarow.msg");
				} else {
					selectedrow = table.convertRowIndexToModel(table.getSelectedRow());
					exam = (Exam) (((ExamBrowsingModel) model).getValueAt(selectedrow, -1));
					ExamEdit editrecord = new ExamEdit(myFrame, exam, false);
					editrecord.addExamListener(ExamBrowser.this);
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
					MessageDialog.error(ExamBrowser.this, "angal.common.pleaseselectarow.msg");
				} else {
					selectedrow = table.convertRowIndexToModel(table.getSelectedRow());
					exam = (Exam)(((ExamBrowsingModel) model).getValueAt(selectedrow, -1));
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
				pExam = examBrowsingManager.getExamsByTypeDescription(s);
                                
			} catch (OHServiceException e) {
				pExam = null;
				OHServiceExceptionUtil.showMessages(e);
			}
		}
		public ExamBrowsingModel() {
			try {
				pExam = examBrowsingManager.getExams();
			} catch (OHServiceException e) {
				pExam = null;
				OHServiceExceptionUtil.showMessages(e);
			}
		}
		@Override
		public int getRowCount() {
			if (pExam == null) {
				return 0;
			}
			return pExam.size();
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
			Exam exam = pExam.get(r);
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
		if ((table.getRowCount() > 0) && selectedrow > -1) {
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

	private void filterExam() {
		String s = searchTextField.getText().trim();
		List<RowFilter<Object, Object>> filters = new ExamFilterFactory().buildFilters(s);
		if (!filters.isEmpty()) {
			sorter.setRowFilter(RowFilter.andFilter(filters));
		}
	}

	private void reloadTable() {
		pSelection = pbox.getSelectedItem().toString();
		if (pSelection.compareTo(STR_ALL) == 0) {
			model = new ExamBrowsingModel();
		} else {
			model = new ExamBrowsingModel(pSelection);
		}
		model.fireTableDataChanged();
		table.updateUI();
	}

}
