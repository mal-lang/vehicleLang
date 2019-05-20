#!/bin/bash

mvn clean
mvn -f foreseetipom.xml package -Dmaven.test.skip
