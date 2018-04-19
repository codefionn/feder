/*
 * Copyright (C) 2018 Fionn Langhans
 */
package feder.types;

import java.util.LinkedList;
import java.util.List;

import feder.FederCompiler;

/**
 * @author Fionn Langhans
 * @ingroup types
 */
public class FederInterface extends FederBody implements FederArguments, FederHeaderGen
{
	/**
	 * The arguments of the interface
	 */
	public List<FederObject> arguments = new LinkedList<>();
	
	/**
	 * The result type of the interface/function
	 */
	public FederBinding returnType;
	
	/**
	 * False: You can't call the function described by this object
	 * 
	 * True: You can call the function described by this object
	 */
	private boolean cancall = false;

	@SuppressWarnings("unchecked")
	/**
	 * @param compiler0 The compiler instance to use
	 * @param name0 The name of the interface
	 * @param parent The parent of this body
	 */
	public FederInterface(FederCompiler compiler0, String name0, FederBody parent)
	{
		super(compiler0, name0, parent);
	}

	/**
	 * @param compiler0 The compiler instance to use
	 * @param name The name of the callable interface
	 * @param parent The parent of the interface
	 * @return Returns a callable interface from a not callable one
	 */
	public FederInterface interfaceFrom(FederCompiler compiler0, String name, FederBody parent)
	{
		if (cancall) {
			throw new RuntimeException("Cannot create interface from" + " already object-interface");
		}

		return new FederInterface(compiler0, name, parent, arguments, returnType);
	}

	@SuppressWarnings("unchecked")
	/**
	 * @param compiler0 The compiler instance to use
	 * @param name0 The name of the interface
	 * @param parent The parent of this body
	 * @param arguments0 The arguments of the interface
	 * @param returnType0 The result type of this interface
	 */
	public FederInterface(FederCompiler compiler0, String name0, FederBody parent, List<FederObject> arguments0,
	                      FederBinding returnType0)
	{
		super(compiler0, name0, parent);

		arguments = arguments0;
		returnType = returnType0;
		cancall = true;
	}

	@Override
	/**
	 * @return Returns the name for C source code
	 */
	public String generateCName()
	{
		if (canBeCalled()) {
			String infrontofname = "";
			if (getParent() != null && getParent() instanceof FederNamespace) {
				String namespacestostr = ((FederNamespace) getParent()).getNamespacesToString();
				infrontofname = (namespacestostr.isEmpty() ? "" : ("0" + namespacestostr)) + "0";
			}
			return "federobj_" + infrontofname + getName();
		}

		String result = "0" + getName();
		return "fdint_" + getNamespacesToString() + result;
	}

	@Override
	/**
	 * @return Returns the arguments required by this interface
	 */
	public List<FederObject> getArguments()
	{
		return arguments;
	}

	@Override
	/**
	 * @param name If null, every name gets accepted
	 * @param arguments0 The arguments if the function/interface
	 * @return Returns true, if this interface is "the same" as the one described by name and arguments0
	 */
	public boolean isEqual(String name, List<FederBinding> arguments0)
	{
		return FederFunction.isEqual(name, arguments0, getArguments(), getName());
	}
	
	@Override
	/**
	 * @return Can be null, otherwise this returns what class the function returns
	 */
	public FederBinding getReturnType()
	{
		return returnType;
	}

	@Override
	/**
	 * @return Set the return type of this interface
	 */
	public void setReturnType(FederBinding fc)
	{
		returnType = fc;
	}

	/**
	 * @return Returns true, if this interface can be called (is an object)
	 */
	@Override
	public boolean canBeCalled()
	{
		return cancall;
	}

	/**
	 * @return Returns a string, which should be generated
	 * in the header file
	 */
	@Override
	public String generateInHeader()
	{
		if (canBeCalled()) {
			return "";
		}

		String inbetween = "";
		if (getReturnType() == null) {
			inbetween = "void";
		} else if (getReturnType () instanceof FederClass) {
			inbetween = getReturnType().generateCName() + "";
		} else {
			inbetween = getReturnType().generateCName();
		}

		return "typedef " + inbetween + " (*" + generateCName() + ") ("
		       + FederFunction.generateArgumentsListString(getParent(), getArguments()) + ");\n";
	}

	/**
	 * @param args
	 * @return Returns true if the given 'args' are similiar to the
	 * ones of this interface
	 */
	public boolean similiarToArguments(FederArguments args)
	{
		return isEqual(null, FederFunction.objectListToClassList(args.getArguments()));
	}
}
