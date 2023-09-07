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
package org.isf.medstockmovtype.gui;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.isf.medicalstockward.manager.MovWardBrowserManager;
import org.isf.medicalstockward.model.MovementWard;
import org.isf.menu.manager.Context;
import org.isf.patient.model.Patient;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.jobjects.OhDefaultCellRenderer;
import org.isf.utils.jobjects.OhTableDrugsModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MedicalsrMovPatList extends JPanel {

	private static final Logger LOGGER = LoggerFactory.getLogger(MedicalsrMovPatList.class);

	private Patient myPatient;
	private List<MovementWard> drugsData;
	private JTable jTableData;
	private MovWardBrowserManager movWardBrowserManager = Context.getApplicationContext().getBean(MovWardBrowserManager.class);

	public MedicalsrMovPatList(Object object) {

		setLayout(new BorderLayout(0, 0));
		JPanel panelData = new JPanel();
		add(panelData);
		panelData.setLayout(new BorderLayout(0, 0));

		JScrollPane scrollPaneData = new JScrollPane();
		panelData.add(scrollPaneData);
	
		if (object instanceof Patient) {
			myPatient = (Patient) object;
		}
		
		if (myPatient != null) {
			try {
				List<MovementWard> movPat = movWardBrowserManager.getMovementToPatient(myPatient);
				drugsData = new ArrayList<>();
				drugsData.addAll(movPat);
			} catch (OHServiceException ohServiceException) {
				LOGGER.error(ohServiceException.getMessage(), ohServiceException);
			}
		}
		jTableData = new JTable();
		scrollPaneData.setViewportView(jTableData);
		/* ** apply default oh cellRender **** */
		OhDefaultCellRenderer cellRenderer = new OhDefaultCellRenderer();
		jTableData.setDefaultRenderer(Object.class, cellRenderer);
		jTableData.setDefaultRenderer(Double.class, cellRenderer);

		OhTableDrugsModel<MovementWard> modelMedWard = new OhTableDrugsModel<>(drugsData);

		jTableData.setModel(modelMedWard);
		JDialog dialogDrug = new JDialog();
		dialogDrug.setLocationRelativeTo(null);
		dialogDrug.setSize(450, 280);
		dialogDrug.setLocationRelativeTo(null);
		dialogDrug.setModal(true);
	}
	
	public List<MovementWard> getDrugsData() {
		return drugsData;
	}

	public JTable getJTable() {
		return jTableData;
	}

}
