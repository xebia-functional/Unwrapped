package fx

/**
 * Models http status codes as Integers
 */
opaque type StatusCode = Int

extension (s: StatusCode) def value: Int = s

object StatusCode:
  val statusCodes =
    (100 to 103).toSet ++ (200 to 208).toSet + 226 ++ (300 to 308).toSet ++ (400 to 418).toSet ++ (421 to 426).toSet ++ Set(
      428,
      429,
      431,
      451) ++ (500 to 511).toSet

  private[this] val predicateError = "status code must be a valid status code"

  inline def apply(i: Int): StatusCode =
    requires(
      ((100 to 103).toSet ++ (200 to 208).toSet + 226 ++ (300 to 308).toSet ++ (400 to 418).toSet ++ (421 to 426).toSet ++ Set(
        428,
        429,
        431,
        451) ++ (500 to 511).toSet).contains(i),
      "status code must be a valid status code",
      i
    )
  def of(i: Int): Errors[String] ?=> StatusCode =
    if (statusCodes.contains(i))
      i
    else
      predicateError.shift

  def unsafeOf(i: Int): StatusCode =
    assert(statusCodes.contains(i))
    i
