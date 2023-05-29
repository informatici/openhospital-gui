/*
 * Open Hospital (www.open-hospital.org)
 * Copyright © 2006-2023 Informatici Senza Frontiere (info@informaticisenzafrontiere.org)
 *
 * Open Hospital is a free and open source software for healthcare data management.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * https://www.gnu.org/licenses/gpl-3.0-standalone.html
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.isf.lab.gui.elements;

import java.util.List;
import java.util.Optional;

import javax.swing.JComboBox;

import org.isf.generaldata.MessageBundle;
import org.isf.lab.model.Laboratory;
import org.isf.patient.model.Patient;

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
