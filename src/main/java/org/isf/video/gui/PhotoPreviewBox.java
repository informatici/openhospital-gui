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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;

public class PhotoPreviewBox extends Box {

	//private static ArrayList<JButton> lstPhotoPreviewsButton = new ArrayList<JButton>();
	//private static ArrayList<String> lstPhotoPaths = new ArrayList<String>();

	private static final long serialVersionUID = 1L;
	public JButton photoButton;
	private ImageIcon previewIcon;
	
	public String path;
	public String resolution;

	public PhotoPreviewBox(String path, String resolution)	{
		//lstPhotoPaths.add(path);
		super(BoxLayout.Y_AXIS) ;
		
		this.path = path;
		this.resolution = resolution;
		
		Box.createVerticalBox();
		//box.setLayout(new GridLayout(2,1));
		this.setMaximumSize(new Dimension(100, 100));
		
		photoButton = null;
		
		try	{
			Image img = ImageIO.read(new File(path));
			
			previewIcon = new ImageIcon(img.getScaledInstance(80, 63, Image.SCALE_SMOOTH));
			photoButton = new JButton("", previewIcon);
			
			photoButton.setBackground(Color.white);
			photoButton.setPreferredSize(new Dimension(90,90));			
		}
		catch (IOException ioe)	{
			System.out.println("Path: " + path);
			ioe.printStackTrace();
		}
		
		this.add(photoButton);
		
		//lstPhotoPreviewsButton.add(photoButton);
	}
}
