package org.isf.accounting;

import org.isf.accounting.model.Bill;
import org.isf.accounting.model.BillPayments;

public class TestPayment {
    public static BillPayments withAmountAndBill(double amount, Bill bill) {
        BillPayments billPayments = new BillPayments();
        billPayments.setAmount(amount);
        billPayments.setBill(bill);
        return billPayments;
    }

    public static BillPayments withAmountBillAndUser(double amount, Bill bill, String user) {
        BillPayments billPayments = withAmountAndBill(amount, bill);
        billPayments.setUser(user);
        return billPayments;
    }
}
