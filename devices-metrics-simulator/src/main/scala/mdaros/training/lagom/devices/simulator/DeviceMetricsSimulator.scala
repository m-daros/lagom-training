package mdaros.training.lagom.devices.simulator

import akka.actor.ActorSystem
import akka.event.Logging
import com.softwaremill.macwire.wire
import com.typesafe.config.ConfigFactory
import mdaros.training.lagom.devices.simulator.config.ConfigurationKeys.{ CLIENT_ID_PREFIX, DEVICES_COUNT }
import mdaros.training.lagom.devices.simulator.devices.DeviceMeasureGenerator
import mdaros.training.lagom.devices.simulator.sink.MqttSinkBuilder

import scala.concurrent.ExecutionContext
import scala.language.postfixOps

object DeviceMetricsSimulator extends App {

  val SERVICE_NAME = "devices-metrics-simulator"

  run ()

  def run (): Unit = {

    implicit val actorSystem = ActorSystem ( SERVICE_NAME )
    implicit val executionContext = ExecutionContext.Implicits.global

    val deviceMeasureGenerator = wire [ DeviceMeasureGenerator ]
    val mqttSinkBuilder        = wire [ MqttSinkBuilder ]

    val logger = Logging ( actorSystem, getClass )

    val config = ConfigFactory.load ( "application.conf" )
      .getConfig ( SERVICE_NAME )

    val numeDevices    = config.getInt ( DEVICES_COUNT.key )
    val clientIdPrefix = config.getString ( CLIENT_ID_PREFIX.key )

    val devices = 1 to numeDevices

    // Generate random measures for every simulated device
    devices.foreach ( index => {

      val clientId = s"${clientIdPrefix}${index}"
      val measuresSource = deviceMeasureGenerator.generateMeasures ( index, config )
      val mqttSink = mqttSinkBuilder.buildMqttSink ( config, index )

      // Run the flow
      measuresSource.runWith ( mqttSink )

      logger.info ( s"Added simulated device clientId: $clientId" )
    } )

    logger.info ( "DeviceMetricsSimulator started" )
  }
}