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
package org.isf.patient.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
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

import org.isf.generaldata.MessageBundle;
import org.isf.menu.manager.Context;
import org.isf.patient.manager.PatientBrowserManager;
import org.isf.patient.model.Patient;
import org.isf.utils.time.TimeTools;

/**
 * A class to compose a summary of the data of a given patient
 * 
 * @author flavio
 * 
 */
public class PatientSummary {

	private Patient patient;
	
	private int maximumWidth = 350;
	private int borderTickness = 10;
	private int imageMaxWidth = 140;

	private PatientBrowserManager patientManager = Context.getApplicationContext().getBean(PatientBrowserManager.class);

	public PatientSummary(Patient patient) {
		super();
		this.patient = patient;
	}

	/**
	 * a short summary in AdmissionBrowser
	 * 
	 * @return
	 */
	public JPanel getPatientDataPanel() {
		
		JPanel p = new JPanel(new BorderLayout(borderTickness, borderTickness));

		p.add(getPatientTitlePanel(), BorderLayout.NORTH);
		JPanel dataPanel = null;
		dataPanel = new JPanel();
		dataPanel.setLayout(new BoxLayout(dataPanel, BoxLayout.Y_AXIS));
		dataPanel.add(setMyBorder(getPatientNamePanel(), MessageBundle.getMessage("angal.admission.namem")));
		dataPanel.add(setMyBorder(getPatientTaxCodePanel(), MessageBundle.getMessage("angal.admission.taxcode")));
		dataPanel.add(setMyBorder(getPatientSexPanel(), MessageBundle.getMessage("angal.admission.sexm")));
		dataPanel.add(setMyBorder(getPatientAgePanel(), MessageBundle.getMessage("angal.admission.agem")));
		dataPanel.add(setMyBorder(getPatientNotePanel(), MessageBundle.getMessage("angal.admission.patientnotes")));
		p.add(dataPanel, BorderLayout.CENTER);

		return p;
	}

	/**
	 * create and returns a JPanel with all patient's informations
	 * 
	 * @return
	 */
	public JPanel getPatientCompleteSummary() {

		JPanel p = new JPanel(new BorderLayout(borderTickness,borderTickness));

		//p.add(getPatientTitlePanel(), BorderLayout.NORTH);
		p.add(getPatientCard(), BorderLayout.NORTH);
		
		JPanel dataPanel = null;
		dataPanel = new JPanel();
		dataPanel.setLayout(new BoxLayout(dataPanel, BoxLayout.Y_AXIS));

//		dataPanel.add(setMyBorder(getPatientNamePanel(), MessageBundle.getMessage("angal.admission.namem")));
//		dataPanel.add(getPatientSexAndAgePanel());
		dataPanel.add(setMyBorder(getPatientTaxCodePanel(), MessageBundle.getMessage("angal.admission.taxcode")));
		dataPanel.add(getPatientAddressAndCityPanel());
		dataPanel.add(setMyBorder(getPatientParentNewsPanel(), MessageBundle.getMessage("angal.admission.parents")));
		dataPanel.add(getPatientKinAndTelephonePanel());
		dataPanel.add(getPatientBloodAndEcoPanel());
		dataPanel.add(getPatientMaritalAndProfession());
		
		p.add(dataPanel, BorderLayout.CENTER);
		p.add(setMyBorder(getPatientNotePanel(), MessageBundle.getMessage("angal.admission.patientnotes")), BorderLayout.SOUTH);
		
		Dimension dim = p.getPreferredSize();
		p.setMaximumSize(new Dimension(maximumWidth, dim.height));

		return p;
	}

	private JPanel getPatientAddressAndCityPanel() {
		JPanel dataPanel = null;
		dataPanel = new JPanel(new java.awt.GridLayout(1, 2));
		dataPanel.add(setMyBorder(getPatientAddressPanel(), MessageBundle.getMessage("angal.admission.addressm")));
		dataPanel.add(setMyBorder(getPatientCityPanel(), MessageBundle.getMessage("angal.admission.city")));
		return dataPanel;
	}

	private JPanel getPatientBloodAndEcoPanel() {
		JPanel dataPanel = null;
		dataPanel = new JPanel(new java.awt.GridLayout(1, 2));
		dataPanel.add(setMyBorder(getPatientBloodTypePanel(), MessageBundle.getMessage("angal.admission.bloodtype")));
		dataPanel.add(setMyBorder(getPatientEcoStatusPanel(), MessageBundle.getMessage("angal.admission.insurance")));
		return dataPanel;
	}

	private JPanel getPatientMaritalAndProfession(){
		JPanel dataPanel = null;
		dataPanel = new JPanel(new java.awt.GridLayout(1, 2));
		dataPanel.add(setMyBorder(getPatientMaritalStatusPanel(), MessageBundle.getMessage("angal.admission.maritalstatus")));
		dataPanel.add(setMyBorder(getPatientProfessionPanel(), MessageBundle.getMessage("angal.admission.profession")));
		return dataPanel;
	}

	private JPanel getPatientKinAndTelephonePanel() {
		JPanel dataPanel = null;
		dataPanel = new JPanel(new java.awt.GridLayout(1, 2));
		dataPanel.add(setMyBorder(getPatientKinPanel(), MessageBundle.getMessage("angal.admission.nextkin")));
		dataPanel.add(setMyBorder(getPatientTelephonePanel(), MessageBundle.getMessage("angal.admission.telephone")));
		return dataPanel;
	}

	final int insetSize = 5;

	private JPanel getPatientTitlePanel() {
		StringBuilder label = new StringBuilder(MessageBundle.getMessage("angal.admission.patientsummary"))
				.append(" (")
				.append(MessageBundle.getMessage("angal.common.code"))
				.append(": ")
				.append(patient.getCode())
				.append(")");
		JLabel l = new JLabel(label.toString());
		l.setBackground(Color.CYAN);
		JPanel lP = new JPanel(new FlowLayout(FlowLayout.CENTER, insetSize, insetSize));
		lP.add(l);
		return lP;
	}
	
	private Image scaleImage(int maxDim, Image photo) {
		double scale = (double) maxDim / (double) photo.getHeight(null);
		if (photo.getWidth(null) > photo.getHeight(null))
		{
			scale = (double) maxDim / (double) photo.getWidth(null);
		}
		int scaledW = (int) (scale * photo.getWidth(null));
		int scaledH = (int) (scale * photo.getHeight(null));
		
		return photo.getScaledInstance(scaledW, scaledH, Image.SCALE_SMOOTH);
	}
	
	private JPanel getPatientCard() {
		JPanel cardPanel = new JPanel();
		cardPanel.setLayout(new BoxLayout(cardPanel, BoxLayout.X_AXIS));
		cardPanel.setBackground(Color.WHITE);
		cardPanel.setBorder(BorderFactory.createEmptyBorder(insetSize, insetSize, insetSize, insetSize));
		
		JPanel patientData = new JPanel();
		patientData.setLayout(new BoxLayout(patientData, BoxLayout.Y_AXIS));
		patientData.setBackground(Color.WHITE);
		patientData.setBorder(BorderFactory.createEmptyBorder(insetSize, insetSize, insetSize, insetSize));
		
		if (patient == null) patient = new Patient();
		Integer code = patient.getCode();
		JLabel patientCode = null;
		if (code != null) 
			patientCode = new JLabel(MessageBundle.getMessage("angal.common.code") + ": " + code.toString());
		else 
			patientCode = new JLabel(" ");
		JLabel patientName = new JLabel(MessageBundle.getMessage("angal.patient.name") + ": " + filtra(patient.getName()));
		JLabel patientAge = new JLabel(MessageBundle.getMessage("angal.patient.age") + ": " + TimeTools.getFormattedAge(patient.getBirthDate()));
		JLabel patientSex = new JLabel(MessageBundle.getMessage("angal.patient.sex") + ": " + patient.getSex());
		JLabel patientTOB = new JLabel(MessageBundle.getMessage("angal.patient.tobm") + ": " + filtra(patient.getBloodType()));
		
		JLabel patientPhoto = new JLabel();
		if (patient.getPatientProfilePhoto() != null && patient.getPatientProfilePhoto().getPhotoAsImage() != null) {
			patientPhoto.setIcon(new ImageIcon(scaleImage(imageMaxWidth, patient.getPatientProfilePhoto().getPhotoAsImage())));
		} else {
			try {
				Image noPhotoImage = ImageIO.read(new File("rsc/images/nophoto.png"));
				patientPhoto.setIcon(new ImageIcon(scaleImage(imageMaxWidth, noPhotoImage)));
			} catch (IOException ioe) {
				System.out.println("rsc/images/nophoto.png is missing...");
			}
		}
		
		patientData.add(patientCode);
		patientData.add(Box.createVerticalStrut(insetSize));
		patientData.add(patientName);
		patientData.add(patientAge);
		patientData.add(patientSex);
		patientData.add(Box.createVerticalGlue());
		patientData.add(patientTOB);
		
		cardPanel.add(patientPhoto);
		cardPanel.add(Box.createHorizontalStrut(insetSize));
		cardPanel.add(patientData);
		return cardPanel;
	}

	private String filtra(String string) {
		if (string == null) return " ";
		if (string.equalsIgnoreCase("Unknown")) return " ";
		return string;
	}
	
	private JPanel getPatientNamePanel() {
		JLabel l = new JLabel(patient.getSecondName() + " " + patient.getFirstName());
		JPanel lP = new JPanel(new FlowLayout(FlowLayout.LEFT, insetSize, insetSize));
		lP.add(l);
		return lP;
	}

	private JPanel getPatientTaxCodePanel() {
		JLabel l = new JLabel(patient.getTaxCode() + " ");
		JPanel lP = new JPanel(new FlowLayout(FlowLayout.LEFT, insetSize, insetSize));
		lP.add(l);
		return lP;
	}

	private JPanel getPatientKinPanel() {
		JLabel l = null;
		if (patient.getNextKin() == null || patient.getNextKin().equalsIgnoreCase("")) {
			//l = new JLabel(MessageBundle.getMessage("angal.admission.unknown"));
			l = new JLabel(" ");
		} else {
			l = new JLabel(patient.getNextKin());
		}
		JPanel lP = new JPanel(new FlowLayout(FlowLayout.LEFT, insetSize, insetSize));
		lP.add(l);
		return lP;
	}

	private JPanel getPatientTelephonePanel() {
		JLabel l = null;
		if (patient.getTelephone() == null || patient.getTelephone().equalsIgnoreCase("")) {
			//l = new JLabel(MessageBundle.getMessage("angal.admission.unknown"));
			l = new JLabel(" ");
		} else {
			l = new JLabel("" + patient.getTelephone());
		}
		JPanel lP = new JPanel(new FlowLayout(FlowLayout.LEFT, insetSize, insetSize));
		lP.add(l);
		return lP;
	}
	
	private JPanel getPatientAddressPanel() {
		JLabel l = null;
		if (patient.getAddress() == null || patient.getAddress().equalsIgnoreCase("")) {
			//l = new JLabel(MessageBundle.getMessage("angal.admission.unknown"));
			l = new JLabel(" ");
		} else {
			l = new JLabel("" + patient.getAddress());
		}
		JPanel lP = new JPanel(new FlowLayout(FlowLayout.LEFT, insetSize, insetSize));
		lP.add(l);
		return lP;
	}

	private JPanel getPatientCityPanel() {
		JLabel l = null;
		if (patient.getCity() == null || patient.getCity().equalsIgnoreCase("")) {
			//l = new JLabel(MessageBundle.getMessage("angal.admission.unknown"));
			l = new JLabel(" ");
		} else {
			l = new JLabel("" + patient.getCity());
		}
		JPanel lP = new JPanel(new FlowLayout(FlowLayout.LEFT, insetSize, insetSize));
		lP.add(l);
		return lP;
	}

	// Panel for Blood Type
	private JPanel getPatientBloodTypePanel() {
		JLabel l = null;
		String c = new String(patient.getBloodType());
		if (c == null || c.equalsIgnoreCase("Unknown")) {
			l = new JLabel(MessageBundle.getMessage("angal.admission.bloodtypeisunknown"));
			l = new JLabel(" ");
		} else {
			// l = new
			// JLabel(MessageBundle.getMessage("angal.admission.bloodtypeis")+c);
			l = new JLabel(c); // Added - Bundle is not necessary here
		}
		JPanel lP = new JPanel(new FlowLayout(FlowLayout.LEFT, insetSize, insetSize));
		lP.add(l);
		return lP;
	}

	private JPanel getPatientEcoStatusPanel() {
		JLabel l = null;
		char c = patient.getHasInsurance();
		if (c == 'Y') {
			l = new JLabel(MessageBundle.getMessage("angal.admission.hasinsuranceyes"));
		} else if (c == 'N') {
			l = new JLabel(MessageBundle.getMessage("angal.admission.hasinsuranceno"));
		} else {
			l = new JLabel(MessageBundle.getMessage("angal.admission.unknown"));
			l = new JLabel(" ");
		}
		JPanel lP = new JPanel(new FlowLayout(FlowLayout.LEFT, insetSize, insetSize));
		lP.add(l);
		return lP;
	}

	private JPanel getPatientMaritalStatusPanel() {
		JLabel l = null;
		if (patient.getMaritalStatus().equalsIgnoreCase("unknown") || patient.getMaritalStatus().equalsIgnoreCase("")) {
			//l = new JLabel(MessageBundle.getMessage("angal.admission.unknown"));
			l = new JLabel(" ");
		} else {
			l = new JLabel("" + patientManager.getMaritalTranslated(patient.getMaritalStatus()));
		}
		JPanel lP = new JPanel(new FlowLayout(FlowLayout.LEFT, insetSize, insetSize));
		lP.add(l);
		return lP;
	}

	private JPanel getPatientProfessionPanel() {
		JLabel l = null;
		if (patient.getProfession().equalsIgnoreCase("unknown") || patient.getProfession().equalsIgnoreCase("")) {
			//l = new JLabel(MessageBundle.getMessage("angal.admission.unknown"));
			l = new JLabel(" ");
		} else {
			l = new JLabel("" +  patientManager.getProfessionTranslated(patient.getProfession()));
		}
		JPanel lP = new JPanel(new FlowLayout(FlowLayout.LEFT, insetSize, insetSize));
		lP.add(l);
		return lP;
	}

	private JPanel getPatientAgePanel() {
		JLabel l = new JLabel("" + patient.getAge());
		JPanel lP = new JPanel(new FlowLayout(FlowLayout.LEFT, insetSize, insetSize));
		lP.add(l);
		return lP;
	}

	private JPanel getPatientSexPanel() {
		JLabel l = new JLabel((patient.getSex() == 'F' ? MessageBundle.getMessage("angal.admission.female") : MessageBundle.getMessage("angal.admission.male")));
		JPanel lP = new JPanel(new FlowLayout(FlowLayout.LEFT, insetSize, insetSize));
		lP.add(l);
		return lP;
	}

	private JPanel getPatientParentNewsPanel() {
		StringBuffer labelBfr = new StringBuffer("<html>");
		if (patient.getMother() == 'A')
			labelBfr.append(MessageBundle.getMessage("angal.admission.motherisalive"));
		else if (patient.getMother() == 'D')
			labelBfr.append(MessageBundle.getMessage("angal.admission.motherisdead"));;
		// added
			labelBfr.append((patient.getMother_name() == null || patient.getMother_name().compareTo("") == 0 ? "<BR>" : "(" + patient.getMother_name() + ")<BR>"));
		if (patient.getFather() == 'A')
			labelBfr.append(MessageBundle.getMessage("angal.admission.fatherisalive"));
		else if (patient.getFather() == 'D')
			labelBfr.append(MessageBundle.getMessage("angal.admission.fatherisdead"));
		// added
		labelBfr.append((patient.getFather_name() == null || patient.getFather_name().compareTo("") == 0 ? "<BR>" : "(" + patient.getFather_name() + ")<BR>"));
		if (patient.getParentTogether() == 'Y')
			labelBfr.append(MessageBundle.getMessage("angal.admission.parentslivetoghether"));
		else if (patient.getParentTogether() == 'N')
			labelBfr.append(MessageBundle.getMessage("angal.admission.parentsnotlivingtogether"));
		else 
			labelBfr.append("<BR>");
		labelBfr.append("</html>");
		JLabel l = new JLabel(labelBfr.toString());
		JPanel lP = new JPanel(new FlowLayout(FlowLayout.LEFT, insetSize, insetSize));
		lP.add(l);
		return lP;
	}

	// alex: modified with scroolbar
	private JPanel getPatientNotePanel() {
		JTextArea textArea = new JTextArea(3, 40);
		textArea.setText(patient.getNote());
		textArea.setEditable(false);
		textArea.setLineWrap(true);

		JScrollPane scrollPane = new JScrollPane(textArea);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		JPanel lP = new JPanel(new BorderLayout());
		lP.add(scrollPane, BorderLayout.CENTER);

		return lP;
	}

	private JPanel setMyBorder(JPanel c, String title) {
		javax.swing.border.Border b1 = BorderFactory.createLineBorder(Color.lightGray);
		javax.swing.border.Border b2 = BorderFactory.createTitledBorder(b1, title, javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.TOP);
		c.setBorder(b2);
		return c;
	}

}
