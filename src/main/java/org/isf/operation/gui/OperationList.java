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

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.WindowConstants;

import org.isf.admission.manager.AdmissionBrowserManager;
import org.isf.admission.model.Admission;
import org.isf.generaldata.MessageBundle;
import org.isf.menu.manager.Context;
import org.isf.opd.manager.OpdBrowserManager;
import org.isf.opd.model.Opd;
import org.isf.operation.gui.OperationRowEdit.OperationRowEditListener;
import org.isf.operation.gui.OperationRowEdit.OperationRowListener;
import org.isf.operation.manager.OperationRowBrowserManager;
import org.isf.operation.model.OperationRow;
import org.isf.patient.model.Patient;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.OhDefaultCellRenderer;
import org.isf.utils.jobjects.OhTableOperationModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OperationList extends JPanel implements OperationRowListener, OperationRowEditListener {

	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = LoggerFactory.getLogger(OperationList.class);

	private JTable jTableData;
	private JLabel typeSourceLabelValue;
	private JLabel codeSourceLabelValue;
	private JLabel dateLabelValue;
	private JLabel patientLabelValue;
	private JDialog parentContainer;
	private JDialog dialogOpe;
	private OperationRowEdit opeRowEdit;
	private List<OperationRow> oprowData;
	private Opd myOpd = null;
	private Admission myAdmission;
	private Patient myPatient;
	private Image ico;
	OhDefaultCellRenderer cellRenderer = new OhDefaultCellRenderer();

	OhTableOperationModel<OperationRow> modelOhOpeRow;
	OperationRowBrowserManager opeRowManager;

	public OperationList(Object object) {
		if (object instanceof Opd) {
			myOpd = (Opd) object;
		}
		if (object instanceof Admission) {
			myAdmission = (Admission) object;
		}
		if (object instanceof Patient) {
			myPatient = (Patient) object;
		}
		opeRowManager = Context.getApplicationContext().getBean(OperationRowBrowserManager.class);
		setLayout(new BorderLayout(0, 0));
		ico = new javax.swing.ImageIcon("rsc/icons/oh.png").getImage(); //$NON-NLS-1$

		JPanel panelHeader = new JPanel();
		add(panelHeader, BorderLayout.NORTH);
		GridBagLayout gblPanelHeader = new GridBagLayout();
		gblPanelHeader.columnWidths = new int[] { 50, 0, 65, 100, 165, 0 };
		gblPanelHeader.rowHeights = new int[] { 20, 0 };
		gblPanelHeader.columnWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		gblPanelHeader.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		panelHeader.setLayout(gblPanelHeader);

		typeSourceLabelValue = new JLabel(MessageBundle.getMessage("angal.admission.patientfolder.opd")); //$NON-NLS-1$
		typeSourceLabelValue.setFont(new Font("Tahoma", Font.PLAIN, 12)); //$NON-NLS-1$
		GridBagConstraints gbcTypeSourceLabelValue = new GridBagConstraints();
		gbcTypeSourceLabelValue.fill = GridBagConstraints.BOTH;
		gbcTypeSourceLabelValue.insets = new Insets(0, 0, 0, 5);
		gbcTypeSourceLabelValue.gridx = 1;
		gbcTypeSourceLabelValue.gridy = 0;
		panelHeader.add(typeSourceLabelValue, gbcTypeSourceLabelValue);

		if (myOpd != null) {
			codeSourceLabelValue = new JLabel(myOpd.getCode() + ""); //$NON-NLS-1$
		} else {
			codeSourceLabelValue = new JLabel(""); //$NON-NLS-1$
		}
		codeSourceLabelValue.setFont(new Font("Tahoma", Font.PLAIN, 12)); //$NON-NLS-1$
		GridBagConstraints gbcCodeSourceLabelValue = new GridBagConstraints();
		gbcCodeSourceLabelValue.fill = GridBagConstraints.BOTH;
		gbcCodeSourceLabelValue.insets = new Insets(0, 0, 0, 5);
		gbcCodeSourceLabelValue.gridx = 2;
		gbcCodeSourceLabelValue.gridy = 0;
		panelHeader.add(codeSourceLabelValue, gbcCodeSourceLabelValue);

		if (myOpd != null) {
			dateLabelValue = new JLabel(myOpd.getDate().toString());
		} else {
			dateLabelValue = new JLabel(""); //$NON-NLS-1$
		}
		dateLabelValue.setFont(new Font("Tahoma", Font.PLAIN, 12)); //$NON-NLS-1$
		GridBagConstraints gbcDateLabelValue = new GridBagConstraints();
		gbcDateLabelValue.insets = new Insets(0, 0, 0, 5);
		gbcDateLabelValue.anchor = GridBagConstraints.WEST;
		gbcDateLabelValue.fill = GridBagConstraints.VERTICAL;
		gbcDateLabelValue.gridx = 3;
		gbcDateLabelValue.gridy = 0;
		panelHeader.add(dateLabelValue, gbcDateLabelValue);

		if (myOpd != null) {
			patientLabelValue = new JLabel(myOpd.getFullName());
		} else {
			patientLabelValue = new JLabel(""); //$NON-NLS-1$
		}
		patientLabelValue.setFont(new Font("Tahoma", Font.PLAIN, 14)); //$NON-NLS-1$
		GridBagConstraints gbcPatientLabelValue = new GridBagConstraints();
		gbcPatientLabelValue.anchor = GridBagConstraints.EAST;
		gbcPatientLabelValue.fill = GridBagConstraints.VERTICAL;
		gbcPatientLabelValue.gridx = 4;
		gbcPatientLabelValue.gridy = 0;
		panelHeader.add(patientLabelValue, gbcPatientLabelValue);

		JPanel panelData = new JPanel();
		add(panelData);
		panelData.setLayout(new BorderLayout(0, 0));

		JScrollPane scrollPaneData = new JScrollPane();
		panelData.add(scrollPaneData);

		JPanel panelButtons = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panelButtons.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		add(panelButtons, BorderLayout.SOUTH);

		JButton updateButton = new JButton(MessageBundle.getMessage("angal.common.update.btn"));
		updateButton.setMnemonic(MessageBundle.getMnemonic("angal.common.update.btn.key"));
		updateButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				updateButtonMouseClicked(e);
			}
		});

		JButton addButton = new JButton(MessageBundle.getMessage("angal.operationrowlist.add.btn"));
		addButton.setMnemonic(MessageBundle.getMnemonic("angal.operationrowlist.add.btn.key"));
		addButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent evt) {
				addButtonMouseClicked(evt);
			}
		});
		panelButtons.add(addButton);
		panelButtons.add(updateButton);

		JButton deleteButton = new JButton(MessageBundle.getMessage("angal.common.delete.btn"));
		deleteButton.setMnemonic(MessageBundle.getMnemonic("angal.common.delete.btn.key"));
		deleteButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				deleteButtonMouseClicked(e);
			}
		});
		panelButtons.add(deleteButton);

		JButton cancelButton = new JButton(MessageBundle.getMessage("angal.common.cancel.btn"));
		cancelButton.setMnemonic(MessageBundle.getMnemonic("angal.common.cancel.btn.key"));
		cancelButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent evt) {
				closeButtonMouseClicked(evt);
			}
		});
		panelButtons.add(cancelButton);

		/* *** getting data *** */
		if (myOpd != null) {
			try {
				oprowData = opeRowManager.getOperationRowByOpd(myOpd);
			} catch (OHServiceException e1) {
				OHServiceExceptionUtil.showMessages(e1);
			}
		}
		if (myAdmission != null) {
			try {
				oprowData = opeRowManager.getOperationRowByAdmission(myAdmission);
			} catch (OHServiceException ohServiceException) {
				LOGGER.error(ohServiceException.getMessage(), ohServiceException);
			}
		}
		if (myPatient != null) {
			AdmissionBrowserManager admManager = Context.getApplicationContext().getBean(AdmissionBrowserManager.class);
			OpdBrowserManager opdManager= Context.getApplicationContext().getBean(OpdBrowserManager.class);
			try {
				List<Admission> admissions = admManager.getAdmissions(myPatient);
				oprowData = new ArrayList<>();
				for (Admission adm : admissions) {
					oprowData.addAll(opeRowManager.getOperationRowByAdmission(adm));
				}
				List<Opd> opds =  opdManager.getOpdList(myPatient.getCode());
				for (Opd op : opds) {
					oprowData.addAll(opeRowManager.getOperationRowByOpd(op));
				}
			} catch (OHServiceException ohServiceException) {
				LOGGER.error(ohServiceException.getMessage(), ohServiceException);
			}
			panelHeader.setVisible(false);
			panelButtons.setVisible(false);
		}
		jTableData = new JTable();
		jTableData.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseExited(MouseEvent e) {
				cellRenderer.setHoveredRow(-1);
			}
		});
		scrollPaneData.setViewportView(jTableData);

		/* ** apply default oh cellRender **** */
		jTableData.setDefaultRenderer(Object.class, cellRenderer);
		jTableData.setDefaultRenderer(Double.class, cellRenderer);
		jTableData.addMouseMotionListener(new MouseMotionListener() {

			@Override
			public void mouseMoved(MouseEvent e) {
				JTable aTable = (JTable) e.getSource();
				int itsRow = aTable.rowAtPoint(e.getPoint());
				if ((itsRow >= 0) && ((aTable.getModel().getRowCount() - 1) >= itsRow)) {
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

		modelOhOpeRow = new OhTableOperationModel<>(oprowData);
		
		jTableData.setModel(modelOhOpeRow);
		
		dialogOpe = new JDialog();
		dialogOpe.setLocationRelativeTo(null);
		dialogOpe.setSize(450, 280);
		dialogOpe.setLocationRelativeTo(null);
		dialogOpe.setModal(true);
	}
	
	public void selectCorrect(LocalDateTime startDate, LocalDateTime endDate) {
		jTableData.clearSelection();
		for (int i = 0; i < oprowData.size(); i++) {

			OperationRow operation = (OperationRow) jTableData.getValueAt(i, -1);
			LocalDateTime operationDate = operation.getOpDate();

			// Check that the operation date is included between startDate and endDate (if any).
			// On true condition select the corresponding table row.
			if (operationDate.isAfter(startDate) && (endDate != null && operationDate.isBefore(endDate))) {
				jTableData.addRowSelectionInterval(i, i);
			}
		}
	}

	public JTable getjTableData() {
		return jTableData;
	}

	public void setjTableData(JTable jTableData) {
		this.jTableData = jTableData;
	}

	public JLabel getTypeSourceLabelValue() {
		return typeSourceLabelValue;
	}

	public void setTypeSourceLabelValue(JLabel typeSourceLabelValue) {
		this.typeSourceLabelValue = typeSourceLabelValue;
	}

	public JLabel getCodeSourceLabelValue() {
		return codeSourceLabelValue;
	}

	public void setCodeSourceLabelValue(JLabel codeSourceLabelValue) {
		this.codeSourceLabelValue = codeSourceLabelValue;
	}

	public JLabel getDateLabelValue() {
		return dateLabelValue;
	}

	public void setDateLabelValue(JLabel dateLabelValue) {
		this.dateLabelValue = dateLabelValue;
	}

	public JLabel getPatientLabelValue() {
		return patientLabelValue;
	}

	public void setPatientLabelValue(JLabel patientLabelValue) {
		this.patientLabelValue = patientLabelValue;
	}

	public JDialog getParentContainer() {
		return parentContainer;
	}

	public void setParentContainer(JDialog parentContainer) {
		this.parentContainer = parentContainer;
	}

	/* ***** functions events **** */
	private void closeButtonMouseClicked(java.awt.event.MouseEvent evt) {
		this.setVisible(false);
		this.parentContainer.dispose();
	}

	private void addButtonMouseClicked(java.awt.event.MouseEvent evt) {
		int idRow = this.jTableData.getSelectedRow();
		OperationRow operationRow = null;
		opeRowEdit = new OperationRowEdit(operationRow);
		opeRowEdit.setMyOpd(myOpd);
		opeRowEdit.addOperationListener(OperationList.this);
		dialogOpe.setContentPane(opeRowEdit);
		dialogOpe.setIconImage(ico);
		dialogOpe.setTitle(MessageBundle.getMessage("angal.operationrowlist.newoperation.title"));
		opeRowEdit.setMyParent(dialogOpe);
		opeRowEdit.getTitleLabel().setText(MessageBundle.getMessage("angal.operationrowlist.newoperation.title"));
		dialogOpe.setVisible(true);
		dialogOpe.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	}

	private void updateButtonMouseClicked(java.awt.event.MouseEvent evt) {
		int idRow = this.jTableData.getSelectedRow();
		OperationRow operationRow;
		if (idRow < 0) {
			MessageDialog.error(OperationList.this, "angal.common.pleaseselectarow.msg");
			return;
		} else {
			operationRow = oprowData.get(idRow);
		}
		opeRowEdit = new OperationRowEdit(operationRow);
		opeRowEdit.setMyOpd(myOpd);
		opeRowEdit.addOperationRowListener(OperationList.this);
		dialogOpe.setContentPane(opeRowEdit);
		dialogOpe.setIconImage(ico);
		dialogOpe.setTitle(MessageBundle.getMessage("angal.operationrowlist.editoperation.title"));
		opeRowEdit.setMyParent(dialogOpe);
		opeRowEdit.getTitleLabel().setText(MessageBundle.getMessage("angal.operationrowlist.editoperation.title"));
		dialogOpe.setVisible(true);
		dialogOpe.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	}

	private void deleteButtonMouseClicked(java.awt.event.MouseEvent evt) {
		int idRow = this.jTableData.getSelectedRow();
		OperationRow operationRow;
		if (idRow < 0) {
			MessageDialog.error(OperationList.this, "angal.common.pleaseselectarow.msg");
			return;
		}
		operationRow = oprowData.get(idRow);
		int answer = MessageDialog.yesNo(OperationList.this, "angal.operationrowlist.delete.operation.msg");
		if (answer == JOptionPane.YES_OPTION) {
			boolean result;
			try {
				result = opeRowManager.deleteOperationRow(operationRow);
			} catch (OHServiceException e) {
				OHServiceExceptionUtil.showMessages(e);
				return;
			}
			if (result) {
				MessageDialog.info(OperationList.this, "angal.operationrowlist.successdel");
				refreshJtable();
			} else {
				MessageDialog.error(OperationList.this, "angal.operationrowlist.errosdel");
			}
		}
	}

	@Override
	public void operationRowInserted(AWTEvent aEvent) {
		refreshJtable();
	}

	@Override
	public void operationRowEdited(AWTEvent event) {
		refreshJtable();
	}

	public void refreshJtable() {
		try {
			oprowData = opeRowManager.getOperationRowByOpd(myOpd);
		} catch (OHServiceException e) {
			OHServiceExceptionUtil.showMessages(e);
		}
		modelOhOpeRow = new OhTableOperationModel<>(oprowData);
		jTableData.setModel(modelOhOpeRow);
		jTableData.repaint();
	}

	public Admission getMyAdmission() {
		return myAdmission;
	}

	public void setMyAdmission(Admission myAdmission) {
		this.myAdmission = myAdmission;
	}

	public List<OperationRow> getOprowData() {
		return oprowData;
	}

	public void setOprowData(List<OperationRow> oprowData) {
		this.oprowData = oprowData;
	}

}
