#!/usr/bin/env bash

# This script installs feder:
# The executable jar archive will be copied to /usr/lib/feder
# The Feder Standard Library federlang/base will be copied to /usr/lib/feder
# The script "jfederc" will be created, you probably want to use this.
# The script "jfedercnolib" will be created, this doesn't include any libraries.
#
# /usr/bin should be in your path variable, if you want to use jfederc instead
# of /usr/bin/jfederc

if (! [ -w /usr/lib ] ) ; then
  echo "Insufficient permissions, run this program as a privileged user"
  exit 1
fi

( ! [ -d /usr/lib/feder ] ) && mkdir --parents /usr/lib/feder
if ! [ -d /usr/lib/feder ] ; then
  echo "Insufficient permissions, run this program as a privileged user"
  exit 1
fi

if ! [ -f jfederc.jar ] ; then
  echo "jfederc.jar is missing. Run build.sh first!"
  exit 1
fi

if ! cp jfederc.jar /usr/lib/feder/ ; then
  echo "Insufficient permissions, run this program as a privileged"
  exit 1
fi

cp -R federlang/base /usr/lib/feder/

if ! uname -o | grep Cygwin > /dev/null ; then

	cat > /usr/bin/jfederc << EOF
#!/usr/bin/env bash
java -jar /usr/lib/feder/jfederc.jar -I /usr/lib/feder/base \$@
exit \$?
EOF
else
	cat > /usr/bin/jfederc << EOF
#!/usr/bin/env bash
java -jar \$(cygpath -w /usr/lib/feder/jfederc.jar) -I \$(cygpath -w /usr/lib/feder/base) \$@
exit \$?
fi
EOF
fi

chmod a+x /usr/bin/jfederc # Allow exeuction (all users)

if ! uname -o | grep Cygwin > /dev/null ; then
	cat > /usr/bin/jfedercnolib << EOF
#!/usr/bin/env bash
java -jar /usr/lib/feder/jfederc.jar \$@
exit \$?
EOF
else
	cat > /usr/bin/jfedercnolob << EOF
#!/usr/bin/env bash
java -jar \$(cygpath -w /usr/lib/feder/jfeder.jar \$@
exit \$?
EOF
fi

chmod a+x /usr/bin/jfedercnolib # Allow exeuction (all users)

