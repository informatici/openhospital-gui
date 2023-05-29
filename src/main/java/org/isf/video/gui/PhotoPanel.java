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
package org.isf.video.gui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;

import javax.swing.JPanel;

class PhotoPanel extends JPanel {

	private static final long serialVersionUID = 7684416938326266810L;

	public Image img;
	private Dimension dimension;

	public PhotoPanel(final Image img) {
		setLayout(null);
		updatePhoto(img);
	}

	private void refreshPanel(Dimension dimension) {
		setPreferredSize(dimension);
		setMinimumSize(dimension);
		setMaximumSize(dimension);
		setSize(dimension);

		repaint();
	}

	public void updatePhoto(Image img) {
		this.img = img;

		if (this.img != null) {
			dimension = new Dimension(img.getWidth(null), img.getHeight(null));
			refreshPanel(dimension);
		}
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		int x = (getWidth() - dimension.width) / 2;
		int y = (getHeight() - dimension.height) / 2;
		if (img != null) {
			g.drawImage(img, x, y, null);
		}
	}
}