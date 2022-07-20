package fx

/**
 * Models http status codes as Integers
 */
opaque type StatusCode = Int

object StatusCode:
  def statusCodes =
    (100 to 103).toSet ++ (200 to 208).toSet + 226 ++ (300 to 308).toSet ++ (400 to 418).toSet ++ (421 to 426).toSet ++ Set(
      428,
      429,
      431,
      451) ++ (500 to 511).toSet

  private[this] val predicateError = "status code must be a valid status code"

  inline def apply(i: Int): StatusCode =
    requires(
      (i >= 100 && i <= 103) ||
        (i >= 200 && i <= 208) ||
        i == 226 ||
        (i >= 300 && i <= 308) ||
        (i >= 400 && i <= 418) ||
        (i >= 421 && i <= 426) ||
        i == 428 ||
        i == 429 ||
        i == 431 ||
        i == 451 ||
        (i >= 500 && i <= 511),
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
  extension (s: StatusCode) def value: Int = s
