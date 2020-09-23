package org.isf.accounting.gui.totals;

import org.isf.accounting.model.BillPayments;
import org.isf.generaldata.GeneralData;

import java.math.BigDecimal;
import java.util.Collection;

public class TotalToday {
    private final Collection<Integer> notDeletedBills;
    private final Collection<BillPayments> paymentsToday;

    public TotalToday(Collection<Integer> notDeletedBills, Collection<BillPayments> paymentsToday) {
        this.notDeletedBills = notDeletedBills;
        this.paymentsToday = paymentsToday;
    }

    public BigDecimal getTotalsToday() {
        BigDecimal totalToday = BigDecimal.ZERO;
        if(paymentsToday != null){
            for (BillPayments payment : paymentsToday) {
                if (notDeletedBills.contains(payment.getBill().getId())) {
                    BigDecimal payAmount = new BigDecimal(Double.toString(payment.getAmount()));
                    totalToday = totalToday.add(payAmount);
                }
            }
        }
        return totalToday;
    }
}
