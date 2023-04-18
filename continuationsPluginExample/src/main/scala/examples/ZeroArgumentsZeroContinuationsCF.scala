package examples

import continuations.Suspend
import continuations.jvm.internal.SuspendApp

@main def ZeroArgumentsZeroContinuationsCF =
  def zeroArgumentsZeroContinuationsCF(): Suspend ?=> Int = 1
  println(SuspendApp(zeroArgumentsZeroContinuationsCF()))
