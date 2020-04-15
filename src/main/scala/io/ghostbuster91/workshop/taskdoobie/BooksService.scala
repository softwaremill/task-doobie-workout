package io.ghostbuster91.workshop.taskdoobie

import cats.Monad
import cats.implicits._
import io.ghostbuster91.workshop.taskdoobie.db.Doobie._
import monix.eval.Task

class BooksService[F[_]: Monad](repository: BooksRepository[F]) {

  def createBook(book: Book): F[Unit] = {
    for {
      _ <- repository.insertBook(book)
      _ <- repository.assignBookToLibrary(book.id, book.libraryId)
    } yield ()
  }
}

object BooksService {
  def withTask() = new BooksService[Task](new TaskBooksRepository)
  def withConnectionIO() =
    new BooksService[ConnectionIO](new SqlBooksRepository)
}

trait BooksRepository[F[_]] {
  def insertBook(book: Book): F[Unit]

  def assignBookToLibrary(id: String, libraryId: String): F[Unit]
}

case class Book(id: String, isbn: String, title: String, libraryId: String)

class SqlBooksRepository extends BooksRepository[ConnectionIO] {
  override def insertBook(book: Book): ConnectionIO[Unit] = {
    ().pure[ConnectionIO]
  }

  override def assignBookToLibrary(id: String,
                                   libraryId: String): ConnectionIO[Unit] =
    ().pure[ConnectionIO]
}

class TaskBooksRepository() extends BooksRepository[Task] {
  override def insertBook(book: Book): Task[Unit] = Task.unit

  override def assignBookToLibrary(id: String, libraryId: String): Task[Unit] =
    Task.unit
}
