package feder.types;

import java.util.List;

/**
 *
 * @author Fionn Langhans
 */
public interface FederArguments
{
	public List<FederObject> getArguments();

	public FederBinding getReturnType();

	public void setReturnType(FederBinding fc);

	public String getName();

	public boolean isEqual(String name, List<FederBinding> args);

	public String generateCName();

	public FederBody getParent();

	public boolean canBeCalled();

	public boolean isPrivate();
}
