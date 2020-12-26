package org.isf.lab.gui.elements;

import org.isf.lab.model.Laboratory;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;

public class MatComboBoxTest {
    @Test
    public void shouldCreateComboBoxWithMaterialsAndMaterialFromLaboratorySelected() {
        // given:
        List<String> materials = Arrays.asList("mat1translated", "mat2translated");
        Laboratory laboratory = new Laboratory();
        laboratory.setMaterial("mat2");
        Function testTranslator = (text) -> text + "translated";

        // when:
        MatComboBox matComboBox = MatComboBox.withMaterialsAndMaterialFromLabSelected(materials, laboratory, false, testTranslator);

        // then:
        assertEquals(3, matComboBox.getItemCount());
        assertEquals("mat2translated", matComboBox.getSelectedItem());
    }

    @Test
    public void shouldCreateComboBoxWithMaterialsAndNotSelectWhenInsertMode() {
        // given:
        List<String> materials = Arrays.asList("mat1translated", "mat2translated");
        Laboratory laboratory = new Laboratory();
        laboratory.setMaterial("mat2");
        Function testTranslator = (text) -> text + "translated";

        // when:
        MatComboBox matComboBox = MatComboBox.withMaterialsAndMaterialFromLabSelected(materials, laboratory, true, testTranslator);

        // then:
        assertEquals(3, matComboBox.getItemCount());
        assertEquals("", matComboBox.getSelectedItem());
    }

}