package feder.types;

import feder.FederCompiler;

/**
 * 
 * @author Fionn Langhans
 * @ingroup compiler
 */
public class FederNamespace extends FederBody implements FederHeaderGen
{

	/**
	 * 
	 * @param compiler0 The compiler instance to use
	 * @param name0 The name of the namespace
	 * @param parent0 The parent of this body
	 */
	@SuppressWarnings("unchecked")
	public FederNamespace(FederCompiler compiler0, String name0, FederBody parent0)
	{
		super(compiler0, name0, parent0);
	}

	/**
	 * @return Returns a name for C source source (1 + getName())
	 */
	@Override
	public String generateCName()
	{
		// 1 to avoid any name issues
		return getName();
	}

	/**
	 * @return Returns a string, which should be generated in
	 * the header file
	 */
	@Override
	public String generateInHeader()
	{
		if (this.getName().startsWith("h_intern")) {
			String res = compile_file_text.toString();
			compile_file_text = new StringBuilder();
			return res + "\n" + inFrontOfSyntax().substring(1) + "// }\n\n";
		}

		return "";
	}
}
