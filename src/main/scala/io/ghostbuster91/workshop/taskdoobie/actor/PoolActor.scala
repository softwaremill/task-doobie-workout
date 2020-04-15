package io.ghostbuster91.workshop.taskdoobie.actor

import io.ghostbuster91.workshop.taskdoobie.pool.{
  CheckService,
  Response,
  UpdateService
}
import monix.eval.Task
import monix.execution.AsyncQueue
import monix.execution.Scheduler.Implicits.global

class PoolActor(checkService: CheckService, updateService: UpdateService) {
  val queue = MQueue.make[PoolActorMessage]

  def handleMessage(message: PoolActorMessage): Task[Unit] = {
    message match {
      case CheckMessage                 => checkService.checkPool()
      case NewResponseMessage(response) => updateService.onNewResponse(response)
    }
  }

  def run = {
    queue.take
      .flatMap(handleMessage)
      .restartUntil(_ => false)
      .start
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
