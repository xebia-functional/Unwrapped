package fx

import java.util.concurrent.Future
import java.util.concurrent.CompletableFuture

opaque type Fiber[A] = Future[A]

extension [A](fiber: Fiber[A])
  def join: A * Structured = fiber.get
  def cancel(mayInterrupt: Boolean = true): Boolean * Structured =
    fiber.cancel(mayInterrupt)

def uncancellable[A](fn: () => A): () => A = {
  val promise = new CompletableFuture[A]()
  Thread
    .ofVirtual()
    .start(() => {
      try promise.complete(fn())
      catch
        case t: Throwable =>
          promise.completeExceptionally(t)
    })
  () => promise.join
}

def fork[B](f: () => B): Fiber[B] * Structured =
  summon[Structured].forked(callableOf(f))