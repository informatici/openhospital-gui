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
package org.isf.stat.gui.report;

import java.util.List;

import javax.swing.JOptionPane;

import org.isf.generaldata.GeneralData;
import org.isf.generaldata.TxtPrinter;
import org.isf.menu.manager.Context;
import org.isf.patient.model.Patient;
import org.isf.serviceprinting.manager.PrintReceipt;
import org.isf.stat.dto.JasperReportResultDto;
import org.isf.stat.manager.JasperReportsManager;
import org.isf.utils.jobjects.MessageDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * Created on 15/Jun/08
 */
public class GenericReportBill extends DisplayReport {

    private static final Logger LOGGER = LoggerFactory.getLogger(GenericReportBill.class);
	private JasperReportsManager jasperReportsManager = Context.getApplicationContext().getBean(JasperReportsManager.class);

	public GenericReportBill(Integer billID, String jasperFileName) {
		new GenericReportBill(billID, jasperFileName, true, true);
	}

	public GenericReportBill(Integer billID, String jasperFileName, boolean show, boolean askForPrint) {
		
		TxtPrinter.initialize();
		
		try {
            JasperReportResultDto jasperReportPDFResultDto = jasperReportsManager.getGenericReportBillPdf(billID, jasperFileName, show, askForPrint);

			if (show) {
                showReport(jasperReportPDFResultDto);
			}
			
			if (GeneralData.RECEIPTPRINTER) {
				JasperReportResultDto jasperReportTxtResultDto = jasperReportsManager.getGenericReportBillTxt(billID, jasperFileName, show, askForPrint);
				int print = JOptionPane.OK_OPTION;
				if (askForPrint) {
					print = MessageDialog.yesNo(null, "angal.genericreportbill.doyouwanttoprintreceipt.msg");
				}
				if (print != JOptionPane.OK_OPTION) {
					return; //STOP
				}
				
				if (TxtPrinter.MODE.equals("PDF")) {
					new PrintReceipt(jasperReportPDFResultDto.getJasperPrint(), jasperReportPDFResultDto.getFilename());
				} else if (TxtPrinter.MODE.equals("TXT") || TxtPrinter.MODE.equals("ZPL")) {
					new PrintReceipt(jasperReportTxtResultDto.getJasperPrint(), jasperReportTxtResultDto.getFilename());
				}
			}
		} catch (Exception e) {
            LOGGER.error("", e);
			MessageDialog.error(null, "angal.stat.reporterror.msg");
        }
	}
	
	public GenericReportBill(Integer billID, String jasperFileName, Patient patient, List<Integer> billListId, String dateFrom, String dateTo, boolean show, boolean askForPrint) {
		try {
			
			JasperReportResultDto jasperReportPDFResultDto = jasperReportsManager.getGenericReportBillGroupedPdf(billID, jasperFileName, patient, billListId, dateFrom, dateTo, show, askForPrint);
			
			if (show) {
				showReport(jasperReportPDFResultDto);
			}
			
			if (GeneralData.RECEIPTPRINTER) {				
				JasperReportResultDto jasperReportTxtResultDto = jasperReportsManager.getGenericReportBillGroupedTxt(billID, jasperFileName, patient, billListId, dateFrom, dateTo, show, askForPrint);
				
				int print = JOptionPane.OK_OPTION;
				if (askForPrint) {
					print = MessageDialog.yesNo(null, "angal.genericreportbill.doyouwanttoprintreceipt.msg");
				}
				if (print == JOptionPane.OK_OPTION) {
					new PrintReceipt(jasperReportTxtResultDto.getJasperPrint(), jasperReportTxtResultDto.getFilename());
				}
			}
		} catch (Exception exception) {
			LOGGER.error(exception.getMessage(), exception);
		}
	}	
}
