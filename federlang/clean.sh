function fd_del {
	if [ -d "$1" ] ; then
		$(which rm) -r "$1"
	fi
}

fd_del 'tests/build'
fd_del 'base/build'
fd_del 'federtool/build'
fd_del 'comparison/build'
fd_del 'comparison/feder/build'
fd_del 'comparison/c/build'
fd_del 'comparison/go/build'
