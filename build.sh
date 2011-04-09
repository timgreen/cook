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
  java -cp $CP mouse.TestPEG -G $SRC/cook/parser/config.peg
  java -cp $CP mouse.TestPEG -G $SRC/cook/parser/rule.peg
}

generateParser() {
  mkBuildDir
  mkdir -p $GEN_DIR/cook/parser/
  java -cp $CP mouse.Generate -G $SRC/cook/parser/config.peg -p cook.parser -P ConfigParser -S ConfigSemantics -D $GEN_DIR/cook/parser/
  java -cp $CP mouse.Generate -G $SRC/cook/parser/rule.peg -p cook.parser -P RuleParser -S RuleSemantics -D $GEN_DIR/cook/parser/
}

build() {
  generateParser
  scala_files=$(find $SRC -name "*.scala" -print)
  java_files=$(find $GEN_DIR/cook/parser/ -name "*.java" -print)
  scalac -unchecked -d $BUILD_DIR -classpath $CP ${scala_files[*]} ${java_files[*]}
  javac -d $BUILD_DIR -classpath $CP:$BUILD_DIR ${java_files[*]}
}

buildTest() {
  build
  scala_files=$(find $TEST -name "*.scala" -print)
  scalac -unchecked -d $BUILD_DIR -classpath $CP:$BUILD_DIR:$SCALA_TEST_CP ${scala_files[*]}
}

runTest() {
  buildTest
  java -cp $CP:$BUILD_DIR:$SCALA_TEST_CP org.scalatest.tools.Runner -p . -o -s cook.parser.ConfigTest
}

clear() {
  rm -fr $GEN_DIR $BUILD_DIR $BIN_DIR
}

main() {
  clear
  testPeg
  runTest
}

main
