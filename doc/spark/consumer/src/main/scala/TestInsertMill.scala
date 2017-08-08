import java.sql.DriverManager

import org.apache.log4j.{Level, Logger}
import org.apache.spark.{SparkConf, SparkContext}

import scala.collection._

/**
	* Jaguar SQL:
	* 	create table jbench (key: uid char(16), addr char(32))
	*/

object TestInsertMill {

	def main(args: Array[String]) {
		if (args.length < 2) {
			println("Usage: java TestInsertMill nodeName rowNumber")
			sys.exit(1);
		}

		val prefix = args(0)
		val max = args(1).toInt
		sparkfunc(prefix, max)
	}

	def sparkfunc(prefix: String, max: Int)
	{
		Class.forName("com.jaguar.jdbc.JaguarDriver");
		val sparkConf = new SparkConf().setAppName("TestScalaJDBC")
		val sc = new SparkContext(sparkConf)
		val sqlContext = new org.apache.spark.sql.SQLContext(sc)
		import sqlContext.implicits._

		val url = "jdbc:jaguar://127.0.0.1:8888/test";

		val conn= DriverManager.getConnection(url, "admin", "jaguar" );
		val st = conn.prepareStatement("insert into jbench values (prepare_0000, prepare_0000)")
		st.executeUpdate()

		val st2 = conn.createStatement()
		var n = 1
		while( n < max ) {
			val sqlstr = "insert into jbench values (" + prefix + n + "," + prefix + n + ")"
			println(sqlstr)
			st2.executeUpdate(sqlstr)
			n = n + 1
		}

		// works
		val df = sqlContext.sql("SELECT count(*) FROM jbench")
		df.show()

	}

}

