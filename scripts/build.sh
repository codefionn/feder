#!/usr/bin/env bash
# Copyright (C) 2018 Fionn Langhans <fionn.langhans@gmail.com>
shopt -s globstar

if ! [ -d "bin" ] ; then mkdir "bin" ; fi

if ! javac -cp src -d bin -encoding UTF-8 -source 8 src/feder/**/*.java ; then
	exit 1
fi

cd bin
jar -cfe ./jfederc.jar feder.Feder feder/**/*.class
cd ..

if (which doxygen 1>/dev/null) 2>/dev/null ; then
	(doxygen doxyconf 1>/dev/null) 2>/dev/null
fi
