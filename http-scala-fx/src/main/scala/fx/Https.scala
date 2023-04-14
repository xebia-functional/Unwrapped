package fx

/**
 * Models the http scheme
 */
type Https[A] =
  (Structured, Control[HttpExecutionException], Resource[HttpClientConfig]) ?=> A
