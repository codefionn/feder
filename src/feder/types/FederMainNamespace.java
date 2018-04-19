/*
 * Copyright (C) 2018 Fionn Langhans
 */
package feder.types;

import feder.FederCompiler;

/**
 * 
 * @author Fionn Langhans
 * @ingroup utils
 */
public class FederMainNamespace extends FederNamespace
{

	/**
	 * @param compiler0 The compiler instance to use
	 * @param name0 The name of the namespace
	 * @param parent0 The body's parent (Shouldn't have any parents, so null)
	 */
	public FederMainNamespace(FederCompiler compiler0, String name0, FederBody parent0)
	{
		super(compiler0, name0, parent0);
	}
}
