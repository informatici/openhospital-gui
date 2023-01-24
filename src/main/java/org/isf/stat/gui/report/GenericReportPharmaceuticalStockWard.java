/*
 * Open Hospital (www.open-hospital.org)
 * Copyright © 2006-2022 Informatici Senza Frontiere (info@informaticisenzafrontiere.org)
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
package org.isf.stat.gui.report;

import java.time.LocalDateTime;

import org.isf.menu.manager.Context;
import org.isf.stat.dto.JasperReportResultDto;
import org.isf.stat.manager.JasperReportsManager;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.ward.model.Ward;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * Created on 15/Jun/08
 */
public class GenericReportPharmaceuticalStockWard extends DisplayReport {

	private static final Logger LOGGER = LoggerFactory.getLogger(GenericReportPharmaceuticalStockWard.class);
	private JasperReportsManager jasperReportsManager = Context.getApplicationContext().getBean(JasperReportsManager.class);

	public GenericReportPharmaceuticalStockWard(LocalDateTime date, String jasperFileName, Ward ward) {
		try {
			JasperReportResultDto jasperReportResultDto = jasperReportsManager.getGenericReportPharmaceuticalStockWardPdf(date, jasperFileName, ward);
			showReport(jasperReportResultDto);
		} catch (Exception e) {
			LOGGER.error("", e);
			MessageDialog.error(null, "angal.stat.reporterror.msg");
		}
	}

}
