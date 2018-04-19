/*
 * Copyright (C) 2018 Fionn Langhans
 */
package feder.types;

import java.util.List;

/**
 *
 * @author Fionn Langhans
 */
public interface FederArguments
{
	/**
	 * @return The arguments of the function
	 */
	public List<FederObject> getArguments();

	/**
	 * @return The return type of the function
	 */
	public FederBinding getReturnType();

	/**
	 * @param fc
	 * @return Set the return type of thic function to 'fc'
	 */
	public void setReturnType(FederBinding fc);

	/**
	 * @return The name of the "function"
	 */
	public String getName();

	/**
	 * @param name If null, everyname will be accepted
	 * @param args The arguments, the function should have
	 * @return True if this is equal to the function described by 'name' and 'args'
	 */
	public boolean isEqual(String name, List<FederBinding> args);

	/**
	 * @return The name for C source code
	 */
	public String generateCName();

	/**
	 * @return The parent (body) of this function
	 */
	public FederBody getParent();

	/**
	 * @return True if the function-like contruct is callable/executable
	 */
	public boolean canBeCalled();

	/**
	 * @return Returns true if this "function" is private
	 */
	public boolean isPrivate();
}
