/*
 * Open Hospital (www.open-hospital.org)
 * Copyright © 2006-2024 Informatici Senza Frontiere (info@informaticisenzafrontiere.org)
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
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;

/**
 * @author Mwithi
 */
public class JLabelInfo extends JLabel {

	private static final int defaultInitDelay = ToolTipManager.sharedInstance().getInitialDelay();
	private static final Color defaultBackgroundColor = (Color) UIManager.get("ToolTip.background");

	/**
	 * Creates a {@code JLabelInfo} instance with the specified
	 * tooltip, icon and default background color, delay is set to zero milliseconds
	 * 
	 * @param tooltip
	 * @param icon
	 */
	public JLabelInfo(ImageIcon icon, String tooltip) {
		new JLabelInfo(icon, tooltip, defaultBackgroundColor);
	}

	/**
	 * Creates a {@code JLabelInfo} instance with the specified
	 * tooltip, icon and background color, delay is set to zero milliseconds
	 * 
	 * @param tooltip
	 * @param icon
	 * @param backgroundColor
	 */
	public JLabelInfo(ImageIcon icon, String tooltip, Color backgroundColor) {

		MouseListener ml = new MouseAdapter() {

			@Override
			public void mouseEntered(MouseEvent me) {
				ToolTipManager.sharedInstance().setInitialDelay(0);
				UIManager.put("ToolTip.background", backgroundColor);
			}

			@Override
			public void mouseExited(MouseEvent me) {
				ToolTipManager.sharedInstance().setInitialDelay(defaultInitDelay);
				UIManager.put("ToolTip.background", defaultBackgroundColor);
			}
		};

		addMouseListener(ml);
		setIcon(icon);
		setHorizontalAlignment(SwingConstants.RIGHT);
		setToolTipText(tooltip);
	}

	@Override
	public Point getToolTipLocation(MouseEvent event) {
		return new Point(30, 0);
	}

}
