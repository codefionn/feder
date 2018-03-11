# Interfaces

In Feder interfaces are structures, which can be used to declare objects, which
can represent functions.

In the following example is an interface call 'int\_sum', which has two
arguments: int32 n0 and int32 n1. The result type of the interface is int32.
Interfaces are declared the same way as functions, but the 'func' keyword is
replaced by the 'interface' keyword.

```
int32 interface int_sum (int32 n0, int32 n1)
```

The following code creates a function called sum, which has the same arguments
and result type as the interface 'int\_sum'.

```
int32 func sum (int32 n0, int32 n1)
	return n0 + n1
;
```

Then an object called 'fn\_sum' is being created and it has the type 'int\_sum'
and will be assigned to the function 'sum'. After that, the object 'fn\_sum' is
called as a function (the result is assigned to the object 'n').

```
int_sum fn_sum = sum
n = fn_sum (5, 2)
```
