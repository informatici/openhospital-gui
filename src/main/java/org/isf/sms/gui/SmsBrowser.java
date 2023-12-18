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
package org.isf.sms.gui;

import static org.isf.utils.Constants.DATE_FORMAT_DD_MM_YYYY_HH_MM;
import static org.isf.utils.Constants.TIME_FORMAT_HH_MM;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import org.isf.generaldata.MessageBundle;
import org.isf.menu.manager.Context;
import org.isf.sms.manager.SmsManager;
import org.isf.sms.model.Sms;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.GoodDateChooser;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.ModalJFrame;

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
	private int[] columnPreferredSize = {130, 130, 150, 200, 130};
	private boolean[] columnResizable = {false, false, false, true, false};
	private int width;
	private int eight;

	private SmsManager smsManager = Context.getApplicationContext().getBean(SmsManager.class);
	private List<Sms> smsList;

	private final LocalDateTime dateTimeAtStartOfToday = LocalDate.now().atStartOfDay();
	private final LocalDateTime dateTimeAtEndOfToday = LocalDate.now().atStartOfDay().plusDays(1);

	private SmsTableModel model;
	private JPanel jFilterPanel;
	private GoodDateChooser jFromDateChooser;
	private GoodDateChooser jToDateChooser;

	private LocalDateTime dateFrom;
	private LocalDateTime dateTo;
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
		dateFrom = dateTimeAtStartOfToday;
		dateTo = dateTimeAtEndOfToday;
		updateModel(dateFrom, dateTo);
		for (int size : columnPreferredSize) {
			width += size;
		}
		eight = width * 3 / 4;
	}

	private void initComponents() {
		setTitle(MessageBundle.getMessage("angal.sms.smsmanager.title"));
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
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
			jDateFromLabel = new JLabel(MessageBundle.getMessage("angal.common.datefrom.label"));
		}
		return jDateFromLabel;
	}
	
	private JLabel getJDateToLabel() {
		if (jDateToLabel == null) {
			jDateToLabel = new JLabel(MessageBundle.getMessage("angal.common.dateto.label"));
		}
		return jDateToLabel;
	}
	
	private GoodDateChooser getJFromDateChooser() {
		if (jFromDateChooser == null) {
			jFromDateChooser = new GoodDateChooser(LocalDate.now());
			jFromDateChooser.addDateChangeListener(dateChangeEvent -> {
				LocalDate newDate = dateChangeEvent.getNewDate();
				if (newDate != null) {
					dateFrom = newDate.atStartOfDay();
					updateModel(dateFrom, dateTo);
					updateGUI();
				}
			});
		}
		return jFromDateChooser;
	}
	
	private GoodDateChooser getJToDateChooser() {
		if (jToDateChooser == null) {
			jToDateChooser = new GoodDateChooser(LocalDate.now());
			jToDateChooser.addDateChangeListener(dateChangeEvent -> {
				LocalDate newDate = dateChangeEvent.getNewDate();
				if (newDate != null) {
					dateTo = newDate.atTime(LocalTime.MAX);
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
				if (!columnResizable[i]) {
					jSmsTable.getColumnModel().getColumn(i).setMaxWidth(columnPreferredSize[i]);
				}
			}
			jSmsTable.addMouseListener(new MouseListener() {
				
				@Override
				public void mouseReleased(MouseEvent e) {}
				
				@Override
				public void mousePressed(MouseEvent e) {}
				
				@Override
				public void mouseExited(MouseEvent e) {}
				
				@Override
				public void mouseEntered(MouseEvent e) {}
				
				@Override
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
		JOptionPane.showMessageDialog(this, textArea);
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
			jNewButton.addActionListener(actionEvent -> {
				new SmsEdit(this);
				updateModel(dateFrom, dateTo);
				updateGUI();
			});
		}
		return jNewButton;
	}
	
	private JButton getJDeleteButton() {
		if (jDeleteButton == null) {
			jDeleteButton = new JButton(MessageBundle.getMessage("angal.common.delete.btn"));
			jDeleteButton.setMnemonic(MessageBundle.getMnemonic("angal.common.delete.btn.key"));
			jDeleteButton.addActionListener(actionEvent -> {
				int[] indexes = jSmsTable.getSelectedRows();
				if (indexes.length == 0) {
					MessageDialog.error(null, "angal.common.pleaseselectarow.msg");
				} else {
					List<Sms> smsList = new ArrayList<>();
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
						OHServiceExceptionUtil.showMessages(e1, this);
					}
					updateModel(dateFrom, dateTo);
					updateGUI();
				}
			});
		}
		return jDeleteButton;
	}
	
	private void updateModel(LocalDateTime from, LocalDateTime to) {
		try {
			smsList = smsManager.getAll(from, to);
		} catch (OHServiceException e) {
			OHServiceExceptionUtil.showMessages(e, this);
		}
	}
	
	private void updateGUI() {
		model.fireTableDataChanged();
	}

	private JButton getJCloseButton() {
		if (jCloseButton == null) {
			jCloseButton = new JButton(MessageBundle.getMessage("angal.common.close.btn"));
			jCloseButton.setMnemonic(MessageBundle.getMnemonic("angal.common.close.btn.key"));
			jCloseButton.addActionListener(actionEvent -> dispose());
		}
		return jCloseButton;
	}
	
	class SmsTableModel extends DefaultTableModel {

		private static final long serialVersionUID = 1L;
		
		public SmsTableModel() {
			
		}

		@Override
		public int getRowCount() {
			return smsList.size();
		}

		@Override
		public int getColumnCount() {
			return columnNames.length;
		}

		@Override
		public String getColumnName(int column) {
			return columnNames[column];
		}

		@Override
		public boolean isCellEditable(int row, int column) {
			return false;
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			return (Class<?>) columnClasses[columnIndex];
		}

		@Override
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

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			cell.setForeground(Color.BLACK);
			Sms sms = smsList.get(row);
			if (sms.getSmsDateSent() != null) {
				cell.setForeground(Color.GRAY); // sent
			} else if (sms.getSmsDateSched().isAfter(dateTimeAtEndOfToday) || sms.getSmsDateSched().isEqual(dateTimeAtEndOfToday)) {
				cell.setForeground(Color.BLUE); // send tomorrow
			}
			return cell;
		}
	}
	
	private String formatDateTime(LocalDateTime smsDateSent) {
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DATE_FORMAT_DD_MM_YYYY_HH_MM);
		if (smsDateSent != null) {
			return dateTimeFormatter.format(smsDateSent);
		}
		return null;
	}
	
	private String formatTodayDateTime(LocalDateTime smsDateSent) {
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DATE_FORMAT_DD_MM_YYYY_HH_MM);
		DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern(TIME_FORMAT_HH_MM);
		if (smsDateSent != null) {
			if (smsDateSent.isAfter(dateTimeAtStartOfToday) && smsDateSent.isBefore(dateTimeAtEndOfToday)) {
				return timeFormatter.format(smsDateSent);
			}
			return dateTimeFormatter.format(smsDateSent);
		}
		return null;
	}
	
}
