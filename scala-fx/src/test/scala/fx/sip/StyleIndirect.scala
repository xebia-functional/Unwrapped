package fx.sip

import scala.concurrent.Future
import concurrent.ExecutionContext.Implicits.global

def getCountryCodeIndirect(maybePerson: Option[Person]): Future[Option[String]] =
  maybePerson match
    case Some(person) =>
      person.address.map {
        case Right(address) =>
          address.country.flatMap(_.code)
        case Left(_) =>
          None
      }
    case None => Future.successful(None)
