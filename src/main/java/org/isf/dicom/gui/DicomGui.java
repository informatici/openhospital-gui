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

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Date;
import java.util.List;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ScrollPaneConstants;
import javax.swing.WindowConstants;

import org.isf.dicom.manager.DicomManagerFactory;
import org.isf.dicom.manager.SourceFiles;
import org.isf.dicom.model.FileDicom;
import org.isf.generaldata.MessageBundle;
import org.isf.patient.model.Patient;
import org.isf.utils.exception.OHDicomException;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.file.FileTools;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.ModalJFrame;

/**
 * GUI for Dicom Viewer
 *
 * @author Pietro Castellucci
 * @version 1.0.0
 */
public class DicomGui extends ModalJFrame implements WindowListener {

	private static final long serialVersionUID = 1L;
	
	// STATUS
	private String lastDir = ".";

	// GUI COMPONENTS
	private final float factor = 8f / 11f;

	private JButton jButtonLoadDicom;
	private JButton jButtonDeleteDicom;
	private JPanel jPanelDetail;
	private JSplitPane jSplitPane1;

	private ThumbnailViewGui thumbnail;
	private int patient;
	private Patient ohPatient;
	private JFrame myJFrame;

	/**
	 * Construct a GUI
	 *
	 * @param patient the data wrapper for OH Patient
	 */
	public DicomGui(Patient patient, ModalJFrame owner) {
		super();
		this.patient = patient.getCode();
		this.ohPatient = patient;

		initialize();
		//setVisible(true);
		addWindowListener(this);
		myJFrame = this;

		// TMP
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	}

	/**
	 * Save preference for DICOM window
	 */
	private void saveWindowSettings() {
		try {
			File f = new File("rsc/dicom.user.pref");
			ObjectOutputStream ous = new ObjectOutputStream(new FileOutputStream(f));

			ous.writeInt(getX());
			ous.writeInt(getY());
			ous.writeInt(getHeight());
			ous.writeInt(getWidth());

			ous.writeInt(jSplitPane1.getDividerLocation());

			ous.writeUTF(lastDir);

			ous.flush();
			ous.close();
		} catch (Exception ec) {
		}
	}

	/**
	 * Load preferences for DICOM windows
	 */
	private void loadWindowSettings() {

		int x;
		int y;
		int w;
		int h;

		try {
			File f = new File("rsc/dicom.user.pref");
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f));
			x = ois.readInt();
			y = ois.readInt();
			h = ois.readInt();
			w = ois.readInt();
			lastDir = ois.readUTF();
			ois.close();
		} catch (Exception e) {
			Toolkit kit = Toolkit.getDefaultToolkit();
			Dimension screensize = kit.getScreenSize();
			h = Math.round(screensize.height * factor);
			w = Math.round(screensize.width * factor);
			x = Math.round((screensize.width - w) / 2);
			y = Math.round((screensize.height - h) / 2);
		}

		this.setBounds(x, y, w, h);

	}

	private void initialize() {
		loadWindowSettings();

		this.setTitle(MessageBundle.getMessage("angal.dicomviewer.title"));

		initComponents();
	}

	private void initComponents() {
		JPanel jPanelMain = new JPanel();
		JPanel jPanel1 = new JPanel();
		jButtonLoadDicom = new JButton(MessageBundle.getMessage("angal.dicom.load.btn"));
		jButtonLoadDicom.setMnemonic(MessageBundle.getMnemonic("angal.dicom.load.btn.key"));
		jButtonLoadDicom.setName("jButtonLoadDicom");

		jButtonDeleteDicom = new JButton(MessageBundle.getMessage("angal.dicom.delete.btn"));
		jButtonDeleteDicom.setMnemonic(MessageBundle.getMnemonic("angal.dicom.delete.btn.key"));
		jButtonDeleteDicom.setName("jButtonDeleteDicom");
		jButtonDeleteDicom.setEnabled(false);

		jPanelDetail = new DicomViewGui(null, null);
		jPanelDetail.setName("jPanelDetail");
		JPanel jPanelButton = new JPanel();
		jPanelButton.add(jButtonLoadDicom);
		jPanelButton.add(jButtonDeleteDicom);
		jPanelMain.setName("mainPanel");
		jPanel1.setName("jPanel1");

		GroupLayout jPanel1Layout = new GroupLayout(jPanel1);
		jPanel1Layout.setAutoCreateContainerGaps(true);
		jPanel1.setLayout(jPanel1Layout);
		jPanel1Layout.setHorizontalGroup(jPanel1Layout.createParallelGroup(Alignment.LEADING).addGroup(
				jPanel1Layout.createSequentialGroup().addGroup(jPanel1Layout.createParallelGroup(Alignment.LEADING))
						.addGroup(jPanel1Layout.createParallelGroup(Alignment.LEADING)).addComponent(jPanelButton)));
		jPanel1Layout.setVerticalGroup(jPanel1Layout
				.createParallelGroup(Alignment.LEADING)
				.addGroup(
						Alignment.TRAILING,
						jPanel1Layout.createSequentialGroup().addGroup(jPanel1Layout.createParallelGroup(Alignment.BASELINE))
								.addGroup(jPanel1Layout.createParallelGroup(Alignment.BASELINE))
								.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				.addGroup(Alignment.TRAILING,
						jPanel1Layout.createSequentialGroup().addContainerGap()
								.addGroup(jPanel1Layout.createParallelGroup(Alignment.BASELINE).addComponent(jPanelButton))));

		//jSplitPane1.setDividerLocation(position);

		thumbnail = new ThumbnailViewGui(patient, this);
		thumbnail.initialize();

		JScrollPane jScrollPane2 = new JScrollPane();
		jScrollPane2.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		jScrollPane2.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		jScrollPane2.setViewportView(thumbnail);
		jScrollPane2.setName("jScrollPane2");
		jScrollPane2.setMinimumSize(new Dimension(150, 50));

		jSplitPane1 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, jScrollPane2, jPanelDetail);
		jSplitPane1.setName("jSplitPane1");
		jSplitPane1.setEnabled(false);

		GroupLayout mainPanelLayout = new GroupLayout(jPanelMain);
		jPanelMain.setLayout(mainPanelLayout);

		mainPanelLayout.setHorizontalGroup(mainPanelLayout.createParallelGroup(Alignment.LEADING)
				.addComponent(jPanel1, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addGroup(
						mainPanelLayout.createSequentialGroup().addComponent(jSplitPane1, GroupLayout.DEFAULT_SIZE, 658, Short.MAX_VALUE).addGap(10, 10, 10)));

		mainPanelLayout.setVerticalGroup(mainPanelLayout.createParallelGroup(Alignment.LEADING).addGroup(
				Alignment.TRAILING,
				mainPanelLayout.createSequentialGroup().addComponent(jSplitPane1, GroupLayout.DEFAULT_SIZE, 358, Short.MAX_VALUE)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(jPanel1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)));

		actionListenerJButtonLoadDicom();
		actionListenerJButtonDeleteDicom();
		this.setContentPane(jPanelMain);
	}

	// EVENT LISTENER
	private void actionListenerJButtonLoadDicom() {

		jButtonLoadDicom.addActionListener(actionEvent -> {

			JFileChooser jfc = new JFileChooser(new File(lastDir));

			jfc.addChoosableFileFilter(new FileDicomFilter());
			jfc.addChoosableFileFilter(new FileJPEGFilter());
			jfc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

			int status = jfc.showDialog(new JLabel(""), MessageBundle.getMessage("angal.dicom.open.txt"));

			if (status == JFileChooser.APPROVE_OPTION) {

				File selectedFile = jfc.getSelectedFile();

				File dir = null;

				try {
					dir = selectedFile.getParentFile();
				} catch (Exception ec) {
				}

				if (dir != null) {
					lastDir = dir.getAbsolutePath();
				}

				File file = selectedFile;
				int numfiles = 1;
				if (selectedFile.isDirectory()) {
					try {
						numfiles = SourceFiles.countFiles(selectedFile, patient);
					} catch (OHDicomException e1) {
						OHServiceExceptionUtil.showMessages(e1, this);
						return;
					}
					if (numfiles == 1) {
						return;
					}
					file = selectedFile.listFiles()[0];
				} else {
					try {
						if (!SourceFiles.checkSize(file)) {
							MessageDialog.error(this, "angal.dicom.thefileistoobigpleasesetdicommaxsizeproperty.fmt.msg",
									DicomManagerFactory.getMaxDicomSize());
							return;
						}
					} catch (OHDicomException e1) {
						OHServiceExceptionUtil.showMessages(e1, this);
						return;
					}
				}

				//dummyFileDicom: temporary FileDicom type in order to allow some settings by the user
				FileDicom dummyFileDicom = SourceFiles.preLoadDicom(file, numfiles);

				//shows settings to the user for validation/modification
				List<Date> dates = FileTools.getTimestampFromName(file);

				ShowPreLoadDialog preLoadDialog = new ShowPreLoadDialog(this, numfiles, dummyFileDicom, dates);
				preLoadDialog.setVisible(true);

				if (!preLoadDialog.isSave()) {
					return; //user pressed CANCEL
				}

				dummyFileDicom.setDicomSeriesDescription(preLoadDialog.getDicomDescription());
				dummyFileDicom.setDicomSeriesDate(preLoadDialog.getDicomDate());
				dummyFileDicom.setDicomStudyDate(preLoadDialog.getDicomDate());
				dummyFileDicom.setDicomType(preLoadDialog.getDicomType());

				//TODO: to specify in which already existing series to load the file

				if (selectedFile.isDirectory()) {
					//folder
					thumbnail.disableLoadButton();
					new SourceFiles(dummyFileDicom, selectedFile, patient, numfiles, thumbnail, new DicomLoader(numfiles, myJFrame));
				} else {
					// single file
					try {
						SourceFiles.loadDicom(dummyFileDicom, selectedFile, patient);
					} catch (Exception e1) {
						OHServiceExceptionUtil.showMessages((OHDicomException) e1);
					}
					thumbnail.initialize();
				}
			}
		});
	}

	private void actionListenerJButtonDeleteDicom() {
		jButtonDeleteDicom.addActionListener(actionEvent -> {

			Object[] options = { MessageBundle.getMessage("angal.dicom.delete.yes"), MessageBundle.getMessage("angal.dicom.delete.no") };

			int n = JOptionPane.showOptionDialog(this, MessageBundle.getMessage("angal.dicom.delete.request"),
					MessageBundle.getMessage("angal.dicom.delete.title"),
					JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, "");

			if (n == 0) {
				try {
					DicomManagerFactory.getManager().deleteSeries(patient, thumbnail.getSelectedInstance().getDicomSeriesNumber());
				} catch (OHServiceException ohServiceException) {
					MessageDialog.showExceptions(ohServiceException);
				}
			}
			thumbnail.initialize();
			((DicomViewGui) jPanelDetail).clear();
		});
	}

	// WINDOW LISTENER

	/**
	 * Invoked the first time a window is made visible.
	 */
	@Override
	public void windowOpened(WindowEvent e) {
	}

	/**
	 * Invoked when the user attempts to close the window from the window's
	 * system menu.
	 */
	@Override
	public void windowClosing(WindowEvent e) {
		saveWindowSettings();
	}

	/**
	 * Invoked when a window has been closed as the result of calling dispose on
	 * the window.
	 */
	@Override
	public void windowClosed(WindowEvent e) {
		this.setVisible(false);
		this.dispose();
	}

	/**
	 * Invoked when a window is changed from a normal to a minimized state. For
	 * many platforms, a minimized window is displayed as the icon specified in
	 * the window's iconImage property.
	 *
	 * @see java.awt.Frame#setIconImage
	 */
	@Override
	public void windowIconified(WindowEvent e) {
		this.dispose();
	}

	/**
	 * Invoked when a window is changed from a minimized to a normal state.
	 */
	@Override
	public void windowDeiconified(WindowEvent e) {

	}

	/**
	 * Invoked when the Window is set to be the active Window. Only a Frame or a
	 * Dialog can be the active Window. The native windowing system may denote
	 * the active Window or its children with special decorations, such as a
	 * highlighted title bar. The active Window is always either the focused
	 * Window, or the first Frame or Dialog that is an owner of the focused
	 * Window.
	 */
	@Override
	public void windowActivated(WindowEvent e) {
	}

	/**
	 * Invoked when a Window is no longer the active Window. Only a Frame or a
	 * Dialog can be the active Window. The native windowing system may denote
	 * the active Window or its children with special decorations, such as a
	 * highlighted title bar. The active Window is always either the focused
	 * Window, or the first Frame or Dialog that is an owner of the focused
	 * Window.
	 */
	@Override
	public void windowDeactivated(WindowEvent e) {
	}

	// BUTTONS

	private FileDicom selectedElement;

	public void disableDeleteButton() {
		selectedElement = null;

		jButtonDeleteDicom.setEnabled(false);
	}

	public void enableDeleteButton(FileDicom selected) {

		this.selectedElement = selected;

		jButtonDeleteDicom.setEnabled(true);

	}

	public void disableLoadButton() {

		jButtonLoadDicom.setEnabled(false);
	}

	public void enableLoadButton() {

		jButtonLoadDicom.setEnabled(true);

	}

	public void detail() {

		String serie = "";

		try {
			serie = selectedElement.getDicomSeriesNumber();
		} catch (Exception ecc) {
		}

		((DicomViewGui) jPanelDetail).notifyChanges(ohPatient, serie);

	}

}
