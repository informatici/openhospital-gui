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
package org.isf.medicals.gui;

import static org.isf.utils.Constants.DATE_FORMAT_DD_MM_YYYY;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import org.isf.generaldata.GeneralData;
import org.isf.generaldata.MessageBundle;
import org.isf.medicals.gui.MedicalEdit.MedicalListener;
import org.isf.medicals.manager.MedicalBrowsingManager;
import org.isf.medicals.model.Medical;
import org.isf.medtype.manager.MedicalTypeBrowserManager;
import org.isf.medtype.model.MedicalType;
import org.isf.menu.gui.MainMenu;
import org.isf.menu.manager.Context;
import org.isf.stat.gui.report.GenericReportFromDateToDate;
import org.isf.stat.gui.report.GenericReportPharmaceuticalOrder;
import org.isf.stat.gui.report.GenericReportPharmaceuticalStock;
import org.isf.stat.gui.report.GenericReportPharmaceuticalStockCard;
import org.isf.utils.excel.ExcelExporter;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.CustomJDateChooser;
import org.isf.utils.jobjects.GoodFromDateToDateChooser;
import org.isf.utils.jobjects.JMonthYearChooser;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.ModalJFrame;
import org.isf.utils.time.TimeTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.lgooddatepicker.zinternaltools.WrapLayout;

/**
 * This class shows a complete extended list of medical drugs,
 * supplies-sundries, diagnostic kits -reagents, laboratory chemicals. It is
 * possible to filter data with a selection combo box
 * and edit-insert-delete records
 *
 * @author bob
 * 11-dic-2005
 * modified by alex:
 * - product code
 * - pieces per packet
 */
public class MedicalBrowser extends ModalJFrame implements MedicalListener {

	private static final long serialVersionUID = 1L;

	private static final Logger LOGGER = LoggerFactory.getLogger(MedicalBrowser.class);
	private static final String STR_ALL = MessageBundle.getMessage("angal.common.all.txt").toUpperCase();

	@Override
	public void medicalInserted(Medical medical) {
		pMedicals.add(0, medical);
		((MedicalBrowsingModel) table.getModel()).fireTableDataChanged();
		table.updateUI();
		if (table.getRowCount() > 0) {
			table.setRowSelectionInterval(0, 0);
		}
		repaint();
	}

	@Override
	public void medicalUpdated(AWTEvent e) {
		pMedicals.set(selectedrow, medical);
		((MedicalBrowsingModel) table.getModel()).fireTableDataChanged();
		table.updateUI();
		if ((table.getRowCount() > 0) && selectedrow > -1) {
			table.setRowSelectionInterval(selectedrow, selectedrow);
		}
		repaint();

	}

	private int selectedrow;
	private JComboBox pbox;
	private List<Medical> pMedicals;
	private String[] pColumns = {
			MessageBundle.getMessage("angal.common.type.txt").toUpperCase(),
			MessageBundle.getMessage("angal.common.code.txt").toUpperCase(),
			MessageBundle.getMessage("angal.common.description.txt").toUpperCase(),
			MessageBundle.getMessage("angal.medicals.pcsperpck.col"),      // not uppercased so column reads better
			MessageBundle.getMessage("angal.medicals.stock.col").toUpperCase(),
			MessageBundle.getMessage("angal.medicals.critlevel.col").toUpperCase(),
			MessageBundle.getMessage("angal.medicals.outofstock.col").toUpperCase()
	};
	private String[] pColumnsSorter = { "MDSRT_DESC", "MDSR_CODE", "MDSR_DESC", null, "STOCK", "MDSR_MIN_STOCK_QTI", "STOCK" };
	private boolean[] pColumnsNormalSorting = { true, true, true, true, true, true, false };
	private int[] pColumnWidth = { 100, 100, 400, 60, 60, 80, 100 };
	private boolean[] pColumnResizable = { true, true, true, true, true, true, true };
	private Medical medical;
	private DefaultTableModel model;
	private JTable table;
	private final JFrame me;

	private String pSelection;
	private JTextField searchString = null;
	protected boolean altKeyReleased = true;
	private String lastKey = "";
	private JButton buttonAMC;

	private MedicalTypeBrowserManager medicalTypeManager = Context.getApplicationContext().getBean(MedicalTypeBrowserManager.class);
	private MedicalBrowsingManager medicalBrowsingManager = Context.getApplicationContext().getBean(MedicalBrowsingManager.class);

	private void filterMedical(String key) {
		model = new MedicalBrowsingModel(key, false);
		table.setModel(model);
		searchString.requestFocus();
	}

	public MedicalBrowser() {
		me = this;
		setTitle(MessageBundle.getMessage("angal.medicals.pharmaceuticalbrowser.title"));
		setPreferredSize(new Dimension(1220, 550));
		setMinimumSize(new Dimension(940, 550));
		setContentPane(getContentpane());
		pack();
		setVisible(true);
		setLocationRelativeTo(null);
		searchString.requestFocus();
	}

	private JPanel getContentpane() {
		JPanel contentPane = new JPanel(new BorderLayout());
		contentPane.add(getScrollPane(), BorderLayout.CENTER);
		contentPane.add(getJButtonPanel(), BorderLayout.SOUTH);
		return contentPane;
	}

	private JScrollPane getScrollPane() {
		JScrollPane scrollPane = new JScrollPane(getJTable());
		int totWidth = 0;
		for (int colWidth : pColumnWidth) {
			totWidth += colWidth;
		}
		scrollPane.setPreferredSize(new Dimension(totWidth, 450));
		return scrollPane;
	}

	private JTable getJTable() {
		if (table == null) {
			model = new MedicalBrowsingModel();
			table = new JTable(model);
			table.setAutoCreateRowSorter(true);
			table.setDefaultRenderer(Object.class, new ColorTableCellRenderer());
			for (int i = 0; i < pColumnWidth.length; i++) {
				table.getColumnModel().getColumn(i).setMinWidth(pColumnWidth[i]);
				if (!pColumnResizable[i]) {
					table.getColumnModel().getColumn(i).setMaxWidth(pColumnWidth[i]);
				}
			}
		}
		return table;
	}

	private JPanel getJButtonPanel() {
		JPanel buttonPanel = new JPanel(new WrapLayout());
		buttonPanel.add(new JLabel(MessageBundle.getMessage("angal.medicals.selecttype")));
		buttonPanel.add(getComboBoxMedicalType());
		buttonPanel.add(getSearchBox());
		if (MainMenu.checkUserGrants("btnpharmaceuticalnew")) {
			buttonPanel.add(getJButtonNew());
		}
		if (MainMenu.checkUserGrants("btnpharmaceuticaledit")) {
			buttonPanel.add(getJButtonEdit());
		}
		if (MainMenu.checkUserGrants("btnpharmaceuticaldel")) {
			buttonPanel.add(getJButtonDelete());
		}
		buttonPanel.add(getJButtonReport());
		buttonPanel.add(getJButtonStock());
		buttonPanel.add(getJButtonStockCard());
		buttonPanel.add(getJButtonOrderList());
		buttonPanel.add(getJButtonExpiring());
		buttonPanel.add(getJButtonAMC());
		buttonPanel.add(getJButtonClose());
		return buttonPanel;
	}

	private JButton getJButtonAMC() {
		if (buttonAMC == null) {
			buttonAMC = new JButton(MessageBundle.getMessage("angal.medicals.averagemonthlyconsumption.btn"));
			buttonAMC.setMnemonic(MessageBundle.getMnemonic("angal.medicals.averagemonthlyconsumption.btn.key"));
			buttonAMC.addActionListener(actionEvent -> new GenericReportPharmaceuticalOrder(GeneralData.PHARMACEUTICALAMC));
		}
		return buttonAMC;
	}

	private JTextField getSearchBox() {
		if (searchString == null) {
			searchString = new JTextField();
			searchString.setColumns(15);
			searchString.addKeyListener(new KeyListener() {

				@Override
				public void keyTyped(KeyEvent e) {
					if (altKeyReleased) {
						lastKey = "";
						String s = "" + e.getKeyChar();
						if (Character.isLetterOrDigit(e.getKeyChar())) {
							lastKey = s;
						}
						filterMedical(searchString.getText());
					}
				}

				@Override
				public void keyPressed(KeyEvent e) {
					int key = e.getKeyCode();
					if (key == KeyEvent.VK_ALT) {
						altKeyReleased = false;
					}
				}

				@Override
				public void keyReleased(KeyEvent e) {
					altKeyReleased = true;
				}
			});
		}
		return searchString;
	}

	private JButton getJButtonClose() {
		JButton closeButton = new JButton(MessageBundle.getMessage("angal.common.close.btn"));
		closeButton.setMnemonic(MessageBundle.getMnemonic("angal.common.close.btn.key"));
		closeButton.addActionListener(actionEvent -> dispose());
		return closeButton;
	}

	private JButton getJButtonExpiring() {
		JButton buttonExpiring = new JButton(MessageBundle.getMessage("angal.medicals.expiring.btn"));
		buttonExpiring.setMnemonic(MessageBundle.getMnemonic("angal.medicals.expiring.btn.key"));
		buttonExpiring.addActionListener(actionEvent -> launchExpiringReport());
		return buttonExpiring;
	}

	private JButton getJButtonOrderList() {
		JButton buttonOrderList = new JButton(MessageBundle.getMessage("angal.medicals.order.btn"));
		buttonOrderList.setMnemonic(MessageBundle.getMnemonic("angal.medicals.order.btn.key"));
		buttonOrderList.addActionListener(actionEvent -> new GenericReportPharmaceuticalOrder(GeneralData.PHARMACEUTICALORDER));
		return buttonOrderList;
	}

	private JButton getJButtonStock() {
		JButton buttonStock = new JButton(MessageBundle.getMessage("angal.medicals.stock.btn"));
		buttonStock.setMnemonic(MessageBundle.getMnemonic("angal.medicals.stock.btn.key"));
		buttonStock.addActionListener(actionEvent -> {

			List<String> dateOptions = new ArrayList<>();
			dateOptions.add(MessageBundle.getMessage("angal.medicals.today"));
			dateOptions.add(MessageBundle.getMessage("angal.common.date.txt"));

			Icon icon = new ImageIcon("rsc/icons/calendar_dialog.png"); //$NON-NLS-1$
			String dateOption = (String) MessageDialog.inputDialog(MedicalBrowser.this,
					icon,
					dateOptions.toArray(),
					dateOptions.get(0),
					"angal.medicals.pleaseselectareport.msg");

			if (dateOption == null) {
				return;
			}

			List<String> lotOptions = new ArrayList<>();
			lotOptions.add(MessageBundle.getMessage("angal.medicals.onlyquantity"));
			lotOptions.add(MessageBundle.getMessage("angal.medicals.withlot"));

			String lotOption = (String) MessageDialog.inputDialog(MedicalBrowser.this,
					icon,
					lotOptions.toArray(),
					lotOptions.get(0),
					"angal.medicals.pleaseselectareport.msg");

			/* Getting Report parameters */
			String sortBy;
			String groupBy = null;
			String filter = "%" + searchString.getText() + "%";
			if (pbox.getSelectedItem() instanceof MedicalType) {
				groupBy = ((MedicalType) pbox.getSelectedItem()).getDescription();
			}
			List<?> sortedKeys = table.getRowSorter().getSortKeys();
			if (!sortedKeys.isEmpty()) {
				int sortedColumn = ((SortKey) sortedKeys.get(0)).getColumn();
				SortOrder sortedOrder = ((SortKey) sortedKeys.get(0)).getSortOrder();

				String columnName = pColumnsSorter[sortedColumn];
				String columnOrder = sortedOrder.toString().equals("ASCENDING") ? "ASC" : "DESC";
				if (!pColumnsNormalSorting[sortedColumn]) {
					columnOrder = sortedOrder.toString().equals("ASCENDING") ? "DESC" : "ASC";
				}
				if (groupBy == null) {
					groupBy = "%";
					sortBy = "MDSRT_DESC, " + columnName + " " + columnOrder;
				} else {
					sortBy = columnName + " " + columnOrder;
				}

			} else { //default values
				groupBy = "%%";
				sortBy = "MDSRT_DESC, MDSR_DESC";
			}

			String report = "";

			int i = 0;
			if (lotOptions.indexOf(lotOption) == i) {
				report = GeneralData.PHARMACEUTICALSTOCK;
			}
			if (lotOptions.indexOf(lotOption) == ++i) {
				report = GeneralData.PHARMACEUTICALSTOCKLOT;
			}
			i = 0;
			if (dateOptions.indexOf(dateOption) == i) {
				new GenericReportPharmaceuticalStock(null, report, filter, groupBy, sortBy, false);
				new GenericReportPharmaceuticalStock(null, report, filter, groupBy, sortBy, true);
				return;
			}
			if (dateOptions.indexOf(dateOption) == ++i) {

				icon = new ImageIcon("rsc/icons/calendar_dialog.png"); //$NON-NLS-1$

				CustomJDateChooser dateChooser = new CustomJDateChooser();
				dateChooser.setLocale(new Locale(GeneralData.LANGUAGE));

				int r = JOptionPane.showConfirmDialog(MedicalBrowser.this,
						dateChooser,
						MessageBundle.getMessage("angal.common.date.txt"),
						JOptionPane.OK_CANCEL_OPTION,
						JOptionPane.PLAIN_MESSAGE,
						icon);

				if (r == JOptionPane.OK_OPTION) {
					new GenericReportPharmaceuticalStock(dateChooser.getLocalDateTime(), report, filter, groupBy, sortBy, false);
					new GenericReportPharmaceuticalStock(dateChooser.getLocalDateTime(), report, filter, groupBy, sortBy, true);
				}
			}
		});
		return buttonStock;
	}

	private JButton getJButtonStockCard() {
		JButton buttonStockCard = new JButton(MessageBundle.getMessage("angal.common.stockcard.btn"));
		buttonStockCard.setMnemonic(MessageBundle.getMnemonic("angal.common.stockcard.btn.key"));
		buttonStockCard.addActionListener(actionEvent -> {
			if (table.getSelectedRow() < 0) {
				MessageDialog.error(MedicalBrowser.this, "angal.common.pleaseselectarow.msg");
			} else {
				selectedrow = table.convertRowIndexToModel(table.getSelectedRow());
				Medical medical = (Medical) (((MedicalBrowsingModel) model).getValueAt(selectedrow, -1));

				// Select Dates
				GoodFromDateToDateChooser dataRange = new GoodFromDateToDateChooser(MedicalBrowser.this);
				dataRange.setTitle(MessageBundle.getMessage("angal.messagedialog.question.title"));
				dataRange.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
				dataRange.setVisible(true);

				LocalDate dateFrom = dataRange.getDateFrom();
				LocalDate dateTo = dataRange.getDateTo();
				boolean toExcel = dataRange.isExcel();

				if (!dataRange.isCancel()) {
					new GenericReportPharmaceuticalStockCard("ProductLedger", dateFrom.atStartOfDay(), dateTo.atTime(LocalTime.MAX), medical,
							null, toExcel);
				}
			}
		});
		return buttonStockCard;
	}

	private JButton getJButtonReport() {
		JButton buttonExport = new JButton(MessageBundle.getMessage("angal.medicals.export.btn"));
		buttonExport.setMnemonic(MessageBundle.getMnemonic("angal.medicals.export.btn.key"));
		buttonExport.addActionListener(actionEvent -> {

			String fileName = compileFileName();
			File defaultFileName = new File(fileName);
			JFileChooser fcExcel = ExcelExporter.getJFileChooserExcel(defaultFileName);

			int iRetVal = fcExcel.showSaveDialog(MedicalBrowser.this);
			if (iRetVal == JFileChooser.APPROVE_OPTION) {
				File exportFile = fcExcel.getSelectedFile();
				if (!exportFile.getName().endsWith("xls")) {
					exportFile = new File(exportFile.getAbsoluteFile() + ".xls");
				}
				ExcelExporter xlsExport = new ExcelExporter();
				try {
					xlsExport.exportTableToExcel(table, exportFile);
				} catch (IOException exc) {
					JOptionPane.showMessageDialog(MedicalBrowser.this,
							exc.getMessage(),
							MessageBundle.getMessage("angal.messagedialog.error.title"),
							JOptionPane.PLAIN_MESSAGE);
					LOGGER.error("Export to excel error : {}", exc.getMessage());
				}
			}
		});
		return buttonExport;
	}

	private String compileFileName() {
		StringBuilder filename = new StringBuilder(MessageBundle.getMessage("angal.medicals.stock.txt"));
		if (pbox.isEnabled()
				&& !pbox.getSelectedItem().equals(
				MessageBundle.getMessage("angal.common.all.txt").toUpperCase())) {

			filename.append("_").append(pbox.getSelectedItem());
		}
		return filename.toString();
	}

	private JButton getJButtonDelete() {
		JButton buttonDelete = new JButton(MessageBundle.getMessage("angal.common.delete.btn"));
		buttonDelete.setMnemonic(MessageBundle.getMnemonic("angal.common.delete.btn.key"));
		buttonDelete.addActionListener(actionEvent -> {
			if (table.getSelectedRow() < 0) {
				MessageDialog.error(MedicalBrowser.this, "angal.common.pleaseselectarow.msg");
			} else {
				selectedrow = table.convertRowIndexToModel(table.getSelectedRow());
				Medical med = (Medical) (((MedicalBrowsingModel) model).getValueAt(selectedrow, -1));
				int answer = MessageDialog.yesNo(MedicalBrowser.this, "angal.medicals.deletemedical.fmt.msg", med.getDescription());
				if (answer == JOptionPane.YES_OPTION) {
					boolean deleted;
					try {
						deleted = medicalBrowsingManager.deleteMedical(med);
					} catch (OHServiceException e) {
						deleted = false;
						OHServiceExceptionUtil.showMessages(e);
					}
					if (deleted) {
						pMedicals.remove(selectedrow);
						model.fireTableDataChanged();
						table.updateUI();
					}
				}
			}
		});
		return buttonDelete;
	}

	private JButton getJButtonEdit() {
		JButton buttonEdit = new JButton(MessageBundle.getMessage("angal.common.edit.btn"));
		buttonEdit.setMnemonic(MessageBundle.getMnemonic("angal.common.edit.btn.key"));
		buttonEdit.addActionListener(actionEvent -> {
			if (table.getSelectedRow() < 0) {
				MessageDialog.error(MedicalBrowser.this, "angal.common.pleaseselectarow.msg");
			} else {
				selectedrow = table.convertRowIndexToModel(table.getSelectedRow());
				medical = (Medical) (((MedicalBrowsingModel) model).getValueAt(selectedrow, -1));
				MedicalEdit editrecord = new MedicalEdit(medical, false, me);
				editrecord.addMedicalListener(MedicalBrowser.this);
				editrecord.setVisible(true);
			}
		});
		return buttonEdit;
	}

	private JButton getJButtonNew() {
		JButton buttonNew = new JButton(MessageBundle.getMessage("angal.common.new.btn"));
		buttonNew.setMnemonic(MessageBundle.getMnemonic("angal.common.new.btn.key"));
		buttonNew.addActionListener(actionEvent -> {
			// medical will reference the new record
			medical = new Medical(null, new MedicalType("", ""), "", "", 0, 0, 0, 0, 0);
			MedicalEdit newrecord = new MedicalEdit(medical, true, me);
			newrecord.addMedicalListener(MedicalBrowser.this);
			newrecord.setVisible(true);
		});
		return buttonNew;
	}

	private JComboBox getComboBoxMedicalType() {
		if (pbox == null) {
			pbox = new JComboBox();
			pbox.addItem(MessageBundle.getMessage("angal.common.all.txt").toUpperCase());
			List<MedicalType> type;
			try {
				type = medicalTypeManager.getMedicalType();
				for (MedicalType elem : type) {
					pbox.addItem(elem);
				}
			} catch (OHServiceException e) {
				OHServiceExceptionUtil.showMessages(e);
			}
		}
		pbox.addActionListener(actionEvent -> {
			pSelection = pbox.getSelectedItem().toString();
			if (pSelection.compareTo(STR_ALL) == 0) {
				model = new MedicalBrowsingModel();
			} else {
				model = new MedicalBrowsingModel(pSelection, true);
			}
			table.setModel(model);
			model.fireTableDataChanged();
			table.updateUI();
		});
		return pbox;
	}

	protected void launchExpiringReport() {

		List<String> options = new ArrayList<>();
		options.add(MessageBundle.getMessage("angal.medicals.today"));
		options.add(MessageBundle.getMessage("angal.medicals.thismonth"));
		options.add(MessageBundle.getMessage("angal.medicals.nextmonth"));
		options.add(MessageBundle.getMessage("angal.medicals.nexttwomonths"));
		options.add(MessageBundle.getMessage("angal.medicals.nextthreemonths"));
		options.add(MessageBundle.getMessage("angal.medicals.othermonth"));

		Icon icon = new ImageIcon("rsc/icons/calendar_dialog.png"); //$NON-NLS-1$
		String option = (String) MessageDialog.inputDialog(MedicalBrowser.this,
				icon,
				options.toArray(),
				options.get(0),
				"angal.medicals.pleaseselectperiod.msg");

		if (option == null) {
			return;
		}

		String from = null;
		String to = null;

		int i = 0;

		if (options.indexOf(option) == i) {
			from = TimeTools.formatDateTime(LocalDateTime.now(), DATE_FORMAT_DD_MM_YYYY);
			to = from;
		}
		if (options.indexOf(option) == ++i) {
			//this month
			LocalDate gc = getFromDate();
			from = TimeTools.formatDateTime(gc.atStartOfDay(), DATE_FORMAT_DD_MM_YYYY);

			LocalDate toDate = getToDatePlusMonth(0);
			to = TimeTools.formatDateTime(toDate.atTime(LocalTime.MAX), DATE_FORMAT_DD_MM_YYYY);
		}
		if (options.indexOf(option) == ++i) {
			from = TimeTools.formatDateTime(getFromDate().atStartOfDay(), DATE_FORMAT_DD_MM_YYYY);
			//next month
			to = TimeTools.formatDateTime(getToDatePlusMonth(1).atTime(LocalTime.MAX), DATE_FORMAT_DD_MM_YYYY);
		}
		if (options.indexOf(option) == ++i) {
			from = TimeTools.formatDateTime(getFromDate().atStartOfDay(), DATE_FORMAT_DD_MM_YYYY);
			//next two month
			to = TimeTools.formatDateTime(getToDatePlusMonth(2).atTime(LocalTime.MAX), DATE_FORMAT_DD_MM_YYYY);
		}
		if (options.indexOf(option) == ++i) {
			from = TimeTools.formatDateTime(getFromDate().atStartOfDay(), DATE_FORMAT_DD_MM_YYYY);
			//next three month
			to = TimeTools.formatDateTime(getToDatePlusMonth(3).atTime(LocalTime.MAX), DATE_FORMAT_DD_MM_YYYY);
		}
		if (options.indexOf(option) == ++i) {
			LocalDate monthYear;
			icon = new ImageIcon("rsc/icons/calendar_dialog.png"); //$NON-NLS-1$
			JMonthYearChooser monthYearChooser = new JMonthYearChooser();
			int r = JOptionPane.showConfirmDialog(MedicalBrowser.this,
					monthYearChooser,
					MessageBundle.getMessage("angal.billbrowser.month.txt"),
					JOptionPane.OK_CANCEL_OPTION,
					JOptionPane.PLAIN_MESSAGE,
					icon);

			if (r == JOptionPane.OK_OPTION) {
				monthYear = monthYearChooser.getLocalDate();
			} else {
				return;
			}

			LocalDate fromDate = getFromDate();
			from = TimeTools.formatDateTime(fromDate.atStartOfDay(), DATE_FORMAT_DD_MM_YYYY);
			LocalDate toDate = monthYear;
			toDate = toDate.with (TemporalAdjusters.lastDayOfMonth());
			to = TimeTools.formatDateTime(toDate.atTime(LocalTime.MAX), DATE_FORMAT_DD_MM_YYYY);
		}

		new GenericReportFromDateToDate(
				from,
				to,
				"PharmaceuticalExpiration",
				MessageBundle.getMessage("angal.medicals.expiringreport"),
				false);
	}

	private LocalDate getToDatePlusMonth(int monthsToMove) {
		LocalDate plusMonth = LocalDate.now().plusMonths(monthsToMove);
		return plusMonth.withDayOfMonth(plusMonth.lengthOfMonth());
	}

	private LocalDate getFromDate() {
		return LocalDate.now().withDayOfMonth(1);
	}

	class MedicalBrowsingModel extends DefaultTableModel {

		private static final long serialVersionUID = 1L;

		List<Medical> medicalList = new ArrayList<>();

		public MedicalBrowsingModel(String key, boolean isType) {
			if (isType) {
				try {
					medicalList = pMedicals = medicalBrowsingManager.getMedicals(key, false);
				} catch (OHServiceException e) {
					pMedicals = null;
					OHServiceExceptionUtil.showMessages(e);
				}
			} else {
				for (Medical med : pMedicals) {
					if (key != null) {

						String s = key + lastKey;
						s = s.trim();
						String[] tokens = s.split(" ");

						if (!s.equals("")) {
							String description = med.getProdCode() + med.getDescription();
							int a = 0;
							for (String value : tokens) {
								String token = value.toLowerCase();
								if (description.toLowerCase().contains(token)) {
									a++;
								}
							}
							if (a == tokens.length) {
								medicalList.add(med);
							}
						} else {
							medicalList.add(med);
						}
					} else {
						medicalList.add(med);
					}
				}
			}
		}

		public MedicalBrowsingModel() {
			try {
				medicalList = pMedicals = medicalBrowsingManager.getMedicals(null, false);
			} catch (OHServiceException e) {
				pMedicals = null;
				OHServiceExceptionUtil.showMessages(e);
			}
		}

		@Override
		public Class<?> getColumnClass(int c) {
			if (c == 0) {
				return String.class;
			} else if (c == 1) {
				return String.class;
			} else if (c == 2) {
				return String.class;
			} else if (c == 3) {
				return Integer.class;
			} else if (c == 4) {
				return Double.class;
			} else if (c == 5) {
				return Double.class;
			} else if (c == 6) {
				return Boolean.class;
			}
			return null;
		}

		@Override
		public int getRowCount() {
			if (medicalList == null) {
				return 0;
			}
			return medicalList.size();
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
			Medical med = medicalList.get(r);
			double actualQty = med.getInitialqty() + med.getInqty() - med.getOutqty();
			double minQuantity = med.getMinqty();
			if (c == -1) {
				return med;
			} else if (c == 0) {
				return med.getType().getDescription();
			} else if (c == 1) {
				return med.getProdCode();
			} else if (c == 2) {
				return med.getDescription();
			} else if (c == 3) {
				return med.getPcsperpck();
			} else if (c == 4) {
				return actualQty;
			} else if (c == 5) {
				return minQuantity;
			} else if (c == 6) {
				return actualQty == 0;
			}
			return null;
		}

		@Override
		public boolean isCellEditable(int arg0, int arg1) {
			return false;
		}

	}

	class ColorTableCellRenderer extends DefaultTableCellRenderer {

		private static final long serialVersionUID = 1L;

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
				boolean hasFocus, int row, int column) {
			Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			cell.setForeground(Color.BLACK);
			Medical med = pMedicals.get(row);
			double actualQty = med.getInitialqty() + med.getInqty() - med.getOutqty();
			if ((Boolean) table.getValueAt(row, 6)) {
				cell.setForeground(Color.GRAY); // out of stock
			}
			if (med.getMinqty() != 0 && actualQty <= med.getMinqty()) {
				cell.setForeground(Color.RED); // under critical level
			}
			return cell;
		}
	}

}
