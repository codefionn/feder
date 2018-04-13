package feder;

import java.util.LinkedList;
import java.util.List;

/**
 * Class for syntax analysis
 * @author Fionn Langhans
 * @ingroup compiler
 */
public class Syntax {

	private Syntax() {
	}

	/**
	 * debug output (with one line 'ln')
	 * @param s The string, which should be printed
	 */
	public static void dboutln(String s) {
		System.out.println("# " + s);
	}

	/**
	 * @param c the compiler instance to use
	 * @param tokens The tokens, describing the elements in stringsOfTokens
	 * @param stringsOfTokens The values of tokens
	 * @param line the line, where the error happend
	 * @param index The index, where the error happend in 'tokens'
	 * @param msg The error message (a short error description)
	 * @return Returns the error message
	 */
	public static String error(FederCompiler c, List<String> tokens, List<String> stringsOfTokens, int line, int index,
	                           String msg) {
		StringBuilder result = new StringBuilder();

		result.append("File=" + c.getName() + ", Line=" + (line)).append("\n");

		/*
		 * for (int i = 0; i < tokens.size(); i++) { result.append(tokens.get(i) + " ");
		 * }
		 *
		 * result.append("\n");
		 */

		for (int i = 0; i < stringsOfTokens.size(); i++) {
			String s = stringsOfTokens.get(i);
			if (tokens.get(i).equals("string")) {
				result.append("\"");
			}

			result.append(s);

			if (tokens.get(i).equals("string")) {
				result.append("\"");
			}

			result.append(" ");
		}

		result.append("\n");

		for (int i = 0; i < stringsOfTokens.size() && i < index; i++) {
			for (int ii = 0; ii < stringsOfTokens.get(i).length() + 1; ii++) {
				result.append(" ");
			}

			if (tokens.get(i).equals("string"))
				result.append("  ");
		}

		result.append("^").append("\n");
		result.append(msg).append("\n");

		return result.toString();
	}

	/**
	 * Prints an error of the lexer
	 * @param lexer the lexer to use
	 * @param msg the error description
	 */
	public static void error(Lexer lexer, String msg) {
		/*
		 * int index = 0; int line = 0; while (index < lexer.tokens.size() && line !=
		 * lexer.tpm.line) { String token = lexer.getToken(index); if
		 * (token.equals("newline")) line++;
		 *
		 * index++; }
		 *
		 * System.err.println("Line="+(line+1));
		 *
		 * int oldindex = index;
		 *
		 * for (; index < lexer.softokens.size() &&
		 * !lexer.getToken(index).equals("newline"); index++) {
		 * System.err.print(lexer.getStringOfToken(index) + " "); }
		 * System.err.println();
		 *
		 * for (; oldindex < lexer.indexList; oldindex++) {
		 *
		 * for (int i = 0; i < lexer.getStringOfToken(oldindex).length(); i++) {
		 * System.err.print(' '); }
		 *
		 * System.err.print(' '); }
		 *
		 * System.err.println("^"); System.err.println(msg);
		 *
		 * try { Thread.sleep(100l); } catch (Exception ex) {}
		 */

		if (lexer.tokens.size() == 0) {
			System.err.println(msg);
			return;
		}

		int index = lexer.indexList;
		if (index >= lexer.tokens.size())
			index = lexer.tokens.size() - 1;

		if (lexer.tokens.get(index).equals("newline")) {
			index--;
		}

		int oldindex = index;

		if (index < 0)
			index = 0;

		List<String> tokens = new LinkedList<>();
		List<String> stringsOfTokens = new LinkedList<>();

		for (; index >= 0 && !lexer.tokens.get(index).equals("newline"); index--) {
			tokens.add(0, lexer.tokens.get(index));
			stringsOfTokens.add(0, lexer.softokens.get(index));
		}

		index += 1;

		System.err.println(error(lexer.compiler, tokens, stringsOfTokens, lexer.tpm.line, oldindex - index, msg));
	}

	/**
	 * Validate the syntax (with error catching)
	 * @param lexer the lexer to use, the elements of lexer have to be analyzed already
	 * by @link Lexer.lex lex @endlink
	 * @return Returns true, if all operations were successfully executed.
	 */
	public static boolean validateSyntax(Lexer lexer) {
		try {
			_validateSyntax(lexer);
		} catch (Exception ex) {
			error(lexer, ex.getMessage());
			return false;
		}

		return true;
	}

	/**
	 * Validate the syntax
	 * @param lexer The lexer to use, the elements of the lexer have
	 * to be analyzed already by @link Lexer.lex lex @endlink
	 */
	private static void _validateSyntax(Lexer lexer) {
		lexer.indexList = 0;
		lexer.tpm.line = 0;

		while (lexer.indexList < lexer.tokens.size()) {
			String token = lexer.getToken();

			if (token.equals("func") || token.equals("namespace") || token.equals("class")
			        || token.equals("interface")) {
				lexer.indexList++;
				lexer.getTokenWhitelist("name");
				lexer.indexList++;

				if (token.equals("func") || token.equals("interface")) {
					lexer.getTokenWhitelist("newline", "(");
				} else if (token.equals("class")) {
					lexer.getTokenWhitelist("newline", "name");

					while (lexer.getToken().equals("name")) {
						lexer.indexList++;
						lexer.getTokenWhitelist(".", "newline");
						if (lexer.getToken().equals("newline"))
							break;

						lexer.indexList++;
					}
				} else {
					lexer.getTokenWhitelist("newline");
				}
			} else if (token.equals("newline")) {
				lexer.tpm.line++;

				lexer.indexList++;
				if (lexer.indexList < lexer.tokens.size()) {
					lexer.getTokenWhitelist("(", "func", "class", "namespace", "newline", "name", "command",
					                        "nativelang", ";", "return", "include", "import", "if", "else", "while", "for", "interface",
					                        "continue", "break", "global", "type", "append", "rule", "roperator");
				}
			} else if (token.equals("=")) {
				lexer.indexList++;
				lexer.getTokenWhitelist("name", "(", "int", "double", "string", "char", "true", "false", "null", "len", "append");
			} else if (token.equals("name")) {
				lexer.indexList++;
				String token0 = lexer.getTokenWhitelist(".", "name", "=", "func", "(", ")", "newline", "!", "interface",
				                                        ",", "from", "==", "!=", "[]", "[", "]", "roperator", "&&", "||");
				if (token0.equals("[]")) {
					lexer.indexList++;
					if (lexer.getTokenWhitelist("name", "from", "func", "interface").equals("name")) {
						lexer.indexList++;
						lexer.getTokenWhitelist("=", "newline", ",", ")");
					}
				} else if (token0.equals("name")) {
					lexer.indexList++;
					lexer.getTokenWhitelist("=", ",", ")", "newline");
				}
			} else if (token.equals("while") || token.equals("if") || token.equals("for")) {
				lexer.indexList++;
				lexer.getTokenWhitelist("name", "true", "false", "roperator", "(", "len", "append");
			} else if (token.equals("nativelang") || token.equals("command")) {
				lexer.indexList++;
				lexer.getTokenWhitelist("newline");
			} else if (token.equals("(")) {
				lexer.indexList++;
				lexer.getTokenWhitelist("roperator", "name", "(", ")", "true",
				                        "false", "null", "string", "char",
				                        "int", "double", "append", "len");
			} else if (token.equals(")")) {
				lexer.indexList++;
				lexer.getTokenWhitelist(".", "newline", ")", ",", "(", "||",
				                        "&&", "!=", "==", "[", "]",
				                        "roperator");
			} else if (token.equals(";")) {
				lexer.indexList++;
				lexer.getTokenWhitelist("newline");
			} else if (token.equals(".")) {
				lexer.indexList++;
				lexer.getTokenWhitelist("name");
			} else if (token.equals("return")) {
				lexer.indexList++;
				lexer.getTokenWhitelist("(", "int", "double", "string", "char",
				                        "name", "null", "true", "false",
				                        "newline", "roperator");
			} else if (token.equals("null")) {
				lexer.indexList++;
				lexer.getTokenWhitelist("newline", ")", ",", "&&", "||", "roperator");
			} else if (token.equals("import")) {
				lexer.indexList++;
				lexer.getTokenWhitelist("string");
				lexer.indexList++;
				lexer.getTokenWhitelist("newline");
			} else if (token.equals("include")) {
				lexer.indexList++;
				lexer.getTokenWhitelist("string");
				lexer.indexList++;
				lexer.getTokenWhitelist("newline");
			} else if (token.equals("else")) {
				lexer.indexList++;
				lexer.getTokenWhitelist("newline", "if");
			} else if (token.equals("from")) {
				lexer.indexList++;
				lexer.getTokenWhitelist("name", "int", "float", "double",
				                        "string", "char", "true", "false",
				                        "(", "null", "append", "len");
			}/* else if (token.equals("==") || token.equals("!=")) {
				lexer.indexList++;
				lexer.getTokenWhitelist("name", "!", "(", "null", "int", "double", "roperator");
			}*/ else if (token.equals("global")) {
				lexer.indexList++;
				lexer.getTokenWhitelist("name");
			} else if (token.equals("type")) {
				lexer.indexList++;
				lexer.getTokenWhitelist("string");
				lexer.indexList++;
				lexer.getTokenWhitelist("name");
				lexer.indexList++;
				lexer.getTokenWhitelist("newline");
				lexer.indexList--;
			} else if (token.equals("[")) {
				lexer.indexList++;
			} else if (token.equals("len") || token.equals("append")) {
				lexer.indexList++;
				lexer.getTokenWhitelist("(");
			} else if (token.equals("rule")) {
				lexer.indexList++;
				lexer.getTokenWhitelist("string");
				lexer.indexList += 2;
				lexer.getTokenWhitelist("string");
				lexer.indexList++;

				int turn = 0;
				for (; lexer.indexList < lexer.tokens.size(); lexer.indexList++) {
					String token0 = lexer.getTokenWhitelist("name", "null");

					lexer.indexList++;
					if (lexer.tokens.size() <= lexer.indexList) {
						throw new RuntimeException("Awaited some more tokens!");
					}

					token0 = lexer.getTokenWhitelist(".", "name", "newline", "null");
					if (token0.equals("newline"))
						break;

					if (token0.equals("name")) {
						if (turn > 2) {
							throw new RuntimeException("You can't mention a fourth value!");
						}

						lexer.indexList--;
						turn++;
					} else if (token0.equals("null")) {
						if (turn > 2) {
							throw new RuntimeException("You can't mention a fourth value!");
						}

						lexer.indexList--;
						turn++;
						continue;
					}
				}

				if (turn != 2) {
					throw new RuntimeException("You have to mention a second value (left value): " + turn);
				}

				lexer.indexList--;
			} else {
				lexer.indexList++;
			}
		}
	}
}
