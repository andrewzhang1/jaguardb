#!/bin/bash

LIB=../../jdbc/lib
JARS=$LIB/ojdbc6.jar:$LIB/jaguar-jdbc-2.0.jar:$LIB/dbimport.jar 
nohup java -cp $JARS -Dapp.conf=app.conf.oracle  com.jaguar.dbimport.Importer > import_oracle.log &

