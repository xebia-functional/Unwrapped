package fx

import java.util.concurrent.StructuredExecutor
import scala.annotation.implicitNotFound
import java.util.concurrent.Callable
import java.util.concurrent.Future
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

import java.util.concurrent.CancellationException

@implicitNotFound(
  "Structured concurrency requires capability:\n* Structured"
)
opaque type Structured = StructuredExecutor

extension (s: Structured)
  private[fx] def forked[A](callable: Callable[A]): Future[A] =
    s.fork(callable)

inline def structured[B](f: B * Structured): B =
  val scope = StructuredExecutor.open("scala fx structured scope")
  given Structured = scope
  try f
  finally
    scope.join
    scope.close()

def joinAll: Unit * Structured =
  summon[Structured].join

private[fx] inline def callableOf[A](f: () => A): Callable[A] =
  new Callable[A] { def call() = f() }
