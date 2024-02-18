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
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.RasterFormatException;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.event.MouseInputAdapter;

import org.isf.generaldata.MessageBundle;
import org.isf.utils.image.ImageUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Cropping.java - 27/gen/2014
 *
 * @author Internet, Mwithi
 */
public class Cropping extends JPanel {

	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = LoggerFactory.getLogger(Cropping.class);
	
	BufferedImage image;
	Dimension size;
	Rectangle clip;
	BufferedImage clipped;

	public Cropping(BufferedImage image) {
		size = calculateDimension(image);
		this.image = ImageUtil.scaleImage(image, size.width, size.height);
		ClipMoverAndResizer mover = new ClipMoverAndResizer(this);
		this.addMouseListener(mover);
		this.addMouseMotionListener(mover);
	}

	private Dimension calculateDimension(BufferedImage image) {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		final int image_max_width = (int) screenSize.getWidth() - 54;
		final int image_max_height = (int) screenSize.getHeight() - 88;
		
		final int currentWidth = image.getWidth();
		final int currentHeight = image.getHeight();
		
		if (currentWidth > image_max_width || currentHeight > image_max_height) {

			if (currentWidth == currentHeight && currentHeight > image_max_height) {
				return new Dimension(image_max_height, image_max_height);
			}
			
			if (currentWidth > currentHeight) {
				double ratio = (float) currentHeight / currentWidth;
				int newWidth = image_max_width;
				int newHeigth = (int) (newWidth * ratio);
				return new Dimension(newWidth, newHeigth);
			} 
			if (currentHeight > currentWidth) {
				double ratio = (float) currentWidth / currentHeight;
				int newHeight = image_max_height;
				int newWidth = (int) (newHeight * ratio);
				return new Dimension(newWidth, newHeight);
			}
			return new Dimension(currentWidth, currentHeight);
		}
		
		return new Dimension(currentWidth, currentHeight);
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		int x = 0;
		int y = 0;
		g2.drawImage(image, x, y, this);
		if (clip == null) {
			createClip();
		}
		g2.setPaint(Color.red);
		g2.draw(clip);
	}

	public void setClip(int x, int y) {
		// keep clip within raster
		int x0 = (getWidth() - size.width) / 2;
		int y0 = (getHeight() - size.height) / 2;
		if (x < x0 || x + clip.width > x0 + size.width || y < y0 || y + clip.height > y0 + size.height) {
			return;
		}
		clip.setLocation(x, y);
		repaint();
	}

	public void resizeClip(int x, int y) {
		// keep clip within raster
		int x0 = 100;
		int y0 = 100;
		if (x < x0 || x > size.width || y < y0 || y > size.height) {
			return;
		}
		clip.setSize(x, y);
		repaint();
	}

	@Override
	public Dimension getPreferredSize() {
		return size;
	}

	private void createClip() {
		int min = Math.min(size.width, size.height);
		if (min > 160) {
			min = 160;
		}
		clip = new Rectangle(min, min);
		clip.x = (size.width - clip.width) / 2;
		clip.y = (size.height - clip.height) / 2;
	}

	public BufferedImage clipImage() {
		try {
			int w = clip.width;
			int h = clip.height;
			int x0 = 0;
			int y0 = 0;
			int x = clip.x - x0;
			int y = clip.y - y0;
			clipped = image.getSubimage(x, y, w, h);
			return clipped;
		} catch (RasterFormatException rfe) {
			LOGGER.error("raster format error: {}", rfe.getMessage());
			return null;
		}
	}

	public JPanel getUIPanel() {
		JButton saveButton = new JButton(MessageBundle.getMessage("angal.common.save.btn"));
		saveButton.setMnemonic(MessageBundle.getMnemonic("angal.common.save.btn.key"));
		saveButton.addActionListener(actionEvent -> clipImage());
		JPanel panel = new JPanel();
		panel.add(saveButton);
		return panel;
	}

}

class ClipMoverAndResizer extends MouseInputAdapter {

	Cropping cropping;
	Point offset;
	boolean dragging;
	boolean resizing;
	int precision = 10;

	public ClipMoverAndResizer(Cropping c) {
		cropping = c;
		offset = new Point();
		dragging = false;
		resizing = false;
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		Point p = e.getPoint();
		if (Math.abs(cropping.clip.getMaxX() - p.getX()) <= precision &&
				Math.abs(cropping.clip.getMaxY() - p.getY()) <= precision) {
			cropping.setCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
		} else if (cropping.clip.contains(p)) {
			cropping.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
		} else {
			cropping.setCursor(Cursor.getDefaultCursor());
		}
		super.mouseEntered(e);
	}

	@Override
	public void mousePressed(MouseEvent e) {
		Point p = e.getPoint();
		if (Math.abs(cropping.clip.getMaxX() - p.getX()) <= precision &&
				Math.abs(cropping.clip.getMaxY() - p.getY()) <= precision) {
			cropping.setCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
			resizing = true;
		} else if (cropping.clip.contains(p)) {
			offset.x = p.x - cropping.clip.x;
			offset.y = p.y - cropping.clip.y;
			cropping.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
			dragging = true;
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		dragging = false;
		resizing = false;
		cropping.setCursor(Cursor.getDefaultCursor());
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if (dragging) {
			int x = e.getX() - offset.x;
			int y = e.getY() - offset.y;
			cropping.setClip(x, y);
		} else if (resizing) {
			int x = e.getX() - cropping.clip.x;
			cropping.resizeClip(x, x); //square
		}
	}
}
