package org.isf.video.gui;

import org.isf.video.manager.VideoDeviceStreamApplet;

import javax.swing.*;
import java.awt.*;

class PhotoPanel extends JPanel {

	private static final long serialVersionUID = 7684416938326266810L;
	
	public Image img;
	public VideoDeviceStreamApplet applet;
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

	public void removeApplet () {
		if (this.applet != null)
		{
			remove (this.applet);
			repaint();
		}
	}
	
	public void updatePhoto(Image img) {
		
		removeApplet();
		
		this.img = img;
		
		if (this.img != null)	{
			dimension = new Dimension(img.getWidth(null), img.getHeight(null));
			refreshPanel(dimension);
		}
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		int x = (getWidth() - dimension.width) / 2;
		int y = (getHeight() - dimension.height) / 2;
		if (img != null) {
			g.drawImage(img, x, y, null);
		}
	}	
}