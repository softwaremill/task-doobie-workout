package io.ghostbuster91.workshop.taskdoobie.db

import java.net.URI

import cats.effect.{Async, Blocker, Resource}
import com.typesafe.scalalogging.StrictLogging
import doobie.hikari.HikariTransactor
import doobie.util.transactor.Transactor
import io.ghostbuster91.workshop.taskdoobie.db.Doobie._
import monix.eval.Task
import monix.execution.Scheduler
import org.flywaydb.core.Flyway

import scala.concurrent.duration._

/**
  * Configures the database, setting up the connection pool and performing migrations.
  */
class DB(_config: DBConfig) extends StrictLogging {
  private val config: DBConfig = {
    // on heroku, the url is passed without the jdbc: prefix, and with a different scheme
    // see https://devcenter.heroku.com/articles/connecting-to-relational-databases-on-heroku-with-java#using-the-database_url-in-plain-jdbc
    if (_config.url.startsWith("postgres://")) {
      val dbUri = URI.create(_config.url)
      val usernamePassword = dbUri.getUserInfo.split(":")
      _config.copy(
        username = usernamePassword(0),
        password = Sensitive(
          if (usernamePassword.length > 1) usernamePassword(1) else ""
        ),
        url = "jdbc:postgresql://" + dbUri.getHost + ':' + dbUri.getPort + dbUri.getPath
      )
    } else _config
  }

  val transactorResource: Resource[Task, Transactor[Task]] = {
    /*
     * When running DB operations, there are three thread pools at play:
     * (1) connectEC: this is a thread pool for awaiting connections to the database. There might be an arbitrary
     * number of clients waiting for a connection, so this should be bounded.
     * (2) transactEC: this is a thread pool for executing JDBC operations. As the connection pool is limited,
     * this can be unbounded pool
     * (3) contextShift: pool for executing non-blocking operations, to which control shifts after completing
     * DB operations. This is provided by Monix for Task.
     *
     * See also: https://tpolecat.github.io/doobie/docs/14-Managing-Connections.html#about-threading
     */
    for {
      connectEC <- doobie.util.ExecutionContexts
        .fixedThreadPool[Task](config.connectThreadPoolSize)
      transactEC <- doobie.util.ExecutionContexts.cachedThreadPool[Task]
      _ <- Resource.liftF(Async[Task].delay(Class.forName(config.driver)))
      xa <- HikariTransactor
        .initial[Task](connectEC, Blocker.liftExecutionContext(transactEC))
      _ <- Resource.liftF {
        xa.configure { ds =>
          Async[Task].delay {
            ds.setJdbcUrl(config.url)
            ds.setUsername(config.username)
            ds.setPassword(config.password.value)
            ds.setMaxLifetime(5 * 60 * 1000)
          }
        }
      }
      _ <- Resource.liftF(connectAndMigrate(xa))
    } yield xa
  }

  private def connectAndMigrate(xa: Transactor[Task]): Task[Unit] = {
    (testConnection(xa) >> migrate() >> Task(
      logger.info("Database migration & connection test complete")
    )).onErrorRecoverWith {
      case e: Exception =>
        logger.warn("Database not available, waiting 5 seconds to retry...", e)
        Task.sleep(5.seconds) >> connectAndMigrate(xa)
    }
  }

  private val flyway = {
    Flyway
      .configure()
      .dataSource(config.url, config.username, config.password.value)
      .load()
  }

  def migrate(): Task[Unit] = {
    if (config.migrateOnStart) {
      Task(flyway.migrate()).void
    } else Task.unit
  }

  def clean(): Unit = {
    flyway.clean()
  }

  def cleanPreparedStatements(
    xa: Transactor[Task]
  )(implicit s: Scheduler): Unit = {
    sql"DEALLOCATE ALL".update.run
      .map(_ => ())
      .transact(xa)
      .runSyncUnsafe(1.minute)
  }

  private def testConnection(xa: Transactor[Task]): Task[Unit] =
    Task {
      sql"select 1".query[Int].unique.transact(xa)
    }.void
}
