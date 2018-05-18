/*
 * Copyright (C) 2018 Fionn Langhans
 *
 *  ______       _
 * |  ____|     | |
 * | |__ ___  __| | ___ _ __
 * |  __/ _ \/ _` |/ _ \ '__|
 * | | |  __/ (_| |  __/ |
 * |_|  \___|\__,_|\___|_|
 *   A programming language
 */
package feder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

import feder.types.FederNamespace;

/**
 * Class for the main method
 *
 * @author Fionn Langhans
 * @ingroup compiler
 */
public class Feder {
	/**
	 * @mainpage Describing jfederc
	 * jfederc is a compiler (a parser), which parses Feder source code to
	 * C code. The compiler is a bit messy because it is the first compiler
	 * written by me (the programmer and creator of Feder).
	 *
	 * You probably should start reading in @link Feder Feder @endlink, if
	 * you want to modify the compiler. The 'heart' of the project is
	 * @link SyntaxTreeElement SyntaxTreeElement @endlink, there is much
	 * processing done. A small part of its functionality is in
	 * @link SyntaxTreeElementUtils SyntaxTreeElementUtils @endlink.
	 *
	 * If you want to modify the behavior of certain types or structures
	 * analyze @link feder.types.FederClass FederClass @endlink,
	 * @link feder.types.FederInterface FederInterface @endlink,
	 * @link feder.types.FederFunction FederFunction @endlink,
	 * @link feder.types.FederArray FederArray @endlink
	 * and @link feder.types.FederObject FederObject @endlink.
	 *
	 * If your aim is to hack a bit and create new operators give the
	 * @link Syntax syntax analysis @endlink,
	 * @link Lexer lexical anaylsis @endlink,
	 * @link SyntaxTreeElement.compile compile @endlink a try.
	 *
	 * Currently the compiler supports:
	 *
	 *   - POSIX (like BSD, Linux)
	 *   - WINDOWS
	 *
	 * Current development aims:
	 *
	 *   - Bugfixes
	 *   - Comment the code
	 *   - Give more and more competences to the library files written in Feder
	 *
	 * How the compiler works:
	 *
	 *   - The given files (as arguments) are read and processed by
	 *     FederCompiler and there with Lexer and Syntax (lexical
	 *     and syntax analysis)
	 *   - FederCompiler creates
	 *     @link SyntaxTreeElement SyntaxTreeElements @endlink, which then
	 *     parse the Feder source to C with the help of the library files
	 *     in feder.types
	 *
	 * Several points describing how the compiler reads the source code:
	 *   - This compiler is a top-down compiler (meaning method and
	 *     again methods are called to process the source code)
	 *   - For operator (e.g. arithmetic operators) a operator-precedence
	 *     parse operation is used (in SyntaxTreeElementUtils)
	 *   - The compiler reads all lines from the left to the right
	 */

	/**
	 * @defgroup compiler Compiler
	 * Contains compiler utilities
	 */

	/**
	 * @defgroup utils Utilities
	 * Random utilities
	 */

	/**
	 * @defgroup types Types
	 * Describing types objects can have.
	 * These are:
	 * 	- Arrays
	 * 	- Classes
	 * 	- datatypes
	 * 	- interfaces
	 */

	private Feder() {
	}

	/**
	 * This thing should describe the filename separator, which
	 * should be used by this program
	 */
	public static String separator = "/";

	/**
	 * The suffix/file type of object files
	 */
	public static String objEnd = "o";

	/**
	 * Current version of this program
	 */
	public static String VERSION = "1.1";

	/**
	 * This method processes the arguments given to the program
	 * and calls other functions to compile files given with the
	 * arguments
	 */
	public static final void main(String[] args) {
		if (args.length == 0) {
			/*
			 * No arguments given to the program
			 * => print help and exit (with error code)
			 */

			System.err.println("No operation given!");
			printHelp();
			System.exit(1);
			return;
		}

		/*
		 * The following are some objects, which can be modified in order to manipulate
		 * the compile process
		 */

		// This list contains the files, which should be compiled
		List<String> filestocompile = new LinkedList<>();
		// This list should contain some directories, which may be necassary to
		// satisfy the dependencies of the 'include' operator
		List<String> includeDirs = new LinkedList<>();

		List<String> linkerOptions = new LinkedList<>();
		List<String> progOptions = new LinkedList<>();
		String buildDir = "./build";
		String progCC = "cc";
		String progName = null;
		boolean debug = false;
		boolean allowCCcompile = true;
		boolean usewincl = false;
		boolean nomultithread = false;
		boolean printCommands = false;

		/*
		 * Set the default file separator used to the one of the operating system
		 */
		separator = File.separator;

		/*
		 * Currently two systems:
		 * - POSIX
		 * - WINDOWS
		 */
		String systemName = "POSIX";

		/*
		 * The following condition clauses analyse the
		 * current operating system name and try to figure out
		 * which library model should be used
		 */

		String osName = System.getProperty("os.name").toLowerCase();
		if (osName.contains("mac") || osName.contains("linux")) {
			systemName = "POSIX";
		} else if (osName.contains("windows")) {
			systemName = "WINDOWS";
		} else {
			System.err.println ("WARNING: System not recognized, using POSIX library!");
		}

		// System.out.println ("System: " + systemName);

		/*
		 * The following conditioned loop checks given arguments and manipulates the
		 * objects above
		 */

		String await = null;
		for (String arg : args) {
			if (arg.equals("-H") || arg.equals("--help")) {
				printHelp();
				System.exit(1);
				return;
			}

			boolean islongoption = arg.startsWith("--");
			boolean isshortoption = arg.startsWith("-");

			// option is not null, if either of islongoption or
			// isshortoption is true
			String option = null;
			if (islongoption)
				option = arg.substring(2);
			else if (isshortoption)
				option = arg.substring(1);

			if (await != null) {
				islongoption = false;
				isshortoption = false;
			}

			if (await == null && option == null) {
				filestocompile.add(arg);
				continue;
			}

			if (await != null && await.equals("builddir")) {
				buildDir = arg;
			} else if (await != null && await.equals("includedir")) {
				if (!arg.endsWith("/"))
					arg = arg + "/";
				includeDirs.add(arg);
			} else if (await != null && await.equals("output")) {
				progName = arg;
			}  else if (await != null && await.equals("toolchain")) {
				progCC = arg;
			} else if (await != null && await.equals ("separator")) {
				separator = arg;
			} else if (await != null && await.equals("coption")) {
				progOptions.add(arg);
			} else if (await != null && await.equals("loption")) {
				linkerOptions.add(arg);
			} else if (await != null && await.equals("system")) {
				systemName = arg;

			} else if ((islongoption && option.equals("builddir")) ||
			           (isshortoption && option.equals("D"))) {
				await = "builddir";
				continue;
			} else if ((islongoption && option.equals("include")) ||
			           (isshortoption && option.equals("I"))) {
				await = "includedir";
				continue;
			} else if ((islongoption && option.equals("output")) ||
			           (isshortoption && option.equals("O"))) {
				await = "output";
				continue;
			} else if (islongoption && option.equals("debug")) {
				debug = true;
				continue;
			} else if ((isshortoption && option.equals ("T")) ||
			           (islongoption && option.equals ("toolchain"))) {
				await = "toolchain";
				continue;
			} else if ((isshortoption && option.equals("W")) ||
			           (islongoption && option.equals ("usewinop"))) {
				usewincl = true;
				continue;
			} else if (islongoption && option.equals ("separator")) {
				await = "separator";
				continue;
			} else if (islongoption && option.equals ("coption")) {
				await = "coption";
				continue;
			} else if (islongoption && option.equals("loption")) {
				await = "loption";
				continue;
			} else if (islongoption && option.equals("system")) {
				await = "system";
				continue;
			} else if ((isshortoption && option.equals("v")) ||
			           (islongoption && option.equals("version"))) {
				System.out.println (VERSION);
				return;
			} else if (islongoption && option.equals("printbuild")) {
				printCommands = true;
				continue;
			}

			else {
				System.err.println("Invalid option \"" + arg + "\"!");
				System.exit (1);
				return;
			}

			await = null;
		}

		/*
		 * The "build directory" path has to have a specific format: - it must end with
		 * '/' - and '/' is being replaced by the operating system specific seperator
		 */

		if (buildDir.trim().isEmpty()) {
			buildDir = "./";
		} else if (!buildDir.endsWith("/")) {
			buildDir = buildDir + "/";
		}

		/**
		 * If the windows cl compiler options should be used,
		 * tell this program to use "obj" as a suffix for object
		 * files
		 */

		if (usewincl) {
			objEnd = "obj";
		}

		if (!buildDir.startsWith("/")
		        && !(buildDir.length() >= 2 && buildDir.startsWith(":", 1))) {
			buildDir = "./" + buildDir;
		}

		// This operation replaces the filename sepeator used on default by this
		// program ('/', the UNIX one) with one of the current operating system
		buildDir = buildDir.replace("/", Feder.separator);

		// Checks if there were any files given in the arguments, which should be
		// compiled
		if (filestocompile.size() == 0) {
			printHelp();
			System.exit(1);
			return;
		}

		/**
		 * Guess the program name from the file name
		 */
		if (progName == null) {
			progName = filestocompile.get(0);
			if (progName.endsWith(".fd"))
				progName = progName.substring(0, progName.length()-3);

			String separator0 = separator;
			if (separator0.equals ("\\"))
				separator0 += "\\";

			String[] parts = progName.split("/");
			String[] parts0 = parts[parts.length-1].split(separator0);
			progName = parts0[parts0.length-1];
		}

		/*
		 * The following calls some methods, which compile Feders source code
		 */
		List<FederCompiler> compilers = new LinkedList<>();
		boolean error = false;

		// This for loop runs through all the files, which have to be compiled
		// and converts the Feder code to C source code.
		for (String filetocompile : filestocompile) {
			FederCompiler compiler = compile(buildDir, filetocompile,
			                                 includeDirs, debug, systemName,
			                                 progCC, usewincl, nomultithread,
			                                 printCommands,
			                                 progOptions);
			if (compiler == null) {
				error = true;
				continue;
			}

			linkerOptions.addAll(compiler.linkerOptions);
			compilers.add(compiler);
		}

		if (error) {
			// One or more files could not be compiled
			System.err.println("- Could not complete build process");
			System.exit(1);
			return;
		}

		if (allowCCcompile) {
			/*
			 * The program must compile the resulting C code to machine code.
			 * This is being done with an CC compiler.
			 * This compiler has to be present.
			 */

			// Collect all object files
			List<String> objFiles = new LinkedList<>();
			for (FederCompiler c : compilers) {
				addObjectFiles(c, objFiles);
			}

			// Create a command for the system cli (command line interface)
			List<String> cmd = new LinkedList<>();
			if (usewincl)
				cmd.add("LINK"); // Use The Developer Console from Visual Studio !!!
			else
				cmd.add(progCC);

			if (!usewincl) {
				cmd.add("-pipe");
			}

			for (String o : objFiles) {
				cmd.add(o);
			}

			if (usewincl) {
				// Add default libraries to the linker
				// These are used by Visual Studio at default
				String[] libs = ("kernel32.lib;user32.lib;gdi32.lib;" +
				                 "winspool.lib;comdlg32.lib;advapi32.lib;" +
				                 "shell32.lib;ole32.lib;oleaut32.lib;uuid.lib;" +
				                 "odbc32.lib;odbccp32.lib;").split(";");
				for (String lib : libs) {
					cmd.add("/DEFAULTLIB:" + lib);
				}
			}

			/*if (!usewincl)
			cmd.addAll (compilers.get(0).progCCOptions);*/

			// The output name of the program

			if (usewincl)
				cmd.add("\"/OUT:" + progName + ".exe\"");
			else {
				cmd.add("-o"); // --output
				cmd.add(progName);
			}

			// Add the options, which where given as arguments to the program, to the linker
			cmd.addAll(linkerOptions); // When putting this in the beginning of
			// arguments, this could cause an
			// bug on gcc used by Ubuntu 16.04 LTS

			if (printCommands) {
				// print the compile command, if the user wants to
				for (String s : cmd) {
					System.out.print (s + " ");
				}
				System.out.println ();
			}

			try {
				/*
				 * Create a process, which's working directory is
				 * in the build directory
				 */
				Process p = new ProcessBuilder(cmd).inheritIO().directory(new File(buildDir)).start();
				if (p.waitFor() != 0) {
					System.err.println("- Could not complete build process: CC compiler failed linking");
					System.exit(1);
					return;
				}
			} catch (IOException | InterruptedException e) {
				System.err.println("- Could not complete build process: Command I/O");
				System.exit(1);
				return;
			}
		}

		// Successfully exit
	}

	private static void addObjectFiles(FederCompiler c, List<String> objFiles) {
		if (!objFiles.contains(c.getNameFileObjectOnly())) {
			objFiles.add(c.getNameFileObjectOnly());
		}

		for (FederCompiler c0 : c.included) {
			addObjectFiles(c0, objFiles);
		}
	}

	/**
	 * @param buildDir The directory, where the compiled files
	 * should be created (*.h, *.c, *.o)
	 * @param filetocompile The file, which should be compiled
	 * @param includeDirs Directories, where files will be search, which were
	 * requested by the 'include' keyword
	 * @param debug True, if the created program should print
	 * excessive informations
	 * @param system The target system (Windows, POSIX)
	 * @param progCC The Compiler name
	 * @param usewincl Use the Visual Studio Compiler options
	 * @param nomultithread This value is made for future versions.
	 * @param options Options, which should be added, when compiling a file with
	 * the C compiler
	 */
	private static FederCompiler compile(String buildDir, String filetocompile,
	                                     List<String> includeDirs,
	                                     boolean debug, String system,
	                                     String progCC, boolean usewincl,
	                                     boolean nomultithread,
	                                     boolean printCommands,
	                                     List<String> options) {
		FederCompiler compiler = new FederCompiler(filetocompile, debug, system,
		        progCC, usewincl,
		        nomultithread, printCommands);
		compiler.includeDirs.addAll(includeDirs);
		compiler.buildDir = buildDir;
		compiler.progCCOptions.addAll (options);

		String text = null;
		try {
			text = getStringFromFile(filetocompile);
		} catch (Exception ex) {
			System.err.println("- " + ex.getMessage());
			return null;
		}

		// System.out.println("+ Starting to compile " + filetocompile);

		// Compile!
		FederNamespace fn = compiler.compile(text);
		if (fn == null)
			return null;

		// System.out.println("+ Compiler has finished processing " + filetocompile);

		return compiler;
	}

	/**
	 * This option simply prints help, if the user requested it
	 * or if the user needs some help
	 */
	private static void printHelp() {
		System.out.println("# jfederc - The Java Feder compiler created by Fionn Langhans");
		System.out.println("# Made for MS Windows & POSIX systems (e.g. Linux, BSD)");
		System.out.println();
		System.out.println("jfederc ([options]) [feder-files]:");
		System.out.println("--version | -v                  " + "Prints the current version");
		System.out.println("--include | -I [directory]      " + "Add an directory to include path");
		System.out.println("--builddir | -D [directory]     " + "Set the build directory");
		System.out.println("--output | -O [path]            "
		                   + "Sets the output path of the program (Relative to build path)");
		System.out.println("--debug                         "
		                   + "May help debugging the compiled program");
		System.out.println("-T | --toolchain [toolchain]    "
		                   + "Set the C compiler (Default is 'cc')");
		System.out.println("-W | --usewinop                 "
		                   + "Using the option applyable to the Visual Studio compiler");
		System.out.println("--coption [option]              "
		                   + "Option for the C compiler");
		System.out.println("--loption [option]              "
		                   + "Option for the linker");
		System.out.println("--printbuild                    "
		                   + "Prints what commands are started");
	}

	/**
	 * @param file The file, which should be read
	 * @return Returns the contents of file. Throws an error if an IOException
	 * or anything like that occured
	 */
	public static String getStringFromFile(File file) {
		if (!file.exists())
			throw new RuntimeException("\"" + file.getPath() + "\" doesn't exist!");
		if (!file.isFile())
			throw new RuntimeException("\"" + file.getPath() + "\" is not a file!");

		String result = null;

		try {
			FileInputStream fis = new FileInputStream(file);
			byte[] bytes = new byte[fis.available()];
			fis.read(bytes);
			fis.close();

			result = new String(bytes, "UTF-8");
		} catch (IOException e) {
			throw new RuntimeException(e.getLocalizedMessage());
		}

		return result;
	}

	/**
	 * @param filen A path, which should point to a file
	 * @return Returns the contents of the file described by filen
	 */
	public static String getStringFromFile(String filen) {
		File file = new File(filen.replace ("/", Feder.separator));
		return getStringFromFile(file);
	}
}
