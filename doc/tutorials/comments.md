# Comments

A comment is a text block in a programming language, which is ignored by the
compiler/parser.

Typically, there are two types of comments:

- One liners
- Comments, that have a specific range (can have mutliple lines)

In Feder a "one liner" starts with # and is then terminated by the end of the
line. Example

```
# I am a comment
code = do_something() # I am a comment, too
```

And those "ranged" comments start with "##" and end with "##". Example:
```
## I
am
a
comment
over multiple lines ##
code = ## Thats a function -> ## do_something()
```
