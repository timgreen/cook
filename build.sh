#!/bin/bash

./bin/cook build :cook-unprocessed.all.jar
if (( $? == 0 )); then
  cd proguard
  ln -sf ../cook_build/COOK_TARGET_cook-unprocessed.all.jar/cook-unprocessed.all.jar cook-unprocessed.jar
  java -Xms512m -Xmx2048m -jar proguard.jar @cook-standalone.pro
fi
