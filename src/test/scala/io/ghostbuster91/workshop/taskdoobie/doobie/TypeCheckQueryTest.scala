package io.ghostbuster91.workshop.taskdoobie.doobie

import java.time.Instant

import cats.effect.Effect
import doobie.scalatest.Checker
import io.ghostbuster91.workshop.taskdoobie.doobie.infra.PostgresDocker
import io.ghostbuster91.workshop.taskdoobie.{
  CountryCode,
  SqlUserRepository,
  User
}
import monix.eval.Task
import monix.execution.Scheduler.Implicits.global
import org.scalatest.flatspec.AnyFlatSpec

class TypeCheckQueryTest
    extends AnyFlatSpec
    with Checker[Task]
    with PostgresDocker {

  val userRepository = new SqlUserRepository

  "findAll" should "typecheck" in {
    check(userRepository.findAllQuery)
  }

  "insert" should "typecheck" in {
    check(
      userRepository.insertQuery(User("a", "a", CountryCode("PL"), Instant.now))
    )
  }

  "findByCC" should "typecheck" in {
    check(userRepository.findByCountryCodeQuery(CountryCode("PL")))
  }

  override implicit def M: Effect[Task] = Task.catsEffect
  override def transactor: doobie.Transactor[Task] = xa
}
