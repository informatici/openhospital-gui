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
import org.isf.medicals.model.Medical;
import org.isf.medicalstockward.model.MovementWard;

public class OhTableDrugsModel<T> implements TableModel {

	List<T> filteredList;

	public OhTableDrugsModel(List<T> dataList) {
		this.filteredList = new ArrayList<>();

		if (dataList != null) {
			this.filteredList.addAll(dataList);
		}
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
		return 4;
	}

	@Override
	public String getColumnName(int columnIndex) {
		String columnLabel = "";
		switch (columnIndex) {
			case 0:
				columnLabel = MessageBundle.getMessage("angal.medicalstockward.patient.date.col").toUpperCase();
				break;
			case 1:
				columnLabel = MessageBundle.getMessage("angal.medicalstockward.patient.drug.col").toUpperCase();
				break;
			case 2:
				columnLabel = MessageBundle.getMessage("angal.medicalstockward.patient.quantity.col").toUpperCase();
				break;
			case 3:
				columnLabel = MessageBundle.getMessage("angal.medicalstockward.patient.units.col").toUpperCase();
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
			if (obj instanceof MovementWard) {
				MovementWard drugObj = (MovementWard) obj;
				switch (columnIndex) {
					case 0:
						String dt;
						try {
							final DateTimeFormatter currentDateFormat = DateTimeFormatter.ofPattern(DATE_FORMAT_DD_MM_YYYY, new Locale(GeneralData.LANGUAGE));
							dt = currentDateFormat.format(drugObj.getDate());
							value = dt;
						} catch (Exception ex) {
							value = drugObj.getDate().toString();
						}

						break;
					case 1:
						Medical drugsname;
						drugsname = drugObj.getMedical();
						if (drugsname != null) {
							value = drugsname.getDescription();
						} else {
							value = "";
						}
						break;
					case 2:
						value = String.valueOf(drugObj.getQuantity());
						break;
					case 3:
						value = drugObj.getUnits();
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
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
	}

}
