# Rules

Rules can do very much things in Feder. They are used for decisions, how the
compiler should generated the C source code or what should happend, if an
operator occurres in the source code. With Feder rules it is possible to allow
arithmetic operations in different ways. Rules can call existing functions or
describe certain patterns, which are directly generated to C source files.

## Rule 1: pattern

How to call the rule 'pattern' ?

```
::rule pattern [operator] [pattern] [result_type] [left_type] [right_type]
[operator]: Can be +,-,*,/,%,==,!=,!,+=,-=,/=,*= or anything like that
            Words are also possible, but have to be specified by the compiler
[pattern]:     A pattern (Should be between "", while \ is the escape character)
               {0} is replaced by the left side, {1} by the right one
[result_type]: Result type, can be null or a declared type in the source code.
               Can also be a generalisation starting with !. There are two
			   possible: !first, which would return the type of the left
			   type of the operator (in Feder source code), !second, which would
			   return the type of the right side from the operator (again, in
			   Feder source code).
[left_type]:   Can be null or a declared type in the source code. Can also
               be a generalisation starting with !. The possible ones are:
			   !all, accepts all types, !void, this side has to be nothing
			   at all (can be used for increment operators ++,--), !class,
			   the side's type has to be a class, !interface, the side's type
			   has to be an interface (callable one), !array, the side's type
			   has to be an array, !datatype, the side's type has to be a
			   datatype.
[right_type]:  Like left_type, just on the right side of the operator.
```

The purpose of the 'pattern' rule, is to provide a fast and easy way to
create operations, which require an operator and should be directly generated
into native language (C). For example: If you want to check if an object with
a class as type is 'null' and there is a function called 'fdIsNull' created in
the native language, which returns true, if the object is null and false, if it
is not null. The operator used is !=, the left side of the operator has to be
object with a class as type and the right side 'null'.

```
::rule pattern != "fdIsNull" bool !class null
```

## Rule 2: func

How to call the rule 'func' ?

```
::rule func [operator] [function] ( [arguments] )
[operator]: Just like in Rule 1: pattern
[function]: A function, which should be called. Must have
            at least one argument (or none if function in class) and at most
			2 arguments (or 1 if function in class).
[arguments]: (Optional) The arguments the called function should. The arguments
             should be represented just as their types (not names) and must be
			 seperated by space.
```

## Rule 3: buildin

Used for buildin patterns, which represent data like floating-point numbers,
integers, strings or bool\_ean.

How to call the rule 'buildin' ?

```
::rule buildin [operator] [pattern] [result_type]
[operator]: Should be a specific 'keyword', better explanation later
[patter]:       A pattern (Should be between "", escape character is \), where
                {0} can be replaced.
[result_type]:  The result type, which will be used, if the buildin type
                is mentioned.
```

Possible operator names:

	- int: This is for anything, which looks like an integer
	- double: This is for anything, which looks lie a floating-point number
	- string: This is for anything, which look like an string ("")
	- bool: This is for 'false' and 'true'

## Rule 4: struct

Used for buildin functionalities like deleting arrays, increasing the usage of
objects or anything like that.

How to call the rule 'struct' ?

```
::rule struct [operator] [pattern]
[operator]:   Like in Rule 3, this will be explained later
[pattern]:    A pattern (Should be between "", escape character is\), where
              {0}, {1} and {2} can be replaced, depending on the operator
```

The names operator can have:
	
	- increase: Increase the usage of an object

	- decrease: Decrease the usage of an object

	- remove: 'Remove' an object
	  (decrease it's usage and if usage 0 => delete it)

	- remove_func: Delete object if usage 0

	- assign_obj: Increase the usage of the object and
	  return the object

	- assign_obj\_old: Assign an old object to a new one => remove
	  the old object and increase the object of the new one and return
	  the new one
