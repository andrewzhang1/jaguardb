#!/bin/bash

LIB=../../jdbc/lib

JARS=$LIB/postgresql-42.1.4.jar:$LIB/jaguar-jdbc-2.0.jar:lib/dbimport.jar 

nohup java -cp $JARS -Dapp.conf=app.conf.postgresql com.jaguar.dbimport.Importer >> import_postgresql.log &

