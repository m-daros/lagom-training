package mdaros.training.lagom.devices.simulator

import akka.Done
import akka.actor.ActorSystem
import akka.stream.alpakka.mqtt.scaladsl.MqttSink
import akka.stream.alpakka.mqtt.{ MqttConnectionSettings, MqttMessage, MqttQoS }
import akka.stream.scaladsl.{ Sink, Source }
import akka.util.ByteString
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.typesafe.config.ConfigFactory
import mdaros.training.lagom.devices.simulator.config.ConfigurationKeys.{ CLIENT_ID_PREFIX, DEVICES_COUNT, MQTT_BROKER_HOST, MQTT_BROKER_PORT, TOPIC }
import mdaros.training.lagom.model.Measure
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence

import java.util.Date
import scala.concurrent.duration.DurationInt
import scala.concurrent.{ ExecutionContext, Future }
import scala.language.postfixOps

object DeviceMetricsSimulator extends App {

  implicit val actorSystem = ActorSystem ( "devices-metrics-simulator" )
  implicit val executionContext = ExecutionContext.Implicits.global

  val config = ConfigFactory.load ( "application.conf" ).getConfig ( "devices-metrics-simulator" )

  val topic          = config.getString ( TOPIC.key )
  val mqttHost       = config.getString ( MQTT_BROKER_HOST.key )
  val mqttPort       = config.getInt ( MQTT_BROKER_PORT.key )
  val clientIdPrefix = config.getString ( CLIENT_ID_PREFIX.key )
  val numeDevices    = config.getInt ( DEVICES_COUNT.key )

  val jsonMapper = JsonMapper.builder ()
    .addModule ( DefaultScalaModule )
    .build ()

  val randomGenerator = new scala.util.Random

  val devices = 1 to numeDevices

  // Generate random measures for every simulated device
  devices.foreach ( index => {

    val clientId = s"${clientIdPrefix}${index}"

    val connectionSettings = MqttConnectionSettings ( s"tcp://${ mqttHost }:${ mqttPort }", clientId, new MemoryPersistence )

    val measuresSource = Source.tick ( 0 seconds, 5 seconds, new Date () )
      .map ( timestamp => Seq ( Measure ( clientId, "cpu.usage", timestamp, randomGenerator.nextDouble () * 100 ),
                                Measure ( clientId, "mem.usage", timestamp, randomGenerator.nextDouble () * 100 ),
                                Measure ( clientId, "disk.usage", timestamp, randomGenerator.nextDouble () * 100 ) ) )
      .map ( measure => MqttMessage ( topic, ByteString ( jsonMapper.writer ().writeValueAsString ( measure ) ) ) )

    val mqttSink: Sink [ MqttMessage, Future [ Done ] ] = MqttSink ( connectionSettings, MqttQoS.ExactlyOnce )

    // Run the flow
    measuresSource.runWith ( mqttSink )
  } )
}