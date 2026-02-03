/*
 * Open Hospital (www.open-hospital.org)
 * Copyright © 2006-2025 Informatici Senza Frontiere (info@informaticisenzafrontiere.org)
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
package org.isf.medicalinventory.gui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.table.DefaultTableModel;

import org.isf.generaldata.MessageBundle;
import org.isf.medicals.model.Medical;
import org.isf.utils.db.NormalizeString;

public class StockMedModel extends DefaultTableModel {

	private static final long serialVersionUID = 1L;
	private List<Medical> medList;
	private List<Medical> initList = new ArrayList<>();

	public StockMedModel(List<Medical> meds) {
		medList = meds;
		initList.addAll(medList);
	}

	@Override
	public int getRowCount() {
		if (medList == null) {
			return 0;
		}
		return medList.size();
	}

	@Override
	public String getColumnName(int c) {
		if (c == 0) {
			return MessageBundle.getMessage("angal.common.code.txt").toUpperCase();
		}
		if (c == 1) {
			return MessageBundle.getMessage("angal.common.description.txt").toUpperCase();
		}
		return "";
	}

	@Override
	public int getColumnCount() {
		return 2;
	}

	@Override
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

	public void filter(String searchValue) {
		medList.clear();
		for (Iterator<Medical> iterator = initList.iterator(); iterator.hasNext();) {
			Medical med = iterator.next();
			if (med.getProdCode().trim().equalsIgnoreCase(searchValue.trim())) {
				medList.add(med);
			} else if (NormalizeString.normalizeContains(med.getProdCode().toLowerCase().trim() + med.getDescription().toLowerCase(),
				searchValue.toLowerCase().trim())) {
				medList.add(med);
			}
		}
	}

	public Medical getMedicalAtRow(int row) {
		if (medList.size() > row && row >= 0) {
			return medList.get(row);
		}
		return null;
	}

	@Override
	public boolean isCellEditable(int arg0, int arg1) {
		return false;
	}

}