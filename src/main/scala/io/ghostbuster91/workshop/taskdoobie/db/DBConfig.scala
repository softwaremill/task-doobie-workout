package io.ghostbuster91.workshop.taskdoobie.db

case class DBConfig(username: String,
                    password: Sensitive,
                    url: String,
                    migrateOnStart: Boolean,
                    driver: String,
                    connectThreadPoolSize: Int)
