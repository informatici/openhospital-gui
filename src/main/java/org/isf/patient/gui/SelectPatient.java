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

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.EventListenerList;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import org.isf.accounting.gui.BillBrowser;
import org.isf.generaldata.GeneralData;
import org.isf.generaldata.MessageBundle;
import org.isf.menu.gui.MainMenu;
import org.isf.menu.manager.Context;
import org.isf.patient.gui.PatientInsertExtended.PatientListener;
import org.isf.patient.manager.PatientBrowserManager;
import org.isf.patient.model.Patient;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.VoLimitedTextField;

public class SelectPatient extends JDialog implements PatientListener {

//LISTENER INTERFACE --------------------------------------------------------
	private EventListenerList selectionListener = new EventListenerList();

	public interface SelectionListener extends EventListener {

		void patientSelected(Patient patient);
	}

	public void addSelectionListener(SelectionListener l) {
		selectionListener.add(SelectionListener.class, l);
	}

	private void fireSelectedPatient(Patient patient) {
		new AWTEvent(new Object(), AWTEvent.RESERVED_ID_MAX + 1) {

			private static final long serialVersionUID = 1L;
		};

		EventListener[] listeners = selectionListener.getListeners(SelectionListener.class);
		for (EventListener listener : listeners) {
			((SelectionListener) listener).patientSelected(patient);
		}
	}

//---------------------------------------------------------------------------	
	private static final long serialVersionUID = 1L;
	private JPanel jPanelButtons;
	private JPanel jPanelTop;
	private JPanel jPanelCenter;
	private JTable jTablePatient;
	private JScrollPane jScrollPaneTablePatient;
	private JButton jButtonCancel;
	private JButton jButtonSelect;
	private JLabel jLabelSearch;
	private JTextField jTextFieldSearchPatient;
	private JButton jSearchButton;
	private JPanel jPanelDataPatient;
	private Patient patient;

	public Patient getPatient() {
		return patient;
	}

	private JButton buttonNew;
	private PatientSummary ps;
	private String[] patColumns = { MessageBundle.getMessage("angal.common.code.txt").toUpperCase(),
			MessageBundle.getMessage("angal.common.name.txt").toUpperCase() };
	private int[] patColumnsWidth = { 100, 250 };
	private boolean[] patColumnsResizable = { false, true };

	private PatientBrowserManager patientBrowserManager = Context.getApplicationContext().getBean(PatientBrowserManager.class);
	List<Patient> patArray = new ArrayList<>();
	List<Patient> patSearch = new ArrayList<>();
	private String lastKey = "";

	public SelectPatient(JFrame owner, Patient pat) {
		super(owner, true);
		if (!GeneralData.ENHANCEDSEARCH) {
			try {
				patArray = patientBrowserManager.getPatientsByOneOfFieldsLike(null);
			} catch (OHServiceException ohServiceException) {
				MessageDialog.showExceptions(ohServiceException);
				patArray = new ArrayList<>();
			}
			patSearch = patArray;
		}
		patient = pat;
		ps = new PatientSummary(patient);
		initComponents();
		addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				// to free memory
				patArray.clear();
				patSearch.clear();
				dispose();
			}
		});
		setLocationRelativeTo(null);
	}

	public SelectPatient(JDialog owner, Patient pat) {
		super(owner, true);
		if (!GeneralData.ENHANCEDSEARCH) {
			try {
				patArray = patientBrowserManager.getPatientsByOneOfFieldsLike(null);
			} catch (OHServiceException ohServiceException) {
				MessageDialog.showExceptions(ohServiceException);
				patArray = new ArrayList<>();
			}
			patSearch = patArray;
		}
		patient = pat;
		ps = new PatientSummary(patient);
		initComponents();
		addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				// to free memory
				patArray.clear();
				patSearch.clear();
				dispose();
			}
		});
		setLocationRelativeTo(null);
	}

	public SelectPatient(JDialog owner, String search) {
		super(owner, true);
		if (!GeneralData.ENHANCEDSEARCH) {
			try {
				patArray = patientBrowserManager.getPatientsByOneOfFieldsLike(null);
			} catch (OHServiceException ohServiceException) {
				MessageDialog.showExceptions(ohServiceException);
				patArray = new ArrayList<>();
			}
			patSearch = patArray;
		}
		ps = new PatientSummary(patient);
		initComponents();
		addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				// to free memory
				patArray.clear();
				patSearch.clear();
				dispose();
			}
		});
		setLocationRelativeTo(null);
		jTextFieldSearchPatient.setText(search);
		if (GeneralData.ENHANCEDSEARCH) {
			jSearchButton.doClick();
		}
	}

	public SelectPatient(JFrame owner, boolean abbleAddPatient, boolean full) {
		super(owner, true);
		if (!GeneralData.ENHANCEDSEARCH) {
			if (!full) {
				try {
					patArray = patientBrowserManager.getPatientsByOneOfFieldsLike(null);
				} catch (OHServiceException ohServiceException) {
					MessageDialog.showExceptions(ohServiceException);
				}
			} else {
				try {
					patArray = patientBrowserManager.getPatient();
				} catch (OHServiceException ohServiceException) {
					MessageDialog.showExceptions(ohServiceException);
				}
			}
			patSearch = patArray;
		}
		ps = new PatientSummary(patient);
		initComponents();
		addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				// to free memory
				patArray.clear();
				patSearch.clear();
				dispose();
			}
		});
		setLocationRelativeTo(null);
		buttonNew.setVisible(abbleAddPatient);
	}

	public SelectPatient(JDialog owner, boolean abbleAddPatient, boolean full) {
		super(owner, true);
		if (!GeneralData.ENHANCEDSEARCH) {
			if (!full) {
				try {
					patArray = patientBrowserManager.getPatientsByOneOfFieldsLike(null);
				} catch (OHServiceException e2) {
					OHServiceExceptionUtil.showMessages(e2);
				}
			} else {
				try {
					patArray = patientBrowserManager.getPatient();
				} catch (OHServiceException e1) {
					OHServiceExceptionUtil.showMessages(e1);
				}
			}
			patSearch = patArray;
		}
		ps = new PatientSummary(patient);
		initComponents();
		addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				// to free memory
				patArray.clear();
				patSearch.clear();
				dispose();
			}
		});
		setLocationRelativeTo(null);
		buttonNew.setVisible(abbleAddPatient);
	}

	private void initComponents() {
		add(getJPanelTop(), BorderLayout.NORTH);
		add(getJPanelCenter(), BorderLayout.CENTER);
		add(getJPanelButtons(), BorderLayout.SOUTH);
		setTitle(MessageBundle.getMessage("angal.patient.patientselection.title"));
		pack();
	}

	private JPanel getJPanelDataPatient() {
		if (jPanelDataPatient == null) {
			jPanelDataPatient = ps.getPatientCompleteSummary();
			jPanelDataPatient.setAlignmentY(Component.TOP_ALIGNMENT);
		}
		return jPanelDataPatient;
	}

	private JTextField getJTextFieldSearchPatient() {
		if (jTextFieldSearchPatient == null) {
			jTextFieldSearchPatient = new VoLimitedTextField(100, 20);
			jTextFieldSearchPatient.setText("");
			jTextFieldSearchPatient.selectAll();
			if (GeneralData.ENHANCEDSEARCH) {
				jTextFieldSearchPatient.addKeyListener(new KeyListener() {

					@Override
					public void keyPressed(KeyEvent e) {
						int key = e.getKeyCode();
						if (key == KeyEvent.VK_ENTER) {
							jSearchButton.doClick();
						}
					}

					@Override
					public void keyReleased(KeyEvent e) {
					}

					@Override
					public void keyTyped(KeyEvent e) {
					}
				});
			} else {
				jTextFieldSearchPatient.addKeyListener(new KeyListener() {

					@Override
					public void keyTyped(KeyEvent e) {
						lastKey = "";
						String s = String.valueOf(e.getKeyChar());
						if (Character.isLetterOrDigit(e.getKeyChar())) {
							lastKey = s;
						}
						filterPatient();
					}

					@Override
					public void keyPressed(KeyEvent e) {
					}

					@Override
					public void keyReleased(KeyEvent e) {
					}
				});
			}
		}
		return jTextFieldSearchPatient;
	}

	private void filterPatient() {

		String s = jTextFieldSearchPatient.getText() + lastKey;
		s = s.trim();
		String[] s1 = s.split(" ");

		patSearch = new ArrayList<>();

		for (Patient pat : patArray) {

			if (!s.equals("")) {
				String name = pat.getSearchString();
				int a = 0;
				for (String value : s1) {
					if (name.contains(value.toLowerCase())) {
						a++;
					}
				}
				if (a == s1.length) {
					patSearch.add(pat);
				}
			} else {
				patSearch.add(pat);
			}
		}

		if (jTablePatient.getRowCount() == 0) {

			patient = null;
			updatePatientSummary();
		}
		if (jTablePatient.getRowCount() == 1) {

			Patient selectedPatient = (Patient) jTablePatient.getValueAt(0, -1);
			patient = reloadSelectedPatient(selectedPatient.getCode());
			updatePatientSummary();
		}
		jTablePatient.updateUI();
		jTextFieldSearchPatient.requestFocus();
	}

	private JLabel getJLabelSearch() {
		if (jLabelSearch == null) {
			jLabelSearch = new JLabel(MessageBundle.getMessage("angal.patient.searchpatient"));
		}
		return jLabelSearch;
	}

	private JButton getJButtonSelect() {
		if (jButtonSelect == null) {
			jButtonSelect = new JButton(MessageBundle.getMessage("angal.common.select.btn"));
			jButtonSelect.setMnemonic(MessageBundle.getMnemonic("angal.common.select.btn.key"));
			jButtonSelect.addActionListener(actionEvent -> {

				if (patient != null) {
					// to free memory
					patArray.clear();
					patSearch.clear();
					dispose();
					fireSelectedPatient(patient);
				}
			});
		}
		return jButtonSelect;
	}

	private JButton getJButtonCancel() {
		if (jButtonCancel == null) {
			jButtonCancel = new JButton(MessageBundle.getMessage("angal.common.cancel.btn"));
			jButtonCancel.setMnemonic(MessageBundle.getMnemonic("angal.common.cancel.btn.key"));
			jButtonCancel.addActionListener(actionEvent -> {
				// to free memory
				patArray.clear();
				patSearch.clear();
				dispose();
			});
		}
		return jButtonCancel;
	}

	private JScrollPane getJScrollPaneTablePatient() {
		if (jScrollPaneTablePatient == null) {
			jScrollPaneTablePatient = new JScrollPane();
			jScrollPaneTablePatient.setViewportView(getJTablePatient());
			jScrollPaneTablePatient.setAlignmentY(Component.TOP_ALIGNMENT);
		}
		return jScrollPaneTablePatient;
	}

	private JTable getJTablePatient() {
		if (jTablePatient == null) {
			jTablePatient = new JTable();
			jTablePatient.setModel(new SelectPatientModel());
			jTablePatient.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			for (int i = 0; i < patColumns.length; i++) {
				jTablePatient.getColumnModel().getColumn(i).setMinWidth(patColumnsWidth[i]);
				if (!patColumnsResizable[i]) {
					jTablePatient.getColumnModel().getColumn(i).setMaxWidth(patColumnsWidth[i]);
				}
			}
			jTablePatient.setAutoCreateColumnsFromModel(false);
			jTablePatient.getColumnModel().getColumn(0).setCellRenderer(new CenterTableCellRenderer());

			ListSelectionModel listSelectionModel = jTablePatient.getSelectionModel();
			listSelectionModel.addListSelectionListener(selectionEvent -> {
				if (!selectionEvent.getValueIsAdjusting()) {
					int index = jTablePatient.getSelectedRow();
					Patient selectedPatient = (Patient) jTablePatient.getValueAt(index, -1);
					patient = reloadSelectedPatient(selectedPatient.getCode());
					updatePatientSummary();
				}
			});

			jTablePatient.addMouseListener(new MouseListener() {

				@Override
				public void mouseReleased(MouseEvent e) {
				}

				@Override
				public void mousePressed(MouseEvent e) {
				}

				@Override
				public void mouseExited(MouseEvent e) {
				}

				@Override
				public void mouseEntered(MouseEvent e) {
				}

				@Override
				public void mouseClicked(MouseEvent e) {
					if (e.getClickCount() == 2 && !e.isConsumed()) {
						e.consume();
						jButtonSelect.doClick();
					}
				}
			});
		}
		return jTablePatient;
	}

	private Patient reloadSelectedPatient(Integer code) {
		try {
			return patientBrowserManager.getPatientById(code);
		} catch (OHServiceException ex) {
			throw new RuntimeException("Unable to load patient");
		}
	}

	private void updatePatientSummary() {
		jPanelCenter.remove(jPanelDataPatient);
		ps = new PatientSummary(patient);
		jPanelDataPatient = ps.getPatientCompleteSummary();
		jPanelDataPatient.setAlignmentY(Component.TOP_ALIGNMENT);

		jPanelCenter.add(jPanelDataPatient);
		jPanelCenter.validate();
		jPanelCenter.repaint();
	}

	private JPanel getJPanelCenter() {
		if (jPanelCenter == null) {
			jPanelCenter = new JPanel();
			jPanelCenter.setLayout(new BoxLayout(jPanelCenter, BoxLayout.X_AXIS));
			jPanelCenter.add(getJScrollPaneTablePatient());
			jPanelCenter.add(getJPanelDataPatient());

			if (patient != null) {
				for (int i = 0; i < patSearch.size(); i++) {
					if (patSearch.get(i).getCode().equals(patient.getCode())) {
						jTablePatient.addRowSelectionInterval(i, i);
						int j = 0;
						if (i > 10) {
							j = i - 10; // to center the selected row
						}
						jTablePatient.scrollRectToVisible(jTablePatient.getCellRect(j, i, true));
						break;
					}
				}
			}
		}
		return jPanelCenter;
	}

	private JPanel getJPanelTop() {
		if (jPanelTop == null) {
			jPanelTop = new JPanel(new FlowLayout(FlowLayout.LEFT));
			jPanelTop.add(getJLabelSearch());
			jPanelTop.add(getJTextFieldSearchPatient());
			if (MainMenu.checkUserGrants("btnadmnew")) {
				jPanelTop.add(getButtonNew());
			}
			if (GeneralData.ENHANCEDSEARCH) {
				jPanelTop.add(getJSearchButton());
			}
		}
		return jPanelTop;
	}

	private JButton getJSearchButton() {
		if (jSearchButton == null) {
			jSearchButton = new JButton();
			jSearchButton.setIcon(new ImageIcon("rsc/icons/zoom_r_button.png"));
			jSearchButton.setPreferredSize(new Dimension(20, 20));
			jSearchButton.addActionListener(actionEvent -> {
				try {
					patArray = patientBrowserManager.getPatientsByOneOfFieldsLike(jTextFieldSearchPatient.getText());
				} catch (OHServiceException ohServiceException) {
					MessageDialog.showExceptions(ohServiceException);
					patArray = new ArrayList<>();
				}
				filterPatient();
			});
		}
		return jSearchButton;
	}
	private JButton getButtonNew() {
		buttonNew = new JButton(MessageBundle.getMessage("angal.common.newpatient.btn"));
		buttonNew.setMnemonic(MessageBundle.getMnemonic("angal.common.newpatient.btn.key"));
		buttonNew.addActionListener(actionEvent -> {

			if (GeneralData.PATIENTEXTENDED) {
				PatientInsertExtended newrecord = new PatientInsertExtended(this, new Patient(), true);
				newrecord.addPatientListener(this);
				newrecord.setVisible(true);
			} else {
				PatientInsert newrecord = new PatientInsert(this, new Patient(), true);
				newrecord.addPatientListener((PatientInsert.PatientListener) this);
				newrecord.setVisible(true);
			}

		});
		return buttonNew;
	}

	private JPanel getJPanelButtons() {
		if (jPanelButtons == null) {
			jPanelButtons = new JPanel();
			jPanelButtons.add(getJButtonSelect());
			jPanelButtons.add(getJButtonCancel());
		}
		return jPanelButtons;
	}

	class SelectPatientModel extends DefaultTableModel {

		private static final long serialVersionUID = 1L;

		public SelectPatientModel() {
		}

		@Override
		public int getRowCount() {
			if (patSearch == null) {
				return 0;
			}
			return patSearch.size();
		}

		@Override
		public String getColumnName(int c) {
			return patColumns[c];
		}

		@Override
		public int getColumnCount() {
			return patColumns.length;
		}

		@Override
		public Object getValueAt(int r, int c) {
			Patient patient = patSearch.get(r);
			if (c == -1) {
				return patient;
			} else if (c == 0) {
				return patient.getCode();
			} else if (c == 1) {
				return patient.getName();
			}
			return null;
		}

		@Override
		public boolean isCellEditable(int arg0, int arg1) {
			return false;
		}
	}

	class CenterTableCellRenderer extends DefaultTableCellRenderer {

		private static final long serialVersionUID = 1L;

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

			Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			cell.setForeground(Color.BLACK);
			setHorizontalAlignment(CENTER);
			return cell;
		}
	}

	public void setButtonNew(JButton buttonNew) {
		this.buttonNew = buttonNew;
	}

	List<BillBrowser> billBrowserListeners = new ArrayList<>();

	public void addSelectionListener(BillBrowser l) {
		billBrowserListeners.add(l);
	}

	@Override
	public void patientUpdated(AWTEvent e) {
	}

	@Override
	public void patientInserted(AWTEvent e) {
		Patient patient = (Patient) e.getSource();
		patSearch.add(0, patient);
	}
}
