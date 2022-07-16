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
    HttpRetryPolicy[A],
    Control[HttpExecutionException],
    HttpClient
) ?=> A

extension [A](
    http: Http[A])(using HttpRetryPolicy[A], Control[HttpExecutionException], HttpClient)
  def value: A = http
  def fmap[B](f: A => B): Http[B] =
    f(http)
  def bindMap[B](f: A => Http[B]): Http[B] =
    f(http)


extension(uri: URI)
  private def addHeadersToRequestBuilder(
      builder: HttpRequest.Builder,
      headers: HttpHeader*): HttpRequest.Builder =
    headers.foldLeft(builder) { (builder: HttpRequest.Builder, header: HttpHeader) =>
      header.values.foldLeft[HttpRequest.Builder](builder) {
        (builder: HttpRequest.Builder, headerValue: String) =>
          builder.header(header.name, headerValue)
      }
    }

  def GET[A](): HttpResponseMapper[A] ?=> Http[HttpResponse[A]] =
    GET(0, List.empty: _*)

  def GET[A](headers: HttpHeader*): HttpResponseMapper[A] ?=> Http[HttpResponse[A]] =
    GET(0, headers: _*)

  @tailrec
  private def GET[A](
      retryCount: Int,
      headers: HttpHeader*): HttpResponseMapper[A] ?=> Http[HttpResponse[A]] =
    val mapper = summon[HttpResponseMapper[A]]
    val config = summon[HttpClientConfig]
    val retries = config.maximumRetries.getOrElse(summon[HttpRetries])

    val response: HttpResponse[A] =
      summon[HttpClient]
        .client
        .send(
          addHeadersToRequestBuilder(HttpRequest.newBuilder().GET().uri(uri), headers: _*)
            .build(),
          mapper.bodyHandler)

    if (response.shouldRetry(retryCount)) {
      GET(retryCount + 1, headers: _*)
    } else {
      response
    }

  def POST[A, B](body: B, headers: HttpHeader*)
      : (HttpResponseMapper[A], HttpBodyMapper[B]) ?=> Http[HttpResponse[A]] =
    POST(body, 0, headers: _*)

  @tailrec
  private def POST[A, B](body: B, retryCount: Int = 0, headers: HttpHeader*)
      : (HttpResponseMapper[A], HttpBodyMapper[B]) ?=> Http[HttpResponse[A]] =
    val mapper = summon[HttpResponseMapper[A]]
    val bodyMapper = summon[HttpBodyMapper[B]]
    val config = summon[HttpClientConfig]
    val retries = config.maximumRetries.getOrElse(summon[HttpRetries])
    val response =
      summon[HttpClient]
        .client
        .send(
          addHeadersToRequestBuilder(
            HttpRequest.newBuilder().POST(bodyMapper.bodyPublisher(body)).uri(uri),
            headers: _*).build(),
          mapper.bodyHandler)
    if (response.shouldRetry(retryCount)) {
      POST(body, retryCount + 1, headers: _*)
    } else {
      response
    }

  def HEAD(headers: HttpHeader*): Http[HttpResponse[Void]] = HEAD(0, headers: _*)

  @tailrec
  private def HEAD(
      retryCount: Int = 0,
      headers: HttpHeader*): Http[HttpResponse[Void]] =
    val config = summon[HttpClientConfig]
    val retries = config.maximumRetries.getOrElse(summon[HttpRetries])
    val response =
      summon[HttpClient]
        .client
        .send(
          addHeadersToRequestBuilder(HttpRequest.newBuilder().HEAD().uri(uri), headers: _*)
            .build(),
          BodyHandlers.discarding)
    if (response.shouldRetry(retryCount)) {
      HEAD(retryCount + 1, headers: _*)
    } else response

  def PUT[A, B](body: B, headers: HttpHeader*)
      : (HttpResponseMapper[A], HttpBodyMapper[B]) ?=> Http[HttpResponse[A]] =
    PUT[A, B](body, 0, headers: _*)

  @tailrec
  private def PUT[A, B](body: B, retryCount: Int = 0, headers: HttpHeader*)
      : (HttpResponseMapper[A], HttpBodyMapper[B]) ?=> Http[HttpResponse[A]] =
    val mapper = summon[HttpResponseMapper[A]]
    val bodyMapper = summon[HttpBodyMapper[B]]
    val config = summon[HttpClientConfig]
    val retries = config.maximumRetries.getOrElse(summon[HttpRetries])
    val response =
      summon[HttpClient]
        .client
        .send(
          addHeadersToRequestBuilder(
            HttpRequest.newBuilder().PUT(bodyMapper.bodyPublisher(body)).uri(uri),
            headers: _*).build(),
          mapper.bodyHandler)
    if (response.shouldRetry(retryCount)) {
      PUT(body, retryCount + 1, headers: _*)
    } else response

  def DELETE[A](
      headers: HttpHeader*): (HttpResponseMapper[A]) ?=> Http[HttpResponse[A]] =
    DELETE[A](0, headers: _*)

  @tailrec
  private def DELETE[A](
      retryCount: Int = 0,
      headers: HttpHeader*): (HttpResponseMapper[A]) ?=> Http[HttpResponse[A]] =
    val mapper = summon[HttpResponseMapper[A]]
    val config = summon[HttpClientConfig]
    val retries = config.maximumRetries.getOrElse(summon[HttpRetries])
    val response =
      summon[HttpClient]
        .client
        .send(
          addHeadersToRequestBuilder(HttpRequest.newBuilder().DELETE().uri(uri), headers: _*)
            .build(),
          mapper.bodyHandler)
    if (response.shouldRetry(retryCount)) {
      DELETE(retryCount + 1, headers: _*)
    } else {
      response
    }

  def OPTIONS[A](
      headers: HttpHeader*): (HttpResponseMapper[A]) ?=> Http[HttpResponse[A]] =
    OPTIONS(0, headers: _*)

  @tailrec
  private def OPTIONS[A](
      retryCount: Int = 0,
      headers: HttpHeader*): (HttpResponseMapper[A]) ?=> Http[HttpResponse[A]] =
    val mapper = summon[HttpResponseMapper[A]]
    val config = summon[HttpClientConfig]
    val retries = config.maximumRetries.getOrElse(summon[HttpRetries])
    val response =
      summon[HttpClient]
        .client
        .send(
          addHeadersToRequestBuilder(
            HttpRequest.newBuilder().method("OPTIONS", BodyPublishers.noBody).uri(uri)).build(),
          mapper.bodyHandler)
    if (response.shouldRetry(retryCount)) {
      OPTIONS(retryCount + 1, headers: _*)
    } else response

  def TRACE[A](
      headers: HttpHeader*): HttpResponseMapper[A] ?=> Http[HttpResponse[A]] =
    TRACE[A](0, headers: _*)

  @tailrec
  private def TRACE[A](
      retryCount: Int = 0,
      headers: HttpHeader*): HttpResponseMapper[A] ?=> Http[HttpResponse[A]] =
    val mapper = summon[HttpResponseMapper[A]]
    val config = summon[HttpClientConfig]
    val retries = config.maximumRetries.getOrElse(summon[HttpRetries])
    val response =
      summon[HttpClient]
        .client
        .send(
          addHeadersToRequestBuilder(
            HttpRequest.newBuilder().method("TRACE", BodyPublishers.noBody).uri(uri),
            headers: _*).build(),
          mapper.bodyHandler)
    if (response.shouldRetry(retryCount)) {
      TRACE(retryCount + 1, headers: _*)
    } else response

  def PATCH[A, B](body: B, headers: HttpHeader*)
      : (HttpResponseMapper[A], HttpBodyMapper[B]) ?=> Http[HttpResponse[A]] =
    PATCH(body, 0, headers: _*)

  @tailrec
  private def PATCH[A, B](body: B, retryCount: Int = 0, headers: HttpHeader*)
      : (HttpResponseMapper[A], HttpBodyMapper[B]) ?=> Http[HttpResponse[A]] =
    val mapper = summon[HttpResponseMapper[A]]
    val config = summon[HttpClientConfig]
    val retries = config.maximumRetries.getOrElse(summon[HttpRetries])
    val response =
      summon[HttpClient]
        .client
        .send(
          addHeadersToRequestBuilder(
            HttpRequest
              .newBuilder()
              .method("PATCH", summon[HttpBodyMapper[B]].bodyPublisher(body))
              .uri(uri),
            headers: _*).build(),
          mapper.bodyHandler
        )
    if (response.shouldRetry(retryCount)) {
      PATCH(body, retryCount + 1, headers: _*)
    } else response

object Http:

  def apply[A](a: A): Http[A] =
    a

  
