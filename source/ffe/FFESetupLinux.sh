#!/bin/sh
shopt -s expand_aliases
#
# ######################################################## 
# ##                                                    ##
# ##  sh Initialization Script for Tinker-FFE on Linux  ##
# ##                                                    ##
# ######################################################## 
#
# This is an sh/bash script to set environment variables
# needed to run Force Field Explorer at the command line
#
# Note FFE_HOME and TINKER_HOME are special directory names
# needed by the Force Field Explorer application
#

#
# Set Tinker-FFE installation directory
#
INSTALLDIR="$HOME/Tinker-FFE"
export INSTALLDIR

#
# Set location of JRE, Tinker and Force Field Explorer
#
FFE_HOME="$INSTALLDIR/ffe"
export FFE_HOME
TINKER_HOME="$INSTALLDIR/tinker"
export TINKER_HOME
JRE_HOME="$INSTALLDIR/jre"
export JRE_HOME
FFE_LIB="$FFE_HOME/lib"
export FFE_LIB
FFE_NATIVE="$FFE_HOME/native"
export FFE_NATIVE

#
# Set location of system-dependent Linux library
#
OS_LIB="$FFE_NATIVE/linux/libffe.so"
export OS_LIB

#
# Append the Tinker binaries to default PATH
#
if [ -z "${PATH}" ]
then
   PATH="$TINKER_HOME/bin"
else
   PATH="$TINKER_HOME/bin:$PATH"
fi
export PATH

#
# Add required JAR files to CLASSPATH variable
#
if [ -z "${CLASSPATH}" ]
then
   CLASSPATH="$FFE_LIB/AppleJavaExtensions-1.6.jar;$FFE_LIB/commons-io-2.11.0.jar;$FFE_LIB/commons-lang-2.7.jar;$FFE_LIB/commons-logging-1.3.jar;$FFE_LIB/ffe-8.10.jar;$FFE_LIB/gluegen-rt-android-natives-linux-amd64.jar;$FFE_LIB/gluegen-rt.jar:$FFE_LIB/gluegen.jar;$FFE_LIB/groovy-all-2.4.4.jar;$FFE_LIB/j3dcore-1.7.jar;$FFE_LIB/j3dutils-1.7.jar;$FFE_LIB/jh-2.0.5.jar;$FFE_LIB/joal-natives-linux-amd64.jar;$FFE_LIB/joal.jar;$FFE_LIB/jogl-all-noawt-natives-linux-amd64.jar;$FFE_LIB/jogl-all.jar;$FFE_LIB/sunjce_provider.jar;$FFE_LIB/vecmath-1.7.jar;$OS_LIB"
else
   CLASSPATH="$FFE_LIB/AppleJavaExtensions-1.6.jar;$FFE_LIB/commons-io-2.11.0.jar;$FFE_LIB/commons-lang-2.7.jar;$FFE_LIB/commons-logging-1.3.jar;$FFE_LIB/ffe-8.10.jar;$FFE_LIB/gluegen-rt-android-natives-linux-amd64.jar;$FFE_LIB/gluegen-rt.jar:$FFE_LIB/gluegen.jar;$FFE_LIB/groovy-all-2.4.4.jar;$FFE_LIB/j3dcore-1.7.jar;$FFE_LIB/j3dutils-1.7.jar;$FFE_LIB/jh-2.0.5.jar;$FFE_LIB/joal-natives-linux-amd64.jar;$FFE_LIB/joal.jar;$FFE_LIB/jogl-all-noawt-natives-linux-amd64.jar;$FFE_LIB/jogl-all.jar;$FFE_LIB/sunjce_provider.jar;$FFE_LIB/vecmath-1.7.jar;$OS_LIB;$CLASSPATH"
fi
export CLASSPATH

#
# Create an alias to execute Force Field Explorer
#
#alias ffe="java -server -ms2048M -mx4096M -Djava.library.path=$FFE_LIB -Dtinker.dir=$TINKER_HOME -jar $FFE_LIB/ffe-8.10.jar"
alias ffe="$FFE_HOME/Force*Field*Explorer"
