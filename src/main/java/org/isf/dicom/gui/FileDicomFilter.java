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
package org.isf.dicom.gui;

import java.io.File;

import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileView;

/**
 * Filter for file DICOM
 *
 * @author Pietro Castellucci
 * @version 1.0.0
 */
public class FileDicomFilter extends FileFilter {

	/**
	 * Whether the given file is accepted by this filter.
	 */
	@Override
	public boolean accept(File f) {
		return (f.getName().toLowerCase().endsWith(".dcm")
				|| f.isDirectory()
				|| (!f.getName().contains(".")));
	}

	/**
	 * The description of this filter. For example: "JPG and GIF Images"
	 *
	 * @see FileView#getName
	 */
	@Override
	public String getDescription() {
		return "DICOM Images";
	}
}
