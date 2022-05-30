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
package org.isf.examination.gui;

import static org.isf.utils.Constants.DATE_FORMAT_DD_MM_YYYY_HH_MM;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.InputVerifier;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.OverlayLayout;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.JTextComponent;

import org.isf.examination.manager.ExaminationBrowserManager;
import org.isf.examination.model.GenderPatientExamination;
import org.isf.examination.model.PatientExamination;
import org.isf.generaldata.ExaminationParameters;
import org.isf.generaldata.GeneralData;
import org.isf.generaldata.MessageBundle;
import org.isf.menu.manager.Context;
import org.isf.stat.gui.report.GenericReportExamination;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.jobjects.GoodDateTimeChooser;
import org.isf.utils.jobjects.IconButton;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.ModalJFrame;
import org.isf.utils.jobjects.ScaledJSlider;
import org.isf.utils.jobjects.VoDoubleTextField;
import org.isf.utils.jobjects.VoIntegerTextField;
import org.isf.utils.jobjects.VoLimitedTextArea;

import com.github.lgooddatepicker.components.TimePicker;
import com.github.lgooddatepicker.zinternaltools.DateChangeEvent;

public class PatientExaminationEdit extends ModalJFrame {

	private static final long serialVersionUID = 1L;

	private JPanel jPanelExamination;
	private JPanel jPanelButtons;
	private JSlider jSliderHeight;
	private JSlider jSliderHGT;
	private JSlider jSliderDiuresisVolume;
	private ScaledJSlider jSliderWeight;
	private VoIntegerTextField jTextFieldHR;
	private VoIntegerTextField jTextFieldRR;
	private VoDoubleTextField jTextFieldTemp;
	private VoDoubleTextField jTextFieldSaturation;
	private VoIntegerTextField jTextFieldHGT;
	private VoIntegerTextField jTextFieldDiuresisVolume;
	private VoLimitedTextArea jTextAreaNote;
	private VoIntegerTextField jTextFieldHeight;
	private VoDoubleTextField jTextFieldWeight;
	private JPanel jPanelAPPanel;
	private JLabel jLabelAPMin;
	private JLabel jLabelAPSlash;
	private JLabel jLabelAPMax;
	private JSlider jSliderHR;
	private JSlider jSliderRR;
	private ScaledJSlider jSliderTemp;
	private ScaledJSlider jSliderSaturation;
	private JScrollPane jScrollPaneNote;
	private GoodDateTimeChooser jDateChooserDate;
	private VoIntegerTextField jSpinnerAPmin;
	private VoIntegerTextField jSpinnerAPmax;
	private JLabel jLabelHeightAbb;
	private JLabel jLabelWeightAbb;
	private JCheckBox jCheckBoxToggleAP;
	private JCheckBox jCheckBoxToggleHR;
	private JCheckBox jCheckBoxToggleTemp;
	private JCheckBox jCheckBoxToggleSaturation;
	private JCheckBox jCheckBoxToggleHGT;
	private JCheckBox jCheckBoxToggleDiuresisVolume;
	private JCheckBox jCheckBoxToggleDiuresisType;
	private JCheckBox jCheckBoxToggleBowel;
	private JCheckBox jCheckBoxToggleRR;
	private JCheckBox jCheckBoxToggleAusc;
	private JComboBox jComboBoxDiuresisType;
	private JComboBox jComboBoxBowel;
	private JComboBox jComboBoxAuscultation;
	private JButton jButtonSave;
	private JButton jButtonDelete;
	private JButton jButtonClose;
	private JButton jButtonPrint;
	private Action actionSavePatientExamination;
	private Action actionToggleAP;
	private Action actionToggleHR;
	private Action actionToggleRR;
	private Action actionToggleTemp;
	private Action actionToggleSaturation;
	private Action actionToggleHGT;
	private Action actionToggleDiuresisVolume;
	private Action actionToggleDiuresisType;
	private Action actionToggleBowel;
	private Action actionToggleAusc;
	private JPanel jPanelGender;
	private JLabel jLabelGender;
	private JEditorPane jEditorPaneBMI;
	private JPanel jPanelSummary;
	private JPanel jNotePanel;

	private PatientExamination patex;
	private boolean isMale;
	private double bmi;
	private boolean modified;

	private static final String PATH_FEMALE_GENDER = "rsc/images/sagoma-donna-132x300.jpg";
	private static final String PATH_MALE_GENDER = "rsc/images/sagoma-uomo-132x300.jpg";

	private final String[] columnNames = {
			MessageBundle.getMessage("angal.common.date.txt").toUpperCase(),
			MessageBundle.getMessage("angal.examination.heightabbr.col").toUpperCase(),
			MessageBundle.getMessage("angal.examination.weightabbr.col").toUpperCase(),
			MessageBundle.getMessage("angal.examination.arterialpressureabbr.col").toUpperCase(),
			MessageBundle.getMessage("angal.examination.heartrateabbr.col").toUpperCase(),
			MessageBundle.getMessage("angal.examination.temperatureabbr.col").toUpperCase(),
			MessageBundle.getMessage("angal.examination.saturationabbr.col").toUpperCase(),
			MessageBundle.getMessage("angal.examination.hgt.col").toUpperCase(),
			MessageBundle.getMessage("angal.examination.respiratoryrateabbr.col").toUpperCase(),
			MessageBundle.getMessage("angal.examination.diuresisvolume24habbr.col").toUpperCase(),
			MessageBundle.getMessage("angal.examination.diuresisabbr.col").toUpperCase(),
			MessageBundle.getMessage("angal.examination.bowelabbr.col").toUpperCase(),
			MessageBundle.getMessage("angal.examination.auscultationabbr.col").toUpperCase(),
			MessageBundle.getMessage("angal.examination.note.col").toUpperCase()
	};
	private final Class[] columnClasses = { String.class, Integer.class, Double.class, String.class, Integer.class, Double.class, Double.class, Integer.class,
			Integer.class, Integer.class, String.class, String.class, String.class, JButton.class };
	private int[] columnWidth = { 120, 40, 40, 100, 70, 50, 50, 50, 40, 50, 70, 70, 70, 70 };
	private int[] columnAlignment = { SwingConstants.LEFT, SwingConstants.CENTER, SwingConstants.CENTER, SwingConstants.CENTER, SwingConstants.CENTER,
			SwingConstants.CENTER, SwingConstants.CENTER, SwingConstants.CENTER, SwingConstants.CENTER, SwingConstants.CENTER, SwingConstants.CENTER,
			SwingConstants.CENTER, SwingConstants.CENTER, SwingConstants.CENTER };

	private JTable jTableSummary;

	private ExaminationBrowserManager examManager = Context.getApplicationContext().getBean(ExaminationBrowserManager.class);

	/**
	 * Create the dialog.
	 */
	public PatientExaminationEdit() {
		super();
		initComponents();
		updateGUI();
	}

	public PatientExaminationEdit(GenderPatientExamination gpatex) {
		super();
		this.patex = gpatex.getPatex();
		this.isMale = gpatex.isMale();
		initComponents();
		updateGUI();
	}

	public PatientExaminationEdit(Frame parent, GenderPatientExamination gpatex) {
		super();
		this.patex = gpatex.getPatex();
		this.isMale = gpatex.isMale();
		initComponents();
		updateGUI();
	}

	public PatientExaminationEdit(Dialog parent, GenderPatientExamination gpatex) {
		super();
		this.patex = gpatex.getPatex();
		this.isMale = gpatex.isMale();
		initComponents();
		updateGUI();
	}

	private void initComponents() {
		ExaminationParameters.initialize();
		setTitle(MessageBundle.getMessage("angal.examination.title"));
		getContentPane().add(getJPanelCenter(), BorderLayout.CENTER);
		getContentPane().add(getJPanelButtons(), BorderLayout.SOUTH);
		//updateSummary();
		updateBMI();
		pack();
		setResizable(false);
	}

	private JPanel getJPanelCenter() {
		JPanel centerPanel = new JPanel(new BorderLayout());

		JPanel dataPanel = new JPanel(new BorderLayout());
		dataPanel.add(getJPanelGender(), BorderLayout.WEST);
		dataPanel.add(getJPanelExamination(), BorderLayout.CENTER);
		dataPanel.add(getJPanelNote(), BorderLayout.EAST);

		centerPanel.add(dataPanel, BorderLayout.CENTER);
		centerPanel.add(getJPanelSummary(), BorderLayout.SOUTH);
		return centerPanel;
	}

	private JPanel getJPanelButtons() {
		if (jPanelButtons == null) {
			jPanelButtons = new JPanel();
			jPanelButtons.add(getJButtonSave());
			jPanelButtons.add(getJButtonDelete());
			jPanelButtons.add(getJButtonPrint());
			jPanelButtons.add(getJButtonClose());
		}
		return jPanelButtons;
	}

	//TODO: try to use JDOM...
	private void updateBMI() {
		this.bmi = patex.getBMI();
		StringBuilder bmi = new StringBuilder();
		bmi.append("<html><body>");
		bmi.append("<strong>");
		bmi.append(MessageBundle.getMessage("angal.examination.bmi")).append(':');
		bmi.append("<br />");
		bmi.append(this.bmi);
		bmi.append("<br /><br />");
		bmi.append("<font color=\"red\">");
		bmi.append(examManager.getBMIdescription(this.bmi));
		bmi.append("</font>");
		bmi.append("</strong>");
		bmi.append("</body></html>");
		jEditorPaneBMI.setText(bmi.toString());
	}

	private void updateGUI() {
		jDateChooserDate.setDateTime(patex.getPex_date());
		jTextFieldHeight.setText(String.valueOf(patex.getPex_height()));
		jSliderHeight.setValue(patex.getPex_height());
		jTextFieldWeight.setText(String.valueOf(patex.getPex_weight()));
		jSliderWeight.setValue(patex.getPex_weight() != null ? patex.getPex_weight() : 0);
		jSpinnerAPmin.setText(patex.getPex_ap_min() != null ? String.valueOf(patex.getPex_ap_min()) : "" + ExaminationParameters.AP_MIN_INIT);
		jSpinnerAPmax.setText(patex.getPex_ap_max() != null ? String.valueOf(patex.getPex_ap_max()) : "" + ExaminationParameters.AP_MAX_INIT);
		jSliderHR.setValue(patex.getPex_hr() != null ? patex.getPex_hr() : ExaminationParameters.HR_INIT);
		jTextFieldHR.setText(patex.getPex_hr() != null ? String.valueOf(patex.getPex_hr()) : "" + ExaminationParameters.HR_INIT);
		jSliderTemp.setValue(patex.getPex_temp());
		jTextFieldTemp.setText(patex.getPex_temp() != null ? String.valueOf(patex.getPex_temp()) : "" + ExaminationParameters.TEMP_INIT);
		jSliderSaturation.setValue(patex.getPex_sat());
		jTextFieldSaturation.setText(patex.getPex_sat() != null ? String.valueOf(patex.getPex_sat()) : "" + ExaminationParameters.SAT_INIT);
		jSliderHGT.setValue(patex.getPex_hgt() != null ? patex.getPex_hgt() : ExaminationParameters.HGT_INIT);
		jTextFieldHGT.setText(patex.getPex_hgt() != null ? String.valueOf(patex.getPex_hgt()) : "" + ExaminationParameters.HGT_INIT);
		jTextFieldDiuresisVolume.setText(patex.getPex_diuresis() != null ? String.valueOf(patex.getPex_diuresis()) : "" + ExaminationParameters.DIURESIS_INIT);
		jComboBoxDiuresisType.setSelectedItem(patex.getPex_diuresis_desc() != null ?
				examManager.getDiuresisDescriptionTranslated(patex.getPex_diuresis_desc()) :
				examManager.getDiuresisDescriptionTranslated(ExaminationParameters.DIURESIS_DESC_INIT));
		jComboBoxBowel.setSelectedItem(patex.getPex_bowel_desc() != null ?
				examManager.getBowelDescriptionTranslated(patex.getPex_bowel_desc()) :
				examManager.getBowelDescriptionTranslated(ExaminationParameters.BOWEL_DESC_INIT));
		jSliderRR.setValue(patex.getPex_rr() != null ? patex.getPex_rr() : ExaminationParameters.RR_INIT);
		jTextFieldRR.setText(patex.getPex_rr() != null ? String.valueOf(patex.getPex_rr()) : "" + ExaminationParameters.RR_INIT);
		jComboBoxAuscultation.setSelectedItem(patex.getPex_auscultation() != null ?
				examManager.getAuscultationTranslated(patex.getPex_auscultation()) :
				examManager.getAuscultationTranslated(ExaminationParameters.AUSCULTATION_INIT));
		jTextAreaNote.setText(patex.getPex_note());
		disableAP();
		disableHR();
		disableTemp();
		disableSaturation();
		disableRR();
		disableAuscultation();
		disableHGT();
		disableDiuresisVolume();
		disableDiuresisType();
		disableBowel();
		modified = false;
	}

	private JPanel getJPanelExamination() {
		if (jPanelExamination == null) {
			jPanelExamination = new JPanel();

			GridBagLayout gblPanelExamination = new GridBagLayout();
			gblPanelExamination.columnWidths = new int[] { 0, 0, 0, 0 };
			gblPanelExamination.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0 };
			gblPanelExamination.columnWeights = new double[] { 0.0, 0.0, 1.0, 0.0 };
			gblPanelExamination.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0 };
			jPanelExamination.setLayout(gblPanelExamination);

			JLabel jLabelDate = new JLabel(MessageBundle.getMessage("angal.common.date.txt"));
			GridBagConstraints gbcLabelDate = new GridBagConstraints();
			gbcLabelDate.anchor = GridBagConstraints.WEST;
			gbcLabelDate.insets = new Insets(10, 5, 5, 5);
			gbcLabelDate.gridx = 1;
			gbcLabelDate.gridy = 0;
			jPanelExamination.add(jLabelDate, gbcLabelDate);

			GridBagConstraints gbcDateChooserDate = new GridBagConstraints();
			gbcDateChooserDate.anchor = GridBagConstraints.WEST;
			gbcDateChooserDate.insets = new Insets(10, 5, 5, 5);
			gbcDateChooserDate.gridx = 2;
			gbcDateChooserDate.gridy = 0;
			jPanelExamination.add(getJDateChooserDate(), gbcDateChooserDate);

			{
				jLabelHeightAbb = new JLabel(MessageBundle.getMessage("angal.examination.heightabbr.txt")); //$NON-NLS-1$
				GridBagConstraints gbcLabelHeightAbb = new GridBagConstraints();
				gbcLabelHeightAbb.insets = new Insets(5, 5, 5, 5);
				gbcLabelHeightAbb.gridx = 0;
				gbcLabelHeightAbb.gridy = 1;
				jPanelExamination.add(jLabelHeightAbb, gbcLabelHeightAbb);

				JLabel jLabelHeight = new JLabel(MessageBundle.getMessage("angal.common.height.txt"));
				GridBagConstraints gbcLabelHeight = new GridBagConstraints();
				gbcLabelHeight.anchor = GridBagConstraints.WEST;
				gbcLabelHeight.insets = new Insets(5, 5, 5, 5);
				gbcLabelHeight.gridx = 1;
				gbcLabelHeight.gridy = 1;
				jPanelExamination.add(jLabelHeight, gbcLabelHeight);

				GridBagConstraints gbcSliderHeight = new GridBagConstraints();
				gbcSliderHeight.insets = new Insets(5, 5, 5, 5);
				gbcSliderHeight.fill = GridBagConstraints.HORIZONTAL;
				gbcSliderHeight.gridx = 2;
				gbcSliderHeight.gridy = 1;
				jPanelExamination.add(getJSliderHeight(), gbcSliderHeight);

				JLabel jLabelHeightUnit = new JLabel(MessageBundle.getMessage("angal.common.uom.centimeter"));
				GridBagConstraints gbcLabelHeightUnit = new GridBagConstraints();
				gbcLabelHeightUnit.insets = new Insets(5, 5, 5, 5);
				gbcLabelHeightUnit.gridx = 3;
				gbcLabelHeightUnit.gridy = 1;
				jPanelExamination.add(jLabelHeightUnit, gbcLabelHeightUnit);

				GridBagConstraints gbcTextFieldHeight = new GridBagConstraints();
				gbcTextFieldHeight.anchor = GridBagConstraints.WEST;
				gbcTextFieldHeight.insets = new Insets(0, 0, 5, 5);
				gbcTextFieldHeight.gridx = 4;
				gbcTextFieldHeight.gridy = 1;
				jPanelExamination.add(getJTextFieldHeight(), gbcTextFieldHeight);
			}

			{
				jLabelWeightAbb = new JLabel(MessageBundle.getMessage("angal.examination.weightabbr.txt"));
				GridBagConstraints gbcLabelWeightAbb = new GridBagConstraints();
				gbcLabelWeightAbb.insets = new Insets(5, 5, 5, 5);
				gbcLabelWeightAbb.gridx = 0;
				gbcLabelWeightAbb.gridy = 2;
				jPanelExamination.add(jLabelWeightAbb, gbcLabelWeightAbb);

				JLabel jLabelWeight = new JLabel(MessageBundle.getMessage("angal.common.weight.txt"));
				GridBagConstraints gbcLabelWeight = new GridBagConstraints();
				gbcLabelWeight.anchor = GridBagConstraints.WEST;
				gbcLabelWeight.insets = new Insets(5, 5, 5, 5);
				gbcLabelWeight.gridx = 1;
				gbcLabelWeight.gridy = 2;
				jPanelExamination.add(jLabelWeight, gbcLabelWeight);

				GridBagConstraints gbcSliderWeight = new GridBagConstraints();
				gbcSliderWeight.insets = new Insets(5, 5, 5, 5);
				gbcSliderWeight.fill = GridBagConstraints.HORIZONTAL;
				gbcSliderWeight.gridx = 2;
				gbcSliderWeight.gridy = 2;
				jPanelExamination.add(getJSliderWeight(), gbcSliderWeight);

				JLabel jLabelWeightUnit = new JLabel(MessageBundle.getMessage("angal.common.uom.kg"));
				GridBagConstraints gbcLabelWeightUnit = new GridBagConstraints();
				gbcLabelWeightUnit.insets = new Insets(5, 5, 5, 5);
				gbcLabelWeightUnit.gridx = 3;
				gbcLabelWeightUnit.gridy = 2;
				jPanelExamination.add(jLabelWeightUnit, gbcLabelWeightUnit);

				GridBagConstraints gbcTextFieldWeight = new GridBagConstraints();
				gbcTextFieldWeight.anchor = GridBagConstraints.WEST;
				gbcTextFieldWeight.insets = new Insets(0, 0, 5, 5);
				gbcTextFieldWeight.gridx = 4;
				gbcTextFieldWeight.gridy = 2;
				jPanelExamination.add(getJTextFieldWeight(), gbcTextFieldWeight);
			}

			{
				GridBagConstraints gbcCheckBoxAP= new GridBagConstraints();
				gbcCheckBoxAP.insets = new Insets(5, 5, 5, 5);
				gbcCheckBoxAP.gridx = 0;
				gbcCheckBoxAP.gridy = 3;
				jPanelExamination.add(getJCheckBoxAP(), gbcCheckBoxAP);

				JLabel jLabelAPmin = new JLabel(MessageBundle.getMessage("angal.examination.arterialpressure")); //$NON-NLS-1$
				GridBagConstraints labelGbc_3 = new GridBagConstraints();
				labelGbc_3.anchor = GridBagConstraints.WEST;
				labelGbc_3.insets = new Insets(5, 5, 5, 5);
				labelGbc_3.gridx = 1;
				labelGbc_3.gridy = 3;
				jPanelExamination.add(jLabelAPmin, labelGbc_3);

				GridBagConstraints gbcPanelApPanel = new GridBagConstraints();
				gbcPanelApPanel.insets = new Insets(0, 0, 5, 5);
				gbcPanelApPanel.fill = GridBagConstraints.BOTH;
				gbcPanelApPanel.gridx = 2;
				gbcPanelApPanel.gridy = 3;
				jPanelExamination.add(getJPanelAPPanel(), gbcPanelApPanel);

				JLabel jLabelAPUnit = new JLabel(MessageBundle.getMessage("angal.common.uom.mmHg"));
				GridBagConstraints gbcLabelAPUnit = new GridBagConstraints();
				gbcLabelAPUnit.insets = new Insets(5, 5, 5, 5);
				gbcLabelAPUnit.gridx = 3;
				gbcLabelAPUnit.gridy = 3;
				jPanelExamination.add(jLabelAPUnit, gbcLabelAPUnit);
			}

			{
				GridBagConstraints gbcCheckBoxToggleHR = new GridBagConstraints();
				gbcCheckBoxToggleHR.insets = new Insets(5, 5, 5, 5);
				gbcCheckBoxToggleHR.gridx = 0;
				gbcCheckBoxToggleHR.gridy = 4;
				jPanelExamination.add(getJCheckBoxToggleHR(), gbcCheckBoxToggleHR);

				JLabel jLabelHR = new JLabel(MessageBundle.getMessage("angal.examination.heartrate")); //$NON-NLS-1$
				GridBagConstraints gbcLabelHR = new GridBagConstraints();
				gbcLabelHR.anchor = GridBagConstraints.WEST;
				gbcLabelHR.insets = new Insets(5, 5, 5, 5);
				gbcLabelHR.gridx = 1;
				gbcLabelHR.gridy = 4;
				jPanelExamination.add(jLabelHR, gbcLabelHR);

				GridBagConstraints gbcSliderHR = new GridBagConstraints();
				gbcSliderHR.insets = new Insets(5, 5, 5, 5);
				gbcSliderHR.fill = GridBagConstraints.HORIZONTAL;
				gbcSliderHR.gridx = 2;
				gbcSliderHR.gridy = 4;
				jPanelExamination.add(getJSliderHR(), gbcSliderHR);

				JLabel jLabelHRUnit = new JLabel(MessageBundle.getMessage("angal.common.uom.bpm"));
				GridBagConstraints gbcLabelHRUnit = new GridBagConstraints();
				gbcLabelHRUnit.insets = new Insets(5, 5, 5, 5);
				gbcLabelHRUnit.gridx = 3;
				gbcLabelHRUnit.gridy = 4;
				jPanelExamination.add(jLabelHRUnit, gbcLabelHRUnit);

				GridBagConstraints gbcTextFieldHR = new GridBagConstraints();
				gbcTextFieldHR.anchor = GridBagConstraints.WEST;
				gbcTextFieldHR.insets = new Insets(5, 0, 5, 5);
				gbcTextFieldHR.gridx = 4;
				gbcTextFieldHR.gridy = 4;
				jPanelExamination.add(getJTextFieldHR(), gbcTextFieldHR);
			}

			{
				GridBagConstraints gbcCheckBoxToggleTemp = new GridBagConstraints();
				gbcCheckBoxToggleTemp.insets = new Insets(5, 5, 5, 5);
				gbcCheckBoxToggleTemp.gridx = 0;
				gbcCheckBoxToggleTemp.gridy = 5;
				jPanelExamination.add(getJCheckBoxToggleTemp(), gbcCheckBoxToggleTemp);

				JLabel jLabelTemp = new JLabel(MessageBundle.getMessage("angal.examination.temperature")); //$NON-NLS-1$
				GridBagConstraints gbcLabelTemp = new GridBagConstraints();
				gbcLabelTemp.anchor = GridBagConstraints.WEST;
				gbcLabelTemp.insets = new Insets(5, 5, 5, 5);
				gbcLabelTemp.gridx = 1;
				gbcLabelTemp.gridy = 5;
				jPanelExamination.add(jLabelTemp, gbcLabelTemp);

				GridBagConstraints gbcSliderTemp = new GridBagConstraints();
				gbcSliderTemp.insets = new Insets(5, 5, 5, 5);
				gbcSliderTemp.fill = GridBagConstraints.HORIZONTAL;
				gbcSliderTemp.gridx = 2;
				gbcSliderTemp.gridy = 5;
				jPanelExamination.add(getJSliderTemp(), gbcSliderTemp);

				JLabel jLabelTempUnit = new JLabel(MessageBundle.getMessage("angal.common.uom.celsius"));
				GridBagConstraints gbcLabelTempUnit = new GridBagConstraints();
				gbcLabelTempUnit.insets = new Insets(5, 5, 5, 5);
				gbcLabelTempUnit.gridx = 3;
				gbcLabelTempUnit.gridy = 5;
				jPanelExamination.add(jLabelTempUnit, gbcLabelTempUnit);

				GridBagConstraints gbcTextFieldTemp = new GridBagConstraints();
				gbcTextFieldTemp.anchor = GridBagConstraints.WEST;
				gbcTextFieldTemp.insets = new Insets(5, 0, 5, 5);
				gbcTextFieldTemp.gridx = 4;
				gbcTextFieldTemp.gridy = 5;
				jPanelExamination.add(getJTextFieldTemp(), gbcTextFieldTemp);
			}

			{
				GridBagConstraints gbcCheckBoxToggleSaturation = new GridBagConstraints();
				gbcCheckBoxToggleSaturation.insets = new Insets(5, 5, 5, 5);
				gbcCheckBoxToggleSaturation.gridx = 0;
				gbcCheckBoxToggleSaturation.gridy = 6;
				jPanelExamination.add(getJCheckBoxToggleSaturation(), gbcCheckBoxToggleSaturation);

				JLabel jLabelSaturation = new JLabel(MessageBundle.getMessage("angal.examination.saturation")); //$NON-NLS-1$
				GridBagConstraints gbcLabelSaturation = new GridBagConstraints();
				gbcLabelSaturation.anchor = GridBagConstraints.WEST;
				gbcLabelSaturation.insets = new Insets(5, 5, 5, 5);
				gbcLabelSaturation.gridx = 1;
				gbcLabelSaturation.gridy = 6;
				jPanelExamination.add(jLabelSaturation, gbcLabelSaturation);

				GridBagConstraints gbcSliderSaturation = new GridBagConstraints();
				gbcSliderSaturation.insets = new Insets(5, 5, 5, 5);
				gbcSliderSaturation.fill = GridBagConstraints.HORIZONTAL;
				gbcSliderSaturation.gridx = 2;
				gbcSliderSaturation.gridy = 6;
				jPanelExamination.add(getJSliderSaturation(), gbcSliderSaturation);

				JLabel jLabelSaturationUnit = new JLabel(MessageBundle.getMessage("angal.common.uom.percentage"));
				GridBagConstraints gbcLabelSaturationUnit = new GridBagConstraints();
				gbcLabelSaturationUnit.insets = new Insets(5, 5, 5, 5);
				gbcLabelSaturationUnit.gridx = 3;
				gbcLabelSaturationUnit.gridy = 6;
				jPanelExamination.add(jLabelSaturationUnit, gbcLabelSaturationUnit);

				GridBagConstraints gbcTextFieldSaturation = new GridBagConstraints();
				gbcTextFieldSaturation.anchor = GridBagConstraints.WEST;
				gbcTextFieldSaturation.insets = new Insets(5, 0, 5, 5);
				gbcTextFieldSaturation.gridx = 4;
				gbcTextFieldSaturation.gridy = 6;
				jPanelExamination.add(getJTextFieldSaturation(), gbcTextFieldSaturation);
			}

			{
				GridBagConstraints gbcCheckBoxToggleHGT = new GridBagConstraints();
				gbcCheckBoxToggleHGT.insets = new Insets(5, 5, 5, 5);
				gbcCheckBoxToggleHGT.gridx = 0;
				gbcCheckBoxToggleHGT.gridy = 7;
				jPanelExamination.add(getJCheckBoxToggleHGT(), gbcCheckBoxToggleHGT);

				JLabel jLabelHGT = new JLabel(MessageBundle.getMessage("angal.examination.hgt")); //$NON-NLS-1$
				GridBagConstraints gbcLabelHGT = new GridBagConstraints();
				gbcLabelHGT.anchor = GridBagConstraints.WEST;
				gbcLabelHGT.insets = new Insets(5, 5, 5, 5);
				gbcLabelHGT.gridx = 1;
				gbcLabelHGT.gridy = 7;
				jPanelExamination.add(jLabelHGT, gbcLabelHGT);

				GridBagConstraints gbcSliderHGT = new GridBagConstraints();
				gbcSliderHGT.insets = new Insets(5, 5, 5, 5);
				gbcSliderHGT.fill = GridBagConstraints.HORIZONTAL;
				gbcSliderHGT.gridx = 2;
				gbcSliderHGT.gridy = 7;
				jPanelExamination.add(getJSliderHGT(), gbcSliderHGT);

				JLabel jLabelHGTUnit = new JLabel(MessageBundle.getMessage("angal.common.uom.mgdl"));
				GridBagConstraints gbcLabelHGTUnit = new GridBagConstraints();
				gbcLabelHGTUnit.insets = new Insets(5, 5, 5, 5);
				gbcLabelHGTUnit.gridx = 3;
				gbcLabelHGTUnit.gridy = 7;
				jPanelExamination.add(jLabelHGTUnit, gbcLabelHGTUnit);

				GridBagConstraints gbcTextFieldHGT = new GridBagConstraints();
				gbcTextFieldHGT.anchor = GridBagConstraints.WEST;
				gbcTextFieldHGT.insets = new Insets(5, 0, 5, 5);
				gbcTextFieldHGT.gridx = 4;
				gbcTextFieldHGT.gridy = 7;
				jPanelExamination.add(getJTextFieldHGT(), gbcTextFieldHGT);
			}

			{
				GridBagConstraints gbcCheckBoxToggleRR = new GridBagConstraints();
				gbcCheckBoxToggleRR.insets = new Insets(5, 5, 5, 5);
				gbcCheckBoxToggleRR.gridx = 0;
				gbcCheckBoxToggleRR.gridy = 8;
				jPanelExamination.add(getJCheckBoxToggleRR(), gbcCheckBoxToggleRR);

				JLabel jLabelRR = new JLabel(MessageBundle.getMessage("angal.examination.respiratoryrate")); //$NON-NLS-1$
				GridBagConstraints gbcLabelRR = new GridBagConstraints();
				gbcLabelRR.anchor = GridBagConstraints.WEST;
				gbcLabelRR.insets = new Insets(5, 5, 5, 5);
				gbcLabelRR.gridx = 1;
				gbcLabelRR.gridy = 8;
				jPanelExamination.add(jLabelRR, gbcLabelRR);

				GridBagConstraints gbcSliderRR = new GridBagConstraints();
				gbcSliderRR.insets = new Insets(5, 5, 5, 5);
				gbcSliderRR.gridx = 2;
				gbcSliderRR.gridy = 8;
				jPanelExamination.add(getJSliderRR(), gbcSliderRR);

				JLabel jLabelRRUnit = new JLabel(MessageBundle.getMessage("angal.examination.respiratoryrateunit"));
				GridBagConstraints gbcLabelRRUnit = new GridBagConstraints();
				gbcLabelRRUnit.insets = new Insets(5, 5, 5, 5);
				gbcLabelRRUnit.gridx = 3;
				gbcLabelRRUnit.gridy = 8;
				jPanelExamination.add(jLabelRRUnit, gbcLabelRRUnit);

				GridBagConstraints gbcTextFieldRR = new GridBagConstraints();
				gbcTextFieldRR.anchor = GridBagConstraints.WEST;
				gbcTextFieldRR.insets = new Insets(5, 0, 5, 0);
				gbcTextFieldRR.gridx = 4;
				gbcTextFieldRR.gridy = 8;
				jPanelExamination.add(getJTextFieldRR(), gbcTextFieldRR);
			}

			{
				GridBagConstraints gbcCheckBoxToggleDiuresisVolume = new GridBagConstraints();
				gbcCheckBoxToggleDiuresisVolume.insets = new Insets(5, 5, 5, 5);
				gbcCheckBoxToggleDiuresisVolume.gridx = 0;
				gbcCheckBoxToggleDiuresisVolume.gridy = 9;
				jPanelExamination.add(getJCheckBoxToggleDiuresisVolume(), gbcCheckBoxToggleDiuresisVolume);

				JLabel jLabelDiuresisVolume = new JLabel(MessageBundle.getMessage("angal.examination.diuresisvolume24h")); //$NON-NLS-1$
				GridBagConstraints gbcLabelDiuresisVolume = new GridBagConstraints();
				gbcLabelDiuresisVolume.anchor = GridBagConstraints.WEST;
				gbcLabelDiuresisVolume.insets = new Insets(5, 5, 5, 5);
				gbcLabelDiuresisVolume.gridx = 1;
				gbcLabelDiuresisVolume.gridy = 9;
				jPanelExamination.add(jLabelDiuresisVolume, gbcLabelDiuresisVolume);

				GridBagConstraints gbcSliderDiuresisVolume = new GridBagConstraints();
				gbcSliderDiuresisVolume.insets = new Insets(5, 5, 5, 5);
				gbcSliderDiuresisVolume.fill = GridBagConstraints.HORIZONTAL;
				gbcSliderDiuresisVolume.gridx = 2;
				gbcSliderDiuresisVolume.gridy = 9;
				jPanelExamination.add(getJSliderDiuresisVolume(), gbcSliderDiuresisVolume);

				JLabel jLabelDiuresisVolumeUnit = new JLabel(MessageBundle.getMessage("angal.common.uom.milliliter"));
				GridBagConstraints gbcLabelDiuresisVolumeUnit = new GridBagConstraints();
				gbcLabelDiuresisVolumeUnit.insets = new Insets(5, 5, 5, 5);
				gbcLabelDiuresisVolumeUnit.gridx = 3;
				gbcLabelDiuresisVolumeUnit.gridy = 9;
				jPanelExamination.add(jLabelDiuresisVolumeUnit, gbcLabelDiuresisVolumeUnit);

				GridBagConstraints gbcTextFieldDiuresisVolume = new GridBagConstraints();
				gbcTextFieldDiuresisVolume.anchor = GridBagConstraints.WEST;
				gbcTextFieldDiuresisVolume.insets = new Insets(5, 0, 5, 5);
				gbcTextFieldDiuresisVolume.gridx = 4;
				gbcTextFieldDiuresisVolume.gridy = 9;
				jPanelExamination.add(getJTextFieldDiuresisVolume(), gbcTextFieldDiuresisVolume);
			}

			{
				GridBagConstraints gbcCheckBoxToggleDiuresisType = new GridBagConstraints();
				gbcCheckBoxToggleDiuresisType.insets = new Insets(5, 5, 5, 5);
				gbcCheckBoxToggleDiuresisType.gridx = 0;
				gbcCheckBoxToggleDiuresisType.gridy = 10;
				jPanelExamination.add(getJCheckBoxToggleDiuresisType(), gbcCheckBoxToggleDiuresisType);

				JLabel jLabelDiuresis = new JLabel(MessageBundle.getMessage("angal.examination.diuresis")); //$NON-NLS-1$
				GridBagConstraints gbcLabelDiuresis = new GridBagConstraints();
				gbcLabelDiuresis.anchor = GridBagConstraints.WEST;
				gbcLabelDiuresis.insets = new Insets(5, 5, 5, 5);
				gbcLabelDiuresis.gridx = 1;
				gbcLabelDiuresis.gridy = 10;
				jPanelExamination.add(jLabelDiuresis, gbcLabelDiuresis);

				GridBagConstraints gbcComboBoxDiuresisType = new GridBagConstraints();
				gbcComboBoxDiuresisType.anchor = GridBagConstraints.CENTER;
				gbcComboBoxDiuresisType.insets = new Insets(5, 0, 5, 5);
				gbcComboBoxDiuresisType.gridx = 2;
				gbcComboBoxDiuresisType.gridy = 10;
				jPanelExamination.add(getJComboBoxDiuresisType(), gbcComboBoxDiuresisType);
			}

			{
				GridBagConstraints gbcCheckBoxToggleBowel = new GridBagConstraints();
				gbcCheckBoxToggleBowel.insets = new Insets(5, 5, 5, 5);
				gbcCheckBoxToggleBowel.gridx = 0;
				gbcCheckBoxToggleBowel.gridy = 11;
				jPanelExamination.add(getJCheckBoxToggleBowel(), gbcCheckBoxToggleBowel);

				JLabel jLabelBowel = new JLabel(MessageBundle.getMessage("angal.examination.bowel")); //$NON-NLS-1$
				GridBagConstraints gbcLabelBowel = new GridBagConstraints();
				gbcLabelBowel.anchor = GridBagConstraints.WEST;
				gbcLabelBowel.insets = new Insets(5, 5, 5, 5);
				gbcLabelBowel.gridx = 1;
				gbcLabelBowel.gridy = 11;
				jPanelExamination.add(jLabelBowel, gbcLabelBowel);

				GridBagConstraints gbcComboBoxBowel = new GridBagConstraints();
				gbcComboBoxBowel.anchor = GridBagConstraints.CENTER;
				gbcComboBoxBowel.insets = new Insets(5, 0, 5, 5);
				gbcComboBoxBowel.gridx = 2;
				gbcComboBoxBowel.gridy = 11;
				jPanelExamination.add(getJComboBoxBowel(), gbcComboBoxBowel);
			}

			{
				GridBagConstraints gbcCheckBoxToggleAusc = new GridBagConstraints();
				gbcCheckBoxToggleAusc.insets = new Insets(5, 5, 5, 5);
				gbcCheckBoxToggleAusc.gridx = 0;
				gbcCheckBoxToggleAusc.gridy = 12;
				jPanelExamination.add(getJCheckBoxToggleAusc(), gbcCheckBoxToggleAusc);

				JLabel jLabelAusc = new JLabel(MessageBundle.getMessage("angal.examination.auscultation")); //$NON-NLS-1$
				GridBagConstraints gbcLabelAusc = new GridBagConstraints();
				gbcLabelAusc.anchor = GridBagConstraints.WEST;
				gbcLabelAusc.insets = new Insets(5, 5, 5, 5);
				gbcLabelAusc.gridx = 1;
				gbcLabelAusc.gridy = 12;
				jPanelExamination.add(jLabelAusc, gbcLabelAusc);

				GridBagConstraints gbcComboBoxAuscultation = new GridBagConstraints();
				gbcComboBoxAuscultation.anchor = GridBagConstraints.CENTER;
				gbcComboBoxAuscultation.insets = new Insets(5, 0, 5, 0);
				gbcComboBoxAuscultation.gridx = 2;
				gbcComboBoxAuscultation.gridy = 12;
				jPanelExamination.add(getJComboBoxAuscultation(), gbcComboBoxAuscultation);
			}
		}
		return jPanelExamination;
	}

	private JComboBox getJComboBoxDiuresisType() {
		if (jComboBoxDiuresisType == null) {
			jComboBoxDiuresisType = new JComboBox();
			List<String> diuresisDescription = examManager.getDiuresisDescriptionList();
			for (String description : diuresisDescription) {
				jComboBoxDiuresisType.addItem(description);
			}
			jComboBoxDiuresisType.addItemListener(itemEvent -> {
				if (itemEvent.getStateChange() == ItemEvent.SELECTED) {
					patex.setPex_diuresis_desc(examManager.getDiuresisDescriptionKey((String) itemEvent.getItem()));
				}
			});
		}
		return jComboBoxDiuresisType;
	}

	private JComboBox getJComboBoxBowel() {
		if (jComboBoxBowel == null) {
			jComboBoxBowel = new JComboBox();
			List<String> bowelDescription = examManager.getBowelDescriptionList();
			for (String description : bowelDescription) {
				jComboBoxBowel.addItem(description);
			}
			jComboBoxBowel.addItemListener(itemEvent -> {
				if (itemEvent.getStateChange() == ItemEvent.SELECTED) {
					patex.setPex_bowel_desc(examManager.getBowelDescriptionKey((String) itemEvent.getItem()));
				}
			});
		}
		return jComboBoxBowel;
	}

	private JCheckBox getJCheckBoxToggleHGT() {
		if (jCheckBoxToggleHGT == null) {
			jCheckBoxToggleHGT = new JCheckBox(""); //$NON-NLS-1$
			jCheckBoxToggleHGT.setAction(getActionToggleHGT());
			jCheckBoxToggleHGT.addFocusListener(new CheckBoxFocus());
			jCheckBoxToggleHGT.setFocusPainted(true);
		}
		return jCheckBoxToggleHGT;
	}

	private JCheckBox getJCheckBoxToggleBowel() {
		if (jCheckBoxToggleBowel == null) {
			jCheckBoxToggleBowel = new JCheckBox(""); //$NON-NLS-1$
			jCheckBoxToggleBowel.setAction(getActionToggleBowel());
			jCheckBoxToggleBowel.addFocusListener(new CheckBoxFocus());
			jCheckBoxToggleBowel.setFocusPainted(true);
		}
		return jCheckBoxToggleBowel;
	}

	private JCheckBox getJCheckBoxToggleDiuresisVolume() {
		if (jCheckBoxToggleDiuresisVolume == null) {
			jCheckBoxToggleDiuresisVolume = new JCheckBox(""); //$NON-NLS-1$
			jCheckBoxToggleDiuresisVolume.setAction(getActionToggleDiuresisVolume());
			jCheckBoxToggleDiuresisVolume.addFocusListener(new CheckBoxFocus());
			jCheckBoxToggleDiuresisVolume.setFocusPainted(true);
		}
		return jCheckBoxToggleDiuresisVolume;
	}

	private JCheckBox getJCheckBoxToggleDiuresisType() {
		if (jCheckBoxToggleDiuresisType == null) {
			jCheckBoxToggleDiuresisType = new JCheckBox(""); //$NON-NLS-1$
			jCheckBoxToggleDiuresisType.setAction(getActionToggleDiuresisType());
			jCheckBoxToggleDiuresisType.addFocusListener(new CheckBoxFocus());
			jCheckBoxToggleDiuresisType.setFocusPainted(true);
		}
		return jCheckBoxToggleDiuresisType;
	}

	private JCheckBox getJCheckBoxToggleSaturation() {
		if (jCheckBoxToggleSaturation == null) {
			jCheckBoxToggleSaturation = new JCheckBox(""); //$NON-NLS-1$
			jCheckBoxToggleSaturation.setAction(getActionToggleSaturation());
			jCheckBoxToggleSaturation.addFocusListener(new CheckBoxFocus());
			jCheckBoxToggleSaturation.setFocusPainted(true);
		}
		return jCheckBoxToggleSaturation;
	}

	private JCheckBox getJCheckBoxToggleRR() {
		if (jCheckBoxToggleRR == null) {
			jCheckBoxToggleRR = new JCheckBox(""); //$NON-NLS-1$
			jCheckBoxToggleRR.setAction(getActionToggleRR());
			jCheckBoxToggleRR.addFocusListener(new CheckBoxFocus());
			jCheckBoxToggleRR.setFocusPainted(true);
		}
		return jCheckBoxToggleRR;
	}

	private JCheckBox getJCheckBoxToggleAusc() {
		if (jCheckBoxToggleAusc == null) {
			jCheckBoxToggleAusc = new JCheckBox(""); //$NON-NLS-1$
			jCheckBoxToggleAusc.setAction(getActionToggleAusc());
			jCheckBoxToggleAusc.addFocusListener(new CheckBoxFocus());
			jCheckBoxToggleAusc.setFocusPainted(true);
		}
		return jCheckBoxToggleAusc;
	}

	private JCheckBox getJCheckBoxToggleTemp() {
		if (jCheckBoxToggleTemp == null) {
			jCheckBoxToggleTemp = new JCheckBox(""); //$NON-NLS-1$
			jCheckBoxToggleTemp.setAction(getActionToggleTemp());
			jCheckBoxToggleTemp.addFocusListener(new CheckBoxFocus());
			jCheckBoxToggleTemp.setFocusPainted(true);
		}
		return jCheckBoxToggleTemp;
	}

	private JCheckBox getJCheckBoxToggleHR() {
		if (jCheckBoxToggleHR == null) {
			jCheckBoxToggleHR = new JCheckBox(""); //$NON-NLS-1$
			jCheckBoxToggleHR.setAction(getActionToggleHR());
			jCheckBoxToggleHR.addFocusListener(new CheckBoxFocus());
			jCheckBoxToggleHR.setFocusPainted(true);
		}
		return jCheckBoxToggleHR;
	}

	private JCheckBox getJCheckBoxAP() {
		if (jCheckBoxToggleAP == null) {
			jCheckBoxToggleAP = new JCheckBox(""); //$NON-NLS-1$
			jCheckBoxToggleAP.setAction(getActionToggleAP());
			jCheckBoxToggleAP.addFocusListener(new CheckBoxFocus());
			jCheckBoxToggleAP.setFocusPainted(true);
		}
		return jCheckBoxToggleAP;
	}

	private GoodDateTimeChooser getJDateChooserDate() {
		if (jDateChooserDate == null) {
			jDateChooserDate = new GoodDateTimeChooser(null);
			jDateChooserDate.addDateTimeChangeListener(event -> {
				DateChangeEvent dateChangeEvent = event.getDateChangeEvent();
				if (dateChangeEvent != null) {
					// if the time is blank set it to the current time; otherwise leave it alone
					TimePicker timePicker = event.getTimePicker();
					if (timePicker.getTime() == null) {
						timePicker.setTime(LocalTime.now());
					}
				}
				patex.setPex_date(jDateChooserDate.getLocalDateTime());
			});
		}
		return jDateChooserDate;
	}

	private VoLimitedTextArea getJTextAreaNote() {
		if (jTextAreaNote == null) {
			jTextAreaNote = new VoLimitedTextArea(PatientExamination.PEX_NOTE_LENGTH, 6, 20);
			jTextAreaNote.setLineWrap(true);
			jTextAreaNote.setWrapStyleWord(true);
			jTextAreaNote.setMargin(new Insets(0, 5, 0, 0));
			jTextAreaNote.addFocusListener(new FocusAdapter() {

				@Override
				public void focusLost(FocusEvent e) {
					super.focusLost(e);
					patex.setPex_note(jTextAreaNote.getText());
					modified = true;
				}
			});
		}
		jTextAreaNote.setSize(jTextAreaNote.getPreferredSize());
		return jTextAreaNote;
	}

	private JPanel getJPanelNote() {
		if (jNotePanel == null) {
			jNotePanel = new JPanel(new BorderLayout());

			GridBagLayout gblNotePanel = new GridBagLayout();
			gblNotePanel.rowWeights = new double[] { 0.0, 1.0 };
			jNotePanel.setLayout(gblNotePanel);

			JLabel jLabelNote = new JLabel(MessageBundle.getMessage("angal.examination.note"), new ImageIcon("rsc/icons/list_button.png"),
					SwingConstants.LEFT);
			GridBagConstraints gbcLabelNote = new GridBagConstraints();
			gbcLabelNote.anchor = GridBagConstraints.WEST;
			gbcLabelNote.insets = new Insets(5, 5, 5, 5);
			gbcLabelNote.gridx = 0;
			gbcLabelNote.gridy = 0;
			jNotePanel.add(jLabelNote, gbcLabelNote);

			GridBagConstraints gbcScrollPaneNote = new GridBagConstraints();
			gbcScrollPaneNote.fill = GridBagConstraints.BOTH;
			gbcScrollPaneNote.insets = new Insets(5, 5, 5, 5);
			gbcScrollPaneNote.gridx = 0;
			gbcScrollPaneNote.gridy = 1;
			jNotePanel.add(getJScrollPaneNote(), gbcScrollPaneNote);
		}
		return jNotePanel;
	}

	private JScrollPane getJScrollPaneNote() {
		if (jScrollPaneNote == null) {
			jScrollPaneNote = new JScrollPane();
			jScrollPaneNote.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
			VoLimitedTextArea text = getJTextAreaNote();
			jScrollPaneNote.setViewportView(text);
			jScrollPaneNote.setPreferredSize(text.getPreferredSize());
		}
		return jScrollPaneNote;
	}

	private JPanel getJPanelAPPanel() {
		if (jPanelAPPanel == null) {
			jPanelAPPanel = new JPanel();
			jLabelAPMin = new JLabel(MessageBundle.getMessage("angal.examination.ap.min")); //$NON-NLS-1$
			jPanelAPPanel.add(jLabelAPMin);
			jPanelAPPanel.add(getJSpinnerAPmin());
			jLabelAPSlash = new JLabel("/"); //$NON-NLS-1$
			jPanelAPPanel.add(jLabelAPSlash);
			jPanelAPPanel.add(getJSpinnerAPmax());
			jLabelAPMax = new JLabel(MessageBundle.getMessage("angal.examination.ap.max")); //$NON-NLS-1$
			jPanelAPPanel.add(jLabelAPMax);
		}
		return jPanelAPPanel;
	}

	private VoIntegerTextField getJSpinnerAPmin() {
		if (jSpinnerAPmin == null) {
			jSpinnerAPmin = new VoIntegerTextField(0, 3);
			jSpinnerAPmin.setInputVerifier(new MinMaxIntegerInputVerifier(0));
			jSpinnerAPmin.addFocusListener(new FocusListener() {

				@Override
				public void focusLost(FocusEvent e) {
					patex.setPex_ap_min((Integer) jSpinnerAPmin.getValue());
					modified = true;
				}

				@Override
				public void focusGained(FocusEvent e) {
					((JTextField) e.getSource()).selectAll();
				}
			});
		}
		return jSpinnerAPmin;
	}

	private VoIntegerTextField getJSpinnerAPmax() {
		if (jSpinnerAPmax == null) {
			jSpinnerAPmax = new VoIntegerTextField(0, 3);
			jSpinnerAPmax.setInputVerifier(new MinMaxIntegerInputVerifier(0));
			jSpinnerAPmax.addFocusListener(new FocusListener() {

				@Override
				public void focusLost(FocusEvent e) {
					patex.setPex_ap_max((Integer) jSpinnerAPmax.getValue());
					modified = true;
				}

				@Override
				public void focusGained(FocusEvent e) {
					((JTextField) e.getSource()).selectAll();
				}
			});
		}
		return jSpinnerAPmax;
	}

	private VoIntegerTextField getJTextFieldHGT() {
		if (jTextFieldHGT == null) {
			jTextFieldHGT = new VoIntegerTextField(ExaminationParameters.HGT_INIT, 5);
			jTextFieldHGT.setInputVerifier(new MinMaxIntegerInputVerifier(0));
			jTextFieldHGT.addFocusListener(new FocusListener() {

				@Override
				public void focusLost(FocusEvent e) {
					int hgt = Integer.parseInt(jTextFieldHGT.getText());
					jSliderHGT.setValue(hgt);
					patex.setPex_hgt(hgt);
					modified = true;
				}

				@Override
				public void focusGained(FocusEvent e) {
					((JTextField) e.getSource()).selectAll();
				}
			});
		}
		return jTextFieldHGT;
	}

	private VoIntegerTextField getJTextFieldDiuresisVolume() {
		if (jTextFieldDiuresisVolume == null) {
			jTextFieldDiuresisVolume = new VoIntegerTextField(ExaminationParameters.DIURESIS_INIT, 5);
			jTextFieldDiuresisVolume.setInputVerifier(new MinMaxIntegerInputVerifier(0));
			jTextFieldDiuresisVolume.addFocusListener(new FocusListener() {

				@Override
				public void focusLost(FocusEvent e) {
					int diuresisVolume = Integer.parseInt(jTextFieldDiuresisVolume.getText());
					jSliderDiuresisVolume.setValue(diuresisVolume);
					patex.setPex_diuresis(diuresisVolume);
					modified = true;
				}

				@Override
				public void focusGained(FocusEvent e) {
					((JTextField) e.getSource()).selectAll();
				}
			});
		}
		return jTextFieldDiuresisVolume;
	}

	private VoIntegerTextField getJTextFieldHeight() {
		if (jTextFieldHeight == null) {
			jTextFieldHeight = new VoIntegerTextField(0, 5);
			jTextFieldHeight.setInputVerifier(new MinMaxIntegerInputVerifier(ExaminationParameters.HEIGHT_MIN, ExaminationParameters.HEIGHT_MAX));
			jTextFieldHeight.addFocusListener(new FocusListener() {

				@Override
				public void focusLost(FocusEvent e) {
					int height = Integer.parseInt(jTextFieldHeight.getText());
					jSliderHeight.setValue(height);
					patex.setPex_height(height);
					modified = true;
				}

				@Override
				public void focusGained(FocusEvent e) {
					((JTextField) e.getSource()).selectAll();
				}
			});
		}
		return jTextFieldHeight;
	}

	private VoDoubleTextField getJTextFieldWeight() {
		if (jTextFieldWeight == null) {
			jTextFieldWeight = new VoDoubleTextField(0, 5);
			jTextFieldWeight.setInputVerifier(new MinMaxIntegerInputVerifier(ExaminationParameters.WEIGHT_MIN, ExaminationParameters.WEIGHT_MAX));
			jTextFieldWeight.addFocusListener(new FocusListener() {

				@Override
				public void focusLost(FocusEvent e) {
					double weight = Double.parseDouble(jTextFieldWeight.getText());
					jSliderWeight.setValue(weight);
					patex.setPex_weight(weight);
					modified = true;
				}

				@Override
				public void focusGained(FocusEvent e) {
					((JTextField) e.getSource()).selectAll();
				}
			});
		}
		return jTextFieldWeight;
	}

	private VoDoubleTextField getJTextFieldTemp() {
		if (jTextFieldTemp == null) {
			jTextFieldTemp = new VoDoubleTextField(0, 5);
			jTextFieldTemp.setInputVerifier(new MinMaxIntegerInputVerifier(ExaminationParameters.TEMP_MIN, ExaminationParameters.TEMP_MAX));
			jTextFieldTemp.addFocusListener(new FocusListener() {

				@Override
				public void focusLost(FocusEvent e) {
					double temp = Double.parseDouble(jTextFieldTemp.getText());
					jSliderTemp.setValue(temp);
					patex.setPex_temp(temp);
					modified = true;
				}

				@Override
				public void focusGained(FocusEvent e) {
					((JTextField) e.getSource()).selectAll();
				}
			});
		}
		return jTextFieldTemp;
	}

	private VoDoubleTextField getJTextFieldSaturation() {
		if (jTextFieldSaturation == null) {
			jTextFieldSaturation = new VoDoubleTextField(0, 5);
			jTextFieldSaturation.setInputVerifier(new MinMaxIntegerInputVerifier(ExaminationParameters.SAT_MIN, 100));
			jTextFieldSaturation.addFocusListener(new FocusListener() {

				@Override
				public void focusLost(FocusEvent e) {
					double sat = Double.parseDouble(jTextFieldSaturation.getText());
					jSliderSaturation.setValue(sat);
					patex.setPex_sat(sat);
					modified = true;
				}

				@Override
				public void focusGained(FocusEvent e) {
					((JTextField) e.getSource()).selectAll();
				}
			});
		}
		return jTextFieldSaturation;
	}

	private VoIntegerTextField getJTextFieldHR() {
		if (jTextFieldHR == null) {
			jTextFieldHR = new VoIntegerTextField(0, 5);
			jTextFieldHR.setInputVerifier(new MinMaxIntegerInputVerifier(ExaminationParameters.HR_MIN, ExaminationParameters.HR_MAX));
			jTextFieldHR.addFocusListener(new FocusListener() {

				@Override
				public void focusLost(FocusEvent e) {
					int hr = Integer.parseInt(jTextFieldHR.getText());
					jSliderHR.setValue(hr);
					patex.setPex_hr(hr);
					modified = true;
				}

				@Override
				public void focusGained(FocusEvent e) {
					((JTextField) e.getSource()).selectAll();
				}
			});
		}
		return jTextFieldHR;
	}

	private VoIntegerTextField getJTextFieldRR() {
		if (jTextFieldRR == null) {
			jTextFieldRR = new VoIntegerTextField(0, 5);
			jTextFieldRR.addFocusListener(new FocusAdapter() {

				@Override
				public void focusLost(FocusEvent e) {
					int rr = Integer.parseInt(jTextFieldRR.getText());
					jSliderRR.setValue(rr);
					patex.setPex_rr(rr);
					modified = true;
				}
			});
		}
		return jTextFieldRR;
	}

	private JComboBox getJComboBoxAuscultation() {
		if (jComboBoxAuscultation == null) {
			jComboBoxAuscultation = new JComboBox();
			List<String> auscultationList = examManager.getAuscultationList();
			for (String description : auscultationList) {
				jComboBoxAuscultation.addItem(description);
			}
			jComboBoxAuscultation.addItemListener(e -> {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					patex.setPex_auscultation(examManager.getAuscultationKey((String) e.getItem()));
				}
			});
		}
		return jComboBoxAuscultation;
	}

	private JSlider getJSliderHeight() {
		if (jSliderHeight == null) {
			jSliderHeight = new JSlider(ExaminationParameters.HEIGHT_MIN, ExaminationParameters.HEIGHT_MAX, ExaminationParameters.HEIGHT_INIT);
			jSliderHeight.addChangeListener(e -> {
				JSlider source = (JSlider) e.getSource();
				if (!source.getValueIsAdjusting()) {
					int value = jSliderHeight.getValue();
					jTextFieldHeight.setText(String.valueOf(value));
					patex.setPex_height(value);
					updateBMI();
					modified = true;
				}
			});
			jSliderHeight.setFocusable(false);
		}
		return jSliderHeight;
	}

	private ScaledJSlider getJSliderWeight() {
		if (jSliderWeight == null) {
			jSliderWeight = new ScaledJSlider(ExaminationParameters.WEIGHT_MIN, ExaminationParameters.WEIGHT_MAX, ExaminationParameters.WEIGHT_STEP,
					ExaminationParameters.WEIGHT_INIT);
			jSliderWeight.addChangeListener(e -> {
				JSlider source = (JSlider) e.getSource();
				if (!source.getValueIsAdjusting()) {
					double value = jSliderWeight.getScaledValue();
					jTextFieldWeight.setText(String.valueOf(value));
					patex.setPex_weight(value);
					updateBMI();
					modified = true;
				}
			});
			jSliderWeight.setFocusable(false);
		}
		return jSliderWeight;
	}

	private ScaledJSlider getJSliderTemp() {
		if (jSliderTemp == null) {
			jSliderTemp = new ScaledJSlider(ExaminationParameters.TEMP_MIN, ExaminationParameters.TEMP_MAX, ExaminationParameters.TEMP_STEP,
					ExaminationParameters.TEMP_INIT);
			jSliderTemp.addChangeListener(e -> {
				double value = jSliderTemp.getScaledValue();
				jTextFieldTemp.setText(String.valueOf(value));
				patex.setPex_temp(value);
			});
			jSliderTemp.setFocusable(false);
		}
		return jSliderTemp;
	}

	private ScaledJSlider getJSliderSaturation() {
		if (jSliderSaturation == null) {
			jSliderSaturation = new ScaledJSlider(ExaminationParameters.SAT_MIN, 100, ExaminationParameters.SAT_STEP,
					ExaminationParameters.SAT_INIT); //MAX / STEP
			jSliderSaturation.addChangeListener(e -> {
				double value = jSliderSaturation.getScaledValue();
				jTextFieldSaturation.setText(String.valueOf(value));
				patex.setPex_sat(value);
			});
			jSliderSaturation.setFocusable(false);
		}
		return jSliderSaturation;
	}

	private JSlider getJSliderDiuresisVolume() {
		if (jSliderDiuresisVolume == null) {
			jSliderDiuresisVolume = new JSlider(ExaminationParameters.DIURESIS_MIN, ExaminationParameters.DIURESIS_MAX, ExaminationParameters.DIURESIS_INIT);
			jSliderDiuresisVolume.addChangeListener(e -> {
				int value = jSliderDiuresisVolume.getValue();
				jTextFieldDiuresisVolume.setText(String.valueOf(value));
				patex.setPex_diuresis(value);
			});
			jSliderDiuresisVolume.setFocusable(false);
		}
		return jSliderDiuresisVolume;
	}

	private JSlider getJSliderHGT() {
		if (jSliderHGT == null) {
			jSliderHGT = new JSlider(ExaminationParameters.HGT_MIN, ExaminationParameters.HGT_MAX, ExaminationParameters.HGT_INIT);
			jSliderHGT.addChangeListener(e -> {
				int value = jSliderHGT.getValue();
				jTextFieldHGT.setText(String.valueOf(value));
				patex.setPex_hgt(value);
			});
			jSliderHGT.setFocusable(false);
		}
		return jSliderHGT;
	}

	private JSlider getJSliderHR() {
		if (jSliderHR == null) {
			jSliderHR = new JSlider(ExaminationParameters.HR_MIN, ExaminationParameters.HR_MAX, ExaminationParameters.HR_INIT);
			jSliderHR.addChangeListener(e -> {
				int hr = jSliderHR.getValue();
				jTextFieldHR.setText(String.valueOf(hr));
				patex.setPex_hr(hr);
			});
			jSliderHR.setFocusable(false);
		}
		return jSliderHR;
	}

	private JSlider getJSliderRR() {
		if (jSliderRR == null) {
			jSliderRR = new JSlider(0, 100, 0);
			jSliderRR.addChangeListener(e -> {
				int rr = jSliderRR.getValue();
				jTextFieldRR.setText(String.valueOf(rr));
				patex.setPex_rr(rr);
			});
		}
		return jSliderRR;
	}

	private JButton getJButtonSave() {
		if (jButtonSave == null) {
			jButtonSave = new JButton();
			jButtonSave.setAction(getActionSavePatientExamination());
		}
		return jButtonSave;
	}

	private JButton getJButtonDelete() {
		if (jButtonDelete == null) {
			jButtonDelete = new JButton(MessageBundle.getMessage("angal.common.delete.btn"));
			jButtonDelete.setMnemonic(MessageBundle.getMnemonic("angal.common.delete.btn.key"));
			jButtonDelete.addActionListener(actionEvent -> {
				int[] row = jTableSummary.getSelectedRows();
				if (row.length == 0) {
					MessageDialog.error(PatientExaminationEdit.this, "angal.common.pleaseselectarow.msg");
					return;
				}
				int ok = JOptionPane.showConfirmDialog(PatientExaminationEdit.this, MessageBundle.getMessage("angal.common.doyouwanttoproceed.msg"));
				if (ok == JOptionPane.OK_OPTION) {
					List<PatientExamination> patexList = new ArrayList<>();
					for (int j : row) {
						patexList.add((PatientExamination) jTableSummary.getModel().getValueAt(j, -1));
					}
					try {
						examManager.remove(patexList);
					} catch (OHServiceException ohServiceException) {
						MessageDialog.showExceptions(ohServiceException);
					} finally {
						JTableModelSummary model = (JTableModelSummary) jTableSummary.getModel();
						model.reloadData();
					}
				}
			});
		}
		return jButtonDelete;
	}

	private JButton getJButtonClose() {
		if (jButtonClose == null) {
			jButtonClose = new JButton(MessageBundle.getMessage("angal.common.close.btn"));
			jButtonClose.setMnemonic(MessageBundle.getMnemonic("angal.common.close.btn.key"));
			jButtonClose.addActionListener(actionEvent -> {

				//TODO: to provide a more rigorous changes inspections logic
				if (modified) {
					int ok = MessageDialog.yesNoCancel(PatientExaminationEdit.this, "angal.examination.savethechanges.msg");
					if (ok == JOptionPane.YES_OPTION) {
						jButtonSave.doClick();
						dispose();
					} else if (ok == JOptionPane.NO_OPTION) {
						dispose();
					}
				} else {
					dispose();
				}
			});
		}
		return jButtonClose;
	}

	private JButton getJButtonPrint() {
		if (jButtonPrint == null) {
			jButtonPrint = new JButton(MessageBundle.getMessage("angal.common.print.btn"));
			jButtonPrint.setMnemonic(MessageBundle.getMnemonic("angal.common.print.btn.key"));
			jButtonPrint.addActionListener(actionEvent -> {
				int selectedrow = jTableSummary.getSelectedRow();
				if (selectedrow < 0) {
					selectedrow = 0;
				}

				PatientExamination exam = (PatientExamination) jTableSummary.getValueAt(selectedrow, -1);
				new GenericReportExamination(patex.getPatient().getCode(), exam.getPex_ID(), GeneralData.EXAMINATIONCHART);

			});
		}
		return jButtonPrint;
	}

	private JPanel getJPanelGender() {
		if (jPanelGender == null) {
			jPanelGender = new JPanel();
			LayoutManager overlay = new OverlayLayout(jPanelGender);
			jPanelGender.setLayout(overlay);
			jPanelGender.add(getJEditorPaneBMI());
			jPanelGender.add(getJLabelImageGender());
			jPanelGender.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
		}
		return jPanelGender;
	}

	private JEditorPane getJEditorPaneBMI() {
		if (jEditorPaneBMI == null) {
			jEditorPaneBMI = new JEditorPane();
			jEditorPaneBMI.setFont(new Font("Arial", Font.BOLD, 14));
			jEditorPaneBMI.setContentType("text/html");
			jEditorPaneBMI.setEditable(false);
			jEditorPaneBMI.setOpaque(false);
			jEditorPaneBMI.setMinimumSize(new Dimension(132, 300));
			jEditorPaneBMI.setPreferredSize(new Dimension(132, 300));
			jEditorPaneBMI.setMaximumSize(new Dimension(132, 300));
		}
		return jEditorPaneBMI;
	}

	private JLabel getJLabelImageGender() {
		if (jLabelGender == null) {
			jLabelGender = new JLabel();
			if (isMale) {
				jLabelGender.setIcon(new ImageIcon(PATH_MALE_GENDER));
			} else {
				jLabelGender.setIcon(new ImageIcon(PATH_FEMALE_GENDER));
			}
			jLabelGender.setAlignmentX(0.5f);
			jLabelGender.setAlignmentY(0.5f);
		}
		return jLabelGender;
	}

	private final class MinMaxIntegerInputVerifier extends InputVerifier {

		boolean bottom;
		boolean ceiling = false;
		int min;
		int max;

		public MinMaxIntegerInputVerifier(int min) {
			this.min = min;
			this.bottom = true;
		}

		public MinMaxIntegerInputVerifier(int min, int max) {
			this.min = min;
			this.max = max;
			this.bottom = true;
			this.ceiling = true;
		}

		@Override
		public boolean verify(JComponent input) {
			JTextComponent comp = (JTextComponent) input;
			try {
				double value = Double.parseDouble(comp.getText());
				if (bottom && value < min) {
					MessageDialog.error(PatientExaminationEdit.this, "angal.common.thisvaluecannotbelessthan.fmt.msg", min);
					return false;
				}
				if (ceiling && value > max) {
					MessageDialog.error(PatientExaminationEdit.this, "angal.common.thisvaluecannotbegreaterthan.fmt.msg", max);
					return false;
				}
			} catch (NumberFormatException e) {
				return false;
			}
			return true;
		}
	}

	private class SwingActionSavePatientExamination extends AbstractAction {

		private static final long serialVersionUID = 1L;

		public SwingActionSavePatientExamination() {
			putValue(NAME, MessageBundle.getMessage("angal.common.save.btn"));
			putValue(MNEMONIC_KEY, MessageBundle.getMnemonic("angal.common.save.btn.key"));
			putValue(SHORT_DESCRIPTION, MessageBundle.getMessage("angal.examination.tooltip.savepatientexamination"));
		}

		@Override
		public void actionPerformed(ActionEvent actionEvent) {

			try {
				examManager.saveOrUpdate(patex);
				modified = false;
			} catch (OHServiceException ohServiceException) {
				MessageDialog.showExceptions(ohServiceException);
			}
			JTableModelSummary model = (JTableModelSummary) jTableSummary.getModel();
			model.reloadData();
		}
	}

	private Action getActionSavePatientExamination() {
		if (actionSavePatientExamination == null) {
			actionSavePatientExamination = new SwingActionSavePatientExamination();
		}
		return actionSavePatientExamination;
	}

	private void enableAP() {
		jSpinnerAPmin.setEnabled(true);
		patex.setPex_ap_min((Integer) jSpinnerAPmin.getValue());
		jSpinnerAPmax.setEnabled(true);
		patex.setPex_ap_max((Integer) jSpinnerAPmax.getValue());
		modified = true;
	}

	private void disableAP() {
		jSpinnerAPmin.setEnabled(false);
		patex.setPex_ap_min(null);
		jSpinnerAPmax.setEnabled(false);
		patex.setPex_ap_max(null);
	}

	private class SwingActionToggleAP extends AbstractAction {

		private static final long serialVersionUID = 1L;

		public SwingActionToggleAP() {
			putValue(NAME, ""); //$NON-NLS-1$
			putValue(SHORT_DESCRIPTION, MessageBundle.getMessage("angal.examination.tooltip.toggleexamination")); //$NON-NLS-1$
		}

		@Override
		public void actionPerformed(ActionEvent actionEvent) {
			if (!jCheckBoxToggleAP.isSelected()) {
				disableAP();
			} else {
				enableAP();
			}
		}
	}

	private Action getActionToggleAP() {
		if (actionToggleAP == null) {
			actionToggleAP = new SwingActionToggleAP();
		}
		return actionToggleAP;
	}

	private void enableTemp() throws NumberFormatException {
		jSliderTemp.setEnabled(true);
		jTextFieldTemp.setEnabled(true);
		String text = jTextFieldTemp.getText();
		if (!text.equals("")) {
			patex.setPex_temp(Double.parseDouble(text));
		} else {
			patex.setPex_temp(null);
		}
		modified = true;
	}

	private void disableTemp() {
		jSliderTemp.setEnabled(false);
		jTextFieldTemp.setEnabled(false);
		patex.setPex_temp(null);
	}

	private void enableSaturation() throws NumberFormatException {
		jSliderSaturation.setEnabled(true);
		jTextFieldSaturation.setEnabled(true);
		String text = jTextFieldSaturation.getText();
		if (!text.equals("")) {
			patex.setPex_sat(Double.parseDouble(text));
		} else {
			patex.setPex_sat(null);
		}
		modified = true;
	}

	private void disableSaturation() {
		jSliderSaturation.setEnabled(false);
		jTextFieldSaturation.setEnabled(false);
		patex.setPex_sat(null);
	}

	private void enableHGT() throws NumberFormatException {
		jSliderHGT.setEnabled(true);
		jTextFieldHGT.setEnabled(true);
		String text = jTextFieldHGT.getText();
		if (!text.equals("")) {
			patex.setPex_hgt(Integer.parseInt(text));
		} else {
			patex.setPex_hgt(null);
		}
		modified = true;
	}

	private void disableHGT() {
		jSliderHGT.setEnabled(false);
		jTextFieldHGT.setEnabled(false);
		patex.setPex_hgt(null);
	}

	private void enableBowel() {
		jComboBoxBowel.setEnabled(true);
		patex.setPex_bowel_desc(examManager.getBowelDescriptionKey((String) jComboBoxBowel.getSelectedItem()));
		modified = true;
	}

	private void disableBowel() {
		jComboBoxBowel.setEnabled(false);
		patex.setPex_bowel_desc(null);
	}

	private void enableDiuresisVolume() throws NumberFormatException {
		jSliderDiuresisVolume.setEnabled(true);
		jTextFieldDiuresisVolume.setEnabled(true);
		String text = jTextFieldDiuresisVolume.getText();
		if (!text.equals("")) {
			patex.setPex_diuresis(Integer.parseInt(text));
		} else {
			patex.setPex_diuresis(null);
		}
		modified = true;
	}

	private void disableDiuresisVolume() {
		jSliderDiuresisVolume.setEnabled(false);
		jTextFieldDiuresisVolume.setEnabled(false);
		patex.setPex_diuresis(null);
	}

	private void enableDiuresisType() {
		jComboBoxDiuresisType.setEnabled(true);
		patex.setPex_diuresis_desc(examManager.getDiuresisDescriptionKey((String) jComboBoxDiuresisType.getSelectedItem()));
		modified = true;
	}

	private void disableDiuresisType() {
		jComboBoxDiuresisType.setEnabled(false);
		patex.setPex_diuresis_desc(null);
	}

	private void enableHR() throws NumberFormatException {
		jSliderHR.setEnabled(true);
		jTextFieldHR.setEnabled(true);
		patex.setPex_hr(Integer.parseInt(jTextFieldHR.getText()));
		modified = true;
	}

	private void disableHR() {
		jSliderHR.setEnabled(false);
		jTextFieldHR.setEnabled(false);
		patex.setPex_hr(null);
	}

	private class SwingActionToggleHGT extends AbstractAction {

		private static final long serialVersionUID = 1L;

		public SwingActionToggleHGT() {
			putValue(NAME, ""); //$NON-NLS-1$
			putValue(SHORT_DESCRIPTION, MessageBundle.getMessage("angal.examination.tooltip.toggleexamination")); //$NON-NLS-1$
		}

		@Override
		public void actionPerformed(ActionEvent actionEvent) {
			if (!jCheckBoxToggleHGT.isSelected()) {
				disableHGT();
			} else {
				enableHGT();
			}
		}
	}

	private class SwingActionToggleBowel extends AbstractAction {

		private static final long serialVersionUID = 1L;

		public SwingActionToggleBowel() {
			putValue(NAME, ""); //$NON-NLS-1$
			putValue(SHORT_DESCRIPTION, MessageBundle.getMessage("angal.examination.tooltip.toggleexamination")); //$NON-NLS-1$
		}

		@Override
		public void actionPerformed(ActionEvent actionEvent) {
			if (!jCheckBoxToggleBowel.isSelected()) {
				disableBowel();
			} else {
				enableBowel();
			}
		}
	}

	private class SwingActionToggleDiuresisVolume extends AbstractAction {

		private static final long serialVersionUID = 1L;

		public SwingActionToggleDiuresisVolume() {
			putValue(NAME, ""); //$NON-NLS-1$
			putValue(SHORT_DESCRIPTION, MessageBundle.getMessage("angal.examination.tooltip.toggleexamination")); //$NON-NLS-1$
		}

		@Override
		public void actionPerformed(ActionEvent actionEvent) {
			if (!jCheckBoxToggleDiuresisVolume.isSelected()) {
				disableDiuresisVolume();
			} else {
				enableDiuresisVolume();
			}
		}
	}

	private class SwingActionToggleDiuresisType extends AbstractAction {

		private static final long serialVersionUID = 1L;

		public SwingActionToggleDiuresisType() {
			putValue(NAME, ""); //$NON-NLS-1$
			putValue(SHORT_DESCRIPTION, MessageBundle.getMessage("angal.examination.tooltip.toggleexamination")); //$NON-NLS-1$
		}

		@Override
		public void actionPerformed(ActionEvent actionEvent) {
			if (!jCheckBoxToggleDiuresisType.isSelected()) {
				disableDiuresisType();
			} else {
				enableDiuresisType();
			}
		}
	}

	private void enableRR() throws NumberFormatException {
		jSliderRR.setEnabled(true);
		jTextFieldRR.setEnabled(true);
		patex.setPex_rr(Integer.parseInt(jTextFieldRR.getText()));
		modified = true;
	}

	private void disableRR() {
		jSliderRR.setEnabled(false);
		jTextFieldRR.setEnabled(false);
		patex.setPex_rr(null);
	}

	private void enableAuscultation() {
		jComboBoxAuscultation.setEnabled(true);
		patex.setPex_auscultation(examManager.getAuscultationKey((String) jComboBoxAuscultation.getSelectedItem()));
		modified = true;
	}

	private void disableAuscultation() {
		jComboBoxAuscultation.setEnabled(false);
		patex.setPex_auscultation(null);
	}

	private class SwingActionToggleSaturation extends AbstractAction {

		private static final long serialVersionUID = 1L;

		public SwingActionToggleSaturation() {
			putValue(NAME, ""); //$NON-NLS-1$
			putValue(SHORT_DESCRIPTION, MessageBundle.getMessage("angal.examination.tooltip.toggleexamination")); //$NON-NLS-1$
		}

		@Override
		public void actionPerformed(ActionEvent actionEvent) {
			if (!jCheckBoxToggleSaturation.isSelected()) {
				disableSaturation();
			} else {
				enableSaturation();
			}
		}
	}

	private class SwingActionToggleRR extends AbstractAction {

		private static final long serialVersionUID = 1L;

		public SwingActionToggleRR() {
			putValue(NAME, ""); //$NON-NLS-1$
			putValue(SHORT_DESCRIPTION, MessageBundle.getMessage("angal.examination.tooltip.toggleexamination")); //$NON-NLS-1$
		}

		@Override
		public void actionPerformed(ActionEvent actionEvent) {
			if (!jCheckBoxToggleRR.isSelected()) {
				disableRR();
			} else {
				enableRR();
			}
		}
	}

	private class SwingActionToggleAusc extends AbstractAction {

		private static final long serialVersionUID = 1L;

		public SwingActionToggleAusc() {
			putValue(NAME, ""); //$NON-NLS-1$
			putValue(SHORT_DESCRIPTION, MessageBundle.getMessage("angal.examination.tooltip.toggleexamination")); //$NON-NLS-1$
		}

		@Override
		public void actionPerformed(ActionEvent actionEvent) {
			if (!jCheckBoxToggleAusc.isSelected()) {
				disableAuscultation();
			} else {
				enableAuscultation();
			}
		}
	}

	private class SwingActionToggleTemp extends AbstractAction {

		private static final long serialVersionUID = 1L;

		public SwingActionToggleTemp() {
			putValue(NAME, ""); //$NON-NLS-1$
			putValue(SHORT_DESCRIPTION, MessageBundle.getMessage("angal.examination.tooltip.toggleexamination")); //$NON-NLS-1$
		}

		@Override
		public void actionPerformed(ActionEvent actionEvent) {
			if (!jCheckBoxToggleTemp.isSelected()) {
				disableTemp();
			} else {
				enableTemp();
			}
		}
	}

	private class SwingActionToggleHR extends AbstractAction {

		private static final long serialVersionUID = 1L;

		public SwingActionToggleHR() {
			putValue(NAME, ""); //$NON-NLS-1$
			putValue(SHORT_DESCRIPTION, MessageBundle.getMessage("angal.examination.tooltip.toggleexamination")); //$NON-NLS-1$
		}

		@Override
		public void actionPerformed(ActionEvent actionEvent) {
			if (!jCheckBoxToggleHR.isSelected()) {
				disableHR();
			} else {
				enableHR();
			}
		}
	}

	private Action getActionToggleHR() {
		if (actionToggleHR == null) {
			actionToggleHR = new SwingActionToggleHR();
		}
		return actionToggleHR;
	}

	private Action getActionToggleTemp() {
		if (actionToggleTemp == null) {
			actionToggleTemp = new SwingActionToggleTemp();
		}
		return actionToggleTemp;
	}

	private Action getActionToggleSaturation() {
		if (actionToggleSaturation == null) {
			actionToggleSaturation = new SwingActionToggleSaturation();
		}
		return actionToggleSaturation;
	}

	private Action getActionToggleRR() {
		if (actionToggleRR == null) {
			actionToggleRR = new SwingActionToggleRR();
		}
		return actionToggleRR;
	}

	private Action getActionToggleAusc() {
		if (actionToggleAusc == null) {
			actionToggleAusc = new SwingActionToggleAusc();
		}
		return actionToggleAusc;
	}

	private Action getActionToggleHGT() {
		if (actionToggleHGT == null) {
			actionToggleHGT = new SwingActionToggleHGT();
		}
		return actionToggleHGT;
	}

	private Action getActionToggleDiuresisVolume() {
		if (actionToggleDiuresisVolume == null) {
			actionToggleDiuresisVolume = new SwingActionToggleDiuresisVolume();
		}
		return actionToggleDiuresisVolume;
	}

	private Action getActionToggleDiuresisType() {
		if (actionToggleDiuresisType == null) {
			actionToggleDiuresisType = new SwingActionToggleDiuresisType();
		}
		return actionToggleDiuresisType;
	}

	private Action getActionToggleBowel() {
		if (actionToggleBowel == null) {
			actionToggleBowel = new SwingActionToggleBowel();
		}
		return actionToggleBowel;
	}

	private JPanel getJPanelSummary() {
		if (jPanelSummary == null) {
			jPanelSummary = new JPanel();
			jPanelSummary.setBorder(new EmptyBorder(5, 5, 5, 5));
			jPanelSummary.setLayout(new BorderLayout(0, 0));
			jPanelSummary.add(getJTableSummary());
		}
		return jPanelSummary;
	}

	private JScrollPane getJTableSummary() {
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setPreferredSize(new Dimension(890, 150));
		TableCellRenderer buttonRenderer = new JTableButtonRenderer();
		jTableSummary = new JTable(new JTableModelSummary());
		for (int i = 0; i < columnNames.length - 1; i++) { //last column is for JButton
			jTableSummary.getColumnModel().getColumn(i).setCellRenderer(new EnabledTableCellRenderer());
			jTableSummary.getColumnModel().getColumn(i).setMinWidth(columnWidth[i]);
		}
		jTableSummary.getColumnModel().getColumn(columnNames.length - 1).setCellRenderer(buttonRenderer);
		jTableSummary.getColumnModel().getColumn(columnNames.length - 1).setMinWidth(columnWidth[columnNames.length - 1]);
		jTableSummary.setShowGrid(false);

		JTableHeader header = jTableSummary.getTableHeader();
		header.setBackground(Color.white);

		jTableSummary.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent me) {
				int column = jTableSummary.getColumnModel().getColumnIndexAtX(me.getX()); // get the column of the button
				JTable target = (JTable) me.getSource();
				int row = target.getSelectedRow(); // select a row

				/*Checking the row or column is valid or not*/
				if (row < jTableSummary.getRowCount() && row >= 0 && column < jTableSummary.getColumnCount() && column >= 0) {
					Object value = jTableSummary.getValueAt(row, column);
					if (value instanceof JButton) {
						/*perform a click event*/
						((JButton) value).doClick();
					}
				}
			}
		});

		scrollPane.setViewportView(jTableSummary);
		scrollPane.getViewport().setBackground(Color.white);

		return scrollPane;
	}

	private static class JTableButtonRenderer implements TableCellRenderer {

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			JButton button = (JButton) value;
			return button;
		}
	}

	public class JTableModelSummary extends AbstractTableModel {

		private static final long serialVersionUID = 1L;

		private ExaminationBrowserManager examManager = Context.getApplicationContext().getBean(ExaminationBrowserManager.class);
		private List<PatientExamination> patexList = null;

		public JTableModelSummary() {
			reloadData();
		}

		public void reloadData() {
			try {
				patexList = examManager.getLastNByPatID(patex.getPatient().getCode(), ExaminationParameters.LIST_SIZE);
			} catch (OHServiceException ohServiceException) {
				MessageDialog.showExceptions(ohServiceException);
			}
			fireTableDataChanged();
		}

		public List<PatientExamination> getList() {
			return patexList;
		}

		public void removeItem(int row) {
			patexList.remove(row);
			fireTableDataChanged();
		}

		@Override
		public int getRowCount() {
			return patexList.size();
		}

		@Override
		public int getColumnCount() {
			return columnNames.length;
		}

		@Override
		public String getColumnName(int columnIndex) {
			return columnNames[columnIndex];
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			return columnClasses[columnIndex];
		}

		@Override
		public boolean isCellEditable(int r, int c) {
			return false;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see javax.swing.table.TableModel#getValueAt(int, int)
		 */
		@Override
		public Object getValueAt(int r, int c) {
			PatientExamination patex = patexList.get(r);
			StringBuilder ap_string = new StringBuilder();
			ap_string.append(patex.getPex_ap_min() == null ? "-" : patex.getPex_ap_min())
					.append(" / ").append(patex.getPex_ap_max() == null ? "-" : patex.getPex_ap_max());
			String datetime = DateTimeFormatter.ofPattern(DATE_FORMAT_DD_MM_YYYY_HH_MM).format(patex.getPex_date());
			String diuresis = patex.getPex_diuresis_desc() == null ? "-" : examManager.getDiuresisDescriptionTranslated(patex.getPex_diuresis_desc());
			String bowel = patex.getPex_bowel_desc() == null ? "-" : examManager.getBowelDescriptionTranslated(patex.getPex_bowel_desc());
			String ausc = patex.getPex_auscultation() == null ? "-" : examManager.getAuscultationTranslated(patex.getPex_auscultation());
			String note = patex.getPex_note();
			if (c == -1) {
				return patex;
			} else if (c == 0) {
				return datetime;
			} else if (c == 1) {
				return patex.getPex_height();
			} else if (c == 2) {
				return patex.getPex_weight();
			} else if (c == 3) {
				return ap_string.toString();
			} else if (c == 4) {
				return patex.getPex_hr() == null ? "-" : patex.getPex_hr();
			} else if (c == 5) {
				return patex.getPex_temp() == null ? "-" : patex.getPex_temp();
			} else if (c == 6) {
				return patex.getPex_sat() == null ? "-" : patex.getPex_sat();
			} else if (c == 7) {
				return patex.getPex_hgt() == null ? "-" : patex.getPex_hgt();
			} else if (c == 8) {
				return patex.getPex_rr() == null ? "-" : patex.getPex_rr();
			} else if (c == 9) {
				return patex.getPex_diuresis() == null ? "-" : patex.getPex_diuresis();
			} else if (c == 10) {
				return diuresis;
			} else if (c == 11) {
				return bowel;
			} else if (c == 12) {
				return ausc;
			} else if (c == 13) {
				if (!note.trim().isEmpty()) {
					final IconButton button = new IconButton(new ImageIcon("rsc/icons/list_button.png"));
					button.addActionListener(actionEvent -> {
						VoLimitedTextArea noteArea = new VoLimitedTextArea(PatientExamination.PEX_NOTE_LENGTH, 6, 20);
						noteArea.setText(note);
						noteArea.setEditable(false);
						JOptionPane.showMessageDialog(PatientExaminationEdit.this,
								new JScrollPane(noteArea),
								MessageBundle.getMessage("angal.examination.note"), //$NON-NLS-1$
								JOptionPane.INFORMATION_MESSAGE);
					});
					return button;
				}
				return null;
			}
			return null;
		}
	}

	private final class CheckBoxFocus implements FocusListener {

		private Color bgColor = null;

		@Override
		public void focusLost(FocusEvent e) {
			JCheckBox thisCheckBox = (JCheckBox) e.getSource();
			thisCheckBox.setBackground(this.bgColor);
		}

		@Override
		public void focusGained(FocusEvent e) {
			JCheckBox thisCheckBox = (JCheckBox) e.getSource();
			this.bgColor = thisCheckBox.getBackground();
			thisCheckBox.setBackground(Color.red);
		}
	}

	class EnabledTableCellRenderer extends DefaultTableCellRenderer {

		private static final long serialVersionUID = 1L;

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			setHorizontalAlignment(columnAlignment[column]);
			return cell;
		}
	}

}
