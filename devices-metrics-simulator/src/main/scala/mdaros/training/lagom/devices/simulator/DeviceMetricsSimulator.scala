package mdaros.training.lagom.devices.simulator

import akka.actor.ActorSystem
import akka.event.Logging
import akka.stream.alpakka.mqtt.MqttMessage
import akka.stream.scaladsl.Source
import akka.util.ByteString
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.softwaremill.macwire.wire
import com.typesafe.config.ConfigFactory
import mdaros.training.lagom.devices.simulator.config.ConfigurationKeys.{ CLIENT_ID_PREFIX, DEVICES_COUNT, TOPIC }
import mdaros.training.lagom.devices.simulator.sink.MqttSinkBuilder
import mdaros.training.lagom.model.Measure

import java.util.Date
import scala.concurrent.duration.DurationInt
import scala.concurrent.ExecutionContext
import scala.language.postfixOps

object DeviceMetricsSimulator extends App {

  implicit val actorSystem = ActorSystem ( "devices-metrics-simulator" )
  implicit val executionContext = ExecutionContext.Implicits.global

  val mqttSinkBuilder = wire [ MqttSinkBuilder ]

  val logger = Logging ( actorSystem, getClass )

  val config = ConfigFactory.load ( "application.conf" )
    .getConfig ( "devices-metrics-simulator" )

  val topic          = config.getString ( TOPIC.key )
  val numeDevices    = config.getInt ( DEVICES_COUNT.key )
  val clientIdPrefix = config.getString ( CLIENT_ID_PREFIX.key )

  val jsonMapper = JsonMapper.builder ()
    .addModule ( DefaultScalaModule )
    .build ()

  val randomGenerator = new scala.util.Random

  val devices = 1 to numeDevices

  // Generate random measures for every simulated device
  devices.foreach ( index => {

    val clientId = s"${clientIdPrefix}${index}"

    val measuresSource = Source.tick ( 0 seconds, 5 seconds, new Date () )
      .map ( timestamp => Seq ( Measure ( clientId, "cpu.usage", timestamp, randomGenerator.nextDouble () * 100 ),
                                Measure ( clientId, "mem.usage", timestamp, randomGenerator.nextDouble () * 100 ),
                                Measure ( clientId, "disk.usage", timestamp, randomGenerator.nextDouble () * 100 ) ) )
      .map ( measure => MqttMessage ( topic, ByteString ( jsonMapper.writer ().writeValueAsString ( measure ) ) ) )

    val mqttSink = mqttSinkBuilder.buildMqttSink ( config, index )

    // Run the flow
    measuresSource.runWith ( mqttSink )

    logger.info ( s"Added simulated device clientId: $clientId" )
  } )

  logger.info ( "DeviceMetricsSimulator started" )
}