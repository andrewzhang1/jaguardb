#!/bin/bash

nohup java -cp lib/mysql-connector-java-5.1.43-bin.jar:lib/jaguar-jdbc-2.0.jar:lib/dbsync.jar \
  -Dapp.conf=app.conf.mysql \
  com.jaguar.dbsync.Sync >> mysql_to_jaguar.log &


