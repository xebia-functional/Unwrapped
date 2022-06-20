package fx

/**
 * Models the http scheme
 */
opaque type Https = Structured ?=> Control[HttpExecutionException]

/**
 * Inline semantic alias for structured for http calls
 */
inline def https[B](f: Https ?=> B): Control[HttpExecutionException] ?=> B =
  structured(f)  
