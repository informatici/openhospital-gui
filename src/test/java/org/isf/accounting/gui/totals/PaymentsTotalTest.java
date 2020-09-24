package org.isf.accounting.gui.totals;

import org.isf.accounting.TestBill;
import org.isf.accounting.TestPayment;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Arrays;

import static org.junit.Assert.*;

public class PaymentsTotalTest {

    @Test
    public void shouldCalculateFromPayments() {
        // given:
        PaymentsTotal PaymentsTotal = new PaymentsTotal(
                Arrays.asList(1, 2),
                Arrays.asList(
                        TestPayment.withAmountAndBill(10, TestBill.notDeletedBillWithBalance(1, 1)),
                        TestPayment.withAmountAndBill(15, TestBill.notDeletedBillWithBalance(2, 1))
                )
        );

        // when:
        BigDecimal result = PaymentsTotal.getValue();

        // then:
        assertEquals(25, result.longValue());
    }

    @Test
    public void shouldSkipPaymentsForDeletedBills() {
        // given:
        PaymentsTotal PaymentsTotal = new PaymentsTotal(
                Arrays.asList(1),
                Arrays.asList(
                        TestPayment.withAmountAndBill(10, TestBill.notDeletedBillWithBalance(1, 1)),
                        TestPayment.withAmountAndBill(15, TestBill.deletedBillWithBalance(2, 1))
                )
        );

        // when:
        BigDecimal result = PaymentsTotal.getValue();

        // then:
        assertEquals(10, result.longValue());
    }
}