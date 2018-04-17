# Functions

Functions contain instructions how a the computer should operate and can be
called in the program. A function can have arguments and typically a return
type.

In Feder function are declared with the "func" keyword. The return type is in
front of the keyword, the function's name after the keyword and the
name the arguments in '(' [x] ')' brackets. Several arguments can be separated
with ','. In the following example is a function called 'sum'. The function
computes the sum of two integers (int32) and returns the result. The integers
are given to the function as arguments called 'n0' and 'n1'. The type of both
arguments is 'int32' (An 4 byte integer).

```
int32 func sum (int32 n0, int32 n1)
	return n0 + n1
;
```

To call this function one would type:

```
sum (5, 7)
# Or:
result = sum (5, 7)
# Or:
io.println (sum (5, 7))
# ... there are many possibilities
```

If you want to use the same type for several arguments in function, you don't
need to mention the type of the variable, the type will be the one of element
in front.

The function "sum" from above would look like this:
```
int32 func sum (int32 n0, n1)
    return n0 + n1
;
```
