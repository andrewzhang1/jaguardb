#!/bin/bash

export LD_LIBRARY_PATH=$HOME/jaguar/lib
java -cp ../target/jaguar-kafka-1.0-SNAPSHOT-jar-with-dependencies.jar:../lib/jaguar-jdbc-2.0.jar:../lib/slf4j-simple-1.6.2.jar SQLRunner consumer
