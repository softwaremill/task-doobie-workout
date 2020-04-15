package io.ghostbuster91.workshop.taskdoobie

import java.time.Instant
import java.util.UUID

import cats.effect.Bracket
import io.ghostbuster91.workshop.taskdoobie.db.Doobie._

class UserService[F[_]](userRepository: SqlUserRepository, xa: Transactor[F])(
  implicit b: Bracket[F, Throwable]
) {
  def create(name: String): F[Unit] = {
    userRepository
      .insert(
        User(UUID.randomUUID().toString, name, CountryCode("PL"), Instant.now())
      )
      .transact(xa)
  }
}
