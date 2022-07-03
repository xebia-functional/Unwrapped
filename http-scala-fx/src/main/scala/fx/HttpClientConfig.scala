package fx

import scala.quoted.*

/**
 * Defines the client configuration options for an http client. Not open for extension.
 */
final class HttpClientConfig(
    val connectionTimeout: Nullable[HttpConnectionTimeout],
    val followRedirects: Nullable[HttpFollowRedirects],
    val maximumRetries: Nullable[HttpRetries]
)

object HttpClientConfig:
  given HttpClientConfig =
    HttpClientConfig(Nullable.none, Nullable.none, Nullable.none)
