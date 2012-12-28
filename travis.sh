#!/bin/bash

set -e

mkdir -p tmp
cd tmp
wget http://www.scala-lang.org/downloads/distrib/files/scala-$TRAVIS_SCALA_VERSION.tgz
tar xf scala-$TRAVIS_SCALA_VERSION.tgz
export SCALA_HOME=$PWD/scala-$TRAVIS_SCALA_VERSION
cd ..
./bin/cook build src/cook/app:main
./bin/cook run test/cook/ref:run_basic_ref_test
