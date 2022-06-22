package fx

/**
 * Models http connection timeouts as finite durations
 */
opaque type HttpConnectionTimeout = Long

extension (h: HttpConnectionTimeout)
  def toLong:Long = h

object HttpConnectionTimeout:
  /**
   * @constructor
   */
  inline def apply(durationInSeconds: Long): HttpConnectionTimeout =
    requires(durationInSeconds > 0, "Durations must be positive", durationInSeconds)

  def of(durationInSeconds: Long): Either[String, HttpConnectionTimeout] =
    if (durationInSeconds > 0)
      Right(durationInSeconds)
    else
      Left("Durations must be positive")
  
  /**
   * Default connection timeout is 30 seconds
   */
  given defaultHttpConnectionTimeout: HttpConnectionTimeout =
    HttpConnectionTimeout(30)
