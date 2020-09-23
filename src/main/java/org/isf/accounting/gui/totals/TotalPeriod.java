package org.isf.accounting.gui.totals;

import org.isf.accounting.model.BillPayments;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TotalPeriod {
    private final Collection<Integer> notDeletedBills;
    private final Collection<BillPayments> paymentsPeriod;

    public TotalPeriod(Collection<Integer> notDeletedBills,
                       Collection<BillPayments> paymentsPeriod) {
        this.notDeletedBills = notDeletedBills;
        this.paymentsPeriod = paymentsPeriod;
    }

    public BigDecimal getTotalPeriod() {
        return paymentsPeriod.stream()
                .filter(payment -> notDeletedBills.contains(payment.getBill().getId()))
                .map(payment -> new BigDecimal(Double.toString(payment.getAmount())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
