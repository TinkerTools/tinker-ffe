BLD="$HOME/ffe/build"
export BLD
TLS="$BLD/buildtools"
export TLS

ANT_HOME="$TLS/ant"
export ANT_HOME
ANT_OPTS="-Xms512M -Xmx1G"
export ANT_OPTS
ANT_ARGS=""
export ANT_ARGS
PATH="$ANT_HOME/bin:.:..:$PATH"
export PATH
