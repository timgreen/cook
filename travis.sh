#!/bin/bash

set -e

mkdir -p lib
mkdir -p tmp
cd tmp

## prepare scala home
mkdir -p scala
cd scala
wget -c http://www.scala-lang.org/downloads/distrib/files/scala-$TRAVIS_SCALA_VERSION.tgz
rm -fr scala-$TRAVIS_SCALA_VERSION
tar xf scala-$TRAVIS_SCALA_VERSION.tgz
export SCALA_HOME=$PWD/scala-$TRAVIS_SCALA_VERSION
ln -sf $SCALA_HOME/lib/scala-compiler.jar ../../lib/
ln -sf $SCALA_HOME/lib/scala-library.jar ../../lib/
cd ..

## prepare lib
mkdir -p lib
cd lib
if [[ "$TRAVIS_SCALA_VERSION" == "2.10.0-RC5" ]]; then
  wget -c http://repo1.maven.org/maven2/org/scalatest/scalatest_2.10.0-M7/2.0.M4-2.10.0-M7-B1/scalatest_2.10.0-M7-2.0.M4-2.10.0-M7-B1.jar
  ln -sf $PWD/scalatest_2.10.0-M7-2.0.M4-2.10.0-M7-B1.jar ../../lib/scalatest.jar
else
  wget -c http://repo1.maven.org/maven2/org/scalatest/scalatest_2.9.2/2.0.M4/scalatest_2.9.2-2.0.M4.jar
  ln -sf $PWD/scalatest_2.9.2-2.0.M4.jar ../../lib/scalatest.jar
fi
cd ..

## build & test
cd ..
./bin/cook clean
./bin/cook build src/cook/app:main
./bin/cook build src/cook/target:target
./bin/cook run test/cook/ref:run_basic_ref_test
