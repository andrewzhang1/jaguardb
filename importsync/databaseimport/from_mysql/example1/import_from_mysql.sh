#!/bin/bash

LIB=../../jdbc/lib
JARS=$LIB/mysql-connector-java-5.1.43-bin.jar:$LIB/jaguar-jdbc-2.0.jar:$LIB/dbimport.jar 

nohup java -cp $JARS -Dapp.conf=app.conf.mysql com.jaguar.dbimport.Importer >> import_mysql.log &

