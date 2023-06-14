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
package org.isf.admission.gui.ward;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JComboBox;

import org.isf.admission.model.Admission;
import org.isf.patient.model.Patient;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.ward.manager.WardBrowserManager;
import org.isf.ward.model.Ward;

public class WardComboBoxInitializer {

	private final JComboBox<Ward> wardBox;
	private final WardBrowserManager wardBrowserManager;
	private final Patient patient;
	private final Ward saveWard;
	private final boolean editing;
	private final Admission admission;

	public WardComboBoxInitializer(JComboBox wardBox, WardBrowserManager wardBrowserManager, Patient patient, Ward saveWard, boolean editing,
			Admission admission) {
		this.wardBox = wardBox;
		this.wardBrowserManager = wardBrowserManager;
		this.patient = patient;
		this.saveWard = saveWard;
		this.editing = editing;
		this.admission = admission;
	}

	public void initialize() {
		wardBox.addItem(null);
		List<Ward> wardList = fetchWards();
		populateWardBoxWithElements(wardList);
		selectItem(wardList);
	}

	private void selectItem(List<Ward> wardList) {
		wardList.forEach(ward -> {
			if (saveWard != null) {
				if (saveWard.getCode().equalsIgnoreCase(ward.getCode())) {
					wardBox.setSelectedItem(ward);
				}
			} else if (editing) {
				if (admission.getWard().getCode().equalsIgnoreCase(ward.getCode())) {
					wardBox.setSelectedItem(ward);
				}
			}
		});
	}

	private void populateWardBoxWithElements(List<Ward> wardList) {
		wardList.stream()
				.filter(ward -> !(isFemale(patient) && !ward.isFemale()))
				.filter(ward -> !(isMale(patient) && !ward.isMale()))
				.filter(ward -> ward.getBeds() > 0)
				.forEach(wardBox::addItem);
	}

	private List<Ward> fetchWards() {
		try {
			return wardBrowserManager.getWards();
		} catch (OHServiceException e) {
			OHServiceExceptionUtil.showMessages(e);
			return new ArrayList<>();
		}
	}

	private boolean isFemale(Patient patient) {
		return (String.valueOf(patient.getSex())).equalsIgnoreCase("F");
	}

	private boolean isMale(Patient patient) {
		return (String.valueOf(patient.getSex())).equalsIgnoreCase("M");
	}

}
