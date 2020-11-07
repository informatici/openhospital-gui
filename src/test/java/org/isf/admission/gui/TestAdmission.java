package org.isf.admission.gui;

import org.isf.admission.model.Admission;

import java.time.LocalDateTime;
import java.util.GregorianCalendar;

public class TestAdmission {
    public static Admission withAdmAndDisDate(LocalDateTime admDate, LocalDateTime disDate) {
        Admission admission = new Admission();
        admission.setAdmDate(admDate);
        admission.setDisDate(disDate);
        return admission;
    }
}
