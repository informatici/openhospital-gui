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
package org.isf.lab.gui.elements;

import java.awt.Dimension;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.isf.exa.model.ExamRow;
import org.isf.generaldata.MessageBundle;
import org.isf.lab.model.LaboratoryRow;

public class ExamRowSubPanel extends JPanel {

	private static final long serialVersionUID = -8847689740511562992L;

	private static final String POSITIVE_ABBR_TXT = MessageBundle.getMessage("angal.lab.positiveabbr.btn");
	private static final String NEGATIVE_ABBR_TXT = MessageBundle.getMessage("angal.lab.negativeabbr.btn");
	private static final Dimension LABEL_SIZE = new Dimension(175, 20);
	private static final Dimension SUBPANEL_SIZE = new Dimension(500, 25);

	private JRadioButton radioPos;

	public static ExamRowSubPanel forExamRow(ExamRow r) {
		return new ExamRowSubPanel(r, NEGATIVE_ABBR_TXT);
	}

	public static ExamRowSubPanel forExamRowAndLaboratoryRows(ExamRow r, List<LaboratoryRow> lRows) {
		return lRows.stream()
				.filter(laboratoryRow -> r.getDescription().equalsIgnoreCase(laboratoryRow.getDescription()))
				.findFirst()
				.map(laboratoryRow -> new ExamRowSubPanel(r, POSITIVE_ABBR_TXT))
				.orElse(new ExamRowSubPanel(r, NEGATIVE_ABBR_TXT));
	}

	private ExamRowSubPanel(ExamRow row, String result) {
		JLabel label = new JLabel(row.getDescription());
		label.setMinimumSize(LABEL_SIZE);
		label.setPreferredSize(LABEL_SIZE);
		this.add(label);

		ButtonGroup group = new ButtonGroup();
		radioPos = new JRadioButton(POSITIVE_ABBR_TXT);
		JRadioButton radioNeg = new JRadioButton(NEGATIVE_ABBR_TXT);
		group.add(radioPos);
		group.add(radioNeg);

		this.add(radioPos);
		this.add(radioNeg);
		if (result.equals(POSITIVE_ABBR_TXT)) {
			radioPos.setSelected(true);
		} else {
			radioNeg.setSelected(true);
		}
		setMaximumSize(SUBPANEL_SIZE);
		setPreferredSize(SUBPANEL_SIZE);
	}

	public String getSelectedResult() {
		if (radioPos.isSelected()) {
			return POSITIVE_ABBR_TXT;
		}
		return NEGATIVE_ABBR_TXT;
	}

}
