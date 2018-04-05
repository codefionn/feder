package feder;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import feder.types.FederArguments;
import feder.types.FederAutomat;
import feder.types.FederBinding;
import feder.types.FederBody;
import feder.types.FederClass;
import feder.types.FederCompileGen;
import feder.types.FederFunction;
import feder.types.FederHeaderGen;
import feder.types.FederMainNamespace;
import feder.types.FederNamespace;
import feder.types.FederObject;

/**
 * @author Fionn Langhans
 * @ingroup compiler
 */
public class FederCompiler {
	private Lexer lexer;
	private String name;

	/**
	 * Allows this compiler instance to generate
	 * 'int main (int lenargs, char ** vargs)'
	 * instead of the library loader function
	 */
	public boolean allowMain = true;

	/**
	 * This value allows this program to use options
	 * for the visual studio c/c++ compiler instead of
	 * the ones for the GNU GCC compiler (or clang)
	 */
	public boolean usewincl = false;

	/**
	 * This value is currently unneeded, may be important
	 * in later releases
	 */
	boolean nomultithread = false;

	/**
	 * The build directory, where the files should be
	 * generated
	 */
	public String buildDir = "./";

	/**
	 * Which include directories should be used
	 */
	public List<String> includeDirs = new LinkedList<String>();

	/**
	 * This list contains compiler instances, which are included
	 * by this compiler instance
	 */
	public List<FederCompiler> included = new LinkedList<>();

	/**
	 * This list contains compiler instances, which are generally
	 * used by the compile process
	 */
	public List<FederCompiler> globalIncluded = new LinkedList<>();

	/**
	 * This list represents, in which order the components should
	 * be build
	 */
	public List<FederBinding> buildOrder = new LinkedList<>();

	/**
	 * This allows this program to start a cc compiler to generate
	 * an object file
	 */
	public boolean allowCCcompile = true;

	/**
	 * The name of the cc compiler
	 */
	public String progCC = "cc";

	/**
	 * The list of options, which should be given to the cc compiler
	 * (--coption)
	 */
	public List<String> progCCOptions = new LinkedList<>();

	/**
	 * The list of options, which should be given to the linker
	 * (--loption)
	 */
	public List<String> linkerOptions = new LinkedList<>();

	/**
	 * Print the build command to console (stdout)
	 */
	public boolean printCommands = false;

	/**
	 * This string contains the main method/library method
	 * of this compiler instance
	 */
	private StringBuilder mainmethod = new StringBuilder();

	/**
	 * This object is the main namespace of this compiler instance
	 */
	public FederNamespace main;

	/**
	 * Allow this program to include debug peaces in the code
	 */
	private boolean debug = false;

	/**
	 * Was one of the last preprocessor statements true ?
	 */
	public boolean preprocessorWasTrue = false;

	/**
	 * Should the compiler skip the code
	 */
	public boolean preprocessorSkipCode = false;

	/**
	 * Did the preprocessor use an (unclosed) conditional statement (if) ?
	 */
	public boolean preprocessorIfCame = false;

	/**
	 * All preprocessor macros
	 */
	public List<String> preprocessorMacros = new LinkedList<>();

	/**
	 * Contains Feder rules, which were defined in Feder source code
	 * (all feder rules)
	 */
	public List<FederRule> feder_rules = new LinkedList<>();

	/**
	 * Contains only the buildin Feder rules
	 * (performance improvements)
	 */
	public List<FederRule> feder_buildin_rules = new LinkedList<>();

	/**
	 * Contains only the struct Feder rules
	 * (performance improvements)
	 */
	public List<FederRule> feder_struct_rules = new LinkedList<>();

	/**
	 * If true, the thrown error is fatal, meaning that the compile
	 * instance will immediately stop processing the source code and
	 * return an error (state)
	 */
	public boolean fatalError = false;

	/**
	 * A map, for mapping the precedence of operators
	 * (for the operator presedence parser)
	 *
	 * Type of key: String => The operator, represented by the original string
	 * Type of value: Integer => The presedence
	 */
	public Map<String, Integer> operator_precedence = new HashMap<>();

	/**
	 * @param name0 The name of the compiler (file name)
	 * @param debug0 Debug mode ? (print more information, when running a compiled program)
	 * @param systemName The name of the system (WINDOWS, POSIX)
	 * @param progCC0 the cc compiler to use
	 * @param usewincl0 use the options of microsoft visual studio compiler ?
	 * @param nomultithread0 Not implement, maybe in later versions
	 * @param printCommands0 print the executed build command ?
	 */
	public FederCompiler(String name0, boolean debug0, String systemName, String progCC0, boolean usewincl0,
	                     boolean nomultithread0, boolean printCommands0) {
		setName(name0);
		debug = debug0;
		progCC = progCC0;
		usewincl = usewincl0;
		nomultithread = nomultithread0;
		printCommands = printCommands0;

		preprocessorMacros.add(systemName);
	}

	/**
	 * Create a compiler, which depends on an other compiler
	 * @param name0 The name (file name)
	 * @param compiler the compiler instance to use (the 'parent' of this one)
	 */
	public FederCompiler(String name0, FederCompiler compiler) {
		setName(name0);
		debug = compiler.isDebug();
		includeDirs = compiler.includeDirs;
		buildDir = compiler.buildDir;
		globalIncluded = compiler.globalIncluded;
		allowMain = false;
		preprocessorMacros = compiler.preprocessorMacros;
		progCC = compiler.progCC;
		progCCOptions = compiler.progCCOptions;
		usewincl = compiler.usewincl;
		nomultithread = compiler.nomultithread;
		linkerOptions = compiler.linkerOptions;
		printCommands = compiler.printCommands;
		feder_rules = compiler.feder_rules;
		feder_buildin_rules = compiler.feder_buildin_rules;
		feder_struct_rules = compiler.feder_struct_rules;
		operator_precedence = compiler.operator_precedence;
	}

	/**
	 * @param operator An operator
	 * @param lvalue The type of the left side
	 * @param rvalue The type of the right side
	 * @param lvalue_nothing Is left of the operator nothing ?
	 * @param rvalue_nothing Is right of the operator nothing ?
	 * @return Returns a rule, which is can be applied to the given arguments (operator, lvalue,
	 * rvalue, lvalue_nothing, rvalue_nothing)
	 */
	public FederRule getApplyRuleFor (String operator, FederBinding lvalue, FederBinding rvalue,
	                                  boolean lvalue_nothing, boolean rvalue_nothing) {
		for (FederRule rule : feder_rules) {
			if (rule.isApplyable(operator, lvalue, rvalue, lvalue_nothing, rvalue_nothing)) {
				return rule;
			}
		}

		return null;
	}

	/**
	 * @param buildin_name
	 * @return Returns a rule, which has the type 'buildin' and the name 'buildin_name'.
	 * If nothing was found, this method returns null.
	 */
	public FederRule getApplyableRuleForBuildin(String buildin_name) {
		for (FederRule rule : feder_buildin_rules) {
			if (rule.isApplyable (buildin_name)) {
				return rule;
			}
		}

		return null; // no rule found
	}

	/**
	 * @param struct_name
	 * @return Returns a rule, which has the type 'struct' and the name 'struct_name'.
	 * If nothing was found, this method returns null.
	 */
	public FederRule getApplyableRuleForStruct(String struct_name) {
		for (FederRule rule : feder_struct_rules) {
			if (rule.isApplyable (struct_name)) {
				return rule;
			}
		}

		return null; // no rule found
	}

	/**
	 * @return Returns true, if debug mode is switched on
	 * (massive code flow informations)
	 */
	public boolean isDebug() {
		return debug;
	}

	/**
	 * @param The new name of the compiler, all inappropriate sign
	 * will be replaced by '_'
	 */
	private void setName(String name0) {
		name = name0.replace(".", "_").replace("\\", "_").replace("/", "_").replace("+", "_").replace("-", "_");
		if (name.endsWith("_fd"))
			name = name.substring(0, name.length() - 3);
	}

	/**
	 * Return the name of the current compiler, which was
	 * generated from the file name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return Returns the path to the header file, which will be
	 * generated by the 'compile' method
	 */
	public String getNameFileHeader() {
		return (buildDir + name + ".fd.h").replace("/", Feder.separator);
	}

	/**
	 * @return Returns the only the name of the header file.
	 */
	public String getNameFileHeaderOnly() {
		return ( name + ".fd.h").replace("/", Feder.separator);
	}

	/**
	 * @return Returns the path to the source code file
	 */
	public String getNameFileCompile() {
		return (buildDir + name + ".fd.c").replace("/", Feder.separator);
	}

	/**
	 * @return Returns the name of the source code file, which
	 * is generated by the 'compile' method.
	 */
	public String getNameFileCompileOnly() {
		return (name + ".fd.c").replace("/", Feder.separator);
	}

	/**
	 * @return Returns the path to the object file, which will be generated by
	 * the 'compile' method, if the cc compiler is enabled.
	 */
	public String getNameFileObject() {
		return (buildDir + name + ".fd." + Feder.objEnd).replace("/", Feder.separator);
	}

	/**
	 * @return Returns the name of the object file, without a path in front
	 */
	public String getNameFileObjectOnly() {
		return (name + ".fd." + Feder.objEnd).replace("/", Feder.separator);
	}

	/**
	 * @return Returns true, if the include file named 'name0' has already been
	 * included once in the current file (compiler)
	 */
	public boolean alreadyIncluded(String name0) {
		if (name.equals(name0)) {
			// The given name equals the name of the current compiler
			return true;
		}

		for (FederCompiler fc : included) {
			/*
			 * if (fc.equals(this)) { throw new RuntimeException("Unexpected error: " +
			 * "compiler added itself to include list"); }
			 */
			if (fc.getName().equals(name0)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Add a preprocessor macro to preprocessorMacros
	 */
	public void addMacros(List<String> macros) {
		for (String macro : macros) {
			if (!preprocessorMacros.contains(macro))
				preprocessorMacros.add(macro);
		}
	}

	/**
	 * @return Returns the compiler, which is named 'name0'. If no compiler named
	 * 'name0' has been found, this method returns 'null'.
	 */
	public FederCompiler getCompiler(String name0) {
		if (name.equals(name0)) {
			return this;
		}

		for (FederCompiler fc : globalIncluded) {
			/*
			 * if (fc.equals(this)) { throw new RuntimeException("Unexpected eror: " +
			 * "compiler added itself to include list"); }
			 */
			if (fc.getName().equals(name0)) {
				return fc;
			}
		}

		return null;
	}

	/**
	 * This function first runs a lexical analysis other text0. If no errors
	 * occurred during that process, a raw syntax analysis is started, which
	 * analysis the results generated by @link Lexer.lex Lexer @endlink . For
	 * the syntax analysis @link Syntax.validateSyntax validateSyntax @endlink
	 * is called. Again, if an error occurred the process is terminated and
	 * hopefully an error message will be printed to the standard output.
	 *
	 * The next step is to process the generated tokens (which were sporadically
	 * analyzed by the syntax analysis). The tokens are separated by the 'newline'
	 * token and passed to @link SyntaxTreeElement.compile compile @endlink . The
	 * generated source code is put in the corresponding bodies (the current bodies
	 * used in the source code like a class or function).
	 *
	 * This source code again will be written to the header and source code file in
	 * a more or less weird way: First the main function of the source file will be
	 * completed, then the beginning of both files will be generated (like includes,
	 * the compile only once mechanism) after the the method
	 * @link FederCompiler.writeToFileForwardDeclaration writeToFileForwardDeclaration @endlink
	 * will be called to generate forward declarations, like classes, functions. After that
	 * the 'real' source code will be put into the files with
	 * @link FederCompiler.writeToFile writeToFile @endlink . If that is done, the end of both
	 * files will be generated, like the end of the compile once mechanism or the end of
	 * 'extern "C" {'.
	 *
	 * @param text0 Feder source code
	 * @return Returns the 'genesis' namespace (the first namespace, which
	 * doesn't have any parent above). If an error occurred during one of the
	 * phases, this function returns null.
	 */
	public FederNamespace compile(String text0) {
		String text = text0 + "\n"; // make it easier for the lexer
		mainmethod = new StringBuilder();
		// System.out.println("# Compile=" + getName());

		// System.out.println("+ Starting lexical analysis");
		(lexer = new Lexer(this)).lex(text);
		if (lexer.errors == 0) {
			// System.out.println("+ Lexical analysis finished");
		} else {
			System.err.println("- Lexical analysis finished with errors");
			return null;
		}

		// lexer.dbgOutput();

		// System.out.println("+ Starting syntax analysis");
		// System.out.println("+ Validating syntax");
		if (Syntax.validateSyntax(lexer)) {
			// System.out.println("+ Validated syntax");
		} else {
			System.err.println("- Couldn't validate syntax");

			try {
				Thread.sleep(10l);
			} catch (Exception ex) {
				/*
				 * User wants to kill the program
				 */
			}

			return null;
		}

		// Create a 'genisis' object. That is a object, which has no parent. All
		// other bodies created will depend on this one.
		main = new FederMainNamespace(this, getName(), null);
		main.compile_file_text.append("// namespace " + main.getName() + " {\n");
		if (allowMain) {
			// If the main method is allowed, add an object called 'args'
			FederObject args_obj = new FederObject("args", main);
			main.addBinding(args_obj);
		}

		List<SyntaxTreeElement> elements = new LinkedList<>();
		List<String> tokens0 = new LinkedList<>();
		List<String> stringsOfTokens0 = new LinkedList<>();
		FederBody currentBody = main;

		/*
		 * If this value is greater than 0, the compiler
		 * will return an error code
		 */
		int errors = 0;
		int line = 0;

		/*
		 * The for loop below cuts the code into pieces (line by line) and tries to
		 * convert the code to C code
		 */

		for (int i = 0; i < lexer.tokens.size(); i++) {
			if (lexer.tokens.get(i).equals("newline")) {

				/*
				 * A new line has been started
				 */

				line++;

				if (tokens0.size() >= 1 && preprocessorSkipCode && (!tokens0.get(0).equals("command")
				        || (tokens0.get(0).equals("command")
				            && !stringsOfTokens0.get(0).startsWith("else")
				            && !stringsOfTokens0.get(0).startsWith("fi")
				            && !stringsOfTokens0.get(0).startsWith("elif")))) {


					tokens0 = new LinkedList<>();
					stringsOfTokens0 = new LinkedList<>();
					continue;
				}

				SyntaxTreeElement ste = new SyntaxTreeElement(this, currentBody, line, tokens0,
				        stringsOfTokens0);
				ste.isMain = true;
				tokens0 = new LinkedList<>();
				stringsOfTokens0 = new LinkedList<>();

				elements.add(ste);

				/*
				 * Try - catch block is used here, because
				 * SyntaxTreeElement.compile() may throw Exceptions
				 * (hopefully only RuntimeException s)
				 */
				try {
					StringBuilder compiled = ste.compile();
					boolean changed = (currentBody != ste.body);

					if (changed && currentBody instanceof FederAutomat && currentBody.getParent() == ste.body.getParent()
					        && ((FederAutomat) ste.body).getType().startsWith("else")) {
						SyntaxTreeElementUtils.generateEnding("", currentBody, false, null);
						ste.body.getParent().compile_file_text.append(currentBody.compile_file_text);
						ste.body.getParent().compile_file_text.append(ste.body.inFrontOfSyntax() + "}\n");
					} else if (changed && currentBody instanceof FederAutomat && currentBody.getParent() == ste.body) {
						SyntaxTreeElementUtils.generateEnding("", currentBody, false, null);
						ste.body.compile_file_text.append(currentBody.compile_file_text);
						ste.body.compile_file_text.append(ste.body.inFrontOfSyntax() + "}\n");
					}

					currentBody = ste.body;

					// Add to compile_file_test ?
					if (!compiled.toString().trim().equals(";") && compiled.toString().trim().length() != 0) {
						String infront = currentBody.inFrontOfSyntax();
						if (currentBody == main)
							infront = "\t";
						else if (changed) {
							infront = infront.substring(1);
						}

						if (ste.returnedClasses.size() > 1) {
							throw new RuntimeException(Syntax.error(this, ste.tokens, ste.stringsOfTokens, ste.line,
							                                        ste.tokens.size() - 1, "Can't usage ',' in main scope!"));
						}

						currentBody.compile_file_text.append(infront);

						if (ste.returnedClasses.size() == 1 && ste.returnedClasses.get(0) != null
						        && ste.returnedClasses.get(0) instanceof FederClass
						        && !((FederClass) ste.returnedClasses.get(0)).isType()) {
							/*
							 * System.out.println("# " + getName() + " | L. "+ elements.size() + " | " +
							 * ste.returnedClasses.get(0).getName());
							 */

							FederRule ruleRemoveFunc = getApplyableRuleForStruct("remove_func");
							if (ruleRemoveFunc == null) {
								throw new RuntimeException("struct rule 'remove_func' doesn't exist!");
							}

							//currentBody.compile_file_text.append("fdRemoveObject_func ((fdobject*) ");
							if (compiled.toString().endsWith(";")) {
								compiled.replace(compiled.length() - 1,
								                 compiled.length(), "");
							}

							compiled = new StringBuilder(ruleRemoveFunc.applyRule(currentBody, compiled.toString()));
							compiled.append(";");
						}

						currentBody.compile_file_text.append(compiled.toString());

						/*if (ste.returnedClasses.size() == 1 && ste.returnedClasses.get(0) != null
						        && ste.returnedClasses.get(0) instanceof FederClass
						        && !((FederClass) ste.returnedClasses.get(0)).isType()) {
							if (currentBody.compile_file_text.toString().endsWith(";")) {
								currentBody.compile_file_text.replace(currentBody.compile_file_text.length() - 1,
								                                      currentBody.compile_file_text.length(), "");
							}

							currentBody.compile_file_text.append(");");
						}*/

						currentBody.compile_file_text.append("\n");
					}
				} catch (Exception ex) {
					if (!ex.getClass().equals(RuntimeException.class)) {
						System.err.println(
						    Syntax.error(this, ste.tokens, ste.stringsOfTokens, ste.line, ste.indexToken, ""));
						ex.printStackTrace();
					} else {
						ste.adjustTokeIndexForError();
						if (ex.getMessage().trim().isEmpty()) {
							ex.printStackTrace();
						}

						if (ste.currentInUse != null && ste.currentInUse != ste) {
							for (String s : ste.currentInUse.stringsOfTokens) {
								System.out.print(s + " ");
							}
							System.out.println();
						}

						System.err.println(Syntax.error(this, ste.tokens, ste.stringsOfTokens, ste.line, ste.indexToken,
						                                ex.getMessage()));
					}

					if (ste.body != null)
						currentBody = ste.body;

					errors++;

					if (fatalError) {
						return null; // Return nothing = error
					}
				}
			} else {
				tokens0.add(lexer.tokens.get(i));
				stringsOfTokens0.add(lexer.softokens.get(i));
			}
		}

		if (errors > 0 || currentBody != main) {
			/*
			 * An errror arose
			 */

			if (currentBody != main) {
				System.err.println("- File: " + getName ());
				System.err.println("- One or more ';' are missing!");
			}

			if (errors == 1)
				System.err.println("- Failed to translate syntax because one error occured.");
			else
				System.err.println("- Failed to translated syntax because " + errors + " errors occured.");

			try {
				Thread.sleep(10l);
			} catch (Exception ex) {
				/*
				 * User wants to kill the program or some other things arised
				 */
			}

			return null;
		}

		File file = new File(getNameFileHeader()).getParentFile();
		if (file != null)
			file.mkdirs();

		try {

			/*
			 * Try to generate and write the generated code
			 * to the compiler & header file
			 */

			BufferedWriter compileFile = Files.newBufferedWriter(Paths.get(getNameFileCompile()),
			                             Charset.forName("UTF-8"));
			BufferedWriter headerFile = Files.newBufferedWriter(Paths.get(getNameFileHeader()),
			                            Charset.forName("UTF-8"));

			headerFile.write("/* Compiled with jfederc */\n");
			headerFile.write("#ifdef _WIN32\n#pragma once\n#endif\n");
			compileFile.write("/* Compiled with jfederc */\n#ifdef __cplusplus\nextern \"C\" {\n#endif\n");
			headerFile.write("#ifndef " + name.toUpperCase() + "_H_\n#define " + name.toUpperCase() + "_H_\n\n");
			headerFile.write("#ifdef __cplusplus\nextern \"C\" {\n#endif\n");
			compileFile.write("#include \"" + getNameFileHeaderOnly() + "\"\n\n");

			for (FederCompiler compiler : included) {
				if (compiler == this)
					continue;

				headerFile.write("#include \"" + compiler.getNameFileHeaderOnly() + "\"\n");
			}

			writeToFileForwardDeclaration(headerFile);
			headerFile.write("\n");

			writeToFile(compileFile, headerFile, main);
			// writeToFile (compileFile, headerFile);

			String returnExitSuccess = "\t#ifdef EXIT_SUCCESS\n\treturn EXIT_SUCCESS;\n"
			                           + "\t#else\n\treturn 0;\n\t#endif\n";

			if (allowMain) {
				// Generate main method
				compileFile.write("int main (int lenargs, char ** vargs) {\n");

				/**
				 * The following inserts code, which changes the args given the
				 * program to a Feder compatible object (fdc_List)
				 */
				FederBinding binding_list_class = main.getBinding(main, "List", false);
				if (binding_list_class == null || !(binding_list_class instanceof FederClass)) {
					System.err.println(
					    "WARNING: Program arguments are not supported, because 'List' hasn't been created as class!");
					compileFile.write("\tfdobject * federobj_0args = NULL;\n");
				} else {
					FederObject args_obj = (FederObject) main.getBinding(main, "args", false);
					args_obj.setTypeManual(binding_list_class);

					compileFile.write("\tfdc_List * " + args_obj.generateCNameOnly() + " = fdCreateClass_fdc_List ();\n");

					FederRule ruleIncrease = getApplyableRuleForStruct("increase");
					if (ruleIncrease == null) {
						throw new RuntimeException("Feder struct rule 'increase' doesn't exist!");
					}

					//compileFile.write("\tfdIncreaseUsage ((fdobject*) federobj_args);\n");
					compileFile.write(ruleIncrease.applyRule(main, args_obj.generateCName()) + ";\n");

					FederRule ruleString = getApplyableRuleForBuildin ("string_raw");
					if (ruleString == null) {
						throw new RuntimeException("No rule found for \"buildin\" type \"string_raw\"");
					}

					compileFile.write("\tfor (int i = 0; i < lenargs; i++) {\n");
					FederBinding class_obj = main.getBinding(main, "object", true);
					if (class_obj == null || !(class_obj instanceof FederClass)) {
						throw new RuntimeException("For 'args': List has been defined, but not the class 'object'");
					}

					FederArguments list_class_add = ((FederClass) binding_list_class).getFunction(main, "add", Arrays.asList(class_obj), true);
					if (list_class_add == null) {
						throw new RuntimeException("For 'args': List has been created, but not a function add (object) in that class!");
					}

					compileFile.write(
					    "\t\t" + list_class_add.generateCName()
					    + " (&" + args_obj.generateCNameOnly()
					    + ", (" + class_obj.generateCName() + ") "
					    + ruleString.applyRule(main, "vargs[i]") + ");\n");
					compileFile.write("\t}\n");
				}

				// Generate an ending for all global values
				for (FederCompiler compiler : globalIncluded) {
					if (compiler == this)
					continue;

					SyntaxTreeElementUtils.generateGlobalEnding(compiler.main, mainmethod);
				}

				for (FederCompiler compiler : globalIncluded) {
					if (compiler == this)
					continue;

					compileFile.write(SyntaxTreeElementUtils.generateGlobalStart(compiler.main));
				}

				compileFile.write(mainmethod.toString());
				compileFile.write(returnExitSuccess);
				compileFile.write("}");
			} else {
				// Generate library initialize method
				// The method is an inline method, to allow the other developers to inspect it's
				// contents
				headerFile.write("int " + generateLibraryCallerFunctionName() + " ();\n");
				compileFile.write("int " + generateLibraryCallerFunctionName() + " () {\n");
				compileFile.write(mainmethod.toString());
				compileFile.write(returnExitSuccess);
				compileFile.write("}\n");
			}

			// Terminate the extern '"C" {' if using c++
			compileFile.write("\n#ifdef __cplusplus\n}\n#endif\n");
			headerFile.write("\n#ifdef __cplusplus\n}\n#endif\n");

			headerFile.write("#endif /* " + name.toUpperCase() + "_H_*/\n");

			compileFile.close();
			headerFile.close();
		} catch (Exception ex) {
			ex.printStackTrace();
			System.err.println("- Failed to translate syntax");
			try {
				Thread.sleep(10l);
			} catch (Exception ex0) {
				/*
				 * User wants to kill the program
				 */
			}
			return null;
		}

		if (allowCCcompile) {
			// System.out.println("+ Starting CC compiler");
			List<String> cmd = new LinkedList<>();
			cmd.add(progCC);

			if (!usewincl) {
				// Tell the GNU GCC compatible compiler
				// to use c11 & pipe optimization
				cmd.add("-std=c11");
				cmd.add("-pipe");
			}

			if (usewincl)
				cmd.add("/TC"); // Tell the ms compiler, that it should compile 'C' source code

			// Include files
			if (usewincl)
				cmd.add("/I");
			else
				cmd.add("-I");
			cmd.add(".");

			// compile only, no linking
			if (usewincl)
				cmd.add("/c");
			else
				cmd.add("-c");
			cmd.add(getNameFileCompileOnly());

			// Define C Macro "DEBUG" if using the debug mode
			if (isDebug()) {
				if (usewincl)
					cmd.add("/DDEBUG");
				else
					cmd.add("-DDEBUG");
			}

			// The output file's name (object file)
			if (usewincl) {
				cmd.add("/Fo"+getNameFileObjectOnly());
			} else {
				cmd.add("-o");
				cmd.add(getNameFileObjectOnly());
			}

			cmd.addAll(progCCOptions); // Could cause in incomprehensible bug
			// when using a specific gcc version
			// (the one of Ubuntu 16.04 LTS)

			if (printCommands) {
				for (String s : cmd) {
					System.out.print (s + " ");
				}

				System.out.println ();
			}

			try {
				// Start a compile process in the build directory
				// (may prevent the compiler from damaging the current
				// directory)
				Process pb = new ProcessBuilder(cmd).inheritIO().directory(new File(buildDir)).start();
				if (pb.waitFor() != 0) {
					System.err.println("- CC compiler failed");
					try {
						Thread.sleep(10l);
					} catch (Exception ex0) {
						/*
						 * User wants to kill the program
						 */
					}
					return null;
				}
			} catch (IOException | InterruptedException e) {
				System.err.println("- I/O: Starting CC compiler failed");
				try {
					Thread.sleep(10l);
				} catch (Exception ex0) {
					/*
					 * User wants to kill the program
					 */
				}
				return null;
			}
		}

		return main;
	}

	/**
	 * Generate the name of the library load function
	 */
	public String generateLibraryCallerFunctionName() {
		return "fdlibcall_" + getName();
	}

	public void writeToFileForwardDeclaration(BufferedWriter headerFile) throws IOException {
		for (FederBinding body0 : buildOrder) {
			if (body0 instanceof FederHeaderGen && body0.hasToBuild()) {
				headerFile.write(((FederHeaderGen) body0).generateInHeader());
			}
		}
	}

	/*
	 * public void writeToFile (BufferedWriter compileFile, BufferedWriter
	 * headerFile) throws IOException { for (FederBinding body1 : buildOrder) {
	 *
	 * if (body1 instanceof FederObject) { if (!body1.hasToBuild()) return;
	 *
	 * compileFile.write(((FederObject) body1).generateInCompile());
	 * compileFile.write("\n"); body1.setHasToBuild(false); }
	 *
	 * if (!(body1 instanceof FederBody)) return;
	 *
	 * FederBody body0 = (FederBody) body1;
	 *
	 * if (body0.hasToBuild()) { if (!body0.returnCame)
	 * SyntaxTreeElement.generateEnding("", body0, false, null);
	 *
	 * if (body0 instanceof FederNamespace &&
	 * body0.getName().startsWith("c_intern")) {
	 * compileFile.write(body0.compile_file_text.toString()); } else if (body0
	 * instanceof FederNamespace) { mainmethod.append(body0.compile_file_text); }
	 * else if (body0 instanceof FederClass) { headerFile.write("\n");
	 * headerFile.write(body0.compile_file_text.toString());
	 * headerFile.write(body0.inFrontOfSyntax() + "};\n\n"); } else {
	 * compileFile.write(body0.compile_file_text.toString()); } }
	 *
	 * if (body0.hasToBuild()) { if (body0 instanceof FederNamespace &&
	 * body0.getName().startsWith("c_intern")) {
	 * compileFile.write(body0.inFrontOfSyntax() + "// } " + body0.getName() +
	 * "\n"); } else if (body0 instanceof FederNamespace) {
	 * mainmethod.append(body0.inFrontOfSyntax() + "// } " + body0.getName() +
	 * "\n"); } else if (body0 instanceof FederFunction) { // if (!body0.returnCame)
	 * compileFile.write(body0.inFrontOfSyntax() + "\treturn NULL;\n");
	 * compileFile.write(body0.inFrontOfSyntax().substring(1) + "}\n\n"); }
	 *
	 * if (body0 instanceof FederCompileGen) { compileFile.write(((FederCompileGen)
	 * body0).generateInCompile()); } }
	 *
	 * body0.setHasToBuild(false); } }
	 */

	/**
	 * Write the main code of the generated bodies to the files
	 * @param compileFile The compile file to write to
	 * @param headerFile The header file to write to
	 * @param body1 the binding, where the generate process should be started.
	 */
	public void writeToFile(BufferedWriter compileFile,
	                        BufferedWriter headerFile, FederBinding body1)
	throws IOException {

		if (body1 instanceof FederObject) {
			if (!body1.hasToBuild())
				return;

			compileFile.write(((FederObject) body1).generateInCompile());
			compileFile.write("\n");
			body1.setHasToBuild(false);
		}

		if (!(body1 instanceof FederBody))
			return;

		FederBody body0 = (FederBody) body1;

		if (body0.hasToBuild()) {
			if (!body0.returnCame)
				SyntaxTreeElementUtils.generateEnding("", body0, false, null);

			if (body0 instanceof FederNamespace && body0.getName().startsWith("c_intern")) {
				compileFile.write(body0.compile_file_text.toString());
			} else if (body0 instanceof FederNamespace) {
				mainmethod.append(body0.compile_file_text);
			} else if (body0 instanceof FederClass && !((FederClass) body0).isType()) {
				headerFile.write("\n");
				headerFile.write(body0.compile_file_text.toString());
				headerFile.write(body0.inFrontOfSyntax() + "};\n\n");
			} else if (!(body0 instanceof FederClass && ((FederClass) body0).isType())) {
				compileFile.write(body0.compile_file_text.toString());
			}
		}

		for (FederBinding binding : body0.getBindings()) {
			writeToFile(compileFile, headerFile, binding);
		}

		if (body0.hasToBuild()) {
			if (body0 instanceof FederNamespace && body0.getName().startsWith("c_intern")) {
				compileFile.write(body0.inFrontOfSyntax() + "// } " + body0.getName() + "\n");
			} else if (body0 instanceof FederNamespace) {
				mainmethod.append(body0.inFrontOfSyntax() + "// } " + body0.getName() + "\n");
			} else if (body0 instanceof FederFunction) {
				// if (!body0.returnCame)
				FederFunction ff = (FederFunction) body0;
				FederBinding fr = ff.getReturnType();
				if (fr == null)
					compileFile.write(body0.inFrontOfSyntax() + "\treturn;\n");
				else if ((fr instanceof FederClass && !((FederClass) fr).isType())
				         || (fr instanceof FederArguments))
					compileFile.write(body0.inFrontOfSyntax() + "\treturn NULL;\n");

				compileFile.write(body0.inFrontOfSyntax().substring(1) + "}\n\n");
			}

			if (body0 instanceof FederCompileGen) {
				compileFile.write(((FederCompileGen) body0).generateInCompile());
			}
		}

		body0.setHasToBuild(false);
	}

	/**
	 * This method tries to set the type of "args" to List, whenever possible
	 * @param name1 The name of compiled instance
	 */
	public void informInclude(String name1) {
		if (allowMain) {
			FederObject args_obj = (FederObject) main.getBinding(main, "args", false);
			if (args_obj.getResultType() == null) {
				FederClass list = (FederClass) main.getBinding(main, "List", false);
				if (list != null && list.getName().equals("List")) {
					args_obj.setTypeManual(list);
				}
			}
		}
	}
}
