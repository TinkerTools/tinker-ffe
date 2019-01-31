#
#
#  #################################################################
#  ##                                                             ##
#  ##  shared.make  --  create shared object library for FFE GUI  ##
#  ##              (Intel Compiler for MacOS Version)             ##
#  ##                                                             ##
#  #################################################################
#
#
##icc -c -O3 -fPIC -static-intel -w nativeExec.c -I /Library/Java/JavaVirtualMachines/jdk1.8.0_112.jdk/Contents/Home/include -I /Library/Java/JavaVirtualMachines/jdk1.8.0_112.jdk/Contents/Home/include/darwin
#icc -c -O3 -fPIC -static-intel -w nativeExec.c -I.. -I.
#icc -shared nativeExec.o -o libffe.jnilib
#rm nativeExec.o
#
#
#  #################################################################
#  ##                                                             ##
#  ##  shared.make  --  create shared object library for FFE GUI  ##
#  ##              (GNU Compiler for MacOS Version)               ##
#  ##                                                             ##
#  #################################################################
#
#
#gcc -c nativeExec.c -I /Library/Java/JavaVirtualMachines/jdk1.8.0_112.jdk/Contents/Home/include -I /Library/Java/JavaVirtualMachines/jdk1.8.0_112.jdk/Contents/Home/include/darwin
gcc -c nativeExec.c -I.. -I.
gcc -dynamiclib nativeExec.o -o libffe.jnilib
rm nativeExec.o
