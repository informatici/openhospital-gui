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
package org.isf.exa.gui;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.EventListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.WindowConstants;
import javax.swing.event.EventListenerList;

import org.isf.exa.manager.ExamBrowsingManager;
import org.isf.exa.model.Exam;
import org.isf.exatype.model.ExamType;
import org.isf.generaldata.MessageBundle;
import org.isf.menu.manager.Context;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.VoLimitedTextField;
import org.isf.utils.layout.SpringUtilities;

/**
 * ExamEdit - add/edit an exam
 */
public class ExamEdit extends JDialog {

	private static final long serialVersionUID = 1L;

	private EventListenerList examListeners = new EventListenerList();

    public interface ExamListener extends EventListener {
        void examUpdated(AWTEvent e);
        void examInserted(AWTEvent e);
    }

    public void addExamListener(ExamListener l) {
    	examListeners.add(ExamListener.class, l);
    }

    public void removeExamListener(ExamListener listener) {
    	examListeners.remove(ExamListener.class, listener);
    }

	private void fireExamInserted() {
		AWTEvent event = new AWTEvent(new Object(), AWTEvent.RESERVED_ID_MAX + 1) {

			private static final long serialVersionUID = 1L;
		};

		EventListener[] listeners = examListeners.getListeners(ExamListener.class);
		for (EventListener listener : listeners) {
			((ExamListener) listener).examInserted(event);
		}
	}

	private void fireExamUpdated() {
		AWTEvent event = new AWTEvent(new Object(), AWTEvent.RESERVED_ID_MAX + 1) {

			private static final long serialVersionUID = 1L;
		};

		EventListener[] listeners = examListeners.getListeners(ExamListener.class);
		for (EventListener listener : listeners) {
			((ExamListener) listener).examUpdated(event);
		}
	}
    
	private JPanel jContentPane;
	private JPanel dataPanel;
	private JPanel buttonPanel;
	private JButton cancelButton;
	private JButton okButton;
	private VoLimitedTextField descriptionTextField;
	private VoLimitedTextField codeTextField;
	private JComboBox<String> procComboBox;
	private VoLimitedTextField defTextField;
	private JComboBox<ExamType> examTypeComboBox;
	private Exam exam;
	private boolean insert;
	
	private ExamBrowsingManager examBrowsingManager = Context.getApplicationContext().getBean(ExamBrowsingManager.class);
    
	/**
	 * This is the default constructor; we pass the arraylist and the selectedrow
     * because we need to update them
	 */
	public ExamEdit(JFrame owner, Exam old, boolean inserting) {
		super(owner, true);
		insert = inserting;
		exam = old;        // exam will be used for every operation
		initialize();
	}

	/**
	 * This method initializes this
	 */
	private void initialize() {
		
		Toolkit kit = Toolkit.getDefaultToolkit();
		Dimension screensize = kit.getScreenSize();
        final int pfrmBase = 20;
        final int pfrmWidth = 7;
        final int pfrmHeight = 8;
        this.setBounds((screensize.width - screensize.width * pfrmWidth / pfrmBase ) / 2, (screensize.height - screensize.height * pfrmHeight / pfrmBase)/2, 
                screensize.width * pfrmWidth / pfrmBase, screensize.height * pfrmHeight / pfrmBase);
		this.setContentPane(getJContentPane());
		if (insert) {
			this.setTitle(MessageBundle.getMessage("angal.exa.newexam.title"));
		} else {
			this.setTitle(MessageBundle.getMessage("angal.exa.editexam.title"));
		}
		this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
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
			jContentPane.add(getDataPanel(), BorderLayout.NORTH);
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
			JLabel typeLabel = new JLabel(MessageBundle.getMessage("angal.exa.type") + ':');
			JLabel descLabel = new JLabel(MessageBundle.getMessage("angal.common.description.txt") + ':');
			JLabel codeLabel = new JLabel(MessageBundle.getMessage("angal.common.code.txt") + ':');
			JLabel procLabel = new JLabel(MessageBundle.getMessage("angal.exa.procedure") + ':');
			JLabel defLabel = new JLabel(MessageBundle.getMessage("angal.exa.default") + ':');
			dataPanel = new JPanel(new SpringLayout());
			dataPanel.add(typeLabel);
			dataPanel.add(getExamTypeComboBox());
			dataPanel.add(codeLabel);
			dataPanel.add(getCodeTextField());
			dataPanel.add(descLabel);
			dataPanel.add(getDescriptionTextField());
			dataPanel.add(procLabel);
			dataPanel.add(getProcComboBox());
			dataPanel.add(defLabel);
			dataPanel.add(getDefTextField());
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
			okButton.addActionListener(actionEvent -> {
				if (codeTextField.getText().trim().equals("") || descriptionTextField.getText().trim().equals("")) {
					MessageDialog.error(null, "angal.exa.pleaseinsertcodeoranddescription");
				} else {
					int procedure = Integer.parseInt(procComboBox.getSelectedItem().toString());

					exam.setExamtype((ExamType) examTypeComboBox.getSelectedItem());
					exam.setDescription(descriptionTextField.getText());

					exam.setCode(codeTextField.getText().toUpperCase());
					exam.setDefaultResult(defTextField.getText().toUpperCase());
					exam.setProcedure(procedure);

					boolean inError = false;
					if (insert) {
						try {
							if (examBrowsingManager.isKeyPresent(exam)) {
								MessageDialog.error(this, "angal.exa.changethecodebecauseisalreadyinuse");
								return;
							}
						} catch (OHServiceException e1) {
							OHServiceExceptionUtil.showMessages(e1);
							inError = true;
						}
						try {
							examBrowsingManager.newExam(exam);
							fireExamInserted();
						} catch (OHServiceException e1) {
							OHServiceExceptionUtil.showMessages(e1);
							inError = true;
						}
					} else {
						try {
							examBrowsingManager.updateExam(exam);
							fireExamUpdated();
						} catch (OHServiceException e1) {
							OHServiceExceptionUtil.showMessages(e1);
							inError = true;
						}
					}
					if (inError) {
						MessageDialog.error(null, "angal.common.datacouldnotbesaved.msg");
					} else {
						dispose();
					}
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
	private JTextField getDescriptionTextField() {
		if (descriptionTextField == null) {
			//changed size from 50 to 100
			descriptionTextField = new VoLimitedTextField(100);
			if (!insert) {
			descriptionTextField.setText(exam.getDescription());
			}
		}
		return descriptionTextField;
	}
	
	private JTextField getDefTextField() {
		if (defTextField == null) {
				defTextField = new VoLimitedTextField(50);
				if (!insert) {
				defTextField.setText(exam.getDefaultResult());
			}
		}
		return defTextField;
	}
	
	private JTextField getCodeTextField() {
		if (codeTextField == null) {
                        codeTextField = new VoLimitedTextField(10);
                    if (!insert) {
                        codeTextField.setText(exam.getCode());
                        codeTextField.setEnabled(false);
                    }
		}
		return codeTextField;
	}

	private JComboBox<String> getProcComboBox() {
		if (procComboBox == null) {
			procComboBox = new JComboBox<>();
			if (insert) {
				procComboBox.addItem("1");
				procComboBox.addItem("2");
				procComboBox.addItem("3");
			} else {
				procComboBox.addItem(String.valueOf(exam.getProcedure()));
				procComboBox.setEnabled(false);
			}
		}
		return procComboBox;
	}
	
	/**
	 * This method initializes examTypeComboBox
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox<ExamType> getExamTypeComboBox() {
		if (examTypeComboBox == null) {
			examTypeComboBox = new JComboBox<>();
			try {
				List<ExamType> types = examBrowsingManager.getExamType();
				if (insert) {
					if (null != types) {
						for (ExamType elem : types) {
							examTypeComboBox.addItem(elem);
						}
					}
				} else {
					ExamType selectExamType = null;
					if (null != types) {
						for (ExamType elem : types) {
							examTypeComboBox.addItem(elem);
							if (exam.getExamtype().equals(elem)) {
								selectExamType = elem;
							}
						}
					}
					if (selectExamType != null) {
						examTypeComboBox.setSelectedItem(selectExamType);
					}
				}
			} catch (OHServiceException ohServiceException) {
				OHServiceExceptionUtil.showMessages(ohServiceException);
			}
		}
		return examTypeComboBox;
	}

}
