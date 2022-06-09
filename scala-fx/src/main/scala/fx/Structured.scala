package fx

import jdk.incubator.concurrent.StructuredTaskScope
import scala.annotation.implicitNotFound
import java.util.concurrent.Callable
import java.util.concurrent.CancellationException
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import scala.annotation.implicitNotFound

@implicitNotFound(
  "Structured concurrency requires capability:\n% Structured"
)
opaque type Structured = StructuredTaskScope[Any]

extension (s: Structured)
  private[fx] def forked[A](callable: Callable[A]): Future[A] =
    s.fork(callable)

inline def structured[B](f: Structured ?=> B): B =
  val scope = new StructuredTaskScope[Any]()
  given Structured = scope
  try f
  finally
    scope.join
    scope.close()

def joinAll(using structured: Structured): Unit =
  structured.join

private[fx] inline def callableOf[A](f: () => A): Callable[A] =
  new Callable[A] { def call(): A = f() }
