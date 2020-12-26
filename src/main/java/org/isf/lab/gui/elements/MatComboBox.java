package org.isf.lab.gui.elements;

import org.isf.lab.model.Laboratory;

import javax.swing.*;
import java.util.List;
import java.util.function.Function;

public class MatComboBox extends JComboBox {
    private MatComboBox() {

    }

    public static MatComboBox withMaterialsAndMaterialFromLabSelected(List<String> materials, Laboratory lab, boolean insert, Function<String, String> translator) {
        MatComboBox matComboBox = new MatComboBox();
        matComboBox.addItem("");
        materials.forEach(matComboBox::addItem);
        if (!insert) {
            try {
                matComboBox.setSelectedItem(translator.apply(lab.getMaterial()));
            } catch (Exception e) {
            }
        }
        return matComboBox;
    }

}
