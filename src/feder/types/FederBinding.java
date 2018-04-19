/*
 * Copyright (C) 2018 Fionn Langhans
 */
package feder.types;

/**
 * 
 * @author Fionn Langhans
 * @ingroup utils
 */
public abstract class FederBinding
{
	/**
	 * @return Should return the name of the binding
	 */
	public abstract String getName();
	
	/**
	 * @return Should return a 'code friendly' name
	 * (something like generateCName, but compatible to
	 * construct bigger names)
	 */
	public abstract String getCodeFriendlyName ();

	/**
	 * @return Returns the name, which should be used in C.
	 */
	public abstract String generateCName();

	/**
	 * @return Should return true, if the binding has to be build.
	 */
	public abstract boolean hasToBuild();

	/**
	 * @param build You probably want to set this to 'false'
	 */
	public abstract void setHasToBuild(boolean build);

	/**
	 * @return Returns true, if the binding should be private
	 */
	public boolean isPrivate()
	{
		return getName().startsWith("_");
	}

	/**
	 * @return Returns true, if the binding should be immutable
	 */
	public boolean isImmutable()
	{
		return Character.isUpperCase(getName().charAt((isPrivate() ? 1 : 0)));
	}

	/**
	 * @return Should return the parent of the binding
	 */
	public abstract FederBody getParent();
	
	/**
	 * @param bind
	 * @return Returns true, if the binding 'bind' is garbageable
	 * (true if class and not datatype or if type is array)
	 */
	public static boolean IsGarbagable (FederBinding bind) {
		return (bind instanceof FederClass && !((FederClass) bind).isType()) || bind instanceof FederArray;
	}
	
	/**
	 * @param bind0
	 * @param bind1
	 * @return Returns true, if bind0 and bind1 have the same types or
	 * are the same (if bind0 or bind1 is null, the result is false).
	 */
	public static boolean areSameTypes (FederBinding bind0, FederBinding bind1) {
		if (bind0 == null || bind1 == null) {
			return false;
		}

		if (bind0 instanceof FederObject) {
			return areSameTypes(((FederObject) bind0).getResultType(), bind1);
		}
		
		if (bind1 instanceof FederObject) {
			return areSameTypes(bind0, ((FederObject) bind1).getResultType());
		}
		
		if (bind0 instanceof FederArguments && bind1 instanceof FederArguments) {
			FederArguments inter0 = (FederArguments) bind0;
			FederArguments inter1 = (FederArguments) bind1;
			return FederFunction.isEqual(null, FederFunction.objectListToClassList(inter1.getArguments()), inter0.getArguments(), "");
		} else if (bind0 instanceof FederInterface || bind1 instanceof FederInterface) {
			return false;
		}
		
		if (bind0 instanceof FederArray) {
			return bind0.equals(bind1);
		}
		
		return bind0 == bind1;
	}
	
	/**
	 * @param binding
	 * @return Returns true, if binding is a datatype.
	 */
	public static boolean isDataType (FederBinding binding) {
		return (binding instanceof FederClass && ((FederClass) binding).isType())
				|| (binding instanceof FederObject && ((FederObject)  binding).isDataType());
	}
	
	/**
	 * @param binding
	 * @return Returns true, if binding is a class type
	 */
	public static boolean isClassType (FederBinding binding) {
		return (binding instanceof FederClass && !((FederClass) binding).isType())
				|| (binding instanceof FederObject && ((FederObject)  binding).isClassObject());
	}
	
	/**
	 * 
	 * @param binding
	 * @return Returns true, if the binding is an interface type
	 */
	public static boolean isInterfaceType (FederBinding binding) {
		return (binding instanceof FederInterface)
				|| (binding instanceof FederObject && ((FederObject) binding).isInterface());
	}
	
	/**
	 * @param binding
	 * @return
	 */
	public static boolean isArrayType (FederBinding binding) {
		return (binding instanceof FederArray) 
				|| (binding instanceof FederObject
						&& ((FederObject) binding).isArray());
	}
	
	/**
	 * @param binding
	 * @return
	 */
	public static boolean isTypeArrayType (FederBinding binding) {
		return (binding instanceof FederArray && ((FederArray) binding).isDataType()) 
				|| (binding instanceof FederObject
						&& ((FederObject) binding).isArray()
						&& ((FederArray) ((FederObject) binding).getResultType()).isDataType());
	}
	
	/**
	 * @param binding
	 * @return
	 */
	public static boolean isClassArrayType (FederBinding binding) {
		return (binding instanceof FederArray && ((FederArray) binding).isClassType()) 
				|| (binding instanceof FederObject
						&& ((FederObject) binding).isArray()
						&& ((FederArray) ((FederObject) binding).getResultType()).isClassType());
	}
	
	/**
	 * @param binding
	 * @return
	 */
	public static boolean isArrayArrayType (FederBinding binding) {
		return (binding instanceof FederArray && ((FederArray) binding).isArray()) 
				|| (binding instanceof FederObject
						&& ((FederObject) binding).isArray()
						&& ((FederArray) ((FederObject) binding).getResultType()).isArray());
	}
}
