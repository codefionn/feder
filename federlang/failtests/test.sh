#!/usr/bin/env bash
# Copyright (C) 2018 Fionn Langhans <fionn.langhans@gmail.com>

FDC_OPTIONS=$@

if ! [ -d "failtests" ] ; then
	echo "Current directory has to be 'federlang'"
	exit 1
fi

if ! ( [ -e "failtests/error.fd" ] && [ -e "failtests/private_fc.fd" ] && [ -e "failtests/README" ]) ; then
	echo "error.fd, private_fc.fd or README not found in the 'failtests' directory"
	exit 1
fi

function feder_failtest_compile {
	./jfederc -I base -D failtests/build $@ $FDC_OPTIONS 2>/dev/null
	if [ $? == 0 ] ; then
		echo "Test $1 should have failed"
	fi
}

feder_failtest_compile failtests/error.fd
feder_failtest_compile failtests/private_fc.fd

