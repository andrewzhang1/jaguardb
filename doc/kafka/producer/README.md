## Change configurations

Change bootstrap servers for kafka in target/classes/producer.props
After 'bootstrap.servers=', add kafka servers and port.
For example, we can change like below:
bootstrap.servers=192.168.7.120:9091,192.168.7.108:9092

Producer will put data in those two kafka servers.


## build program and run the fun

$ mvn clean package

$ bin/start_producer.sh
