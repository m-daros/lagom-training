package mdaros.training.lagom.mqtt.kafka.bridge.config

object ConfigurationKeys extends Enumeration {

  protected case class ConfigurationKey ( key: String ) extends super.Val

  val CLIENT_ID        = ConfigurationKey ( "client-id" )
  val TOPIC            = ConfigurationKey ( "topic" )
  val MQTT_BROKER_HOST = ConfigurationKey ( "mqtt-broker-host" )
  val MQTT_BROKER_PORT = ConfigurationKey ( "mqtt-broker-port" )
}