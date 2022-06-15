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
package org.isf.visits.gui;

import static org.isf.utils.Constants.DATE_FORMAT_DD_MM_YYYY;
import static org.isf.utils.Constants.TIME_FORMAT_HH_MM;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.event.EventListenerList;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import org.isf.generaldata.GeneralData;
import org.isf.generaldata.MessageBundle;
import org.isf.menu.manager.Context;
import org.isf.patient.model.Patient;
import org.isf.stat.gui.report.WardVisitsReport;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.LocalDateSupportingJDateChooser;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.ModalJFrame;
import org.isf.utils.time.TimeTools;
import org.isf.visits.manager.VisitManager;
import org.isf.visits.model.Visit;
import org.isf.ward.manager.WardBrowserManager;
import org.isf.ward.model.Ward;

import com.toedter.calendar.JDateChooser;

/**
 * @author Mwithi
 */
public class VisitView extends ModalJFrame {

	private static final long serialVersionUID = 1L;
	private EventListenerList visitViewListeners = new EventListenerList();

	public interface VisitListener extends EventListener {

		void visitsUpdated(AWTEvent e);
	}

	public void addVisitListener(VisitListener l) {
		visitViewListeners.add(VisitListener.class, l);
	}

	private void fireVisitsUpdated() {
		AWTEvent event = new AWTEvent(VisitView.this, AWTEvent.RESERVED_ID_MAX + 1) {

			private static final long serialVersionUID = 1L;
		};

		EventListener[] listeners = visitViewListeners.getListeners(VisitListener.class);
		for (EventListener listener : listeners) {
			((VisitListener) listener).visitsUpdated(event);
		}
	}

	/*
	 * Constants
	 */
	private static final int VISIT_BUTTON_WIDTH = 200;
	private static final int ACTIONS_BUTTON_WIDTH = 240;
	private static final int ALL_BUTTON_HEIGHT = 30;
	
	/*
	 * Attributes
	 */
	private JPanel northPanel;
	private Patient patient;
	private JButton addFirstVisitButton;
	private JButton deleteFirstVisitButton;
	private JButton addSecondVisitButton;
	private JButton deleteSecondVisitButton;
	private JButton closeButton;
	private JPanel datefirstPanel;
	private JLabel dateFirstLabel;
	private JPanel datesecondPanel;
	private JLabel datesecondLabel;
	private JPanel wardPanel;
	private JButton todayButton;
	private JButton tomorrowButton;
	private JPanel dateViPanel;
	private JTable jTableFirst;
	private JScrollPane jScrollPaneFirstday;
	private JFrame owner;
	private JScrollPane jScrollPaneSecondtday;
	private JTable jTableSecond;
	private LocalDateSupportingJDateChooser visitDateChooser;
	private JButton backButton;
	private JButton nextButton;
	private JPanel todayPanel;
	private JComboBox<Ward> wardBox;
	private SpringLayout slVisitParamsPanel;
	
	public String[] visColumns = { MessageBundle.getMessage("angal.visit.visits") };

	/*
	 * Managers
	 */
	private VisitManager vstManager = Context.getApplicationContext().getBean(VisitManager.class);
	private WardBrowserManager wbm = Context.getApplicationContext().getBean(WardBrowserManager.class);

	private List<Visit> visits = new ArrayList<>();
	private List<Visit> visitfirst = new ArrayList<>();
	private List<Visit> visitSecond = new ArrayList<>();
	private Ward ward;
	private LocalDateTime dateFirst;
	private LocalDateTime dateSecond;


	private void initialize() {
		setDateFirstThenSecond(LocalDateTime.now());
	}

	private void loadDataForWard(Ward ward) {
		try {
			if (ward != null) {
				visits = vstManager.getVisitsWard(ward.getCode());
			} else {
				visits = vstManager.getVisitsWard(null);
			}
		} catch (OHServiceException e1) {
			OHServiceExceptionUtil.showMessages(e1);
		}
	}

	public VisitView(JFrame owner, Patient patient, Ward ward) {
		super();
		this.owner = owner;
		this.ward = ward;
		this.patient = patient;
		if (ward != null) {
			loadDataForWard(ward);
		}
		initialize();
		initComponents();
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setLocationRelativeTo(null);
		addWindowListener(new FreeMemoryAdapter());
		this.pack();
	}

	public VisitView() {
		initialize();
		initComponents();
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setLocationRelativeTo(null);
		setVisible(true);
		addWindowListener(new FreeMemoryAdapter());
	}

	private void initComponents() {

		setTitle(MessageBundle.getMessage("angal.visit.worksheet.title"));
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(getNorthPanel(), BorderLayout.NORTH);
		getContentPane().add(dayCalendar(), BorderLayout.CENTER);
		showGui(ward != null);

		setSize(1350, 600);
		setResizable(false);
	}

	private JPanel dayCalendar() {
		slVisitParamsPanel = new SpringLayout();
		JPanel visitParamPanel = new JPanel(slVisitParamsPanel);

		GridBagLayout gbcPanelData = new GridBagLayout();
		gbcPanelData.columnWidths = new int[] { 20, 20, 20, 0, 0, 0 };
		gbcPanelData.rowHeights = new int[] { 20, 20, 20, 0, 0, 0, 0, 0 };
		gbcPanelData.columnWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };
		gbcPanelData.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };
		visitParamPanel.setLayout(gbcPanelData);

		GridBagConstraints gbcButtonBack = new GridBagConstraints();
		gbcButtonBack.fill = GridBagConstraints.VERTICAL;
		gbcButtonBack.anchor = GridBagConstraints.WEST;
		gbcButtonBack.gridy = 1;
		gbcButtonBack.gridx = 0;
		visitParamPanel.add(getButtonBack(), gbcButtonBack);

		GridBagConstraints gbcDatelabel = new GridBagConstraints();
		gbcDatelabel.fill = GridBagConstraints.VERTICAL;
		gbcDatelabel.anchor = GridBagConstraints.WEST;
		gbcDatelabel.gridy = 0;
		gbcDatelabel.gridx = 1;
		visitParamPanel.add(getDateFirstDay(), gbcDatelabel);

		GridBagConstraints gbcDuration = new GridBagConstraints();
		gbcDuration.fill = GridBagConstraints.VERTICAL;
		gbcDuration.anchor = GridBagConstraints.WEST;
		gbcDuration.gridy = 1;
		gbcDuration.gridx = 1;
		visitParamPanel.add(getVisitFirstday(), gbcDuration);

		GridBagConstraints gbcDateSecond = new GridBagConstraints();
		gbcDateSecond.fill = GridBagConstraints.VERTICAL;
		gbcDateSecond.anchor = GridBagConstraints.WEST;
		gbcDateSecond.gridy = 0;
		gbcDateSecond.gridx = 2;
		visitParamPanel.add(getDateSecondDay(), gbcDateSecond);

		GridBagConstraints gbcButtonNext = new GridBagConstraints();
		gbcButtonNext.fill = GridBagConstraints.VERTICAL;
		gbcButtonNext.anchor = GridBagConstraints.WEST;
		gbcButtonNext.gridy = 1;
		gbcButtonNext.gridx = 3;
		visitParamPanel.add(getButtonNext(), gbcButtonNext);

		GridBagConstraints gbcSecondDay = new GridBagConstraints();
		gbcSecondDay.fill = GridBagConstraints.VERTICAL;
		gbcSecondDay.anchor = GridBagConstraints.WEST;
		gbcSecondDay.gridy = 1;
		gbcSecondDay.gridx = 2;
		visitParamPanel.add(getVisitSecondDay(), gbcSecondDay);

		GridBagConstraints gbcToday = new GridBagConstraints();
		gbcToday.fill = GridBagConstraints.CENTER;
		gbcToday.anchor = GridBagConstraints.CENTER;
		gbcToday.gridy = 2;
		gbcToday.gridx = 1;
		visitParamPanel.add(getTodayPanel(), gbcToday);

		GridBagConstraints gbcTomorrow = new GridBagConstraints();
		gbcTomorrow.fill = GridBagConstraints.CENTER;
		gbcTomorrow.anchor = GridBagConstraints.CENTER;
		gbcTomorrow.gridy = 2;
		gbcTomorrow.gridx = 2;
		visitParamPanel.add(getTomorrowPanel(), gbcTomorrow);

		GridBagConstraints gbcCloseButton = new GridBagConstraints();
		gbcCloseButton.fill = GridBagConstraints.WEST;
		gbcToday.anchor = GridBagConstraints.WEST;
		gbcCloseButton.gridy = 3;
		gbcCloseButton.gridx = 3;
		visitParamPanel.add(getCloseButton(), gbcCloseButton);

		return visitParamPanel;

	}

	private JPanel getTodayPanel() {
		JPanel firstPanel = new JPanel();
		firstPanel.add(getAddVisitFirstButton());
		firstPanel.add(getDeleteFirstVisitButton());
		firstPanel.add(getPrintTodayButton());
		return firstPanel;
	}

	private JPanel getTomorrowPanel() {
		JPanel secondPanel = new JPanel();
		secondPanel.add(getAddVisitSecondButton());
		secondPanel.add(getDeleteSecondVisitButton());
		secondPanel.add(getPrintTomorrowButton());
		return secondPanel;
	}

	private JButton getAddVisitFirstButton() {
		if (addFirstVisitButton == null) {
			addFirstVisitButton = new JButton(MessageBundle.getMessage("angal.visit.addvisit1.btn"));
			addFirstVisitButton.setMnemonic(MessageBundle.getMnemonic("angal.visit.addvisit1.btn.key"));
			addFirstVisitButton.setIcon(new ImageIcon("rsc/icons/calendar_button.png"));
			addFirstVisitButton.setMaximumSize(new Dimension(VISIT_BUTTON_WIDTH, ALL_BUTTON_HEIGHT));
			addFirstVisitButton.setHorizontalAlignment(SwingConstants.LEFT);
			addFirstVisitButton.addActionListener(actionEvent -> {

				InsertVisit newVsRow = new InsertVisit(VisitView.this, dateFirst, getWard(), patient, true);
				newVsRow.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
				newVsRow.setVisible(true);

				Visit visit = newVsRow.getVisit();
				addVisit(visit);
				newVsRow.dispose();
			});
		}
		return addFirstVisitButton;
	}
	
	private JButton getDeleteFirstVisitButton() {
		if (deleteFirstVisitButton == null) {
			deleteFirstVisitButton = new JButton(MessageBundle.getMessage("angal.visit.removevisit.btn"));
			deleteFirstVisitButton.setMaximumSize(new Dimension(VISIT_BUTTON_WIDTH, ALL_BUTTON_HEIGHT));
			deleteFirstVisitButton.setHorizontalAlignment(SwingConstants.LEFT);
			deleteFirstVisitButton.addActionListener(actionEvent -> {

				int row = jTableFirst.getSelectedRow();
				if (row < 0) {
					MessageDialog.info(VisitView.this, "angal.common.pleaseselectarow.msg");
					return;
				}
				Visit visit = (Visit) jTableFirst.getModel().getValueAt(row, -1);
				int ok = MessageDialog.okCancel(VisitView.this, "angal.visit.removevisit.msg");
				if (ok == JOptionPane.YES_OPTION) {
					vstManager.deleteVisit(visit);
					loadDataForWard(ward);
					updatePanels();
				}
			});
		}
		return deleteFirstVisitButton;
	}

	private JButton getAddVisitSecondButton() {
		if (addSecondVisitButton == null) {
			addSecondVisitButton = new JButton(MessageBundle.getMessage("angal.visit.addvisit2.btn"));
			addSecondVisitButton.setMnemonic(MessageBundle.getMnemonic("angal.visit.addvisit2.btn.key"));
			addSecondVisitButton.setIcon(new ImageIcon("rsc/icons/calendar_button.png"));
			addSecondVisitButton.setMaximumSize(new Dimension(VISIT_BUTTON_WIDTH, ALL_BUTTON_HEIGHT));
			addSecondVisitButton.setHorizontalAlignment(SwingConstants.LEFT);
			addSecondVisitButton.addActionListener(actionEvent -> {

				InsertVisit newVsRow = new InsertVisit(VisitView.this, dateSecond, getWard(), patient, true);
				newVsRow.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
				newVsRow.setVisible(true);

				Visit vsRow = newVsRow.getVisit();
				addVisit(vsRow);
				newVsRow.dispose();
			});
		}
		return addSecondVisitButton;
	}
	
	private JButton getDeleteSecondVisitButton() {
		if (deleteSecondVisitButton == null) {
			deleteSecondVisitButton = new JButton(MessageBundle.getMessage("angal.visit.removevisit.btn"));
			deleteSecondVisitButton.setMaximumSize(new Dimension(VISIT_BUTTON_WIDTH, ALL_BUTTON_HEIGHT));
			deleteSecondVisitButton.setHorizontalAlignment(SwingConstants.LEFT);
			deleteSecondVisitButton.addActionListener(actionEvent -> {

				int row = jTableSecond.getSelectedRow();
				if (row < 0) {
					MessageDialog.info(VisitView.this, "angal.common.pleaseselectarow.msg");
					return;
				}
				Visit visit = (Visit) jTableSecond.getModel().getValueAt(row, -1);
				int ok = MessageDialog.okCancel(VisitView.this, "angal.visit.removevisit.msg");
				if (ok == JOptionPane.YES_OPTION) {
					vstManager.deleteVisit(visit);
					loadDataForWard(ward);
					updatePanels();
				}
			});
		}
		return deleteSecondVisitButton;
	}

	private void addVisit(Visit vsRow) {
		if (vsRow != null && vsRow.getVisitID() != 0) {

			loadDataForWard(ward);

			if (!TimeTools.isSameDay(dateFirst, vsRow.getDate()) && !TimeTools.isSameDay(dateSecond, vsRow.getDate())) {
				// if new visit date is not already shown, change view
				setDateFirstThenSecond(vsRow.getDate());
			}

			updatePanels();
		}
	}

	private JPanel getDateFirstDay() {
		if (datefirstPanel == null) {
			datefirstPanel = new JPanel();
			dateFirstLabel = new JLabel(TimeTools.formatDateTime(dateFirst, DATE_FORMAT_DD_MM_YYYY));
			datefirstPanel.add(dateFirstLabel);
		}
		return datefirstPanel;
	}

	private JPanel getDateSecondDay() {
		if (datesecondPanel == null) {
			datesecondPanel = new JPanel();
			datesecondLabel = new JLabel(TimeTools.formatDateTime(dateSecond, DATE_FORMAT_DD_MM_YYYY));
			datesecondPanel.add(datesecondLabel);
		}
		return datesecondPanel;
	}

	private JScrollPane getVisitFirstday() {
		if (jScrollPaneFirstday == null) {
			jScrollPaneFirstday = new JScrollPane();
			jScrollPaneFirstday.setViewportView(visitFirstDayPanel());
			jScrollPaneFirstday.setAlignmentY(Component.TOP_ALIGNMENT);
			jScrollPaneFirstday.getViewport().setBackground(Color.WHITE);

			jScrollPaneFirstday.setMinimumSize(new Dimension(500, 400));
		}
		return jScrollPaneFirstday;
	}

	private JTable visitFirstDayPanel() {
		if (jTableFirst == null) {
			jTableFirst = new JTable();
			jTableFirst.setModel(new VisitModel());
			jTableFirst.setBackground(Color.white);
			jTableFirst.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

			jTableFirst.setAutoCreateColumnsFromModel(false);
			jTableFirst.getColumnModel().getColumn(0).setCellRenderer(new CenterTableCellRenderer());

		}
		return jTableFirst;
	}

	private JScrollPane getVisitSecondDay() {
		if (jScrollPaneSecondtday == null) {
			jScrollPaneSecondtday = new JScrollPane();
			jScrollPaneSecondtday.getViewport().setBackground(Color.WHITE);
			slVisitParamsPanel.putConstraint(SpringLayout.NORTH, jScrollPaneSecondtday, 0, SpringLayout.NORTH, getVisitFirstday());
			slVisitParamsPanel.putConstraint(SpringLayout.EAST, jScrollPaneSecondtday, -104, SpringLayout.WEST, getVisitFirstday());
			jScrollPaneSecondtday.setViewportView(visitSecondDayPanel());
			jScrollPaneSecondtday.setAlignmentY(Component.TOP_ALIGNMENT);
			jScrollPaneSecondtday.setMinimumSize(new Dimension(500, 400));
		}
		return jScrollPaneSecondtday;
	}

	private JTable visitSecondDayPanel() {
		if (jTableSecond == null) {
			jTableSecond = new JTable();
			jTableSecond.setBackground(Color.white);
			jTableSecond.setModel(new VisitSecondModel());
			jTableSecond.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

			jTableSecond.setAutoCreateColumnsFromModel(false);
			jTableSecond.getColumnModel().getColumn(0).setCellRenderer(new CenterTableCellRenderer());

		}
		return jTableSecond;
	}

	public JPanel getVisitDateChooserPanel() {

		if (dateViPanel == null) {
			dateViPanel = new JPanel();
			JButton gotoDateButton = new JButton(MessageBundle.getMessage("angal.visit.gotodate.btn"));
			gotoDateButton.setMnemonic(MessageBundle.getMnemonic("angal.visit.gotodate.btn.key"));
			gotoDateButton.addActionListener(actionEvent -> {
				if (visitDateChooser.getDate() != null) {
					setDateFirstThenSecond(visitDateChooser.getLocalDateTime());
					updatePanels();
				} else {
					visitDateChooser.getCalendarButton().doClick();
				}
			});

			dateViPanel.add(gotoDateButton);
			dateViPanel.add(getVisitDateChooser());
		}
		return dateViPanel;
	}

	private JDateChooser getVisitDateChooser() {
		visitDateChooser = new LocalDateSupportingJDateChooser();
		visitDateChooser.setLocale(new Locale(GeneralData.LANGUAGE));
		visitDateChooser.setDateFormatString(DATE_FORMAT_DD_MM_YYYY);
		visitDateChooser.addPropertyChangeListener("date", propertyChangeEvent -> {
			setDateFirstThenSecond(visitDateChooser.getLocalDateTime());
			updatePanels();
		});
		return visitDateChooser;
	}

	private void updatePanels() {

		visitfirst = getVisitForDate(dateFirst);
		dateFirstLabel.setText(TimeTools.formatDateTime(dateFirst, DATE_FORMAT_DD_MM_YYYY));
		addFirstVisitButton.setEnabled(dateFirst.isAfter(TimeTools.getDateToday0()));

		visitSecond = getVisitForDate(dateSecond);
		datesecondLabel.setText(TimeTools.formatDateTime(dateSecond, DATE_FORMAT_DD_MM_YYYY));
		addSecondVisitButton.setEnabled(dateSecond.isAfter(TimeTools.getDateToday0()));

		((VisitModel) jTableFirst.getModel()).fireTableDataChanged();
		jTableFirst.updateUI();
		((VisitSecondModel) jTableSecond.getModel()).fireTableDataChanged();
		jTableSecond.updateUI();

		visitDateChooser.setDate(dateFirst);
	}

	private void setDateFirstThenSecond(LocalDateTime date) {
		dateFirst = date;
		dateSecond = date.plusDays(1);
	}

	private void setDateDayAfter() {
		dateFirst = dateFirst.plusDays(1);
		dateSecond = dateFirst.plusDays(1);
	}

	private void setDateDayBefore() {
		dateSecond = dateFirst;
		dateFirst = dateFirst.minusDays(1);
	}

	private List<Visit> getVisitForDate(LocalDateTime date) {
		List<Visit> vis = new ArrayList<>();
		for (Visit visit : visits) {
			if (TimeTools.isSameDay(visit.getDate(), date)) {
				vis.add(visit);
			}
		}
		return vis;
	}

	private final class FreeMemoryAdapter extends WindowAdapter {

		@Override
		public void windowClosing(WindowEvent e) {
			// force close operation
			closeButton.doClick();

			// to free memory
			freeMemory();
		}
	}

	private void freeMemory() {
		if (visits != null) {
			visits.clear();
		}
	}

	class CenterTableCellRenderer extends DefaultTableCellRenderer {

		private static final long serialVersionUID = 1L;

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

			Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			cell.setForeground(Color.BLACK);
			setHorizontalAlignment(LEFT);
			return cell;
		}
	}

	private Object getVisitString(Visit visit, LocalDateTime localDateTime) {
		StringBuilder strBuilder = new StringBuilder();
		strBuilder.append(formatDateTime(localDateTime)).append(" - "); //$NON-NLS-1$
		strBuilder.append("(").append(MessageBundle.getMessage("angal.common.patientID")).append(": ").append(visit.getPatient().getCode()).append(") - "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		strBuilder.append(visit.getPatient().getName()).append(" - "); //$NON-NLS-1$
		strBuilder.append(visit.getService() == null || visit.getService().isEmpty() ? MessageBundle.getMessage("angal.common.notdefined.txt") : visit.getService()) //$NON-NLS-1$
						.append(" "); //$NON-NLS-1$
		strBuilder.append("(").append(visit.getDuration()).append(MessageBundle.getMessage("angal.common.minutesabbr")).append(")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		return strBuilder.toString();
	}

	class VisitModel extends DefaultTableModel {

		public VisitModel() {
		}

		@Override
		public boolean isCellEditable(int row, int column) {
			return false;
		}

		@Override
		public int getRowCount() {
			if (visitfirst == null) {
				return 0;
			}
			return visitfirst.size();
		}

		@Override
		public String getColumnName(int c) {
			return visColumns[c];
		}

		@Override
		public int getColumnCount() {
			return visColumns.length;
		}

		@Override
		public Object getValueAt(int r, int c) {
			Visit visit = visitfirst.get(r);
			if (c == -1) {
				return visit;
			}
			return getVisitString(visit, visitfirst.get(r).getDate());
		}
	}

	public String formatDateTime(LocalDateTime time) {
		return DateTimeFormatter.ofPattern(TIME_FORMAT_HH_MM).format(time);
	}

	class VisitSecondModel extends DefaultTableModel {

		public VisitSecondModel() {
		}

		@Override
		public boolean isCellEditable(int row, int column) {
			return false;
		}

		@Override
		public int getRowCount() {
			if (visitSecond == null) {
				return 0;
			}
			return visitSecond.size();
		}

		@Override
		public String getColumnName(int c) {
			return visColumns[c];
		}

		@Override
		public int getColumnCount() {
			return visColumns.length;
		}

		@Override
		public Object getValueAt(int r, int c) {
			Visit visit = visitSecond.get(r);
			if (c == -1) {
				return visit;
			}
			return getVisitString(visit, visitSecond.get(r).getDate());
		}
	}

	private JButton getPrintTodayButton() {
		if (todayButton == null) {
			todayButton = new JButton(MessageBundle.getMessage("angal.visit.printthisdaysvisits1.btn"));
			todayButton.setMnemonic(MessageBundle.getMnemonic("angal.visit.printthisdaysvisits1.btn.key"));
			todayButton.setMaximumSize(new Dimension(ACTIONS_BUTTON_WIDTH, ALL_BUTTON_HEIGHT));
			todayButton.addActionListener(actionEvent -> new WardVisitsReport(getWard().getCode(), dateFirst, GeneralData.VISITSHEET));
		}
		return todayButton;
	}

	private JButton getPrintTomorrowButton() {
		if (tomorrowButton == null) {
			tomorrowButton = new JButton(MessageBundle.getMessage("angal.visit.printthisdaysvisits2.btn"));
			tomorrowButton.setMnemonic(MessageBundle.getMnemonic("angal.visit.printthisdaysvisits2.btn.key"));
			tomorrowButton.addActionListener(actionEvent -> {
				ward = (Ward) wardBox.getSelectedItem();
				new WardVisitsReport(ward.getCode(), dateSecond, GeneralData.VISITSHEET);

			});
		}
		return tomorrowButton;
	}

	private JButton getButtonNext() {
		if (nextButton == null) {
			nextButton = new JButton(MessageBundle.getMessage("angal.visit.nextarrow.btn"));
			nextButton.setMnemonic(KeyEvent.VK_RIGHT);
			nextButton.setMaximumSize(new Dimension(ACTIONS_BUTTON_WIDTH, ALL_BUTTON_HEIGHT));
			nextButton.setHorizontalAlignment(SwingConstants.LEFT);
			nextButton.addActionListener(actionEvent -> {
				setDateDayAfter();
				updatePanels();
			});
		}
		return nextButton;
	}

	private JButton getButtonBack() {
		if (backButton == null) {
			backButton = new JButton(MessageBundle.getMessage("angal.visit.arrowprevious.btn"));
			backButton.setMnemonic(KeyEvent.VK_LEFT);
			backButton.setMaximumSize(new Dimension(ACTIONS_BUTTON_WIDTH, ALL_BUTTON_HEIGHT));
			backButton.setHorizontalAlignment(SwingConstants.LEFT);
			backButton.addActionListener(actionEvent -> {
				setDateDayBefore();
				updatePanels();
			});
		}
		return backButton;
	}

	private JButton getCloseButton() {
		if (closeButton == null) {
			closeButton = new JButton(MessageBundle.getMessage("angal.common.close.btn"));
			closeButton.setMnemonic(MessageBundle.getMnemonic("angal.common.close.btn.key"));
			closeButton.setIcon(new ImageIcon("rsc/icons/close_button.png"));
			closeButton.setMaximumSize(new Dimension(ACTIONS_BUTTON_WIDTH, ALL_BUTTON_HEIGHT));
			closeButton.setHorizontalAlignment(SwingConstants.LEFT);
			closeButton.addActionListener(actionEvent -> {
				// to free memory
				freeMemory();
				if (owner != null) {
					fireVisitsUpdated();
				}
				dispose();
			});
		}
		return closeButton;
	}

	private JPanel getNorthPanel() {
		if (northPanel == null) {
			northPanel = new JPanel(new GridLayout(1, 2));

			northPanel.add(getWardPanel());
			northPanel.add(getVisitDateChooserPanel());
			northPanel.add(getTodayVisit());
		}
		return northPanel;
	}

	private JPanel getTodayVisit() {
		if (todayPanel == null) {
			todayPanel = new JPanel();
			JButton todayBtn = new JButton(MessageBundle.getMessage("angal.visit.today.btn"));
			todayBtn.setMnemonic(MessageBundle.getMnemonic("angal.visit.today.btn.key"));
			todayBtn.addActionListener(actionEvent -> {
				setDateFirstThenSecond(LocalDateTime.now());
				updatePanels();
			});
			todayPanel.add(todayBtn);
		}
		return todayPanel;

	}

	private JPanel getWardPanel() {
		if (wardPanel == null) {
			wardPanel = new JPanel();
			wardBox = new JComboBox<>();
			wardBox.addItem(null);
			List<Ward> wardList;
			try {
				wardList = wbm.getWards();
			} catch (OHServiceException e) {
				wardList = new ArrayList<>();
				OHServiceExceptionUtil.showMessages(e);
			}
			for (Ward ward : wardList) {
				wardBox.addItem(ward);
				if (this.ward != null) {
					if (this.ward.getCode().equalsIgnoreCase(ward.getCode())) {
						wardBox.setSelectedItem(ward);
					}
				}
			}

			wardBox.addActionListener(actionEvent -> {
				
				Object selectedWard = wardBox.getSelectedItem();

				if (selectedWard instanceof Ward) {
					ward = (Ward) selectedWard;
					loadDataForWard(ward);
					showGui(true);
				}
				updatePanels();
			});
		}

		wardPanel.add(wardBox);
		wardPanel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.visit.selectaward"))); //$NON-NLS-1$

		return wardPanel;
	}

	private void showGui(boolean show) {
		getButtonBack().setVisible(show);
		getDateFirstDay().setVisible(show);
		getVisitFirstday().setVisible(show);
		getDateSecondDay().setVisible(show);
		getButtonNext().setVisible(show);
		getVisitSecondDay().setVisible(show);
		getPrintTodayButton().setVisible(show);
		getPrintTomorrowButton().setVisible(show);
		getCloseButton().setVisible(show);
		getAddVisitSecondButton().setVisible(show);
		getDeleteSecondVisitButton().setVisible(show);
		getAddVisitFirstButton().setVisible(show);
		getDeleteFirstVisitButton().setVisible(show);
		getVisitDateChooserPanel().setVisible(show);
		getTodayVisit().setVisible(show);

		// call repack, as we are showing more components
		this.pack();
		this.setLocationRelativeTo(null);
	}

	public Ward getWard() {
		return (Ward) wardBox.getSelectedItem();
	}

}
