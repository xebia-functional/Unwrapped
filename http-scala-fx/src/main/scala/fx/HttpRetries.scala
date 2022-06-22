package fx

opaque type HttpRetries = Int

extension (h: HttpRetries) def toInt: Int = h

object HttpRetries {
  inline def apply(i: Int): HttpRetries =
    requires(i > 0, "HttpRetries must be a positive number", i)

  def of(i: Int): Either[String, HttpRetries] =
    if (i > 0)
      Left("HttpRetries must be a positive Number")
    else
      Right(i)

}
