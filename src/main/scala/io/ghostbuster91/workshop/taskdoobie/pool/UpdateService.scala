package io.ghostbuster91.workshop.taskdoobie.pool

import com.typesafe.scalalogging.StrictLogging
import io.ghostbuster91.workshop.taskdoobie.db.Doobie._
import monix.eval.Task

class UpdateService(repository: SqlRequestPoolRepository, xa: Transactor[Task])
    extends StrictLogging {

  def onNewResponse(response: Response): Task[Unit] = {
    logger.info(s"Got new response $response")
    (for {
      _ <- repository.addToPool(response.data)
      _ <- repository.delete(response.requestId)
    } yield ()).transact(xa)
  }
}

case class Response(requestId: String, data: String)
