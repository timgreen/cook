#!/bin/bash

version() {
  git describe --tags --abbrev=40 2>/dev/null || date +"t%Y-%m-%d_%H:%M:%S"
}

gen() {
  cat << EOF
package cook.app.version

object Version {

  val version = "$(version)"
  def apply = version
}
EOF
}

if (( $# > 0 )); then
  gen > "$1"
else
  gen
fi
