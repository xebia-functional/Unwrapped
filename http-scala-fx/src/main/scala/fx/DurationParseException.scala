package fx

/**
 * Models exceptions when parsing duration strings.
 */
sealed abstract class DurationParseException(val message: String) extends Exception(message)

object DurationParseException:
  private final case class InvalidDurationString(failedString: String)
    extends DurationParseException(s"$failedString is an invalid duration")
  private case object NonFiniteDurationException extends DurationParseException("The duration must be finite")

  def apply(): DurationParseException = NonFiniteDurationException
  def apply(s: String): DurationParseException = InvalidDurationString(s)




