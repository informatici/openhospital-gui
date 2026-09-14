/*
 * Open Hospital (www.open-hospital.org)
 * Copyright © 2006-2026 Informatici Senza Frontiere (info@informaticisenzafrontiere.org)
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

import java.awt.Component;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.function.Supplier;

import javax.swing.JOptionPane;

import org.isf.generaldata.GeneralData;
import org.isf.generaldata.MessageBundle;
import org.isf.medicals.model.Medical;
import org.isf.medicalstock.manager.MovStockInsertingManager;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;

/**
 * Shared lot-cost variance confirmation (OP-1300). When the cost entered for a new lot deviates from the average cost of
 * the previous lots of the medical by more than the configured {@link GeneralData#LOTCOSTVARIANCEPERCENT}, the user is
 * asked to confirm. It is reused by every entry point that prices a new lot: the charging wizard, the ward pharmacy
 * rectify and the inventory dialogs.
 */
public final class LotCostVariance {

	private LotCostVariance() {
	}

	/**
	 * Asks the user for the cost of a new lot through {@code costPrompt} and validates it against the average cost of the previous lots of
	 * {@code medical}: an out-of-variance cost must be confirmed, a refused confirmation re-opens the prompt, and a cancelled or zero cost aborts the
	 * new-lot operation.
	 *
	 * @param parent the dialog parent component
	 * @param movStockInsertingManager used to compute the average lot cost and the variance
	 * @param medical the medical whose previous lots provide the average cost
	 * @param costPrompt the caller's cost prompt, expected to return {@code null} or zero when the user cancels
	 * @return the entered (and, when needed, confirmed) cost, or {@code null} when the user aborted
	 */
	public static BigDecimal askNewLotCost(Component parent, MovStockInsertingManager movStockInsertingManager, Medical medical,
		Supplier<BigDecimal> costPrompt) {
		BigDecimal cost;
		do {
			cost = costPrompt.get();
			if (cost == null || cost.compareTo(BigDecimal.ZERO) == 0) {
				return null;
			}
		} while (!confirm(parent, movStockInsertingManager, medical, cost));
		return cost;
	}

	/**
	 * @param parent the dialog parent component
	 * @param movStockInsertingManager used to compute the average lot cost and the variance
	 * @param medical the medical whose previous lots provide the average cost
	 * @param cost the cost entered for the new lot
	 * @return {@code true} to proceed with the entered cost, {@code false} to re-enter it
	 */
	private static boolean confirm(Component parent, MovStockInsertingManager movStockInsertingManager, Medical medical, BigDecimal cost) {
		try {
			BigDecimal averageCost = movStockInsertingManager.getAverageLotCost(medical);
			if (movStockInsertingManager.isLotCostWithinVariance(cost, averageCost)) {
				return true;
			}
			int answer = JOptionPane.showConfirmDialog(parent,
				MessageBundle.formatMessage("angal.medicalstock.multiplecharging.lotcostvariance.fmt",
					cost.setScale(2, RoundingMode.HALF_UP).toPlainString(), averageCost.toPlainString(), GeneralData.LOTCOSTVARIANCEPERCENT),
				MessageBundle.getMessage("angal.messagedialog.question.title"),
				JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
			return answer == JOptionPane.YES_OPTION;
		} catch (OHServiceException e) {
			OHServiceExceptionUtil.showMessages(e);
			return true; // do not block the user if the average cannot be computed
		}
	}
}
