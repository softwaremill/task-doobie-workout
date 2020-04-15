package io.ghostbuster91.workshop.taskdoobie

import java.time.Instant

case class User(id: String,
                email: String,
                countryCode: CountryCode,
                createdAt: Instant)

case class CountryCode(value: String)
