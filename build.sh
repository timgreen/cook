#!/bin/sh

ROOT=$(cd -P -- "$(dirname -- "$0")" && pwd -P)

LIB=$ROOT/lib
SRC=$ROOT/src
TEST=$ROOT/test
GEN_DIR=$ROOT/cook_gen
BUILD_DIR=$ROOT/cook_build
BIN_DIR=$ROOT/cook_bin

CP=$LIB/Mouse-1.3.jar:$LIB/scala-library.jar
RUNTIM_CP=$LIB/Mouse-1.3.runtime.jar


mkBuildDir() {
  mkdir -p $GEN_DIR
  mkdir -p $BUILD_DIR
  mkdir -p $BIN_DIR
}

generateParser() {
  mkBuildDir
  mkdir -p $GEN_DIR/cook/parser/
  java -cp $CP mouse.Generate -G $SRC/cook/parser/config.peg -p cook.parser -P ConfigParser -S ConfigSemantics -D $GEN_DIR/cook/parser/
}

build() {
  generateParser
  scala_files=$(find $SRC -name "*.scala" -print)
  java_files=$(find $GEN_DIR/cook/parser/ -name "*.java" -print)
  scalac -unchecked -d $BUILD_DIR -classpath $CP ${scala_files[*]} ${java_files[*]}
  javac -d $BUILD_DIR -classpath $CP:$BUILD_DIR ${java_files[*]}
}

testParser() {
  build

  IFS="
"
  for TEST_CASE in $TEST/*.cook; do
    java -cp $CP:$BUILD_DIR mouse.TryParser -P cook.parser.ConfigParser -f $TEST_CASE
  done
}

clear() {
  rm -fr $GEN_DIR $BUILD_DIR $BIN_DIR
}

main() {
  clear
  testParser
}

main
