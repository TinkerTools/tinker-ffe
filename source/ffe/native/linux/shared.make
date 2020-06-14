#
#
#  #################################################################
#  ##                                                             ##
#  ##  shared.make  --  create shared object library for FFE GUI  ##
#  ##              (Intel Compiler for Linux Version)             ##
#  ##                                                             ##
#  #################################################################
#
#
##icc -c -O3 -fPIC -static-intel -w nativeExec.c -I /usr/lib/jvm/java-8-oracle/include -I /usr/lib/jvm/java-8-oracle/include/linux
#icc -c -O3 -fPIC -static-intel -w nativeExec.c -I.. -I.
#icc -shared -soname nativeExec.o -o libffe.so
##xild -shared -soname libffe.so -o libffe.so nativeExec.o
#rm nativeExec.o
#
#
#  #################################################################
#  ##                                                             ##
#  ##  shared.make  --  create shared object library for FFE GUI  ##
#  ##               (GNU Compiler for Linux Version)              ##
#  ##                                                             ##
#  #################################################################
#
#
#gcc -c -O3 -fPIC nativeExec.c -I /usr/lib/jvm/java-8-oracle/include -I /usr/lib/jvm/java-8-oracle/include/linux
gcc -c -O3 -fPIC nativeExec.c -I.. -I.
gcc -shared nativeExec.o -o libffe.so
rm  nativeExec.o
