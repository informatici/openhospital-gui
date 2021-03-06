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
package org.isf.exa.gui;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.EventListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
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

/**
 * ------------------------------------------
 * ExamEdit - add/edit an exam
 * -----------------------------------------
 * modification history
 * 03/11/2006 - ross - Enlarged Description from 50 to 100
 *                   - removed toupper for the description
 * 			         - version is now 1.0
 * ------------------------------------------
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

			private static final long serialVersionUID = 1L;};

        EventListener[] listeners = examListeners.getListeners(ExamListener.class);
        for (int i = 0; i < listeners.length; i++)
            ((ExamListener)listeners[i]).examInserted(event);
    }
    private void fireExamUpdated() {
        AWTEvent event = new AWTEvent(new Object(), AWTEvent.RESERVED_ID_MAX + 1) {

			private static final long serialVersionUID = 1L;};

        EventListener[] listeners = examListeners.getListeners(ExamListener.class);
        for (int i = 0; i < listeners.length; i++)
            ((ExamListener)listeners[i]).examUpdated(event);
    }
    
	private JPanel jContentPane = null;
	private JPanel dataPanel = null;
	private JPanel buttonPanel = null;
	private JButton cancelButton = null;
	private JButton okButton = null;
	private JLabel descLabel = null;
	private JLabel codeLabel= null;
	private JLabel procLabel = null;
	private JLabel defLabel = null;
	private VoLimitedTextField descriptionTextField = null;
	private VoLimitedTextField codeTextField=null;
	private JComboBox procComboBox = null;
	private VoLimitedTextField defTextField = null;
	private JLabel typeLabel = null;
	private JComboBox typeComboBox = null;
	private Exam exam = null;
	private boolean insert = false;
	
	private ExamBrowsingManager manager = Context.getApplicationContext().getBean(ExamBrowsingManager.class);
    
	/**
	 * This is the default constructor; we pass the arraylist and the selectedrow
     * because we need to update them
	 */
	public ExamEdit(JFrame owner,Exam old,boolean inserting) {
		super(owner,true);
		insert = inserting;
		exam = old;		//medical will be used for every operation
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
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
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
                jContentPane.add(getDataPanel(), java.awt.BorderLayout.NORTH);  // Generated
                jContentPane.add(getButtonPanel(), java.awt.BorderLayout.SOUTH);  // Generated
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
			typeLabel = new JLabel(MessageBundle.getMessage("angal.exa.type"));
			descLabel = new JLabel(MessageBundle.getMessage("angal.common.description.txt"));
			codeLabel = new JLabel(MessageBundle.getMessage("angal.common.code.txt"));
			procLabel = new JLabel(MessageBundle.getMessage("angal.exa.procedure"));
			defLabel = new JLabel(MessageBundle.getMessage("angal.exa.default"));
			dataPanel = new JPanel();
			dataPanel.setLayout(new BoxLayout(getDataPanel(), BoxLayout.Y_AXIS));  // Generated
			dataPanel.add(typeLabel, null);
			dataPanel.add(getTypeComboBox(), null);  
			dataPanel.add(codeLabel,null);
			dataPanel.add(getCodeTextField(),null);
			dataPanel.add(descLabel, null);  
			dataPanel.add(getDescriptionTextField(), null);
			dataPanel.add(procLabel, null);  
			dataPanel.add(getProcComboBox(), null);
			dataPanel.add(defLabel, null);  
			dataPanel.add(getDefTextField(), null);
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
			buttonPanel.add(getOkButton(), null);  // Generated
			buttonPanel.add(getCancelButton(), null);  // Generated
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
			if (okButton == null) {
				okButton = new JButton(MessageBundle.getMessage("angal.common.ok.btn"));
				okButton.setMnemonic(MessageBundle.getMnemonic("angal.common.ok.btn.key"));
				okButton.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent e) {
						if ((codeTextField.getText().trim().equals(""))||(descriptionTextField.getText().trim().equals(""))){
							MessageDialog.error(null, "angal.exa.pleaseinsertcodeoranddescription");
						}
						else{
							int procedure = Integer.parseInt(procComboBox.getSelectedItem().toString());
							
							exam.setExamtype((ExamType)typeComboBox.getSelectedItem());
							exam.setDescription(descriptionTextField.getText());
							
							exam.setCode(codeTextField.getText().toUpperCase());
							exam.setDefaultResult(defTextField.getText().toUpperCase());
							exam.setProcedure(procedure);
							
							boolean result = false;
							if (insert) {
								try {
									if (manager.isKeyPresent(exam)) {
										MessageDialog.error(ExamEdit.this, "angal.exa.changethecodebecauseisalreadyinuse");
										return;
									}
								} catch (OHServiceException e1) {
									OHServiceExceptionUtil.showMessages(e1);
								}
								try {
									result = manager.newExam(exam);
									if (result) fireExamInserted();
								} catch (OHServiceException e1) {
									OHServiceExceptionUtil.showMessages(e1);
								}
							} else {
								try {
									result = manager.updateExam(exam);
									if (result) fireExamUpdated();
								} catch (OHServiceException e1) {
									OHServiceExceptionUtil.showMessages(e1);
								}
							}
							if (!result) {
								MessageDialog.error(null, "angal.common.datacouldnotbesaved.msg");
							}
							else  {
								dispose();
							}
						}
					}
				});
			}
			return okButton;
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
	
	private JComboBox getProcComboBox(){
		if (procComboBox==null){
			procComboBox = new JComboBox();
			if (insert) {
				procComboBox.addItem("1");
				procComboBox.addItem("2");
				procComboBox.addItem("3");
			} else {
				procComboBox.addItem(exam.getProcedure());
				procComboBox.setEnabled(false);
			}
		}
		return procComboBox;
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
				ArrayList<ExamType> types;
				try {
					types = manager.getExamType();
				} catch (OHServiceException e) {
					types = null;
					OHServiceExceptionUtil.showMessages(e);
				}
				if (null != types) {
					for (ExamType elem : types) {
						typeComboBox.addItem(elem);
					}
				}
			} else {
				typeComboBox.addItem(exam.getExamtype());
				typeComboBox.setEnabled(false);
			}
			
		}
		return typeComboBox;
	}

}
