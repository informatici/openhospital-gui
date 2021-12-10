/*
 * Open Hospital (www.open-hospital.org)
 * Copyright © 2006-2020 Informatici Senza Frontiere (info@informaticisenzafrontiere.org)
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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.isf.admission.gui.ward;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import javax.swing.JComboBox;

import org.isf.admission.gui.TestAdmission;
import org.isf.admission.gui.TestWard;
import org.isf.admission.model.Admission;
import org.isf.patient.model.Patient;
import org.isf.utils.exception.OHServiceException;
import org.isf.ward.manager.WardBrowserManager;
import org.isf.ward.model.Ward;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class WardComboBoxInitializerTest {

	private WardBrowserManager wardBrowserManager;
	private JComboBox wardComboBox;

	@BeforeEach
	public void setUp() {
		wardBrowserManager = new WardBrowserManagerStub();
		wardComboBox = new JComboBox();
	}

	@Test
	public void shouldSkipFemaleWardForMalePatient() throws OHServiceException {
		// given:
		Patient patient = new Patient();
		patient.setSex('M');
		wardBrowserManager.newWard(TestWard.maleWardWithBeds("1"));
		wardBrowserManager.newWard(TestWard.femaleWardWithBeds("2"));

		// when:
		new WardComboBoxInitializer(
				wardComboBox,
				wardBrowserManager,
				patient,
				null,
				false,
				TestAdmission.withAdmAndDisDate(LocalDateTime.now(), LocalDateTime.now())
		).initialize();

		// then:
		assertThat(wardComboBox.getItemCount()).isEqualTo(2);
		assertThat(wardComboBox.getItemAt(0)).isEqualTo("");
		assertThat(((Ward) wardComboBox.getItemAt(1)).isMale()).isTrue();
	}

	@Test
	public void shouldSkipWardWithoutBeds() throws OHServiceException {
		// given:
		Patient patient = new Patient();
		patient.setSex('M');
		wardBrowserManager.newWard(TestWard.maleWardWithoutBeds("1"));
		wardBrowserManager.newWard(TestWard.maleWardWithoutBeds("2"));
		wardBrowserManager.newWard(TestWard.maleWardWithBeds("3"));

		// when:
		new WardComboBoxInitializer(
				wardComboBox,
				wardBrowserManager,
				patient,
				null,
				false,
				TestAdmission.withAdmAndDisDate(LocalDateTime.now(), LocalDateTime.now())
		).initialize();

		// then:
		assertThat(wardComboBox.getItemCount()).isEqualTo(2);
		assertThat(wardComboBox.getItemAt(0)).isEqualTo("");
		assertThat(((Ward) wardComboBox.getItemAt(1)).getBeds()).isEqualTo(1);
	}

	@Test
	public void shouldSkipMaleWardForFemalePatient() throws OHServiceException {
		// given:
		Patient patient = new Patient();
		patient.setSex('F');
		wardBrowserManager.newWard(TestWard.maleWardWithBeds("1"));
		wardBrowserManager.newWard(TestWard.femaleWardWithBeds("2"));

		// when:
		new WardComboBoxInitializer(
				wardComboBox,
				wardBrowserManager,
				patient,
				null,
				false,
				TestAdmission.withAdmAndDisDate(LocalDateTime.now(), LocalDateTime.now())
		).initialize();

		// then:
		assertThat(wardComboBox.getItemCount()).isEqualTo(2);
		assertThat(wardComboBox.getItemAt(0)).isEqualTo("");
		assertThat(((Ward) wardComboBox.getItemAt(1)).isFemale()).isTrue();
	}

	@Test
	public void shouldSelectRecentlySavedWard() throws OHServiceException {
		// given:
		Patient patient = new Patient();
		patient.setSex('F');
		wardBrowserManager.newWard(TestWard.maleWardWithBeds("1"));
		Ward recentlySavedWard = TestWard.femaleWardWithBeds("2");
		wardBrowserManager.newWard(recentlySavedWard);

		// when:
		new WardComboBoxInitializer(
				wardComboBox,
				wardBrowserManager,
				patient,
				recentlySavedWard,
				false,
				TestAdmission.withAdmAndDisDate(LocalDateTime.now(), LocalDateTime.now())
		).initialize();

		// then:
		assertThat(((Ward) wardComboBox.getSelectedItem()).getCode()).isEqualTo(recentlySavedWard.getCode());
	}

	@Test
	public void shouldSelectWardFromAdmissionWhenEditing() throws OHServiceException {
		// given:
		Patient patient = new Patient();
		patient.setSex('M');
		Ward editedWard = TestWard.maleWardWithBeds("1");
		wardBrowserManager.newWard(editedWard);
		wardBrowserManager.newWard(TestWard.femaleWardWithBeds("2"));
		Admission admission = TestAdmission.withAdmAndDisDateAndWard(LocalDateTime.now(),
				LocalDateTime.now(),
				editedWard);

		// when:
		new WardComboBoxInitializer(
				wardComboBox,
				wardBrowserManager,
				patient,
				null,
				true,
				admission
		).initialize();

		// then:
		assertThat(((Ward) wardComboBox.getSelectedItem()).getCode()).isEqualTo(editedWard.getCode());
	}

}
