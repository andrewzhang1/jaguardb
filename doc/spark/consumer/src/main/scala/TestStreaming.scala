import java.sql.DriverManager
import java.util.HashMap

import com.yammer.metrics.Metrics
import org.I0Itec.zkclient.serialize.ZkSerializer
import org.apache.kafka.clients.producer._
import org.apache.spark.{SparkContext, SparkConf}
import org.apache.spark.streaming._
import org.apache.spark.streaming.kafka._

import scala.util.Random

/**
  * Consumes messages from one or more topics in Kafka and does wordcount.
  * Usage: KafkaWordCount <zkQuorum> <group> <topics> <numThreads>
  *   <zkQuorum> is a list of one or more zookeeper servers that make quorum
  *   <group> is the name of kafka consumer group
  *   <topics> is a list of one or more kafka topics to consume from
  *   <numThreads> is the number of threads the kafka consumer should use
  *
  * Example:
  *    `$ bin/run-example \
  *      org.apache.spark.examples.streaming.KafkaWordCount zoo01,zoo02,zoo03 \
  *      my-consumer-group topic1,topic2 1`
  */

object TestStreaming {

  def main(args: Array[String]) {

    val jagaurTopic = "jaguar-sql-messages"
    kafkaConsume("192.168.7.120:2181", "test-group", jagaurTopic, 10)
  }

  def kafkaConsume(zkQuorum: String, group: String, topics: String, threads: Int)  {

    val sparkConf = new SparkConf().setAppName("KafkaWordCount")
    val streamCtx = new StreamingContext(sparkConf, Seconds(1))
    //streamCtx.checkpoint("checkpoints")

    val topicMap = topics.split(",").map((_, threads)).toMap

    val dstream = KafkaUtils.createStream(streamCtx, zkQuorum, group, topicMap)

    val jdb = new JaguarDB
	
    dstream.foreachRDD { rdd =>
	    val threadId = Thread.currentThread().getId()
	    println("=== In each RDD : rdd.id=" + rdd.id + " threadID=" + threadId + " =============")
	    println(rdd)

        rdd.foreach { record =>
                	val threadId2 = Thread.currentThread().getId()
	                println("*** In each record:  threadID2=" + threadId2 + " *************")
                    val sqlstr = "insert into test values (" + record._2 + ")"
	                println( sqlstr )
	                jdb.runSql(sqlstr)
        }

    }
 
    streamCtx.start()
    streamCtx.awaitTermination()

  }
}

class JaguarDB extends Serializable {
 
    Class.forName("com.jaguar.jdbc.JaguarDriver");
    @transient lazy val conn = DriverManager.getConnection("jdbc:jaguar://192.168.7.120:5555/test", "admin", "jaguar" );

    def runSql(sqlstr: String) {
    	val createStatement = conn.createStatement()
	    createStatement.executeUpdate(sqlstr)
    }
}

