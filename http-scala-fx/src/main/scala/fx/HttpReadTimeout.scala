package fx

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.duration.Duration
import scala.concurrent.duration.NANOSECONDS

opaque type HttpReadTimeout = Control[DurationParseException] ?=> FiniteDuration

object HttpReadTimeout {
  /**
    * @constructor
    */
  def apply(f: FiniteDuration): HttpReadTimeout = f
  /**
    * Safely creates an HttpReadTimeout from a string. Infinite
    * and unparsable strings are shifted into DurationParseException
    * control.
    * @constructor
    */
  def apply(s: String): HttpReadTimeout =
    val stringAsDuration = handle(Duration(s))(_ => DurationParseException(s).raise)
    fx.DurationParseException().ensure(stringAsDuration.isFinite)
    FiniteDuration(stringAsDuration.toNanos, NANOSECONDS)

  given defaultHttpReadTimeout: HttpReadTimeout = apply("30 seconds")

}
