#!/usr/bin/env bash
START_DIR="$2"
if [ $1 == "" ] ; then
    cd /tmp
else
    cd $1
fi

rm -rf linguist
git clone https://github.com/github/linguist
cd linguist
script/bootstrap
bundle exec rake samples
bundle exec bin/linguist
cat >> lib/linguist/languages.yml << EOF
Feder:
  type: programming
  color: "#445533"
  extensions:
  - ".fd"
  - ".feder"
  tm_scope: source.feder
EOF

bundle exec script/add-grammar https://github.com/codefionn/feder-atom-highlighting

# Samples
mkdir --parents samples/Feder
JFEDER_DIR="."
if ! [ $2 == "" ] ; then
    JFEDER_DIR="$2"
fi

cp $JFEDER_DIR/federlang/base/*.fd samples/Feder
cp $JFEDER_DIR/federlang/base/types/*.fd samples/Feder

bundle exec script/set-language-ids --update

# Go back
cd $START_DIR
