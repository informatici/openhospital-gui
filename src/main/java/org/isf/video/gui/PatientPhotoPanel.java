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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.isf.generaldata.GeneralData;
import org.isf.generaldata.MessageBundle;
import org.isf.patient.gui.PatientInsertExtended;
import org.isf.utils.image.ImageUtil;
import org.isf.utils.jobjects.Cropping;
import org.isf.utils.jobjects.IconButton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.sarxos.webcam.Webcam;

public class PatientPhotoPanel extends JPanel {

	private static final long serialVersionUID = 9129641275344016618L;
	
	private final Logger logger = LoggerFactory.getLogger(PatientInsertExtended.class);

	// Photo Components:
	private JPanel jPhotoPanel = null;
	private PhotoPanel externalPanel = null;
	private PatientInsertExtended owner = null;
	
	private JButton jGetPhotoButton = null;
	private JButton jAttachPhotoButton = null;

	private final PhotoboothPanelPresentationModel photoboothPanelPresentationModel;

	public PatientPhotoPanel(final PatientInsertExtended patientFrame, final Integer code, final Image patientPhoto) throws IOException {
		owner = patientFrame;
		this.photoboothPanelPresentationModel = new PhotoboothPanelPresentationModel();
		if (jPhotoPanel == null) {
			jPhotoPanel = new JPanel();
			jPhotoPanel = setMyBorder(jPhotoPanel, MessageBundle.getMessage("angal.patient.patientphoto")); //$NON-NLS-1$
			jPhotoPanel.setLayout(new BorderLayout());
			jPhotoPanel.setBackground(null);

			final Image nophoto = ImageIO.read(new File("rsc/images/nophoto.png")); //$NON-NLS-1$

			final IconButton btnDeletePhoto = new IconButton(new ImageIcon("rsc/icons/delete_button.png")); //$NON-NLS-1$
			btnDeletePhoto.setSize(new Dimension(40, 40));
			btnDeletePhoto.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					
					int n = JOptionPane.showConfirmDialog(owner, 
							MessageBundle.getMessage("angal.patient.doyoureallywanttodeletepatientsphoto"),  //$NON-NLS-1$
							MessageBundle.getMessage("angal.patient.confirmdeletion"),  //$NON-NLS-1$
							JOptionPane.YES_NO_OPTION);

					if (n == JOptionPane.YES_OPTION) {
						btnDeletePhoto.setVisible(false);
						patientFrame.setPatientPhoto(null);
						externalPanel.updatePhoto(nophoto);
						logger.debug(MessageBundle.getMessage("angal.patient.photodeleted"));
					} else {
						logger.debug(MessageBundle.getMessage("angal.patient.photonotdeleted"));
						return;
					}
				}
			});

			Image photo = patientPhoto;
			boolean patientHasPhoto = photo != null;

			if (!patientHasPhoto) {
				photo = nophoto;
			}

			externalPanel = new PhotoPanel(photo);
			externalPanel.setLayout(new BorderLayout());
			Box box = Box.createHorizontalBox();
			box.add(Box.createHorizontalGlue());

			externalPanel.add(box, BorderLayout.NORTH);
			photoboothPanelPresentationModel.addBeanPropertyChangeListener(PhotoboothPanelModel.PROPERTY_IMAGE, new PropertyChangeListener() {
				@Override
				public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
					final BufferedImage newImage = (BufferedImage) propertyChangeEvent.getNewValue();
					if (newImage != null) {
						externalPanel.updatePhoto(ImageUtil.scaleImage(newImage, 160, 160));
						patientFrame.setPatientPhoto(newImage);
					}
				}
			});

			box.add(btnDeletePhoto);

			if (patientHasPhoto)
				btnDeletePhoto.setVisible(true);
			else
				btnDeletePhoto.setVisible(false);
			
			final Box buttonBox1 = Box.createHorizontalBox();

			jAttachPhotoButton = new JButton(MessageBundle.getMessage("angal.patient.file"));
			jAttachPhotoButton.setMinimumSize(new Dimension(200, (int) jAttachPhotoButton.getPreferredSize().getHeight()));
			jAttachPhotoButton.setMaximumSize(new Dimension(200, (int) jAttachPhotoButton.getPreferredSize().getHeight()));
			jAttachPhotoButton.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					JFileChooser fc = new JFileChooser();
					String[] extensions = {"tif","tiff","jpg","jpeg","bmp","png","gif"};
					FileFilter imageFilter = new FileNameExtensionFilter("Image files", extensions); //ImageIO.getReaderFileSuffixes());
					fc.setFileFilter(imageFilter);
					fc.setAcceptAllFileFilterUsed(false);
					int returnVal = fc.showOpenDialog(patientFrame);
					if (returnVal == JFileChooser.APPROVE_OPTION) {  
                        File image = fc.getSelectedFile();
                        CroppingDialog cropDiag = new CroppingDialog(patientFrame, image);
                        cropDiag.pack();
                        cropDiag.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                        cropDiag.setLocationRelativeTo(null);
                        cropDiag.setVisible(true);
                        
                        final Image croppedImage = cropDiag.getCropped();
						if (croppedImage != null) {
							photoboothPanelPresentationModel.setImage(croppedImage);
						}
					}
				}
			});

			final Webcam webcam = Webcam.getDefault();

			if (GeneralData.VIDEOMODULEENABLED && webcam != null) {
				jGetPhotoButton = new JButton(MessageBundle.getMessage("angal.patient.newphoto")); //$NON-NLS-1$
				jGetPhotoButton.setMinimumSize(new Dimension(200, (int) jGetPhotoButton.getPreferredSize().getHeight()));
				jGetPhotoButton.setMaximumSize(new Dimension(200, (int) jGetPhotoButton.getPreferredSize().getHeight()));

				final Dimension[] resolutions = webcam.getDevice().getResolutions();
				jGetPhotoButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent event) {
						photoboothPanelPresentationModel.setWebcam(webcam);
						// start with the highest resolution.
						photoboothPanelPresentationModel.setResolution(resolutions[resolutions.length - 1]);

						final PhotoboothDialog photoBoothDialog = new PhotoboothDialog(photoboothPanelPresentationModel, owner);
						photoBoothDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
						photoBoothDialog.setVisible(true);
						photoBoothDialog.toFront();
						photoBoothDialog.requestFocus();
					}
				});

				buttonBox1.add(jGetPhotoButton);
				buttonBox1.add(jAttachPhotoButton);
			} else {
				jAttachPhotoButton.setText(MessageBundle.getMessage("angal.patient.loadfile"));
				buttonBox1.add(jAttachPhotoButton);
			}

			jPhotoPanel.add(externalPanel, BorderLayout.NORTH);
			jPhotoPanel.add(buttonBox1, java.awt.BorderLayout.CENTER);

			jPhotoPanel.setMinimumSize(new Dimension((int) getPreferredSize().getWidth(), 100));
		}
		
		add(jPhotoPanel);
	}

	
	private JPanel setMyBorder(JPanel c, String title) {
		javax.swing.border.Border b1 = BorderFactory.createLineBorder(Color.lightGray);
		/*
		 * javax.swing.border.Border b2 = BorderFactory.createCompoundBorder(
		 * BorderFactory.createTitledBorder(title),null);
		 */
		javax.swing.border.Border b2 = BorderFactory.createTitledBorder(b1, title, javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.TOP);

		c.setBorder(b2);
		return c;
	}
}

class CroppingDialog extends JDialog {

	private static final long serialVersionUID = 1L;
	
	/*
	 * Attributes
	 */
	private Cropping crop;
	private File image;
	
	/*
	 * Return Value
	 */
	private BufferedImage cropped = null;

	private JButton saveButton;

	public CroppingDialog(JDialog owner, File image) {
		super(owner, true);
		this.image = image;
		initComponents();
		
	}

	private void initComponents() {
		try {
			crop = new Cropping(ImageIO.read(image));
			getContentPane().add(crop, BorderLayout.CENTER);
			getContentPane().add(getSaveButton(), BorderLayout.SOUTH);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	private JButton getSaveButton() {
		if (saveButton == null) {
			saveButton = new JButton("save");
			saveButton.addActionListener(new ActionListener() {
				
				public void actionPerformed(ActionEvent e) {
					cropped = crop.clipImage();
					dispose();
				}
			});
		}
		return saveButton;
	}

	/**
	 * @return the cropped
	 */
	public Image getCropped() {
		return cropped;
	}
}
