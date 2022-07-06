package fx.sip

import scala.concurrent.Future
import concurrent.ExecutionContext.Implicits.global

def getCountryCodeIndirect(futurePerson: Future[Person]): Future[Option[String]] =
  futurePerson.map { person =>
    person.address match
      case Right(address) =>
        address.country.flatMap(_.code)
      case Left(_) =>
        None
  }
