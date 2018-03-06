package feder.types;

import java.util.LinkedList;
import java.util.List;

import feder.SyntaxTreeElement;

/**
 * @author Fionn Langhans
 * @ingroup compiler
 */
public class FederRule {
	/**
	 * rule is defined for operators like +,-,*,/,%,&,|, (...)
	 */
	public static final int RULE_OPERATOR = 0x0001;
	
	/**
	 * rule is defined for an assignment
	 * (not implemented [yet])
	 */
	public static final int RULE_ASSIGNMENT = 0x0002;
	
	/**
	 * rule is defined for types (like buildin ones)
	 */
	public static final int RULE_TYPE = 0x0004;

	// Reserved till 0x0080
	
	/**
	 * rule is defined as of a function
	 */
	public static final int RULE_FUNCTION = 0x0100;
	
	/**
	 * rule is defined as of a pattern
	 */
	public static final int RULE_PATTERN = 0x0200;
	
	/**
	 * rule is defined as a pattern for a buildin type
	 */
	public static final int RULE_BUILDIN = 0x0400;
	
	/**
	 * rule is defined as a pattern for buildin required
	 * operations
	 */
	public static final int RULE_STRUCT = 0x0800;

	/**
	 * The result binding this rule may return
	 */
	private FederBinding returnValue;
	
	/**
	 * The left value of the operator
	 */
	private FederBinding lvalue;
	
	/**
	 * The right type of the operator
	 */
	private FederBinding rvalue; 
	
	/**
	 * The left type description of the operator
	 */
	private String lvalue_desc = null;
	
	/**
	 * The right type description of the operator
	 */
	private String rvalue_desc = null;

	/**
	 * The left type of the operator
	 */
	private FederArguments op_function = null;
	
	/**
	 * integer created with the RULE_* (<- wildcard) constants
	 */
	private int rule;
	
	/**
	 * The operator, if the type of the rule 
	 * is RULE_BUILDIN the name of the buildin type
	 */
	private String operator = "";
	
	/**
	 * The pattern to apply
	 */
	private String toApply = "";
	
	/**
	 * 
	 * @param rule0
	 * @param operator0
	 * @param toApply0
	 * @param returnValue0
	 * @param lvalue0
	 * @param rvalue0
	 */
	public FederRule (int rule0, String operator0, String toApply0,
	                  FederBinding returnValue0,
	                  FederBinding lvalue0, FederBinding rvalue0,
	                  String lvalue0_desc, String rvalue0_desc) {
		rule = rule0;
		returnValue = returnValue0;
		lvalue = lvalue0;
		rvalue = rvalue0;
		
		lvalue_desc = lvalue0_desc;
		rvalue_desc = rvalue0_desc;
		
		if ((rule & RULE_ASSIGNMENT) != 0) {
			operator = "=";
		} else if ((rule & RULE_OPERATOR) != 0) {
			operator = operator0;
		}
		
		toApply = toApply0;
	}

	/**
	 * 
	 * @param rule0
	 * @param buildin_name
	 * @param toApply0
	 * @param result
	 */
	public FederRule (int rule0, String buildin_name, String toApply0,
	                  FederBinding result) {
		rule = rule0;
		operator = buildin_name;
		toApply = toApply0;
		returnValue = result;
	}
	
	/**
	 * 
	 * @param rule0
	 * @param operator0
	 * @param func
	 */
	public FederRule (int rule0, String operator0, FederArguments func) {
		rule = rule0;
		returnValue = func.getReturnType();
		
		if (!func.canBeCalled()) {
			throw new RuntimeException("You can't use an interface definition!");
		}
		
		if (!(func.getParent() instanceof FederClass) && func.getArguments().size() != 2 && func.getArguments().size() != 1) {
			throw new RuntimeException("The argument length must be equal to 2 or 1!");
		} else if (func.getParent() instanceof FederClass && func.getArguments().size() != 1 && func.getArguments().size() != 0) {
			throw new RuntimeException("The argument length of function from classes must be equal to 0 or 1");
		}
		
		op_function = func;
		
		if (func.getParent() instanceof FederClass) {
			lvalue = func.getParent();
			if (func.getArguments().size() == 1) {
				rvalue = func.getArguments().get(0);
			} else {
				rvalue_desc = "!void";
			}
		} else {
			lvalue = func.getArguments().get(0);
			if (func.getArguments().size() == 2) {
				rvalue = func.getArguments().get(1);
			} else {
				rvalue_desc = "!void";
			}
		}

		if ((rule & RULE_ASSIGNMENT) != 0) {
			operator = "=";
		} else if ((rule & RULE_OPERATOR) != 0) {
			operator = operator0;
		}
	}
	
	public FederRule (int rule0, String operator0, String toApply0) {
		rule = rule0;
		operator = operator0;
		toApply = toApply0;
	}

	/**
	 * @param buildin string|int|double|char|bool
	 * @return Returns true, if this rule is a build in rule
	 * ((rule & RULE_BUILDIN) == 0) and if the operator of this
	 * rule is equal to buildin
	 */
	public boolean isApplyable (String buildin) {
		if ((rule & RULE_BUILDIN) == 0 && (rule & RULE_STRUCT) == 0) {
			return false;
		}

		return operator.equals (buildin);
	}

	public String applyRule (FederBody currentBody,
			String buildin) {
		if ((rule & RULE_BUILDIN) == 0 && (rule & RULE_STRUCT) == 0) {
			throw new RuntimeException ("Compile err: Rule to apply not a buildin or structures!");
		}

		return toApply.replace ("{0}", buildin);
	}
	
	private static boolean checkIfApplyable (String value_desc,
			FederBinding value, FederBinding value0, boolean value0_nothing) {
		
		if (value_desc == null) {
			if (!(value == null && value0 == null)
					&& !FederBinding.areSameTypes(value, value0)) {
				return false;
			}
		} else if (value_desc.equals("!class") && !FederBinding.isClassType(value0)) {
			return false;
		} else if (value_desc.equals("!interface") && !FederBinding.isInterfaceType(value0)) {
			return false;
		} else if (value_desc.equals("!array") && !FederBinding.isArrayType(value0)) {
			return false;
		} else if (value_desc.equals("!datatype") && !FederBinding.isDataType(value0)) {
			return false;
		} else if (value_desc.equals("!void") && !value0_nothing) {
			return false;
		}
		
		return true;
	}
	
	/**
	 * @param operator0 the operator separating the two types
	 * @param lvalue0 type, which is on the left of the operator
	 * @param rvalue0 type, which is on the right of the operator
	 * @return Returns true, if this rule is applyable to a certain context
	 */
	public boolean isApplyable(String operator0,
	                           FederBinding lvalue0, FederBinding rvalue0,
	                           boolean lvalue0_nothing, boolean rvalue0_nothing) {

		if (!operator.equals(operator0)) {
			return false;
		}
		
		if (!checkIfApplyable(lvalue_desc, lvalue, lvalue0, lvalue0_nothing)) {
			return false;
		}
		
		if (!checkIfApplyable(rvalue_desc, rvalue, rvalue0, rvalue0_nothing)) {
			return false;
		}
		
		return true;
	}
	
	/**
	 * @return Returns the result of the rule
	 */
	public String applyRule (FederBody currentBody,
	                         String lstring, String rstring) {
		if ((rule & RULE_PATTERN) != 0) {
			String rstring0 = "(" + rstring + ")";
			String lstring0 = "(" + lstring + ")";
			String stage0 = toApply.replace("{0}", lstring0);
			String stage1 = stage0.replace("{1}", rstring0);
			/*String stage0 = toApply.replace("{0}", lstring);
			String stage1 = stage0.replace("{1}", rstring);*/
			return stage1;
		} else if ((rule & RULE_FUNCTION) != 0) {
			if (op_function.getParent() instanceof FederClass
					&& op_function.getArguments().size() == 1) {
				return "ptr_" + op_function.generateCName() + "("
						+ lstring + ", " + rstring + ")";
			} else if (op_function.getParent() instanceof FederClass
					&& op_function.getArguments().size() == 0) {
				return "ptr_" + op_function.generateCName() + "("
						+ lstring + ")";
			} else if (op_function.getArguments().size() == 1) {
				return op_function.generateCName() + "("
						+ lstring + ")";
			}

			return op_function.generateCName() + "(" + lstring + ", "
			                                   + rstring + ")";
		}
		
		return null;
	}
	
	/**
	 * @return Returns the left value from the operator
	 */
	public FederBinding getLValue() {
		return lvalue;
	}
	
	/**
	 * @return Returns the right value from the operator
	 */
	public FederBinding getRValue() {
		return rvalue;
	}
	
	/**
	 * @return Returns the result of the operation/rule
	 */
	public FederBinding getResultValue() {
		return returnValue;
	}
	
	/**
	 * @return Returns the settings of this rule
	 * (composed out of the RULE_ constants in this class)
	 */
	public int getRule() {
		return rule;
	}
	
	/**
	 * @return Returns the operator, can be an 'roperator' or
	 * a string describing a buildin functionality, which is necessary
	 * do to some operations
	 */
	public String getOperator() {
		return operator;
	}
	
	public String getToApply() {
		return toApply;
	}
	
	private static final FederRule define_pattern (FederBody currentBody,
			List<String> args, SyntaxTreeElement ste) {
			
		String operator = args.get(2);
		String ruletoapply = args.get(3);
		
		if (args.size() < 6) {
			ste.indexToken = 0;
			throw new RuntimeException("Invalid rule definition."
			        + "'rule func [operator] \"[ruletoapply]\" [result_type] [ltype] [rtype]'"
					+ " or 'rule \"pattern\" [operator] \"[ruletoapply]\" [result_type] [ltype] [rtype]'.");
		}
		
		FederBinding returnValue = null;
		FederBinding ltype = null;
		FederBinding rtype = null;
		int turn = 0;
		
		String ltype_desc = null;
		String rtype_desc = null;
		
		for (int i = 4; i < args.size(); i++) {
			String stoken = args.get(i);
			//System.out.println(stoken);
			
			if (stoken.trim().startsWith("!")) {
				if (turn == 0) {
					throw new RuntimeException("No general description of the type allowd for the result type!");
				} else if (turn == 1) {
					ltype_desc = stoken;
					turn++;
					continue;
				} else if (turn == 2) {
					rtype_desc = stoken;
					turn++;
					continue;
				} else {
					throw new RuntimeException("Too many types: " + turn);
				}
			}
			
			String nextToken = (i+1 == args.size() ? "" : args.get(i+1));
			
			FederBinding current_type = null;
			if (turn == 0)
				current_type = returnValue;
			else if (turn == 1)
				current_type = ltype;
			else
				current_type = rtype;
			
			if (!stoken.equals("null") && !stoken.equals(".")) {
				if (current_type == null) {
					current_type = currentBody.getBinding(currentBody, stoken, true);
					if (current_type == null && !stoken.equals("null")) {
						ste.indexToken = i;
						throw new RuntimeException("'" + stoken + "' doesn't exist!");
					}
				} else {
					if (!(current_type instanceof FederBody)) {
						ste.indexToken = i;
						throw new RuntimeException("The current element '" + current_type.getName() + "' is not a body!");
					}
					
					current_type = ((FederBody) current_type).getBinding((FederBody) current_type, stoken, false);
					if (current_type == null && !stoken.equals("null")) {
						ste.indexToken = i;
						throw new RuntimeException("The current element '" + stoken + "' wasn't found by the compiler!");
					}
				}
			}
			
			if (turn == 0)
				returnValue = current_type;
			else if (turn == 1)
				ltype = current_type;
			else
				rtype = current_type;
			
			if ((!nextToken.equals(".") && !nextToken.isEmpty())
					|| nextToken.equals("null")) {
				if (turn > 2) {
					ste.indexToken = i+1;
					throw new RuntimeException("You can't mention a fourth type!");
				}

				turn++;
			}/* else if ((!nextToken.equals("") && !nextToken.equals("."))
					|| (nextToken.equals(".") && stoken.equals("null"))) {

				ste.indexToken = i+1;
				throw new RuntimeException("Invalid " + i + " element: " + args.get(i+1));
			}*/
		}
		
		if (turn == 0) {
			throw new RuntimeException("You have to mention a rvalue and rvalue, too.");
		}
		
		if (turn == 1) {
			throw new RuntimeException("You have to mention a lvalue after rvalue, too.");
		}
		
		int rule = 0;
		rule |= RULE_OPERATOR;
		rule |= RULE_PATTERN;
		
		return new FederRule(rule, operator, ruletoapply, returnValue,
				ltype, rtype, ltype_desc, rtype_desc);
	}
	
	private static final FederRule define_func(FederBody currentBody,
			List<String> args, SyntaxTreeElement ste) {
		String operator = args.get(2);
		
		List<FederBinding> arguments = new LinkedList<>();
		
		FederBinding binding = currentBody;
		for (int i = 3; i < args.size(); i++) {
			String arg = args.get(i);
			if (arg.equals(".")) {
				throw new RuntimeException("'.' not expected!");
			}

			if (binding == null) {
				throw new RuntimeException("The given binding is not a valid one!");
			}
			
			if (!(binding instanceof FederBody)) {
				throw new RuntimeException("The given instance should contain "
				                           + "bindings, but can't contain any"
				                           + " (isn't an body)");
			}
			
			if (i == 3) {
				binding = ((FederBody) binding).getBinding((FederBody) binding, arg, true);
			} else {
				binding = ((FederBody) binding).getBinding((FederBody) binding, arg, false);
			}
			
			/*if (binding instanceof FederClass) {
				throw new RuntimeException("Class can't be used in this context!");
			}*/
			
			String nextToken = (i+1 == args.size() ? "" : args.get(i+1));
			if (nextToken.equals(".")) {
				i++;
				continue;
			} else {
				arguments.add(binding);
				binding = currentBody;
			}
		}
		
		arguments.add(binding);
		binding = arguments.remove(0);
		
		if (binding == null) {
			throw new RuntimeException("The current binding isn't a valid one!");
		}
		
		FederArguments fun = null;
		if (binding instanceof FederFunction) {
			fun = (FederFunction) binding;
		} else if (binding instanceof FederObject) {
			FederObject obj = (FederObject) binding;
			if (!obj.isInterface()) {
				throw new RuntimeException("The given object's type should be an interface!");
			}
			
			if (!obj.isGlobal) {
				throw new RuntimeException("The given object must be a global one!");
			}
			
			fun = (FederArguments) obj.getResultType();
		} else {
			throw new RuntimeException("The given binding isn't allowed here!");
		}
		
		int rule = 0;
		rule |= RULE_OPERATOR;
		rule |= RULE_FUNCTION;
		return new FederRule(rule, operator, fun);
	}
	
	/**
	 * ::rule buildin [buildin_name] [pattern] [type]
	 * [buildin_name] = "int"|"double"|"string"|"char"
	 */
	private static final FederRule define_buildin (FederBody currentBody,
			List<String> args, SyntaxTreeElement ste) {

		if (args.size() < 5) {
			throw new RuntimeException("Expected: rule builin [buildin_name] [pattern] [type]");
		}

		String buildin_name = args.get(2);
		/*if (!buildin_name.equals("int")
				&& !buildin_name.equals("double")
				&& !buildin_name.equals("string")
				&& !buildin_name.equals("char")
				&& !buildin_name.equals("bool")) {
			throw new RuntimeException("buildin_name has to be int, double, string or char!");
		}*/

		String toApply = args.get(3);

		FederBinding binding = currentBody;
		for (int i = 4; i < args.size(); i++) {
			String arg = args.get(i);
			if (arg.equals(".")) {
				throw new RuntimeException("'.' not expected!");
			}

			if (binding == null) {
				throw new RuntimeException("The given binding is not a valid one!");
			}
			
			if (!(binding instanceof FederBody)) {
				throw new RuntimeException("The given instance should contain "
				                           + "bindings, but can't contain any"
				                           + " (isn't an body)");
			}
			
			if (i == 4) {
				binding = ((FederBody) binding).getBinding((FederBody) binding, arg, true);
			} else {
				binding = ((FederBody) binding).getBinding((FederBody) binding, arg, false);
			}
			
			/*if (binding instanceof FederClass) {
				throw new RuntimeException("Class can't be used in this context!");
			}*/
			
			String nextToken = (i+1 == args.size() ? "" : args.get(i+1));
			if (nextToken.equals(".")) {
				i++;
				continue;
			} else if (!nextToken.isEmpty()) {
				throw new RuntimeException("There shouldn't be anything else after '" + arg + "'.");
			}
		}
		
		/*if (binding == null) {
			throw new RuntimeException("The current binding isn't a valid one!");
		}*/

		int rule = 0;
		rule |= RULE_BUILDIN;
		rule |= RULE_PATTERN;

		return new FederRule (rule, buildin_name, toApply, binding);
	}
	
	private static final FederRule define_struct (FederBody currentBody,
			List<String> args, SyntaxTreeElement ste) {
		
		if (args.size() != 4) {
			throw new RuntimeException("def.: rule struct [buildin_name] [pattern]");
		}
		
		return new FederRule(RULE_STRUCT | RULE_PATTERN, args.get(2), args.get(3));
	}
	
	/**
	 * @param currentBody The current body to use
	 * @param args the arguments (te rule call)
	 * @param ste the syntax tree element to use
	 * @return Returns a new rule definition
	 * 
	 * @error Throws an error, if the rule call was invalid
	 */
	public static final FederRule define (FederBody currentBody,
			List<String> args, SyntaxTreeElement ste) {
		
		if (args.size() < 3) {
			throw new RuntimeException("Acceptable\n"
					+ "rule pattern operator ruletoapply resulttype left_type right_type\n"
					+ "rule func operator function\n"
					+ "rule buildin buildin_name pattern resulttype\n"
					+ "rule struct buildin_name pattern");
		}
		
		if(args.get(1).equals("pattern")) {
			return define_pattern(currentBody, args, ste);
		}
		
		if (args.get(1).equals("func")) {
			return define_func(currentBody, args, ste);
		}

		if (args.get(1).equals("buildin")) {
			return define_buildin(currentBody, args, ste);
		}
		
		if (args.get(1).equals("struct")) {
			return define_struct(currentBody, args, ste);
		}
		
		ste.indexToken = 1;
		throw new RuntimeException("Unexpected operation: " + args.get(1));
	}
}
