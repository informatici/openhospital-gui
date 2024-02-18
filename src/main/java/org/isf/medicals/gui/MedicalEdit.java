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
package org.isf.medicals.gui;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.EventListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.WindowConstants;
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
import org.isf.utils.layout.SpringUtilities;
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
		for (EventListener listener : listeners) {
			((MedicalListener) listener).medicalInserted(medical);
		}
	}

	private void fireMedicalUpdated() {
		AWTEvent event = new AWTEvent(new Object(), AWTEvent.RESERVED_ID_MAX + 1) {

			private static final long serialVersionUID = 1L;
		};

		EventListener[] listeners = medicalListeners.getListeners(MedicalListener.class);
		for (EventListener listener : listeners) {
			((MedicalListener) listener).medicalUpdated(event);
		}
	}

	private JPanel jContentPane;
	private JPanel dataPanel;
	private JPanel buttonPanel;
	private JButton cancelButton;
	private JButton okButton;
	private VoIntegerTextField pcsperpckField;
	private VoLimitedTextField descriptionTextField;
	private VoLimitedTextField codeTextField;
	private VoDoubleTextField minQtiField;
	private JComboBox<MedicalType> typeComboBox;
	private Medical oldMedical;
	private Medical medical;
	private boolean insert;

	private MedicalTypeBrowserManager medicalTypeManager = Context.getApplicationContext().getBean(MedicalTypeBrowserManager.class);
	private MedicalBrowsingManager medicalBrowsingManager = Context.getApplicationContext().getBean(MedicalBrowsingManager.class);

	/**
	 * This is the default constructor; we pass the arraylist and the
	 * selectedrow because we need to update them
	 */
	public MedicalEdit(Medical old, boolean inserting, JFrame owner) {
		super(owner, true);
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
		this.setContentPane(getJContentPane());
		if (insert) {
			this.setTitle(MessageBundle.getMessage("angal.medicals.newmedical.title"));
		} else {
			this.setTitle(MessageBundle.getMessage("angal.medicals.editmedical.title"));
		}
		this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
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
			jContentPane.add(getDataPanel(), BorderLayout.CENTER);
			jContentPane.add(getButtonPanel(), BorderLayout.SOUTH);
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
			dataPanel = new JPanel(new SpringLayout());

			JLabel typeLabel = new JLabel(MessageBundle.getMessage("angal.medicals.type") + ':');
			JLabel codeLabel = new JLabel(MessageBundle.getMessage("angal.common.code.txt") + ':');
			JLabel descLabel = new JLabel(MessageBundle.getMessage("angal.common.description.txt") + ':');
			JLabel pcsperpckLabel = new JLabel(MessageBundle.getMessage("angal.medicals.pcsperpckExt") + ':');
			JLabel criticLabel = new JLabel(MessageBundle.getMessage("angal.medicals.criticallevel") + ':');

			dataPanel.add(typeLabel);
			dataPanel.add(getTypeComboBox());
			dataPanel.add(codeLabel);
			dataPanel.add(getCodeTextField());
			dataPanel.add(descLabel);
			dataPanel.add(getDescriptionTextField());
			dataPanel.add(pcsperpckLabel);
			dataPanel.add(getPcsperpckField());
			dataPanel.add(criticLabel);
			dataPanel.add(getMinQtiField());
			SpringUtilities.makeCompactGrid(dataPanel, 5, 2, 5, 5, 5, 5);
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
			buttonPanel.add(getOkButton(), null);
			buttonPanel.add(getCancelButton(), null);
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
			cancelButton.addActionListener(actionEvent -> dispose());
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
			okButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					boolean result = false;
					if (insert) { // inserting
						Medical newMedical = null;
						try {
							newMedical = (Medical) medical.clone();
							newMedical.setType((MedicalType) typeComboBox.getSelectedItem());
							newMedical.setDescription(descriptionTextField.getText());
							newMedical.setProdCode(codeTextField.getText());
							newMedical.setPcsperpck(pcsperpckField.getValue());
							newMedical.setMinqty(minQtiField.getValue());
						} catch (CloneNotSupportedException cloneNotSupportedException) {
							LOGGER.error(cloneNotSupportedException.getMessage(), cloneNotSupportedException);
						}
						try {
							Medical insertedMedical = medicalBrowsingManager.newMedical(newMedical);
							if (insertedMedical != null) {
								result = true;
							}
						} catch (OHServiceException e1) {
							OHServiceExceptionUtil.showMessages(e1, MedicalEdit.this);
							List<OHExceptionMessage> errors = e1.getMessages();

							for (OHExceptionMessage error : errors) {
								if (error.getLevel() == OHSeverityLevel.WARNING) {
									if (error.getTitle().equals("similarsFoundWarning")) {
										int ok = manageSimilarFoundWarning(error);

										if (ok == JOptionPane.OK_OPTION) {
											try {
												Medical insertedMedical = medicalBrowsingManager.newMedical(newMedical, true);
												if (insertedMedical != null) {
													result = true;
												}
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
						oldMedical.setType((MedicalType) typeComboBox.getSelectedItem());
						oldMedical.setDescription(descriptionTextField.getText());
						oldMedical.setProdCode(codeTextField.getText());
						oldMedical.setPcsperpck(pcsperpckField.getValue());
						oldMedical.setMinqty(minQtiField.getValue());
						try {
							Medical updatedMedical = medicalBrowsingManager.updateMedical(oldMedical);
							if (updatedMedical != null) {
								result = true;
							}
						} catch (OHServiceException e1) {
							List<OHExceptionMessage> errors = e1.getMessages();

							for (OHExceptionMessage error : errors) {
								OHServiceExceptionUtil.showMessages(e1, MedicalEdit.this);
								if (error.getLevel() == OHSeverityLevel.WARNING) {
									if (error.getTitle().equals("similarsFoundWarning")) {
										int ok = manageSimilarFoundWarning(error);

										if (ok == JOptionPane.OK_OPTION) {
											try {
												Medical updatedMedical = medicalBrowsingManager.updateMedical(oldMedical, true);
												if (updatedMedical != null) {
													result = true;
												}
											} catch (OHServiceException e2) {
												OHServiceExceptionUtil.showMessages(e2);
											}
										}
									}
								}
							}
						}
						if (result) {
							Medical updatedMedical;
							try {
								updatedMedical = medicalBrowsingManager.getMedical(oldMedical.getCode());
							} catch (OHServiceException exception) {
								LOGGER.error(exception.getMessage(), exception);
								updatedMedical = medical;
							}
							medical.setType((MedicalType) typeComboBox.getSelectedItem());
							medical.setDescription(descriptionTextField.getText());
							medical.setProdCode(codeTextField.getText());
							medical.setPcsperpck(pcsperpckField.getValue());
							medical.setMinqty(minQtiField.getValue());
							medical.setLock(updatedMedical.getLock());
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
				descriptionTextField = new VoLimitedTextField(100, 50);
			} else {
				descriptionTextField = new VoLimitedTextField(100, 50);
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
				codeTextField.setText(medical.getProdCode());
			}
		}
		return codeTextField;
	}

	private JTextField getMinQtiField() {
		if (minQtiField == null) {
			if (insert) {
				minQtiField = new VoDoubleTextField(0, 3);
			} else {
				minQtiField = new VoDoubleTextField(medical.getMinqty(), 3);
			}
		}
		return minQtiField;
	}

	private JTextField getPcsperpckField() {
		if (pcsperpckField == null) {
			if (insert) {
				pcsperpckField = new VoIntegerTextField(1, 3);
			} else {
				pcsperpckField = new VoIntegerTextField(medical.getPcsperpck(), 3);
			}
		}
		return pcsperpckField;
	}

	/**
	 * This method initializes typeComboBox
	 * 
	 * @return javax.swing.JComboBox
	 */
	private JComboBox<MedicalType> getTypeComboBox() {
		if (typeComboBox == null) {
			typeComboBox = new JComboBox<>();
			if (insert) {
				List<MedicalType> types;
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
