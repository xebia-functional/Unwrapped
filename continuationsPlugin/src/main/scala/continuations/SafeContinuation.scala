package continuations

import continuations.jvm.internal.ContinuationStackFrame

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater

class SafeContinuation[T] private (
    val delegate: Continuation[T],
    initialResult: T | Continuation.State)
    extends SafeContinuationBase,
      Continuation[T],
      ContinuationStackFrame:
  override type Ctx = delegate.Ctx
  override def context: Ctx = delegate.context
  result = initialResult
  var errored: Boolean = false

  override def resume(value: T): Unit =
    while true do
      this.result match {
        case Continuation.State.Undecided =>
          if (CAS_RESULT(Continuation.State.Undecided, value))
            return ()
        case Continuation.State.Suspended =>
          if (CAS_RESULT(Continuation.State.Suspended, Continuation.State.Resumed)) {
            delegate.resume(value)
            return ()
          }
        case _ =>
          throw IllegalStateException("Already resumed")
      }

  override def raise(error: Throwable): Unit =
    while true do
      val cur = this.result
      if (cur == Continuation.State.Undecided) {
        if (CAS_RESULT(Continuation.State.Undecided, error))
          return ()
      } else if (cur == Continuation.State.Suspended) {
        if (CAS_RESULT(Continuation.State.Suspended, Continuation.State.Resumed)) {
          errored = true
          delegate.raise(error)
          return ()
        }
      } else throw IllegalStateException("Already resumed")

  def getOrThrow(): T | Null | Continuation.State.Suspended.type =
    var result = this.result

    if (result == Continuation.State.Undecided) {
      if (CAS_RESULT(Continuation.State.Undecided, Continuation.State.Suspended)) {
        return Continuation.State.Suspended
      }
      result = this.result
    }

    if (result == Continuation.State.Resumed) {
      Continuation.State.Suspended
    } else if ((result ne null) && errored) {
      throw result.asInstanceOf[Throwable]
    } else
      result.asInstanceOf[T]

  override def callerFrame: ContinuationStackFrame | Null =
    if (delegate != null && delegate.isInstanceOf[ContinuationStackFrame]) delegate.asInstanceOf
    else null

  override def getStackTraceElement(): StackTraceElement | Null =
    null

object SafeContinuation:
  def init[A](cont: Continuation[A]): SafeContinuation[A] =
    import continuations.intrinsics.intercepted
    val intrinsic = cont.intercepted()
    new SafeContinuation[A](intrinsic, Continuation.State.Undecided)
