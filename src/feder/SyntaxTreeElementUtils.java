package feder;

import java.util.*;
import feder.types.*;
import java.io.File;

/**
 * This class is just there to minimize the line numbers in SyntaxTreeElement.java
 * @author Fionn Langhans
 * @ingroup compiler
 */
public class SyntaxTreeElementUtils {

	/**
	 * @param index
	 * @return This method returns a new branch, which is created with a context
	 *         aware function. If the current index equals ')', the branch will be
	 *         reaching to the next ')'.
	 */
	public static SyntaxTreeElement newBranchAt(SyntaxTreeElement el, int index) {
		List<String> tokens0 = new LinkedList<>();
		List<String> stringsOfTokens0 = new LinkedList<>();

		if (index >= el.tokens.size()) {
			return new SyntaxTreeElement(el.compiler, el.body, el.line,
			                             tokens0, stringsOfTokens0);
		}

		if (el.tokens.get(index).equals("(")
		        || el.tokens.get(index).equals("[")) {

			String open_bracket = el.tokens.get(index);
			String close_bracket = ")";
			if (open_bracket.equals("[")) {
				close_bracket = "]";
			}

			// System.out.println(open_bracket + ", " + close_bracket);

			// Read till the next ')' (which is not in yet another scope)
			int count = 1;
			int i;
			for (i = index + 1; i < el.tokens.size(); i++) {
				if (el.tokens.get(i).equals(open_bracket)) {
					count++;
				} else if (el.tokens.get(i).equals(close_bracket)) {
					count--;
					if (count == 0) {
						break;
					}
				}

				tokens0.add(el.tokens.get(i));
				stringsOfTokens0.add(el.stringsOfTokens.get(i));
			}

			if (i == el.tokens.size()) {
				el.indexToken = index + tokens0.size();
				if (count == 1)
					throw new RuntimeException("A '" + close_bracket + "' is missing!");

				throw new RuntimeException("Several '" + close_bracket + "' are missing!");
			}

			return new SyntaxTreeElement(el.compiler, el.body, el.line, tokens0, stringsOfTokens0);
		}

		if (el.tokens.get(index).equals("=") || el.tokens.get(index).equals("==") || el.tokens.get(index).equals("!=")
		        || el.tokens.get(index).equals("if") || el.tokens.get(index).equals("while")
		        || el.tokens.get(index).equals("from") || el.tokens.get(index).equals("return")
		        || el.tokens.get(index).equals("global") || el.tokens.get(index).equals("for")) {
			// Start reading from the next index (i+1)
			for (int i = index + 1; i < el.tokens.size(); i++) {
				tokens0.add(el.tokens.get(i));
				stringsOfTokens0.add(el.stringsOfTokens.get(i));
			}
		} else {
			for (int i = index; i < el.tokens.size(); i++) {
				tokens0.add(el.tokens.get(i));
				stringsOfTokens0.add(el.stringsOfTokens.get(i));
			}
		}

		SyntaxTreeElement ste0 = new SyntaxTreeElement(el.compiler, el.body, el.line, tokens0, stringsOfTokens0);
		ste0.parent = el;
		return ste0;
	}

	/**
	 * This method generates an ending to compileTo from the body
	 *
	 * @param body
	 * @param compileTo
	 * @param ignore
	 */
	private static void generateEnding(String pos, FederBody body, StringBuilder compileTo, FederObject ignore,
	                                   boolean global) {
		if (body instanceof FederClass) {
			return;
		}


		FederRule ruleRemove = body.getCompiler().getApplyableRuleForStruct("remove");
			
		for (FederBinding binding : body.getBindings()) {
			if (binding == null)
				continue;
			if (!(binding instanceof FederObject))
				continue;
			FederObject obj = (FederObject) binding;

			// Interface can't/mustn't be deleted
			if (!obj.isGarbagable())
				continue;

			// Don't delete objects, which were generated in different compiler instances
			if (obj.parent != null
			        && !obj.parent.getMainNamespace().getCompiler().equals(body.getMainNamespace().getCompiler()))
				continue;

			// Don't delete global object
			if (obj.isGlobal)
				continue;

			/*
			 * The following code in the 'if' statement is currently unused, but could be
			 * used to improve the performance a program written in Feder is running
			 */
			/*if (obj == ignore) {
				// Ignore
				FederRule ruleDecrease = body.getCompiler().getApplyableRuleForStruct("decrease");
				if (ruleDecrease == null) {
					throw new RuntimeException("struct rule 'decrease' doesn't exist!");
				}

				//compileTo.append(body.inFrontOfSyntax()).append("fdDecreaseUsage ((fdobject*) ")
				//	.append(obj.generateCName()).append(");\n");

				compileTo.append(body.inFrontOfSyntax()).append(ruleDecrease.applyRule(body, obj.generateCName()));
				continue;
			}*/

			// Write a little notice when in Debug Mode
			if (body.getCompiler().isDebug()) {
				compileTo.append(body.inFrontOfSyntax())
				.append("printf (\"" + pos + ". Removing object " + obj.getName() + "\\n\");\n");
			}

			if (ruleRemove == null) {
				throw new RuntimeException("struct rule 'remove' doesn't exist!");
			}

			// Append source code which removes the selected object
			//compileTo.append(body.inFrontOfSyntax()).append("fdRemoveObject ((fdobject*)").append(obj.generateCName())
			//.append(");\n");
			compileTo.append(body.inFrontOfSyntax()).append(ruleRemove.applyRule(body, obj.generateCName()) + ";\n");
		}
	}

	/**
	 * Generates an ending of 'body' (remove objects). If 'completely' is true, the objects
	 * of the parents (to a specific one) are deleted, too.
	 * @param pos String representing the position of the current instance (for errors only)
	 * @param body
	 * @param completely
	 * @param ignore
	 */
	public static void generateEnding(String pos, FederBody body, boolean completely, FederObject ignore) {
		generateEnding(pos, body, completely, ignore, body.getMainNamespace().getCompiler().allowMain);
	}

	/**
	 * This method generates some source code, which deletes all global variables
	 * created by the program.
	 *
	 * @param fmn
	 * @param mainMethod
	 */
	public static void generateGlobalEnding(FederBody fmn, StringBuilder mainMethod) {
		_generateGlobalEnding(fmn.getMainNamespace(), mainMethod, new LinkedList<>());
	}

	private static void _generateGlobalEnding(FederBody fmn, StringBuilder mainMethod, List<FederBinding> objs) {
		for (FederBinding bind : fmn.getBindings()) {
			if (bind instanceof FederObject) {
				if (objs.contains(bind)) continue;

				objs.add(bind);
				FederObject obj = (FederObject) bind;
				if (obj.isGlobal && obj.isGarbagable()) {
					FederRule ruleRemove = fmn.getCompiler().getApplyableRuleForStruct("remove");
					if (ruleRemove == null) {
						throw new RuntimeException("struct rule 'remove' doesn't exist!");
					}

					if(fmn.getCompiler().isDebug()) {
						mainMethod.append("puts (\" Removing global object " + obj.getName() + "\");\n");
					}

					mainMethod.append(ruleRemove.applyRule(fmn, obj.generateCName()) + ";\n");
				} else if(obj.isGlobal && obj.isDataType()) {
					mainMethod.append("free ((void*) " + obj.generateCNameOnly() + ");\n");
				}
			} else if (bind instanceof FederBody) {
				_generateGlobalEnding((FederBody) bind, mainMethod, objs);
			}
		}
	}


	/**
	 * @param fmn
	 * @return Returns a string, which contains an initializing process for all
	 * global values used
	 */
	public static String generateGlobalStart(FederBody fmn) {
		return _generateGlobalStart(fmn, new LinkedList<>());
	}

	private static String _generateGlobalStart(FederBody fmn, List<FederBinding> objs) {
		StringBuilder sb = new StringBuilder();
		for (FederBinding bind : fmn.getBindings()) {
			if (bind instanceof FederObject) {
				if (objs.contains(bind)) continue;

				objs.add(bind);
				FederObject obj = (FederObject) bind;
				if(obj.isGlobal && obj.isDataType()) {
					sb.append(obj.generateCNameOnly() + " = (" + obj.getResultType().generateCName() + " *) malloc (sizeof (" + obj.getResultType().generateCName() + "));\n");
				}
			} else if (bind instanceof FederBody) {
				sb.append(_generateGlobalStart((FederBody) bind, objs));
			}
		}

		return sb.toString();
	}

	/**
	 * This method generates the end of a 'body'
	 *
	 * @param body
	 * @param completely
	 *            If this boolean is true, the generated text, will not only delete
	 *            the objects used by the current body, but also those used by
	 *
	 * @param ignore
	 */
	public static void generateEnding(String pos, FederBody body, boolean completely, FederObject ignore,
	                                  boolean global) {
		if (body instanceof FederClass) {
			return;
		}

		body.compile_file_text.append("\n// Generated ending\n");
		generateEnding(pos, body, body.compile_file_text, ignore, global);

		if (!completely)
			return;

		if (!body.isInFunction() && global) {
			generateGlobalEnding(body, body.compile_file_text);
		}

		FederBody b = body.getParent();
		FederBody last = body;
		while (b != null && last instanceof FederAutomat) {
			generateEnding(pos, b, body.compile_file_text, ignore, global);
			last = b;
			b = b.getParent();
		}
	}


	/**
	 * Preproccesor This function processes commands, so lines which start with
	 * "::".
	 *
	 * @param command
	 * @return
	 */
	public static StringBuilder command(FederCompiler compiler, SyntaxTreeElement ste,
	                                    FederBody currentBody, String command) {
		String[] args = command.split("[\t ]");

		if (args.length == 0) {
			throw new RuntimeException("Expected a command!");
		}

		if (args[0].equals("def")) {
			if (args.length != 2) {
				throw new RuntimeException(
				    "Expected a name (args length should be 2)! ([[:alpha:]_][[:alpha:]_[:digits:]]*)");
			}

			if (!args[1].matches("[A-Za-z_][A-Za-z_0-9]*")) {
				throw new RuntimeException("Expected a name! ([[:alpha:]_][[:alpha:]_[:digits:]]*)");
			}

			if (!compiler.preprocessorMacros.contains(args[1]))
				compiler.preprocessorMacros.add(args[1]);

			return new StringBuilder();
		} else if (args[0].equals("if") || args[0].equals("elif") || args[0].equals("ifn") || args[0].equals("elifn")) {
			if (args.length != 2) {
				throw new RuntimeException("Expected a defined macro name!");
			}

			if (args[0].equals("elif") && !compiler.preprocessorIfCame) {
				throw new RuntimeException("Expected a preprocessor 'if' before 'elif'");
			}

			boolean shouldBeTrue = !args[0].endsWith("n");

			compiler.preprocessorIfCame = true;
			boolean res = (compiler.preprocessorMacros.contains(args[1])
			               && !compiler.preprocessorWasTrue) == shouldBeTrue;
			if (res && !compiler.preprocessorWasTrue) {
				compiler.preprocessorWasTrue = true;
				compiler.preprocessorSkipCode = false;
			} else {
				compiler.preprocessorSkipCode = true;
			}

			return new StringBuilder();
		} else if (args[0].equals("else")) {
			if (args.length != 1) {
				throw new RuntimeException("Didn't expected anything else besides 'else'");
			}

			if (!compiler.preprocessorIfCame) {
				throw new RuntimeException("Expected a preprocessor 'if' before 'elif'");
			}

			compiler.preprocessorSkipCode = compiler.preprocessorWasTrue;

			return new StringBuilder();
		} else if ((args[0].equals("fi"))) {
			if (args.length != 1) {
				throw new RuntimeException("Didn't expected anything else besides 'fi'");
			}

			if (!compiler.preprocessorIfCame) {
				throw new RuntimeException("Expected a preprocessor 'if', 'elif' befor 'fi'");
			}

			compiler.preprocessorWasTrue = false;
			compiler.preprocessorIfCame = false;
			compiler.preprocessorSkipCode = false;
			return new StringBuilder();
		}

		else if (args[0].equals("use")) {
			if (args.length != 2) {
				throw new RuntimeException("Excpected ::use [argument]");
			}

			compiler.linkerOptions.add(args[1]);
			compiler.progCCOptions.add(args[1]);

			return new StringBuilder();
		} else if (args[0].equals("error") || args[0].equals("warning")) {
			StringBuilder msg = new StringBuilder();
			for (int i = 1; i < args.length; i++) {
				if (i != 1)
					msg.append(" ");

				msg.append(args[i]);
			}

			if (args[0].equals("error")) {
				throw new RuntimeException("Error: " + msg.toString());
			}

			System.err.println("Warning: " + msg.toString());
			return new StringBuilder();
		} else if (args[0].equals("rule")) {
			List<StringBuilder> list = new LinkedList<>();
			list.add(new StringBuilder());
			int i = 0;
			boolean isInString = false;
			while (i < command.length()) {
				char current_char = command.charAt(i);
				if (isInString && current_char == '"') {
					isInString = false;
					i++;
					continue;
				} else if (isInString && command.startsWith("\\\"", i)) {
					list.get(list.size()-1).append("\"");
					i += 2;
					continue;
				} else if (!isInString && current_char == '"') {
					isInString = true;
					i++;
					continue;
				} else if (!isInString && current_char == ' ') {
					if (list.get(list.size()-1).length() == 0) {
						i++;
						continue;
					}

					list.add(new StringBuilder());
					i++;
					continue;
				} else if (!isInString && current_char == '.') {
					list.add(new StringBuilder("."));
					list.add(new StringBuilder());
					i++;
					continue;
				} else {
					list.get(list.size()-1).append(current_char);
					i++;
					continue;
				}
			}

			List<String> topasstofunc = new LinkedList<>();
			for (StringBuilder sb : list) {
				topasstofunc.add(sb.toString());
				//System.out.print ("'" + sb.toString() + "'' ");
			}

			//System.out.println();

			FederRule rule = FederRule.define(currentBody, topasstofunc, ste);
			if (rule != null) {
				compiler.feder_rules.add(0, rule);

				if ((rule.getRule() & FederRule.RULE_BUILDIN) != 0) {
					compiler.feder_buildin_rules.add(0, rule);
				}

				if ((rule.getRule() & FederRule.RULE_STRUCT) != 0) {
					compiler.feder_struct_rules.add(0, rule);
				}
			}

			return new StringBuilder();
		} else if (args[0].equals("assume")) {
			assume(currentBody, args);
		}

		throw new RuntimeException("That command is invalid!");
	}

	private static void assume (FederBody currentBody, String[] args) {
		int turn = 0;

		FederBinding b0 = null;
		FederBinding b1 = null;

		for (int i = 1; i < args.length; i++) {
			String stoken = args[i];

			String nextToken = (i+1 == args.length ? "" : args[i+1]);

			FederBinding current_type = null;
			if (turn == 0)
				current_type = b0;
			else if (turn == 1)
				current_type = b1;

			if (!stoken.equals("null") && !stoken.equals(".")) {
				if (current_type == null) {
					current_type = currentBody.getBinding(currentBody, stoken, true);
					if (current_type == null && !stoken.equals("null")) {
						throw new RuntimeException("'" + stoken + "' doesn't exist!");
					}
				} else {
					if (!(current_type instanceof FederBody)) {
						throw new RuntimeException("The current element '" + current_type.getName() + "' is not a body!");
					}

					current_type = ((FederBody) current_type).getBinding((FederBody) current_type, stoken, false);
					if (current_type == null && !stoken.equals("null")) {
						throw new RuntimeException("The current element '" + stoken + "' wasn't found by the compiler!");
					}
				}
			}

			if (turn == 0)
				b0 = current_type;
			else if (turn == 1)
				b1 = current_type;

			if ((!nextToken.equals(".") && !nextToken.isEmpty())
			        || nextToken.equals("null")) {
				if (turn > 2) {
					throw new RuntimeException("You can't mention a third type!");
				}

				turn++;
			} else if ((!nextToken.equals("") && !nextToken.equals("."))
			           || (nextToken.equals(".") && stoken.equals("null"))) {

				throw new RuntimeException("Invalid " + i + " element: " + args[i+1]);
			}
		}
	}

	/**
	 * This function fails, if the include file could not be found.
	 * If the file was already included in the same compiler or if the
	 * compiler, which compiled the file to include, failed.
	 * @param name The name of the file to include (or its path).
	 * The file should be in one of the include paths.
	 * @return Returns a generated string, which might be necessary.
	 */
	public static String opInclude(FederCompiler compiler, String name) {
		if (compiler.includeDirs.size() == 0) {
			throw new RuntimeException("No include directories were added to compiler!");
		}

		// Create a new compiler
		FederCompiler compilerInclude = new FederCompiler(name, compiler);
		if (compiler.alreadyIncluded(compilerInclude.getName())) {
			compiler.fatalError = true;
			// File was already included in this file
			throw new RuntimeException("Already included in file!");
		}

		// Look if the file was already included
		FederCompiler fc = compiler.getCompiler(compilerInclude.getName());
		if (fc != null) {
			// System.out.println("# nonnull");
			for (FederBinding binding : fc.main.getBindings()) {
				binding.setHasToBuild(false);

				if (!compiler.main.getBindings().contains(binding))
					compiler.main.addBinding(binding);

			}

			compiler.included.add(fc);
			if (!fc.main.dependBodies.contains (compiler.main)) {
				fc.main.dependBodies.add (compiler.main);
			}

			return "";
		}

		// Search for the file
		File file = null;
		for (String dirpath : compiler.includeDirs) {
			String path0 = (dirpath + name).replace("/", Feder.separator);
			//System.err.println ("Try include path: " + path0);
			if (!path0.endsWith(Feder.separator)) {
				path0 += Feder.separator;
			}

			File file0 = new File(path0);
			if (file0.exists()) {
				file = file0;
				break;
			}
		}

		if (file == null) {
			// !! File not found
			throw new RuntimeException("File \"" + name + "\" not found in include paths!");
		}

		// Read the file
		String text = Feder.getStringFromFile(file);
		// Compile the source code
		FederNamespace main = compilerInclude.compile(text);

		if (main == null) {
			compiler.fatalError = true;
			throw new RuntimeException("Couldn't import file!");
		}

		if (!main.dependBodies.contains (compiler.main)) {
			main.dependBodies.add (compiler.main);
		}

		compiler.included.add(compilerInclude);
		compiler.globalIncluded.add(compilerInclude);

		for (FederBinding binding : main.getBindings()) {
			binding.setHasToBuild(false);

			if (!compiler.main.getBindings().contains(binding))
				compiler.main.addBinding(binding);
		}

		compiler.informInclude(name);

		return compilerInclude.generateLibraryCallerFunctionName() + " ();\n";
	}

	/**
	 * This method tries to convert a context to a bool (if clause!)
	 * @param s
	 * @param fc
	 * @param body
	 * @return
	 */
	public static String handleNonBoolToBool(FederCompiler compiler, String s,
	        FederClass fc, FederBody body) {
		if (fc == null) {
			return "0";
		}

		String typename_written = fc.toWrittenString();
		FederRule rule = compiler.getApplyableRuleForStruct(
		                     "conditional_statement_" + typename_written);
		if (rule == null) {
			compiler.fatalError = true;
			throw new RuntimeException("struct rule 'conditional_statement_"
			                           + typename_written + "' was not found!");
		}

		return rule.applyRule(body, s);
	}

	public static boolean isOperator (String tokenOperator) {
		return tokenOperator.equals ("roperator")
		       || tokenOperator.equals ("!=")
		       || tokenOperator.equals ("==")
		       || tokenOperator.equals ("&&")
		       || tokenOperator.equals ("||");
	}

	private static SyntaxTreeElement parseOperatorRule (String operator,
	        SyntaxTreeElement ste_left, SyntaxTreeElement ste_right) {

		SyntaxTreeElement result = new SyntaxTreeElement(
		    ste_left.compiler, ste_left.body, ste_left.line);

		if (((ste_left.returnedClasses.size() == 1
		        && ste_right.returnedClasses.size() == 1)

		        || (ste_left.returnedClasses.size() == 0
		            && ste_right.returnedClasses.size() == 1)

		        || (ste_left.returnedClasses.size() == 1
		            && ste_right.returnedClasses.size() == 0))) {

			FederBinding lvalue = null;
			boolean lvalue_nothing = false;
			if (ste_left.returnedClasses.size() == 1)
				lvalue = ste_left.returnedClasses.get(0);
			else
				lvalue_nothing = true;

			FederBinding rvalue = null;
			boolean rvalue_nothing = true;
			if (ste_right.returnedClasses.size() == 1)
				rvalue = ste_right.returnedClasses.get(0);
			else
				rvalue_nothing = true;

			for (FederRule rule : ste_left.compiler.feder_rules) {
				if (rule.isApplyable(operator, lvalue, rvalue, lvalue_nothing, rvalue_nothing)) {
					FederBinding return_value = rule.getResultValue(lvalue, rvalue);
					result.getfrombinding = return_value;
					result.getfromhistory.add(return_value);

					if (return_value != null) {
						result.returnedClasses.add(return_value);
					}

					result.result = new StringBuilder(
					    rule.applyRule(ste_left.body,
					                   ste_left.result.toString(),
					                   ste_right.result.toString()));

					return result;
				}
			}
		}

		FederBinding lvalue = null;
		if (ste_left.returnedClasses.size() == 1)
			lvalue = ste_left.returnedClasses.get(0);

		FederBinding rvalue = null;
		if (ste_right.returnedClasses.size() == 1)
			rvalue = ste_right.returnedClasses.get(0);

		String s0 = "null";
		if (lvalue != null)
			s0 = lvalue.getName();

		String s1 = "null";
		if (rvalue != null)
			s1 = rvalue.getName();

		throw new RuntimeException("Didn't find any rule for: " + s0 + " "
		                           + operator + " " + s1
		                           + ", " + ste_left.returnedClasses.size()
		                           + " " + ste_right.returnedClasses.size());
	}

	private static int getOperatorPrecedence (SyntaxTreeElement ste,
	        String operator) {

		Integer precedenceValue = ste.compiler.operator_precedence.get(operator);
		if (precedenceValue == null)
			return -1;

		return precedenceValue.intValue();
	}

	private static int lookaheadOperator (SyntaxTreeElement ste,
	                                      int minPrecedence,
	                                      List<String> tokens, List<String> stringsOfTokens, int indexStart) {

		int scope = 0;
		int i;
		for (i = indexStart; i < tokens.size()
		        && (scope > 0
		            || !isOperator(tokens.get(i))
		            || getOperatorPrecedence(ste, stringsOfTokens.get(i)) >= minPrecedence); i++) {

			if (tokens.get(i).equals("(") || tokens.get(i).equals("["))
				scope++;
			else if (tokens.get(i).equals(")") || tokens.get(i).equals("]"))
				scope--;
		}

		return i;
	}

	private static int lookaheadNextOperator (SyntaxTreeElement ste,
	        int precedence, String operatorSearched,
	        List<String> tokens, List<String> stringsOfTokens, int indexStart) {

		int scope = 0;
		int i;
		for (i = indexStart; i < tokens.size()
		        && (scope > 0
		            || !isOperator(tokens.get(i))
		            || (getOperatorPrecedence(ste, stringsOfTokens.get(i)) != precedence
						&& !stringsOfTokens.get(i).equals(operatorSearched))) ; i++) {

			if (tokens.get(i).equals("(") || tokens.get(i).equals("["))
				scope++;
			else if (tokens.get(i).equals(")") || tokens.get(i).equals("]"))
				scope--;
		}

		/*if (i > maxLen)
			return maxLen;*/

		return i;
	}

	private static SyntaxTreeElement createInRange (SyntaxTreeElement ste,
	        List<String> tokens, List<String> stringsOfTokens,
	        int indexStart, int indexEnd) {

		List<String> tokens0 = new LinkedList<>();
		List<String> stringsOfTokens0 = new LinkedList<>();

		for (int i = indexStart; i <= indexEnd; i++) {
			tokens0.add(tokens.get(i));
			stringsOfTokens0.add(stringsOfTokens.get(i));
			//System.out.print(stringsOfTokens.get(i) + " ");
		}

		//System.out.println();

		return new SyntaxTreeElement(ste.compiler, ste.body, ste.line,
		                             tokens0, stringsOfTokens0);
	}

	/**
	 * The operator-precendence parsing algorithm
	 * (look at https://en.wikipedia.org/wiki/Operator-precedence_parser
	 * for more information)
	 * @param operator The operator found
	 * @param tokens
	 * @param stringsOfTokens
	 * @param indexStart
	 */
	public static SyntaxTreeElement parseOperatorPrecedence (
	    SyntaxTreeElement ste_left,
	    String operator,
	    List<String> tokens,
	    List<String> stringsOfTokens,
	    int indexStart) {

		int minPrecedence = getOperatorPrecedence(ste_left, operator);
		int indexNextLessOperator = lookaheadOperator (ste_left, minPrecedence,
		                            tokens, stringsOfTokens, indexStart);

		String operatorSearched = "";
		if (indexNextLessOperator < tokens.size())
			operatorSearched = stringsOfTokens.get(indexNextLessOperator);

		int indexNextOperator = -1;
		while ((indexNextOperator = lookaheadNextOperator (ste_left,
				minPrecedence, operatorSearched,
				tokens, stringsOfTokens, indexStart))
			<= indexNextLessOperator) {

			SyntaxTreeElement ste_right = createInRange(ste_left,
				tokens, stringsOfTokens, indexStart, indexNextOperator-1);
			ste_right.result = ste_right.compile();

			SyntaxTreeElement old_ste_left = ste_left;
			ste_left = parseOperatorRule(operator, ste_left, ste_right);

			if (indexNextOperator == tokens.size()) {
				ste_left.parent = old_ste_left;
				return ste_left;
			}

			operator = stringsOfTokens.get(indexNextOperator);
			indexStart = indexNextOperator + 1;
		}

		SyntaxTreeElement ste_right = parseOperatorPrecedence (ste_left,
		                              operator,
		                              tokens, stringsOfTokens, indexNextLessOperator+1);

		if (ste_right.parent == ste_left)
			return ste_right;

		try {
			ste_left = parseOperatorRule(operator, ste_left, ste_right);
		} catch (Exception ex) {
			throw ex;
		}

		return ste_left;
	}
}
