@echo off

java -jar ..\jfederc.jar %*

if not %errorlevel%==0 (
	EXIT /B 1
)

EXIT /B 0
