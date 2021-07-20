package mdaros.training.lagom.lagomtrainingstream.impl

import com.lightbend.lagom.scaladsl.api.ServiceCall
import mdaros.training.lagom.lagomtrainingstream.api.LagomtrainingStreamService
import mdaros.training.lagom.lagomtraining.api.LagomtrainingService

import scala.concurrent.Future

/**
  * Implementation of the LagomtrainingStreamService.
  */
class LagomtrainingStreamServiceImpl(lagomtrainingService: LagomtrainingService) extends LagomtrainingStreamService {
  def stream = ServiceCall { hellos =>
    Future.successful(hellos.mapAsync(8)(lagomtrainingService.hello(_).invoke()))
  }
}
