package org.isf.lab.gui.elements;

import org.isf.generaldata.MessageBundle;
import org.isf.lab.model.Laboratory;
import org.isf.patient.model.Patient;

import javax.swing.*;
import java.util.List;
import java.util.Optional;

public class PatientComboBox extends JComboBox {
    public Optional<Patient> getSelectedPatient() {
        return Optional.ofNullable(getSelectedItem())
                .filter(selectedItem -> selectedItem instanceof Patient)
                .map(selectedItem -> (Patient) selectedItem);
    }


    public static PatientComboBox withPatientsAndPatientFromLaboratorySelected(List<Patient> patients, Laboratory laboratory, boolean inserting) {
        PatientComboBox patientComboBox = new PatientComboBox();
        patientComboBox.addItem(MessageBundle.getMessage("angal.lab.selectapatient"));
        Optional.ofNullable(patients).ifPresent(patients2 ->
                patients2.stream()
                        .peek(patient -> patientComboBox.addItem(patient))
                        .filter(patient -> (laboratory.getPatient() != null && !inserting) && patient.getCode().equals(laboratory.getPatient().getCode()))
                        .forEach(patient -> patientComboBox.setSelectedItem(patient))
        );
        return patientComboBox;
    }

    public void addPatientsFilteredByKey(List<Patient> patients, String key) {
        for (Patient elem : patients) {
            if (key != null) {
                //Search key extended to name and code
                StringBuilder sbName = new StringBuilder();
                sbName.append(elem.getSecondName().toUpperCase());
                sbName.append(elem.getFirstName().toUpperCase());
                sbName.append(elem.getCode());
                String name = sbName.toString();

                if (name.toLowerCase().contains(key.toLowerCase())) {
                    this.addItem(elem);
                }
            } else {
                this.addItem(elem);
            }
        }
    }

}
