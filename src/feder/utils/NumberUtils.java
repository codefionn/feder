/*
 * Copyright (C) 2018 Fionn Langhans
 */
package feder.utils;

import java.util.regex.*;

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
		return Pattern.matches("0x[A-F0-9]" + (complete ? "+" : "*"), str);
	}

	/**
	 * @param str The string to check
	 * @param allowFloatingPoint Allow numbers with float point numbers (numbers after '.')
	 * @param allow_minus Allow the - sign in front of the number
	 * @return Returns true if str is a number, also using allowFloatingPoint and allow_minus
	 */
	public static boolean isNumber(String str, boolean allowFloatingPoint, boolean allow_minus)
	{
		return Pattern.matches((allow_minus ? "-?" : "") + "(0|[1-9][0-9]*)" + (allowFloatingPoint ? "(\\.[0-9]+)?" : ""), str);
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
