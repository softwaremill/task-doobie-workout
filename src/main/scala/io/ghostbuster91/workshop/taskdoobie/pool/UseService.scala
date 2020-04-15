package io.ghostbuster91.workshop.taskdoobie.pool

import io.ghostbuster91.workshop.taskdoobie.db.Doobie._
import monix.eval.Task
import monix.reactive.Observable
import scala.concurrent.duration._

class UseService(requestPoolRepository: SqlRequestPoolRepository,
                 xa: Transactor[Task]) {

  def usingInterval(duration: FiniteDuration) = {
    Observable
      .intervalWithFixedDelay(duration)
      .mapEval { _ =>
        requestPoolRepository.useSingleData().transact(xa)
      }
  }
}
