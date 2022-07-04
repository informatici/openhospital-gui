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
package org.isf.operation.gui;

import java.awt.AWTEvent;
import java.util.ArrayList;
import java.util.List;

import org.isf.menu.gui.MainMenu;
import org.isf.opd.gui.OpdEditExtended;
import org.isf.opd.model.Opd;
import org.isf.operation.manager.OperationRowBrowserManager;
import org.isf.operation.model.Operation;
import org.isf.operation.model.OperationRow;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.OhTableOperationModel;

public class OperationRowOpd extends OperationRowBase implements OpdEditExtended.SurgeryListener {

	private static final long serialVersionUID = 1L;

	private Opd myOpd;

	public OperationRowOpd(Opd opd) {
		super();
		myOpd = opd;

		if (myOpd != null) {
			List<OperationRow> res = new ArrayList<>();
			try {
				res = opeRowManager.getOperationRowByOpd(myOpd);
			} catch (OHServiceException e1) {
				OHServiceExceptionUtil.showMessages(e1);
			}
			oprowData.addAll(res);
		}
		modelOhOpeRow = new OhTableOperationModel<>(oprowData);
		tableData.setModel(modelOhOpeRow);
	}

	@Override
	public void addToGrid() {
		if ((this.textDate.getLocalDateTime() == null) || (this.comboOperation.getSelectedItem() == null)) {
			MessageDialog.error(OperationRowOpd.this, "angal.operationrowedit.warningdateope");
			return;
		}

		OperationRow operationRow = new OperationRow();
		operationRow.setOpDate(this.textDate.getLocalDateTime());
		if (this.comboResult.getSelectedItem() != null) {
			String opResult = opeManager.getResultDescriptionKey((String) comboResult.getSelectedItem());
			operationRow.setOpResult(opResult);
		} else {
			operationRow.setOpResult(""); //$NON-NLS-1$
		}
		try {
			operationRow.setTransUnit(Float.parseFloat(this.textFieldUnit.getText()));
		} catch (NumberFormatException e) {
			operationRow.setTransUnit(0.0F);
		}
		Operation op = (Operation) this.comboOperation.getSelectedItem();
		operationRow.setOperation(op);
		if (myOpd != null) {
			operationRow.setOpd(myOpd);
		}
		operationRow.setPrescriber(MainMenu.getUser().getUserName());
		operationRow.setRemarks(textAreaRemark.getText());
		int index = tableData.getSelectedRow();
		if (index < 0) {
			oprowData.add(operationRow);
		} else {
			OperationRow opeInter = oprowData.get(index);
			opeInter.setOpDate(this.textDate.getLocalDateTime());
			opeInter.setOpResult(this.comboResult.getSelectedItem().toString());
			String opResult = opeManager.getResultDescriptionKey((String) comboResult.getSelectedItem());
			opeInter.setOpResult(opResult);
			opeInter.setTransUnit(Float.parseFloat(this.textFieldUnit.getText()));
			op = (Operation) this.comboOperation.getSelectedItem();
			opeInter.setOperation(op);
			opeInter.setPrescriber(MainMenu.getUser().getUserName());
			opeInter.setRemarks(textAreaRemark.getText());
			oprowData.set(index, opeInter);
		}
		modelOhOpeRow = new OhTableOperationModel<>(oprowData);
		tableData.setModel(modelOhOpeRow);
		clearForm();
	}

	// used by addToForm()
	@Override
	public List<Operation> getOperationCollection() throws OHServiceException {
		return opeManager.getOperationOpd();
	}

	@Override
	public void surgeryUpdated(AWTEvent e, Opd opd) {
		try {
			saveAllOpeRow(oprowData, opeRowManager, opd);
		} catch (OHServiceException e1) {
			OHServiceExceptionUtil.showMessages(e1);
		}
	}

	@Override
	public void surgeryInserted(AWTEvent e, Opd opd) {
		try {
			saveAllOpeRow(oprowData, opeRowManager, opd);
		} catch (OHServiceException e1) {
			OHServiceExceptionUtil.showMessages(e1);
		}
	}

	public void saveAllOpeRow(List<OperationRow> listOpe, OperationRowBrowserManager rowManager, Opd opd) throws OHServiceException {
		for (org.isf.operation.model.OperationRow opRow : listOpe) {
			if ((opRow.getId() > 0) && (opRow.getOpd().getCode() > 0)) {
				rowManager.updateOperationRow(opRow);
			}
			if ((opRow.getId() <= 0) && (opRow.getOpd().getCode() > 0)) {
				rowManager.newOperationRow(opRow);
			}
			if ((opRow.getId() <= 0) && (opRow.getOpd().getCode() <= 0)) {
				opRow.setOpd(opd);
				rowManager.newOperationRow(opRow);
			}
		}
	}

}
