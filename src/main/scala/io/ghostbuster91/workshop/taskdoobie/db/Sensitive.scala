package io.ghostbuster91.workshop.taskdoobie.db

case class Sensitive(value: String) extends AnyVal {
  override def toString: String = "***"
}
