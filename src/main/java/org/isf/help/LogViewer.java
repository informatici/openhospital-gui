/*
 * Open Hospital (www.open-hospital.org)
 * Copyright © 2006-2026 Informatici Senza Frontiere (info@informaticisenzafrontiere.org)
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
package org.isf.help;

import java.io.File;
import java.io.IOException;

import javax.swing.JDialog;

import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.log.LogUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
public class LogViewer extends JDialog {

	private static final Logger LOGGER = LoggerFactory.getLogger(LogViewer.class);

	private String logfile = LogUtil.getLogFileAbsolutePath();

	public LogViewer() {
		try {
			LOGGER.debug("Opening location for: {}", logfile);
			LogUtil.openLogFileLocation();
		} catch (IOException e) {
			// the path is null when no file appender is configured, and then the reason why there is
			// no folder to open is all this dialog can show
			String folder = logfile != null ? new File(logfile).getAbsoluteFile().getParent() : e.getMessage();
			MessageDialog.error(this, "angal.log.foldererror.fmt.msg", folder);
		}
	}

}
