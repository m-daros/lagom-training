package mdaros.training.lagom.devices.simulator.config

object ConfigurationKeys extends Enumeration {

  protected case class ConfigurationKey ( key: String ) extends super.Val

  val CLIENT_ID_PREFIX = ConfigurationKey ( "client-id-prefix" )
  val TOPIC            = ConfigurationKey ( "topic" )
  val DEVICES_COUNT    = ConfigurationKey ( "devices-count" )
  val MQTT_BROKER_HOST = ConfigurationKey ( "mqtt-broker-host" )
  val MQTT_BROKER_PORT = ConfigurationKey ( "mqtt-broker-port" )

}