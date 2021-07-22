package mdaros.training.lagom.mqtt.kafka.bridge.source

import akka.Done
import akka.stream.alpakka.mqtt.{ MqttConnectionSettings, MqttMessage, MqttQoS, MqttSubscriptions }
import akka.stream.alpakka.mqtt.scaladsl.MqttSource
import akka.stream.scaladsl.Source
import com.typesafe.config.Config
import mdaros.training.lagom.mqtt.kafka.bridge.config.ConfigurationKeys.{ MQTT_BROKER_HOST, MQTT_BROKER_PORT, MQTT_CLIENT_ID, MQTT_TOPIC }
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence

import scala.concurrent.Future

class MqttSourceBuilder {

  def buildSource ( mqttConfig: Config ): Source [MqttMessage, Future [Done]] = {

    val mqttTopic    = mqttConfig.getString ( MQTT_TOPIC.key )
    val mqttHost     = mqttConfig.getString ( MQTT_BROKER_HOST.key )
    val mqttPort     = mqttConfig.getInt ( MQTT_BROKER_PORT.key )
    val mqttClientId = mqttConfig.getString ( MQTT_CLIENT_ID.key )

    // TODO automaticReconnect = true
    val mqttConnectionSettings = MqttConnectionSettings ( s"tcp://${ mqttHost }:${ mqttPort }", mqttClientId, new MemoryPersistence )

    MqttSource.atMostOnce (
        mqttConnectionSettings.withClientId ( mqttClientId ),
        MqttSubscriptions ( Map ( mqttTopic -> MqttQoS.AtLeastOnce ) ),
        bufferSize = 8
      )
  }
}