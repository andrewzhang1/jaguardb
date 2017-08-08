import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import scala.collection._
import org.apache.spark.sql._
import org.apache.spark.sql.types._
import org.apache.log4j.Logger
import org.apache.log4j.Level
import com.jaguar.jdbc.internal.jaguar._
import com.jaguar.jdbc.JaguarDataSource
import org.apache.hadoop.io._
import org.apache.hadoop.util._;

import java.sql.{Connection, DriverManager, ResultSet}


object TestScalaJDBC {
    def main(args: Array[String]) {
		if ( true ) {
			sparkfunc()
		} else {
			sparkfunc2()
		}
    }

	def sparkfunc()
	{
		Class.forName("com.jaguar.jdbc.JaguarDriver");
		val sparkConf = new SparkConf().setAppName("TestScalaJDBC")
		val sc = new SparkContext(sparkConf)
		val sqlContext = new org.apache.spark.sql.SQLContext(sc)
		import sqlContext.implicits._

		val hadoopConf = new org.apache.hadoop.conf.Configuration()
		val hdfs = org.apache.hadoop.fs.FileSystem.get(new java.net.URI("hdfs://localhost:9000"), hadoopConf)

		// Logger.getLogger("org").setLevel(Level.OFF)
		// Logger.getLogger("akka").setLevel(Level.OFF)

		val url = "jdbc:jaguar://127.0.0.1:8888/test";

		val people = sqlContext.read.format("jdbc")
		   .options(
		       Map( "url" -> "jdbc:jaguar://127.0.0.1:8888/test",
			        "dbtable" -> "int10k",
					"user" -> "admin",
					"password" -> "jaguar",
					"partitionColumn" -> "k1",
					"lowerBound" -> "2",
					"upperBound" -> "2000000",
					"numPartitions" -> "4",
					"driver" -> "com.jaguar.jdbc.JaguarDriver"
		   )).load()
		    
		// work fine
		people.registerTempTable("int10k")
		people.printSchema()

		val people2 = sqlContext.read.format("jdbc")
		   .options(
		       Map( "url" -> "jdbc:jaguar://127.0.0.1:8888/test",
			        "dbtable" -> "int10k_2",
					"user" -> "admin",
					"password" -> "jaguar",
					"partitionColumn" -> "k1",
					"lowerBound" -> "2",
					"upperBound" -> "2000000",
					"numPartitions" -> "4",
					"driver" -> "com.jaguar.jdbc.JaguarDriver"
		   )).load()
		people2.registerTempTable("int10k_2")
		people2.printSchema();

		// works fine
		if ( false )  {
			val output = "/tmp/sparkout.txt";
			try { hdfs.delete(new org.apache.hadoop.fs.Path(output), true) } 
			catch { case _ : Throwable => { } }

			people.sort("k2").show()
			people.sort($"k2".desc).show()
			people.sort($"k2".desc, $"k1".asc).show()
			people.orderBy($"k2".desc, $"k1".asc).show()
			// people.rdd.coalesce(1).saveAsTextFile("/tmp/sparkout.txt");
			// people.coalesce(1).rdd.saveAsTextFile("/tmp/sparkout.txt");
			people.toJSON.coalesce(1).saveAsTextFile( output );
		}

		// works fine
		// people.selectExpr("k2", "k1"  ).show()
		// people.selectExpr("k2", "k1 as keyone"  ).show()
		people.selectExpr("k2", "k1 as keyone", "abs(k2)" ).show()

		val conn= DriverManager.getConnection(url, "admin", "jaguar" );
		val st = conn.prepareStatement("insert into jbench values (qqq, qqqqq)")
		st.executeUpdate()

		val st2 = conn.createStatement()
		st2.executeUpdate( "insert into jbench values (qqq22, qqqqq222)" )
		st2.executeUpdate( "insert into jbench values (qqq23, qqqqq222)" )
		st2.executeUpdate( "update jbench set addr=999999 where uid=qqq22" )
		st2.executeUpdate( "delete from jbench where uid=qqq23" )


		// bad
		// people.insertInto( "int10k_2", false );
		// people.write().jdbc(url, "int10k_2", true );
		people.insertIntoJDBC(url, "int10k_2", true );


		// workd fine
		val k12 = people.select("k1", "k2")
		k12.show();

		// workd fine
		val sk1 = people.select("k1")
		sk1.show();

		// workd fine
		val sk2 = people.select("k2")
		sk2.show();
		
		// works fine
		val below60 = people.filter(people("k1") <= 3927570 ).show()

		// works fine
		people.groupBy("addr").count().show()

		// works fine
		people.groupBy("addr").avg().show()

		// bad
		/***
        people.groupBy(people("addr"))
		       .agg(
			   		Map(
						 "k2" -> "avg",
						 "k1" -> "max"
					   )
				   )
			   .show();
		   **/

		// bad
		/**
        people.rollup("addr").avg().show()
        people.rollup($"addr")
		       .agg(
			   		Map(
						 "k1" -> "avg",
						 "k2" -> "max"
					   )
				   )
			   .show();
		

        people.cube($"addr").avg().show()
        people.cube($"addr")
		       .agg(
			   		Map(
						 "k1" -> "avg",
						 "k2" -> "max"
					   )
				   )
			   .show();
		  **/

		// cannot compile
		// people.agg( max($"k1"), avg($"k2") ).show()
		// .agg is short for groupBy().agg()
		// people.groupBy().agg(max($"k2"), avg($"k1"))

		// works great
		people.describe( "k1", "k2").show()

		// works great 
		people.stat.freqItems( Seq("k1") ).show()

		// works great  
		people.join( people2, "k1" ).show()
		people.join( people2, "k2" ).show()

		// not compile people.join( people2, Seq("k2", "addr") ).show()
		// not compile people.join( people2, Seq("k1", "k2") ).show()
		// not compile people.join( people2).where ( $"people.k1" === $"people2.k1" ).show()

		// works fine
		people.join(people2).where ( people("k1") === people2("k1")  ).show()

		// works fine
		people.join(people2).where ( people("addr") === people2("addr")  ).show()
		
		// works fine
		people.join(people2).where ( people("k1") === people2("k1") and people("addr") === people2("addr")  ).show()
		people.join(people2).where ( people("k1") === people2("k1") && people("addr") === people2("addr") ).show()
		people.join(people2).where ( people("k1") === people2("k1") && people("addr") === people2("addr") )
			.limit(3).show()

		// works fine
		people.unionAll(people2).show()
		people.intersect(people2).show()
		people.except(people2).show()
		people.sample( true, 0.1, 100 ).show()
		people.distinct.show()
		people.dropDuplicates().show()
		people.dropDuplicates.show()

		// works fine
		people.dropDuplicates.cache.show()
		people.dropDuplicates.persist.show()

		// not compile
		// val inf = people.inputFiles()

		// save fails with table must be temp table error
		// people.show()
		// bad people.write.mode(SaveMode.Append).saveAsTable("int10k_3")
		// bad people.write.saveAsTable("int10k_3")

		// bad val df = sqlContext.sql("SELECT * FROM int10k where k1 < 200000000 ")
		// OK val df = sqlContext.sql("SELECT k1, k2, addr FROM int10k where k1 < 200000000 ")
        // df.groupBy("addr").count.show()
		// bad val df = sqlContext.sql("SELECT k2, addr, k1 FROM int10k where k1 < 200000000 ")

		// works
		val df = sqlContext.sql("SELECT * FROM int10k where k1 < 200000000 and addr between 'JDHJDH' and 'ZBJDDJDJKDJKJDKJKD' ")
		df.distinct.show()
	}

	def sparkfunc2()
	{
        Class.forName("com.jaguar.jdbc.JaguarDriver");
        val sparkConf = new SparkConf().setAppName("TestScalaJDBC")
        val sc = new SparkContext(sparkConf)
        val sqlContext = new org.apache.spark.sql.SQLContext(sc)
		import sqlContext.implicits._

		val hadoopConf = new org.apache.hadoop.conf.Configuration()
		val hdfs = org.apache.hadoop.fs.FileSystem.get(new java.net.URI("hdfs://localhost:9000"), hadoopConf)

		Logger.getLogger("org").setLevel(Level.OFF)
		Logger.getLogger("akka").setLevel(Level.OFF)

		val people = sqlContext.read.format("jdbc")
		   .options(
		       Map( "url" -> "jdbc:jaguar://127.0.0.1:8888/test",
			        "dbtable" -> "tm3",
					"user" -> "admin",
					"password" -> "jaguar",
					"partitionColumn" -> "uid",
					"lowerBound" -> "2",
					"upperBound" -> "2000000",
					"numPartitions" -> "4",
					"driver" -> "com.jaguar.jdbc.JaguarDriver"
		   )).load()
		    
		// work fine
		people.registerTempTable("tm3")
		people.printSchema()


		// works
		val df = sqlContext.sql("SELECT uid, sum(amt) as amt_sum FROM tm3 where daytime > '2014-01-01 00:00:00' and daytime < '2014-03-01 0:0:0' group by uid ")
        // df.distinct.show()
		df.rdd.map( row=> "uid: " + row(0) ).collect().foreach( println)
		df.foreach( println)

        df.show()
		val output = "/tmp/sparkout.txt";
		df.toJSON.coalesce(1).saveAsTextFile( output );
	}

}

