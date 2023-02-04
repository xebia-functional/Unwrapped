package continuations

trait ContinuationInterceptor:
  def interceptContinuation[T](continuation: Continuation[T]): Continuation[T]
  def releaseInterceptedContinuation[T](continuation: Continuation[_]): Unit = ()
