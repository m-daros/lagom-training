package mdaros.training.lagom.lagomtraining.impl

import akka.cluster.sharding.typed.scaladsl.Entity
import com.lightbend.lagom.scaladsl.api.ServiceLocator
import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraPersistenceComponents
import com.lightbend.lagom.scaladsl.server._
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import play.api.libs.ws.ahc.AhcWSComponents
import mdaros.training.lagom.lagomtraining.api.LagomtrainingService
import com.lightbend.lagom.scaladsl.broker.kafka.LagomKafkaComponents
import com.lightbend.lagom.scaladsl.playjson.JsonSerializerRegistry
import com.softwaremill.macwire._

class LagomtrainingLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new LagomtrainingApplication(context) {
      override def serviceLocator: ServiceLocator = NoServiceLocator
    }

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new LagomtrainingApplication(context) with LagomDevModeComponents

  override def describeService = Some(readDescriptor[LagomtrainingService])
}

abstract class LagomtrainingApplication(context: LagomApplicationContext)
  extends LagomApplication(context)
    with CassandraPersistenceComponents
    with LagomKafkaComponents
    with AhcWSComponents {

  // Bind the service that this server provides
  override lazy val lagomServer: LagomServer = serverFor[LagomtrainingService](wire[LagomtrainingServiceImpl])

  // Register the JSON serializer registry
  override lazy val jsonSerializerRegistry: JsonSerializerRegistry = LagomtrainingSerializerRegistry

  // Initialize the sharding of the Aggregate. The following starts the aggregate Behavior under
  // a given sharding entity typeKey.
  clusterSharding.init(
    Entity(LagomtrainingState.typeKey)(
      entityContext => LagomtrainingBehavior.create(entityContext)
    )
  )

}
