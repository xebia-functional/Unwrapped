package fx.sip

import scala.concurrent.Future

def getCountryCodeDirect(
    futurePerson: Fiber[Person])(using Structured, Control[NotFound.type | None.type]): String =
  val person = futurePerson.bind
  val address = person.address.bind
  val country = address.country.bind
  country.code.bind

// or if bind is defined as apply()...

def getCountryDirect2(
    futurePerson: Fiber[Person])(using Structured, Control[NotFound.type | None.type]): String =
  futurePerson.bind.address().country().code()
