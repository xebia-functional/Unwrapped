package unwrapped

import java.util.concurrent.Future
import java.util.concurrent.CompletableFuture

opaque type Fiber[A] = Future[A]

extension [A](fiber: Fiber[A])
  def join: A = fiber.get
  def cancel(mayInterrupt: Boolean = true): Boolean =
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

def fork[B](f: () => B)(using structured: Structured): Fiber[B] =
  structured.forked(callableOf(f))
