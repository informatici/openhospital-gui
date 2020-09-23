package org.isf.accounting.gui.totals;

import org.isf.accounting.model.BillPayments;
import org.isf.generaldata.GeneralData;

import java.math.BigDecimal;
import java.util.Collection;

public class UserPeriod {
    private final Collection<Integer> notDeletedBills;
    private final Collection<BillPayments> paymentsFromPeriod;
    private final String user;

    public UserPeriod(Collection<Integer> notDeletedBills, Collection<BillPayments> paymentsFromPeriod, String user) {
        this.notDeletedBills = notDeletedBills;
        this.paymentsFromPeriod = paymentsFromPeriod;
        this.user = user;
    }

    public BigDecimal getUserPaymentsFromPeriod() {
        return paymentsFromPeriod.stream()
                .filter(payment -> notDeletedBills.contains(payment.getBill().getId()))
                .filter(payment -> !GeneralData.SINGLEUSER && payment.getUser().equals(user))
                .map(payment -> new BigDecimal(Double.toString(payment.getAmount())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
