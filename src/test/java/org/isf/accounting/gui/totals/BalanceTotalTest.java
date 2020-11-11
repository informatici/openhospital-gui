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
package org.isf.accounting.gui.totals;

import org.isf.accounting.TestBill;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Arrays;

import static org.junit.Assert.*;

public class BalanceTotalTest {
    @Test
    public void shouldCalculateBalanceForWholePeriod() {
        // given:
        BalanceTotal balanceTotal = new BalanceTotal(Arrays.asList(
                TestBill.notDeletedBillWithBalance(1,123),
                TestBill.notDeletedBillWithBalance(2,111)
        ));

        // when:
        BigDecimal result = balanceTotal.getValue();

        // then:
        assertEquals(234, result.longValue());
    }

    @Test
    public void shouldSkipDeletedBills() {
        // given:
        BalanceTotal balanceTotal = new BalanceTotal(Arrays.asList(
                TestBill.notDeletedBillWithBalance(1,123),
                TestBill.deletedBillWithBalance(2,111)
        ));

        // when:
        BigDecimal result = balanceTotal.getValue();

        // then:
        assertEquals(123, result.longValue());
    }
}