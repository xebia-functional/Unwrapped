package fx

import java.net.http.HttpResponse

/**
 * Models a retry policy as a function from HttpResponse to Boolean.
 */
type HttpRetryPolicy =
  HttpResponse[Any] => Boolean

extension (a: HttpResponse[Any])
  /**
   * Returns true if the policy determines the requust should be retried.
   */
  def shouldRetry: HttpRetryPolicy ?=> Boolean = summon[HttpRetryPolicy](a)

object HttpRetryPolicy:
  /**
   * @constructor
   */
  def apply(f: HttpResponse[Any] => Boolean): HttpRetryPolicy =
    f

  /**
   * By default, retry if the request is a bad request.
   */
  given defaultRetryPolicy: HttpRetryPolicy =
    r => (400 to 499).contains(r.statusCode)
