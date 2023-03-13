package continuations.jvm.internal

import continuations.{Continuation, Suspend}

import java.util.concurrent.CountDownLatch
import scala.concurrent.ExecutionContext

object ContinuationStub:
  private def c: Continuation[Any | Null] = new Continuation[Any | Null] {
    type Ctx = EmptyTuple

    def resume(value: Either[Throwable, Any | Null]): Unit =
      println("ContinuationStub.resume")

    override def context: Ctx = EmptyTuple
  }

  def contImpl: ContinuationImpl = new ContinuationImpl(c, c.context) {
    /*
    def suspendApp(block: Continuation[A]): Unit =
      val pool: ExecutionContext = ExecutionContext.global
      val latch: CountDownLatch = CountDownLatch(1)
      ExecutionContext.global.execute {
        block.resume()
      }

    import continuations.intrinsics.startContinuation
    def startContinuation1[A](block: Continuation[A] => Unit) =
      startContinuation(block)
     */
    protected def invokeSuspend(
        result: Either[Throwable, Any | Null | Continuation.State.Suspended.type]): Any | Null =
      result.fold(t => throw t, or => or)
  }
