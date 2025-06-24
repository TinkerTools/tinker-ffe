#
#
#  #################################################################
#  ##                                                             ##
#  ##  shared.make  --  create shared object library for FFE GUI  ##
#  ##              (GNU Compiler for macOS Version)               ##
#  ##                                                             ##
#  #################################################################
#
#
#gcc -c -fPIC nativeExec.c -I /Library/Java/JavaVirtualMachines/amazon-corretto-11.jdk/Contents/Home/include -I /Library/Java/JavaVirtualMachines/amazon-corretto-11.jdk/Contents/Home/include/darwin
gcc -c -fPIC nativeExec.c -I.. -I.
gcc -dynamiclib nativeExec.o -o libffe.jnilib
rm nativeExec.o
