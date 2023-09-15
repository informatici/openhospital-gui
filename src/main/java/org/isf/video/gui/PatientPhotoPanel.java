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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.BufferedImage;
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
import javax.swing.WindowConstants;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.isf.generaldata.GeneralData;
import org.isf.generaldata.MessageBundle;
import org.isf.patient.gui.PatientInsertExtended;
import org.isf.utils.image.ImageUtil;
import org.isf.utils.jobjects.Cropping;
import org.isf.utils.jobjects.IconButton;
import org.isf.utils.jobjects.MessageDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.sarxos.webcam.Webcam;

public class PatientPhotoPanel extends JPanel {

	private static final long serialVersionUID = 9129641275344016618L;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(PatientPhotoPanel.class);
	private static final String PROFILE_PICTURE_FORMAT = "png";
	// Photo Components:
	private JPanel jPhotoPanel;
	private PhotoPanel externalPanel;
	private PatientInsertExtended owner;

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
			btnDeletePhoto.addActionListener(actionEvent -> {

				int answer = MessageDialog.yesNo(owner, "angal.patient.doyouwanttodeletethepatientsphoto.msg");
				if (answer == JOptionPane.YES_OPTION) {
					btnDeletePhoto.setVisible(false);
					patientFrame.setPatientPhoto(null);
					externalPanel.updatePhoto(nophoto);
					LOGGER.debug(MessageBundle.getMessage("angal.patient.photodeleted"));
				} else {
					LOGGER.debug(MessageBundle.getMessage("angal.patient.photonotdeleted"));
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
			photoboothPanelPresentationModel.addBeanPropertyChangeListener(PhotoboothPanelModel.PROPERTY_IMAGE, propertyChangeEvent -> {
				
				try {
					BufferedImage bi = (BufferedImage) propertyChangeEvent.getNewValue();
					if (bi != null) {
						externalPanel.updatePhoto(ImageUtil.scaleImage(bi, 160, 160));
						patientFrame.setPatientPhoto(ImageUtil.fixImageFileSize(bi, GeneralData.MAX_PROFILE_IMAGE_FILE_SIZE_BYTES, PROFILE_PICTURE_FORMAT));
					} 
				} catch (IOException e1) {
					LOGGER.error("Oooops! Can't resize profile picture.", e1);
				}
			});

			box.add(btnDeletePhoto);

			btnDeletePhoto.setVisible(patientHasPhoto);

			final Box buttonBox1 = Box.createHorizontalBox();

			JButton jAttachPhotoButton = new JButton(MessageBundle.getMessage("angal.patientphoto.file.btn"));
			jAttachPhotoButton.setMnemonic(MessageBundle.getMnemonic("angal.patientphoto.file.btn.key"));
			jAttachPhotoButton.setMinimumSize(new Dimension(200, (int) jAttachPhotoButton.getPreferredSize().getHeight()));
			jAttachPhotoButton.setMaximumSize(new Dimension(200, (int) jAttachPhotoButton.getPreferredSize().getHeight()));
			jAttachPhotoButton.addActionListener(actionEvent -> {
				JFileChooser fc = new JFileChooser();
				String[] extensions = { "tif", "tiff", "jpg", "jpeg", "bmp", "png", "gif" };
				FileFilter imageFilter = new FileNameExtensionFilter(MessageBundle.getMessage("angal.patientphoto.imagefiles.txt"), extensions);
				fc.setFileFilter(imageFilter);
				fc.setAcceptAllFileFilterUsed(false);
				int returnVal = fc.showOpenDialog(patientFrame);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File image = fc.getSelectedFile();
					CroppingDialog cropDiag = new CroppingDialog(patientFrame, image);
					cropDiag.pack();
					cropDiag.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
					cropDiag.setLocationRelativeTo(null);
					cropDiag.setVisible(true);

					final Image croppedImage = cropDiag.getCropped();
					if (croppedImage != null) {
						photoboothPanelPresentationModel.setImage(croppedImage);
					}
				}
			});

			final Webcam webcam = Webcam.getDefault();

			if (GeneralData.VIDEOMODULEENABLED && webcam != null) {
				JButton jGetPhotoButton = new JButton(MessageBundle.getMessage("angal.patientphoto.newphoto.btn"));
				jGetPhotoButton.setMnemonic(MessageBundle.getMnemonic("angal.patientphoto.newphoto.btn.key"));
				jGetPhotoButton.setMinimumSize(new Dimension(200, (int) jGetPhotoButton.getPreferredSize().getHeight()));
				jGetPhotoButton.setMaximumSize(new Dimension(200, (int) jGetPhotoButton.getPreferredSize().getHeight()));

				final Dimension[] resolutions = webcam.getDevice().getResolutions();
				jGetPhotoButton.addActionListener(actionEvent -> {
					photoboothPanelPresentationModel.setWebcam(webcam);
					// start with the highest resolution.
					photoboothPanelPresentationModel.setResolution(resolutions[resolutions.length - 1]);

					final PhotoboothDialog photoBoothDialog = new PhotoboothDialog(photoboothPanelPresentationModel, owner);
					photoBoothDialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
					photoBoothDialog.setVisible(true);
					photoBoothDialog.toFront();
					photoBoothDialog.requestFocus();
				});

				buttonBox1.add(jGetPhotoButton);
			} else {
				jAttachPhotoButton.setText(MessageBundle.getMessage("angal.patient.loadfile"));
			}
			buttonBox1.add(jAttachPhotoButton);

			jPhotoPanel.add(externalPanel, BorderLayout.NORTH);
			jPhotoPanel.add(buttonBox1, BorderLayout.CENTER);

			jPhotoPanel.setMinimumSize(new Dimension((int) getPreferredSize().getWidth(), 100));
		}

		add(jPhotoPanel);
	}

	
	private JPanel setMyBorder(JPanel c, String title) {
		Border b1 = BorderFactory.createLineBorder(Color.lightGray);
		Border b2 = BorderFactory.createTitledBorder(b1, title, TitledBorder.LEFT, TitledBorder.TOP);

		c.setBorder(b2);
		return c;
	}
}

class CroppingDialog extends JDialog {

	private static final long serialVersionUID = 1L;

	private static final Logger LOGGER = LoggerFactory.getLogger(CroppingDialog.class);

	/*
	 * Attributes
	 */
	private Cropping crop;
	private File image;
	
	/*
	 * Return Value
	 */
	private BufferedImage cropped;

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
		} catch (IOException ioException) {
			LOGGER.error(ioException.getMessage(), ioException);
		}
	}

	private JButton getSaveButton() {
		if (saveButton == null) {
			saveButton = new JButton(MessageBundle.getMessage("angal.common.save.btn"));
			saveButton.setMnemonic(MessageBundle.getMnemonic("angal.common.save.btn.key"));
			saveButton.addActionListener(actionEvent -> {
				cropped = crop.clipImage();
				dispose();
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
