package org.isf.accounting.gui.totals;

import org.isf.accounting.TestBill;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Arrays;

import static org.junit.Assert.*;

public class BalanceTodayTest {
    @Test
    public void shouldCalculateBalance() {
        // given:
        BalanceToday BalanceToday = new BalanceToday(Arrays.asList(
                TestBill.notDeletedBillWithBalance(1,123),
                TestBill.notDeletedBillWithBalance(2,111)
        ));

        // when:
        BigDecimal result = BalanceToday.getBalanceToday();

        // then:
        assertEquals(234, result.longValue());
    }

    @Test
    public void shouldSkipDeletedBills() {
        // given:
        BalanceToday BalanceToday = new BalanceToday(Arrays.asList(
                TestBill.notDeletedBillWithBalance(1,123),
                TestBill.deletedBillWithBalance(2,111)
        ));

        // when:
        BigDecimal result = BalanceToday.getBalanceToday();

        // then:
        assertEquals(123, result.longValue());
    }
}