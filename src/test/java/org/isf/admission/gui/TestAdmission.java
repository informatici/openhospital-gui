package org.isf.admission.gui;

import org.isf.admission.model.Admission;

import java.util.GregorianCalendar;

public class TestAdmission {
    public static Admission withAdmAndDisDate(GregorianCalendar admDate, GregorianCalendar disDate) {
        Admission admission = new Admission();
        admission.setAdmDate(admDate);
        admission.setDisDate(disDate);
        return admission;
    }
}
