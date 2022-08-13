package sttp
package fx

import _root_.fx.{given, *}
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

class HttpScalaFXBackend(
    using client: HttpClient,
    control: Control[Throwable | HttpExecutionException])
    extends SttpBackend[Http, ReceiveStreams] {

  given MonadError[Http] = responseMonad

  def requestAsBody[R, A <: RequestBody[R]](r: A)(
    using ToHttpBodyMapper[A]): Http[HttpBodyMapper[A]] =
    
    responseMonad.handleError(r.toHttpBodyMapper()) { case e: Throwable => e.shift }

  def getHeaders[T, R >: ReceiveStreams with Effect[Http]](request: Request[T, R])(
      using bodyMapper: HttpBodyMapper[RequestBody[R]]): Http[::[HttpHeader]] = {
    val `Content-Type`: HttpHeader = HttpHeader(
      ("Content-Type" -> ::[String](bodyMapper.mediaType.value, List.empty[String])))
    ::(`Content-Type`, request.headers.groupBy(_.name).map { (p: (String, Seq[Header])) =>
      val values = p._2.map(_.value).toList
      HttpHeader(p._1, values.headOption.getOrElse(""), values.tail: _*)
    }.toList)
  }

  def toResponse[T, R >: ReceiveStreams with Effect[Http], A](response: jnh.HttpResponse[A]): Http[Response[T]] = {
    ???
  }

  def getUri[T, R >: ReceiveStreams with Effect[Http]](request: Request[T, R]): Http[URI] = {
    handle(request.uri.toJavaUri){ e => HttpExecutionException(e).shift}
  }


  def makeRequest[T, R >: ReceiveStreams with Effect[Http]](
      request: Request[T, R])(using HttpBodyMapper[RequestBody[R]], HttpResponseMapper[T]): Http[Response[T]] = {
    val headers: ::[HttpHeader] = getHeaders(request)
    val uri: URI = getUri(request).httpValue
    request.method match {
      case Method.CONNECT => handle(???){e => HttpExecutionException(e).shift}
      case Method.DELETE => toResponse(uri.DELETE[T](headers: _*))
      case Method.GET => toResponse(uri.GET[T](headers: _*))
      case Method.HEAD => toResponse(uri.HEAD(headers: _*))
      case Method.OPTIONS => toResponse(uri.OPTIONS(headers: _*))
      case Method.PATCH => toResponse(uri.PATCH(request.body, headers: _*))
      case Method.POST => toResponse(uri.POST(request.body, headers: _*))
      case Method.PUT => toResponse(uri.PUT(request.body, headers: _*))
      case Method.TRACE => toResponse(uri.TRACE(headers: _*))
    }
  }

  def send[T, R >: ReceiveStreams with Effect[Http]](
      request: Request[T, R])(using HttpResponseMapper[T]): Http[Response[T]] =
    given bodyMapper: HttpBodyMapper[RequestBody[R]] = requestAsBody(request.body).httpValue
    makeRequest(request)

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
