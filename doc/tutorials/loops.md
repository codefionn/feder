# Loops

Loops are operations, which can repeat themselves. Typically there's a 
'for' and 'while' loop in a programming language. Feder supports both,
even if 'for' can be used for the same thing as 'while'.

The following program uses a for loop to iterate 10 times and prints the
current 'i' to the console.
```
include "stdio.fd"

for i = 0, i < 10, i++
	io.println (i)
;
```

This for loop declare at first the object i and assigns that object to
'0'. Then there's the loops condition: As long as this condition is 'true'
the loop continues. After that we add the value 1 to the object 'i'. The
program above is nearly the same as the one below:

```
i = 0
for i < 10, i++
	io.println (i)
;
```

And the last program, where the 'i++' is moved somewhere else:

```
i = 0
for i < 10
	io.println (i)
	i++
;
```
