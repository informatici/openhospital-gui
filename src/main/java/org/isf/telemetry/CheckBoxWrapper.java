package org.isf.telemetry;

import javax.swing.JCheckBox;

public class CheckBoxWrapper {

	private String id;
	private Integer order;
	private JCheckBox checkbox;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Integer getOrder() {
		return order;
	}

	public void setOrder(Integer order) {
		this.order = order;
	}

	public JCheckBox getCheckbox() {
		return checkbox;
	}

	public void setCheckbox(JCheckBox checkbox) {
		this.checkbox = checkbox;
	}

}
