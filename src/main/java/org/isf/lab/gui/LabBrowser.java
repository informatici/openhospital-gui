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
package org.isf.lab.gui;

import static org.isf.utils.Constants.DATE_TIME_FORMATTER;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SpringLayout;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

import org.isf.exa.manager.ExamBrowsingManager;
import org.isf.exa.model.Exam;
import org.isf.exatype.model.ExamType;
import org.isf.generaldata.GeneralData;
import org.isf.generaldata.MessageBundle;
import org.isf.lab.gui.LabEdit.LabEditListener;
import org.isf.lab.gui.LabEditExtended.LabEditExtendedListener;
import org.isf.lab.gui.LabNew.LabListener;
import org.isf.lab.manager.LabManager;
import org.isf.lab.model.Laboratory;
import org.isf.lab.model.LaboratoryForPrint;
import org.isf.lab.service.LabIoOperations;
import org.isf.menu.gui.MainMenu;
import org.isf.menu.manager.Context;
import org.isf.patient.gui.SelectPatient;
import org.isf.patient.model.Patient;
import org.isf.serviceprinting.manager.PrintLabels;
import org.isf.serviceprinting.manager.PrintManager;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.GoodDateChooser;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.ModalJFrame;
import org.isf.utils.layout.SpringUtilities;

/**
 * ------------------------------------------
 * LabBrowser - list all labs
 * -----------------------------------------
 * modification history
 * 02/03/2006 - theo, Davide - first beta version
 * 08/11/2006 - ross - changed button Show into Results
 *                     fixed the exam deletion
 * 					   version is now 1.0
 * 04/01/2009 - ross - do not use roll, use add(week,-1)!
 *                     roll does not change the year!
 * ------------------------------------------
 */
public class LabBrowser extends ModalJFrame implements LabListener, LabEditListener, LabEditExtendedListener {

	private static final long serialVersionUID = 1L;

	@Override
	public void labInserted() {
		jTable.setModel(new LabBrowsingModel());
	}

	@Override
	public void labUpdated() {
		filterButton.doClick();
	}

	private JPanel jContentPane = null;
	private JPanel jButtonPanel = null;
	private JButton buttonEdit = null;
	private JButton buttonNew = null;
	private JButton buttonDelete = null;
	private JButton buttonClose = null;
	private JButton printTableButton = null;
	private JButton filterButton = null;
	private JPanel jSelectionPanel = null;
	private JTable jTable = null;
	private JComboBox comboExams = null;
	private int pfrmHeight;
	private List<Laboratory> pLabs;
	private String[] pColumns = {
			MessageBundle.getMessage("angal.common.date.txt").toUpperCase(),
			MessageBundle.getMessage("angal.common.patient.txt").toUpperCase(),
			MessageBundle.getMessage("angal.common.exam.txt").toUpperCase(),
			MessageBundle.getMessage("angal.common.result.txt").toUpperCase()
	};
	private boolean[] columnsResizable = {false, true, true, false};
	private int[] pColumnWidth = {150, 200, 200, 200};
	private int[] maxWidth = {150, 200, 200, 200};
	private boolean[] columnsVisible = { true, GeneralData.LABEXTENDED, true, true};
	private LabManager labManager = Context.getApplicationContext().getBean(LabManager.class);
	private PrintManager printManager = Context.getApplicationContext().getBean(PrintManager.class);
	private LabBrowsingModel model;
	private Laboratory laboratory;
	private int selectedrow;
	private String typeSelected = null;
	private JPanel dateFilterPanel;
	private GoodDateChooser dateFrom;
	private GoodDateChooser dateTo;
	private final JFrame myFrame;
	private JButton printLabelButton;

	/**
	 * This is the default constructor
	 */
	public LabBrowser() {
		super();
		myFrame = this;
		this.setTitle(MessageBundle.getMessage("angal.lab.laboratorybrowser.title"));
		this.setContentPane(getJContentPane());
		setSize(new Dimension(1345, 650));
		setResizable(false);
		setVisible(true);
		setLocationRelativeTo(null);
	}

	/**
	 * This method initializes jContentPane, adds the main parts of the frame
	 *
	 * @return jContentPanel (JPanel)
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jContentPane = new JPanel();
			jContentPane.setLayout(new BorderLayout());
			jContentPane.add(getJButtonPanel(), java.awt.BorderLayout.SOUTH);
			jContentPane.add(getJSelectionPanel(), java.awt.BorderLayout.WEST);
			jContentPane.add(new JScrollPane(getJTable()),
					java.awt.BorderLayout.CENTER);
			validate();
		}
		return jContentPane;
	}

	/**
	 * This method initializes JButtonPanel, that contains the buttons of the
	 * frame (on the bottom)
	 *
	 * @return JButtonPanel (JPanel)
	 */
	private JPanel getJButtonPanel() {
		if (jButtonPanel == null) {
			jButtonPanel = new JPanel();
			if (MainMenu.checkUserGrants("btnlaboratorynew")) {
				jButtonPanel.add(getButtonNew(), null);
			}
			if (MainMenu.checkUserGrants("btnlaboratoryedit")) {
				jButtonPanel.add(getButtonEdit(), null);
			}
			if (MainMenu.checkUserGrants("btnlaboratorydel")) {
				jButtonPanel.add(getButtonDelete(), null);
			}
			jButtonPanel.add((getPrintTableButton()), null);
			jButtonPanel.add((getPrintLabelButton()), null);
			jButtonPanel.add((getCloseButton()), null);

		}
		return jButtonPanel;
	}

	private JButton getPrintTableButton() {
		if (printTableButton == null) {
			printTableButton = new JButton(MessageBundle.getMessage("angal.lab.printtable.btn"));
			printTableButton.setMnemonic(MessageBundle.getMnemonic("angal.lab.printtable.btn.key"));
			printTableButton.addActionListener(actionEvent -> {
				typeSelected = ((Exam) comboExams.getSelectedItem()).toString();
				if (typeSelected.equalsIgnoreCase(MessageBundle.getMessage("angal.common.all.txt"))) {
					typeSelected = null;
				}

				try {
					List<LaboratoryForPrint> labs;
					labs = labManager.getLaboratoryForPrint(typeSelected, dateFrom.getDateStartOfDay(), dateTo.getDateEndOfDay());
					if (!labs.isEmpty()) {
						printManager.print(MessageBundle.getMessage("angal.common.laboratory.txt"), labs, 0);
					}
				} catch (OHServiceException e) {
					OHServiceExceptionUtil.showMessages(e);
				}
			});
		}
		return printTableButton;
	}

	private JButton getPrintLabelButton() {
		if (printLabelButton == null) {
			printLabelButton = new JButton(MessageBundle.getMessage("angal.labnew.printlabel.btn"));
			printLabelButton.setMnemonic(MessageBundle.getMnemonic("angal.labnew.printlabel.btn.key"));
			printLabelButton.addActionListener(actionEvent -> {
				Integer patId = null;
				if (GeneralData.LABEXTENDED) {
					selectedrow = jTable.getSelectedRow();
					if (selectedrow < 0) {
						int ok = MessageDialog.yesNoCancel(LabBrowser.this, "angal.lab.nopatientselectedprintempylabel.msg");
						if (ok == JOptionPane.NO_OPTION) {
							SelectPatient selectPatient = new SelectPatient(LabBrowser.this, null);
							selectPatient.setVisible(true);
							Patient patient = selectPatient.getPatient();
							if (patient != null) {
								patId = selectPatient.getPatient().getCode();
							} else {
								return;
							}
						}
						if (ok == JOptionPane.CANCEL_OPTION) {
							return;
						}
					} else {
						laboratory = (Laboratory) (model.getValueAt(selectedrow, -1));
						patId = laboratory.getPatient().getCode();
					}
				}
				try {
					new PrintLabels("LabelForSamples", patId);
				} catch (OHServiceException e) {
					OHServiceExceptionUtil.showMessages(e);
				}

			});
		}
		return printLabelButton;
	}

	private JButton getButtonEdit() {
		if (buttonEdit == null) {
			buttonEdit = new JButton(MessageBundle.getMessage("angal.common.edit.btn"));
			buttonEdit.setMnemonic(MessageBundle.getMnemonic("angal.common.edit.btn.key"));
			buttonEdit.addActionListener(actionEvent -> {
				selectedrow = jTable.getSelectedRow();
				if (selectedrow < 0) {
					MessageDialog.error(null, "angal.common.pleaseselectarow.msg");
					return;
				}
				laboratory = (Laboratory) (model.getValueAt(selectedrow, -1));
				if (GeneralData.LABEXTENDED) {
					LabEditExtended editrecord = new LabEditExtended(myFrame, laboratory, false);
					editrecord.addLabEditExtendedListener(LabBrowser.this);
					editrecord.showAsModal(LabBrowser.this);
				} else {
					LabEdit editrecord = new LabEdit(myFrame, laboratory, false);
					editrecord.addLabEditListener(LabBrowser.this);
					editrecord.showAsModal(LabBrowser.this);
				}
			});
		}
		return buttonEdit;
	}

	/**
	 * This method initializes buttonNew, that loads LabEdit Mask
	 *
	 * @return buttonNew (JButton)
	 */
	private JButton getButtonNew() {
		if (buttonNew == null) {
			buttonNew = new JButton(MessageBundle.getMessage("angal.common.new.btn"));
			buttonNew.setMnemonic(MessageBundle.getMnemonic("angal.common.new.btn.key"));
			buttonNew.addActionListener(actionEvent -> {
				laboratory = new Laboratory(0, new Exam("", "",
						new ExamType("", ""), 0, ""),
						LocalDateTime.now(), "P", "", new Patient(), "");
				if (GeneralData.LABEXTENDED) {
					if (GeneralData.LABMULTIPLEINSERT) {
						LabNew editrecord = new LabNew(myFrame);
						editrecord.addLabListener(LabBrowser.this);
						editrecord.setVisible(true);
					} else {
						LabEditExtended editrecord = new LabEditExtended(myFrame, laboratory, true);
						editrecord.addLabEditExtendedListener(LabBrowser.this);
						editrecord.setVisible(true);
					}
				} else {
					LabEdit editrecord = new LabEdit(myFrame, laboratory, true);
					editrecord.addLabEditListener(LabBrowser.this);
					editrecord.setVisible(true);
				}
			});
		}
		return buttonNew;
	}

	/**
	 * This method initializes buttonDelete, that deletes the selected records
	 *
	 * @return buttonDelete (JButton)
	 */
	private JButton getButtonDelete() {
		if (buttonDelete == null) {
			buttonDelete = new JButton(MessageBundle.getMessage("angal.common.delete.btn"));
			buttonDelete.setMnemonic(MessageBundle.getMnemonic("angal.common.delete.btn.key"));
			buttonDelete.addActionListener(actionEvent -> {
				if (jTable.getSelectedRow() < 0) {
					MessageDialog.error(null, "angal.common.pleaseselectarow.msg");
				} else {
					Laboratory lab = (Laboratory) (model.getValueAt(jTable.getSelectedRow(), -1));
					int answer = MessageDialog.yesNo(LabBrowser.this, "angal.lab.deletelabexam.fmt.msg",
							lab.getCreatedDate().format(DATE_TIME_FORMATTER),
							lab.getDate().format(DATE_TIME_FORMATTER),
							lab.getExam(),
							lab.getPatName(),
							lab.getResult());

					if (answer == JOptionPane.YES_OPTION) {
						boolean deleted;

						try {
							deleted = labManager.deleteLaboratory(lab);
						} catch (OHServiceException e) {
							deleted = false;
							OHServiceExceptionUtil.showMessages(e);
						}

						if (deleted) {
							pLabs.remove(jTable.getSelectedRow());
							model.fireTableDataChanged();
							jTable.updateUI();
						}
					}
				}
			});
		}
		return buttonDelete;
	}

	/**
	 * This method initializes buttonClose, that disposes the entire Frame
	 *
	 * @return buttonClose (JButton)
	 */
	private JButton getCloseButton() {
		if (buttonClose == null) {
			buttonClose = new JButton(MessageBundle.getMessage("angal.common.close.btn"));
			buttonClose.setMnemonic(MessageBundle.getMnemonic("angal.common.close.btn.key"));
			buttonClose.addActionListener(actionEvent -> dispose());
		}
		return buttonClose;
	}

	/**
	 * This method initializes JSelectionPanel, that contains the filter objects
	 *
	 * @return JSelectionPanel (JPanel)
	 */
	private JPanel getJSelectionPanel() {
		if (jSelectionPanel == null) {
			jSelectionPanel = new JPanel();
			jSelectionPanel.setPreferredSize(new Dimension(225, pfrmHeight));
			jSelectionPanel.add(new JLabel(MessageBundle.getMessage("angal.lab.selectanexam")));
			jSelectionPanel.add(getComboExams());
			jSelectionPanel.add(getDateFilterPanel());
			jSelectionPanel.add(getFilterButton());
		}
		return jSelectionPanel;
	}

	/**
	 * This method initializes jTable, that contains the information about the
	 * Laboratory Tests
	 *
	 * @return jTable (JTable)
	 */
	private JTable getJTable() {
		if (jTable == null) {
			model = new LabBrowsingModel();
			jTable = new JTable(model);
			TableColumnModel columnModel = jTable.getColumnModel();
			for (int i = 0; i < model.getColumnCount(); i++) {
				jTable.getColumnModel().getColumn(i).setMinWidth(pColumnWidth[i]);
				if (!columnsResizable[i]) {
					columnModel.getColumn(i).setMaxWidth(maxWidth[i]);
				}
				if (!columnsVisible[i]) {
					columnModel.getColumn(i).setMaxWidth(0);
					columnModel.getColumn(i).setMinWidth(0);
					columnModel.getColumn(i).setPreferredWidth(0);
				}
			}
		}
		return jTable;
	}

	/**
	 * This method initializes comboExams, that allows to choose which Exam the
	 * user want to display on the Table
	 *
	 * @return comboExams (JComboBox)
	 */
	private JComboBox getComboExams() {
		ExamBrowsingManager managerExams = Context.getApplicationContext().getBean(ExamBrowsingManager.class);
		if (comboExams == null) {
			comboExams = new JComboBox();
			comboExams.setPreferredSize(new Dimension(225, 30));
			comboExams.addItem(new Exam("", MessageBundle.getMessage("angal.common.all.txt"), new ExamType("", ""), 0, ""));
			List<Exam> type;
			try {
				type = managerExams.getExams();
			} catch (OHServiceException e1) {
				type = null;
				OHServiceExceptionUtil.showMessages(e1);
			} // for efficiency in the sequent for
			if (null != type) {
				for (Exam elem : type) {
					comboExams.addItem(elem);
				}
			}
			comboExams.addActionListener(actionEvent -> {
				typeSelected = ((Exam) comboExams.getSelectedItem()).toString();
				if (typeSelected.equalsIgnoreCase(MessageBundle.getMessage("angal.common.all.txt"))) {
					typeSelected = null;
				}

			});
		}
		return comboExams;
	}

	private Component getDateFilterPanel() {
		if (dateFilterPanel == null) {
			dateFilterPanel = new JPanel(new SpringLayout());
			dateFilterPanel.add(new JLabel(MessageBundle.getMessage("angal.common.datefrom.label")));
			dateFrom = new GoodDateChooser(LocalDate.now().minusWeeks(1));
			dateFilterPanel.add(dateFrom);
			dateFilterPanel.add(new JLabel(MessageBundle.getMessage("angal.common.dateto.label")));
			dateTo = new GoodDateChooser(LocalDate.now());
			dateFilterPanel.add(dateTo);
			SpringUtilities.makeCompactGrid(dateFilterPanel, 2, 2, 5, 5, 5, 5);
		}
		return dateFilterPanel;
	}

	/**
	 * This method initializes filterButton, which is the button that perform
	 * the filtering and calls the methods to refresh the Table
	 *
	 * @return filterButton (JButton)
	 */
	private JButton getFilterButton() {
		if (filterButton == null) {
			filterButton = new JButton(MessageBundle.getMessage("angal.common.search.btn"));
			filterButton.setMnemonic(MessageBundle.getMnemonic("angal.common.search.btn.key"));
			filterButton.addActionListener(actionEvent -> {
				typeSelected = ((Exam) comboExams.getSelectedItem()).toString();
				if (typeSelected.equalsIgnoreCase(MessageBundle.getMessage("angal.common.all.txt"))) {
					typeSelected = null;
				}
				model = new LabBrowsingModel(typeSelected, dateFrom.getDate(), dateTo.getDate());
				model.fireTableDataChanged();
				jTable.updateUI();
			});
		}
		return filterButton;
	}

	/**
	 * This class defines the model for the Table
	 *
	 * @author theo
	 *
	 */
	class LabBrowsingModel extends DefaultTableModel {

		private static final long serialVersionUID = 1L;
		private LabManager manager = Context.getApplicationContext().getBean(LabManager.class, Context.getApplicationContext().getBean(LabIoOperations.class));

		public LabBrowsingModel(String exam, LocalDate dateFrom, LocalDate dateTo) {
			try {
				pLabs = manager.getLaboratory(exam, dateFrom.atStartOfDay(), dateTo.atStartOfDay());
			} catch (OHServiceException e) {
				pLabs = new ArrayList<>();
				OHServiceExceptionUtil.showMessages(e);
			}
		}

		public LabBrowsingModel() {
			try {
				pLabs = manager.getLaboratory();
			} catch (OHServiceException e) {
				pLabs = new ArrayList<>();
				OHServiceExceptionUtil.showMessages(e);
			}
		}

		@Override
		public int getRowCount() {
			if (pLabs == null) {
				return 0;
			}
			return pLabs.size();
		}

		@Override
		public String getColumnName(int c) {
			return pColumns[c];
		}

		@Override
		public int getColumnCount() {
			return pColumns.length;
		}

		/**
		 * Note: We must get the objects in a reversed way because of the query
		 *
		 * @see org.isf.lab.service.LabIoOperations
		 */
		@Override
		public Object getValueAt(int r, int c) {
			Laboratory lab = pLabs.get(r);
			if (c == -1) {
				return lab;
			} else if (c == 0) {
				return lab.getDate().format(DATE_TIME_FORMATTER);
			} else if (c == 1) {
				return lab.getPatName();
			} else if (c == 2) {
				return lab.getExam();
			} else if (c == 3) {
				return lab.getResult();
			}
			return null;
		}

		@Override
		public boolean isCellEditable(int arg0, int arg1) {
			return false;
		}
	}

	/**
	 * This method updates the Table because a laboratory test has been updated
	 * Sets the focus on the same record as before
	 */
	public void laboratoryUpdated() {
		pLabs.set(pLabs.size() - selectedrow - 1, laboratory);
		((LabBrowsingModel) jTable.getModel()).fireTableDataChanged();
		jTable.updateUI();
		if ((jTable.getRowCount() > 0) && selectedrow > -1) {
			jTable.setRowSelectionInterval(selectedrow, selectedrow);
		}
	}

	/**
	 * This method updates the Table because a laboratory test has been inserted
	 * Sets the focus on the first record
	 */
	public void laboratoryInserted() {
		pLabs.add(pLabs.size(), laboratory);
		((LabBrowsingModel) jTable.getModel()).fireTableDataChanged();
		if (jTable.getRowCount() > 0) {
			jTable.setRowSelectionInterval(0, 0);
		}
	}

}
