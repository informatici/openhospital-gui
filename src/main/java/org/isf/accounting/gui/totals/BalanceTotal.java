package org.isf.accounting.gui.totals;

import org.isf.accounting.model.Bill;
import org.isf.accounting.model.BillPayments;
import org.isf.generaldata.GeneralData;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

public class BalanceTotal {
    private final Collection<Bill> billPeriod;

    public BalanceTotal(Collection<Bill> billPeriod) {
        this.billPeriod = billPeriod;
    }

    public BigDecimal getValue() {
        return billPeriod.stream()
                .filter(bill -> !bill.getStatus().equals("D"))
                .map(bill -> new BigDecimal(Double.toString(bill.getBalance())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
