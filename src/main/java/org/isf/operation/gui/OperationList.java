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
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

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

public class OperationList extends JPanel implements OperationRowListener, OperationRowEditListener {

	private static final long serialVersionUID = 1L;
	private JTable JtableData;
	private JLabel TypeSourceLabelValue;
	private JLabel CodeSourceLabelValue;
	private JLabel DateLabelValue;
	private JLabel PatientLabelValue;
	private JDialog parentContainer;
	private JDialog dialogOpe;
	private OperationRowEdit opeRowEdit;
	private List<OperationRow> oprowData;
	private Opd myOpd = null;
	private Admission myAdmission;
	private Patient myPatient;
	private Image ico;
	OhDefaultCellRenderer cellRenderer = new OhDefaultCellRenderer();
	// private int[] pColumnWidth = { 50, 120 ,70, 70 };

	OhTableOperationModel<OperationRow> modelOhOpeRow;
	OperationRowBrowserManager opeRowManager;

	// public OperationList(Opd opd) {
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
		GridBagLayout gbl_panelHeader = new GridBagLayout();
		gbl_panelHeader.columnWidths = new int[] { 50, 0, 65, 100, 165, 0 };
		gbl_panelHeader.rowHeights = new int[] { 20, 0 };
		gbl_panelHeader.columnWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		gbl_panelHeader.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		panelHeader.setLayout(gbl_panelHeader);

		TypeSourceLabelValue = new JLabel(MessageBundle.getMessage("angal.admission.patientfolder.opd")); //$NON-NLS-1$
		TypeSourceLabelValue.setFont(new Font("Tahoma", Font.PLAIN, 12)); //$NON-NLS-1$
		GridBagConstraints gbc_TypeSourceLabelValue = new GridBagConstraints();
		gbc_TypeSourceLabelValue.fill = GridBagConstraints.BOTH;
		gbc_TypeSourceLabelValue.insets = new Insets(0, 0, 0, 5);
		gbc_TypeSourceLabelValue.gridx = 1;
		gbc_TypeSourceLabelValue.gridy = 0;
		panelHeader.add(TypeSourceLabelValue, gbc_TypeSourceLabelValue);

		if (myOpd != null)
			CodeSourceLabelValue = new JLabel(myOpd.getCode() + ""); //$NON-NLS-1$
		else
			CodeSourceLabelValue = new JLabel(""); //$NON-NLS-1$
		CodeSourceLabelValue.setFont(new Font("Tahoma", Font.PLAIN, 12)); //$NON-NLS-1$
		GridBagConstraints gbc_CodeSourceLabelValue = new GridBagConstraints();
		gbc_CodeSourceLabelValue.fill = GridBagConstraints.BOTH;
		gbc_CodeSourceLabelValue.insets = new Insets(0, 0, 0, 5);
		gbc_CodeSourceLabelValue.gridx = 2;
		gbc_CodeSourceLabelValue.gridy = 0;
		panelHeader.add(CodeSourceLabelValue, gbc_CodeSourceLabelValue);

		if (myOpd != null)
			DateLabelValue = new JLabel(myOpd.getDate().toString());
		else
			DateLabelValue = new JLabel(""); //$NON-NLS-1$
		DateLabelValue.setFont(new Font("Tahoma", Font.PLAIN, 12)); //$NON-NLS-1$
		GridBagConstraints gbc_DateLabelValue = new GridBagConstraints();
		gbc_DateLabelValue.insets = new Insets(0, 0, 0, 5);
		gbc_DateLabelValue.anchor = GridBagConstraints.WEST;
		gbc_DateLabelValue.fill = GridBagConstraints.VERTICAL;
		gbc_DateLabelValue.gridx = 3;
		gbc_DateLabelValue.gridy = 0;
		panelHeader.add(DateLabelValue, gbc_DateLabelValue);

		if (myOpd != null)
			PatientLabelValue = new JLabel(myOpd.getFullName());
		else
			PatientLabelValue = new JLabel(""); //$NON-NLS-1$
		PatientLabelValue.setFont(new Font("Tahoma", Font.PLAIN, 14)); //$NON-NLS-1$
		GridBagConstraints gbc_PatientLabelValue = new GridBagConstraints();
		gbc_PatientLabelValue.anchor = GridBagConstraints.EAST;
		gbc_PatientLabelValue.fill = GridBagConstraints.VERTICAL;
		gbc_PatientLabelValue.gridx = 4;
		gbc_PatientLabelValue.gridy = 0;
		panelHeader.add(PatientLabelValue, gbc_PatientLabelValue);

		JPanel panelData = new JPanel();
		add(panelData);
		panelData.setLayout(new BorderLayout(0, 0));

		JScrollPane scrollPaneData = new JScrollPane();
		panelData.add(scrollPaneData);

		JPanel panelButtons = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panelButtons.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		add(panelButtons, BorderLayout.SOUTH);

		JButton UpdateButton = new JButton(MessageBundle.getMessage("angal.operationrowlist.update")); //$NON-NLS-1$
		UpdateButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				updateButtonMouseClicked(e);
			}
		});

		JButton addButton = new JButton(MessageBundle.getMessage("angal.operationrowlist.add")); //$NON-NLS-1$
		addButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent evt) {
				addButtonMouseClicked(evt);
			}
		});
		panelButtons.add(addButton);
		panelButtons.add(UpdateButton);

		JButton deleteButton = new JButton(MessageBundle.getMessage("angal.operationrowlist.delete")); //$NON-NLS-1$
		deleteButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				deleteButtonMouseClicked(e);
			}
		});
		panelButtons.add(deleteButton);

		JButton closeButton = new JButton(MessageBundle.getMessage("angal.operationrowlist.close")); //$NON-NLS-1$
		closeButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent evt) {
				closeButtonMouseClicked(evt);
			}
		});
		panelButtons.add(closeButton);

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
			} catch (OHServiceException ex) {
				ex.printStackTrace();
			}
		}
		if (myPatient != null) {
			AdmissionBrowserManager admManager = Context.getApplicationContext().getBean(AdmissionBrowserManager.class);
			OpdBrowserManager opdManager= Context.getApplicationContext().getBean(OpdBrowserManager.class);
			try {
				ArrayList<Admission> admissions = admManager.getAdmissions(myPatient);
				oprowData = new ArrayList<>();
				for (Admission adm : admissions) {
					oprowData.addAll(opeRowManager.getOperationRowByAdmission(adm));
				
				}
				ArrayList<Opd> opds =  opdManager.getOpdList(myPatient.getCode());
				for (Opd op : opds) {
					oprowData.addAll(opeRowManager.getOperationRowByOpd(op));
				
				}
			} catch (OHServiceException ex) {
				ex.printStackTrace();
			}
			panelHeader.setVisible(false);
			panelButtons.setVisible(false);
		}
		JtableData = new JTable();
		JtableData.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseExited(MouseEvent e) {
				cellRenderer.setHoveredRow(-1);
			}
		});
		scrollPaneData.setViewportView(JtableData);

		/* ** apply default oh cellRender **** */
		JtableData.setDefaultRenderer(Object.class, cellRenderer);
		JtableData.setDefaultRenderer(Double.class, cellRenderer);
		JtableData.addMouseMotionListener(new MouseMotionListener() {

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
		
		JtableData.setModel(modelOhOpeRow);
		
		dialogOpe = new JDialog();
		dialogOpe.setLocationRelativeTo(null);
		dialogOpe.setSize(450, 280);
		dialogOpe.setLocationRelativeTo(null);
		dialogOpe.setModal(true);
	}
	
	public void selectCorrect(GregorianCalendar startDate, GregorianCalendar endDate) {
		JtableData.clearSelection();
		for (int i = 0; i < oprowData.size(); i++) {

			OperationRow operation = (OperationRow) JtableData.getValueAt(i, -1);
			Date operationDate = operation.getOpDate().getTime();

			// Check that the operation date is included between startDate and endDate (if any).
			// On true condition select the corresponding table row.
			if (operationDate.after(startDate.getTime())
					&& (endDate != null && operationDate.before(endDate.getTime()))) {

				JtableData.addRowSelectionInterval(i, i);
			}
		}
	}

	public JTable getJtableData() {
		return JtableData;
	}

	public void setJtableData(JTable jtableData) {
		JtableData = jtableData;
	}

	public JLabel getTypeSourceLabelValue() {
		return TypeSourceLabelValue;
	}

	public void setTypeSourceLabelValue(JLabel typeSourceLabelValue) {
		TypeSourceLabelValue = typeSourceLabelValue;
	}

	public JLabel getCodeSourceLabelValue() {
		return CodeSourceLabelValue;
	}

	public void setCodeSourceLabelValue(JLabel codeSourceLabelValue) {
		CodeSourceLabelValue = codeSourceLabelValue;
	}

	public JLabel getDateLabelValue() {
		return DateLabelValue;
	}

	public void setDateLabelValue(JLabel dateLabelValue) {
		DateLabelValue = dateLabelValue;
	}

	public JLabel getPatientLabelValue() {
		return PatientLabelValue;
	}

	public void setPatientLabelValue(JLabel patientLabelValue) {
		PatientLabelValue = patientLabelValue;
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
		int idRow = this.JtableData.getSelectedRow();
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
		dialogOpe.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
	}

	private void updateButtonMouseClicked(java.awt.event.MouseEvent evt) {
		int idRow = this.JtableData.getSelectedRow();
		OperationRow operationRow = null;
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
		dialogOpe.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
	}

	private void deleteButtonMouseClicked(java.awt.event.MouseEvent evt) {
		int idRow = this.JtableData.getSelectedRow();
		OperationRow operationRow = null;
		if (idRow < 0) {
			MessageDialog.error(OperationList.this, "angal.common.pleaseselectarow.msg");
			return;
		} else {
			operationRow = oprowData.get(idRow);
		}
		int yesOrNo = JOptionPane.showConfirmDialog(OperationList.this,
				MessageBundle.getMessage("angal.operationrowlist.confirmdelete"), null, JOptionPane.YES_NO_OPTION); //$NON-NLS-1$
		if (yesOrNo == JOptionPane.YES_OPTION) {
			boolean result = false;
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
		JtableData.setModel(modelOhOpeRow);
		JtableData.repaint();
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
