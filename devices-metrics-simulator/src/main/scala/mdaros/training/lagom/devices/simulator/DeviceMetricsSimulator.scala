package mdaros.training.lagom.devices.simulator

import akka.actor.ActorSystem
import akka.event.Logging
import akka.stream.alpakka.mqtt.MqttMessage
import akka.stream.scaladsl.{ Sink, Source }
import com.softwaremill.macwire.wire
import mdaros.training.lagom.devices.simulator.config.ConfigurationKeys.{ CLIENT_ID_PREFIX, DEVICES_COUNT }
import mdaros.training.lagom.devices.simulator.config.Configurator
import mdaros.training.lagom.devices.simulator.config.Configurator._
import mdaros.training.lagom.devices.simulator.devices.DeviceMeasureGenerator
import mdaros.training.lagom.devices.simulator.sink.MqttSinkBuilder

import scala.concurrent.ExecutionContext
import scala.language.postfixOps

class DeviceMetricsSimulator {

  val deviceMeasureGenerator = wire [ DeviceMeasureGenerator ]
  val mqttSinkBuilder        = wire [ MqttSinkBuilder ]
  val configurator           = wire [ Configurator ]

  implicit val actorSystem = ActorSystem ( SERVICE_NAME )
  implicit val executionContext = ExecutionContext.Implicits.global

  def run (): Unit = {

    val logger = Logging ( actorSystem, getClass )

    val config = configurator.getConfig ()

    val numeDevices    = config.getInt ( DEVICES_COUNT.key )
    val clientIdPrefix = config.getString ( CLIENT_ID_PREFIX.key )

    val devices = 1 to numeDevices

    // Generate random measures for every simulated device
    devices.foreach ( index => {

      val clientId = s"${clientIdPrefix}${index}"
      val measuresSource: Source [MqttMessage, Any] = deviceMeasureGenerator.generateMeasures ( index, config )
      val mqttSink: Sink [MqttMessage, Any] = mqttSinkBuilder.buildMqttSink ( index, config )

      // Run the flow
      runFlow ( measuresSource, mqttSink )

      logger.info ( s"Added simulated device clientId: $clientId" )
    } )

    logger.info ( "DeviceMetricsSimulator started" )
  }

  protected def runFlow ( measuresSource: Source [ MqttMessage, Any ], mqttSink: Sink [ MqttMessage, Any ] ): Any = {

    measuresSource.runWith ( mqttSink )
  }
}