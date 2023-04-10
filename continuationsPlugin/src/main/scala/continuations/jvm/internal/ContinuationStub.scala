package continuations.jvm.internal

import continuations.Continuation.State
import continuations.{Continuation, Suspend}

import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.{CountDownLatch, Executors, ThreadFactory}
import scala.concurrent.ExecutionContext

object ContinuationStub:
  private def c: Continuation[Any | Null] = new Continuation[Any | Null] {
    type Ctx = EmptyTuple

    override val executionContext: ExecutionContext =
      ExecutionContext.global // TODO

    override def resume(value: Any | Null): Unit =
      println("ContinuationStub.resume")

    override def raise(error: Throwable): Unit =
      println("ContinuationStub.raise")

    override def context: Ctx = EmptyTuple
  }

  def contImpl: ContinuationImpl = new ContinuationImpl(c, c.context) {

    protected def invokeSuspend(
        result: Either[Throwable, Any | Null | Continuation.State.Suspended.type]): Any | Null =
      result.fold(t => throw t, or => or)
  }

  def potato: ContinuationImpl = new ContinuationImpl(c, c.context) {
    override def invokeSuspend(result: Either[Throwable, Any | Null | State.Suspended.type]): Any | Null = ???
    def invoke[A](continuation: Continuation[A]): Nothing = ???
  }