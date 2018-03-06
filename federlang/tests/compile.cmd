@echo off
set FEDER_TOOLCHAIN=CL
set FEDER_COMPILER=java -jar jfederc.jar
::set WINDOWS_DEFAULT_LIB_PATH=C:\\Program Files (x86)\\Windows Kits\\10\\Lib\\10.0.16299.0\\um\\x86
::set WINDOWS_DEFAULT_LIBS="kernel32.lib;user32.lib;gdi32.lib;winspool.lib;comdlg32.lib;advapi32.lib;shell32.lib;ole32.lib;oleaut32.lib;uuid.lib;odbc32.lib;odbccp32.lib;"
::set H_WINDOWS_KITS=C:\\Program Files (x86)\\Windows Kits\\10\\Include\\10.0.16299.0\\
::set H_VISUAL_STUDIO_C="C:\\Program Files (x86)\\Microsoft Visual Studio\\2017\\Community\\VC\Tools\\MSVC\\14.12.25827\\include"
::set FEDER_CMD_START=--usewinop --coption /I --coption "%H_WINDOWS_KITS%\\shared" --coption /I --coption "%H_WINDOWS_KITS%\\ucrt" --coption /I --coption "%H_WINDOWS_KITS%\\um" --coption /I --coption "%H_WINDOWS_KITS%\\winrt" --coption /I --coption %H_VISUAL_STUDIO_C% --loption "/LIBPATH:%WINDOWS_DEFAULT_LIB_PATH%"
set FEDER_CMD_START=--usewinop
::set FEDER_CMD_START=
set FEDER_END=2>nul

%FEDER_COMPILER% %FEDER_CMD_START% -I base -I tests -D tests/build --toolchain %FEDER_TOOLCHAIN% -O tint tests\test_int.fd %FEDER_END%
%FEDER_COMPILER% %FEDER_CMD_START% -I base -I tests -D tests/build --toolchain %FEDER_TOOLCHAIN% -O tllong tests\test_llong.fd %FEDER_END%
%FEDER_COMPILER% %FEDER_CMD_START% -I base -I tests -D tests/build --toolchain %FEDER_TOOLCHAIN% -O loops tests\test_loops.fd %FEDER_END%
%FEDER_COMPILER% %FEDER_CMD_START% -I base -I tests -D tests/build --toolchain %FEDER_TOOLCHAIN% -O string tests\test_string.fd %FEDER_END%
%FEDER_COMPILER% %FEDER_CMD_START% -I base -I tests -D tests/build --toolchain %FEDER_TOOLCHAIN% -O global tests\test_global.fd %FEDER_END%
%FEDER_COMPILER% %FEDER_CMD_START% -I base -I tests -D tests/build --toolchain %FEDER_TOOLCHAIN% -O libload tests\test_libloader.fd %FEDER_END%
%FEDER_COMPILER% %FEDER_CMD_START% -I base -I tests -D tests/build --toolchain %FEDER_TOOLCHAIN% -O directory tests\test_directory.fd %FEDER_END%
%FEDER_COMPILER% %FEDER_CMD_START% -I base -I tests -D tests/build --toolchain %FEDER_TOOLCHAIN% -O array tests\test_array.fd %FEDER_END%
%FEDER_COMPILER% %FEDER_CMD_START% -I base -I tests -D tests/build --toolchain %FEDER_TOOLCHAIN% -O string_array tests\test_string_array.fd %FEDER_END%
%FEDER_COMPILER% %FEDER_CMD_START% -I base -I tests -D tests/build --toolchain %FEDER_TOOLCHAIN% -O dynamic tests\test_dynamic.fd %FEDER_END%
%FEDER_COMPILER% %FEDER_CMD_START% -I base -I tests -D tests/build --toolchain %FEDER_TOOLCHAIN% -O higher_order tests\test_higher_order.fd %FEDER_END%

.\tests\build\tint 
.\tests\build\tllong
.\tests\build\loops
.\tests\build\string
.\tests\build\global
.\tests\build\libload
.\tests\build\directory
.\tests\build\array
.\tests\build\string
.\tests\build\dynamic
.\tests\build\higher_order
