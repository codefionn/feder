@echo off

java -jar ..\jfederc.jar %1 %2 %3 %4 %5 %6 %7 %8 %9 

if not %errorlevel%==0 (
	EXIT /B 1
)

EXIT /B 0
