package org.isf.accounting.gui.totals;

import org.isf.accounting.TestBill;
import org.isf.accounting.TestPayment;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Arrays;

import static org.junit.Assert.*;

public class UserTotalTest {

    public static final String TEST_USER = "testUser";
    public static final String OTHER_USER = "dupaUser";

    @Test
    public void shouldCalculateFromPayments() {
        // given:
        UserTotal userTotal = new UserTotal(
                Arrays.asList(1, 2),
                Arrays.asList(
                        TestPayment.withAmountBillAndUser(10, TestBill.notDeletedBillWithBalance(1, 1), TEST_USER),
                        TestPayment.withAmountBillAndUser(15, TestBill.notDeletedBillWithBalance(2, 1), TEST_USER)
                ),
                TEST_USER
        );

        // when:
        BigDecimal result = userTotal.getValue();

        // then:
        assertEquals(25, result.longValue());
    }

    @Test
    public void shouldSkipPaymentsForDeletedBills() {
        // given:
        UserTotal userTotal = new UserTotal(
                Arrays.asList(1),
                Arrays.asList(
                        TestPayment.withAmountBillAndUser(10, TestBill.notDeletedBillWithBalance(1, 1), TEST_USER),
                        TestPayment.withAmountBillAndUser(15, TestBill.deletedBillWithBalance(2, 1), TEST_USER)
                ),
                TEST_USER
        );

        // when:
        BigDecimal result = userTotal.getValue();

        // then:
        assertEquals(10, result.longValue());
    }

    @Test
    public void shouldSkipPaymentForOtherUser() {
        // given:
        UserTotal userTotal = new UserTotal(
                Arrays.asList(1, 2),
                Arrays.asList(
                        TestPayment.withAmountBillAndUser(10, TestBill.notDeletedBillWithBalance(1, 1), TEST_USER),
                        TestPayment.withAmountBillAndUser(15, TestBill.notDeletedBillWithBalance(2, 1), OTHER_USER)
                ),
                TEST_USER
        );

        // when:
        BigDecimal result = userTotal.getValue();

        // then:
        assertEquals(10, result.longValue());
    }
}