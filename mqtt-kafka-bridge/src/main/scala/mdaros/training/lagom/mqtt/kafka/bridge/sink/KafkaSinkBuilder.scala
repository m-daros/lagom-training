package mdaros.training.lagom.mqtt.kafka.bridge.sink

import akka.Done
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.stream.scaladsl.Sink
import com.typesafe.config.Config
import mdaros.training.lagom.mqtt.kafka.bridge.config.ConfigurationKeys.KAFKA_BOOTSTRAP_SERVERS
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer

import scala.concurrent.Future

class KafkaSinkBuilder {

  def buildSink ( kafkaConfig: Config ): Sink [ProducerRecord [String, String], Future [Done]] = {

    val kafkaProducerSettings = ProducerSettings ( kafkaConfig, new StringSerializer, new StringSerializer )
      .withBootstrapServers ( kafkaConfig.getString ( KAFKA_BOOTSTRAP_SERVERS.key ) )

    // Publish messages to a Kafka topic
    Producer.plainSink ( kafkaProducerSettings )
  }
}