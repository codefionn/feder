::Copyright (C) 2018 Fionn Langhans <fionn.langhans@gmail.com>
@echo off
@echo "Attention: You have to put the path 'C:\Program Files\jfederc' in the PATH env yourself!"

mkdir "C:\Program Files\jfederc"
if not exist "jfederc.jar" (
    echo "Couldn't install: Missing jfederc.jar"
    echo "Did you forget to build the compiler ?"
    EXIT /B 0
)

copy /Y bin\jfederc.jar "C:\Program Files\jfederc\jfederc.jar"
robocopy /E federlang\base "C:\Program Files\jfederc\base"

:: Write to file
@echo @echo off > "C:\Program Files\jfederc\jfederc.cmd"
@echo java -jar "C:\Program Files\jfederc\jfederc.jar" -T cl -W -I "C:\Program Files\jfederc\base" %%* >> "C:\Program Files\jfederc\jfederc.cmd"
::set PATH=C:\Program Files\jfederc;%PATH%
