package examples

import continuations.Suspend
import continuations.jvm.internal.SuspendApp

@main def ZeroArgumentsOneContinuationsCF =
  def zeroArgumentsOneContinuationsCF(): Suspend ?=> Int =
    summon[Suspend].shift[Int] { continuation => continuation.resume(1) }
  println(SuspendApp(zeroArgumentsOneContinuationsCF()))
