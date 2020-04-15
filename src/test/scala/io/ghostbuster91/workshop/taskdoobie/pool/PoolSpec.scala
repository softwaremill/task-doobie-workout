package io.ghostbuster91.workshop.taskdoobie.pool

import cats.effect.Resource
import io.ghostbuster91.workshop.taskdoobie.actor.PoolActor
import io.ghostbuster91.workshop.taskdoobie.doobie.infra.PostgresDocker
import monix.eval.Task
import monix.execution.{Cancelable, Scheduler}
import monix.reactive.Observable
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import scala.concurrent.duration._

class PoolSpec extends AnyFreeSpec with Matchers with PostgresDocker {
  "work" in {
    import monix.execution.Scheduler.Implicits.global

    val repository = new SqlRequestPoolRepository
    val actor = new PoolActor(repository, xa, 100)
    val checkService = new CheckService(actor)
    val updateService = new UpdateService(actor)
    val generatorService = new GeneratorService(repository, xa, updateService)
    val userService = new UseService(repository, xa)

    (for {
      _ <- checkService.checkPoolInterval(75 millis).toResource(Scheduler.io())
      _ <- generatorService
        .generate(50 millis)
        .toResource(Scheduler.computation())
      _ <- userService.usingInterval(300 millis).toResource(Scheduler.global)
      _ <- Resource.make(actor.run)(_.cancel)
    } yield ()).use(_ => Task.never).runSyncUnsafe()
  }

  implicit class ObservableOpsImpl[T](source: Observable[T]) {

    def toResource(implicit s: Scheduler): Resource[Task, Cancelable] =
      Resource.make(Task.eval(source.subscribe()))(r => Task.eval(r.cancel()))
  }
}
