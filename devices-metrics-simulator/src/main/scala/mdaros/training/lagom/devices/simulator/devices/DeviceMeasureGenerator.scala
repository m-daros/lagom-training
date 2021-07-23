package mdaros.training.lagom.devices.simulator.devices

import akka.actor.Cancellable
import akka.stream.alpakka.mqtt.MqttMessage
import akka.stream.scaladsl.Source
import akka.util.ByteString
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.typesafe.config.Config
import mdaros.training.lagom.devices.simulator.config.ConfigurationKeys.{ CLIENT_ID_PREFIX, TOPIC }
import mdaros.training.lagom.model.Measure

import java.util.Date
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

class DeviceMeasureGenerator {

  private val jsonMapper = JsonMapper.builder ()
    .addModule ( DefaultScalaModule )
    .build ()

  private val randomGenerator = new scala.util.Random

  def generateMeasures ( index: Int, config: Config ): Source[MqttMessage, Cancellable] = {

    val topic          = config.getString ( TOPIC.key )
    val clientIdPrefix = config.getString ( CLIENT_ID_PREFIX.key )

    val clientId = s"${clientIdPrefix}${index}"

    Source.tick ( 0 seconds, 5 seconds, new Date () )
      .map ( timestamp => Seq ( Measure ( clientId, "cpu.usage", timestamp, randomGenerator.nextDouble () * 100 ),
        Measure ( clientId, "mem.usage", timestamp, randomGenerator.nextDouble () * 100 ),
        Measure ( clientId, "disk.usage", timestamp, randomGenerator.nextDouble () * 100 ) ) )
      .map ( measure => MqttMessage ( topic, ByteString ( jsonMapper.writer ().writeValueAsString ( measure ) ) ) )
  }
}