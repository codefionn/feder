package feder.types;

/**
 * Representing a object in Feder
 * @author Fionn Langhans
 * @ingroup compiler
 */
public class FederObject extends FederBinding implements FederHeaderGen, FederCompileGen
{
	/**
	 * The binding's name
	 */
	private String name;
	
	/**
	 * Force the type of the object
	 */
	public boolean isForced = false;
	
	/**
	 * The parent of this object
	 */
	public FederBody parent = null;

	/**
	 * The resulttype of this object (the object's type)
	 */
	private FederBinding resulttype;

	/**
	 * Is the object global ?
	 */
	public boolean isGlobal = false;
	
	/**
	 * Is the object an 'out' object 
	 * (an extra pointer is necessary) ?
	 */
	public boolean isOut = false;

	/**
	 * True if the object has to be 'build'
	 */
	private boolean build = true;
	
	/**
	 * Use the object's name directly
	 * as C name
	 */
	public boolean raw_c_gen = false;

	/**
	 * 
	 * @param name0 the name of binding
	 * @param parent0 the parent of this object
	 * (can be null, but sometimes not)
	 */
	public FederObject(String name0, FederBody parent0)
	{
		name = name0;
		parent = parent0;

		if (parent != null) {
			parent.getMainNamespace().getCompiler().buildOrder.add (this);
		}
	}

	@Override
	public boolean hasToBuild()
	{
		return build;
	}

	@Override
	public void setHasToBuild(boolean b)
	{
		build = b;
	}

	@Override
	public FederBody getParent ()
	{
		return parent;
	}
	
	public boolean isType (FederBinding binding) {
		return FederBinding.areSameTypes(this, binding);
	}

	/**
	 * @return Returns true, if the type of this object is an interface.
	 */
	public boolean isInterface()
	{
		return resulttype != null && resulttype instanceof FederInterface;
	}

	/**
	 * @return Returns true, if the type of this object is an datatype.
	 */
	public boolean isDataType()
	{
		return resulttype != null && resulttype instanceof FederClass && ((FederClass) resulttype).isType();
	}
	
	/**
	 * @return Returns true, if the type of this object is an array.
	 */
	public boolean isArray()
	{
		return resulttype != null && resulttype instanceof FederArray;
	}
	
	public boolean isClassObject()
	{
		return !isInterface() && !isDataType() && !isArray();
	}
	
	public boolean hasSubtypes() {
		return isClassObject() || isDataType();
	}
	
	/**
	 * Checks if allowd to change type
	 */
	public void setTypeIntelligent (FederBinding bind) {
		if (isImmutable()) {
			throw new RuntimeException("The object can't changed its type, because it is immutable!");
		}

		if (bind instanceof FederArguments && isInterface()) {
			if (!areSameTypes(bind, getResultType())) {
				String s = "";
				if (getResultType() != null) {
					s += getResultType().getName();
					if (bind != null) {
						s += " -> ";
					}
				}
				
				if (bind != null) {
					s += bind.getName();
				}
				throw new RuntimeException("This object can't change its type: " + s);
			} else {
				return;
			}
		}
		
		if (bind == getResultType()) {
			return;
		}

		if (isForced || !isClassObject()) {
			String s = "";
			if (getResultType() != null) {
				s += getResultType().getName();
			} else {
				s += "null";
			}
			
			s += " -> ";

			if (bind != null) {
				s += bind.getName();
			} else {
				s += "null";
			}

			throw new RuntimeException("This object can't change its type: " + s);
		}
		
		if (!(bind instanceof FederClass) || ((FederClass) bind).isType()) {
			throw new RuntimeException("Can only change type to object with class as a type!");
		}
		
		resulttype = bind;
	}
	
	/**
	 * Doesn't do any checks
	 * @param bind
	 */
	public void setTypeManual(FederBinding bind) {
		resulttype = bind;
	}
	
	/**
	 * @return Returns the type of this object
	 */
	public FederBinding getResultType() {
		return resulttype;
	}

	/**
	 * @return Returns true, if this object can be
	 * collected by the garbage collection
	 */
	public boolean isGarbagable()
	{
		return isClassObject() || isArray();
	}

	@Override
	/**
	 * @return THE binding
	 */
	public String getName()
	{
		return name;
	}

	@Override
	/**
	 * @return the name to use in C source code
	 */
	public String generateCName()
	{
		if (raw_c_gen) {
			return getName();
		}
		
		if (isGlobal && isDataType()) {
			return "(*federobj_" + getName() + ")";
		}

		if (isOut) {
			return "(*federobj_" + getName() + ")";
		}

		return "federobj_" + getName();
	}

	/**
	 * @return Returns the C source code name without any memory modifiers
	 */
	public String generateCNameOnly()
	{
		if (raw_c_gen) {
			return getName();
		}
		
		return "federobj_" + getName();
	}

	private static final String infront = "";

	@Override
	/**
	 * @return Returns a string, which should be generated
	 * in the header file
	 */
	public String generateInHeader()
	{
		if (!isGlobal) {
			return "";
		}

		if (isInterface()) {
			return "extern " + resulttype.generateCName() + " " + generateCName() + ";\n";
		}

		String inbetween = "fdobject*";
		if (isForced) {
			inbetween = resulttype.generateCName();
		}

		if (isDataType()) {
			inbetween = inbetween + "*";
		}

		return "extern " + infront + inbetween + " " + generateCNameOnly() + ";\n";
	}

	@Override
	/**
	 * @return Returns a string, which should be generated
	 * in the compile/code file
	 */
	public String generateInCompile()
	{
		if (!isGlobal) {
			return "";
		}

		String inbetween = "fdobject*";
		if (isForced) {
			inbetween = resulttype.generateCName();
		}

		if (isDataType()) {
			inbetween = inbetween + "*";
		}

		return infront + (isInterface() ? resulttype.generateCName() : (inbetween + " ")) + generateCNameOnly() + ";\n";
	}

	@Override
	/**
	 * @return the object's name is a code friendly name,
	 * so @link FederObject.getName getName @endlink is returned.
	 */
	public String getCodeFriendlyName() {
		return getName();
	}
}
