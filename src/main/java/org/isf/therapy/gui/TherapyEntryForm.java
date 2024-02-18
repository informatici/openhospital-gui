/*
 * Open Hospital (www.open-hospital.org)
 * Copyright © 2006-2024 Informatici Senza Frontiere (info@informaticisenzafrontiere.org)
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
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.isf.therapy.gui;

import static org.isf.utils.Constants.DATE_FORMAT_DD_MM_YYYY;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.TitledBorder;

import org.isf.generaldata.MessageBundle;
import org.isf.medicals.manager.MedicalBrowsingManager;
import org.isf.medicals.model.Medical;
import org.isf.menu.manager.Context;
import org.isf.therapy.manager.TherapyManager;
import org.isf.therapy.model.Therapy;
import org.isf.therapy.model.TherapyRow;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.GoodDateChooser;
import org.isf.utils.jobjects.IconButton;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.time.TimeTools;

/**
 * @author Mwithi
 */
public class TherapyEntryForm extends JDialog {

	private static final long serialVersionUID = 1L;

	/*
	 * Managers
	 */
	private MedicalBrowsingManager medicalBrowsingManager = Context.getApplicationContext().getBean(MedicalBrowsingManager.class);
	private TherapyManager therapyManager = Context.getApplicationContext().getBean(TherapyManager.class);

	/*
	 * Constants
	 */
	private static final int SLIDER_MIN_VALUE = 0;
	private static final int SLIDER_MAX_VALUE = 500;
	private static final int SLIDER_MAJOR_STEP_VALUE = 250;
	private static final int SLIDER_MINOR_STEP_VALUE = 50;
	private static final int PREFERRED_SPINNER_WIDTH = 100;
	private static final int ONE_LINE_COMPONENTS_HEIGHT = 30;
	private static final int VISIBLE_MEDICALS_ROWS = 5;
	private static final int FREQUENCY_IN_DAY_OPTIONS = 4;

	/*
	 * Attributes
	 */
	private List<Medical> medArray;
	private Therapy therapy;
	private TherapyRow thRow;
	private JList medicalsList;
	private JScrollPane medicalListscrollPane;
	private JPanel dayWeeksMonthsPanel;
	private JPanel frequencyInDayPanel;
	private JPanel frequencyInPeriodPanel;
	private JPanel quantityPanel;
	private JPanel medicalsPanel;
	private JPanel therapyPanelWest;
	private JPanel therapyPanelEast;
	private JPanel startEndDatePanel;
	private JPanel startDatePanel;
	private JPanel endDatePanel;
	private JPanel notePanel;
	private JScrollPane noteScrollPane;
	private JTextArea noteTextArea;
	private JPanel iconMedicalPanel;
	private JPanel iconFrequenciesPanel;
	private JPanel iconPeriodPanel;
	private JPanel iconNotePanel;
	private JPanel frequenciesPanel;
	private JSlider jSliderQty;
	private JSpinner jSpinnerQty;
	private List<JRadioButton> radioButtonSet;
	private JSpinner jSpinnerFreqInPeriod;
	private GoodDateChooser therapyStartdate;
	private LocalDateTime therapyEndDate;
	private JSpinner jSpinnerDays;
	private JSpinner jSpinnerWeeks;
	private JSpinner jSpinnerMonths;
	private JLabel endDateLabel;
	private final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern(DATE_FORMAT_DD_MM_YYYY);
	private final String[] radioButtonLabels = {
			MessageBundle.getMessage("angal.therapy.one"),
			MessageBundle.getMessage("angal.therapy.two"),
			MessageBundle.getMessage("angal.therapy.three"),
			MessageBundle.getMessage("angal.therapy.four")
	};
	private JButton buttonCancel;
	private JButton buttonOK;
	private JPanel buttonPanel;
	private JPanel therapyPanel;
	private int freqInDay;
	private int patID;

	private boolean inserting;
	
	/**
	 * Create the dialog.
	 */
	public TherapyEntryForm(JFrame owner, int patID, Therapy th) {
		super(owner, true);
		setIconImage(new ImageIcon("./rsc/icons/oh.png").getImage());
		inserting = th == null;
		try {
			this.medArray = medicalBrowsingManager.getMedicals();
		} catch (OHServiceException e) {
			this.medArray = new ArrayList<>();
			OHServiceExceptionUtil.showMessages(e, this);
		}
		this.therapy = th;
		this.patID = patID;

		initComponents();

		if (!inserting) {
			fillFormWithTherapy(therapy);
		} else {
			therapy = new Therapy();
			radioButtonSet.get(0).setSelected(true);
			endDateLabel.setText(dateFormat.format(TimeTools.getNow()));
		}
		this.pack();
	}

	private void initComponents() {
		if (inserting) {
			setTitle(MessageBundle.getMessage("angal.therapy.newtherapyentryform.title"));
		} else {
			setTitle(MessageBundle.getMessage("angal.therapy.edittherapyentryform.title"));
			getContentPane().setBackground(Color.RED);
		}
		setSize(new Dimension(740, 400));
		getContentPane().setLayout(new BorderLayout(0, 0));
		getContentPane().add(getTherapyPanel(), BorderLayout.CENTER);
		getContentPane().add(getButtonPanel(), BorderLayout.SOUTH);
		setResizable(false);
		setLocationRelativeTo(null);
	}

	private void fillFormWithTherapy(Therapy th) {
		/*
		 * Medicals
		 */
		medicalsList.setSelectedValue(th.getMedical(), true);

		/*
		 * Quantity
		 */
		jSpinnerQty.setValue(th.getQty());

		/*
		 * Frequency Within Day
		 */
		radioButtonSet.get(th.getFreqInDay() - 1).setSelected(true);
		
		/*
		 * Calendars
		 */
		fillCalendarsFromTherapy(th);

		/*
		 * Note
		 */
		noteTextArea.setText(th.getNote());
	}

	private void fillCalendarsFromTherapy(Therapy th) {
		LocalDateTime[] dates = th.getDates();
		int datesLength = dates.length;
		LocalDateTime firstDay = dates[0];
		LocalDateTime lastDay = dates[datesLength - 1];
		LocalDateTime secondDay;
		
		if (datesLength > 1) {
			secondDay = dates[1];
		} else {
			secondDay = firstDay;
		}
		int days = TimeTools.getDaysBetweenDates(firstDay, secondDay, true);

		jSpinnerFreqInPeriod.setValue(days > 0 ? days : 1);
		therapyStartdate.setDate(firstDay.toLocalDate());
		endDateLabel.setText(dateFormat.format(lastDay));

		fillDaysWeeksMonthsFromDates(firstDay, lastDay); 
	}

	private void fillDaysWeeksMonthsFromDates(LocalDateTime firstDay, LocalDateTime lastDay) {
		Period period = Period.between(firstDay.toLocalDate(), lastDay.toLocalDate().plusDays(1));

		int months = period.getMonths();
		int days = period.getDays();
		int weeks = days / 7;
		days = days - (weeks * 7);

		jSpinnerMonths.setValue(months);
		jSpinnerWeeks.setValue(weeks);
		jSpinnerDays.setValue(days);
	}

	private JList getMedicalsList() {
		if (medicalsList == null) {
			medicalsList = new JList(medArray.toArray());
			medicalsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		}
		return medicalsList;
	}

	private JScrollPane getMedicalListscrollPane() {
		if (medicalListscrollPane == null) {
			medicalListscrollPane = new JScrollPane(getMedicalsList());
			medicalListscrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
			medicalListscrollPane.setMaximumSize(new Dimension(Short.MAX_VALUE,
					ONE_LINE_COMPONENTS_HEIGHT * VISIBLE_MEDICALS_ROWS));
		}
		return medicalListscrollPane;
	}

	private JSpinner getSpinnerQty() {
		Double startQty = 0.;
		Double minQty = 0.;
		Double stepQty = 0.5;
		Double maxQty = null;
		jSpinnerQty = new JSpinner(new SpinnerNumberModel(startQty, minQty, maxQty, stepQty));
		jSpinnerQty.setFont(new Font("Dialog", Font.BOLD, 14));
		jSpinnerQty.setAlignmentX(Component.LEFT_ALIGNMENT);
		jSpinnerQty.setPreferredSize(new Dimension(PREFERRED_SPINNER_WIDTH, ONE_LINE_COMPONENTS_HEIGHT));
		jSpinnerQty.setMaximumSize(new Dimension(Short.MAX_VALUE, ONE_LINE_COMPONENTS_HEIGHT));
		jSpinnerQty.addChangeListener(changeEvent -> {
			JSpinner source = (JSpinner) changeEvent.getSource();
			double value = (Double) source.getValue();
			therapy.setQty(value);
			int intValue = (int) Math.round(value);
			jSliderQty.setValue(intValue);
		});
		return jSpinnerQty;
	}

	private JPanel getDaysWeeksMonthsPanel() {
		if (dayWeeksMonthsPanel == null) {
			dayWeeksMonthsPanel = new JPanel();
			dayWeeksMonthsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
			dayWeeksMonthsPanel.setBorder(new TitledBorder(null, MessageBundle.getMessage("angal.therapy.period"), //$NON-NLS-1$
					TitledBorder.LEADING, TitledBorder.TOP, null, null));
			dayWeeksMonthsPanel.setMaximumSize(new Dimension(Short.MAX_VALUE, Short.MAX_VALUE));
			dayWeeksMonthsPanel.add(getPeriodSpinners());
		}
		return dayWeeksMonthsPanel;
	}

	private JPanel getPeriodSpinners() {

		int startQty = 0;
		int minQty = 0;
		int maxQty = 99;
		int stepQty = 1;

		jSpinnerDays = new JSpinner(new SpinnerNumberModel(1, minQty, maxQty, stepQty));
		jSpinnerWeeks = new JSpinner(new SpinnerNumberModel(startQty, minQty, maxQty, stepQty));
		jSpinnerMonths = new JSpinner(new SpinnerNumberModel(startQty, minQty, maxQty, stepQty));

		JPanel daysPanel = new JPanel();
		BoxLayout daysLayout = new BoxLayout(daysPanel, BoxLayout.Y_AXIS);
		daysPanel.setLayout(daysLayout);
		JLabel labelDays = new JLabel(MessageBundle.getMessage("angal.common.days.txt"));
		labelDays.setAlignmentX(CENTER_ALIGNMENT);
		jSpinnerDays.addChangeListener(changeEvent -> {
			JSpinner theSpinner = (JSpinner) changeEvent.getSource();
			if ((Integer) theSpinner.getValue() == 0) {
				/*
				 * Days must be at least one.
				 */
				if ((Integer) jSpinnerWeeks.getValue() == 0 && (Integer) jSpinnerMonths.getValue() == 0) {
					theSpinner.setValue(theSpinner.getNextValue());
				}
			}
			updateEndDateLabel();
		});
		jSpinnerDays.setAlignmentX(CENTER_ALIGNMENT);
		daysPanel.add(labelDays);
		daysPanel.add(jSpinnerDays);
		JPanel weeksPanel = new JPanel();
		BoxLayout weeksLayout = new BoxLayout(weeksPanel, BoxLayout.Y_AXIS);
		weeksPanel.setLayout(weeksLayout);
		JLabel labelWeeks = new JLabel(MessageBundle.getMessage("angal.therapy.weeks")); //$NON-NLS-1$
		labelWeeks.setAlignmentX(CENTER_ALIGNMENT);

		jSpinnerWeeks.addChangeListener(changeEvent -> updateEndDateLabel());
		jSpinnerWeeks.setAlignmentX(CENTER_ALIGNMENT);
		weeksPanel.add(labelWeeks);
		weeksPanel.add(jSpinnerWeeks);
		JPanel monthsPanel = new JPanel();
		BoxLayout monthsLayout = new BoxLayout(monthsPanel, BoxLayout.Y_AXIS);
		monthsPanel.setLayout(monthsLayout);
		JLabel labelMonths = new JLabel(MessageBundle.getMessage("angal.common.months.txt"));
		labelMonths.setAlignmentX(CENTER_ALIGNMENT);

		jSpinnerMonths.addChangeListener(changeEvent -> updateEndDateLabel());
		jSpinnerMonths.setAlignmentX(CENTER_ALIGNMENT);
		monthsPanel.add(labelMonths);
		monthsPanel.add(jSpinnerMonths);
		JPanel daysWeeksMonthsPanel = new JPanel();
		daysWeeksMonthsPanel.add(daysPanel);
		daysWeeksMonthsPanel.add(weeksPanel);
		daysWeeksMonthsPanel.add(monthsPanel);
		return daysWeeksMonthsPanel;
	}

	private JPanel getFrequencyInDayPanel() {
		if (frequencyInDayPanel == null) {
			frequencyInDayPanel = new JPanel();
			frequencyInDayPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
			frequencyInDayPanel.setBorder(new TitledBorder(null,
					MessageBundle.getMessage("angal.therapy.frequencywithinday"), TitledBorder.LEADING, //$NON-NLS-1$
					TitledBorder.TOP, null, null));

			radioButtonSet = getRadioButtonSet(FREQUENCY_IN_DAY_OPTIONS);
			for (JRadioButton radioButton : radioButtonSet) {
				frequencyInDayPanel.add(radioButton);
			}
		}
		return frequencyInDayPanel;
	}

	private JPanel getFrequencyInPeriodPanel() {
		if (frequencyInPeriodPanel == null) {
			frequencyInPeriodPanel = new JPanel();
			frequencyInPeriodPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
			frequencyInPeriodPanel.setBorder(new TitledBorder(null,
					MessageBundle.getMessage("angal.therapy.frequencywithinperiod"), TitledBorder.LEADING, //$NON-NLS-1$
					TitledBorder.TOP, null, null));

			JLabel labelPrefix = new JLabel(MessageBundle.getMessage("angal.therapy.every")); //$NON-NLS-1$
			frequencyInPeriodPanel.add(labelPrefix);

			frequencyInPeriodPanel.add(getSpinnerFreqInPeriod());

			JLabel labelSuffix = new JLabel(MessageBundle.getMessage("angal.therapy.daydays")); //$NON-NLS-1$
			frequencyInPeriodPanel.add(labelSuffix);
		}
		return frequencyInPeriodPanel;
	}

	private List<JRadioButton> getRadioButtonSet(int frequencyInDayOptions) {

		radioButtonSet = new ArrayList<>();
		ButtonGroup buttonGroup = new ButtonGroup();

		for (int i = 0; i < frequencyInDayOptions; i++) {
			JRadioButton radioButton = new JRadioButton(radioButtonLabels[i]);
			radioButtonSet.add(radioButton);
			buttonGroup.add(radioButton);
		}

		return radioButtonSet;
	}

	private JPanel getQuantityPanel() {
		if (quantityPanel == null) {
			quantityPanel = new JPanel();
			quantityPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
			quantityPanel.setLayout(new BoxLayout(quantityPanel, BoxLayout.X_AXIS));

			JLabel quantityLabel = new JLabel(MessageBundle.getMessage("angal.common.quantity.txt"));
			quantityPanel.add(quantityLabel);
			quantityPanel.add(getSpinnerQty());
			quantityPanel.add(getQuantitySlider());
		}
		return quantityPanel;
	}

	private JPanel getMedicalsPanel() {
		if (medicalsPanel == null) {
			medicalsPanel = new JPanel();
			medicalsPanel.setAlignmentY(Component.TOP_ALIGNMENT);
			medicalsPanel.setLayout(new BoxLayout(medicalsPanel, BoxLayout.Y_AXIS));
			medicalsPanel.setBorder(new TitledBorder(null, MessageBundle.getMessage("angal.therapy.pharmaceutical"), //$NON-NLS-1$
					TitledBorder.LEADING, TitledBorder.TOP, null, null));
			medicalsPanel.add(getMedicalListscrollPane());
			medicalsPanel.add(Box.createVerticalGlue());
			medicalsPanel.add(getQuantityPanel());
		}
		return medicalsPanel;
	}

	private JPanel getTherapyPanelWest() {
		if (therapyPanelWest == null) {
			therapyPanelWest = new JPanel();
			therapyPanelWest.setLayout(new BoxLayout(therapyPanelWest, BoxLayout.Y_AXIS));

			therapyPanelWest.add(getIconMedicalPanel());
			therapyPanelWest.add(getIconFrequenciesPanel());
		}
		return therapyPanelWest;
	}

	private JPanel getTherapyPanelEast() {
		if (therapyPanelEast == null) {
			therapyPanelEast = new JPanel();
			therapyPanelEast.setLayout(new BoxLayout(therapyPanelEast, BoxLayout.Y_AXIS));
			
			therapyPanelEast.add(getIconPeriodPanel());
			therapyPanelEast.add(getIconNotePanel());
		}
		return therapyPanelEast;
	}

	private JSpinner getSpinnerFreqInPeriod() {
		Integer startQty = 1;
		Integer minQty = 1;
		Integer stepQty = 1;
		Integer maxQty = 100;
		jSpinnerFreqInPeriod = new JSpinner(new SpinnerNumberModel(startQty, minQty, maxQty, stepQty));
		jSpinnerFreqInPeriod.setAlignmentX(Component.LEFT_ALIGNMENT);
		jSpinnerFreqInPeriod.setMaximumSize(new Dimension(Short.MAX_VALUE, ONE_LINE_COMPONENTS_HEIGHT));
		jSpinnerFreqInPeriod.addChangeListener(changeEvent -> {
		});
		return jSpinnerFreqInPeriod;
	}

	private GoodDateChooser getStartDate() {
		if (therapyStartdate == null) {
			therapyStartdate = new GoodDateChooser(LocalDate.now());
			therapyStartdate.addDateChangeListener(dateChangeEvent -> updateEndDateLabel());
		}
		return therapyStartdate;
	}
	
	private void updateEndDateLabel() {
		
		int days = (Integer) jSpinnerDays.getValue();
		int weeks = (Integer) jSpinnerWeeks.getValue();
		int months = (Integer) jSpinnerMonths.getValue();

		therapyEndDate = therapyStartdate.getDateStartOfDay()
				.plusDays(days - 1L)
				.plusWeeks(weeks)
				.plusMonths(months);
		
		endDateLabel.setText(dateFormat.format(therapyEndDate));
	}

	private JPanel getStartEndDatePanel() {
		if (startEndDatePanel == null) {
			startEndDatePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
			startEndDatePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
			startEndDatePanel.setBorder(new TitledBorder(null, MessageBundle.getMessage("angal.therapy.startsdashend"), //$NON-NLS-1$
					TitledBorder.LEADING, TitledBorder.TOP, null, null));

			startEndDatePanel.add(getStartDatePanel());
			startEndDatePanel.add(getEndDatePanel());

		}
		return startEndDatePanel;
	}

	private JPanel getStartDatePanel() {
		if (startDatePanel == null) {
			startDatePanel = new JPanel();
			startDatePanel.setLayout(new BoxLayout(startDatePanel, BoxLayout.Y_AXIS));
			JLabel startLabel = new JLabel(MessageBundle.getMessage("angal.therapy.start")); //$NON-NLS-1$
			startLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
			startDatePanel.add(startLabel);
			startDatePanel.add(getStartDate());
		}
		return startDatePanel;
	}

	private JPanel getEndDatePanel() {
		if (endDatePanel == null) {
			endDatePanel = new JPanel();
			endDatePanel.setLayout(new BoxLayout(endDatePanel, BoxLayout.Y_AXIS));
			JLabel endDateLabel = new JLabel(MessageBundle.getMessage("angal.therapy.end")); //$NON-NLS-1$
			endDateLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
			endDatePanel.add(endDateLabel);
			endDatePanel.add(getEndDateField());
		}
		return endDatePanel;
	}

	private JPanel getNotePanel() {
		if (notePanel == null) {
			notePanel = new JPanel();
			notePanel.setAlignmentY(Component.TOP_ALIGNMENT);
			notePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
			notePanel.setBorder(new TitledBorder(null, MessageBundle.getMessage("angal.therapy.note"), //$NON-NLS-1$
					TitledBorder.LEADING, TitledBorder.TOP, null, null));
			notePanel.setLayout(new BorderLayout(0, 0));
			notePanel.add(getNoteScrollPane());
		}
		return notePanel;
	}

	private JScrollPane getNoteScrollPane() {
		if (noteScrollPane == null) {
			noteScrollPane = new JScrollPane(getNoteTextArea());
			noteScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		}
		return noteScrollPane;
	}

	private JTextArea getNoteTextArea() {
		if (noteTextArea == null) {
			noteTextArea = new JTextArea();
			noteTextArea.setLineWrap(true);
		}
		return noteTextArea;
	}

	private JPanel getIconMedicalPanel() {
		if (iconMedicalPanel == null) {
			iconMedicalPanel = new JPanel();
			iconMedicalPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
			iconMedicalPanel.setLayout(new BoxLayout(iconMedicalPanel, BoxLayout.X_AXIS));

			JPanel iconPanel = new JPanel();
			iconPanel.setLayout(new BoxLayout(iconPanel, BoxLayout.X_AXIS));
			IconButton iconButton = new IconButton(new ImageIcon("rsc/icons/medical_dialog.png")); //$NON-NLS-1$
			iconButton.setAlignmentY(Component.TOP_ALIGNMENT);
			iconPanel.add(iconButton);
			iconMedicalPanel.add(iconPanel);
			iconMedicalPanel.add(getMedicalsPanel());

		}
		return iconMedicalPanel;
	}

	private JPanel getIconFrequenciesPanel() {
		if (iconFrequenciesPanel == null) {
			iconFrequenciesPanel = new JPanel();
			iconFrequenciesPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
			iconFrequenciesPanel.setLayout(new BoxLayout(iconFrequenciesPanel, BoxLayout.X_AXIS));

			JPanel iconPanel = new JPanel();
			iconPanel.setLayout(new BoxLayout(iconPanel, BoxLayout.X_AXIS));
			IconButton iconButton = new IconButton(new ImageIcon("rsc/icons/clock_dialog.png")); //$NON-NLS-1$
			iconButton.setAlignmentY(Component.TOP_ALIGNMENT);
			iconPanel.add(iconButton);
			iconFrequenciesPanel.add(iconPanel);
			iconFrequenciesPanel.add(getFrequenciesPanel());
		}
		return iconFrequenciesPanel;
	}

	private JPanel getIconPeriodPanel() {
		if (iconPeriodPanel == null) {
			iconPeriodPanel = new JPanel();
			iconPeriodPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
			iconPeriodPanel.setLayout(new BoxLayout(iconPeriodPanel, BoxLayout.X_AXIS));

			JPanel iconPanel = new JPanel();
			iconPanel.setLayout(new BoxLayout(iconPanel, BoxLayout.X_AXIS));
			IconButton iconButton = new IconButton(new ImageIcon("rsc/icons/calendar_dialog.png")); //$NON-NLS-1$
			iconButton.setAlignmentY(Component.TOP_ALIGNMENT);
			iconPanel.add(iconButton);
			iconPeriodPanel.add(iconPanel);

			JPanel periodPanel = new JPanel();
			periodPanel.setAlignmentY(Component.TOP_ALIGNMENT);
			periodPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
			periodPanel.setLayout(new BoxLayout(periodPanel, BoxLayout.Y_AXIS));
			periodPanel.add(getDaysWeeksMonthsPanel());
			periodPanel.add(getStartEndDatePanel());
			iconPeriodPanel.add(periodPanel);
		}
		return iconPeriodPanel;
	}

	private JPanel getIconNotePanel() {
		if (iconNotePanel == null) {
			iconNotePanel = new JPanel();
			iconNotePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
			iconNotePanel.setLayout(new BoxLayout(iconNotePanel, BoxLayout.X_AXIS));

			JPanel iconPanel = new JPanel();
			iconPanel.setAlignmentY(Component.TOP_ALIGNMENT);
			iconPanel.setLayout(new BoxLayout(iconPanel, BoxLayout.X_AXIS));
			iconPanel.add(new IconButton(new ImageIcon("rsc/icons/list_dialog.png"))); //$NON-NLS-1$
			iconNotePanel.add(iconPanel);
			iconNotePanel.add(getNotePanel());
		}
		return iconNotePanel;
	}

	private JPanel getFrequenciesPanel() {
		if (frequenciesPanel == null) {
			frequenciesPanel = new JPanel();
			frequenciesPanel.setAlignmentY(Component.TOP_ALIGNMENT);
			frequenciesPanel.setAlignmentX(Component.RIGHT_ALIGNMENT);
			frequenciesPanel.setLayout(new BoxLayout(frequenciesPanel, BoxLayout.Y_AXIS));
			frequenciesPanel.add(getFrequencyInDayPanel());
			frequenciesPanel.add(getFrequencyInPeriodPanel());
		}
		return frequenciesPanel;
	}

	private JSlider getQuantitySlider() {
		if (jSliderQty == null) {
			jSliderQty = new JSlider(SLIDER_MIN_VALUE, SLIDER_MAX_VALUE);
			jSliderQty.setFont(new Font("Arial", Font.BOLD, 8)); //$NON-NLS-1$
			jSliderQty.setValue(SLIDER_MIN_VALUE);
			jSliderQty.setMajorTickSpacing(SLIDER_MAJOR_STEP_VALUE);
			jSliderQty.setMinorTickSpacing(SLIDER_MINOR_STEP_VALUE);
			jSliderQty.setPaintLabels(true);

			jSliderQty.addChangeListener(changeEvent -> {
				JSlider source = (JSlider) changeEvent.getSource();
				double value = source.getValue();
				jSpinnerQty.setValue(value);
				therapy.setQty(value);
			});
		}
		return jSliderQty;
	}

	private JLabel getEndDateField() {
		if (endDateLabel == null) {
			endDateLabel = new JLabel(""); //$NON-NLS-1$
			endDateLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
			updateEndDateLabel();
		}
		return endDateLabel;
	}

	private JButton getButtonCancel() {
		if (buttonCancel == null) {
			buttonCancel = new JButton(MessageBundle.getMessage("angal.common.cancel.btn"));
			buttonCancel.setMnemonic(MessageBundle.getMnemonic("angal.common.cancel.btn.key"));
			buttonCancel.addActionListener(actionEvent -> dispose());
		}
		return buttonCancel;
	}

	private JButton getButtonOK() {
		if (buttonOK == null) {
			buttonOK = new JButton(MessageBundle.getMessage("angal.common.ok.btn"));
			buttonOK.setMnemonic(MessageBundle.getMnemonic("angal.common.ok.btn.key"));
			buttonOK.addActionListener(actionEvent -> {
				/*
				 * Data extrapolation
				 */
				LocalDateTime startDate = therapyStartdate.getDateStartOfDay();
				if (startDate.isBefore(TimeTools.getDateToday0())) {
					MessageDialog.error(this, "angal.therapy.atherapycannotbedefinedforadatethatispast.msg");
					return;
				}
				LocalDateTime endDate = therapyEndDate;
				Medical medical = (Medical) medicalsList.getSelectedValue();
				if (medical == null) {
					MessageDialog.error(this, "angal.therapy.selectapharmaceutical");
					return;
				}
				Double qty = (Double) jSpinnerQty.getValue();
				if (qty == 0.) {
					MessageDialog.error(this, "angal.therapy.pleaseinsertaquantitygreaterthanzero.msg");
					return;
				}
				int therapyID = inserting ? 0 : therapy.getTherapyID();
				int unitID = 0; //TODO: UoM table
				int freqInDay = getFreqInDay();
				int freqInPeriod = Integer.parseInt(jSpinnerFreqInPeriod.getValue().toString());
				String note = noteTextArea.getText();
				boolean notify = false;
				boolean sms = false;

				try {
					thRow = therapyManager.getTherapyRow(therapyID, patID, startDate, endDate, medical, qty, unitID, freqInDay, freqInPeriod, note, notify, sms);
				} catch (OHServiceException e) {
					OHServiceExceptionUtil.showMessages(e, this);
				}
				setVisible(false);

			});
		}
		return buttonOK;
	}

	private int getFreqInDay() {
		for (JRadioButton button : radioButtonSet) {
			if (button.isSelected()) {
				freqInDay = radioButtonSet.indexOf(button) + 1;
			}
		}
		return freqInDay;
	}

	private JPanel getButtonPanel() {
		if (buttonPanel == null) {
			buttonPanel = new JPanel();
			buttonPanel.add(getButtonOK());
			buttonPanel.add(getButtonCancel());
		}
		return buttonPanel;
	}

	private JPanel getTherapyPanel() {
		if (therapyPanel == null) {
			therapyPanel = new JPanel();
			therapyPanel.setLayout(new GridLayout(0, 2, 0, 0));
			therapyPanel.add(getTherapyPanelWest());
			therapyPanel.add(getTherapyPanelEast());
		}
		return therapyPanel;
	}

	public Therapy getTherapy() {
		return therapy;
	}

	public void setTherapy(Therapy therapy) {
		this.therapy = therapy;
	}

	public TherapyRow getThRow() {
		return thRow;
	}

	public void setThRow(TherapyRow thRow) {
		this.thRow = thRow;
	}

}
