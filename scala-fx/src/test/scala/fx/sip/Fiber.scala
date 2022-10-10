package fx.sip

import java.util.concurrent.{CompletableFuture, Future}

opaque type Fiber[A] = Future[A]

extension [A](fiber: Fiber[A])
  def join: A = fiber.get
  def cancel(mayInterrupt: Boolean = true): Boolean =
    fiber.cancel(mayInterrupt)

def fork[B](f: () => B)(using structured: Structured): Fiber[B] =
  structured.forked(callableOf(f))
