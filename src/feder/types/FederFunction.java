package feder.types;

import java.util.*;

import feder.FederCompiler;

/**
 * @author Fionn Langhans
 */
public class FederFunction extends FederBody implements FederHeaderGen, FederArguments, FederCompileGen
{

	public List<FederObject> arguments = new LinkedList<>();
	public FederBinding returnClass = null;

	@SuppressWarnings("unchecked")
	public FederFunction(FederCompiler compiler0, String name0, FederBody parent0)
	{
		super(compiler0, name0, parent0, FederClass.class, FederFunction.class, FederNamespace.class);

		if (getParent() instanceof FederClass) {
			FederObject obj = new FederObject("this", null);
			obj.setTypeManual((FederClass) getParent());
			obj.isForced = true;
			obj.isOut = true;
//	    obj.parent = getParent ();
			addBinding(obj);
		}
	}

	@Override
	public boolean isEqual(String name0, List<FederBinding> arguments0)
	{
		return isEqual(name0, arguments0, arguments, getName());
	}

	public static boolean isEqual(String name, List<FederBinding> arguments0, List<FederObject> arguments,
	                              String name0)
	{
		if (arguments0.size() != arguments.size())
			return false;

		if (name != null && !name0.equals(name))
			return false;

		for (int i = 0; i < arguments.size(); i++) {
			if (arguments0.get(i) == null && !arguments.get(i).isDataType())
				continue;

			if (arguments0.get(i) == null)
				return false;
			
			if (!areSameTypes(arguments.get(i), arguments0.get(i))) {
				return false;
			}

/*			if (!arguments0.get(i).equals(arguments.get(i).classType) && !(arguments0.get(i) instanceof FederArguments
			        && arguments.get(i).interfaceType != null && ((FederArguments) arguments0.get(i)).isEqual(null,
			                objectListToClassList(arguments.get(i).interfaceType.getArguments()))))
				return false; */
		}

		return true;
	}

	public String argumentsToString()
	{
		StringBuilder sb = new StringBuilder();
		for (FederObject arg : arguments) {
			sb.append("0");
			sb.append(arg.getResultType().getCodeFriendlyName());
			sb.append("_");
		}

		return sb.toString();
	}

	public static String generateArgumentsListString(FederBody parent, List<FederObject> arguments)
	{
		return generateArgumentsListString(parent, arguments, true);
	}

	private static String generateArgumentsListString(FederBody parent, List<FederObject> arguments, boolean b)
	{
		StringBuilder sb = new StringBuilder();

		boolean isFirst = true;

		if (parent != null && parent instanceof FederClass) {
			sb.append(parent.generateCName()).append((b ? "*" : "") + " federobj_this");
			isFirst = false;
		}

		for (FederObject obj : arguments) {
			if (!isFirst)
				sb.append(", ");
			else
				isFirst = false;

			sb.append(obj.getResultType().generateCName()).append(" ");
			sb.append(obj.generateCName());
		}

		return sb.toString();
	}

	@Override
	public String generateCName()
	{
		return "fdfunc_" + ((getParent() instanceof FederClass) ? "3" + getParent().getName() + "_" : "")
		       + getNamespacesToString() + argumentsToString() + getName();
	}

	public static List<FederBinding> objectListToClassList(List<FederObject> list)
	{
		List<FederBinding> classes = new LinkedList<>();
		for (FederObject obj : list) {
			if (obj == null)
				classes.add(null);
			else if (!obj.isInterface())
				classes.add(obj.getResultType());
		}

		return classes;
	}

	@Override
	public String generateInHeader()
	{
		if (isPrivate()) {
			return "";
		}

		StringBuilder appendto = new StringBuilder(
		    "ptr_" + generateCName() + " (" + generateArgumentsListString(getParent(), arguments, false) + ");\n");

		StringBuilder result = new StringBuilder(
		    generateCName() + " (" + generateArgumentsListString(getParent(), getArguments()) + ");\n");

		if (getReturnType() == null) {
			result.insert(0, "void ");
			appendto.insert(0, "void ");
		} else {
			result.insert(0, getReturnType().generateCName() + " ");
			appendto.insert(0, getReturnType().generateCName() + " ");
		}

		if (getParent() instanceof FederClass) {
			result.append(appendto);
		}

		return result.toString();
	}

	@Override
	public String generateInCompile()
	{
		if (!(getParent() instanceof FederClass)) {
			return "";
		}

		StringBuilder appendto = new StringBuilder(
		    "ptr_" + generateCName() + " (" + generateArgumentsListString(getParent(), arguments, false) + ") {\n");

		StringBuilder result = new StringBuilder(
		    generateCName() + " (" + generateArgumentsListString(getParent(), getArguments()) + ");\n");

		if (getReturnType() == null) {
			result.insert(0, "void ");
			appendto.insert(0, "void ");
		} else {
			result.insert(0, getReturnType().generateCName() + " ");
			appendto.insert(0, getReturnType().generateCName() + " ");
		}

		if (isPrivate()) {
			appendto.insert(0, "static ");
			result.insert(0, "static ");
		}

		appendto.append("\t");
		if (getReturnType() != null) {
			appendto.append("return ");
		}

		appendto.append(generateCName() + "(");
		appendto.append("&federobj_this");
		for (FederObject obj : arguments) {
			appendto.append(", ");
			appendto.append(obj.generateCName());
		}

		appendto.append(");\n}\n");

		if (isPrivate()) {
			result.append(appendto);
		} else {
			result = new StringBuilder (appendto);
		}

		return result.toString();
	}

	@Override
	public List<FederObject> getArguments()
	{
		return arguments;
	}

	@Override
	public FederBinding getReturnType()
	{
		return returnClass;
	}

	@Override
	public void setReturnType(FederBinding fc)
	{
		returnClass = fc;
	}

	@Override
	public boolean canBeCalled()
	{
		return true;
	}
}
