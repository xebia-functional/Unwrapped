package fx

import java.util.concurrent.StructuredExecutor
import scala.annotation.implicitNotFound
import java.util.concurrent.Callable
import java.util.concurrent.Future
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CancellationException

@implicitNotFound(
  "Structured concurrency requires capability:\n* Structured"
)
opaque type Structured = StructuredExecutor

inline def structured[B](f: B * Structured): B =
  val scope = StructuredExecutor.open("scala fx structured scope")
  given Structured = scope
  try f
  finally
    scope.join
    scope.close()

// inspired on https://github.com/AugustNagro/java-async-await/blob/master/src/main/java/com/augustnagro/jaa/Async.java#L14
// todo refactor to use ScopeLocal when it's no longer a draft JEP.
// https://openjdk.java.net/jeps/8263012
private object Await:
  private[fx] val context: ThreadLocal[Coroutine] = new ThreadLocal()

def uncancellable[A](fn: () => A): () => A = {
  val promise = new CompletableFuture[A]()
  Thread
    .ofVirtual()
    .start(() => {
      try
        Await.context.set(new Coroutine())
        promise.complete(fn())
        Await.context.remove();
      catch
        case t: Throwable =>
          promise.completeExceptionally(t)
    })
  () => promise.join
}

def never: Nothing =
    val promise = CompletableFuture[Nothing]()
    promise.join

def fork[B](f: () => B): Future[B] * Structured =
  summon[Structured].fork(callableOf(f))

def join: Unit * Structured =
  summon[Structured].join

private[fx] inline def callableOf[A](f: () => A): Callable[A] =
  new Callable[A] { def call() = f() }
