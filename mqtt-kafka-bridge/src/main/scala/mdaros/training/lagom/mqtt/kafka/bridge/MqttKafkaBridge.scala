package mdaros.training.lagom.mqtt.kafka.bridge

import akka.Done
import akka.actor.ActorSystem
import akka.stream.alpakka.mqtt.scaladsl.MqttSource
import akka.stream.alpakka.mqtt.{ MqttConnectionSettings, MqttMessage, MqttQoS, MqttSubscriptions }
import akka.stream.scaladsl.{ Keep, Sink, Source }
import com.typesafe.config.ConfigFactory
import mdaros.training.lagom.mqtt.kafka.bridge.config.ConfigurationKeys.{ CLIENT_ID, MQTT_BROKER_HOST, MQTT_BROKER_PORT, TOPIC }
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence

import scala.concurrent.{ ExecutionContext, Future }

object MqttKafkaBridge extends App {

  implicit val actorSystem = ActorSystem ( "mqtt-kafka-beidge" )
  implicit val executionContext = ExecutionContext.Implicits.global

  val config = ConfigFactory.load ( "application.conf" ).getConfig ( "mqtt-kafka-bridge" )

  val topic    = config.getString ( TOPIC.key )
  val mqttHost = config.getString ( MQTT_BROKER_HOST.key )
  val mqttPort = config.getInt ( MQTT_BROKER_PORT.key )
  val clientId = config.getString ( CLIENT_ID.key )

  // TODO automaticReconnect = true
  val connectionSettings = MqttConnectionSettings ( s"tcp://${ mqttHost }:${ mqttPort }", clientId, new MemoryPersistence )

  val mqttSource: Source [ MqttMessage, Future [ Done ] ] =
    MqttSource.atMostOnce (
      connectionSettings.withClientId ( clientId ),
      MqttSubscriptions ( Map ( topic -> MqttQoS.AtLeastOnce ) ),
      bufferSize = 8
    )

  // TODO Publish message to a Kafka topic
  val kafkaSink: Sink [MqttMessage, Future [Done] ] = Sink.foreach ( message => println ( s"RECEIVED ${message}" ) )

  val ( subscribed, streamResult ) = mqttSource
    .toMat ( kafkaSink ) ( Keep.both )
    .run ()
}