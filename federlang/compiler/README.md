# Compiler

In this directory is a project, which tries to create a Feder compiler, which
is written in Feder.

## Design aims

- A compiler, which is written to be fast and clean
- A compiler, which contains a tri-color marking garbage collection

## Structure

- Lexer: read tokens directly from the file (or from a buffer)
- Syntax Analysis: A component syntax analysis, which is easy to modify
