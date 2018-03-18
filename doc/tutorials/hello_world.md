# Hello World

The "hello world" program prints "Hello World" to the console and terminates,
even if that task is rather an easy one, it shows some basics of a programming
language.

The Task:

Create a file called hello_world.fd in your directory of choice.
hello_world.fd:

```
include "stdio.fd"
io.println ("Hello World")
```

This program shows: We need a library called stdio.fd. That library allows us
to use basic input/output operations like printing a String to the console.
Another thing: The function 'println' is called from something called 'io'.
'io' is a namespace and the '.' marks, that we search for bindings (classes,
functions, interfaces, types, ...) in 'io'. Next are the brackets '(' and
')', these mark after coming println (a binding), that we try to call 'println'
as a function with the things inside the brackets '(' and ')'. That 'thing'
between the brackets are arguments, in this case just one: "Hello World". This
is called a string (the class in the Feder Standard Library is called 'String').
"Hello World" is an object.
