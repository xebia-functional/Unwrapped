package fx

opaque type HttpRetries = Int

object HttpRetries {
  inline def apply(i: Int): HttpRetries =
    requires(i > 0, "HttpRetries must be a positive number")
    i
    
}
