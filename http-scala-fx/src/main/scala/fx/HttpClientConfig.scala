package fx

import scala.quoted.*

/**
 * Defines the client configuration options for an http client. Not open for extension.
 */
final class HttpClientConfig(
    val connectionTimeout: HttpConnectionTimeout | Null,
    val retryPolicy: HttpRetryPolicy | Null,
    val followRedirects: HttpFollowRedirects,
    val maximumRetries: HttpRetries
)

object HttpClientConfig:
  lazy val defaultHttpClientConfig: Errors[DurationParseException] ?=> HttpClientConfig = HttpClientConfig(
    HttpConnectionTimeout.defaultHttpConnectionTimeout,
    HttpRetryPolicy.defaultRetryPolicy,
    HttpFollowRedirects.normal,
    HttpRetries(3))

