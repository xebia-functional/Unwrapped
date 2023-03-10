package continuations

import continuations.jvm.internal.ContinuationStackFrame

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater

class SafeContinuation[-T](val delegate: Continuation[T], initialResult: Any | Null)
    extends SafeContinuationBase,
      Continuation[T],
      ContinuationStackFrame:
  override type Ctx = delegate.Ctx
  override def context: Ctx = delegate.context
  result = initialResult

  override def resume(value: Either[Throwable, T]): Unit =
    while true do
      val cur = this.result

      if (cur == Continuation.State.Undecided) {
        if (CAS_RESULT(Continuation.State.Undecided, value))
          return ()
      } else if (cur == Continuation.State.Suspended) {
        if (CAS_RESULT(Continuation.State.Suspended, Continuation.State.Resumed)) {
          delegate.resume(value)
          return ()
        }
      } else {
        throw IllegalStateException("Already resumed")
      }

  def getOrThrow(): Any | Null | Continuation.State.Suspended.type =
    var result = this.result

    if (result == Continuation.State.Undecided) {
      if (CAS_RESULT(Continuation.State.Undecided, Continuation.State.Suspended)) {
        return Continuation.State.Suspended
      }
      result = this.result
    }

    if (result == Continuation.State.Resumed) {
      Continuation.State.Suspended
    } else if (result.isInstanceOf[Left[_, _]]) {
      throw result.asInstanceOf[Left[Throwable, _]].value
    } else if (result.isInstanceOf[Right[_, _]]) {
      result.asInstanceOf[Right[_, _]].value
    } else {
      result // Continuation.State.Suspended
    }

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
