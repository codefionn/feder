::Copyright (C) 2018 Fionn Langhans <fionn.langhans@gmail.com>
@echo off

mkdir /parents "C:\Program Files\jfederc"
if not exist "jfederc.jar" (
    echo "Couldn't install: Missing jfederc.jar"
    echo "Did you forget to build the compiler ?"
    EXIT /B 0
)

cp jfederc.jar "C:\Program Files\jfederc\jfederc.jar"
cp federlang\base "C:\Program Files\jfederc\base"
