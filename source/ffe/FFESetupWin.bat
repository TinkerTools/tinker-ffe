@echo off
set location=%~dp0

set tinkerdir=%location%tinker
set ffedir=%location%ffe

set ffejardir=%ffedir%\lib\ffe-8.8.jar
set ffedlldir=%ffedir%\native\windows\ffe.dll
set tclasspath=%ffejardir%;%ffedlldir%;

setx TINKER "%tinkerdir%"
setx FFEHOME "%ffedir%"

set "blankspace="

call set CLASSPATH=%%CLASSPATH:%tclasspath%=%blankspace%%%
setx CLASSPATH "%tclasspath%%CLASSPATH%"

mklink /J bin jre\bin
mklink /J lib jre\lib
