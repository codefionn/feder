package feder.types;

import java.util.*;

import feder.FederCompiler;

/**
 * @author Fionn Langhans
 * @ingroup compiler
 */
public class FederFunction extends FederBody implements FederHeaderGen, FederArguments, FederCompileGen
{
	/**
	 * The arguments (objects) of the function
	 */
	public List<FederObject> arguments = new LinkedList<>();
	
	/**
	 * The type, which is returned by the function. Can be null.
	 */
	public FederBinding returnClass = null;

	/**
	 * @param compiler0 The compiler instance to use
	 * @param name0 The name of the function
	 * @param parent0 The body's parent
	 */
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

    public boolean hasArgumentName(String name)
    {
        for (FederObject obj : arguments) {
            if (obj.getName().equals(name)) {
                return true;
            }
        }

        return false;
    }

	@Override
	/**
	 * @param name0 If null, every name will be accepted
	 * @param arguments0 The expected arguments (types)
	 * @return Returns true, if the given arguments are similiar to this function
	 */
	public boolean isEqual(String name0, List<FederBinding> arguments0)
	{
		return isEqual(name0, arguments0, arguments, getName());
	}

	/**
	 * @param name If null, every name will be accepted (First function)
	 * @param arguments0 Expected arguments (types) (First function)
	 * @param arguments Expected arguments (types) (Second function)
	 * @param name0 The name of the second function
	 * @return Returns true, if the given arguments are similar to each other
	 */
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

	/**
	 * @return Returns the current arguments in this function as a string
	 * (separated by '_', each argument type starting with '0')
	 */
	public String argumentsToString()
	{
		StringBuilder sb = new StringBuilder();
		for (FederObject arg : arguments) {
			sb.append(arg.getResultType().getCodeFriendlyName());
			sb.append("_");
		}

		return sb.toString();
	}

	/**
	 * @param parent
	 * @param arguments
	 * @return Returns the arguments, that should be between
	 * the brackets, which are in the function declaration
	 */
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

	/**
	 * @return Returns the name of this function, which should be used in C source code
	 */
	@Override
	public String generateCName()
	{
		String argstostr = argumentsToString();
		String namespacestostr = getNamespacesToString();
		return "fdfunc_" + ((getParent() instanceof FederClass) ? /*"3" +*/ getParent().getName() + "_" : "")
		       + (namespacestostr.isEmpty() ? "" : ("2" + namespacestostr))
		       + (argstostr.isEmpty() ? "" : ("1" + argumentsToString())) + "0" + getName();
	}

	/**
	 * @param list
	 * @return Returns the given objects as a list containing types
	 */
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

	/**
	 * @return Returns a string, which should be generated
	 * in the header file
	 */
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

	/**
	 * @return Returns a string, which should be generated
	 * in a compile/code file
	 */
	@Override
	public String generateInCompile()
	{
		/*if (!(getParent() instanceof FederClass)) {
			return "";
		}*/

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

        if (!(getParent() instanceof FederClass)) {
            if (isPrivate())
                return result.toString();

            return "";
        }

		if (isPrivate()) {
			result.append(appendto);
		} else {
			result = new StringBuilder (appendto);
		}

		return result.toString();
	}

	/**
	 * @return Returns the expected arguments (objects)
	 */
	@Override
	public List<FederObject> getArguments()
	{
		return arguments;
	}

	/**
	 * @return Returns the type, which is returned by this function
	 */
	@Override
	public FederBinding getReturnType()
	{
		return returnClass;
	}

	/**
	 * Sets the current type, which should be returned by this function,
	 * with 'fc'
	 */
	@Override
	public void setReturnType(FederBinding fc)
	{
		returnClass = fc;
	}

	/**
	 * @return Always true
	 */
	@Override
	public boolean canBeCalled()
	{
		return true;
	}
}
