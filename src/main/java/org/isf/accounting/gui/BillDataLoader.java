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
        Map<Integer, Bill> billsSortedById;
        List<Bill> tableArray = new ArrayList<>();
        List<Bill> billAll = new ArrayList<>();

        /*
         * Mappings Bills in the period
         */
        billsSortedById = billPeriod.stream()
                .collect(Collectors.toMap(Bill::getId, bill -> bill, (a, b) -> b));

        /*
         * Merging the two bills lists
         */
        billAll.addAll(billPeriod);
        billFromPayments.stream()
                .filter(bill -> billsSortedById.get(bill.getId()) == null)
                .forEach(billAll::add);

        if (status.equals("O")) {
            if (patientParent != null) {
                tableArray = billManager.getPendingBillsAffiliate(patientParent.getCode());
            } else {
                billPeriod.stream()
                        .filter(bill -> bill.getStatus().equals(status))
                        .forEach(tableArray::add);
            }
        }
        else if (status.equals("ALL")) {

            Collections.sort(billAll);
            tableArray = billAll;

        }
        else if (status.equals("C")) {
            billPeriod.stream()
                    .filter(bill -> bill.getStatus().equals(status))
                    .forEach(tableArray::add);
        }

        Collections.sort(tableArray, Collections.reverseOrder());
        return tableArray;
    }
}
