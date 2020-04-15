package io.ghostbuster91.workshop.taskdoobie.pool
import java.util.UUID

import com.typesafe.scalalogging.StrictLogging
import io.ghostbuster91.workshop.taskdoobie.db.Doobie._
import monix.eval.Task
import monix.reactive.Observable

import scala.concurrent.duration._

class CheckService(repository: SqlRequestPoolRepository,
                   xa: Transactor[Task],
                   targetSize: Int)
    extends StrictLogging {

  def checkPoolInterval(interval: FiniteDuration): Observable[Unit] = {
    Observable
      .intervalWithFixedDelay(interval)
      .mapEval(_ => checkPool())
  }

  def checkPool(): Task[Unit] = {
    (for {
      _ <- repository.lockPoolTable()
      currentSize <- repository.getCurrentSize()
      inFlight <- repository.requestsInFlight()
      _ <- addIfNecessary(currentSize, inFlight, targetSize)
    } yield ()).transact(xa)
  }

  private def addIfNecessary(currentSize: Int,
                             inFlight: Int,
                             targetSize: Int): ConnectionIO[Unit] = {
    val howManyMissing = targetSize - (currentSize + inFlight)
    logger.info(s"Current pool state - missing: $howManyMissing")
    if (howManyMissing < 0) {
      AsyncConnectionIO.raiseError(new RuntimeException("to many requests!!!"))
    } else {
      repository.addManyRequests(
        (0 until howManyMissing).map(_ => UUID.randomUUID().toString).toList
      )
    }
  }
}
