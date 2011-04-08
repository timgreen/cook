#!/bin/sh

ROOT=$(cd -P -- "$(dirname -- "$0")" && pwd -P)

LIB=$ROOT/lib
SRC=$ROOT/src
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
  java -cp $CP mouse.Generate -G $SRC/cook/parser/cook.peg -p cook.parser -P Parser -S Semantics
  cd $ROOT
}

main() {
  generateParser
}

main
