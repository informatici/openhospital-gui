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
package org.isf.visits.gui;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.EventListener;
import java.util.GregorianCalendar;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
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

/**
 * This code was edited or generated using CloudGarden's Jigloo SWT/Swing GUI
 * Builder, which is free for non-commercial use. If Jigloo is being used
 * commercially (ie, by a corporation, company or business for any purpose
 * whatever) then you should purchase a license for each developer using Jigloo.
 * Please visit www.cloudgarden.com for details. Use of Jigloo implies
 * acceptance of these licensing terms. A COMMERCIAL LICENSE HAS NOT BEEN
 * PURCHASED FOR THIS MACHINE, SO JIGLOO OR THIS CODE CANNOT BE USED LEGALLY FOR
 * ANY CORPORATE OR COMMERCIAL PURPOSE.
 */
public class VisitView extends ModalJFrame {

	/**
	 * 
	 */
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

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;
		};

		EventListener[] listeners = visitViewListeners.getListeners(VisitListener.class);
		for (int i = 0; i < listeners.length; i++)
			((VisitListener) listeners[i]).visitsUpdated(event);
	}

	private JPanel northPanel;
	private Patient patient;
	private JButton addFirstVisitButton;
	private JButton addScondVisitButton;
	private JButton closeButton;
	// private JButton reportButton; TODO to enable when a report will be
	// designed

	private static final int VisitButtonWidth = 200;
	private static final int ActionsButtonWidth = 240;
	private static final int AllButtonHeight = 30;

	private VisitManager vstManager = Context.getApplicationContext().getBean(VisitManager.class);
	private WardBrowserManager wbm = Context.getApplicationContext().getBean(WardBrowserManager.class);

	private ArrayList<Visit> visits = new ArrayList<Visit>();
	private Ward ward;
	private JPanel wardPanel;
	private JButton todayButton;
	private JButton tomorrowButton;
	private JPanel dateViPanel;
	private JButton dateAdm;

	private JTable jTableFirst;

	private JScrollPane jScrollPaneFirstday;

	private JFrame owner;

	private void initialize() {
		setDateFirstThenSecond(new Date());

	}

	private void loadDataForWard(Ward ward) {
		try {
			if (ward != null)
				visits = vstManager.getVisitsWard(ward.getCode());
			else
				visits = vstManager.getVisitsWard(null);
		} catch (OHServiceException e1) {
			OHServiceExceptionUtil.showMessages(e1);
		}
	}

	public VisitView(JFrame owner, Patient patient, Ward ward) {
		super();
		this.owner = owner;
		this.ward = ward;
		this.patient = patient;
		if (ward != null)
			loadDataForWard(ward);
		initialize();
		initComponents();
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setLocationRelativeTo(null);
		addWindowListener(new FreeMemoryAdapter());
	}

	public VisitView() {
		initialize();
		initComponents();
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setLocationRelativeTo(null);
		setVisible(true);
		addWindowListener(new FreeMemoryAdapter());
	}

	private void initComponents() {

		setTitle(MessageBundle.getMessage("angal.visit.worksheet"));
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(getNorthPanel(), BorderLayout.NORTH);
		getContentPane().add(dayCalendar(), BorderLayout.CENTER);
		showGui(ward != null);

		setSize(1350, 600);
	}

	private JPanel dayCalendar() {
		sl_visitParamsPanel = new SpringLayout();
		JPanel visitParamPanel = new JPanel(sl_visitParamsPanel);

		GridBagLayout gbl_jPanelData = new GridBagLayout();
		gbl_jPanelData.columnWidths = new int[] { 20, 20, 20, 0, 0, 00 };
		gbl_jPanelData.rowHeights = new int[] { 20, 20, 20, 0, 0, 0, 0, 0 };
		gbl_jPanelData.columnWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };
		gbl_jPanelData.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };
		visitParamPanel.setLayout(gbl_jPanelData);

		GridBagConstraints gbc_buttonback = new GridBagConstraints();
		gbc_buttonback.fill = GridBagConstraints.VERTICAL;
		gbc_buttonback.anchor = GridBagConstraints.WEST;
		gbc_buttonback.gridy = 1;
		gbc_buttonback.gridx = 0;
		visitParamPanel.add(getButtonBack(), gbc_buttonback);

		GridBagConstraints gbc_datelabel = new GridBagConstraints();
		gbc_datelabel.fill = GridBagConstraints.VERTICAL;
		gbc_datelabel.anchor = GridBagConstraints.WEST;
		gbc_datelabel.gridy = 0;
		gbc_datelabel.gridx = 1;
		visitParamPanel.add(getDateFirstDay(), gbc_datelabel);

		GridBagConstraints gbc_Duration = new GridBagConstraints();
		gbc_Duration.fill = GridBagConstraints.VERTICAL;
		gbc_Duration.anchor = GridBagConstraints.WEST;
		gbc_Duration.gridy = 1;
		gbc_Duration.gridx = 1;
		visitParamPanel.add(getVisitFirstday(), gbc_Duration);

		GridBagConstraints gbc_datesecond = new GridBagConstraints();
		gbc_datesecond.fill = GridBagConstraints.VERTICAL;
		gbc_datesecond.anchor = GridBagConstraints.WEST;
		gbc_datesecond.gridy = 0;
		gbc_datesecond.gridx = 2;
		visitParamPanel.add(getDateSecondDay(), gbc_datesecond);

		GridBagConstraints gbc_buttonnext = new GridBagConstraints();
		gbc_buttonnext.fill = GridBagConstraints.VERTICAL;
		gbc_buttonnext.anchor = GridBagConstraints.WEST;
		gbc_buttonnext.gridy = 1;
		gbc_buttonnext.gridx = 3;
		visitParamPanel.add(getButtonNext(), gbc_buttonnext);

		GridBagConstraints gbc_date = new GridBagConstraints();
		gbc_date.fill = GridBagConstraints.VERTICAL;
		gbc_date.anchor = GridBagConstraints.WEST;
		gbc_date.gridy = 1;
		gbc_date.gridx = 2;
		visitParamPanel.add(getVisitSecondDay(), gbc_date);

		GridBagConstraints gbc_printfirst = new GridBagConstraints();
		gbc_printfirst.fill = GridBagConstraints.CENTER;
		gbc_printfirst.anchor = GridBagConstraints.CENTER;
		gbc_printfirst.gridy = 2;
		gbc_printfirst.gridx = 1;
		visitParamPanel.add(getTodayPanel(), gbc_printfirst);

		GridBagConstraints gbc_printsecond = new GridBagConstraints();
		gbc_printsecond.fill = GridBagConstraints.CENTER;
		gbc_printsecond.anchor = GridBagConstraints.CENTER;
		gbc_printsecond.gridy = 2;
		gbc_printsecond.gridx = 2;
		visitParamPanel.add(getTomorrowPanel(), gbc_printsecond);

		GridBagConstraints gbc_close = new GridBagConstraints();
		gbc_close.fill = GridBagConstraints.WEST;
		gbc_printfirst.anchor = GridBagConstraints.WEST;
		gbc_close.gridy = 3;
		gbc_close.gridx = 3;
		visitParamPanel.add(getCloseButton(), gbc_close);

		return visitParamPanel;

	}

	private JPanel getTodayPanel() {
		JPanel firstPanel = new JPanel();
		firstPanel.add(getAddVisitFirstButton());
		firstPanel.add(getPrintTodayButton());
		return firstPanel;
	}

	private JPanel getTomorrowPanel() {
		JPanel secondPanel = new JPanel();
		secondPanel.add(getAddVisitSecondButton());
		secondPanel.add(getPrintTomorrowButton());
		return secondPanel;
	}

	private JButton getAddVisitFirstButton() {
		if (addFirstVisitButton == null) {
			addFirstVisitButton = new JButton(MessageBundle.getMessage("angal.therapy.addvisit")); //$NON-NLS-1$
			addFirstVisitButton.setIcon(new ImageIcon("rsc/icons/calendar_button.png"));
			addFirstVisitButton.setMnemonic(KeyEvent.VK_1);
			addFirstVisitButton.setMaximumSize(new Dimension(VisitButtonWidth, AllButtonHeight));
			addFirstVisitButton.setHorizontalAlignment(SwingConstants.LEFT);
			addFirstVisitButton.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {

					InsertVisit newVsRow = new InsertVisit(VisitView.this, dateFirst, getWard(), patient);
					newVsRow.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
					newVsRow.setVisible(true);

					Visit visit = newVsRow.getVisit();
					addVisit(visit);
					newVsRow.dispose();
				}
			});
		}
		return addFirstVisitButton;
	}

	private JButton getAddVisitSecondButton() {
		if (addScondVisitButton == null) {
			addScondVisitButton = new JButton(MessageBundle.getMessage("angal.therapy.addvisit")); //$NON-NLS-1$
			addScondVisitButton.setIcon(new ImageIcon("rsc/icons/calendar_button.png"));
			addScondVisitButton.setMnemonic(KeyEvent.VK_V);
			addScondVisitButton.setMaximumSize(new Dimension(VisitButtonWidth, AllButtonHeight));
			addScondVisitButton.setHorizontalAlignment(SwingConstants.LEFT);
			addScondVisitButton.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {

					InsertVisit newVsRow = new InsertVisit(VisitView.this, dateSecond, getWard(), patient);
					newVsRow.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
					newVsRow.setVisible(true);

					Visit vsRow = newVsRow.getVisit();
					addVisit(vsRow);
					newVsRow.dispose();
				}
			});
		}
		return addScondVisitButton;
	}

	private void addVisit(Visit vsRow) {
		if (vsRow != null && vsRow.getVisitID() != 0) {

			visits.add(vsRow); // FOR GUI

			if (!TimeTools.isSameDay(dateFirst, vsRow.getDate().getTime()) && !TimeTools.isSameDay(dateSecond, vsRow.getDate().getTime()))
				// if new visit date is not already shown, change view
				setDateFirstThenSecond(vsRow.getDate().getTime());

			updatePanels();
		}
	}

	private JPanel datefirstPanel;
	private Date dateFirst;
	private JLabel dateFirstLabel;

	private JPanel getDateFirstDay() {
		if (datefirstPanel == null) {
			datefirstPanel = new JPanel();
			dateFirstLabel = new JLabel(TimeTools.formatDateTime(dateFirst, dateFormat));
			datefirstPanel.add(dateFirstLabel);
		}
		return datefirstPanel;
	}

	private JPanel datesecondPanel;
	private Date dateSecond;
	private JLabel datesecondLabel;

	private JPanel getDateSecondDay() {
		if (datesecondPanel == null) {
			datesecondPanel = new JPanel();
			datesecondLabel = new JLabel(TimeTools.formatDateTime(dateSecond, dateFormat));
			datesecondPanel.add(datesecondLabel);
		}
		return datesecondPanel;
	}

	private JScrollPane getVisitFirstday() {
		if (jScrollPaneFirstday == null) {
			jScrollPaneFirstday = new JScrollPane();
			jScrollPaneFirstday.setViewportView(visitFirstDayPanel());
			jScrollPaneFirstday.setAlignmentY(Box.TOP_ALIGNMENT);
			jScrollPaneFirstday.getViewport().setBackground(Color.WHITE);

			jScrollPaneFirstday.setMinimumSize(new Dimension(500, 400));
		}
		return jScrollPaneFirstday;
	}

	private int[] visColumsWidth = { 500, 350 };
	private boolean[] visColumsResizable = { false, true };
	private ArrayList<Visit> visitfirst = new ArrayList<Visit>();

	private JScrollPane jScrollPaneSecondtday;

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
			sl_visitParamsPanel.putConstraint(SpringLayout.NORTH, jScrollPaneSecondtday, 0, SpringLayout.NORTH, getVisitFirstday());
			sl_visitParamsPanel.putConstraint(SpringLayout.EAST, jScrollPaneSecondtday, -104, SpringLayout.WEST, getVisitFirstday());
			jScrollPaneSecondtday.setViewportView(visitSecondDayPanel());
			jScrollPaneSecondtday.setAlignmentY(Box.TOP_ALIGNMENT);
			jScrollPaneSecondtday.setMinimumSize(new Dimension(500, 400));
		}
		return jScrollPaneSecondtday;
	}

	private ArrayList<Visit> visitSecond = new ArrayList<Visit>();
	private JTable jTableSecond;

	private JTable visitSecondDayPanel() {
		if (jTableSecond == null) {
			jTableSecond = new JTable();
			jTableSecond.setBackground(Color.white);
			jTableSecond.setModel(new VisitSecondModel());
			jTableSecond.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			for (int i = 0; i < visColums.length; i++) {
				jTableFirst.getColumnModel().getColumn(i).setMinWidth(visColumsWidth[i]);
				if (!visColumsResizable[i])
					jTableFirst.getColumnModel().getColumn(i).setMaxWidth(visColumsWidth[i]);
			}
			jTableSecond.setAutoCreateColumnsFromModel(false);
			jTableSecond.getColumnModel().getColumn(0).setCellRenderer(new CenterTableCellRenderer());

		}
		return jTableSecond;
	}

	public JPanel getVisitDateChooserPanel() {

		if (dateViPanel == null) {

			dateViPanel = new JPanel();

			dateAdm = new JButton();
			dateAdm.setText(MessageBundle.getMessage("angal.visit.gotodate") + ":"); //$NON-NLS-1$
			dateAdm.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					if (visitDateChooser.getDate() != null) {
						setDateFirstThenSecond(visitDateChooser.getDate());
						updatePanels();
					} else {
						visitDateChooser.getCalendarButton().doClick();
					}
				}
			});

			dateViPanel.add(dateAdm);
			dateViPanel.add(getVisitDateChooser());

		}
		return dateViPanel;
	}

	private JDateChooser visitDateChooser;
	private final String dateFormat = "dd/MM/yyyy";

	private JDateChooser getVisitDateChooser() {
		visitDateChooser = new JDateChooser();
		visitDateChooser.setLocale(new Locale(GeneralData.LANGUAGE));
		visitDateChooser.setDateFormatString(dateFormat);
		visitDateChooser.addPropertyChangeListener("date", new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				setDateFirstThenSecond(visitDateChooser.getDate());
				updatePanels();

			}
		});
		return visitDateChooser;
	}

	private void updatePanels() {

		visitfirst = getVisitForDate(dateFirst);
		dateFirstLabel.setText(TimeTools.formatDateTime(dateFirst, dateFormat));

		visitSecond = getVisitForDate(dateSecond);
		datesecondLabel.setText(TimeTools.formatDateTime(dateSecond, dateFormat));

		((VisitModel) jTableFirst.getModel()).fireTableDataChanged();
		jTableFirst.updateUI();
		((VisitSecondModel) jTableSecond.getModel()).fireTableDataChanged();
		jTableSecond.updateUI();
	}

	private void setDateFirstThenSecond(Date date) {
		dateFirst = date;
		Calendar c = Calendar.getInstance();
		c.setTime(dateFirst);
		c.add(Calendar.DATE, 1);
		dateSecond = c.getTime();
	}

	private void setDateSecondThenFirst(Date date) {
		dateSecond = date;
		Calendar c = Calendar.getInstance();
		c.setTime(dateSecond);
		c.add(Calendar.DATE, -1);
		dateFirst = c.getTime();
	}

	private void setDateDayAfter() {
		Calendar c = Calendar.getInstance();
		c.setTime(dateFirst);
		c.add(Calendar.DATE, 1);
		dateFirst = c.getTime();
		c.add(Calendar.DATE, 1);
		dateSecond = c.getTime();
	}

	private void setDateDayBefore() {
		Calendar c = Calendar.getInstance();
		c.setTime(dateFirst);
		dateSecond = c.getTime();
		c.add(Calendar.DATE, -1);
		dateFirst = c.getTime();
	}

	private ArrayList<Visit> getVisitForDate(Date date) {
		ArrayList<Visit> vis = new ArrayList<Visit>();
		for (int i = 0; i < visits.size(); i++) {
			Visit visit = visits.get(i);

			if (TimeTools.isSameDay(visit.getDate().getTime(), date)) {
				vis.add(visit);
			}
		}
		return vis;
	}

	private final class FreeMemoryAdapter extends WindowAdapter {

		public void windowClosing(WindowEvent e) {
			// force close operation
			closeButton.doClick();

			// to free memory
			freeMemory();
		}
	}

	private void freeMemory() {
		if (visits != null)
			visits.clear();
	}

	class CenterTableCellRenderer extends DefaultTableCellRenderer {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

			Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			cell.setForeground(Color.BLACK);
			setHorizontalAlignment(LEFT);
			return cell;
		}
	}

	public String[] visColums = { MessageBundle.getMessage("angal.visit.visits"), //$NON-NLS-1$
	};
	private SpringLayout sl_visitParamsPanel;

	private JButton backButton;

	private JButton nextButton;

	private Object getVisitString(Visit visit, GregorianCalendar d) {
		StringBuilder strBuilder = new StringBuilder();
		strBuilder.append(formatDateTime(d)).append(" - "); //$NON-NLS-1$
		strBuilder.append("(").append(MessageBundle.getMessage("angal.common.patientID")).append(": ").append(visit.getPatient().getCode()).append(") - "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		strBuilder.append(visit.getPatient().getName()).append(" - "); //$NON-NLS-1$
		strBuilder.append(visit.getService() == null || visit.getService().isEmpty() ? MessageBundle.getMessage("angal.common.notdefined") : visit.getService()) //$NON-NLS-1$
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

		public int getRowCount() {
			if (visitfirst == null)
				return 0;
			return visitfirst.size();
		}

		public String getColumnName(int c) {
			return visColums[c];
		}

		public int getColumnCount() {
			return visColums.length;
		}

		public Object getValueAt(int r, int c) {
			Visit visit = visitfirst.get(r);
			GregorianCalendar d = visitfirst.get(r).getDate();
			return getVisitString(visit, d);
		}
	}

	public String formatDateTime(GregorianCalendar time) {
		SimpleDateFormat format = new SimpleDateFormat("HH:mm"); //$NON-NLS-1$
		return format.format(time.getTime());
	}

	class VisitSecondModel extends DefaultTableModel {

		public VisitSecondModel() {
		}

		@Override
		public boolean isCellEditable(int row, int column) {
			return false;
		}

		public int getRowCount() {
			if (visitSecond == null)
				return 0;
			return visitSecond.size();
		}

		public String getColumnName(int c) {
			return visColums[c];
		}

		public int getColumnCount() {
			return visColums.length;
		}

		public Object getValueAt(int r, int c) {

			Visit visit = visitSecond.get(r);
			GregorianCalendar d = visitSecond.get(r).getDate();
			return getVisitString(visit, d);
		}
	}

	private JButton getPrintTodayButton() {
		if (todayButton == null) {
			todayButton = new JButton();
			todayButton.setMnemonic(KeyEvent.VK_1);
			todayButton.setMaximumSize(new Dimension(ActionsButtonWidth, AllButtonHeight));
			todayButton.setText(MessageBundle.getMessage("angal.visit.printthisday") + " (1)"); //$NON-NLS-1$
			todayButton.addActionListener(new ActionListener() {

				public void actionPerformed(java.awt.event.ActionEvent e) {
					new WardVisitsReport(getWard().getCode(), dateFirst, GeneralData.VISITSHEET);

				}
			});
		}
		return todayButton;
	}

	private JButton getPrintTomorrowButton() {
		if (tomorrowButton == null) {
			tomorrowButton = new JButton();
			tomorrowButton.setMnemonic(KeyEvent.VK_2);
			tomorrowButton.setText(MessageBundle.getMessage("angal.visit.printthisday") + " (2)"); //$NON-NLS-1$
			tomorrowButton.addActionListener(new ActionListener() {

				public void actionPerformed(java.awt.event.ActionEvent e) {
					ward = (Ward) wardBox.getSelectedItem();
					new WardVisitsReport(ward.getCode(), dateSecond, GeneralData.VISITSHEET);

				}
			});
		}
		return tomorrowButton;
	}

	private JButton getButtonNext() {
		if (nextButton == null) {
			nextButton = new JButton(MessageBundle.getMessage("angal.visit.nextabbr") + "->"); //$NON-NLS-1$
			nextButton.setMnemonic(KeyEvent.VK_N);
			nextButton.setMaximumSize(new Dimension(ActionsButtonWidth, AllButtonHeight));
			nextButton.setHorizontalAlignment(SwingConstants.LEFT);
			nextButton.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					setDateDayAfter();
					updatePanels();
				}
			});
		}
		return nextButton;
	}

	private JButton getButtonBack() {
		if (backButton == null) {
			backButton = new JButton("<-" + MessageBundle.getMessage("angal.visit.previousabbr")); //$NON-NLS-1$
			backButton.setMnemonic(KeyEvent.VK_P);
			backButton.setMaximumSize(new Dimension(ActionsButtonWidth, AllButtonHeight));
			backButton.setHorizontalAlignment(SwingConstants.LEFT);
			backButton.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					setDateDayBefore();
					updatePanels();
				}
			});
		}
		return backButton;
	}
	private JButton getCloseButton() {
		if (closeButton == null) {
			closeButton = new JButton(MessageBundle.getMessage("angal.common.close")); //$NON-NLS-1$
			closeButton.setIcon(new ImageIcon("rsc/icons/close_button.png"));
			closeButton.setMnemonic(KeyEvent.VK_X);
			closeButton.setMaximumSize(new Dimension(ActionsButtonWidth, AllButtonHeight));
			closeButton.setHorizontalAlignment(SwingConstants.LEFT);
			closeButton.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					// to free memory
					freeMemory();
					if (owner != null)
						fireVisitsUpdated();
					dispose();
				}
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

	private JPanel TodayPanel;
	private JButton Todaybut;

	private JPanel getTodayVisit() {

		if (TodayPanel == null) {

			TodayPanel = new JPanel();

			Todaybut = new JButton();
			Todaybut.setText(MessageBundle.getMessage("angal.common.today")); //$NON-NLS-1$
			Todaybut.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					setDateFirstThenSecond(new Date());
					updatePanels();
				}
			});

			TodayPanel.add(Todaybut);

		}
		return TodayPanel;

	}

	private JComboBox wardBox;
	private ArrayList<Ward> wardList = null;

	private JPanel getWardPanel() {
		if (wardPanel == null) {
			wardPanel = new JPanel();

			wardBox = new JComboBox();
			wardBox.addItem("");
			try {
				wardList = wbm.getWards();
			} catch (OHServiceException e) {
				wardList = new ArrayList<Ward>();
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

			wardBox.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent arg0) {

					Object selectedWard = wardBox.getSelectedItem();

					if (selectedWard instanceof Ward) {
						loadDataForWard((Ward) selectedWard);
						showGui(true);
					}
					updatePanels();
				}

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
		getAddVisitFirstButton().setVisible(show);
		getVisitDateChooserPanel().setVisible(show);
		getTodayVisit().setVisible(show);
	}

	public Ward getWard() {
		return (Ward) wardBox.getSelectedItem();
	}

}