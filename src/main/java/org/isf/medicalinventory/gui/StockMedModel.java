package org.isf.medicalinventory.gui;

import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.table.DefaultTableModel;

import org.isf.generaldata.MessageBundle;
import org.isf.medicals.model.Medical;
import org.isf.utils.db.NormalizeString;

public class StockMedModel extends DefaultTableModel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ArrayList<Medical> medList;
	private ArrayList<Medical> initList=new ArrayList<Medical>();
	
	public StockMedModel(ArrayList<Medical> meds) {
		medList = meds;
		initList.addAll(medList);
	}
	public int getRowCount() {
		if (medList == null)
			return 0;
		return medList.size();
	}

	public String getColumnName(int c) {
		if (c == 0) {
			return MessageBundle.getMessage("angal.common.code.txt").toUpperCase();
		}
		if (c == 1) {
			return MessageBundle.getMessage("angal.common.description.txt").toUpperCase();
		}
		return ""; //$NON-NLS-1$
	}

	public int getColumnCount() {
		return 2;
	}

	public Object getValueAt(int r, int c) {
		Medical med = medList.get(r);
		if (c == -1) {
			return med;
		} else if (c == 0) {
			return med.getProdCode();
		} else if (c == 1) {
			return med.getDescription();
		}
		return null;
	}
	
	public void filter(String searchValue){
		medList.clear();
		for (Iterator<Medical> iterator = initList.iterator(); iterator.hasNext();) {
			Medical med = (Medical) iterator.next();
			if(med.getProdCode().trim().equalsIgnoreCase(searchValue.trim())){
				medList.add(med);
			}
			else if(NormalizeString.normalizeContains(med.getProdCode().toLowerCase().trim()+med.getDescription().toLowerCase(), searchValue.toLowerCase().trim())){
				medList.add(med);
			}
		}
	}
	public Medical getMedicalAtRow(int row){
		if(medList.size()>row && row>=0){
			return medList.get(row);
		}
		return null;
	}

	@Override
	public boolean isCellEditable(int arg0, int arg1) {
		return false;
	}


}