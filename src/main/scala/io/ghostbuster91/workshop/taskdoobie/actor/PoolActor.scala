package io.ghostbuster91.workshop.taskdoobie.actor

import java.util.UUID

import com.typesafe.scalalogging.StrictLogging
import doobie.util.transactor.Transactor
import io.ghostbuster91.workshop.taskdoobie.db.Doobie.{
  AsyncConnectionIO,
  ConnectionIO,
  _
}
import io.ghostbuster91.workshop.taskdoobie.pool.{
  Response,
  SqlRequestPoolRepository
}
import monix.eval.Task
import monix.execution.AsyncQueue
import monix.execution.Scheduler.Implicits.global

class PoolActor(repository: SqlRequestPoolRepository,
                xa: Transactor[Task],
                targetSize: Int)
    extends StrictLogging {
  val queue = MQueue.make[PoolActorMessage]

  def handleMessage(message: PoolActorMessage): Task[Unit] = {
    message match {
      case CheckMessage                 => checkPool()
      case NewResponseMessage(response) => onNewResponse(response)
    }
  }

  def checkPool(): Task[Unit] = {
    (for {
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

  private def onNewResponse(response: Response) = {
    (for {
      _ <- repository.addToPool(response.data)
      _ <- repository.delete(response.requestId)
    } yield ()).transact(xa)
  }

  def run = {
    queue.take
      .flatMap(handleMessage)
      .restartUntil(_ => false)
      .start
  }

  def offer(message: PoolActorMessage) = {
    queue.offer(message)
  }
}

sealed trait PoolActorMessage
case object CheckMessage extends PoolActorMessage
case class NewResponseMessage(response: Response) extends PoolActorMessage

class MQueue[T](q: AsyncQueue[T]) {
  def take: Task[T] = {
    Task.deferFuture(q.poll())
  }
  def offer(t: T): Task[Unit] = {
    Task.eval(q.offer(t))
  }
}
object MQueue {
  def make[T]: MQueue[T] = new MQueue(AsyncQueue.unbounded(None))
}
