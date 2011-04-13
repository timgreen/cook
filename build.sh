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
SCALA_TEST_CP=$LIB/scalatest-1.3.jar


mkBuildDir() {
  mkdir -p $GEN_DIR
  mkdir -p $BUILD_DIR
  mkdir -p $BIN_DIR
}

testPeg() {
  java -cp $CP mouse.TestPEG -G $SRC/cook/config/parser/cook.peg
}

generateParser() {
  mkBuildDir
  mkdir -p $GEN_DIR/cook/config/parser/
  java -cp $CP mouse.Generate -G $SRC/cook/config/parser/cook.peg -p cook.config.parser -P Parser -S Semantics -D $GEN_DIR/cook/config/parser/
}

build() {
  generateParser
  scala_files=$(find $SRC -name "*.scala" -print)
  java_files=$(find $GEN_DIR/cook/config/parser/ -name "*.java" -print)
  scalac -deprecation -unchecked -d $BUILD_DIR -classpath $CP ${scala_files[*]} ${java_files[*]}
  javac -d $BUILD_DIR -classpath $CP:$BUILD_DIR ${java_files[*]}
}

buildTest() {
  build
  scala_files=$(find $TEST -name "*.scala" -print)
  scalac -unchecked -d $BUILD_DIR -classpath $CP:$BUILD_DIR:$SCALA_TEST_CP ${scala_files[*]}
}

runTest() {
  buildTest
  java -cp $CP:$BUILD_DIR:$SCALA_TEST_CP org.scalatest.tools.Runner -p . -o -s cook.config.parser.CookParserTest
}

clear() {
  rm -fr $GEN_DIR $BUILD_DIR $BIN_DIR
}

main() {
  runTest
}

main
