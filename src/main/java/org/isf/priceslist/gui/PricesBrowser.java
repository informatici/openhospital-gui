/*
 * Open Hospital (www.open-hospital.org)
 * Copyright © 2006-2021 Informatici Senza Frontiere (info@informaticisenzafrontiere.org)
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
package org.isf.priceslist.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;

import org.isf.exa.manager.ExamBrowsingManager;
import org.isf.exa.model.Exam;
import org.isf.generaldata.MessageBundle;
import org.isf.medicals.manager.MedicalBrowsingManager;
import org.isf.medicals.model.Medical;
import org.isf.menu.manager.Context;
import org.isf.operation.manager.OperationBrowserManager;
import org.isf.operation.model.Operation;
import org.isf.priceslist.manager.PriceListManager;
import org.isf.priceslist.model.Price;
import org.isf.priceslist.model.PriceList;
import org.isf.pricesothers.manager.PricesOthersManager;
import org.isf.pricesothers.model.PricesOthers;
import org.isf.serviceprinting.manager.PrintManager;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.ModalJFrame;
import org.isf.utils.treetable.JTreeTable;

/**
 * Browsing of table PriceList
 *
 * @author Alessandro
 */
public class PricesBrowser extends ModalJFrame {

	private static final long serialVersionUID = 1L;
	private JPanel jPanelNorth;
	private JComboBox jComboBoxLists;
	private JScrollPane jScrollPaneList;
	private JTreeTable jTreeTable;
	private JPanel jPanelButtons;
	private JButton jButtonSave;
	private JButton jButtonCancel;
	private JLabel jLabelDescription;
	private JPanel jPanelSelection;
	private JPanel jPanelConfig;
	private JButton jButtonManage;
	private JButton jPrintTableButton;
	private JPanel jPanelDescription;
    protected static String[] cCategories = {"EXA","OPE","MED","OTH"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    protected static String[] cCategoriesNames = {MessageBundle.getMessage("angal.priceslist.exams"),MessageBundle.getMessage("angal.priceslist.operations"),MessageBundle.getMessage("angal.priceslist.medicals"),MessageBundle.getMessage("angal.priceslist.others")}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    private boolean[] columnsResizable = {true, false};
	private int[] columnWidth = {400,150};
    
	private PriceListManager listManager = Context.getApplicationContext().getBean(PriceListManager.class);
	private ArrayList<PriceList> listArray;
	private ArrayList<Price> priceArray;
	private PriceList listSelected;
	
	private PriceNode examNodes;
	private ExamBrowsingManager examManager = Context.getApplicationContext().getBean(ExamBrowsingManager.class);
    private ArrayList<Exam> examArray;
    
    private PriceNode opeNodes;
    private OperationBrowserManager operManager = Context.getApplicationContext().getBean(OperationBrowserManager.class);
    private ArrayList<Operation> operArray;
       
    private PriceNode medNodes;
    private MedicalBrowsingManager mediManager = Context.getApplicationContext().getBean(MedicalBrowsingManager.class);
    private ArrayList<Medical> mediArray;
    
    private PriceNode othNodes;
    private PricesOthersManager othManager = Context.getApplicationContext().getBean(PricesOthersManager.class);
    private ArrayList<PricesOthers> othArray;
    
    private PrintManager printManager = Context.getApplicationContext().getBean(PrintManager.class);
	
	public PricesBrowser() {
		try {
			mediArray = mediManager.getMedicals();
			examArray = examManager.getExams();
			operArray = operManager.getOperation();
			listArray = listManager.getLists();
			priceArray = listManager.getPrices();
			othArray = othManager.getOthers();
		}catch(OHServiceException e){
			OHServiceExceptionUtil.showMessages(e);
		}
		initComponents();
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setLocationRelativeTo(null);
		setVisible(true);
	}

	private void initComponents() {
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setFont(new Font("Dialog", Font.PLAIN, 12)); //$NON-NLS-1$
		setForeground(Color.black);
		checkLists();
		add(getJPanelNorth(), BorderLayout.NORTH);
		add(getJScrollPaneList(), BorderLayout.CENTER);
		add(getJPanelButtons(), BorderLayout.SOUTH);
		setTitle(MessageBundle.getMessage("angal.priceslist.pricebrowser.title"));
		setSize(647, 440);
	}

	private void checkLists() {
		if (listArray.isEmpty()) {
			MessageDialog.error(null, "angal.priceslist.pleasecreatealistfirst");
			ListBrowser browseList = new ListBrowser();
			browseList.setVisible(true);
			dispose();
		}
	}
	
	private JButton getPrintTableButton() {
		if (jPrintTableButton == null) {
			jPrintTableButton = new JButton(MessageBundle.getMessage("angal.priceslist.printing.btn"));
			jPrintTableButton.setMnemonic(MessageBundle.getMnemonic("angal.priceslist.printing.btn.key"));
			jPrintTableButton.setVisible(true);
			jPrintTableButton.addActionListener(actionEvent -> {

				try {
					printManager.print("PriceList", listManager.convertPrice(listSelected, priceArray), 0);
				} catch (OHServiceException e) {
					OHServiceExceptionUtil.showMessages(e, PricesBrowser.this);
				}
			});
		}
		return jPrintTableButton;
	}

	private JButton getJButtonManage() {
		if (jButtonManage == null) {
			jButtonManage = new JButton(MessageBundle.getMessage("angal.priceslist.managelists.btn"));
			jButtonManage.setMnemonic(MessageBundle.getMnemonic("angal.priceslist.managelists.btn.key"));
			//jButtonManage.setEnabled(false);
			jButtonManage.addActionListener(actionEvent -> {
					ListBrowser browseList = new ListBrowser();
					browseList.setVisible(true);
					dispose();
			});
			
		}
		return jButtonManage;
	}

	private JPanel getJPanelConfig() {
		if (jPanelConfig == null) {
			jPanelConfig = new JPanel();
			jPanelConfig.setLayout(new FlowLayout(FlowLayout.RIGHT));
			jPanelConfig.add(getJButtonManage());
		}
		return jPanelConfig;
	}

	protected void updateDescription() {
		jLabelDescription.setText(getTextDescription());
		
	}

	private JPanel getJPanelSelection() {
		if (jPanelSelection == null) {
			jPanelSelection = new JPanel();
			jPanelSelection.setLayout(new FlowLayout(FlowLayout.LEFT));
			jPanelSelection.add(getJComboBoxLists());
		}
		return jPanelSelection;
	}

	private JLabel getJLabelDescription() {
		if (jLabelDescription == null) {
			jLabelDescription = new JLabel();
			jLabelDescription.setText(getTextDescription());
		}
		return jLabelDescription;
	}

	private String getTextDescription() {
		String desc = listSelected.getDescription().toUpperCase()+
		  " ("+ //$NON-NLS-1$
		  listSelected.getCurrency()+")"; //$NON-NLS-1$
		return desc;
	}

	private JButton getJButtonCancel() {
		if (jButtonCancel == null) {
			jButtonCancel = new JButton(MessageBundle.getMessage("angal.common.cancel.btn"));
			jButtonCancel.setMnemonic(MessageBundle.getMnemonic("angal.common.cancel.btn.key"));
			jButtonCancel.addActionListener(actionEvent -> dispose());
		}
		return jButtonCancel;
	}

	private JButton getJButtonSave() {
		if (jButtonSave == null) {
			jButtonSave = new JButton(MessageBundle.getMessage("angal.common.save.btn"));
			jButtonSave.setMnemonic(MessageBundle.getMnemonic("angal.common.save.btn.key"));
			jButtonSave.addActionListener(actionEvent -> {
				int option = JOptionPane.showConfirmDialog(null,
						MessageBundle.getMessage("angal.priceslist.thiswillsavecurrentpricescontinue"),  //$NON-NLS-1$
						MessageBundle.getMessage("angal.priceslist.savelist"),  //$NON-NLS-1$
						JOptionPane.OK_CANCEL_OPTION);

				if (option == 0) {

					ArrayList<Price> updateList = new ArrayList<>();
					updateList = convertTreeToArray();
					boolean updated = false;
					try {
						updated = listManager.updatePrices(listSelected, updateList);
					} catch (OHServiceException e) {
						OHServiceExceptionUtil.showMessages(e);
					}

					if (updated) {
						MessageDialog.info(null, "angal.priceslist.listsaved");
						updateFromDB();
						PriceNode root = getTreeContent();
						jTreeTable.setModel(new PriceModel(root));
						jTreeTable.getTree().expandRow(3);
						jTreeTable.getTree().expandRow(2);
						jTreeTable.getTree().expandRow(1);
						validate();
						repaint();
					} else {
						MessageDialog.error(null, "angal.priceslist.listcouldnotbesaved");
					}
				}
			});
		}
		return jButtonSave;
	}

	private ArrayList<Price> convertTreeToArray() {
		
		ArrayList<Price> listPrices = new ArrayList<>();
		for (int i=0; i<examNodes.getItems().length; i++) {
			
			PriceNode newPriceNode = (PriceNode)examNodes.getItems()[i];
			listPrices.add(newPriceNode.getPrice());
		}
		for (int i=0; i<opeNodes.getItems().length; i++) {
			
			PriceNode newPriceNode = (PriceNode)opeNodes.getItems()[i];
			listPrices.add(newPriceNode.getPrice());
		}
		for (int i=0; i<medNodes.getItems().length; i++) {
			
			PriceNode newPriceNode = (PriceNode)medNodes.getItems()[i];
			listPrices.add(newPriceNode.getPrice());
		}
		for (int i=0; i<othNodes.getItems().length; i++) {
			
			PriceNode newPriceNode = (PriceNode)othNodes.getItems()[i];
			listPrices.add(newPriceNode.getPrice());
		}
		return listPrices;
	}

	private JPanel getJPanelButtons() {
		if (jPanelButtons == null) {
			jPanelButtons = new JPanel();
			jPanelButtons.add(getJButtonSave());
			jPanelButtons.add(getPrintTableButton());
			jPanelButtons.add(getJButtonCancel());
		}
		return jPanelButtons;
	}

	private JTreeTable getJTreeList() {
		if (jTreeTable == null) {
			
			updateFromDB();
		    PriceNode root = getTreeContent();
		    
		    jTreeTable = new JTreeTable(new PriceModel(root));
		    
		    jTreeTable.getTree().expandRow(4);
		    jTreeTable.getTree().expandRow(3);
		    jTreeTable.getTree().expandRow(2);
		    jTreeTable.getTree().expandRow(1);
		    
		    for (int i = 0; i< columnWidth.length; i++){
		    	jTreeTable.getColumnModel().getColumn(i).setMinWidth(columnWidth[i]);
		    	
		    	if (!columnsResizable[i]) {
				    jTreeTable.getColumnModel().getColumn(i).setMaxWidth(columnWidth[i]);
			    }
			}
		    jTreeTable.setAutoCreateColumnsFromModel(false); 
		    
		}
		    
		return jTreeTable;
	}

	private void updateFromDB() {
		
	    try {
	    	listArray = listManager.getLists();
	    	priceArray = listManager.getPrices();
	    	examArray = examManager.getExams();
			operArray = operManager.getOperation();
			mediArray = mediManager.getMedicals();
			othArray = othManager.getOthers();
	    }catch(OHServiceException e){
	    	OHServiceExceptionUtil.showMessages(e);
		}
	}

	private PriceNode getTreeContent() {
		
		HashMap<String,Price> priceHashTable = new HashMap<>();
	    for (Price price : priceArray) {
	    	priceHashTable.put(price.getList().getId()+
	    					  price.getGroup()+
	    					  price.getItem(), price);
	    }
	    
	    examNodes = new PriceNode(new Price(null,"","",cCategoriesNames[0],null)); //$NON-NLS-1$ //$NON-NLS-2$
	    for(Exam exa: examArray){
	    	Price p = priceHashTable.get(listSelected.getId()+cCategories[0]+exa.getCode());
	    	double priceValue = p != null ? p.getPrice() : 0.;
		    examNodes.addItem(new PriceNode(new Price(null, cCategories[0], exa.getCode(), exa.getDescription(), priceValue)));
	    }
	    
	    opeNodes = new PriceNode(new Price(null,"","",cCategoriesNames[1],null)); //$NON-NLS-1$ //$NON-NLS-2$
	    for(Operation ope: operArray){
	    	Price p = priceHashTable.get(listSelected.getId()+cCategories[1]+ope.getCode());
	    	double priceValue = p != null ? p.getPrice() : 0.;
		    opeNodes.addItem(new PriceNode(new Price(null, cCategories[1], ope.getCode(), ope.getDescription(), priceValue)));
	    }
	    
	    medNodes = new PriceNode(new Price(null,"","",cCategoriesNames[2],null)); //$NON-NLS-1$ //$NON-NLS-2$
	    for(Medical med: mediArray){
	    	Price p = priceHashTable.get(listSelected.getId()+cCategories[2]+med.getCode().toString());
	    	double priceValue = p != null ? p.getPrice() : 0.;
		    medNodes.addItem(new PriceNode(new Price(null, cCategories[2], med.getCode().toString(), med.getDescription(), priceValue)));
	    }
	    
	    othNodes = new PriceNode(new Price(null,"","",cCategoriesNames[3],null)); //$NON-NLS-1$ //$NON-NLS-2$
	    for(PricesOthers oth: othArray){
	    	Price p = priceHashTable.get(listSelected.getId()+cCategories[3]+oth.getId());
	    	double priceValue = p != null ? p.getPrice() : 0.;
	    	othNodes.addItem(new PriceNode(new Price(null, cCategories[3], Integer.toString(oth.getId()), oth.getDescription(), priceValue, !oth.isUndefined())));
	    }
	    
	    PriceNode root = new PriceNode(new Price(null,"","",listSelected.getName(),null)); //$NON-NLS-1$ //$NON-NLS-2$
	    root.addItem(examNodes);
	    root.addItem(opeNodes);
	    root.addItem(medNodes);
	    root.addItem(othNodes);
	    
	    return root;
	}

	private JScrollPane getJScrollPaneList() {
		if (jScrollPaneList == null) {
			jScrollPaneList = new JScrollPane();
			jScrollPaneList.setViewportView(getJTreeList());
		}
		return jScrollPaneList;
	}

	private JComboBox getJComboBoxLists() {
		if (jComboBoxLists == null) {
			jComboBoxLists = new JComboBox();
			//jComboBoxLists.setModel(new DefaultComboBoxModel(new Object[] { "item0", "item1", "item2", "item3" }));
			for (PriceList elem : listArray) {
				
				jComboBoxLists.addItem(elem);
			}
			jComboBoxLists.addActionListener(actionEvent -> {

				int option = JOptionPane.showConfirmDialog(null,
						MessageBundle.getMessage("angal.priceslist.doyoureallywanttochangelist"),  //$NON-NLS-1$
						MessageBundle.getMessage("angal.priceslist.changelist"),  //$NON-NLS-1$
						JOptionPane.OK_CANCEL_OPTION);

				if (option == 0) {
					listSelected = (PriceList) jComboBoxLists.getSelectedItem();

					PriceNode root = getTreeContent();
					jTreeTable.setModel(new PriceModel(root));
					jTreeTable.getTree().expandRow(3);
					jTreeTable.getTree().expandRow(2);
					jTreeTable.getTree().expandRow(1);

					updateDescription();
					validate();
					repaint();
				} else {
					jComboBoxLists.setSelectedItem(listSelected);
				}
			});
			listSelected = (PriceList) jComboBoxLists.getSelectedItem();
			jComboBoxLists.setDoubleBuffered(false);
			jComboBoxLists.setBorder(null);
		}
		return jComboBoxLists;
	}

	private JPanel getJPanelNorth() {
		if (jPanelNorth == null) {
			jPanelNorth = new JPanel();
			jPanelNorth.setLayout(new BoxLayout(jPanelNorth, BoxLayout.X_AXIS));
			jPanelNorth.add(getJPanelSelection());
			jPanelNorth.add(getJPanelDescription());
			jPanelNorth.add(getJPanelConfig());
		}
		return jPanelNorth;
	}

	private JPanel getJPanelDescription() {
		if (jPanelDescription == null) {
			jPanelDescription = new JPanel();
			//jPanelDescription.setLayout(new FlowLayout(FlowLayout.CENTER));
			jPanelDescription.add(getJLabelDescription());
		}
		return jPanelDescription;
	}
}
