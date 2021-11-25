package fx

import java.util.concurrent.StructuredExecutor
import scala.annotation.implicitNotFound
import java.util.concurrent.Callable
import java.util.concurrent.Future

@implicitNotFound(
  "Structured concurrency requires capability:\n* Structured"
)
opaque type Structured = StructuredExecutor

inline def structured[B](f: B * Structured): B  =
  val scope = StructuredExecutor.open("scala fx structured scope")
  given Structured = scope
  try f
  finally
    scope.join
    scope.close()

def fork[B](f: Callable[B]): Future[B] * Structured = 
  summon[Structured].fork(f)

def join: Unit * Structured =
  summon[Structured].join

private[fx] inline def callableOf[A](f: () => A): Callable[A] =
  new Callable[A] { def call() = f() }