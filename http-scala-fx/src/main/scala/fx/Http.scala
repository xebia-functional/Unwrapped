package fx

import java.net.URI
import java.net.http.HttpHeaders
import java.net.http.HttpRequest
import java.net.http.HttpRequest.BodyPublishers
import java.net.http.HttpResponse
import java.net.http.HttpResponse.BodyHandlers
import java.time.Duration
import java.util.Random
import scala.annotation.tailrec

/**
 * Models the http scheme
 */
type Http[A] = (
    Structured,
    HttpRetryPolicy[A],
    Control[HttpExecutionException],
    HttpClient
) ?=> Fiber[HttpResponse[A]] // need to change the signature to be HttpResponse[A]

object Http:

  private def addHeadersToRequestBuilder(
      builder: HttpRequest.Builder,
      headers: HttpHeader*): HttpRequest.Builder = {
    headers.foldLeft(builder) { (builder: HttpRequest.Builder, header: HttpHeader) =>
      header.values.foldLeft[HttpRequest.Builder](builder) {
        (builder: HttpRequest.Builder, headerValue: String) =>
          builder.header(header.name, headerValue)
      }
    }
  }

  def GET[A](uri: URI, headers: HttpHeader*): HttpResponseMapper[A] ?=> Http[A] =
    GET(uri, 0, headers: _*)

  @tailrec
  private def GET[A](
      uri: URI,
      retryCount: Int,
      headers: HttpHeader*): HttpResponseMapper[A] ?=> Http[A] = {
    val mapper = summon[HttpResponseMapper[A]]
    val config = summon[HttpClientConfig]
    val retries = config.maximumRetries.getOrElse(summon[HttpRetries])

    val response: HttpResponse[A] = fork(() => {
      summon[HttpClient]
        .value
        .send(
          addHeadersToRequestBuilder(HttpRequest.newBuilder().GET().uri(uri), headers: _*)
            .build(),
          mapper.bodyHandler)
    }).join
    if (response.shouldRetry(retryCount)) {
      GET(uri, retryCount + 1, headers: _*)
    } else {
      fork(() => {
        response
      })
    }
  }

  def POST[A](
      uri: URI,
      body: String,
      headers: HttpHeader*): (HttpResponseMapper[String], HttpBodyMapper[A]) ?=> Http[String] =
    POST[String, String](uri, body, headers: _*)

  def POST[A, B](
      uri: URI,
      body: B,
      headers: HttpHeader*): (HttpResponseMapper[A], HttpBodyMapper[B]) ?=> Http[A] =
    POST(uri, body, 0, headers: _*)

  @tailrec
  private def POST[A, B](
      uri: URI,
      body: B,
      retryCount: Int = 0,
      headers: HttpHeader*): (HttpResponseMapper[A], HttpBodyMapper[B]) ?=> Http[A] = {
    val mapper = summon[HttpResponseMapper[A]]
    val bodyMapper = summon[HttpBodyMapper[B]]
    val config = summon[HttpClientConfig]
    val retries = config.maximumRetries.getOrElse(summon[HttpRetries])
    val response = fork(() =>
      summon[HttpClient]
        .value
        .send(
          addHeadersToRequestBuilder(
            HttpRequest.newBuilder().POST(bodyMapper.bodyPublisher(body)).uri(uri),
            headers: _*).build(),
          mapper.bodyHandler)).join
    if (response.shouldRetry(retryCount)) {
      POST(uri, body, retryCount + 1, headers: _*)
    } else {
      fork(() => response)
    }
  }

  def HEAD(uri: URI, headers: HttpHeader*): Http[Void] = HEAD(uri, 0, headers: _*)

  @tailrec
  private def HEAD(uri: URI, retryCount: Int = 0, headers: HttpHeader*): Http[Void] = {
    val config = summon[HttpClientConfig]
    val retries = config.maximumRetries.getOrElse(summon[HttpRetries])
    val response = fork(() =>
      summon[HttpClient]
        .value
        .send(
          addHeadersToRequestBuilder(HttpRequest.newBuilder().HEAD().uri(uri), headers: _*)
            .build(),
          BodyHandlers.discarding)).join
    if (response.shouldRetry(retryCount)) {
      HEAD(uri, retryCount + 1, headers: _*)
    } else fork(() => response)
  }

  def PUT[A, B](
      uri: URI,
      body: B,
      headers: HttpHeader*): (HttpResponseMapper[A], HttpBodyMapper[B]) ?=> Http[A] =
    PUT[A, B](uri, body, 0, headers: _*)

  @tailrec
  private def PUT[A, B](
      uri: URI,
      body: B,
      retryCount: Int = 0,
      headers: HttpHeader*): (HttpResponseMapper[A], HttpBodyMapper[B]) ?=> Http[A] = {
    val mapper = summon[HttpResponseMapper[A]]
    val bodyMapper = summon[HttpBodyMapper[B]]
    val config = summon[HttpClientConfig]
    val retries = config.maximumRetries.getOrElse(summon[HttpRetries])
    val response = fork(() =>
      summon[HttpClient]
        .value
        .send(
          addHeadersToRequestBuilder(
            HttpRequest.newBuilder().PUT(bodyMapper.bodyPublisher(body)).uri(uri),
            headers: _*).build(),
          mapper.bodyHandler)).join
    if (response.shouldRetry(retryCount)) {
      PUT(uri, body, retryCount + 1, headers: _*)
    } else {
      fork(() => response)
    }
  }

  @tailrec
  def DELETE[A](
      uri: URI,
      retryCount: Int = 0,
      headers: HttpHeader*): (HttpResponseMapper[A]) ?=> Http[A] = {
    val mapper = summon[HttpResponseMapper[A]]
    val config = summon[HttpClientConfig]
    val retries = config.maximumRetries.getOrElse(summon[HttpRetries])
    val response = fork(() =>
      summon[HttpClient]
        .value
        .send(
          addHeadersToRequestBuilder(HttpRequest.newBuilder().DELETE().uri(uri), headers: _*)
            .build(),
          mapper.bodyHandler)).join
    if (response.shouldRetry(retryCount)) {
      DELETE(uri, retryCount + 1, headers: _*)
    } else {
      fork(() => response)
    }
  }

  @tailrec
  def OPTIONS[A](
      uri: URI,
      retryCount: Int = 0,
      headers: HttpHeader*): (HttpResponseMapper[A]) ?=> Http[A] = {
    val mapper = summon[HttpResponseMapper[A]]
    val config = summon[HttpClientConfig]
    val retries = config.maximumRetries.getOrElse(summon[HttpRetries])
    val response = fork(() =>
      summon[HttpClient]
        .value
        .send(
          addHeadersToRequestBuilder(
            HttpRequest.newBuilder().method("OPTIONS", BodyPublishers.noBody).uri(uri)).build(),
          mapper.bodyHandler)).join
    if (response.shouldRetry(retryCount)) {
      OPTIONS(uri, retryCount + 1, headers: _*)
    } else {
      fork(() => response)
    }
  }

  @tailrec
  def TRACE[A](
      uri: URI,
      retryCount: Int = 0,
      headers: HttpHeader*): HttpResponseMapper[A] ?=> Http[A] = {
    val mapper = summon[HttpResponseMapper[A]]
    val config = summon[HttpClientConfig]
    val retries = config.maximumRetries.getOrElse(summon[HttpRetries])
    val response = fork(() =>
      summon[HttpClient]
        .value
        .send(
          addHeadersToRequestBuilder(
            HttpRequest.newBuilder().method("TRACE", BodyPublishers.noBody).uri(uri),
            headers: _*).build(),
          mapper.bodyHandler)).join
    if (response.shouldRetry(retryCount)) {
      TRACE(uri, retryCount + 1, headers: _*)
    } else {
      fork(() => response)
    }
  }

  @tailrec
  def PATCH[A, B](
      uri: URI,
      body: B,
      retryCount: Int = 0,
      headers: HttpHeader*): (HttpResponseMapper[A], HttpBodyMapper[B]) ?=> Http[A] = {
    val mapper = summon[HttpResponseMapper[A]]
    val config = summon[HttpClientConfig]
    val retries = config.maximumRetries.getOrElse(summon[HttpRetries])
    val response = fork(() =>
      summon[HttpClient]
        .value
        .send(
          addHeadersToRequestBuilder(
            HttpRequest
              .newBuilder()
              .method("PATCH", summon[HttpBodyMapper[B]].bodyPublisher(body))
              .uri(uri),
            headers: _*).build(),
          mapper.bodyHandler
        )).join
    if (response.shouldRetry(retryCount)) {
      PATCH(uri, body, retryCount + 1, headers: _*)
    } else {
      fork(() => response)
    }
  }
