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
package org.isf.utils.jobjects;

import java.util.ArrayList;
import java.util.List;

import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import org.isf.accounting.model.BillItems;
import org.isf.generaldata.MessageBundle;
import org.isf.medicalstockward.model.MedicalWard;
import org.isf.priceslist.model.Price;
import org.isf.pricesothers.model.PricesOthers;
import org.isf.utils.exception.OHException;

/**
 * This class builds products table with filter
 *
 * @author u2g
 */
public class OhTableModel<T> implements TableModel {

	List<T> dataList;
	List<T> filteredList;
	String searchQuery = "";
	boolean allowSearchByCode;

	public OhTableModel(List<T> dataList) {
		this.dataList = dataList;
		this.filteredList = new ArrayList<>();

		this.filteredList.addAll(dataList);
	}

	public OhTableModel(List<T> dataList, boolean allowSearchByCode) {
		this.allowSearchByCode = allowSearchByCode;
		this.dataList = dataList;
		this.filteredList = new ArrayList<>();

		this.filteredList.addAll(dataList);
	}

	public T filter(String searchQuery) throws OHException {
		this.searchQuery = searchQuery;
		this.filteredList = new ArrayList<>();

		for (T t : this.dataList) {
			Object object = t;
			if (object instanceof Price) {
				Price price = (Price) object;
				String strItem = price.getItem() + price.getDesc();
				if (allowSearchByCode && searchQuery.equalsIgnoreCase(price.getItem())) {
					T resPbj = (T) object;
					filteredList.clear();
					filteredList.add(resPbj);
					return resPbj;
				}
				strItem = strItem.toLowerCase();
				searchQuery = searchQuery.toLowerCase();
				if (strItem.contains(searchQuery)) {
					filteredList.add((T) object);
				}
			}

			if (object instanceof MedicalWard) {
				MedicalWard mdw = (MedicalWard) object;
				String strItem = mdw.getMedical().getProdCode() + mdw.getMedical().getDescription();

				if (allowSearchByCode && searchQuery.equalsIgnoreCase(mdw.getMedical().getProdCode())) {
					T resPbj = (T) object;
					filteredList.clear();
					filteredList.add(resPbj);
					return resPbj;
				}

				strItem = strItem.toLowerCase();
				searchQuery = searchQuery.toLowerCase();
				if (strItem.contains(searchQuery)) {
					filteredList.add((T) object);
				}
			}

			if (object instanceof PricesOthers) {
				PricesOthers priceO = (PricesOthers) object;
				String strItem = priceO.getCode() + priceO.getDescription();

				if (allowSearchByCode && searchQuery.equalsIgnoreCase(priceO.getCode())) {
					T resPbj = (T) object;
					filteredList.clear();
					filteredList.add(resPbj);
					return resPbj;
				}

				strItem = strItem.toLowerCase();
				searchQuery = searchQuery.toLowerCase();
				if (strItem.contains(searchQuery)) {
					filteredList.add((T) object);
				}
			}

			if (object instanceof BillItems) {
				BillItems priceO = (BillItems) object;
				String strItem = priceO.getItemDisplayCode() + priceO.getItemDescription();

				if (allowSearchByCode && searchQuery.equalsIgnoreCase(priceO.getItemDisplayCode())) {
					T resPbj = (T) object;
					filteredList.clear();
					filteredList.add(resPbj);
					return resPbj;
				}

				strItem = strItem.toLowerCase();
				searchQuery = searchQuery.toLowerCase();
				if (strItem.contains(searchQuery)) {
					filteredList.add((T) object);
				}
			}

		}
		if (filteredList.size() == 1) {
			return filteredList.get(0);
		}
		return null;
	}

	@Override
	public void addTableModelListener(TableModelListener l) {
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return String.class;
	}

	@Override
	public int getColumnCount() {
		return 2;
	}

	@Override
	public String getColumnName(int columnIndex) {
		String columnLabel = "";
		switch (columnIndex) {
			case 0:
				columnLabel = MessageBundle.getMessage("angal.disctype.codem").toUpperCase();
				break;
			case 1:
				columnLabel = MessageBundle.getMessage("angal.common.description.txt").toUpperCase();
				break;
			default:
				break;
		}
		return columnLabel;
	}

	@Override
	public int getRowCount() {
		if (this.filteredList == null) {
			return 0;
		}
		return this.filteredList.size();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {

		String value = "";
		if (rowIndex >= 0 && rowIndex < this.filteredList.size()) {
			T obj = this.filteredList.get(rowIndex);
			if (obj instanceof Price) {
				Price priceObj = (Price) obj;
				if (columnIndex == 0) {
					value = priceObj.getItem() != null ? priceObj.getItem() : String.valueOf(priceObj.getId());
				} else {
					value = priceObj.getDesc();
				}
			}
			if (obj instanceof MedicalWard) {
				MedicalWard mdwObj = (MedicalWard) obj;
				if (columnIndex == 0) {
					value = mdwObj.getMedical().getProdCode() != null ? mdwObj.getMedical().getProdCode() : String.valueOf(mdwObj.getMedical().getCode());
				} else {
					value = mdwObj.getMedical().getDescription();
				}
			}
			if (obj instanceof PricesOthers) {
				PricesOthers mdwObj = (PricesOthers) obj;
				if (columnIndex == 0) {
					value = mdwObj.getCode() != null ? mdwObj.getCode() : String.valueOf(mdwObj.getId());
				} else {
					value = mdwObj.getDescription();
				}
			}

			if (obj instanceof BillItems) {
				BillItems mdwObj = (BillItems) obj;
				if (columnIndex == 0) {
					value = mdwObj.getItemDisplayCode() != null ? mdwObj.getItemDisplayCode() : String.valueOf(mdwObj.getId());
				} else {
					value = mdwObj.getItemDescription();
				}
			}

		}
		return value;
	}

	public T getObjectAt(int rowIndex) {
		if (rowIndex >= 0 && rowIndex < this.filteredList.size()) {
			return this.filteredList.get(rowIndex);
		}
		return null;
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return false;
	}

	@Override
	public void removeTableModelListener(TableModelListener l) {
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
	}

	public String getSearchQuery() {
		return searchQuery;
	}

}
