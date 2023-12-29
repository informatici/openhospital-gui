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
package org.isf.utils.layout;

import java.awt.Component;
import java.awt.Container;

import javax.swing.Spring;
import javax.swing.SpringLayout;
import javax.swing.SpringLayout.Constraints;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A file that provides utility methods for creating form- or grid-style layouts with SpringLayout..
 */
public class SpringUtilities {

	private static final Logger LOGGER = LoggerFactory.getLogger(SpringUtilities.class);

	private SpringUtilities() {
		// all static methods; no constructor needed
	}

	private static Constraints getConstraintsForCell(int row, int col, Container parent, int cols) {
		SpringLayout layout = (SpringLayout) parent.getLayout();
		Component c = parent.getComponent(row * cols + col);
		return layout.getConstraints(c);
	}

	/**
	 * Aligns the first {@code rows} * {@code cols} components of {@code parent} in a grid. Each component in a column is as wide as
	 * the maximum preferred width of the components in that column; height is similarly determined for each row.
	 * The parent is made just big enough to fit them all.
	 *
	 * @param rows number of rows
	 * @param cols number of columns
	 * @param initialX x location to start the grid at
	 * @param initialY y location to start the grid at
	 * @param xPad x padding between cells
	 * @param yPad y padding between cells
	 */
	public static void makeCompactGrid(Container parent, int rows, int cols, int initialX, int initialY, int xPad, int yPad) {
		SpringLayout layout;
		try {
			layout = (SpringLayout) parent.getLayout();
		} catch (ClassCastException exc) {
			LOGGER.error("The first argument to makeCompactGrid must be SpringLayout.");
			return;
		}

		// Align all cells in each column and make them the same width.
		Spring x = Spring.constant(initialX);
		for (int c = 0; c < cols; c++) {
			Spring width = Spring.constant(0);
			for (int r = 0; r < rows; r++) {
				width = Spring.max(width, getConstraintsForCell(r, c, parent, cols).getWidth());
			}
			for (int r = 0; r < rows; r++) {
				Constraints constraints = getConstraintsForCell(r, c, parent, cols);
				constraints.setX(x);
				constraints.setWidth(width);
			}
			x = Spring.sum(x, Spring.sum(width, Spring.constant(xPad)));
		}

		// Align all cells in each row and make them the same height.
		Spring y = Spring.constant(initialY);
		for (int r = 0; r < rows; r++) {
			Spring height = Spring.constant(0);
			for (int c = 0; c < cols; c++) {
				height = Spring.max(height, getConstraintsForCell(r, c, parent, cols).getHeight());
			}
			for (int c = 0; c < cols; c++) {
				Constraints constraints = getConstraintsForCell(r, c, parent, cols);
				constraints.setY(y);
				constraints.setHeight(height);
			}
			if (r < rows - 1) {
				y = Spring.sum(y, Spring.sum(height, Spring.constant(yPad)));
			} else {
				y = Spring.sum(y, height);
			}
		}

		// Set the parent's size.
		Constraints pCons = layout.getConstraints(parent);
		pCons.setConstraint(SpringLayout.SOUTH, y);
		pCons.setConstraint(SpringLayout.EAST, x);
	}
}
