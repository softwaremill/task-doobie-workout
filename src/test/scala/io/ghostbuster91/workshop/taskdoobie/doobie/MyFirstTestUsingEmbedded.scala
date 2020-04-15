package io.ghostbuster91.workshop.taskdoobie.doobie

import java.time.Instant

import com.softwaremill.diffx.scalatest.DiffMatcher
import io.ghostbuster91.workshop.taskdoobie.doobie.infra.EmbeddedPostgres
import io.ghostbuster91.workshop.taskdoobie.{
  CountryCode,
  SqlUserRepository,
  User
}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class MyFirstTestUsingEmbedded
    extends AnyFlatSpec
    with EmbeddedPostgres
    with DiffMatcher
    with Matchers {
  val userRepository = new SqlUserRepository

  it should "work" in {
    val user = User("1", "asd", CountryCode("PL"), Instant.now)
    userRepository.insert(user).unwrap

    userRepository.findAll().unwrap should matchTo(List(user))

    userRepository.findByCountryCode(CountryCode("PL")).unwrap should matchTo(
      List(user)
    )
  }

  it should "work2" in {
    val user = User("1", "asd", CountryCode("PL"), Instant.now)
    userRepository.insert(user).unwrap

    userRepository.findAll().unwrap should matchTo(List(user))

    userRepository.findByCountryCode(CountryCode("PL")).unwrap should matchTo(
      List(user)
    )
  }
}
