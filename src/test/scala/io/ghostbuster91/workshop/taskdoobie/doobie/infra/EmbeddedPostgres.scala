package io.ghostbuster91.workshop.taskdoobie.doobie.infra

import com.opentable.db.postgres.embedded
import com.opentable.db.postgres.embedded.EmbeddedPostgres
import com.typesafe.scalalogging.StrictLogging
import io.ghostbuster91.workshop.taskdoobie.db.Doobie._
import io.ghostbuster91.workshop.taskdoobie.db.{DB, DBConfig, Sensitive}
import monix.eval.Task
import monix.execution.Scheduler.Implicits.global
import org.postgresql.jdbc.PgConnection
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, Suite}

import scala.concurrent.duration._

trait EmbeddedPostgres
    extends StrictLogging
    with BeforeAndAfterAll
    with BeforeAndAfterEach { outer: Suite =>

  lazy val postgres: embedded.EmbeddedPostgres = EmbeddedPostgres
    .builder()
    .setServerConfig("default_transaction_isolation", "'serializable'")
    .start()

  lazy val currentDb: DB = {
    val url = postgres.getJdbcUrl("postgres", "postgres")
    postgres.getPostgresDatabase.getConnection
      .asInstanceOf[PgConnection]
      .setPrepareThreshold(100)
    val currentDbConfig = DBConfig(
      username = "postgres",
      password = Sensitive(""),
      url = url,
      migrateOnStart = true,
      driver = "org.postgresql.Driver",
      connectThreadPoolSize = 32
    )
    new DB(currentDbConfig)
  }

  private lazy val xaWrapper: (Transactor[Task], Task[Unit]) =
    currentDb.transactorResource.allocated.runSyncUnsafe()
  lazy val xa: Transactor[Task] = xaWrapper._1

  override protected def afterAll(): Unit = {
    postgres.close()
    xaWrapper._2.runSyncUnsafe(10 seconds)
    super.afterAll()
  }

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    currentDb.migrate().runSyncUnsafe(10 seconds)
  }

  override protected def afterEach(): Unit = {
    currentDb.clean()
    currentDb.cleanPreparedStatements(xa)
    super.afterEach()
  }

  implicit class RichConnectionIO[T](t: ConnectionIO[T]) {
    def unwrap: T = t.transact(xa).runSyncUnsafe(10 seconds)
  }
}
