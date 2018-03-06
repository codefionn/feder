package feder.types;

/**
 * This class should handle arrays created for objects
 * (primitive datatype objects and objects created from classes)
 * 
 * @author Fionn Langhans
 * @ingroup types
 */
public class FederArray extends FederBinding {
	private FederBinding type;
	
	/**
	 * @param type0 The type of the array
	 */
	public FederArray (FederBinding type0) {
		type = type0;
	}

	@Override
	/**
	 * @return Returns true if obj is an array and
	 * if the array has the same type as this object
	 */
	public boolean equals(Object obj) {
		if (obj instanceof FederArray) {
			FederArray ar = (FederArray) obj;
			return ar.getType().equals(getType());
		}

		return false;
	}

	/**
	 * @return Returns the type of the object
	 */
	public FederBinding getType() {
		return type;
	}

	/**
	 * @return Returns true if the array's type is a datatype
	 */
	public boolean isDataType() {
		return type instanceof FederClass && ((FederClass) type).isType();
	}

	/**
	 * @return Returns true if the array's type is an interface
	 */
	public boolean isInterface() {
		return type instanceof FederInterface;
	}

	/**
	 * @return Returns true, if the array's type is an interface or datatype
	 */
	public boolean isTypeOrInterface() {
		return type instanceof FederInterface || (type instanceof FederClass && ((FederClass) type).isType());
	}

	@Override
	/**
	 * @return Returns the name of this object
	 */
	public String getName() {
		return type.getName() + "[]";
	}
	
	@Override
	/**
	 * @return Returns the name of this object (in
	 * a code-friendly way)
	 */
	public String getCodeFriendlyName() {
		return "4array_" + type.getName();
	}

	@Override
	/**
	 * @return Returns the name to use in C (the native language)
	 */
	public String generateCName() {
		if (type instanceof FederInterface
				|| ((type instanceof FederClass && ((FederClass) type).isType()))) {
			return "fdtypearray*";
		}
		
		return "fdclassarray*";
	}

	@Override
	/**
	 * @return Always returns false (mustn't be build)
	 */
	public boolean hasToBuild() {
		return false;
	}

	@Override
	/**
	 * This function does nothing
	 * @param build
	 */
	public void setHasToBuild(boolean build) {
		// Nothing here, because arrays mustn't be build
	}

	@Override
	/**
	 * @return Returns null
	 */
	public FederBody getParent() {
		return null;
	}
	
	/**
	 * @return Returns true, if the result type is an array
	 */
	public boolean isArray () {
		return getType() instanceof FederArray;
	}
	
	/**
	 * @return Returns true, if the result type is an class
	 */
	public boolean isClassType () {
		return type instanceof FederClass && !((FederClass) type).isType();
	}
}
