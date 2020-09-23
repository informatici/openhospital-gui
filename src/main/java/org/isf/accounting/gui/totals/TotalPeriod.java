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
        BigDecimal totalPeriod = new BigDecimal(0);
        for (BillPayments payment : paymentsPeriod) {
            if (notDeletedBills.contains(payment.getBill().getId())) {
                BigDecimal payAmount = new BigDecimal(Double.toString(payment.getAmount()));
                totalPeriod = totalPeriod.add(payAmount);
            }
        }
        return totalPeriod;
    }
}
