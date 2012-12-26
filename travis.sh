#!/bin/bash

set -e

mkdir -p tmp
cd tmp
wget http://www.scala-lang.org/downloads/distrib/files/scala-2.9.2.tgz
tar xf scala-$SCALA_VERSION.tgz
export SCALA_HOME=$PWD/scala-$SCALA_VERSION
cd ..
./bin/cook build src/cook/app:main
