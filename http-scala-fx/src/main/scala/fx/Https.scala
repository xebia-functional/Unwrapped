package fx

/**
 * Models the http scheme
 */
opaque type Https[A] = (Structured, Control[HttpExecutionException], Resource[HttpClientConfig]) ?=> A

