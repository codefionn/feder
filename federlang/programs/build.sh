#!/usr/bin/env bash

function feder_compile {
	./jfederc -I base -D programs/build -O "$1" "programs/$1.fd"
}

feder_compile args_test
feder_compile clock
feder_compile dirtest
feder_compile fahrenheit_to_celsius
feder_compile file_hello_world
feder_compile hello_world
feder_compile print_license
feder_compile progtime
feder_compile readfile
feder_compile sys_progtime
feder_compile compute_pi
feder_compile printinput
feder_compile greatest_common_divisor
feder_compile pi_leibniz
