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
package org.isf.sms.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import org.isf.generaldata.GeneralData;
import org.isf.generaldata.MessageBundle;
import org.isf.menu.manager.Context;
import org.isf.sms.manager.SmsManager;
import org.isf.sms.model.Sms;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.CustomJDateChooser;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.ModalJFrame;
import org.joda.time.DateMidnight;
import org.joda.time.DateTime;

/**
 * @author Mwithi
 */
public class SmsBrowser extends ModalJFrame {

	private static final long serialVersionUID = 1L;
	
	private JTable jSmsTable;
	private JPanel jButtonPanel;
	private JButton jCloseButton;
	private JButton jDeleteButton;
	private JButton jNewButton;
	
	private String[] columnNames = {
			MessageBundle.getMessage("angal.common.date.txt").toUpperCase(),
			MessageBundle.getMessage("angal.sms.scheduleddate.col").toUpperCase(),
			MessageBundle.getMessage("angal.common.telephone.txt").toUpperCase(),
			MessageBundle.getMessage("angal.common.sms.txt").toUpperCase(),
			MessageBundle.getMessage("angal.sms.sent.col").toUpperCase()
	};
	private Object[] columnClasses = {Date.class, Date.class, String.class, String.class, Date.class};
	private int[] columnPreferredSize = {110, 110, 150, 100, 110};
	private boolean[] columnResizable = {false, false, false, true, false};
	private int width;
	private int eight;

	private SmsManager smsManager = Context.getApplicationContext().getBean(SmsManager.class);
	private List<Sms> smsList = null;
	
	private final DateTime dateTimeAtStartOfToday = new DateTime(new DateMidnight());  
	private final DateTime dateTimeAtEndOfToday = new DateTime((new DateMidnight()).plusDays(1));

	private SmsTableModel model;
	private JPanel jFilterPanel;
	private CustomJDateChooser jFromDateChooser;
	private CustomJDateChooser jToDateChooser;
	
	private Date dateFrom;
	private Date dateTo;
	private JLabel jDateFromLabel;
	private JLabel jDateToLabel;

	/**
	 * Create the frame.
	 */
	public SmsBrowser() {
		initVariables();
		initComponents();
	}

	private void initVariables() {
		dateFrom = dateTimeAtStartOfToday.toDate();
		dateTo = dateTimeAtEndOfToday.toDate();
		updateModel(dateFrom, dateTo);
		for (int size : columnPreferredSize) {
			width += size;
		}
		eight = width * 3 / 4;
	}

	private void initComponents() {
		setTitle(MessageBundle.getMessage("angal.sms.smsmanager.title"));
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		getContentPane().add(getJButtonPanel(), BorderLayout.SOUTH);
		JScrollPane scrollPane = new JScrollPane(getJSmsTable());
		getContentPane().add(scrollPane, BorderLayout.CENTER);
		getContentPane().add(getJFilterPanel(), BorderLayout.NORTH);
		setPreferredSize(new Dimension(width, eight));
		pack();
		setLocationRelativeTo(null);
	}
	
	private JPanel getJFilterPanel() {
		if (jFilterPanel == null) {
			jFilterPanel = new JPanel();
			jFilterPanel.add(getJDateFromLabel());
			jFilterPanel.add(getJFromDateChooser());
			jFilterPanel.add(getJDateToLabel());
			jFilterPanel.add(getJToDateChooser());
			
		}
		return jFilterPanel;
	}
	
	private JLabel getJDateFromLabel() {
		if (jDateFromLabel == null) {
			jDateFromLabel = new JLabel(MessageBundle.getMessage("angal.common.from.txt"));
		}
		return jDateFromLabel;
	}
	
	private JLabel getJDateToLabel() {
		if (jDateToLabel == null) {
			jDateToLabel = new JLabel(MessageBundle.getMessage("angal.common.to.txt"));
		}
		return jDateToLabel;
	}
	
	private CustomJDateChooser getJFromDateChooser() {
		if (jFromDateChooser == null) {
			jFromDateChooser = new CustomJDateChooser();
			jFromDateChooser.setLocale(new Locale(GeneralData.LANGUAGE));
			jFromDateChooser.setDate(dateTimeAtStartOfToday.toDate());
			jFromDateChooser.setDateFormatString("dd/MM/yy"); //$NON-NLS-1$
			jFromDateChooser.addPropertyChangeListener("date", new PropertyChangeListener() { //$NON-NLS-1$

				@Override
				public void propertyChange(PropertyChangeEvent evt) {
					dateFrom = (Date) evt.getNewValue();
					updateModel(dateFrom, dateTo);
					updateGUI();
					
				} 
			});
		}
		return jFromDateChooser;
	}
	
	private CustomJDateChooser getJToDateChooser() {
		if (jToDateChooser == null) {
			jToDateChooser = new CustomJDateChooser();
			jToDateChooser.setLocale(new Locale(GeneralData.LANGUAGE));
			jToDateChooser.setDate(dateTimeAtEndOfToday.toDate());
			jToDateChooser.setDateFormatString("dd/MM/yy"); //$NON-NLS-1$
			jToDateChooser.addPropertyChangeListener("date", new PropertyChangeListener() { //$NON-NLS-1$

				@Override
				public void propertyChange(PropertyChangeEvent evt) {
					dateTo = (Date) evt.getNewValue();
					updateModel(dateFrom, dateTo);
					updateGUI();
				} 
			});
		}
		return jToDateChooser;
	}

	private JTable getJSmsTable() {
		if (jSmsTable == null) {
			model = new SmsTableModel();
			jSmsTable = new JTable(model);
			jSmsTable.setDefaultRenderer(Object.class, new ColorTableCellRenderer());
			jSmsTable.setDefaultRenderer(Date.class, new ColorTableCellRenderer());
			for (int i = 0; i < columnNames.length; i++) {
				jSmsTable.getColumnModel().getColumn(i).setPreferredWidth(columnPreferredSize[i]);
				if (!columnResizable[i]) jSmsTable.getColumnModel().getColumn(i).setMaxWidth(columnPreferredSize[i]);
			}
			jSmsTable.addMouseListener(new MouseListener() {
				
				public void mouseReleased(MouseEvent e) {}
				
				public void mousePressed(MouseEvent e) {}
				
				public void mouseExited(MouseEvent e) {}
				
				public void mouseEntered(MouseEvent e) {}
				
				public void mouseClicked(MouseEvent e) {
					if (e.getClickCount() == 2 && !e.isConsumed()) {
						e.consume();
						showDetail();
					}
				}
			});
			
		}
		return jSmsTable;
	}
	
	private void showDetail() {
		int row = jSmsTable.getSelectedRow();
		Sms sms = (Sms) jSmsTable.getValueAt(row, -1);
		JTextArea textArea = new JTextArea(8, 20);
		textArea.setWrapStyleWord(true);
		textArea.setLineWrap(true);
		textArea.setText(sms.getSmsText());
		JOptionPane.showMessageDialog(SmsBrowser.this, textArea);
	}

	private JPanel getJButtonPanel() {
		if (jButtonPanel == null) {
			jButtonPanel = new JPanel();
			jButtonPanel.add(getJNewButton());
			jButtonPanel.add(getJDeleteButton());
			jButtonPanel.add(getJCloseButton());
			
		}
		return jButtonPanel;
	}
	
	private JButton getJNewButton() {
		if (jNewButton == null) {
			jNewButton = new JButton(MessageBundle.getMessage("angal.common.new.btn"));
			jNewButton.setMnemonic(MessageBundle.getMnemonic("angal.common.new.btn.key"));
			jNewButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					new SmsEdit(SmsBrowser.this);
					updateModel(dateFrom, dateTo);
					updateGUI();
				}
			});
		}
		return jNewButton;
	}
	
	private JButton getJDeleteButton() {
		if (jDeleteButton == null) {
			jDeleteButton = new JButton(MessageBundle.getMessage("angal.common.delete.btn"));
			jDeleteButton.setMnemonic(MessageBundle.getMnemonic("angal.common.delete.btn.key"));
			jDeleteButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int[] indexes = jSmsTable.getSelectedRows();
					if (indexes.length == 0) {
						MessageDialog.error(null, "angal.common.pleaseselectarow.msg");
					} else {
						ArrayList<Sms> smsList = new ArrayList<>();
						int answer = MessageDialog.yesNo(null, "angal.sms.deletetheselectedsms.msg");
						if (answer == JOptionPane.YES_OPTION) {
							for (int i : indexes) {
								Sms sms = (Sms) jSmsTable.getValueAt(i, -1);
								smsList.add(sms);
							}
						}
						
						try {
							smsManager.delete(smsList);
						} catch (OHServiceException e1) {
							OHServiceExceptionUtil.showMessages(e1, SmsBrowser.this);
						}
						updateModel(dateFrom, dateTo);
						updateGUI();
					}
				}
			});
		}
		return jDeleteButton;
	}
	
	private void updateModel(Date from, Date to) {
		try {
			smsList = smsManager.getAll(from, to);
		} catch (OHServiceException e) {
			OHServiceExceptionUtil.showMessages(e, SmsBrowser.this);
		}
	}
	
	private void updateGUI() {
		model.fireTableDataChanged();
	}

	private JButton getJCloseButton() {
		if (jCloseButton == null) {
			jCloseButton = new JButton(MessageBundle.getMessage("angal.common.close.btn"));
			jCloseButton.setMnemonic(MessageBundle.getMnemonic("angal.common.close.btn.key"));
			jCloseButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					dispose();
				}
			});
		}
		return jCloseButton;
	}
	
	class SmsTableModel extends DefaultTableModel {

		private static final long serialVersionUID = 1L;
		
		public SmsTableModel() {
			
		}

		public int getRowCount() {
			return smsList.size();
		}

		public int getColumnCount() {
			return columnNames.length;
		}

		public String getColumnName(int column) {
			return columnNames[column];
		}

		public boolean isCellEditable(int row, int column) {
			return false;
		}

		public Class<?> getColumnClass(int columnIndex) {
			return (Class<?>) columnClasses[columnIndex];
		}

		public Object getValueAt(int row, int column) {
			int c = -1;
			Sms sms = smsList.get(row);
			if (column == c) {
				return sms;
			} else if (column == ++c) {
				return formatDateTime(sms.getSmsDate());
			} else if (column == ++c) {
				return formatTodayDateTime(sms.getSmsDateSched());
			} else if (column == ++c) {
				return sms.getSmsNumber();
			} else if (column == ++c) {
				return sms.getSmsText();
			} else if (column == ++c) {
				return formatTodayDateTime(sms.getSmsDateSent());
			}
			return null;
		}
 	}
	
	class ColorTableCellRenderer extends DefaultTableCellRenderer {

		private static final long serialVersionUID = 1L;

		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			cell.setForeground(Color.BLACK);
			Sms sms = smsList.get(row);
			DateTime date = new DateTime(sms.getSmsDateSched().getTime());
			if (sms.getSmsDateSent() != null) cell.setForeground(Color.GRAY); // sent
			else if (date.isAfter(dateTimeAtEndOfToday) || date.isEqual(dateTimeAtEndOfToday)) cell.setForeground(Color.BLUE); // send tomorrow
			return cell;
		}
	}
	
	public String formatDateTime(Date smsDateSent) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		if (smsDateSent != null) return sdf.format(smsDateSent);
		return null;
	}
	
	public String formatTodayDateTime(Date smsDateSent) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		SimpleDateFormat sdfToday = new SimpleDateFormat("HH:mm");
		if (smsDateSent != null) {
			DateTime date = new DateTime(smsDateSent.getTime());
			if (date.isAfter(dateTimeAtStartOfToday) &&
					date.isBefore(dateTimeAtEndOfToday))
				return sdfToday.format(smsDateSent);
			return sdf.format(smsDateSent);
		}
		return null;
	}
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		GeneralData.initialize();
		MessageBundle.initialize();
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					SmsBrowser frame = new SmsBrowser();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
}
