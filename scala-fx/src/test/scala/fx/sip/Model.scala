package fx.sip

import scala.concurrent.Future

object NotFound

case class Country(code: Option[String])

case class Address(country: Option[Country])

case class Person(name: String, address: Either[NotFound.type, Address])
