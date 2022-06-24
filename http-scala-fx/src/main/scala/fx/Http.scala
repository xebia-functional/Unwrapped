package fx

import java.net.URI
import java.time.Duration
import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodyHandlers
import java.net.http.HttpResponse

/**
 * Models the http scheme
 */
opaque type Http[A] = (HttpRetryPolicy, Structured, Control[HttpExecutionException], fx.HttpClient, HttpResponseMapper[A] ) ?=> A

object Http:
  
  def GET[A](uri: URI): Http[A] = {
    val mapper = summon[HttpResponseMapper[A]]
    val config = summon[HttpClientConfig]
    val retryPolicy = config.retryPolicy
    val retries = config.maximumRetries.getOrElse(summon[HttpRetries])
    val response: HttpResponse[String] = summon[fx.HttpClient].value.send(HttpRequest.newBuilder().GET.uri(uri).build(), BodyHandlers.ofString())
    if(response.shouldRetry){
      GET(uri)
    } else if(response.statusCode() == 200){
      response.toA
    }
  }
  def HEAD(uri: URI): Http ?=> A = ???
  def POST(uri: URI): HttpClientConfig ?=> A = ???
  def PUT(uri: URI): HttpClientConfig ?=> A = ???
  def PATCH(uri: URI): HttpClientConfig ?=> A = ???

