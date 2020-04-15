name := "task-doobie-workshop"

version := "0.1"

scalaVersion := "2.13.1"
val doobieVersion = "0.9.0"

val dbDependencies = Seq(
  "org.tpolecat" %% "doobie-core" % doobieVersion,
  "org.tpolecat" %% "doobie-hikari" % doobieVersion,
  "org.tpolecat" %% "doobie-postgres" % doobieVersion,
  "org.flywaydb" % "flyway-core" % "6.3.3",
  "com.beachape" %% "enumeratum" % "1.5.15",
  "com.beachape" %% "enumeratum-doobie" % "1.5.17",
  "org.tpolecat" %% "doobie-scalatest" % doobieVersion % Test  // ScalaTest support for typechecking statements.
)

val testDependencies = Seq(
  "org.scalatest" %% "scalatest" % "3.1.0",
  "org.testcontainers" % "testcontainers" % "1.12.4",
  "com.dimafeng" %% "testcontainers-scala-postgresql" % "0.34.2",
  "com.opentable.components" % "otj-pg-embedded" % "0.13.2",
  "com.softwaremill.diffx" %% "diffx-scalatest" % "0.3.27",
).map(_ % Test)

libraryDependencies ++= (Seq(
  "org.typelevel" %% "cats-core" % "2.0.0",
  "org.typelevel" %% "cats-effect" % "2.1.2",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "ch.qos.logback" % "logback-core" % "1.2.3",
  "io.monix" %% "monix" % "3.1.0",
  "com.softwaremill.common" %% "tagging"    % "2.2.1"
) ++ dbDependencies ++ testDependencies)

lazy val root = (project in file(".")).settings(commonSmlBuildSettings ++ splainSettings)
