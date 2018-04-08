# Exchange operator

The exchange operator changes the value of two variables. But not only that,
the operator also allows a programmer to manipulate two values, before they
are exchanged.

A simple example: We have two integers one called n0 and another one called
n1. n0 has the value 10 and n1 the value 12. Then we exchange n0 and n1:

```
n0 <=> n1
```

Now n0 has the value 12 and n1 10. We exchange them again, but now we manipulate
the values of the variables, before they are exchanged.

```
n0, n0 * 2 <=> n1, n1 + 3
```

Now n0 has the value 13 and n1 24.
