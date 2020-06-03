/**
 * 
 */
package org.isf.utils.jobjects;

import java.math.BigDecimal;
import java.math.RoundingMode;

import javax.swing.JSlider;

/**
 * JSlider customization in order to manage decimal values and a given step
 * @author Mwithi
 *
 */
public class ScaledJSlider extends JSlider {

	/**
	 * 
	 */
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
	 * Internally the component still works with <code>int</code> values
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
	 * @param step
	 */
	private void setPrecision(double step) {
		String number = String.valueOf(step);
		String[] result = number.split("\\.");
		if (result.length == 2)
			this.precision = result[1].length();
		else
			this.precision = 0;
	}

	/**
	 * New setValue() method in order to accept <code>double</code> values, range and step
	 * @param doubleValue
	 * @param scaledMin
	 * @param step
	 * @param scaledMax
	 */
	public void setValue(Double doubleValue) {
		int value = convertFromDoubleToInt(doubleValue, scaledMin, step, scaledMax);
		super.setValue(value);
	}
	
	/**
	 * Convert from <code>Double</code> to <code>int</code> with specified range and step
	 * @param doubleValue - the value to be converted
	 * @param scaledMin - the minimum value (implicit casting from <code>int</code> to <code>double</code>)
	 * @param step - the step to round up the result (implicit casting from <code>int</code> to <code>double</code>)
	 * @param scaledMax - the maximum value (implicit casting from <code>int</code> to <code>double</code>)
	 * @return the nearest integer to the provided <code>Double</code> value, or min or max if value is out of range
	 */
	private int convertFromDoubleToInt(Double doubleValue, double scaledMin, double step, double scaledMax) {
		if (doubleValue == null) return (int) Math.round(scaledInit * (1. / step));
		int intValue = 0;
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
	 * @param step
	 */
	public double getScaledValue() {
		double value = super.getValue() * step;
		return new BigDecimal(value).setScale(precision, RoundingMode.HALF_UP).doubleValue();
	}
	
}
