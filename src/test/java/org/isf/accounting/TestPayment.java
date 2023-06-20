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
package org.isf.accounting;

import org.isf.accounting.model.Bill;
import org.isf.accounting.model.BillPayments;

public class TestPayment {

	public static BillPayments withAmountAndBill(double amount, Bill bill) {
		BillPayments billPayments = new BillPayments();
		billPayments.setAmount(amount);
		billPayments.setBill(bill);
		return billPayments;
	}

	public static BillPayments withAmountBillAndUser(double amount, Bill bill, String user) {
		BillPayments billPayments = withAmountAndBill(amount, bill);
		billPayments.setUser(user);
		return billPayments;
	}

}
