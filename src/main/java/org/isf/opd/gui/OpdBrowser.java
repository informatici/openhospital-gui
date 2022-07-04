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
package org.isf.opd.gui;

import static org.isf.utils.Constants.DATE_FORMATTER;
import static org.isf.utils.Constants.DATE_TIME_FORMATTER;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;

import org.isf.disease.manager.DiseaseBrowserManager;
import org.isf.disease.model.Disease;
import org.isf.distype.manager.DiseaseTypeBrowserManager;
import org.isf.distype.model.DiseaseType;
import org.isf.generaldata.GeneralData;
import org.isf.generaldata.MessageBundle;
import org.isf.menu.gui.MainMenu;
import org.isf.menu.manager.Context;
import org.isf.opd.manager.OpdBrowserManager;
import org.isf.opd.model.Opd;
import org.isf.patient.model.Patient;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.jobjects.GoodDateChooser;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.ModalJFrame;
import org.isf.utils.jobjects.VoLimitedTextField;
import org.isf.utils.layout.SpringUtilities;
import org.isf.utils.time.TimeTools;

/**
 * ------------------------------------------
 * OpdBrowser - list all OPD. Let the user select an opd to edit or delete
 * -----------------------------------------
 * modification history
 * 11/12/2005 - Vero, Rick  - first beta version
 * 07/11/2006 - ross - renamed from Surgery
 *                   - changed confirm delete message
 * 			         - version is now 1.0
 *    12/2007 - isf bari - multilanguage version
 * 			         - version is now 1.2
 * 21/06/2008 - ross - fixed getFilterButton method, need compare to translated string "female" to get correct filter
 *                   - displayed visitdate in the grid instead of opdDate (=system date)
 *                   - fixed "todate" bug (in case of 31/12: 31/12/2008 became 1/1/2008)
 * 			         - version is now 1.2.1
 * 09/01/2009 - fabrizio - Column full name appears only in OPD extended. Better formatting of OPD date.
 *                         Age column justified to the right. Cosmetic changed to code style.
 * 13/02/2009 - alex - fixed variable visibility in filtering mechanism
 * 06/02/2020 - alex - added search field for diseases
 *                   - version is now 1.2.2
 * ------------------------------------------
 */
public class OpdBrowser extends ModalJFrame implements OpdEdit.SurgeryListener, OpdEditExtended.SurgeryListener {

	private static final long serialVersionUID = 2372745781159245861L;

	private JPanel jButtonPanel = null;
	private JPanel jContainPanel = null;
	private JButton jNewButton = null;
	private JButton jEditButton = null;
	private JButton jCloseButton = null;
	private JButton jDeleteButton = null;
	private JPanel jSelectionPanel = null;
	private JPanel dateFilterPanel = null;
	private JPanel jSelectionDiseasePanel = null;
	private JPanel jAgeFromPanel = null;
	private VoLimitedTextField jAgeFromTextField = null;
	private JPanel jAgeToPanel = null;
	private VoLimitedTextField jAgeToTextField = null;
	private JPanel jAgePanel = null;
	private JComboBox<DiseaseType> jDiseaseTypeBox;
	private JComboBox jDiseaseBox;
	private JPanel sexPanel = null;
	private JPanel newPatientPanel = null;
	private Integer ageTo = 0;
	private Integer ageFrom = 0;
	private DiseaseType allType= new DiseaseType(
			MessageBundle.getMessage("angal.common.alltypes.txt"),
			MessageBundle.getMessage("angal.common.alltypes.txt"));
	private String[] pColumns = {
			MessageBundle.getMessage("angal.common.code.txt").toUpperCase(),
			MessageBundle.getMessage("angal.opd.opdnumber.col").toUpperCase(),
			MessageBundle.getMessage("angal.common.date.txt").toUpperCase(),
			MessageBundle.getMessage("angal.opd.patientid.col").toUpperCase(),
			MessageBundle.getMessage("angal.opd.fullname.col").toUpperCase(),
			MessageBundle.getMessage("angal.common.sex.txt").toUpperCase(),
			MessageBundle.getMessage("angal.common.age.txt").toUpperCase(),
			MessageBundle.getMessage("angal.opd.disease.col").toUpperCase(),
			MessageBundle.getMessage("angal.opd.diseasetype.col").toUpperCase(),
			MessageBundle.getMessage("angal.opd.patientstatus.col").toUpperCase()
	};
	private List<Opd> pSur;
	private JTable jTable = null;
	private OpdBrowsingModel model;
	private int[] pColumnWidth = {50, 100, 130, 70, 150, 30, 30, 195, 195, 50 };
	private boolean[] columnResizable = { false, false, false, false, true, false, false, true, true, false };
	private boolean[] columnsVisible = { true, true, true, GeneralData.OPDEXTENDED, GeneralData.OPDEXTENDED, true, true, true, true, true };
	private int[] columnsAlignment = { SwingConstants.LEFT, SwingConstants.LEFT, SwingConstants.LEFT, SwingConstants.CENTER, SwingConstants.LEFT, SwingConstants.CENTER, SwingConstants.CENTER, SwingConstants.LEFT, SwingConstants.LEFT, SwingConstants.LEFT };
	private boolean[] columnsBold = { true, true, false, true, false, false, false, false, false, false };
	private int selectedrow;
	private OpdBrowserManager manager = Context.getApplicationContext().getBean(OpdBrowserManager.class);
	private JButton filterButton = null;
	private String rowCounterText = MessageBundle.getMessage("angal.common.count.label") + ' ';
	private JLabel rowCounter = null;
	private JRadioButton radioNew;
	private JRadioButton radioAll;
	private final JFrame myFrame;
	private JRadioButton radiom;
	private JRadioButton radioa;
	private DiseaseBrowserManager diseaseManager = Context.getApplicationContext().getBean(DiseaseBrowserManager.class);
	private List<Disease> diseases = null;
	protected AbstractButton searchButton;
	private GoodDateChooser dateFrom;
	private GoodDateChooser dateTo;

	private JTable getJTable() {
		if (jTable == null) {
			model = new OpdBrowsingModel();
			jTable = new JTable(model);
			jTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			TableColumnModel columnModel = jTable.getColumnModel();
			DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer();
			cellRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
			for (int i = 0; i < model.getColumnCount(); i++) {
				columnModel.getColumn(i).setMinWidth(pColumnWidth[i]);
				columnModel.getColumn(i).setCellRenderer(new AlignmentCellRenderer());
				if (!columnResizable[i]) {
					columnModel.getColumn(i).setMaxWidth(pColumnWidth[i]);
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

	class AlignmentCellRenderer extends DefaultTableCellRenderer {

		private static final long serialVersionUID = 1L;

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

			Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			setHorizontalAlignment(columnsAlignment[column]);
			if (columnsBold[column]) {
				cell.setFont(new Font(null, Font.BOLD, 12));
			}
			return cell;
		}
	}

	/**
	 * This method initializes
	 */
	public OpdBrowser() {
		super();
		myFrame = this;
		initialize();
		setMinimumSize(new Dimension(1340, 550));
		setLocationRelativeTo(null);
	}

	public OpdBrowser(Patient patient) {
		super();
		myFrame = this;
		initialize();
		Opd newOpd = new Opd(0, ' ', -1, new Disease());
		OpdEditExtended editrecord = new OpdEditExtended(myFrame, newOpd, patient, true);
		editrecord.addSurgeryListener(OpdBrowser.this);
		editrecord.showAsModal(myFrame);
		setLocationRelativeTo(null);
	}

	/**
	 * This method initializes jButtonPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJButtonPanel() {
		if (jButtonPanel == null) {
			jButtonPanel = new JPanel();
			if (MainMenu.checkUserGrants("btnopdnew")) {
				jButtonPanel.add(getJNewButton(), null);
			}
			if (MainMenu.checkUserGrants("btnopdedit")) {
				jButtonPanel.add(getJEditButton(), null);
			}
			if (MainMenu.checkUserGrants("btnopddel")) {
				jButtonPanel.add(getJDeleteButton(), null);
			}
			jButtonPanel.add(getJCloseButton(), null);
		}
		return jButtonPanel;
	}

	/**
	 * This method initializes this
	 */
	private void initialize() {
		this.setTitle(MessageBundle.getMessage("angal.opd.opdoutpatientdepartment.title"));
		this.setContentPane(getJContainPanel());
		rowCounter.setText(rowCounterText + pSur.size());
		validate();
		pack();
	}

	/**
	 * This method initializes containPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContainPanel() {
		if (jContainPanel == null) {
			jContainPanel = new JPanel();
			jContainPanel.setLayout(new BorderLayout());
			jContainPanel.add(getJButtonPanel(), java.awt.BorderLayout.SOUTH);
			jContainPanel.add(getJSelectionPanel(), java.awt.BorderLayout.WEST);
			jContainPanel.add(new JScrollPane(getJTable()),	java.awt.BorderLayout.CENTER);
			validate();
		}
		return jContainPanel;
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
			jNewButton.addActionListener(actionEvent -> {
				Opd newOpd = new Opd(0, ' ', -1, new Disease());
				if (GeneralData.OPDEXTENDED) {
					OpdEditExtended newrecord = new OpdEditExtended(myFrame, newOpd, true);
					newrecord.addSurgeryListener(OpdBrowser.this);
					newrecord.showAsModal(myFrame);
				} else {
					OpdEdit newrecord = new OpdEdit(myFrame, newOpd, true);
					newrecord.addSurgeryListener(OpdBrowser.this);
					newrecord.setLocationRelativeTo(myFrame);
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
			jEditButton.addActionListener(actionEvent -> {
				if (jTable.getSelectedRow() < 0) {
					MessageDialog.error(OpdBrowser.this, "angal.common.pleaseselectarow.msg");
					return;
				}
				selectedrow = jTable.getSelectedRow();
				Opd opd = (Opd) (model.getValueAt(selectedrow, -1));
				if (GeneralData.OPDEXTENDED) {
					OpdEditExtended editrecord = new OpdEditExtended(myFrame, opd, false);
					editrecord.addSurgeryListener(OpdBrowser.this);
					editrecord.showAsModal(myFrame);
				} else {
					OpdEdit editrecord = new OpdEdit(myFrame, opd, false);
					editrecord.addSurgeryListener(OpdBrowser.this);
					editrecord.setLocationRelativeTo(myFrame);
					editrecord.setVisible(true);
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
					MessageDialog.error(OpdBrowser.this, "angal.common.pleaseselectarow.msg");
					return;
				}
				Opd opd = (Opd) (model.getValueAt(jTable.getSelectedRow(), -1));

				String message;
				if (GeneralData.OPDEXTENDED) {
					message = MessageBundle.formatMessage("angal.opd.deletefollowingopdextended.fmt.msg",
							opd.getPatient().getName(),
							opd.getCreatedDate() == null
									? opd.getDate().format(DATE_FORMATTER)
									: opd.getCreatedDate().format(DATE_FORMATTER),
							opd.getDisease().getDescription() == null
									? '[' + MessageBundle.getMessage("angal.opd.notspecified.msg") + ']'
									: opd.getDisease().getDescription(),
							opd.getAge(),
							opd.getSex(),
							opd.getDate().format(DATE_TIME_FORMATTER));
				} else {
					message = MessageBundle.formatMessage("angal.opd.deletefollowingopd.fmt.msg",
							opd.getCreatedDate() == null
									? opd.getDate().format(DATE_FORMATTER)
									: opd.getCreatedDate().format(DATE_FORMATTER),
							opd.getDisease().getDescription() == null
									? '[' + MessageBundle.getMessage("angal.opd.notspecified.msg") + ']'
									: opd.getDisease().getDescription(),
							opd.getAge(),
							opd.getSex(),
							opd.getDate().format(DATE_FORMATTER));
				}

				int n = JOptionPane.showConfirmDialog(null, message,
						MessageBundle.getMessage("angal.messagedialog.question.title"), JOptionPane.YES_NO_OPTION);
				try {
					if ((n == JOptionPane.YES_OPTION) && (manager.deleteOpd(opd))) {
						pSur.remove(pSur.size() - jTable.getSelectedRow() - 1);
						model.fireTableDataChanged();
						jTable.updateUI();
					}
				} catch (OHServiceException ohServiceException) {
					MessageDialog.showExceptions(ohServiceException);
				}
			});
		}
		return jDeleteButton;
	}

	/**
	 * This method initializes jSelectionPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJSelectionPanel() {
		if (jSelectionPanel == null) {
			JPanel sexLabelPanel = new JPanel();
			sexLabelPanel.add(new JLabel(MessageBundle.getMessage("angal.common.selectsex.txt")));
			sexLabelPanel.setPreferredSize(new Dimension(300, 25));
			JPanel newPatientLabelPanel = new JPanel();
			newPatientLabelPanel.add(new JLabel(MessageBundle.getMessage("angal.common.patient.txt")));
			newPatientLabelPanel.setPreferredSize(new Dimension(300, 25));
			JPanel diseaseLabelPanel = new JPanel();
			diseaseLabelPanel.add(new JLabel(MessageBundle.getMessage("angal.opd.selectadisease.txt")));
			JPanel filterButtonPanel = new JPanel();
			filterButtonPanel.add(getFilterButton());
			filterButtonPanel.setPreferredSize(new Dimension(300, 30));
			jSelectionPanel = new JPanel();
			jSelectionPanel.setPreferredSize(new Dimension(300, 25));
			jSelectionPanel.add(diseaseLabelPanel);
			jSelectionPanel.add(getJSelectionDiseasePanel());
			jSelectionPanel.add(Box.createVerticalGlue());
			jSelectionPanel.add(getDateFilterPanel());
			jSelectionPanel.add(Box.createVerticalGlue());
			jSelectionPanel.add(getJAgePanel());
			jSelectionPanel.add(Box.createVerticalGlue());
			jSelectionPanel.add(sexLabelPanel);
			jSelectionPanel.add(getSexPanel());
			jSelectionPanel.add(newPatientLabelPanel);
			jSelectionPanel.add(getNewPatientPanel());
			jSelectionPanel.add(filterButtonPanel);
			jSelectionPanel.add(getRowCounter());
		}
		return jSelectionPanel;
	}

	private JLabel getRowCounter() {
		if (rowCounter == null) {
			rowCounter = new JLabel();
			rowCounter.setAlignmentX(Component.CENTER_ALIGNMENT);
		}
		return rowCounter;
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


	public class DocumentLimit extends DefaultStyledDocument {

		private static final long serialVersionUID = -5098766139884585921L;

		private final int maximumNumberOfCharacters;

		public DocumentLimit(int numeroMassimoCaratteri) {
			maximumNumberOfCharacters = numeroMassimoCaratteri;
		}

		@Override
		public void insertString(int off, String text, AttributeSet att) throws BadLocationException {
			int numberOfCharactersInDocument = getLength();
			int newTextLength = text.length();
			if (numberOfCharactersInDocument + newTextLength > maximumNumberOfCharacters) {
				int numeroCaratteriInseribili = maximumNumberOfCharacters - numberOfCharactersInDocument;
				if (numeroCaratteriInseribili > 0) {
					String parteNuovoTesto = text.substring(0, numeroCaratteriInseribili);
					super.insertString(off, parteNuovoTesto, att);
				}
			} else {
				super.insertString(off, text, att);
			}
		}
	}

	/**
	 * This method initializes jComboBox
	 *
	 * @return javax.swing.JComboBox
	 */
	public JComboBox<DiseaseType> getDiseaseTypeBox() {
		if (jDiseaseTypeBox == null) {
			jDiseaseTypeBox = new JComboBox<>();
			jDiseaseTypeBox.setMaximumSize(new Dimension(300, 50));

			DiseaseTypeBrowserManager diseaseTypeManager = Context.getApplicationContext().getBean(DiseaseTypeBrowserManager.class);
			List<DiseaseType> types = null;
			try {
				types = diseaseTypeManager.getDiseaseType();
			} catch (OHServiceException ohServiceException) {
				MessageDialog.showExceptions(ohServiceException);
			}

			jDiseaseTypeBox.addItem(allType);
			if (types != null) {
				for (DiseaseType elem : types) {
					jDiseaseTypeBox.addItem(elem);
				}
			}

			jDiseaseTypeBox.addActionListener(actionEvent -> {
				jDiseaseBox.removeAllItems();
				getDiseaseBox();
			});
		}
		return jDiseaseTypeBox;
	}

	/**
	 * This method initializes jComboBox1
	 *
	 * @return javax.swing.JComboBox
	 */
	public JComboBox getDiseaseBox() {
		if (jDiseaseBox == null) {
			jDiseaseBox = new JComboBox();
			jDiseaseBox.setMaximumSize(new Dimension(300, 50));

		}
		try {
			if (((DiseaseType) jDiseaseTypeBox.getSelectedItem()).getDescription().equals(MessageBundle.getMessage("angal.common.alltypes.txt"))) {
				diseases = diseaseManager.getDiseaseOpd();
			} else {
				diseases = diseaseManager.getDiseaseOpd(((DiseaseType) jDiseaseTypeBox.getSelectedItem()).getCode());
			}
		} catch (OHServiceException ohServiceException) {
			MessageDialog.showExceptions(ohServiceException);
		}
		Disease allDisease = new Disease(MessageBundle.getMessage("angal.opd.alldiseases.txt"), MessageBundle.getMessage("angal.opd.alldiseases.txt"), allType);
		jDiseaseBox.addItem(allDisease);
		if (diseases != null) {
			for (Disease elem : diseases) {
				jDiseaseBox.addItem(elem);
			}
		}
		return jDiseaseBox;
	}

	/**
	 * This method initializes sexPanel
	 *
	 * @return javax.swing.JPanel
	 */
	public JPanel getSexPanel() {
		if (sexPanel == null) {
			sexPanel = new JPanel();
			ButtonGroup group = new ButtonGroup();
			radiom = new JRadioButton(MessageBundle.getMessage("angal.common.male.btn"));
			JRadioButton radiof = new JRadioButton(MessageBundle.getMessage("angal.common.female.btn"));
			radioa = new JRadioButton(MessageBundle.getMessage("angal.common.all.btn"));
			radioa.setSelected(true);
			group.add(radiom);
			group.add(radiof);
			group.add(radioa);
			sexPanel.add(radioa);
			sexPanel.add(radiom);
			sexPanel.add(radiof);
		}
		return sexPanel;
	}

	public JPanel getNewPatientPanel() {
		if (newPatientPanel == null) {
			newPatientPanel = new JPanel();
			ButtonGroup groupNewPatient = new ButtonGroup();
			radioNew= new JRadioButton(MessageBundle.getMessage("angal.opd.new.btn"));
			JRadioButton radioRea= new JRadioButton(MessageBundle.getMessage("angal.opd.reattendance.btn"));
			radioAll= new JRadioButton(MessageBundle.getMessage("angal.common.all.btn"));
			radioAll.setSelected(true);
			groupNewPatient.add(radioAll);
			groupNewPatient.add(radioNew);
			groupNewPatient.add(radioRea);
			newPatientPanel.add(radioAll);
			newPatientPanel.add(radioNew);
			newPatientPanel.add(radioRea);
		}
		return newPatientPanel;
	}

	/**
	 * This method initializes jSelectionDiseasePanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJSelectionDiseasePanel() {
		if (jSelectionDiseasePanel == null) {
			JLabel jLabel2 = new JLabel("    ");
			jSelectionDiseasePanel = new JPanel();
			jSelectionDiseasePanel.setLayout(new BoxLayout(jSelectionDiseasePanel, BoxLayout.Y_AXIS));
			jSelectionDiseasePanel.add(getDiseaseTypeBox(), null);
			jSelectionDiseasePanel.add(jLabel2, null);
			jSelectionDiseasePanel.add(getJSearchDiseaseTextFieldPanel(), null);
			jSelectionDiseasePanel.add(getDiseaseBox(), null);
		}
		return jSelectionDiseasePanel;
	}

	private JPanel getJSearchDiseaseTextFieldPanel() {
		JPanel searchFieldPanel = new JPanel();
		JTextField searchDiseasetextField = new JTextField(10);
		searchFieldPanel.add(searchDiseasetextField);
		searchDiseasetextField.addKeyListener(new KeyListener() {

			@Override
			public void keyPressed(KeyEvent e) {
				int key = e.getKeyCode();
				if (key == KeyEvent.VK_ENTER) {
					searchButton.doClick();
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {
			}

			@Override
			public void keyTyped(KeyEvent e) {
			}
		});

		searchButton = new JButton("");
		searchFieldPanel.add(searchButton);
		searchButton.addActionListener(actionEvent -> {
			jDiseaseBox.removeAllItems();
			jDiseaseBox.addItem("");
			for (Disease disease : getSearchDiagnosisResults(searchDiseasetextField.getText(), diseases)) {
				jDiseaseBox.addItem(disease);
			}

			if (jDiseaseBox.getItemCount() >= 2) {
				jDiseaseBox.setSelectedIndex(1);
			}
			jDiseaseBox.requestFocus();
			if (jDiseaseBox.getItemCount() > 2) {
				jDiseaseBox.showPopup();
			}
		});
		searchButton.setPreferredSize(new Dimension(20, 20));
		searchButton.setIcon(new ImageIcon("rsc/icons/zoom_r_button.png"));
		return searchFieldPanel;
	}

	private List<Disease> getSearchDiagnosisResults(String s, List<Disease> diseaseList) {
		String query = s.trim();
		List<Disease> results = new ArrayList<>();
		for (Disease disease : diseaseList) {
			if (!query.isEmpty()) {
				String[] patterns = query.split(" ");
				String name = disease.getDescription().toLowerCase();
				boolean patternFound = false;
				for (String pattern : patterns) {
					if (name.contains(pattern.toLowerCase())) {
						patternFound = true;
						// It is sufficient that only one pattern matches the query
						break;
					}
				}
				if (patternFound) {
					results.add(disease);
				}
			} else {
				results.add(disease);
			}
		}
		return results;
	}

	/**
	 * This method initializes jAgePanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJAgeFromPanel() {
		if (jAgeFromPanel == null) {
			JLabel jLabel = new JLabel(MessageBundle.getMessage("angal.common.agefrom.label"));
			jAgeFromPanel = new JPanel();
			jAgeFromPanel.add(jLabel, null);
			jAgeFromPanel.add(getJAgeFromTextField(), null);
		}
		return jAgeFromPanel;
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
						if (ageFrom < 0 || ageFrom > 200) {
							jAgeFromTextField.setText("");
							MessageDialog.error(OpdBrowser.this, "angal.opd.insertavalidage.msg");
						}
					} catch (NumberFormatException ex) {
						jAgeFromTextField.setText("");
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
	 * This method initializes jPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJAgeToPanel() {
		if (jAgeToPanel == null) {
			JLabel jLabel = new JLabel(MessageBundle.getMessage("angal.common.ageto.label"));
			jAgeToPanel = new JPanel();
			jAgeToPanel.add(jLabel, null);
			jAgeToPanel.add(getJAgeToTextField(), null);
		}
		return jAgeToPanel;
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
						if (ageTo < 0 || ageTo > 200) {
							jAgeToTextField.setText("");
							MessageDialog.error(OpdBrowser.this, "angal.opd.insertavalidage.msg");
						}
					} catch (NumberFormatException ex) {
						jAgeToTextField.setText("");
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
	 * This method initializes jAgePanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJAgePanel() {
		if (jAgePanel == null) {
			jAgePanel = new JPanel();
			jAgePanel.setLayout(new BoxLayout(getJAgePanel(), BoxLayout.Y_AXIS));
			jAgePanel.add(getJAgeFromPanel(), null);
			jAgePanel.add(getJAgeToPanel(), null);
		}
		return jAgePanel;
	}

	class OpdBrowsingModel extends DefaultTableModel {

		private static final long serialVersionUID = -9129145534999353730L;

		public OpdBrowsingModel(String diseaseTypeCode, String diseaseCode, LocalDate dateFrom, LocalDate dateTo, int ageFrom, int ageTo,
				char sex, char newPatient) {
			try {
				pSur = manager.getOpd(diseaseTypeCode, diseaseCode, dateFrom, dateTo, ageFrom, ageTo, sex, newPatient);
			} catch (OHServiceException ohServiceException) {
				MessageDialog.showExceptions(ohServiceException);
			}
		}

		public OpdBrowsingModel() {
			try {
				pSur = manager.getOpd(!GeneralData.ENHANCEDSEARCH);
			} catch (OHServiceException ohServiceException) {
				MessageDialog.showExceptions(ohServiceException);
			}
		}

		@Override
		public int getRowCount() {
			if (pSur == null) {
				return 0;
			}
			return pSur.size();
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
			Opd opd = pSur.get(pSur.size() - r - 1);
			Patient pat = opd.getPatient();
			if (c == -1) {
				return opd;
			} else if (c == 0) {
				return opd.getCode();
			} else if (c == 1) {
				return opd.getProgYear();
			} else if (c == 2) {
				if (GeneralData.OPDEXTENDED) {
					return opd.getDate().format(DATE_TIME_FORMATTER);
				}
				return opd.getDate().format(DATE_FORMATTER);
			} else if (c == 3) {
				return pat != null ? opd.getPatient().getCode() : null;
			} else if (c == 4) {
				return pat != null ? opd.getFullName() : null;
			} else if (c == 5) {
				return opd.getSex();
			} else if (c == 6) {
				return opd.getAge();
			} else if (c == 7) {
				return opd.getDisease().getDescription();
			} else if (c == 8) {
				return opd.getDisease().getType().getDescription();
			} else if (c == 9) {
				String patientStatus;
				if (opd.getNewPatient() == 'N') {
					patientStatus = MessageBundle.getMessage("angal.opd.new.btn");
				} else {
					patientStatus = MessageBundle.getMessage("angal.opd.reattendance.btn");
				}
				return patientStatus;
			}
			return null;
		}

		@Override
		public boolean isCellEditable(int arg0, int arg1) {
			return false;
		}
	}

	@Override
	public void surgeryUpdated(AWTEvent e, Opd opd) {
		pSur.set(pSur.size() - selectedrow - 1, opd);
		((OpdBrowsingModel) jTable.getModel()).fireTableDataChanged();
		jTable.updateUI();
		if ((jTable.getRowCount() > 0) && selectedrow > -1) {
			jTable.setRowSelectionInterval(selectedrow, selectedrow);
		}
		rowCounter.setText(rowCounterText + pSur.size());
	}

	@Override
	public void surgeryInserted(AWTEvent e, Opd opd) {
		pSur.add(pSur.size(), opd);
		((OpdBrowsingModel) jTable.getModel()).fireTableDataChanged();
		if (jTable.getRowCount() > 0) {
			jTable.setRowSelectionInterval(0, 0);
		}
		rowCounter.setText(rowCounterText + pSur.size());
	}

	private JButton getFilterButton() {
		if (filterButton == null) {
			filterButton = new JButton(MessageBundle.getMessage("angal.common.search.btn"));
            filterButton.setMnemonic(MessageBundle.getMnemonic("angal.common.search.btn.key"));
			filterButton.addActionListener(actionEvent -> {
				Object selectedItem = jDiseaseBox.getSelectedItem();
				if (!(selectedItem instanceof Disease)) {
					MessageDialog.error(OpdBrowser.this, "angal.opd.pleaseselectadisease.msg");
					return;
				}
				String disease = ((Disease)selectedItem).getCode();
				String diseasetype = ((DiseaseType)jDiseaseTypeBox.getSelectedItem()).getCode();

				char sex;
				if (radioa.isSelected()) {
					sex='A';
				} else {
					if (radiom.isSelected()) {
						sex='M';
					} else {
						sex='F';
					}
				}

				char newPatient;
				if (radioAll.isSelected()) {
					newPatient='A';
				} else {
					if (radioNew.isSelected()) {
						newPatient='N';
					} else {
						newPatient='R';
					}
				}

				LocalDate dateFromDate = dateFrom.getDate();
				LocalDate dateToDate = dateTo.getDate();

				if (dateFromDate.isAfter(dateToDate)) {
					MessageDialog.error(OpdBrowser.this, "angal.opd.datefrommustbebefordateto.msg");
					return;
				}

				if (ageFrom > ageTo) {
					MessageDialog.error(OpdBrowser.this, "angal.opd.agefrommustbelowerthanageto.msg");
					jAgeFromTextField.setText(ageTo.toString());
					ageFrom = ageTo;
					return;
				}

				//TODO: to retrieve resultset size instead of assuming 1 year as limit for the warning
				if (TimeTools.getDaysBetweenDates(dateFromDate, dateToDate, true) >= 360) {
					int ok = JOptionPane.showConfirmDialog(OpdBrowser.this,
							MessageBundle.getMessage("angal.common.thiscouldretrievealargeamountofdataproceed.msg"),
							MessageBundle.getMessage("angal.messagedialog.question.title"),
							JOptionPane.OK_CANCEL_OPTION);
					if (ok != JOptionPane.OK_OPTION) {
						return;
					}
				}

				model = new OpdBrowsingModel(diseasetype, disease, dateFrom.getDate(), dateTo.getDate(), ageFrom, ageTo, sex, newPatient);
				model.fireTableDataChanged();
				jTable.updateUI();
				rowCounter.setText(rowCounterText + pSur.size());
			});
		}
		return filterButton;
	}

} 
