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
package org.isf.dicom.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.imageio.plugins.dcm.DicomImageReadParam;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.util.SafeClose;
import org.imgscalr.Scalr;
import org.isf.dicom.manager.DicomManagerFactory;
import org.isf.dicom.model.FileDicom;
import org.isf.generaldata.MessageBundle;
import org.isf.patient.model.Patient;
import org.isf.utils.exception.OHDicomException;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.model.OHExceptionMessage;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.time.Converters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Detail for DICOM image
 *
 * @author Pietro Castellucci
 * @version 1.0.0
 */
public class DicomViewGui extends JPanel {

	private static final long serialVersionUID = 1L;

	private static final Logger LOGGER = LoggerFactory.getLogger(DicomViewGui.class);

	// status of framereader
	private int patID;
	private Patient ohPatient;
	private String serieNumber;
	private Long[] frames;

	// status of frame
	private int frameIndex;
	private BufferedImage tmpImg;
	private Attributes attributes;
	private FileDicom tmpDbFile;

	private JPanel jPanelCenter;
	private JSlider jSliderZoom;
	private JSlider jSliderFrame;
	
	// GUI parameters
	private int x = -1;
	private int y = -1;
	private int totX = -1;
	private int totY = -1;
	private static final Color colScr = Color.LIGHT_GRAY;
	private static final int VGAP = 15;

	/**
	 * Construct a new detail for DICOM image
	 * 
	 * @param patient
	 * @param serieNumber
	 */
	public DicomViewGui(Patient patient, String serieNumber) {

		this.patID = (patient != null ? (patient.getAge()) : -1);
		this.serieNumber = serieNumber;
		this.ohPatient = patient;
		this.frameIndex = 0;

		addMouseListener(new DicomViewGuiMouseListener());
		addMouseMotionListener(new DicomViewGuiMouseMotionListener());
		addMouseWheelListener(new DicomViewGuiMouseWheelListener());

		if (patID >= 0) {
			try {
				frames = DicomManagerFactory.getManager().getSeriesDetail(patID, serieNumber);
			} catch (OHServiceException ohServiceException) {
				MessageDialog.showExceptions(ohServiceException);
			}
		}

		if (frames == null) {
			frames = new Long[0];
		} else {
			refreshFrame();
		}

		initComponent();

	}

	public void notifyChanges(Patient patient, String serieNumber) {

		this.patID = patient.getCode();
		this.ohPatient = patient;
		this.serieNumber = serieNumber;
		this.frameIndex = 0;

		if (patID >= 0) {
			try {
				frames = DicomManagerFactory.getManager().getSeriesDetail(patID, serieNumber);
			} catch (OHServiceException ohServiceException) {
				MessageDialog.showExceptions(ohServiceException);
			}
		}

		if (frames == null) {
			frames = new Long[0];
		}

		jSliderZoom.setValue(100);

		if (frames.length > 0) {
			refreshFrame();
		}

		reInitComponent();
	}

	/**
	 * Initialize GUI
	 */
	void initComponent() {

		JPanel jPanelHeader = new JPanel();
		JPanel jPanelFooter = new JPanel();

		jSliderFrame = new JSlider(0, 0, 0);
		jSliderZoom = new JSlider(50, 300, 100);
		jSliderFrame.addChangeListener(new FrameListener());
		jSliderZoom.addChangeListener(new ZoomListener());
		jPanelHeader.setBackground(Color.BLACK);

		if (patID <= 0) {
			// center = new JScrollPane();
			jPanelCenter = new JPanel();
			jSliderFrame.setEnabled(false);
			jSliderZoom.setEnabled(false);
		} else {
			jSliderZoom.setEnabled(true);
			jSliderZoom.setPaintTicks(true);
			jSliderZoom.setMajorTickSpacing(10);
			
			if (frames.length > 1) {
				jSliderFrame.setMaximum(frames.length - 1);
				jSliderFrame.setEnabled(true);
				jSliderFrame.setPaintTicks(true);
				jSliderFrame.setMajorTickSpacing(1);
			} else {
				jSliderFrame.setEnabled(false);
			}

			jPanelCenter = new JPanel();

			jPanelCenter.add(composeCenter(jPanelCenter.getWidth(), jPanelCenter.getHeight(), true));
		}

		jPanelCenter.setBackground(Color.BLACK);

		JPanel fp1 = new JPanel();
		JPanel fp2 = new JPanel();
		fp1.setLayout(new BoxLayout(fp1, BoxLayout.Y_AXIS));
		fp2.setLayout(new BoxLayout(fp2, BoxLayout.Y_AXIS));
		fp1.setBorder(new TitledBorder(MessageBundle.getMessage("angal.dicomview.zoom.title")));
		fp2.setBorder(new TitledBorder(MessageBundle.getMessage("angal.dicomview.frames.title")));
		jPanelFooter.setLayout(new GridLayout(1, 2));
		fp1.add(Box.createRigidArea(new Dimension(5, 5)));
		fp1.add(jSliderZoom);
		fp1.add(Box.createRigidArea(new Dimension(5, 5)));
		fp2.add(Box.createRigidArea(new Dimension(5, 5)));
		fp2.add(jSliderFrame);
		fp2.add(Box.createRigidArea(new Dimension(5, 5)));
		jPanelFooter.add(fp1);
		jPanelFooter.add(fp2);
		setLayout(new BorderLayout());
		setBackground(Color.BLACK);
		add(jPanelCenter, BorderLayout.CENTER);
		add(jPanelFooter, BorderLayout.SOUTH);
	}

	void reInitComponent() {
		if (patID <= 0) {
			jPanelCenter = new JPanel();
			jSliderFrame.setEnabled(false);
			jSliderZoom.setEnabled(false);
		} else {
			// reset mouse relative position
			resetMouseRelativePosition();

			jSliderZoom.setEnabled(true);
			jSliderZoom.setPaintTicks(true);
			jSliderZoom.setMajorTickSpacing(10);
			
			if (frames.length > 1) {
				jSliderFrame.setMaximum(frames.length - 1);
				jSliderFrame.setEnabled(true);
				jSliderFrame.setPaintTicks(true);
				jSliderFrame.setMajorTickSpacing(1);
			} else {
				jSliderFrame.setEnabled(false);
			}

			jPanelCenter.removeAll();
			jPanelCenter.add(composeCenter(jPanelCenter.getWidth(), jPanelCenter.getHeight(), true));
			validate();
		}
		jSliderFrame.setValue(0);
		jSliderZoom.setValue(100);
	}

	// DRAWS METHODS

	/**
	 * Compose the panel of central image
	 * 
	 * @return the panel
	 */
	private JPanel composeCenter(int w, int h, boolean calculate) {

		JPanel centerPanel = new JPanel(new BorderLayout(), true);
		BufferedImage imageCanvas = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		centerPanel.setBackground(Color.BLACK);
		int perc = jSliderZoom.getValue();
		float value = (float) tmpImg.getWidth() * (float) perc / 100f;
		BufferedImage immagineResized = Scalr.resize(tmpImg, Math.round(value));

		Graphics2D canvas = (Graphics2D) imageCanvas.getGraphics();

		// design on canvas
		int width = immagineResized.getWidth();
		int height = immagineResized.getHeight();

		if (calculate) {
			x = (w - width) / 2;
			y = (h - height) / 2;
		}

		totX = x - (p1x - p2x);
		totY = y - (p1y - p2y);

		if (totX < -width) {
			totX = -width;
		}
		if (totY < -height) {
			totY = -height;
		}

		if (totX > imageCanvas.getWidth()) {
			totX = imageCanvas.getWidth();
		}

		if (totY > imageCanvas.getHeight()) {
			totY = imageCanvas.getHeight();
		}

		canvas.drawImage(immagineResized, totX, totY, this);
		
		// draws info
		drawPatientUpRight(canvas, imageCanvas.getWidth(), imageCanvas.getHeight());
		drawInfoFrameBottomLeft(canvas, imageCanvas.getWidth(), imageCanvas.getHeight());
		drawStudyUpRight(canvas, imageCanvas.getWidth(), imageCanvas.getHeight());
		drawSerieBottomRight(canvas, imageCanvas.getWidth(), imageCanvas.getHeight());

		JLabel centerImgLabel = new JLabel(new ImageIcon(imageCanvas));
		centerPanel.add(centerImgLabel, BorderLayout.CENTER);

		return centerPanel;
	}

	private void drawQuadrant(Graphics g, int h, int w, Color c) {

		Color original = g.getColor();
		g.setColor(c);
		g.drawLine(0, 0, 0, h - 1);
		g.drawLine(0, 0, w - 1, 0);
		g.drawLine(w - 1, 0, w - 1, h - 1);
		g.drawLine(0, h - 1, w - 1, h - 1);
		g.setColor(original);
	}

	private void drawPatientUpRight(Graphics2D canvas, int w, int h) {
		Color orig = canvas.getColor();
		canvas.setColor(colScr);
		int hi = 10;
		String txt;
		canvas.drawString(MessageBundle.getMessage("angal.dicom.image.patient.oh"), 10, hi);
		hi += VGAP;
		txt = ohPatient.getName();
		canvas.drawString(MessageBundle.getMessage("angal.common.name.txt") + " : " + txt, 10, hi);
		hi += VGAP;
		txt = String.valueOf(ohPatient.getAge());
		canvas.drawString(MessageBundle.getMessage("angal.common.age.txt") + txt, 10, hi);
		hi += VGAP;
		txt = String.valueOf(ohPatient.getSex());
		canvas.drawString(MessageBundle.getMessage("angal.common.sex.txt") + " : " + txt, 10, hi);
		hi += VGAP;
		hi += VGAP;
		canvas.drawString(MessageBundle.getMessage("angal.dicom.image.patient.dicom"), 10, hi);
		hi += VGAP;
		txt = attributes != null ? attributes.getString(Tag.PatientName) : tmpDbFile.getDicomPatientName();
		if (txt == null) {
			txt = "";
		}
		canvas.drawString(MessageBundle.getMessage("angal.common.name.txt") + " : " + txt, 10, hi);
		hi += VGAP;
		txt = attributes != null ? attributes.getString(Tag.PatientSex) : tmpDbFile.getDicomPatientSex();
		if (txt == null) {
			txt = "";
		}
		canvas.drawString(MessageBundle.getMessage("angal.common.sex.txt") + " : " + txt, 10, hi);
		hi += VGAP;
		txt = attributes != null ? attributes.getString(Tag.PatientAge) :  tmpDbFile.getDicomPatientAge();
		if (txt == null) {
			txt = "";
		}
		canvas.drawString(MessageBundle.getMessage("angal.common.age.txt") + " : " + txt, 10, hi);

		if (ohPatient.getPatientProfilePhoto() != null) {
			final Image photoAsImage = ohPatient.getPatientProfilePhoto().getPhotoAsImage();
			if (photoAsImage != null) {
				hi += VGAP;
				BufferedImage bi = new BufferedImage(photoAsImage.getWidth(this), photoAsImage.getHeight(this), BufferedImage.TYPE_INT_ARGB);
				bi.getGraphics().drawImage(photoAsImage, 0, 0, this);
				drawQuadrant(bi.getGraphics(), photoAsImage.getHeight(this), photoAsImage.getWidth(this), Color.WHITE);
				canvas.drawImage(Scalr.resize(bi, 100), 10, hi, this);
			}
		}
		canvas.setColor(orig);
	}

	private void drawInfoFrameBottomLeft(Graphics2D canvas, int w, int h) {
		Color orig = canvas.getColor();
		int hi = h - 20;
		canvas.setColor(colScr);
		String txt = jSliderZoom.getValue() + " %";
		canvas.drawString(MessageBundle.getMessage("angal.dicom.image.zoom") + " : " + txt, 10, hi);
		hi -= VGAP;
		txt = "[" + (frameIndex + 1) + "]/" + frames.length;
		canvas.drawString(MessageBundle.getMessage("angal.dicom.image.frames") + " : " + txt, 10, hi);
		canvas.setColor(orig);
	}

	private void drawStudyUpRight(Graphics2D canvas, int w, int h) {

		Color orig = canvas.getColor();
		String txt;
		canvas.setColor(colScr);
		int hi = 10;
		int ws = w - 200;
		txt = attributes != null ? attributes.getString(Tag.InstitutionName) :  tmpDbFile.getDicomInstitutionName();
		if (txt == null) {
			txt = "";
		}
		canvas.drawString(txt, ws, hi);
		hi += VGAP;
		txt = attributes != null ? attributes.getString(Tag.StudyID) : tmpDbFile.getDicomStudyId();
		if (txt == null) {
			txt = "";
		}
		canvas.drawString(MessageBundle.getMessage("angal.dicom.image.studyid") + " : " + txt, ws, hi);
		txt = attributes != null ? attributes.getString(Tag.StudyDescription) : tmpDbFile.getDicomStudyDescription();
		hi += VGAP;
		if (txt == null) {
			txt = "";
		}
		canvas.drawString(txt, ws, hi);
		hi += VGAP;
		Date d = attributes != null ? attributes.getDate(Tag.StudyDate) : Converters.toDate(tmpDbFile.getDicomStudyDate());
		DateFormat df = DateFormat.getDateInstance();
		if (d != null) {
			txt = df.format(d);
		} else {
			txt = "";
		}
		canvas.drawString(MessageBundle.getMessage("angal.common.date.txt") + " : " + txt, ws, hi);
		canvas.setColor(orig);
	}

	private void drawSerieBottomRight(Graphics2D canvas, int w, int h) {
		Color orig = canvas.getColor();
		int ws = w - 200;
		int hi = h - 20;
		canvas.setColor(colScr);
		String txt;
		txt = attributes != null ? attributes.getString(Tag.SeriesDescription) : tmpDbFile.getDicomSeriesDescription();
		if (txt == null) {
			txt = "";
		}
		canvas.drawString(txt, ws, hi);
		hi -= VGAP;
		txt = attributes != null ? attributes.getString(Tag.SeriesNumber) : tmpDbFile.getDicomSeriesNumber() + "      ";
		if (txt == null) {
			txt = "";
		}
		canvas.drawString(MessageBundle.getMessage("angal.dicom.image.serie.n") + ' ' + txt, ws, hi);
		canvas.setColor(orig);
	}

	/**
	 * Load actual frame from storage
	 */
	private void refreshFrame() {
		Long id = frames[frameIndex];
		try {
			tmpDbFile = DicomManagerFactory.getManager().loadDetails(id, patID, serieNumber);
			String fileType = tmpDbFile.getFileName().substring(tmpDbFile.getFileName().lastIndexOf('.')+1);
			if (fileType.equalsIgnoreCase("jpg") || fileType.equalsIgnoreCase("jpeg")) {
				getImageFromJPG(tmpDbFile);
			} else if (fileType.equalsIgnoreCase("dcm")) {
				getImageFromDicom(tmpDbFile);
			}
		} catch(OHServiceException ohServiceException) {
			MessageDialog.showExceptions(ohServiceException);
		}
	}
	
	/**
	 * Get the BufferedImage from JPG/JPEG object
	 * 
	 * @param dett
	 */
	private void getImageFromJPG(FileDicom dett) {
		try {
			tmpImg = null;
			ImageInputStream imageInputStream = ImageIO.createImageInputStream(dett.getDicomData().getData().getBinaryStream());
			try {
				tmpImg = ImageIO.read(imageInputStream);
			} catch (IOException ioException) {
				throw new OHDicomException(
						new OHExceptionMessage(MessageBundle.formatMessage("angal.dicom.thefileisnotindicomformat.fmt.msg", dett.getFileName())));
			}
			//imageInputStream.close();
			this.attributes = null;
		} catch (Exception exception) {
			LOGGER.error(exception.getMessage(), exception);
		}
	}
	
	/**
	 * Get the BufferedImage from DICOM object
	 * 
	 * @param dett
	 */
	private void getImageFromDicom(FileDicom dett) {
		ImageInputStream imageInputStream = null;
		DicomInputStream dicomInputStream = null;
		try {
			tmpImg = null;
			Iterator<?> iter = ImageIO.getImageReadersByFormatName("DICOM");
			ImageReader reader = (ImageReader) iter.next();
			DicomImageReadParam param = (DicomImageReadParam) reader.getDefaultReadParam();
			imageInputStream = ImageIO.createImageInputStream(dett.getDicomData().getData().getBinaryStream());
			reader.setInput(imageInputStream, false);

			try {
				tmpImg = reader.read(0, param);
			} catch (IOException ioException) {
				throw new OHDicomException(
						new OHExceptionMessage(MessageBundle.formatMessage("angal.dicom.thefileisnotindicomformat.fmt.msg", dett.getFileName())));
			}
			dicomInputStream = new DicomInputStream(dett.getDicomData().getData().getBinaryStream());
			this.attributes = dicomInputStream.readDataset();
		} catch (Exception exception) {
			LOGGER.error(exception.getMessage(), exception);
		} finally {
			SafeClose.close(imageInputStream);
			SafeClose.close(dicomInputStream);
		}
	}

	private void refreshPan() {

		jPanelCenter.removeAll();
		jPanelCenter.add(composeCenter(jPanelCenter.getWidth(), jPanelCenter.getHeight(), false));
		validate();
	}

	private void refreshZoom() {

		jPanelCenter.removeAll();
		jPanelCenter.add(composeCenter(jPanelCenter.getWidth(), jPanelCenter.getHeight(), true));
		validate();
	}

	/**
	 * Set image in frame viewer
	 * 
	 * @param frame
	 *            , the frame to visualize
	 */
	private void setFrame(int frame) {
		frameIndex = frame;
		refreshFrame();

		resetMouseRelativePosition();
		jPanelCenter.removeAll();
		jPanelCenter.add(composeCenter(jPanelCenter.getWidth(), jPanelCenter.getHeight(), false));
		validate();
	}

	class ZoomListener implements ChangeListener {

		public ZoomListener() {

		}

		@Override
		public void stateChanged(ChangeEvent e) {
			JSlider s1 = (JSlider) e.getSource();
			if (s1.getValueIsAdjusting()) {
				refreshZoom();
			}
		}
	}

	class FrameListener implements ChangeListener {

		public FrameListener() {

		}

		@Override
		public void stateChanged(ChangeEvent e) {
			JSlider s1 = (JSlider) e.getSource();
			if (s1.getValueIsAdjusting()) {
				setFrame(s1.getValue());
			}

		}
	}

	private void resetMouseRelativePosition() {
		p1x = 0;
		p2x = 0;
		p1y = 0;
		p2y = 0;
	}

	/**
	 * relative X in mouse motion
	 */
	int p1x;
	int p2x;

	/**
	 * relative Y in mouse motion
	 */
	int p1y;
	int p2y;
	
	/**
	 * Mouse wheel listener for DicomViewGui
	 */
	class DicomViewGuiMouseWheelListener implements MouseWheelListener {

		/**
		 * Mouse wheel rolled
		 */
		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			int value = jSliderZoom.getValue();
			if (e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {
				int totalScrollAmount = e.getUnitsToScroll();
				jSliderZoom.setValue(value - totalScrollAmount);
				refreshZoom();
			}
		}

	}

	/**
	 * Mouse motion listener for DicomViewGui
	 */
	class DicomViewGuiMouseMotionListener implements MouseMotionListener {

		/**
		 * Mouse dragged, if is also pressed a button calculate the displacement
		 * of position with point of initial position
		 */
		@Override
		public void mouseDragged(MouseEvent e) {
			p2x = e.getXOnScreen();
			p2y = e.getYOnScreen();
			refreshPan();
		}

		/**
		 * Mouse moved, NOT USED
		 */
		@Override
		public void mouseMoved(MouseEvent e) {
		}

	}

	/**
	 * Mouse listener for DicomViewGui
	 */
	class DicomViewGuiMouseListener implements MouseListener {

		/**
		 * Mouse pressed, enable mouse motion and set relative X, Y with the click position
		 */
		@Override
		public void mousePressed(MouseEvent e) {
			p1x = e.getXOnScreen();
			p1y = e.getYOnScreen();
		}

		/**
		 * Mouse released, disable mouse motion
		 */
		@Override
		public void mouseReleased(MouseEvent e) {

			x = totX;

			y = totY;
		}

		/**
		 * Mouse clicked in frame, NOT USED
		 */
		@Override
		public void mouseClicked(MouseEvent e) {
		}

		/**
		 * Mouse entered into frame, NOT USED
		 */
		@Override
		public void mouseEntered(MouseEvent e) {
		}

		/**
		 * Mouse exited of frame, NOT USED
		 */
		@Override
		public void mouseExited(MouseEvent e) {
		}
	}

	public void clear() {
		jPanelCenter.removeAll();
		jPanelCenter.repaint();
		
	}

}
