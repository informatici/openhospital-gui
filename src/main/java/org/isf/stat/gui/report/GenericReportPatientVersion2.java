package org.isf.stat.gui.report;

import java.util.Date;

import javax.swing.JOptionPane;

import org.isf.generaldata.GeneralData;
import org.isf.generaldata.MessageBundle;
import org.isf.menu.manager.Context;
import org.isf.stat.dto.JasperReportResultDto;
import org.isf.stat.manager.JasperReportsManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.jasperreports.view.JasperViewer;

public class GenericReportPatientVersion2 {


    private final Logger logger = LoggerFactory.getLogger(GenericReportPatient.class);
    private JasperReportsManager jasperReportsManager = Context.getApplicationContext().getBean(JasperReportsManager.class);

	public GenericReportPatientVersion2(Integer patientID, Boolean all, Boolean admission, Boolean opd, Boolean examination, Boolean drugs, Date date, Date date2, String jasperFileName) {
		try{
			Date date_To = date2;
			Date date_From = date;
            JasperReportResultDto jasperReportResultDto = jasperReportsManager.getGenericReportPatientVersion2Pdf(patientID,all,admission,opd,drugs,examination, date_From, date_To, jasperFileName);
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
