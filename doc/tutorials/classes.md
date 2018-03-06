# Classes

Classes are basic structures for object-oriented programming. Feder was not
designed to have a OOD (object-oriented design) or functional oriented one,
either. But classes can be very useful for many solutions and a quick (and
maybe dirty way) to develop new operations. Classes can be used in an elegant
way to create graphical user interfaces.

The following program creates an object from a class called Person. That class
has the attributes 'name' with the type String and the attribut 'age' with the
type 'int32'. The function 'printData' of the class 'Person' will be called wit
help of the object 'obj'.

```
include "stdio.fd"

class Person
	int32 age
	String name

	Person func set (int32 age0, String name0)
		age = age0
		name = name0
		return this
	;

	func printData
		io.print ("Age: ")
		io.println (age)
		io.print ("Name: ")
		io.println (name)
	;
;

obj = Person
obj.set (18, "Fionn Langhans")
# Or:
obj = Person.set (18, "Fionn Langhans")
obj.printData ()
```
