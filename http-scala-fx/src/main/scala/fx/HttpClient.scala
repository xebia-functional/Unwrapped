package fx

import java.net.URI
import java.net.{http => jnh}
import java.time.Duration
import java.net.http.HttpRequest

opaque type HttpClient = jnh.HttpClient

extension (h: HttpClient) def client: jnh.HttpClient = h

object HttpClient:
  System.setProperty("jdk.httpclient.allowRestrictedHeaders", "Content-Length")
  def apply(a: jnh.HttpClient): HttpClient = a
  def apply(using config: HttpClientConfig): HttpClient =
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
