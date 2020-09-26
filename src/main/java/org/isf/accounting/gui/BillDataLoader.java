package org.isf.accounting.gui;

import org.isf.accounting.manager.BillBrowserManager;
import org.isf.accounting.model.Bill;
import org.isf.patient.model.Patient;
import org.isf.utils.exception.OHServiceException;

import java.util.*;

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



    public BillData loadData(String status) throws OHServiceException {
        Map<Integer, Bill> mapBill = new HashMap<>();
        List<Bill> tableArray = new ArrayList<>();
        List<Bill> billAll = new ArrayList<>();


        /*
         * Mappings Bills in the period
         */
        for (Bill bill : billPeriod) {
            //mapBill.clear();
            mapBill.put(bill.getId(), bill);
        }

        /*
         * Merging the two bills lists
         */
        billAll.addAll(billPeriod);
        for (Bill bill : billFromPayments) {
            if (mapBill.get(bill.getId()) == null)
                billAll.add(bill);
        }

        if (status.equals("O")) {
            if (patientParent != null) {
                tableArray = billManager.getPendingBillsAffiliate(patientParent.getCode());
            } else {
                if (status.equals("O")) {
                    for (Bill bill : billPeriod) {

                        if (bill.getStatus().equals(status))
                            tableArray.add(bill);
                    }
                }
            }
        }
        else if (status.equals("ALL")) {

            Collections.sort(billAll);
            tableArray = billAll;

        }
        else if (status.equals("C")) {

            for (Bill bill : billPeriod) {

                if (bill.getStatus().equals(status))
                    tableArray.add(bill);
            }
        }

        Collections.sort(tableArray, Collections.reverseOrder());
        return new BillData(mapBill, billAll, tableArray);
    }

    static class BillData {
        private final Map<Integer, Bill> mapBill;
        private final List<Bill> billAll;
        private final List<Bill> tableArray;

        BillData(Map<Integer, Bill> mapBill, List<Bill> billAll, List<Bill> tableArray) {
            this.mapBill = mapBill;
            this.billAll = billAll;
            this.tableArray = tableArray;
        }

        public List<Bill> getTableArray() {
            return tableArray;
        }
    }
}
