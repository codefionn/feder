package feder.types;

import java.util.*;

import feder.FederCompiler;

/**
 * @author Fionn Langhans
 * @ingroup types
 */
public class FederClass extends FederBody implements FederHeaderGen, FederCompileGen
{

	private FederClass inherit_parent;
	private boolean istype = false;

	/**
	 * @param compiler0 The compiler instance to use
	 * @param name0 The name/binding of the class/datatype
	 * @param parent0 The parent of this body
	 */
	@SuppressWarnings("unchecked")
	public FederClass(FederCompiler compiler0, String name0, FederBody parent0)
	{
		super(compiler0, name0, parent0, FederNamespace.class, FederClass.class, FederAutomat.class);
	}

	/**
	 * @param compiler0 The compiler instance to use
	 * @param name0 The name/binding of the class/datatype
	 * @param parent0 The parent of this body
	 * @param istype0 Is type ?
	 */
	@SuppressWarnings("unchecked")
	public FederClass(FederCompiler compiler0, String name0, FederBody parent0, boolean istype0)
	{
		super(compiler0, name0, parent0, FederNamespace.class, FederClass.class, FederAutomat.class, FederObject.class);
		
		if(!istype0) {
			blacklist_classes[blacklist_classes.length-1] = FederClass.class;
		}
		
		istype = istype0;
	}

	/**
	 * @return Returns the parent 'this class' inherits from (the class in Feder)
	 */
	public FederClass getInheritParent()
	{
		return inherit_parent;
	}

	/**
	 * @return Returns true if this 'class' is a datatype (declared with 'type')
	 */
	public boolean isType ()
	{
		return istype;
	}

	/**
	 * @param requestingbody The body, which requests the binding
	 * @param name0 the binding's name
	 * @param allowparent Allow to search through parent (bodies)
	 * @param bodiesChecked Race detection
	 * @return Returns a fitting binding. If nothing was found, null.
	 */
	@Override
	protected FederBinding getBinding(FederBody requestingbody, String name0,
			boolean allowparent, List<FederBody> bodiesChecked)
	{
		FederBinding result = super.getBinding(requestingbody, name0, allowparent, bodiesChecked);
		if (result == null && inherit_parent != null) {
			return inherit_parent.getBinding(requestingbody, name0, allowparent, bodiesChecked);
		}

		return result;
	}

	/**
	 * @param requestingbody The body, which requests the binding
	 * @param name1 The binding's name
	 * @param arguments The arguments, which the function should have.
	 * @param allowParent Allow to search through parents (bodies)
	 * @param bodiesChecked Race detection
	 * @return Returns a fitting function name. If nothing was found, null.
	 */
	@Override
	protected FederArguments getFunction (FederBody requestingbody, String name1,
		List<FederBinding> arguments, boolean allowParent, List<FederBody> bodiesChecked) {
		
		FederArguments result = super.getFunction (requestingbody, name1,
				arguments, allowParent, bodiesChecked);
		if (result == null && inherit_parent != null) {
			return inherit_parent.getFunction (requestingbody, name1,
				arguments, allowParent, bodiesChecked);
		}

		return result;
	}

	/**
	 * This method sets the inherited class of 'this class'
	 *
	 * @param inherit_parent0
	 */
	public void setInheritParent(FederClass inherit_parent0)
	{
		if(isType()) {
			return;
		}

		if (inherit_parent != null && inherit_parent != inherit_parent0) {
			throw new RuntimeException("Can't set a new inherit class!");
		} else if (inherit_parent != null) {
			return;
		}

		inherit_parent = inherit_parent0;
		/*int len = inherit_parent.getBindings().size();
		for (int i = 0; i < len; i++) {
			FederBinding bind = inherit_parent.getBindings().get(i);
			FederBinding resBind = null;
			if (bind instanceof FederFunction) {
				FederFunction func0 = (FederFunction) bind;
				FederFunction newfunc0 = new FederFunction(getCompiler(), func0.getName(), this);
				newfunc0.arguments = func0.arguments;
				newfunc0.compile_file_text.append("fdobject * " + newfunc0.generateCName() + " (");
				newfunc0.compile_file_text
				.append(FederFunction.generateArgumentsListString(this, newfunc0.arguments) + ") {\n");

				newfunc0.compile_file_text
				.append("\tif (federobj_this) fdIncreaseUsage ((fdobject*) federobj_this);\n");
				for (FederObject arg : newfunc0.arguments) {
					newfunc0.compile_file_text
					.append("\tif (" + arg + ") fdIncreaseUsage ((fdobject*) " + arg.generateCName() + ");\n");
				}

				newfunc0.compile_file_text.append("\n");

				newfunc0.compile_file_text.append("\t" + func0.generateCName() + "(");
				newfunc0.compile_file_text.append("(" + getInheritParent().generateCName() + "*) federobj_this");
				for (int ii = 0; ii < newfunc0.arguments.size(); ii++) {
					newfunc0.compile_file_text.append(", ");

					newfunc0.compile_file_text.append(newfunc0.arguments.get(ii));
				}

				newfunc0.compile_file_text.append(");\n");
				resBind = newfunc0;
			} else {
				resBind = bind;
			}

			insertBinding(resBind, i);
		}*/
	}

	@Override
	/**
	 * @return Returns the name for the native language (C)
	 */
	public String generateCName()
	{
		return generateCNameOnly() + (isType() ? "" : "*");
	}

	/**
	 * @return Returns the name for the native language (C)
	 * without memory specifiers or anything like that
	 */
	public String generateCNameOnly()
	{
		return "fdc_" + getNamespacesToString() + getName();
	}

	@Override
	/**
	 * @return Returns a string, which should be generated in
	 * the header file.
	 */
	public String generateInHeader ()
	{
		String result = new String();
		/*result += "struct _array" + generateCNameOnly() + ";\n"
						+ "typedef struct _array" + generateCNameOnly() + " " + generateCNameOnly() + ";\n"
						+ "void fdDeleteClass_array" + generateCNameOnly() + "(void * data);\n"
						+ generateCName() + " fdCreateClass_array" + generateCNameOnly() + " (int size);\n"
						+ "void fdIncreaseUsage_array" + generateCNameOnly() + "(fdobject * object, int usage);\n";*/

		if (isType()) {
			return result + "\n" + compile_file_text.toString();
		}

		result += "struct _" + generateCNameOnly() + ";\n"
		                + "typedef struct _" + generateCNameOnly() + " " + generateCNameOnly() + ";\n"
		                + "void fdDeleteClass_" + generateCNameOnly() + " (void * data);\n"
		                + generateCName() + " fdCreateClass_" + generateCNameOnly() + " ();\n"
						+ "void fdIncreaseUsage_"
		                + generateCNameOnly() + " (fdobject * object, int usage);" + "\n";

		return result;
	}

	@Override
	/**
	 * @return Returns a string, which should be generated in
	 * the compile/code file.
	 */
	public String generateInCompile()
	{
		if (isType()) {
			return "";
		}

		StringBuilder result = new StringBuilder();

		result.append("void fdDeleteClass_").append(generateCNameOnly()).append(" (void * data0) {\n");

		if (getCompiler().isDebug()) {
			result.append("  printf (\"\\nTrying to delete a object from the class \'" + getName() + "\'\\n\");\n");
		}

		result.append("  " + generateCName() + " result = (" + generateCName() + ") data0;\n");

		FederFunction func = (FederFunction) getFunction(this, "del", new LinkedList<>(), false);
		if (func != null && func.returnClass == null) {
			result.append("  ").append(func.generateCName()).append("((");
			result.append(generateCName()).append("*) &result);\n");
		}

		for (FederBinding binding : getBindings()) {
			if (!(binding instanceof FederObject))
				continue;
			if (!((FederObject) binding).isGarbagable())
				continue;

			if (getCompiler().isDebug()) {
				result.append("  printf (\"  Call remove of " + binding.generateCName() + "\\n\");\n");
			}

			result.append("  fdRemoveObject ((fdobject*) ((" + generateCName() + ") result)->" + binding.generateCName() + ");\n");
		}

		result.append("  free ((" + generateCName() + ") result);\n");

		result.append("}\n\n");

		result.append(generateCName() + " fdCreateClass_");
		result.append(generateCNameOnly()).append(" () {\n");

		result.append("  ").append(generateCName());
		result.append(" result = (" + generateCName());
		result.append( ") malloc (sizeof (" + generateCNameOnly() + "));\n");

		result.append("  ").append("result->usage = 1;\n");

		result.append("  ").append("result->usagefn = ");
		result.append("fdIncreaseUsage_");
		result.append(generateCNameOnly()).append(";\n");

		result.append("  ").append("result->delfn = ");
		result.append("fdDeleteClass_");
		result.append(generateCNameOnly()).append(";\n");

		for (FederBinding binding : getBindings()) {
			if (binding instanceof FederObject) {
				FederObject obj = (FederObject) binding;
				if (obj.isDataType())
					continue;

				result.append("  result->" + obj.generateCName() + " = NULL;\n");
			}
		}

		func = (FederFunction) getFunction(this, "init", new LinkedList<>(), false);
		if (func != null && func.returnClass == null) {
			result.append("  ").append(func.generateCName()).append("((").append(generateCName());
			result.append("*) &result);\n");
		}

		result.append("result->usage -= 1;\n");

		// result.append(" result->usage = 0;\n");
		result.append("  return result;\n");

		result.append("}\n\n");

		result.append("void fdIncreaseUsage_").append(generateCNameOnly());
		result.append("(fdobject * object, int usage) {\n");
		result.append("  if (usage) {\n");
		result.append("    object->usage++;\n");

		/*
		 * for (FederBinding binding : getBindings()) { if (binding instanceof
		 * FederObject) { FederObject obj = (FederObject) binding; if
		 * (obj.isInterface()) continue;
		 *
		 * result.append("    fdIncreaseUsage (((").append(generateCName());
		 * result.append("*) object)->").append(obj.generateCName());
		 * result.append(");\n"); } }
		 */

		result.append("  } else {\n");
		result.append("    object->usage--;\n");

		/*
		 * for (FederBinding binding : getBindings()) { if (binding instanceof
		 * FederObject) { FederObject obj = (FederObject) binding; if
		 * (obj.isInterface()) continue;
		 *
		 * result.append("    fdDecreaseUsage (((").append(generateCName());
		 * result.append("*) object)->").append(obj.generateCName());
		 * result.append(");\n"); } }
		 */

		result.append("  }\n}\n\n");

		return result.toString();
	}
}
