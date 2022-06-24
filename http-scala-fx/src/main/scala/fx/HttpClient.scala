package fx

import java.net.URI
import java.net.{http => jnh}
import java.time.Duration
import java.net.http.HttpRequest

opaque type HttpClient = jnh.HttpClient

extension(h: HttpClient)
  def value: jnh.HttpClient = h

object HttpClient:
  def apply[A](using HttpClientConfig): HttpClient =
    val config = summon[HttpClientConfig]
    // need to setup proxy and ssl contexts
    jnh
      .HttpClient
      .newBuilder()
      .connectTimeout(Duration.ofSeconds(
        config.connectionTimeout.getOrElse(summon[HttpConnectionTimeout]).value))
      .followRedirects(config.followRedirects.getOrElse(summon[HttpFollowRedirects]).value)
      .version(jnh.HttpClient.Version.HTTP_2)
      .build()

  given HttpClient = apply
