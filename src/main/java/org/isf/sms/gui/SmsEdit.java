/*
 * Open Hospital (www.open-hospital.org)
 * Copyright © 2006-2023 Informatici Senza Frontiere (info@informaticisenzafrontiere.org)
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
package org.isf.sms.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.time.LocalDateTime;
import java.util.regex.Pattern;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import org.isf.generaldata.MessageBundle;
import org.isf.menu.manager.Context;
import org.isf.menu.manager.UserBrowsingManager;
import org.isf.patient.gui.SelectPatient;
import org.isf.patient.gui.SelectPatient.SelectionListener;
import org.isf.patient.model.Patient;
import org.isf.sms.manager.SmsManager;
import org.isf.sms.model.Sms;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.GoodDateTimeToggleChooser;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.time.TimeTools;

/**
 * @author Mwithi
 */
public class SmsEdit extends JDialog implements SelectionListener {

	private static final long serialVersionUID = 1L;
	private static final Pattern SPACE_PATTERN = Pattern.compile(" ");

	private JPanel jCenterPanel;
	private JPanel jButtonPanel;
	private JPanel jNorthPanel;
	private JTextField jNumberTextField;
	private JButton jOkButton;
	private JButton jCancelButton;
	private JPanel panel;
	private JLabel jCharactersLabel;
	private JLabel jLabelCount;
	private JTextArea jTextArea;
	private GoodDateTimeToggleChooser jSchedDateChooser;
	private JButton jPatientButton;

	private int maxLength;

	private SmsManager smsManager = Context.getApplicationContext().getBean(SmsManager.class);

	/**
	 * Create the dialog.
	 */
	public SmsEdit(JFrame owner) {
		super(owner, true);
		initialize();
		initComponents();
	}
	
	private void initialize() {
		maxLength = SmsManager.MAX_LENGHT;
	}

	private void initComponents() {
		setTitle(MessageBundle.getMessage("angal.sms.newsms.title"));
		setResizable(false);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		add(getJNorthPanel(), BorderLayout.NORTH);
		add(getJCenterPanel(), BorderLayout.CENTER);
		add(getJButtonPanel(), BorderLayout.SOUTH);
		setPreferredSize(new Dimension(450, 300));
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}

	private JPanel getJNorthPanel() {
		if (jNorthPanel == null) {
			jNorthPanel = new JPanel();
			jNorthPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
			GridBagLayout panel = new GridBagLayout();
			panel.columnWidths = new int[] { 46, 110, 0, 0 };
			panel.rowHeights = new int[] { 20, 0, 0, 0 };
			panel.columnWeights = new double[] { 0.0, 0.0, 0.0, Double.MIN_VALUE };
			panel.rowWeights = new double[] { 0.0, 0.0, 0.0, Double.MIN_VALUE };
			jNorthPanel.setLayout(panel);

			JLabel jSchedDateLabel = new JLabel(MessageBundle.getMessage("angal.sms.scheduleddate")); //$NON-NLS-1$
			GridBagConstraints gbcSchedDateLabel = new GridBagConstraints();
			gbcSchedDateLabel.anchor = GridBagConstraints.WEST;
			gbcSchedDateLabel.insets = new Insets(0, 0, 5, 5);
			gbcSchedDateLabel.gridx = 0;
			gbcSchedDateLabel.gridy = 0;
			jNorthPanel.add(jSchedDateLabel, gbcSchedDateLabel);

			GridBagConstraints gbcSchedDateChooser = new GridBagConstraints();
			gbcSchedDateChooser.insets = new Insets(0, 0, 5, 5);
			gbcSchedDateChooser.anchor = GridBagConstraints.NORTHWEST;
			gbcSchedDateChooser.gridx = 1;
			gbcSchedDateChooser.gridy = 0;
			jNorthPanel.add(getJSchedDateChooser(), gbcSchedDateChooser);

			JLabel jNumberLabel = new JLabel(MessageBundle.getMessage("angal.sms.number")); //$NON-NLS-1$
			GridBagConstraints gbcNumberLabel = new GridBagConstraints();
			gbcNumberLabel.anchor = GridBagConstraints.WEST;
			gbcNumberLabel.insets = new Insets(0, 0, 0, 5);
			gbcNumberLabel.gridx = 0;
			gbcNumberLabel.gridy = 2;
			jNorthPanel.add(jNumberLabel, gbcNumberLabel);
			jNumberTextField = new JTextField();
			GridBagConstraints gbcNumberTextField = new GridBagConstraints();
			gbcNumberTextField.fill = GridBagConstraints.HORIZONTAL;
			gbcNumberTextField.insets = new Insets(0, 0, 0, 5);
			gbcNumberTextField.gridx = 1;
			gbcNumberTextField.gridy = 2;
			jNorthPanel.add(jNumberTextField, gbcNumberTextField);
			jNumberTextField.setColumns(15);
			GridBagConstraints gbcPatientButton = new GridBagConstraints();
			gbcPatientButton.gridx = 2;
			gbcPatientButton.gridy = 2;
			jNorthPanel.add(getJPatientButton(), gbcPatientButton);
		}
		return jNorthPanel;
	}
	
	private JButton getJPatientButton() {
		if (jPatientButton == null) {
			jPatientButton = new JButton();
			jPatientButton.setIcon(new ImageIcon("./rsc/icons/other_button.png")); //$NON-NLS-1$
			jPatientButton.addActionListener(actionEvent -> {
				SelectPatient sp = new SelectPatient(this, "");
				sp.addSelectionListener(this);
				sp.pack();
				sp.setVisible(true);
			});
		}
		return jPatientButton;
	}
	
	private GoodDateTimeToggleChooser getJSchedDateChooser() {
		if (jSchedDateChooser == null) {
			jSchedDateChooser = new GoodDateTimeToggleChooser(TimeTools.getNow());
		}
		return jSchedDateChooser;
	}

	private JPanel getJCenterPanel() {
		if (jCenterPanel == null) {
			jCenterPanel = new JPanel();
			jCenterPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
			jCenterPanel.setLayout(new BorderLayout(0, 0));
			jCenterPanel.add(new JScrollPane(getJTextArea()));
			jCenterPanel.add(getPanel(), BorderLayout.SOUTH);
		}
		return jCenterPanel;
	}

	private JTextArea getJTextArea() {
		if (jTextArea == null) {
			jTextArea = new JTextArea();
			jTextArea.setWrapStyleWord(true);
			jTextArea.setLineWrap(true);
			jTextArea.addKeyListener(new KeyListener() {
				
				@Override
				public void keyTyped(KeyEvent e) {}
				
				@Override
				public void keyReleased(KeyEvent e) {
					JTextArea thisTextArea = (JTextArea) e.getComponent();
					int remainingChars = maxLength - thisTextArea.getText().length();
					jLabelCount.setText(String.valueOf(remainingChars));
				}
				
				@Override
				public void keyPressed(KeyEvent e) {}
				
			});
		}
		return jTextArea;
	}

	private JPanel getJButtonPanel() {
		if (jButtonPanel == null) {
			jButtonPanel = new JPanel();
			jButtonPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
			jButtonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
			jButtonPanel.add(getJOkButton());
			jButtonPanel.add(getJCancelButton());
		}
		return jButtonPanel;
	}
	
	private JButton getJOkButton() {
		if (jOkButton == null) {
			jOkButton = new JButton(MessageBundle.getMessage("angal.common.ok.btn"));
			jOkButton.setMnemonic(MessageBundle.getMnemonic("angal.common.ok.btn.key"));
			jOkButton.addActionListener(actionEvent -> {
				String number = SPACE_PATTERN.matcher(jNumberTextField.getText()).replaceAll("").trim();
				String text = jTextArea.getText();
				LocalDateTime schedDate = jSchedDateChooser.getLocalDateTime();
				if (schedDate == null) {

					MessageDialog.error(this, "angal.sms.pleaseenteravaliddateandtime.msg");
					return;
				}

				Sms smsToSend = new Sms();
				smsToSend.setSmsNumber(number);
				smsToSend.setSmsDateSched(schedDate);
				smsToSend.setSmsUser(UserBrowsingManager.getCurrentUser());
				smsToSend.setSmsText(text);
				smsToSend.setModule("smsmanager");

				try {
					smsManager.saveOrUpdate(smsToSend, false);
				} catch (OHServiceException e1) {

					if (e1.getMessages().get(0).getTitle().equals("testMaxLenghtError")) {
						int textLength = text.length();
						int textParts = (textLength + maxLength - 1) / maxLength;
						StringBuilder message = new StringBuilder();
						message.append(e1.getMessages().get(0).getMessage())
								.append('\n')
								.append(MessageBundle.getMessage("angal.sms.doyouwanttosplitinmoremessages"))
								.append(" (").append(textParts).append(")?");

						int ok = JOptionPane.showConfirmDialog(this, message.toString());
						if (ok == JOptionPane.YES_OPTION) {
							try {
								smsManager.saveOrUpdate(smsToSend, true);
							} catch (OHServiceException e2) {
								OHServiceExceptionUtil.showMessages(e2, this);
								return;
							}

						} else {
							return;
						}
					} else {
						OHServiceExceptionUtil.showMessages(e1, this);
						return;
					}
				}
				dispose();
			});
		}
		return jOkButton;
	}
	
	private JButton getJCancelButton() {
		if (jCancelButton == null) {
			jCancelButton = new JButton(MessageBundle.getMessage("angal.common.cancel.btn"));
			jCancelButton.setMnemonic(MessageBundle.getMnemonic("angal.common.cancel.btn.key"));
			jCancelButton.addActionListener(actionEvent -> dispose());
		}
		return jCancelButton;
	}

	private JPanel getPanel() {
		if (panel == null) {
			panel = new JPanel();
			GridBagLayout gridBagLayout = new GridBagLayout();
			gridBagLayout.columnWidths = new int[]{366, 53, 0};
			gridBagLayout.rowHeights = new int[]{14, 0};
			gridBagLayout.columnWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
			gridBagLayout.rowWeights = new double[]{0.0, Double.MIN_VALUE};
			panel.setLayout(gridBagLayout);
			GridBagConstraints gbcCharactersLabel = new GridBagConstraints();
			gbcCharactersLabel.insets = new Insets(0, 0, 0, 5);
			gbcCharactersLabel.anchor = GridBagConstraints.NORTHEAST;
			gbcCharactersLabel.gridx = 0;
			gbcCharactersLabel.gridy = 0;
			panel.add(getJCharactersLabel(), gbcCharactersLabel);
			GridBagConstraints gbcLabelCount = new GridBagConstraints();
			gbcLabelCount.anchor = GridBagConstraints.EAST;
			gbcLabelCount.gridx = 1;
			gbcLabelCount.gridy = 0;
			panel.add(getJLabelCount(), gbcLabelCount);
		}
		return panel;
	}
	
	private JLabel getJCharactersLabel() {
		if (jCharactersLabel == null) {
			jCharactersLabel = new JLabel(MessageBundle.getMessage("angal.sms.Characters")); //$NON-NLS-1$
			jCharactersLabel.setForeground(Color.GRAY);
		}
		return jCharactersLabel;
	}
	
	private JLabel getJLabelCount() {
		if (jLabelCount == null) {
			jLabelCount = new JLabel(String.valueOf(maxLength));
			jLabelCount.setForeground(Color.GRAY);
		}
		return jLabelCount;
	}

	@Override
	public void patientSelected(Patient patient) {
		jNumberTextField.setText(patient.getTelephone());
	}
}