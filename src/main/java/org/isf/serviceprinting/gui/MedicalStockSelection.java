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
package org.isf.serviceprinting.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.GregorianCalendar;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.isf.generaldata.MessageBundle;
import org.isf.medicals.manager.MedicalBrowsingManager;
import org.isf.medicals.model.Medical;
import org.isf.medicalstock.model.Movement;
import org.isf.medicalstock.service.MedicalStockIoOperations;
import org.isf.medstockmovtype.manager.MedicaldsrstockmovTypeBrowserManager;
import org.isf.medstockmovtype.model.MovementType;
import org.isf.medtype.manager.MedicalTypeBrowserManager;
import org.isf.medtype.model.MedicalType;
import org.isf.menu.manager.Context;
import org.isf.serviceprinting.manager.PrintManager;
import org.isf.serviceprinting.print.Movement4Print;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.DateTextField;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.ward.manager.WardBrowserManager;
import org.isf.ward.model.Ward;

/**
 * @deprecated in favor of new reports StockCard and StockLedger
 */
@Deprecated
public class MedicalStockSelection extends JDialog implements ActionListener{

	private static final long serialVersionUID = 1L;

	private JPanel selectionPanel;

	private JPanel buttonsPanel;

	private JComboBox medicalBox;

	private JComboBox medicalTypeBox;

	private JButton okButton;

	private JButton closeButton;

	private JPanel jContentPane;

	private JPanel formatPanel;
	private ButtonGroup formatGroup;
	private JComboBox orderBox;

	private JCheckBox javaCheck;

	private JCheckBox pdfCheck;

	private JCheckBox printCheck;

	private JCheckBox docWordCheck;

	private JCheckBox docOpenCheck;

	private DateTextField movDateFrom;

	private DateTextField movDateTo;

	private JTextField lotField;
	private JComboBox movTypeBox;
	private JComboBox wardBox;
	private String formatSelected="Java";
	
	private PrintManager printManager = Context.getApplicationContext().getBean(PrintManager.class);
	private MedicalBrowsingManager medicalBrowsingManager = Context.getApplicationContext().getBean(MedicalBrowsingManager.class);
	private MedicalTypeBrowserManager medicalTypeBrowserManager = Context.getApplicationContext().getBean(MedicalTypeBrowserManager.class);
	private MedicaldsrstockmovTypeBrowserManager medicaldsrstockmovTypeBrowserManager = Context.getApplicationContext().getBean(MedicaldsrstockmovTypeBrowserManager.class);

	public MedicalStockSelection(JFrame owner) {
		super(owner, true);
		initialize();
		setVisible(true);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}

	private void initialize() {
		Toolkit kit = Toolkit.getDefaultToolkit();
		Dimension screensize = kit.getScreenSize();
		int pfrmWidth = 700;
		int pfrmHeight = 300;
		setBounds(screensize.width / 4, screensize.height / 4, pfrmWidth,
				pfrmHeight);

		this.setContentPane(getJContentPane());
		this.setTitle("Movement Stock Print Selection");
		pack();
	}

	/**
	 * ascaksch
	 * @return un pannello
	 */
	private JPanel getJContentPane() {
		jContentPane = new JPanel();
		jContentPane.setLayout(new BoxLayout(jContentPane, BoxLayout.Y_AXIS));
		jContentPane.add(getSelectionPanel());
		jContentPane.add(getFormatPanel());
		jContentPane.add(getButtonsPanel());
		validate();
		return jContentPane;
	}

	private JPanel getSelectionPanel() {
		selectionPanel = new JPanel();
		selectionPanel
				.setLayout(new BoxLayout(selectionPanel, BoxLayout.Y_AXIS));
		selectionPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory
				.createLineBorder(Color.GRAY), "Selection Panel"));
		JPanel label1Panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		label1Panel.add(new JLabel("Date From"));
		selectionPanel.add(label1Panel);
		selectionPanel.add(getMovDateFrom());
		JPanel label2Panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		label2Panel.add(new JLabel("Date To"));
		selectionPanel.add(label2Panel);
		selectionPanel.add(getMovDateTo());
		JPanel label3Panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		label3Panel.add(new JLabel("Select a Pharmaceutical"));
		selectionPanel.add(label3Panel);
		selectionPanel.add(getMedicalBox());
		JPanel label4Panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		label4Panel.add(new JLabel("Select a Pharmaceutical Type"));
		selectionPanel.add(label4Panel);
		selectionPanel.add(getMedicalTypeBox());
		JPanel label5Panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		label5Panel.add(new JLabel("Select a Movement Type"));
		selectionPanel.add(label5Panel);
		selectionPanel.add(getMovementTypeBox());
		JPanel label6Panel=new JPanel(new FlowLayout(FlowLayout.CENTER));
		label6Panel.add(new JLabel("Select a Ward"));
		selectionPanel.add(label6Panel);
		selectionPanel.add(getWardBox());
		JPanel label7Panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		label7Panel.add(new JLabel("Lot Code"));
		selectionPanel.add(label7Panel);
		selectionPanel.add(getLotField());
		return selectionPanel;
	}

	private JPanel getFormatPanel() {
		formatPanel = new JPanel();
		formatPanel.setLayout(new BoxLayout(formatPanel, BoxLayout.Y_AXIS));
		formatPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory
				.createLineBorder(Color.GRAY), "Output"));
		javaCheck=new JCheckBox("Java Preview");
		javaCheck.setActionCommand("Java");
		javaCheck.addActionListener(this);
		javaCheck.setSelected(true);
		pdfCheck=new JCheckBox("Pdf Document");
		pdfCheck.setActionCommand("Pdf");
		pdfCheck.addActionListener(this);
		printCheck=new JCheckBox("Print");
		printCheck.setActionCommand("Print");
		printCheck.addActionListener(this);
		//docWordCheck=new JCheckBox("Doc (Word)");
		//docOpenCheck=new JCheckBox("Doc (Open Office)");

		formatGroup = new ButtonGroup();
		formatGroup.add(javaCheck);
		formatGroup.add(pdfCheck);
		formatGroup.add(printCheck);
		formatGroup.add(docWordCheck);
		formatGroup.add(docOpenCheck);

		JPanel javaCheckPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 30,
				0));
		javaCheckPanel.add(javaCheck);
		formatPanel.add(javaCheckPanel);
		JPanel pdfCheckPanel = new JPanel(
				new FlowLayout(FlowLayout.LEFT, 30, 0));
		pdfCheckPanel.add(pdfCheck);
		formatPanel.add(pdfCheckPanel);
		JPanel printCheckPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 30,
				0));
		printCheckPanel.add(printCheck);
		formatPanel.add(printCheckPanel);
		/*JPanel docWordCheckPanel = new JPanel(new FlowLayout(FlowLayout.LEFT,
				30, 0));
		docWordCheckPanel.add(docWordCheck);
		formatPanel.add(docWordCheckPanel);
		JPanel docOpenCheckPanel = new JPanel(new FlowLayout(FlowLayout.LEFT,
				30, 0));
		docOpenCheckPanel.add(docOpenCheck);
		formatPanel.add(docOpenCheckPanel);*/
		JPanel orderPanel=new JPanel(new FlowLayout(FlowLayout.CENTER));
		orderPanel.add(new JLabel("Order by"));
		orderPanel.add(getOrderBox());
		formatPanel.add(orderPanel);
		return formatPanel;
	}

	private JPanel getButtonsPanel() {
		buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		buttonsPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
		buttonsPanel.add(getOkButton());
		buttonsPanel.add(getCloseButton());
		return buttonsPanel;
	}

	private JComboBox getMedicalBox() {
		medicalBox = new JComboBox();
		ArrayList<Medical> medical;
		try {
			medical = medicalBrowsingManager.getMedicals();
		} catch (OHServiceException e1) {
			medical = null;
			JOptionPane.showMessageDialog(null, e1.getMessage());
		}
		medicalBox.addItem("All");
		if (null != medical) {
			for (Medical aMedical : medical) {
				medicalBox.addItem(aMedical);
			}
		}
		medicalBox.addMouseListener(new MouseListener() {
			public void mouseExited(MouseEvent e) {
			}

			public void mouseEntered(MouseEvent e) {
			}

			public void mouseReleased(MouseEvent e) {
			}

			public void mousePressed(MouseEvent e) {
			}

			public void mouseClicked(MouseEvent e) {
				medicalBox.setEnabled(true);
				medicalBox.setEditable(true);
				medicalTypeBox.setSelectedIndex(0);
				medicalTypeBox.setEnabled(false);
			}
		});
		medicalBox.setEditable(true);
		medicalBox.setEnabled(true);
		// medicalBox.addActionListener(this);
		return medicalBox;

	}

	private JComboBox getMedicalTypeBox() {
		medicalTypeBox = new JComboBox();
		ArrayList<MedicalType> medical;
		
		medicalTypeBox.addItem("All");
		
		try {
			medical = medicalTypeBrowserManager.getMedicalType();
			
			for (MedicalType aMedicalType : medical) {
				medicalTypeBox.addItem(aMedicalType);
			}
		} catch (OHServiceException e1) {
			OHServiceExceptionUtil.showMessages(e1);
		}
		
		medicalTypeBox.addMouseListener(new MouseListener() {
			public void mouseExited(MouseEvent e) {
			}

			public void mouseEntered(MouseEvent e) {
			}

			public void mouseReleased(MouseEvent e) {
			}

			public void mousePressed(MouseEvent e) {
			}

			public void mouseClicked(MouseEvent e) {
				medicalTypeBox.setEnabled(true);
				medicalBox.setSelectedIndex(0);
				medicalBox.setEditable(false);
				medicalBox.setEnabled(false);
			}
		});
		medicalTypeBox.setEnabled(false);
		return medicalTypeBox;
	}
	private JComboBox getOrderBox(){
		orderBox=new JComboBox();
		orderBox.addItem("Date");
		orderBox.addItem("Ward");
		orderBox.addItem("Pharmaceutical Type");
		orderBox.addItem("Movement Type");
		return orderBox;
	}

	private DateTextField getMovDateFrom() {
		GregorianCalendar time = new GregorianCalendar();
		//time.roll(GregorianCalendar.MONTH, false);
		time.add(GregorianCalendar.MONTH, -1);
		movDateFrom = new DateTextField(time);
		return movDateFrom;
	}

	private DateTextField getMovDateTo() {
		movDateTo = new DateTextField(new GregorianCalendar());
		return movDateTo;
	}

	private JTextField getLotField() {
		lotField = new JTextField(10);
		return lotField;
	}
	private JComboBox getMovementTypeBox() {
		movTypeBox = new JComboBox();
		ArrayList<MovementType> type;
		try {
			type = medicaldsrstockmovTypeBrowserManager.getMedicaldsrstockmovType();
		} catch (OHServiceException e1) {
			type = null;
			JOptionPane.showMessageDialog(null, e1.getMessage());
		}
		movTypeBox.addItem("All");
		if (null != type) {
			for (MovementType movementType : type) {
				movTypeBox.addItem(movementType);
			}
		}
		movTypeBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!(movTypeBox.getSelectedItem() instanceof String)) {
					MovementType selected = (MovementType) movTypeBox
							.getSelectedItem();
					if (selected.getType().contains("-")) {
						wardBox.setEnabled(true);
					} else {
						wardBox.setSelectedIndex(0);
						wardBox.setEnabled(false);
					}
				} else {
					wardBox.setSelectedIndex(0);
					wardBox.setEnabled(false);
				}
			}
		});
		return movTypeBox;
	}
	private JComboBox getWardBox() {
		WardBrowserManager wbm = Context.getApplicationContext().getBean(WardBrowserManager.class);
		wardBox = new JComboBox();
		wardBox.addItem("All");
		ArrayList<Ward> wardList;
		try {
			wardList = wbm.getWards();
		} catch(OHServiceException ohServiceException) {
			wardList = new ArrayList<>();
			MessageDialog.showExceptions(ohServiceException);
		}
		for (org.isf.ward.model.Ward elem : wardList) {
			wardBox.addItem(elem);
		}
		wardBox.setEnabled(false);
		return wardBox;
	}

	private JButton getOkButton() {
		okButton = new JButton(MessageBundle.getMessage("angal.common.ok.btn"));
		okButton.setMnemonic(MessageBundle.getMnemonic("angal.common.ok.btn.key"));
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ArrayList<Movement> pMovements=null;
				GregorianCalendar movFrom = movDateFrom.getCompleteDate();
				GregorianCalendar movTo = movDateTo.getCompleteDate();
				boolean correctDate = true;
				if ((movFrom==null) || (movTo == null)) {
					JOptionPane.showMessageDialog(null, "Date/s not valid");
					correctDate = false;
				} else if (movTo.compareTo(movFrom) < 0) {
					JOptionPane.showMessageDialog(null, "Date To cannot be chronologically before than Date From");
					correctDate = false;
				}
				if (correctDate) {
					String medical = null;
					if (medicalBox.getSelectedItem() instanceof Medical) {
						medical = ((Medical) medicalBox.getSelectedItem())
								.getDescription();
					} else if (!(((String) medicalBox.getSelectedItem())
							.equalsIgnoreCase("All"))) {
						medical = (String) medicalBox.getSelectedItem();
					}
					String medicalType = null;
					if (!(medicalTypeBox.getSelectedItem() instanceof String)) {
						medicalType = ((MedicalType) medicalTypeBox
								.getSelectedItem()).getCode();
					}
					String movementType=null;
					if (!(movTypeBox.getSelectedItem() instanceof String)){
						movementType=((MovementType)movTypeBox.getSelectedItem()).getCode();
					}
					String wardSelected=null;
					if (!(wardBox.getSelectedItem() instanceof String)) {
						wardSelected = ((Ward) wardBox.getSelectedItem())
								.getCode();
					}
					
					String lot = null;
					if (!(lotField.getText().equalsIgnoreCase(""))) {
						lot = lotField.getText();
					}
					
					MedicalStockIoOperations ioOperations = Context.getApplicationContext().getBean(MedicalStockIoOperations.class);
					int format=0;String path=null;
					if (formatSelected.equalsIgnoreCase("Java")){
						format=PrintManager.toDisplay;
					}else if (formatSelected.equalsIgnoreCase("Pdf")){
						format=PrintManager.toPdf;
					}else if (formatSelected.equalsIgnoreCase("Print")){
						format=PrintManager.toPrint;
					}
					int selectedIndex=orderBox.getSelectedIndex();
					
					try {
					switch(selectedIndex){
						case 0:
							path="stockMovementDate";
							pMovements = ioOperations.getMovementForPrint(medical,
										medicalType,wardSelected ,movementType, movFrom, movTo, lot,MedicalStockIoOperations.MovementOrder.DATE);
							break;
						case 1:
							path="stockMovementWard";
							pMovements = ioOperations.getMovementForPrint(medical,
									medicalType,wardSelected ,movementType, movFrom, movTo, lot,MedicalStockIoOperations.MovementOrder.WARD);
							break;
						case 2:
							path="stockMovementPhrType";
							pMovements = ioOperations.getMovementForPrint(medical,
									medicalType,wardSelected ,movementType, movFrom, movTo, lot,MedicalStockIoOperations.MovementOrder.PHARMACEUTICAL_TYPE);
							break;
						case 3:
							path="stockMovementType";
							pMovements = ioOperations.getMovementForPrint(medical,
									medicalType,wardSelected ,movementType, movFrom, movTo, lot,MedicalStockIoOperations.MovementOrder.TYPE);
							break;
					}
					} catch (OHServiceException exception) {
						OHServiceExceptionUtil.showMessages(exception);
						return;
					}
					ArrayList<Movement4Print> pMovements2 = convertToPrint(pMovements);
					try {
						printManager.print(path,pMovements2,format);
					} catch (OHServiceException e1) {
						JOptionPane.showMessageDialog(MedicalStockSelection.this, e1.getMessage());
					}
				}
			}
		});
		return okButton;
	}
	private ArrayList<Movement4Print> convertToPrint(ArrayList<Movement> movements){
		ArrayList<Movement4Print> toPrint= new ArrayList<>();
		for(Movement movement:movements){
			toPrint.add(new Movement4Print(movement));
		}
		return toPrint;
	}
	private JButton getCloseButton() {
		closeButton = new JButton("Close");
		closeButton.setMnemonic(KeyEvent.VK_C);
		closeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		return closeButton;
	}
	public void actionPerformed(ActionEvent e) {
		formatSelected = e.getActionCommand();
	}
}
