package io.ghostbuster91.workshop.taskdoobie.pool
import com.typesafe.scalalogging.StrictLogging
import io.ghostbuster91.workshop.taskdoobie.actor.{CheckMessage, PoolActor}
import monix.reactive.Observable

import scala.concurrent.duration._

class CheckService(actor: PoolActor) extends StrictLogging {

  def checkPoolInterval(interval: FiniteDuration): Observable[Unit] = {
    Observable
      .intervalWithFixedDelay(interval)
      .mapEval(_ => actor.offer(CheckMessage))
  }
}
