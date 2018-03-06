# Arrays

Arrays can contain several objects at once. This is especially useful for
working with an unknown amount of data, like user input or strings.

The following program reads an integer from the console and then reads
as much integers as the previously specified length. Then the program awaits
one more integer, which will be added at the end of the array. The program
writes every element of the array to the console at the end.

```
include "stdio.fd"

io.print ("Length of the array: ")
length = io.readint32 ()
# Create the array
ar = int32[length]

# Read 'length' integers
for i = 0, i < len (ar), i++
	io.print ("Integer: ")
	ar [i] = io.readint32 ()
;

# Read one integer and append it to the array
io.print ("Integer (last): ")
append (ar, io.readint32 ())

# Print the contents seperated by ','
for i = 0, i < len (ar), i++
	if i != 0
		io.print (", ")
	;

	io.print (ar[i])
;

io.println ()
```
