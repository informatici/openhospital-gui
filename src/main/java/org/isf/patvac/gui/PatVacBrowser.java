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
package org.isf.patvac.gui;

import static org.isf.utils.Constants.DATE_FORMATTER;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SpringLayout;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

import org.isf.generaldata.GeneralData;
import org.isf.generaldata.MessageBundle;
import org.isf.menu.gui.MainMenu;
import org.isf.menu.manager.Context;
import org.isf.patient.model.Patient;
import org.isf.patvac.manager.PatVacManager;
import org.isf.patvac.model.PatientVaccine;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.GoodDateChooser;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.ModalJFrame;
import org.isf.utils.jobjects.VoLimitedTextField;
import org.isf.utils.layout.SpringUtilities;
import org.isf.vaccine.manager.VaccineBrowserManager;
import org.isf.vaccine.model.Vaccine;
import org.isf.vactype.manager.VaccineTypeBrowserManager;
import org.isf.vactype.model.VaccineType;

import com.github.lgooddatepicker.zinternaltools.WrapLayout;

/**
 * ------------------------------------------
 * PatVacBrowser - list all patient's vaccines
 * -----------------------------------------
 * modification history
 * 25/08/2011 - claudia - first beta version
 * 25/10/2011 - claudia - modify selection section
 * 14/11/2011 - claudia - eliminated @override tag
 *                      - inserted ENHANCEDSEARCH functionality on search
 * ------------------------------------------
 */
public class PatVacBrowser extends ModalJFrame {

	private static final long serialVersionUID = 1L;

	private JPanel jContentPane = null;
	private JPanel jButtonPanel = null;
	private JButton buttonEdit = null;
	private JButton buttonNew = null;
	private JButton buttonDelete = null;
	private JButton buttonClose = null;
	private JButton filterButton = null;
	private JPanel jSelectionPanel = null;
	private JPanel jAgePanel = null ;
	private VoLimitedTextField jAgeFromTextField = null;
	private VoLimitedTextField jAgeToTextField = null;
	private Integer ageTo = 0;
	private Integer ageFrom = 0;
	private JPanel sexPanel = null;
	private JRadioButton radiom;
	private JRadioButton radiof;
	private JLabel rowCounter = null;
	private String rowCounterText = MessageBundle.getMessage("angal.patvac.count") + ": ";
	
	private JTable jTable = null;
	private JComboBox vaccineComboBox = null;
	private JComboBox vaccineTypeComboBox = null;
	private int pfrmHeight;
	private List<PatientVaccine> lPatVac;

	private String[] pColumns = {
			MessageBundle.getMessage("angal.common.date.txt").toUpperCase(),
			MessageBundle.getMessage("angal.common.patient.txt").toUpperCase(),
			MessageBundle.getMessage("angal.common.sex.txt").toUpperCase(),
			MessageBundle.getMessage("angal.common.age.txt").toUpperCase(),
			MessageBundle.getMessage("angal.patvac.vaccine.col").toUpperCase(),
			MessageBundle.getMessage("angal.patvac.vaccinetype.col").toUpperCase()
	};
	private int[] pColumnWidth = {100, 150, 50, 50, 150, 150};
	private boolean[] columnsVisible = {true, GeneralData.PATIENTVACCINEEXTENDED, true, true, true, true};
	private PatVacManager manager;
	private PatVacBrowsingModel model;
	private PatientVaccine patientVaccine;
	private int selectedrow;
	private GoodDateChooser dateFrom;
	private GoodDateChooser dateTo;
	private JPanel dateFilterPanel;
	private final JFrame myFrame;

	public PatVacBrowser() {
		super();
		myFrame = this;
		manager = Context.getApplicationContext().getBean(PatVacManager.class);
		initialize();
		setVisible(true);
	}

	/**
	 * This method initializes this Frame, sets the correct Dimensions
	 */
	private void initialize() {
		setTitle(MessageBundle.getMessage("angal.patvac.patientvaccinebrowser.title"));
		this.setContentPane(getJContentPane());
		setPreferredSize(new Dimension(1680, 670));
		setMinimumSize(new Dimension(880, 510));
		pack();
		updateRowCounter();
		this.setLocationRelativeTo(null);
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
			jContentPane.add(new JScrollPane(getJTable()), java.awt.BorderLayout.CENTER);
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
			jButtonPanel = new JPanel(new WrapLayout());
			if (MainMenu.checkUserGrants("btnpatientvaccinenew")) {
				jButtonPanel.add(getButtonNew(), null);
			}
			if (MainMenu.checkUserGrants("btnpatientvaccineedit")) {
				jButtonPanel.add(getButtonEdit(), null);
			}
			if (MainMenu.checkUserGrants("btnpatientvaccinedel")) {
				jButtonPanel.add(getButtonDelete(), null);
			}
			jButtonPanel.add((getCloseButton()), null);
		}
		return jButtonPanel;
	}

	/**
	 * This method initializes buttonNew, that loads patientVaccineEdit Mask
	 * 
	 * @return buttonNew (JButton)
	 */
	private JButton getButtonNew() {
		if (buttonNew == null) {
			buttonNew = new JButton(MessageBundle.getMessage("angal.common.new.btn"));
			buttonNew.setMnemonic(MessageBundle.getMnemonic("angal.common.new.btn.key"));
			buttonNew.addActionListener(actionEvent -> {
				LocalDateTime now = LocalDateTime.now();
				patientVaccine = new PatientVaccine(0, 0, now, new Patient(),
						new Vaccine("", "", new VaccineType("", "")), 0);

				PatientVaccine last = new PatientVaccine(0, 0, now, new Patient(),
						new Vaccine("", "", new VaccineType("", "")), 0);
				new PatVacEdit(myFrame, patientVaccine, true);

				if (!last.equals(patientVaccine)) {
					lPatVac.add(0, patientVaccine);
					((PatVacBrowsingModel) jTable.getModel()).fireTableDataChanged();
					updateRowCounter();
					if (jTable.getRowCount() > 0) {
						jTable.setRowSelectionInterval(0, 0);
					}
				}
			});
		}
		return buttonNew;
	}

	/**
	 * This method initializes buttonEdit, that loads patientVaccineEdit Mask
	 * 
	 * @return buttonEdit (JButton)
	 */
	private JButton getButtonEdit() {
		if (buttonEdit == null) {
			buttonEdit = new JButton(MessageBundle.getMessage("angal.common.edit.btn"));
			buttonEdit.setMnemonic(MessageBundle.getMnemonic("angal.common.edit.btn.key"));
			buttonEdit.addActionListener(actionEvent -> {
				if (jTable.getSelectedRow() < 0) {
					MessageDialog.error(null, "angal.common.pleaseselectarow.msg");
					return;
				}

				selectedrow = jTable.getSelectedRow();
				patientVaccine = (PatientVaccine) (model.getValueAt(selectedrow, -1));

				PatientVaccine last = new PatientVaccine(patientVaccine.getCode(),
						patientVaccine.getProgr(),
						patientVaccine.getVaccineDate(),
						patientVaccine.getPatient(),
						patientVaccine.getVaccine(),
						patientVaccine.getLock());

				new PatVacEdit(myFrame, patientVaccine, false);

				if (!last.equals(patientVaccine)) {
					((PatVacBrowsingModel) jTable.getModel()).fireTableDataChanged();
					updateRowCounter();
					if ((jTable.getRowCount() > 0) && selectedrow > -1) {
						jTable.setRowSelectionInterval(selectedrow, selectedrow);
					}
				}
			});
		}
		return buttonEdit;
	}

	/**
	 * This method initializes buttonDelete, that loads patientVaccineEdit Mask
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
					return;
				}
				selectedrow = jTable.getSelectedRow();
				patientVaccine = (PatientVaccine) (model.getValueAt(selectedrow, -1));
				int answer = MessageDialog.yesNo(null, "angal.patvac.deletepatientvaccine.fmt.msg",
						patientVaccine.getVaccineDate().format(DATE_FORMATTER),
						patientVaccine.getVaccine().getDescription(),
						patientVaccine.getPatName());

				if (answer == JOptionPane.YES_OPTION) {

					boolean deleted;
					try {
						deleted = manager.deletePatientVaccine(patientVaccine);
					} catch (OHServiceException e) {
						deleted = false;
						OHServiceExceptionUtil.showMessages(e);
					}

					if (deleted) {
						lPatVac.remove(jTable.getSelectedRow());
						model.fireTableDataChanged();
						jTable.updateUI();
					}
				}
			});
		}
		return buttonDelete;
	}	
	
	/**
	 * This method initializes buttonClose
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

			jSelectionPanel.add(getVaccineTypePanel());
			jSelectionPanel.add(getVaccinePanel());
			
			jSelectionPanel.add(getDateFilterPanel());
			jSelectionPanel.add(getAgePanel());

			jSelectionPanel.add(getSexPanel());
			jSelectionPanel.add(getFilterPanel());
			jSelectionPanel.add(getRowCounterPanel());
		}
		return jSelectionPanel;
	}

	/**
	 * This method initializes getVaccineTypePanel
	 * 
	 * @return vaccineTypePanel  (JPanel)
	 */
	private JPanel getVaccineTypePanel() {

		JPanel vaccineTypePanel = new JPanel();

		vaccineTypePanel.setLayout(new BoxLayout(vaccineTypePanel, BoxLayout.Y_AXIS));
		JPanel label1Panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		label1Panel.add(new JLabel(MessageBundle.getMessage("angal.patvac.selectavaccinetype")));
		vaccineTypePanel.add(label1Panel);

		label1Panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		label1Panel.add(getComboVaccineTypes());
		vaccineTypePanel.add(label1Panel, null);
		return vaccineTypePanel;
	}

	/**
	 * This method initializes getVaccinePanel
	 * 
	 * @return vaccinePanel  (JPanel)
	 */
	private JPanel getVaccinePanel() {

		JPanel vaccinePanel = new JPanel();

		vaccinePanel.setLayout(new BoxLayout(vaccinePanel, BoxLayout.Y_AXIS));
		JPanel label1Panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		label1Panel.add(new JLabel(MessageBundle.getMessage("angal.patvac.selectavaccine")));
		vaccinePanel.add(label1Panel);

		label1Panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		label1Panel.add(getComboVaccines());
		vaccinePanel.add(label1Panel, null);
		return vaccinePanel;
	}

	/**
	 * This method initializes getAgePanel
	 * 
	 * @return jAgePanel  (JPanel)
	 */
	private JPanel getAgePanel() {
		if (jAgePanel == null) {
			jAgePanel = new JPanel();
			jAgePanel.setLayout(new BoxLayout(getAgePanel(), BoxLayout.Y_AXIS));

			JPanel label1Panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
			label1Panel.add(new JLabel(MessageBundle.getMessage("angal.common.agefrom.label")), null);
			jAgePanel.add(label1Panel);
			label1Panel.add(getJAgeFromTextField(), null);
			jAgePanel.add(label1Panel);

			label1Panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
			label1Panel.add(new JLabel(MessageBundle.getMessage("angal.common.ageto.label")), null);
			jAgePanel.add(label1Panel);
			label1Panel.add(getJAgeToTextField(), null);
			jAgePanel.add(label1Panel);
		}
		return jAgePanel;
	}
	
	/**
	 * This method initializes getSexPanel
	 * 
	 * @return sexPanel  (JPanel)
	 */
	public JPanel getSexPanel() {
		if (sexPanel == null) {
			sexPanel = new JPanel();
			sexPanel.setLayout(new BoxLayout(sexPanel, BoxLayout.Y_AXIS));
			JPanel label1Panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
			label1Panel.add(new JLabel(MessageBundle.getMessage("angal.common.selectsex.txt")), null);
			sexPanel.add(label1Panel);
			
			label1Panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
			ButtonGroup group = new ButtonGroup();
			radiom = new JRadioButton(MessageBundle.getMessage("angal.common.male.btn"));
			radiof = new JRadioButton(MessageBundle.getMessage("angal.common.female.btn"));
			JRadioButton radioa = new JRadioButton(MessageBundle.getMessage("angal.common.all.btn"));
			radioa.setSelected(true);
			group.add(radiom);
			group.add(radiof);
			group.add(radioa);
			
			label1Panel.add(radioa);
			sexPanel.add(label1Panel);
			label1Panel.add(radiom);
			sexPanel.add(label1Panel);
			label1Panel.add(radiof);
			sexPanel.add(label1Panel);
		}
		return sexPanel;
	}
	
	/**
	 * This method initializes getFilterPanel 
	 * 
	 * @return filterPanel  (JPanel)
	 */
	private JPanel getFilterPanel() {

		JPanel filterPanel = new JPanel();
		filterPanel.setPreferredSize(new Dimension(225, 30));
		filterPanel.setLayout(new BoxLayout(filterPanel, BoxLayout.Y_AXIS));
		JPanel label1Panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		label1Panel.add(getFilterButton());
		filterPanel.add(label1Panel);
		return filterPanel;
	}
	
	/**
	 * This method initializes getRowCounterPanel 
	 * 
	 * @return rowCounterPanel  (JPanel)
	 */
	private JPanel  getRowCounterPanel() {
		
		JPanel rowCounterPanel = new JPanel();
		
		rowCounterPanel.setLayout(new BoxLayout(rowCounterPanel, BoxLayout.Y_AXIS));
		JPanel label1Panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		rowCounter = new JLabel(MessageBundle.getMessage("angal.patvac.rowcounter"));
		label1Panel.add(rowCounter, null);
		rowCounterPanel.add(label1Panel);
		return rowCounterPanel;
	}

	/**
	 * This method initializes jAgeFromTextField
	 *
	 * @return javax.swing.JTextField
	 */
	private VoLimitedTextField getJAgeFromTextField() {
		if (jAgeFromTextField == null) {
			jAgeFromTextField = new VoLimitedTextField(3, 2);
			jAgeFromTextField.setText("0");
			jAgeFromTextField.setMinimumSize(new Dimension(100, 50));
			ageFrom = 0;
			jAgeFromTextField.addFocusListener(new FocusListener() {

				@Override
				public void focusLost(FocusEvent e) {
					try {
						ageFrom = Integer.parseInt(jAgeFromTextField.getText());
						if ((ageFrom < 0) || (ageFrom > 200)) {
							jAgeFromTextField.setText("0");
							ageFrom = Integer.parseInt(jAgeFromTextField.getText());
							MessageDialog.error(null, "angal.patvac.insertvalidage");
						}
					} catch (NumberFormatException ex) {
						jAgeFromTextField.setText("0");
						ageFrom = Integer.parseInt(jAgeFromTextField.getText());
					}
				}

				@Override
				public void focusGained(FocusEvent e) {
				}
			});
		}
		return jAgeFromTextField;
	}

	/**
	 * This method initializes jTextField
	 *
	 * @return javax.swing.JTextField
	 */
	private VoLimitedTextField getJAgeToTextField() {
		if (jAgeToTextField == null) {
			jAgeToTextField = new VoLimitedTextField(3, 2);
			jAgeToTextField.setText("0");
			jAgeToTextField.setMaximumSize(new Dimension(100, 50));
			ageTo = 0;
			jAgeToTextField.addFocusListener(new FocusListener() {

				@Override
				public void focusLost(FocusEvent e) {
					try {
						ageTo = Integer.parseInt(jAgeToTextField.getText());
						if ((ageTo < 0) || (ageTo > 200)) {
							jAgeToTextField.setText("0");
							ageTo = Integer.parseInt(jAgeToTextField.getText());
							MessageDialog.error(null, "angal.patvac.insertvalidage");
						}
						if (ageFrom > ageTo) {
							MessageDialog.error(null, "angal.patvac.agefrommustbelowerthanageto");
							jAgeFromTextField.setText(ageTo.toString());
							ageFrom = ageTo;
						}
					} catch (NumberFormatException ex) {
						jAgeToTextField.setText("0");
						ageTo = Integer.parseInt(jAgeToTextField.getText());
					}
				}

				@Override
				public void focusGained(FocusEvent e) {
				}
			});
		}
		return jAgeToTextField;
	}
	
	
	/**
	 * This method initializes getComboVaccineTypes	
	 * 	
	 * @return vaccineTypeComboBox (jComboBox)	
	 */
	private JComboBox getComboVaccineTypes() {
		if (vaccineTypeComboBox == null) {
			vaccineTypeComboBox = new JComboBox();
			vaccineTypeComboBox.setPreferredSize(new Dimension(200, 30));
			vaccineTypeComboBox.addItem(new VaccineType("", MessageBundle.getMessage("angal.patvac.allvaccinetype")));

			VaccineTypeBrowserManager manager = Context.getApplicationContext().getBean(VaccineTypeBrowserManager.class);
			List<VaccineType> types = null;
			try {
				types = manager.getVaccineType();
			} catch (OHServiceException e1) {
				OHServiceExceptionUtil.showMessages(e1);
			}
			if (types != null) {
				for (VaccineType elem : types) {
					vaccineTypeComboBox.addItem(elem);
				}
			}

			vaccineTypeComboBox.addActionListener(actionEvent -> {
				vaccineComboBox.removeAllItems();
				getComboVaccines();
			});
		}
		return vaccineTypeComboBox;
	}

	/**
	 * This method initializes comboVaccine.
	 * It used to display available vaccine  
     *
	 * @return vaccineComboBox (JComboBox)
	 */
	private JComboBox getComboVaccines() {
		if (vaccineComboBox == null) {
			vaccineComboBox = new JComboBox();
			vaccineComboBox.setPreferredSize(new Dimension(200, 30));
		}
		VaccineBrowserManager vaccineBrowserManager = Context.getApplicationContext().getBean(VaccineBrowserManager.class);

		List<Vaccine> allVac = null;
		vaccineComboBox.addItem(new Vaccine("", MessageBundle.getMessage("angal.patvac.allvaccine"), new VaccineType("", "")));
		try {
			if (((VaccineType) vaccineTypeComboBox.getSelectedItem()).getDescription().equals(MessageBundle.getMessage("angal.patvac.allvaccinetype"))) {
				allVac = vaccineBrowserManager.getVaccine();
			} else {
				allVac = vaccineBrowserManager.getVaccine(((VaccineType) vaccineTypeComboBox.getSelectedItem()).getCode());
			}
		} catch (OHServiceException e) {
			OHServiceExceptionUtil.showMessages(e);
		}

		if (allVac != null) {
			for (Vaccine elem : allVac) {
				vaccineComboBox.addItem(elem);
			}
		}
		return vaccineComboBox;
	}

	private Component getDateFilterPanel() {
		if (dateFilterPanel == null) {
			dateFilterPanel = new JPanel(new SpringLayout());
			dateFilterPanel.add(new JLabel(MessageBundle.getMessage("angal.common.datefrom.label")));
			if (!GeneralData.ENHANCEDSEARCH) {
				dateFrom = new GoodDateChooser(LocalDate.now().minusWeeks(1));
			} else {
				dateFrom = new GoodDateChooser(LocalDate.now());
			}
			dateFilterPanel.add(dateFrom);
			dateFilterPanel.add(new JLabel(MessageBundle.getMessage("angal.common.dateto.label")));
			dateTo = new GoodDateChooser(LocalDate.now());
			dateFilterPanel.add(dateTo);
			SpringUtilities.makeCompactGrid(dateFilterPanel, 2, 2, 5, 5, 5, 5);
		}
		return dateFilterPanel;
	}

	/**
	 * This method initializes filterButton, which is the button that performs
	 * the filtering and calls the methods to refresh the Table
	 * 
	 * @return filterButton (JButton)
	 */
	private JButton getFilterButton() {
		if (filterButton == null) {
			filterButton = new JButton(MessageBundle.getMessage("angal.common.search.btn"));
			filterButton.setMnemonic(MessageBundle.getMnemonic("angal.common.search.btn.key"));
			filterButton.addActionListener(actionEvent -> {

				String vaccineTypeCode = ((VaccineType) vaccineTypeComboBox.getSelectedItem()).getCode();
				String vaccineCode = ((Vaccine) vaccineComboBox.getSelectedItem()).getCode();

				if (vaccineTypeComboBox.getSelectedItem().toString().equalsIgnoreCase(MessageBundle.getMessage("angal.patvac.allvaccinetype"))) {
					vaccineTypeCode = null;
				}
				if (vaccineComboBox.getSelectedItem().toString().equalsIgnoreCase(MessageBundle.getMessage("angal.patvac.allvaccine"))) {
					vaccineCode = null;
				}
				char sex;
				if (radiof.isSelected()) {
					sex = 'F';
				} else {
					if (radiom.isSelected()) {
						sex = 'M';
					} else {
						sex = 'A';
					}
				}

				if (dateFrom.getDate() == null) {
					MessageDialog.error(null, "angal.patvac.pleaseinsertvaliddatefrom");
					return;
				}

				if (dateTo.getDate() == null) {
					MessageDialog.error(null, "angal.patvac.pleaseinsertvaliddateto");
					return;
				}

				model = new PatVacBrowsingModel(vaccineTypeCode, vaccineCode, dateFrom.getDateStartOfDay(), dateTo.getDateEndOfDay(), sex, ageFrom, ageTo);
				model.fireTableDataChanged();
				jTable.updateUI();
				updateRowCounter();
			});
		}
		return filterButton;
	}

	/**
	 * This method initializes jTable, that contains the information about the
	 * patient's vaccines
	 * 
	 * @return jTable (JTable)
	 */
	private JTable getJTable() {
		if (jTable == null) {
			model = new PatVacBrowsingModel();
			jTable = new JTable(model);
			TableColumnModel columnModel = jTable.getColumnModel();
			if (GeneralData.PATIENTVACCINEEXTENDED) {
				columnModel.getColumn(0).setMinWidth(pColumnWidth[0]);
				columnModel.getColumn(1).setMinWidth(pColumnWidth[1]);
				columnModel.getColumn(2).setMinWidth(pColumnWidth[2]);
				columnModel.getColumn(3).setMinWidth(pColumnWidth[3]);
				columnModel.getColumn(4).setMinWidth(pColumnWidth[4]);
				columnModel.getColumn(5).setMinWidth(pColumnWidth[5]);
			} else {
				columnModel.getColumn(0).setMinWidth(pColumnWidth[0]);
				columnModel.getColumn(1).setMaxWidth(pColumnWidth[2]);
				columnModel.getColumn(2).setMinWidth(pColumnWidth[3]);
				columnModel.getColumn(3).setMinWidth(pColumnWidth[4]);
				columnModel.getColumn(4).setMinWidth(pColumnWidth[5]);
			}
		}
		return jTable;
	}
	
	/**
	 * This class defines the model for the Table
	 */
	class PatVacBrowsingModel extends DefaultTableModel {

		private static final long serialVersionUID = 1L;
		private PatVacManager manager = Context.getApplicationContext().getBean(PatVacManager.class);

		public PatVacBrowsingModel() {
			PatVacManager patVacManager = Context.getApplicationContext().getBean(PatVacManager.class);
			try {
				lPatVac = patVacManager.getPatientVaccine(!GeneralData.ENHANCEDSEARCH);
			} catch (OHServiceException e) {
				lPatVac = null;
				OHServiceExceptionUtil.showMessages(e);
			}
		}

		public PatVacBrowsingModel(String vaccineTypeCode, String vaccineCode, LocalDateTime dateFrom, LocalDateTime dateTo, char sex, int ageFrom, int ageTo) {
			try {
				lPatVac = manager.getPatientVaccine(vaccineTypeCode, vaccineCode, dateFrom, dateTo, sex, ageFrom, ageTo);
			} catch (OHServiceException e) {
				lPatVac = null;
				OHServiceExceptionUtil.showMessages(e);
			}
		}

		@Override
		public int getRowCount() {
			if (lPatVac == null) {
				return 0;
			}
			return lPatVac.size();
		}
		
		@Override
		public String getColumnName(int c) {
			return pColumns[getNumber(c)];
		}

		@Override
		public int getColumnCount() {
			int c = 0;
			for (boolean b : columnsVisible) {
				if (b) {
					c++;
				}
			}
			return c;
		}

		/**
		 * This method converts a column number in the table 
		 * to the right number in the data.
		 */
		protected int getNumber(int col) {
			// right number to return
			int n = col;
			int i = 0;
			do {
				if (!columnsVisible[i]) {
					n++;
				}
				i++;
			} while (i < n);
			// If we are on an invisible column,
			// we have to go one step further
			while (!columnsVisible[n]) {
				n++;
			}
			return n;
		}
	    
		@Override
		public Object getValueAt(int r, int c) {
			PatientVaccine patVac = lPatVac.get(r);
			if (c == -1) {
				return patVac;
			} else if (getNumber(c) == 0) {
				return patVac.getVaccineDate().format(DATE_FORMATTER);
			} else if (getNumber(c) == 1) {
				return patVac.getPatient().getName();
			} else if (getNumber(c) == 2) {
				return patVac.getPatSex();
			} else if (getNumber(c) == 3) {
				return patVac.getPatAge();
			} else if (getNumber(c) == 4) {
				return patVac.getVaccine().getDescription();
			} else if (getNumber(c) == 5) {
				return patVac.getVaccine().getVaccineType().getDescription();
			}
			return null;
		}

		@Override
		public boolean isCellEditable(int arg0, int arg1) {
			return false;
		}
	
	}

	private void updateRowCounter() {
		rowCounter.setText(rowCounterText + lPatVac.size());
	}

}
