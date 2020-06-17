package org.isf.visits.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import org.isf.generaldata.MessageBundle;
import org.isf.menu.manager.Context;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.ModalJFrame;
import org.isf.ward.manager.WardBrowserManager;
import org.isf.ward.model.Ward;

import com.toedter.calendar.JDateChooser;

public class VisitsEdit extends ModalJFrame{


	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/*
	 * Constants
	 */
	private final String dateTimeFormat = "dd/MM/yy HH:mm:ss";
	private static final int textSize = 30;

	/*
	 * Attributes
	 */
	private JDateChooser visitDateChooser;
	private JPanel buttonsPanel;
	private JButton buttonOK;
	private JButton buttonCancel;

	/*
	 * Return Value
	 */
	private Date visitDate = null;

	private JPanel wardPanel;

	private JComboBox wardBox;

	private VisitsEdit myFrame;

	public VisitsEdit() {
		super();
		myFrame=this;
		initComponents();
        setVisible(true);
		
	}


	private void initComponents() {
		setSize(new Dimension(200, 150));
		getContentPane().setLayout(new BorderLayout(0, 0));
		
		getContentPane().add(getpVisitInf(),BorderLayout.NORTH);
		getContentPane().add(getButtonsPanel(), BorderLayout.SOUTH);
		
		setLocationRelativeTo(null);
		pack();

	}
	private JPanel getpVisitInf() {

		JPanel patientParamsPanel = new JPanel(new SpringLayout());

		GridBagLayout gbl_jPanelData = new GridBagLayout();
		gbl_jPanelData.columnWidths = new int[] { 20, 20, 20, 0, 0, 00 };
		gbl_jPanelData.columnWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };
		gbl_jPanelData.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };
		patientParamsPanel.setLayout(gbl_jPanelData);
		
		GridBagConstraints gbc_ward = new GridBagConstraints();
		

		gbc_ward.gridy = 1;
		gbc_ward.gridx = 0;
		patientParamsPanel.add(getWardPanel(), gbc_ward);
		return patientParamsPanel;
	}

	private JTextField yProgTextField = null;
	private Ward saveWard = null;
	private ArrayList<Ward> wardList = null;

	private JPanel ServicePanel;

	private JLabel Servicelabel;

	private JTextField ServiceField;

	private JPanel DurationPanel;

	private JLabel Durationlabel;

	private JTextField DurationField;

	private JPanel dateViPanel;

	private JLabel dateAdm;

	private JButton admButton;

	private JPanel getWardPanel() {
		if (wardPanel == null) {
			wardPanel = new JPanel();
			
			WardBrowserManager wbm = Context.getApplicationContext().getBean(WardBrowserManager.class);
			wardBox = new JComboBox();
			wardBox.addItem("");
			try {
				wardList = wbm.getWards();
			}catch(OHServiceException e){
				wardList = new ArrayList<Ward>();
                OHServiceExceptionUtil.showMessages(e);
			}
			for (Ward ward : wardList) {
		
					if (ward.getBeds() > 0)
						wardBox.addItem(ward);
				
				if (saveWard != null) {
					if (saveWard.getCode().equalsIgnoreCase(ward.getCode())) {
						wardBox.setSelectedItem(ward);
					}
				} 
				}
			}

			
			wardPanel.add(wardBox);
			wardPanel.setBorder(BorderFactory.createTitledBorder("Select a ward:"));
		
		return wardPanel;
	}
	
	
	
	
	private JPanel getButtonsPanel() {
		if (buttonsPanel == null) {
			buttonsPanel = new JPanel();
			buttonsPanel.add(getButtonOK());
			buttonsPanel.add(getButtonCancel());
		}
		return buttonsPanel;
	}

	private JButton getButtonCancel() {
		if (buttonCancel == null) {
			buttonCancel = new JButton(MessageBundle.getMessage("angal.common.cancel"));
			buttonCancel.setMnemonic(KeyEvent.VK_N);
			buttonCancel.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent arg0) {
					dispose();
				}
			});
		}
		return buttonCancel;
	}

	private JButton getButtonOK() {
		if (buttonOK == null) {
			buttonOK = new JButton(MessageBundle.getMessage("angal.common.ok"));
			buttonOK.setMnemonic(KeyEvent.VK_O);
			buttonOK.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent arg0) {
//					VisitView therapy = new VisitView(VisitsEdit.this,getWard(), false);
//					therapy.setLocationRelativeTo(null);
//					therapy.setVisible(true);
				}
			});
		}
		return buttonOK;
	}

	public Ward getWard() {
		return (Ward) wardBox.getSelectedItem();
	}

}