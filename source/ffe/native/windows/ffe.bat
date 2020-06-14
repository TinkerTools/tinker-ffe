@echo off
set FFEHOME="%HOMEPATH%\Tinker-FFE\ffe\lib"
java -server -mx4096M -Djava.library.path=%FFEHOME% -jar %FFEHOME%\ffe.jar
