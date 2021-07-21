package mdaros.training.lagom.mqtt.kafka.bridge.config

object ConfigurationKeys extends Enumeration {

  protected case class ConfigurationKey ( key: String ) extends super.Val

  val MQTT_CLIENT_ID          = ConfigurationKey ( "client-id" )
  val MQTT_TOPIC              = ConfigurationKey ( "topic" )
  val MQTT_BROKER_HOST        = ConfigurationKey ( "mqtt-broker-host" )
  val MQTT_BROKER_PORT        = ConfigurationKey ( "mqtt-broker-port" )
  val KAFKA_BOOTSTRAP_SERVERS = ConfigurationKey ( "bootstrap-servers" )
  val KAFKA_TOPIC             = ConfigurationKey ( "kafka-topic" )
}