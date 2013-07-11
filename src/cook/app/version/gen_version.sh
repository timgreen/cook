#!/bin/bash

version() {
  git describe --tags --abbrev=40 2>/dev/null || date +"t%Y-%m-%d_%H:%M:%S"
}

gen() {
  echo "cook.version = \"$(version)\""
}

if (( $# > 0 )); then
  gen > "$1"
else
  gen
fi
