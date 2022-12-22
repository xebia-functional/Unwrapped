package fx

/**
 * Models the http scheme
 */
opaque type Https[A] =
  (Structured, Raise[HttpExecutionException], Resource[HttpClientConfig]) ?=> A
