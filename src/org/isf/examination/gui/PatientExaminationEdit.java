package org.isf.examination.gui;

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
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.InputVerifier;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
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
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.text.JTextComponent;

import org.apache.log4j.PropertyConfigurator;
import org.isf.examination.manager.ExaminationBrowserManager;
import org.isf.examination.model.GenderPatientExamination;
import org.isf.examination.model.PatientExamination;
import org.isf.generaldata.ExaminationParameters;
import org.isf.generaldata.GeneralData;
import org.isf.generaldata.MessageBundle;
import org.isf.menu.manager.Context;
import org.isf.stat.gui.report.GenericReportPatient;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.model.OHExceptionMessage;
import org.isf.utils.jobjects.ScaledJSlider;
import org.isf.utils.jobjects.VoDoubleTextField;
import org.isf.utils.jobjects.VoIntegerTextField;
import org.isf.utils.jobjects.VoLimitedTextArea;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import org.isf.utils.jobjects.CustomJDateChooser;

public class PatientExaminationEdit extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private JPanel jPanelExamination;
	private JPanel jPanelButtons;
	private JSlider jSliderHeight;
	private JSlider jSliderHGT;
	private JSlider jSliderDiuresis;
	private ScaledJSlider jSliderWeight;
	private VoIntegerTextField jTextFieldHR;
	private VoDoubleTextField jTextFieldTemp;
	private VoDoubleTextField jTextFieldSaturation;
	private VoIntegerTextField jTextFieldHGT;
	private VoIntegerTextField jTextFieldDiuresis;
	private VoLimitedTextArea jTextAreaNote;
	private VoIntegerTextField jTextFieldHeight;
	private VoDoubleTextField jTextFieldWeight;
	private JPanel jPanelAPPanel;
	private JLabel jLabelAPMin;
	private JLabel jLabelAPSlash;
	private JLabel jLabelAPMax;
	private JSlider jSliderHR;
	private ScaledJSlider jSliderTemp;
	private ScaledJSlider jSliderSaturation;
	private JScrollPane jScrollPaneNote;
	private CustomJDateChooser jDateChooserDate;
	private VoIntegerTextField jSpinnerAPmin;
	private VoIntegerTextField jSpinnerAPmax;
	private JLabel jLabelHeightAbb;
	private JLabel jLabelWeightAbb;
	private JCheckBox jCheckBoxToggleAP;
	private JCheckBox jCheckBoxToggleHR;
	private JCheckBox jCheckBoxToggleTemp;
	private JCheckBox jCheckBoxToggleSaturation;
	private JCheckBox jCheckBoxToggleHGT;
	private JCheckBox jCheckBoxToggleDiuresis;
	private JCheckBox jCheckBoxToggleBowel;
	private JComboBox jComboBoxDiuresis;
	private JComboBox jComboBoxBowel;
	private JButton jButtonOK;
	private JButton jButtonDelete;
	private JButton jButtonCancel;
	private JButton jButtonPrint;
	private Action actionSavePatientExamination;
	private Action actionToggleAP;
	private Action actionToggleHR;
	private Action actionToggleTemp;
	private Action actionToggleSaturation;
	private Action actionToggleHGT;
	private Action actionToggleDiuresis;
	private Action actionToggleBowel;
	private JPanel jPanelGender;
	private JLabel jLabelGender;
	private JEditorPane jEditorPaneBMI;
	private JPanel jPanelSummary;
	private JPanel jNotePanel;
	private JEditorPane jEditorPaneSummary;
	
	private PatientExamination patex;
	private boolean isMale;
	private double bmi;
	
	private final String PATH_FEMALE_GENDER = "rsc/images/sagoma-donna-132x300.jpg"; //$NON-NLS-1$
	private final String PATH_MALE_GENDER = "rsc/images/sagoma-uomo-132x300.jpg"; //$NON-NLS-1$
	private final String SUMMARY_START_ROW = "<tr align=\"center\">";
	private final String SUMMARY_END_ROW = "</tr>";
	private final String STD = "<td>";
	private final String ETD = "</td>";
	private final String SUMMARY_HEADER = "" +
			"<html><head></head><body><table>"+
			SUMMARY_START_ROW+
			STD+MessageBundle.getMessage("angal.common.datem")+ETD+
			STD+MessageBundle.getMessage("angal.examination.height")+ETD+
			STD+MessageBundle.getMessage("angal.examination.weight")+ETD+
			STD+MessageBundle.getMessage("angal.examination.arterialpressureabbr")+ETD+
			STD+MessageBundle.getMessage("angal.examination.heartrateabbr")+ETD+
			STD+MessageBundle.getMessage("angal.examination.temperatureabbr")+ETD+
			STD+MessageBundle.getMessage("angal.examination.saturationabbr")+ETD+
			SUMMARY_END_ROW;
	private final String SUMMARY_FOOTER = "</table></body></html>";
	
	private final String[] columnNames = { 
			MessageBundle.getMessage("angal.common.datem"), //$NON-NLS-1$
			MessageBundle.getMessage("angal.examination.height"), //$NON-NLS-1$
			MessageBundle.getMessage("angal.examination.weight"), //$NON-NLS-1$
			MessageBundle.getMessage("angal.examination.arterialpressure"), //$NON-NLS-1$
			MessageBundle.getMessage("angal.examination.heartrate"), //$NON-NLS-1$
			MessageBundle.getMessage("angal.examination.temperature"), //$NON-NLS-1$
			MessageBundle.getMessage("angal.examination.saturation"), //$NON-NLS-1$
			MessageBundle.getMessage("angal.examination.hgt"), //$NON-NLS-1$
			MessageBundle.getMessage("angal.examination.diuresis"), //$NON-NLS-1$
			MessageBundle.getMessage("angal.examination.diuresis"), //$NON-NLS-1$
			MessageBundle.getMessage("angal.examination.bowel"), //$NON-NLS-1$
	};
	private final Class[] columnClasses = { String.class, Integer.class, Double.class, String.class, Integer.class, Double.class, Double.class, Integer.class, Integer.class, String.class, String.class};
	private int[] columnWidth = { 100, 40, 40, 100, 70, 50, 50, 50, 50, 70, 70};
	private int[] columnAlignment = { SwingConstants.LEFT, SwingConstants.CENTER, SwingConstants.CENTER, SwingConstants.CENTER, SwingConstants.CENTER, SwingConstants.CENTER, SwingConstants.CENTER, SwingConstants.CENTER, SwingConstants.CENTER, SwingConstants.CENTER, SwingConstants.CENTER};
	
	private final String DATE_FORMAT = "dd/MM/yy HH:mm";

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
		super(parent, true);
		this.patex = gpatex.getPatex();
		this.isMale = gpatex.isMale();
		initComponents();
		updateGUI();
	}

	public PatientExaminationEdit(Dialog parent, GenderPatientExamination gpatex) {
		super(parent, true);
		this.patex = gpatex.getPatex();
		this.isMale = gpatex.isMale();
		initComponents();
		updateGUI();
	}
	
	private void initComponents() {
		ExaminationParameters.getExaminationParameters();
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
			jPanelButtons.add(getJButtonOK());
			jPanelButtons.add(getJButtonDelete());
			jPanelButtons.add(getJButtonCancel());
			jPanelButtons.add(getJButtonPrint());
			
			
		}
		return jPanelButtons;
	}
	
	//TODO: try to use JDOM...
	private void updateBMI() {
		this.bmi = patex.getBMI();
		StringBuilder bmi = new StringBuilder();
		bmi.append("<html><body>");
		bmi.append("<strong>");
		bmi.append(MessageBundle.getMessage("angal.examination.bmi") + ":");
		bmi.append("<br />");
		bmi.append("" + this.bmi);
		bmi.append("<br /><br />");
		bmi.append("<font color=\"red\">");
		bmi.append(examManager.getBMIdescription(this.bmi));
		bmi.append("</font>");
		bmi.append("</strong>");
		bmi.append("</body></html>");
		jEditorPaneBMI.setText(bmi.toString());
	}
	
	//TODO: try to use JDOM...
	private void updateSummary() {
		StringBuilder summary = new StringBuilder();
		summary.append(SUMMARY_HEADER);
		ExaminationBrowserManager examManager = Context.getApplicationContext().getBean(ExaminationBrowserManager.class);
		ArrayList<PatientExamination> patexList = null;
		try {
			patexList = examManager.getLastNByPatID(patex.getPatient().getCode().intValue(), ExaminationParameters.LIST_SIZE);
		}catch(OHServiceException e){
			if(e.getMessages() != null){
				for(OHExceptionMessage msg : e.getMessages()){
					JOptionPane.showMessageDialog(null, msg.getMessage(), msg.getTitle() == null ? "" : msg.getTitle(), msg.getLevel().getSwingSeverity());
				}
			}
		}
		Collections.sort(patexList);
		if(patexList != null){
			for (PatientExamination patex : patexList) {
				summary.append(SUMMARY_START_ROW);
				summary.append(STD).append(new SimpleDateFormat(DATE_FORMAT).format(new Date(patex.getPex_date().getTime()))).append(ETD);
				summary.append(STD).append(patex.getPex_height() == null ? "-" : patex.getPex_height()).append(ETD);
				summary.append(STD).append(patex.getPex_weight() == null ? "-" : patex.getPex_weight()).append(ETD);
				summary.append(STD).append(patex.getPex_ap_min() == null ? "-" : patex.getPex_ap_min())
					.append(" / ").append(patex.getPex_ap_max() == null ? "-" : patex.getPex_ap_max()).append(ETD);
				summary.append(STD).append(patex.getPex_hr() == null ? "-" : patex.getPex_hr()).append(ETD);
				summary.append(STD).append(patex.getPex_temp() == null ? "-" : patex.getPex_temp()).append(ETD);
				summary.append(STD).append(patex.getPex_sat() == null ? "-" : patex.getPex_sat()).append(ETD);
				summary.append(SUMMARY_END_ROW);
			}
		}
		summary.append(SUMMARY_FOOTER);
		jEditorPaneSummary.setText(summary.toString());
	}
	
	private void updateGUI() {
		jDateChooserDate.setDate(new Date(patex.getPex_date().getTime()));
		jTextFieldHeight.setText(String.valueOf(patex.getPex_height()));
		jSliderHeight.setValue(patex.getPex_height());
		jTextFieldWeight.setText(String.valueOf(patex.getPex_weight()));
		jSliderWeight.setValue(patex.getPex_weight() != null ? patex.getPex_weight() : 0);
		jSpinnerAPmin.setText(patex.getPex_ap_min() != null ? String.valueOf(patex.getPex_ap_min()) : ""+ExaminationParameters.AP_MIN_INIT);
		jSpinnerAPmax.setText(patex.getPex_ap_max() != null ? String.valueOf(patex.getPex_ap_max()) : ""+ExaminationParameters.AP_MAX_INIT);
		jSliderHR.setValue(patex.getPex_hr() != null ? patex.getPex_hr() : ExaminationParameters.HR_INIT);
		jTextFieldHR.setText(patex.getPex_hr() != null ? String.valueOf(patex.getPex_hr()) : ""+ExaminationParameters.HR_INIT);
		jSliderTemp.setValue(patex.getPex_temp());
		jTextFieldTemp.setText(patex.getPex_temp() != null ? String.valueOf(patex.getPex_temp()) : ""+ExaminationParameters.TEMP_INIT);
		jSliderSaturation.setValue(patex.getPex_sat());
		jTextFieldSaturation.setText(patex.getPex_sat() != null ? String.valueOf(patex.getPex_sat()) : ""+ExaminationParameters.SAT_INIT);
		jTextFieldHGT.setText(patex.getPex_hgt() != null ? String.valueOf(patex.getPex_hgt()) : ""+ExaminationParameters.HGT_INIT);
		jTextFieldDiuresis.setText(patex.getPex_diuresis() != null ? String.valueOf(patex.getPex_diuresis()) : ""+ExaminationParameters.DIURESIS_INIT);
		jComboBoxDiuresis.setSelectedItem(patex.getPex_diuresis_desc() != null ? examManager.getDiuresisDescriptionTranslated(patex.getPex_diuresis_desc()) : null);
		jComboBoxBowel.setSelectedItem(patex.getPex_bowel_desc() != null ? examManager.getBowelDescriptionTranslated(patex.getPex_bowel_desc()) : null);
		jTextAreaNote.setText(patex.getPex_note());
		disableAP();
		disableHR();
		disableTemp();
		disableSaturation();
		disableHGT();
		disableDiuresis();
		disableBowel();
	}

	private JPanel getJPanelExamination() {
		if (jPanelExamination == null) {
			jPanelExamination = new JPanel();
			
			GridBagLayout gbl_jPanelExamination = new GridBagLayout();
			gbl_jPanelExamination.columnWidths = new int[] { 0, 0, 0, 0 };
			gbl_jPanelExamination.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0 };
			gbl_jPanelExamination.columnWeights = new double[] { 0.0, 0.0, 1.0, 0.0 };
			gbl_jPanelExamination.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0 };
			jPanelExamination.setLayout(gbl_jPanelExamination);

			JLabel jLabelDate = new JLabel(MessageBundle.getMessage("angal.common.date")); //$NON-NLS-1$
			GridBagConstraints gbc_jLabelDate = new GridBagConstraints();
			gbc_jLabelDate.anchor = GridBagConstraints.WEST;
			gbc_jLabelDate.insets = new Insets(10, 5, 5, 5);
			gbc_jLabelDate.gridx = 1;
			gbc_jLabelDate.gridy = 0;
			jPanelExamination.add(jLabelDate, gbc_jLabelDate);
			
			GridBagConstraints gbc_jDateChooserDate = new GridBagConstraints();
			gbc_jDateChooserDate.anchor = GridBagConstraints.WEST;
			gbc_jDateChooserDate.insets = new Insets(10, 5, 5, 5);
			gbc_jDateChooserDate.gridx = 2;
			gbc_jDateChooserDate.gridy = 0;
			jPanelExamination.add(getJDateChooserDate(), gbc_jDateChooserDate);
			
			{
				jLabelHeightAbb = new JLabel(MessageBundle.getMessage("angal.examination.heightabbr")); //$NON-NLS-1$
				GridBagConstraints gbc_lblh = new GridBagConstraints();
				gbc_lblh.insets = new Insets(5, 5, 5, 5);
				gbc_lblh.gridx = 0;
				gbc_lblh.gridy = 1;
				jPanelExamination.add(jLabelHeightAbb, gbc_lblh);
	
				JLabel jLabelHeight = new JLabel(MessageBundle.getMessage("angal.examination.height")); //$NON-NLS-1$
				GridBagConstraints gbc_jLabelHeight = new GridBagConstraints();
				gbc_jLabelHeight.anchor = GridBagConstraints.WEST;
				gbc_jLabelHeight.insets = new Insets(5, 5, 5, 5);
				gbc_jLabelHeight.gridx = 1;
				gbc_jLabelHeight.gridy = 1;
				jPanelExamination.add(jLabelHeight, gbc_jLabelHeight);
				
				GridBagConstraints gbc_jSliderHeight = new GridBagConstraints();
				gbc_jSliderHeight.insets = new Insets(5, 5, 5, 5);
				gbc_jSliderHeight.fill = GridBagConstraints.HORIZONTAL;
				gbc_jSliderHeight.gridx = 2;
				gbc_jSliderHeight.gridy = 1;
				jPanelExamination.add(getJSliderHeight(), gbc_jSliderHeight);
				
				JLabel jLabelHeightUnit = new JLabel(MessageBundle.getMessage("angal.common.uom.centimeter"));
				GridBagConstraints gbc_jLabelHeightUnit = new GridBagConstraints();
				gbc_jLabelHeightUnit.insets = new Insets(5, 5, 5, 5);
				gbc_jLabelHeightUnit.gridx = 3;
				gbc_jLabelHeightUnit.gridy = 1;
				jPanelExamination.add(jLabelHeightUnit, gbc_jLabelHeightUnit);
				
				GridBagConstraints gbc_jTextFieldHeight = new GridBagConstraints();
				gbc_jTextFieldHeight.anchor = GridBagConstraints.WEST;
				gbc_jTextFieldHeight.insets = new Insets(0, 0, 5, 5);
				gbc_jTextFieldHeight.gridx = 4;
				gbc_jTextFieldHeight.gridy = 1;
				jPanelExamination.add(getJTextFieldHeight(), gbc_jTextFieldHeight);
			}
			
			{
				jLabelWeightAbb = new JLabel(MessageBundle.getMessage("angal.examination.weightabbr")); //$NON-NLS-1$
				GridBagConstraints gbc_lblw = new GridBagConstraints();
				gbc_lblw.insets = new Insets(5, 5, 5, 5);
				gbc_lblw.gridx = 0;
				gbc_lblw.gridy = 2;
				jPanelExamination.add(jLabelWeightAbb, gbc_lblw);
	
				JLabel jLabelWeight = new JLabel(MessageBundle.getMessage("angal.examination.weight")); //$NON-NLS-1$
				GridBagConstraints gbc_jLabelWeight = new GridBagConstraints();
				gbc_jLabelWeight.anchor = GridBagConstraints.WEST;
				gbc_jLabelWeight.insets = new Insets(5, 5, 5, 5);
				gbc_jLabelWeight.gridx = 1;
				gbc_jLabelWeight.gridy = 2;
				jPanelExamination.add(jLabelWeight, gbc_jLabelWeight);
	
				GridBagConstraints gbc_jSliderWeight = new GridBagConstraints();
				gbc_jSliderWeight.insets = new Insets(5, 5, 5, 5);
				gbc_jSliderWeight.fill = GridBagConstraints.HORIZONTAL;
				gbc_jSliderWeight.gridx = 2;
				gbc_jSliderWeight.gridy = 2;
				jPanelExamination.add(getJSliderWeight(), gbc_jSliderWeight);
				
				JLabel jLabelWeightUnit = new JLabel(MessageBundle.getMessage("angal.common.uom.kg"));
				GridBagConstraints gbc_jLabelWeightUnit = new GridBagConstraints();
				gbc_jLabelWeightUnit.insets = new Insets(5, 5, 5, 5);
				gbc_jLabelWeightUnit.gridx = 3;
				gbc_jLabelWeightUnit.gridy = 2;
				jPanelExamination.add(jLabelWeightUnit, gbc_jLabelWeightUnit);
				
				GridBagConstraints gbc_jTextFieldWeight = new GridBagConstraints();
				gbc_jTextFieldWeight.anchor = GridBagConstraints.WEST;
				gbc_jTextFieldWeight.insets = new Insets(0, 0, 5, 5);
				gbc_jTextFieldWeight.gridx = 4;
				gbc_jTextFieldWeight.gridy = 2;
				jPanelExamination.add(getJTextFieldWeight(), gbc_jTextFieldWeight);
			}
			
			{
				GridBagConstraints gbc_lblap = new GridBagConstraints();
				gbc_lblap.insets = new Insets(5, 5, 5, 5);
				gbc_lblap.gridx = 0;
				gbc_lblap.gridy = 3;
				jPanelExamination.add(getJCheckBoxAP(), gbc_lblap);
	
				JLabel jLabelAPmin = new JLabel(MessageBundle.getMessage("angal.examination.arterialpressure")); //$NON-NLS-1$
				GridBagConstraints labelGbc_3 = new GridBagConstraints();
				labelGbc_3.insets = new Insets(5, 5, 5, 5);
				labelGbc_3.gridx = 1;
				labelGbc_3.gridy = 3;
				jPanelExamination.add(jLabelAPmin, labelGbc_3);
				
				GridBagConstraints gbc_panel = new GridBagConstraints();
				gbc_panel.insets = new Insets(0, 0, 5, 5);
				gbc_panel.fill = GridBagConstraints.BOTH;
				gbc_panel.gridx = 2;
				gbc_panel.gridy = 3;
				jPanelExamination.add(getJPanelAPPanel(), gbc_panel);
				
				JLabel jLabelAPUnit = new JLabel(MessageBundle.getMessage("angal.common.uom.mmHg"));
				GridBagConstraints gbc_jLabelAPUnit = new GridBagConstraints();
				gbc_jLabelAPUnit.insets = new Insets(5, 5, 5, 5);
				gbc_jLabelAPUnit.gridx = 3;
				gbc_jLabelAPUnit.gridy = 3;
				jPanelExamination.add(jLabelAPUnit, gbc_jLabelAPUnit);
			}
			
			{
				GridBagConstraints gbc_lblhr = new GridBagConstraints();
				gbc_lblhr.insets = new Insets(5, 5, 5, 5);
				gbc_lblhr.gridx = 0;
				gbc_lblhr.gridy = 4;
				jPanelExamination.add(getJCheckBoxToggleHR(), gbc_lblhr);
				
				JLabel jLabelHR = new JLabel(MessageBundle.getMessage("angal.examination.heartrate")); //$NON-NLS-1$
				GridBagConstraints gbc_jLabelHR = new GridBagConstraints();
				gbc_jLabelHR.anchor = GridBagConstraints.WEST;
				gbc_jLabelHR.insets = new Insets(5, 5, 5, 5);
				gbc_jLabelHR.gridx = 1;
				gbc_jLabelHR.gridy = 4;
				jPanelExamination.add(jLabelHR, gbc_jLabelHR);
				
				GridBagConstraints gbc_jSliderHR = new GridBagConstraints();
				gbc_jSliderHR.insets = new Insets(5, 5, 5, 5);
				gbc_jSliderHR.fill = GridBagConstraints.HORIZONTAL;
				gbc_jSliderHR.gridx = 2;
				gbc_jSliderHR.gridy = 4;
				jPanelExamination.add(getJSliderHR(), gbc_jSliderHR);
				
				JLabel jLabelHRUnit = new JLabel(MessageBundle.getMessage("angal.common.uom.bpm"));
				GridBagConstraints gbc_jLabelHRUnit = new GridBagConstraints();
				gbc_jLabelHRUnit.insets = new Insets(5, 5, 5, 5);
				gbc_jLabelHRUnit.gridx = 3;
				gbc_jLabelHRUnit.gridy = 4;
				jPanelExamination.add(jLabelHRUnit, gbc_jLabelHRUnit);
				
				GridBagConstraints gbc_jTextFieldHR = new GridBagConstraints();
				gbc_jTextFieldHR.anchor = GridBagConstraints.WEST;
				gbc_jTextFieldHR.insets = new Insets(5, 0, 5, 5);
				gbc_jTextFieldHR.gridx = 4;
				gbc_jTextFieldHR.gridy = 4;
				jPanelExamination.add(getJTextFieldHR(), gbc_jTextFieldHR);
			}
			
			{
				GridBagConstraints gbc_lbltemp = new GridBagConstraints();
				gbc_lbltemp.insets = new Insets(5, 5, 5, 5);
				gbc_lbltemp.gridx = 0;
				gbc_lbltemp.gridy = 5;
				jPanelExamination.add(getJCheckBoxToggleTemp(), gbc_lbltemp);
				
				JLabel jLabelTemp = new JLabel(MessageBundle.getMessage("angal.examination.temperature")); //$NON-NLS-1$
				GridBagConstraints gbc_jLabelTemp = new GridBagConstraints();
				gbc_jLabelTemp.anchor = GridBagConstraints.WEST;
				gbc_jLabelTemp.insets = new Insets(5, 5, 5, 5);
				gbc_jLabelTemp.gridx = 1;
				gbc_jLabelTemp.gridy = 5;
				jPanelExamination.add(jLabelTemp, gbc_jLabelTemp);
				
				GridBagConstraints gbc_jSliderTemp = new GridBagConstraints();
				gbc_jSliderTemp.insets = new Insets(5, 5, 5, 5);
				gbc_jSliderTemp.fill = GridBagConstraints.HORIZONTAL;
				gbc_jSliderTemp.gridx = 2;
				gbc_jSliderTemp.gridy = 5;
				jPanelExamination.add(getJSliderTemp(), gbc_jSliderTemp);
				
				JLabel jLabelTempUnit = new JLabel(MessageBundle.getMessage("angal.common.uom.celsius"));
				GridBagConstraints gbc_jLabelTempUnit = new GridBagConstraints();
				gbc_jLabelTempUnit.insets = new Insets(5, 5, 5, 5);
				gbc_jLabelTempUnit.gridx = 3;
				gbc_jLabelTempUnit.gridy = 5;
				jPanelExamination.add(jLabelTempUnit, gbc_jLabelTempUnit);
				
				GridBagConstraints gbc_jTextFieldTemp = new GridBagConstraints();
				gbc_jTextFieldTemp.anchor = GridBagConstraints.WEST;
				gbc_jTextFieldTemp.insets = new Insets(5, 0, 5, 5);
				gbc_jTextFieldTemp.gridx = 4;
				gbc_jTextFieldTemp.gridy = 5;
				jPanelExamination.add(getJTextFieldTemp(), gbc_jTextFieldTemp);
			}
			
			{
				GridBagConstraints gbc_lblsaturation = new GridBagConstraints();
				gbc_lblsaturation.insets = new Insets(5, 5, 5, 5);
				gbc_lblsaturation.gridx = 0;
				gbc_lblsaturation.gridy = 6;
				jPanelExamination.add(getJCheckBoxToggleSaturation(), gbc_lblsaturation);
				
				JLabel jLabelSaturation = new JLabel(MessageBundle.getMessage("angal.examination.saturation")); //$NON-NLS-1$
				GridBagConstraints gbc_jLabelSaturation = new GridBagConstraints();
				gbc_jLabelSaturation.anchor = GridBagConstraints.WEST;
				gbc_jLabelSaturation.insets = new Insets(5, 5, 5, 5);
				gbc_jLabelSaturation.gridx = 1;
				gbc_jLabelSaturation.gridy = 6;
				jPanelExamination.add(jLabelSaturation, gbc_jLabelSaturation);
	
				GridBagConstraints gbc_jSliderSaturation = new GridBagConstraints();
				gbc_jSliderSaturation.insets = new Insets(5, 5, 5, 5);
				gbc_jSliderSaturation.fill = GridBagConstraints.HORIZONTAL;
				gbc_jSliderSaturation.gridx = 2;
				gbc_jSliderSaturation.gridy = 6;
				jPanelExamination.add(getJSliderSaturation(), gbc_jSliderSaturation);
				
				JLabel jLabelSaturationUnit = new JLabel(MessageBundle.getMessage("angal.common.uom.percentage"));
				GridBagConstraints gbc_jLabelSaturationUnit = new GridBagConstraints();
				gbc_jLabelSaturationUnit.insets = new Insets(5, 5, 5, 5);
				gbc_jLabelSaturationUnit.gridx = 3;
				gbc_jLabelSaturationUnit.gridy = 6;
				jPanelExamination.add(jLabelSaturationUnit, gbc_jLabelSaturationUnit);
	
				GridBagConstraints gbc_jTextFieldSaturation = new GridBagConstraints();
				gbc_jTextFieldSaturation.anchor = GridBagConstraints.WEST;
				gbc_jTextFieldSaturation.insets = new Insets(5, 0, 5, 5);
				gbc_jTextFieldSaturation.gridx = 4;
				gbc_jTextFieldSaturation.gridy = 6;
				jPanelExamination.add(getJTextFieldSaturation(), gbc_jTextFieldSaturation);
			}
			
			{
				GridBagConstraints gbc_chkBoxHGT = new GridBagConstraints();
				gbc_chkBoxHGT.insets = new Insets(5, 5, 5, 5);
				gbc_chkBoxHGT.gridx = 0;
				gbc_chkBoxHGT.gridy = 7;
				jPanelExamination.add(getJCheckBoxToggleHGT(), gbc_chkBoxHGT);
				
				JLabel jLabelHGT = new JLabel(MessageBundle.getMessage("angal.examination.hgt")); //$NON-NLS-1$
				GridBagConstraints gbc_jLabelHGT = new GridBagConstraints();
				gbc_jLabelHGT.anchor = GridBagConstraints.WEST;
				gbc_jLabelHGT.insets = new Insets(5, 5, 5, 5);
				gbc_jLabelHGT.gridx = 1;
				gbc_jLabelHGT.gridy = 7;
				jPanelExamination.add(jLabelHGT, gbc_jLabelHGT);
				
				GridBagConstraints gbc_jSliderHGT = new GridBagConstraints();
				gbc_jSliderHGT.insets = new Insets(5, 5, 5, 5);
				gbc_jSliderHGT.fill = GridBagConstraints.HORIZONTAL;
				gbc_jSliderHGT.gridx = 2;
				gbc_jSliderHGT.gridy = 7;
				jPanelExamination.add(getJSliderHGT(), gbc_jSliderHGT);
				
				JLabel jLabelHGTUnit = new JLabel(MessageBundle.getMessage("angal.common.uom.mgdl"));
				GridBagConstraints gbc_jLabelHGTUnit = new GridBagConstraints();
				gbc_jLabelHGTUnit.insets = new Insets(5, 5, 5, 5);
				gbc_jLabelHGTUnit.gridx = 3;
				gbc_jLabelHGTUnit.gridy = 7;
				jPanelExamination.add(jLabelHGTUnit, gbc_jLabelHGTUnit);
	
				GridBagConstraints gbc_jTextFieldHGT = new GridBagConstraints();
				gbc_jTextFieldHGT.anchor = GridBagConstraints.WEST;
				gbc_jTextFieldHGT.insets = new Insets(5, 0, 5, 5);
				gbc_jTextFieldHGT.gridx = 4;
				gbc_jTextFieldHGT.gridy = 7;
				jPanelExamination.add(getJTextFieldHGT(), gbc_jTextFieldHGT);
			}
			
			{
				GridBagConstraints gbc_chkBoxDiuresis = new GridBagConstraints();
				gbc_chkBoxDiuresis.insets = new Insets(5, 5, 5, 5);
				gbc_chkBoxDiuresis.gridx = 0;
				gbc_chkBoxDiuresis.gridy = 8;
				jPanelExamination.add(getJCheckBoxToggleDiuresis(), gbc_chkBoxDiuresis);
				
				JLabel jLabelDiuresis = new JLabel(MessageBundle.getMessage("angal.examination.diuresis")); //$NON-NLS-1$
				GridBagConstraints gbc_jLabelDiuresis = new GridBagConstraints();
				gbc_jLabelDiuresis.anchor = GridBagConstraints.WEST;
				gbc_jLabelDiuresis.insets = new Insets(5, 5, 5, 5);
				gbc_jLabelDiuresis.gridx = 1;
				gbc_jLabelDiuresis.gridy = 8;
				jPanelExamination.add(jLabelDiuresis, gbc_jLabelDiuresis);
				
				GridBagConstraints gbc_jSliderDiuresis = new GridBagConstraints();
				gbc_jSliderDiuresis.insets = new Insets(5, 5, 5, 5);
				gbc_jSliderDiuresis.fill = GridBagConstraints.HORIZONTAL;
				gbc_jSliderDiuresis.gridx = 2;
				gbc_jSliderDiuresis.gridy = 8;
				jPanelExamination.add(getJSliderDiuresis(), gbc_jSliderDiuresis);
				
				JLabel jLabelDiuresisUnit = new JLabel(MessageBundle.getMessage("angal.common.uom.milliliter"));
				GridBagConstraints gbc_jLabelDiuresisUnit = new GridBagConstraints();
				gbc_jLabelDiuresisUnit.insets = new Insets(5, 5, 5, 5);
				gbc_jLabelDiuresisUnit.gridx = 3;
				gbc_jLabelDiuresisUnit.gridy = 8;
				jPanelExamination.add(jLabelDiuresisUnit, gbc_jLabelDiuresisUnit);
	
				GridBagConstraints gbc_jTextFieldDiuresis = new GridBagConstraints();
				gbc_jTextFieldDiuresis.anchor = GridBagConstraints.WEST;
				gbc_jTextFieldDiuresis.insets = new Insets(5, 0, 5, 5);
				gbc_jTextFieldDiuresis.gridx = 4;
				gbc_jTextFieldDiuresis.gridy = 8;
				jPanelExamination.add(getJTextFieldDiuresis(), gbc_jTextFieldDiuresis);
				
				GridBagConstraints gbc_comboBoxDiuresis = new GridBagConstraints();
				gbc_comboBoxDiuresis.anchor = GridBagConstraints.CENTER;
				gbc_comboBoxDiuresis.insets = new Insets(5, 0, 5, 5);
				gbc_comboBoxDiuresis.gridx = 4;
				gbc_comboBoxDiuresis.gridy = 9;
				jPanelExamination.add(getJComboBoxDiuresis(), gbc_comboBoxDiuresis);
			}
			
			{
				GridBagConstraints gbc_chkBoxBowel = new GridBagConstraints();
				gbc_chkBoxBowel.insets = new Insets(5, 5, 5, 5);
				gbc_chkBoxBowel.gridx = 0;
				gbc_chkBoxBowel.gridy = 10;
				jPanelExamination.add(getJCheckBoxToggleBowel(), gbc_chkBoxBowel);
				
				JLabel jLabelBowel = new JLabel(MessageBundle.getMessage("angal.examination.bowel")); //$NON-NLS-1$
				GridBagConstraints gbc_jLabelBowel = new GridBagConstraints();
				gbc_jLabelBowel.anchor = GridBagConstraints.WEST;
				gbc_jLabelBowel.insets = new Insets(5, 5, 5, 5);
				gbc_jLabelBowel.gridx = 1;
				gbc_jLabelBowel.gridy = 10;
				jPanelExamination.add(jLabelBowel, gbc_jLabelBowel);
				
				GridBagConstraints gbc_comboBoxBowel = new GridBagConstraints();
				gbc_comboBoxBowel.anchor = GridBagConstraints.WEST;
				gbc_comboBoxBowel.insets = new Insets(5, 0, 5, 5);
				gbc_comboBoxBowel.gridx = 2;
				gbc_comboBoxBowel.gridy = 10;
				jPanelExamination.add(getJComboBoxBowel(), gbc_comboBoxBowel);
			}
		}
		return jPanelExamination;
	}
	
	private JComboBox getJComboBoxDiuresis() {
		if (jComboBoxDiuresis == null) {
			jComboBoxDiuresis = new JComboBox();
			ArrayList<String> diuresisDescription = examManager.getDiuresisDescriptionList();
			for (String description : diuresisDescription) {
				jComboBoxDiuresis.addItem(description);
			}
			jComboBoxDiuresis.addItemListener(new ItemListener() {
				
				@Override
				public void itemStateChanged(ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						patex.setPex_diuresis_desc(examManager.getDiuresisDescriptionKey((String) e.getItem()));
					}
				}
			});
		}
		return jComboBoxDiuresis;
	}
	
	private JComboBox getJComboBoxBowel() {
		if (jComboBoxBowel == null) {
			jComboBoxBowel = new JComboBox();
			ArrayList<String> bowelDescription = examManager.getBowelDescriptionList();
			for (String description : bowelDescription) {
				jComboBoxBowel.addItem(description);
			}
			jComboBoxBowel.addItemListener(new ItemListener() {
				
				@Override
				public void itemStateChanged(ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						patex.setPex_bowel_desc(examManager.getBowelDescriptionKey((String) e.getItem()));
					}
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
	
	private JCheckBox getJCheckBoxToggleDiuresis() {
		if (jCheckBoxToggleDiuresis == null) {
			jCheckBoxToggleDiuresis = new JCheckBox(""); //$NON-NLS-1$
			jCheckBoxToggleDiuresis.setAction(getActionToggleDiuresis());
			jCheckBoxToggleDiuresis.addFocusListener(new CheckBoxFocus());
			jCheckBoxToggleDiuresis.setFocusPainted(true);
		}
		return jCheckBoxToggleDiuresis;
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

	private CustomJDateChooser getJDateChooserDate() {
		if (jDateChooserDate == null) {
			jDateChooserDate = new CustomJDateChooser();
			//jDateChooserDate.setLocale(new Locale(GeneralData.LANGUAGE));
			jDateChooserDate.setLocale(new Locale("en")); //$NON-NLS-1$
			jDateChooserDate.setDateFormatString("dd/MM/yyyy - HH:mm"); //$NON-NLS-1$
			jDateChooserDate.addPropertyChangeListener("date", new PropertyChangeListener() {
				
				@Override
				public void propertyChange(PropertyChangeEvent evt) {
					Date date = (Date) evt.getNewValue();
					jDateChooserDate.setDate(date);
					patex.setPex_date(new Timestamp(date.getTime()));
					
				}
			});
		}
		return jDateChooserDate;
	}

	private VoLimitedTextArea getJTextAreaNote() {
		if (jTextAreaNote == null) {
			jTextAreaNote = new VoLimitedTextArea(300, 6, 20);
			jTextAreaNote.setLineWrap(true);
			jTextAreaNote.setWrapStyleWord(true);
			jTextAreaNote.setMargin(new Insets(0, 5, 0, 0));
			jTextAreaNote.addFocusListener(new FocusAdapter() {

				@Override
				public void focusLost(FocusEvent e) {
					super.focusLost(e);
					patex.setPex_note(jTextAreaNote.getText());
				}
			});
		}
		jTextAreaNote.setSize(jTextAreaNote.getPreferredSize());
		return jTextAreaNote;
	}
	
	private JPanel getJPanelNote() {
		if (jNotePanel == null) {
			jNotePanel = new JPanel(new BorderLayout());
			
			GridBagLayout gbl_jNotePanel = new GridBagLayout();
			gbl_jNotePanel.rowWeights = new double[] { 0.0, 1.0};
			jNotePanel.setLayout(gbl_jNotePanel);
			
			JLabel jLabelNote = new JLabel(MessageBundle.getMessage("angal.examination.note")); //$NON-NLS-1$
			GridBagConstraints gbc_jLabelNote = new GridBagConstraints();
			gbc_jLabelNote.anchor = GridBagConstraints.WEST;
			gbc_jLabelNote.insets = new Insets(5, 5, 5, 5);
			gbc_jLabelNote.gridx = 0;
			gbc_jLabelNote.gridy = 0;
			jNotePanel.add(jLabelNote, gbc_jLabelNote);
			
			GridBagConstraints gbc_jScrollPaneNote = new GridBagConstraints();
			gbc_jScrollPaneNote.fill = GridBagConstraints.BOTH;
			gbc_jScrollPaneNote.insets = new Insets(5, 5, 5, 5);
			gbc_jScrollPaneNote.gridx = 0;
			gbc_jScrollPaneNote.gridy = 1;
			jNotePanel.add(getJScrollPaneNote(), gbc_jScrollPaneNote);
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
			jSpinnerAPmax = new VoIntegerTextField(0,3);
			jSpinnerAPmax.setInputVerifier(new MinMaxIntegerInputVerifier(0));
			jSpinnerAPmax.addFocusListener(new FocusListener() {
				
				@Override
				public void focusLost(FocusEvent e) {
					patex.setPex_ap_max((Integer) jSpinnerAPmax.getValue());
					
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
			jTextFieldHGT = new VoIntegerTextField(ExaminationParameters.HGT_INIT,5);
			jTextFieldHGT.setInputVerifier(new MinMaxIntegerInputVerifier(0));
			jTextFieldHGT.addFocusListener(new FocusListener() {
				
				@Override
				public void focusLost(FocusEvent e) {
					int height = Integer.parseInt(jTextFieldHGT.getText());
					jSliderHGT.setValue(height);
					patex.setPex_hgt(height);
					
				}
				
				@Override
				public void focusGained(FocusEvent e) {
					((JTextField) e.getSource()).selectAll();
				}
			});
		}
		return jTextFieldHGT;
	}
	
	private VoIntegerTextField getJTextFieldDiuresis() {
		if (jTextFieldDiuresis == null) {
			jTextFieldDiuresis = new VoIntegerTextField(ExaminationParameters.DIURESIS_INIT,5);
			jTextFieldDiuresis.setInputVerifier(new MinMaxIntegerInputVerifier(0));
			jTextFieldDiuresis.addFocusListener(new FocusListener() {
				
				@Override
				public void focusLost(FocusEvent e) {
					int height = Integer.parseInt(jTextFieldDiuresis.getText());
					jSliderDiuresis.setValue(height);
					patex.setPex_diuresis(height);
					
				}
				
				@Override
				public void focusGained(FocusEvent e) {
					((JTextField) e.getSource()).selectAll();
				}
			});
		}
		return jTextFieldDiuresis;
	}

	private VoIntegerTextField getJTextFieldHeight() {
		if (jTextFieldHeight == null) {
			jTextFieldHeight = new VoIntegerTextField(0,5);
			jTextFieldHeight.setInputVerifier(new MinMaxIntegerInputVerifier(ExaminationParameters.HEIGHT_MIN, ExaminationParameters.HEIGHT_MAX));
			jTextFieldHeight.addFocusListener(new FocusListener() {
				
				@Override
				public void focusLost(FocusEvent e) {
					int height = Integer.parseInt(jTextFieldHeight.getText());
					jSliderHeight.setValue(height);
					patex.setPex_height(height);
					
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
			jTextFieldWeight = new VoDoubleTextField(0,5);
			jTextFieldWeight.setInputVerifier(new MinMaxIntegerInputVerifier(ExaminationParameters.WEIGHT_MIN, ExaminationParameters.WEIGHT_MAX));
			jTextFieldWeight.addFocusListener(new FocusListener() {
				
				@Override
				public void focusLost(FocusEvent e) {
					double weight = Double.parseDouble(jTextFieldWeight.getText());
					jSliderWeight.setValue(weight);
					patex.setPex_weight(weight);
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
			jTextFieldHR = new VoIntegerTextField(0,5);
			jTextFieldHR.setInputVerifier(new MinMaxIntegerInputVerifier(ExaminationParameters.HR_MIN, ExaminationParameters.HR_MAX));
			jTextFieldHR.addFocusListener(new FocusListener() {
				
				@Override
				public void focusLost(FocusEvent e) {
					int hr = Integer.parseInt(jTextFieldHR.getText());
					jSliderHR.setValue(hr);
					patex.setPex_hr(hr);
				}
				
				@Override
				public void focusGained(FocusEvent e) {
					((JTextField) e.getSource()).selectAll();
				}
			});	
		}
		return jTextFieldHR;
	}

	private JSlider getJSliderHeight() {
		if (jSliderHeight == null) {
			jSliderHeight = new JSlider(ExaminationParameters.HEIGHT_MIN, ExaminationParameters.HEIGHT_MAX, ExaminationParameters.HEIGHT_INIT);
			jSliderHeight.addChangeListener(new ChangeListener() {
				
				@Override
				public void stateChanged(ChangeEvent e) {
					int value = jSliderHeight.getValue();
					jTextFieldHeight.setText(String.valueOf(value));
					patex.setPex_height(value);
					updateBMI();
				}
			});
			jSliderHeight.setFocusable(false);
		}
		return jSliderHeight;
	}
	
	private ScaledJSlider getJSliderWeight() {
		if (jSliderWeight == null) {
			jSliderWeight = new ScaledJSlider(ExaminationParameters.WEIGHT_MIN, ExaminationParameters.WEIGHT_MAX, ExaminationParameters.WEIGHT_STEP, ExaminationParameters.WEIGHT_INIT);
			jSliderWeight.addChangeListener(new ChangeListener() {
				
				@Override
				public void stateChanged(ChangeEvent e) {
					double value = jSliderWeight.getScaledValue();
					//double weight = (double) value / 10;
					jTextFieldWeight.setText(String.valueOf(value));
					patex.setPex_weight(value);
					updateBMI();
				}
			});
			jSliderWeight.setFocusable(false);
		}
		return jSliderWeight;
	}
	
	private ScaledJSlider getJSliderTemp() {
		if (jSliderTemp == null) {
			jSliderTemp = new ScaledJSlider(ExaminationParameters.TEMP_MIN, ExaminationParameters.TEMP_MAX, ExaminationParameters.TEMP_STEP, ExaminationParameters.TEMP_INIT);
			jSliderTemp.addChangeListener(new ChangeListener() {
				
				@Override
				public void stateChanged(ChangeEvent e) {
					double value = jSliderTemp.getScaledValue();
					jTextFieldTemp.setText(String.valueOf(value));
					patex.setPex_temp(value);
				}
			});
			jSliderTemp.setFocusable(false);
		}
		return jSliderTemp;
	}
	
	private ScaledJSlider getJSliderSaturation() {
		if (jSliderSaturation == null) {
			jSliderSaturation = new ScaledJSlider(ExaminationParameters.SAT_MIN, 100, ExaminationParameters.SAT_STEP, ExaminationParameters.SAT_INIT); //MAX / STEP
			jSliderSaturation.addChangeListener(new ChangeListener() {
				
				@Override
				public void stateChanged(ChangeEvent e) {
					double value = jSliderSaturation.getScaledValue();
					jTextFieldSaturation.setText(String.valueOf(value));
					patex.setPex_sat(value);
				}
			});
			jSliderSaturation.setFocusable(false);
		}
		return jSliderSaturation;
	}
	
	private JSlider getJSliderDiuresis() {
		if (jSliderDiuresis == null) {
			jSliderDiuresis = new JSlider(0, 200, ExaminationParameters.DIURESIS_INIT);
			jSliderDiuresis.addChangeListener(new ChangeListener() {
				
				@Override
				public void stateChanged(ChangeEvent e) {
					int value = jSliderDiuresis.getValue();
					jTextFieldDiuresis.setText(String.valueOf(value));
					patex.setPex_diuresis(value);
				}
			});
			jSliderDiuresis.setFocusable(false);
		}
		return jSliderDiuresis;
	}
	
	private JSlider getJSliderHGT() {
		if (jSliderHGT == null) {
			jSliderHGT = new JSlider(0, 200, ExaminationParameters.HGT_INIT);
			jSliderHGT.addChangeListener(new ChangeListener() {
				
				@Override
				public void stateChanged(ChangeEvent e) {
					int value = jSliderHGT.getValue();
					jTextFieldHGT.setText(String.valueOf(value));
					patex.setPex_hgt(value);
				}
			});
			jSliderHGT.setFocusable(false);
		}
		return jSliderHGT;
	}
	
	private JSlider getJSliderHR() {
		if (jSliderHR == null) {
			jSliderHR = new JSlider(ExaminationParameters.HR_MIN, ExaminationParameters.HR_MAX, ExaminationParameters.HR_INIT);
			jSliderHR.addChangeListener(new ChangeListener() {
				
				@Override
				public void stateChanged(ChangeEvent e) {
					int hr = jSliderHR.getValue();
					jTextFieldHR.setText(String.valueOf(hr));
					patex.setPex_hr(hr);
				}
			});
			jSliderHR.setFocusable(false);
		}
		return jSliderHR;
	}
	
	private JButton getJButtonOK() {
		if (jButtonOK == null) {
			jButtonOK = new JButton();
			jButtonOK.setMnemonic(KeyEvent.VK_O);
			jButtonOK.setAction(getActionSavePatientExamination());
		}
		return jButtonOK;
	}
	
	private JButton getJButtonDelete() {
		if (jButtonDelete == null) {
			jButtonDelete = new JButton();
			jButtonDelete = new JButton(MessageBundle.getMessage("angal.common.delete")); //$NON-NLS-1$
			jButtonDelete.setMnemonic(KeyEvent.VK_D);
			jButtonDelete.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					int[] row = jTableSummary.getSelectedRows();
					if (row.length == 0) {
						JOptionPane.showMessageDialog(PatientExaminationEdit.this, MessageBundle.getMessage("angal.common.pleaseselectarow"));
						return;
					}
					int ok = JOptionPane.showConfirmDialog(PatientExaminationEdit.this, MessageBundle.getMessage("angal.common.doyouwanttoproceed"));
					if (ok == JOptionPane.OK_OPTION) {
						ArrayList<PatientExamination> patexList = new ArrayList<PatientExamination>();
						for (int i = 0; i < row.length; i++) {
							patexList.add((PatientExamination) jTableSummary.getModel().getValueAt(row[i], -1));
						}
						try {
							examManager.remove(patexList);
						}catch(OHServiceException ex){
							if(ex.getMessages() != null){
								for(OHExceptionMessage msg : ex.getMessages()){
									JOptionPane.showMessageDialog(null, msg.getMessage(), msg.getTitle() == null ? "" : msg.getTitle(), msg.getLevel().getSwingSeverity());
								}
							}
						} finally {
							JTableModelSummary model = (JTableModelSummary) jTableSummary.getModel();
							model.reloadData();
						}
					} else return;
				}
			});
		}
		return jButtonDelete;
	}
	
	private JButton getJButtonCancel() {
		if (jButtonCancel == null) {
			jButtonCancel = new JButton(MessageBundle.getMessage("angal.common.close")); //$NON-NLS-1$
			jButtonCancel.setMnemonic(KeyEvent.VK_C);
			jButtonCancel.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					dispose();
				}
			});
		}
		return jButtonCancel;
	}
	
	private JButton getJButtonPrint() {
		if (jButtonPrint == null) {
			jButtonPrint = new JButton(MessageBundle.getMessage("angal.common.print")); //$NON-NLS-1$
			jButtonPrint.setMnemonic(KeyEvent.VK_C);
			jButtonPrint.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					new GenericReportPatient(patex.getPatient().getCode(), GeneralData.EXAMINATIONCHART);
				}
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
			jLabelGender = new JLabel(); //$NON-NLS-1$
			if (isMale) 
				jLabelGender.setIcon(new ImageIcon(PATH_MALE_GENDER));
			else
				jLabelGender.setIcon(new ImageIcon(PATH_FEMALE_GENDER));
			jLabelGender.setAlignmentX(0.5f);
			jLabelGender.setAlignmentY(0.5f);
		}
		return jLabelGender;
	}
	
	private final class MinMaxIntegerInputVerifier extends InputVerifier {
		
		boolean bottom = false;
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
					JOptionPane.showMessageDialog(PatientExaminationEdit.this, 
							MessageBundle.getMessage("angal.common.thisvaluecannotbelesserthan") + min,
							MessageBundle.getMessage("angal.hospital"), 
							JOptionPane.ERROR_MESSAGE);
					return false;
				}
				if (ceiling && value > max) {
					JOptionPane.showMessageDialog(PatientExaminationEdit.this, 
							MessageBundle.getMessage("angal.common.thisvaluecannotbegreaterthan") + max,
							MessageBundle.getMessage("angal.hospital"), 
							JOptionPane.ERROR_MESSAGE);
					return false;
				}
			} catch (NumberFormatException e) {
				return false;
			}
			return true;
		}
	}

	private class SwingActionSavePatientExamination extends AbstractAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		public SwingActionSavePatientExamination() {
			putValue(NAME, MessageBundle.getMessage("angal.common.savem"));
			putValue(MNEMONIC_KEY, KeyEvent.VK_O);
			putValue(SHORT_DESCRIPTION, MessageBundle.getMessage("angal.examination.tooltip.savepatientexamination")); //$NON-NLS-1$
		}
		public void actionPerformed(ActionEvent e) {
			
			try {
				examManager.saveOrUpdate(patex);
			}catch(OHServiceException ex){
				if(ex.getMessages() != null){
					for(OHExceptionMessage msg : ex.getMessages()){
						JOptionPane.showMessageDialog(null, msg.getMessage(), msg.getTitle() == null ? "" : msg.getTitle(), msg.getLevel().getSwingSeverity());
					}
				}
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
		patex.setPex_ap_min((Integer)jSpinnerAPmin.getValue());
		jSpinnerAPmax.setEnabled(true);
		patex.setPex_ap_max((Integer)jSpinnerAPmax.getValue());
	}

	private void disableAP() {
		jSpinnerAPmin.setEnabled(false);
		patex.setPex_ap_min(null);
		jSpinnerAPmax.setEnabled(false);
		patex.setPex_ap_max(null);
	}
	
	private class SwingActionToggleAP extends AbstractAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		public SwingActionToggleAP() {
			putValue(NAME, ""); //$NON-NLS-1$
			putValue(SHORT_DESCRIPTION, MessageBundle.getMessage("angal.examination.tooltip.toggleexamination")); //$NON-NLS-1$
		}
		public void actionPerformed(ActionEvent e) {
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
	}

	private void disableHGT() {
		jSliderHGT.setEnabled(false);
		jTextFieldHGT.setEnabled(false);
		patex.setPex_hgt(null);
	}
	
	private void enableBowel() throws NumberFormatException {
		jComboBoxBowel.setEnabled(true);
		patex.setPex_bowel_desc(examManager.getBowelDescriptionKey((String) jComboBoxBowel.getSelectedItem()));
	}

	private void disableBowel() {
		jComboBoxBowel.setEnabled(false);
		patex.setPex_bowel_desc(null);
	}
	
	private void enableDiuresis() throws NumberFormatException {
		jSliderDiuresis.setEnabled(true);
		jTextFieldDiuresis.setEnabled(true);
		jComboBoxDiuresis.setEnabled(true);
		String text = jTextFieldDiuresis.getText();
		if (!text.equals("")) {
			patex.setPex_diuresis(Integer.parseInt(text));
			patex.setPex_diuresis_desc(examManager.getDiuresisDescriptionKey((String) jComboBoxDiuresis.getSelectedItem()));
		} else {
			patex.setPex_diuresis(null);
			patex.setPex_diuresis_desc(null);
		}
	}

	private void disableDiuresis() {
		jSliderDiuresis.setEnabled(false);
		jTextFieldDiuresis.setEnabled(false);
		jComboBoxDiuresis.setEnabled(false);
		patex.setPex_diuresis(null);
		patex.setPex_diuresis_desc(null);
	}
	
	private void enableHR() throws NumberFormatException {
		jSliderHR.setEnabled(true);
		jTextFieldHR.setEnabled(true);
		patex.setPex_hr(Integer.parseInt(jTextFieldHR.getText()));
	}

	private void disableHR() {
		jSliderHR.setEnabled(false);
		jTextFieldHR.setEnabled(false);
		patex.setPex_hr(null);
	}
	
	private class SwingActionToggleHGT extends AbstractAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		public SwingActionToggleHGT() {
			putValue(NAME, ""); //$NON-NLS-1$
			putValue(SHORT_DESCRIPTION, MessageBundle.getMessage("angal.examination.tooltip.toggleexamination")); //$NON-NLS-1$
		}
		public void actionPerformed(ActionEvent e) {
			if (!jCheckBoxToggleHGT.isSelected()) {
				disableHGT();
			} else {
				enableHGT();
			}
		}
	}
	
	private class SwingActionToggleBowel extends AbstractAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		public SwingActionToggleBowel() {
			putValue(NAME, ""); //$NON-NLS-1$
			putValue(SHORT_DESCRIPTION, MessageBundle.getMessage("angal.examination.tooltip.toggleexamination")); //$NON-NLS-1$
		}
		public void actionPerformed(ActionEvent e) {
			if (!jCheckBoxToggleBowel.isSelected()) {
				disableBowel();
			} else {
				enableBowel();
			}
		}
	}
	
	private class SwingActionToggleDiuresis extends AbstractAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		public SwingActionToggleDiuresis() {
			putValue(NAME, ""); //$NON-NLS-1$
			putValue(SHORT_DESCRIPTION, MessageBundle.getMessage("angal.examination.tooltip.toggleexamination")); //$NON-NLS-1$
		}
		public void actionPerformed(ActionEvent e) {
			if (!jCheckBoxToggleDiuresis.isSelected()) {
				disableDiuresis();
			} else {
				enableDiuresis();
			}
		}
	}
	
	private class SwingActionToggleSaturation extends AbstractAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		public SwingActionToggleSaturation() {
			putValue(NAME, ""); //$NON-NLS-1$
			putValue(SHORT_DESCRIPTION, MessageBundle.getMessage("angal.examination.tooltip.toggleexamination")); //$NON-NLS-1$
		}
		public void actionPerformed(ActionEvent e) {
			if (!jCheckBoxToggleSaturation.isSelected()) {
				disableSaturation();
			} else {
				enableSaturation();
			}
		}
	}
	
	private class SwingActionToggleTemp extends AbstractAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		public SwingActionToggleTemp() {
			putValue(NAME, ""); //$NON-NLS-1$
			putValue(SHORT_DESCRIPTION, MessageBundle.getMessage("angal.examination.tooltip.toggleexamination")); //$NON-NLS-1$
		}
		public void actionPerformed(ActionEvent e) {
			if (!jCheckBoxToggleTemp.isSelected()) {
				disableTemp();
			} else {
				enableTemp();
			}
		}
	}
	
	private class SwingActionToggleHR extends AbstractAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		public SwingActionToggleHR() {
			putValue(NAME, ""); //$NON-NLS-1$
			putValue(SHORT_DESCRIPTION, MessageBundle.getMessage("angal.examination.tooltip.toggleexamination")); //$NON-NLS-1$
		}
		public void actionPerformed(ActionEvent e) {
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
	
	private Action getActionToggleHGT() {
		if (actionToggleHGT == null) {
			actionToggleHGT = new SwingActionToggleHGT();
		}
		return actionToggleHGT;
	}
	
	private Action getActionToggleDiuresis() {
		if (actionToggleDiuresis == null) {
			actionToggleDiuresis = new SwingActionToggleDiuresis();
		}
		return actionToggleDiuresis;
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
		scrollPane.setPreferredSize(new Dimension(400, 150));
		jTableSummary = new JTable(new JTableModelSummary());
		for (int i = 0; i < columnNames.length; i++) {
			jTableSummary.getColumnModel().getColumn(i).setCellRenderer(new EnabledTableCellRenderer());
			jTableSummary.getColumnModel().getColumn(i).setMinWidth(columnWidth[i]);
		}
		jTableSummary.setShowGrid(false);
		
		JTableHeader header = jTableSummary.getTableHeader();
		header.setBackground(Color.white);
		
		scrollPane.setViewportView(jTableSummary);
		scrollPane.getViewport().setBackground(Color.white);
		
		return scrollPane;
	}

	private JEditorPane getJEditorPaneSummary() {
		if (jEditorPaneSummary == null) {
			jEditorPaneSummary = new JEditorPane();
			jEditorPaneSummary.setFont(new Font("Arial", Font.PLAIN, 11));
			jEditorPaneSummary.setContentType("text/html");
			jEditorPaneSummary.setEditable(false);
			jEditorPaneSummary.setBorder(BorderFactory.createLineBorder(Color.GRAY));
			
		}
		return jEditorPaneSummary;
	}
	
	public class JTableModelSummary extends AbstractTableModel {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		private ExaminationBrowserManager examManager = Context.getApplicationContext().getBean(ExaminationBrowserManager.class);
		private ArrayList<PatientExamination> patexList = null;

		public JTableModelSummary() {
			reloadData();
		}
		
		public void reloadData() {
			try {
				patexList = examManager.getLastNByPatID(patex.getPatient().getCode().intValue(), ExaminationParameters.LIST_SIZE);
			}catch(OHServiceException e){
				if(e.getMessages() != null){
					for(OHExceptionMessage msg : e.getMessages()){
						JOptionPane.showMessageDialog(null, msg.getMessage(), msg.getTitle() == null ? "" : msg.getTitle(), msg.getLevel().getSwingSeverity());
					}
				}
			}
			fireTableDataChanged();
		}
		
		public ArrayList<PatientExamination> getList() {
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
			String datetime = new SimpleDateFormat(DATE_FORMAT).format(new Date(patex.getPex_date().getTime()));
			String diuresis = patex.getPex_diuresis_desc() == null ? "-" : examManager.getDiuresisDescriptionTranslated(patex.getPex_diuresis_desc());
			String bowel = patex.getPex_bowel_desc() == null ? "-" : examManager.getBowelDescriptionTranslated(patex.getPex_bowel_desc());
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
				return patex.getPex_diuresis() == null ? "-" : patex.getPex_diuresis();
			} else if (c == 9) {
				return diuresis;
			} else if (c == 10) {
				return bowel;
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
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			setHorizontalAlignment(columnAlignment[column]);
			return cell;
		}
	}
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		
		/*
		 * Default Values
		 */
		final int INITIAL_HEIGHT = 170;
		final double INITIAL_WEIGHT = 80.;
		final int INITIAL_AP_MIN = 80;
		final int INITIAL_AP_MAX = 120;
		final int INITIAL_HR = 60;
		
		
		PropertyConfigurator.configure(new File("./rsc/log4j.properties").getAbsolutePath());
		ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
		Context.setApplicationContext(context);
		GeneralData.getGeneralData();
		
		try {
			ExaminationBrowserManager examManager = Context.getApplicationContext().getBean(ExaminationBrowserManager.class);
			PatientExamination patex = examManager.getLastByPatID(500);
			
			
//			PatientExamination patex = new PatientExamination();
//			Patient patient = new Patient();
//			patient.setCode(1);
//			patex.setPatient(patient);
//			patex.setPex_date(new Timestamp(new Date().getTime()));
//			patex.setPex_height(INITIAL_HEIGHT);
//			patex.setPex_weight(INITIAL_WEIGHT);
//			patex.setPex_ap_min(INITIAL_AP_MIN);
//			patex.setPex_ap_max(INITIAL_AP_MAX);
//			patex.setPex_hr(INITIAL_HR);
			
			GenderPatientExamination gpatex = new GenderPatientExamination(patex, patex.getPatient().getSex() == 'M');
			
			PatientExaminationEdit dialog = new PatientExaminationEdit(gpatex);
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.pack();
			dialog.setLocationRelativeTo(null);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
