#!/bin/csh
#
# #########################################################
# ##                                                     ##
# ##  Initialization Script for Tinker-FFE on Linux/csh  ##
# ##                                                     ##
# #########################################################
#
# This is a csh/tcsh script to set environment variables
# needed to run Force Field Explorer at the command line
#
# Note FFE_HOME and TINKER_HOME are special directory names
# needed by the Force Field Explorer application
#

#
# Set Tinker-FFE installation directory
#
setenv INSTALLDIR "$HOME/Tinker-FFE"

#
# Set location of FFE, Tinker, JRE and FFE libraries
#
setenv FFE_HOME "$INSTALLDIR/ffe"
setenv TINKER_HOME "$INSTALLDIR/tinker"
setenv JRE_HOME "$INSTALLDIR/jre"
setenv FFE_LIB "$FFE_HOME/lib"
setenv FFE_NATIVE "$FFE_HOME/native"

#
# Set location of system-dependent Linux library
#
setenv OS_LIB "$FFE_NATIVE/linux/libffe.so"

#
# Append the Tinker binaries to default PATH
#
if !($?PATH) then
   setenv PATH "$TINKER_HOME/bin"
else
   setenv PATH "$TINKER_HOME/bin:$PATH"
endif

#
# Set LD_LIBRARY_PATH environment variable
#
if !($?LD_LIBRARY_PATH) then
   setenv LD_LIBRARY_PATH "$JRE_HOME/lib/server:$JRE_HOME/lib"
else
   setenv LD_LIBRARY_PATH "$JRE_HOME/lib/server:$JRE_HOME/lib:LD_LIBRARY_PATH"
endif

#
# Add required JAR files to CLASSPATH variable
#
if !($?CLASSPATH) then
   setenv CLASSPATH "$FFE_LIB/AppleJavaExtensions-1.6.jar:$FFE_LIB/commons-io-2.6.jar:$FFE_LIB/commons-lang-2.7.jar:$FFE_LIB/commons-logging-1.3.jar:$FFE_LIB/ffe-8.5.jar:$FFE_LIB/gluegen-rt-android-natives-macosx-universal.jar:$FFE_LIB/gluegen-rt.jar:$FFE_LIB/gluegen.jar:$FFE_LIB/groovy-all-2.4.4.jar:$FFE_LIB/j3dcore-1.7.jar:$FFE_LIB/j3dutils-1.7.jar:$FFE_LIB/jh-2.0.5.jar:$FFE_LIB/joal-natives-macosx-universal.jar:$FFE_LIB/joal.jar:$FFE_LIB/jogl-all-noawt-natives-macosx-universal.jar:$FFE_LIB/jogl-all.jar:$FFE_LIB/sunjce_provider.jar:$FFE_LIB/vecmath-1.7.jar:$OS_LIB"
else
   setenv CLASSPATH "$FFE_LIB/AppleJavaExtensions-1.6.jar:$FFE_LIB/commons-io-2.6.jar:$FFE_LIB/commons-lang-2.7.jar:$FFE_LIB/commons-logging-1.3.jar:$FFE_LIB/ffe-8.5.jar:$FFE_LIB/gluegen-rt-android-natives-linux-amd64.jar:$FFE_LIB/gluegen-rt.jar:$FFE_LIB/gluegen.jar:$FFE_LIB/groovy-all-2.4.4.jar:$FFE_LIB/j3dcore-1.7.jar:$FFE_LIB/j3dutils-1.7.jar:$FFE_LIB/jh-2.0.5.jar:$FFE_LIB/joal-natives-linux-amd64.jar:$FFE_LIB/joal.jar:$FFE_LIB/jogl-all-noawt-natives-linux-amd64.jar:$FFE_LIB/jogl-all.jar:$FFE_LIB/sunjce_provider.jar:$FFE_LIB/vecmath-1.7.jar:$OS_LIB:$CLASSPATH"
endif

#
# Create an alias to execute Force Field Explorer
#
#alias ffe "java -server -ms1024M -mx4096M -Djava.library.path=$FFE_LIB -Dtinker.dir=$TINKER_HOME -jar $FFE_LIB/ffe-8.5.jar"
alias ffe "$FFE_HOME/Force*Field*Explorer"
