package sttp
package fx

import sttp.client3.SttpBackend
import sttp.capabilities.Streams
import _root_.fx.*
import sttp.capabilities.Effect
import sttp.client3.{Request, Response}
import sttp.monad.MonadError

class HttpScalaFXBackend(using client: HttpClient, control: Control[Throwable])
    extends SttpBackend[Http, Streams[Receive[Byte]] with Effect[Http]] {

  def send[T, R >: Streams[Receive[Byte]] with Effect[Http]](
      request: Request[T, R]): Http[Response[T]] = ???

  def close(): Http[Unit] = ???

  def responseMonad: MonadError[Http] = new MonadError[Http] {
    def ensure[T](f: Http[T], e: => Http[Unit]): Http[T] = ???
    def error[T](t: Throwable): Http[T] = t.shift
    def flatMap[T, T2](fa: Http[T])(f: T => Http[T2]): Http[T2] = ???
    protected def handleWrappedError[T](rt: Http[T])(
        h: PartialFunction[Throwable, Http[T]]): Http[T] = ???
    def map[T, T2](fa: Http[T])(f: T => T2): Http[T2] = ???
    def unit[T](t: T): Http[T] = ???
  }
}
