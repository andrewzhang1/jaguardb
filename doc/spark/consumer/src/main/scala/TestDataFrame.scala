import java.sql.DriverManager

import org.apache.log4j.{Level, Logger}
import org.apache.spark.{SparkConf, SparkContext}

import scala.collection._

/**
	* Jaguar SQL:
	* 	create table jdataframe (key: uid char(16), addr char(32))
	*/

object TestDataFrame {

	def main(args: Array[String]) {
		sparkfunc()
	}

	def sparkfunc()
	{
		Class.forName("com.jaguar.jdbc.JaguarDriver");
		val sparkConf = new SparkConf().setAppName("TestDataFrame")
		val sc = new SparkContext(sparkConf)
		val sqlContext = new org.apache.spark.sql.SQLContext(sc)
		import sqlContext.implicits._

		val url = "jdbc:jaguar://127.0.0.1:8888/test";
		val jdataframe = sqlContext.read.format("jdbc")
		   .options(
		       Map( "url" -> url,
						 "dbtable" -> "jdataframe",
						 "user" -> "admin",
						 "password" -> "jaguar",
//						 "partitionColumn" -> "k1",
//						 "lowerBound" -> "2",
//						 "upperBound" -> "2000000",
//						 "numPartitions" -> "4",
						 "driver" -> "com.jaguar.jdbc.JaguarDriver"
		   )).load()
		    
		// works fine
		jdataframe.registerTempTable("jbench")
		jdataframe.printSchema()

		jdataframe.sort("k2").show()
		jdataframe.sort($"k2".desc).show()
		jdataframe.sort($"k2".desc, $"k1".asc).show()
		jdataframe.orderBy($"k2".desc, $"k1".asc).show()
		jdataframe.toJSON.coalesce(1).saveAsTextFile( "/tmp/jbench.txt" );

		// works fine
		jdataframe.groupBy("addr").count().show()

		// works fine
		jdataframe.groupBy("addr").avg().show()

		// works great 
		jdataframe.stat.freqItems( Seq("addr") ).show()

		jdataframe.sample( true, 0.1, 100 ).show()
		jdataframe.distinct.show()
		jdataframe.dropDuplicates().show()
		jdataframe.dropDuplicates.show()

		// works fine
		jdataframe.dropDuplicates.cache.show()
		jdataframe.dropDuplicates.persist.show()

		// works
		val df = sqlContext.sql("SELECT * FROM jdataframe where addr between 'NODE6' and 'NODE7' ")
		df.distinct.show()

	}

}

