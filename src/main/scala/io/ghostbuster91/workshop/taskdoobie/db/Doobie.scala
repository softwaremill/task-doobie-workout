package io.ghostbuster91.workshop.taskdoobie.db

import java.sql.Timestamp
import java.time.{Instant, LocalDateTime, ZoneOffset}

import com.typesafe.scalalogging.StrictLogging
import doobie.util.log.{ExecFailure, ProcessingFailure, Success}

object Doobie
    extends doobie.Aliases
    with doobie.hi.Modules
    with doobie.free.Modules
    with doobie.free.Types
    with doobie.postgres.Instances
    with doobie.free.Instances
    with doobie.syntax.AllSyntax
    with StrictLogging {
  import doobie.implicits.javasql.TimestampMeta

  implicit val instantMeta: Meta[Instant] =
    TimestampMeta.imap[Instant](
      t => t.toLocalDateTime.atZone(ZoneOffset.UTC).toInstant
    )(i => Timestamp.valueOf(LocalDateTime.ofInstant(i, ZoneOffset.UTC)))

  implicit val doobieLogHandler: LogHandler = LogHandler {
    case Success(sql, args, exec, processing) =>
    case ProcessingFailure(sql, args, _, _, failure) =>
      logger.error(s"Processing failure: $sql | args: $args", failure)
    case ExecFailure(sql, args, _, failure) =>
      logger.error(s"Execution failure: $sql | args: $args", failure)
  }
}
