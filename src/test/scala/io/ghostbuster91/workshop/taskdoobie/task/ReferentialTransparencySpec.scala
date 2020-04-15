package io.ghostbuster91.workshop.taskdoobie.task

import monix.eval.Task
import monix.execution.Scheduler.Implicits.global
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import scala.annotation.tailrec
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class ReferentialTransparencySpec extends AnyFreeSpec with Matchers {

  "start here" in {
    val threshold = 200
    def test(i: Int) = i > threshold

    test(201) shouldBe true
    test(200) shouldBe false
  }

  class Person(var name: String) {
    def setName(n: String): Unit = name = n
    def getName(): String = name
  }

  "mutability" in {
    val p = new Person("john")
    println(p.getName())
  }

  "mutability - more" in {
    val p = new Person("john")
    val p2 = p
    p2.setName("henry")
    println(p.getName()) // "henry"
    println(p2.getName()) // "henry"
  }

  case class Person2(name: String) {
    println(s"I'm a new Person: $name")
  }

  "side eff" in {
    val p = Person2("john")
    val p2 = p.copy(name = "henry")
    println(p.name)
    println(p2.name)
  }

  "two futures" in {
    Await.ready(for {
      x <- Future { println("Foo") }
      y <- Future { println("Foo") }
    } yield (), 1 seconds)
  }

  "two tasks" in { // memonize/evalOnce
    (for {
      x <- Task { println("Foo") }
      y <- Task { println("Foo") }
    } yield ()).runSyncUnsafe()
  }

  "fib" in {
    @tailrec
    def fib(cycles: Int, a: BigInt, b: BigInt): BigInt = {
      if (cycles > 0)
        fib(cycles - 1, b, a + b)
      else
        b
    }
    println(fib(150, 0, 1))
  }

  "fib task" in {
    def fib(cycles: Int, a: BigInt, b: BigInt): Task[BigInt] = {
      if (cycles > 0)
        Task.defer(fib(cycles - 1, b, a + b))
      else
        Task.now(b)
    }
    println(fib(150, 0, 1).runSyncUnsafe())
  }

  "error handling" in {}
  // concurrent
  // reactive
  // error handling
}
