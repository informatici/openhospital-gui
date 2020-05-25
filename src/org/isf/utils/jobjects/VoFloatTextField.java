package org.isf.utils.jobjects;

import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

/**
 * 
 * @author http://www.java2s.com/Code/Java/Swing-JFC/Textfieldonlyacceptsnumbers.htm
 *
 */
public class VoFloatTextField extends JTextField {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 * @param defval - default value
	 * @param columns - number of columns to show
	 */
	public VoFloatTextField(int defval, int columns) {
		super("" + defval, columns);
	}
	public VoFloatTextField(String defval, int columns) {
		super(defval, columns);
	}

	protected Document createDefaultModel() {
		return new FloatTextDocument();
	}

	public float getValue() {
		try {
			return Float.parseFloat(getText());
		} catch (NumberFormatException e) {
			return  0;
		}
	}

	class FloatTextDocument extends PlainDocument {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
			
			if (str == null)
				return;
			String oldString = getText(0, getLength());
			String newString = oldString.substring(0, offs) + str + oldString.substring(offs);
			try {
				Float.parseFloat(newString + "0");
				super.insertString(offs, str, a);
			} catch (NumberFormatException e) {
				System.out.println("==> Exception: string is " + str);
				if (!str.matches("^[a-zA-Z0-9]*$"))
					//super.insertString(offs, String.valueOf(DecimalFormatSymbols.getInstance(new Locale(GeneralData.LANGUAGE)).getDecimalSeparator()), a);
					super.insertString(offs, ".", a);
			}
		}
	}
}