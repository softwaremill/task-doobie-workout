package io.ghostbuster91.workshop.taskdoobie.task

import monix.eval.Task
import monix.execution.Scheduler
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import monix.execution.Scheduler.Implicits.global

class ThreadsSpec extends AnyFlatSpec with Matchers {

  it should "qwe" in {
    Task
      .eval(println(Thread.currentThread().getName))
      .map(_ => println(Thread.currentThread().getName))
      .executeOn(Scheduler.io())
      .flatMap(_ => Task.eval(println(Thread.currentThread().getName)))
      .flatMap(
        _ =>
          Task
            .eval(println(Thread.currentThread().getName))
            .executeOn(Scheduler.computation())
            .asyncBoundary(Scheduler.computation())
      )
      .map(_ => println(Thread.currentThread().getName))
      .asyncBoundary(Scheduler.computation())
      .map(_ => println(Thread.currentThread().getName))
      .asyncBoundary
      .map(_ => println(Thread.currentThread().getName))
      .runSyncUnsafe()
  }

}
