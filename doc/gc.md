# Garbage Collection (GC)

This document describes the garbage collection coming from the brach 3cgc.
3cgc is an abbreviation for Tri-color marking garbage collection. The first aim
is to replace the old garbage collection (a reference counting one). Because the
main problem of that one is well known, namely 'cycles'.

## Strategy

The first strategy is to implement a naive mark-and-sweep garbage collection,
while hoping, that implementing that one leads to an tri-color marking


