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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.jasperreports.view.JasperViewer;

/**
 * --------------------------------------------------------
 * GenericReportLauncerMY - launch all the reports that have as parameters year and month
 * 							the class expects initialization through year, month, report name (without .jasper)
 * ---------------------------------------------------------
 * modification history
 * 11/11/2006 - first version
 * -----------------------------------------------------------------*/
public class GenericReportMY {

    private static final Logger LOGGER = LoggerFactory.getLogger(GenericReportMY.class);
    private JasperReportsManager jasperReportsManager = Context.getApplicationContext().getBean(JasperReportsManager.class);

	public GenericReportMY(Integer month, Integer year, String jasperFileName, String defaultName, boolean toExcel) {
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
                    jasperReportsManager.getGenericReportMYExcel(month,year,jasperFileName,exportFile.getAbsolutePath());
                }
			} else {
                JasperReportResultDto jasperReportResultDto = jasperReportsManager.getGenericReportMYPdf(month,year,jasperFileName);
                if (GeneralData.INTERNALVIEWER)
                    JasperViewer.viewReport(jasperReportResultDto.getJasperPrint(),false, new Locale(GeneralData.LANGUAGE));
                else {
                    Runtime rt = Runtime.getRuntime();
                    rt.exec(GeneralData.VIEWER +" "+ jasperReportResultDto.getFilename());
                }
			}
		} catch (Exception e) {
            LOGGER.error("", e);
            JOptionPane.showMessageDialog(null, MessageBundle.getMessage("angal.stat.reporterror"), MessageBundle.getMessage("angal.hospital"), JOptionPane.ERROR_MESSAGE);
		}
	}

}
