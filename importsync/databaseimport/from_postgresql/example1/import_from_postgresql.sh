#!/bin/bash

LIB=../../../jdbc/lib

JARS=$LIB/postgresql-42.1.4.jar:$LIB/jaguar-jdbc-2.0.jar:lib/jdbcsql.jar 

nohup java -cp $JARS -Dapp.conf=app.conf.postgresql com.jaguar.jdbcsql.Importer >> import_postgresql.log &

