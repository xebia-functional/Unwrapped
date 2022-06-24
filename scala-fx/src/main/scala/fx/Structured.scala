package fx

import jdk.incubator.concurrent.StructuredTaskScope

import scala.annotation.implicitNotFound
import java.util.concurrent.Callable
import java.util.concurrent.CancellationException
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.atomic.AtomicReference
import scala.annotation.implicitNotFound

@implicitNotFound(
  "Structured concurrency requires capability:\n% Structured"
)
case class Structured(externalFibers: AtomicReference[List[Fiber[Any]]], scope: StructuredTaskScope[Any])

extension (s: Structured)
  private[fx] def forked[A](callable: Callable[A]): Future[A] =
    s.scope.fork(callable)

inline def structured[B](f: Structured ?=> B): B =
  val scope = new StructuredTaskScope[Any]()
  given Structured = Structured(AtomicReference(List.empty), scope)
  try f
  finally
    joinAll
    scope.close()

def joinAll(using structured: Structured): Unit =
  structured.externalFibers.get().foreach(_.join)
  structured.scope.join

def track[A](fiber: Future[A])(using structured: Structured): Unit =
  structured.externalFibers.updateAndGet(_.appended(fiber.asInstanceOf[Fiber[Any]]))

private[fx] inline def callableOf[A](f: () => A): Callable[A] =
  () => f()
