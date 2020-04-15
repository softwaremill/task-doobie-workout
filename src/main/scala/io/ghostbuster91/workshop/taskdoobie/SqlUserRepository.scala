package io.ghostbuster91.workshop.taskdoobie

import io.ghostbuster91.workshop.taskdoobie.db.Doobie._
import cats.implicits._

class SqlUserRepository extends FUserRepository[ConnectionIO] {

  def insert(user: User): ConnectionIO[Unit] = {
    insertQuery(user).run.void
  }

  def insertQuery(user: User): Update0 = {
    sql"""INSERT INTO users(id, email, country_code, created_at) VALUES (${user.id}, ${user.email}, ${user.countryCode}, ${user.createdAt})""".update
  }

  def findAll(): ConnectionIO[List[User]] = {
    findAllQuery.to[List]
  }

  def findAllQuery: Query0[User] = {
    sql"""SELECT id, email, country_code, created_at FROM users""".query[User]
  }

  def findByCountryCodeQuery(cc: CountryCode): Query0[User] = {
    sql"""SELECT id, email, country_code,created_at FROM users WHERE country_code = $cc"""
      .query[User]
  }

  def findByCountryCode(cc: CountryCode): ConnectionIO[List[User]] = {
    findByCountryCodeQuery(cc).to[List]
  }

  def findAllStreaming() = {
    findAllQuery.stream.take(5).compile.toList
  }
}
