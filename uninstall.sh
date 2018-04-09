#!/usr/bin/env bash

# This script uninstalls feder:
#  - Delete directory /usr/lib/feder
#  - Delete file /usr/bin/jfederc
#  - Delete file /usr/bin/jfedercnolib

DEST_DIR=$1

if ( ! [ -w $DEST_DIR/usr/lib ] ) || ( ! [ -w $DEST_DIR/usr/bin ] ) ; then
  echo "Insufficient permissions, run program as root"
  exit 1
fi

[ -d $DEST_DIR/usr/lib/feder ] && $(which rm) -r $DEST_DIR/usr/lib/feder
[ -f $DEST_DIR/usr/bin/jfederc ] && $(which rm) $DEST_DIR/usr/bin/jfederc
[ -f $DEST_DIR/usr/bin/jfedercnolib ] && $(which rm) $DEST_DIR/usr/bin/jfedercnolib
