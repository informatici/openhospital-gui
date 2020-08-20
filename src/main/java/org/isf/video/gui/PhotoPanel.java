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