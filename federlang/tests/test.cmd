@echo off
:: Copyright (C) 2018 Fionn Langhans

call:test test.fd
call:test test_array
call:test test_bool
call:test test_config
call:test test_directory
call:test test_dynamic
call:test test_empty
call:test test_global
call:test test_hexadecimal
call:test test_higher_order
call:test test_if
call:test test_inheritance
call:test test_int
call:test test_io_dir
call:test test_iterator
call:test test_libloader
call:test test_lines
call:test test_list
call:test test_loops
call:test test_object_comparison
call:test test_real_array
call:test test_return_false
call:test test_return_true
call:test test_roperator
call:test test_string
call:test test_string_array
call:test test_whileinif
call:test test_math

EXIT /B 0

:test
Set arg1=%1
echo # Testing %arg1%
call jfederc.cmd -I base -I tests -D tests\build --toolchain CL --usewinop --coption "/nologo" --loption "/nologo" .\tests\%1.fd 
if not %errorlevel%==0 (
	echo Failed to compile
) else (
	.\tests\build\%1 
	if not %errorlevel%==0 (
		echo Test failed
	)
)
EXIT /B 0
