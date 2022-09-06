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
package org.isf.utils.jobjects;

import static org.isf.utils.Constants.DATE_FORMAT_DD_MM_YYYY;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import org.isf.generaldata.GeneralData;
import org.isf.generaldata.MessageBundle;
import org.isf.menu.manager.Context;
import org.isf.operation.manager.OperationBrowserManager;
import org.isf.operation.model.Operation;
import org.isf.operation.model.OperationRow;
import org.isf.utils.exception.OHServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OhTableOperationModel<T> implements TableModel {

	private static final Logger LOGGER = LoggerFactory.getLogger(OhTableOperationModel.class);

	List<T> dataList;
	List<T> filteredList;
	OperationBrowserManager manageop = Context.getApplicationContext().getBean(OperationBrowserManager.class);
	
	public  OhTableOperationModel(List<T> dataList) {
		this.dataList = dataList;
		this.filteredList = new ArrayList<>();

		if (dataList != null) {
			this.filteredList.addAll(dataList);
		}
	}

	public int filter(String searchQuery) {
		this.filteredList = new ArrayList<>();

		for (T t : this.dataList) {
			Object object = t;
			if (object instanceof OperationRow) {
				OperationRow price = (OperationRow) object;
				String strItem = price.getOperation().getCode() + price.getOpResult();
				strItem = strItem.toLowerCase();
				searchQuery = searchQuery.toLowerCase();
				if (strItem.contains(searchQuery)) {
					filteredList.add((T) object);
				}
			}
		}
		return filteredList.size();
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
		String columnLabel="";
		switch (columnIndex) {
		case 0:
			columnLabel= MessageBundle.getMessage("angal.operationrowlist.date").toUpperCase();
			break;
		case 1:
			columnLabel= MessageBundle.getMessage("angal.operationrowlist.natureop").toUpperCase();
			break;
		case 2:
			columnLabel= MessageBundle.getMessage("angal.common.result.txt").toUpperCase();
			break;
		case 3:
			columnLabel= MessageBundle.getMessage("angal.operationrowedit.unitetrans").toUpperCase();
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
			if (obj instanceof OperationRow) {
				OperationRow opdObj = (OperationRow) obj;
				switch (columnIndex) {
					case -1:
						return opdObj;
					case 0:
						try {
							final DateTimeFormatter currentDateFormat = DateTimeFormatter.ofPattern(DATE_FORMAT_DD_MM_YYYY, new Locale(GeneralData.LANGUAGE));
							value = currentDateFormat.format(opdObj.getOpDate());
						} catch (Exception ex) {
							value = opdObj.getOpDate().toString();
						}
						break;
					case 1:
						Operation ope = null;
						try {
							ope = manageop.getOperationByCode(opdObj.getOperation().getCode());
						} catch (OHServiceException ohServiceException) {
							LOGGER.error(ohServiceException.getMessage(), ohServiceException);
						}
						if (ope != null) {
							value = ope.getDescription();
						} else {
							value = "";
						}
						break;
					case 2:
						value = manageop.getResultDescriptionTranslated(opdObj.getOpResult());
						break;
					case 3:
						value = opdObj.getTransUnit() + "";
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
