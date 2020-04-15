package io.ghostbuster91.workshop.taskdoobie.pool

import java.util.UUID

import io.ghostbuster91.workshop.taskdoobie.db.Doobie._
import monix.eval.Task
import monix.reactive.Observable
import scala.concurrent.duration._

class GeneratorService(repository: SqlRequestPoolRepository,
                       xa: Transactor[Task],
                       updateService: UpdateService) {
  def generate(duration: FiniteDuration): Observable[Unit] = {
    Observable
      .intervalWithFixedDelay(duration)
      .mapEval { _ =>
        repository
          .takeRequest()
          .transact(xa)
          .flatMap {
            case Some(value) =>
              updateService.onNewResponse(
                Response(value, s"data-${UUID.randomUUID()}")
              )
            case None => Task.unit
          }
      }
  }
}
