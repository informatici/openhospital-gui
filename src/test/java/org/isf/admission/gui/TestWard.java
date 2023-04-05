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
package org.isf.admission.gui;

import org.isf.ward.model.Ward;

public class TestWard {

	public static Ward maleWardWithBeds(String code) {
		Ward ward = ward(code);
		ward.setBeds(1);
		ward.setMale(true);
		ward.setFemale(false);
		return ward;
	}

	public static Ward femaleWardWithBeds(String code) {
		Ward ward = ward(code);
		ward.setBeds(1);
		ward.setMale(false);
		ward.setFemale(true);
		return ward;
	}

	private static Ward ward(String code) {
		Ward ward = new Ward();
		ward.setCode(code);
		ward.setDescription("desc");
		ward.setDocs(1);
		ward.setEmail("test1488@hwdp.pl");
		ward.setFax("1234");
		ward.setPharmacy(false);
		ward.setTelephone("+48 1238282");
		ward.setNurs(1);
		return ward;
	}

	public static Ward maleWardWithoutBeds(String code) {
		Ward ward = ward(code);
		ward.setBeds(0);
		ward.setMale(true);
		ward.setFemale(false);
		return ward;
	}

}
