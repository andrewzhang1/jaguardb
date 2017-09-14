#!/bin/bash


nohup java -cp lib/ojdbc6.jar:lib/jaguar-jdbc-2.0.jar:lib/dbsync.jar \
    -Dapp.conf=app.conf.oracle \
	com.jaguar.dbsync.Sync >> oracle_to_jaguar.log &

