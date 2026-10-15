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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.isf.medicalinventory.model.MedicalInventoryRow;

/**
 * Computes which inventory rows are new or have a changed theoretical quantity after a validation/actualize step, so the
 * inventory table can highlight them. The logic is kept out of the Swing screens ({@code InventoryEdit} and
 * {@code InventoryWardEdit}) so it can be shared between them and unit-tested in isolation.
 */
public class InventoryRowChangeTracker {

	/**
	 * Snapshots the theoretical quantity of each row, keyed by {@link #rowKey(MedicalInventoryRow)}, to be compared after
	 * the inventory is actualized.
	 */
	public Map<String, Double> snapshotTheoreticQty(List<MedicalInventoryRow> rows) {
		Map<String, Double> snapshot = new HashMap<>();
		for (MedicalInventoryRow invRow : rows) {
			snapshot.put(rowKey(invRow), invRow.getTheoreticQty());
		}
		return snapshot;
	}

	/**
	 * Returns the keys of the rows that were added (absent from the snapshot) or whose theoretical quantity changed with
	 * respect to {@code theoreticQtyBefore}.
	 */
	public Set<String> findChangedRowKeys(Map<String, Double> theoreticQtyBefore, List<MedicalInventoryRow> currentRows) {
		Set<String> changedRowKeys = new HashSet<>();
		for (MedicalInventoryRow invRow : currentRows) {
			String key = rowKey(invRow);
			Double previousQty = theoreticQtyBefore.get(key);
			if (previousQty == null || Double.compare(previousQty, invRow.getTheoreticQty()) != 0) {
				changedRowKeys.add(key);
			}
		}
		return changedRowKeys;
	}

	/**
	 * Row identity used for the before/after diff; assumes one row per (medical, lot), which the validate/actualize path
	 * guarantees by requiring every row to carry a lot.
	 */
	public String rowKey(MedicalInventoryRow invRow) {
		String medicalCode = invRow.getMedical() != null ? invRow.getMedical().getProdCode() : "";
		String lotCode = invRow.getLot() != null ? invRow.getLot().getCode() : "";
		return medicalCode + '|' + lotCode;
	}
}
