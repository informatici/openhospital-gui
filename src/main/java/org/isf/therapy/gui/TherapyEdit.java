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
package org.isf.therapy.gui;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListModel;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableCellRenderer;

import org.isf.admission.manager.AdmissionBrowserManager;
import org.isf.generaldata.GeneralData;
import org.isf.generaldata.MessageBundle;
import org.isf.medicals.manager.MedicalBrowsingManager;
import org.isf.medicals.model.Medical;
import org.isf.menu.manager.Context;
import org.isf.patient.manager.PatientBrowserManager;
import org.isf.patient.model.Patient;
import org.isf.therapy.manager.TherapyManager;
import org.isf.therapy.model.Therapy;
import org.isf.therapy.model.TherapyRow;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.JAgenda;
import org.isf.utils.jobjects.JAgenda.AgendaDayObject;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.ModalJFrame;
import org.isf.visits.gui.InsertVisit;
import org.isf.visits.gui.VisitView;
import org.isf.visits.gui.VisitView.VisitListener;
import org.isf.visits.manager.VisitManager;
import org.isf.visits.model.Visit;
import org.isf.ward.model.Ward;

import com.toedter.calendar.JMonthChooser;
import com.toedter.calendar.JYearChooser;

public class TherapyEdit extends ModalJFrame implements VisitListener {

	private static final long serialVersionUID = 1L;

	private static final int THERAPY_BUTTON_WIDTH = 200;
	private static final int VISIT_BUTTON_WIDTH = 200;
	private static final int ACTIONS_BUTTON_WIDTH = 240;
	private static final int ALL_BUTTON_HEIGHT = 30;

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
	private JButton addVisitButton;
	private JButton removeVisitButton;

	private JButton removeTherapyButton;
	private JButton addTherapyButton;
	private JButton editTherapyButton;
	private JButton checkTherapyButton;
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
	private boolean smsenable = GeneralData.SMSENABLED;;
	private boolean visitModified = false;
	private Therapy selectedTherapy;
	private Visit selectedVisit;
	private Hashtable<Integer, TherapyRow> hashTableThRow;
	private Hashtable<Integer, Visit> hashTableVisits;

	private AdmissionBrowserManager admMan = Context.getApplicationContext().getBean(AdmissionBrowserManager.class);
	private MedicalBrowsingManager medBrowser = Context.getApplicationContext().getBean(MedicalBrowsingManager.class);
	private TherapyManager thManager = Context.getApplicationContext().getBean(TherapyManager.class);
	private VisitManager vstManager = Context.getApplicationContext().getBean(VisitManager.class);
	private PatientBrowserManager patientBrowserManager = Context.getApplicationContext().getBean(PatientBrowserManager.class);
	private ArrayList<Medical> medArray;
	private ArrayList<Therapy> therapies = new ArrayList<>();
	private ArrayList<TherapyRow> thRows = new ArrayList<>();
	private ArrayList<Visit> visits = new ArrayList<>();
	private Ward ward;

	public TherapyEdit(JFrame owner, Patient patient, boolean admitted) {
		super();
		try {
			this.medArray = medBrowser.getMedicals();
		} catch (OHServiceException e1) {
			this.medArray = null;
			OHServiceExceptionUtil.showMessages(e1);
		}
		this.patient = patient;
		if (admitted) {
			try {
				this.ward = admMan.getCurrentAdmission(patient).getWard();
			} catch (OHServiceException e1) {
				OHServiceExceptionUtil.showMessages(e1);
			}
		}
		initComponents();
		addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				// force close operation
				closeButton.doClick();
				
				// to free memory
				if (medArray != null) {
					medArray.clear();
				}
				if (therapies != null) {
					therapies.clear();
				}
				if (thRows != null) {
					thRows.clear();
				}
				if (visits != null) {
					visits.clear();
				}
			}
		});
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		// setResizable(false);
		setTitle(MessageBundle.getMessage("angal.therapy.therapy.title"));
		setSize(new Dimension(screenSize.width - 20, screenSize.height - 100));

	}

	private void initComponents() {

		getContentPane().setLayout(new BorderLayout());
		jAgenda = new JAgenda(TherapyEdit.this);
		
		loadFromDB();

		for (int i = 0; i < jAgenda.getDayPanel().getComponentCount(); i++) {
			AgendaDayObject obj = (AgendaDayObject) jAgenda.getDayPanel().getComponent(i);
			if (obj.getList() != null) {
				obj.getList().addMouseListener(new MyMouseListener());
			}
		}
		getContentPane().add(jAgenda, BorderLayout.CENTER);
		getContentPane().add(getNorthPanel(), BorderLayout.NORTH);
		getContentPane().add(getEastPanel(), BorderLayout.EAST);
		getContentPane().add(getSouthPanel(), BorderLayout.SOUTH);

		showAll();

		setSize(540, 480);
	}

	private void loadFromDB() {
		/*
		 * Rows in the therapies table
		 */
		try {
			thRows = thManager.getTherapyRows(patient.getCode());
		} catch (OHServiceException e) {
			OHServiceExceptionUtil.showMessages(e);
		}

		/*
		 * HashTable of the rows
		 */
		hashTableThRow = new Hashtable<>();
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
		} catch (OHServiceException e) {
			OHServiceExceptionUtil.showMessages(e);
		}
		/*
		 * Visit(s) in the visits table
		 */
		try {
			visits = vstManager.getVisits(patient.getCode());
		} catch (OHServiceException e) {
			OHServiceExceptionUtil.showMessages(e);
		}

		/*
		 * HashTable of the visits
		 */
		hashTableVisits = new Hashtable<>();
		if (!visits.isEmpty()) {
			for (Visit visit : visits) {
				hashTableVisits.put(visit.getVisitID(), visit);
			}
		}
	}

	class CenterTableCellRenderer extends DefaultTableCellRenderer {  
		   
		private static final long serialVersionUID = 1L;

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
				boolean hasFocus, int row, int column) {  
		   
			Component cell=super.getTableCellRendererComponent(table,value,isSelected,hasFocus,row,column);
			cell.setForeground(Color.BLACK);
			setHorizontalAlignment(LEFT);	   
			return cell;
	   }
	}
	
	private void showAll() {
		jAgenda.removeAll();
		if (therapies != null) {
			showTherapies();
		}
		if (visits != null) {
			showVisits();
		}
		noteTextArea.setText("");
		smsCheckBox.setEnabled(false);
		notifyCheckBox.setEnabled(false);
	}

	private String getDate() {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date date = new Date();
		return dateFormat.format(date);
	}

	private void showTherapies() {
		for (Therapy th : therapies) {
			showTherapy(th);
		}
	}

	private void showTherapy(Therapy th) {
		for (GregorianCalendar gc : th.getDates()) {
			if (gc.get(Calendar.YEAR) == yearChooser.getYear()) {
				if (gc.get(Calendar.MONTH) == monthChooser.getMonth()) {
					jAgenda.addElement(th, gc.get(Calendar.DAY_OF_MONTH));
					notifyCheckBox.setSelected(th.isNotify());
				}
			}
		}
	}

	private void showVisits() {
		hashTableVisits = new Hashtable<>();
		for (Visit vs : visits) {
			hashTableVisits.put(vs.getVisitID(), vs);
			showVisit(vs);
		}
	}

	private void showVisit(Visit vs) {

		final String dateTimeFormat = "dd/MM/yy HH:mm:ss";
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
		Date vis = vs.getDate().getTime();
		sdf.setCalendar(vs.getDate());
		String dateFormatted = sdf.format(vs.getDate().getTime());
		if (vs.getDate().get(Calendar.YEAR) == yearChooser.getYear()) {
			if (vs.getDate().get(Calendar.MONTH) == monthChooser.getMonth()) {

				jAgenda.addElement(vs, vs.getDate().get(Calendar.DAY_OF_MONTH));

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

				@Override
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

				@Override
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
			eastPanel.add(getTherapyPanel());
			eastPanel.add(getVisitPanel());
			eastPanel.add(getNotifyAndSMSPanel());
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
			visitPanel.add(getWorkSheetButton());
			visitPanel.add(Box.createVerticalGlue());

		}
		return visitPanel;
	}

	private JButton getRemoveVisitButton() {
		if (removeVisitButton == null) {
			removeVisitButton = new JButton(MessageBundle.getMessage("angal.therapy.removevisit.btn"));
			removeVisitButton.setMnemonic(MessageBundle.getMnemonic("angal.therapy.removevisit.btn.key"));
			removeVisitButton.setIcon(new ImageIcon("rsc/icons/delete_button.png"));
			removeVisitButton.setMaximumSize(new Dimension(VISIT_BUTTON_WIDTH, ALL_BUTTON_HEIGHT));
			removeVisitButton.setHorizontalAlignment(SwingConstants.LEFT);
			removeVisitButton.addActionListener(actionEvent -> {
				if (selectedVisit == null) {
					return;
				}
				visits.remove(selectedVisit);

				visitModified = true;
				selectedVisit = null;
				saveButton.setEnabled(true);
				checked = false;
				showAll();
			});
		}
		return removeVisitButton;
	}
	
	private JButton worksheetButton;

	private JButton getWorkSheetButton() {
		if (worksheetButton == null) {
			worksheetButton = new JButton(MessageBundle.getMessage("angal.therapy.worksheet.btn"));
			worksheetButton.setMnemonic(MessageBundle.getMnemonic("angal.therapy.worksheet.btn.key"));
			worksheetButton.setIcon(new ImageIcon("rsc/icons/worksheet_button.png"));
			worksheetButton.setMaximumSize(new Dimension(VISIT_BUTTON_WIDTH, ALL_BUTTON_HEIGHT));
			worksheetButton.setHorizontalAlignment(SwingConstants.LEFT);

			worksheetButton.addActionListener(actionEvent -> {
				
				if (visitModified || therapyModified) {
					MessageDialog.info(TherapyEdit.this, "angal.therapy.pleasesavechangesfirst.msg");
					return;
				}
				
				VisitView worksheet = new VisitView(TherapyEdit.this, patient, ward);
				worksheet.addVisitListener(TherapyEdit.this);
				worksheet.showAsModal(TherapyEdit.this);
			});
		}
		return worksheetButton;
	}
	
	private JButton getAddVisitButton() {
		if (addVisitButton == null) {
			addVisitButton = new JButton(MessageBundle.getMessage("angal.therapy.addvisit.btn"));
			addVisitButton.setMnemonic(MessageBundle.getMnemonic("angal.therapy.addvisit.btn.key"));
			addVisitButton.setIcon(new ImageIcon("rsc/icons/calendar_button.png"));
			addVisitButton.setMaximumSize(new Dimension(VISIT_BUTTON_WIDTH, ALL_BUTTON_HEIGHT));
			addVisitButton.setHorizontalAlignment(SwingConstants.LEFT);
			addVisitButton.addActionListener(actionEvent -> {

				InsertVisit newVsRow = new InsertVisit(TherapyEdit.this, ward, patient, false);
				newVsRow.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
				newVsRow.setVisible(true);

				Visit visit = newVsRow.getVisit();

				if (visit != null) {

					addVisitForSave(visit);
				}
				selectedVisit = null;
				newVsRow.dispose();
			});
		}
		return addVisitButton;
	}

	private void addVisitForSave(Visit visit) {
		visits.add(visit); // FOR GUI
		hashTableVisits.put(visit.getVisitID(), visit);
		checked = false;
		visitModified = true;
		if (smsenable) {
			smsCheckBox.setEnabled(true);
		}
		if (notifiable) {
			notifyCheckBox.setEnabled(true);
		}
		checkTherapyButton.setEnabled(true);
		saveButton.setEnabled(true);
		updateCheckLabel();
		showAll();
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
			notifyCheckBox.setEnabled(false);
			if (thRows.isEmpty()) {
				notifiable = true;
			}
			notifyCheckBox.addActionListener(actionEvent -> {
				selectedTherapy.setNotify(!selectedTherapy.isNotify());
				saveButton.setEnabled(true);
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
			checkIconButton.setMaximumSize(new Dimension(Short.MAX_VALUE, ALL_BUTTON_HEIGHT));
			checkIconButton.setMinimumSize(new Dimension(ALL_BUTTON_HEIGHT, ALL_BUTTON_HEIGHT));
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
			smsCheckBox.addActionListener(actionEvent -> {
				String telephone = patient.getTelephone();
				if (smsCheckBox.isSelected() && (telephone.equals("") || telephone.length() < 7)) {
					MessageDialog.warning(TherapyEdit.this, "angal.therapy.theresnotelephonenumberassociatedwiththispatient");
					int ok = JOptionPane.showConfirmDialog(
							TherapyEdit.this,
							MessageBundle.formatMessage("angal.therapy.doyouwanttosetanumernowfor.fmt", patient.getName()),
							MessageBundle.getMessage("angal.therapy.settelephonenumber"),
							JOptionPane.CANCEL_OPTION);
					if (ok == JOptionPane.YES_OPTION) {

						String number = JOptionPane.showInputDialog(
								MessageBundle.formatMessage("angal.therapy.telephonenumberfor.fmt", patient.getName()));
						if (number != null) {
							patient.setTelephone(number);
							try {
								patientBrowserManager.savePatient(patient);
							} catch (OHServiceException ex) {
								OHServiceExceptionUtil.showMessages(ex);
							}
						}
					} else {
						return;
					}
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

				public void actionPerformed(ActionEvent actionEvent) {

				}
			});
		}
		return reportButton;
	}*/

	private JButton getSaveButton() {
		if (saveButton == null) {
			saveButton = new JButton(MessageBundle.getMessage("angal.common.save.btn"));
			saveButton.setMnemonic(MessageBundle.getMnemonic("angal.common.save.btn.key"));
			saveButton.setIcon(new ImageIcon("rsc/icons/save_button.png"));
			saveButton.setEnabled(false);
			saveButton.setMaximumSize(new Dimension(ACTIONS_BUTTON_WIDTH, ALL_BUTTON_HEIGHT));
			saveButton.setHorizontalAlignment(SwingConstants.LEFT);
			saveButton.addActionListener(actionEvent -> {

				int ok;
				boolean saveTherapies = false;

				/*
				 * Check Therapies before save
				 */
				if (therapyModified) {
					if (thRows.isEmpty()) {
						ok = JOptionPane.showConfirmDialog(
								TherapyEdit.this,
								MessageBundle.formatMessage("angal.therapy.deletealltherapiesfor.fmt", patient.getName()),
								MessageBundle.getMessage("angal.therapy.notherapies"),
								JOptionPane.CANCEL_OPTION); //$NON-NLS-1$
						if (ok == JOptionPane.YES_OPTION) {
							try {
								thManager.deleteAllTherapies(patient.getCode());
							} catch (OHServiceException ex) {
								OHServiceExceptionUtil.showMessages(ex);
							}
						} else {
							return;
						}
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
								if (ok == JOptionPane.YES_OPTION) {
									saveTherapies = true;
								} else {
									return;
								}
							}
						} else {
							ok = JOptionPane.showConfirmDialog(
									TherapyEdit.this,
									MessageBundle.getMessage("angal.therapy.thetherapyhasnotbeencheckedcontinue"),
									MessageBundle.getMessage("angal.therapy.notchecked"),
									JOptionPane.CANCEL_OPTION); //$NON-NLS-1$
							if (ok == JOptionPane.YES_OPTION) {
								saveTherapies = true;
							} else {
								return;
							}
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
								MessageBundle.formatMessage("angal.therapy.deleteallvisitsfor.fmt", patient.getName()),
								MessageBundle.getMessage("angal.therapy.novisits"),
								JOptionPane.CANCEL_OPTION); //$NON-NLS-1$
						if (ok == JOptionPane.YES_OPTION) {
							try {
								vstManager.deleteAllVisits(patient.getCode());
							} catch (OHServiceException ex) {
								OHServiceExceptionUtil.showMessages(ex);
							}
						} else {
							return;
						}
					} else {
						boolean result = false;
						try {
							result = vstManager.newVisits(visits);

						} catch (OHServiceException ex) {
							OHServiceExceptionUtil.showMessages(ex, TherapyEdit.this);
							return;
						}
						if (result) {
							MessageDialog.info(TherapyEdit.this, "angal.therapy.patientvisitssaved");
						} else {
							MessageDialog.error(TherapyEdit.this, "angal.therapy.patientvisitscouldnotbesaved");
						}
					}
				}

				if (!therapyModified && !visitModified) {
					ok = JOptionPane.showConfirmDialog(
							TherapyEdit.this,
							MessageBundle.formatMessage("angal.therapy.changenotifysettingsfor.fmt", patient.getName()),
							MessageBundle.getMessage("angal.therapy.notifychanged"),
							JOptionPane.CANCEL_OPTION);
					if (ok == JOptionPane.YES_OPTION) {
						saveTherapies = true;
					} else {
						return;
					}
				}

				if (saveTherapies) {
					boolean result = false;
					try {
						result = thManager.deleteAllTherapies(patient.getCode());
						result = result && thManager.newTherapies(thRows);
					} catch (OHServiceException ex) {
						OHServiceExceptionUtil.showMessages(ex);
					}
					if (result) {
						MessageDialog.info(TherapyEdit.this, "angal.therapy.therapiesplansaved");
					} else {
						MessageDialog.error(TherapyEdit.this, "angal.therapy.therapiesplancouldnotbesaved");
					}
				}
				loadFromDB();
				therapyModified = false;
				visitModified = false;
				saveButton.setEnabled(false);
				showAll();
			});
		}
		return saveButton;
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
				if (therapyModified || visitModified) {
					int ok = JOptionPane.showConfirmDialog(TherapyEdit.this,
									MessageBundle.getMessage("angal.common.save") + "?"); //$NON-NLS-1$
					if (ok == JOptionPane.YES_OPTION) {
						saveButton.doClick();
					} else if (ok == JOptionPane.NO_OPTION) {
						//NO -> do nothing
					} else if (ok == JOptionPane.CANCEL_OPTION) {
						return;
					}
				}

				if (medArray != null) {
					medArray.clear();
				}
				if (therapies != null) {
					therapies.clear();
				}
				if (thRows != null) {
					thRows.clear();
				}
				if (visits != null) {
					visits.clear();
				}
				dispose();
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
		if (checkTherapyButton == null) {
			checkTherapyButton = new JButton(MessageBundle.getMessage("angal.therapy.checkavailability.btn"));
			checkTherapyButton.setMnemonic(MessageBundle.getMnemonic("angal.therapy.checkavailability.btn.key"));
			checkTherapyButton.setIcon(new ImageIcon("rsc/icons/flag_button.png"));
			checkTherapyButton.setMaximumSize(new Dimension(THERAPY_BUTTON_WIDTH, ALL_BUTTON_HEIGHT));
			checkTherapyButton.setHorizontalAlignment(SwingConstants.LEFT);
			if (thRows.isEmpty()) {
					checkTherapyButton.setEnabled(false);
			}
			checkTherapyButton.addActionListener(actionEvent -> {

				available = true;
				ArrayList<Medical> medOutStock = null;
				try {
					medOutStock = thManager.getMedicalsOutOfStock(therapies);
				} catch (OHServiceException ex) {
					available = false;
					OHServiceExceptionUtil.showMessages(ex);
				}
				if (medOutStock != null && !medOutStock.isEmpty()) {
					available = false;
				}

				checked = true;
				updateCheckLabel();
				showMedOutOfStock(medOutStock);

				// to free memory
				if (medArray != null) {
					medArray.clear();
				}
				if (medOutStock != null) {
					medOutStock.clear();
				}
			});
		}
		return checkTherapyButton;
	}

	protected void showMedOutOfStock(ArrayList<Medical> medOutStock) {

		if (!medOutStock.isEmpty()) {
			StringBuilder message = new StringBuilder(MessageBundle.getMessage("angal.therapy.followingdrugsarefewornotavailable")); //$NON-NLS-1$
			for (Medical med : medOutStock) {
				message.append("\n").append(med.toString());
			}
			JOptionPane.showMessageDialog(TherapyEdit.this, message.toString(),
					MessageBundle.getMessage("angal.therapy.therapynotavailable.title"), JOptionPane.WARNING_MESSAGE);
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
	 * New AddTherapy action
	 */
	private JButton getAddTherapyButton() {
		if (addTherapyButton == null) {
			addTherapyButton = new JButton(MessageBundle.getMessage("angal.therapy.addtherapy.btn"));
			addTherapyButton.setMnemonic(MessageBundle.getMnemonic("angal.therapy.addtherapy.btn.key"));
			addTherapyButton.setIcon(new ImageIcon("rsc/icons/therapy_button.png"));
			addTherapyButton.setMaximumSize(new Dimension(THERAPY_BUTTON_WIDTH, ALL_BUTTON_HEIGHT));
			addTherapyButton.setHorizontalAlignment(SwingConstants.LEFT);
			addTherapyButton.addActionListener(actionEvent -> {

				TherapyEntryForm newThRow = new TherapyEntryForm(TherapyEdit.this, patient.getCode(), null);
				newThRow.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
				newThRow.setVisible(true);

				TherapyRow thRow = newThRow.getThRow();

				if (thRow != null) {

					// Adding new therapy
					addTherapyForSave(thRow);
					
					if (smsenable) {
						smsCheckBox.setEnabled(true);
					}
					if (notifiable) {
						notifyCheckBox.setEnabled(true);
					}
				}
				selectedTherapy = null;
				newThRow.dispose();
			});
		}
		return addTherapyButton;
	}

	private void addTherapyForSave(TherapyRow thRow) {
		thRows.add(thRow); // FOR DB;
		Therapy thisTherapy = null;
		try {
			thisTherapy = thManager.createTherapy(thRow);
		} catch (OHServiceException ex) {
			OHServiceExceptionUtil.showMessages(ex);
		}
		therapies.add(thisTherapy); // FOR GUI
		hashTableThRow.put(thRow.getTherapyID(), thRow);
		checked = false;
		therapyModified = true;
		checkTherapyButton.setEnabled(true);
		saveButton.setEnabled(true);
		updateCheckLabel();
		showAll();
	}
	
	/*
	 * RemoveTherapy action
	 */
	private JButton getEditTherapyButton() {
		if (editTherapyButton == null) {
			editTherapyButton = new JButton(MessageBundle.getMessage("angal.therapy.edittherapy.btn"));
			editTherapyButton.setMnemonic(MessageBundle.getMnemonic("angal.therapy.edittherapy.btn.key"));
			editTherapyButton.setIcon(new ImageIcon("rsc/icons/therapy_button.png"));
			editTherapyButton.setMaximumSize(new Dimension(THERAPY_BUTTON_WIDTH, ALL_BUTTON_HEIGHT));
			editTherapyButton.setHorizontalAlignment(SwingConstants.LEFT);
			editTherapyButton.addActionListener(actionEvent -> {

				if (selectedTherapy == null) {
					return;
				}
				TherapyEntryForm newThRow = new TherapyEntryForm(TherapyEdit.this, patient.getCode(), selectedTherapy);
				newThRow.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
				newThRow.setVisible(true);

				TherapyRow thRow = newThRow.getThRow();

				if (thRow != null) {

					// Removing original modified therapy from arrays
					thRows.remove(hashTableThRow.get(selectedTherapy.getTherapyID()));
					therapies.remove(selectedTherapy);

					// Re-adding modified therapy
					addTherapyForSave(thRow);
				}
				selectedTherapy = null;
				newThRow.dispose();
			});
		}
		return editTherapyButton;
	}
	
	private JButton getRemoveTherapyButton() {
		if (removeTherapyButton == null) {
			removeTherapyButton = new JButton(MessageBundle.getMessage("angal.therapy.removetherapy.btn"));
			removeTherapyButton.setMnemonic(MessageBundle.getMnemonic("angal.therapy.removetherapy.btn.key"));
			removeTherapyButton.setIcon(new ImageIcon("rsc/icons/delete_button.png"));
			removeTherapyButton.setMaximumSize(new Dimension(THERAPY_BUTTON_WIDTH, ALL_BUTTON_HEIGHT));
			removeTherapyButton.setHorizontalAlignment(SwingConstants.LEFT);
			removeTherapyButton.addActionListener(actionEvent -> {
				if (selectedTherapy == null) {
					return;
				}
				thRows.remove(hashTableThRow.get(selectedTherapy.getTherapyID()));
				therapies.remove(selectedTherapy);
				//thRows.remove(selectedTherapy.getNumTherapy() - 1);
				if (thRows.isEmpty()) {
					checkTherapyButton.setEnabled(false);
				}
				therapyModified = true;
				selectedTherapy = null;
				saveButton.setEnabled(true);
				checked = false;
				updateCheckLabel();
				showAll();
			});

		}
		return removeTherapyButton;
	}

	private JPanel getNorthPanel() {
		if (northPanel == null) {
			northPanel = new JPanel(new GridLayout(0, 2));
			northPanel.add(getMonthYearPanel());
			northPanel.add(getPatientPanel());
		}
		return northPanel;
	}

	private JPanel getPatientPanel() {
		if (patientPanel == null) {
			patientPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			String patientString = MessageBundle.formatMessage("angal.therapy.therapyfor.fmt", patient.getName());
			JLabel patientLabel = new JLabel(patientString);
			patientLabel.setFont(new Font("Serif", Font.PLAIN, 30));
			patientPanel.add(patientLabel);

		}
		return patientPanel;
	}

	private JPanel getMonthYearPanel() {
		if (monthYearPanel == null) {
			monthYearPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			monthChooser = new JMonthChooser();
			monthChooser.addPropertyChangeListener("month", propertyChangeEvent -> {
				JMonthChooser thisChooser = (JMonthChooser) propertyChangeEvent.getSource();
				jAgenda.setMonth(thisChooser.getMonth());
				showAll();
			});
			yearChooser = new JYearChooser();
			yearChooser.addPropertyChangeListener("year", propertyChangeEvent -> {
				JYearChooser thisChooser = (JYearChooser) propertyChangeEvent.getSource();
				jAgenda.setYear(thisChooser.getYear());
				showAll();
			});
			monthYearPanel.add(monthChooser);
			monthYearPanel.add(yearChooser);
		}
		return monthYearPanel;
	}

	private class MyMouseListener implements MouseListener {

		public MyMouseListener() {
		}

		@Override
		public void mouseClicked(MouseEvent e) {

			JList thisList = (JList) e.getSource();
			ListModel model = thisList.getModel();
			Therapy th = new Therapy();
			Visit vs = new Visit();
			int therapyID = 0;
			int visitID =0;

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
			Object selectedItem = model.getElementAt(index);
			if (selectedItem instanceof Visit) {
				vs = (Visit) selectedItem;
				selectedTherapy = null;
				selectedVisit = vs;
				if (vs != null) {
					visitID = vs.getVisitID();
					noteTextArea.setText(vs.getNote());
					noteTextArea.setEnabled(true);
					smsCheckBox.setEnabled(true);
					smsCheckBox.setSelected(vs.isSms());
				}
				thisList.setSelectedIndex(index);
			} else if (selectedItem instanceof Therapy) {
				th = (Therapy) selectedItem;
				selectedTherapy = th;
				selectedVisit = null;
				if (th != null) {
					therapyID = th.getTherapyID();
					noteTextArea.setText(th.getNote());
					noteTextArea.setEnabled(true);
					smsCheckBox.setEnabled(true);
					smsCheckBox.setSelected(th.isSms());
				} 
				thisList.setSelectedIndex(index);
			} else {
				return;
			}
			/* 
			 * highlighting/de-highlighting therapies and visits
			 * if saved (therapyID == 0)
			 * 
			 * TODO:
			 * - better arrays management (to highlight also unsaved)
			 * - improve events handling (to avoid selection flickering) 
			 */
			for (Component comp : jAgenda.getDayPanel().getComponents()) {
				AgendaDayObject day = (AgendaDayObject) comp;
				JList list = day.getList();
				if (list != null) {
					if (list != thisList) {
						list.clearSelection();
					}
					model = list.getModel();
					for (int i = 0; i < model.getSize(); i++) {
						Object iteratedItem = model.getElementAt(i);
						if (iteratedItem instanceof Therapy) {
							Therapy aTherapy = (Therapy) iteratedItem;
							if (therapyID != 0 && aTherapy.getTherapyID() == therapyID) {
								list.setSelectedIndex(i);
							}
						}
						if (iteratedItem instanceof Visit) {
							Visit aVisit = (Visit) iteratedItem;
							if (visitID != 0 && aVisit.getVisitID() == visitID) {
								list.setSelectedIndex(i);
							}
						}
					}
				}
			}
		}

		@Override
		public void mouseEntered(MouseEvent e) {
		}

		@Override
		public void mouseExited(MouseEvent e) {
		}

		@Override
		public void mousePressed(MouseEvent e) {
		}

		@Override
		public void mouseReleased(MouseEvent e) {
		}
	}

	@Override
	public void visitsUpdated(AWTEvent e) {
		loadFromDB();
		showAll();
	}

}
