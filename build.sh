#!/bin/sh

ROOT=$(cd -P -- "$(dirname -- "$0")" && pwd -P)

LIB=$ROOT/lib
SRC=$ROOT/src
TEST=$ROOT/test
GEN_DIR=$ROOT/cook_gen
BUILD_DIR=$ROOT/cook_build

CP=$LIB/Mouse-1.3.jar:$LIB/scala-library.jar:$LIB/ant.jar
RUNTIM_CP=$LIB/Mouse-1.3.runtime.jar
SCALA_TEST_CP=$LIB/scalatest-1.3.jar


mkBuildDir() {
  mkdir -p $GEN_DIR
  mkdir -p $BUILD_DIR
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
  fsc -deprecation -unchecked -d $BUILD_DIR -classpath $CP ${scala_files[*]} ${java_files[*]}
  javac -d $BUILD_DIR -classpath $CP:$BUILD_DIR ${java_files[*]}
}

buildTest() {
  build
  scala_files=$(find $TEST -name "*.scala" -print)
  fsc -unchecked -d $BUILD_DIR -classpath $CP:$BUILD_DIR:$SCALA_TEST_CP ${scala_files[*]}
}

runTest() {
  buildTest
  java -cp $CP:$BUILD_DIR:$SCALA_TEST_CP org.scalatest.tools.Runner -p . -o -s cook.config.parser.CookParserTest
  java -cp $CP:$BUILD_DIR:$SCALA_TEST_CP org.scalatest.tools.Runner -p . -o -s cook.config.runner.unit.UnitTest
}

clear() {
  rm -fr $GEN_DIR $BUILD_DIR
}

run() {
  build
  java -cp $CP:$BUILD_DIR cook.app.Main build "src/cook/config/parser:parser"
}

main() {
  #runTest
  run
}

main
