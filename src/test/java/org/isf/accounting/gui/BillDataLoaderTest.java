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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.isf.accounting.TestBill;
import org.isf.accounting.manager.BillBrowserManager;
import org.isf.accounting.model.Bill;
import org.isf.patient.model.Patient;
import org.isf.utils.exception.OHServiceException;
import org.junit.jupiter.api.Test;

class BillDataLoaderTest {
	
	private static final String NO_USERNAME = null;

    @Test
    void shouldLoadPendingBillsFromManagerForParentPatient() throws OHServiceException {
        // given:
        Patient patientParent = new Patient();
        patientParent.setCode(1);
        BillDataLoader billDataLoader = new BillDataLoader(
                Collections.emptyList(),
                Collections.emptyList(),
                patientParent,
                new BillBrowserManager() {
                    @Override
                    public List<Bill> getPendingBillsAffiliate(int patID) throws OHServiceException {
                        return new ArrayList<>(Arrays.asList(
                                TestBill.notDeletedBillWithStatus(1, "O"),
                                TestBill.notDeletedBillWithStatus(2, "O"),
                                TestBill.notDeletedBillWithStatus(3, "O")
                        ));
                    }
                }
        );

        // when:
        List<Bill> result = billDataLoader.loadBills("O", NO_USERNAME);

        // then:
        assertThat(result).hasSize(3);
    }

    @Test
    void shouldLoadPendingBillsFromPeriodOnly() throws OHServiceException {
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
        List<Bill> result = billDataLoader.loadBills("O", NO_USERNAME);

        // then:
        assertThat(result).hasSize(1);
    }

    @Test
    void shouldLoadAllBillsMergedWithBillsFromPaymentWithoutDuplicates() throws OHServiceException {
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
        List<Bill> result = billDataLoader.loadBills("ALL", NO_USERNAME);

        // then:
        assertThat(result).hasSize(3);
    }

    @Test
    void shouldLoadClosedBillFromGivenPeriod() throws OHServiceException {
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
        List<Bill> result = billDataLoader.loadBills("C", NO_USERNAME);

        // then:
        assertThat(result).hasSize(1);
    }


}