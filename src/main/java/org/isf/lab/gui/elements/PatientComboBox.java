package org.isf.lab.gui.elements;

import org.isf.generaldata.MessageBundle;
import org.isf.lab.model.Laboratory;
import org.isf.patient.model.Patient;

import javax.swing.*;
import java.util.List;

public class PatientComboBox extends JComboBox {
    public static PatientComboBox withPatientsAndPatientFromLaboratorySelected(List<Patient> patients, Laboratory laboratory, boolean inserting) {
        PatientComboBox patientComboBox = new PatientComboBox();
        Patient patSelected = null;

        patientComboBox.addItem(MessageBundle.getMessage("angal.lab.selectapatient"));
        if(patients != null){
            for (Patient elem : patients) {
                if (laboratory.getPatient() != null && !inserting) {
                    if (elem.getCode() == laboratory.getPatient().getCode()) {
                        patSelected = elem;
                    }
                }
                patientComboBox.addItem(elem);
            }
        }
        if (patSelected!=null)
            patientComboBox.setSelectedItem(patSelected);

        return patientComboBox;
    }
}
