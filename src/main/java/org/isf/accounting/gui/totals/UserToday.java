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
        BigDecimal userToday = BigDecimal.ZERO;
        if(paymentsToday != null){
            for (BillPayments payment : paymentsToday) {
                if (notDeletedBills.contains(payment.getBill().getId())) {
                    BigDecimal payAmount = new BigDecimal(Double.toString(payment.getAmount()));
                    String payUser = payment.getUser();
                    if (!GeneralData.SINGLEUSER && payUser.equals(user))
                        userToday = userToday.add(payAmount);
                }
            }
        }
        return userToday;
    }
}
