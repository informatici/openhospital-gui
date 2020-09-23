package org.isf.accounting.gui.totals;

import org.isf.accounting.model.Bill;

import java.math.BigDecimal;
import java.util.Collection;

public class BalanceToday {
    private final Collection<Bill> billToday;

    public BalanceToday(Collection<Bill> billToday) {
        this.billToday = billToday;
    }

    public BigDecimal getBalanceToday() {
        return billToday.stream()
                .filter(bill -> !bill.getStatus().equals("D"))
                .map(bill -> new BigDecimal(Double.toString(bill.getBalance())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
