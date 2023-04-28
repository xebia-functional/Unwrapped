package unwrapped

import jdk.incubator.concurrent.StructuredTaskScope
import jdk.incubator.concurrent.StructuredTaskScope.FactoryHolder

import scala.annotation.implicitNotFound
import java.util.concurrent.{
  Callable,
  CancellationException,
  Executor,
  ExecutorService,
  Executors,
  Future,
  ThreadFactory
}
import java.util.concurrent.atomic.AtomicReference
import scala.annotation.implicitNotFound

@implicitNotFound(
  "Structured concurrency requires capability:\n% Structured"
)
case class Structured(
    name: String,
    threadFactory: ThreadFactory,
    externalFibers: AtomicReference[List[Future[Any]]],
    scope: StructuredTaskScope[Any])

extension (s: Structured)
  private[unwrapped] def forked[A](callable: Callable[A]): Future[A] =
    s.scope.fork(callable)

inline def structured[B](f: Structured ?=> B): B =
  val name = "structured"
  val threadFactory: ThreadFactory = Thread.ofVirtual().factory()
  val scope = new StructuredTaskScope[Any]()
  given Structured = Structured(name, threadFactory, AtomicReference(List.empty), scope)
  try f
  finally
    joinAll
    scope.close()

def joinAll(using structured: Structured): Unit =
  structured.externalFibers.get().foreach { fiber => if (!fiber.isCancelled) fiber.join }
  structured.scope.join

def track[A](fiber: Future[A])(using structured: Structured): Unit =
  structured.externalFibers.updateAndGet(_.appended(fiber.asInstanceOf[Future[Any]]))

private[unwrapped] inline def callableOf[A](f: () => A): Callable[A] =
  () => f()
