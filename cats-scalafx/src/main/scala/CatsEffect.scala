package fx
package cats

import _root_.cats.*
import _root_.cats.implicits.*
import _root_.cats.effect.*
import _root_.cats.effect.unsafe.implicits.*
import fx.run
import fx.Fiber

import java.util.concurrent.{CancellationException, CompletableFuture, Future}
import scala.concurrent.ExecutionException

case class NonThrowableFXToCatsException[R](underlying: R)
    extends RuntimeException(
      s"Control was shifted to when running a scala-fx program $underlying")

def toCatsEffect[F[_]: [g[_]] =>> ApplicativeError[g, Throwable], R: Manifest, A: Manifest](
  program: Control[R] ?=> A): F[A] =
  val x = run(program)
  x match {
    case x:A => x.pure
    case err: Throwable => ApplicativeError[F, Throwable].raiseError[A](err)
    case err: R => ApplicativeError[F, Throwable].raiseError[A](NonThrowableFXToCatsException(err))
    case _ => ApplicativeError[F, Throwable].raiseError[A](RuntimeException("Impossible!"))
  }


def fromIO[A](program: IO[A]): Structured ?=> Fiber[A] =
  val fiber = CompletableFuture[A]()
  track(fiber)
  val future = program.unsafeToCompletableFuture()
  setupCancellation(fiber, future)
  future.whenComplete { (a, exception) =>
    if (exception == null)
      fiber.complete(a)
    else fiber.completeExceptionally(exception)
  }
  fiber.asInstanceOf[Fiber[A]]

def setupCancellation[A](fiber: Future[A], future: CompletableFuture[A]) =
  future.whenComplete { (_, exception) =>
    if (exception != null && exception.isInstanceOf[CancellationException])
      fiber.cancel()
  }
