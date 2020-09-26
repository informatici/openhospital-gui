package org.isf.accounting.gui;

import org.isf.accounting.TestBill;
import org.isf.accounting.manager.BillBrowserManager;
import org.isf.accounting.model.Bill;
import org.isf.patient.model.Patient;
import org.isf.utils.exception.OHServiceException;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class BillDataLoaderTest {
    @Test
    public void shouldLoadPendingBillsFromManagerForParentPatient() throws OHServiceException {
        // given:
        Patient patientParent = new Patient();
        patientParent.setCode(1);
        BillDataLoader billDataLoader = new BillDataLoader(
                Collections.emptyList(),
                Collections.emptyList(),
                patientParent,
                new BillBrowserManager() {
                    @Override
                    public ArrayList<Bill> getPendingBillsAffiliate(int patID) throws OHServiceException {
                        return new ArrayList<>(Arrays.asList(
                                TestBill.notDeletedBillWithStatus(1, "O"),
                                TestBill.notDeletedBillWithStatus(2, "O"),
                                TestBill.notDeletedBillWithStatus(3, "O")
                        ));
                    }
                }
        );

        // when:
        List<Bill> result = billDataLoader.loadBills("O");

        // then:
        assertEquals(3, result.size());
    }

    @Test
    public void shouldLoadPendingBillsFromPeriodOnly() throws OHServiceException {
        // given:
        BillDataLoader billDataLoader = new BillDataLoader(
                Arrays.asList(
                        TestBill.notDeletedBillWithStatus(1, "C"),
                        TestBill.notDeletedBillWithStatus(2, "O")
                ),
                Arrays.asList(
                        TestBill.notDeletedBillWithStatus(1, "C"),
                        TestBill.notDeletedBillWithStatus(3, "O")
                ),
                null,
                new BillBrowserManager()
        );

        // when:
        List<Bill> result = billDataLoader.loadBills("O");

        // then:
        assertEquals(1, result.size());
    }

    @Test
    public void shouldLoadAllBillsMergedWithBillsFromPaymentWithoutDuplicates() throws OHServiceException {
        // given:
        BillDataLoader billDataLoader = new BillDataLoader(
                Arrays.asList(
                        TestBill.notDeletedBillWithStatus(1, "O"),
                        TestBill.notDeletedBillWithStatus(2, "C")
                ),
                Arrays.asList(
                        TestBill.notDeletedBillWithStatus(1, "0"),
                        TestBill.notDeletedBillWithStatus(3, "C")
                ),
                null,
                new BillBrowserManager()
        );

        // when:
        List<Bill> result = billDataLoader.loadBills("ALL");

        // then:
        assertEquals(3, result.size());
    }

    @Test
    public void shouldLoadClosedBillFromGivenPeriod() throws OHServiceException {
        // given:
        BillDataLoader billDataLoader = new BillDataLoader(
                Arrays.asList(
                        TestBill.notDeletedBillWithStatus(1, "O"),
                        TestBill.notDeletedBillWithStatus(2, "C")
                ),
                Arrays.asList(
                        TestBill.notDeletedBillWithStatus(1, "0"),
                        TestBill.notDeletedBillWithStatus(3, "C")
                ),
                null,
                new BillBrowserManager()
        );

        // when:
        List<Bill> result = billDataLoader.loadBills("C");

        // then:
        assertEquals(1, result.size());
    }


}