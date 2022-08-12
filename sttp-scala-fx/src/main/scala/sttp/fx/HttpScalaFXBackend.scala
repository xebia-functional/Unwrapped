package sttp
package fx

import _root_.fx.{given, *}
import sttp.monad.MonadError
import sttp.client3.SttpBackend
import sttp.capabilities.Effect
import sttp.client3.Request
import sttp.client3.Response
import sttp.client3.RequestBody

class HttpScalaFXBackend(
    using client: HttpClient,
    control: Control[Throwable | HttpExecutionException])
    extends SttpBackend[Http, ReceiveStreams] {

  given MonadError[Http] = responseMonad

  def requestAsBody[R, A <: RequestBody[R]](r: A)(
      using ToHttpBodyMapper[A]): Http[HttpBodyMapper[A]] =
    responseMonad.handleError(r.toHttpBodyMapper()) { case e: Throwable => e.shift }

  def send[T, R >: ReceiveStreams with Effect[Http]](
    request: Request[T, R]): Http[Response[T]] =
    val bodyMapper: HttpBodyMapper[RequestBody[R]] = requestAsBody(request.body).httpValue
    request.method match {
      //map to request and do bodyFromResponseAs
      case _ => ???
    }
    ???

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
