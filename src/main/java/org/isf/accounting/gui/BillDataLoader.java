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
package org.isf.accounting.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.isf.accounting.manager.BillBrowserManager;
import org.isf.accounting.model.Bill;
import org.isf.patient.model.Patient;
import org.isf.utils.exception.OHServiceException;

public class BillDataLoader {

	private final List<Bill> billPeriod;
	private final List<Bill> billFromPayments;
	private final Patient patientParent;
	private final BillBrowserManager billManager;

	public BillDataLoader(List<Bill> billPeriod, List<Bill> billFromPayments, Patient patientParent, BillBrowserManager billManager) {
		this.billPeriod = billPeriod;
		this.billFromPayments = billFromPayments;
		this.patientParent = patientParent;
		this.billManager = billManager;
	}

	public List<Bill> loadBills(String status, String username) throws OHServiceException {
		List<Bill> tableArray = new ArrayList<>();

		switch (status) {
			case "O":
				tableArray = getPendingBills(status, username);
				break;
			case "ALL":
				tableArray = getAllBills(username);
				break;
			case "C":
				tableArray = getClosedBills(status, username);
				break;
		}

		tableArray.sort(Collections.reverseOrder());
		return tableArray;
	}

	private List<Bill> getAllBills(String username) {
		List<Bill> billAll = mergeBillsFromPeriodAndFromPayments();
		if (username != null) {
			billAll = billAll.stream().filter(bill-> bill.getUser().equals(username)).collect(Collectors.toList());
		}
		Collections.sort(billAll);
		return billAll;
	}

	private List<Bill> mergeBillsFromPeriodAndFromPayments() {
		Map<Integer, Bill> billsSortedById = billPeriod.stream()
				.collect(Collectors.toMap(Bill::getId, bill -> bill, (a, b) -> b));
		List<Bill> billAll = new ArrayList<>(billPeriod);
		billFromPayments.stream()
				.filter(bill -> billsSortedById.get(bill.getId()) == null)
				.forEach(billAll::add);
		return billAll;
	}

	private List<Bill> getClosedBills(String status, String username) {
		List<Bill> 	list = billPeriod.stream()
				.filter(bill -> bill.getStatus().equals(status))
				.collect(Collectors.toList());
		if (username != null) {
			list = list.stream().filter(bill-> bill.getUser().equals(username)).collect(Collectors.toList());
		}
		return list;
	}

	private List<Bill> getPendingBills(String status, String username) throws OHServiceException {
		if (patientParent != null) {
			return  billManager.getPendingBillsAffiliate(patientParent.getCode()) ; 
		}
		List<Bill> list = billPeriod.stream()
					.filter(bill -> bill.getStatus().equals(status))
					.collect(Collectors.toList());
		if (username != null) {
			list = list.stream().filter(bill-> bill.getUser().equals(username)).collect(Collectors.toList());
		}
		return list;
		
	}

}
