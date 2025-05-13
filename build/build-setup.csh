setenv BLD $HOME/ffe/build
setenv TLS $BLD/buildtools

setenv ANT_HOME $TLS/ant-1.9.6
setenv ANT_OPTS "-Xms2048M -Xmx4096M"
setenv ANT_ARGS ""
set path = ($ANT_HOME/bin . .. $path)
