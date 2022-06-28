package fx

import java.net.URI
import java.time.Duration
import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodyHandlers
import java.net.http.HttpResponse
import scala.annotation.tailrec
import java.net.http.HttpHeaders

/**
 * Models the http scheme
 */
opaque type Http[A] = (
    HttpRetryPolicy,
    Structured,
    Control[HttpExecutionException],
    fx.HttpClient,
    ) ?=> A

extension [A](a: Http[A])
  @tailrec
  def GET(uri: URI): HttpResponseMapper[A] ?=> Http[A] = {
    val mapper = summon[HttpResponseMapper[A]]
    val config = summon[HttpClientConfig]
    val retryPolicy = config.retryPolicy
    val retries = config.maximumRetries.getOrElse(summon[HttpRetries])
    val response = summon[fx.HttpClient]
      .value
      .send(HttpRequest.newBuilder().GET().uri(uri).build(), mapper.bodyHandler)
    if (response.shouldRetry) {
      GET(uri)
    } else {
      response.body
    }
  }

  def POST[B](uri: URI, body: B): (HttpResponseMapper[A], HttpBodyMapper[B]) ?=> Http[A] = {
    val mapper = summon[HttpResponseMapper[A]]
    val bodyMapper = summon[HttpBodyMapper[B]]
    val config = summon[HttpClientConfig]
    val retryPolicy = config.retryPolicy
    val retries = config.maximumRetries.getOrElse(summon[HttpRetries])
    val response = summon[fx.HttpClient]
      .value
      .send(HttpRequest.newBuilder().POST(bodyMapper.bodyPublisher(body)).uri(uri).build(), mapper.bodyHandler)
    if (response.shouldRetry) {
      GET(uri)
    } else {
      response.body
    }
  }

  @tailrec
  def HEAD(uri: URI): Http[HttpHeaders] = {
    val config = summon[HttpClientConfig]
    val retryPolicy = config.retryPolicy
    val retries = config.maximumRetries.getOrElse(summon[HttpRetries])
    val response = summon[fx.HttpClient]
      .value
      .send(HttpRequest.newBuilder().HEAD().uri(uri).build(), BodyHandlers.discarding)
    if (response.shouldRetry) {
      HEAD(uri)
    } else response.headers
  }
  

object Http
