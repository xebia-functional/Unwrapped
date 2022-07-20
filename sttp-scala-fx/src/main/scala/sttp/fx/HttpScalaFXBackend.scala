package sttp
package fx

import sttp.client3.SttpBackend
import sttp.capabilities.Streams
import _root_.fx.{given, *}
import sttp.capabilities.Effect
import sttp.client3.{Request, Response}
import sttp.monad.MonadError
import sttp.model.Methods
import sttp.model.Method
import java.net.URI
import sttp.model.Header
import sttp.client3.Identity
import java.net.http.HttpResponse
import scala.jdk.CollectionConverters.*
import sttp.model.RequestMetadata
import java.util.zip.Deflater

class HttpScalaFXBackend(using client: HttpClient, control: Control[Throwable])
    extends SttpBackend[Http, Streams[Receive[Byte]] with Effect[Http]] {

  def send[T, R >: Streams[Receive[Byte]] with Effect[Http]](
      request: Request[T, R]): Http[Response[T]] = {
    val method: Identity[Method] = request.method
    val body = request.body
    val headers: List[HttpHeader] = request
      .headers
      .groupBy(_.name)
      .map { p =>
        val values = p._2.map(_.value).toList
        HttpHeader(p._1, values.headOption.getOrElse(""), values.tail: _*)
      }
      .toList
    val uri = request.uri
    if(method == Method.GET){

    } else {

    }
    val httpResponse = method match {
      case Method.GET => uri.toJavaUri.GET[Receive[Byte]](headers: _*)
      case Method.HEAD => uri.toJavaUri.HEAD(headers: _*).fmap{ (r:HttpResponse[Void]) =>
        Response(null, sttp.model.StatusCode(r.statusCode()), "", r.headers().map().asScala.flatMap{kv =>
          kv._2.asScala.map{ headerValue =>
            Header(kv._1, headerValue)
          }
        }.toList, List.empty, RequestMetadata(method, uri, request.headers))
      }
      case Method.POST =>
        val response = uri.toJavaUri.POST[Receive[Byte], String](body.show, headers:_*)
        if(response == OK.value){
          val byteStream = response.body().transform{byte =>
            
          }
        }
          
          
        }
      case Method.PUT => uri.toJavaUri.PUT[Receive[Byte], String](body.show, headers: _*)
      case Method.DELETE => uri.toJavaUri.DELETE[Receive[Byte]](headers: _*)
      case Method.OPTIONS => uri.toJavaUri.OPTIONS[Receive[Byte]](headers: _*)
      case Method.PATCH => uri.toJavaUri.PATCH[Receive[Byte], String](body.show, headers: _*)
      case Method.TRACE => uri.toJavaUri.TRACE[Receive[Byte]](headers: _*)
      case Method.CONNECT => ???
    }
    ???
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
