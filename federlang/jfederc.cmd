:: Copyright (C) 2018 Fionn Langhans <fionn.langhans@gmail.com>
@echo off

java -jar ..\bin\jfederc.jar %*

if not %errorlevel%==0 (
	EXIT /B 1
)

EXIT /B 0
