package fx

import java.net.http.HttpResponse

/**
 * Models a retry policy as a function from HttpResponse to Boolean.
 */
type HttpRetryPolicy =
  HttpResponse[?] => Boolean

extension (a: HttpResponse[?])
  /**
   * Returns true if the policy determines the requust should be retried.
   */
  def shouldRetry: HttpRetryPolicy ?=> Boolean = summon[HttpRetryPolicy](a)

object HttpRetryPolicy:
  /**
   * @constructor
   */
  def apply(f: HttpResponse[?] => Boolean): HttpRetryPolicy =
    f

  /**
   * By default, retry if the request is a bad request.
   */
  lazy val defaultRetryPolicy: HttpRetryPolicy =
    r => (400 to 499).contains(r.statusCode)
