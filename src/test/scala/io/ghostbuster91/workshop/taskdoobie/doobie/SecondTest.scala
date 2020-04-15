package io.ghostbuster91.workshop.taskdoobie.doobie

import io.ghostbuster91.workshop.taskdoobie.db.Doobie._
import io.ghostbuster91.workshop.taskdoobie.doobie.infra.PostgresDocker
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class SecondTest extends AnyFreeSpec with Matchers with PostgresDocker {

  case class NewPerson(name: String, age: Option[Int])
  case class Person(id: Long, name: String, age: Int)

  "1" in {
    val newPerson = NewPerson("kasper", Some(29))
    sql"""INSERT INTO person(name, age) VALUES(${newPerson.name}, ${newPerson.age})""".update.run.unwrap
  }

  "2" in {
    def insert2(p: NewPerson): ConnectionIO[Person] =
      for {
        _ <- sql"insert into person (name, age) values (${p.name}, ${p.age})".update.run
        id <- sql"select lastval()".query[Long].unique
        p <- sql"select id, name, age from person where id = $id"
          .query[Person]
          .unique
      } yield p

    println(insert2(NewPerson("kasper", Some(29))).unwrap)
    println(insert2(NewPerson("kasper", Some(29))).unwrap)
    println(insert2(NewPerson("kasper", Some(29))).unwrap)
    println(insert2(NewPerson("kasper", Some(29))).unwrap)
  }

  "3" in {
    def insert3(p: NewPerson): ConnectionIO[Person] = {
      sql"insert into person (name, age) values (${p.name}, ${p.age})".update
        .withUniqueGeneratedKeys("id", "name", "age")
    }
    println(insert3(NewPerson("kasper", Some(29))).unwrap)
    println(insert3(NewPerson("kasper", Some(29))).unwrap)
    println(insert3(NewPerson("kasper", Some(29))).unwrap)
    println(insert3(NewPerson("kasper", Some(29))).unwrap)
  }

  "4" in {
    import doobie.postgres.implicits._
    println(sql"""SELECT name FROM person""".query[String].explain.unwrap)
  }
}
