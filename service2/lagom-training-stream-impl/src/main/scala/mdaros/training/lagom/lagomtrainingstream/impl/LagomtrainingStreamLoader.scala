package mdaros.training.lagom.lagomtrainingstream.impl

import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.server._
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import play.api.libs.ws.ahc.AhcWSComponents
import mdaros.training.lagom.lagomtrainingstream.api.LagomtrainingStreamService
import mdaros.training.lagom.lagomtraining.api.LagomtrainingService
import com.softwaremill.macwire._

class LagomtrainingStreamLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new LagomtrainingStreamApplication(context) {
      override def serviceLocator: NoServiceLocator.type = NoServiceLocator
    }

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new LagomtrainingStreamApplication(context) with LagomDevModeComponents

  override def describeService = Some(readDescriptor[LagomtrainingStreamService])
}

abstract class LagomtrainingStreamApplication(context: LagomApplicationContext)
  extends LagomApplication(context)
    with AhcWSComponents {

  // Bind the service that this server provides
  override lazy val lagomServer: LagomServer = serverFor[LagomtrainingStreamService](wire[LagomtrainingStreamServiceImpl])

  // Bind the LagomtrainingService client
  lazy val lagomtrainingService: LagomtrainingService = serviceClient.implement[LagomtrainingService]
}
