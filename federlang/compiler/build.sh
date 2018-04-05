#!/usr/bin/env bash
BACK_TO_DIR=""
if [ -d "src" ] ; then
	cd ..
	BACK_TO_DIR="compiler"
fi

./jfederc $@ -I base -I compiler/src -D compiler/build -O federc compiler/src/main.fd

if ! [ $BACK_TO_DIR == "" ] ; then
	cd $BACK_TO_DIR
fi

# < vim tabsize=4 >
