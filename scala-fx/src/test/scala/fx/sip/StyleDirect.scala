package fx.sip

def getCountryCodeDirect(maybePerson: Option[Person])(
    using Control[NotFound.type | None.type]): String =
  val person = maybePerson.bind
  val addressOrNotFound = person.address.join()
  val address = addressOrNotFound.bind
  val country = address.country.bind
  country.code.bind

// or if bind is defined as apply()...

def getCountryDirect2(maybePerson: Option[Person])(
    using Control[NotFound.type | None.type]): String =
  maybePerson().address.join()().country().code()
