package fx.sip

import jdk.incubator.concurrent.StructuredTaskScope

import java.util.concurrent.*
import scala.annotation.implicitNotFound
import scala.jdk.FutureConverters.*

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
