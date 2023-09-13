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
package org.isf.operation.gui;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.isf.admission.manager.AdmissionBrowserManager;
import org.isf.admission.model.Admission;
import org.isf.menu.manager.Context;
import org.isf.opd.manager.OpdBrowserManager;
import org.isf.opd.model.Opd;
import org.isf.operation.manager.OperationRowBrowserManager;
import org.isf.operation.model.OperationRow;
import org.isf.patient.model.Patient;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.OhDefaultCellRenderer;
import org.isf.utils.jobjects.OhTableOperationModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OperationList extends JPanel {

	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = LoggerFactory.getLogger(OperationList.class);

	// LISTENER INTERFACE
	// --------------------------------------------------------
	List<OperationList> operationRowListener = new ArrayList<>();

	public interface OperationRowListener extends EventListener {

		void operationRowInserted(AWTEvent aEvent);
	}

	private JTable jTableData;
	private List<OperationRow> oprowData;
	private Opd myOpd;
	private Admission myAdmission;
	private Patient myPatient;
	OhDefaultCellRenderer cellRenderer = new OhDefaultCellRenderer();

	OhTableOperationModel<OperationRow> modelOhOpeRow;

	OperationRowBrowserManager operationRowBrowserManager = Context.getApplicationContext().getBean(OperationRowBrowserManager.class);
	AdmissionBrowserManager admissionBrowserManager = Context.getApplicationContext().getBean(AdmissionBrowserManager.class);
	OpdBrowserManager opdBrowserManager = Context.getApplicationContext().getBean(OpdBrowserManager.class);

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
		setLayout(new BorderLayout(0, 0));

		JPanel panelData = new JPanel();
		add(panelData);
		panelData.setLayout(new BorderLayout(0, 0));

		JScrollPane scrollPaneData = new JScrollPane();
		panelData.add(scrollPaneData);

		/* *** getting data *** */
		if (myOpd != null) {
			try {
				oprowData = operationRowBrowserManager.getOperationRowByOpd(myOpd);
			} catch (OHServiceException e1) {
				OHServiceExceptionUtil.showMessages(e1);
			}
		}
		if (myAdmission != null) {
			try {
				oprowData = operationRowBrowserManager.getOperationRowByAdmission(myAdmission);
			} catch (OHServiceException ohServiceException) {
				LOGGER.error(ohServiceException.getMessage(), ohServiceException);
			}
		}
		if (myPatient != null) {
			try {
				List<Admission> admissions = admissionBrowserManager.getAdmissions(myPatient);
				oprowData = new ArrayList<>();
				for (Admission adm : admissions) {
					oprowData.addAll(operationRowBrowserManager.getOperationRowByAdmission(adm));
				}
				List<Opd> opds =  opdBrowserManager.getOpdList(myPatient.getCode());
				for (Opd op : opds) {
					oprowData.addAll(operationRowBrowserManager.getOperationRowByOpd(op));
				}
			} catch (OHServiceException ohServiceException) {
				LOGGER.error(ohServiceException.getMessage(), ohServiceException);
			}
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

		JDialog dialogOpe = new JDialog();
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

	public void operationRowInserted(AWTEvent aEvent) {
		refreshJtable();
	}

	private void refreshJtable() {
		try {
			oprowData = operationRowBrowserManager.getOperationRowByOpd(myOpd);
		} catch (OHServiceException e) {
			OHServiceExceptionUtil.showMessages(e);
		}
		modelOhOpeRow = new OhTableOperationModel<>(oprowData);
		jTableData.setModel(modelOhOpeRow);
		jTableData.repaint();
	}

	public List<OperationRow> getOprowData() {
		return oprowData;
	}

}
