package sttp
package fx

import _root_.fx.{given, *}
import StatusCodeToStatusCode.given
import sttp.monad.MonadError
import sttp.client3.SttpBackend
import sttp.capabilities.Effect
import sttp.client3.Request
import sttp.client3.Response
import sttp.client3.RequestBody
import sttp.model.Header
import sttp.model.Method
import java.net.URI
import java.net.{http => jnh}
import sttp.{model => sm}
import sm.ResponseMetadata

import scala.jdk.CollectionConverters.*
import sttp.client3.internal.BodyFromResponseAs
import sttp.client3.ResponseAs
import sttp.client3.internal.SttpFile
import sttp.client3.WebSocketResponseAs
import sttp.client3.ws.NotAWebSocketException
import sttp.client3.ws.GotAWebSocketException
import java.util.Optional
import javax.net.ssl.SSLSession
import java.nio.file.Files
import java.io.File
import java.nio.file.Path
import sttp.client3.NoBody
import sttp.client3.ByteBufferBody
import sttp.client3.InputStreamBody
import sttp.client3.FileBody
import sttp.client3.StreamBody
import sttp.client3.MultipartBody

class HttpScalaFXBackend(
    using client: HttpClient,
    control: Control[Throwable | HttpExecutionException])
    extends SttpBackend[Http, ReceiveStreams] {

  given MonadError[Http] = responseMonad

  def requestAsBody[R, A <: RequestBody[R]](r: A)(
      using ToHttpBodyMapper[A]): Http[HttpBodyMapper[A]] =
    handle(r.toHttpBodyMapper()) { e => HttpExecutionException(e).shift }

  def getRequestHeaders[T, R >: ReceiveStreams with Effect[Http], A](
      request: Request[T, R],
      body: A)(using bodyMapper: HttpBodyMapper[A]): Http[::[HttpHeader]] = {
    val `Content-Type`: HttpHeader = HttpHeader(
      ("Content-Type" -> ::[String](bodyMapper.mediaType.value, List.empty[String])))
    ::(
      `Content-Type`,
      request
        .headers
        .groupBy(_.name)
        .map { (p: (String, Seq[Header])) =>
          val values = p._2.map(_.value).toList
          HttpHeader(p._1, values.headOption.getOrElse(""), values.tail: _*)
        }
        .toList
    )
  }

  def getResponseHeaders[T](response: jnh.HttpResponse[T]): Http[Seq[sm.Header]] = {
    response.headers.map().asScala.toList.foldLeft(Seq.empty[sm.Header]) { (acc, jnhHeader) =>
      acc ++ jnhHeader._2.asScala.map(sm.Header(jnhHeader._1, _))
    }
  }

  private def bodyAsResponseAs[T](using HttpBodyMapper[Receive[Byte]], MonadError[Http]) = {
    new BodyFromResponseAs[Http, jnh.HttpResponse[Receive[Byte]], Nothing, Receive[Byte]] {
      override protected def withReplayableBody(
          response: jnh.HttpResponse[Receive[Byte]],
          replayableBody: Either[Array[Byte], SttpFile]
      ): Http[jnh.HttpResponse[Receive[Byte]]] = {
        new jnh.HttpResponse[Receive[Byte]] {
          def statusCode(): Int = response.statusCode()
          def request(): jnh.HttpRequest = response.request()
          def previousResponse(): Optional[jnh.HttpResponse[Receive[Byte]]] =
            Optional.of(response)
          def headers(): jnh.HttpHeaders = response.headers()
          def body(): Receive[Byte] = replayableBody.fold(
            bytes => streamOf(bytes.toList: _*),
            file => streamOf(file.readAsByteArray.toList: _*)
          )
          def sslSession(): Optional[SSLSession] = response.sslSession()
          def uri(): URI = response.uri()
          def version(): jnh.HttpClient.Version = response.version()
        }
      }
      override protected def regularIgnore(
          response: jnh.HttpResponse[Receive[Byte]]): Http[Unit] = {
        val x: Http[Receive[Byte]] = handle[Exception, Receive[Byte]](response.body()) { e =>
          HttpExecutionException(e).shift[Receive[Byte]]
        }.httpValue
        Http(())
      }
      override protected def regularAsByteArray(
          response: jnh.HttpResponse[Receive[Byte]]): Http[Array[Byte]] =
        handle[Exception, Array[Byte]](response.body().toList.toArray) { e =>
          HttpExecutionException(e).shift[Array[Byte]]
        }

      override protected def regularAsFile(
          response: jnh.HttpResponse[Receive[Byte]],
          file: SttpFile): Http[SttpFile] = {
        val pathWritten =
          handle[Exception, Path](Files.write(file.toPath, response.body.toList.toArray)) { e =>
            HttpExecutionException(e).shift[Path]
          }.httpValue
        Http(file)
      }

      override protected def regularAsStream(
          response: jnh.HttpResponse[Receive[Byte]]): Http[(Receive[Byte], () => Http[Unit])] =
        handle[Exception, (Receive[Byte], () => Http[Unit])](
          (response.body(), () => Http(()))) { e =>
          HttpExecutionException(e).shift[(Receive[Byte], () => Http[Unit])]
        }

      override protected def handleWS[T](
          responseAs: WebSocketResponseAs[T, _],
          meta: ResponseMetadata,
          ws: Nothing): Http[T] =
        HttpExecutionException(new RuntimeException("Websockets are unsupported")).shift[T]

      override protected def cleanupWhenNotAWebSocket(
          response: jnh.HttpResponse[Receive[Byte]],
          e: NotAWebSocketException): Http[Unit] =
        Http(())

      override protected def cleanupWhenGotWebSocket(
          response: Nothing,
          e: GotAWebSocketException): Http[Unit] =
        HttpExecutionException(new RuntimeException("Websockets are unsupported")).shift[Unit]
    }
  }

  private def toResponse[T, R >: ReceiveStreams with Effect[Http]](
      request: Request[T, R],
      response: jnh.HttpResponse[Receive[Byte]])(
      using StatusCodeToStatusCode[sm.StatusCode, StatusCode],
      StatusCodeToStatusCode[Int, sm.StatusCode]): Http[Response[T]] = {
    val status = response.statusCode().toStatusCode
    val statusText = status.toStatusCode.statusText
    val responseMetadata = ResponseMetadata(status, statusText, getResponseHeaders(response))
    val body = bodyAsResponseAs[T].apply(
      request.response,
      responseMetadata,
      Left(response)
    )
    ???
  }

  def getUri[T, R >: ReceiveStreams with Effect[Http]](request: Request[T, R]): Http[URI] = {
    handle(request.uri.toJavaUri) { e => HttpExecutionException(e).shift }
  }

  def makeRequest[T, R >: ReceiveStreams with Effect[Http], A](request: Request[T, R], body: A)(
      using ToHttpBodyMapper[A],
      HttpResponseMapper[Receive[Byte]]): Http[Response[T]] = {
    given bm: HttpBodyMapper[A] = body.toHttpBodyMapper()
    val headers: ::[HttpHeader] = getRequestHeaders(request, body)
    val uri: URI = getUri(request).httpValue
    request.method match {
      case Method.DELETE => toResponse(request, uri.DELETE[Receive[Byte]](headers: _*))
      case Method.GET => toResponse(request, uri.GET[Receive[Byte]](headers: _*))
      case Method.HEAD =>
        toResponse(
          request,
          uri.HEAD(headers: _*).fmap { response =>
            new jnh.HttpResponse[Receive[Byte]] {
              def statusCode(): Int = response.statusCode()
              def request(): jnh.HttpRequest = response.request()
              def previousResponse(): Optional[jnh.HttpResponse[Receive[Byte]]] =
                Optional.ofNullable(null)
              def headers(): jnh.HttpHeaders = response.headers()
              def body(): Receive[Byte] = streamOf(Array.emptyByteArray.toList: _*)
              def sslSession(): Optional[SSLSession] = response.sslSession()
              def uri(): URI = response.uri()
              def version(): jnh.HttpClient.Version = response.version()
            }
          }
        )
      case Method.OPTIONS => toResponse(request, uri.OPTIONS[Receive[Byte]](headers: _*))
      case Method.PATCH =>
        toResponse(request, uri.patch[Receive[Byte]](body, headers: _*))
      case Method.POST =>
        toResponse(request, uri.post[Receive[Byte]](body, headers: _*))
      case Method.PUT => toResponse(request, uri.put[Receive[Byte]](body, headers: _*))
      case Method.TRACE => toResponse(request, uri.TRACE(headers: _*))
      case m @ _ =>
        HttpExecutionException(new RuntimeException(s"Method: $m is unsupported."))
          .shift[Response[T]]
    }
  }

  def send[T, R >: ReceiveStreams with Effect[Http]](
      request: Request[T, R]): Http[Response[T]] =
    request.body match {
      case x: NoBody.type => makeRequest(request, x)
      case x: ByteBufferBody => makeRequest(request, x)
      case x: InputStreamBody => makeRequest(request, x)
      case x: FileBody => makeRequest(request, x)
      case x @ StreamBody(_) => {
        val body = handle[Exception, StreamBody[Receive[Byte], ReceiveStreams]](
          x.asInstanceOf[StreamBody[Receive[Byte], ReceiveStreams]]) { e =>
          HttpExecutionException(e).shift[StreamBody[Receive[Byte], ReceiveStreams]]
        }.httpValue
        makeRequest(request, body)
      }
      case x @ MultipartBody(_) =>
        val body = handle[Exception, MultipartBody[R]](x.asInstanceOf[MultipartBody[R]]) { e =>
          HttpExecutionException(e).shift[MultipartBody[R]]
        }.httpValue
        makeRequest(request, body)
      case b =>
        HttpExecutionException(new RuntimeException(s"unsupported body type: ${b.toString()}"))
          .shift[Response[T]]
    }

  def close(): Http[Unit] = ???

  def responseMonad: MonadError[Http] = new MonadError[Http] {
    def ensure[T](f: Http[T], e: => Http[Unit]): Http[T] = {
      val x: Throwable | T = run(f)
      x match {
        case ex: Throwable =>
          val ignored = run(e)
          f
        case a => unit(a.asInstanceOf[T]) // the only other possible
        // value is T. Since we
        // cannot change the
        // signature, we cast
      }
    }
    def error[T](t: Throwable): Http[T] = t.shift
    def flatMap[T, T2](fa: Http[T])(f: T => Http[T2]): Http[T2] = fa.bindMap(f)
    protected def handleWrappedError[T](rt: Http[T])(
        h: PartialFunction[Throwable, Http[T]]): Http[T] = {
      val x: Throwable | T = run(rt)
      x match {
        case ex: Throwable if h.isDefinedAt(ex) => h(ex)
        case ex: Throwable => ex.shift[T]
        case a => unit(a.asInstanceOf[T]) // because the only other
        // possible value is T, and
        // we cannot change the
        // signature to include a
        // manifest, we cast here

      }
    }
    def map[T, T2](fa: Http[T])(f: T => T2): Http[T2] = fa.fmap(f)
    def unit[T](t: T): Http[T] = Http(t)
  }
}
