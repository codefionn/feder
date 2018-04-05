@echo off

if not exist "bin" mkdir "bin"

javac -cp src -d bin -encoding UTF-8 -source 8 src\feder\*.java src\feder\utils\*.java src\feder\types\*.java

cd bin
jar -cfe ..\jfederc.jar feder.Feder feder\*.class feder\utils\*.class feder\types\*.class
cd ..
