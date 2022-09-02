package fx

import java.net.http.HttpResponse

/**
 * Models a retry policy as a function from HttpResponse to Boolean.
 */
type HttpRetryPolicy[A] =
  (HttpResponse[A], HttpRetries, HttpRetries) => Boolean

/**
 * By default, retry if the request is not a bad request.
 */
given defaultRetryPolicy[A]: HttpRetryPolicy[A] =
  (r, retryCount, maxRetries) => {
    retryCount.value < maxRetries.value && (500 to 599).contains(r.statusCode)
  }

extension [A](a: HttpResponse[A])
  /**
   * Returns true if the policy determines the request should be retried.
   */
  def shouldRetry(retryCount: HttpRetries)(
      using policy: HttpRetryPolicy[A],
      config: HttpClientConfig): Boolean =
    policy(a, retryCount, config.maximumRetries.getOrElse(HttpRetries(3)))

object HttpRetryPolicy:
  /**
   * @constructor
   */
  def apply[A](f: (HttpResponse[A], HttpRetries, HttpRetries) => Boolean): HttpRetryPolicy[A] =
    f
