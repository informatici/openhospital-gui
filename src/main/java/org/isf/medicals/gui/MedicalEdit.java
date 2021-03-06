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
package org.isf.medicals.gui;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.EventListenerList;

import org.isf.generaldata.MessageBundle;
import org.isf.medicals.manager.MedicalBrowsingManager;
import org.isf.medicals.model.Medical;
import org.isf.medtype.manager.MedicalTypeBrowserManager;
import org.isf.medtype.model.MedicalType;
import org.isf.menu.manager.Context;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.exception.model.OHExceptionMessage;
import org.isf.utils.exception.model.OHSeverityLevel;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.VoDoubleTextField;
import org.isf.utils.jobjects.VoIntegerTextField;
import org.isf.utils.jobjects.VoLimitedTextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 18-ago-2008
 * added by alex:
 * 	- product code
 *  - pieces per packet
 */
public class MedicalEdit extends JDialog {

	private static final long serialVersionUID = 1L;

	private static final Logger LOGGER = LoggerFactory.getLogger(MedicalEdit.class);

	private EventListenerList medicalListeners = new EventListenerList();
	
	public interface MedicalListener extends EventListener {
        void medicalUpdated(AWTEvent e);
        void medicalInserted(Medical medical);
    }

    public void addMedicalListener(MedicalListener l) {
    	medicalListeners.add(MedicalListener.class, l);
    }

    public void removeMedicalListener(MedicalListener listener) {
    	medicalListeners.remove(MedicalListener.class, listener);
    }

	private void fireMedicalInserted(Medical medical) {
		new AWTEvent(new Object(), AWTEvent.RESERVED_ID_MAX + 1) {

			private static final long serialVersionUID = 1L;
		};

		EventListener[] listeners = medicalListeners.getListeners(MedicalListener.class);
		for (int i = 0; i < listeners.length; i++)
			((MedicalListener) listeners[i]).medicalInserted(medical);
	}

	private void fireMedicalUpdated() {
		AWTEvent event = new AWTEvent(new Object(), AWTEvent.RESERVED_ID_MAX + 1) {

			private static final long serialVersionUID = 1L;
		};

		EventListener[] listeners = medicalListeners.getListeners(MedicalListener.class);
		for (int i = 0; i < listeners.length; i++)
			((MedicalListener) listeners[i]).medicalUpdated(event);
	}

	private JPanel jContentPane = null;
	private JPanel dataPanel = null;
	private JPanel buttonPanel = null;
	private JButton cancelButton = null;
	private JButton okButton = null;
	private JLabel descLabel = null;
	private JLabel codeLabel = null;
	private JLabel pcsperpckLabel = null;
	private JLabel criticLabel = null;
	private VoIntegerTextField pcsperpckField = null;
	private VoLimitedTextField descriptionTextField = null;
	private VoLimitedTextField codeTextField = null;
	private VoDoubleTextField minQtiField = null;
	private JLabel typeLabel = null;
	private JComboBox typeComboBox = null;
	private Medical oldMedical = null;
	private Medical medical = null;
	private boolean insert = false;

	private MedicalTypeBrowserManager medicalTypeManager = Context.getApplicationContext().getBean(MedicalTypeBrowserManager.class);
	private MedicalBrowsingManager medicalBrowsingManager = Context.getApplicationContext().getBean(MedicalBrowsingManager.class);

	/**
	 * This is the default constructor; we pass the arraylist and the
	 * selectedrow because we need to update them
	 */
	public MedicalEdit(Medical old, boolean inserting, JFrame owner) {
		super(owner,true);
		insert = inserting;
		try {
			oldMedical = (Medical) old.clone();
		} catch (CloneNotSupportedException cloneNotSupportedException) {
			LOGGER.error(cloneNotSupportedException.getMessage(), cloneNotSupportedException);
		}
		medical = old; // medical will be used for every operation
		initialize();
	}

	/**
	 * This method initializes this
	 */
	private void initialize() {

		//this.setBounds(300, 300, 350, 240);
		this.setContentPane(getJContentPane());
		if (insert) {
			this.setTitle(MessageBundle.getMessage("angal.medicals.newmedical.title"));
		} else {
			this.setTitle(MessageBundle.getMessage("angal.medicals.editmedical.title"));
		}
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.pack();
		this.setLocationRelativeTo(null);
	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jContentPane = new JPanel();
			jContentPane.setLayout(new BorderLayout());
			jContentPane.add(getDataPanel(), java.awt.BorderLayout.CENTER); // Generated
			jContentPane.add(getButtonPanel(), java.awt.BorderLayout.SOUTH); // Generated
		}
		return jContentPane;
	}

	/**
	 * This method initializes dataPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getDataPanel() {
		if (dataPanel == null) {
			dataPanel = new JPanel();
			dataPanel.setLayout(new BoxLayout(dataPanel, BoxLayout.Y_AXIS)); // Generated
			
			typeLabel = new JLabel();
			typeLabel.setText(MessageBundle.getMessage("angal.medicals.type")); // Generated
			typeLabel.setAlignmentX(CENTER_ALIGNMENT);
			codeLabel = new JLabel();
			codeLabel.setText(MessageBundle.getMessage("angal.common.code.txt"));
			codeLabel.setAlignmentX(CENTER_ALIGNMENT);
			descLabel = new JLabel(MessageBundle.getMessage("angal.common.description.txt"));
			descLabel.setAlignmentX(CENTER_ALIGNMENT);
			pcsperpckLabel = new JLabel();
			pcsperpckLabel.setText(MessageBundle.getMessage("angal.medicals.pcsperpckExt")); // Generated
			pcsperpckLabel.setAlignmentX(CENTER_ALIGNMENT);
			criticLabel = new JLabel();
			criticLabel.setText(MessageBundle.getMessage("angal.medicals.criticallevel")); // Generated
			criticLabel.setAlignmentX(CENTER_ALIGNMENT);
			
			dataPanel.add(typeLabel, null); // Generated
			dataPanel.add(getTypeComboBox(), null); // Generated
			dataPanel.add(codeLabel, null); // Generated
			dataPanel.add(getCodeTextField(), null); // Generated
			dataPanel.add(descLabel, null); // Generated
			dataPanel.add(getDescriptionTextField(), null); // Generated
			dataPanel.add(pcsperpckLabel, null);
			dataPanel.add(getPcsperpckField());
			dataPanel.add(criticLabel, null);
			dataPanel.add(getMinQtiField());
		}
		return dataPanel;
	}

	/**
	 * This method initializes buttonPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getButtonPanel() {
		if (buttonPanel == null) {
			buttonPanel = new JPanel();
			buttonPanel.add(getOkButton(), null); // Generated
			buttonPanel.add(getCancelButton(), null); // Generated
		}
		return buttonPanel;
	}

	/**
	 * This method initializes cancelButton
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getCancelButton() {
		if (cancelButton == null) {
			cancelButton = new JButton(MessageBundle.getMessage("angal.common.cancel.btn"));
			cancelButton.setMnemonic(MessageBundle.getMnemonic("angal.common.cancel.btn.key"));
			cancelButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					dispose();
				}
			});
		}
		return cancelButton;
	}

	/**
	 * This method initializes okButton
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getOkButton() {
		if (okButton == null) {
			okButton = new JButton(MessageBundle.getMessage("angal.common.ok.btn"));
			okButton.setMnemonic(MessageBundle.getMnemonic("angal.common.ok.btn.key"));
			okButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					Medical newMedical = null;
					try {
						newMedical = (Medical) medical.clone();
						newMedical.setType((MedicalType) typeComboBox.getSelectedItem());
						newMedical.setDescription(descriptionTextField.getText());
						newMedical.setProd_code(codeTextField.getText());
						newMedical.setPcsperpck(pcsperpckField.getValue());
						newMedical.setMinqty(minQtiField.getValue());
					} catch (CloneNotSupportedException cloneNotSupportedException) {
						LOGGER.error(cloneNotSupportedException.getMessage(), cloneNotSupportedException);
					}
					boolean result = false;
					if (insert) { // inserting
						try {
							result = medicalBrowsingManager.newMedical(newMedical);
						} catch (OHServiceException e1) {
							OHServiceExceptionUtil.showMessages(e1, MedicalEdit.this);
							List<OHExceptionMessage> errors = e1.getMessages();

							for (OHExceptionMessage error : errors) {
								if (error.getLevel() == OHSeverityLevel.WARNING) {
									if (error.getTitle().equals("similarsFoundWarning")) {
										int ok = manageSimilarFoundWarning(error);

										if (ok == JOptionPane.OK_OPTION) {
											try {
												result = medicalBrowsingManager.newMedical(newMedical, true);
											} catch (OHServiceException e2) {
												OHServiceExceptionUtil.showMessages(e2);
											}
										}
									}
								}
							}
						}
						if (result) {
							fireMedicalInserted(newMedical);
							dispose();
						}
					} else { // updating
						try {
							result = medicalBrowsingManager.updateMedical(newMedical);
						} catch (OHServiceException e1) {
							List<OHExceptionMessage> errors = e1.getMessages();

							for (OHExceptionMessage error : errors) {
								OHServiceExceptionUtil.showMessages(e1, MedicalEdit.this);
								if (error.getLevel() == OHSeverityLevel.WARNING) {
									if (error.getTitle().equals("similarsFoundWarning")) {
										int ok = manageSimilarFoundWarning(error);

										if (ok == JOptionPane.OK_OPTION) {
											try {
												result = medicalBrowsingManager.updateMedical(newMedical, true);
											} catch (OHServiceException e2) {
												OHServiceExceptionUtil.showMessages(e2);
											}
										}
									}
								}
							}
						}
						if (result) {
							medical.setType((MedicalType) typeComboBox.getSelectedItem());
							medical.setDescription(descriptionTextField.getText());
							medical.setProd_code(codeTextField.getText());
							medical.setPcsperpck(pcsperpckField.getValue());
							medical.setMinqty(minQtiField.getValue());
							fireMedicalUpdated();
							dispose();
						}
					}
					if (!result) {
						MessageDialog.error(null, "angal.common.datacouldnotbesaved.msg");
					}
				}

				private int manageSimilarFoundWarning(OHExceptionMessage error) {
					/* Already shown by OHServiceExceptionUtil
					int messageType = error.getLevel().getSwingSeverity();
					JOptionPane.showMessageDialog(MedicalEdit.this, error.getMessage(),
							error.getTitle(), messageType);*/
					return MessageDialog.yesNoCancel(MedicalEdit.this, "angal.common.doyouwanttoproceed.msg");
				}
			});
		}
		return okButton;
	}

	/**
	 * This method initializes descriptionTextField
	 * 
	 * @return javax.swing.JTextField
	 */
	private VoLimitedTextField getDescriptionTextField() {
		if (descriptionTextField == null) {
			if (insert) {
				descriptionTextField = new VoLimitedTextField(100,50);
			} else {
				descriptionTextField = new VoLimitedTextField(100,50);
				descriptionTextField.setText(medical.getDescription());
			}
		}
		return descriptionTextField;
	}

	/**
	 * This method initializes codeTextField
	 * 
	 * @return javax.swing.JTextField
	 */
	private VoLimitedTextField getCodeTextField() {
		if (codeTextField == null) {
			if (insert) {
				codeTextField = new VoLimitedTextField(5);
			} else {
				codeTextField = new VoLimitedTextField(5);
				codeTextField.setText(medical.getProd_code());
			}
		}
		return codeTextField;
	}

	private JTextField getMinQtiField() {
		if (minQtiField == null) {
			if (insert)
				minQtiField = new VoDoubleTextField(0,3);
			else
				minQtiField = new VoDoubleTextField(medical.getMinqty(),3);
		}
		return minQtiField;
	}
	
	private JTextField getPcsperpckField() {
		if (pcsperpckField == null) {
			if (insert)
				pcsperpckField = new VoIntegerTextField(1,3);
			else
				pcsperpckField = new VoIntegerTextField(medical.getPcsperpck(),3);
		}
		return pcsperpckField;
	}

	/**
	 * This method initializes typeComboBox
	 * 
	 * @return javax.swing.JComboBox
	 */
	private JComboBox getTypeComboBox() {
		if (typeComboBox == null) {
			typeComboBox = new JComboBox();
			if (insert) {
				ArrayList<MedicalType> types;
				try {
					types = medicalTypeManager.getMedicalType();
					
					for (MedicalType elem : types) {
						typeComboBox.addItem(elem);
					}
				} catch (OHServiceException e) {
					OHServiceExceptionUtil.showMessages(e);
					
				}
			} else {
				typeComboBox.addItem(medical.getType());
				typeComboBox.setEnabled(false);
			}

		}
		return typeComboBox;
	}

}
