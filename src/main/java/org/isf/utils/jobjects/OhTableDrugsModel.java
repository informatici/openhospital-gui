package org.isf.utils.jobjects;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import org.isf.generaldata.MessageBundle;
import org.isf.medicals.model.Medical;
import org.isf.medicalstockward.model.MovementWard;
import org.isf.operation.manager.OperationBrowserManager;

public class OhTableDrugsModel <T> implements TableModel{

	List<T> dataList;	
	List<T> filteredList;
	OperationBrowserManager manageop = new OperationBrowserManager();
	
	public  OhTableDrugsModel(List<T> dataList) {
		this.dataList = dataList;
		this.filteredList = new ArrayList<T>();
		
		if(dataList!=null){
			for (Iterator<T> iterator = dataList.iterator(); iterator.hasNext();) {
				T t = (T) iterator.next();
				this.filteredList.add(t);			
			}
		}
	}
	

	
	@Override
	public void addTableModelListener(TableModelListener l) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return String.class;
	}

	@Override
	public int getColumnCount() {
		return 4;
	}

	@Override
	public String getColumnName(int columnIndex) {
		String columnLable="";
		switch (columnIndex) {
		case 0:
			columnLable= MessageBundle.getMessage("angal.medicalstockward.patient.date");
			//columnLable= "Date";
			break;
		case 1:
			columnLable= MessageBundle.getMessage("angal.medicalstockward.patient.decription");
			//columnLable= "Nature Operation";
			break;
		case 2:
			columnLable= MessageBundle.getMessage("angal.medicalstockward.patient.quantity");
			//columnLable= "Resultat";
			break;
		case 3:
			columnLable= MessageBundle.getMessage("angal.medicalstockward.patient.units");
			//columnLable= "Unite Trans";
			break;	
		default:
			break;
		}
		return columnLable;
	}

	@Override
	public int getRowCount() {
		if(this.filteredList==null){
			return 0;
		}
		return this.filteredList.size();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		// TODO Auto-generated method stub
		String value="";
		if(rowIndex >=0 && rowIndex < this.filteredList.size()){
			T obj=this.filteredList.get(rowIndex);
			if(obj instanceof MovementWard){
				MovementWard DrugObj=(MovementWard)obj;
				switch (columnIndex) {
				case 0:
					String dt = "";
					try {
						final DateFormat currentDateFormat = DateFormat.getDateInstance(DateFormat.SHORT, Locale.FRENCH);
						dt = currentDateFormat.format(DrugObj.getDate().getTime());
						value = dt;
					}
					catch (Exception ex){
						value=DrugObj.getDate().getTime().toString();
					}
					
					break;
				case 1:
										Medical drugsname = null;
					//System.out.println("Looking operation whose code is " + opdObj.getOperation().getCode());
					   drugsname = DrugObj.getMedical();
					if(drugsname != null)					
						value = drugsname.getDescription();
					else
						value = "";
					break;
				case 2:
					value=String.valueOf(DrugObj.getQuantity());
					break;
				case 3:
					value=DrugObj.getUnits();
					break;	
				default:
					break;
				}
			}			
		}
		return value;
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void removeTableModelListener(TableModelListener l) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		// TODO Auto-generated method stub
		
	}

}
