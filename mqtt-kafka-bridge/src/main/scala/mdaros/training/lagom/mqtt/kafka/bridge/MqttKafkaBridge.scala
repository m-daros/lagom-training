package mdaros.training.lagom.mqtt.kafka.bridge

import akka.actor.ActorSystem
import akka.event.Logging
import com.softwaremill.macwire.wire
import com.typesafe.config.ConfigFactory
import mdaros.training.lagom.mqtt.kafka.bridge.config.ConfigurationKeys.KAFKA_TOPIC
import mdaros.training.lagom.mqtt.kafka.bridge.sink.KafkaSinkBuilder
import mdaros.training.lagom.mqtt.kafka.bridge.source.MqttSourceBuilder
import org.apache.kafka.clients.producer.ProducerRecord

import scala.concurrent.ExecutionContext

object MqttKafkaBridge extends App {

  val SERVICE_NAME     = "mqtt-kafka-bridge"
  val CONFIG_FILE_NAME = "application.conf"

  implicit val actorSystem = ActorSystem ( SERVICE_NAME )
  implicit val executionContext = ExecutionContext.Implicits.global

  val mqttSourceBuilder = wire [ MqttSourceBuilder ]
  val kafkaSinkBuilder  = wire [ KafkaSinkBuilder ]

  val logger = Logging ( actorSystem, getClass )

  val mqttConfig  = ConfigFactory.load ( CONFIG_FILE_NAME )
    .getConfig ( SERVICE_NAME )
    .getConfig ( "mqtt-source" )

  val kafkaConfig = ConfigFactory.load ( CONFIG_FILE_NAME )
    .getConfig ( SERVICE_NAME )
    .getConfig ( "kafka-sink" )

  val mqttSource = mqttSourceBuilder.buildSource ( mqttConfig )
  val kafkaSink = kafkaSinkBuilder.buildSink ( kafkaConfig )

  val kafdkaTopic = kafkaConfig.getString ( KAFKA_TOPIC.key )

  // Start the flow
  mqttSource
    .map ( mqttMessage => mqttMessage.payload.utf8String )
    .map ( value => new ProducerRecord [ String, String ] ( kafdkaTopic, value ) )
    .runWith ( kafkaSink )

  logger.info ( "MQTT -> Kafka bridge started" )
}