package fx
package cats

import _root_.cats.*
import _root_.cats.implicits.*
import _root_.cats.effect.*
import _root_.cats.effect.unsafe.*
import fx.run
import fx.Fiber

import java.util.concurrent.{CancellationException, CompletableFuture, Executors, Future}
import scala.annotation.unchecked.uncheckedVariance
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, ExecutionException}
import scala.util.{Failure, Success}

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

def fromIO[A](program: IO[A])(using runtime: IORuntime): Structured ?=> Fiber[A] =
  val fiber = CompletableFuture[A]()
  track(fiber)
  val (future, close) = program.unsafeToFutureCancelable()
  setupCancellation(fiber, close)
  given ExecutionContext = runtime.compute
  future.onComplete { trying =>
    trying match
      case s: Success[a] => fiber.complete(s.get)
      case f: Failure[a] => fiber.completeExceptionally(f.exception)
  }
  fiber.asInstanceOf[Fiber[A]]

def setupCancellation[A](fiber: CompletableFuture[A], close: () => scala.concurrent.Future[Unit]) =
  fiber.whenComplete { (_, exception) =>
    if (exception != null && exception.isInstanceOf[CancellationException])
      Await.result(close(), Duration.Inf)
  }

/*
private fun Job.setupCancellation(future: CompletableFuture<*>) {
    future.whenComplete { _, exception ->
        cancel(exception?.let {
            it as? CancellationException ?: CancellationException("CompletableFuture was completed exceptionally", it)
        })
    }
}
*/

/*
suspend fun suspended(): A = suspendCoroutine { cont ->
    val connection = cont.context[SuspendConnection] ?: SuspendConnection.uncancellable

    IORunLoop.startCancellable(this, connection) {
      it.fold(cont::resumeWithException, cont::resume)
    }
  }
*/
