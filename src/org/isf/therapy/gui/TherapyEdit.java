/**
 * @author Mwithi
 */
package org.isf.therapy.gui;

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
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Hashtable;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import org.isf.generaldata.GeneralData;
import org.isf.generaldata.MessageBundle;
import org.isf.medicals.manager.MedicalBrowsingManager;
import org.isf.medicals.model.Medical;
import org.isf.menu.manager.Context;

import org.isf.patient.manager.PatientBrowserManager;
import org.isf.patient.model.Patient;
import org.isf.stat.gui.report.GenericReportPatient;
import org.isf.stat.gui.report.WardVisitsReport;
import org.isf.therapy.manager.TherapyManager;
import org.isf.therapy.model.Therapy;
import org.isf.therapy.model.TherapyRow;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.JAgenda;
import org.isf.utils.jobjects.DayCalendar;
import org.isf.utils.jobjects.JAgenda.AgendaDayObject;
import org.isf.visits.gui.InsertVisit;
import org.isf.visits.manager.VisitManager;
import org.isf.visits.model.Visit;
import org.isf.ward.model.Ward;

import com.toedter.calendar.JMonthChooser;
import com.toedter.calendar.JYearChooser;


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
public class TherapyEdit extends JDialog {

	private JAgenda jAgenda;
	
	private JPanel northPanel;
	private JPanel monthYearPanel;
	private JYearChooser yearChooser;
	private JMonthChooser monthChooser;
	private JPanel patientPanel;
	private JPanel eastPanel;
	private JPanel southPanel;
	private JTextArea noteTextArea;
	private JScrollPane noteScrollPane;
	private JPanel therapyPanel;
	private JPanel visitPanel;
	private JPanel notifyAndSMSPanel;
	private JPanel actionsPanel;
	private Patient patient;
	private boolean admitted;
	private JButton addVisit;
	private JButton removeVisit;

	private JButton removeTherapy;
	private JButton addTherapy;
	private JButton editTherapy;
	private JButton checkTherapy;
	private JLabel therapyCheckLabel;
	private JButton checkIconButton;
	private JButton smsIconButton;
	private JButton notifyIconButton;
	private JCheckBox notifyCheckBox;
	private JCheckBox smsCheckBox;
	private JButton closeButton;
	private JButton saveButton;
//	private JButton reportButton; TODO to enable when a report will be designed

	private boolean checked = false;
	private boolean available = false;
	private boolean therapyModified = false;
	private boolean notifiable = false;
	private boolean smsenable = false;
	private boolean visitModified = false;
	private Therapy selectedTherapy;
	private Visit selectedVisit;
	private Hashtable<Integer, Therapy> hashTableTherapy;
	private Hashtable<Integer, TherapyRow> hashTableThRow;
	private Hashtable<Integer, Visit> hashTableVisits;

	private static final int TherapyButtonWidth = 200;
	private static final int VisitButtonWidth = 200;
	private static final int ActionsButtonWidth = 240;
	private static final int AllButtonHeight = 30;

	private static final long serialVersionUID = 1L;

	private MedicalBrowsingManager medBrowser = new MedicalBrowsingManager();
	private TherapyManager thManager = Context.getApplicationContext().getBean(TherapyManager.class);
	private VisitManager vstManager = Context.getApplicationContext().getBean(VisitManager.class);
	private ArrayList<Medical> medArray;
	private ArrayList<Double> qtyArray = new ArrayList<Double>();
	private ArrayList<Therapy> therapies = new ArrayList<Therapy>();
	private ArrayList<TherapyRow> thRows = new ArrayList<TherapyRow>();
	private ArrayList<Visit> visits = new ArrayList<Visit>();
	private Ward ward;
	private boolean ad;
	private JPanel wardPanel;
	private JButton todayButton;
	private JButton tomorrowButton;

	private JTable jTableFirst;

	private JScrollPane jScrollPaneFirstday;

	public TherapyEdit(JFrame owner, Patient patient, boolean admitted) {
		super(owner, true);
		try {
			this.medArray = medBrowser.getMedicals();
		} catch (OHServiceException e1) {
			this.medArray = null;
			OHServiceExceptionUtil.showMessages(e1);
		}
		this.patient = patient;
		this.admitted = admitted;
		this.ad=true;
		initComponents();
		addWindowListener(new WindowAdapter() {

			public void windowClosing(WindowEvent e) {
				// force close operation
				closeButton.doClick();
				
				// to free memory
				if (medArray != null) medArray.clear();
				if (therapies != null) therapies.clear();
				if (thRows != null) thRows.clear();
				if (qtyArray != null) qtyArray.clear();
				if (visits != null) visits.clear();
			}
		});
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		// setResizable(false);
		setTitle(MessageBundle.getMessage("angal.therapy.therapy"));
		setSize(new Dimension(screenSize.width - 20, screenSize.height - 100));

	}

	
	public TherapyEdit(JFrame owner, Ward ward, boolean ad) {
		super(owner, true);

		this.ward=ward;
		this.ad=ad;
		initComponents();
		addWindowListener(new WindowAdapter() {

			public void windowClosing(WindowEvent e) {
				// force close operation
				closeButton.doClick();
				
				// to free memory
				if (medArray != null) medArray.clear();
				if (therapies != null) therapies.clear();
				if (thRows != null) thRows.clear();
				if (qtyArray != null) qtyArray.clear();
				if (visits != null) visits.clear();
			}
		});
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		// setResizable(false);
		setTitle("Visits");
		setSize(new Dimension(screenSize.width - 20, screenSize.height - 100));

	}
	private void initComponents() {

		getContentPane().setLayout(new BorderLayout());

		jAgenda = new JAgenda(TherapyEdit.this);
		
		/*
		 * Rows in the therapies table
		 */
		try {
			if (ad) {
			thRows = thManager.getTherapyRows(patient.getCode());
			}
		}catch(OHServiceException e){
			OHServiceExceptionUtil.showMessages(e);
		}
		
		/*
		 * HashTable of the rows
		 */
		if (ad) {
		hashTableThRow = new Hashtable<Integer, TherapyRow>();
		if (!thRows.isEmpty()) {
			for (TherapyRow thRow : thRows) {
				hashTableThRow.put(thRow.getTherapyID(), thRow);
			}
		}
		
		/*
		 * Therapy(s) related to the rows in the therapies table
		 */
		try {
			therapies = thManager.getTherapies(thRows);
		}catch(OHServiceException e){
			OHServiceExceptionUtil.showMessages(e);
		}
		}
		/*
		 * Visit(s) in the visits table
		 */
		try {
			if (ad) {
			visits = vstManager.getVisits(patient.getCode());
			}
//			else {
//				visits = vstManager.getVisitsWard(ward.getCode());	
//			}
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

		for (int i = 0; i < jAgenda.getDayPanel().getComponentCount(); i++) {
			org.isf.utils.jobjects.JAgenda.AgendaDayObject obj = (org.isf.utils.jobjects.JAgenda.AgendaDayObject) jAgenda
					.getDayPanel().getComponent(i);
			if (obj.getList() != null)
				obj.getList().addMouseListener(new MyMouseListener());
		}
if(ad) {
		getContentPane().add(jAgenda, BorderLayout.CENTER);
}else {
	getContentPane().add(dayCalendar(), BorderLayout.CENTER);
}
		getContentPane().add(getNorthPanel(), BorderLayout.NORTH);
		getContentPane().add(getEastPanel(), BorderLayout.EAST);
		getContentPane().add(getSouthPanel(), BorderLayout.SOUTH);

		showAll();

		setSize(540, 480);
	}

	private JPanel dayCalendar() {
		JPanel patientParamsPanel = new JPanel(new SpringLayout());

		GridBagLayout gbl_jPanelData = new GridBagLayout();
		gbl_jPanelData.columnWidths = new int[] { 20, 20, 20, 0, 0, 00 };
		gbl_jPanelData.rowHeights = new int[] { 20, 20, 20, 0, 0, 0, 0, 0 };
		gbl_jPanelData.columnWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };
		gbl_jPanelData.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };
		patientParamsPanel.setLayout(gbl_jPanelData);

	

		GridBagConstraints gbc_ward = new GridBagConstraints();
		gbc_ward.fill = GridBagConstraints.VERTICAL;
		gbc_ward.anchor = GridBagConstraints.WEST;

		gbc_ward.gridy = 0;
		gbc_ward.gridx = 0;
//		patientParamsPanel.add(getWardPanel(), gbc_ward);
//		if (!ad) {
//			GridBagConstraints gbc_Pat = new GridBagConstraints();
//			gbc_Pat.fill = GridBagConstraints.HORIZONTAL;
//			gbc_Pat.anchor = GridBagConstraints.WEST;
//
//			gbc_Pat.gridy = 0;
//			gbc_Pat.gridx = 0;
//			patientParamsPanel.add(previuousButt(), gbc_Pat);	
//		}
//
//		GridBagConstraints gbc_Service = new GridBagConstraints();
//		gbc_Service.fill = GridBagConstraints.HORIZONTAL;
//		gbc_Service.anchor = GridBagConstraints.WEST;
//
//		gbc_Service.gridy = 0;
//		gbc_Service.gridx = 1;
//		patientParamsPanel.add(nextButton(), gbc_Service);

		GridBagConstraints gbc_Duration = new GridBagConstraints();
		gbc_Duration.fill = GridBagConstraints.VERTICAL;
		gbc_Duration.anchor = GridBagConstraints.WEST;
		gbc_Duration.gridy = 1;
		gbc_Duration.gridx = 0;
		gbc_Duration.gridwidth = 2;
		patientParamsPanel.add(getVisitFirstday(), gbc_Duration);

//		GridBagConstraints gbc_date = new GridBagConstraints();
//		gbc_date.fill = GridBagConstraints.VERTICAL;
//		gbc_date.anchor = GridBagConstraints.WEST;
//
//		gbc_date.gridy = 3;
//		gbc_date.gridx = 0;
//		gbc_date.gridwidth = 2;
//		patientParamsPanel.add(getVisitSecondDay(), gbc_date);


		return patientParamsPanel;
	
	}

	private JScrollPane getVisitFirstday() {
		if (jScrollPaneFirstday == null) {
			jScrollPaneFirstday = new JScrollPane();
			jScrollPaneFirstday.setViewportView(getJTablePatient());
			jScrollPaneFirstday.setAlignmentY(Box.TOP_ALIGNMENT);
		}
		return jScrollPaneFirstday;
	}
	private int[] visColumsWidth = { 500, 350 };
	private boolean[] visColumsResizable = { false, true };
	private ArrayList<Visit> visitfirst = new ArrayList<Visit>();
	private JTable getJTablePatient() {
		if (jTableFirst == null) {
			jTableFirst = new JTable();
			visitfirst= getvisit((visits));
			jTableFirst.setModel(new VisitModel());
			jTableFirst.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			for (int i = 0 ; i < visColums.length; i++) {
				jTableFirst.getColumnModel().getColumn(i).setMinWidth(visColumsWidth[i]);
				if (!visColumsResizable[i]) jTableFirst.getColumnModel().getColumn(i).setMaxWidth(visColumsWidth[i]);
			}
			jTableFirst.setAutoCreateColumnsFromModel(false);
			jTableFirst.getColumnModel().getColumn(0).setCellRenderer(new CenterTableCellRenderer());
			
			ListSelectionModel listSelectionModel = jTableFirst.getSelectionModel();
		
	
		}
		return jTableFirst;
	}

	private ArrayList<Visit> getvisit(ArrayList<Visit> arrayList) {
		// TODO Auto-generated method stub
		return null;
	}

	class CenterTableCellRenderer extends DefaultTableCellRenderer {  
		   
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, 
				boolean hasFocus, int row, int column) {  
		   
			Component cell=super.getTableCellRendererComponent(table,value,isSelected,hasFocus,row,column);
			cell.setForeground(Color.BLACK);
			setHorizontalAlignment(LEFT);	   
			return cell;
	   }
	}
	public String[] visColums = {
			MessageBundle.getMessage(getDate()),
			
	}; 
	
	class VisitModel extends DefaultTableModel {
	public VisitModel() {
	}

	public int getRowCount() {
		if (visits == null)
			return 0;
		return visits.size();
	}

	public String getColumnName(int c) {
		return visColums[c];
	}

	public int getColumnCount() {
		return visColums.length;
	}

	public Object getValueAt(int r, int c) {
		Visit visit = visits.get(r); 
		GregorianCalendar d = visits.get(r).getDate();
		SimpleDateFormat formatter=new SimpleDateFormat("yyyy-MM-dd");   // lowercase "dd"
		String da = formatter.format(d.getTime() );
		String dat = getDate();
		if (da.equals(dat)) {
		
			return  visit.getPatient().getName() +","+ visit.getPatient().getCode();
			
		
		} /*else if (c == 2) {
			return patient.getAge();
		} else if (c == 3) {
			return patient.getSex();
		} else if (c == 4) {
			return patient.getCity() + " "
					+ patient.getAddress();
		} */
		return null;
	}
	}

	

	private void showAll() {
		jAgenda.removeAll();
		if (ad) {
		if (therapies != null) showTherapies();
		}
		if (visits != null) showVisits();
		noteTextArea.setText("");
		if(ad) {
			smsCheckBox.setEnabled(false);
			notifyCheckBox.setEnabled(false);
		}
		
		
	}

	private String getDate() {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date date = new Date();
		return dateFormat.format(date);
	}


	private void showTherapies() {
		
		hashTableTherapy = new Hashtable<Integer, Therapy>();
		for (Therapy th : therapies) {
			hashTableTherapy.put(th.getTherapyID(), th);
			showTherapy(th);
		}
	}

	private void showTherapy(Therapy th) {
		for (GregorianCalendar gc : th.getDates()) {
			if (gc.get(GregorianCalendar.YEAR) == yearChooser.getYear()) {
				if (gc.get(GregorianCalendar.MONTH) == monthChooser.getMonth()) {
					jAgenda.addElement(th, gc.get(GregorianCalendar.DAY_OF_MONTH));
					notifyCheckBox.setSelected(th.isNotify());
				}
			}
		}
	}

	private void showVisits() {
		for (Visit visit : visits) {
			final String dateTimeFormat = "dd/MM/yy HH:mm:ss";
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
			Date vis= visit.getDate().getTime();
			sdf.setCalendar(visit.getDate());
			 String dateFormatted = sdf.format(visit.getDate().getTime());
			if (visit.getDate().get(GregorianCalendar.YEAR) == yearChooser.getYear()) {
				if (visit.getDate().get(GregorianCalendar.MONTH) == monthChooser.getMonth()) {
					if (ad) {
					jAgenda.addElement(visit.getWard().getDescription() +"-" +dateFormatted, visit.getDate().get(GregorianCalendar.DAY_OF_MONTH));
					}else {
						jAgenda.addElement(dateFormatted +"-" + visit.getPatient().getName(), visit.getDate().get(GregorianCalendar.DAY_OF_MONTH));
							
					}
					
					
					}
				}
			}
		}
	

	private JPanel getSouthPanel() {
		if (southPanel == null) {
			southPanel = new JPanel();
			southPanel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.therapy.note"))); //$NON-NLS-1$
			southPanel.add(getNote());

		}
		return southPanel;
	}

	private JScrollPane getNote() {
		if (noteTextArea == null) {
			noteTextArea = new JTextArea(4, 100);
			noteTextArea.setLineWrap(true);
			noteTextArea.setEnabled(false);
			//noteTextArea.setAutoscrolls(true);
			noteScrollPane = new JScrollPane(noteTextArea);
			noteTextArea.addFocusListener(new FocusListener() {

				public void focusLost(FocusEvent e) {
					if (selectedTherapy != null) {
						String note = noteTextArea.getText();
						//if (selectedTherapy.getNote() != null && !selectedTherapy.getNote().equals(note))
							selectedTherapy.setNote(note);
						hashTableThRow.get(selectedTherapy.getTherapyID()).setNote(note);
						therapyModified = true;
						saveButton.setEnabled(true);
					}
					if (selectedVisit != null) {
						String note = noteTextArea.getText();
						//if (selectedVisit.getNote() != null && !selectedVisit.getNote().equals(note))
							selectedVisit.setNote(note);
						hashTableVisits.get(selectedVisit.getVisitID()).setNote(note);
						visitModified = true;
						saveButton.setEnabled(true);
					}
				}

				public void focusGained(FocusEvent e) {
				}
			});
		}
		return noteScrollPane;
	}

	private JPanel getEastPanel() {
		if (eastPanel == null) {
			eastPanel = new JPanel();
			eastPanel.setLayout(new BoxLayout(eastPanel, BoxLayout.Y_AXIS));
			if(ad) {
			eastPanel.add(getTherapyPanel());
			}
			eastPanel.add(getVisitPanel());
			if(ad) {
			eastPanel.add(getNotifyAndSMSPanel());
			}
			eastPanel.add(getActionsPanel());

		}
		return eastPanel;
	}

	private JPanel getVisitPanel() {
		if (visitPanel == null) {
			visitPanel = new JPanel();
			visitPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
			visitPanel.setLayout(new BoxLayout(visitPanel, BoxLayout.Y_AXIS));
			visitPanel.setMaximumSize(new Dimension(Short.MAX_VALUE, Short.MAX_VALUE));
			visitPanel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.therapy.visitsandreview"))); //$NON-NLS-1$
			visitPanel.add(getAddVisitButton());
			visitPanel.add(getRemoveVisitButton());
			visitPanel.add(Box.createVerticalGlue());

		}
		return visitPanel;
	}

	private JButton getRemoveVisitButton() {
		if (removeVisit == null) {
			removeVisit = new JButton(MessageBundle.getMessage("angal.therapy.removevisit")); //$NON-NLS-1$
			removeVisit.setIcon(new ImageIcon("rsc/icons/delete_button.png"));
			removeVisit.setMnemonic(KeyEvent.VK_W);
			if (admitted) {
				removeVisit.setEnabled(false);
			}
			removeVisit.setMaximumSize(new Dimension(VisitButtonWidth, AllButtonHeight));
			removeVisit.setHorizontalAlignment(SwingConstants.LEFT);
			removeVisit.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					if (selectedVisit == null)
						return;
					visits.remove(selectedVisit);
					visitModified = true;
					saveButton.setEnabled(true);
					showAll();
				}
			});
		}
		return removeVisit;
	}

	private JButton getAddVisitButton() {
		if (addVisit == null) {
			addVisit = new JButton(MessageBundle.getMessage("angal.therapy.addvisit")); //$NON-NLS-1$
			addVisit.setIcon(new ImageIcon("rsc/icons/calendar_button.png"));
			addVisit.setMnemonic(KeyEvent.VK_V);
			addVisit.setMaximumSize(new Dimension(VisitButtonWidth, AllButtonHeight));
			addVisit.setHorizontalAlignment(SwingConstants.LEFT);
			if (admitted) {
				addVisit.setEnabled(false);
			}
			addVisit.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {

					InsertVisit newVisit = new InsertVisit(TherapyEdit.this, ad, ward);
					newVisit.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
					newVisit.setVisible(true);
					
					Date date = newVisit.getVisitDate();
					if (ad) {
						ward = newVisit.getWard();
					}else {
						patient = newVisit.getPatient();
					}
					
					String service = newVisit.getServ();
					String duration = newVisit.getdur();
					if (date != null) {
						
						Visit visit = new Visit();
						visit.setDate(date);
						visit.setPatient(patient);
						visit.setWard(ward);
						visit.setDuration(duration);
						visit.setService(service);
						
						int visitID = 0;
						try {
							visitID = vstManager.newVisit(visit);
						} catch (OHServiceException e1) {
							OHServiceExceptionUtil.showMessages(e1);
						}
						if (visitID > 0) {
							visit.setVisitID(visitID);
							visits.add(visit);
							hashTableVisits.put(visitID, visit);
							//visitModified = true;
							if (smsenable) smsCheckBox.setEnabled(true);
							if (notifiable) notifyCheckBox.setEnabled(true);
							//saveButton.setEnabled(true);
							showAll();
						}
					} else {
						return;
					}
				}
			});
		}
		return addVisit;
	}

	private JPanel getNotifyAndSMSPanel() {
		if (notifyAndSMSPanel == null) {
			notifyAndSMSPanel = new JPanel();
			notifyAndSMSPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
			notifyAndSMSPanel.setLayout(new BoxLayout(notifyAndSMSPanel, BoxLayout.Y_AXIS));
			notifyAndSMSPanel.setMaximumSize(new Dimension(Short.MAX_VALUE, Short.MAX_VALUE));
			notifyAndSMSPanel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.therapy.notifyandsms"))); //$NON-NLS-1$
			notifyAndSMSPanel.add(Box.createVerticalGlue());
			JPanel notifyPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			notifyPanel.add(getNotifyIconButton());
			notifyPanel.add(getNotifyCheckBox());
			notifyAndSMSPanel.add(notifyPanel);
			JPanel smsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			smsPanel.add(getSmsIconButton());
			smsPanel.add(getSmsCheckBox());
			notifyAndSMSPanel.add(smsPanel);
			notifyAndSMSPanel.add(Box.createVerticalGlue());
			
		}
		return notifyAndSMSPanel;
	}

	private JButton getNotifyIconButton() {
		if (notifyIconButton == null) {
			notifyIconButton = new JButton();
			notifyIconButton.setIcon(new ImageIcon("rsc/icons/notify_dialog.png"));
			notifyIconButton.setOpaque(false);
			notifyIconButton.setBorderPainted(false);
			notifyIconButton.setFocusPainted(false);
			notifyIconButton.setContentAreaFilled(false);
			notifyIconButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		}
		return notifyIconButton;
	}

	private JCheckBox getNotifyCheckBox() {
		if (notifyCheckBox == null) {
			notifyCheckBox = new JCheckBox(MessageBundle.getMessage("angal.therapy.important")); //$NON-NLS-1$
			notifyCheckBox.setSelected(false);
			notifyCheckBox.setAlignmentX(CENTER_ALIGNMENT);
			if 	(!admitted) {
				notifiable = false;
				notifyCheckBox.setEnabled(false);
			} else if (thRows.isEmpty()) {
				notifiable = true;
				notifyCheckBox.setEnabled(false);
			}
			notifyCheckBox.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					selectedTherapy.setNotify(!selectedTherapy.isNotify());
					saveButton.setEnabled(true);
				}
			});
		}
		return notifyCheckBox;
	}

	private JButton getSmsIconButton() {
		if (smsIconButton == null) {
			smsIconButton = new JButton();
			smsIconButton.setIcon(new ImageIcon("rsc/icons/SMS_dialog.png"));
			smsIconButton.setOpaque(false);
			smsIconButton.setBorderPainted(false);
			smsIconButton.setFocusPainted(false);
			smsIconButton.setContentAreaFilled(false);
			smsIconButton.setAlignmentX(Component.CENTER_ALIGNMENT);

		}
		return smsIconButton;
	}

	private JButton getCheckIconButton() {
		if (checkIconButton == null) {
			checkIconButton = new JButton();
			checkIconButton.setIcon(new ImageIcon("rsc/icons/delete_dialog.png"));
			checkIconButton.setOpaque(false);
			checkIconButton.setBorderPainted(false);
			checkIconButton.setFocusPainted(false);
			checkIconButton.setContentAreaFilled(false);
			checkIconButton.setMaximumSize(new Dimension(Short.MAX_VALUE, AllButtonHeight));
			checkIconButton.setMinimumSize(new Dimension(AllButtonHeight, AllButtonHeight));
		}
		return checkIconButton;
	}

	private JCheckBox getSmsCheckBox() {
		if (smsCheckBox == null) {
			smsCheckBox = new JCheckBox(MessageBundle.getMessage("angal.therapy.smsm")); //$NON-NLS-1$
			smsCheckBox.setSelected(false);
			smsCheckBox.setEnabled(false);
			smsCheckBox.setAlignmentX(CENTER_ALIGNMENT);
			smsCheckBox.setAlignmentY(CENTER_ALIGNMENT);
			if (!GeneralData.SMSENABLED || admitted) {
				smsenable = false;
			} else {
				smsenable = true;
			}
			smsCheckBox.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					String telephone = patient.getTelephone();
					if (smsCheckBox.isSelected() && (telephone.equals("") || telephone.length() < 7)) {
						JOptionPane.showMessageDialog(TherapyEdit.this, 
								MessageBundle.getMessage("angal.therapy.theresnotelephonenumberassociatedwiththispatient"), //$NON-NLS-1$
								MessageBundle.getMessage("angal.therapy.warning"), //$NON-NLS-1$
								JOptionPane.WARNING_MESSAGE);
						int ok = JOptionPane.showConfirmDialog(
								TherapyEdit.this,
								MessageBundle.getMessage("angal.therapy.doyouwanttosetanumernowfor") + " " + patient.getName(),
								MessageBundle.getMessage("angal.therapy.settelephonenumber"),
								JOptionPane.CANCEL_OPTION); //$NON-NLS-1$
						if (ok == JOptionPane.YES_OPTION) {
							
							String number = JOptionPane.showInputDialog(
									MessageBundle.getMessage("angal.therapy.telephonenumberfor") + " " + patient.getName());
							if (number != null) {
								patient.setTelephone(number);
								PatientBrowserManager patManager = new PatientBrowserManager();
								try{
									patManager.updatePatient(patient);
								}catch(OHServiceException ex){
									OHServiceExceptionUtil.showMessages(ex);
								}
							}
						} else return;
					}
					if (selectedTherapy != null) {
						selectedTherapy.setSms(smsCheckBox.isSelected());
						hashTableThRow.get(selectedTherapy.getTherapyID()).setSms(smsCheckBox.isSelected());
						therapyModified = true;
					} else if (selectedVisit != null) {
						selectedVisit.setSms(smsCheckBox.isSelected());
						visitModified = true;
					}
					saveButton.setEnabled(true);
				}
			});
		}
		return smsCheckBox;
	}

	private JPanel getActionsPanel() {
		if (actionsPanel == null) {
			actionsPanel = new JPanel();
			actionsPanel.setLayout(new BoxLayout(actionsPanel, BoxLayout.Y_AXIS));
			actionsPanel.setMaximumSize(new Dimension(Short.MAX_VALUE, Short.MAX_VALUE));
			actionsPanel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.therapy.actions"))); //$NON-NLS-1$
			actionsPanel.add(Box.createVerticalGlue());
			if(!ad) {
			actionsPanel.add(getPrintTodayButton());
			actionsPanel.add(gettomorrowTodayButton());
			}
			//TODO: actionsPanel.add(getReportButton());
			actionsPanel.add(getSaveButton());
			actionsPanel.add(getCloseButton());
		}
		return actionsPanel;
	}

	/*
	 * TODO: to be enabled in the future.
	 * 
	 * private JButton getReportButton() {
		if (reportButton == null) {
			reportButton = new JButton("Report");
			reportButton.setIcon(new ImageIcon("rsc/icons/list_button.png"));
			reportButton.setMnemonic(KeyEvent.VK_P);
			reportButton.setMaximumSize(new Dimension(ActionsButtonWidth,
					AllButtonHeight));
			reportButton.setHorizontalAlignment(SwingConstants.LEFT);
			reportButton.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {

				}
			});
		}
		return reportButton;
	}*/

	private JButton  getPrintTodayButton() {
		if (todayButton == null) {
			todayButton = new JButton();
			todayButton.setMnemonic(KeyEvent.VK_R);
			todayButton.setMaximumSize(new Dimension(ActionsButtonWidth,
					AllButtonHeight));
			todayButton.setText(MessageBundle.getMessage("angal.visit.visittoday")); //$NON-NLS-1$
			todayButton.addActionListener(new ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					// GenericReportMY rpt3 = new GenericReportMY(new Integer(6), new Integer(2008), "hmis108_adm_by_diagnosis_in");
					Date today = new Date();
					new WardVisitsReport(ward.getCode(), today, GeneralData.VISITSHEET);
					dispose();
				}
			});
		}
		return todayButton;
	}
	
	private JButton  gettomorrowTodayButton() {
		if (tomorrowButton == null) {
			tomorrowButton = new JButton();
			tomorrowButton.setMnemonic(KeyEvent.VK_R);
			tomorrowButton.setText(MessageBundle.getMessage("angal.visit.visittomorrow")); //$NON-NLS-1$
			tomorrowButton.addActionListener(new ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					// GenericReportMY rpt3 = new GenericReportMY(new Integer(6), new Integer(2008), "hmis108_adm_by_diagnosis_in");
					
					Calendar calendar = Calendar.getInstance();
					Date today = calendar.getTime();

					calendar.add(Calendar.DAY_OF_YEAR, 1);

					
					Date tomorrow = calendar.getTime();

					new WardVisitsReport(ward.getCode(), tomorrow, GeneralData.VISITSHEET);
					dispose();
				}
			});
		}
		return tomorrowButton;
	}
	
	


	private JButton getSaveButton() {
		if (saveButton == null) {
			saveButton = new JButton(MessageBundle.getMessage("angal.common.savem")); //$NON-NLS-1$
			saveButton.setIcon(new ImageIcon("rsc/icons/save_button.png"));
			saveButton.setMnemonic(KeyEvent.VK_S);
			saveButton.setEnabled(false);
			saveButton.setMaximumSize(new Dimension(ActionsButtonWidth,
					AllButtonHeight));
			saveButton.setHorizontalAlignment(SwingConstants.LEFT);
			saveButton.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {

					int ok;
					boolean saveTherapies = false;
					
					/*
					 * Check Therapies before save
					 */
					if (therapyModified) {
						if (thRows.isEmpty()) {
							ok = JOptionPane.showConfirmDialog(
									TherapyEdit.this,
									MessageBundle.getMessage("angal.therapy.deletealltherapiesfor") + " " + patient.getName(),
									MessageBundle.getMessage("angal.therapy.notherapies"),
									JOptionPane.CANCEL_OPTION); //$NON-NLS-1$
							if (ok == JOptionPane.YES_OPTION) {
								try {
									thManager.deleteAllTherapies(patient.getCode());
								} catch (OHServiceException ex) {
									OHServiceExceptionUtil.showMessages(ex);
								}
							} else return;
						} else {
							if (checked) {
								if (available) {
										saveTherapies = true;
								} else {
									ok = JOptionPane.showConfirmDialog(
											TherapyEdit.this,
											MessageBundle.getMessage("angal.therapy.thetherapyisnotavailablecontinue"),
											MessageBundle.getMessage("angal.therapy.notavailable"),
											JOptionPane.CANCEL_OPTION); //$NON-NLS-1$
									if (ok == JOptionPane.YES_OPTION)
										saveTherapies = true;
									else
										return;
								}
							} else {
								ok = JOptionPane.showConfirmDialog(
										TherapyEdit.this,
										MessageBundle.getMessage("angal.therapy.thetherapyhasnotbeencheckedcontinue"),
										MessageBundle.getMessage("angal.therapy.notchecked"),
										JOptionPane.CANCEL_OPTION); //$NON-NLS-1$
								if (ok == JOptionPane.YES_OPTION)
									saveTherapies = true;
								else
									return;
							}
						}
					}

					/*
					 * Check visits before save.
					 */
					if (visitModified) {
						if (visits.isEmpty()) {
							ok = JOptionPane.showConfirmDialog(
									TherapyEdit.this,
									MessageBundle.getMessage("angal.therapy.deleteallvisitsfor") + " " + patient.getName(),
									MessageBundle.getMessage("angal.therapy.novisits"),
									JOptionPane.CANCEL_OPTION); //$NON-NLS-1$
							if (ok == JOptionPane.YES_OPTION) {
								try {
									vstManager.deleteAllVisits(patient.getCode());
								} catch (OHServiceException ex) {
									OHServiceExceptionUtil.showMessages(ex);
								}
							} else 	return;
						} else {
							boolean result = false;
							try {
								result = vstManager.newVisits(visits);
								
							} catch (OHServiceException ex) {
								OHServiceExceptionUtil.showMessages(ex);
							}
							if (result) {
								JOptionPane.showMessageDialog(TherapyEdit.this,
										MessageBundle.getMessage("angal.therapy.patientvisitssaved")); //$NON-NLS-1$
							} else {
								JOptionPane.showMessageDialog(TherapyEdit.this,
										MessageBundle.getMessage("angal.therapy.patientvisitscouldnotbesaved")); //$NON-NLS-1$
							}
						}
					}
					
					if (!therapyModified && !visitModified) {
						ok = JOptionPane.showConfirmDialog(
								TherapyEdit.this,
								MessageBundle.getMessage("angal.therapy.changenotifysettingsfor") + " " + patient.getName(),
								MessageBundle.getMessage("angal.therapy.notifychanged"),
								JOptionPane.CANCEL_OPTION); //$NON-NLS-1$
						if (ok == JOptionPane.YES_OPTION)
							saveTherapies = true;
						else
							return;
					}
					
					if (saveTherapies) {
						boolean result = false;
						try {
							result = thManager.newTherapies(thRows);
						} catch (OHServiceException ex) {
							OHServiceExceptionUtil.showMessages(ex);
						}
						if (result) {
							JOptionPane.showMessageDialog(TherapyEdit.this,
									MessageBundle.getMessage("angal.therapy.therapiesplansaved"));
						} else {
							JOptionPane.showMessageDialog(TherapyEdit.this,
									MessageBundle.getMessage("angal.therapy.therapiesplancouldnotbesaved"));
						}
					}
					
					therapyModified = false;
					visitModified = false;
					saveButton.setEnabled(false);
				}
			});
		}
		return saveButton;
	}

	private JButton getCloseButton() {
		if (closeButton == null) {
			closeButton = new JButton(MessageBundle.getMessage("angal.common.close")); //$NON-NLS-1$
			closeButton.setIcon(new ImageIcon("rsc/icons/close_button.png"));
			closeButton.setMnemonic(KeyEvent.VK_X);
			closeButton.setMaximumSize(new Dimension(ActionsButtonWidth,
					AllButtonHeight));
			closeButton.setHorizontalAlignment(SwingConstants.LEFT);
			closeButton.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					// to free memory
					if (therapyModified || visitModified) {
						int ok = JOptionPane
								.showConfirmDialog(TherapyEdit.this,
										MessageBundle.getMessage("angal.common.save") + "?"); //$NON-NLS-1$
						if (ok == JOptionPane.YES_OPTION) {
							saveButton.doClick();
						} else if (ok == JOptionPane.NO_OPTION) {
							//NO -> do nothing
						} else if (ok == JOptionPane.CANCEL_OPTION) {
							return;
						}
					} 
					
					if (medArray != null) medArray.clear();
					if (therapies != null) therapies.clear();
					if (thRows != null) thRows.clear();
					if (qtyArray != null) qtyArray.clear();
					if (visits != null) visits.clear();
					dispose();
				}
			});
		}
		return closeButton;
	}

	private JPanel getTherapyPanel() {
		if (therapyPanel == null) {
			therapyPanel = new JPanel();
			therapyPanel.setLayout(new BoxLayout(therapyPanel, BoxLayout.Y_AXIS));
			therapyPanel.setMaximumSize(new Dimension(Short.MAX_VALUE, Short.MAX_VALUE));
			therapyPanel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.therapy.therapy"))); //$NON-NLS-1$
			therapyPanel.add(getAddTherapyButton());
			therapyPanel.add(getEditTherapyButton());
			therapyPanel.add(getRemoveTherapyButton());
			therapyPanel.add(getCheckTherapyButton());
			therapyPanel.add(Box.createVerticalGlue());
			therapyPanel.add(getCheckIconButton());
			therapyPanel.add(getTherapyCheckLabel());
			therapyPanel.add(Box.createVerticalGlue());
		}
		return therapyPanel;
	}

	private JLabel getTherapyCheckLabel() {
		if (therapyCheckLabel == null) {
			therapyCheckLabel = new JLabel(MessageBundle.getMessage("angal.therapy.notcheckedm")); //$NON-NLS-1$
			therapyCheckLabel.setForeground(Color.RED);
			therapyCheckLabel
					.setMaximumSize(new Dimension(Short.MAX_VALUE, 25));
			therapyCheckLabel.setHorizontalAlignment(SwingConstants.CENTER);
		}
		return therapyCheckLabel;
	}

	private JButton getCheckTherapyButton() {
		if (checkTherapy == null) {
			checkTherapy = new JButton(MessageBundle.getMessage("angal.therapy.checkavailability")); //$NON-NLS-1$
			checkTherapy.setIcon(new ImageIcon("rsc/icons/flag_button.png"));
			checkTherapy.setMnemonic(KeyEvent.VK_C);
			checkTherapy.setMaximumSize(new Dimension(TherapyButtonWidth,
					AllButtonHeight));
			checkTherapy.setHorizontalAlignment(SwingConstants.LEFT);
			if (thRows.isEmpty()) {
					checkTherapy.setEnabled(false);
			}
			checkTherapy.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {

					available = true;
					ArrayList<Medical> medOutStock = null;
					try {
						medOutStock = thManager.getMedicalsOutOfStock(therapies);
					} catch (OHServiceException ex) {
						available = false;
						OHServiceExceptionUtil.showMessages(ex);
					}
					if(medOutStock != null 
							&& !medOutStock.isEmpty()){
						available = false;
					}
					
					checked = true;
					updateCheckLabel();
					showMedOutOfStock(medOutStock);

					// to free memory
					if (medArray != null) medArray.clear();
					if (medOutStock != null) medOutStock.clear();
				}
			});
		}
		return checkTherapy;
	}

	protected void showMedOutOfStock(ArrayList<Medical> medOutStock) {

		if (medOutStock.size() > 0) {
			StringBuilder message = new StringBuilder(MessageBundle.getMessage("angal.therapy.followingdrugsarefewornotavailable")); //$NON-NLS-1$
			for (Medical med : medOutStock) {
				message.append("\n").append(med.toString());
			}
			JOptionPane.showMessageDialog(TherapyEdit.this, message.toString(),
					MessageBundle.getMessage("angal.therapy.therapynotavailable"), JOptionPane.WARNING_MESSAGE); //$NON-NLS-1$
		}
	}

	protected void updateCheckLabel() {
		if (checked) {
			if (available) {
				checkIconButton
						.setIcon(new ImageIcon("rsc/icons/ok_dialog.png"));
				therapyCheckLabel.setText(MessageBundle.getMessage("angal.therapy.availablem")); //$NON-NLS-1$
				therapyCheckLabel.setForeground(Color.GREEN);
			} else {
				checkIconButton.setIcon(new ImageIcon(
						"rsc/icons/delete_dialog.png"));
				therapyCheckLabel.setText(MessageBundle.getMessage("angal.therapy.notavailablem")); //$NON-NLS-1$
				therapyCheckLabel.setForeground(Color.RED);
			}
		} else {
			checkIconButton
					.setIcon(new ImageIcon("rsc/icons/delete_dialog.png"));
			therapyCheckLabel.setText(MessageBundle.getMessage("angal.therapy.notcheckedm")); //$NON-NLS-1$
			therapyCheckLabel.setForeground(Color.RED);
		}

	}

	/*
	 * new AddTherapy action
	 * 
	 */
	private JButton getAddTherapyButton() {
		if (addTherapy == null) {
			addTherapy = new JButton(MessageBundle.getMessage("angal.therapy.addtherapy")); //$NON-NLS-1$
			addTherapy.setIcon(new ImageIcon("rsc/icons/therapy_button.png"));
			addTherapy.setMnemonic(KeyEvent.VK_A);
			addTherapy.setMaximumSize(new Dimension(TherapyButtonWidth,
					AllButtonHeight));
			addTherapy.setHorizontalAlignment(SwingConstants.LEFT);
			addTherapy.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {

					TherapyEntryForm newThRow = new TherapyEntryForm(TherapyEdit.this, patient.getCode(), null);
					newThRow.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
					newThRow.setVisible(true);

					TherapyRow thRow = newThRow.getThRow();
					
					if (thRow != null && thRow.getTherapyID() != 0) {
	
						thRows.add(thRow); // FOR DB;
						Therapy thisTherapy = null;
						try {
							thisTherapy = thManager.createTherapy(thRow);
						}catch(OHServiceException ex){
							OHServiceExceptionUtil.showMessages(ex);
						}
						therapies.add(thisTherapy); // FOR GUI
						hashTableTherapy.put(thRow.getTherapyID(), thisTherapy);
						hashTableThRow.put(thRow.getTherapyID(), thRow);
						checked = false;
						//therapyModified = true;
						if (smsenable) smsCheckBox.setEnabled(true);
						if (notifiable) notifyCheckBox.setEnabled(true);
						checkTherapy.setEnabled(true);
						//saveButton.setEnabled(true);
						updateCheckLabel();
						showAll();
					}
					selectedTherapy = null;
					newThRow.dispose();
				}
			});
		}
		return addTherapy;
	}
	
	/*
	 * RemoveTherapy action
	 * 
	 */
	private JButton getEditTherapyButton() {
		if (editTherapy == null) {
			editTherapy = new JButton(MessageBundle.getMessage("angal.therapy.edittherapy")); //$NON-NLS-1$
			editTherapy.setIcon(new ImageIcon("rsc/icons/therapy_button.png"));
			editTherapy.setMnemonic(KeyEvent.VK_E);
			editTherapy.setMaximumSize(new Dimension(TherapyButtonWidth,
					AllButtonHeight));
			editTherapy.setHorizontalAlignment(SwingConstants.LEFT);
			editTherapy.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {

					if (selectedTherapy == null) 
						return;
					TherapyEntryForm newThRow = new TherapyEntryForm(TherapyEdit.this, patient.getCode(), selectedTherapy);
					newThRow.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
					newThRow.setVisible(true);
					
					TherapyRow thRow = newThRow.getThRow();

					if (thRow != null) {
						
						//Removing the therapy from the array
						thRows.remove(hashTableThRow.get(selectedTherapy.getTherapyID()));
						therapies.remove(selectedTherapy);
						
						//Rewrite all the therapies
						thRows.add(thRow); // FOR DB;
						Therapy thisTherapy = null;
						try {
							thisTherapy = thManager.createTherapy(thRow);
						}catch(OHServiceException ex){
							OHServiceExceptionUtil.showMessages(ex);
						}
						therapies.add(thisTherapy); // FOR GUI
						checked = false;
						therapyModified = true;
						saveButton.setEnabled(true);
						updateCheckLabel();
						showAll();
					}
					selectedTherapy = null;
					showAll();
					newThRow.dispose();
				}
			});
		}
		return editTherapy;
	}
	
	private JButton getRemoveTherapyButton() {
		if (removeTherapy == null) {
			removeTherapy = new JButton(MessageBundle.getMessage("angal.therapy.removetherapy")); //$NON-NLS-1$
			removeTherapy.setIcon(new ImageIcon("rsc/icons/delete_button.png"));
			removeTherapy.setMaximumSize(new Dimension(TherapyButtonWidth, AllButtonHeight));
			removeTherapy.setHorizontalAlignment(SwingConstants.LEFT);
			removeTherapy.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					if (selectedTherapy == null)
						return;
					System.out.println("==> SelectedTherapy : " + selectedTherapy.getTherapyID());
					System.out.println("==> hashTableThRow : " + hashTableThRow.get(selectedTherapy.getTherapyID()));
					thRows.remove(hashTableThRow.get(selectedTherapy.getTherapyID()));
					therapies.remove(selectedTherapy);
					//thRows.remove(selectedTherapy.getNumTherapy() - 1);
					if (thRows.isEmpty()) checkTherapy.setEnabled(false);
					therapyModified = true;
					selectedTherapy = null;
					saveButton.setEnabled(true);
					checked = false;
					updateCheckLabel();
					showAll();
				}
			});

		}
		return removeTherapy;
	}

	private JPanel getNorthPanel() {
		if (northPanel == null) {
			northPanel = new JPanel(new GridLayout(0, 2));
			northPanel.add(getMonthYearPanel());
			if(ad) {
			northPanel.add(getPatientPanel());
			}else {
				northPanel.add(getWardPanel());
			}

		}
		return northPanel;
	}

	private JPanel getPatientPanel() {
		if (patientPanel == null) {
			patientPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			String patientString = MessageBundle.getMessage("angal.therapy.therapyfor") + " " + patient.getName();// + //$NON-NLS-1$
			// " (Code: "
			// +
			// patient.getCode()
			// +
			// ")";
			JLabel patientLabel = new JLabel(patientString);
			patientLabel.setFont(new Font("Serif", Font.PLAIN, 30));
			patientPanel.add(patientLabel);

		}
		return patientPanel;
	}
	
	private JPanel getWardPanel() {
		if (wardPanel == null) {
			wardPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			String wardString = MessageBundle.getMessage("angal.visit.visitfor") + " " + ward.getDescription();// + //$NON-NLS-1$
			// " (Code: "
			// +
			// patient.getCode()
			// +
			// ")";
			JLabel wardLabel = new JLabel(wardString);
			wardLabel.setFont(new Font("Serif", Font.PLAIN, 30));
			wardPanel.add(wardLabel);

		}
		return wardPanel;
	}
	

	private JPanel getMonthYearPanel() {
		if (monthYearPanel == null) {
			monthYearPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			monthChooser = new JMonthChooser();
			monthChooser.addPropertyChangeListener("month",
					new PropertyChangeListener() {

						public void propertyChange(PropertyChangeEvent evt) {
							JMonthChooser thisChooser = (JMonthChooser) evt
									.getSource();
							jAgenda.setMonth(thisChooser.getMonth());
							showAll();

						}
					});
			yearChooser = new JYearChooser();
			yearChooser.addPropertyChangeListener("year",
					new PropertyChangeListener() {

						public void propertyChange(PropertyChangeEvent evt) {
							JYearChooser thisChooser = (JYearChooser) evt
									.getSource();
							jAgenda.setYear(thisChooser.getYear());
							showAll();
						}
					});
			monthYearPanel.add(monthChooser);
			monthYearPanel.add(yearChooser);

		}
		return monthYearPanel;
	}

	private class MyMouseListener implements MouseListener {

		public MyMouseListener() {
		}

		public void mouseClicked(MouseEvent e) {

			JList thisList = (JList) e.getSource();
			ListModel model = thisList.getModel();
			Therapy th = new Therapy();
			int therapyID = 0;

			int index = thisList.getSelectedIndex();
			if (index == -1) {
				for (Component comp : jAgenda.getDayPanel().getComponents()) {
					AgendaDayObject day = (AgendaDayObject) comp;
					JList list = day.getList();
					if (list != null) {
						list.clearSelection();
					}
				}
				selectedTherapy = null;
				selectedVisit = null;
				noteTextArea.setEnabled(false);
				smsCheckBox.setSelected(false);
				smsCheckBox.setEnabled(false);
				return;
			}
			if (model.getElementAt(index) instanceof Visit) {
				Visit visit = (Visit) model.getElementAt(index);
				selectedTherapy = null;
				selectedVisit = visit;
				if (visit != null) {
					noteTextArea.setText(visit.getNote());
					noteTextArea.setEnabled(true);
					smsCheckBox.setEnabled(true);
					smsCheckBox.setSelected(visit.isSms());
				}
				selectedTherapy = null;
			} else if (model.getElementAt(index) instanceof Therapy) {
				th = (Therapy) model.getElementAt(index);
				selectedTherapy = th;
				selectedVisit = null;
				if (th != null) {
					therapyID = th.getTherapyID();
					noteTextArea.setText(th.getNote());
					noteTextArea.setEnabled(true);
					smsCheckBox.setEnabled(true);
					smsCheckBox.setSelected(th.isSms());
				} else
					return;
			}
			for (Component comp : jAgenda.getDayPanel().getComponents()) {
				AgendaDayObject day = (AgendaDayObject) comp;
				JList list = day.getList();
				if (list != null) {
					list.clearSelection();
					model = list.getModel();
					for (int i = 0; i < model.getSize(); i++) {
						if (model.getElementAt(i) instanceof Therapy) {
							Therapy aTherapy = (Therapy) model.getElementAt(i);
							if (aTherapy.getTherapyID() == therapyID)
								list.setSelectedIndex(i);
						}
						if (model.getElementAt(i) instanceof Visit) {
							Visit aVisit = (Visit) model.getElementAt(i);
							if (selectedVisit != null && selectedVisit == aVisit)
								list.setSelectedIndex(i);
						}
					}
				}
			}
		}

		public void mouseEntered(MouseEvent e) {
		}

		public void mouseExited(MouseEvent e) {
		}

		public void mousePressed(MouseEvent e) {
		}

		public void mouseReleased(MouseEvent e) {
		}
	}

	public void therapyInserted() {
		System.out.println("Therapy successfully inserted"); //$NON-NLS-1$

	}

}
