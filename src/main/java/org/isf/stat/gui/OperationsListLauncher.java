package org.isf.stat.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Rectangle;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.Border;

import org.isf.generaldata.MessageBundle;
import org.isf.stat.gui.report.OperationsList;
import org.isf.utils.jobjects.ModalJFrame;

public class OperationsListLauncher extends ModalJFrame {

	private static final long serialVersionUID = 1L;

	private JPanel jPanel = null;
	private JPanel jButtonPanel = null;
	private JButton jCloseButton = null;
	private JPanel jContentPanel = null;
	private JButton jReport1Button = null;

	/**
	 * This is the default constructor
	 */
	public OperationsListLauncher() {
		super();
		this.setResizable(false);
		initialize();
		setVisible(true);
	}

	/**
	 * This method initializes this
	 */
	private void initialize() {
		this.setTitle(MessageBundle.getMessage("angal.stat.operationreport.title"));
		this.setContentPane(getJPanel());
		pack();
		setLocationRelativeTo(null);
	}

	/**
	 * This method initializes jPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel() {
		if (jPanel == null) {
			jPanel = new JPanel();
			jPanel.setLayout(new BorderLayout());
			jPanel.add(getJButtonPanel(), BorderLayout.SOUTH);
			jPanel.add(getJContentPanel(), BorderLayout.CENTER);
		}
		return jPanel;
	}

	/**
	 * This method initializes jButtonPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJButtonPanel() {
		if (jButtonPanel == null) {
			jButtonPanel = new JPanel();
			jButtonPanel.setLayout(new FlowLayout());
			jButtonPanel.add(getJCloseButton(), null);
		}
		return jButtonPanel;
	}

	/**
	 * This method initializes jCloseButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJCloseButton() {
		if (jCloseButton == null) {
			jCloseButton = new JButton(MessageBundle.getMessage("angal.common.close.btn"));
			jCloseButton.setMnemonic(MessageBundle.getMnemonic("angal.common.close.btn.key"));
			jCloseButton.addActionListener(actionEvent -> dispose());
		}
		return jCloseButton;
	}

	/**
	 * This method initializes jContentPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJContentPanel() {
		if (jContentPanel == null) {
			
			jContentPanel = new JPanel();
			jContentPanel.setLayout(new BorderLayout());
			
			JPanel up = new JPanel(new FlowLayout(FlowLayout.LEFT));
			up = setMyBorder(up, MessageBundle.getMessage("angal.stat.operationlist"));
			up.add(getJReport1Button());

			jContentPanel.add(up, BorderLayout.NORTH);
		}
		return jContentPanel;
	}
	
	/**
	 * This method initializes jReport1Button	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJReport1Button() {
		if (jReport1Button == null) {
			jReport1Button = new JButton(MessageBundle.getMessage("angal.stat.runoperationslistbytype.btn"));
			jReport1Button.setMnemonic(MessageBundle.getMnemonic("angal.stat.runoperationslistbytype.btn.key"));
			jReport1Button.setBounds(new Rectangle(15, 15, 130, 31));
			jReport1Button.addActionListener(actionEvent -> new OperationsList());
		}
		return jReport1Button;
	}

	
	/**
	 * Set a specific border+title to a panel
	 */
	private JPanel setMyBorder(JPanel panel, String title) {
		Border border = BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder(title), BorderFactory.createEmptyBorder(0, 0, 0, 0));
		panel.setBorder(border);
		return panel;
	}

}  

