package examples

import continuations.Suspend

@main def ZeroArgumentsOneContinuationsCF =
  def zeroArgumentsOneContinuationsCF(): Suspend ?=> Int =
    summon[Suspend].suspendContinuation[Int] { continuation => continuation.resume(1) }
  println(zeroArgumentsOneContinuationsCF())
