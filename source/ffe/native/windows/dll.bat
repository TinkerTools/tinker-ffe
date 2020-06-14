@echo off
rem
rem
rem  #############################################################
rem  ##                                                         ##
rem  ##  dll.bat  --  create shared object library for FFE GUI  ##
rem  ##          (Intel Compiler for Windows Version)           ##
rem  ##                                                         ##
rem  #############################################################
rem
rem
rem icl /LD /w nativeExec.c /o ffe.dll /I "C:\Program Files\Java\jdk1.8.0_112\include" /I "C:\Program Files\Java\jdk1.8.0_112\include\win32"
rem icl /LD /w nativeExec.c /o ffe.dll /I.. /I.
rem del nativeExec.obj ffe.lib ffe.exp
rem
rem
rem  #############################################################
rem  ##                                                         ##
rem  ##  dll.bat  --  create shared object library for FFE GUI  ##
rem  ##           (GNU Compiler for Windows Version)            ##
rem  ##                                                         ##
rem  #############################################################
rem
rem
gcc -shared nativeExec.c -o ffe.dll -I.. -I.
