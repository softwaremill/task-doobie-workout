package io.ghostbuster91.workshop.taskdoobie.pool

import com.typesafe.scalalogging.StrictLogging
import io.ghostbuster91.workshop.taskdoobie.actor.{
  NewResponseMessage,
  PoolActor
}
import monix.eval.Task

class UpdateService(actor: PoolActor) extends StrictLogging {

  def onNewResponse(response: Response): Task[Unit] = {
    logger.info(s"Got new response $response")
    actor.offer(NewResponseMessage(response))
  }
}

case class Response(requestId: String, data: String)
