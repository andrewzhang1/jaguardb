val scalaCompiler = "org.scala-lang" % "scala-compiler" % "2.10.4"
val sparkVersion = "1.6.3"

lazy val root = (project in file(".")).
  settings(
    name := "TestJDBC",
    version := "1.0",
    scalaVersion := "2.10.4"
  )

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-sql" % sparkVersion,
  "org.apache.spark" %% "spark-core" % sparkVersion,
  "org.apache.spark" %% "spark-streaming" % sparkVersion,
  "org.apache.spark" %% "spark-streaming-kafka" % sparkVersion
)



