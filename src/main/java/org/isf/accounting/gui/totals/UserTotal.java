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
package org.isf.accounting.gui.totals;

import java.math.BigDecimal;
import java.util.Collection;

import org.isf.accounting.model.BillPayments;

public class UserTotal {

	private final Collection<Integer> notDeletedBills;
	private final Collection<BillPayments> paymentsFromPeriod;
	private final String user;

	public UserTotal(Collection<Integer> notDeletedBills, Collection<BillPayments> paymentsFromPeriod, String user) {
		this.notDeletedBills = notDeletedBills;
		this.paymentsFromPeriod = paymentsFromPeriod;
		this.user = user;
	}

	public BigDecimal getValue() {
		return paymentsFromPeriod.stream()
				.filter(payment -> notDeletedBills.contains(payment.getBill().getId()))
				.filter(payment -> payment.getUser().equals(user))
				.map(payment -> new BigDecimal(Double.toString(payment.getAmount())))
				.reduce(BigDecimal.ZERO, BigDecimal::add);
	}

}
