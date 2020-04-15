package io.ghostbuster91.workshop.taskdoobie.task

import monix.reactive.Observable
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import scala.concurrent.duration._
import monix.execution.Scheduler.Implicits.global
import monix.execution.schedulers.TestScheduler
import monix.reactive.subjects.ConcurrentSubject

import scala.concurrent.Await

class ObservableSpec extends AnyFreeSpec with Matchers {
  "1" in {
    val f = Observable
      .fromIterable(List(1, 2, 3))
      .delayOnNext(1 second)
      .foreach(i => println(i))

    Await.ready(f, Duration.Inf)
  }

  "2" in {
    val ts = TestScheduler()

    val f = Observable
      .fromIterable(List(1, 2, 3))
      .delayOnNext(1 seconds)
      .foreach(i => println(i))(ts)

    ts.tick(1 seconds)
    ts.tick(1 seconds)
    ts.tick(1 seconds)
  }

  "3" in {
    val ts = TestScheduler()

    val subj = ConcurrentSubject.publish[Int]
    val f = subj
      .debounce(1 seconds)
      .foreach(i => println(i))(ts)

    subj.onNext(1)
    subj.onNext(2)
    subj.onNext(3)
    ts.tick(1 seconds)
    subj.onNext(4)
    ts.tick(1 seconds)
  }

  "4" in {
    val o = Observable
      .intervalWithFixedDelay(100 millis)
      .foreach(println)

    Thread.sleep(5000)

    o.cancel()

    Thread.sleep(5000)
  }
}
