#!/bin/bash
port=$1
export LD_LIBRARY_PATH=/home/dev2/jaguar/lib
ulimit -c unlimited
myclass=EgiftStreaming

MY_LIB=/home/dev2/jaguar_spark/lib/

JARS=$MY_LIB/spark-streaming_2.10-1.4.0.jar,$MY_LIB/spark-streaming-kafka_2.10-1.4.0.jar,$MY_LIB/kafka_2.10-0.8.2.1.jar,$MY_LIB/zkclient-0.3.jar,$MY_LIB/metrics-core-2.2.0.jar,$MY_LIB/kafka-clients-0.8.2.1.jar,$MY_LIB/jaguar-jdbc-2.0.jar

PROJJAR=/home/dev2/jaguar_spark/target/scala-2.10/testjdbc_2.10-1.0.jar

export SPARK_HOME=/opt/cloudera/parcels/CDH-5.9.0-1.cdh5.9.0.p0.23/lib/spark

date
t1=`date +'%s'`

echo "run $1"

/opt/cloudera/parcels/CDH-5.9.0-1.cdh5.9.0.p0.23/bin/spark-submit --conf spark.ui.port=$port \
    --class $myclass \
    --jars $JARS \
    --master local[16] \
    --driver-library-path $LD_LIBRARY_PATH \
    $PROJJAR


t2=`date +'%s'`
((dt=t2-t1))
date
echo "Total $t1 --- $t2  $dt seconds"

## --conf "yarn.nodemanager.delete.debug-delay-sec=36000" 
## --master local[3] \
## --master yarn \
