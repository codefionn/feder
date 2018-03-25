# Datatypes

In Feder datatypes are primitive objects, which usually get copied, if one 
assigns them to a new binding (a object with a name), they also get copied,
if they are arguments of a function.

When creating a compiler, which translates Feder to C, datatypes are
defined as a C type (e.g.: int32_t or char).

The following example creates an integer like datatype, with the C type
int32_t.

```
type "int32_t" int32
;
```

In the example one can see, that the generel describtion of a declaration
of a datatype is:

```
'type' space '"' [C type] '"' space [Binding name]
[datatype body]
';'
```
