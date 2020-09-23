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
        BigDecimal balanceToday = BigDecimal.ZERO;
        for (Bill bill : billToday) {
            if (!bill.getStatus().equals("D")) {
                BigDecimal balance = new BigDecimal(Double.toString(bill.getBalance()));
                balanceToday = balanceToday.add(balance);
            }
        }
        return balanceToday;
    }
}
