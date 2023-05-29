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
import java.util.function.Function;

import javax.swing.JComboBox;

import org.isf.lab.model.Laboratory;

public class MatComboBox extends JComboBox {

	private MatComboBox() {

	}

	public static MatComboBox withMaterialsAndMaterialFromLabSelected(List<String> materials, Laboratory lab, boolean insert,
			Function<String, String> translator) {
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
