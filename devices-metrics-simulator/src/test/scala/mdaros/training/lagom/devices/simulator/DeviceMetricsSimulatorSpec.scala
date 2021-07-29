package mdaros.training.lagom.devices.simulator

import akka.NotUsed
import akka.actor.ActorSystem
import akka.event.Logging
import akka.stream.alpakka.mqtt.MqttMessage
import akka.stream.scaladsl.{ Sink, Source }
import akka.stream.testkit.TestSubscriber
import akka.stream.testkit.scaladsl.TestSink
import akka.util.ByteString
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.typesafe.config.{ Config, ConfigFactory }
import mdaros.training.lagom.devices.simulator.config.ConfigurationKeys.{ CLIENT_ID_PREFIX, DEVICES_COUNT }
import mdaros.training.lagom.devices.simulator.config.Configurator
import mdaros.training.lagom.devices.simulator.devices.DeviceMeasureGenerator
import mdaros.training.lagom.devices.simulator.sink.MqttSinkBuilder
import mdaros.training.lagom.model.Measure
import org.mockito.ArgumentMatchersSugar.any
import org.mockito.MockitoSugar.{ mock, when }
import org.scalatest.{ BeforeAndAfterAll, GivenWhenThen }
import org.scalatest.featurespec.AnyFeatureSpec

import java.util
import java.util.Date
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

class DeviceMetricsSimulatorSpec extends AnyFeatureSpec
  with GivenWhenThen
  with BeforeAndAfterAll {

  implicit val actorSystem = ActorSystem ( "DeviceMetricsSimulatorSpec" )

  val logger = Logging ( actorSystem, getClass )

  override def afterAll (): Unit = {

    actorSystem.terminate ()
  }

  Feature ( "DeviceMetricsSimulator" ) {

    Scenario ( "Generate device measures" ) {

      Given ( "a DeviceMetricsSimulator" )

        val jsonMapper = JsonMapper.builder ()
          .addModule ( DefaultScalaModule )
          .build ()

        val configValuesMock = new util.HashMap [ String, Any ] ()
        configValuesMock.put ( DEVICES_COUNT.key, 2 )
        configValuesMock.put ( CLIENT_ID_PREFIX.key, "my-client-id-" )

        val configMock = ConfigFactory.parseMap ( configValuesMock )

        val now = new Date ()

        val measure1Mock = Measure ( "my-client-id-1", "cpu.usage", now, 10.0067 )
        val measure2Mock = Measure ( "my-client-id-2", "mem.usage", now, 34.8976 )
        val measure3Mock = Measure ( "my-client-id-3", "disk.usage", now, 84.0056 )

        val measuresMock = Seq ( measure1Mock, measure2Mock, measure3Mock )

        val messagesMock = measuresMock.map ( measure => MqttMessage ( "my-topic", ByteString ( jsonMapper.writer ().writeValueAsString ( measure ) ) ) )

        val sourceMock: Source [ MqttMessage, NotUsed ] = Source ( messagesMock )
        val sinkMock: Sink [MqttMessage, TestSubscriber.Probe [ MqttMessage ] ] = TestSink [ MqttMessage ] ()

        val deviceMeasureGeneratorMock = mock [ DeviceMeasureGenerator ]
        val mqttSinkBuilderMock        = mock [ MqttSinkBuilder ]
        val configuratorMock           = mock [ Configurator ]

        when ( deviceMeasureGeneratorMock.generateMeasures ( any [ Int ], any [ Config ] ) )
          .thenReturn ( sourceMock )

        when ( mqttSinkBuilderMock.buildMqttSink ( any [ Int ], any [ Config ] ) )
          .thenReturn ( sinkMock )

        when ( configuratorMock.getConfig () )
          .thenReturn ( configMock )

        var testProbes = scala.collection.mutable.Buffer [TestSubscriber.Probe [ MqttMessage ]] ()

        val deviceMetricsSimulator = new DeviceMetricsSimulator () {

          // Override wiring of dependencies
          override val deviceMeasureGenerator = deviceMeasureGeneratorMock
          override val mqttSinkBuilder = mqttSinkBuilderMock
          override val configurator = configuratorMock

          override def runFlow ( measuresSource: Source [ MqttMessage, Any ], mqttSink: Sink [ MqttMessage, Any ] ): TestSubscriber.Probe [ MqttMessage ] = {

            val probe = sourceMock.runWith ( sinkMock )
            testProbes = testProbes :+ probe

            probe
          }
        }

      When ( "I run the device metrics simulator" )

        deviceMetricsSimulator.run ()

      Then ( "I expect all the measures are sent to the sink" )

        testProbes.foreach ( testProbe => {

          testProbe.request ( 3 )

          val message1 = testProbe.expectNext ( 3 second )
          val message2 = testProbe.expectNext ( 3 second )
          val message3 = testProbe.expectNext ( 3 second )

          assert ( message1.topic ==  "my-topic", s"Expected tppic: my-topic, but was ${message1.topic}" )
          assert ( message2.topic ==  "my-topic", s"Expected tppic: my-topic, but was ${message2.topic}" )
          assert ( message3.topic ==  "my-topic", s"Expected tppic: my-topic, but was ${message3.topic}" )

          assert ( jsonMapper.readValue ( message1.payload.utf8String, classOf [ Measure ] ) == measure1Mock, s"Expected payload $measure1Mock" )
          assert ( jsonMapper.readValue ( message2.payload.utf8String, classOf [ Measure ] ) == measure2Mock, s"Expected payload $measure2Mock" )
          assert ( jsonMapper.readValue ( message3.payload.utf8String, classOf [ Measure ] ) == measure3Mock, s"Expected payload $measure3Mock" )
        } )

        logger.info ( "Test OK" )
    }
  }
}