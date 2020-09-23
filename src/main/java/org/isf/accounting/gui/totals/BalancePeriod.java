package org.isf.accounting.gui.totals;

import org.isf.accounting.model.Bill;
import org.isf.accounting.model.BillPayments;
import org.isf.generaldata.GeneralData;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

public class BalancePeriod {
    private final Collection<Bill> billPeriod;

    public BalancePeriod(Collection<Bill> billPeriod) {
        this.billPeriod = billPeriod;
    }

    public BigDecimal getBalancePeriod() {
        BigDecimal balancePeriod = new BigDecimal(0);
        for (Bill bill : billPeriod) {
            if (!bill.getStatus().equals("D")) {
                BigDecimal balance = new BigDecimal(Double.toString(bill.getBalance()));
                balancePeriod = balancePeriod.add(balance);
            }
        }
        return balancePeriod;
    }
}
