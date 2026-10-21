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
package org.isf.medicalstock.gui;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.isf.medicals.model.Medical;
import org.isf.medicalstock.model.Lot;
import org.isf.medicalstock.model.Movement;
import org.isf.medicalstock.model.MovementDraft;
import org.isf.medicalstock.model.MovementDraftKind;
import org.isf.medicalstock.model.MovementDraftRow;
import org.isf.medstockmovtype.model.MovementType;
import org.isf.medtype.model.MedicalType;
import org.isf.supplier.model.Supplier;
import org.isf.ward.model.Ward;
import org.junit.jupiter.api.Test;

class MovStockDraftMapperTest {

	private static final LocalDateTime DATE = LocalDateTime.of(2026, 5, 20, 10, 30);
	private static final LocalDateTime PREPARATION_DATE = LocalDateTime.of(2026, 5, 1, 0, 0);
	private static final LocalDateTime DUE_DATE = LocalDateTime.of(2027, 5, 1, 23, 59);

	private Medical medical = new Medical(1, new MedicalType("M", "Medicine"), "PROD1", "Test Medical", 10, 5.0, 0.0, 0.0);
	private MovementType chargeType = new MovementType("inp", "Charge", "+", "operational");
	private Supplier supplier = new Supplier(7, "Test Supplier", null, null, null, null, null, null);
	private Ward ward = new Ward("Z", "Test Ward", null, null, null, 10, 2, 1, false, false, true, true);

	@Test
	void shouldMapWizardHeaderToNewDraft() {
		// when:
		MovementDraft draft = MovStockDraftMapper.toDraft(null, MovementDraftKind.charge, chargeType, DATE, "REF1", supplier, null);

		// then:
		assertThat(draft.getId()).isNull();
		assertThat(draft.getKind()).isEqualTo(MovementDraftKind.charge.toString());
		assertThat(draft.getType()).isSameAs(chargeType);
		assertThat(draft.getDate()).isEqualTo(DATE);
		assertThat(draft.getRefNo()).isEqualTo("REF1");
		assertThat(draft.getSupplier()).isSameAs(supplier);
		assertThat(draft.getWard()).isNull();
	}

	@Test
	void shouldUpdateExistingDraftHeaderKeepingItsIdentity() {
		// given:
		MovementDraft existingDraft = new MovementDraft(42, MovementDraftKind.charge.toString(), chargeType, DATE, "OLD", supplier, null);

		// when:
		MovementDraft draft = MovStockDraftMapper.toDraft(existingDraft, MovementDraftKind.discharge, null, null, "NEW", null, ward);

		// then:
		assertThat(draft).isSameAs(existingDraft);
		assertThat(draft.getId()).isEqualTo(42);
		assertThat(draft.getKind()).isEqualTo(MovementDraftKind.discharge.toString());
		assertThat(draft.getType()).isNull();
		assertThat(draft.getDate()).isNull();
		assertThat(draft.getRefNo()).isEqualTo("NEW");
		assertThat(draft.getSupplier()).isNull();
		assertThat(draft.getWard()).isSameAs(ward);
	}

	@Test
	void shouldMapGridRowToDraftRowKeepingQuantityAsTyped() {
		// given:
		Lot lot = new Lot(medical, "LOT1", PREPARATION_DATE, DUE_DATE, new BigDecimal("2.50"));
		Movement movement = new Movement(medical, chargeType, null, lot, DATE, 3, supplier, "REF1");

		// when: 3 packets of 10 pieces each must stay 3, NOT become 30
		MovementDraftRow draftRow = MovStockDraftMapper.toDraftRow(movement, 1, true, false);

		// then:
		assertThat(draftRow.getMedical()).isSameAs(medical);
		assertThat(draftRow.getQuantity()).isEqualTo(3);
		assertThat(draftRow.getUnitsOrPackets()).isEqualTo(1);
		assertThat(draftRow.getLotCode()).isEqualTo("LOT1");
		assertThat(draftRow.getLotPreparationDate()).isEqualTo(PREPARATION_DATE);
		assertThat(draftRow.getLotDueDate()).isEqualTo(DUE_DATE);
		assertThat(draftRow.getLotCost()).isEqualTo(new BigDecimal("2.50"));
		assertThat(draftRow.isNewLot()).isTrue();
		assertThat(draftRow.isUpdateLotCost()).isFalse();
	}

	@Test
	void shouldMapGridRowWithAutomaticLotToDraftRow() {
		// given: a discharging automatic lot placeholder
		Lot lot = new Lot(medical, "", null, null);
		Movement movement = new Movement(medical, chargeType, null, lot, DATE, 5, null, "");

		// when:
		MovementDraftRow draftRow = MovStockDraftMapper.toDraftRow(movement, 0, false, false);

		// then:
		assertThat(draftRow.getLotCode()).isEmpty();
		assertThat(draftRow.getLotPreparationDate()).isNull();
		assertThat(draftRow.getLotDueDate()).isNull();
		assertThat(draftRow.getLotCost()).isNull();
		assertThat(draftRow.getQuantity()).isEqualTo(5);
		assertThat(draftRow.getUnitsOrPackets()).isZero();
	}

	@Test
	void shouldMapGridRowWithoutLotToDraftRow() {
		// given:
		Movement movement = new Movement(medical, chargeType, null, null, DATE, 5, supplier, "REF1");

		// when:
		MovementDraftRow draftRow = MovStockDraftMapper.toDraftRow(movement, 0, false, false);

		// then:
		assertThat(draftRow.getLotCode()).isNull();
		assertThat(draftRow.getLotPreparationDate()).isNull();
		assertThat(draftRow.getLotDueDate()).isNull();
		assertThat(draftRow.getLotCost()).isNull();
	}

	@Test
	void shouldRebuildNewLotFromDraftRow() {
		// given:
		MovementDraftRow draftRow = new MovementDraftRow(null, null, medical, 3, 1, "LOT1", PREPARATION_DATE, DUE_DATE, new BigDecimal("2.50"), true, false);

		// when:
		Lot lot = MovStockDraftMapper.toNewLot(draftRow);

		// then:
		assertThat(lot.getMedical()).isSameAs(medical);
		assertThat(lot.getCode()).isEqualTo("LOT1");
		assertThat(lot.getPreparationDate()).isEqualTo(PREPARATION_DATE);
		assertThat(lot.getDueDate()).isEqualTo(DUE_DATE);
		assertThat(lot.getCost()).isEqualTo(new BigDecimal("2.50"));
	}

	@Test
	void shouldRebuildAutomaticNewLotFromDraftRowWithNullCode() {
		// given:
		MovementDraftRow draftRow = new MovementDraftRow(null, null, medical, 3, 0, null, null, null, null, true, false);

		// when:
		Lot lot = MovStockDraftMapper.toNewLot(draftRow);

		// then: a null code becomes the empty code of an automatic lot
		assertThat(lot.getCode()).isEmpty();
		assertThat(lot.getPreparationDate()).isNull();
		assertThat(lot.getDueDate()).isNull();
		assertThat(lot.getCost()).isNull();
	}
}
