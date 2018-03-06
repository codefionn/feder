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
