REM THIS FILE SHOULD BE APPENDED TO YOUR C:\AUTOEXEC.BAT FILE PRIOR
REM TO RUNNING FORCE FIELD EXPLORER; IF YOU DO NOT ALREADY HAVE AN
REM AUTOEXEC.BAT FILE, USE THIS ONE BY PLACING IT AT C:\.; PLEASE
REM REBOOT YOUR MACHINE FOR CHANGES TO AUTOEXEC.BAT TO TAKE EFFECT
REM
REM Set the three variables below to reflect local directory choices:
REM TINKER --  directory containing the Tinker executable files
REM FFEHOME -- directory with FFE.JAR, the Force Field Explorer JAR file
REM JRE -- directory containing JAVA.EXE, the Java executable
REM
set MTINSTALLDIR="%HOMEPATH%\Desktop\Force Field Explorer"
REM
REM You should not have to modify lines below this point
REM
set TINKER="%MTINSTALLDIR%\tinker"
set FFEHOME="%MTINSTALLDIR%\ffe"
set JRE="%MTINSTALLDIR%\jre"
REM
REM Please do not modify lines below this point
REM
setx PATH %JRE%\bin;%JRE%\bin\server;%tinker%;%PATH% /M
setx CLASSPATH %FFEHOME%\ffe-7.1.jar;%CLASSPATH% /M
