package fx

/**
 * Base effect type for http requests.
 */
type HttpClient[A] = Http ?=> A


object HttpClient:
  def apply[A](a: A): HttpClient[A] =
    a
