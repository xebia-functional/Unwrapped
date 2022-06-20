package fx

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.duration.Duration
import scala.concurrent.duration.SECONDS

/**
 * Models http connection timeouts as finite durations
 */
opaque type HttpConnectionTimeout = FiniteDuration

object HttpConnectionTimeout:
  /**
   * @constructor
   */
  inline def apply(durationInSeconds: Long): HttpConnectionTimeout =
    requires(durationInSeconds > 0, "Durations must be positive")
    FiniteDuration(durationInSeconds, SECONDS)



  /**
    * Default connection timeout is 30 seconds
    */
  given defaultHttpConnectionTimeout: HttpConnectionTimeout = HttpConnectionTimeout(30)
