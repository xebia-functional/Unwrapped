package fx

import java.net.http.HttpResponse

class FakeHttpResponse[A](
    val status: StatusCode
) extends HttpResponse[A]:
  def body(): A = ???
  def headers(): java.net.http.HttpHeaders = ???
  def previousResponse(): java.util.Optional[java.net.http.HttpResponse[A]] = ???
  def request(): java.net.http.HttpRequest = ???
  def sslSession(): java.util.Optional[javax.net.ssl.SSLSession] = ???
  def uri(): java.net.URI = ???
  def version(): java.net.http.HttpClient.Version = ???

  override def statusCode(): Int = status.statusCodeValue
