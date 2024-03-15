ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.10"



lazy val root = (project in file("."))
  .settings(
    name := "Library"
  )

libraryDependencies += "org.mongodb.scala" %% "mongo-scala-driver" % "4.9.0"
val circeVersion = "0.14.5"


libraryDependencies += "com.typesafe.akka" %% "akka-http-spray-json" % "10.5.0"
libraryDependencies += "com.typesafe.akka" %% "akka-http" % "10.5.0"
libraryDependencies += "com.typesafe.akka" %% "akka-stream" % "2.8.0"
libraryDependencies +=  "com.rabbitmq" % "amqp-client" % "5.14.0"
libraryDependencies += "com.lightbend.akka" %% "akka-stream-alpakka-amqp" % "2.0.2"
libraryDependencies += "com.typesafe.play" %% "play-json" % "2.9.2"
libraryDependencies += "io.circe" %% "circe-core" % "0.14.1"
libraryDependencies += "io.circe" %% "circe-generic" % "0.14.1"
libraryDependencies += "io.circe" %% "circe-parser" % "0.14.1"

libraryDependencies += "org.json4s" %% "json4s-native" % "3.6.11"
libraryDependencies += "org.json4s" %% "json4s-jackson" % "3.6.11"
libraryDependencies += "de.heikoseeberger" %% "akka-http-json4s" % "1.37.0"

