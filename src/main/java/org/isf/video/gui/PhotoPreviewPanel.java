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
package org.isf.video.gui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PhotoPreviewPanel extends JPanel	{

	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = LoggerFactory.getLogger(PhotoPreviewPanel.class);

	private Image img;
	
	public PhotoPreviewPanel(String path)	{
		try	{
			img = ImageIO.read(new File(path));
		}
		catch (IOException ioe)	{
			LOGGER.error("Path: {}", path);
			LOGGER.error(ioe.getMessage(), ioe);
		}
		
		setPreferredSize(new Dimension(img.getWidth(null), img.getHeight(null)));
	}
	
	//override paint method of panel
	public void paint(Graphics g)	{
		//draw the image
		if (img != null)	{
			g.drawImage(img, 0, 0, this);
		}
	}
}
