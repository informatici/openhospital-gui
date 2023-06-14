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
package org.isf.priceslist.gui;

/*
 * %W% %E%
 *
 * Copyright 1997, 1998 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 * 
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer. 
 *   
 * - Redistribution in binary form must reproduce the above
 *   copyright notice, this list of conditions and the following
 *   disclaimer in the documentation and/or other materials
 *   provided with the distribution. 
 *   
 * Neither the name of Sun Microsystems, Inc. or the names of
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.  
 * 
 * This software is provided "AS IS," without a warranty of any
 * kind. ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND
 * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY
 * EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY
 * DAMAGES OR LIABILITIES SUFFERED BY LICENSEE AS A RESULT OF OR
 * RELATING TO USE, MODIFICATION OR DISTRIBUTION OF THIS SOFTWARE OR
 * ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE 
 * FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT,   
 * SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER  
 * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF 
 * THE USE OF OR INABILITY TO USE THIS SOFTWARE, EVEN IF SUN HAS 
 * BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 * 
 * You acknowledge that this software is not designed, licensed or
 * intended for use in the design, construction, operation or
 * maintenance of any nuclear facility.
 */

import java.util.ArrayList;
import java.util.List;

import org.isf.generaldata.MessageBundle;
import org.isf.priceslist.model.Price;
import org.isf.utils.treetable.AbstractTreeTableModel;
import org.isf.utils.treetable.TreeTableModel;

/**
 * FileSystemModel is a TreeTableModel representing a hierarchical file
 * system. Nodes in the FileSystemModel are FileNodes which, when they
 * are directory nodes, cache their children to avoid repeatedly querying
 * the real file system.
 *
 * @author Philip Milne
 * @author Scott Violet
 */
public class PriceModel extends AbstractTreeTableModel {

	// Names of the columns.
	protected static String[] cNames = {
			MessageBundle.getMessage("angal.common.name.txt").toUpperCase(),
			MessageBundle.getMessage("angal.priceslist.prices.txt").toUpperCase()
	};

	// Types of the columns.
	protected static Class<?>[] cTypes = { TreeTableModel.class, Double.class };

	protected static String[] cCategories = { "EXA", "OPE", "MED", "OTH" };

	public PriceModel(Object root) {
		super(root);
	}

	@Override
	public int getColumnCount() {
		return cNames.length;
	}

	@Override
	public String getColumnName(int column) {
		return cNames[column];
	}

	@Override
	public Class<?> getColumnClass(int column) {
		return cTypes[column];
	}

	@Override
	public void setValueAt(Object aValue, Object node, int column) {
		if (column == 1) {
			((PriceNode) node).getPrice().setPrice((Double) aValue);
		}
	}

	@Override
	public Object getValueAt(Object node, int column) {
		Price price = getPrice(node);
		Object displayValue = MessageBundle.getMessage("angal.priceslist.failed.txt");
		switch (column) {
			case 0:
				displayValue = price.getDesc();
				break;
			case 1:
				displayValue = price.getPrice();
				break;
			default:
				break;
		}
		return displayValue;
	}

	@Override
	public boolean isCellEditable(Object node, int column) {
		if (getPrice(node).isPrice() && getPrice(node).isEditable()) {
			return true;
		}
		return super.isCellEditable(node, column);
	}

	protected Price getPrice(Object node) {
		PriceNode priceNode = ((PriceNode) node);
		return priceNode.getPrice();
	}

	protected Object[] getChildren(Object node) {
		PriceNode priceNode = (PriceNode) node;
		return priceNode.getItems();
	}

	@Override
	public Object getChild(Object node, int i) {
		return getChildren(node)[i];
	}

	@Override
	public int getChildCount(Object node) {
		Object[] items = getChildren(node);
		return (items == null) ? 0 : items.length;
	}
}

class PriceNode {

	private Price price;
	private List<PriceNode> items = new ArrayList<>();

	public PriceNode(Price price) {
		this.price = price;
	}

	public boolean isPrice() {
		return price.getList().getId() == 0;
	}

	public void addItem(PriceNode price) {
		this.items.add(price);
	}

	/**
	 * Returns the string to be used to display this leaf in the JTree.
	 */
	@Override
	public String toString() {
		return price.getDesc();
	}

	public Price getPrice() {
		return price;
	}

	/**
	 * Loads the children, caching the results in the children ivar.
	 */
	public Object[] getItems() {
		return this.items.toArray();
	}

}
