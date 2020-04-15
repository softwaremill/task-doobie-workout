package io.ghostbuster91.workshop.taskdoobie.doobie

import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

import cats.effect.Sync
import cats.implicits._
import com.softwaremill.diffx.scalatest.DiffMatcher
import io.ghostbuster91.workshop.taskdoobie.db.Doobie._
import io.ghostbuster91.workshop.taskdoobie.doobie.infra.FakeTransactor
import io.ghostbuster91.workshop.taskdoobie.{CountryCode, FUserRepository, User}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class MyFirstTestUsingFakeTransactor
    extends AnyFlatSpec
    with DiffMatcher
    with Matchers
    with FakeTransactor {

  it should "work" in {
    val user = User("1", "a", CountryCode("PL"), Instant.now)
    StubUserRepository.insert(user).unwrap

    StubUserRepository.findAll().unwrap should matchTo(List(user))
  }
}

object StubUserRepository extends FUserRepository[ConnectionIO] {

  private val users = ConcurrentHashMap.newKeySet[User]()

  override def insert(user: User): ConnectionIO[Unit] = {
    Sync[ConnectionIO].delay(users.add(user)).void
  }

  override def findAll(): ConnectionIO[List[User]] = {
    import scala.jdk.CollectionConverters._
    Sync[ConnectionIO].pure(users.asScala.toList)
  }
}
