organization in ThisBuild := "mdaros.training.lagom"
version in ThisBuild := "1.0-SNAPSHOT"

// the Scala version that will be used for cross-compiled libraries
scalaVersion in ThisBuild := "2.13.0"

val AkkaVersion = "2.6.14"
val JacksonVersion = "2.12.4"

val macwire = "com.softwaremill.macwire" %% "macros" % "2.3.3" % "provided"
val scalaTest = "org.scalatest" %% "scalatest" % "3.1.1" % Test

lazy val `lagom-training` = ( project in file ( "." ) )
  .aggregate ( `lagom-training-api`, `lagom-training-impl`, `lagom-training-stream-api`, `lagom-training-stream-impl`, `mqtt-kafka-bridge`, `data-model`, `devices-metrics-simulator` )

lazy val `lagom-training-api` = ( project in file ( "service1/lagom-training-api" ) )
  .settings (
    libraryDependencies ++= Seq (
      lagomScaladslApi
    )
  )

lazy val `lagom-training-impl` = ( project in file ( "service1/lagom-training-impl" ) )
  .enablePlugins ( LagomScala )
  .settings (
    libraryDependencies ++= Seq (
      lagomScaladslPersistenceCassandra,
      lagomScaladslKafkaBroker,
      lagomScaladslTestKit,
      macwire,
      scalaTest
    )
  )
  .settings ( lagomForkedTestSettings )
  .dependsOn ( `lagom-training-api` )

lazy val `lagom-training-stream-api` = ( project in file ( "service2/lagom-training-stream-api" ) )
  .settings (
    libraryDependencies ++= Seq (
      lagomScaladslApi
    )
  )

lazy val `lagom-training-stream-impl` = ( project in file ( "service2/lagom-training-stream-impl" ) )
  .enablePlugins ( LagomScala )
  .settings (
    libraryDependencies ++= Seq (
      lagomScaladslTestKit,
      macwire,
      scalaTest
    )
  )
  .dependsOn ( `lagom-training-stream-api`, `lagom-training-api` )


lazy val `mqtt-kafka-bridge` = ( project in file ( "mqtt-kafka-bridge" ) )
  .settings ( libraryDependencies ++=  Seq (
    "com.lightbend.akka" %% "akka-stream-alpakka-mqtt" % "3.0.2",
    "com.typesafe.akka" %% "akka-stream-kafka" % "2.1.0",
    "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
    "com.fasterxml.jackson.core" % "jackson-databind" % JacksonVersion,
    macwire,
    scalaTest
  ) )

lazy val `data-model` = ( project in file ( "data-model" ) )
  .settings ( libraryDependencies ++=  Seq ( scalaTest ) )

lazy val `devices-metrics-simulator` = ( project in file ( "devices-metrics-simulator" ) )
  .dependsOn ( `data-model` )
  .settings ( libraryDependencies ++=  Seq (
    "com.lightbend.akka" %% "akka-stream-alpakka-mqtt" % "3.0.2",
    "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
    "com.fasterxml.jackson.module" %% "jackson-module-scala" % JacksonVersion,
    "com.fasterxml.jackson.module" % "jackson-module-parameter-names" % JacksonVersion,
    "com.fasterxml.jackson.datatype" % "jackson-datatype-jdk8" % JacksonVersion,
    "com.fasterxml.jackson.datatype" % "jackson-datatype-jsr310" % JacksonVersion,
    "mdaros.training.lagom" %% "data-model" % "1.0-SNAPSHOT",
    macwire,
    scalaTest
  ) )