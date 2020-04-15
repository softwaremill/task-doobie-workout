package io.ghostbuster91.workshop.taskdoobie

trait FUserRepository[F[_]] {
  def insert(user: User): F[Unit]

  def findAll(): F[List[User]]
}
