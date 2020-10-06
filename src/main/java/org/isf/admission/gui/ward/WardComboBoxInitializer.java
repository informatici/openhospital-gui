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
