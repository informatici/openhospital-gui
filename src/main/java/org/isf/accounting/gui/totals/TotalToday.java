package org.isf.accounting.gui.totals;

import org.isf.accounting.model.BillPayments;
import org.isf.generaldata.GeneralData;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Optional;

public class TotalToday {
    private final Collection<Integer> notDeletedBills;
    private final Collection<BillPayments> paymentsToday;

    public TotalToday(Collection<Integer> notDeletedBills, Collection<BillPayments> paymentsToday) {
        this.notDeletedBills = notDeletedBills;
        this.paymentsToday = paymentsToday;
    }

    public BigDecimal getTotalsToday() {
        return Optional.ofNullable(paymentsToday)
                .map(this::calculateTotalFromTodaysPayments)
                .orElse(BigDecimal.ZERO);
    }

    private BigDecimal calculateTotalFromTodaysPayments(Collection<BillPayments> paymentsToday) {
        return paymentsToday.stream()
                .filter(payment -> notDeletedBills.contains(payment.getBill().getId()))
                .map(payment -> new BigDecimal(Double.toString(payment.getAmount())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
