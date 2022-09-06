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
package org.isf.lab.gui.elements;

import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.isf.exa.model.ExamRow;
import org.isf.generaldata.MessageBundle;
import org.isf.lab.model.LaboratoryRow;

public class ExamRowSubPanel extends JPanel {

	private static final long serialVersionUID = -8847689740511562992L;

	private JLabel label;
	private JRadioButton radioPos;
	private JRadioButton radioNeg;
	private ButtonGroup group;

	public static ExamRowSubPanel forExamRow(ExamRow r) {
		return new ExamRowSubPanel(r, "N");
	}

	public static ExamRowSubPanel forExamRowAndLaboratoryRows(ExamRow r, List<LaboratoryRow> lRows) {
		return lRows.stream()
				.filter(laboratoryRow -> r.getDescription().equalsIgnoreCase(laboratoryRow.getDescription()))
				.findFirst()
				.map(laboratoryRow -> new ExamRowSubPanel(r, MessageBundle.getMessage("angal.lab.positiveabbr.btn")))
				.orElse(new ExamRowSubPanel(r, MessageBundle.getMessage("angal.lab.negativeabbr.btn")));
	}

	private ExamRowSubPanel(ExamRow row, String result) {
		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		label = new JLabel(row.getDescription());
		this.add(label);

		group = new ButtonGroup();
		radioPos = new JRadioButton(MessageBundle.getMessage("angal.lab.positiveabbr.btn"));
		radioNeg = new JRadioButton(MessageBundle.getMessage("angal.lab.negativeabbr.btn"));
		group.add(radioPos);
		group.add(radioNeg);

		this.add(radioPos);
		this.add(radioNeg);
		if (result.equals(MessageBundle.getMessage("angal.lab.positiveabbr.btn"))) {
			radioPos.setSelected(true);
		} else {
			radioNeg.setSelected(true);
		}
	}

	public String getSelectedResult() {
		if (radioPos.isSelected()) {
			return MessageBundle.getMessage("angal.lab.positiveabbr.btn");
		}
		return MessageBundle.getMessage("angal.lab.negativeabbr.btn");
	}

}
