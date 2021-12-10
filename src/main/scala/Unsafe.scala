package fx

object unsafe:
  given unsafeExceptions[R <: Exception]: Throws[R] =
    Handled.asInstanceOf[Throws[R]]
