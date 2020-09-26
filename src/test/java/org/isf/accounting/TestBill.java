package org.isf.accounting;

import org.isf.accounting.model.Bill;

import java.math.BigDecimal;

public class TestBill {
    public static Bill notDeletedBillWithBalance(int id, double amount) {
        Bill bill = new Bill();
        bill.setId(id);
        bill.setBalance(amount);
        return bill;
    }

    public static Bill notDeletedBillWithStatus(int id, String status) {
        Bill bill = new Bill();
        bill.setId(id);
        bill.setStatus(status);
        bill.setBalance(100d);
        return bill;
    }

    public static Bill deletedBillWithBalance(int id, double amount) {
        Bill bill = new Bill();
        bill.setId(id);
        bill.setBalance(amount);
        bill.setStatus("D");
        return bill;
    }
}
