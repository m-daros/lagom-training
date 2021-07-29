package mdaros.training.lagom.devices.simulator.config

import com.typesafe.config.{ Config, ConfigFactory }
import mdaros.training.lagom.devices.simulator.config.Configurator.SERVICE_NAME

class Configurator {

  def getConfig (): Config = {

    ConfigFactory.load ( "application.conf" )
      .getConfig ( SERVICE_NAME )
  }
}

object Configurator {

  val SERVICE_NAME = "devices-metrics-simulator"
}