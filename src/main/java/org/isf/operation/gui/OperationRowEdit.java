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
package org.isf.operation.gui;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import org.isf.generaldata.GeneralData;
import org.isf.generaldata.MessageBundle;
import org.isf.menu.gui.MainMenu;
import org.isf.menu.manager.Context;
import org.isf.opd.model.Opd;
import org.isf.operation.manager.OperationBrowserManager;
import org.isf.operation.manager.OperationRowBrowserManager;
import org.isf.operation.model.Operation;
import org.isf.operation.model.OperationRow;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.CustomJDateChooser;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.VoFloatTextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OperationRowEdit extends JPanel {

	private static final Logger LOGGER = LoggerFactory.getLogger(OperationRowEdit.class);

	     // LISTENER INTERFACE
		// --------------------------------------------------------
			List<OperationList> operationRowListener = new ArrayList<>();

			public interface OperationRowListener extends EventListener {
				void operationRowInserted(AWTEvent aEvent);
			}

			public interface OperationRowEditListener extends EventListener{
				void operationRowEdited(AWTEvent event);
			}
			
			public void addOperationRowListener(OperationList l) {
				operationRowListener.add(l);
			}
			
			public void addOperationListener(OperationList l) {
				operationRowListener.add(l);
			}

			private void fireOperationRowInserted(OperationRow opeRow) {
				AWTEvent event = new AWTEvent(opeRow, AWTEvent.RESERVED_ID_MAX + 1) {
					private static final long serialVersionUID = 1L;
				};
				for (Iterator<OperationList> iterator = operationRowListener.iterator(); iterator.hasNext();){
					OperationList opelist = (OperationList) iterator.next();
						opelist.operationRowInserted(event);
				}
			}
			
			private void fireOperationRowUpdated(OperationRow opeRow) {
				AWTEvent event = new AWTEvent(opeRow, AWTEvent.RESERVED_ID_MAX + 1) {
					private static final long serialVersionUID = 1L;
				};
                for (Iterator<OperationList> iterator = operationRowListener.iterator(); iterator.hasNext();){
					OperationList opelist = (OperationList) iterator.next();
						opelist.operationRowEdited(event);
				}
			}
			
			
	
	
	//private JTextField TransTextField;
	private VoFloatTextField TransTextField;
	
	private JTextArea remarkstextArea ;
	private JPanel panelButtons;
	private JLabel lblTransUnite;
	private JComboBox resultComboBox;
	private JLabel lblResultat;
	private JLabel lblDate;
	private JComboBox OpecomboBox;
	private JLabel lblOperation;
	
	private CustomJDateChooser jCalendarDate;
	private GregorianCalendar date ;
	
	private OperationRow opeRow;
	private JTextField DateTextField;
	private JButton btnCancelButton;
	private JDialog myParent;
	OperationBrowserManager ope ;
	OperationRowBrowserManager opeManageRow;
	private Opd myOpd;
	private JSeparator separator;
	private JSeparator separator_1;
	private JLabel lblNewLabel;
	private JLabel titleLabel;
	
	private ArrayList<String> operationResults = ope.getResultDescriptionList();

	
	public OperationRowEdit(OperationRow opRow) {
		
		opeRow = opRow;
		ope = Context.getApplicationContext().getBean(OperationBrowserManager.class);
		opeManageRow = Context.getApplicationContext().getBean(OperationRowBrowserManager.class);
		operationResults = ope.getResultDescriptionList();
		setLayout(new BorderLayout(0, 0));
		
		JPanel panelHeader = new JPanel();
		panelHeader.setBorder(new EmptyBorder(7, 0, 0, 0));
		add(panelHeader, BorderLayout.NORTH);
		GridBagLayout gbl_panelHeader = new GridBagLayout();
		gbl_panelHeader.columnWidths = new int[]{55, 267, 0};
		gbl_panelHeader.rowHeights = new int[]{14, 0};
		gbl_panelHeader.columnWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		gbl_panelHeader.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		panelHeader.setLayout(gbl_panelHeader);
		
		titleLabel = new JLabel(MessageBundle.getMessage("angal.operationrowlist.add") + "/" +  //$NON-NLS-1$ //$NON-NLS-2$
                        MessageBundle.getMessage("angal.operationrowlist.update")); //$NON-NLS-1$
		titleLabel.setFont(new Font("Tahoma", Font.PLAIN, 15)); //$NON-NLS-1$
		GridBagConstraints gbc_titleLabel = new GridBagConstraints();
		gbc_titleLabel.anchor = GridBagConstraints.NORTH;
		gbc_titleLabel.fill = GridBagConstraints.HORIZONTAL;
		gbc_titleLabel.gridx = 1;
		gbc_titleLabel.gridy = 0;
		panelHeader.add(titleLabel, gbc_titleLabel);
		
		JPanel panelBody = new JPanel();
		panelBody.setBorder(new EmptyBorder(3, 15, 3, 15));
		panelBody.setMaximumSize(new Dimension(32767, 30000));
		add(panelBody, BorderLayout.CENTER);
		GridBagLayout gbl_panelBody = new GridBagLayout();
		gbl_panelBody.columnWidths = new int[]{0, 0, 0, 0, 0, 0};
		gbl_panelBody.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0};
		gbl_panelBody.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		gbl_panelBody.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
		panelBody.setLayout(gbl_panelBody);
		
		separator = new JSeparator();
		separator.setBackground(Color.GRAY);
		GridBagConstraints gbc_separator = new GridBagConstraints();
		gbc_separator.gridwidth = 5;
		gbc_separator.fill = GridBagConstraints.HORIZONTAL;
		gbc_separator.insets = new Insets(0, 0, 5, 0);
		gbc_separator.gridx = 0;
		gbc_separator.gridy = 0;
		panelBody.add(separator, gbc_separator);
		
		lblNewLabel = new JLabel("    "); //$NON-NLS-1$
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.gridx = 1;
		gbc_lblNewLabel.gridy = 1;
		panelBody.add(lblNewLabel, gbc_lblNewLabel);
		
		lblOperation = new JLabel(MessageBundle.getMessage("angal.operationrowedit.operation")); //$NON-NLS-1$
		lblOperation.setBorder(new EmptyBorder(0, 0, 0, 4));
		lblOperation.setHorizontalAlignment(SwingConstants.LEFT);
		GridBagConstraints gbc_lblOperation = new GridBagConstraints();
		gbc_lblOperation.insets = new Insets(0, 0, 5, 5);
		gbc_lblOperation.gridx = 1;
		gbc_lblOperation.gridy = 2;
		panelBody.add(lblOperation, gbc_lblOperation);
		
		//OpecomboBox = new JComboBox();
		OpecomboBox = getOperationsBox();
		GridBagConstraints gbc_OpecomboBox = new GridBagConstraints();
		gbc_OpecomboBox.insets = new Insets(0, 0, 5, 0);
		gbc_OpecomboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_OpecomboBox.gridx = 4;
		gbc_OpecomboBox.gridy = 2;
		panelBody.add(OpecomboBox, gbc_OpecomboBox);
		
		DateTextField = new JTextField();
		
		
		lblDate = new JLabel(MessageBundle.getMessage("angal.operationrowlist.date")); //$NON-NLS-1$
		lblDate.setBorder(new EmptyBorder(0, 0, 0, 4));
		lblDate.setHorizontalAlignment(SwingConstants.LEFT);
		GridBagConstraints gbc_lblDate = new GridBagConstraints();
		gbc_lblDate.insets = new Insets(0, 0, 5, 5);
		gbc_lblDate.gridx = 1;
		gbc_lblDate.gridy = 3;
		panelBody.add(lblDate, gbc_lblDate);
		GridBagConstraints gbc_DateTextField = new GridBagConstraints();
		gbc_DateTextField.insets = new Insets(0, 0, 5, 0);
		gbc_DateTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_DateTextField.gridx = 4;
		gbc_DateTextField.gridy = 3;
		panelBody.add(this.getJCalendarDate(), gbc_DateTextField);
		//panelBody.add(DateTextField, gbc_DateTextField);
		DateTextField.setColumns(10);
		
		resultComboBox = getComboResultBox();
		
		lblResultat = new JLabel(MessageBundle.getMessage("angal.operationrowedit.result")); //$NON-NLS-1$
		lblResultat.setBorder(new EmptyBorder(0, 0, 0, 4));
		lblResultat.setHorizontalAlignment(SwingConstants.LEFT);
		GridBagConstraints gbc_lblResultat = new GridBagConstraints();
		gbc_lblResultat.insets = new Insets(0, 0, 5, 5);
		gbc_lblResultat.gridx = 1;
		gbc_lblResultat.gridy = 4;
		panelBody.add(lblResultat, gbc_lblResultat);
		
		GridBagConstraints gbc_ResultatcomboBox = new GridBagConstraints();
		gbc_ResultatcomboBox.insets = new Insets(0, 0, 5, 0);
		gbc_ResultatcomboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_ResultatcomboBox.gridx = 4;
		gbc_ResultatcomboBox.gridy = 4;
		panelBody.add(resultComboBox, gbc_ResultatcomboBox);
		
		lblTransUnite = new JLabel(MessageBundle.getMessage("angal.operationrowedit.unitetrans")); //$NON-NLS-1$
		lblTransUnite.setBorder(new EmptyBorder(0, 0, 0, 4));
		lblTransUnite.setHorizontalAlignment(SwingConstants.LEFT);
		GridBagConstraints gbc_lblTransUnite = new GridBagConstraints();
		gbc_lblTransUnite.insets = new Insets(0, 0, 5, 5);
		gbc_lblTransUnite.gridx = 1;
		gbc_lblTransUnite.gridy = 5;
		panelBody.add(lblTransUnite, gbc_lblTransUnite);
		
		//TransTextField = new JTextField();
		TransTextField = new VoFloatTextField(0,100);
		GridBagConstraints gbc_TransTextField = new GridBagConstraints();
		gbc_TransTextField.insets = new Insets(0, 0, 5, 0);
		gbc_TransTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_TransTextField.gridx = 4;
		gbc_TransTextField.gridy = 5;
		panelBody.add(TransTextField, gbc_TransTextField);
		TransTextField.setColumns(10);
		
		remarkstextArea = new JTextArea();
		remarkstextArea.setTabSize(5);
		if (this.opeRow!=null){
			remarkstextArea.setText(opeRow.getRemarks());
		}
		
		JLabel remarksLabel = new JLabel(MessageBundle.getMessage("angal.operationrowedit.remark")); //$NON-NLS-1$
		remarksLabel.setVerticalAlignment(SwingConstants.TOP);
		remarksLabel.setHorizontalAlignment(SwingConstants.LEFT);
		GridBagConstraints gbc_remarksLabel = new GridBagConstraints();
		gbc_remarksLabel.insets = new Insets(0, 0, 5, 5);
		gbc_remarksLabel.gridx = 1;
		gbc_remarksLabel.gridy = 6;
		panelBody.add(remarksLabel, gbc_remarksLabel);
		GridBagConstraints gbc_remarkstextArea = new GridBagConstraints();
		gbc_remarkstextArea.insets = new Insets(0, 0, 5, 0);
		gbc_remarkstextArea.fill = GridBagConstraints.BOTH;
		gbc_remarkstextArea.gridx = 4;
		gbc_remarkstextArea.gridy = 6;
		panelBody.add(remarkstextArea, gbc_remarkstextArea);
		
		separator_1 = new JSeparator();
		separator_1.setBackground(Color.GRAY);
		GridBagConstraints gbc_separator_1 = new GridBagConstraints();
		gbc_separator_1.fill = GridBagConstraints.HORIZONTAL;
		gbc_separator_1.insets = new Insets(0, 0, 0, 5);
		gbc_separator_1.gridx = 0;
		gbc_separator_1.gridy = 7;
		panelBody.add(separator_1, gbc_separator_1);
		
		if (this.opeRow != null){
			TransTextField.setText(opeRow.getTransUnit()+""); //$NON-NLS-1$
		}
		
		panelButtons = new JPanel();
		add(panelButtons, BorderLayout.SOUTH);
		panelButtons.setLayout(new FlowLayout(FlowLayout.RIGHT, 5, 5));
		
		JButton btnSaveButton = new JButton(MessageBundle.getMessage("angal.operationrowedit.save")); //$NON-NLS-1$
		btnSaveButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent evt) {
				saveButtonMouseClicked(evt) ;
			}
		});
		panelButtons.add(btnSaveButton);

		btnCancelButton = new JButton(MessageBundle.getMessage("angal.common.cancel.btn"));
		btnCancelButton.setMnemonic(MessageBundle.getMnemonic("angal.common.cancel.btn.key"));
		btnCancelButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent evt) {
				cancelButtonMouseClicked(evt);
			}
		});
		panelButtons.add(btnCancelButton);
	}

	public OperationRow getOpeRow() {
		return opeRow;
	}

	public void setOpeRow(OperationRow opeRow) {
		this.opeRow = opeRow;
	}
	
	public JDialog getMyParent() {
		return myParent;
	}

	public void setMyParent(JDialog myParent) {
		this.myParent = myParent;
	}
	
	private CustomJDateChooser getJCalendarDate() {
		if (jCalendarDate == null) {
			jCalendarDate = new CustomJDateChooser();
			jCalendarDate.setLocale(new Locale(GeneralData.LANGUAGE));
			jCalendarDate.setDateFormatString("dd/MM/yy"); //$NON-NLS-1$
			if (opeRow !=null ){
				jCalendarDate.setDate(this.opeRow.getOpDate().getTime());
			}
		}			
		return jCalendarDate;
	}

	private JComboBox getOperationsBox() {

		JComboBox comboOpe = new JComboBox();
		ArrayList<Operation> opeList = new ArrayList<>();
		try {
			opeList = ope.getOperation();
		} catch (OHServiceException ohServiceException) {
			LOGGER.error(ohServiceException.getMessage(), ohServiceException);
		}
		if (opeRow != null) {
			boolean found = false;
			for (org.isf.operation.model.Operation elem : opeList) {
				if (opeRow.getOperation().getCode().equals(elem.getCode())) {
					found = true;
					comboOpe.addItem(elem);
					break;
				}
			}
			if (!found) {
				//comboOpe.addItem("");
				comboOpe.addItem(null);
			}
			for (org.isf.operation.model.Operation elem : opeList) {
				comboOpe.addItem(elem);
			}
		} else {
			//comboOpe.addItem("");
			comboOpe.addItem(null);
			for (org.isf.operation.model.Operation elem : opeList) {
				comboOpe.addItem(elem);
			}
		}
		comboOpe.setEnabled(true);
		return comboOpe;
	}

	private JComboBox getComboResultBox() {
		JComboBox comboResult = new JComboBox();
			for (String description : operationResults) {
				comboResult.addItem(description);
			}
			if (opeRow != null){
				boolean found = false;
				for (String elem : operationResults) {
					if (opeRow.getOpResult().equals(ope.getResultDescriptionKey(elem))) {
						found = true;
						comboResult.addItem(elem);
						break;
					}				
				}
				if (!found){
					//comboOpe.addItem("");
					comboResult.addItem(null);
				}
				for (String elem : operationResults) {
					
					comboResult.addItem(elem);
				}
			}
			
		return comboResult;
	}
	
	/* **************  functions events ***** */
	private void saveButtonMouseClicked(java.awt.event.MouseEvent evt) {
	      if ((this.jCalendarDate.getDate()==null) || (this.OpecomboBox.getSelectedItem()==null)) {
		      MessageDialog.error(OperationRowEdit.this, "angal.operationrowedit.warningdateope");
	      } else {
	    	  if (getMyOpd().getDate().after(this.jCalendarDate.getDate())){
			      MessageDialog.error(OperationRowEdit.this, "angal.operationrowedit.warningdateafter");
	    		  return;
	    	  }
			if (opeRow!=null){
	        	OperationRow updateOpeRow = opeRow;
	        	GregorianCalendar dateop = new GregorianCalendar();
				dateop.setTime(jCalendarDate.getDate());
				updateOpeRow.setOpDate(dateop);
				
				String opResult = ope.getResultDescriptionKey((String) this.resultComboBox.getSelectedItem());
				updateOpeRow.setOpResult(opResult);
				
				updateOpeRow.setTransUnit(Float.parseFloat(TransTextField.getText()));
	        	Operation op = (Operation)OpecomboBox.getSelectedItem();
	        	updateOpeRow.setOperation(op);
	        	updateOpeRow.setRemarks(remarkstextArea.getText());
	        	boolean result = false;
				try {
					result = opeManageRow.updateOperationRow(updateOpeRow);
				} catch (OHServiceException e) {
					OHServiceExceptionUtil.showMessages(e);
					return;
				}
	        	if (result){
			        MessageDialog.info(OperationRowEdit.this, "angal.operationrowedit.updatesucces");
	        		fireOperationRowUpdated(updateOpeRow);
	        		this.myParent.dispose();
	        	}
	        	else {
			        MessageDialog.error(OperationRowEdit.this, "angal.operationrowedit.updateerror");
	        	}
	        }
	        else {
	        	OperationRow operationRow = new OperationRow();
	        	GregorianCalendar dateop = new GregorianCalendar();
				dateop.setTime(this.jCalendarDate.getDate());
				operationRow.setOpDate(dateop);
				
				String opResult = ope.getResultDescriptionKey((String) this.resultComboBox.getSelectedItem());
				operationRow.setOpResult(opResult);

				operationRow.setTransUnit(Float.parseFloat(this.TransTextField.getText()));
	        	Operation op = (Operation)this.OpecomboBox.getSelectedItem();
	        	operationRow.setOperation(op);
	        	operationRow.setOpd(this.getMyOpd());	       
	        	operationRow.setPrescriber(MainMenu.getUser().getUserName());
	        	operationRow.setRemarks(remarkstextArea.getText());
	        	boolean result = false;
				try {
					result = opeManageRow.newOperationRow(operationRow);
				} catch (OHServiceException e) {
					OHServiceExceptionUtil.showMessages(e);
					return;
				}
	        	if (result){
			        MessageDialog.info(OperationRowEdit.this, "angal.operationrowedit.savesucces");
	        		fireOperationRowInserted(operationRow);
	        		this.myParent.dispose();
	        	}
	        	else {
			        MessageDialog.error(OperationRowEdit.this, "angal.operationrowedit.saveerror");
	        	}	
	        }
	      }
    }
	
	private void cancelButtonMouseClicked(java.awt.event.MouseEvent evt) {
        this.getMyParent().dispose();
    }

	public Opd getMyOpd() {
		return myOpd;
	}

	public void setMyOpd(Opd myOpd) {
		this.myOpd = myOpd;
	}

	public JLabel getTitleLabel() {
		return titleLabel;
	}

	public void setTitleLabel(JLabel titleLabel) {
		this.titleLabel = titleLabel;
	}
	
}
