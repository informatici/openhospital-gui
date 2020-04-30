package org.isf.medstockmovtype.gui;

import java.awt.BorderLayout;
import java.util.ArrayList;

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

public class MedicalsrMovPatList extends JPanel {
	private Patient myPatient;
	private ArrayList<MovementWard> oprowData;
	private JDialog dialogDrug;
	private JTable JtableData;
	private OhTableDrugsModel<MovementWard> modelOhOpeRow;
	private OhDefaultCellRenderer cellRenderer = new OhDefaultCellRenderer();
	private MovWardBrowserManager movManager = Context.getApplicationContext().getBean(MovWardBrowserManager.class);
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
				ArrayList<MovementWard> movPat = movManager.getMovementToPatient(myPatient);
				oprowData = new ArrayList<MovementWard>();
				for (MovementWard mov : movPat) {
					oprowData.add(mov);
				}
			} catch (OHServiceException ex) {
				ex.printStackTrace();
			} 
			
		}
		JtableData = new JTable();
		scrollPaneData.setViewportView(JtableData);
		/*** apply default oh cellRender *****/
		JtableData.setDefaultRenderer(Object.class, cellRenderer);
		JtableData.setDefaultRenderer(Double.class, cellRenderer);
		
		
		modelOhOpeRow = new OhTableDrugsModel<MovementWard>(oprowData);

		JtableData.setModel(modelOhOpeRow);
		dialogDrug = new JDialog();
		dialogDrug.setLocationRelativeTo(null);
		dialogDrug.setSize(450, 280);
		dialogDrug.setLocationRelativeTo(null);
		dialogDrug.setModal(true);
	}
}
