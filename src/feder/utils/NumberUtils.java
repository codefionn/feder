package feder.utils;

/**
 * 
 * @author Fionn Langhans
 * @ingroup utils
 */
public class NumberUtils
{
	private NumberUtils()
	{
	}
	
	/**
	 * @param str String to check
	 * @param complete
	 * @return returns true, if str could represent a hexadecimal number. The string
	 * has to start with '0x'. If complete is true, the string mustn't be just '0x', if
	 * complete is false the string is allowed to be just '0x'. Regular expressions:
	 * 
	 * complete == true: 0x[A-F0-9]\+
	 * complete == false: 0x[A-F0-9]*
	 */
	public static boolean isHexadecimalNumber (String str, boolean complete) {
		if (str.isEmpty())
			return false;
		
		if (!str.startsWith("0x") || (str.length() == 2 && complete))
			return false;
		
		for (int i = 2; i < str.length(); i++) {
			if (!Character.isDigit(str.charAt(i))
					&& (str.charAt(i) >= 'A' && str.charAt(i) <= 'F')) {
				return false;
			}
		}
		
		return true;
	}

	/**
	 * @param str The string to check
	 * @param allowFloatingPoint Allow numbers with float point numbers (numbers after '.')
	 * @param allow_minus Allow the - sign in front of the number
	 * @return Returns true if str is a number, also using allowFloatingPoint and allow_minus
	 */
	public static boolean isNumber(String str, boolean allowFloatingPoint, boolean allow_minus)
	{
		if (str.isEmpty()) {
			return false;
		}

		int index = 0;
		if (allow_minus && str.startsWith("-")) {
			index++;
		}

		boolean floatingPointCame = false;

		for (int i = index; i < str.length(); i++) {
			if (!Character.isDigit(str.charAt(i))
			        && !(str.charAt(i) == '.' && allowFloatingPoint && !floatingPointCame)) {
				return false;
			}

			if (str.charAt(i) == '.' && allowFloatingPoint)
				floatingPointCame = false;
		}

		return true;
	}

	/**
	 * @param str
	 * @return Returns true if str is an integer
	 */
	public static boolean isInteger(String str)
	{
		return isNumber(str, false, true);
	}

	/**
	 * @param str
	 * @return Returns true if the str is a floating point number
	 */
	public static boolean isFloat(String str)
	{
		return isNumber(str, true, true);
	}
}
