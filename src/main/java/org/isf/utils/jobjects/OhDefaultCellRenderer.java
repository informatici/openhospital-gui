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
package org.isf.utils.jobjects;

import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * @author u2g
 */
public class OhDefaultCellRenderer extends DefaultTableCellRenderer {

	private static final long serialVersionUID = 1L;
	Color darkOrange = new Color(159, 188, 208);
	Color lightOrange = new Color(231, 236, 240);
	Color lightGray = new Color(242, 242, 242);
	int hoveredRow = -1;

	List<Integer> centeredColumns = new ArrayList<>();

	public OhDefaultCellRenderer(List<Integer> centeredColumns) {
		super();
		this.centeredColumns = centeredColumns;
	}

	public OhDefaultCellRenderer() {
		super();
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		Component cmp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

		JLabel lbl = (JLabel) cmp;

		boolean found = false;
		for (Integer centered : this.centeredColumns) {
			if (centered == column) {

				lbl.setHorizontalAlignment(CENTER);
				found = true;
				break;
			}

		}

		if (!found) {
			lbl.setHorizontalAlignment(LEFT);
		}
		if (isSelected) {
			cmp.setBackground(darkOrange);
		} else {
			if (row % 2 == 0) {
				cmp.setBackground(lightGray);
			} else {
				cmp.setBackground(lightOrange);
			}
		}
		if (row == hoveredRow) {
			cmp.setBackground(darkOrange);
		}
		return cmp;
	}

	public void setHoveredRow(int hoveredRow) {
		this.hoveredRow = hoveredRow;
	}

}
