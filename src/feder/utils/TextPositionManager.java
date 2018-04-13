package feder.utils;

/**
 * 
 * @author Fionn Langhans
 * @ingroup utils
 */
public class TextPositionManager
{
	/**
	 * Current line number
	 */
	public int line;
	
	/**
	 * Current index in @link TextPositionManager.text text @endlink
	 */
	public int indexChar;
	
	/**
	 * Text to work on
	 */
	public String text;
	
	/**
	 * The filename, where (optionally) the text was found
	 */
	public String filename;

	/**
	 * @param line0 Current line
	 * @param indexChar0 current index in @link TextPositionManager.text text @endlink
	 * @param text0 The text to use
	 */
	public TextPositionManager(int line0, int indexChar0, String text0)
	{
		line = line0;
		indexChar = indexChar0;
		text = text0;
	}

	/**
	 * Report an error at 'index0' and print the error message 'msg'
	 * @param index0 Index in @link TextPositionManager.text text @endlink
	 * @param msg
	 */
	public void error(int index0, String msg)
	{
		printError(index0);
		System.err.println(msg);

		toNextLine();
	}

	/**
	 * Prints an error with the information where it is (just prints the location,
	 * not the error itself)
	 *
	 * @param index0
	 *            Where the error is in 'text'
	 */
	private void printError(int index0)
	{
		int index = index0;

		if (index < 0)
			index = 0;

		if (index >= text.length())
			index = text.length();

		index -= getNewLineOperatorLength(index);
		for (; index > 0 && getNewLineOperatorLength(index) == 0; index--) {
			/*
			 * Go to the last new line sign
			 */
		}
		// jump forth (over the new line sign)
		// because we:
		// a) are pointing at a new line sign
		// or b) we are at index 0 and have no new line sign,
		// meaning, there's no new line operator, which could be added
		index += getNewLineOperatorLength(index);

		/*
		 * Saves the position where the line starts
		 */
		int oldindex = index;

		/*
		 * Saves the whole line (from 0 or newline to end or newline)
		 */
		StringBuilder sbline = new StringBuilder();
		int column = 0;
		for (; index < text.length() && getNewLineOperatorLength(index) == 0; index++) {
			if (index <= index0)
				column++;
			sbline.append(text.charAt(index));
		}

		// Print the Location (file + line + column)
		System.err.println("File=" + filename + ", Line=" + line + ", Column=" + column);
		// Print line
		System.err.println(sbline);

		/*
		 * Saves the column where the string ends
		 */
		int oldcolumn = column - 1;
		// ol' column has to be greater than zero
		// Why ? Society
		if (oldcolumn <= 0)
			oldcolumn = 1;

		// There was once a bug, which was out of the accepted limits,
		// the society (Java) didn't except it
		// System.out.println (oldindex + ", " + oldcolumn + ", " + column);

		// The following lines mark the position with '^'
		for (; column > 0; column--) {
			char c = text.charAt(oldindex + oldcolumn - column);
			if (c != '\t')
				System.err.print(' ');
			else
				System.err.print('\t');
		}

		System.err.println('^'); // The marking character
	}

	/**
	 * @param s
	 * @return True if the text at the current position indexChar
	 * starts with 's'
	 */
	public boolean startsWith(String s)
	{
		return text.startsWith(s, indexChar);
	}

	/**
	 * @param index The current position in 'text'
	 * @return Returns the length of the new line operator,
	 * if the new line operator exists
	 */
	public int getNewLineOperatorLength(int index)
	{
		if (text.startsWith("\n\r", index))
			return 2;
		if (text.startsWith("\r\n", index))
			return 2;
		if (text.startsWith("\n", index))
			return 1;
		if (text.startsWith("\r", index))
			return 1;

		return 0;
	}

	/**
	 * @return Returns the index where the next line is starting
	 */
	public int findNextLine()
	{
		int result;
		for (result = indexChar; result < text.length() && getNewLineOperatorLength(result) == 0; result++) {
			/*
			 * Go to the next new line sign
			 */
		}

		if (result < text.length())
			result += getNewLineOperatorLength(result);

		return result;
	}

	/**
	 * Sets the current position to the start of the next line
	 */
	public void toNextLine()
	{
		int oldindex = indexChar;
		indexChar = findNextLine();
		if (oldindex != indexChar)
			line++;
	}

	/**
	 * @return Returns true if the current position indexChar is valid
	 */
	public boolean isPosValid()
	{
		return indexChar < text.length();
	}
}
