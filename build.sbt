ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.14"

lazy val root = (project in file("."))
  .settings(
    name := "library-api"
  )


libraryDependencies += "org.mongodb.scala" %% "mongo-scala-driver" % "4.9.0"
val circeVersion = "0.14.5"


libraryDependencies += "com.typesafe.akka" %% "akka-http-spray-json" % "10.5.0"
libraryDependencies += "com.typesafe.akka" %% "akka-http" % "10.5.0"
libraryDependencies += "com.typesafe.akka" %% "akka-stream" % "2.8.0"
libraryDependencies += "com.rabbitmq" % "amqp-client" % "5.16.0"
libraryDependencies += "org.apache.commons" % "commons-email" % "1.5"
libraryDependencies += "com.sun.mail" % "javax.mail"%"1.6.2"

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case PathList("reference.conf") => MergeStrategy.concat
  case x => MergeStrategy.first
}