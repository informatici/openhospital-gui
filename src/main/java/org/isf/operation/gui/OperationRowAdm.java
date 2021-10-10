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
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import org.isf.admission.gui.AdmissionBrowser;
import org.isf.admission.model.Admission;
import org.isf.generaldata.GeneralData;
import org.isf.generaldata.MessageBundle;
import org.isf.menu.gui.MainMenu;
import org.isf.menu.manager.Context;
import org.isf.operation.manager.OperationBrowserManager;
import org.isf.operation.manager.OperationRowBrowserManager;
import org.isf.operation.model.Operation;
import org.isf.operation.model.OperationRow;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.CustomJDateChooser;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.OhDefaultCellRenderer;
import org.isf.utils.jobjects.OhTableOperationModel;
import org.isf.utils.jobjects.VoFloatTextField;
import org.isf.utils.time.Converters;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author hp
 */
public class OperationRowAdm extends JPanel implements AdmissionBrowser.AdmissionListener {

	private static final long serialVersionUID = 1L;

	private static final Logger LOGGER = LoggerFactory.getLogger(OperationRowAdm.class);

	private JLabel labelDate;
	private JTextField textFieldUnit;
	private CustomJDateChooser textDate;
	private JComboBox comboOperation;
	private JComboBox comboResult;
	private JTextArea textAreaRemark;

	OperationBrowserManager opeManager = Context.getApplicationContext().getBean(OperationBrowserManager.class);
	OperationRowBrowserManager opeRowManager = Context.getApplicationContext().getBean(OperationRowBrowserManager.class);
	OhTableOperationModel<OperationRow> modelOhOpeRow;
	private List<OperationRow> oprowData = new ArrayList<>();
	private Admission myAdmission;

	OhDefaultCellRenderer cellRenderer = new OhDefaultCellRenderer();

	private CustomJDateChooser jCalendarDate;
	private JTable tableData;

	private List<String> operationResults = opeManager.getResultDescriptionList();

	
	public OperationRowAdm(Admission adm) {
		setLayout(new BorderLayout(0, 0));
		myAdmission = adm;
		JPanel panelForm = new JPanel();
		panelForm.setBorder(new EmptyBorder(10, 10, 10, 10));
		panelForm.setSize(new Dimension(200, 200));

		add(panelForm, BorderLayout.NORTH);
		GridBagLayout gblPanelForm = new GridBagLayout();
		gblPanelForm.columnWidths = new int[] { 0, 0, 0, 0, 0 };
		gblPanelForm.rowHeights = new int[] { 0, 0, 30, 0, 0 };
		gblPanelForm.columnWeights = new double[] { 0.0, 1.0, 0.0, 1.0, Double.MIN_VALUE };
		gblPanelForm.rowWeights = new double[] { 0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE };
		panelForm.setLayout(gblPanelForm);

		JLabel labelOperation = new JLabel(MessageBundle.getMessage("angal.operationrowedit.operation")); //$NON-NLS-1$
		GridBagConstraints gbcLabelOperation = new GridBagConstraints();
		gbcLabelOperation.anchor = GridBagConstraints.EAST;
		gbcLabelOperation.insets = new Insets(0, 0, 5, 5);
		gbcLabelOperation.gridx = 0;
		gbcLabelOperation.gridy = 0;
		panelForm.add(labelOperation, gbcLabelOperation);

		comboOperation = getOperationsBox();

		GridBagConstraints gbcComboOperation = new GridBagConstraints();
		gbcComboOperation.insets = new Insets(0, 0, 5, 5);
		gbcComboOperation.fill = GridBagConstraints.HORIZONTAL;
		gbcComboOperation.gridx = 1;
		gbcComboOperation.gridy = 0;
		panelForm.add(comboOperation, gbcComboOperation);

		labelDate = new JLabel(MessageBundle.getMessage("angal.operationrowlist.date")); //$NON-NLS-1$
		GridBagConstraints gbcLabelDate = new GridBagConstraints();
		gbcLabelDate.anchor = GridBagConstraints.EAST;
		gbcLabelDate.insets = new Insets(0, 0, 5, 5);
		gbcLabelDate.gridx = 2;
		gbcLabelDate.gridy = 0;
		panelForm.add(labelDate, gbcLabelDate);

		textDate = getJCalendarDate();
		GridBagConstraints gbcTextDate = new GridBagConstraints();
		gbcTextDate.insets = new Insets(0, 0, 5, 0);
		gbcTextDate.fill = GridBagConstraints.HORIZONTAL;
		gbcTextDate.gridx = 3;
		gbcTextDate.gridy = 0;
		panelForm.add(textDate, gbcTextDate);

		JLabel labelResultat = new JLabel(MessageBundle.getMessage("angal.common.result.txt"));
		GridBagConstraints gbcLabelResultat = new GridBagConstraints();
		gbcLabelResultat.anchor = GridBagConstraints.EAST;
		gbcLabelResultat.insets = new Insets(0, 0, 5, 5);
		gbcLabelResultat.gridx = 0;
		gbcLabelResultat.gridy = 1;
		panelForm.add(labelResultat, gbcLabelResultat);

		comboResult = getComboResultBox();
		GridBagConstraints gbcComboResult = new GridBagConstraints();
		gbcComboResult.insets = new Insets(0, 0, 5, 5);
		gbcComboResult.fill = GridBagConstraints.HORIZONTAL;
		gbcComboResult.gridx = 1;
		gbcComboResult.gridy = 1;
		panelForm.add(comboResult, gbcComboResult);

		JLabel lblUniteTrans = new JLabel(MessageBundle.getMessage("angal.operationrowedit.unitetrans")); //$NON-NLS-1$
		GridBagConstraints gbcLblUniteTrans = new GridBagConstraints();
		gbcLblUniteTrans.anchor = GridBagConstraints.EAST;
		gbcLblUniteTrans.insets = new Insets(0, 0, 5, 5);
		gbcLblUniteTrans.gridx = 2;
		gbcLblUniteTrans.gridy = 1;
		panelForm.add(lblUniteTrans, gbcLblUniteTrans);

		textFieldUnit = new VoFloatTextField(0, 100);
		GridBagConstraints gbcTextFieldUnit = new GridBagConstraints();
		gbcTextFieldUnit.insets = new Insets(0, 0, 5, 0);
		gbcTextFieldUnit.fill = GridBagConstraints.HORIZONTAL;
		gbcTextFieldUnit.gridx = 3;
		gbcTextFieldUnit.gridy = 1;
		panelForm.add(textFieldUnit, gbcTextFieldUnit);
		textFieldUnit.setColumns(10);

		JLabel lblRemarques = new JLabel(MessageBundle.getMessage("angal.operationrowedit.remark")); //$NON-NLS-1$
		GridBagConstraints gbcLblRemarques = new GridBagConstraints();
		gbcLblRemarques.insets = new Insets(0, 0, 5, 5);
		gbcLblRemarques.gridx = 0;
		gbcLblRemarques.gridy = 2;
		panelForm.add(lblRemarques, gbcLblRemarques);

		textAreaRemark = new JTextArea();
		textAreaRemark.setLineWrap(true);
		GridBagConstraints gbcTextAreaRemark = new GridBagConstraints();
		gbcTextAreaRemark.insets = new Insets(0, 0, 5, 0);
		gbcTextAreaRemark.gridwidth = 3;
		gbcTextAreaRemark.fill = GridBagConstraints.BOTH;
		gbcTextAreaRemark.gridx = 1;
		gbcTextAreaRemark.gridy = 2;
		panelForm.add(textAreaRemark, gbcTextAreaRemark);

		JPanel panelListData = new JPanel();
		add(panelListData, BorderLayout.CENTER);
		panelListData.setLayout(new BorderLayout(0, 0));

		JPanel panelActions = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panelActions.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		panelListData.add(panelActions, BorderLayout.NORTH);

		JButton btnAdd = new JButton(MessageBundle.getMessage("angal.operationrowlist.add.btn"));
		btnAdd.setMnemonic(MessageBundle.getMnemonic("angal.operationrowlist.add.btn.key"));
		btnAdd.addActionListener(actionEvent -> addToGrid());
		panelActions.add(btnAdd);

		JButton btnClear = new JButton(MessageBundle.getMessage("angal.operationrow.clear.btn"));
		btnClear.setMnemonic(MessageBundle.getMnemonic("angal.operationrow.clear.btn.key"));
		btnClear.addActionListener(actionEvent -> clearForm());
		panelActions.add(btnClear);

		JButton btnDelete = new JButton(MessageBundle.getMessage("angal.common.delete.btn"));
		btnDelete.setMnemonic(MessageBundle.getMnemonic("angal.common.delete.btn.key"));
		btnDelete.addActionListener(actionEvent -> {
			int index = tableData.getSelectedRow();
			deleteOpeRow(index);
		});
		panelActions.add(btnDelete);

		JPanel panelGridData = new JPanel();
		panelListData.add(panelGridData, BorderLayout.CENTER);
		panelGridData.setLayout(new BorderLayout(0, 0));

		JScrollPane scrollPaneData = new JScrollPane();

		panelGridData.add(scrollPaneData);

		tableData = new JTable();
		/* ** apply default oh cellRender **** */
		tableData.setDefaultRenderer(Object.class, cellRenderer);
		tableData.setDefaultRenderer(Double.class, cellRenderer);

		tableData.addMouseMotionListener(new MouseMotionListener() {
			@Override
			public void mouseMoved(MouseEvent e) {
				// TODO Auto-generated method stub
				JTable aTable = (JTable) e.getSource();
				int itsRow = aTable.rowAtPoint(e.getPoint());
				if (itsRow >= 0) {
					cellRenderer.setHoveredRow(itsRow);
				} else {
					cellRenderer.setHoveredRow(-1);
				}
				aTable.repaint();
			}

			@Override
			public void mouseDragged(MouseEvent e) {
				// TODO Auto-generated method stub

			}
		});
		tableData.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseExited(MouseEvent e) {
				cellRenderer.setHoveredRow(-1);
			}
		});

		tableData.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				addToForm();
			}
		});
		scrollPaneData.setViewportView(tableData);

		if (myAdmission != null) {
			try {
				List<OperationRow> res = opeRowManager.getOperationRowByAdmission(myAdmission);
				oprowData.addAll(res);
			} catch (OHServiceException ohServiceException) {
				LOGGER.error(ohServiceException.getMessage(), ohServiceException);
			}
		}
		modelOhOpeRow = new OhTableOperationModel<>(oprowData);
		tableData.setModel(modelOhOpeRow);
	}

	private CustomJDateChooser getJCalendarDate() {
		if (jCalendarDate == null) {
			jCalendarDate = new CustomJDateChooser();
			jCalendarDate.setLocale(new Locale(GeneralData.LANGUAGE));
			jCalendarDate.setDateFormatString(DATE_FORMAT_DD_MM_YY);
			jCalendarDate.setDate(DateTime.now().toDate());
		}
		return jCalendarDate;
	}

	private JComboBox getOperationsBox() {
		JComboBox comboOpe = new JComboBox();
		List<Operation> opeList = new ArrayList<>();
		try {
			opeList.addAll(opeManager.getOperationAdm());
		} catch (OHServiceException ex) {
			LOGGER.error(ex.getMessage(), ex);
		}
		comboOpe.addItem(null);
		for (org.isf.operation.model.Operation elem : opeList) {
			comboOpe.addItem(elem);
		}
		comboOpe.setEnabled(true);
		return comboOpe;
	}
	
	private JComboBox getComboResultBox() {
		JComboBox comboResult = new JComboBox();
			for (String description : operationResults) {
				comboResult.addItem(description);
			}
		return comboResult;
	}

	public void addToGrid() {
		if ((this.textDate.getDate() == null) || (this.comboOperation.getSelectedItem() == null)) {
			MessageDialog.error(OperationRowAdm.this, "angal.operationrowedit.warningdateope");
			return;
		}
		if ((myAdmission != null) && (myAdmission.getAdmDate().isAfter(Converters.convertToLocalDateTime(this.textDate.getDate())))) {
			MessageDialog.error(OperationRowAdm.this, "angal.operationrowedit.warningdateafter");
			return;
		}

		OperationRow operationRow = new OperationRow();
		operationRow.setOpDate(Converters.convertToLocalDateTime(this.textDate.getDate()));
		if (this.comboResult.getSelectedItem() != null) {
			operationRow.setOpResult(opeManager.getResultDescriptionKey((String) comboResult.getSelectedItem()));
		} else {
			operationRow.setOpResult("");
		}
		try {
			operationRow.setTransUnit(Float.parseFloat(this.textFieldUnit.getText()));
		} catch (NumberFormatException e) {
			operationRow.setTransUnit(0.0F);
		}
		Operation op = (Operation) this.comboOperation.getSelectedItem();
		operationRow.setOperation(op);
		if (myAdmission != null) {
			operationRow.setAdmission(myAdmission);
		}
		operationRow.setPrescriber(MainMenu.getUser().getUserName());
		operationRow.setRemarks(textAreaRemark.getText());
		int index = tableData.getSelectedRow();
		if (index < 0) {
			oprowData.add(operationRow);
		} else {
			OperationRow opeInter = oprowData.get(index);
			opeInter.setOpDate(Converters.convertToLocalDateTime(this.textDate.getDate()));
			String opResult = opeManager.getResultDescriptionKey((String) comboResult.getSelectedItem());
			opeInter.setOpResult(opResult);
			opeInter.setTransUnit(Float.parseFloat(this.textFieldUnit.getText()));
			op = (Operation) this.comboOperation.getSelectedItem();
			opeInter.setOperation(op);
			opeInter.setPrescriber(MainMenu.getUser().getUserName());
			opeInter.setRemarks(textAreaRemark.getText());
			oprowData.set(index, opeInter);
		}
		modelOhOpeRow = new OhTableOperationModel<>(oprowData);
		tableData.setModel(modelOhOpeRow);
		clearForm();
	}

	public void addToForm() {
		OperationRow opeRow = (OperationRow) oprowData.get(tableData.getSelectedRow());
		/* ** for combo operation **** */
		List<Operation> opeList = new ArrayList<>();
		try {
			opeList.addAll(opeManager.getOperationAdm());
		} catch (OHServiceException ex) {
			//
		}
		if (opeRow != null) {
			boolean found = false;
			for (org.isf.operation.model.Operation elem : opeList) {
				if (opeRow.getOperation().getCode().equals(elem.getCode())) {
					found = true;
					comboOperation.setSelectedItem(elem);
					comboOperation.setEditable(false);
					comboOperation.setEnabled(false);
					break;
				}
			}
			if (!found) {
				comboOperation.addItem(null);
			}
		}

		if (opeRow != null) {
			textDate.setDate(opeRow.getOpDate());
			textAreaRemark.setText(opeRow.getRemarks());
			textFieldUnit.setText(opeRow.getTransUnit() + ""); //$NON-NLS-1$
		}

		/* ***** resultat **** */
		int index = 0;
		for (int i = 0; i < operationResults.size(); i++) {
			if (opeRow.getOpResult() != null && (opeManager.getResultDescriptionKey(operationResults.get(i) + "")).equals(opeRow.getOpResult())) { //$NON-NLS-1$ 
				index = i;
			}
		}
		comboResult.setSelectedIndex(index);
		/* *********** */

	}

	public void deleteOpeRow(int idRow) {
		OperationRow operationRow;
		if (idRow < 0) {
			MessageDialog.error(OperationRowAdm.this, "angal.common.pleaseselectarow.msg");
		} else {
			operationRow = oprowData.get(idRow);
			int answer = MessageDialog.yesNo(OperationRowAdm.this, "angal.operationrowlist.delete.operation.msg");
			if (answer == JOptionPane.YES_OPTION) {
				int idOpe = operationRow.getId();
				if (idOpe > 0) {
					boolean result = false;
					try {
						result = opeRowManager.deleteOperationRow(operationRow);
					} catch (OHServiceException e) {
						OHServiceExceptionUtil.showMessages(e);
						return;
					}
					if (result) {
						MessageDialog.info(OperationRowAdm.this, "angal.operationrowlist.successdel");
						oprowData.remove(idRow);
						modelOhOpeRow = new OhTableOperationModel<>(oprowData);
						tableData.setModel(modelOhOpeRow);
						tableData.repaint();
						clearForm();
					} else {
						MessageDialog.error(OperationRowAdm.this, "angal.operationrowlist.errosdel");
					}
				} else {
					MessageDialog.info(OperationRowAdm.this, "angal.operationrowlist.successdel");
					oprowData.remove(idOpe);
					modelOhOpeRow = new OhTableOperationModel<>(oprowData);
					tableData.setModel(modelOhOpeRow);
					tableData.repaint();
					clearForm();
				}
			}
		}
	}

	@Override
	public void admissionUpdated(AWTEvent e) {
		try {
			saveAllOpeRow(oprowData, opeRowManager, e);
		} catch (OHServiceException e1) {
			OHServiceExceptionUtil.showMessages(e1);
		}
	}

	@Override
	public void admissionInserted(AWTEvent e) {
		try {
			saveAllOpeRow(oprowData, opeRowManager, e);
		} catch (OHServiceException e1) {
			OHServiceExceptionUtil.showMessages(e1);
		}
	}

	public void saveAllOpeRow(List<OperationRow> listOpe, OperationRowBrowserManager rowManager, AWTEvent e) throws OHServiceException {
		for (org.isf.operation.model.OperationRow opRow : listOpe) {
			if ((opRow.getId() > 0) && (opRow.getAdmission() != null && opRow.getAdmission().getId() > 0)) {
				try {
					rowManager.updateOperationRow(opRow);
				} catch (OHServiceException e1) {
					OHServiceExceptionUtil.showMessages(e1);
				}
			}
			if ((opRow.getId() <= 0) && (opRow.getAdmission() != null && opRow.getAdmission().getId() > 0)) {
				try {
					rowManager.newOperationRow(opRow);
				} catch (OHServiceException e1) {
					OHServiceExceptionUtil.showMessages(e1);
				}
			}
			if ((opRow.getId() <= 0) && (opRow.getAdmission() == null || opRow.getAdmission().getId() <= 0)) {
				Admission admiss = (Admission) e.getSource();
				opRow.setAdmission(admiss);
				try {
					rowManager.newOperationRow(opRow);
				} catch (OHServiceException e1) {
					OHServiceExceptionUtil.showMessages(e1);
				}
			}
		}
	}

	public void clearForm() {
		comboOperation.setSelectedItem(null);
		textDate.setDate((LocalDateTime) null);
		textAreaRemark.setText("");
		comboResult.setSelectedIndex(-1);
		textFieldUnit.setText("");
		tableData.clearSelection();
		comboOperation.setEditable(true);
		comboOperation.setEnabled(true);
	}

	public List<OperationRow> getOprowData() {
		return oprowData;
	}

	public void setOprowData(List<OperationRow> oprowData) {
		this.oprowData = oprowData;
	}
	
}
