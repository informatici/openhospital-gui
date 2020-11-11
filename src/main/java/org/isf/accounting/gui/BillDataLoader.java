/*
 * Open Hospital (www.open-hospital.org)
 * Copyright © 2006-2020 Informatici Senza Frontiere (info@informaticisenzafrontiere.org)
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
package org.isf.accounting.gui;

import org.isf.accounting.manager.BillBrowserManager;
import org.isf.accounting.model.Bill;
import org.isf.patient.model.Patient;
import org.isf.utils.exception.OHServiceException;

import java.util.*;
import java.util.stream.Collectors;

public class BillDataLoader {
    private final List<Bill> billPeriod;
    private final List<Bill> billFromPayments;
    private final Patient patientParent;
    private final BillBrowserManager billManager;

    public BillDataLoader(List<Bill> billPeriod,
                          List<Bill> billFromPayments,
                          Patient patientParent,
                          BillBrowserManager billManager) {
        this.billPeriod = billPeriod;
        this.billFromPayments = billFromPayments;
        this.patientParent = patientParent;
        this.billManager = billManager;
    }

    public List<Bill> loadBills(String status) throws OHServiceException {
        List<Bill> tableArray = new ArrayList<>();

        switch (status) {
            case "O":
                tableArray = getPendingBills(status);
                break;
            case "ALL":
                tableArray = getAllBills();
                break;
            case "C":
                tableArray = getClosedBills(status);
                break;
        }

        Collections.sort(tableArray, Collections.reverseOrder());
        return tableArray;
    }

    private List<Bill> getAllBills() {
        List<Bill> billAll = mergeBillsFromPeriodAndFromPayments();
        Collections.sort(billAll);
        return billAll;
    }

    private List<Bill> mergeBillsFromPeriodAndFromPayments() {
        List<Bill> billAll = new ArrayList<>();
        Map<Integer, Bill> billsSortedById = billPeriod.stream()
                .collect(Collectors.toMap(Bill::getId, bill -> bill, (a, b) -> b));
        billAll.addAll(billPeriod);
        billFromPayments.stream()
                .filter(bill -> billsSortedById.get(bill.getId()) == null)
                .forEach(billAll::add);
        return billAll;
    }

    private List<Bill> getClosedBills(String status) {
        return billPeriod.stream()
                .filter(bill -> bill.getStatus().equals(status))
                .collect(Collectors.toList());
    }

    private List<Bill> getPendingBills(String status) throws OHServiceException {
        return patientParent != null ? billManager.getPendingBillsAffiliate(patientParent.getCode()) :
                billPeriod.stream()
                .filter(bill -> bill.getStatus().equals(status))
                .collect(Collectors.toList());
    }
}
