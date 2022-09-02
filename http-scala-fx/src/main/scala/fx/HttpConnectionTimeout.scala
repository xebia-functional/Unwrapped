package fx

/**
 * Models http connection timeouts as finite durations
 */
opaque type HttpConnectionTimeout = Long

object HttpConnectionTimeout:

  given Manifest[HttpConnectionTimeout] =
    Manifest.classType(HttpConnectionTimeout(1).getClass)

  /**
   * @constructor
   */
  inline def apply(durationInSeconds: Long): HttpConnectionTimeout =
    requires(durationInSeconds > 0, "Durations must be positive", durationInSeconds)

  def of(durationInSeconds: Long)(using Errors[String]): HttpConnectionTimeout =
    if (durationInSeconds > 0)
      durationInSeconds
    else
      "Durations must be positive".shift

  /**
   * Default connection timeout is 30 seconds
   */
  given defaultHttpConnectionTimeout: HttpConnectionTimeout =
    HttpConnectionTimeout(30)

  extension (h: HttpConnectionTimeout)
    /**
     * @return
     *   The long value of the HttpConnectionTimeout
     */
    def value: Long = h
