#!/usr/bin/env bash

# This script installs feder:
# The executable jar archive will be copied to /usr/lib/feder
# The Feder Standard Library federlang/base will be copied to /usr/lib/feder
# The script "jfederc" will be created, you probably want to use this.
# The script "jfedercnolib" will be created, this doesn't include any libraries.
#
# /usr/bin should be in your path variable, if you want to use jfederc instead
# of /usr/bin/jfederc
#
# This script detects Cygwin, if you use it to execute this shell script.

DEST_DIR=$1

! [ -d "$DEST_DIR/usr/lib" ] && mkdir --parents $DEST_DIR/usr/lib
! [ -d "$DEST_DIR/usr/share/java" ] && mkdir --parents $DEST_DIR/usr/share/java

if (! [ -w "$DEST_DIR/usr/lib" ] ) ; then
  echo "Insufficient permissions, run this program as a privileged user"
  exit 1
fi

if (! [ -w "$DEST_DIR/usr/share/java" ] ) ; then
  echo "Insufficient permissions, run this program as a privileged user"
  exit 1
fi

( ! [ -d "$DEST_DIR/usr/lib/feder" ] ) && mkdir --parents "$DEST_DIR/usr/lib/feder"
if ! [ -d "$DEST_DIR/usr/lib/feder" ] ; then
  echo "Insufficient permissions, run this program as a privileged user"
  exit 1
fi

if ! [ -f jfederc.jar ] ; then
  echo "jfederc.jar is missing. Run build.sh first!"
  exit 1
fi

if ! cp jfederc.jar "$DEST_DIR/usr/share/java/" ; then
  echo "Insufficient permissions, run this program as a privileged"
  exit 1
fi

cp -R federlang/base "$DEST_DIR/usr/lib/feder/"

! [ -d "$DEST_DIR/usr/bin" ] && mkdir --parents $DEST_DIR/usr/bin
if ! [ -w "$DEST_DIR/usr/bin" ] ; then
  echo "Insufficient permissions, run this program as a privileged user"
  exit 1
fi

if ! uname -o | grep Cygwin > /dev/null ; then

	cat > "$DEST_DIR/usr/bin/jfederc" << EOF
#!/usr/bin/env bash
java -jar /usr/share/java/jfederc.jar -I /usr/lib/feder/base \$@
exit \$?
EOF
else
	cat > "$DEST_DIR/usr/bin/jfederc" << EOF
#!/usr/bin/env bash
java -jar \$(cygpath -w /usr/share/java/jfederc.jar) -I \$(cygpath -w /usr/lib/feder/base) \$@
exit \$?
fi
EOF
fi

chmod a+x "$DEST_DIR/usr/bin/jfederc" # Allow exeuction (all users)

if ! uname -o | grep Cygwin > /dev/null ; then
	cat > "$DEST_DIR/usr/bin/jfedercnolib" << EOF
#!/usr/bin/env bash
java -jar /usr/share/java/jfederc.jar \$@
exit \$?
EOF
else
	cat > "$DEST_DIR/usr/bin/jfedercnolob" << EOF
#!/usr/bin/env bash
java -jar \$(cygpath -w /usr/share/java/jfeder.jar \$@
exit \$?
EOF
fi

chmod a+x "$DEST_DIR/usr/bin/jfedercnolib" # Allow execution (all users)

