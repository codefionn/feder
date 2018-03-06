#!/usr/bin/env bash

# This script uninstalls feder:
#  - Delete directory /usr/lib/feder
#  - Delete file /usr/bin/jfederc
#  - Delete file /usr/bin/jfedercnolib

if ( ! [ -w /usr/lib ] ) || ( ! [ -w /usr/bin ] ) ; then
  echo "Insufficient permissions, run program as root"
  exit 1
fi

[ -d /usr/lib/feder ] && $(which rm) -r /usr/lib/feder
[ -f /usr/bin/jfederc ] && $(which rm) /usr/bin/jfederc
[ -f /usr/bin/jfedercnolib ] && $(which rm) /usr/bin/jfedercnolib
