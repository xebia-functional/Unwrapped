package fx

import java.util.concurrent.StructuredExecutor
import scala.annotation.implicitNotFound
import java.util.concurrent.Callable
import java.util.concurrent.Future
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

import java.util.concurrent.CancellationException
import java.util.function.BiConsumer
import java.util.concurrent.CompletableFuture

@implicitNotFound(
  "Structured concurrency requires capability:\n% Structured"
)
opaque type Structured = StructuredExecutor

extension (s: Structured)
  
  private[fx] def forked[A](callable: Callable[A]): Future[A] =
    s.fork(callable)

  private[fx] def forkedOnComplete[A](callable: Callable[A])(onComplete: (StructuredExecutor, Future[A]) => Unit): Future[A] =
    s.fork(callable, biconsumer(onComplete))

inline def structured[B](f: B % Structured): B =
  val scope = StructuredExecutor.open("scala fx structured scope")
  given Structured = scope
  try f
  finally
    scope.join
    scope.close()

def joinAll: Unit % Structured =
  summon[Structured].join

def fork[B](f: () => B): Fiber[B] % Structured =
  summon[Structured].forked(callableOf(f))

def forkAndComplete[A](f: () => A)(onComplete: (Structured, Fiber[A]) => Unit): Fiber[A] % Structured =
  summon[Structured].forkedOnComplete(callableOf(f))(onComplete)

private[fx] inline def callableOf[A](f: () => A): Callable[A] =
  new Callable[A] { def call() = f() }

private[fx] inline def biconsumer[A, B](f: (A, B) => Unit): BiConsumer[A, B] =
  new BiConsumer[A, B] { def accept(a: A, b: B): Unit = f(a, b) }

opaque type Fiber[A] = Future[A]

extension [A](fiber: Fiber[A])
  def join: A % Structured = fiber.get
  def cancel(mayInterrupt: Boolean = true): Boolean % Structured =
    fiber.cancel(mayInterrupt)

def uncancellable[A](fn: () => A): A = {
  val promise = new CompletableFuture[A]()
  Thread
    .ofVirtual()
    .start(() => {
      try promise.complete(fn())
      catch
        case t: Throwable =>
          promise.completeExceptionally(t)
    })
  promise.join
}


