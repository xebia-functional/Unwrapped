package fx

import java.net.http.HttpResponse

/**
 * Models a retry policy as a function from HttpResponse to Boolean.
 */
type HttpRetryPolicy[A] =
  (HttpResponse[A], Int) => Boolean

/**
 * By default, retry if the request is not a bad request.
 */
given defaultRetryPolicy[A]: HttpRetryPolicy[A] =
  (r, i) => {
    i < 3 && (500 to 599).contains(r.statusCode)
  }

extension [A](a: HttpResponse[A])
  /**
   * Returns true if the policy determines the request should be retried.
   */
  def shouldRetry(retryCount: Int): HttpRetryPolicy[A] ?=> Boolean =
    summon[HttpRetryPolicy[A]](a, retryCount)

object HttpRetryPolicy:
  /**
   * @constructor
   */
  def apply[A](f: (HttpResponse[A], Int) => Boolean): HttpRetryPolicy[A] =
    f
