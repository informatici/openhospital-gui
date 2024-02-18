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
package org.isf.utils.jobjects;

import java.math.BigDecimal;
import java.math.RoundingMode;

import javax.swing.JSlider;

/**
 * JSlider customization in order to manage decimal values and a given step
 *
 * @author Mwithi
 */
public class ScaledJSlider extends JSlider {

	private static final long serialVersionUID = 1L;

	/**
	 * Minimum value scaled
	 */
	private int scaledMin;

	/**
	 * Maximum value scaled
	 */
	private int scaledMax;

	/**
	 * Step (e.g. 0.1)
	 */
	private double step;

	/**
	 * The initial value
	 */
	private double scaledInit;

	/**
	 * Precision (number of decimals in step) (e.g. 1)
	 */
	private int precision;

	/**
	 * JSlider customization in order to manage decimal values with a given step.
	 * Internally the component still works with {@code int} values
	 *
	 * @param scaledMin - minimum value
	 * @param scaledMax - maximum value
	 * @param step - step between values
	 * @param scaledInit - initial value
	 */
	public ScaledJSlider(int scaledMin, int scaledMax, double step, double scaledInit) {
		super();
		this.scaledMin = scaledMin;
		this.scaledMax = scaledMax;
		this.step = step;
		this.scaledInit = scaledInit;
		this.setPrecision(step);
		int min = (int) (scaledMin * (1. / step));
		int max = (int) (scaledMax * (1. / step));
		setMinimum(min);
		setMaximum(max);

		setValue(scaledInit);

	}

	/**
	 * Extract number of digits from step
	 *
	 * @param step
	 */
	private void setPrecision(double step) {
		String number = String.valueOf(step);
		String[] result = number.split("\\.");
		if (result.length == 2) {
			this.precision = result[1].length();
		} else {
			this.precision = 0;
		}
	}

	/**
	 * New setValue() method in order to accept {@code double} values, range and step
	 *
	 * @param doubleValue
	 */
	public void setValue(Double doubleValue) {
		int value = convertFromDoubleToInt(doubleValue, scaledMin, step, scaledMax);
		super.setValue(value);
	}

	/**
	 * Convert from {@code Double} to {@code int} with specified range and step
	 *
	 * @param doubleValue - the value to be converted
	 * @param scaledMin - the minimum value (implicit casting from {@code int} to {@code double})
	 * @param step - the step to round up the result (implicit casting from {@code int} to {@code double})
	 * @param scaledMax - the maximum value (implicit casting from {@code int} to {@code double})
	 * @return the nearest integer to the provided {@code Double} value, or min or max if value is out of range
	 */
	private int convertFromDoubleToInt(Double doubleValue, double scaledMin, double step, double scaledMax) {
		if (doubleValue == null) {
			return (int) Math.round(scaledInit * (1. / step));
		}
		int intValue;
		if (doubleValue >= scaledMax) {
			intValue = (int) (scaledMax * (1. / step));
		} else if (doubleValue <= scaledMin) {
			intValue = (int) Math.round(scaledMin * (1. / step));
		} else {
			intValue = (int) Math.round(doubleValue * (1. / step));
		}
		return intValue;
	}

	/**
	 * New getScaledValue() method in order to scale the internal value using step
	 */
	public double getScaledValue() {
		double value = super.getValue() * step;
		return BigDecimal.valueOf(value).setScale(precision, RoundingMode.HALF_UP).doubleValue();
	}

}
