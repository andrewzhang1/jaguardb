#!/bin/bash

cnt=$1

if [[ "x$cnt" = "x" ]]; then
	cnt=10000
fi

export LD_LIBRARY_PATH=$HOME/jaguar/lib
java -cp ../target/jaguar-kafka-1.0-SNAPSHOT-jar-with-dependencies.jar:../lib/jaguar-jdbc-2.0.jar:../jaguar_kafka/lib/slf4j-simple-1.6.2.jar SQLRunner producer $cnt
