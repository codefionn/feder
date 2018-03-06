package feder.types;

import feder.FederCompiler;

/**
 * Diese Klasse ist für die Syntaxkörper: while, if, else, else if
 *
 * @author Fionn Langhans
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

	public String getType()
	{
		return type;
	}

	@Override
	public String generateCName()
	{
		return null;
	}
}
