# Feder

![Feder Logo](/logo.png)

## Introduction

Feder is a programming language, which aims to be simple and yet fast. Currently
Feder source code can be compiled with jfederc (The Java Feder compiler, a Feder
compiler written in Java).

Attention: Currently, because of the "Jugend forscht" contest, issues can't be
created and you can't submit any changes to the project.

## Requirements

A Java enviroment ([JDK version >= 8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html))
and the programs of the JDK have to be available in the console environment
(```$PATH: export PATH=$PATH:/path/to/jdk/bin or set PATH = $PATH:/path/to/jdk/bin```, %PATH%: [Windows Environment variables](https://www.computerhope.com/issues/ch000549.htm)).
Requires: javac, java, jar. The compiler does also require a C compiler available
in the PATH environment variable. The default one is 'cc'. You can change the
used compiler program with the option '--toolchain [compiler]'. If you want to
use the compiler of MS Visual Studio also pass the '--usewincl' option to the
program (If you want to use clang: Install clang and put the bin directory in
path variable, then use the --toolchain option to change the used compiler).

You should also have installed a bash environment (on Windows [Cygwin](https://cygwin.com/)). This is
optional, but is required if you want to do the tutorials right below.

## Installation

```
$ git clone https://github.com/codefionn/feder.git
$ cd feder
$ ./build.sh
$ sudo ./install.sh
```

Now you have the scripts called "jfederc" and "jfedercnolib" at your complete
disposal. "jfederc" already includes the Feder Standard Library, but if you
don't want that, you should probably use "jfedercnolib", which doesn't include
any libraries. Cleanup (optional, don't do this if you want to read examples
written in Feder):

```
$ cd ..
$ rm -rf feder
```

## Testing

If an error occured during the tests, there could be an error in the Feder
compiler or in the Feder Standard Library or your setup is wrong.

```
$ ./build.sh
$ cd federlang
$ ./tests/test.sh
$ ./failtests/test.sh
```

If you want to use the clang (standard is 'cc'):

```
$ ./build.sh
$ cd federlang
$ ./tests/test.sh --toolchain clang
$ ./failtests/test.sh --toolchain clang
```

## Hello World

```
$ cat > hello_world.fd << EOF
> include "stdio.fd"
> 
> io.println ("Hello, World!")
> EOF
$ jfederc -D build hello_world.fd
$ ./build/hello_world
Hello, World!
```
Well that was the fast way, in different words:

Create a files called 'hello\_world.fd'. The contents of that file should be:

```
include "stdio.fd"

io.println ("Hello, World!")
```

Then open the console in the directory, where you saved the file and type:

```
$ jfederc -D build hello_world.fd
$ ./build/hello_world
Hello, World!
```

Examples are in /federlang/programs and little tutorials in /doc/tutorials/.

## Files

```
feder
    build.cmd       Build Java Feder compiler (batch)
    build.sh        Build Java Feder compiler (bash)
    doc
        Feder.odt   Document for the 'Jugend Forscht' contest
        Feder.7     Trying to created a man page for Feder
        tutorials   Tutorials describing the basics of the language

    federlang
        base        Feder Standard Library (/usr/lib/feder/base)
        clean.sh    Removes build files
        comparison  Compare runtime speed (C, Feder, Go, Python, Java)
        failtests   Tests which should fail to compile (./failtests/test.sh)
        jfederc     Compiles Feder source code (use /usr/bin/jfederc)
        programs    Programs written in Feder (examples)
        tests       Tests which should succeed (./tests/test.sh)
        tutorials   Programs for /doc/tutorials

    install.sh      install Feder, requires root permissions
    LICENSE         zlib license
    README.md       This file
    src             Source files for the Java Feder compiler
    uninstall.sh 	Uninstall files & directories installed by install.sh
```

## Documentation

Documentation can be found in /doc and if you want a documentation run

```
$ doxygen doxyconf
```

in the main directory of the project. This generated a HTML documentation
in /doc/java_api.
