package feder.types;

import feder.FederCompiler;

public class FederNamespace extends FederBody implements FederHeaderGen
{

	@SuppressWarnings("unchecked")
	public FederNamespace(FederCompiler compiler0, String name0, FederBody parent0)
	{
		super(compiler0, name0, parent0);
	}

	@Override
	public String generateCName()
	{
		// 1 to avoid any name issues
		return "1" + getName();
	}

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
