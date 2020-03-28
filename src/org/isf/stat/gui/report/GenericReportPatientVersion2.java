package org.isf.stat.gui.report;

import java.util.Date;
import java.util.GregorianCalendar;

import javax.swing.JOptionPane;

import org.isf.generaldata.GeneralData;
import org.isf.generaldata.MessageBundle;
import org.isf.stat.dto.JasperReportResultDto;
import org.isf.stat.manager.JasperReportsManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.jasperreports.view.JasperViewer;

public class GenericReportPatientVersion2 {


    private final Logger logger = LoggerFactory.getLogger(GenericReportPatient.class);

	public GenericReportPatientVersion2(Integer patientID, Boolean all, Boolean admission, Boolean opd, Boolean examination, Boolean drugs, Date date, Date date2, String jasperFileName) {
		try{
			Integer id = patientID;
			Boolean adm =admission;
			Boolean op =opd;
			Boolean drug =drugs;
			Boolean exam=examination;
			Date date_To = date2;
			Date date_From = date;
            JasperReportsManager jasperReportsManager = new JasperReportsManager();
            JasperReportResultDto jasperReportResultDto = jasperReportsManager.getGenericReportPatientVersion2Pdf(patientID,all, admission,opd,drugs,examination, date_From, date_To, jasperFileName);
			if (GeneralData.INTERNALVIEWER)
				JasperViewer.viewReport(jasperReportResultDto.getJasperPrint(),false);
			else { 
					Runtime rt = Runtime.getRuntime();
					rt.exec(GeneralData.VIEWER +" "+ jasperReportResultDto.getFilename());
			}
		} catch (Exception e) {
            logger.error("", e);
            JOptionPane.showMessageDialog(null, MessageBundle.getMessage("angal.stat.reporterror"), MessageBundle.getMessage("angal.hospital"), JOptionPane.ERROR_MESSAGE);
		}
	}
	

}
