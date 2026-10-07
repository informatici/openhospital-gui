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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.isf.medicalinventory.model.MedicalInventoryRow;
import org.isf.medicals.model.Medical;
import org.isf.medicalstock.model.Lot;
import org.junit.jupiter.api.Test;

class InventoryRowChangeTrackerTest {

	private InventoryRowChangeTracker changeTracker = new InventoryRowChangeTracker();

	@Test
	void shouldKeyRowByMedicalCodeAndLot() {
		MedicalInventoryRow row = inventoryRow("MED1", "LOT1", 10); // given

		String key = changeTracker.rowKey(row); // when

		assertThat(key).isEqualTo("MED1|LOT1"); // then
	}

	@Test
	void shouldSnapshotTheoreticQtyByRowKey() {
		List<MedicalInventoryRow> rows = List.of(inventoryRow("MED1", "LOT1", 10), inventoryRow("MED2", "LOT2", 5)); // given

		Map<String, Double> snapshot = changeTracker.snapshotTheoreticQty(rows); // when

		assertThat(snapshot).containsExactlyInAnyOrderEntriesOf(Map.of("MED1|LOT1", 10.0, "MED2|LOT2", 5.0)); // then
	}

	@Test
	void shouldFlagRowWhoseTheoreticQtyChanged() {
		Map<String, Double> before = Map.of("MED1|LOT1", 10.0); // given
		List<MedicalInventoryRow> current = List.of(inventoryRow("MED1", "LOT1", 7));

		Set<String> changed = changeTracker.findChangedRowKeys(before, current); // when

		assertThat(changed).containsExactlyInAnyOrder("MED1|LOT1"); // then
	}

	@Test
	void shouldFlagNewlyAddedRow() {
		Map<String, Double> before = Map.of("MED1|LOT1", 10.0); // given
		List<MedicalInventoryRow> current = List.of(inventoryRow("MED1", "LOT1", 10), inventoryRow("MED2", "LOT2", 5));

		Set<String> changed = changeTracker.findChangedRowKeys(before, current); // when

		assertThat(changed).containsExactlyInAnyOrder("MED2|LOT2"); // then
	}

	@Test
	void shouldNotFlagUnchangedRows() {
		Map<String, Double> before = Map.of("MED1|LOT1", 10.0, "MED2|LOT2", 5.0); // given
		List<MedicalInventoryRow> current = List.of(inventoryRow("MED1", "LOT1", 10), inventoryRow("MED2", "LOT2", 5));

		Set<String> changed = changeTracker.findChangedRowKeys(before, current); // when

		assertThat(changed).isEmpty(); // then
	}

	private MedicalInventoryRow inventoryRow(String medicalCode, String lotCode, double theoreticQty) {
		Medical medical = new Medical();
		medical.setProdCode(medicalCode);
		MedicalInventoryRow row = new MedicalInventoryRow();
		row.setMedical(medical);
		row.setLot(new Lot(lotCode));
		row.setTheoreticQty(theoreticQty);
		return row;
	}
}
