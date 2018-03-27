package feder;

import java.util.*;

import feder.types.*;

/**
 * The heart of the Java Feder compiler (jFC or jfederc). A syntax tree element
 * can split itself if needed
 * (in the @link SyntaxTreeElement.compile compile @endlink ) method.
 *
 * @author Fionn Langhans
 * @ingroup compiler
 */
public class SyntaxTreeElement {
	/**
	 * Current body
	 */
	public FederBody body;

	/**
	 * Tokens describing the contents of stringsOfTokens
	 */
	public List<String> tokens;

	/**
	 * The real thing used by the source code
	 */
	public List<String> stringsOfTokens;

	/**
	 * Current line number
	 */
	public int line;

	/**
	 * Main == true:
	 * 	Able to bind objects
	 */
	public boolean isMain = false;

	/**
	 * This boolean is true if the analyzed execution should be 'global' (only value
	 * declarations allowed)
	 */
	public boolean isGlobal = false;

	/**
	 * The parent branch of this one
	 */
	public SyntaxTreeElement parent = null;

	/**
	 * The branch currently in use (not reliable)
	 */
	public SyntaxTreeElement currentInUse = null;

	/**
	 * The results of the source code
	 * (like classes, interfaces, functions, arrays)
	 */
	public List<FederBinding> returnedClasses = new LinkedList<>();

	/**
	 * Not really supported ...
	 * returns an object (if possible), used by the 'current' source code
	 */
	public FederObject returnedObject = null;

	/**
	 * This is only for argument like source code (with ','s)
	 */
	public List<String> results_list = new LinkedList<>();

	/**
	 * The compiler instance used
	 */
	public FederCompiler compiler;

	/**
	 * The result of the 'compile' method
	 */
	public StringBuilder result = new StringBuilder();

	/**
	 * The currently used binding (at default the current body)
	 * Changes if source code calls classes, objects and so on
	 */
	public FederBinding getfrombinding = null;

	/**
	 * This value is true if the last list element of 'tokens' was a dot
	 */
	public boolean wasdotinfront = false;

	/**
	 * The currently used token
	 */
	private String token;

	/**
	 * The currently used value of the token
	 */
	private String stoken;

	/**
	 * @param compiler0 The compiler instance to use
	 * @param body0 The current body of the current compiler instance
	 * @param line0 The current line number in the source code
	 */
	public SyntaxTreeElement(FederCompiler compiler0, FederBody body0, int line0) {
		compiler = compiler0;
		body = body0;
		tokens = new ArrayList<>();
		stringsOfTokens = new ArrayList<>();
		line = line0;
	}

	/**
	 *
	 * @param compiler0 The compiler instance to use
	 * @param body0 The current body of the current compiler instance
	 * @param line0 The current line number in the source code
	 * @param tokens0 Tokens (keys) to analyze and compile
	 * @param stringsOfTokens0 Values of the tokens, which are
	 * equal  to the ones used in the source code
	 */
	public SyntaxTreeElement(FederCompiler compiler0, FederBody body0, int line0, List<String> tokens0,
	                         List<String> stringsOfTokens0) {
		compiler = compiler0;
		body = body0;
		tokens = tokens0;
		stringsOfTokens = stringsOfTokens0;
		line = line0;
	}

	/**
	 * Current read position in the list 'tokens'|'stringsOfTokens'
	 */
	public int indexToken;
	public List<FederBinding> getfromhistory = new LinkedList<>();

	/**
	 * Sets many public available objects from this class to the
	 * ones used in element.
	 */
	public void set (SyntaxTreeElement element) {
		returnedClasses = element.returnedClasses;
		body = element.body;
		line = element.line;
		//isMain = element.isMain;
		//isGlobal = element.isGlobal;
		returnedObject = element.returnedObject;
		results_list = element.results_list;
		result = element.result;
		indexToken = element.indexToken;
		compiler = element.compiler;
	}

	/**
	 * @return Returns true if the current scope (tokens between '(' and ')' are
	 *         ignored) contains a comma
	 */
	public int containsCommaInCurrentScope() {
		int count = 0;
		int index = 0;
		for (String token0 : tokens) {
			if (token0.equals("("))
				count++;
			else if (token0.equalsIgnoreCase(")"))
				count--;
			else if (token0.equals(",") && count == 0)
				return index;

			index++;
		}

		return -1;
	}

	/**
	 * This method generates an ending at the end of the current block
	 * @param pos This is printed, when an error arises
	 * @param completly Remove all garbageable objects (not just the ones in the current block)
	 * @param ignore Object to ignore (currently unused)
	 */
	private void generateEnding(String pos, boolean completly, FederObject ignore) {
		generateEnding(pos, completly, ignore, compiler.allowMain);
	}

	/**
	 * This method generates an ending at the end of the current block
	 * @param pos This is printed, when an error arises
	 * @param completely Remove all garbageable objects (not just the ones in the current block)
	 * @param ignore Object to ignore (currently unused)
	 * @param global Is this happening in a global context (currently unused)
	 */
	private void generateEnding(String pos, boolean completely, FederObject ignore, boolean global) {
		SyntaxTreeElementUtils.generateEnding(pos, body, completely, ignore, global);
	}

	/**
	 * If there's a ',' in the current scope (not between '(', ')', '[', ']') this
	 * method splits the tokens into parts and compiles them, and then merges them
	 * together
	 */
	private boolean fixCommasInCurrentScope() {
		if (containsCommaInCurrentScope() != -1) {
			// Contains comma, we have to cut the tokens in to several parts
			List<String> tokens0 = new LinkedList<>();
			List<String> stringsOfTokens0 = new LinkedList<>();
			List<SyntaxTreeElement> stes = new LinkedList<>();

			int bracket_count = 0;

			for (int i = 0; i < tokens.size(); i++) {
				String token0 = tokens.get(i);
				String stoken0 = stringsOfTokens.get(i);

				if (token0.equals("(") || token0.equals("["))
					bracket_count++;
				else if (token0.equals(")") || token0.equals("]"))
					bracket_count--;

				if (token0.equals(",") && bracket_count == 0) {
					stes.add(new SyntaxTreeElement(compiler, body, line, tokens0, stringsOfTokens0));
					tokens0 = new LinkedList<>();
					stringsOfTokens0 = new LinkedList<>();
				} else {
					tokens0.add(token0);
					stringsOfTokens0.add(stoken0);
				}
			}

			stes.add(new SyntaxTreeElement(compiler, body, line, tokens0, stringsOfTokens0));

			int i = 0;
			for (SyntaxTreeElement ste : stes) {
				if (i == 0 && isMain) {
					ste.isMain = true;
				}

				results_list.add(ste.compile().toString());
				returnedClasses.addAll(ste.returnedClasses);
				i++;
			}

			boolean isfirst = true;
			for (String res : results_list) {
				if (!isfirst) {
					result.append(", ");
				} else {
					isfirst = false;
				}

				result.append(res);
			}

			return true;
		}

		return false;
	}

	/**
	 * A type declaration:
	 *     type '"' [native_type] '"' [feder_binding]
	 * @return Returns compiled source code
	 *
	 * @ingroup types
	 */
	private StringBuilder workOnTypeDeclaration() {
		String cname = stringsOfTokens.get(indexToken);
		String fname = stringsOfTokens.get(indexToken + 1);

		FederBinding binding = getFromBinding(body, fname, false);
		if (binding != null && !(binding instanceof FederClass)) {
			throw new RuntimeException(fname + " does already exist, but it is not a 'type'");
		}

		if (binding != null) {
			FederClass fc = (FederClass) binding;
			if (!fc.isType()) {
				throw new RuntimeException(fname + "does already exist. but is not a 'type'");
			}

			fc.setParent(body);
			body = fc;
			return new StringBuilder();
		}

		FederClass fc = new FederClass(compiler, fname, body, true);
		body.addBinding(fc);
		body = fc;

		return new StringBuilder("typedef " + cname + " " + fc.generateCName() + ";");
	}

	/**
	 * A class declaration:
	 *     class [feder_binding] [existing_classes]*
	 *
	 * @return Returns compiled source code
	 *
	 * @ingroup types
	 */
	private StringBuilder workOnClassDeclaration() {
		if (result.length() != 0) {
			throw new RuntimeException("No instructions are allowed in front of the 'class' keyword!");
		}

		String name = stringsOfTokens.get(indexToken);

		FederBinding binding = getFromBinding(body, name, false);
		if (binding != null && binding instanceof FederClass) {
			// Integrate new code in class, to allow forward declaration
			FederClass fc = (FederClass) binding;
			fc.setParent(body);
			body = fc;

			return new StringBuilder();
		} else if (binding != null) {
			throw new RuntimeException("Already declared in current scope and the declared binding is not an class!");
		}

		// Create a new class
		FederClass fc = new FederClass(compiler, name, body);
		body.addBinding(fc);
		body = fc;

		if (indexToken + 1 != tokens.size()) {
			SyntaxTreeElement ste = newBranchAt(indexToken + 1);
			ste.compile();
			/*
			 * if (sb.length() != 0) { throw new RuntimeException(
			 * "No instruction should be given there!"); }
			 */

			if (!(ste.getfrombinding instanceof FederClass)) {
				throw new RuntimeException("There should only be given a class!");
			}

			fc.setInheritParent((FederClass) ste.getfrombinding);
		}

		StringBuilder result0 = new StringBuilder("struct _" + fc.generateCNameOnly() + " {\n");
		if (fc.getInheritParent() != null) {
			String substr = fc.getInheritParent().compile_file_text
			                .substring(fc.getInheritParent().compile_file_text.indexOf("\n") + 1);
			result0.append(substr);
		} else {
			FederRule ruleHeader = compiler.getApplyableRuleForStruct("basic_object_header");
			if (ruleHeader == null) {
				throw new RuntimeException("Basic structure header");
			}

			/*result0.append(body.inFrontOfSyntax() + "int usage;\n"
			               + body.inFrontOfSyntax() + "PFDGETELEMENT getelement;\n"
						   + body.inFrontOfSyntax() + "int length;\n"
			               + body.inFrontOfSyntax() + "char flag;\n");*/
			result0.append (ruleHeader.getToApply());
		}

		return result0;
	}

	/**
	 * A namespace declaration:
	 *     namespace [feder_binding]
	 * @return Returns (possibly) compiled source code
	 */
	private StringBuilder workOnNamespaceDeclaration() {
		if (result.length() != 0) {
			throw new RuntimeException("Doesn't allow any instructions in front of 'namespace'!");
		}

		String name = stringsOfTokens.get(indexToken);
		FederBinding binding = getFromBinding(body, name, false);
		if (binding != null && binding instanceof FederNamespace) {
			// Integrate new code in the namespace, to allow forward declaration
			FederNamespace fn = (FederNamespace) binding;
			fn.setParent(body);
			body = fn;

			return new StringBuilder();
		} else if (binding != null) {
			throw new RuntimeException(
			    "Already declared in current scope and the declared binding is not an namespace!");
		}

		// Create a new namespace
		FederNamespace fn = new FederNamespace(compiler, name, body);
		body.addBinding(fn);
		body = fn;

		String infront = "";

		if (!fn.getName().startsWith("h_intern") && !fn.getName().startsWith("c_intern")) {
			FederRule ruleCreateRoot = compiler.getApplyableRuleForStruct("create_root");
			if (ruleCreateRoot == null) {
				throw new RuntimeException("Struct rule 'create_root' doesn't exist!");
			}

			infront = ruleCreateRoot.applyRule (fn, fn.getIdentifier());
		}

		return new StringBuilder(infront
			+ "// namespace " + name + " {");
	}

	/**
	 * Function declaration:
	 *     ( [return_type] ) 'func'|'interface' [feder_binding] ( '(' [argument] ( ',' [argument] ) ')' )
	 *
	 * @param token
	 * @return Returns compiled source code
	 *
	 * @ingroup types
	 */
	private StringBuilder workOnFunctionDeclaration(String tokbeg) {
		if (result.length() != 0) {
			throw new RuntimeException("Doesn't allow any instructions in front of '" + token + "'!");
		}

		if (getfrombinding == null) {
			throw new RuntimeException("There seems to be code in front of the " + token + " declaration, "
			                           + "which doesn't belong there");
		}

		if (!getfromhistory.isEmpty()
		        && !(getfrombinding instanceof FederClass || getfrombinding instanceof FederArguments
		             || getfrombinding instanceof FederArray)) {
			throw new RuntimeException("Only classes or interfaces are allowed as return types of functions!");
		}

		String name = stringsOfTokens.get(indexToken);

		FederBinding fc = null;
		if (!getfromhistory.isEmpty())
			fc = getfrombinding;

		FederBinding binding = getFromBinding(body, name, false);
		if (binding != null && !(binding instanceof FederFunction)) {
			throw new RuntimeException("The binding already exists and is not an function!");
		}

		indexToken++;
		if (indexToken == tokens.size()) {
			// No arguments
			FederArguments func0 = getFunctionFromBinding(body, name, new LinkedList<>(), false);
			if (func0 != null && func0 instanceof FederFunction) {
				if (func0.getReturnType() != fc) {
					throw new RuntimeException(
					    "Arguments and name of the function already exists, but the type is" + "not the same!");
				}

				body = (FederFunction) func0;
				return new StringBuilder();
			} else if (func0 != null) {
				throw new RuntimeException(
				    "Can't define a interface, because" + "a function with the same name and arguemnts exists!");
			}

			// Is it an interface ?
			if (tokbeg.equals("interface")) {
				FederInterface interf = new FederInterface(compiler, name, body);
				interf.setReturnType(fc);
				body.addBinding(interf);
				return new StringBuilder();
			}

			FederFunction func = new FederFunction(compiler, name, body);
			func.setReturnType(fc);

			StringBuilder sb = new StringBuilder();
			if (body instanceof FederClass) {
				sb.append(body.generateCName() + "* federobj_this");
			}

			body.addBinding(func);
			body = func;

			result = new StringBuilder(func.generateCName() + "(" + sb.toString() + ") {");

			if (func.getReturnType() == null) {
				result.insert(0, "void ");
			} else if (func.getReturnType() instanceof FederClass) {
				result.insert(0, func.getReturnType().generateCName() + " ");
			} else {
				result.insert(0, func.getReturnType().generateCName() + " ");
			}

			FederRule ruleCreateRoot = compiler.getApplyableRuleForStruct("create_root");
			if (ruleCreateRoot == null) {
				throw new RuntimeException("The struct rule 'create_root' has "
					+ " to be available, if a tracing garbage collector should "
					+ "should be used");
			}

			result.append(ruleCreateRoot.applyRule(func, func.getIdentifier()) + "\n");

			if (compiler.isDebug()) {
				result.append("\n" + body.inFrontOfSyntax());
				result.append("printf (\"\\nFile=" + compiler.getName()
				              + ", Line=" + (line) + ". Called function "
				              + func.getNamespacesToString() + "."
							  + func.getName() + "\\n\");\n");
			}

			result.append("\n");

			return result;
		}

		if (!tokens.get(indexToken).equals("(")) {
			throw new RuntimeException("Excpected '(' after basic function declaration!");
		}

		boolean expectComma = false;
		boolean expectObjectName = false;

		FederFunction func = new FederFunction(compiler, name, body);
		func.returnClass = fc;
		indexToken++;

		/*
		 * The following loop works through all the arguments, which can be passed to
		 * the function
		 */

		int depth = 0;

		for (; indexToken < tokens.size(); indexToken++) {
			token = tokens.get(indexToken);
			stoken = stringsOfTokens.get(indexToken);

			if (token.equals(",") && expectComma) {
				expectComma = false;
			} else if (token.equals(",")) {
				throw new RuntimeException("Comma wasn't expected at that position!");
			} else if (token.equals(")") && expectComma) {
				break;
			} else if (token.equals(")")) {
				throw new RuntimeException("Closing bracket ')' wasn't expected at that position!");
			} else if (token.equals("(")) {
				throw new RuntimeException("In a function declaration '(' are not allowed!");
			} else if (token.equals(".")) {
				if (expectObjectName) {
					throw new RuntimeException("'.' not allowed, object name expected.");
				}

				indexToken++;
				token = tokens.get(indexToken);
				stoken = stringsOfTokens.get(indexToken);

				FederBinding binding0 = getFromBinding(getfrombinding, stoken, false);
				if (binding0 == null) {
					throw new RuntimeException("Type not found: " + stoken);
				} else if (binding0 instanceof FederNamespace) {
					getfrombinding = binding0;
				} else if (binding0 instanceof FederClass) {
					getfrombinding = binding0;
					expectObjectName = true;
				} else if (binding0 instanceof FederInterface && !((FederInterface) binding0).canBeCalled()) {
					getfrombinding = binding0;
					expectObjectName = true;
				} else {
					throw new RuntimeException("Type isn't allowed in a function's argument list." + "\n"
					                           + getfrombinding.getClass().getName());
				}
			} else if (token.equals("name")) {
				if (expectObjectName) {
					if (!(getfrombinding instanceof FederClass) && !(getfrombinding instanceof FederInterface)) {
						throw new RuntimeException("Object can only declared with a class/interface/datatype/array!");
					}

					for (FederObject obj0 : func.arguments) {
						if (obj0.getName().equals(stoken)) {
							throw new RuntimeException("The function already contains an argument with the same name!");
						}
					}

					FederObject obj = new FederObject(stoken, null);
					obj.isForced = true;
					if (depth > 0) {
						if (depth > 1) {
							throw new RuntimeException("Currently only arrays with one depth are allowed!");
						} else if (getfrombinding instanceof FederClass && !((FederClass) getfrombinding).isType()) {
							throw new RuntimeException("Currently arrays can only be created with data types/interfaces!");
						}

						obj.setTypeManual(new FederArray(getfrombinding));
						depth = 0;
					} else {
						obj.setTypeManual(getfrombinding);
					}

					func.arguments.add(obj);
					func.addBinding(obj); // Add it to the binding structure of the function
					if (obj.isInterface())
						func.addBinding(((FederInterface) obj.getResultType()).interfaceFrom(compiler, obj.getName(), func));
					expectObjectName = false;
					expectComma = true;
				} else if (!expectComma) {
					FederBinding binding0 = getFromBinding(body, stoken, true);
					if (binding0 == null) {
						throw new RuntimeException("Type not found: " + stoken);
					} else if (binding0 instanceof FederNamespace) {
						getfrombinding = binding0;
					} else if (binding0 instanceof FederClass) {
						getfrombinding = binding0;
						expectObjectName = true;
					} else if (binding0 instanceof FederInterface && !((FederInterface) binding0).canBeCalled()) {
						getfrombinding = binding0;
						expectObjectName = true;
					} else {
						throw new RuntimeException("Type isn't allowed in a function's argument list.");
					}
				} else {
					throw new RuntimeException("Expected ',', but a name came!");
				}
			} else if(token.equals("[]")) {
				if (expectObjectName) {
					depth++;
				} else {
					throw new RuntimeException("Unknown in this context: " + stringsOfTokens.get(indexToken));
				}
			} else {
				throw new RuntimeException("Unknown in this context: " + stringsOfTokens.get(indexToken));
			}
		}

		for (FederObject obj : func.arguments) {
			if (obj == null)
				throw new RuntimeException("Something went wrong! This should not happen!");
		}

		// For loop endend

		if (indexToken == tokens.size()) {
			throw new RuntimeException("Function/Interface declaration wasn't terminated correctly. ')' required!");
		}

		if (indexToken + 1 != tokens.size()) {
			throw new RuntimeException("')' has to be the last operator in line, when defining a function/interface");
		}

		FederArguments func0 = getFunctionFromBinding(body, func.getName(),
		                       FederFunction.objectListToClassList(func.arguments), false);
		if (func0 != null && func0 instanceof FederFunction) {
			System.out.println("# " + func.getName() + " | " + func.argumentsToString());
			if (func0.getReturnType() != func.returnClass) {
				throw new RuntimeException("Arguments and name of functions the same, but return type different!");
			}

			body = (FederFunction) func0;
			return new StringBuilder();
		} else if (func0 != null) {
			throw new RuntimeException("Already defined, can't define interface!");
		}

		if (tokbeg.equals("interface")) {

			/*
			 * This method should work on an interface declaration, so we just created a new
			 * interface add the arguments to it, set the return type and add it to the
			 * current body. A string with no contents is being returned, because the
			 * interface has to be declared at the beginning of the header file
			 */

			FederInterface interf = new FederInterface(compiler, func.getName(), body);
			interf.getArguments().addAll(func.getArguments());
			interf.setReturnType(func.getReturnType());
			body.addBinding(interf);
			return new StringBuilder();
		}

		body.addBinding(func);
		body = func;

		result = new StringBuilder(func.generateCName() + " ("
		                           + FederFunction.generateArgumentsListString(func.getParent(), func.getArguments()) + ") {\n");

		if (func.isPrivate()) {
			result.insert(0, "static ");
		}

		if (func.getReturnType() == null) {
			result.insert(0, "void ");
		} else if (func.getReturnType() instanceof FederClass) {
			result.insert(0, func.getReturnType().generateCName() + " ");
		} else {
			result.insert(0, func.getReturnType().generateCName() + " ");
		}

		/*for (FederObject arg : func.arguments) {
			if (arg.isInterface())
				continue;

			if (!arg.equals(func.arguments.get(0))) {
				result.append("\n");
			}

			if (!arg.isGarbagable())
				continue;

			FederRule ruleIncrease = compiler.getApplyableRuleForStruct("increase");
			if (ruleIncrease == null) {
				throw new RuntimeException("Feder struct rule 'increase' doesn't exist!");
			}

			result.append(body.inFrontOfSyntax()).append(ruleIncrease.applyRule(body, arg.generateCName())).append(";");
		}*/

		if (func.getParent() instanceof FederClass && !((FederClass) body.getParent()).isType()) {
			/*FederRule ruleIncrease = compiler.getApplyableRuleForStruct("increase");
			if (ruleIncrease == null) {
				throw new RuntimeException("Feder struct rule 'increase' doesn't exist!");
			}*/

			FederRule ruleThisNull = compiler.getApplyableRuleForStruct("this_is_null");
			if (ruleThisNull != null) {
				result.append("\n" + body.inFrontOfSyntax())
				.append(ruleThisNull.applyRule(body, "(*federobj_this)", compiler.getName(), String.valueOf(line)));
			}

			//result.append("\n" + body.inFrontOfSyntax() + "fdIncreaseUsage ((fdobject*) (*federobj_this));");
			//result.append("\n" + body.inFrontOfSyntax()).append(ruleIncrease.applyRule(body, "(*federobj_this)") + ";");
		}

		FederRule ruleCreateRoot = compiler.getApplyableRuleForStruct("create_root");
		if (ruleCreateRoot == null) {
			throw new RuntimeException("The struct rule 'create_root' has "
				+ " to be available, if a tracing garbage collector should "
				+ "should be used");
		}

		result.append(ruleCreateRoot.applyRule(func, func.getIdentifier()) + "\n");

		if (compiler.isDebug()) {
			result.append("\n" + body.inFrontOfSyntax());
			result.append("printf (\"\\nFile=" + compiler.getName() + ", Line=" + (line) + ". Called function "
			              + func.getNamespacesToString() + "." + func.getName() + "\\n\");\n");
		}

		return result;
	}

	/**
	 * Works on an assignment operator ('='):
	 *     ( [type] ) [bound_object] '=' [object]
	 * @param obj
	 * @param isNew
	 */
	private void workOnAssignment(FederObject obj, boolean isNew) {
		{
			String resultold0 = result.toString();

			// Its an assignment, yey
			result.append(" = ");

			SyntaxTreeElement ste = newBranchAt(indexToken);
			int tokenscount = ste.tokens.size();
			StringBuilder compiled = ste.compile();

			/*
			 * if (ste.tokens.size() == 1 && ste.tokens.get(0).equals ("null")) {
			 * result.append("NULL"); indexToken += 1 + tokenscount; return; }
			 */

			if (obj.isInterface()) {
				/*
				 * The object is an interface, do interface specific assignment
				 */

				if (ste.returnedClasses.size() != 1
				        || (ste.returnedClasses.size() == 1 && !(ste.returnedClasses.get(0) == null
				                || ste.returnedClasses.get(0) instanceof FederArguments))) {

					/*
					 * The type after the '=' is invalid!
					 */

					throw new RuntimeException("There should be returned something like a function!");
				}

				if (ste.returnedClasses.get(0) != null && !((FederInterface) obj.getResultType()).isEqual(null, FederFunction
				        .objectListToClassList(((FederArguments) ste.returnedClasses.get(0)).getArguments()))) {
					throw new RuntimeException("The arguments of the interface and the function must be equal!");
				}

				result.append(compiled.toString());

				indexToken += 1 + tokenscount;
				return;
			}

			// Catch errors
			if (ste.returnedClasses.size() == 0) {
				throw new RuntimeException("Waited for an object after '=', but none came!");
			} else if (ste.returnedClasses.size() > 1) {
				throw new RuntimeException("Waited only for one object after '=', but too many came!");
			}

			if (ste.returnedClasses.get(0) != null && !(ste.returnedClasses.get(0) instanceof FederClass
			        || ste.returnedClasses.get(0) instanceof FederArray)) {
				throw new RuntimeException("Did not return a regular object (array/class/data type)");
			}

			// Check class matching or assign it if class is not enforced by object
			FederBinding binding = ste.returnedClasses.get(0);

			if (binding instanceof FederClass) {
				FederClass fc = (FederClass) binding;

				if (fc != null && obj.isForced && fc != obj.getResultType()) {
					throw new RuntimeException(
					    "Couldn't change class of object, because the class of the object is forced!");
				} else if (fc != null && !obj.isForced) {
					if (isNew && obj.getResultType() == null && fc.isType()) {
						obj.isForced = true;
						result = new StringBuilder(fc.generateCName() + " " + obj.generateCName() + " = ");
					} else if (fc.isType()) {
						throw new RuntimeException(
						    "Can't assign object (class not a type declaration) to a object with class with one.");
					}

					obj.setTypeManual(fc);
				}
			}

			if (isNew && obj.getResultType() == null && binding instanceof FederArray) {
				result = new StringBuilder(binding.generateCName() + " " + obj.generateCName() + " = ");
				obj.isForced = true;
				obj.setTypeManual(binding);
			}

			if (isNew && obj.isGarbagable()) {
				if (obj.isForced && obj.getResultType() == null) {
					throw new RuntimeException("This error should not happen!");
				}

				if (obj.isForced) {
					result.append("(").append(obj.getResultType().generateCName()).append(") ");
				}

				FederRule ruleAssign = compiler.getApplyableRuleForStruct("assign_obj");
				if (ruleAssign == null) {
					throw new RuntimeException("The struct rule 'assign_obj' doesn't exist!");
				}

				result.append(ruleAssign.applyRule(body, compiled.toString(), body.getIdentifier()));
			} else if (obj.isGarbagable()) {
				FederRule ruleAssignOld = compiler.getApplyableRuleForStruct("assign_obj_old");
				if (ruleAssignOld == null) {
					throw new RuntimeException("The struct rule 'assign_obj_old' doesn't exist");
				}

				result = new StringBuilder(ruleAssignOld.applyRule(body,
					resultold0.toString(), compiled.toString(), body.getIdentifier()));
			} else if (obj.isDataType()) {
				if (isNew)
					result = new StringBuilder(
					    (obj.isGlobal ? "" : (obj.getResultType().generateCName() + " ")) + obj.generateCName() + " = ");
				result.append(compiled);
			}

			indexToken += 1 + tokenscount;
			returnedClasses.clear();

			return;
		}
	}

	/**
	 * Jumpback operators: ';', 'else'
	 */
	private void workOnJumpback() {
		// Go to the tree element, which is in front of this one
		if (body.getParent() == null) {
			indexToken--;
			throw new RuntimeException("Can't go back to the tree element,"
			                           + " which should be in front of this one, because this is the first tree element.");
		}

		// generate the end
		// generateEnding();
		// On second thought not, to allow forward declaration

		body.resetAssume(); // Resets all assumed types
		body = body.getParent();
		getfrombinding = body;
		getfromhistory.clear();
	}

	/**
	 * This method tries to interpret a random name as something useful.
	 * The result is stored in result, getfrombinding/getfromhistory or returnedClasses.
	 *
	 * This method handles function calls, object calls, object declaration, object initialization
	 * or anything else related to bindings (objects, classes, namespaces, functions, interface, ...).
	 */
	private void workOnName() {
		if (!wasdotinfront) {
			getfrombinding = body;
		}

		String nextToken = (indexToken < tokens.size() ? tokens.get(indexToken) : "newline");
		String nextsToken = (indexToken < tokens.size() ? stringsOfTokens.get(indexToken) : "");

		if (nextsToken.equals("!") && indexToken+1 < tokens.size() && tokens.get(indexToken+1).equals("(")) {
			// Must be a buildin function call

			/*if (!isMain) {
				throw new RuntimeException("Buildin function must be called in main context!");
			}*/

			if (isGlobal) {
				throw new RuntimeException("Invalid use of the 'global' keyword");
			}

			SyntaxTreeElement ste = newBranchAt(indexToken+1);
			int tokenslen = ste.tokens.size();
			StringBuilder compiled = ste.compile();

			FederRule ruleBuildin = compiler.getApplyableRuleForBuildin("func_" + stoken);
			if (ruleBuildin != null) {
				getfrombinding = ruleBuildin.getSpecifiedResultValue();
				getfromhistory.add(ruleBuildin.getSpecifiedResultValue());
				returnedClasses.clear();
				returnedClasses.add(ruleBuildin.getSpecifiedResultValue());

				result = new StringBuilder(ruleBuildin.applyRule(body, compiled.toString()));
			} else {
				getfrombinding = null;
				result.append(stoken + "(" + compiled.toString() + ")");
			}

			indexToken += 3 + tokenslen;
			return;
		}

		FederBinding funcassign = getFromBinding(getfrombinding, stoken, !wasdotinfront);

		if (funcassign != null && funcassign instanceof FederInterface
		        && !((FederInterface) funcassign).canBeCalled()) {
			getfromhistory.add(funcassign);
			getfrombinding = funcassign;
			return;
		}

		if (nextToken.equals("(")) {
			// Must be a function call

			if (isGlobal) {
				throw new RuntimeException("Invalid used of the 'global' keyword");
			}

			SyntaxTreeElement ste = newBranchAt(indexToken);
			int tokensize = ste.tokens.size(); // Save them, could change size after compiling
			StringBuilder compiled = ste.compile();

			FederArguments func = getFunctionFromBinding(getfrombinding, stoken, ste.returnedClasses, !wasdotinfront);
			if (func == null) {
				StringBuilder sb = new StringBuilder();
				boolean isFirst = true;
				for (FederBinding fc : ste.returnedClasses) {
					if (!isFirst)
						sb.append(", ");
					else
						isFirst = false;

					if (fc == null)
						sb.append("NULL");
					else
						sb.append(fc.getName());
				}

				throw new RuntimeException("The function \"" + stoken + "(" + sb.toString() + ")\" doesn't exist!\n"
				                           + "CurrentBody=" + getfrombinding.getClass().getName() + " | " + getfrombinding.getName());
			}

			if (func instanceof FederFunction && ((FederFunction) func).getParent() instanceof FederClass
			        && result.length() == 0) {

				result = new StringBuilder(func.generateCName() + " (("
				                           + ((FederFunction) func).getParent ().generateCName() + "*) federobj_this");
				if (func.getArguments().size() > 0) {
					result.append(", ");
					result.append(compiled.toString());
				}

				result.append(")");
			} else if (func instanceof FederFunction && ((FederFunction) func).getParent() instanceof FederClass
			           && (getfrombinding instanceof FederObject || getfrombinding instanceof FederClass
			               || getfrombinding instanceof FederFunction)) {

				// String sresult = result.toString();

				if (getfrombinding instanceof FederObject && ((FederObject) getfrombinding).isForced)
					result = new StringBuilder(func.generateCName() + "(("
					                           + ((FederFunction) func).getParent().generateCName() + "*)&" + result.toString() + "");
				else if (getfrombinding instanceof FederObject && !((FederObject) getfrombinding).isGlobal) {
					String sresult = result.toString();
					int braces = 0;
					boolean starcame = false;
					int insertat0 = -1;
					int insertat1 = -1;
					// System.err.println (sresult);
					for (int i = 0; i < sresult.length(); i++) {
						char c = sresult.charAt(i);
						if (c == '(') {
							braces++;
						} else if (c == ')') {
							braces--;
						}

						if (starcame && (braces == 1 || braces == 2) && Character.isAlphabetic(c)) {
							insertat1 = i;
							break;
						}

						if (c == '*' && braces == 2 && !starcame) {
							starcame = true;
							insertat0 = i;
						}
					}

					if (insertat0 == -1 || insertat1 == -1) {
						throw new RuntimeException("This error should not occur: " + insertat0 + ", " + insertat1);
					}

					result = new StringBuilder(func.generateCName() + "((" + ((FederFunction) func).getParent().generateCName()
					                           + "*) " + sresult.substring(0, insertat0) + "*"
					                           + sresult.substring(insertat0, insertat1) + "&" + sresult.substring(insertat1));
					// System.err.println (result);
				} else
					result = new StringBuilder("ptr_" + func.generateCName() + "(" + result.toString());

				if (func.getArguments().size() > 0) {
					result.append(", ");
					result.append(compiled.toString());
				}

				result.append(")");

			} else if (func instanceof FederFunction && ((FederFunction) func).getParent() instanceof FederClass
			           && body.getClassInBody() != null) {
				// Needs a class
				// The last getfrombinding should be a class

				result = new StringBuilder(func.generateCName() + "((" +
				                           ((FederFunction) func).getParent().generateCName() + "*) federobj_this");
				if (func.getArguments().size() > 0) {
					result.append(", ");
					result.append(compiled.toString());
				}

				result.append(")");
			} else if (func instanceof FederFunction && ((FederFunction) func).getParent() instanceof FederClass) {
				// Function is in need of a class, but can't get one
				throw new RuntimeException(
				    "The function \"" + func.getName() + "\" needs a class, but there wasn't found any object, "
				    + "which would satisfy that requirement!");
			} else {
				// Functions doesn't need a class
				if (!(func instanceof FederInterface))
					result = new StringBuilder();
				else if (result.length() > 0)
					result.append("->");

				result.append(func.generateCName() + "(" + compiled + ")");
			}

			// Finalize function call

			/*
			 * if (func.getReturnType() != null && func.getReturnType() instanceof
			 * FederClass) { //result.insert(0, "((" + func.getReturnType().generateCName()
			 * + "*) "); result.insert(0, "("); result.append(")"); }
			 */
			/*
			 * if (func.getName().equals("clear")) System.err.println
			 * (tokens.get(indexToken));
			 */

			indexToken += 1 + tokensize + 1;

			/*
			 * if (func.getName().equals("clear")) {
			 * System.err.println(tokens.get(indexToken)); }
			 */

			getfrombinding = func.getReturnType();
			returnedClasses.clear();
			returnedObject = null;

			if (func.getReturnType() != null) {
				returnedClasses.add(func.getReturnType());
			}

			/*
			 * if (func.getName().equals("clear")) System.err.println(result.toString())
			 */

			return;
		}

		getfrombinding = getFromBinding(getfrombinding, stoken, !wasdotinfront);
		getfromhistory.add(getfrombinding);

		if (getfrombinding != null && getfrombinding instanceof FederArguments
		        && ((FederArguments) getfrombinding).canBeCalled() && !nextToken.equals("=")) {
			if (isGlobal) {
				throw new RuntimeException("Invalid used of the 'global' keyword");
			}

			returnedClasses.clear();
			returnedClasses.add(getfrombinding);
			if (tokens.size() != indexToken) {
				throw new RuntimeException("Didn't expect anything afterwards!");
			}

			if (getfromhistory.size() >= 2) {
				FederBinding last = getfromhistory.get(getfromhistory.size() - 2);
				if (last instanceof FederClass) {
					result.setLength(0);
				}
			}

			result.append(((FederArguments) getfrombinding).generateCName());

			return;
		}

		if (nextToken.equals("=") || (nextToken.equals("newline") && !wasdotinfront && getfrombinding == null
		                              && getfromhistory.size() > 1 && (getfromhistory.get(getfromhistory.size() - 2) instanceof FederClass
		                                      || getfromhistory.get(getfromhistory.size() - 2) instanceof FederInterface))) {
			/*
			 * Declare/Assign object
			 */

			// int reslen = result.length();
			// String oldresultpointerto = result.toString ();

			FederBinding type_of_object = null;

			if (getfromhistory.size() > 1 && getfromhistory.get(getfromhistory.size() - 2) instanceof FederClass) {
				type_of_object = getfromhistory.get(getfromhistory.size() - 2);
			} else if (getfromhistory.size() > 1
			           && getfromhistory.get(getfromhistory.size() - 2) instanceof FederInterface) {
				type_of_object = getfromhistory.get(getfromhistory.size() - 2);
			} else if (getfromhistory.size() > 1
			           && getfromhistory.get(getfromhistory.size() - 2) instanceof FederArray) {
				type_of_object = getfromhistory.get(getfromhistory.size() - 2);
			} else if (getfromhistory.size() > 1 && !wasdotinfront) {
				throw new RuntimeException(
				    "Not defined in language grammer.\n" + "([class]|[interface]) [name] '=' [instructions]");
			}

			FederObject obj = null;
			boolean isNew = false;

			if (getfrombinding == null) {
				if (wasdotinfront || result.length() != 0 || !isMain) {
					throw new RuntimeException("You can't define an object in another body!");
				}

				// declare the object
				obj = new FederObject(stoken, body);
				obj.isGlobal = isGlobal;
				obj.parent = body;
				if (type_of_object != null) {
					obj.isForced = true;
					obj.setTypeManual(type_of_object);
				}

				body.addBinding(obj);
				isNew = true;

				if ((obj.isClassObject() || obj.isDataType()) && !isGlobal) {
					if (obj.isForced)
						result.append(obj.getResultType().generateCName()).append(" ");
					else
						result.append("fdobject * ");
				} else if (obj.isInterface() && !isGlobal) {
					result.append(obj.getResultType().generateCName() + " ");
					body.addBinding(((FederInterface) obj.getResultType()).interfaceFrom(compiler, obj.getName(), body));
				} else if (obj.isArray()) {
					result.append(obj.getResultType().generateCName());
				}
			} else {
				if (!(getfrombinding instanceof FederObject)) {
					throw new RuntimeException ("Cannot assign a '" + getfrombinding.getClass().getName() + "'");
				}

				obj = (FederObject) getfrombinding;

				if (isGlobal) {
					throw new RuntimeException("Invalid used of the 'global' keyword");
				}

				if (type_of_object != null) {
					throw new RuntimeException("You can't specify an object, that was already declared!");
				}

				if (result.length() > 0) {
					result.append("->");
				} else if (obj.parent != null && obj.parent instanceof FederClass) {
					result.append("(*federobj_this)->");
				}
			}

			result.append(obj.generateCName());

			// Check if assignment operation or not
			if (nextToken.equals("=")) {
				workOnAssignment(obj, isNew);
				return;
			} else if (nextToken.equalsIgnoreCase("newline")) {
				// No assign operation
				indexToken += 1;

				/*
				 * Append " = null", if the object was not declared in a class. This feature
				 * prevents objects from not being initialized (which is bad, because Feders
				 * gabarge collector has to check an objects state all the time)
				 */

				if (!(body instanceof FederClass) && !obj.isDataType()) {
					result.append(" = NULL");
					// And that should do the trick
				}

				return;
			}

			if (getfrombinding == null) {
				throw new RuntimeException("Undefined name: " + stoken
				                           + (getfromhistory.size() > 1
				                              ? ("\nCurrentBody: " + getfromhistory.get(getfromhistory.size() - 2).getName() + " | "
				                                 + getfromhistory.get(getfromhistory.size() - 2).getClass().getName())
				                              : ""));
			}

			return;
		}

		// Its a normal object
		if (getfrombinding != null && getfrombinding instanceof FederObject) {

			if (isGlobal) {
				throw new RuntimeException("Invalid used of the 'global' keyword");
			}

			FederObject obj = (FederObject) getfrombinding;

			if (body.isInFunction() && obj.parent != null && obj.parent.isInNamespace() && !obj.isGlobal) {
				throw new RuntimeException("The called object can only be called in other namespaces!");
			}

			int reslen = result.length();

			if (!obj.isForced) {
				String typecast = obj.getResultType() == null ? "" : ("(" + obj.getResultType().generateCName() + ") ");
				result = new StringBuilder("(" + typecast + result.toString());
			} else {
				result = new StringBuilder("(" + result.toString());
			}

			if (reslen > 0)
				result.append("->");
			else if (obj.parent != null && obj.parent instanceof FederClass)
				result.append("(*federobj_this)->");

			result.append(obj.generateCName() + ")");

			returnedClasses.clear();
			returnedClasses.add(obj.getResultType());
			returnedObject = obj;
			return;
		}

		/*
		 * Create an array with size [x]
		 */
		if (getfrombinding != null && nextToken.equals("[")
		        && (getfrombinding instanceof FederClass || getfrombinding instanceof FederInterface
		            && returnedClasses.size() == 0)) {

			/*if (getfrombinding instanceof FederClass) {
				FederClass fc = (FederClass) getfrombinding;
				if (!fc.isType()) {
					throw new RuntimeException("Current arrays can only be created with types or interfaces!");
				}
			}*/

			if (getfrombinding instanceof FederInterface) {
				FederInterface inter = (FederInterface) getfrombinding;
				if (inter.canBeCalled()) {
					throw new RuntimeException("Array can only be created from the interface not from the object!");
				}
			}

			SyntaxTreeElement ste = newBranchAt (indexToken);
			int tokenslen = ste.tokens.size();
			StringBuilder compiled = ste.compile();

			if (FederBinding.isDataType(getfrombinding)) {
				result = new StringBuilder("fdCreateTypeArray (sizeof (" + getfrombinding.generateCName() + ")");
				result.append(", " + compiled.toString() + ")");
			} else {
				result = new StringBuilder("fdCreateClassArray (");
				result.append(compiled.toString() + ")");
			}

			indexToken += tokenslen + 2;

			FederArray ar = new FederArray(getfrombinding);
			returnedClasses.clear();
			returnedClasses.add(ar);
			getfrombinding = ar;
			getfromhistory.add(ar);

			return;
		}

		if (getfrombinding != null && (nextToken.equals(".") || nextToken.equals("newline"))
		        && getfrombinding instanceof FederClass) {

			if (isGlobal) {
				throw new RuntimeException("Invalid used of the 'global' keyword");
			}

			if (returnedClasses.size() > 0) {
				throw new RuntimeException(
				    "There should be anything returned in front of calling class initializing method!");
			}

			if (!wasdotinfront && getfromhistory.size() > 1) {
				throw new RuntimeException("There should be anything expect declared namespaces "
				                           + "in front of calling class initializing method!");
			}

			if (result.length() > 0) {
				throw new RuntimeException(
				    "There shouldn't be any instructions in front of calling class initializing method!");
			}

			FederClass fc = (FederClass) getfrombinding;
			if (fc.isType()) {
				throw new RuntimeException("Types can't be instantiated!");
			}

			returnedClasses.clear();
			returnedClasses.add(getfrombinding);

			result = new StringBuilder("fdCreateClass_" + ((FederClass) getfrombinding).generateCNameOnly() + " ()");
			return;
		}

		if (getfrombinding == null) {
			indexToken--;
			throw new RuntimeException("Undefined name: " + stoken);
		}

		return;
	}

	/**
	 * @return Returns the main syntax tree element (the first one, on which all
	 *         other branches depend)
	 */
	public SyntaxTreeElement getMain() {
		if (parent == null) {
			return this;
		}

		SyntaxTreeElement steparent = parent;
		while (steparent.parent != null)
			steparent = steparent.parent;

		return steparent;
	}

	/**
	 * @return Returns true, if an assignment operator ('=') is in
	 * the current scope (not between () or [])
	 */
	public boolean isAssignmentOperatorInCurrentScope () {
		int scope = 0;
		for (int i = 0; i < tokens.size(); i++) {
			String token = tokens.get(i);
			if (token.equals("(") || token.equals("[")) {
				scope++;
			} else if (token.equals(")") || token.equals("]")) {
				scope--;
			}

			if (scope == 0 && token.equals ("=")) {
				return true;
			}
		}

		return false;
	}

	/**
	 * This method walks through the given elements (
	 * @link SyntaxTreeElement.tokens tokens @endlink ,
	 * @link SyntaxTreeElement.stringsOfTokens stringsOfTokens @endlink ) and forms a
	 * syntax tree while doing that (Elements that can be compiled with another instance
	 * of SyntaxTreeElement are a new branch of the tree). The given elements should have
	 * been analyzed by @link Syntax.validateSyntax validateSyntax @endlink , otherwise
	 * something like a NullPointerException can occur.
	 *
	 * If @link SyntaxTreeElement.isMain isMain @endlink is true, the source code can declare
	 * functions, classes, namespaces, types, interfaces or conditional statements. If true, it
	 * is also possible to create new bound objects or assign bound object to another object.
	 *
	 * If @link SyntaxTreeElement.isGlobal isGlobal @endlink is true, the source code can
	 * declare global objects. Anything else must not work (is prohibited).
	 *
	 * If @link SyntaxTreeElement.body body @endlink changes to the main parent and the last body
	 * was an automate (conditional statement), the operation executing this method, should generate
	 * and end to that body. If the body is not the parent body, an end should be generated, because
	 * a new else (if) statement has started.
	 *
	 * If something is 'returned' (not the 'return' keyword) by the analysed code it will be
	 * stored in getfrombinding and getfromhistory. If the 'returned' thing is an object, it
	 * will also be added to @link SyntaxTreeElement.returnedClasses returnedClasses @endlink .
	 *
	 * Specific bodies must not be declared in certain bodies. E.g.: You can't declare a method
	 * in another method or a class in class. This is managed by @link FederBody.blacklist_classes
	 * blacklist_classes @endlink
	 *
	 * @return Returns code for the compile file "*.c", header file "*.h" or main method.
	 * This depends on the body (SyntaxTreeElement.body) the current branch uses, so
	 * another instance should decide, where to put this source code (you probably should append
	 * it to @link feder.types.FederBody.compile_file_text FederBody.compile_file_text @endlink
	 * and later decide where to put it).
	 *
	 * Normally throws a RuntimeException, if the input was wrong. If other errors
	 * are thrown by this method, then there is a problem in the source code.
	 */
	public StringBuilder compile() {
		if (getfrombinding == null) {
			getfrombinding = body;
			wasdotinfront = false;
		}

		indexToken = 0;

		if (parent == null) {
			currentInUse = this;
		} else {
			SyntaxTreeElement steparent = parent;
			while (steparent.parent != null)
				steparent = steparent.parent;

			steparent.currentInUse = this;
		}

		if (tokens.size() == 0) {
			return new StringBuilder();
		}

		if (tokens.get(0).equals("include")) {
			// include = primitive import
			String filetoinclude = stringsOfTokens.get(1);
			return new StringBuilder(SyntaxTreeElementUtils.opInclude(compiler, filetoinclude));
		}

		/*
		 * For loop operator
		 */
		if (tokens.size() >= 1 && (tokens.get(0).equals("for")) && isMain) {
			FederRule ruleCreateRoot = compiler.getApplyableRuleForStruct("create_root");
			if (ruleCreateRoot == null) {
				throw new RuntimeException("Struct rule 'create_root' doesn't exist!");
			}

			if (tokens.size() == 1) {
				body = new FederAutomat(compiler, body, "for");
				return new StringBuilder("while (true) {\n"
					+ ruleCreateRoot.applyRule(body, body.getIdentifier()) + "\n");
			}

			FederBody oldbody = body;
			body = new FederAutomat(compiler, body, "for");

			List<SyntaxTreeElement> stes = new LinkedList<>();
			List<String> tokens0 = new LinkedList<>();
			List<String> stringsOfTokens0 = new LinkedList<>();
			int scope = 0;
			for (int i = 1; i <= tokens.size(); i++) {
				String token = (i == tokens.size() ? "" : tokens.get(i));
				if (token.equals("") || (token.equals(",") && scope == 0)) {
					// End of tokens or ',' in current scope
					stes.add(new SyntaxTreeElement(compiler, body, line, tokens0, stringsOfTokens0));
					tokens0 = new LinkedList<>();
					stringsOfTokens0 = new LinkedList<>();
					continue;
				} else if (token.equals("(")) {
					scope++;
				} else if (token.equals(")")) {
					scope--;
				}

				tokens0.add(tokens.get(i));
				stringsOfTokens0.add(stringsOfTokens.get(i));
			}

			if (stes.size() > 3) {
				throw new RuntimeException("Invalid for loop call: " + stes.size());
			}

			if (stes.size() == 0) {
				return new StringBuilder("while (true) {\n"
					+ ruleCreateRoot.applyRule(body, body.getIdentifier()) + "\n");
			}

			if (stes.size() == 3) {
				stes.get(0).isMain = true;
			}

			List<String> parts = new LinkedList<>();
			for (SyntaxTreeElement ste : stes) {
				StringBuilder compiled = ste.compile();
				String s = compiled.toString();
				if (s.endsWith(";\n"))
					s = s.substring(0, s.length()-2);
				else if (s.endsWith(";"))
					s = s.substring(0, s.length()-1);
				parts.add(s);
			}

			if (stes.size() == 3) {
				FederBinding binding = stes.get(1).returnedClasses.get(0);
				if (!(binding instanceof FederClass)) {
					throw new RuntimeException("The 2nd operation after 'for' has to return a object created by a class/data type!");
				}

				String s = SyntaxTreeElementUtils.handleNonBoolToBool(compiler, parts.get(1), (FederClass) binding, oldbody);
				String last = parts.get(2);
				if (stes.get(2).returnedClasses.size() == 1) {
					if (FederBinding.IsGarbagable(stes.get(2).returnedClasses.get(0))) {
						FederRule ruleRemoveFunc = compiler.getApplyableRuleForStruct("remove_func");
						if (ruleRemoveFunc == null) {
							throw new RuntimeException("struct rule 'remove_func' doesn't exist!");
						}

						last = ruleRemoveFunc.applyRule(body, last);
					}
				}

				return new StringBuilder ("for (" + parts.get(0) + "; " + s + "; " + last + ") {\n"
					+ ruleCreateRoot.applyRule(body, body.getIdentifier()) + "\n");
			} else if (stes.size() == 2) {
				FederBinding binding = stes.get(0).returnedClasses.get(0);
				if (!(binding instanceof FederClass)) {
					throw new RuntimeException("The 1st operation after 'for' has to return a object created by a class/data type!");
				}

				String s = SyntaxTreeElementUtils.handleNonBoolToBool(compiler, parts.get(0), (FederClass) binding, oldbody);
				String last = parts.get(1);
				if (stes.get(1).returnedClasses.size() == 1) {
					if (FederBinding.IsGarbagable(stes.get(1).returnedClasses.get(0))) {
						FederRule ruleRemoveFunc = compiler.getApplyableRuleForStruct("remove_func");
						if (ruleRemoveFunc == null) {
							throw new RuntimeException("struct rule 'remove_func' doesn't exist!");
						}

						//last = "fdRemoveObject_func ((fdobject*) " + last + ")";
						last = ruleRemoveFunc.applyRule(body, last);
					}
				}

				return new StringBuilder ("for (; " + s + "; " + last + ") {\n"
					+ ruleCreateRoot.applyRule(body, body.getIdentifier()) + "\n");
			} else if (stes.size() == 1) {
				FederBinding binding = stes.get(0).returnedClasses.get(0);
				if (!(binding instanceof FederClass)) {
					throw new RuntimeException("The operation after 'for' has to return a object created by a class/data type!");
				}

				String s = SyntaxTreeElementUtils.handleNonBoolToBool(compiler, parts.get(0), (FederClass) binding, oldbody);
				return new StringBuilder ("while (" + s + ") {\n"
					+ ruleCreateRoot.applyRule(body, body.getIdentifier()) + "\n");
			}
		}

		if (fixCommasInCurrentScope()) {
			if (result.length() > 0)
				return result;
		}

		if (tokens.get(0).equals("nativelang")) {
			// Just return the native code
			return new StringBuilder(stringsOfTokens.get(0));
		}

		if (tokens.get(0).equals("command")) {
			return SyntaxTreeElementUtils.command(compiler, this, body, stringsOfTokens.get(0));
		}



		if (tokens.get(0).equals("else")) {
			if (!(body instanceof FederAutomat) || !((FederAutomat) body).getType().endsWith("if")) {
				throw new RuntimeException("Invalid call of 'else'");
			}

			workOnJumpback();

			if (tokens.size() == 1) {
				FederRule ruleCreateRoot = compiler.getApplyableRuleForStruct("create_root");
				if (ruleCreateRoot == null) {
					throw new RuntimeException("Struct rule 'create_root' doesn't exist");
				}

				body = new FederAutomat(compiler, body, "else");
				return new StringBuilder("else {\n"
					+ ruleCreateRoot.applyRule(body, body.getIdentifier()) + "\n");
			}

			indexToken++;
			result.append("else ");

			// Otherwise continue to 'if'
		}

		/*
		 * IF
		 * or
		 * WHILE
		 */
		if (tokens.size() > indexToken
		        && (tokens.get(indexToken).equals("if") || tokens.get(indexToken).equals("while"))) {

			FederBody oldbody = body;
			body = new FederAutomat(compiler, body, result.toString() + tokens.get(indexToken));
			SyntaxTreeElement ste = newBranchAt(indexToken);
			ste.body = oldbody;
			StringBuilder compiled = ste.compile();

			if (ste.returnedClasses.size() != 1) {
				throw new RuntimeException("Should only return one object!");
			}


			FederRule ruleCreateRoot = compiler.getApplyableRuleForStruct("create_root");
			if (ruleCreateRoot == null) {
				throw new RuntimeException("Struct rule 'create_root' doesn't exist");
			}

			FederClass fc = (FederClass) ste.returnedClasses.get(0);
			String s = SyntaxTreeElementUtils.handleNonBoolToBool(compiler, compiled.toString(), fc, body);
			return new StringBuilder(result.toString() + tokens.get(indexToken) + " (" + s + ") {\n"
				+ ruleCreateRoot.applyRule(body, body.getIdentifier()) + "\n");
		}

		/*
		 * Bool_ean operators: ||, &&
		 */
		//if (fixBoolOperatorsInCurrentScope())
		//	return result;

		/*
		 * Operators used in loops (continue, break)
		 */
		if (tokens.size() == 1 && (tokens.get(0).equals("continue") || tokens.get(0).equals("break"))) {
			String type = tokens.get(0);
			if (!body.isInLoop()) {
				throw new RuntimeException("This body is not in an loop");
			}

			return new StringBuilder(type + ";");
		}

		/*
		 * Global declaration
		 */
		if (tokens.size() > 1 && tokens.get(0).equals("global")) {
			SyntaxTreeElement ste = newBranchAt(0);
			ste.isGlobal = true;
			ste.isMain = true;
			return ste.compile();
		}

		/*
		 * This loop goes through the tokens and potentially creates new branches
		 */
		while (indexToken < tokens.size()) {
			token = tokens.get(indexToken);
			stoken = stringsOfTokens.get(indexToken);

			//String nextToken = (indexToken+1 >= tokens.size() ? "" : tokens.get(indexToken+1));

			indexToken++;
			// String nextToken = (tokens.size() >= indexToken ? "newline" :
			// tokens.get(indexToken));

			// Work on class, namespace or function declration
			if (token.equals("class")) {
				return workOnClassDeclaration();
			} else if (token.equals("namespace")) {
				return workOnNamespaceDeclaration();
			} else if (token.equals("func") || token.equals("interface")) {
				return workOnFunctionDeclaration(token);
			} else if (token.equals("type")) {
				return workOnTypeDeclaration();
			}

			/*
			 * The following handles:
			 * array[x] (index at x in array)
			 */
			if (getfrombinding != null && token.equals("[")
			        && (getfrombinding instanceof FederArray ||
			            (getfrombinding instanceof FederObject && ((FederObject) getfrombinding).isArray()))) {

				returnedClasses.clear();
				SyntaxTreeElement ste = newBranchAt(indexToken-1);
				int lentokens = ste.tokens.size();
				StringBuilder compiled = ste.compile();

				if (ste.returnedClasses.size() != 1) {
					throw new RuntimeException("Only array[int] is acceptable: Invalid argument size.");
				}

				FederArray ar;
				if (getfrombinding instanceof FederArray) {
					ar = (FederArray) getfrombinding;
				} else {
					ar = (FederArray) ((FederObject) getfrombinding).getResultType();
				}

				indexToken += lentokens + 1;
				if (FederBinding.isDataType (ar.getType())) {
					/*result.insert(0, "((" + ar.getType().generateCName() + "*)");
					result.append("->data)[" + compiled.toString() + "]");*/

					FederRule ruleArrayDataAt = compiler.getApplyableRuleForBuildin("at_dataarray");
					if (ruleArrayDataAt == null) {
						throw new RuntimeException("The buildin rule 'at_dataarray' doesn't exist");
					}

					if (!FederBinding.areSameTypes(ste.returnedClasses.get(0), ruleArrayDataAt.getSpecifiedResultValue())) {
						throw new RuntimeException("Result type of 'at_dataarray' is not the same as in the '[]' brackets!");
					}

					result = new StringBuilder (ruleArrayDataAt.applyRule(body, result.toString(), compiled.toString(), ar.getType().generateCName()));
				} else {
					FederRule ruleClassArrayAt = compiler.getApplyableRuleForBuildin("at_classarray");
					if (ruleClassArrayAt == null) {
						throw new RuntimeException("The buildin rule 'at_classarray' doesn't exist!");
					}

					if (!FederBinding.areSameTypes(ste.returnedClasses.get(0), ruleClassArrayAt.getSpecifiedResultValue())) {
						throw new RuntimeException("Result type of 'at_dataarray' is not the same as in the '[]' brackets!");
					}

					result = new StringBuilder(ruleClassArrayAt.applyRule(body, result.toString(), compiled.toString(), ar.getType().generateCName()));
				}

				returnedClasses.add(ar.getType());
				getfromhistory.add(ar.getType());
				getfrombinding = ar.getType();

				if (isMain && indexToken < tokens.size() && tokens.get(indexToken).equals("=")) {
					FederObject tmpobj = new FederObject(result.toString(), body);
					tmpobj.isForced = true;
					tmpobj.raw_c_gen = true;
					tmpobj.setTypeManual(ar.getType());
					workOnAssignment(tmpobj, false);
				}

				continue;
			}

			/*
			 * The following handles:
			 * array type mentioning
			 */
			if (token.equals("[]")) {
				if (!(getfrombinding instanceof FederClass || getfrombinding instanceof FederInterface)) {
					throw new RuntimeException("Feder can only create arrays from interfaces or classes/data types");
				}

				if(getfrombinding instanceof FederInterface && ((FederInterface) getfrombinding).canBeCalled()) {
					throw new RuntimeException("The interface has to be a type, not an object!");
				}

				FederArray ar = new FederArray(getfrombinding);
				getfromhistory.add(ar);
				getfrombinding = ar;
				/*				returnedClasses.clear();
								returnedClasses.add(ar);*/

				continue;
			}

			/*
			 * Array length operator: len
			 */
			if (token.equals("len")) {
				if (getfromhistory.size() > 0 || result.length() > 0) {
					throw new RuntimeException("Nothing be in front of len (in the current scope)");
				}

				SyntaxTreeElement ste = newBranchAt(indexToken);
				int lentokens = ste.tokens.size();
				StringBuilder compiled = ste.compile();
				if (ste.returnedClasses.size() == 0) {
					throw new RuntimeException("Only len (array) is acceptable: No arguments");
				} else if (ste.returnedClasses.size() > 1) {
					throw new RuntimeException("Only len (array) is acceptable: Too many arguments");
				}

				FederBinding returned = ste.returnedClasses.get(0);
				if (returned == null) {
					throw new RuntimeException("Array in len (array) should not be 'null'");
				}

				if (!(returned instanceof FederArray)) {
					throw new RuntimeException("Only len (array) is acceptable: Given object is not an array!");
				}

				FederBinding binding = body.getBinding(body, "int32", true);
				if (binding == null) {
					throw new RuntimeException("The class/data type 'int' was not declared!");
				}

				if (!(binding instanceof FederClass)) {
					throw new RuntimeException("'int' was not declared as a class/data type!");
				}

				FederClass fc = (FederClass) binding;
				if (FederBinding.isDataType(((FederArray) returned).getType())) {
					result = new StringBuilder("fdGetTypeArrayLength ((fdtypearray*) " + compiled.toString() + ")");
				} else {
					result = new StringBuilder("fdGetClassArrayLength ((fdclassarray*) " + compiled.toString() + ")");
				}

				if (!fc.isType()) {
					result.insert(0, "fdCreateInt(");
					result.append(")");
				} else if (fc.isType()) {
					result.insert(0, "((" + fc.generateCName() + ") ");
					result.append(")");
				}

				returnedClasses.clear();
				returnedClasses.add(fc);
				getfromhistory.add(fc);
				getfrombinding = fc;

				indexToken += lentokens + 2;

				continue;
			}

			/*
			 * Purpose of the following:
			 * append objects to an array
			 */
			if (token.equals("append")) {
				if (getfromhistory.size() > 0 || result.length() > 0) {
					throw new RuntimeException("There should be nothing in front of 'append': " + result.toString());
				}

				SyntaxTreeElement ste = newBranchAt(indexToken);
				int lentokens = ste.tokens.size();
				/* StringBuilder compiled = */ ste.compile();

				if (ste.returnedClasses.size() <= 1) {
					throw new RuntimeException("Only 'append (array ar, type obj, ...)' is acceptable: Not enough arguments.");
				}

				if (!(ste.returnedClasses.get(0) instanceof FederArray)) {
					throw new RuntimeException("Only 'append (array ar, type obj, ...)' is acceptable: First argument not an array.");
				}

				FederArray ar = (FederArray) ste.returnedClasses.get(0);
				result = new StringBuilder(ste.results_list.get(0));

				for (int i = 1; i < ste.returnedClasses.size(); i++) {
					FederBinding binding = ste.returnedClasses.get(i);
					/*					if (!(binding instanceof FederClass) || !((FederClass) binding).isType()) {
											throw new RuntimeException("Only 'append (array ar, type obj, ..)' is acceptable: "
											                           + "Argument " + i + " is not a data type!");
										}*/

					FederClass fc = (FederClass) binding;
					if (!FederBinding.areSameTypes(fc, ar.getType())) {
						throw new RuntimeException("Argument " + i + " has not the same type as the array: " + fc.getName() + " != " + ar.getType().getName());
					}

					if (FederBinding.isDataType (ar.getType())) {
						result.insert(0, "fdAppendToTypeArray_" + fc.generateCNameOnly() + " (");
					} else {
						result.insert(0, "fdAppendToClassArray(");
					}

					result.append(", ");
					if (!FederBinding.isDataType (ar.getType())) {
						result.append ("(fdobject*) ");
					}

					result.append(ste.results_list.get(i));

					result.append(")");
				}

				indexToken += lentokens + 2;

				returnedClasses.clear();
				returnedClasses.add(ar);
				getfromhistory.add(ar);
				getfrombinding = ar;

				continue;
			}

			/*
			 * if (getfrombinding != null) System.out.println("# " +
			 * getfrombinding.getName() + " | " + getfrombinding.getClass().getName());
			 */

			if (token.equals("(")
			        // Special cases
			        && !(getfrombinding != null && getfrombinding instanceof FederInterface)
			        && !(getfrombinding != null && getfrombinding instanceof FederObject
			             && !(getfrombinding != null && ((FederObject) getfrombinding).isInterface()))) {

				SyntaxTreeElement ste = newBranchAt(indexToken - 1);
				int counttokens = ste.tokens.size();
				StringBuilder compiled = ste.compile();

				if (ste.returnedClasses.size() > 1) {
					throw new RuntimeException("Invalid use of ','!");
				}

				result.append("(").append(compiled).append(")");

				returnedClasses.clear();

				if (ste.returnedClasses.size() != 0) {
					returnedClasses.addAll(ste.returnedClasses);
					getfrombinding = ste.returnedClasses.get(0);
				}

				indexToken += counttokens + 1;
				continue;
			}

			if (token.equals("(") && ((getfrombinding != null && getfrombinding instanceof FederInterface)
			                          || (getfrombinding != null && getfrombinding instanceof FederObject
			                              && ((FederObject) getfrombinding).isInterface()))) {
				FederInterface fint = null;
				if (getfrombinding instanceof FederInterface)
					fint = (FederInterface) getfrombinding;
				else if (getfrombinding instanceof FederObject)
					fint = (FederInterface) ((FederObject) getfrombinding).getResultType();

				if (fint == null) {
					// should not happen
					throw new RuntimeException("Unexpected error!");
				}

				SyntaxTreeElement ste = newBranchAt(indexToken - 1);
				int countste = ste.tokens.size();
				StringBuilder compiled = ste.compile();

				if (!fint.isEqual(null, ste.returnedClasses)) {
					throw new RuntimeException(
					    "The arguments of the called function are not " + "the same as the given arguments!");
				}

				indexToken += countste + 1;

				/*if (fint.getReturnType() != null && fint.getReturnType() instanceof FederClass) {
				    result.insert(0, "(" + fint.getReturnType().generateCName() + "*)");
				}*/

				result.append(compiled.toString());

				returnedClasses.clear();
				if (fint.getReturnType() != null)
					returnedClasses.add(fint.getReturnType());

				continue;
			}

			if (token.equals("from")) {
				/*
				 * The 'from' operator is an operator to cast an object's class to another
				 * class. This is extremely helpful, if you want to use list or other things,
				 * which only allow one type.
				 */

				boolean casttoclass = getfrombinding instanceof FederClass || getfrombinding instanceof FederArray;
				boolean casttointerface = getfrombinding instanceof FederInterface
				                          && !((FederInterface) getfrombinding).canBeCalled();

				if (getfrombinding == null || !(casttoclass || casttointerface)) {
					throw new RuntimeException("In front of an from operator should be mentioned a class!");
				}

				SyntaxTreeElement ste = newBranchAt(indexToken - 1);
				int counttokens = ste.tokens.size();
				StringBuilder compiled = ste.compile();

				if (ste.returnedClasses.size() != 1) {
					throw new RuntimeException("There should only be returned one class or "
					                           + "interface/function after the 'from' operator!");
				}

				if (casttoclass && !(ste.returnedClasses.get(0) instanceof FederClass ||
				                     ste.returnedClasses.get(0) instanceof FederArray)) {
					throw new RuntimeException("Expected a class, but none came!");
				}

				if (casttointerface && !(ste.returnedClasses.get(0) instanceof FederArguments)) {
					throw new RuntimeException("Expect a function/callable interface, but none came/!");
				}

				indexToken += counttokens;
				result = new StringBuilder(
				    "(" + getfrombinding.generateCName() + (casttoclass ? "" : "") + ") " + compiled);
				// EDIT !
				returnedClasses.add(getfrombinding);
				continue;
			}

			// Dot = skip
			if (token.equals(".")) {
				returnedObject = null;
				continue;
			}

			// Was the last index a '.' ?
			if (indexToken - 2 >= 0 && tokens.get(indexToken - 2).equals(".")) {
				wasdotinfront = true;
			} else {
				wasdotinfront = false;
				getfrombinding = body;
			}

			if (token.equals(";")) {
				workOnJumpback();
				continue; // We could do 'break' too, but just in case a specification gets changed
			}

			// A name: class, function, object
			if (token.equals("name")) {
				workOnName();
				continue;
			}

			// String ?

			if (token.equals("string") && result.length() == 0) {
				FederRule rule = compiler.getApplyableRuleForBuildin ("string");
				if (rule == null) {
					throw new RuntimeException("No rule found for \"buildin\" type \"string\"");
				}

				result = new StringBuilder (rule.applyRule (body, stoken));

				returnedClasses.clear();
				returnedClasses.add(rule.getSpecifiedResultValue());
				getfrombinding = rule.getSpecifiedResultValue();

				continue;
			} else if (token.equals("string")) {
				throw new RuntimeException("A string must be in a seperate branch!");
			}

			if (token.equals("char") && result.length() == 0) {
				FederRule rule = compiler.getApplyableRuleForBuildin("char");
				if (rule == null) {
					throw new RuntimeException("No rule found for \"buildin\" type \"char\"");
				}

				result = new StringBuilder(rule.applyRule(body, stoken));
				
				returnedClasses.clear();
				returnedClasses.add(rule.getSpecifiedResultValue());
				getfrombinding = rule.getSpecifiedResultValue();

				continue;
			} else if (token.equals("char")) {
				throw new RuntimeException("A char must be in a seperated branch!");
			}

			if (token.equals("return")) {
				// Start new branch at 'return' (newBranch it made for that)
				SyntaxTreeElement ste = newBranchAt(indexToken - 1);
				FederFunction upperFunction = body.getUpperFunction();
				FederNamespace upperNamespace = body.getUpperNamespace();

				body.returnCame = true;

				if (ste.tokens.size() == 0 || (ste.tokens.size() == 1
				                               && (ste.tokens.get(0).equals("false") || ste.tokens.get(0).equals("true"))
				                               && upperNamespace != null)
				        && ((upperFunction != null && upperFunction.returnClass == null) || upperNamespace != null)) {

					generateEnding("File=" + compiler.getName() + ", Line=" + (line), true, null);

					if (upperNamespace != null && (upperNamespace.getName().startsWith("h_intern")
					                               || upperNamespace.getName().startsWith("c_intern"))) {
						throw new RuntimeException(
						    "Using the return operator, requires the namespace to be generated in "
						    + "the main method, but it seems like, that the namespace is an internal one.");
					} else if (upperNamespace != null) {
						if ((ste.tokens.size() == 1 && ste.tokens.get(0).equals("true")) || ste.tokens.size() == 0)
							return new StringBuilder("return EXIT_SUCCESS;");

						return new StringBuilder("return EXIT_FAILURE;");
					}

					return new StringBuilder("return;");
				} else if (ste.tokens.size() != 0
				           && ((upperFunction != null && upperFunction.returnClass == null) || upperNamespace != null)) {
					throw new RuntimeException("Expected just 'return', but something came afterwards!");
				}

				StringBuilder compiled = ste.compile();
				indexToken += ste.tokens.size();

				if (indexToken != tokens.size()) {
					throw new RuntimeException("The instructions after the 'return' operator are invalid!");
				}

				if (ste.returnedClasses.size() != 1) {
					throw new RuntimeException("There should have been returned only one thing!");
				}

				if (body.getUpperFunction() == null) {
					throw new RuntimeException("Something went wrong! There should be a parent function!");
				}

				FederBinding returned = ste.returnedClasses.get(0);
				// System.out.println("# " + returned.getName() + " | " +
				// returned.getClass().getName());
				/*
				if (returned != null && body.getUpperFunction().getReturnType() != null
				        && !body.getUpperFunction().getReturnType().equals(returned)
				        && !(body.getUpperFunction().getReturnType() instanceof FederInterface
				             && returned instanceof FederArguments && ((FederArguments) returned).canBeCalled()
				             && ((FederInterface) body.getUpperFunction().getReturnType())
				             .similiarToArguments((FederArguments) returned))) {
					throw new RuntimeException("Returned type and expected type are not the same!");
				}
				*/

				if (returned != null && !FederBinding.areSameTypes(body.getUpperFunction().getReturnType(), returned)) {
					throw new RuntimeException("Excpetect type and returned type are not the same: " +
					                           body.getUpperFunction().getReturnType().getName() + ", " + returned.getName());
				}

				if (returned != null && returned instanceof FederArguments) {
					generateEnding("File=" + compiler.getName() + ", Line=" + (line), true, null);
					return new StringBuilder("return " + "(" + body.getUpperFunction().getReturnType().generateCName()
					                         + ") " + compiled.toString() + ";\n");
				}

				if (body.getUpperFunction().getReturnType() instanceof FederClass
				        && ((FederClass) body.getUpperFunction().getReturnType()).isType()) {
					if (returned == null) {
						System.err.println("File=" + compiler.getName() + ", Line=" + (line));
						System.err.println("Warning: Returning null may be inappropriate, when the result has to be a type!");
					}

					body.compile_file_text.append(((FederClass) body.getUpperFunction().getReturnType()).generateCName());
					body.compile_file_text.append(" return_result = " + compiled.toString() + ";\n");
					generateEnding("File=" + compiler.getName() + ", Line=" + (line), true, null);
					return new StringBuilder ("return return_result;\n");
				}

				body.compile_file_text.append(body.inFrontOfSyntax());
				body.compile_file_text.append(body.getUpperFunction().getReturnType().generateCName() + " ");
				body.compile_file_text.append(" return_result = ");
				body.compile_file_text.append(compiled.toString() + ";\n");
				// body.compile_file_text.append("int old_rr_usage = return_result->usage;\n");

				generateEnding("File=" + compiler.getName() + ", Line=" + (line), true, null);
				// body.compile_file_text.append("/*if (old_rr_usage == return_result->usage)
				// */\n");

				return new StringBuilder("return return_result;");
			}

			/*
			 * null
			 */

			if (token.equals("null")) {
				returnedClasses.clear();
				returnedClasses.add(null);

				return new StringBuilder("NULL");
			}

			/*
			 * bool(ean) values
			 */

			if ((token.equals("false") || token.equals("true")) && !isMain) {
				FederRule rule = compiler.getApplyableRuleForBuildin ("bool");
				if (rule == null) {
					throw new RuntimeException ("No applyable rule found for \"buildin\" type bool");
				}

				result = new StringBuilder (rule.applyRule (body, stoken));

				returnedClasses.clear();
				returnedClasses.add(rule.getSpecifiedResultValue());
				getfrombinding = rule.getSpecifiedResultValue();

				continue;
			}

			/*
			 * int
			 */

			if (token.equals("int") && !isMain) {
				/*FederBinding fcbinding = body.getBinding(body, "int32", true);
				if (fcbinding == null) {
					throw new RuntimeException(
					    "Can't use int, if there hasn't been declared" + " a class called \"int32\"!");
				} else if (!(fcbinding instanceof FederClass)) {
					throw new RuntimeException("The name with the value 'int' is in use, but not as a class!");
				}

				FederClass fc = (FederClass) fcbinding;
				returnedClasses.clear();
				returnedClasses.add(fc);
				getfrombinding = fc;

				if (fc.isType()) {
					result = new StringBuilder(stoken);
					continue;
				}

				result = new StringBuilder("fdCreateInt (" + stoken + ")");
				continue;*/

				FederRule rule = compiler.getApplyableRuleForBuildin ("int");
				if (rule == null) {
					throw new RuntimeException ("No applyable rule found for \"buildin\" type int");
				}

				result = new StringBuilder (rule.applyRule (body, stoken));

				returnedClasses.clear();
				returnedClasses.add(rule.getSpecifiedResultValue());
				getfrombinding = rule.getSpecifiedResultValue();

				continue;
			}

			/*
			 * double
			 */

			if (token.equals("double") && !isMain) {
				FederRule rule = compiler.getApplyableRuleForBuildin ("double");
				if (rule == null) {
					throw new RuntimeException ("No applyable rule found for \"buildin\" type double");
				}

				result = new StringBuilder (rule.applyRule (body, stoken));

				returnedClasses.clear();
				returnedClasses.add(rule.getSpecifiedResultValue());
				getfrombinding = rule.getSpecifiedResultValue();

				continue;
			}

			if (token.equals("roperator") || token.equals("operator")
			        || token.equals("==") || token.equals("!=")
			        || token.equals("&&") || token.equals("||")) {

				SyntaxTreeElement ste = SyntaxTreeElementUtils.parseOperatorPrecedence (
				                            this, stoken, tokens, stringsOfTokens, indexToken);
				set(ste);
				break;
			}

			throw new RuntimeException("Unrecognized token: " + token + ", " + stoken);
		}

		// Append semicolon to terminate C line
		if (isMain && result.length() > 0)
			result.append(";");

		/*
		 * if (parent == null) currentInUse = this; else currentInUse = parent;
		 */

		return result;
	}

	/**
	 * This method tries to return a function with the return type 'binding', the
	 * name 'name and with the arguments 'classes'
	 *
	 * @param binding
	 * @param name
	 * @param classes
	 * @param allowParent
	 * @return Returns the found function. If nothing has been found, this function
	 *         returns null.
	 */
	public FederArguments getFunctionFromBinding(FederBinding binding, String name, List<FederBinding> classes,
	        boolean allowParent) {
		if (binding instanceof FederObject && ((FederObject) binding).hasSubtypes()) {
			if (((FederObject) binding).getResultType() == null) {
				throw new RuntimeException(
				    "There hasn't been defined a class for the object \"" + binding.getName() + "\".");
			}

			return ((FederClass) ((FederObject) binding).getResultType()).getFunction(body, name, classes, allowParent);
		}

		if (binding instanceof FederArguments && !allowParent && !getfromhistory.isEmpty()) {
			if (((FederArguments) binding).getArguments() == null) {
				throw new RuntimeException("The function from which you wanted a binding, doesn't return anything");
			} else if (((FederArguments) binding).getReturnType() instanceof FederInterface) {
				throw new RuntimeException(
				    "The function from which you wanted probably an object, does return an interface!");
			}

			return ((FederClass) ((FederArguments) binding).getReturnType()).getFunction(body, name, classes,
			        allowParent);
		}

		if (binding instanceof FederBody) {
			return ((FederBody) binding).getFunction(body, name, classes, allowParent);
		}

		throw new RuntimeException("There seems to be something wrong with the binding \"" + binding.getName() + "\"!");
	}

	/**
	 * This function tries to return a binding with the name 'name'
	 *
	 * @param binding
	 * @param name
	 * @param allowParent
	 * @return Returns the found binding. If nothing has been discovered, null is
	 *         returned by this function.
	 */
	public FederBinding getFromBinding(FederBinding binding, String name, boolean allowParent) {
		if (binding instanceof FederObject && ((FederObject) binding).hasSubtypes()) {
			if (((FederObject) binding).getResultType() == null) {
				throw new RuntimeException(
				    "There hasn't been defined a class for the object \"" + binding.getName() + "\".");
			}
			return ((FederClass) ((FederObject) binding).getResultType()).getBinding(body, name, allowParent);
		}

		if (binding instanceof FederArguments && !allowParent && !getfromhistory.isEmpty()) {
			if (((FederArguments) binding).getReturnType() == null) {
				throw new RuntimeException("The function from which you wanted a binding, doesn't return anything");
			} else if (((FederArguments) binding).getReturnType() instanceof FederInterface) {
				throw new RuntimeException(
				    "The function from which you wanted probably an object, does return an interface!");
			}

			return ((FederClass) ((FederArguments) binding).getReturnType()).getBinding(body, name, allowParent);
		}

		if (binding instanceof FederBody) {
			return ((FederBody) binding).getBinding(body, name, allowParent);
		}

		throw new RuntimeException(
		    "There seems to be something wrong with the binding \"" + binding.getName() + "\"!");
	}

	/**
	 * This method conveniently calls @link SyntaxTreeElementUtils.newBranchAt
	 * newBranchAt @endlink with @link SyntaxTreeElement this @endlink as the
	 * first argument.
	 * @param index An index in the bounds of @link SyntaxTreeElement.tokens tokens @endlink.
	 * @return Returns a new syntax tree branch
	 */
	public SyntaxTreeElement newBranchAt(int index) {
		return SyntaxTreeElementUtils.newBranchAt(this, index);
	}

	public void adjustTokeIndexForError() {
		SyntaxTreeElement ste = currentInUse;
		int index0 = 0;
		while (ste != null && ste.parent != null) {
			index0 += ste.indexToken;
			ste = ste.parent;
		}

		indexToken = index0;
	}
}
