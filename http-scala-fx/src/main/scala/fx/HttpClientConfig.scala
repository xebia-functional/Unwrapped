package fx

import scala.quoted.*

/**
 * Defines the client configuration options for an http client. Not open for extension.
 */
final class HttpClientConfig(
    val connectionTimeout: |?[HttpConnectionTimeout],
    val followRedirects: |?[HttpFollowRedirects],
    val maximumRetries: |?[HttpRetries]
)

object HttpClientConfig:
  given HttpClientConfig =
    HttpClientConfig(
      |?.none,
      |?.none,
      |?.none)
