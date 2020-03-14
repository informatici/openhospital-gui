package org.isf.video.gui;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;

public class PhotoPreviewBox extends Box {

	//private static ArrayList<JButton> lstPhotoPreviewsButton = new ArrayList<JButton>();
	//private static ArrayList<String> lstPhotoPaths = new ArrayList<String>();
	
	/**
	 * 
	 */
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
