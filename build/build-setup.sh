BLD="$HOME/ffe/build"
export BLD
TLS="$BLD/buildtools"
export TLS

ANT_HOME="$TLS/ant-1.9.6"
export ANT_HOME
ANT_OPTS="-Xms2048M -Xmx4096M"
export ANT_OPTS
ANT_ARGS=""
export ANT_ARGS
PATH="$ANT_HOME/bin:.:..:$PATH"
export PATH
