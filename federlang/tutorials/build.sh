#!/usr/bin/env bash

function feder_compile {
	./jfederc -I base -D tutorials/build -O "$1" "tutorials/$1.fd"
}

feder_compile 'interfaces'
feder_compile 'functions'
feder_compile 'classes'
