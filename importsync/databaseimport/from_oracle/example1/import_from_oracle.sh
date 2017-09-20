#!/bin/bash

LIB=../../../jdbc/lib
JARS=$LIB/ojdbc6.jar:$LIB/jaguar-jdbc-2.0.jar:$LIB/jdbcsql.jar 
nohup java -cp $JARS -Dapp.conf=app.conf.oracle  com.jaguar.jdbcsql.Importer > import_oracle.log &

