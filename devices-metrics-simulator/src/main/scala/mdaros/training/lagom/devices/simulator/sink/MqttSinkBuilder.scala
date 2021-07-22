package mdaros.training.lagom.devices.simulator.sink

import akka.Done
import akka.stream.alpakka.mqtt.scaladsl.MqttSink
import akka.stream.alpakka.mqtt.{ MqttConnectionSettings, MqttMessage, MqttQoS }
import akka.stream.scaladsl.Sink
import com.typesafe.config.Config
import mdaros.training.lagom.devices.simulator.config.ConfigurationKeys.{ CLIENT_ID_PREFIX, MQTT_BROKER_HOST, MQTT_BROKER_PORT }
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence

import scala.concurrent.Future

class MqttSinkBuilder {

  def buildMqttSink ( config: Config, index: Int ): Sink [ MqttMessage, Future [ Done ] ] = {

    val mqttHost       = config.getString ( MQTT_BROKER_HOST.key )
    val mqttPort       = config.getInt ( MQTT_BROKER_PORT.key )
    val clientIdPrefix = config.getString ( CLIENT_ID_PREFIX.key )

    val clientId = s"${clientIdPrefix}${index}"
    val mqttConnectionSettings = MqttConnectionSettings ( s"tcp://${ mqttHost }:${ mqttPort }", clientId, new MemoryPersistence )

    MqttSink ( mqttConnectionSettings, MqttQoS.ExactlyOnce )
  }
}