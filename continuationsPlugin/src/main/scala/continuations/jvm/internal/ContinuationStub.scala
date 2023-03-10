package continuations.jvm.internal

import continuations.Continuation

object ContinuationStub:
  private def c: Continuation[Any | Null] = new Continuation[Any | Null] {
    type Ctx = EmptyTuple

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
