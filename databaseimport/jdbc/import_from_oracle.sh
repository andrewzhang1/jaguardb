#!/bin/bash

JARS=lib/postgresql-42.1.4.jar:lib/ojdbc6.jar:lib/mysql-connector-java-5.1.43-bin.jar:lib/jaguar-jdbc-2.0.jar:lib/dbimport.jar 

nohup java -cp $JARS -Dapp.conf=app.conf.oracle  com.jaguar.dbimport.Importer >> import_oracle.log &

