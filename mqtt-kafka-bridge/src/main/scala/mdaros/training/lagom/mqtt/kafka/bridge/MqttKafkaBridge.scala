package mdaros.training.lagom.mqtt.kafka.bridge

import akka.Done
import akka.actor.ActorSystem
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.stream.alpakka.mqtt.scaladsl.MqttSource
import akka.stream.alpakka.mqtt.{ MqttConnectionSettings, MqttMessage, MqttQoS, MqttSubscriptions }
import akka.stream.scaladsl.{ Keep, Sink, Source }
import com.typesafe.config.ConfigFactory
import mdaros.training.lagom.mqtt.kafka.bridge.config.ConfigurationKeys.{ MQTT_CLIENT_ID, KAFKA_BOOTSTRAP_SERVERS, KAFKA_TOPIC, MQTT_BROKER_HOST, MQTT_BROKER_PORT, MQTT_TOPIC }
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence

import scala.concurrent.{ ExecutionContext, Future }

object MqttKafkaBridge extends App {

  implicit val actorSystem = ActorSystem ( "mqtt-kafka-beidge" )
  implicit val executionContext = ExecutionContext.Implicits.global

  val mqttConfig  = ConfigFactory.load ( "application.conf" )
    .getConfig ( "mqtt-kafka-bridge" )
    .getConfig ( "mqtt-source" )

  val kafkaConfig = ConfigFactory.load ( "application.conf" )
    .getConfig ( "mqtt-kafka-bridge" )
    .getConfig ( "kafka-sink" )

  val mqttTopic    = mqttConfig.getString ( MQTT_TOPIC.key )
  val mqttHost     = mqttConfig.getString ( MQTT_BROKER_HOST.key )
  val mqttPort     = mqttConfig.getInt ( MQTT_BROKER_PORT.key )
  val mqttClientId = mqttConfig.getString ( MQTT_CLIENT_ID.key )

  val kafkaProducerSettings = ProducerSettings ( kafkaConfig, new StringSerializer, new StringSerializer )
      .withBootstrapServers ( kafkaConfig.getString ( KAFKA_BOOTSTRAP_SERVERS.key ) )

  // TODO automaticReconnect = true
  val mqttConnectionSettings = MqttConnectionSettings ( s"tcp://${ mqttHost }:${ mqttPort }", mqttClientId, new MemoryPersistence )

  val mqttSource: Source [ MqttMessage, Future [ Done ] ] =
    MqttSource.atMostOnce (
      mqttConnectionSettings.withClientId ( mqttClientId ),
      MqttSubscriptions ( Map ( mqttTopic -> MqttQoS.AtLeastOnce ) ),
      bufferSize = 8
    )

  // Publish messages to a Kafka topic
  val kafkaSink = Producer.plainSink ( kafkaProducerSettings )

  val kafdkaTopic = kafkaConfig.getString ( KAFKA_TOPIC.key )

  // Start the flow
  mqttSource
    .map ( mqttMessage => mqttMessage.payload.utf8String )
    .map ( value => new ProducerRecord [ String, String ] ( kafdkaTopic, value ) )
    .runWith ( kafkaSink )
}