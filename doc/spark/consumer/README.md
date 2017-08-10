## Add one more jar file to lib directory

$ wget https://d3kbcqa49mib13.cloudfront.net/spark-1.5.2-bin-hadoop2.6.tgz

$ tar -zxf spark-1.5.2-bin-hadoop2.6.tgz

$ cp spark-1.5.2-bin-hadoop2.6.tgz/lib/spark-assembly-1.5.2-hadoop2.6.0.jar ./lib/


## How to compile and run spark consumer
$ sbt clean package

$ bin/submit_spark.sh
