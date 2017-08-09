          Kafka: Example
 
This example will illustrate how to load data from Kafka to Jaguar database. 

1. Start Zookeeper and Kafka servers on all hosts. 
   In kafka_howto, it is shown how to start Zookeeper and Kafka.

2. Start Jaguar servers on all hosts.

   $ cd $HOME/jaguar/bin
   $ ./jaguarstart_on_all_hosts.sh

   After a few seconds, you can login to Jaguar server and create a table.
   
   $ ./jag -u admin -p jaguar -d Example -h 192.168.7.120:5555

   User name, password, IP address, port number and database name may vary
   depending on your own case.

3. Create a table named pricetable in Jaguar.

   jaguar> create table pricetable ( key: item char(16), value: price bigint);
   
4. Start a Kafka producer.
   Open another terminal window for Kafka producer. Change directory to 
   jaguarkafka and create a topic called jaguar-kafka-Example and start
    Kafka producer.

   $ cd $HOME/jaguarkafka/bin
   $ ./kafka-topics.sh --create --zookeeper localhost:2181 \
     --replication-factor 1 --partitions 1 --topic jaguar-kafka-Example
   $ ./kafka-console-producer.sh --broker-list localhost:9092 \
     --topic jaguar-kafka-Example

5. Start a Kafka consumer. 
   Open another terminal windonw for Kafka consumer.
   
   $ cd doc/kafka/src/main/java
   $ vi SQLConsumer.java
   
   In the function of getSQLStatement() in SQLConsumer.java, there are two lines
   like below: 
   
   DataSource ds = new JaguarDataSource("192.168.7.120", 5555, "Example");
   Connection connection = ds.getConnection("admin", "jaguar");

   "192.168.7.120" is local IP address.
   "5555" is the listening port of Jaguar server.
   "Example" is the name of Jaguar database.
   "admin" and "jaguar" are respectively default user name and password that are used
   to login to Jaguar database.
   
   After finishing changing SQLConsumer.java, change boostrap servers for kafka in 
   kafka/consumer/src/main/resources/consumer.props.
   After bootstrap.servers=, add kafka server IP address and port.
   For example,

   bootstrap.servers=localhost:9092
   --consumer will read data from localhost at port 9092

   bootstrap.servers=192.168.7.120:9092,192.168.7.108:9092
   --consumer will first connect to the first kafka server in the list.
     If kafka consumer cannot connect to the first kafka server, it will
	 connect to second kafka server in the list and consume data in the
	 second server.

   These properties mentioned above should be changed according to your own case.
   After finishing changing these properties, change directory and compile.

   $ cd doc/kafka/consumer
   $ mvn clean package 
   
   Please make sure Maven is installed on local server.

   Start Kafka consumer by executing the following command:

   $ ./bin/start_consumer.sh

   Now the consumer is waiting for consuming messages typed from producer console.

6. Type some messages into the producer console to send to Kafka server. Some messages
   will show up in the consumer console. 

   In the producer console, we can type some messages below:
   Apple, 5
   Banana, 2
   Egg, 3
   Grape, 7
   Orange, 6
   Pear, 3
   Peach, 4

   In the consumer console, we can see some messages below:
   Got 7 messages after 0 timeouts
   Begin inserting 7 messages to Jaguar database:Insert (Apple,  5):
   INSERT INTO pricetable (item, price) VALUES ('Apple' , ' 5');
   Insert (Banana,  2):
   INSERT INTO pricetable (item, price) VALUES ('Banana' , ' 2');
   Insert (Egg,  3):
   INSERT INTO pricetable (item, price) VALUES ('Egg' , ' 3');
   Insert (Grape,  7):
   INSERT INTO pricetable (item, price) VALUES ('Grape' , ' 7');
   Insert (Orange,  6):
   INSERT INTO pricetable (item, price) VALUES ('Orange' , ' 6');
   Insert (Pear,  3):
   INSERT INTO pricetable (item, price) VALUES ('Pear' , ' 3');
   Insert (Peach,  4):
   INSERT INTO pricetable (item, price) VALUES ('Peach' , ' 4');

   Now 7 messages are loaded into Jaguar database.

7. Select all messages loaded from Table pricetable in Jaguar database.
   jaguar> select * from pricetable;
   item:[Banana] price:[0002] 
   item:[Orange] price:[0006] 
   item:[Apple] price:[0005] 
   item:[Egg] price:[0003] 
   item:[Grape] price:[0007] 
   item:[Peach] price:[0004] 
   item:[Pear] price:[0003] 
   
   The Order above may vary.

Some clarifications and explanations:
1. We start a Kafka producer manually but start a consumer by running JAVA
   program calling Jaguar API and Kafka API to achieve data loading from
   Kafka to Jaguar database.

2. Messages typed from Kafka producer are processed before insert into Jaguar
   database. For example, one message, "Banana,2" is meaningless for Jaguar. 
   We have to use this message below to load it to Jaguar database: 
   INSERT INTO pricetable (item, price) VALUES ('Banana' , ' 2');
   By running JAVA program calling Jaguar API, the new statement will be passed
   to Jaguar and Jaguar knows how to deal with the original message "Banana,2".
   No matter what format original message are in, we have to convert them to 
   Jaguar SQL statements. Please read Jaguar manual for more details.
