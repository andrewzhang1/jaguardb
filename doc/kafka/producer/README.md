## Change configurations

Change bootstrap servers for kafka in target/classes/producer.props
After 'bootstrap.servers=', add kafka servers and port.
For example, we can change like below:
bootstrap.servers=192.168.7.120:9091,192.168.7.108:9091

Kafka will first connect to the first kafka server in the list.
If Kafka cannot connect to the first kafka server, it will connect to
the second server and send produced data to the server.

## build program and run the fun

$ mvn clean package

$ bin/start_producer.sh
