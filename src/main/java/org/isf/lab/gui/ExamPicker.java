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
package org.isf.lab.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListSelectionModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableModel;

import org.isf.exa.model.Exam;
import org.isf.generaldata.MessageBundle;
import org.isf.priceslist.model.Price;
import org.isf.utils.jobjects.OhDefaultCellRenderer;
import org.isf.utils.jobjects.OhTableModelExam;

/**
 * @author julio
 */
public class ExamPicker extends JPanel {

	private JButton jButtonSelect;
	private JButton jButtonCancel;
	private JLabel jLabelImage;
	private JPanel jPanel1;
	private JPanel jPanel2;
	private JPanel jPanel3;
	private JScrollPane jScrollPane1;
	private JTable jTableData;
	private JTextField jTextFieldFind;

	OhDefaultCellRenderer cellRenderer = new OhDefaultCellRenderer();

	private static final long serialVersionUID = 1L;

	/**
	 * Creates new form ExamPicker
	 */
	public ExamPicker(TableModel model) {
		initComponents(model);
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
	private void initComponents(TableModel model) {

		jPanel3 = new JPanel();
		jPanel1 = new JPanel();

		jPanel3.setBackground(new java.awt.Color(240, 240, 240));

		jPanel1.setBackground(new java.awt.Color(240, 240, 240));

		setLayout(new BorderLayout(10, 10));
		add(jPanel1, BorderLayout.CENTER);
		GridBagLayout gblPanel1 = new GridBagLayout();
		gblPanel1.columnWidths = new int[] { 575, 0 };
		gblPanel1.rowHeights = new int[] { 268, 0 };
		gblPanel1.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gblPanel1.rowWeights = new double[] { 1.0, Double.MIN_VALUE };
		jPanel1.setLayout(gblPanel1);
		jScrollPane1 = new javax.swing.JScrollPane();
		jTableData = new javax.swing.JTable();

		jTableData.setSelectionModel(new DefaultListSelectionModel() {

			@Override
			public void setSelectionInterval(int index0, int index1) {
				if (super.isSelectedIndex(index0)) {
					super.removeSelectionInterval(index0, index1);
				} else {
					super.addSelectionInterval(index0, index1);
				}
			}
		});

		jTableData.setDefaultRenderer(Object.class, cellRenderer);
		jTableData.setDefaultRenderer(Double.class, cellRenderer);
		jTableData.addMouseMotionListener(new MouseMotionListener() {

			@Override
			public void mouseMoved(MouseEvent e) {
				// TODO Auto-generated method stub
				JTable aTable = (JTable) e.getSource();
				int itsRow = aTable.rowAtPoint(e.getPoint());
				if (itsRow >= 0) {
					cellRenderer.setHoveredRow(itsRow);
				} else {
					cellRenderer.setHoveredRow(-1);
				}
				aTable.repaint();
			}

			@Override
			public void mouseDragged(MouseEvent e) {
				// TODO Auto-generated method stub
			}
		});
		jTableData.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseExited(MouseEvent e) {
				cellRenderer.setHoveredRow(-1);
			}
		});

		jTableData.setModel(model);
		jTableData.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		jTableData.setShowVerticalLines(false);
		jTableData.addMouseListener(new java.awt.event.MouseAdapter() {

			@Override
			public void mouseClicked(java.awt.event.MouseEvent evt) {
				jTableDataMouseClicked(evt);
			}

			@Override
			public void mousePressed(java.awt.event.MouseEvent evt) {
				if (evt.getClickCount() == 2) {
					validateSelection();
				}
			}
		});

		jTableData.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					validateSelection();
				}
				super.keyPressed(e);
			}
		});

		jScrollPane1.setViewportView(jTableData);
		GridBagConstraints gbcScrollPane1 = new GridBagConstraints();
		gbcScrollPane1.insets = new Insets(0, 15, 0, 15);
		gbcScrollPane1.fill = GridBagConstraints.BOTH;
		gbcScrollPane1.gridx = 0;
		gbcScrollPane1.gridy = 0;
		jPanel1.add(jScrollPane1, gbcScrollPane1);
		add(jPanel3, BorderLayout.NORTH);
		GridBagLayout gblPanel3 = new GridBagLayout();
		gblPanel3.columnWidths = new int[] { 90, 237, 0 };
		gblPanel3.rowHeights = new int[] { 50, 0 };
		gblPanel3.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		gblPanel3.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		jPanel3.setLayout(gblPanel3);

		jLabelImage = new JLabel(MessageBundle.getMessage("angal.exams.find"));
		jLabelImage.setIcon(new javax.swing.ImageIcon("rsc/icons/operation_dialog.png"));
		GridBagConstraints gbcLabelImage = new GridBagConstraints();
		gbcLabelImage.anchor = GridBagConstraints.WEST;
		gbcLabelImage.insets = new Insets(0, 15, 0, 5);
		gbcLabelImage.gridx = 0;
		gbcLabelImage.gridy = 0;
		jPanel3.add(jLabelImage, gbcLabelImage);
		jTextFieldFind = new javax.swing.JTextField();

		jTextFieldFind.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void insertUpdate(DocumentEvent e) {
				jTableData.clearSelection();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				jTableData.clearSelection();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				jTableData.clearSelection();
			}

		});

		jTextFieldFind.setName("textRecherche");
		GridBagConstraints gbcTextFieldFind = new GridBagConstraints();
		gbcTextFieldFind.insets = new Insets(0, 0, 0, 15);
		gbcTextFieldFind.fill = GridBagConstraints.HORIZONTAL;
		gbcTextFieldFind.gridx = 1;
		gbcTextFieldFind.gridy = 0;
		jPanel3.add(jTextFieldFind, gbcTextFieldFind);

		jTextFieldFind.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void insertUpdate(DocumentEvent e) {
				String text = jTextFieldFind.getText();
				OhTableModelExam<Price> model = (OhTableModelExam<Price>) jTableData.getModel();
				model.filter(text);
				if (jTableData.getRowCount() > 0) {
					jTableData.setRowSelectionInterval(0, 0);
				}
				jTableData.repaint();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				String text = jTextFieldFind.getText();

				OhTableModelExam<Price> model = (OhTableModelExam<Price>) jTableData.getModel();
				model.filter(text);
				if (jTableData.getRowCount() > 0) {
					jTableData.setRowSelectionInterval(0, 0);
				}
				jTableData.repaint();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				throw new UnsupportedOperationException("Not supported yet.");
			}

		});

		jTextFieldFind.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				// TODO Auto-generated method stub
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					validateSelection();
				}
				super.keyPressed(e);
			}
		});

		jPanel2 = new JPanel();
		jPanel2.setBackground(new java.awt.Color(240, 240, 240));

		jButtonSelect = new JButton(MessageBundle.getMessage("angal.common.select.btn"));
		jButtonSelect.setMnemonic(MessageBundle.getMnemonic("angal.common.select.btn.key"));
		jButtonSelect.addMouseListener(new java.awt.event.MouseAdapter() {

			@Override
			public void mouseClicked(java.awt.event.MouseEvent evt) {
				jButtonSelectMouseClicked(evt);
			}
		});
		jButtonSelect.addActionListener(actionEvent -> jButtonSelectActionPerformed(actionEvent));

		jButtonCancel = new JButton(MessageBundle.getMessage("angal.common.cancel.btn"));
		jButtonCancel.setMnemonic(MessageBundle.getMnemonic("angal.common.cancel.btn.key"));
		jButtonCancel.addMouseListener(new java.awt.event.MouseAdapter() {

			@Override
			public void mouseClicked(java.awt.event.MouseEvent evt) {
				jButtonQuitMouseClicked(evt);
			}
		});

		add(jPanel2, BorderLayout.SOUTH);
		jPanel2.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		jPanel2.add(jButtonSelect);
		jPanel2.add(jButtonCancel);
	}

	private void jTableDataMouseClicked(java.awt.event.MouseEvent mouseEvent) {

	}

	private void validateSelection() {
		this.setSelectedRow(this.jTableData.getSelectedRow());
		this.setVisible(false);
		this.getParentFrame().dispose();
	}

	private void jButtonSelectActionPerformed(java.awt.event.ActionEvent evt) {
		this.setSelectedRow(this.jTableData.getSelectedRow());
		this.setVisible(false);
		this.getParentFrame().dispose();
	}

	private void jButtonSelectMouseClicked(java.awt.event.MouseEvent evt) {
		this.setSelectedRow(this.jTableData.getSelectedRow());
		this.setVisible(false);
		this.getParentFrame().dispose();
	}

	private void jButtonQuitMouseClicked(java.awt.event.MouseEvent evt) {
		this.setVisible(false);
		this.getParentFrame().dispose();
	}

	private int selectedRow = -1;

	private int getSelectedRow() {
		return selectedRow;
	}

	public Object getSelectedObject() {
		OhTableModelExam<?> model = (OhTableModelExam<?>) jTableData.getModel();
		return model.getObjectAt(this.getSelectedRow());
	}

	public List<Exam> getAllSelectedObject() {
		OhTableModelExam<?> model = (OhTableModelExam<?>) jTableData.getModel();
		List<Exam> exams = new ArrayList<>();
		int[] selectedRows = this.jTableData.getSelectedRows();

		for (int row : selectedRows) {
			exams.add(model.getObjectAt(row));
		}
		return exams;
	}

	private void setSelectedRow(int selectedRow) {
		this.selectedRow = selectedRow;
	}

	private JDialog parentFrame;

	public JDialog getParentFrame() {
		return parentFrame;
	}

	public void setParentFrame(JDialog parentFrame) {
		this.parentFrame = parentFrame;
	}

}
