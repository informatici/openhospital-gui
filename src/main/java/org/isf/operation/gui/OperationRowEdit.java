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
package org.isf.operation.gui;

import static org.isf.utils.Constants.DATE_FORMAT_DD_MM_YY;

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
import java.util.List;
import java.util.Locale;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
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

	public interface OperationRowEditListener extends EventListener {

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
		for (OperationList opelist : operationRowListener) {
			opelist.operationRowInserted(event);
		}
	}

	private void fireOperationRowUpdated(OperationRow opeRow) {
		AWTEvent event = new AWTEvent(opeRow, AWTEvent.RESERVED_ID_MAX + 1) {

			private static final long serialVersionUID = 1L;
		};
		for (OperationList opelist : operationRowListener) {
			opelist.operationRowEdited(event);
		}
	}

	private VoFloatTextField transTextField;

	private JTextArea remarksTextArea;
	private JPanel panelButtons;
	private JLabel lblTransUnite;
	private JComboBox<String> resultComboBox;
	private JLabel lblResultat;
	private JLabel lblDate;
	private JComboBox<Operation> opeComboBox;
	private JLabel lblOperation;

	private CustomJDateChooser jCalendarDate;

	private OperationRow opeRow;
	private JButton btnCancel;
	private JDialog myParent;
	OperationBrowserManager ope;
	OperationRowBrowserManager opeManageRow;
	private Opd myOpd;
	private JSeparator separator;
	private JSeparator separator1;
	private JLabel lblNewLabel;
	private JLabel titleLabel;

	private List<String> operationResults;

	
	public OperationRowEdit(OperationRow opRow) {
		opeRow = opRow;
		ope = Context.getApplicationContext().getBean(OperationBrowserManager.class);
		opeManageRow = Context.getApplicationContext().getBean(OperationRowBrowserManager.class);
		operationResults = ope.getResultDescriptionList();
		setLayout(new BorderLayout(0, 0));
		
		JPanel panelHeader = new JPanel();
		panelHeader.setBorder(new EmptyBorder(7, 0, 0, 0));
		add(panelHeader, BorderLayout.NORTH);
		GridBagLayout gblPanelHeader = new GridBagLayout();
		gblPanelHeader.columnWidths = new int[]{55, 267, 0};
		gblPanelHeader.rowHeights = new int[]{14, 0};
		gblPanelHeader.columnWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		gblPanelHeader.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		panelHeader.setLayout(gblPanelHeader);
		
		titleLabel = new JLabel(MessageBundle.getMessage("angal.operationrowlist.addupdate"));
		titleLabel.setFont(new Font("Tahoma", Font.PLAIN, 15)); //$NON-NLS-1$
		GridBagConstraints gbcTitleLabel = new GridBagConstraints();
		gbcTitleLabel.anchor = GridBagConstraints.NORTH;
		gbcTitleLabel.fill = GridBagConstraints.HORIZONTAL;
		gbcTitleLabel.gridx = 1;
		gbcTitleLabel.gridy = 0;
		panelHeader.add(titleLabel, gbcTitleLabel);
		
		JPanel panelBody = new JPanel();
		panelBody.setBorder(new EmptyBorder(3, 15, 3, 15));
		panelBody.setMaximumSize(new Dimension(32767, 30000));
		add(panelBody, BorderLayout.CENTER);
		GridBagLayout gblPanelBody = new GridBagLayout();
		gblPanelBody.columnWidths = new int[]{0, 0, 0, 0, 0, 0};
		gblPanelBody.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0};
		gblPanelBody.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		gblPanelBody.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
		panelBody.setLayout(gblPanelBody);
		
		separator = new JSeparator();
		separator.setBackground(Color.GRAY);
		GridBagConstraints gbcSeparator = new GridBagConstraints();
		gbcSeparator.gridwidth = 5;
		gbcSeparator.fill = GridBagConstraints.HORIZONTAL;
		gbcSeparator.insets = new Insets(0, 0, 5, 0);
		gbcSeparator.gridx = 0;
		gbcSeparator.gridy = 0;
		panelBody.add(separator, gbcSeparator);
		
		lblNewLabel = new JLabel("    "); //$NON-NLS-1$
		GridBagConstraints gbcLblNewLabel = new GridBagConstraints();
		gbcLblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbcLblNewLabel.gridx = 1;
		gbcLblNewLabel.gridy = 1;
		panelBody.add(lblNewLabel, gbcLblNewLabel);
		
		lblOperation = new JLabel(MessageBundle.getMessage("angal.operationrowedit.operation")); //$NON-NLS-1$
		lblOperation.setBorder(new EmptyBorder(0, 0, 0, 4));
		lblOperation.setHorizontalAlignment(SwingConstants.LEFT);
		GridBagConstraints gbcLblOperation = new GridBagConstraints();
		gbcLblOperation.insets = new Insets(0, 0, 5, 5);
		gbcLblOperation.gridx = 1;
		gbcLblOperation.gridy = 2;
		panelBody.add(lblOperation, gbcLblOperation);
		
		opeComboBox = getOperationsBox();
		GridBagConstraints gbcOpecomboBox = new GridBagConstraints();
		gbcOpecomboBox.insets = new Insets(0, 0, 5, 0);
		gbcOpecomboBox.fill = GridBagConstraints.HORIZONTAL;
		gbcOpecomboBox.gridx = 4;
		gbcOpecomboBox.gridy = 2;
		panelBody.add(opeComboBox, gbcOpecomboBox);
		
		lblDate = new JLabel(MessageBundle.getMessage("angal.operationrowlist.date")); //$NON-NLS-1$
		lblDate.setBorder(new EmptyBorder(0, 0, 0, 4));
		lblDate.setHorizontalAlignment(SwingConstants.LEFT);
		GridBagConstraints gbcLblDate = new GridBagConstraints();
		gbcLblDate.insets = new Insets(0, 0, 5, 5);
		gbcLblDate.gridx = 1;
		gbcLblDate.gridy = 3;
		panelBody.add(lblDate, gbcLblDate);
		GridBagConstraints gbcCalendarDate = new GridBagConstraints();
		gbcCalendarDate.insets = new Insets(0, 0, 5, 0);
		gbcCalendarDate.fill = GridBagConstraints.HORIZONTAL;
		gbcCalendarDate.gridx = 4;
		gbcCalendarDate.gridy = 3;
		panelBody.add(this.getJCalendarDate(), gbcCalendarDate);
		
		resultComboBox = getComboResultBox();
		
		lblResultat = new JLabel(MessageBundle.getMessage("angal.common.result.txt"));
		lblResultat.setBorder(new EmptyBorder(0, 0, 0, 4));
		lblResultat.setHorizontalAlignment(SwingConstants.LEFT);
		GridBagConstraints gbcLblResultat = new GridBagConstraints();
		gbcLblResultat.insets = new Insets(0, 0, 5, 5);
		gbcLblResultat.gridx = 1;
		gbcLblResultat.gridy = 4;
		panelBody.add(lblResultat, gbcLblResultat);
		
		GridBagConstraints gbcResultatComboBox = new GridBagConstraints();
		gbcResultatComboBox.insets = new Insets(0, 0, 5, 0);
		gbcResultatComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbcResultatComboBox.gridx = 4;
		gbcResultatComboBox.gridy = 4;
		panelBody.add(resultComboBox, gbcResultatComboBox);
		
		lblTransUnite = new JLabel(MessageBundle.getMessage("angal.operationrowedit.unitetrans")); //$NON-NLS-1$
		lblTransUnite.setBorder(new EmptyBorder(0, 0, 0, 4));
		lblTransUnite.setHorizontalAlignment(SwingConstants.LEFT);
		GridBagConstraints gbcLblTransUnite = new GridBagConstraints();
		gbcLblTransUnite.insets = new Insets(0, 0, 5, 5);
		gbcLblTransUnite.gridx = 1;
		gbcLblTransUnite.gridy = 5;
		panelBody.add(lblTransUnite, gbcLblTransUnite);

		transTextField = new VoFloatTextField(0, 100);
		GridBagConstraints gbcTransTextField = new GridBagConstraints();
		gbcTransTextField.insets = new Insets(0, 0, 5, 0);
		gbcTransTextField.fill = GridBagConstraints.HORIZONTAL;
		gbcTransTextField.gridx = 4;
		gbcTransTextField.gridy = 5;
		panelBody.add(transTextField, gbcTransTextField);
		transTextField.setColumns(10);

		remarksTextArea = new JTextArea();
		remarksTextArea.setTabSize(5);
		if (this.opeRow != null) {
			remarksTextArea.setText(opeRow.getRemarks());
		}
		
		JLabel remarksLabel = new JLabel(MessageBundle.getMessage("angal.operationrowedit.remark")); //$NON-NLS-1$
		remarksLabel.setVerticalAlignment(SwingConstants.TOP);
		remarksLabel.setHorizontalAlignment(SwingConstants.LEFT);
		GridBagConstraints gbcRemarksLabel = new GridBagConstraints();
		gbcRemarksLabel.insets = new Insets(0, 0, 5, 5);
		gbcRemarksLabel.gridx = 1;
		gbcRemarksLabel.gridy = 6;
		panelBody.add(remarksLabel, gbcRemarksLabel);
		GridBagConstraints gbcRemarksTextArea = new GridBagConstraints();
		gbcRemarksTextArea.insets = new Insets(0, 0, 5, 0);
		gbcRemarksTextArea.fill = GridBagConstraints.BOTH;
		gbcRemarksTextArea.gridx = 4;
		gbcRemarksTextArea.gridy = 6;
		panelBody.add(remarksTextArea, gbcRemarksTextArea);
		
		separator1 = new JSeparator();
		separator1.setBackground(Color.GRAY);
		GridBagConstraints gbcSeparator1 = new GridBagConstraints();
		gbcSeparator1.fill = GridBagConstraints.HORIZONTAL;
		gbcSeparator1.insets = new Insets(0, 0, 0, 5);
		gbcSeparator1.gridx = 0;
		gbcSeparator1.gridy = 7;
		panelBody.add(separator1, gbcSeparator1);

		if (this.opeRow != null) {
			transTextField.setText(opeRow.getTransUnit() + "");
		}
		
		panelButtons = new JPanel();
		add(panelButtons, BorderLayout.SOUTH);
		panelButtons.setLayout(new FlowLayout(FlowLayout.RIGHT, 5, 5));
		
		JButton btnSave = new JButton(MessageBundle.getMessage("angal.common.save.btn"));
		btnSave.setMnemonic(MessageBundle.getMnemonic("angal.common.save.btn.key"));
		btnSave.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent evt) {
				saveButtonMouseClicked(evt) ;
			}
		});
		panelButtons.add(btnSave);

		btnCancel = new JButton(MessageBundle.getMessage("angal.common.cancel.btn"));
		btnCancel.setMnemonic(MessageBundle.getMnemonic("angal.common.cancel.btn.key"));
		btnCancel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent evt) {
				cancelButtonMouseClicked(evt);
			}
		});
		panelButtons.add(btnCancel);
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
			jCalendarDate.setDateFormatString(DATE_FORMAT_DD_MM_YY);
			if (opeRow != null) {
				jCalendarDate.setDate(this.opeRow.getOpDate());
			}
		}
		return jCalendarDate;
	}

	private JComboBox<Operation> getOperationsBox() {
		JComboBox<Operation> comboOpe = new JComboBox<>();
		List<Operation> opeList = new ArrayList<>();
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
				comboOpe.addItem(null);
			}
			for (org.isf.operation.model.Operation elem : opeList) {
				comboOpe.addItem(elem);
			}
		} else {
			comboOpe.addItem(null);
			for (org.isf.operation.model.Operation elem : opeList) {
				comboOpe.addItem(elem);
			}
		}
		comboOpe.setEnabled(true);
		return comboOpe;
	}

	private JComboBox<String> getComboResultBox() {
		JComboBox<String> comboResult = new JComboBox<>();
		for (String description : operationResults) {
			comboResult.addItem(description);
		}
		if (opeRow != null) {
			boolean found = false;
			for (String elem : operationResults) {
				if (opeRow.getOpResult().equals(ope.getResultDescriptionKey(elem))) {
					found = true;
					comboResult.addItem(elem);
					break;
				}
			}
			if (!found) {
				comboResult.addItem(null);
			}
			for (String elem : operationResults) {
				comboResult.addItem(elem);
			}
		}
		return comboResult;
	}
	
	/* **************  functions events ***** */
	private void saveButtonMouseClicked(MouseEvent mouseEvent) {
		if ((this.jCalendarDate.getDate() == null) || (this.opeComboBox.getSelectedItem() == null)) {
			MessageDialog.error(OperationRowEdit.this, "angal.operationrowedit.warningdateope");
		} else {
			if (getMyOpd().getDate().isAfter(this.jCalendarDate.getLocalDateTime())) {
				MessageDialog.error(OperationRowEdit.this, "angal.operationrowedit.warningdateafter");
				return;
			}
			if (opeRow != null) {
				OperationRow updateOpeRow = opeRow;
				updateOpeRow.setOpDate(jCalendarDate.getLocalDateTime());
				updateOpeRow.setOpResult(resultComboBox.getSelectedItem().toString());
				updateOpeRow.setTransUnit(Float.parseFloat(transTextField.getText()));
				Operation op = (Operation) opeComboBox.getSelectedItem();
				updateOpeRow.setOperation(op);
				updateOpeRow.setRemarks(remarksTextArea.getText());
				boolean result;
				try {
					result = opeManageRow.updateOperationRow(updateOpeRow);
				} catch (OHServiceException e) {
					OHServiceExceptionUtil.showMessages(e);
					return;
				}
				if (result) {
					MessageDialog.info(OperationRowEdit.this, "angal.operationrowedit.updatesucces");
					fireOperationRowUpdated(updateOpeRow);
					this.myParent.dispose();
				} else {
					MessageDialog.error(OperationRowEdit.this, "angal.operationrowedit.updateerror");
				}
			} else {
				OperationRow operationRow = new OperationRow();
				operationRow.setOpDate(jCalendarDate.getLocalDateTime());
				operationRow.setOpResult(this.resultComboBox.getSelectedItem().toString());
				operationRow.setTransUnit(Float.parseFloat(this.transTextField.getText()));
				Operation op = (Operation) this.opeComboBox.getSelectedItem();
				operationRow.setOperation(op);
				operationRow.setOpd(this.getMyOpd());
				operationRow.setPrescriber(MainMenu.getUser().getUserName());
				operationRow.setRemarks(remarksTextArea.getText());
				boolean result;
				try {
					result = opeManageRow.newOperationRow(operationRow);
				} catch (OHServiceException e) {
					OHServiceExceptionUtil.showMessages(e);
					return;
				}
				if (result) {
					MessageDialog.info(OperationRowEdit.this, "angal.operationrowedit.savesucces");
					fireOperationRowInserted(operationRow);
					this.myParent.dispose();
				} else {
					MessageDialog.error(OperationRowEdit.this, "angal.operationrowedit.saveerror");
				}
			}
		}
	}

	private void cancelButtonMouseClicked(MouseEvent mouseEvent) {
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
