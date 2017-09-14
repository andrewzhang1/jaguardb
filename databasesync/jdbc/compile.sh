#!/bin/bash

pd=`pwd`
export ANT_HOME=$pd/../../util/apache-ant-1.10.1
$ANT_HOME/bin/ant
cp -f build/*.jar lib/

