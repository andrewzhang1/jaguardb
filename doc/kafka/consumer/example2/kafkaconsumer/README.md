## Change configurations

Change boostrap servers for kafka in target/classes/consumer.props
After bootstrap.servers=, add kafka server IP address and port.
For example,

bootstrap.servers=localhost:9091
--consumer will read data from localhost at port 9091

bootstrap.servers=192.168.7.120:9091,192.168.7.108:9092
--consumer will first connect to the first kafka server in the list.
  If kafka consumer cannot connect to the first kafka server, it will
  connect to second kafka server in the list and consume data in the 
  second server.



## build program and run the fun

$ mvn clean package

$ bin/start_consumer.sh
