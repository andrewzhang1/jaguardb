compiling:

1) install ant if not already. Make ant part of PATH

2) run ant


Testing:

0) assume the table is already created with correct columns in jaguar. The order of columns in the table is same as in the table in the source tatabase.

1) create/update a property file 'app.conf'

2) run: java -cp lib/mysql-connector-java-5.1.43-bin.jar:lib/jaguar-jdbc-2.0.jar:build/test.jar -Dapp.conf=app.conf com.jaguar.dbimport.Importer

