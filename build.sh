#!/bin/sh

ROOT=$(cd -P -- "$(dirname -- "$0")" && pwd -P)

LIB=$ROOT/lib
SRC=$ROOT/src
TEST=$ROOT/test
GEN_DIR=$ROOT/cook_gen
BUILD_DIR=$ROOT/cook_build
BIN_DIR=$ROOT/cook_bin

CP=$LIB/Mouse-1.3.jar
RUNTIM_CP=$LIB/Mouse-1.3.runtime.jar


mkBuildDir() {
  mkdir -p $GEN_DIR
  mkdir -p $BUILD_DIR
  mkdir -p $BIN_DIR
}

generateParser() {
  mkBuildDir
  mkdir -p $GEN_DIR/cook/parser/
  cd $GEN_DIR/cook/parser/
  java -cp $CP mouse.Generate -G $SRC/cook/parser/cook.peg -p cook.parser -P Parser
  cd $ROOT
}

buildParser() {
  generateParser
  javac -d $BUILD_DIR -cp $CP $GEN_DIR/cook/parser/Parser.java
}

testParser() {
  buildParser

  IFS="
"
  for TEST_CASE in $(ls $TEST/*.cook); do
    java -cp $CP:$BUILD_DIR mouse.TryParser -P cook.parser.Parser -f $TEST_CASE
  done
}

main() {
  testParser
}

main
