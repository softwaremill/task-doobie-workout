package io.ghostbuster91.workshop.taskdoobie.pool

import cats.data.OptionT
import cats.implicits._
import io.ghostbuster91.workshop.taskdoobie.db.Doobie._

class SqlRequestPoolRepository {

  def getCurrentSize(): ConnectionIO[Int] = {
    sql"""SELECT COUNT(*) FROM pool""".query[Int].unique.onError {
      case e => println("cps").pure[ConnectionIO]
    }
  }

  def requestsInFlight(): ConnectionIO[Int] = {
    sql"""SELECT COUNT(*) FROM request""".query[Int].unique.onError {
      case e => println("qwe").pure[ConnectionIO]
    }
  }

  def addToPool(data: String): ConnectionIO[Int] = {
    sql"""INSERT INTO pool(data) VALUES ($data)""".update.run
  }

  def delete(requestId: String): ConnectionIO[Unit] = {
    sql"""DELETE FROM request WHERE id = $requestId""".update.run.void
  }

  def addRequest(requestID: String): ConnectionIO[Unit] = {
    sql"""INSERT INTO request(id) VALUES ($requestID)""".update.run.void
  }

  def addManyRequests(ids: List[String]): ConnectionIO[Unit] = {
    val sql = """INSERT INTO request(id) VALUES (?)"""
    Update[String](sql).updateMany(ids).void
  }

  def takeRequest(): ConnectionIO[Option[String]] = {
    sql"""SELECT id FROM request LIMIT 1""".query[String].option
  }

  def useSingleData(): ConnectionIO[Unit] = {
    (for {
      data <- OptionT(
        sql"""SELECT data FROM pool LIMIT 1""".query[String].option
      )
      _ <- OptionT.liftF(
        sql"""DELETE FROM pool WHERE data = $data""".update.run.void
      )
    } yield ()).value.void
  }

  def lockPoolTable(): ConnectionIO[Unit] = {
    sql"""LOCK TABLE pool IN EXCLUSIVE MODE""".update.run.void
  }
}
