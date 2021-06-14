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
package org.isf.medicalstockward.gui;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import org.isf.generaldata.GeneralData;
import org.isf.generaldata.MessageBundle;
import org.isf.medicals.manager.MedicalBrowsingManager;
import org.isf.medicals.model.Medical;
import org.isf.medicalstock.manager.MovBrowserManager;
import org.isf.medicalstock.model.Lot;
import org.isf.medicalstock.model.Movement;
import org.isf.medicalstockward.manager.MovWardBrowserManager;
import org.isf.medicalstockward.model.MedicalWard;
import org.isf.medicalstockward.model.MovementWard;
import org.isf.medstockmovtype.model.MovementType;
import org.isf.medtype.manager.MedicalTypeBrowserManager;
import org.isf.medtype.model.MedicalType;
import org.isf.menu.gui.MainMenu;
import org.isf.menu.manager.Context;
import org.isf.patient.model.Patient;
import org.isf.serviceprinting.manager.PrintManager;
import org.isf.stat.gui.report.GenericReportPharmaceuticalStockCard;
import org.isf.stat.gui.report.GenericReportPharmaceuticalStockWard;
import org.isf.utils.excel.ExcelExporter;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.CustomJDateChooser;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.ModalJFrame;
import org.isf.utils.jobjects.StockCardDialog;
import org.isf.utils.jobjects.StockLedgerDialog;
import org.isf.utils.jobjects.VoLimitedTextField;
import org.isf.utils.time.TimeTools;
import org.isf.ward.manager.WardBrowserManager;
import org.isf.ward.model.Ward;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WardPharmacy extends ModalJFrame implements
		WardPharmacyEdit.MovementWardListeners,
		WardPharmacyNew.MovementWardListeners,
		WardPharmacyRectify.MovementWardListeners {

	public void movementInserted(AWTEvent e) {
		jTableOutcomes.setModel(new OutcomesModel());
		jTableDrugs.setModel(new DrugsModel());
		//jTabbedPaneWard.setSelectedComponent(jScrollPaneOutcomes);
	}

	public void movementUpdated(AWTEvent e) {
		jTableOutcomes.setModel(new OutcomesModel());
		jTableDrugs.setModel(new DrugsModel());
		//jTabbedPaneWard.setSelectedComponent(jScrollPaneOutcomes);
	}
	
	private static final long serialVersionUID = 1L;

	private static final Logger LOGGER = LoggerFactory.getLogger(WardPharmacy.class);

	private JComboBox jComboBoxWard;
	private JLabel jLabelWard;
	private JLabel jLabelFrom;
	private JLabel jLabelTo;
	private JTable jTableOutcomes;
	private JPanel jPanelWardAndRange;
	private JPanel jPanelButtons;
	private JScrollPane jScrollPaneOutcomes;
	private JPanel jPanelCentral;
	private JPanel jPanelFilter;
	private JPanel jAgePanel;
	private JPanel sexPanel;
	private JRadioButton radiom;
	private JRadioButton radiof;
	private JRadioButton radioa;
	private JPanel jWeightPanel;
	private VoLimitedTextField jAgeFromTextField;
	private VoLimitedTextField jAgeToTextField;
	private VoLimitedTextField jWeightFromTextField;
	private VoLimitedTextField jWeightToTextField;
	private JComboBox jComboBoxTypes;
	private JComboBox jComboBoxMedicals;
	private JButton filterButton;
	private JButton resetButton;
	private JLabel rowCounter;
	private JTabbedPane jTabbedPaneWard;
	private JTable jTableDrugs;
	private JScrollPane jScrollPaneDrugs;
	private JTable jTableIncomes;
	private JScrollPane jScrollPaneIncomes;
	private JPanel jPanelWard;
	private JButton jButtonNew;
	private JPanel jPanelRange;
	private JButton jButtonClose;
	// private JButton jButtonDelete;
	private JButton jButtonEdit;
	private CustomJDateChooser jCalendarFrom;
	private GregorianCalendar dateFrom = new GregorianCalendar();
	private GregorianCalendar dateTo = new GregorianCalendar();
	private CustomJDateChooser jCalendarTo;
	private DefaultTableModel modelIncomes;
	private DefaultTableModel modelOutcomes;
	private DefaultTableModel modelDrugs;
	private ArrayList<Ward> wardList;
	private Ward wardSelected;
	// private Medical drugSelected;
	private MovementWard movSelected;
	private boolean added = false;
	private String[] columnsIncomes = {
			MessageBundle.getMessage("angal.common.date.txt").toUpperCase(),
			MessageBundle.getMessage("angal.common.from.txt").toUpperCase(),
			MessageBundle.getMessage("angal.wardpharmacy.medical.col").toUpperCase(),
			MessageBundle.getMessage("angal.common.quantity.txt").toUpperCase(),
			MessageBundle.getMessage("angal.common.units.txt").toUpperCase(),
			MessageBundle.getMessage("angal.wardpharmacy.lotnumber.col").toUpperCase(),
			MessageBundle.getMessage("angal.wardpharmacy.lotduedate.col").toUpperCase()
	};
	private boolean[] columnsResizableIncomes = { true, true, true, true, true, true, true };
	private int[] columnWidthIncomes = { 80, 50, 50, 50, 50, 50, 50 };
	private String[] columnsOutcomes = {
			MessageBundle.getMessage("angal.common.date.txt").toUpperCase(),
			MessageBundle.getMessage("angal.wardpharmacy.purpose.col").toUpperCase(),
			MessageBundle.getMessage("angal.common.age.txt").toUpperCase(),
			MessageBundle.getMessage("angal.common.sex.txt").toUpperCase(),
			MessageBundle.getMessage("angal.common.weight.txt").toUpperCase(),
			MessageBundle.getMessage("angal.wardpharmacy.medical.col").toUpperCase(),
			MessageBundle.getMessage("angal.common.quantity.txt").toUpperCase(),
			MessageBundle.getMessage("angal.common.units.txt").toUpperCase(),
			MessageBundle.getMessage("angal.wardpharmacy.lotnumber.col").toUpperCase(),
			MessageBundle.getMessage("angal.wardpharmacy.lotduedate.col").toUpperCase()
	};
	private boolean[] columnsResizableOutcomes = { false, true, false, false, false, true, false, false, true };
	private int[] columnWidthOutcomes = { 150, 150, 50, 50, 50, 170, 50, 50, 50 };
	private String[] columnsDrugs = {
			MessageBundle.getMessage("angal.wardpharmacy.medical.col").toUpperCase(),
			MessageBundle.getMessage("angal.common.quantity.txt").toUpperCase(),
			MessageBundle.getMessage("angal.common.units.txt").toUpperCase(),
			"" //$NON-NLS-1$
	};
	private boolean[] columnsResizableDrugs = { true, true, true, true };
	private int[] columnWidthDrugs = { 150, 50, 50, 50 };
	private final int filterWidth = 250;
	private final int filterSpacing = 5;
	private String rowCounterText = MessageBundle.getMessage("angal.medicalstockward.count") + ": "; //$NON-NLS-1$ //$NON-NLS-2$
	private int ageFrom;
	private int ageTo;
	private int weightFrom;
	private int weightTo;
	private boolean editAllowed;
	private JButton jPrintTableButton = null;
	private JButton jExportToExcelButton = null;
	private JButton jRectifyButton = null;
	private JButton jButtonStockCard;
	private JButton jButtonStockLedger;

	/*
	 * Managers and datas
	 */
	private MovBrowserManager movManager = Context.getApplicationContext().getBean(MovBrowserManager.class);
	private PrintManager printManager = Context.getApplicationContext().getBean(PrintManager.class);
	private ArrayList<Movement> listMovementCentral = new ArrayList<>();
	private MovWardBrowserManager wardManager = Context.getApplicationContext().getBean(MovWardBrowserManager.class);
	private MedicalTypeBrowserManager medicalTypeBrowserManager = Context.getApplicationContext().getBean(MedicalTypeBrowserManager.class);
	private MedicalBrowsingManager medicalManager = Context.getApplicationContext().getBean(MedicalBrowsingManager.class);
	private ArrayList<MovementWard> listMovementWardFromTo = new ArrayList<>();
	private ArrayList<MedicalWard> wardDrugs;
	private ArrayList<MovementWard> wardOutcomes;
	private ArrayList<Movement> wardIncomes;

	//private static final String PREFERRED_LOOK_AND_FEEL = "javax.swing.plaf.metal.MetalLookAndFeel"; //$NON-NLS-1$

	/*
	 * Adds to facilitate the selection of products
	 */
	private JPanel searchPanel;
	private JTextField searchTextField;
	private JButton searchButton;

	public WardPharmacy() {
		if (MainMenu.checkUserGrants("btnmedicalswardedit")) //$NON-NLS-1$
			editAllowed = true;
		initComponents();
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setLocationRelativeTo(null);
		setVisible(true);
		addWindowListener(new WindowAdapter() {

			public void windowClosing(WindowEvent e) {
				// to free memory
				listMovementCentral.clear();
				listMovementWardFromTo.clear();
				if (wardDrugs != null)
					wardDrugs.clear();
				if (wardOutcomes != null)
					wardOutcomes.clear();
				if (wardIncomes != null)
					wardIncomes.clear();
				dispose();
			}
		});
	}

	private void initComponents() {
		add(getJPanelWardAndRange(), BorderLayout.NORTH);
		// add(getJTabbedPaneWard(), BorderLayout.CENTER);
		add(getJPanelButtons(), BorderLayout.SOUTH);
		setTitle(MessageBundle.getMessage("angal.medicalstock.wardpharmacy.title"));
		setSize(800, 450);
	}

	private JPanel getJPanelButtons() {
		if (jPanelButtons == null) {
			jPanelButtons = new JPanel(new FlowLayout());
			jPanelButtons.add(getJButtonNew());
			if (editAllowed)
				jPanelButtons.add(getJButtonEdit());
			if (MainMenu.checkUserGrants("btnmedicalswardrectify")) //$NON-NLS-1$
				jPanelButtons.add(getJRectifyButton());
			// jPanelButtons.add(getJButtonDelete());
			if (MainMenu.checkUserGrants("btnmedicalswardreport")) //$NON-NLS-1$
				jPanelButtons.add(getPrintTableButton());
			if (MainMenu.checkUserGrants("btnmedicalswardexcel")) //$NON-NLS-1$
				jPanelButtons.add(getExportToExcelButton());
			jPanelButtons.add(getJButtonStockCard());
			jPanelButtons.add(getJButtonStockLedger());
			jPanelButtons.add(getJButtonClose());
		}
		return jPanelButtons;
	}

	private JButton getJButtonStockCard() {
		if (jButtonStockCard == null) {
			jButtonStockCard = new JButton(MessageBundle.getMessage("angal.common.stockcard.btn"));
			jButtonStockCard.setMnemonic(MessageBundle.getMnemonic("angal.common.stockcard.btn.key"));
			jButtonStockCard.setVisible(false);
			jButtonStockCard.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent event) {

					Medical medical = null;
					if (jTabbedPaneWard.getSelectedIndex() == 0) {
						if (jTableOutcomes.getSelectedRow() >= 0) {
							MovementWard movWard = (MovementWard) ((jTableOutcomes.getModel()).getValueAt(jTableOutcomes.getSelectedRow(), -1));
							medical = movWard.getMedical();
						}
					} else if (jTabbedPaneWard.getSelectedIndex() == 1) {
						if (jTableIncomes.getSelectedRow() >= 0) {
							Movement mov = (Movement) ((jTableIncomes.getModel()).getValueAt(jTableIncomes.getSelectedRow(), -1));
							medical = mov.getMedical();
						}
					} else if (jTabbedPaneWard.getSelectedIndex() == 2) {
						if (jTableDrugs.getSelectedRow() >= 0) {
							MedicalWard medicalWard = (MedicalWard) ((jTableDrugs.getModel()).getValueAt(jTableDrugs.getSelectedRow(), -1));
							medical = medicalWard.getMedical();
						}
					}

					StockCardDialog stockCardDialog = new StockCardDialog(WardPharmacy.this, medical, dateFrom.getTime(), dateTo.getTime());
					medical = stockCardDialog.getMedical();
					Date dateFrom = stockCardDialog.getDateFrom();
					Date dateTo = stockCardDialog.getDateTo();
					boolean toExcel = stockCardDialog.isExcel();

					if (!stockCardDialog.isCancel()) {
						new GenericReportPharmaceuticalStockCard("ProductLedgerWard", dateFrom, dateTo, medical, wardSelected, toExcel);
					}
				}
			});
		}
		return jButtonStockCard;
	}
	
	private JButton getJButtonStockLedger() {
		if (jButtonStockLedger == null) {
			jButtonStockLedger = new JButton(MessageBundle.getMessage("angal.common.stockledger.btn"));
			jButtonStockLedger.setMnemonic(MessageBundle.getMnemonic("angal.common.stockledger.btn.key"));
			jButtonStockLedger.setVisible(false);
			jButtonStockLedger.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent event){

					StockLedgerDialog stockCardDialog = new StockLedgerDialog(WardPharmacy.this, dateFrom.getTime(), dateTo.getTime());
					Date dateFrom = stockCardDialog.getDateFrom();
					Date dateTo = stockCardDialog.getDateTo();
					
					if (!stockCardDialog.isCancel()) {
						new GenericReportPharmaceuticalStockCard("ProductLedgerWard_multi", dateFrom, dateTo, null, wardSelected, false);
					}
				}
			});
		}
		return jButtonStockLedger;
	}

	private JButton getJButtonNew() {
		if (jButtonNew == null) {
			jButtonNew = new JButton(MessageBundle.getMessage("angal.common.new.btn"));
			jButtonNew.setMnemonic(MessageBundle.getMnemonic("angal.common.new.btn.key"));
			jButtonNew.setVisible(false);
			jButtonNew.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					WardPharmacyNew editor = new WardPharmacyNew(WardPharmacy.this, wardSelected, wardDrugs);
					editor.addMovementWardListener(WardPharmacy.this);
					editor.setVisible(true);
				}
			});
		}
		return jButtonNew;
	}

	private JButton getJButtonEdit() {
		if (jButtonEdit == null) {
			jButtonEdit = new JButton(MessageBundle.getMessage("angal.common.edit.btn"));
			jButtonEdit.setMnemonic(MessageBundle.getMnemonic("angal.common.edit.btn.key"));
			jButtonEdit.setVisible(false);
			jButtonEdit.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {

					if (jTableOutcomes.getSelectedRow() < 0 || !jScrollPaneOutcomes.isShowing()) {
						MessageDialog.error(WardPharmacy.this, "angal.medicalstockward.pleaseselectanoutcomesmovementfirst");
					} else {
						movSelected = (MovementWard) ((jTableOutcomes.getModel()).getValueAt(jTableOutcomes.getSelectedRow(), -1));
						WardPharmacyEdit editor = new WardPharmacyEdit(WardPharmacy.this, movSelected, wardDrugs);
						editor.addMovementWardListener(WardPharmacy.this);
						editor.setVisible(true);
					}
				}
			});
		}
		return jButtonEdit;
	}

	private JButton getJButtonClose() {
		if (jButtonClose == null) {
			jButtonClose = new JButton(MessageBundle.getMessage("angal.common.close.btn"));
			jButtonClose.setMnemonic(MessageBundle.getMnemonic("angal.common.close.btn.key"));
			jButtonClose.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent event) {
					// to free memory
					listMovementCentral.clear();
					listMovementWardFromTo.clear();
					if (wardDrugs != null)
						wardDrugs.clear();
					dispose();
				}
			});
		}
		return jButtonClose;
	}

	private JPanel getJPanelWard() {
		if (jPanelWard == null) {
			jPanelWard = new JPanel();
			jPanelWard.setLayout(new FlowLayout());
			jPanelWard.add(getJComboBoxWard());
			jPanelWard.add(getJLabelWard());
		}
		return jPanelWard;
	}

	private JPanel getJPanelWardAndRange() {
		if (jPanelWardAndRange == null) {
			jPanelWardAndRange = new JPanel(new BorderLayout());
			jPanelWardAndRange.add(getJPanelWard(), BorderLayout.WEST);
			jPanelWardAndRange.add(getJPanelRange(), BorderLayout.EAST);
		}
		return jPanelWardAndRange;
	}

	private Component getJPanelRange() {
		if (jPanelRange == null) {
			jPanelRange = new JPanel();
			jPanelRange.setLayout(new FlowLayout());
			jPanelRange.add(getJLabelFrom());
			jPanelRange.add(getJCalendarFrom());
			jPanelRange.add(getJLabelTo());
			jPanelRange.add(getJCalendarTo());
		}
		return jPanelRange;
	}

	private CustomJDateChooser getJCalendarTo() {
		if (jCalendarTo == null) {
			dateTo.set(GregorianCalendar.HOUR_OF_DAY, 23);
			dateTo.set(GregorianCalendar.MINUTE, 59);
			dateTo.set(GregorianCalendar.SECOND, 59);
			jCalendarTo = new CustomJDateChooser(dateTo.getTime()); // Calendar
			jCalendarTo.setLocale(new Locale(GeneralData.LANGUAGE));
			jCalendarTo.setDateFormatString("dd/MM/yy"); //$NON-NLS-1$
			jCalendarTo.addPropertyChangeListener("date", new PropertyChangeListener() { //$NON-NLS-1$

				public void propertyChange(PropertyChangeEvent evt) {
					dateTo.setTime((Date) evt.getNewValue());
					jTableOutcomes.setModel(new OutcomesModel());
					jTableIncomes.setModel(new IncomesModel());
					rowCounter.setText(rowCounterText + jTableOutcomes.getRowCount());
				}
			});
			jCalendarTo.setEnabled(false);
		}
		return jCalendarTo;
	}

	private CustomJDateChooser getJCalendarFrom() {
		if (jCalendarFrom == null) {
			dateFrom.set(GregorianCalendar.HOUR_OF_DAY, 0);
			dateFrom.set(GregorianCalendar.MINUTE, 0);
			dateFrom.set(GregorianCalendar.SECOND, 0);
			jCalendarFrom = new CustomJDateChooser(dateFrom.getTime()); // Calendar
			jCalendarFrom.setLocale(new Locale(GeneralData.LANGUAGE));
			jCalendarFrom.setDateFormatString("dd/MM/yy"); //$NON-NLS-1$
			jCalendarFrom.addPropertyChangeListener("date", new PropertyChangeListener() { //$NON-NLS-1$

				public void propertyChange(PropertyChangeEvent evt) {
					dateFrom.setTime((Date) evt.getNewValue());
					jTableOutcomes.setModel(new OutcomesModel());
					jTableIncomes.setModel(new IncomesModel());
					rowCounter.setText(rowCounterText + jTableOutcomes.getRowCount());
				}
			});
			jCalendarFrom.setEnabled(false);
		}
		return jCalendarFrom;
	}

	private JScrollPane getJScrollPaneIncomes() {
		if (jScrollPaneIncomes == null) {
			jScrollPaneIncomes = new JScrollPane();
			jScrollPaneIncomes.setViewportView(getJTableIncomes());
		}
		return jScrollPaneIncomes;
	}

	private JTable getJTableIncomes() {
		if (jTableIncomes == null) {
			modelIncomes = new IncomesModel();
			jTableIncomes = new JTable(modelIncomes);
			for (int i = 0; i < columnWidthIncomes.length; i++) {
				jTableIncomes.getColumnModel().getColumn(i).setMinWidth(columnWidthIncomes[i]);
				if (!columnsResizableIncomes[i])
					jTableIncomes.getColumnModel().getColumn(i).setMaxWidth(columnWidthIncomes[i]);
			}
			jTableIncomes.setAutoCreateColumnsFromModel(false);
		}
		return jTableIncomes;
	}

	private JScrollPane getJScrollPaneDrugs() {
		if (jScrollPaneDrugs == null) {
			jScrollPaneDrugs = new JScrollPane();
			jScrollPaneDrugs.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
					MessageBundle.getMessage("angal.medicalstock.clickdrugs"),
					TitledBorder.LEFT,
					TitledBorder.TOP));
			jScrollPaneDrugs.setViewportView(getJTableDrugs());
		}
		return jScrollPaneDrugs;
	}

	private JTable getJTableDrugs() {
		if (jTableDrugs == null) {
			modelDrugs = new DrugsModel();
			jTableDrugs = new JTable(modelDrugs);
			TableCellRenderer buttonRenderer = new JTableButtonRenderer();
			jTableDrugs.getColumn("").setCellRenderer(buttonRenderer);
			for (int i = 0; i < columnWidthDrugs.length; i++) {
				jTableDrugs.getColumnModel().getColumn(i).setMinWidth(columnWidthDrugs[i]);
				if (!columnsResizableDrugs[i])
					jTableDrugs.getColumnModel().getColumn(i).setMaxWidth(columnWidthDrugs[i]);
			}

			jTableDrugs.addMouseListener(new MouseAdapter() {

				public void mouseClicked(MouseEvent me) {
					int column = jTableDrugs.getColumnModel().getColumnIndexAtX(me.getX()); // get the column of the button
					JTable target = (JTable) me.getSource();
					int row = target.getSelectedRow(); // select a row

					/*Checking the row or column is valid or not*/
					if (row < jTableDrugs.getRowCount() && row >= 0 && column < jTableDrugs.getColumnCount() && column >= 0) {
						Object value = jTableDrugs.getValueAt(row, column);
						if (value instanceof JButton) {
							/*perform a click event*/
							((JButton) value).doClick();
						}
					}

					if (me.getClickCount() == 2) {     // to detect double click events

						showLotDetail(wardDrugs, (String) jTableDrugs.getValueAt(row, 0));// get the value of a row and column.
					}
				}
			});
		}
		return jTableDrugs;
	}

	private static class JTableButtonRenderer implements TableCellRenderer {

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			JButton button = (JButton) value;
			return button;
		}
	}

	private void showLotDetail(ArrayList<MedicalWard> drug, String me) {
		ArrayList<MedicalWard> medicalWardList = new ArrayList<>();
		for (MedicalWard elem : drug) {
			if (elem.getMedical().getDescription().equals(me)) {
				if (elem.getQty() != 0.0) {
					MedicalWard e = elem;
					medicalWardList.add(e);
				}
			}
		}
		if (medicalWardList.isEmpty()) {
			return;
		}
		JTable lotTable = new JTable(new StockMovModel(medicalWardList));
		lotTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(new JScrollPane(lotTable), BorderLayout.CENTER);

		JOptionPane.showMessageDialog(WardPharmacy.this,
				panel,
				MessageBundle.getMessage("angal.medicalstock.multipledischarging.lotinformations"), //$NON-NLS-1$
				JOptionPane.INFORMATION_MESSAGE);
	}

	private static final String DATE_FORMAT_DD_MM_YYYY = "dd/MM/yyyy"; //$NON-NLS-1$

	class StockMovModel extends DefaultTableModel {

		private static final long serialVersionUID = 1L;
		private ArrayList<MedicalWard> druglist;

		public StockMovModel(ArrayList<MedicalWard> drug) {
			druglist = drug;
		}

		public int getRowCount() {
			if (druglist == null)
				return 0;
			return druglist.size();
		}

		public String getColumnName(int c) {
			if (c == 0) {
				return MessageBundle.getMessage("angal.medicalstock.lotid"); //$NON-NLS-1$
			}

			if (c == 1) {
				return MessageBundle.getMessage("angal.medicalstock.duedate"); //$NON-NLS-1$
			}
			if (c == 2) {
				return MessageBundle.getMessage("angal.common.quantity.txt");
			}
			return ""; //$NON-NLS-1$
		}

		public int getColumnCount() {
			return 3;
		}

		public Object getValueAt(int r, int c) {
			MedicalWard medicalWard = druglist.get(r);

			if (c == -1) {
				return medicalWard;
			} else if (c == 0) {
				return medicalWard.getId().getLot();
			} else if (c == 1) {
				return TimeTools.formatDateTime(medicalWard.getId().getLot().getDueDate(), DATE_FORMAT_DD_MM_YYYY);
			} else if (c == 2) {
				return medicalWard.getQty();
			}
			return null;
		}

		@Override
		public boolean isCellEditable(int arg0, int arg1) {
			return false;
		}
	}

	private JPanel getJPanelCentral() {
		if (jPanelCentral == null) {
			jPanelCentral = new JPanel(new BorderLayout());
			jPanelCentral.add(getJPanelFilter(), BorderLayout.WEST);
			jPanelCentral.add(getJTabbedPaneWard(), BorderLayout.CENTER);
		}
		return jPanelCentral;
	}

	private JPanel getJPanelFilter() {
		if (jPanelFilter == null) {
			jPanelFilter = new JPanel();
			jPanelFilter.setLayout(new BoxLayout(jPanelFilter, BoxLayout.Y_AXIS));
			jPanelFilter.add(Box.createVerticalStrut(filterSpacing));
			JLabel jLabelMedical = new JLabel(MessageBundle.getMessage("angal.medicalstockward.medical")); //$NON-NLS-1$
			jLabelMedical.setAlignmentX(Box.CENTER_ALIGNMENT);
			jPanelFilter.add(jLabelMedical);
			jPanelFilter.add(Box.createVerticalStrut(filterSpacing));
			jPanelFilter.add(getJComboBoxTypes());
			jPanelFilter.add(Box.createVerticalStrut(filterSpacing));
			jPanelFilter.add(getJPanelMedicalsSearch());
			jPanelFilter.add(Box.createVerticalStrut(filterSpacing));
			jPanelFilter.add(getJComboBoxMedicals());
			jPanelFilter.add(Box.createVerticalStrut(filterSpacing));
			jPanelFilter.add(getJPanelAge());
			jPanelFilter.add(getSexPanel());
			jPanelFilter.add(getJPanelWeight());
			jPanelFilter.add(getFilterResetPanel());
			jPanelFilter.add(Box.createVerticalGlue());
			jPanelFilter.add(getRowCounter());

		}
		return jPanelFilter;
	}

	private JPanel getFilterResetPanel() {
		JPanel jFilterResetPanel = new JPanel();
		jFilterResetPanel.add(getFilterButton());
		jFilterResetPanel.add(getResetButton());
		return jFilterResetPanel;
	}

	private JLabel getRowCounter() {
		if (rowCounter == null) {
			rowCounter = new JLabel();
			rowCounter.setAlignmentX(Box.CENTER_ALIGNMENT);
		}
		return rowCounter;
	}

	private JButton getFilterButton() {
		if (filterButton == null) {
			filterButton = new JButton(MessageBundle.getMessage("angal.common.filter.btn"));
			filterButton.setMnemonic(MessageBundle.getMnemonic("angal.common.filter.btn.key"));
			filterButton.setAlignmentX(Box.CENTER_ALIGNMENT);
			filterButton.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					if (ageFrom > ageTo) {
						MessageDialog.error(WardPharmacy.this, "angal.medicalstockward.agefrommustbelowerthanageto");
						jAgeFromTextField.setText(String.valueOf(ageTo));
						ageFrom = ageTo;
						return;
					}
					if (weightFrom > weightTo) {
						MessageDialog.error(WardPharmacy.this, "angal.medicalstockward.weightfrommustbelowerthanweightto");
						jWeightFromTextField.setText(String.valueOf(weightTo));
						weightFrom = weightTo;
						return;
					}
					jTableOutcomes.setModel(new OutcomesModel());
					rowCounter.setText(rowCounterText + jTableOutcomes.getRowCount());
				}
			});
		}
		return filterButton;
	}

	private JButton getResetButton() {
		if (resetButton == null) {
			resetButton = new JButton(MessageBundle.getMessage("angal.medicalstockward.reset.btn"));
			resetButton.setMnemonic(MessageBundle.getMnemonic("angal.medicalstockward.reset.btn.key"));
			resetButton.setAlignmentX(Box.CENTER_ALIGNMENT);
			resetButton.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					jAgeFromTextField.setText("0"); //$NON-NLS-1$
					jAgeToTextField.setText("0"); //$NON-NLS-1$
					jWeightFromTextField.setText("0"); //$NON-NLS-1$
					jWeightToTextField.setText("0"); //$NON-NLS-1$
					radioa.setSelected(true);
					jComboBoxTypes.setSelectedIndex(0);
					rowCounter.setText(rowCounterText + jTableOutcomes.getRowCount());
				}

			});
		}
		return resetButton;
	}

	private JPanel getJPanelWeight() {
		if (jWeightPanel == null) {
			jWeightPanel = new JPanel();
			jWeightPanel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.common.weight.txt")));

			JLabel jLabelWeightFrom = new JLabel(MessageBundle.getMessage("angal.common.from.txt"));
			jWeightPanel.add(jLabelWeightFrom, null);
			jWeightPanel.add(getJWeightFromTextField(), null);

			JLabel jLabelWeightTo = new JLabel(MessageBundle.getMessage("angal.common.telephone.txt"));
			jWeightPanel.add(jLabelWeightTo, null);
			jWeightPanel.add(getJWeightToTextField(), null);
		}
		return jWeightPanel;
	}

	private VoLimitedTextField getJWeightToTextField() {
		if (jWeightToTextField == null) {
			jWeightToTextField = new VoLimitedTextField(5, 5);
			jWeightToTextField.setText("0"); //$NON-NLS-1$
			jWeightToTextField.setMaximumSize(new Dimension(100, 50));
			jWeightToTextField.addFocusListener(new FocusListener() {

				public void focusLost(FocusEvent e) {
					try {
						weightTo = Integer.parseInt(jWeightToTextField.getText());
						weightFrom = Integer.parseInt(jWeightFromTextField.getText());
						if ((weightTo < 0) || (weightTo > 200)) {
							jWeightToTextField.setText(""); //$NON-NLS-1$
							JOptionPane
									.showMessageDialog(WardPharmacy.this, MessageBundle.getMessage("angal.medicalstockward.insertavalidweight")); //$NON-NLS-1$
						}
					} catch (NumberFormatException ex) {
						jWeightToTextField.setText("0"); //$NON-NLS-1$
					}
				}

				public void focusGained(FocusEvent e) {
				}
			});
		}
		return jWeightToTextField;
	}

	private VoLimitedTextField getJWeightFromTextField() {
		if (jWeightFromTextField == null) {
			jWeightFromTextField = new VoLimitedTextField(5, 5);
			jWeightFromTextField.setText("0"); //$NON-NLS-1$
			jWeightFromTextField.setMinimumSize(new Dimension(100, 50));
			jWeightFromTextField.addFocusListener(new FocusListener() {

				public void focusLost(FocusEvent e) {
					try {
						weightFrom = Integer.parseInt(jWeightFromTextField.getText());
						weightTo = Integer.parseInt(jWeightToTextField.getText());
						if ((weightFrom < 0)) {
							jWeightFromTextField.setText("");
							JOptionPane.showMessageDialog(WardPharmacy.this, MessageBundle.getMessage("angal.medicalstockward.insertavalidweight"));
						}
					} catch (NumberFormatException ex) {
						jWeightFromTextField.setText("0");
					}
				}

				public void focusGained(FocusEvent e) {
				}
			});
		}
		return jWeightFromTextField;
	}

	public JPanel getSexPanel() {
		if (sexPanel == null) {
			sexPanel = new JPanel();
			sexPanel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.common.sex.txt")));
			ButtonGroup group = new ButtonGroup();
			radiom = new JRadioButton(MessageBundle.getMessage("angal.common.male.btn"));
			radiof = new JRadioButton(MessageBundle.getMessage("angal.common.female.btn"));
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

	private JPanel getJPanelAge() {
		if (jAgePanel == null) {
			jAgePanel = new JPanel();
			jAgePanel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.common.age.txt")));

			JLabel jLabelAgeFrom = new JLabel(MessageBundle.getMessage("angal.common.agefrom.label"));
			jAgePanel.add(jLabelAgeFrom);
			jAgePanel.add(getJAgeFromTextField());

			JLabel jLabelAgeTo = new JLabel(MessageBundle.getMessage("angal.common.ageto.label"));
			jAgePanel.add(jLabelAgeTo);
			jAgePanel.add(getJAgeToTextField());
		}
		return jAgePanel;
	}

	private VoLimitedTextField getJAgeToTextField() {
		if (jAgeToTextField == null) {
			jAgeToTextField = new VoLimitedTextField(3, 3);
			jAgeToTextField.setText("0"); //$NON-NLS-1$
			jAgeToTextField.setMaximumSize(new Dimension(100, 50));
			jAgeToTextField.addFocusListener(new FocusListener() {

				public void focusLost(FocusEvent e) {
					try {
						ageTo = Integer.parseInt(jAgeToTextField.getText());
						ageFrom = Integer.parseInt(jAgeFromTextField.getText());
						if ((ageTo < 0) || (ageTo > 200)) {
							jAgeToTextField.setText("");
							MessageDialog.error(WardPharmacy.this, "angal.medicalstockward.insertvalidage");
						}
					} catch (NumberFormatException ex) {
						jAgeToTextField.setText("0");
					}
				}

				public void focusGained(FocusEvent e) {
				}
			});
		}
		return jAgeToTextField;
	}

	/**
	 * This method initializes jAgeFromTextField
	 *
	 * @return javax.swing.JTextField
	 */
	private VoLimitedTextField getJAgeFromTextField() {
		if (jAgeFromTextField == null) {
			jAgeFromTextField = new VoLimitedTextField(3, 3);
			jAgeFromTextField.setText("0"); //$NON-NLS-1$
			jAgeFromTextField.setMinimumSize(new Dimension(100, 50));
			jAgeFromTextField.addFocusListener(new FocusListener() {

				public void focusLost(FocusEvent e) {
					try {
						ageFrom = Integer.parseInt(jAgeFromTextField.getText());
						ageTo = Integer.parseInt(jAgeToTextField.getText());
						if ((ageFrom < 0) || (ageFrom > 200)) {
							jAgeFromTextField.setText("");
							MessageDialog.error(WardPharmacy.this, "angal.medicalstockward.insertvalidage");
						}
					} catch (NumberFormatException ex) {
						jAgeFromTextField.setText("0");
					}
				}

				public void focusGained(FocusEvent e) {
				}
			});
		}
		return jAgeFromTextField;
	}

	private JComboBox getJComboBoxTypes() {
		if (jComboBoxTypes == null) {
			jComboBoxTypes = new JComboBox();
			jComboBoxTypes.setMaximumSize(new Dimension(filterWidth, 24));
			jComboBoxTypes.setPreferredSize(new Dimension(filterWidth, 24));
			ArrayList<MedicalType> medicalTypes;

			jComboBoxTypes.addItem(MessageBundle.getMessage("angal.common.alltypes.txt"));

			try {
				medicalTypes = medicalTypeBrowserManager.getMedicalType();

				for (MedicalType aMedicalType : medicalTypes) {
					jComboBoxTypes.addItem(aMedicalType);
				}
			} catch (OHServiceException e) {
				OHServiceExceptionUtil.showMessages(e);
			}

			jComboBoxTypes.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent arg0) {
					jComboBoxMedicals.removeAllItems();
					getJComboBoxMedicals();

				}
			});
		}
		return jComboBoxTypes;
	}

	private JPanel getJPanelMedicalsSearch() {
		searchButton = new JButton();
		searchButton.setPreferredSize(new Dimension(20, 20));
		searchButton.setIcon(new ImageIcon("rsc/icons/zoom_r_button.png"));
		searchButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent ae) {
				jComboBoxMedicals.removeAllItems();
				ArrayList<Medical> medicals;
				try {
					medicals = medicalManager.getMedicals();
				} catch (OHServiceException e1) {
					medicals = null;
					OHServiceExceptionUtil.showMessages(e1);
				}
				MedicalType medicalType;
				if (jComboBoxTypes.getSelectedItem() instanceof String) {
					medicalType = null;
				} else {
					medicalType = (MedicalType) jComboBoxTypes.getSelectedItem();
				}
				if (null != medicals) {
					ArrayList<Medical> results = getSearchMedicalsResults(searchTextField.getText(), medicals);
					int originalSize = medicals.size();
					int resultsSize = results.size();
					if (originalSize == resultsSize) {
						jComboBoxMedicals.addItem(MessageBundle.getMessage("angal.medicalstockward.allmedicals"));
					}
					for (Medical aMedical : results) {
						boolean ok = true;
						if (medicalType != null)
							ok = ok && aMedical.getType().equals(medicalType);
						if (ok)
							jComboBoxMedicals.addItem(aMedical);
					}
				}
			}
		});

		searchTextField = new JTextField(15);
		//searchTextField.setToolTipText(MessageBundle.getMessage("angal.medicalstock.pharmaceutical"));
		searchTextField.addKeyListener(new KeyListener() {

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

		searchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		searchPanel.add(searchTextField);
		searchPanel.add(searchButton);
		searchPanel.setMaximumSize(new Dimension(filterWidth, 25));
		searchPanel.setMinimumSize(new Dimension(filterWidth, 25));
		searchPanel.setPreferredSize(new Dimension(filterWidth, 25));
		return searchPanel;
	}

	private JComboBox getJComboBoxMedicals() {
		if (jComboBoxMedicals == null) {
			jComboBoxMedicals = new JComboBox();
			jComboBoxMedicals.setMaximumSize(new Dimension(filterWidth, 24));
			jComboBoxMedicals.setPreferredSize(new Dimension(filterWidth, 24));
		}
		ArrayList<Medical> medicals;
		try {
			medicals = medicalManager.getMedicals();
		} catch (OHServiceException e) {
			medicals = null;
			OHServiceExceptionUtil.showMessages(e);
		}
		jComboBoxMedicals.addItem(MessageBundle.getMessage("angal.medicalstockward.allmedicals")); //$NON-NLS-1$
		MedicalType medicalType;
		if (jComboBoxTypes.getSelectedItem() instanceof String) {
			medicalType = null;
		} else {
			medicalType = (MedicalType) jComboBoxTypes.getSelectedItem();
		}
		if (null != medicals) {
			for (Medical aMedical : medicals) {
				boolean ok = true;
				if (medicalType != null)
					ok = ok && aMedical.getType().equals(medicalType);
				if (ok)
					jComboBoxMedicals.addItem(aMedical);
			}
		}
		return jComboBoxMedicals;
	}

	private JTabbedPane getJTabbedPaneWard() {
		if (jTabbedPaneWard == null) {
			jTabbedPaneWard = new JTabbedPane();
			jTabbedPaneWard.addTab(MessageBundle.getMessage("angal.medicalstockward.outcomes"), getJScrollPaneOutcomes()); //$NON-NLS-1$
			jTabbedPaneWard.addTab(MessageBundle.getMessage("angal.medicalstockward.incomings"), getJScrollPaneIncomes()); //$NON-NLS-1$
			jTabbedPaneWard.addTab(MessageBundle.getMessage("angal.medicalstockward.drugs"), getJScrollPaneDrugs()); //$NON-NLS-1$
		}
		return jTabbedPaneWard;
	}

	private JScrollPane getJScrollPaneOutcomes() {
		if (jScrollPaneOutcomes == null) {
			jScrollPaneOutcomes = new JScrollPane();
			jScrollPaneOutcomes.setViewportView(getJTableOutcomes());
		}
		return jScrollPaneOutcomes;
	}

	private JTable getJTableOutcomes() {
		if (jTableOutcomes == null) {
			modelOutcomes = new OutcomesModel();
			jTableOutcomes = new JTable(modelOutcomes);
			jTableOutcomes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			for (int i = 0; i < columnWidthOutcomes.length; i++) {
				jTableOutcomes.getColumnModel().getColumn(i).setPreferredWidth(columnWidthOutcomes[i]);
				if (!columnsResizableOutcomes[i])
					jTableOutcomes.getColumnModel().getColumn(i).setMaxWidth(columnWidthOutcomes[i]);
			}
			jTableOutcomes.setDefaultRenderer(Object.class, new BlueBoldTableCellRenderer());
			jTableOutcomes.setAutoCreateColumnsFromModel(false);
		}
		return jTableOutcomes;
	}

	private JLabel getJLabelTo() {
		if (jLabelTo == null) {
			jLabelTo = new JLabel();
			jLabelTo.setText(MessageBundle.getMessage("angal.common.to.txt"));
			jLabelTo.setBounds(509, 15, 45, 15);
		}
		return jLabelTo;
	}

	private JLabel getJLabelFrom() {
		if (jLabelFrom == null) {
			jLabelFrom = new JLabel();
			jLabelFrom.setText(MessageBundle.getMessage("angal.common.from.txt")); //$NON-NLS-1$
			jLabelFrom.setBounds(365, 14, 45, 15);
		}
		return jLabelFrom;
	}

	private JLabel getJLabelWard() {
		if (jLabelWard == null) {
			jLabelWard = new JLabel();
			jLabelWard.setText(MessageBundle.getMessage("angal.medicalstockward.ward")); //$NON-NLS-1$
			jLabelWard.setBounds(148, 18, 45, 15);
		}
		return jLabelWard;
	}

	private JComboBox getJComboBoxWard() {
		if (jComboBoxWard == null) {
			jComboBoxWard = new JComboBox();
			WardBrowserManager wardManager = Context.getApplicationContext().getBean(WardBrowserManager.class);
			try {
				wardList = wardManager.getWards();
			} catch (OHServiceException e) {
				wardList = new ArrayList<>();
				OHServiceExceptionUtil.showMessages(e);
			}
			jComboBoxWard.addItem(MessageBundle.getMessage("angal.medicalstockward.selectaward")); //$NON-NLS-1$
			for (Ward ward : wardList) {
				if (ward.isPharmacy())
					jComboBoxWard.addItem(ward);
			}
			jComboBoxWard.setBorder(null);
			jComboBoxWard.setBounds(15, 14, 122, 24);
			jComboBoxWard.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent event) {
					Object ward = jComboBoxWard.getSelectedItem();
					if (ward instanceof Ward) {
						wardSelected = (Ward) ward;
						if (!added) {
							add(getJPanelCentral());
							jCalendarFrom.setEnabled(true);
							jCalendarTo.setEnabled(true);
							jButtonNew.setVisible(true);
							if (MainMenu.checkUserGrants("btnmedicalswardreport")) //$NON-NLS-1$
								jPrintTableButton.setVisible(true);
							if (MainMenu.checkUserGrants("btnmedicalswardexcel")) //$NON-NLS-1$
								jExportToExcelButton.setVisible(true);
							if (MainMenu.checkUserGrants("btnmedicalswardrectify")) //$NON-NLS-1$
								jRectifyButton.setVisible(true);
							if (editAllowed)
								jButtonEdit.setVisible(true);
							jButtonStockCard.setVisible(true);
							jButtonStockLedger.setVisible(true);
							validate();
							setLocationRelativeTo(null);
							// jButtonDelete.setVisible(true);
							added = true;
						} else {
							if (wardSelected != null) {
								jTableIncomes.setModel(new IncomesModel());
								jTableOutcomes.setModel(new OutcomesModel());
								jTableDrugs.setModel(new DrugsModel());
							} else {
								remove(jTabbedPaneWard);
								// jButtonEdit.setVisible(false);
								jButtonNew.setVisible(false);
								if (MainMenu.checkUserGrants("btnmedicalswardreport")) //$NON-NLS-1$
									jPrintTableButton.setVisible(false);
								if (MainMenu.checkUserGrants("btnmedicalswardexcel")) //$NON-NLS-1$
									jExportToExcelButton.setVisible(false);
								if (MainMenu.checkUserGrants("btnmedicalswardrectify")) //$NON-NLS-1$
									jRectifyButton.setVisible(false);
								// jButtonDelete.setVisible(false);
								added = false;
							}
						}
						jComboBoxWard.setEnabled(false);
						rowCounter.setText(rowCounterText + jTableOutcomes.getRowCount());
						validate();
						repaint();
					}
				}
			});
		}
		return jComboBoxWard;
	}

	class IncomesModel extends DefaultTableModel {

		/**
		 *
		 */
		private static final long serialVersionUID = 1L;
		//private ArrayList<Movement> wardIncomes; --> Global

		public IncomesModel() {
			wardIncomes = new ArrayList<>();
			try {
				listMovementCentral = movManager.getMovements(wardSelected.getCode(), dateFrom, dateTo);

				for (Movement mov : listMovementCentral) {
					if (mov.getWard().getDescription() != null) {
						if (mov.getWard().equals(wardSelected)) {
							wardIncomes.add(mov);
						}
					}
				}

				//List movements from other wards 
				for (MovementWard wMvnt : wardManager.getWardMovementsToWard(wardSelected.getCode(), dateFrom, dateTo)) {
					if (wMvnt.getWardTo().getDescription() != null) {
						if (wMvnt.getWardTo().equals(wardSelected)) {
							MovementType typeCharge = new MovementType("fromward", wMvnt.getWard().getDescription(), "*");
							wardIncomes.add(new Movement(
									wMvnt.getMedical(),
									typeCharge,
									wardSelected,
									wMvnt.getLot(),
									wMvnt.getDate(),
									wMvnt.getQuantity().intValue(),
									null,
									null));
						}
					}
				}
			} catch (OHServiceException ohServiceException) {
				OHServiceExceptionUtil.showMessages(ohServiceException);
				LOGGER.error(ohServiceException.getMessage(), ohServiceException);
			}
		}

		public int getRowCount() {
			if (wardIncomes == null)
				return 0;
			return wardIncomes.size();
		}

		public Object getValueAt(int r, int c) {
			Movement mov = wardIncomes.get(r);
			int pieces = mov.getQuantity();
			int pcsPerPck = mov.getMedical().getPcsperpck();
			if (c == -1) {
				return mov;
			}
			if (c == 0) {
				return formatDate(mov.getDate());
			}
			if (c == 1) {
				if (mov.getType().getCode().equals("fromward"))
					return mov.getType().getDescription();
				else
					return mov.getRefNo();
			}
			if (c == 2) {
				return mov.getMedical();
			}
			if (c == 3) {
				return pieces;
			}
			if (c == 4) {
				int packets = 0;
				if (pcsPerPck != 0) {
					packets = pieces / pcsPerPck;
					return "" + packets + " " + MessageBundle.getMessage("angal.medicalstockward.packets"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$;
				} else {
					return ""; //$NON-NLS-1$;
				}
			}
			if (c == 5) {
				return mov.getLot().getCode();
			}
			if (c == 6) {
				return formatDate(mov.getLot().getDueDate());
			}
			return null;
		}

		public String getColumnName(int c) {
			return columnsIncomes[c];
		}

		public int getColumnCount() {
			return columnsIncomes.length;
		}

		public boolean isCellEditable(int arg0, int arg1) {
			return false;
		}
	}

	class OutcomesModel extends DefaultTableModel {

		private static final long serialVersionUID = 1L;
		//private ArrayList<MovementWard> wardOutcomes; --> Global

		public OutcomesModel() {
			wardOutcomes = new ArrayList<>();
			try {
				listMovementWardFromTo = wardManager.getMovementWard(wardSelected.getCode(), dateFrom, dateTo);
			} catch (OHServiceException e) {
				OHServiceExceptionUtil.showMessages(e);
				listMovementWardFromTo = new ArrayList<>();
			}

			Medical medicalSelected;
			if (jComboBoxMedicals.getSelectedItem() instanceof String) {
				medicalSelected = null;
			} else {
				medicalSelected = (Medical) jComboBoxMedicals.getSelectedItem();
			}

			MedicalType medicalTypeSelected;
			if (jComboBoxTypes.getSelectedItem() instanceof String) {
				medicalTypeSelected = null;
			} else {
				medicalTypeSelected = (MedicalType) jComboBoxTypes.getSelectedItem();
			}

			char sex;
			if (radioa.isSelected()) {
				sex = 'A';
			} else if (radiom.isSelected()) {
				sex = 'M';
			} else {
				sex = 'F';
			}

			int ageFrom = Integer.parseInt(jAgeFromTextField.getText());
			int ageTo = Integer.parseInt(jAgeToTextField.getText());

			float weightFrom = Float.parseFloat(jWeightFromTextField.getText());
			float weightTo = Float.parseFloat(jWeightToTextField.getText());

			for (MovementWard mov : listMovementWardFromTo) {
				boolean ok = true;
				Patient patient = mov.getPatient();
				Medical medical = mov.getMedical();
				Lot lot = mov.getLot();
				int age = mov.getAge();
				float weight = mov.getWeight();

				// Medical control
				if (medicalSelected != null) {
					ok = ok && medical.equals(medicalSelected);
				} else if (medicalTypeSelected != null) {
					ok = ok && medical.getType().equals(medicalTypeSelected);
				}

				// sex control if sex not 'A'
				if (sex != 'A')
					ok = ok && patient.getSex() == sex;

				// age control if ageTo > 0
				if (ageTo != 0)
					ok = ok && age >= ageFrom && age <= ageTo;

				// weight control if weightTo > 0
				if (weightTo != 0)
					ok = ok && weight >= weightFrom && weight <= weightTo;

				if (ok)
					wardOutcomes.add(mov);
			}

			Collections.reverse(wardOutcomes);
		}

		public int getRowCount() {
			if (wardOutcomes == null)
				return 0;
			return wardOutcomes.size();
		}

		public Object getValueAt(int r, int c) {
			MovementWard mov = wardOutcomes.get(r);
			if (c == -1) {
				return mov;
			}
			if (c == 0) {
				return formatDateTime(mov.getDate());
			}
			if (c == 1) {
				return mov.getDescription();
			}
			if (c == 2) {
				if (mov.isPatient())
					return mov.getAge();
				else
					return MessageBundle.getMessage("angal.common.notapplicable.txt");
			}
			if (c == 3) {
				if (mov.isPatient())
					return mov.getPatient().getSex();
				return MessageBundle.getMessage("angal.common.notapplicable.txt");
			}
			if (c == 4) {
				if (mov.isPatient()) {
					float weight = mov.getWeight();
					return weight == 0 ? MessageBundle.getMessage("angal.common.notdefined.txt") : weight;
				} else
					return MessageBundle.getMessage("angal.common.notapplicable.txt");
			}
			if (c == 5) {
				return mov.getMedical().getDescription();
			}
			if (c == 6) {
				return mov.getQuantity();
			}
			if (c == 7) {
				return mov.getUnits();
			}
			if (c == 8) {
				return mov.getLot().getCode();
			}
			if (c == 9) {
				return formatDate(mov.getLot().getDueDate());
			}
			return null;
		}

		public String getColumnName(int c) {
			return columnsOutcomes[c];
		}

		public int getColumnCount() {
			return columnsOutcomes.length;
		}

		public boolean isCellEditable(int arg0, int arg1) {
			// return super.isCellEditable(arg0, arg1);
			return false;
		}
	}

	class DrugsModel extends DefaultTableModel {

		private static final long serialVersionUID = 1L;

		private ArrayList<MedicalWard> tableModel;

		public DrugsModel() {
			try {
				tableModel = wardManager.getMedicalsWardTotalQuantity(wardSelected.getCode().charAt(0));
				wardDrugs = wardManager.getMedicalsWard(wardSelected.getCode().charAt(0), true);
			} catch (OHServiceException e) {
				OHServiceExceptionUtil.showMessages(e);
				tableModel = new ArrayList<>();
				wardDrugs = new ArrayList<>();
			}
		}

		public int getRowCount() {
			if (tableModel == null)
				return 0;
			return tableModel.size();
		}

		public Object getValueAt(final int r, int c) {
			final MedicalWard wardDrug = tableModel.get(r);
			if (c == -1) {
				return wardDrug;
			}
			if (c == 0) {
				wardDrug.getMedical();
				return wardDrug.getMedical().getDescription();
			}
			if (c == 1) {
				return wardDrug.getQty();
			}
			if (c == 2) {
				return MessageBundle.getMessage("angal.medicalstockward.pieces"); //$NON-NLS-1$
			}
			if (c == 3) {
				final JButton button = new JButton(MessageBundle.getMessage("angal.medicalstockward.rectify.btn"));
				button.setMnemonic(MessageBundle.getMnemonic("angal.medicalstockward.rectify.btn.key"));
				button.addActionListener(new ActionListener() {

					public void actionPerformed(ActionEvent arg0) {
						Medical medic = wardDrug.getMedical();
						WardPharmacyRectify wardRectify = new WardPharmacyRectify(WardPharmacy.this, wardSelected, medic);
						wardRectify.addMovementWardListener(WardPharmacy.this);
						wardRectify.setVisible(true);
						TableCellRenderer buttonRenderer = new JTableButtonRenderer();
						jTableDrugs.getColumn("").setCellRenderer(buttonRenderer);
					}
				});
				return button;
			}

			return null;
		}

		public String getColumnName(int c) {
			return columnsDrugs[c];
		}

		public int getColumnCount() {
			return columnsDrugs.length;
		}

		public boolean isCellEditable(int arg0, int arg1) {
			// return super.isCellEditable(arg0, arg1);
			return false;
		}
	}

	private JButton getJRectifyButton() {
		if (jRectifyButton == null) {
			jRectifyButton = new JButton(MessageBundle.getMessage("angal.medicalstockward.rectify.btn"));
			jRectifyButton.setMnemonic(MessageBundle.getMnemonic("angal.medicalstockward.rectify.btn.key"));
			jRectifyButton.setBackground(Color.PINK);
			jRectifyButton.setVisible(false);
			jRectifyButton.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent arg0) {

					if (jTableDrugs.getSelectedRow() < 0) {
						WardPharmacyRectify wardRectify = new WardPharmacyRectify(WardPharmacy.this, wardSelected);
						wardRectify.addMovementWardListener(WardPharmacy.this);
						wardRectify.setVisible(true);
					} else {
						int[] indexes = jTableDrugs.getSelectedRows();
						Medical medic = (((MedicalWard) jTableDrugs.getValueAt(indexes[0], -1)).getMedical());
						WardPharmacyRectify wardRectify = new WardPharmacyRectify(WardPharmacy.this, wardSelected, medic);
						wardRectify.addMovementWardListener(WardPharmacy.this);
						wardRectify.setVisible(true);
					}

					TableCellRenderer buttonRenderer = new JTableButtonRenderer();
					jTableDrugs.getColumn("").setCellRenderer(buttonRenderer);
				}
			});
		}
		return jRectifyButton;
	}

	private JButton getPrintTableButton() {
		if (jPrintTableButton == null) {
			jPrintTableButton = new JButton(MessageBundle.getMessage("angal.medicalstockward.report.btn"));
			jPrintTableButton.setMnemonic(MessageBundle.getMnemonic("angal.medicalstockward.report.btn.key"));
			jPrintTableButton.setVisible(false);
			jPrintTableButton.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent arg0) {

					if (jTabbedPaneWard.getSelectedIndex() == 0) {
						try {
							printManager.print("WardPharmacyOutcomes", wardManager.convertMovementWardForPrint(wardOutcomes), 0); //$NON-NLS-1$
						} catch (OHServiceException e) {
							OHServiceExceptionUtil.showMessages(e, WardPharmacy.this);
						}
					} else if (jTabbedPaneWard.getSelectedIndex() == 1) {
						try {
							printManager.print("WardPharmacyIncomes", wardManager.convertMovementForPrint(wardIncomes), 0); //$NON-NLS-1$
						} catch (OHServiceException e) {
							OHServiceExceptionUtil.showMessages(e, WardPharmacy.this);
						}
					} else if (jTabbedPaneWard.getSelectedIndex() == 2) {
						ArrayList<String> options = new ArrayList<>();
						options.add(MessageBundle.getMessage("angal.medicals.today")); //$NON-NLS-1$
						options.add(MessageBundle.getMessage("angal.common.date.txt"));

						Icon icon = new ImageIcon("rsc/icons/calendar_dialog.png"); //$NON-NLS-1$
						String option = (String) MessageDialog.inputDialog(WardPharmacy.this,
								icon,
								options.toArray(),
								options.get(0),
								"angal.medicals.pleaseselectareport.msg");

						if (option == null)
							return;
						int i = 0;
						if (options.indexOf(option) == i) {
							new GenericReportPharmaceuticalStockWard(null, "PharmaceuticalStockWard", wardSelected); //$NON-NLS-1$
							return;
						}
						if (options.indexOf(option) == ++i) {

							icon = new ImageIcon("rsc/icons/calendar_dialog.png"); //$NON-NLS-1$

							CustomJDateChooser dateChooser = new CustomJDateChooser();
							dateChooser.setLocale(new Locale(GeneralData.LANGUAGE));

							int r = JOptionPane.showConfirmDialog(WardPharmacy.this,
									dateChooser,
									MessageBundle.getMessage("angal.common.date.txt"),
									JOptionPane.OK_CANCEL_OPTION,
									JOptionPane.PLAIN_MESSAGE,
									icon);

							if (r == JOptionPane.OK_OPTION) {

								new GenericReportPharmaceuticalStockWard(dateChooser.getDate(), "PharmaceuticalStockWard", wardSelected); //$NON-NLS-1$
							}
						}
					}
				}
			});
		}
		return jPrintTableButton;
	}

	private JButton getExportToExcelButton() {
		if (jExportToExcelButton == null) {
			jExportToExcelButton = new JButton(MessageBundle.getMessage("angal.common.excel.btn"));
			jExportToExcelButton.setMnemonic(MessageBundle.getMnemonic("angal.common.excel.btn.key"));
			jExportToExcelButton.setVisible(false);
			jExportToExcelButton.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent arg0) {
					String fileName = compileFileName();
					File defaultFileName = new File(fileName);
					JFileChooser fcExcel = ExcelExporter.getJFileChooserExcel(defaultFileName);

					int iRetVal = fcExcel.showSaveDialog(WardPharmacy.this);
					if (iRetVal == JFileChooser.APPROVE_OPTION) {
						try {
							File exportFile = fcExcel.getSelectedFile();
							if (!exportFile.getName().endsWith("xls"))
								exportFile = new File(exportFile.getAbsoluteFile() + ".xls");

							ExcelExporter xlsExport = new ExcelExporter();
							int index = jTabbedPaneWard.getSelectedIndex();
							if (index == 0) {
								xlsExport.exportTableToExcel(jTableOutcomes, exportFile);
							} else if (index == 1) {
								xlsExport.exportTableToExcel(jTableIncomes, exportFile);
							} else if (index == 2) {
								xlsExport.exportTableToExcel(jTableDrugs, exportFile);
							}

						} catch (IOException exc) {
							JOptionPane.showMessageDialog(WardPharmacy.this,
									exc.getMessage(),
									MessageBundle.getMessage("angal.hospital"),
									JOptionPane.PLAIN_MESSAGE);
							LOGGER.info("Export to excel error : {}", exc.getMessage());
						}

					}
				}
			});
		}
		return jExportToExcelButton;
	}

	private String compileFileName() {
		StringBuilder filename = new StringBuilder(MessageBundle.getMessage("angal.wardpharmacy.stockwardledger.txt"));
		filename.append("_").append(jComboBoxWard.getSelectedItem());
		int index = jTabbedPaneWard.getSelectedIndex();
		if (index == 0) {
			filename.append("_").append(MessageBundle.getMessage("angal.medicalstockward.outcomes"));
		} else if (index == 1) {
			filename.append("_").append(MessageBundle.getMessage("angal.medicalstockward.incomings"));
		} else if (index == 2) {
			filename.append("_").append(MessageBundle.getMessage("angal.medicalstockward.drugs"));
		}
		if (jComboBoxTypes.isEnabled()
				&& !jComboBoxTypes.getSelectedItem().equals(
				MessageBundle.getMessage("angal.common.alltypes.txt"))) {

			filename.append("_").append(jComboBoxTypes.getSelectedItem());
		}
		if (jComboBoxMedicals.isEnabled()
				&& !jComboBoxMedicals.getSelectedItem().equals(
				MessageBundle.getMessage("angal.medicalstockward.allmedicals"))) {

			filename.append("_").append(jComboBoxMedicals.getSelectedItem());
		}
		filename.append("_").append(TimeTools.formatDateTime(jCalendarFrom.getDate(), "yyyyMMdd"))
				.append("_").append(TimeTools.formatDateTime(jCalendarTo.getDate(), "yyyyMMdd"));

		return filename.toString();
	}

	class CenterBoldTableCellRenderer extends DefaultTableCellRenderer {

		private static final long serialVersionUID = 1L;

		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
				boolean hasFocus, int row, int column) {

			Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			cell.setForeground(Color.BLACK);
			setHorizontalAlignment(CENTER);
			cell.setFont(new Font(null, Font.BOLD, 12));
			return cell;
		}
	}

	class BlueBoldTableCellRenderer extends DefaultTableCellRenderer {

		private static final long serialVersionUID = 1L;

		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
				boolean hasFocus, int row, int column) {

			Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			cell.setForeground(Color.BLACK);
			cell.setFont(new Font(null, Font.PLAIN, 12));
			MovementWard mov = wardOutcomes.get(row);
			if (!mov.isPatient()) {
				cell.setForeground(Color.BLUE);
				cell.setFont(new Font(null, Font.BOLD, 12));
			}
			return cell;
		}
	}

	public String formatDate(GregorianCalendar time) {
		SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy"); //$NON-NLS-1$
		return format.format(time.getTime());
	}

	public String formatDateTime(GregorianCalendar time) {
		SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss"); //$NON-NLS-1$
		return format.format(time.getTime());
	}

	private ArrayList<Medical> getSearchMedicalsResults(String s, ArrayList<Medical> medicalsList) {
		String query = s.trim();
		ArrayList<Medical> results = new ArrayList<>();
		for (Medical medoc : medicalsList) {
			if (!query.equals("")) {
				String[] patterns = query.split(" ");
				String code = medoc.getProd_code().toLowerCase();
				String description = medoc.getDescription().toLowerCase();
				boolean patternFound = false;
				for (String pattern : patterns) {
					if (code.contains(pattern.toLowerCase()) || description.contains(pattern.toLowerCase())) {
						patternFound = true;
						//It is sufficient that only one pattern matches the query
						break;
					}
				}
				if (patternFound) {
					results.add(medoc);
				}
			} else {
				results.add(medoc);
			}
		}
		return results;
	}
}
