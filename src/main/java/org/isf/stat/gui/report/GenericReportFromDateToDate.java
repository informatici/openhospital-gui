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
package org.isf.stat.gui.report;

import java.io.File;
import java.util.Locale;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.isf.generaldata.GeneralData;
import org.isf.generaldata.MessageBundle;
import org.isf.menu.manager.Context;
import org.isf.stat.dto.JasperReportResultDto;
import org.isf.stat.manager.JasperReportsManager;
import org.isf.utils.excel.ExcelExporter;
import org.isf.utils.exception.OHReportException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.jasperreports.view.JasperViewer;

/**
 * --------------------------------------------------------
 * GenericReportFromDateToDate
 *  - launch all reports that have "from date" "to date" as parameters
 * 	- the class expects initialization through dadata, adata, name of the report (without .jasper)
 * ---------------------------------------------------------
 * modification history
 * 09/06/2007 - first version
 * -----------------------------------------------------------------
 */
	public class GenericReportFromDateToDate {

    private final Logger logger = LoggerFactory.getLogger(GenericReportFromDateToDate.class);
    private JasperReportsManager jasperReportsManager = Context.getApplicationContext().getBean(JasperReportsManager.class);

		public  GenericReportFromDateToDate(String fromDate, String toDate, String jasperFileName, String defaultName, boolean toExcel) {
			try{
                File defaultFilename = new File(jasperReportsManager.compileDefaultFilename(defaultName));
                
				if (toExcel) {
					JFileChooser fcExcel = ExcelExporter.getJFileChooserExcel(defaultFilename);

                    int iRetVal = fcExcel.showSaveDialog(null);
                    if (iRetVal == JFileChooser.APPROVE_OPTION)
                    {
                        File exportFile = fcExcel.getSelectedFile();
                        FileNameExtensionFilter selectedFilter = (FileNameExtensionFilter) fcExcel.getFileFilter();
    					String extension = selectedFilter.getExtensions()[0];
    					if (!exportFile.getName().endsWith(extension)) exportFile = new File(exportFile.getAbsoluteFile() + "." + extension);
                        jasperReportsManager.getGenericReportFromDateToDateExcel(fromDate,toDate, jasperFileName, exportFile.getAbsolutePath());
                    }
                } else {
                    JasperReportResultDto jasperReportResultDto = jasperReportsManager.getGenericReportFromDateToDatePdf(fromDate, toDate, jasperFileName);

                    //TODO: if (jasperReportResultDto.getJasperPrint().getPages().isEmpty()) show message dialog and avoid calling the viewer
                    if (GeneralData.INTERNALVIEWER)
                        JasperViewer.viewReport(jasperReportResultDto.getJasperPrint(),false, new Locale(GeneralData.LANGUAGE));
                    else {
                        Runtime rt = Runtime.getRuntime();
                        rt.exec(GeneralData.VIEWER +" "+ jasperReportResultDto.getFilename());
                    }
				}
			} catch (OHReportException e) {
				OHServiceExceptionUtil.showMessages(e);
			} catch (Exception e) {
                logger.error("", e);
                JOptionPane.showMessageDialog(null, MessageBundle.getMessage("angal.stat.reporterror"), MessageBundle.getMessage("angal.hospital"), JOptionPane.ERROR_MESSAGE);
			}
		}
		
		

	}
