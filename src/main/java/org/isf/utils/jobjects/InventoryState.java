/*
 * Open Hospital (www.open-hospital.org)
 * Copyright © 2006-2024 Informatici Senza Frontiere (info@informaticisenzafrontiere.org)
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
package org.isf.utils.jobjects;


public class InventoryState {
	public InventoryState(){}
	public enum State
	 {		
		PROGRESS ("1", "angal.common.statecanceled"),
		CANCELED ("2", "angal.common.stateinprogress"),
		VALIDATE ("3", "angal.common.statevalidate");
		
		String code;
		String label;
			
		private State(String code, String label){
			this.code=code;
			this.label=label;
		}

		public String getCode() {
			return code;
		}
		public void setCode(String code) {
			this.code = code;
		}
		public String getLabel() {
			return label;
		}
		public void setLabel(String label) {
			this.label = label;
	    }
	}
}

