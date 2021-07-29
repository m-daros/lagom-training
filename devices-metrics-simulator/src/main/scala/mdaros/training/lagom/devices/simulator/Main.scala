package mdaros.training.lagom.devices.simulator

import com.softwaremill.macwire.wire

object Main extends App {

  wire [ DeviceMetricsSimulator ].run ()
}