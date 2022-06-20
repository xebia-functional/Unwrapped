package fx

/**
 * Models the http scheme
 */
opaque type Http = Structured ?=> Control[HttpExecutionException] ?=> Resource[HttpClientConfig]

