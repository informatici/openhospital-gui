package org.isf.visits.gui;

/**
 * @author Mwithi
 */

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
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
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import org.isf.generaldata.GeneralData;
import org.isf.generaldata.MessageBundle;
import org.isf.menu.manager.Context;
import org.isf.patient.model.Patient;
import org.isf.stat.gui.report.WardVisitsReport;
import org.isf.therapy.gui.TherapyEdit;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.ModalJFrame;
import org.isf.utils.time.TimeTools;
import org.isf.visits.manager.VisitManager;
import org.isf.visits.model.Visit;
import org.isf.visits.model.VisitRow;
import org.isf.ward.manager.WardBrowserManager;
import org.isf.ward.model.Ward;

import com.toedter.calendar.JDateChooser;

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

	private JPanel northPanel;
	private JPanel patientPanel;
	private Patient patient;
	private JButton addFirstVisitButton;
	private JButton addScondVisitButton;
	private JButton closeButton;
	private JButton saveButton;
	// private JButton reportButton; TODO to enable when a report will be designed

	private boolean therapyModified = false;
	private boolean visitModified = false;
	private Hashtable<Integer, Visit> hashTableVisits;

	private static final int VisitButtonWidth = 200;
	private static final int ActionsButtonWidth = 240;
	private static final int AllButtonHeight = 30;

	private static final long serialVersionUID = 1L;

	private VisitManager vstManager = Context.getApplicationContext().getBean(VisitManager.class);
	private WardBrowserManager wbm = Context.getApplicationContext().getBean(WardBrowserManager.class);
	
	private ArrayList<Visit> visits = new ArrayList<Visit>();
	private Ward ward;
	private boolean ad;
	private JPanel wardPanel;
	private JButton todayButton;
	private JButton tomorrowButton;
	private JPanel dateViPanel;
	private JButton dateAdm;
	
	private JTable jTableFirst;

	private JScrollPane jScrollPaneFirstday;

	private ArrayList<VisitRow> vsRows;

	private Hashtable<Integer, VisitRow> hashTableVsRow;
	
	private void initialize() {
		setDateFirstThenSecond(new Date());
	}
	
	public VisitView(TherapyEdit ther) {
		super();
		initialize();
		initComponents();
		
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		// setResizable(false);
		setTitle("Visits"); //TODO: use bundles
		final int x = (screenSize.width - getWidth()) / 2;
		final int y = (screenSize.height - getHeight()) / 2;
		setLocation(x, y);
		setVisible(true);
	}
	
	public VisitView(Ward ward) {
		super();
		this.ward=ward;
		initialize();
		initComponents();
		
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		// setResizable(false);
		setTitle("Visits"); //TODO: use bundles
		final int x = (screenSize.width - getWidth()) / 2;
		final int y = (screenSize.height - getHeight()) / 2;
		setLocation(x, y);
		setVisible(true);
		 if (ward!=null) {
			wardBox.setSelectedItem(ward);
			showGui(true);
		}
		
	}
	public VisitView() {
		initialize();
		initComponents();
		addWindowListener(new WindowAdapter() {

			public void windowClosing(WindowEvent e) {
				// force close operation
				closeButton.doClick();

				// to free memory
				if (visits != null)
					visits.clear();
			}
		});
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		// setResizable(false);
		setTitle("Visits"); //TODO: use bundles
		final int x = (screenSize.width - getWidth()) / 2;
		final int y = (screenSize.height - getHeight()) / 2;
		setLocation(x, y);
		setVisible(true);
		
	}

	private void initComponents() {

		getContentPane().setLayout(new BorderLayout());
		this.setContentPane(getContentPane());
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		pack();
		setLocationRelativeTo(null);
		setResizable(false);
		setVisible(true);
		addWindowListener(new WindowAdapter() {

			public void windowClosing(WindowEvent e) {
				// force close operation
				closeButton.doClick();

				// to free memory
				if (visits != null)
					visits.clear();
			}
		});

		try {
			vsRows = vstManager.getVisitsWard();
		} catch (OHServiceException e1) {
			OHServiceExceptionUtil.showMessages(e1);
		}

		hashTableVsRow = new Hashtable<Integer, VisitRow>();
		if (!vsRows.isEmpty()) {
			for (VisitRow vsRow : vsRows) {
				hashTableVsRow.put(vsRow.getVisitID(), vsRow);
			}
		}
		try {
			visits = vstManager.getVisits(vsRows);
		} catch (OHServiceException e) {
			OHServiceExceptionUtil.showMessages(e);
		}

		/*
		 * HashTable of the visits
		 */
		hashTableVisits = new Hashtable<Integer, Visit>();
		if (!visits.isEmpty()) {
			for (Visit visit : visits) {
				hashTableVisits.put(visit.getVisitID(), visit);
			}
		}
		getContentPane().add(getNorthPanel(), BorderLayout.NORTH);
		getContentPane().add(dayCalendar(), BorderLayout.CENTER);
		showGui(false);

		setSize(1350, 570);
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
		
		GridBagConstraints gbc_buttonnext= new GridBagConstraints();
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

					InsertVisit newVsRow = new InsertVisit(VisitView.this, dateFirst, getWard());
					newVsRow.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
					newVsRow.setVisible(true);

					VisitRow vsRow = newVsRow.getVsRow();
					addVisitRow(vsRow);
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

					InsertVisit newVsRow = new InsertVisit(VisitView.this, dateSecond, getWard());
					newVsRow.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
					newVsRow.setVisible(true);

					VisitRow vsRow = newVsRow.getVsRow();
					addVisitRow(vsRow);
					newVsRow.dispose();
				}
			});
		}
		return addScondVisitButton;
	}
	
	private void addVisitRow(VisitRow vsRow) {
		if (vsRow != null && vsRow.getVisitID() != 0) {
			
			vsRows.add(vsRow); // FOR DB;
			Visit thisVisit = null;
			try {
				thisVisit = vstManager.createVisit(vsRow);
			}catch(OHServiceException ex){
				OHServiceExceptionUtil.showMessages(ex);
			}
			visits.add(thisVisit); // FOR GUI
			hashTableVisits.put(vsRow.getVisitID(), thisVisit);
			hashTableVsRow.put(vsRow.getVisitID(), vsRow);

			if (!TimeTools.isSameDay(dateFirst, thisVisit.getDate().getTime())
					&& !TimeTools.isSameDay(dateSecond, thisVisit.getDate().getTime()))
				//if new visit date is not already shown, change view
				setDateFirstThenSecond(thisVisit.getDate().getTime());
				
			updatePanels();
		}
	}
	
	private JPanel datefirstPanel;
	private Date dateFirst;
	private JLabel dateFirstLabel;
	private JPanel getDateFirstDay() {
		if (datefirstPanel == null) {
			datefirstPanel = new JPanel();
			dateFirstLabel = new JLabel();
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
			datesecondLabel = new JLabel();
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
		
			jScrollPaneFirstday.setMinimumSize(new Dimension(500,400));
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
			sl_visitParamsPanel.putConstraint(SpringLayout.NORTH, jScrollPaneSecondtday, 0, SpringLayout.NORTH,
					getVisitFirstday());
			sl_visitParamsPanel.putConstraint(SpringLayout.EAST, jScrollPaneSecondtday, -104, SpringLayout.WEST,
					getVisitFirstday());
			jScrollPaneSecondtday.setViewportView(visitSecondDayPanel());
			jScrollPaneSecondtday.setAlignmentY(Box.TOP_ALIGNMENT);
			jScrollPaneSecondtday.setMinimumSize(new Dimension(500,400));
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
			//TODO: use bundles key instead of labels
			dateAdm.setText(MessageBundle.getMessage("Go to date:"));
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
		
			if (TimeTools.isSameDay(visit.getDate().getTime(),date)
					&& (visit.getWard().equals(getWard()))) {
				vis.add(visit);
			}
		}
		return vis;
	}

	class CenterTableCellRenderer extends DefaultTableCellRenderer {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {

			Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			cell.setForeground(Color.BLACK);
			setHorizontalAlignment(LEFT);
			return cell;
		}
	}

	public String[] visColums = { MessageBundle.getMessage("Visits"),

	};
	private SpringLayout sl_visitParamsPanel;

	private JButton backButton;

	private JButton nextButton;
	
	private Object getVisitString(Visit visit, GregorianCalendar d) {
		//TODO: use bundles
		StringBuilder strBuilder = new StringBuilder("Time").append(": "); 
		strBuilder.append(formatDateTime(d)).append(" - "); 
		strBuilder.append(visit.getPatient().getName()).append(" ");
		strBuilder.append("(").append(visit.getPatient().getCode()).append(") - ");
		strBuilder.append("Service").append(": ").append(visit.getService()).append(" ");
		strBuilder.append("(").append(visit.getDuration()).append("min").append(")"); 
		
		return strBuilder.toString();
	}

	class VisitModel extends DefaultTableModel {
		public VisitModel() {
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
			todayButton.setMnemonic(KeyEvent.VK_R);
			todayButton.setMaximumSize(new Dimension(ActionsButtonWidth, AllButtonHeight));
			todayButton.setText(MessageBundle.getMessage("angal.visit.visittoday")); //$NON-NLS-1$
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
			tomorrowButton.setMnemonic(KeyEvent.VK_R);
			tomorrowButton.setText(MessageBundle.getMessage("angal.visit.visittoday")); //$NON-NLS-1$
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
			nextButton = new JButton("Next->"); //TODO: use bundles
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
			backButton = new JButton("<-back"); //$NON-NLS-1$
			backButton.setMnemonic(KeyEvent.VK_B);
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
					if (therapyModified || visitModified) {
						int ok = JOptionPane.showConfirmDialog(VisitView.this,
								MessageBundle.getMessage("angal.common.save") + "?"); //$NON-NLS-1$
						if (ok == JOptionPane.YES_OPTION) {
							saveButton.doClick();
						} else if (ok == JOptionPane.NO_OPTION) {
							// NO -> do nothing
						} else if (ok == JOptionPane.CANCEL_OPTION) {
							return;
						}
					}

					if (visits != null)
						visits.clear();
					dispose();
				}
			});
		}
		return closeButton;
	}

	private JPanel getNorthPanel() {
		if (northPanel == null) {
			northPanel = new JPanel(new GridLayout(1, 2));

			if (ad) {
				northPanel.add(getPatientPanel());
			} else {
				northPanel.add(getWardPanel());
				northPanel.add(getVisitDateChooserPanel());
				northPanel.add(getTodayVisit());
			}
		}
		return northPanel;
	}
	private JPanel TodayPanel;
	private JButton Todaybut;
	private JPanel getTodayVisit() {


		if (TodayPanel == null) {

			TodayPanel = new JPanel();

			Todaybut= new JButton();
			Todaybut.setText(MessageBundle.getMessage("Toady Visit"));
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
	private JPanel getPatientPanel() {
		if (patientPanel == null) {
			patientPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			String patientString = MessageBundle.getMessage("angal.therapy.therapyfor") + " " + patient.getName();
			JLabel patientLabel = new JLabel(patientString);
			patientLabel.setFont(new Font("Serif", Font.PLAIN, 30));
			patientPanel.add(patientLabel);

		}
		return patientPanel;
	}
	private JComboBox wardBox;
	private Ward saveWard = null;
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

				if (saveWard != null) {
					if (saveWard.getCode().equalsIgnoreCase(ward.getCode())) {
						wardBox.setSelectedItem(ward);
					}
				}
			}

			wardBox.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent arg0) {

					Object war = wardBox.getSelectedItem();

					if (war instanceof Ward) {
						showGui(true);

					}
					updatePanels();
				}

			});

		}

		wardPanel.add(wardBox);
		wardPanel.setBorder(BorderFactory.createTitledBorder("Select a ward:"));

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
		return  (Ward) wardBox.getSelectedItem();
	}

}
