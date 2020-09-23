package org.isf.accounting.gui.totals;

import org.isf.accounting.model.BillPayments;
import org.isf.generaldata.GeneralData;

import java.math.BigDecimal;
import java.util.Collection;

public class UserPeriod {
    private final Collection<Integer> notDeletedBills;
    private final Collection<BillPayments> paymentsPeriod;
    private final String user;

    public UserPeriod(Collection<Integer> notDeletedBills, Collection<BillPayments> paymentsPeriod, String user) {
        this.notDeletedBills = notDeletedBills;
        this.paymentsPeriod = paymentsPeriod;
        this.user = user;
    }

    public BigDecimal getUserPeriod() {
        BigDecimal userPeriod = BigDecimal.ZERO;
        for (BillPayments payment : paymentsPeriod) {
            if (notDeletedBills.contains(payment.getBill().getId())) {
                BigDecimal payAmount = new BigDecimal(Double.toString(payment.getAmount()));
                String payUser = payment.getUser();
                if (!GeneralData.SINGLEUSER && payUser.equals(user)) {
                    userPeriod = userPeriod.add(payAmount);
                }
            }
        }
        return userPeriod;
    }
}
