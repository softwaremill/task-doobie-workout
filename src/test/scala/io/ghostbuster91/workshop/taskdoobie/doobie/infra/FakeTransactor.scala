package io.ghostbuster91.workshop.taskdoobie.doobie.infra

import java.sql.Connection

import cats.effect.{Blocker, Resource}
import doobie.util.transactor.Strategy
import io.ghostbuster91.workshop.taskdoobie.db.Doobie._
import monix.eval.Task

import scala.concurrent.duration._
import monix.execution.Scheduler.Implicits.global

import scala.concurrent.ExecutionContext

trait FakeTransactor {
  val xa: Transactor[Task] = Transactor(
    (),
    (_: Unit) => Resource.pure[Task, Connection](null),
    KleisliInterpreter[Task](
      Blocker.liftExecutionContext(ExecutionContext.global)
    ).ConnectionInterpreter,
    Strategy.void
  )

  implicit class RichConnectionIO[T](t: ConnectionIO[T]) {
    def unwrap: T = t.transact(xa).runSyncUnsafe(10 seconds)
  }
}
