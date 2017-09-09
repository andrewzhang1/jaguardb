
Java program to sync data from any database that supports JDBC

Compiling:
===================================================================

1) install ant if not already. Make ant part of PATH
	export PATH=$PATH:../../util/ant/bin


2) run ant


Testing:
===================================================================

0) assume the table is already created with correct columns in the target database. The order of 
columns in the table is same as in the source database.

1) create/update a property file 'app.conf'

2) run: 

    java -cp lib/mysql-connector-java-5.1.43-bin.jar:lib/jaguar-jdbc-2.0.jar:build/dbsync.jar -Dapp.conf=app.conf com.jaguar.dbsync.Sync

