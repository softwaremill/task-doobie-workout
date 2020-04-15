package io.ghostbuster91.workshop.taskdoobie.doobie.infra

import com.dimafeng.testcontainers.PostgreSQLContainer
import com.typesafe.scalalogging.StrictLogging
import doobie.free.connection.ConnectionIO
import doobie.util.transactor.Transactor
import io.ghostbuster91.workshop.taskdoobie.db.Doobie._
import io.ghostbuster91.workshop.taskdoobie.db.{DB, DBConfig, Sensitive}
import monix.eval.Task
import monix.execution.Scheduler.Implicits.global
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, Suite}

import scala.concurrent.duration._

trait PostgresDocker
    extends StrictLogging
    with BeforeAndAfterAll
    with BeforeAndAfterEach {
  self: Suite =>

  private val container: PostgreSQLContainer = PostgreSQLContainer().configure {
    c =>
      c.withReuse(true)
  }

  lazy val currentDb: DB = {
    container.start()
    val currentDbConfig = DBConfig(
      username = container.username,
      password = Sensitive(container.password),
      url = container.jdbcUrl,
      driver = container.driverClassName,
      migrateOnStart = true,
      connectThreadPoolSize = 32
    )
    new DB(currentDbConfig)
  }

  private lazy val xaWrapper: (Transactor[Task], Task[Unit]) =
    currentDb.transactorResource.allocated.runSyncUnsafe()
  lazy val xa: Transactor[Task] = xaWrapper._1

  implicit class RichConnectionIO[T](t: ConnectionIO[T]) {
    def unwrap: T = t.transact(xa).runSyncUnsafe(10 seconds)
  }

  override protected def afterAll(): Unit = {
    xaWrapper._2.runSyncUnsafe()
    super.afterAll()
  }

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    currentDb.clean()
    currentDb.cleanPreparedStatements(xa)
    currentDb.migrate().runSyncUnsafe()
  }
}
