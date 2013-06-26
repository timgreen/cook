#!/bin/bash

COOK_VERSION=v1.0.0-M1

mkdir -p lib
mkdir -p tmp
cd tmp

## prepare scala home
mkdir -p scala
cd scala
[ -r scala-$TRAVIS_SCALA_VERSION ] || (wget -c http://www.scala-lang.org/downloads/distrib/files/scala-$TRAVIS_SCALA_VERSION.tgz &&
  rm -fr scala-$TRAVIS_SCALA_VERSION &&
  tar xf scala-$TRAVIS_SCALA_VERSION.tgz)

rm -f scala
ln -s scala-$TRAVIS_SCALA_VERSION scala

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
SCALA_TEST_JAR=scalatest_2.10.0-2.0.M5.jar
[ -r $SCALA_TEST_JAR ] || (wget -c https://oss.sonatype.org/content/groups/public/org/scalatest/scalatest_2.10.0/2.0.M5/$SCALA_TEST_JAR &&
  ln -sf $PWD/$SCALA_TEST_JAR ../../lib/scalatest.jar)
SCALLOP_JAR=scallop_2.10-0.6.4.jar
[ -r $SCALLOP_JAR ] || (wget -c http://repo.typesafe.com/typesafe/repo/org/rogach/scallop_2.10/0.6.4/$SCALLOP_JAR &&
  ln -sf $PWD/$SCALLOP_JAR ../../lib/scallop.jar)
JE_JAR=je-3.2.76.jar
[ -r $JE_JAR ] || (wget -c http://repo1.maven.org/maven2/berkeleydb/je/3.2.76/$JE_JAR &&
  ln -sf $PWD/$JE_JAR ../../lib/je.jar)
cd ..

## prepare cook
mkdir -p cook
cd cook
COOK_JAR=cook-$COOK_VERSION.jar
[ -r $COOK_JAR ] || (wget -c http://downloads.sourceforge.net/project/cook-bin/$COOK_JAR &&
  ln -sf $PWD/$COOK_JAR ../../bin/cook.jar)
cd ..

## build & test
cd ..
set -e
./bin/cook clean
./bin/cook build :cook.jar
./bin/cook run test/cook/ref:run_basic_ref_test
./bin/cook run test/cook/util:run_glob_scanner_test
./bin/cook run test/cook/util:run_dag_solver_test
