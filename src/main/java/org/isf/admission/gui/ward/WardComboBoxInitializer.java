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

import org.isf.admission.model.Admission;
import org.isf.patient.model.Patient;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.ward.manager.WardBrowserManager;
import org.isf.ward.model.Ward;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class WardComboBoxInitializer {
    private final JComboBox wardBox;
    private final WardBrowserManager wardBrowserManager;
    private final Patient patient;
    private final Ward saveWard;
    private final boolean editing;
    private final Admission admission;
    private List<Ward> wardList;

    public WardComboBoxInitializer(JComboBox wardBox,
                                   WardBrowserManager wardBrowserManager,
                                   Patient patient,
                                   Ward saveWard,
                                   boolean editing, Admission admission) {
        this.wardBox = wardBox;
        this.wardBrowserManager = wardBrowserManager;
        this.patient = patient;
        this.saveWard = saveWard;
        this.editing = editing;
        this.admission = admission;
        initialize();
    }

    public void initialize() {
        wardBox.addItem("");
        try {
            wardList = wardBrowserManager.getWards();
        }catch(OHServiceException e){
            wardList = new ArrayList<>();
            OHServiceExceptionUtil.showMessages(e);
        }
        for (Ward ward : wardList) {
            // if patient is a male you don't see pregnancy case
            if (("" + patient.getSex()).equalsIgnoreCase("F") && !ward.isFemale()) {
                continue;
            } else if (("" + patient.getSex()).equalsIgnoreCase("M") && !ward.isMale()) {
                continue;
            } else {
                if (ward.getBeds() > 0)
                    wardBox.addItem(ward);
            }
            if (saveWard != null) {
                if (saveWard.getCode().equalsIgnoreCase(ward.getCode())) {
                    wardBox.setSelectedItem(ward);
                }
            } else if (editing) {
                if (admission.getWard().getCode().equalsIgnoreCase(ward.getCode())) {
                    wardBox.setSelectedItem(ward);
                }
            }
        }
    }
}
