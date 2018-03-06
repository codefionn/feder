package feder.types;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import feder.FederCompiler;

/**
 * 
 * @author Fionn Langhans
 * @ingroup utils
 */
public abstract class FederBody extends FederBinding
{
	/**
	 * Name of the binding
	 * (basically THE binding)
	 */
	private String name;
	
	/**
	 * Children of this binding
	 */
	private List<FederBinding> bindings;
	
	/**
	 * Parent of this binding
	 */
	private FederBody parent;

	/**
	 * Blacklisted components
	 * (necessary to prevent certain structures from 
	 * being a child of this structure)
	 */
	protected Class<? extends FederBinding>[] blacklist_classes;

	/**
	 * Text, which may come in the compile file
	 * (another term is: main file text, so where
	 * the most text is generated in many cases)
	 */
	public StringBuilder compile_file_text = new StringBuilder();
	
	/**
	 *  Text, which should be generated at the start of a
	 *  header file (not really supported)
	 */
	public StringBuilder header_file_text_beg = new StringBuilder();
	
	/**
	 * Text, which should be generated somewhere (in build order) in
	 * the header file (not really supported)
	 */
	public StringBuilder header_file_text = new StringBuilder();
	
	/**
	 * Text, which should be generated at the end of a header
	 * file (not really supported)
	 */
	public StringBuilder header_file_text_end = new StringBuilder();

	/**
	 * Map, which describes, which types are assumed
	 */
	private Map<FederBinding, FederBinding> assume = new HashMap<>();

	/**
	 * true if a 'return' operator came
	 * (which optionally can result in less code)
	 */
	public boolean returnCame = false;

	/**
	 * true if the body has been build already
	 */
	private boolean build = true;

	/**
	 * The compiler instance to use
	 */
	private FederCompiler compiler;

	/**
	 * Well, for hacking and easy type dependencies necessary
	 * (for example with 'include')
	 */
	public List<FederBody> dependBodies = new LinkedList<>();

	@Override
	/**
	 * @return Returns true, if the body has to be build
	 */
	public boolean hasToBuild()
	{
		return build;
	}

	@Override
	/**
	 * @return Set if the compiler has to build the body
	 * (typically you only want to set 'false' here)
	 */
	public void setHasToBuild(boolean b)
	{
		build = b;
	}

	/**
	 * Assume a new type for 'key' (which is value)
	 * @param key
	 * @param value
	 */
	private static void assumeType (FederBinding key, FederBinding value)
	{
		if (key instanceof FederArguments) {
			((FederFunction) key).setReturnType(value);
		} else if (key instanceof FederObject) {
			((FederObject) key).setTypeManual(value);
		}
	}

	/**
	 * Assume a new type for 'key', which is 'value'
	 * @param key Interface|Function|Object
	 * @param value
	 */
	public void newAssume (FederBinding key, FederBinding value)
	{
		FederBinding value0;
		if (assume.containsKey(key)) {
			value0 = assume.remove(key);
		} else {
			value0 = value;
		}

		assume.put(key, value0);
	}

	/**
	 * Reset all assumed keys/bindings
	 */
	public void resetAssume ()
	{
		assume.forEach(new BiConsumer<FederBinding, FederBinding> () {

			@Override
			public void accept(FederBinding key, FederBinding value) {
				assumeType(key, value);
			}
		});

		assume.clear();
	}

	@SuppressWarnings("unchecked")
	/**
	 * @param compiler0 compiler instance to use
	 * @param name0 The name of the body (THE binding)
	 * @param parent0 parent of the body
	 * @param blacklist_classes0 blacklisted classes
	 */
	public FederBody(FederCompiler compiler0, String name0, FederBody parent0,
	                 Class<? extends FederBinding>... blacklist_classes0)
	{
		compiler = compiler0;
		name = name0;
		bindings = new LinkedList<>();
		parent = parent0;

		if ((parent != null || compiler != null) && name != null) {
			if (compiler != null)
				compiler.buildOrder.add(this);
			else
				parent.getMainNamespace().getCompiler().buildOrder.add(this);
		}

		blacklist_classes = blacklist_classes0;
	}

	/**
	 * @return Returns the used compiler instance
	 */
	public FederCompiler getCompiler()
	{
		return compiler;
	}

	/**
	 * @return Returns all children
	 */
	public List<FederBinding> getBindings()
	{
		return bindings;
	}

	/**
	 * Add a child to the body
	 * @param binding The child (optionally has this as parent)
	 */
	public void addBinding(FederBinding binding)
	{
		if (bindings.contains(binding)) {
			throw new RuntimeException("Binding already exist in body \"" + getName() + "\"!");
		}

		for (Class<? extends FederBinding> c : blacklist_classes) {
			if (c.isInstance(binding)) {
				throw new RuntimeException("Blacklisted class: " + c.getName());
			}
		}

		bindings.add(binding);
	}

	/**
	 * Add a binding (as child) to a specific position
	 * @param binding The child (optionally has this as parent)
	 * @param pos The position/index
	 */
	public void insertBinding(FederBinding binding, int pos)
	{
		if (bindings.contains(binding)) {
			throw new RuntimeException("Binding already exist in body \"" + getName() + "\"!");
		}

		for (Class<? extends FederBinding> c : blacklist_classes) {
			if (c.isInstance(binding)) {
				throw new RuntimeException("Blacklisted class: " + c.getName());
			}
		}

		bindings.add(pos, binding);
	}
	
	/**
	 * @return Returns a code friendly name of this class
	 * (necessary to do some stuff generateCName can't do).
	 * Returns at default the result of
	 * @link FederBinding.getName FederBinding.getName() @endlink.
	 */
	public String getCodeFriendlyName () {
		return getName();
	}

	/**
	 * @param requestingbody The body which is requesting.
	 * @param name0 The name to search.
	 * @param allowParent Allow the method to search in parents.
	 * @return Returns a fitting binding. If nothing was found 'null'.
	 */
	public FederBinding getBinding (FederBody requestingbody, String name0, boolean allowParent)
	{
		return getBinding (requestingbody, name0, allowParent, new LinkedList<>());
	}

	/**
	 * @param bodiesChecked Race detection
	 * @return Returns a fitting binding. If nothing was found 'null'.
	 */
	protected FederBinding getBinding(FederBody requestingbody, String name0, boolean allowParent, List<FederBody> bodiesChecked)
	{
		for (FederBinding binding : getBindings()) {
			if (binding.getName().equals(name0)) {
				FederBody parent0 = binding.getParent();
				FederClass cinbody = parent0 == null ? null : parent0.getClassInBody();
				boolean ninsamefile = parent0 == null ? true : (requestingbody.getMainNamespace() != parent0.getMainNamespace());
				boolean ninsameclass = cinbody != null && cinbody != requestingbody.getClassInBody();
				if (binding.isPrivate()
				        && (ninsamefile || ninsameclass)) {
					continue;
				}

				return binding;
			}
		}

		if (!allowParent)
			return null;

		if (parent == null) {
			bodiesChecked.add (this);

			for (FederBody bodies : dependBodies) {
				if (bodiesChecked.contains (bodies))
					continue;

				FederBinding binding = bodies.getBinding (requestingbody, name0, allowParent, bodiesChecked);
				if (binding != null)
					return binding;
			}

			return null;
		}

		return parent.getBinding(requestingbody, name0, true, bodiesChecked);
	}

	@Override
	/**
	 * @return Returns the parent of this body
	 * (main body has no parent)
	 */
	public FederBody getParent()
	{
		return parent;
	}

	/**
	 * Set the parent of this body to 'parent0'
	 * @param parent0
	 */
	public void setParent (FederBody parent0)
	{
		parent = parent0;
	}

	@Override
	/**
	 * @return Returns the name of this body (THE binding)
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * @return Returns all upper namespaces
	 */
	public List<FederNamespace> getNamespaces()
	{
		List<FederNamespace> namespaces = new LinkedList<>();
		FederBody body = parent;
		while (body != null && !(body instanceof FederMainNamespace)) {
			if (body instanceof FederNamespace) {
				namespaces.add(0, (FederNamespace) body);
			}

			body = body.parent;
		}

		// We don't want the file to be a namespace here
		if (namespaces.size() > 0)
			namespaces.remove(0);

		return namespaces;
	}

	/**
	 * @return Returns all the upper namespaces as a string.
	 */
	public String getNamespacesToString()
	{
		StringBuilder result = new StringBuilder();
		List<FederNamespace> namespaces = getNamespaces();
		for (FederNamespace namespace : namespaces) {
			result.append(namespace.generateCName() + "_");
		}

		return result.toString();
	}
	
	/**
	 * @return Returns all upper namespaces seperated by a '.'
	 */
	public String getNamespacesToNormalString() {
		StringBuilder result = new StringBuilder();
		List<FederNamespace> namespaces = getNamespaces();
		for (FederNamespace namespace : namespaces) {
			result.append(namespace.getName() + ".");
		}

		return result.toString();
	}
	
	/**
	 * @return Basically returns getNamespacesToNormalString + "." + getName
	 * if no upper namespaces just getName
	 */
	public String toWrittenString () {
		String s = getNamespacesToNormalString();
		if (s.isEmpty()) {
			return getName();
		} else {
			return s + "." + getName();
		}
	}

	/**
	 * @param requestingbody The body, which requests the binding
	 * @param list stringsOfTokens (values of tokens)
	 * @return Returns the bindings, which are described by list
	 * (the last one is the result of 'list')
	 */
	@Deprecated
	public List<FederBinding> getBindingFromList(FederBody requestingbody, List<String> list)
	{
		List<FederBinding> bindings0 = new LinkedList<>();

		boolean allowParents = true;
		FederBinding binding = this;
		for (String s : list) {
			if (binding instanceof FederObject) {
				FederClass fc = (FederClass) ((FederObject) binding).getResultType();
				if (fc == null) {
					throw new RuntimeException(
					    "\"" + binding.getName() + "\" is an object, but wasn't declared with a class");
				}

				binding = fc;
			}

			if (binding instanceof FederBody) {
				binding = ((FederBody) binding).getBinding(requestingbody, s, allowParents);

				if (binding == null) {
					throw new RuntimeException("\"" + s + "\" is not a valid binding!");
				}

				bindings0.add(binding);
			} else {
				throw new RuntimeException("\"" + s + "\" is doesn't contain any bindings!");
			}

			allowParents = false;
		}

		return bindings0;
	}

	/**
	 * @param requestingbody The body, which requests the function
	 * @param name1 The binding (name of the function)
	 * @param arguments The arguments the function must have
	 * @param allowParent Allows the method to search through parents of this body
	 * @return Returns a fitting function. Null if nothing was found.
	 */
	public FederArguments getFunction (FederBody requestingbody, String name1, List<FederBinding> arguments, boolean allowParent)
	{
		return getFunction (requestingbody, name1, arguments, allowParent, new LinkedList<>());
	}

	/**
	 * @param bodiesChecked race detection
	 */
	protected FederArguments getFunction(FederBody requestingbody, String name1, List<FederBinding> arguments, boolean allowParent, List<FederBody> bodiesChecked)
	{

		for (FederBinding binding : bindings) {
			if (binding instanceof FederArguments && ((FederArguments) binding).isEqual(name1, arguments)) {
				FederArguments args = (FederArguments) binding;
				FederBody body = (FederBody) args;
				FederClass cinbody = body.getClassInBody();
				boolean ninsamefile = requestingbody.getMainNamespace() != body.getMainNamespace();
				boolean ninsameclass = cinbody != null && cinbody != requestingbody.getClassInBody();
				// System.out.println(name1  + ": " + args.isPrivate() + ", " + ninsamefile + ", " + ninsameclass);

				if (args.isPrivate() &&
				        (ninsamefile || ninsameclass)) {
					continue;
				}

				return ((FederArguments) binding);
			}
		}

		if (!allowParent)
			return null;

		if (parent == null) {
			bodiesChecked.add (this);

			for (FederBody bodies : dependBodies) {
				if (bodiesChecked.contains (bodies))
					continue;

				FederArguments binding = bodies.getFunction (requestingbody, name1, arguments, allowParent, bodiesChecked);
				if (binding != null)
					return binding;
			}

			return null;
		}

		return parent.getFunction(requestingbody, name1, arguments, allowParent, bodiesChecked);
	}

	/**
	 * @return Returns the a class, when possible.
	 * Searches through the parents (and also checks this body)
	 */
	public FederClass getClassInBody()
	{
		return getClassInBinding(this);
	}

	/**
	 * @param binding the binding, where the class should be searched
	 * @return Checks if 'binding' is a class or one of its parents
	 */
	public static FederClass getClassInBinding (FederBinding binding)
	{
		if (binding instanceof FederClass) {
			return (FederClass) binding;
		}

		FederBody body = binding.getParent();
		while (body != null) {
			if (body instanceof FederClass) {
				return (FederClass) body;
			}

			body = body.getParent();
		}

		return null;
	}

	/**
	 * @return Returns the number of parents the binding has
	 */
	public int getParentsCount()
	{
		int count = 0;
		FederBody body = parent;
		while (body != null) {
			count++;
			body = body.parent;
		}

		return count;
	}

	/**
	 * @return Returns the indention in front of a line
	 */
	public String inFrontOfSyntax()
	{
		int countinfront = getParentsCount();
		StringBuilder sb = new StringBuilder();
		while (countinfront-- > 0) {
			sb.append("\t");
		}

		return sb.toString();
	}

	/**
	 * @return Returns a function, which can be this body
	 * or a parent of this body. Of none of the parents
	 * (or this body) is a function, this method returns null.
	 */
	public FederFunction getUpperFunction()
	{
		if (this instanceof FederFunction) {
			return (FederFunction) this;
		}

		if (!(this instanceof FederAutomat))
			return null;

		if (this.parent == null) {
			return null;
		}

		return parent.getUpperFunction();
	}

	/**
	 * @return Returns a namespace when possible.
	 * Checks if this body is a namespace and searches
	 * through the parents. Null if nothing has been
	 * found.
	 */
	public FederNamespace getUpperNamespace()
	{
		if (this instanceof FederNamespace) {
			return (FederNamespace) this;
		}

		if (!(this instanceof FederAutomat))
			return null;

		if (this.parent == null)
			return null;

		return parent.getUpperNamespace();
	}

	/**
	 * @return Returns true, if this body is in an
	 * 'for' or 'while' loop (Necassary for 'break'
	 * and 'continue').
	 */
	public boolean isInLoop()
	{
		FederBody fb = this;

		while (fb != null && fb instanceof FederAutomat) {
			FederAutomat fa = (FederAutomat) fb;
			if (fa.getType().equals("while") || fa.getType().equals("for")) {
				return true;
			}

			fb = fb.parent;
		}

		return false;
	}

	/**
	 * @return Returns true, if this body is a function
	 * or if any of its parents is a function.
	 */
	public boolean isInFunction ()
	{
		FederBody fb = this;
		while (fb != null && (fb instanceof FederAutomat)) {
			fb = fb.parent;
		}

		if (fb == null)
			return false;

		return fb instanceof FederFunction;
	}

	/**
	 * @return Returns true, if this body is a namespace
	 * or if any of its parents is a namespace.
	 */
	public boolean isInNamespace ()
	{
		return this instanceof FederNamespace;
	}

	/**
	 * @return Returns the 'first' body (which has no parent
	 * and was created by a compiler instance).
	 */
	public FederBody getMainBody ()
	{
		FederBody current = this;
		while (current.getParent () != null) {
			current = current.getParent ();
		}

		return current;
	}

	/**
	 * @return Returns the 'first' body (which is an instance
	 * of FederMainNamespace)
	 */
	public FederMainNamespace getMainNamespace ()
	{
		FederBody current = this;
		while (!(current instanceof FederMainNamespace)) {
			current = current.parent;
		}

		return (FederMainNamespace) current;
	}
}
