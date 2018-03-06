#!/usr/bin/env bash
if ! [ -d "bin" ] ; then mkdir "bin" ; fi

if ! javac -cp src -d bin -encoding UTF-8 -source 8 src/feder/*.java src/feder/utils/*.java src/feder/types/*.java ; then
	exit 1
fi

cd bin
jar -cfe ../jfederc.jar feder.Feder feder/*.class feder/utils/*.class feder/types/*.class
cd ..
