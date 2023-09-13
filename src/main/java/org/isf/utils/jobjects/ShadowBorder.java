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
import java.awt.Graphics;

import javax.swing.border.AbstractBorder;

/**
 * Custom Border which create a component shadow with specified offset and color
 *
 * @author Mwithi
 */
public class ShadowBorder extends AbstractBorder {

	private static final long serialVersionUID = 1L;
	private Color color;
	private int offset;

	/**
	 * Constructor
	 *
	 * @param aOffset
	 * @param aColor
	 */
	public ShadowBorder(int aOffset, Color aColor) {
		offset = aOffset;
		color = aColor;
	}

	@Override
	public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
		g.setColor(color);
		g.fillRect(x + offset, y + offset, w + offset, h + offset);
	}

	@Override
	public boolean isBorderOpaque() {
		return true;
	}

}
