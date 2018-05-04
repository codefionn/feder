#!/usr/bin/env bash
# Copyright (C) 2018 Fionn Langhans <fionn.langhans@gmail.com>

BACK_TO_DIR=""
if [ -d "src" ] ; then
	cd ..
	BACK_TO_DIR="compiler"
fi

./jfederc $@ --coption -ggdb --loption -ggdb -I base -I compiler/src -D compiler/build -O federc compiler/src/federc/main.fd

if ! [ $BACK_TO_DIR == "" ] ; then
	cd $BACK_TO_DIR
fi

# < vim tabsize=4 >
