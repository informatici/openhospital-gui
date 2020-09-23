package org.isf.accounting.gui.totals;

import org.isf.accounting.model.BillPayments;
import org.isf.generaldata.GeneralData;

import java.math.BigDecimal;
import java.util.Collection;

public class UserToday {
    private final Collection<Integer> notDeletedBills;
    private final Collection<BillPayments> paymentsToday;
    private final String user;

    public UserToday(Collection<Integer> notDeletedBills, Collection<BillPayments> paymentsToday, String user) {
        this.notDeletedBills = notDeletedBills;
        this.paymentsToday = paymentsToday;
        this.user = user;
    }

    public BigDecimal getUserToday() {
        return paymentsToday.stream()
                .filter(payment -> notDeletedBills.contains(payment.getBill().getId()))
                .filter(payment -> !GeneralData.SINGLEUSER && payment.getUser().equals(user))
                .map(payment -> new BigDecimal(Double.toString(payment.getAmount())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
