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

import org.isf.admission.gui.TestAdmission;
import org.isf.admission.gui.TestWard;
import org.isf.admission.model.Admission;
import org.isf.patient.model.Patient;
import org.isf.utils.exception.OHServiceException;
import org.isf.ward.manager.WardBrowserManager;
import org.isf.ward.model.Ward;
import org.junit.Before;
import org.junit.Test;

import javax.swing.JComboBox;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class WardComboBoxInitializerTest {
    private WardBrowserManager wardBrowserManager;
    private JComboBox wardComboBox;

    @Before
    public void setUp() {
        wardBrowserManager = new WardBrowserManagerStub();
        wardComboBox = new JComboBox();
    }

    @Test
    public void shouldSkipFemaleWardForMalePatient() throws OHServiceException {
        // given:
        Patient patient = new Patient();
        patient.setSex("M".charAt(0));
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
        assertEquals(2, wardComboBox.getItemCount());
        assertEquals("", wardComboBox.getItemAt(0));
        assertTrue(((Ward) wardComboBox.getItemAt(1)).isMale());
    }

    @Test
    public void shouldSkipWardWithoutBeds() throws OHServiceException {
        // given:
        Patient patient = new Patient();
        patient.setSex("M".charAt(0));
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
        assertEquals(2, wardComboBox.getItemCount());
        assertEquals("", wardComboBox.getItemAt(0));
        assertEquals(new Integer(1), ((Ward) wardComboBox.getItemAt(1)).getBeds());
    }

    @Test
    public void shouldSkipMaleWardForFemalePatient() throws OHServiceException {
        // given:
        Patient patient = new Patient();
        patient.setSex("F".charAt(0));
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
        assertEquals(2, wardComboBox.getItemCount());
        assertEquals("", wardComboBox.getItemAt(0));
        assertTrue(((Ward) wardComboBox.getItemAt(1)).isFemale());
    }

    @Test
    public void shouldSelectRecentlySavedWard() throws OHServiceException {
        // given:
        Patient patient = new Patient();
        patient.setSex("F".charAt(0));
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
        assertEquals(recentlySavedWard.getCode(), ((Ward) wardComboBox.getSelectedItem()).getCode());
    }

    @Test
    public void shouldSelectWardFromAdmissionWhenEditing() throws OHServiceException {
        // given:
        Patient patient = new Patient();
        patient.setSex("M".charAt(0));
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
        assertEquals(editedWard.getCode(), ((Ward) wardComboBox.getSelectedItem()).getCode());
    }

}