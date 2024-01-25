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
package org.isf.patient.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import org.isf.generaldata.GeneralData;
import org.isf.generaldata.MessageBundle;
import org.isf.menu.manager.Context;
import org.isf.patient.manager.PatientBrowserManager;
import org.isf.patient.model.Patient;
import org.isf.patient.model.PatientProfilePhoto;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.image.ImageUtil;
import org.isf.utils.time.TimeTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class to compose a summary of the data of a given patient
 *
 * @author flavio
 */
public class PatientSummary {

	private static final Logger LOGGER = LoggerFactory.getLogger(PatientSummary.class);
	private static final String UNKNOWN = MessageBundle.getMessage("angal.common.unknown.txt");

	private Patient patient;

	private int maximumWidth = 350;
	private int borderThickness = 10;

	private PatientBrowserManager patientBrowserManager = Context.getApplicationContext().getBean(PatientBrowserManager.class);

	public PatientSummary(Patient patient) {
		super();
		this.patient = patient;
	}

	/**
	 * Create and returns a JPanel with all patient's information
	 * 
	 * @return
	 */
	public JPanel getPatientCompleteSummary() {

		JPanel p = new JPanel(new BorderLayout(borderThickness, borderThickness));

		p.add(getPatientCard(), BorderLayout.NORTH);

		JPanel dataPanel = new JPanel();
		dataPanel.setLayout(new BoxLayout(dataPanel, BoxLayout.Y_AXIS));

		dataPanel.add(setMyBorder(getPatientTaxCodePanel(), MessageBundle.getMessage("angal.admission.taxcode.label")));
		dataPanel.add(getPatientAddressAndCityPanel());
		dataPanel.add(setMyBorder(getPatientParentNewsPanel(), MessageBundle.getMessage("angal.admission.parents.label")));
		dataPanel.add(getPatientKinAndTelephonePanel());
		dataPanel.add(getPatientBloodAndEcoPanel());
		dataPanel.add(getPatientMaritalAndProfession());

		p.add(dataPanel, BorderLayout.CENTER);
		p.add(setMyBorder(getPatientNotePanel(), MessageBundle.getMessage("angal.admission.patientnotes.label")), BorderLayout.SOUTH);

		Dimension dim = p.getPreferredSize();
		p.setMaximumSize(new Dimension(maximumWidth, dim.height));

		return p;
	}

	private JPanel getPatientAddressAndCityPanel() {
		JPanel dataPanel = new JPanel(new GridLayout(1, 2));
		dataPanel.add(setMyBorder(getPatientAddressPanel(), MessageBundle.getMessage("angal.admission.address.label")));
		dataPanel.add(setMyBorder(getPatientCityPanel(), MessageBundle.getMessage("angal.admission.city.label")));
		return dataPanel;
	}

	private JPanel getPatientBloodAndEcoPanel() {
		JPanel dataPanel = new JPanel(new GridLayout(1, 2));
		dataPanel.add(setMyBorder(getPatientBloodTypePanel(), MessageBundle.getMessage("angal.admission.bloodtype.label")));
		dataPanel.add(setMyBorder(getPatientEcoStatusPanel(), MessageBundle.getMessage("angal.admission.insurance.label")));
		return dataPanel;
	}

	private JPanel getPatientMaritalAndProfession() {
		JPanel dataPanel = new JPanel(new GridLayout(1, 2));
		dataPanel.add(setMyBorder(getPatientMaritalStatusPanel(), MessageBundle.getMessage("angal.admission.maritalstatus.label")));
		dataPanel.add(setMyBorder(getPatientProfessionPanel(), MessageBundle.getMessage("angal.admission.profession.label")));
		return dataPanel;
	}

	private JPanel getPatientKinAndTelephonePanel() {
		JPanel dataPanel = new JPanel(new GridLayout(1, 2));
		dataPanel.add(setMyBorder(getPatientKinPanel(), MessageBundle.getMessage("angal.admission.nextofkin.label")));
		dataPanel.add(setMyBorder(getPatientTelephonePanel(), MessageBundle.getMessage("angal.admission.telephone.label")));
		return dataPanel;
	}

	static final int INSETSIZE = 5;

	private JPanel getPatientCard() {
		JPanel cardPanel = new JPanel();
		cardPanel.setLayout(new BoxLayout(cardPanel, BoxLayout.X_AXIS));
		cardPanel.setBackground(Color.WHITE);
		cardPanel.setBorder(BorderFactory.createEmptyBorder(INSETSIZE, INSETSIZE, INSETSIZE, INSETSIZE));

		JPanel patientData = new JPanel();
		patientData.setLayout(new BoxLayout(patientData, BoxLayout.Y_AXIS));
		patientData.setBackground(Color.WHITE);
		patientData.setBorder(BorderFactory.createEmptyBorder(INSETSIZE, INSETSIZE, INSETSIZE, INSETSIZE));

		if (patient == null) {
			patient = new Patient();
		}
		Integer code = patient.getCode();
		JLabel patientCode;
		if (code != null) {
			patientCode = new JLabel(MessageBundle.getMessage("angal.common.code.txt") + ": " + code);
		} else {
			patientCode = new JLabel(" ");
		}
		JLabel patientName = new JLabel(MessageBundle.getMessage("angal.common.name.txt") + ": " + filtra(patient.getName()));
		JLabel patientAge = new JLabel(MessageBundle.getMessage("angal.common.age.txt") + ": " + TimeTools.getFormattedAge(patient.getBirthDate()));
		JLabel patientSex = new JLabel(MessageBundle.getMessage("angal.common.sex.txt") + ": " + patient.getSex());
		JLabel patientTOB = new JLabel(MessageBundle.getMessage("angal.patient.tobm") + ": " + filtra(patient.getBloodType()));

		JLabel patientPhoto = new JLabel();
		PatientProfilePhoto photo = null;
		try {
			photo = this.patientBrowserManager.retrievePatientProfilePhoto(patient);
		} catch (OHServiceException e) {
			OHServiceExceptionUtil.showMessages(e);
		}

		if (photo != null && photo.getPhotoAsImage() != null) {
			patientPhoto.setIcon(
							new ImageIcon(ImageUtil.scaleImage(photo.getPhotoAsImage(), GeneralData.IMAGE_THUMBNAIL_MAX_WIDTH)));
		} else {
			try {
				Image noPhotoImage = ImageIO.read(new File("rsc/images/nophoto.png"));
				patientPhoto.setIcon(new ImageIcon(ImageUtil.scaleImage(noPhotoImage, GeneralData.IMAGE_THUMBNAIL_MAX_WIDTH)));
			} catch (IOException ioe) {
				LOGGER.error("rsc/images/nophoto.png is missing...");
			}
		}

		patientData.add(patientCode);
		patientData.add(Box.createVerticalStrut(INSETSIZE));
		patientData.add(patientName);
		patientData.add(patientAge);
		patientData.add(patientSex);
		patientData.add(Box.createVerticalGlue());
		patientData.add(patientTOB);

		cardPanel.add(patientPhoto);
		cardPanel.add(Box.createHorizontalStrut(INSETSIZE));
		cardPanel.add(patientData);
		return cardPanel;
	}

	private String filtra(String string) {
		if (string == null) {
			return " ";
		}
		if (string.equalsIgnoreCase(UNKNOWN)) {
			return " ";
		}
		return string;
	}

	private JPanel getPatientTaxCodePanel() {
		JLabel l = new JLabel(patient.getTaxCode() + ' ');
		JPanel lP = new JPanel(new FlowLayout(FlowLayout.LEFT, INSETSIZE, INSETSIZE));
		lP.add(l);
		return lP;
	}

	private JPanel getPatientKinPanel() {
		JLabel l;
		if (patient.getNextKin() == null || patient.getNextKin().equalsIgnoreCase("")) {
			l = new JLabel(" ");
		} else {
			l = new JLabel(patient.getNextKin());
		}
		JPanel lP = new JPanel(new FlowLayout(FlowLayout.LEFT, INSETSIZE, INSETSIZE));
		lP.add(l);
		return lP;
	}

	private JPanel getPatientTelephonePanel() {
		JLabel l;
		if (patient.getTelephone() == null || patient.getTelephone().equalsIgnoreCase("")) {
			l = new JLabel(" ");
		} else {
			l = new JLabel(patient.getTelephone());
		}
		JPanel lP = new JPanel(new FlowLayout(FlowLayout.LEFT, INSETSIZE, INSETSIZE));
		lP.add(l);
		return lP;
	}

	private JPanel getPatientAddressPanel() {
		JLabel l;
		if (patient.getAddress() == null || patient.getAddress().equalsIgnoreCase("")) {
			l = new JLabel(" ");
		} else {
			l = new JLabel(patient.getAddress());
		}
		JPanel lP = new JPanel(new FlowLayout(FlowLayout.LEFT, INSETSIZE, INSETSIZE));
		lP.add(l);
		return lP;
	}

	private JPanel getPatientCityPanel() {
		JLabel l;
		if (patient.getCity() == null || patient.getCity().equalsIgnoreCase("")) {
			l = new JLabel(" ");
		} else {
			l = new JLabel(patient.getCity());
		}
		JPanel lP = new JPanel(new FlowLayout(FlowLayout.LEFT, INSETSIZE, INSETSIZE));
		lP.add(l);
		return lP;
	}

	// Panel for Blood Type
	private JPanel getPatientBloodTypePanel() {
		JLabel l;
		String c = patient.getBloodType();
		if (c == null || c.equalsIgnoreCase(MessageBundle.getMessage("angal.common.unknown.txt"))) {
			l = new JLabel(" ");
		} else {
			l = new JLabel(c); // Added - Bundle is not necessary here
		}
		JPanel lP = new JPanel(new FlowLayout(FlowLayout.LEFT, INSETSIZE, INSETSIZE));
		lP.add(l);
		return lP;
	}

	private JPanel getPatientEcoStatusPanel() {
		JLabel l;
		char c = patient.getHasInsurance();
		if (c == 'Y') {
			l = new JLabel(MessageBundle.getMessage("angal.admission.hasinsuranceyes.txt"));
		} else if (c == 'N') {
			l = new JLabel(MessageBundle.getMessage("angal.admission.hasinsuranceno.txt"));
		} else {
			l = new JLabel(" ");
		}
		JPanel lP = new JPanel(new FlowLayout(FlowLayout.LEFT, INSETSIZE, INSETSIZE));
		lP.add(l);
		return lP;
	}

	private JPanel getPatientMaritalStatusPanel() {
		JLabel l;
		if (patient.getMaritalStatus().equalsIgnoreCase(MessageBundle.getMessage("angal.common.unknown.txt"))
						|| patient.getMaritalStatus().equalsIgnoreCase("")) {
			l = new JLabel(" ");
		} else {
			l = new JLabel(patientBrowserManager.getMaritalTranslated(patient.getMaritalStatus()));
		}
		JPanel lP = new JPanel(new FlowLayout(FlowLayout.LEFT, INSETSIZE, INSETSIZE));
		lP.add(l);
		return lP;
	}

	private JPanel getPatientProfessionPanel() {
		JLabel l;
		if (patient.getProfession().equalsIgnoreCase(MessageBundle.getMessage("angal.common.unknown.txt")) || patient.getProfession().equalsIgnoreCase("")) {
			l = new JLabel(" ");
		} else {
			l = new JLabel(patientBrowserManager.getProfessionTranslated(patient.getProfession()));
		}
		JPanel lP = new JPanel(new FlowLayout(FlowLayout.LEFT, INSETSIZE, INSETSIZE));
		lP.add(l);
		return lP;
	}

	private JPanel getPatientParentNewsPanel() {
		StringBuffer labelBfr = new StringBuffer("<html>");
		if (patient.getMother() == 'A') {
			labelBfr.append(MessageBundle.getMessage("angal.admission.motherisalive"));
		} else if (patient.getMother() == 'D') {
			labelBfr.append(MessageBundle.getMessage("angal.admission.motherisdead"));
		}
		// added
		labelBfr.append(patient.getMotherName() == null || patient.getMotherName().compareTo("") == 0 ? "<BR>" : '(' + patient.getMotherName() + ")<BR>");
		if (patient.getFather() == 'A') {
			labelBfr.append(MessageBundle.getMessage("angal.admission.fatherisalive"));
		} else if (patient.getFather() == 'D') {
			labelBfr.append(MessageBundle.getMessage("angal.admission.fatherisdead"));
		}
		// added
		labelBfr.append(patient.getFatherName() == null || patient.getFatherName().compareTo("") == 0 ? "<BR>" : '(' + patient.getFatherName() + ")<BR>");
		if (patient.getParentTogether() == 'Y') {
			labelBfr.append(MessageBundle.getMessage("angal.admission.parentslivetoghether"));
		} else if (patient.getParentTogether() == 'N') {
			labelBfr.append(MessageBundle.getMessage("angal.admission.parentsnotlivingtogether"));
		} else {
			labelBfr.append("<BR>");
		}
		labelBfr.append("</html>");
		JLabel l = new JLabel(labelBfr.toString());
		JPanel lP = new JPanel(new FlowLayout(FlowLayout.LEFT, INSETSIZE, INSETSIZE));
		lP.add(l);
		return lP;
	}

	// alex: modified with scrollbar
	private JPanel getPatientNotePanel() {
		JTextArea textArea = new JTextArea(3, 40);
		textArea.setText(patient.getNote());
		textArea.setEditable(false);
		textArea.setLineWrap(true);

		JScrollPane scrollPane = new JScrollPane(textArea);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		JPanel lP = new JPanel(new BorderLayout());
		lP.add(scrollPane, BorderLayout.CENTER);

		return lP;
	}

	private JPanel setMyBorder(JPanel c, String title) {
		Border b1 = BorderFactory.createLineBorder(Color.lightGray);
		Border b2 = BorderFactory.createTitledBorder(b1, title, TitledBorder.LEFT, TitledBorder.TOP);
		c.setBorder(b2);
		return c;
	}

}
