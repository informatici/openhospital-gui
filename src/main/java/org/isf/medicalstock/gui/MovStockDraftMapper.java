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

import java.time.LocalDateTime;

import org.isf.medicalstock.model.Lot;
import org.isf.medicalstock.model.Movement;
import org.isf.medicalstock.model.MovementDraft;
import org.isf.medicalstock.model.MovementDraftKind;
import org.isf.medicalstock.model.MovementDraftRow;
import org.isf.medstockmovtype.model.MovementType;
import org.isf.supplier.model.Supplier;
import org.isf.ward.model.Ward;

/**
 * Pure mapping between the state held by the charge/discharge wizards
 * ({@link MovStockMultipleCharging}, {@link MovStockMultipleDischarging}) and the
 * persisted draft entities ({@link MovementDraft}, {@link MovementDraftRow}).
 * A draft stores the wizard content exactly as typed, possibly incomplete: no
 * validation and no units/packets multiplication happen here.
 */
public final class MovStockDraftMapper {

	private MovStockDraftMapper() {
	}

	/**
	 * Build (or update) the draft header from the wizard header fields.
	 *
	 * @param currentDraft the draft being edited, or {@code null} to create a new one.
	 * @param kind the wizard kind, {@link MovementDraftKind#charge} or {@link MovementDraftKind#discharge}.
	 * @param type the selected movement type, possibly {@code null}.
	 * @param date the movement date as typed, possibly {@code null}.
	 * @param refNo the reference number as typed, possibly {@code null}.
	 * @param supplier the selected supplier (charge only), possibly {@code null}.
	 * @param ward the selected destination ward (discharge only), possibly {@code null}.
	 * @return the draft header, never {@code null}.
	 */
	public static MovementDraft toDraft(MovementDraft currentDraft, MovementDraftKind kind, MovementType type, LocalDateTime date, String refNo,
		Supplier supplier, Ward ward) {
		MovementDraft draft = currentDraft != null ? currentDraft : new MovementDraft();
		draft.setKind(kind.toString());
		draft.setType(type);
		draft.setDate(date);
		draft.setRefNo(refNo);
		draft.setSupplier(supplier);
		draft.setWard(ward);
		return draft;
	}

	/**
	 * Map one wizard grid row to a draft row. The quantity is stored as typed
	 * (BEFORE any units/packets multiplication) and the lot data is stored
	 * denormalized because the lot may not exist in the database yet.
	 *
	 * @param movement the wizard grid row.
	 * @param unitsOrPackets the units/packets option of the row (0 = units, 1 = packets).
	 * @param newLot {@code true} if the lot was created in the wizard session (charge only).
	 * @param updateLotCost {@code true} if the row carries a pending cost update for an existing lot (charge only).
	 * @return the draft row, never {@code null}.
	 */
	public static MovementDraftRow toDraftRow(Movement movement, int unitsOrPackets, boolean newLot, boolean updateLotCost) {
		MovementDraftRow row = new MovementDraftRow();
		row.setMedical(movement.getMedical());
		row.setQuantity(movement.getQuantity());
		row.setUnitsOrPackets(unitsOrPackets);
		Lot lot = movement.getLot();
		if (lot != null) {
			row.setLotCode(lot.getCode());
			row.setLotPreparationDate(lot.getPreparationDate());
			row.setLotDueDate(lot.getDueDate());
			row.setLotCost(lot.getCost());
		}
		row.setNewLot(newLot);
		row.setUpdateLotCost(updateLotCost);
		return row;
	}

	/**
	 * Rebuild the in-memory (not yet persisted) {@link Lot} of a charging row whose
	 * lot was created in the wizard session, from the denormalized draft row data.
	 *
	 * @param draftRow the draft row, with {@code newLot} set.
	 * @return the rebuilt lot, never {@code null}; an empty code means an automatic lot.
	 */
	public static Lot toNewLot(MovementDraftRow draftRow) {
		String lotCode = draftRow.getLotCode() == null ? "" : draftRow.getLotCode();
		Lot lot = new Lot(draftRow.getMedical(), lotCode, draftRow.getLotPreparationDate(), draftRow.getLotDueDate());
		lot.setCost(draftRow.getLotCost());
		return lot;
	}
}
