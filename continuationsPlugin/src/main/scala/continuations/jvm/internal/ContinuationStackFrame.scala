package continuations.jvm.internal

trait ContinuationStackFrame:
  def callerFrame: ContinuationStackFrame | Null
  def getStackTraceElement(): StackTraceElement | Null
