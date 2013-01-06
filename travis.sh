#!/bin/bash

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
ln -sf $SCALA_HOME/lib/scala-reflect.jar ../../lib/
ln -sf $SCALA_HOME/lib/akka-actors.jar ../../lib/
ln -sf $SCALA_HOME/lib/typesafe-config.jar ../../lib/
cd ..

## prepare lib
mkdir -p lib
cd lib
if [ x"$TRAVIS_SCALA_VERSION" == x"2.10.0-RC5" ]; then
  wget -c https://oss.sonatype.org/content/groups/public/org/scalatest/scalatest_2.10.0-RC5/2.0.M5-B1/scalatest_2.10.0-RC5-2.0.M5-B1.jar
  ln -sf $PWD/scalatest_2.10.0-RC5-2.0.M5-B1.jar ../../lib/scalatest.jar
  wget -c http://repo.typesafe.com/typesafe/repo/org/rogach/scallop_2.10/0.6.4/scallop_2.10-0.6.4.jar
  ln -sf $PWD/scallop_2.10-0.6.4.jar ../../lib/scallop.jar
fi
cd ..

## build & test
cd ..
set -e
./bin/cook clean
./bin/cook build src/cook/app:main
./bin/cook build src/cook/target:target
./bin/cook run test/cook/ref:run_basic_ref_test
