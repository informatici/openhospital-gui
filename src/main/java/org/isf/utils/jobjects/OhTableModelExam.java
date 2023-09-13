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

import org.isf.exa.model.Exam;
import org.isf.generaldata.MessageBundle;

public class OhTableModelExam<T> implements TableModel {

	List<Exam> dataListExam;
	List<Exam> filteredList;

	public OhTableModelExam(List<Exam> dataList) {
		this.dataListExam = dataList;

		this.filteredList = new ArrayList<>();

		this.filteredList.addAll(dataList);
	}

	public int filter(String searchQuery) {
		this.filteredList = new ArrayList<>();

		for (Exam value : this.dataListExam) {
			Object object = value;
			if (object != null) {
				Exam exam = (Exam) object;
				String strItem = exam.getCode() + exam.getDescription();
				strItem = strItem.toLowerCase();
				searchQuery = searchQuery.toLowerCase();
				if (strItem.contains(searchQuery)) {
					filteredList.add((Exam) object);
				}
			}
		}
		return filteredList.size();
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
				columnLabel = MessageBundle.getMessage("angal.common.code.txt").toUpperCase();
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
			Exam obj = this.filteredList.get(rowIndex);
			if (obj != null) {
				Exam mdwObj = obj;
				if (columnIndex == 0) {
					value = mdwObj.getCode();
				} else {
					value = mdwObj.getDescription();
				}
			}
		}
		return value;
	}

	public Exam getObjectAt(int rowIndex) {
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

}
