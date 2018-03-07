package feder.types;

import feder.FederCompiler;

/**
 * This class is for the lexical names: for, while, if, else
 *
 * @author Fionn Langhans
 * @ingroup utils
 */
public class FederAutomat extends FederBody
{
	private String type;

	@SuppressWarnings("unchecked")
	public FederAutomat(FederCompiler compiler0, FederBody parent0, String type0)
	{
		super(compiler0, null, parent0, FederClass.class, FederFunction.class, FederNamespace.class);

		type = type0;
	}

	/**
	 * @return Returns the type of this automat (conditional statement):
	 * for, while, if, else, else if
	 */
	public String getType()
	{
		return type;
	}

	@Override
	/**
	 * @return Returns the name for C source code
	 */
	public String generateCName()
	{
		return null;
	}
}
