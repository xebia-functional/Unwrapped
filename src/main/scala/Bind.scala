package fx

import scala.annotation.implicitNotFound
import cats.effect.IO
import cats.effect.unsafe.IORuntime
import scala.concurrent.ExecutionContext
import cats.effect.unsafe.Scheduler
import cats.effect.unsafe.IORuntimeConfig

extension [R, A](fa: Either[R, A])
  def bind: A * Errors[R] = fa.fold(_.shift, identity)

extension [R, A](fa: List[Either[R, A]])
  def bind: List[A] * Errors[R] = fa.map(_.bind)

extension [R, A](fa: IO[A])
  /** TODO suspend Free monads like IO for integrations
    */
  def bind: A * Control[Throwable] =
    val ec = new ExecutionContext {
      def execute(runnable: Runnable): Unit =
        Thread.startVirtualThread(runnable)
      def reportFailure(cause: Throwable): Unit =
        cause.printStackTrace
    }
    val sc: Scheduler = ???
    given IORuntime = IORuntime(
      compute = ec,
      blocking = ec,
      scheduler = sc,
      shutdown = () => (),
      config = IORuntimeConfig()
    )
    fa.unsafeRunSync()
