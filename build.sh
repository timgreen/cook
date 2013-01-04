#!/bin/bash

set -e

export TRAVIS_SCALA_VERSION=2.9.2
./travis.sh

export TRAVIS_SCALA_VERSION=2.10.0-RC5
./travis.sh
