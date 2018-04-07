#!/usr/bin/env bash

FDC_OPTIONS=$@

if ! [ -d "tests" ] ; then
	echo "Current directory has to be 'federlang'"
	exit 1
fi

[ -d tests/build ] && $(which rm) tests/build/*

export FEDER_OUTPUT_ENDING=""
export FEDER_ADDITIONAL=""
if (uname -o | grep "Cygwin") ; then
	export FEDER_TOOLCHAIN="clang"
	#export FEDER_OUTPUT_ENDING=".exe"
	#export FEDER_ADDITIONAL="--usewinop"
	export FEDER_ADDITIONAL="--separator /"
fi

if [ $# -eq 1 ] ; then
	export FEDER_TOOLCHAIN="$1"
fi

function feder_test_program_compile {
	if ./jfederc $FEDER_ADDITIONAL -I base -I tests -O "$2" -D tests/build "$1" $FDC_OPTIONS ; then
		return 0
	else
		return 1
	fi
}

HAS_ERROR=0

feder_test_program_compile "tests/test_return_false.fd" "ret_false"
if ./tests/build/ret_false$FEDER_OUTPUT_ENDING ; then
	echo "The program test_return_false must fail, when it terminates! BUT: It succeded"
	exit 1
fi

feder_test_program_compile "tests/test_return_true.fd" "ret_true"
if ! ./tests/build/ret_true$FEDER_OUTPUT_ENDING ; then
	echo "The program test_return_true must succeed, but it failed!"
	exit 1
fi

function feder_test_program_test {
	if ! timeout 5 $@ > /dev/null ; then
		echo "Test \"$1\" failed" 1>&2 
		HAS_ERROR=1
	fi
}

function feder_test {
	echo "# Test " $1$FEDER_OUTPUT_ENDING
	if feder_test_program_compile $@ ; then
		feder_test_program_test tests/build/$2
	else
		echo "Compiler error"
		HAS_ERROR=1
	fi
}

# feder_test_program_compile "tests/test_empty.fd" "empty"
feder_test "tests/test_bool.fd" "bool"
feder_test "tests/test_lines.fd" "lines"
feder_test "tests/test_if.fd" "testif"
feder_test "tests/test_whileinif.fd" "whileinif"
feder_test "tests/test_object_comparison.fd" "object_comparison"
feder_test "tests/test_hexadecimal.fd" "hexadecimal"
feder_test "tests/test_int.fd" "tint"
feder_test "tests/test_loops.fd" "loops"
feder_test "tests/test_real_array.fd" "real_array"
feder_test "tests/test_roperator.fd" "roperator"
feder_test "tests/test_list.fd" "list"
feder_test "tests/test_array.fd" "array"
feder_test "tests/test_iterator.fd" "iterator"
feder_test "tests/test_string.fd" "string"
feder_test "tests/test_string_array.fd" "string_array"
feder_test "tests/test_inheritance.fd" "inheritance"
feder_test "tests/test_global.fd" "global"
feder_test "tests/test_io_dir.fd" "io_dir"
feder_test "tests/test_libloader.fd" "libload"
feder_test "tests/test_directory.fd" "directory"
feder_test "tests/test_dynamic.fd" "dynamic"
feder_test "tests/test_higher_order.fd" "higher_order"
feder_test "tests/test_config.fd" "config"

exit $HAS_ERROR
