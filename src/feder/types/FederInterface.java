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

	public List<FederObject> arguments = new LinkedList<>();
	public FederBinding returnType;
	private boolean cancall = false;

	/**
	 * @param name0
	 *            Name of the Interface
	 */
	@SuppressWarnings("unchecked")
	public FederInterface(FederCompiler compiler0, String name0, FederBody parent)
	{
		super(compiler0, name0, parent);
	}

	public FederInterface interfaceFrom(FederCompiler compiler0, String name, FederBody parent)
	{
		if (cancall) {
			throw new RuntimeException("Cannot create interface from" + " already object-interface");
		}

		return new FederInterface(compiler0, name, parent, arguments, returnType);
	}

	@SuppressWarnings("unchecked")
	public FederInterface(FederCompiler compiler0, String name0, FederBody parent, List<FederObject> arguments0,
	                      FederBinding returnType0)
	{
		super(compiler0, name0, parent);

		arguments = arguments0;
		returnType = returnType0;
		cancall = true;
	}

	@Override
	public String generateCName()
	{
		if (canBeCalled()) {
			return "federobj_" + getName();
		}

		String result = "federint_" + getName();
		return getNamespacesToString() + result;
	}

	@Override
	public List<FederObject> getArguments()
	{
		return arguments;
	}

	@Override
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
	public void setReturnType(FederBinding fc)
	{
		returnType = fc;
	}

	@Override
	public boolean canBeCalled()
	{
		return cancall;
	}

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

	public boolean similiarToArguments(FederArguments args)
	{
		return isEqual(null, FederFunction.objectListToClassList(args.getArguments()));
	}
}
